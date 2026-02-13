# API 說明
All API of this system are RESTful API.

Below will introduce all APIs and how to access them using python.

---

## Auth

- POST /api/auth/register 

註冊帳號
```python
data = {
  "email": "test_1770911998541@a.com",
  "uname": "FrontendTest",
  "password": "12345678"
}

r = requests.post("http://localhost:3001/api/auth/register", json=data)
```

- POST /api/auth/login 

登入帳號（回傳 JWT token）
```python
data = {
  "email": "test_1770911998541@a.com",
  "password": "12345678"
}

r = requests.post("http://localhost:3001/api/auth/login", json=data)
# token 在 r.json()["token"]
```


---


## Me (需要登入：Authorization Bearer JWT)


- GET /api/me/getInfo

獲取目前登入使用者資訊
```python
token = "<JWT>"  # 由 /api/auth/login 取得
headers = {"Authorization": f"Bearer {token}"}

r = requests.get("http://localhost:3001/api/me/getInfo", headers=headers)
```

回應（200）：
```json
{ "uid": 1, "email": "a@b.com", "uname": "Alice", "role": "ROLE_USER" }
```


- PATCH /api/me

更新 uname / email / password
```python
token = "<JWT>"
headers = {"Authorization": f"Bearer {token}"}

data = {
  "uname": "newname",
  "email": "newemail@test.com",
  "oldPassword": "12345678",
  "newPassword": "87654321"
}

r = requests.patch(
  "http://localhost:3001/api/me",
  json=data,
  headers=headers
)
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
r = requests.get("http://localhost:3001/api/debug/users")
```

回應（200）：
```json
[
  { "uid": 1, "email": "a@b.com", "uname": "Alice", "role": "ROLE_USER" }
]
```
