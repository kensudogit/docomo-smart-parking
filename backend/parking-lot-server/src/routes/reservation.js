const express = require('express');
const { body, validationResult, query } = require('express-validator');
const Reservation = require('../models/Reservation');
const ParkingLot = require('../models/ParkingLot');
const { auth, requireOperator } = require('../middleware/auth');
const moment = require('moment');

const router = express.Router();

// 予約作成
router.post('/', auth, [
  body('parkingLotId').isMongoId(),
  body('startTime').isISO8601(),
  body('endTime').isISO8601(),
  body('vehicle.licensePlate').notEmpty().trim(),
  body('payment.method').isIn(['credit_card', 'debit_card', 'mobile_payment', 'cash'])
], async (req, res) => {
  try {
    const errors = validationResult(req);
    if (!errors.isEmpty()) {
      return res.status(400).json({ errors: errors.array() });
    }

    const {
      parkingLotId,
      parkingSpaceId,
      startTime,
      endTime,
      vehicle,
      specialRequests,
      notes
    } = req.body;

    // 駐車場存在チェック
    const parkingLot = await ParkingLot.findById(parkingLotId);
    if (!parkingLot) {
      return res.status(404).json({ error: 'Parking lot not found' });
    }

    // 営業時間チェック
    if (!parkingLot.isOpen()) {
      return res.status(400).json({ error: 'Parking lot is currently closed' });
    }

    // 予約時間の妥当性チェック
    const start = new Date(startTime);
    const end = new Date(endTime);
    const now = new Date();

    if (start <= now) {
      return res.status(400).json({ error: 'Start time must be in the future' });
    }

    if (end <= start) {
      return res.status(400).json({ error: 'End time must be after start time' });
    }

    // 重複予約チェック
    const conflictingReservation = await Reservation.findOne({
      parkingLot: parkingLotId,
      parkingSpace: parkingSpaceId,
      status: { $in: ['pending', 'confirmed', 'active'] },
      $or: [
        { startTime: { $lt: end, $gte: start } },
        { endTime: { $gt: start, $lte: end } },
        { startTime: { $lte: start }, endTime: { $gte: end } }
      ]
    });

    if (conflictingReservation) {
      return res.status(409).json({ error: 'Time slot already reserved' });
    }

    // 料金計算
    const duration = Math.ceil((end - start) / (1000 * 60 * 60)); // 時間単位
    const hourlyRate = parkingLot.pricing.hourly;
    const amount = duration * hourlyRate + parkingLot.pricing.reservationFee;

    // 予約作成
    const reservation = new Reservation({
      user: req.user.userId,
      parkingLot: parkingLotId,
      parkingSpace: parkingSpaceId,
      startTime: start,
      endTime: end,
      vehicle,
      specialRequests,
      notes,
      payment: {
        method: req.body.payment.method,
        amount,
        currency: 'JPY'
      }
    });

    await reservation.save();

    // 駐車場の予約数を更新
    await ParkingLot.findByIdAndUpdate(parkingLotId, {
      $inc: { 'capacity.reserved': 1 }
    });

    res.status(201).json({
      message: 'Reservation created successfully',
      reservation: await reservation.populate(['parkingLot', 'parkingSpace'])
    });
  } catch (error) {
    console.error('Reservation creation error:', error);
    res.status(500).json({ error: 'Failed to create reservation' });
  }
});

// ユーザーの予約一覧取得
router.get('/my-reservations', auth, [
  query('status').optional().isIn(['pending', 'confirmed', 'active', 'completed', 'cancelled']),
  query('page').optional().isInt({ min: 1 }),
  query('limit').optional().isInt({ min: 1, max: 50 })
], async (req, res) => {
  try {
    const errors = validationResult(req);
    if (!errors.isEmpty()) {
      return res.status(400).json({ errors: errors.array() });
    }

    const { status, page = 1, limit = 10 } = req.query;
    const skip = (page - 1) * limit;

    const filter = { user: req.user.userId };
    if (status) filter.status = status;

    const reservations = await Reservation.find(filter)
      .populate('parkingLot', 'name address')
      .populate('parkingSpace', 'spaceNumber')
      .sort({ createdAt: -1 })
      .skip(skip)
      .limit(parseInt(limit));

    const total = await Reservation.countDocuments(filter);

    res.json({
      reservations,
      pagination: {
        page: parseInt(page),
        limit: parseInt(limit),
        total,
        pages: Math.ceil(total / limit)
      }
    });
  } catch (error) {
    console.error('Reservation fetch error:', error);
    res.status(500).json({ error: 'Failed to fetch reservations' });
  }
});

// 特定の予約詳細取得
router.get('/:id', auth, async (req, res) => {
  try {
    const reservation = await Reservation.findById(req.params.id)
      .populate('parkingLot')
      .populate('parkingSpace')
      .populate('user', 'firstName lastName email phoneNumber');

    if (!reservation) {
      return res.status(404).json({ error: 'Reservation not found' });
    }

    // 自分の予約または管理者のみアクセス可能
    if (reservation.user._id.toString() !== req.user.userId && req.user.userType !== 'admin') {
      return res.status(403).json({ error: 'Access denied' });
    }

    res.json({ reservation });
  } catch (error) {
    console.error('Reservation detail fetch error:', error);
    res.status(500).json({ error: 'Failed to fetch reservation details' });
  }
});

