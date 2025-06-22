const mongoose = require('mongoose');

const parkingSpaceSchema = new mongoose.Schema({
  spaceNumber: {
    type: String,
    required: true,
    trim: true
  },
  parkingLot: {
    type: mongoose.Schema.Types.ObjectId,
    ref: 'ParkingLot',
    required: true
  },
  location: {
    floor: {
      type: Number,
      default: 1
    },
    section: {
      type: String,
      trim: true
    },
    row: {
      type: String,
      trim: true
    },
    coordinates: {
      x: { type: Number },
      y: { type: Number }
    }
  },
  status: {
    type: String,
    enum: ['available', 'occupied', 'reserved', 'maintenance', 'out_of_service'],
    default: 'available'
  },
  sensor: {
    sensorId: {
      type: String,
      required: true,
      unique: true
    },
    type: {
      type: String,
      enum: ['ultrasonic', 'infrared', 'magnetic', 'camera'],
      required: true
    },
    lastSeen: {
      type: Date,
      default: Date.now
    },
    batteryLevel: {
      type: Number,
      min: 0,
      max: 100
    },
    signalStrength: {
      type: Number,
      min: -100,
      max: 0
    }
  },
  features: {
    isEVCharging: {
      type: Boolean,
      default: false
    },
    isWheelchairAccessible: {
      type: Boolean,
      default: false
    },
    isCovered: {
      type: Boolean,
      default: false
    },
    isLargeVehicle: {
      type: Boolean,
      default: false
    }
  },
  currentReservation: {
    type: mongoose.Schema.Types.ObjectId,
    ref: 'Reservation'
  },
  occupancyHistory: [{
    timestamp: {
      type: Date,
      default: Date.now
    },
    status: {
      type: String,
      enum: ['available', 'occupied', 'reserved']
    },
    duration: {
      type: Number // 分単位
    }
  }],
  maintenance: {
    lastMaintenance: {
      type: Date
    },
    nextMaintenance: {
      type: Date
    },
    issues: [{
      type: {
        type: String,
        enum: ['sensor_fault', 'physical_damage', 'electrical_issue', 'other']
      },
      description: {
        type: String
      },
      reportedAt: {
        type: Date,
        default: Date.now
      },
      resolvedAt: {
        type: Date
      },
      priority: {
        type: String,
        enum: ['low', 'medium', 'high', 'critical'],
        default: 'medium'
      }
    }]
  },
  statistics: {
    totalUsage: {
      type: Number,
      default: 0 // 使用回数
    },
    averageOccupancyTime: {
      type: Number,
      default: 0 // 平均使用時間（分）
    },
    revenue: {
      type: Number,
      default: 0 // 収益（円）
    }
  }
}, {
  timestamps: true
});

// インデックス設定
parkingSpaceSchema.index({ parkingLot: 1, spaceNumber: 1 }, { unique: true });
parkingSpaceSchema.index({ 'sensor.sensorId': 1 });
parkingSpaceSchema.index({ status: 1 });
parkingSpaceSchema.index({ 'sensor.lastSeen': 1 });

// 駐車スペースが利用可能かチェック
parkingSpaceSchema.methods.isAvailable = function() {
  return this.status === 'available' && 
         this.maintenance.issues.every(issue => issue.resolvedAt) &&
         this.sensor.lastSeen > new Date(Date.now() - 30 * 60 * 1000); // 30分以内にセンサーからデータを受信
};

// センサーの健全性チェック
parkingSpaceSchema.methods.isSensorHealthy = function() {
  const now = new Date();
  const lastSeenThreshold = 10 * 60 * 1000; // 10分
  
  return this.sensor.lastSeen > new Date(now - lastSeenThreshold) &&
         this.sensor.batteryLevel > 10 &&
         this.sensor.signalStrength > -80;
};

// 使用状況を更新
parkingSpaceSchema.methods.updateOccupancy = function(status, duration = null) {
  this.status = status;
  
  if (duration !== null) {
    this.occupancyHistory.push({
      timestamp: new Date(),
      status,
      duration
    });
    
    // 履歴は最新100件まで保持
    if (this.occupancyHistory.length > 100) {
      this.occupancyHistory = this.occupancyHistory.slice(-100);
    }
  }
  
  // 統計情報を更新
  if (status === 'occupied') {
    this.statistics.totalUsage += 1;
  }
};

// センサー情報を更新
parkingSpaceSchema.methods.updateSensorData = function(sensorData) {
  this.sensor.lastSeen = new Date();
  
  if (sensorData.batteryLevel !== undefined) {
    this.sensor.batteryLevel = sensorData.batteryLevel;
  }
  
  if (sensorData.signalStrength !== undefined) {
    this.sensor.signalStrength = sensorData.signalStrength;
  }
};

// メンテナンス問題を追加
parkingSpaceSchema.methods.addMaintenanceIssue = function(issue) {
  this.maintenance.issues.push({
    type: issue.type,
    description: issue.description,
    priority: issue.priority || 'medium'
  });
  
  // 重要な問題がある場合はステータスを変更
  if (issue.priority === 'critical') {
    this.status = 'maintenance';
  }
};

// メンテナンス問題を解決
parkingSpaceSchema.methods.resolveMaintenanceIssue = function(issueIndex) {
  if (this.maintenance.issues[issueIndex]) {
    this.maintenance.issues[issueIndex].resolvedAt = new Date();
    
    // すべての問題が解決された場合はステータスを復旧
    if (this.maintenance.issues.every(issue => issue.resolvedAt)) {
      this.status = 'available';
    }
  }
};

module.exports = mongoose.model('ParkingSpace', parkingSpaceSchema); 