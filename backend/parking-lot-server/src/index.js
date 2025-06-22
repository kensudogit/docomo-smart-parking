const express = require('express');
const cors = require('cors');
const helmet = require('helmet');
const rateLimit = require('express-rate-limit');
require('dotenv').config();

const authRoutes = require('./routes/auth');
const reservationRoutes = require('./routes/reservation');
const parkingLotRoutes = require('./routes/parkingLot');
const salesRoutes = require('./routes/sales');
const parkingSpaceRoutes = require('./routes/parkingSpace');

const app = express();
const PORT = process.env.PORT || 3001;

// セキュリティミドルウェア
app.use(helmet());
app.use(cors({
  origin: process.env.ALLOWED_ORIGINS?.split(',') || ['http://localhost:3000'],
  credentials: true
}));

// レート制限
const limiter = rateLimit({
  windowMs: 15 * 60 * 1000, // 15分
  max: 100, // 最大100リクエスト
  message: 'Too many requests from this IP'
});
app.use('/api/', limiter);

// ボディパーサー
app.use(express.json({ limit: '10mb' }));
app.use(express.urlencoded({ extended: true }));

// ヘルスチェック
app.get('/health', (req, res) => {
  res.status(200).json({ 
    status: 'OK', 
    service: 'Parking Lot Management Server',
    timestamp: new Date().toISOString()
  });
});

// APIルート
app.use('/api/auth', authRoutes);
app.use('/api/reservations', reservationRoutes);
app.use('/api/parking-lots', parkingLotRoutes);
app.use('/api/sales', salesRoutes);
app.use('/api/parking-spaces', parkingSpaceRoutes);

// エラーハンドリング
app.use((err, req, res, next) => {
  console.error(err.stack);
  res.status(500).json({ 
    error: 'Internal Server Error',
    message: process.env.NODE_ENV === 'development' ? err.message : 'Something went wrong'
  });
});

// 404ハンドリング
app.use('*', (req, res) => {
  res.status(404).json({ error: 'Route not found' });
});

app.listen(PORT, () => {
  console.log(`🚗 Parking Lot Management Server running on port ${PORT}`);
  console.log(`📊 Health check available at http://localhost:${PORT}/health`);
}); 