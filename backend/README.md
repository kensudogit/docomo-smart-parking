# Docomo Smart Parking Backend

このディレクトリには、ドコモスマートパーキングシステムのバックエンドサービスが含まれています。

## システム構成

### 1. 駐車場管理サーバ (Parking Lot Management Server)
- **ポート**: 3001
- **役割**: ユーザー認証、予約処理、販売管理など、アプリケーションの主要なビジネスロジックを担当
- **主要機能**:
  - ユーザー認証・認可
  - 駐車場情報管理
  - 予約作成・管理・キャンセル
  - チェックイン・チェックアウト処理
  - 決済処理
  - 販売レポート

### 2. 車室管理サーバ (Parking Space Management Server)
- **ポート**: 3002
- **役割**: IoTデバイス（パーキングセンサー、ゲートウェイ）からのデータを受け取り、駐車場の空き状況をリアルタイムに管理
- **主要機能**:
  - IoTセンサーデータ受信・処理
  - リアルタイム空き状況管理
  - Socket.IOによるリアルタイム通知
  - センサー健全性監視
  - メンテナンス管理

## 技術スタック

### 共通技術
- **Node.js**: サーバーサイドJavaScriptランタイム
- **Express.js**: Webアプリケーションフレームワーク
- **MongoDB**: NoSQLデータベース
- **Mongoose**: MongoDB ODM
- **JWT**: 認証トークン

### 駐車場管理サーバ
- **bcryptjs**: パスワードハッシュ化
- **express-validator**: 入力検証
- **express-rate-limit**: レート制限
- **helmet**: セキュリティヘッダー

### 車室管理サーバ
- **Socket.IO**: リアルタイム通信
- **MQTT**: IoTデバイスとの通信
- **node-cron**: 定期タスク実行
- **axios**: HTTP通信

## セットアップ手順

### 前提条件
- Node.js (v16以上)
- MongoDB (v5以上)
- npm または yarn

### 1. 依存関係のインストール

#### 駐車場管理サーバ
```bash
cd parking-lot-server
npm install
```

#### 車室管理サーバ
```bash
cd parking-space-server
npm install
```

### 2. 環境変数の設定

#### 駐車場管理サーバ
```bash
cd parking-lot-server
cp env.example .env
# .envファイルを編集して適切な値を設定
```

#### 車室管理サーバ
```bash
cd parking-space-server
cp env.example .env
# .envファイルを編集して適切な値を設定
```

### 3. データベースの準備
```bash
# MongoDBを起動
mongod

# データベースを作成（自動的に作成されます）
```

### 4. サーバーの起動

#### 開発モード
```bash
# 駐車場管理サーバ
cd parking-lot-server
npm run dev

# 車室管理サーバ（別ターミナル）
cd parking-space-server
npm run dev
```

#### 本番モード
```bash
# 駐車場管理サーバ
cd parking-lot-server
npm start

# 車室管理サーバ
cd parking-space-server
npm start
```

## API エンドポイント

### 駐車場管理サーバ (Port 3001)

#### 認証
- `POST /api/auth/register` - ユーザー登録
- `POST /api/auth/login` - ユーザーログイン
- `GET /api/auth/profile` - プロフィール取得
- `PUT /api/auth/profile` - プロフィール更新
- `PUT /api/auth/change-password` - パスワード変更

#### 予約管理
- `POST /api/reservations` - 予約作成
- `GET /api/reservations/my-reservations` - 自分の予約一覧
- `GET /api/reservations/:id` - 予約詳細
- `PUT /api/reservations/:id/cancel` - 予約キャンセル
- `PUT /api/reservations/:id/checkin` - チェックイン
- `PUT /api/reservations/:id/checkout` - チェックアウト

#### 駐車場管理
- `GET /api/parking-lots` - 駐車場一覧
- `GET /api/parking-lots/:id` - 駐車場詳細
- `POST /api/parking-lots` - 駐車場作成
- `PUT /api/parking-lots/:id` - 駐車場更新

### 車室管理サーバ (Port 3002)

#### 駐車スペース管理
- `GET /api/parking-spaces/availability/:parkingLotId` - 空き状況取得
- `GET /api/parking-spaces/:id` - 駐車スペース詳細
- `GET /api/parking-spaces/:id/status` - リアルタイム状態
- `GET /api/parking-spaces/:id/history` - 使用履歴
- `GET /api/parking-spaces/:id/sensor` - センサー情報

#### メンテナンス管理
- `GET /api/parking-spaces/:id/maintenance` - メンテナンス問題一覧
- `POST /api/parking-spaces/:id/maintenance` - メンテナンス問題追加
- `PUT /api/parking-spaces/:id/maintenance/:issueIndex/resolve` - 問題解決

#### IoT通信
- MQTTトピック: `parking/sensor/{sensorId}/data` - センサーデータ受信
- MQTTトピック: `parking/gateway/{gatewayId}/status` - ゲートウェイ状態

## リアルタイム通信

### Socket.IO イベント

#### クライアント → サーバー
- `subscribe-parking-lot` - 駐車場の更新を購読
- `subscribe-parking-space` - 特定の駐車スペースを購読

#### サーバー → クライアント
- `parking-space-update` - 駐車スペース状態更新
- `space-status-update` - 詳細な駐車スペース状態更新

## データベース設計

### 主要なコレクション

#### 駐車場管理サーバ
- `users` - ユーザー情報
- `parkinglots` - 駐車場情報
- `reservations` - 予約情報

#### 車室管理サーバ
- `parkingspaces` - 駐車スペース情報
- `sensors` - センサー情報
- `gateways` - ゲートウェイ情報

## セキュリティ

- JWT認証
- パスワードハッシュ化
- CORS設定
- レート制限
- セキュリティヘッダー
- 入力検証

## 監視・ログ

- センサー健全性監視
- 定期ヘルスチェック
- エラーログ
- パフォーマンス監視

## 開発・テスト

### テスト実行
```bash
# 駐車場管理サーバ
cd parking-lot-server
npm test

# 車室管理サーバ
cd parking-space-server
npm test
```

### コード品質
- ESLint設定
- Prettier設定
- 型チェック（JSDoc）

## デプロイメント

### Docker化
各サーバーにはDockerfileが含まれており、コンテナ化してデプロイ可能です。

### 環境別設定
- 開発環境: `NODE_ENV=development`
- テスト環境: `NODE_ENV=test`
- 本番環境: `NODE_ENV=production`

## トラブルシューティング

### よくある問題

1. **MongoDB接続エラー**
   - MongoDBが起動しているか確認
   - 接続文字列が正しいか確認

2. **ポート競合**
   - 使用中のポートを確認
   - 環境変数でポートを変更

3. **MQTT接続エラー**
   - MQTTブローカーが起動しているか確認
   - 認証情報が正しいか確認

## ライセンス

MIT License 