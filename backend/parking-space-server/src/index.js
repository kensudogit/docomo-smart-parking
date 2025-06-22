const express = require('express');
const http = require('http');
const socketIo = require('socket.io');
const cors = require('cors');
const helmet = require('helmet');
const rateLimit = require('express-rate-limit');
const mqtt = require('mqtt');
const cron = require('node-cron');
require('dotenv').config();

const parkingSpaceRoutes = require('./routes/parkingSpace');
const sensorRoutes = require('./routes/sensor');
const gatewayRoutes = require('./routes/gateway');
const { connectDB } = require('./config/database');
const { processSensorData } = require('./services/sensorService');
const { syncWithParkingLotServer } = require('./services/syncService');

const app = express();
const server = http.createServer(app);
const io = socketIo(server, {
  cors: {
    origin: process.env.ALLOWED_ORIGINS?.split(',') || ['http://localhost:3000'],
    methods: ['GET', 'POST']
  }
});

const PORT = process.env.PORT || 3002;

// セキュリティミドルウェア
app.use(helmet());
app.use(cors({
  origin: process.env.ALLOWED_ORIGINS?.split(',') || ['http://localhost:3000'],
  credentials: true
}));

// レート制限
const limiter = rateLimit({
  windowMs: 15 * 60 * 1000, // 15分
  max: 1000, // IoTデバイスからの大量データを考慮して上限を上げる
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
    service: 'Parking Space Management Server',
    timestamp: new Date().toISOString()
  });
});

// APIルート
app.use('/api/parking-spaces', parkingSpaceRoutes);
app.use('/api/sensors', sensorRoutes);
app.use('/api/gateways', gatewayRoutes);

// Socket.IO接続管理
io.on('connection', (socket) => {
  console.log('Client connected:', socket.id);

  // 駐車場の空き状況をリアルタイムで送信
  socket.on('subscribe-parking-lot', (parkingLotId) => {
    socket.join(`parking-lot-${parkingLotId}`);
    console.log(`Client ${socket.id} subscribed to parking lot ${parkingLotId}`);
  });

  // 特定の駐車スペースの状況を購読
  socket.on('subscribe-parking-space', (spaceId) => {
    socket.join(`parking-space-${spaceId}`);
    console.log(`Client ${socket.id} subscribed to parking space ${spaceId}`);
  });

  socket.on('disconnect', () => {
    console.log('Client disconnected:', socket.id);
  });
});

// MQTTクライアント設定（IoTデバイスからのデータ受信）
let mqttClient = null;

if (process.env.MQTT_BROKER_URL) {
  mqttClient = mqtt.connect(process.env.MQTT_BROKER_URL, {
    username: process.env.MQTT_USERNAME,
    password: process.env.MQTT_PASSWORD,
    clientId: `parking-space-server-${Date.now()}`
  });

  mqttClient.on('connect', () => {
    console.log('MQTT connected');
    // センサーデータのトピックを購読
    mqttClient.subscribe('parking/sensor/+/data');
    mqttClient.subscribe('parking/gateway/+/status');
  });

  mqttClient.on('message', async (topic, message) => {
    try {
      const data = JSON.parse(message.toString());
      console.log(`MQTT message received on ${topic}:`, data);
      
      await processSensorData(topic, data, io);
    } catch (error) {
      console.error('Error processing MQTT message:', error);
    }
  });

  mqttClient.on('error', (error) => {
    console.error('MQTT error:', error);
  });
}

// 定期的な駐車場管理サーバーとの同期
cron.schedule('*/5 * * * *', async () => {
  try {
    await syncWithParkingLotServer();
    console.log('Synced with parking lot server');
  } catch (error) {
    console.error('Sync error:', error);
  }
});

// 定期的なセンサー状態チェック
cron.schedule('*/10 * * * *', async () => {
  try {
    const { checkSensorHealth } = require('./services/sensorService');
    await checkSensorHealth();
    console.log('Sensor health check completed');
  } catch (error) {
    console.error('Sensor health check error:', error);
  }
});

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

// データベース接続とサーバー起動
async function startServer() {
  try {
    await connectDB();
    
    server.listen(PORT, () => {
      console.log(`🚗 Parking Space Management Server running on port ${PORT}`);
      console.log(`📊 Health check available at http://localhost:${PORT}/health`);
      console.log(`🔌 Socket.IO server ready for real-time updates`);
      if (mqttClient) {
        console.log(`📡 MQTT client connected for IoT device communication`);
      }
    });
  } catch (error) {
    console.error('Failed to start server:', error);
    process.exit(1);
  }
}

// グレースフルシャットダウン
process.on('SIGTERM', () => {
  console.log('SIGTERM received, shutting down gracefully');
  if (mqttClient) {
    mqttClient.end();
  }
  server.close(() => {
    console.log('Server closed');
    process.exit(0);
  });
});

process.on('SIGINT', () => {
  console.log('SIGINT received, shutting down gracefully');
  if (mqttClient) {
    mqttClient.end();
  }
  server.close(() => {
    console.log('Server closed');
    process.exit(0);
  });
});

startServer();

// Socket.IOインスタンスを他のモジュールで使用できるようにエクスポート
module.exports = { io }; 