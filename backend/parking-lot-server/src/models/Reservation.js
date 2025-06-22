const mongoose = require('mongoose');

const reservationSchema = new mongoose.Schema({
  user: {
    type: mongoose.Schema.Types.ObjectId,
    ref: 'User',
    required: true
  },
  parkingLot: {
    type: mongoose.Schema.Types.ObjectId,
    ref: 'ParkingLot',
    required: true
  },
  parkingSpace: {
    type: mongoose.Schema.Types.ObjectId,
    ref: 'ParkingSpace'
  },
  startTime: {
    type: Date,
    required: true
  },
  endTime: {
    type: Date,
    required: true
  },
  status: {
    type: String,
    enum: ['pending', 'confirmed', 'active', 'completed', 'cancelled', 'expired'],
    default: 'pending'
  },
  payment: {
    method: {
      type: String,
      enum: ['credit_card', 'debit_card', 'mobile_payment', 'cash'],
      required: true
    },
    amount: {
      type: Number,
      required: true
    },
    currency: {
      type: String,
      default: 'JPY'
    },
    status: {
      type: String,
      enum: ['pending', 'paid', 'failed', 'refunded'],
      default: 'pending'
    },
    transactionId: {
      type: String
    },
    paidAt: {
      type: Date
    }
  },
  vehicle: {
    licensePlate: {
      type: String,
      required: true
    },
    make: {
      type: String
    },
    model: {
      type: String
    },
    color: {
      type: String
    }
  },
  specialRequests: [{
    type: String
  }],
  notes: {
    type: String
  },
  checkInTime: {
    type: Date
  },
  checkOutTime: {
    type: Date
  },
  actualDuration: {
    type: Number // 分単位
  },
  actualAmount: {
    type: Number
  },
  cancellationReason: {
    type: String
  },
  cancelledBy: {
    type: String,
    enum: ['user', 'system', 'admin']
  },
  refundAmount: {
    type: Number,
    default: 0
  }
}, {
  timestamps: true
});

// 予約期間の重複チェック
reservationSchema.index({ 
  parkingLot: 1, 
  parkingSpace: 1, 
  startTime: 1, 
  endTime: 1 
});

// 予約期間を計算するメソッド
reservationSchema.methods.getDuration = function() {
  return Math.ceil((this.endTime - this.startTime) / (1000 * 60)); // 分単位
};

// 予約が有効かチェック
reservationSchema.methods.isValid = function() {
  const now = new Date();
  return this.status === 'confirmed' && 
         this.startTime <= now && 
         this.endTime >= now;
};

// 予約が開始可能かチェック
reservationSchema.methods.canCheckIn = function() {
  const now = new Date();
  const bufferTime = 15 * 60 * 1000; // 15分前からチェックイン可能
  return this.status === 'confirmed' && 
         this.startTime - bufferTime <= now && 
         this.endTime >= now &&
         !this.checkInTime;
};

// 予約が終了可能かチェック
reservationSchema.methods.canCheckOut = function() {
  return this.status === 'active' && 
         this.checkInTime && 
         !this.checkOutTime;
};

module.exports = mongoose.model('Reservation', reservationSchema); 