// 予約キャンセル
router.put('/:id/cancel', auth, [
  body('reason').optional().trim()
], async (req, res) => {
  try {
    const reservation = await Reservation.findById(req.params.id);
    if (!reservation) {
      return res.status(404).json({ error: 'Reservation not found' });
    }

    // 自分の予約または管理者のみキャンセル可能
    if (reservation.user.toString() !== req.user.userId && req.user.userType !== 'admin') {
      return res.status(403).json({ error: 'Access denied' });
    }

    // キャンセル可能な状態かチェック
    if (!['pending', 'confirmed'].includes(reservation.status)) {
      return res.status(400).json({ error: 'Reservation cannot be cancelled' });
    }

    // キャンセル処理
    reservation.status = 'cancelled';
    reservation.cancellationReason = req.body.reason;
    reservation.cancelledBy = req.user.userType === 'admin' ? 'admin' : 'user';

    // 返金処理（支払い済みの場合）
    if (reservation.payment.status === 'paid') {
      const refundAmount = calculateRefundAmount(reservation);
      reservation.refundAmount = refundAmount;
      // 実際の返金処理は決済システムと連携
    }

    await reservation.save();

    // 駐車場の予約数を更新
    await ParkingLot.findByIdAndUpdate(reservation.parkingLot, {
      $inc: { 'capacity.reserved': -1 }
    });

    res.json({
      message: 'Reservation cancelled successfully',
      reservation
    });
  } catch (error) {
    console.error('Reservation cancellation error:', error);
    res.status(500).json({ error: 'Failed to cancel reservation' });
  }
});

// チェックイン
router.put('/:id/checkin', auth, async (req, res) => {
  try {
    const reservation = await Reservation.findById(req.params.id);
    if (!reservation) {
      return res.status(404).json({ error: 'Reservation not found' });
    }

    // チェックイン可能かチェック
    if (!reservation.canCheckIn()) {
      return res.status(400).json({ error: 'Cannot check in at this time' });
    }

    reservation.status = 'active';
    reservation.checkInTime = new Date();
    await reservation.save();

    res.json({
      message: 'Check-in successful',
      reservation
    });
  } catch (error) {
    console.error('Check-in error:', error);
    res.status(500).json({ error: 'Failed to check in' });
  }
});

// チェックアウト
router.put('/:id/checkout', auth, async (req, res) => {
  try {
    const reservation = await Reservation.findById(req.params.id);
    if (!reservation) {
      return res.status(404).json({ error: 'Reservation not found' });
    }

    // チェックアウト可能かチェック
    if (!reservation.canCheckOut()) {
      return res.status(400).json({ error: 'Cannot check out at this time' });
    }

    const checkOutTime = new Date();
    const actualDuration = Math.ceil((checkOutTime - reservation.checkInTime) / (1000 * 60)); // 分単位

    reservation.status = 'completed';
    reservation.checkOutTime = checkOutTime;
    reservation.actualDuration = actualDuration;
    
    // 実際の料金計算
    const parkingLot = await ParkingLot.findById(reservation.parkingLot);
    const actualAmount = Math.ceil(actualDuration / 60 * parkingLot.pricing.hourly);
    reservation.actualAmount = actualAmount;

    await reservation.save();

    // 駐車場の空き状況を更新
    await ParkingLot.findByIdAndUpdate(reservation.parkingLot, {
      $inc: { 'capacity.available': 1, 'capacity.reserved': -1 }
    });

    res.json({
      message: 'Check-out successful',
      reservation
    });
  } catch (error) {
    console.error('Check-out error:', error);
    res.status(500).json({ error: 'Failed to check out' });
  }
});

// 管理者用：全予約一覧取得
router.get('/', auth, requireOperator, [
  query('status').optional().isIn(['pending', 'confirmed', 'active', 'completed', 'cancelled']),
  query('parkingLotId').optional().isMongoId(),
  query('page').optional().isInt({ min: 1 }),
  query('limit').optional().isInt({ min: 1, max: 100 })
], async (req, res) => {
  try {
    const errors = validationResult(req);
    if (!errors.isEmpty()) {
      return res.status(400).json({ errors: errors.array() });
    }

    const { status, parkingLotId, page = 1, limit = 20 } = req.query;
    const skip = (page - 1) * limit;

    const filter = {};
    if (status) filter.status = status;
    if (parkingLotId) filter.parkingLot = parkingLotId;

    const reservations = await Reservation.find(filter)
      .populate('parkingLot', 'name address')
      .populate('parkingSpace', 'spaceNumber')
      .populate('user', 'firstName lastName email')
      .sort({ createdAt: -1 })
      .skip(skip)
      .limit(parseInt(limit));

    const total = await Reservation.countDocuments(filter);

    res.json({
      reservations,
      pagination: {
        page: parseInt(page),
        limit: parseInt(limit),
        total,
        pages: Math.ceil(total / limit)
      }
    });
  } catch (error) {
    console.error('Admin reservation fetch error:', error);
    res.status(500).json({ error: 'Failed to fetch reservations' });
  }
});

// 返金金額計算ヘルパー関数
function calculateRefundAmount(reservation) {
  const now = new Date();
  const startTime = new Date(reservation.startTime);
  
  // 開始時刻の24時間前まで：全額返金
  if (now < startTime - 24 * 60 * 60 * 1000) {
    return reservation.payment.amount;
  }
  
  // 開始時刻の1時間前まで：50%返金
  if (now < startTime - 60 * 60 * 1000) {
    return Math.floor(reservation.payment.amount * 0.5);
  }
  
  // それ以降：返金なし
  return 0;
}

module.exports = router; 