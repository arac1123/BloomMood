# API 說明
All API of this system are RESTful API.

Below will introduce all APIs and how to access them.

---

## Auth

> ✅ 目前登入狀態使用 **HttpOnly Cookie** 存 JWT（預設 cookie name：`accessToken`）。
>
> - 登入成功：後端回 `Set-Cookie`
> - 後續呼叫需要登入的 API：前端 fetch 要加 `credentials: 'include'`
> - Python `requests`：請使用 `requests.Session()`（會自動保存 cookie）

- POST /api/auth/register

註冊帳號
```python
import requests

data = {
  "email": "test@example.com",
  "uname": "FrontendTest",
  "password": "12345678"
}

r = requests.post("http://localhost:3001/api/auth/register", json=data)
print(r.status_code, r.text)
```

- POST /api/auth/login

登入帳號（✅ 成功後寫入 JWT Cookie；回 `{ok:true}`）
```python
import requests

s = requests.Session()

data = {
  "email": "test@example.com",
  "password": "12345678"
}

r = s.post("http://localhost:3001/api/auth/login", json=data)
print(r.status_code, r.text)
print("cookies:", s.cookies.get_dict())
```

- POST /api/auth/google

Google OAuth 授權碼流程（前端拿到 `code` → 丟給後端換 token，成功後寫入 cookie）
```python
import requests

s = requests.Session()

data = {"code": "<GOOGLE_AUTH_CODE>"}

r = s.post("http://localhost:3001/api/auth/google", json=data)
print(r.status_code, r.text)
print("cookies:", s.cookies.get_dict())
```

- POST /api/auth/logout

登出（清掉 JWT Cookie）
```python
import requests

s = requests.Session()
r = s.post("http://localhost:3001/api/auth/logout")
print(r.status_code, r.text)
```

---

## Me (需要登入：Cookie JWT)

- GET /api/me/getInfo

獲取目前登入使用者資訊
```python
import requests

s = requests.Session()
# 先登入（略）...

r = s.get("http://localhost:3001/api/me/getInfo")
print(r.status_code, r.text)
```

回應（200）：
```json
{ "uid": 1, "email": "a@b.com", "uname": "Alice", "role": "ROLE_USER" }
```

- PATCH /api/me

更新 uname / email / password
```python
import requests

s = requests.Session()
# 先登入（略）...

data = {
  "uname": "newname",
  "email": "newemail@test.com",
  "oldPassword": "12345678",
  "newPassword": "87654321"
}

r = s.patch("http://localhost:3001/api/me", json=data)
print(r.status_code, r.text)
```

回應：
- 200: `{ "ok": true, "changed": true/false }`
- 400/401: `{ "message": "..." }`

---

## Debug

> 注意：目前 `SecurityConfig` 將 `/api/debug/**` 設為 `permitAll()`，等於不需要登入就能查所有使用者。

- GET /api/debug/users

列出所有使用者（不含密碼）
```python
import requests

r = requests.get("http://localhost:3001/api/debug/users")
print(r.status_code, r.text)
```

回應（200）：
```json
[
  { "uid": 1, "email": "a@b.com", "uname": "Alice", "role": "ROLE_USER" }
]
```

---

## Plant (需要登入：Cookie JWT)

> DB 只存「真的有種」那天的資料。
> `month` API 會回一整個月每天的紀錄（有/沒有都有）。

- GET /api/plant/today

查今天有沒有種。

如果沒種：`{ "hasPlant": false }`

如果有種：`{ "hasPlant": true, "plant": { ... } }`

- POST /api/plant/today

建立今天的植物（如果今天已經有就直接回那株；每天一人最多一株）

Request body 可留空，或指定 type：
```json
{ "type": "FLOWER" }
```

- GET /api/plant/month?ym=YYYY-MM

查某個月的每天紀錄（含沒種的日子）

Example: `/api/plant/month?ym=2026-02`

- PATCH /api/plant/{pid}

更新植物欄位（只會更新有傳的欄位；null 會忽略）

Request body example:
```json
{ "status": "WITHERED", "stage": 2, "type": "TREE" }
```

- DELETE /api/plant/{pid}

刪除某一筆植物資料

回應：
- 200: `{ "ok": true }`
- 404: `{ "message": "plant not found" }`
