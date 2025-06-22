const express = require('express');
const { query, validationResult } = require('express-validator');
const ParkingSpace = require('../models/ParkingSpace');
const ParkingLot = require('../models/ParkingLot');

const router = express.Router();

// 駐車場の空き状況一覧取得
router.get('/availability/:parkingLotId', [
  query('status').optional().isIn(['available', 'occupied', 'reserved', 'maintenance']),
  query('features').optional().isString()
], async (req, res) => {
  try {
    const errors = validationResult(req);
    if (!errors.isEmpty()) {
      return res.status(400).json({ errors: errors.array() });
    }

    const { parkingLotId } = req.params;
    const { status, features } = req.query;

    // 駐車場の存在確認
    const parkingLot = await ParkingLot.findById(parkingLotId);
    if (!parkingLot) {
      return res.status(404).json({ error: 'Parking lot not found' });
    }

    // フィルター条件を構築
    const filter = { parkingLot: parkingLotId };
    if (status) filter.status = status;
    if (features) {
      const featureArray = features.split(',');
      featureArray.forEach(feature => {
        filter[`features.is${feature.charAt(0).toUpperCase() + feature.slice(1)}`] = true;
      });
    }

    const parkingSpaces = await ParkingSpace.find(filter)
      .select('spaceNumber status location features sensor.lastSeen')
      .sort('spaceNumber');

    // 統計情報を計算
    const stats = await ParkingSpace.aggregate([
      { $match: { parkingLot: parkingLotId } },
      { $group: {
        _id: '$status',
        count: { $sum: 1 }
      }}
    ]);

    const availabilityStats = {
      total: parkingSpaces.length,
      available: 0,
      occupied: 0,
      reserved: 0,
      maintenance: 0
    };

    stats.forEach(stat => {
      availabilityStats[stat._id] = stat.count;
    });

    res.json({
      parkingLot: {
        id: parkingLot._id,
        name: parkingLot.name,
        address: parkingLot.address
      },
      availability: availabilityStats,
      spaces: parkingSpaces
    });
  } catch (error) {
    console.error('Availability fetch error:', error);
    res.status(500).json({ error: 'Failed to fetch availability' });
  }
});

// 特定の駐車スペース詳細取得
router.get('/:id', async (req, res) => {
  try {
    const parkingSpace = await ParkingSpace.findById(req.params.id)
      .populate('parkingLot', 'name address')
      .populate('currentReservation', 'startTime endTime user');

    if (!parkingSpace) {
      return res.status(404).json({ error: 'Parking space not found' });
    }

    res.json({ parkingSpace });
  } catch (error) {
    console.error('Parking space detail fetch error:', error);
    res.status(500).json({ error: 'Failed to fetch parking space details' });
  }
});

// 駐車スペースのリアルタイム状態取得
router.get('/:id/status', async (req, res) => {
  try {
    const parkingSpace = await ParkingSpace.findById(req.params.id)
      .select('spaceNumber status sensor features location');

    if (!parkingSpace) {
      return res.status(404).json({ error: 'Parking space not found' });
    }

    const isHealthy = parkingSpace.isSensorHealthy();
    const isAvailable = parkingSpace.isAvailable();

    res.json({
      spaceId: parkingSpace._id,
      spaceNumber: parkingSpace.spaceNumber,
      status: parkingSpace.status,
      isAvailable,
      isHealthy,
      sensorData: {
        batteryLevel: parkingSpace.sensor.batteryLevel,
        signalStrength: parkingSpace.sensor.signalStrength,
        lastSeen: parkingSpace.sensor.lastSeen
      },
      features: parkingSpace.features,
      location: parkingSpace.location,
      timestamp: new Date().toISOString()
    });
  } catch (error) {
    console.error('Status fetch error:', error);
    res.status(500).json({ error: 'Failed to fetch status' });
  }
});

// 駐車スペースの使用履歴取得
router.get('/:id/history', [
  query('startDate').optional().isISO8601(),
  query('endDate').optional().isISO8601(),
  query('limit').optional().isInt({ min: 1, max: 100 })
], async (req, res) => {
  try {
    const errors = validationResult(req);
    if (!errors.isEmpty()) {
      return res.status(400).json({ errors: errors.array() });
    }

    const { id } = req.params;
    const { startDate, endDate, limit = 50 } = req.query;

    const parkingSpace = await ParkingSpace.findById(id);
    if (!parkingSpace) {
      return res.status(404).json({ error: 'Parking space not found' });
    }

    // 履歴フィルター
    let historyFilter = {};
    if (startDate || endDate) {
      historyFilter.timestamp = {};
      if (startDate) historyFilter.timestamp.$gte = new Date(startDate);
      if (endDate) historyFilter.timestamp.$lte = new Date(endDate);
    }

    const history = parkingSpace.occupancyHistory
      .filter(entry => {
        if (startDate && entry.timestamp < new Date(startDate)) return false;
        if (endDate && entry.timestamp > new Date(endDate)) return false;
        return true;
      })
      .sort((a, b) => b.timestamp - a.timestamp)
      .slice(0, parseInt(limit));

    res.json({
      spaceId: parkingSpace._id,
      spaceNumber: parkingSpace.spaceNumber,
      history,
      statistics: parkingSpace.statistics
    });
  } catch (error) {
    console.error('History fetch error:', error);
    res.status(500).json({ error: 'Failed to fetch history' });
  }
});

