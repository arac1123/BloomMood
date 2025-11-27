BloomMood Use Case 文件

UC-01: 使用者註冊
項目內容 Use Case IDUC-01Use Case Name 使用者註冊 Actor 訪客 Description 訪客建立新帳號以使用 BloomMood 服務 Trigger 訪客點擊「註冊」按鈕 Precondition1. 訪客尚未登入 2. 訪客有有效的 Email 或社群帳號 Postcondition1. 建立新使用者帳號 2. 自動登入系統 3. 導向主頁面
Main Flow:

訪客進入註冊頁面
系統顯示註冊選項（Email/Google/Apple）
訪客選擇註冊方式

3a. Email 註冊：輸入 email、密碼、暱稱
3b. OAuth 註冊：選擇 Google/Apple 登入

系統驗證資料有效性
系統建立帳號
系統發送歡迎郵件
系統自動登入並導向主頁面

Alternative Flow:

3a-1: Email 已被註冊 → 顯示錯誤訊息，建議登入或使用其他 Email
4a: 密碼不符合規則 → 顯示密碼要求提示
3b-1: OAuth 驗證失敗 → 顯示錯誤訊息，返回註冊頁

Exception Flow:

E1: 網路連線失敗 → 顯示重試提示
E2: 資料庫錯誤 → 顯示系統維護訊息

UC-02: 使用者登入
項目內容 Use Case IDUC-02Use Case Name 使用者登入 Actor 訪客（已註冊）Description 使用者登入系統以使用服務 Trigger 使用者點擊「登入」按鈕 Precondition1. 使用者已註冊 2. 使用者未登入 Postcondition1. 建立登入 Session2. 導向主頁面 3. 載入使用者資料
Main Flow:

使用者進入登入頁面
系統顯示登入選項
使用者輸入憑證

3a. Email/密碼登入
3b. OAuth 快速登入

系統驗證身份
系統建立 Session/JWT Token
系統載入使用者個人資料
系統導向主頁面

Alternative Flow:

4a: 帳號密碼錯誤 → 顯示錯誤訊息（剩餘嘗試次數）
4b: 帳號被鎖定 → 顯示解鎖說明
3c: 忘記密碼 → 進入密碼重設流程

Exception Flow:

E1: 多次登入失敗 → 暫時鎖定帳號 15 分鐘
E2: Session 過期 → 要求重新登入

UC-03: 記錄今日心情
項目內容 Use Case IDUC-03Use Case Name 記錄今日心情 Actor 會員 Description 會員記錄當天的心情，系統生成對應植物 Trigger 會員進入主頁面或點擊「記錄心情」Precondition1. 使用者已登入 2. 今日尚未記錄心情 Postcondition1. 儲存心情記錄 2. 生成新植物 3. 更新花園
Main Flow:

會員進入今日心情頁面
系統檢查今日是否已記錄
系統顯示心情輸入介面
會員輸入心情

4a. 輸入文字描述（必填）
4b. 選擇情緒標籤（選填）

會員點擊「記錄心情」
系統進行情緒分析
系統生成對應植物類型
系統播放植物生成動畫
系統儲存記錄至資料庫
系統顯示生成的植物和吉祥物回應

Alternative Flow:

2a: 今日已記錄 → 顯示今日植物，提供檢視/互動選項
4c: 只選擇情緒標籤 → 使用預設文字模板
6a: 情緒分析失敗 → 使用關鍵字匹配備援方案
8a: 動畫載入失敗 → 直接顯示植物結果

Business Rules:

BR1: 每日限記錄一次心情
BR2: 文字最少 10 字，最多 500 字
BR3: 可選情緒標籤：開心、平靜、悲傷、焦慮、憤怒、疲憊、興奮、感恩
BR4: 植物類型對應：

開心 → 向日葵
平靜 → 薰衣草
悲傷 → 藍色風信子
焦慮 → 仙人掌
其他依分析結果

Exception Flow:

E1: 資料庫儲存失敗 → 暫存本地，稍後重試
E2: API 服務無回應 → 使用離線模式

