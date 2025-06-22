const ParkingSpace = require('../models/ParkingSpace');
const ParkingLot = require('../models/ParkingLot');
const moment = require('moment');

// MQTTトピックからセンサーIDを抽出
function extractSensorIdFromTopic(topic) {
  const match = topic.match(/parking\/sensor\/(.+)\/data/);
  return match ? match[1] : null;
}

// センサーデータを処理
async function processSensorData(topic, data, io) {
  try {
    const sensorId = extractSensorIdFromTopic(topic);
    if (!sensorId) {
      console.error('Invalid sensor topic format:', topic);
      return;
    }

    // センサーIDに対応する駐車スペースを検索
    const parkingSpace = await ParkingSpace.findOne({ 'sensor.sensorId': sensorId });
    if (!parkingSpace) {
      console.error('Parking space not found for sensor:', sensorId);
      return;
    }

    // センサー情報を更新
    parkingSpace.updateSensorData({
      batteryLevel: data.batteryLevel,
      signalStrength: data.signalStrength
    });

    // 車両検知データを処理
    if (data.occupancy !== undefined) {
      const previousStatus = parkingSpace.status;
      const newStatus = data.occupancy ? 'occupied' : 'available';
      
      // ステータスが変更された場合のみ処理
      if (previousStatus !== newStatus) {
        parkingSpace.updateOccupancy(newStatus);
        
        // 駐車場の空き状況を更新
        await updateParkingLotAvailability(parkingSpace.parkingLot);
        
        // リアルタイム通知を送信
        sendRealTimeUpdate(io, parkingSpace, newStatus);
        
        console.log(`Parking space ${parkingSpace.spaceNumber} status changed: ${previousStatus} -> ${newStatus}`);
      }
    }

    await parkingSpace.save();

    // センサーの健全性チェック
    if (!parkingSpace.isSensorHealthy()) {
      console.warn(`Sensor ${sensorId} health check failed`);
      // 必要に応じてアラートを送信
    }

  } catch (error) {
    console.error('Error processing sensor data:', error);
  }
}

// 駐車場の空き状況を更新
async function updateParkingLotAvailability(parkingLotId) {
  try {
    const parkingLot = await ParkingLot.findById(parkingLotId);
    if (!parkingLot) return;

    // 各ステータスの駐車スペース数を集計
    const spaceCounts = await ParkingSpace.aggregate([
      { $match: { parkingLot: parkingLotId } },
      { $group: {
        _id: '$status',
        count: { $sum: 1 }
      }}
    ]);

    let available = 0;
    let occupied = 0;
    let reserved = 0;

    spaceCounts.forEach(item => {
      switch (item._id) {
        case 'available':
          available = item.count;
          break;
        case 'occupied':
          occupied = item.count;
          break;
        case 'reserved':
          reserved = item.count;
          break;
      }
    });

    // 駐車場の容量情報を更新
    parkingLot.capacity.available = available;
    parkingLot.capacity.reserved = reserved;
    parkingLot.capacity.total = available + occupied + reserved;

    await parkingLot.save();

  } catch (error) {
    console.error('Error updating parking lot availability:', error);
  }
}

// リアルタイム更新を送信
function sendRealTimeUpdate(io, parkingSpace, status) {
  try {
    // 駐車場全体の更新を送信
    io.to(`parking-lot-${parkingSpace.parkingLot}`).emit('parking-space-update', {
      spaceId: parkingSpace._id,
      spaceNumber: parkingSpace.spaceNumber,
      status,
      timestamp: new Date().toISOString()
    });

    // 特定の駐車スペースの更新を送信
    io.to(`parking-space-${parkingSpace._id}`).emit('space-status-update', {
      spaceId: parkingSpace._id,
      status,
      timestamp: new Date().toISOString(),
      sensorData: {
        batteryLevel: parkingSpace.sensor.batteryLevel,
        signalStrength: parkingSpace.sensor.signalStrength,
        lastSeen: parkingSpace.sensor.lastSeen
      }
    });

  } catch (error) {
    console.error('Error sending real-time update:', error);
  }
}

