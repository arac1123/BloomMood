# BloomMood 植物心情

BloomMood 是一個結合「心情紀錄」與「植物養成」概念的全端作品。使用者可以透過登入、記錄日常行動與心情，讓虛擬植物隨著互動逐步成長，並在儀表板中查看自己的累積狀態與統計資訊。

這個專案的設計重點是把情緒管理、每日習慣追蹤與遊戲化視覺回饋整合在同一個體驗裡，適合作為作品集展示給面試官看。

## 主要功能

- Google 登入與註冊登入流程
- 使用者首頁與導覽介面
- 植物養成與花園畫面
- 心情／日常紀錄功能
- 統計資訊與成長回顧
- 個人設定與帳號相關頁面
- Docker Compose 一鍵啟動前後端與資料庫

## 技術堆疊

- 前端：Vue 3、Vue Router、Vite
- 後端：Spring Boot、Spring Security、Spring Data JPA
- 資料庫：PostgreSQL
- 驗證：Google OAuth、JWT
- 部署／開發環境：Docker、Docker Compose

## 專案結構

- `src/frontend/bloommood`：Vue 前端
- `src/backend/bloommood`：Spring Boot 後端
- `docker-compose.yaml`：整體服務啟動設定
- `env.example`：環境變數範例
- `doc/`：需求、架構與文件

## 快速開始

1. 先複製 `env.example`，填入實際環境變數。
2. 在專案根目錄執行：

```bash
docker compose up --build
```

3. 開啟前端：

```text
http://localhost:3000
```

## 環境變數

### 後端

- `APP_GOOGLE_CLIENT_ID`
- `APP_GOOGLE_CLIENT_SECRET`
- `APP_GOOGLE_REDIRECT_URI`
- `FRONTEND_BASE_URL`

### 前端

- `VITE_API_BASE_URL`
- `VITE_GOOGLE_CLIENT_ID`

## 備註

- 如果你要直接在本機分別啟動前後端，請確認後端資料庫設定、Google OAuth 設定與前端 API 位址一致。
- 目前以 Docker Compose 啟動是最穩定、最適合展示作品集的方式。
