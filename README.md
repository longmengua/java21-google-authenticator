# java21-google-authenticator

# DDD 規範

## 資料夾結構

/
├── /application         # 用例層 (Application Service)
│    ├── service         # 應用服務，協調 domain 物件完成用例
│    └── dto             # 輸入輸出資料 (Data Transfer Objects), DAO, DTO
│
├── /domain              # 核心領域模型 (最重要)
│    ├── model           # Entity, Aggregate Root, Value Object
│    ├── repository      # Repository 介面 (domain abstraction)
│    ├── service         # Domain Service（純領域邏輯）
│    └── event           # Domain Events
│
├── /infrastructure      # 基礎設施層 (Infra Adapter)
│    ├── persistence     # Repository 實作 (ex: JPA, MyBatis, ClickHouse…)
│    ├── messaging       # Kafka, RocketMQ adapter
│    ├── external        # 第三方串接 adapter
│    └── config          # Spring/K8s 設定
│
└── /interfaces          # 使用者介面層 (Inbound/Outbound Adapter)
     ├── rest            # REST API Controller
     ├── grpc            # gRPC API
     └── scheduler       # Job, Event Listener, Consumer

## 指令建立資料夾結構

mkdir -p application/service \
application/dto \
domain/model \
domain/repository \
domain/service \
domain/event \
infrastructure/persistence \
infrastructure/messaging \
infrastructure/config \
interfaces/rest \
interfaces/grpc \
interfaces/scheduler