// センサーの健全性チェック
async function checkSensorHealth() {
  try {
    const now = new Date();
    const threshold = 15 * 60 * 1000; // 15分

    // 長時間データを受信していないセンサーを検出
    const unhealthySensors = await ParkingSpace.find({
      'sensor.lastSeen': { $lt: new Date(now - threshold) }
    });

    for (const space of unhealthySensors) {
      console.warn(`Sensor ${space.sensor.sensorId} has not reported for ${Math.floor((now - space.sensor.lastSeen) / 60000)} minutes`);
      
      // 必要に応じてメンテナンス問題を追加
      if (!space.maintenance.issues.some(issue => 
        issue.type === 'sensor_fault' && !issue.resolvedAt
      )) {
        space.addMaintenanceIssue({
          type: 'sensor_fault',
          description: 'Sensor not responding',
          priority: 'high'
        });
        await space.save();
      }
    }

    // バッテリー残量が低いセンサーを検出
    const lowBatterySensors = await ParkingSpace.find({
      'sensor.batteryLevel': { $lt: 20 }
    });

    for (const space of lowBatterySensors) {
      console.warn(`Sensor ${space.sensor.sensorId} has low battery: ${space.sensor.batteryLevel}%`);
    }

  } catch (error) {
    console.error('Error checking sensor health:', error);
  }
}

// 駐車スペースの統計情報を更新
async function updateSpaceStatistics(spaceId, duration, revenue) {
  try {
    const space = await ParkingSpace.findById(spaceId);
    if (!space) return;

    // 平均使用時間を更新
    const currentAvg = space.statistics.averageOccupancyTime;
    const totalUsage = space.statistics.totalUsage;
    
    if (totalUsage > 0) {
      space.statistics.averageOccupancyTime = 
        (currentAvg * (totalUsage - 1) + duration) / totalUsage;
    }

    // 収益を更新
    space.statistics.revenue += revenue;

    await space.save();

  } catch (error) {
    console.error('Error updating space statistics:', error);
  }
}

// バッチ処理でセンサーデータを一括更新
async function batchUpdateSensorData(sensorDataArray) {
  try {
    const bulkOps = [];

    for (const data of sensorDataArray) {
      const { sensorId, occupancy, batteryLevel, signalStrength } = data;
      
      bulkOps.push({
        updateOne: {
          filter: { 'sensor.sensorId': sensorId },
          update: {
            $set: {
              'sensor.lastSeen': new Date(),
              'sensor.batteryLevel': batteryLevel,
              'sensor.signalStrength': signalStrength,
              status: occupancy ? 'occupied' : 'available'
            }
          }
        }
      });
    }

    if (bulkOps.length > 0) {
      await ParkingSpace.bulkWrite(bulkOps);
      console.log(`Batch updated ${bulkOps.length} sensor records`);
    }

  } catch (error) {
    console.error('Error in batch sensor update:', error);
  }
}

// センサーキャリブレーション
async function calibrateSensor(sensorId, calibrationData) {
  try {
    const space = await ParkingSpace.findOne({ 'sensor.sensorId': sensorId });
    if (!space) {
      throw new Error('Sensor not found');
    }

    // キャリブレーションデータを処理
    // 実際の実装では、センサーの特性に応じた調整を行う
    console.log(`Calibrating sensor ${sensorId} with data:`, calibrationData);

    // キャリブレーション完了後、センサーを再アクティブ化
    space.status = 'available';
    await space.save();

    return { success: true, message: 'Sensor calibrated successfully' };

  } catch (error) {
    console.error('Error calibrating sensor:', error);
    throw error;
  }
}

module.exports = {
  processSensorData,
  updateParkingLotAvailability,
  sendRealTimeUpdate,
  checkSensorHealth,
  updateSpaceStatistics,
  batchUpdateSensorData,
  calibrateSensor
}; 