UC-04: 與植物互動
項目內容 Use Case IDUC-04Use Case Name 與植物互動 Actor 會員 Description 會員透過吉祥物對植物進行照顧互動 Trigger 會員點擊互動按鈕 Precondition1. 使用者已登入 2. 存在可互動的植物 Postcondition1. 更新植物狀態 2. 記錄互動歷史 3. 顯示互動動畫
Main Flow:

會員查看植物
系統顯示可用互動選項
會員選擇互動動作

3a. 澆水
3b. 給予陽光
3c. 施肥

系統檢查互動冷卻時間
吉祥物執行互動動畫
系統更新植物狀態（健康度+10）
系統記錄互動歷史
系統顯示植物反應動畫
吉祥物顯示鼓勵文字

Alternative Flow:

4a: 互動冷卻中 → 顯示剩餘時間
3d: 特殊互動解鎖 → 根據連續互動天數解鎖新動作
8a: 植物升級 → 播放成長動畫

Business Rules:

BR1: 每種互動每小時可執行一次
BR2: 互動效果：

澆水：健康度 +10，持續 2 小時
陽光：健康度 +10，心情加成
施肥：健康度 +15，成長加速

BR3: 連續 3 天未互動，植物進入枯萎狀態

Exception Flow:

E1: 動畫載入失敗 → 顯示靜態圖片
E2: 狀態更新失敗 → 回滾並提示重試

UC-05: 與吉祥物聊天
項目內容 Use Case IDUC-05Use Case Name 與吉祥物聊天 Actor 會員 Description 會員與吉祥物進行文字對話獲得情緒支持 Trigger 會員點擊吉祥物或聊天按鈕 Precondition1. 使用者已登入 2. 吉祥物功能啟用 Postcondition1. 儲存對話記錄 2. 更新使用者情緒檔案
Main Flow:

會員開啟聊天介面
系統載入歷史對話（最近 10 則）
會員輸入訊息
系統分析訊息情緒
系統查詢相關歷史記錄
系統生成個性化回應

6a. 使用 AI API 生成
6b. 從回應範本庫選擇

吉祥物顯示打字動畫
系統顯示回應訊息
系統儲存對話記錄
更新使用者情緒檔案

Alternative Flow:

6a-1: AI API 失敗 → 使用預設回應範本
3a: 輸入語音訊息 → 轉換為文字
8a: 觸發關鍵字 → 提供額外資源（如心理健康熱線）
6c: 偵測負面情緒 → 優先使用支持性回應

Business Rules:

BR1: 訊息長度限制 500 字
BR2: 每日對話上限 100 則
BR3: 回應延遲 1-2 秒模擬真實感
BR4: 敏感詞過濾與處理

Exception Flow:

E1: API 逾時 → 使用本地回應庫
E2: 偵測自傷傾向 → 顯示專業協助資源

UC-06: 查看心情花園
項目內容 Use Case IDUC-06Use Case Name 查看心情花園 Actor 會員 Description 會員查看當月累積的所有心情植物 Trigger 會員點擊「我的花園」Precondition1. 使用者已登入 2. 有心情記錄 Postcondition 顯示花園視圖
Main Flow:

會員進入花園頁面
系統載入當月植物資料
系統渲染花園視圖

3a. 格子佈局（日曆視圖）
3b. 花園佈局（美化視圖）

系統顯示植物統計
會員可點擊單一植物
系統顯示植物詳細資訊

日期、心情內容、情緒類型、健康度

Alternative Flow:

2a: 無當月資料 → 顯示空花園提示
5a: 滑鼠懸停 → 顯示簡易資訊卡
3c: 切換月份 → 載入其他月份資料

Business Rules:

BR1: 花園最多顯示 31 株植物（當月）
BR2: 過去的植物保持最後狀態
BR3: 可切換檢視模式