// メンテナンス問題一覧取得
router.get('/:id/maintenance', async (req, res) => {
  try {
    const parkingSpace = await ParkingSpace.findById(req.params.id)
      .select('spaceNumber maintenance');

    if (!parkingSpace) {
      return res.status(404).json({ error: 'Parking space not found' });
    }

    const activeIssues = parkingSpace.maintenance.issues.filter(issue => !issue.resolvedAt);
    const resolvedIssues = parkingSpace.maintenance.issues.filter(issue => issue.resolvedAt);

    res.json({
      spaceId: parkingSpace._id,
      spaceNumber: parkingSpace.spaceNumber,
      activeIssues,
      resolvedIssues,
      lastMaintenance: parkingSpace.maintenance.lastMaintenance,
      nextMaintenance: parkingSpace.maintenance.nextMaintenance
    });
  } catch (error) {
    console.error('Maintenance fetch error:', error);
    res.status(500).json({ error: 'Failed to fetch maintenance information' });
  }
});

// メンテナンス問題を追加
router.post('/:id/maintenance', [
  query('type').isIn(['sensor_fault', 'physical_damage', 'electrical_issue', 'other']),
  query('description').notEmpty().trim(),
  query('priority').optional().isIn(['low', 'medium', 'high', 'critical'])
], async (req, res) => {
  try {
    const errors = validationResult(req);
    if (!errors.isEmpty()) {
      return res.status(400).json({ errors: errors.array() });
    }

    const { id } = req.params;
    const { type, description, priority = 'medium' } = req.query;

    const parkingSpace = await ParkingSpace.findById(id);
    if (!parkingSpace) {
      return res.status(404).json({ error: 'Parking space not found' });
    }

    parkingSpace.addMaintenanceIssue({
      type,
      description,
      priority
    });

    await parkingSpace.save();

    res.json({
      message: 'Maintenance issue added successfully',
      issue: parkingSpace.maintenance.issues[parkingSpace.maintenance.issues.length - 1]
    });
  } catch (error) {
    console.error('Maintenance issue creation error:', error);
    res.status(500).json({ error: 'Failed to add maintenance issue' });
  }
});

// メンテナンス問題を解決
router.put('/:id/maintenance/:issueIndex/resolve', async (req, res) => {
  try {
    const { id, issueIndex } = req.params;

    const parkingSpace = await ParkingSpace.findById(id);
    if (!parkingSpace) {
      return res.status(404).json({ error: 'Parking space not found' });
    }

    if (!parkingSpace.maintenance.issues[issueIndex]) {
      return res.status(404).json({ error: 'Maintenance issue not found' });
    }

    parkingSpace.resolveMaintenanceIssue(parseInt(issueIndex));
    await parkingSpace.save();

    res.json({
      message: 'Maintenance issue resolved successfully',
      parkingSpace: {
        id: parkingSpace._id,
        spaceNumber: parkingSpace.spaceNumber,
        status: parkingSpace.status,
        maintenance: parkingSpace.maintenance
      }
    });
  } catch (error) {
    console.error('Maintenance issue resolution error:', error);
    res.status(500).json({ error: 'Failed to resolve maintenance issue' });
  }
});

// センサー情報取得
router.get('/:id/sensor', async (req, res) => {
  try {
    const parkingSpace = await ParkingSpace.findById(req.params.id)
      .select('spaceNumber sensor');

    if (!parkingSpace) {
      return res.status(404).json({ error: 'Parking space not found' });
    }

    const isHealthy = parkingSpace.isSensorHealthy();

    res.json({
      spaceId: parkingSpace._id,
      spaceNumber: parkingSpace.spaceNumber,
      sensor: parkingSpace.sensor,
      isHealthy,
      lastSeen: parkingSpace.sensor.lastSeen,
      timestamp: new Date().toISOString()
    });
  } catch (error) {
    console.error('Sensor info fetch error:', error);
    res.status(500).json({ error: 'Failed to fetch sensor information' });
  }
});

// 複数駐車スペースの一括状態取得
router.post('/batch-status', [
  query('spaceIds').isArray()
], async (req, res) => {
  try {
    const errors = validationResult(req);
    if (!errors.isEmpty()) {
      return res.status(400).json({ errors: errors.array() });
    }

    const { spaceIds } = req.query;

    const parkingSpaces = await ParkingSpace.find({
      _id: { $in: spaceIds }
    }).select('spaceNumber status sensor.lastSeen features');

    const statusData = parkingSpaces.map(space => ({
      spaceId: space._id,
      spaceNumber: space.spaceNumber,
      status: space.status,
      isHealthy: space.isSensorHealthy(),
      lastSeen: space.sensor.lastSeen,
      features: space.features
    }));

    res.json({
      spaces: statusData,
      timestamp: new Date().toISOString()
    });
  } catch (error) {
    console.error('Batch status fetch error:', error);
    res.status(500).json({ error: 'Failed to fetch batch status' });
  }
});

module.exports = router; 