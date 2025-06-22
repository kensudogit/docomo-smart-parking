const mongoose = require('mongoose');

const parkingLotSchema = new mongoose.Schema({
  name: {
    type: String,
    required: true,
    trim: true
  },
  address: {
    street: { type: String, required: true },
    city: { type: String, required: true },
    prefecture: { type: String, required: true },
    postalCode: { type: String, required: true },
    coordinates: {
      latitude: { type: Number, required: true },
      longitude: { type: Number, required: true }
    }
  },
  capacity: {
    total: { type: Number, required: true },
    reserved: { type: Number, default: 0 },
    available: { type: Number, required: true }
  },
  pricing: {
    hourly: { type: Number, required: true },
    daily: { type: Number, required: true },
    monthly: { type: Number },
    reservationFee: { type: Number, default: 0 }
  },
  operatingHours: {
    open: { type: String, required: true }, // "HH:MM" format
    close: { type: String, required: true }, // "HH:MM" format
    is24Hours: { type: Boolean, default: false }
  },
  features: {
    hasSecurity: { type: Boolean, default: false },
    hasCovered: { type: Boolean, default: false },
    hasEVCharging: { type: Boolean, default: false },
    hasWheelchairAccess: { type: Boolean, default: false },
    hasBikeParking: { type: Boolean, default: false }
  },
  status: {
    type: String,
    enum: ['active', 'maintenance', 'closed'],
    default: 'active'
  },
  contactInfo: {
    phone: { type: String },
    email: { type: String },
    emergencyContact: { type: String }
  },
  images: [{
    url: { type: String },
    caption: { type: String },
    isPrimary: { type: Boolean, default: false }
  }],
  rules: [{
    title: { type: String },
    description: { type: String }
  }]
}, {
  timestamps: true
});

// 空き状況を計算するメソッド
parkingLotSchema.methods.getAvailability = function() {
  return {
    total: this.capacity.total,
    available: this.capacity.available,
    reserved: this.capacity.reserved,
    occupancyRate: ((this.capacity.total - this.capacity.available) / this.capacity.total * 100).toFixed(2)
  };
};

// 駐車場が営業中かチェック
parkingLotSchema.methods.isOpen = function() {
  if (this.operatingHours.is24Hours) return true;
  
  const now = new Date();
  const currentTime = now.getHours() * 60 + now.getMinutes();
  const openTime = this.operatingHours.open.split(':').map(Number);
  const closeTime = this.operatingHours.close.split(':').map(Number);
  const openMinutes = openTime[0] * 60 + openTime[1];
  const closeMinutes = closeTime[0] * 60 + closeTime[1];
  
  return currentTime >= openMinutes && currentTime <= closeMinutes;
};

module.exports = mongoose.model('ParkingLot', parkingLotSchema); 