UC-07: 查看月度總結
項目內容 Use Case IDUC-07Use Case Name 查看月度總結 Actor 會員 Description 會員查看每月的心情統計與花園總結 Trigger 月底自動生成或會員手動查看 Precondition1. 使用者已登入 2. 當月有至少 7 筆記錄 Postcondition 生成並顯示月度報告
Main Flow:

系統在月底自動觸發（或會員手動請求）
系統統計當月資料

2a. 情緒分佈（圓餅圖）
2b. 心情趨勢（折線圖）
2c. 互動頻率統計
2d. 最常出現關鍵字

系統生成花園快照圖片
系統產生文字總結
系統組合成月度報告
顯示報告頁面
提供下載/分享選項

Alternative Flow:

2e: 記錄不足 → 顯示部分統計
7a: 分享至社群 → 生成分享圖片
7b: 下載 PDF → 產生 PDF 檔案
6a: 查看歷史報告 → 載入過去月份

Business Rules:

BR1: 保留最近 12 個月的報告
BR2: 分享圖片去除敏感資訊
BR3: 統計數據即時計算

Exception Flow:

E1: 生成失敗 → 顯示重試選項
E2: 資料不足 → 顯示累積提示

UC-08: 查看歷史記錄
項目內容 Use Case IDUC-08Use Case Name 查看歷史記錄 Actor 會員 Description 會員查詢過往的心情記錄 Trigger 會員點擊「歷史記錄」Precondition1. 使用者已登入 2. 有歷史記錄 Postcondition 顯示篩選後的記錄
Main Flow:

會員進入歷史記錄頁面
系統顯示搜尋/篩選介面
會員設定篩選條件

3a. 日期範圍
3b. 情緒類型
3c. 關鍵字搜尋

系統查詢資料庫
系統顯示結果列表
會員可展開查看詳細內容
提供匯出功能

Alternative Flow:

5a: 無符合結果 → 顯示無資料提示
6a: 編輯過去記錄 → 標註已編輯
7a: 匯出 CSV/JSON → 產生檔案下載

Business Rules:

BR1: 最多顯示 1000 筆記錄
BR2: 分頁顯示，每頁 20 筆
BR3: 可編輯 7 天內的記錄

UC-09: 匯出花園照片
項目內容 Use Case IDUC-09Use Case Name 匯出花園照片 Actor 會員 Description 會員匯出花園圖片做紀念或分享 Trigger 會員點擊「匯出」按鈕 Precondition1. 使用者已登入 2. 花園有植物 Postcondition 產生圖片檔案
Main Flow:

會員在花園頁面點擊匯出
系統顯示匯出選項

2a. 圖片格式（PNG/JPG）
2b. 解析度選擇
2c. 是否包含文字

會員確認設定
系統渲染花園圖片
系統加入浮水印/日期
系統產生下載連結
自動下載或顯示預覽

Alternative Flow:

2d: 選擇特定日期範圍
5a: 自訂浮水印文字
7a: 直接分享至社群

Business Rules:

BR1: 最大解析度 4K
BR2: 檔案大小限制 10MB
BR3: 包含 BloomMood 標誌

UC-10: OAuth 第三方登入
項目內容 Use Case IDUC-10Use Case NameOAuth 第三方登入 Actor 訪客、OAuth ProviderDescription 使用 Google/Apple 帳號快速登入 Trigger 點擊第三方登入按鈕 Precondition 擁有第三方帳號 Postcondition 完成登入或註冊
Main Flow:

使用者點擊「使用 Google/Apple 登入」
系統重導向至 OAuth 提供者
使用者在第三方頁面授權
OAuth 提供者回傳授權碼
系統交換 Access Token
系統取得使用者資訊
系統檢查是否為新用戶

7a. 新用戶 → 建立帳號
7b. 既有用戶 → 更新資訊

建立 Session
導向主頁面

Alternative Flow:

3a: 使用者拒絕授權 → 返回登入頁
5a: Token 交換失敗 → 顯示錯誤
6a: 資訊不完整 → 要求補充資料

Exception Flow:

E1: OAuth 服務無回應 → 顯示傳統登入
E2: 帳號綁定衝突 → 提示合併帳號
