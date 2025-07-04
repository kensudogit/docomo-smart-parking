# Docomo Smart Parking System

これは、docomoスマートパーキングシステムの構成図に基づいたプロジェクトです。

## システム構成

```mermaid
graph TD
    subgraph " "
        direction LR
        subgraph "車室検索・予約"
            App["アプリ"]
        end
        subgraph "販売管理"
            ParkingLotServer["駐車場管理サーバ"]
        end
        subgraph "経営管理(外部)"
            AdminPC["管理用PC"]
        end
    end

    subgraph "車室管理"
        ParkingSensor["パーキングセンサー"] -- "920MHz帯通信" --> Gateway["ゲートウェイ"]
        Gateway -- "LTE" --> ParkingSpaceServer["車室管理サーバ"]
    end

    App --> ParkingLotServer
    ParkingLotServer --> AdminPC
    ParkingLotServer <--> ParkingSpaceServer
```

## ディレクトリ構成

- `backend/`: 駐車場管理サーバ、車室管理サーバを配置します。
- `frontend/`: ユーザー向けアプリケーション（iOS/Android）を配置します。
- `admin/`: 管理用PCで利用するWeb管理画面を配置します。
- `iot/`: パーキングセンサーやゲートウェイとの通信を担うプログラムを配置します。 #   d o c o m o - s m a r t - p a r k i n g  
 