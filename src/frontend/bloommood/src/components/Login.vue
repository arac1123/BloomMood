<template>
  <div class="page page-bg-auth">
    <div class="deco leaf-left">🍃</div>
    <div class="deco leaf-right">🌿</div>

    <header class="navbar">
      <div class="logo">Bloommood</div>
    </header>

    <main class="content">
      <div class="card">
        <header class="card-header">
          <h2 class="title">開始你的療癒之旅</h2>
          <div class="subtitle">
            <span class="breathing-emoji">🌸</span>
            繼續灌溉你的心靈花園
          </div>
        </header>

        <form class="form" @submit.prevent="handleLogin">
          <div class="input-group">
            <input 
              v-model.trim="loginData.email" 
              type="email" 
              placeholder="電子郵件" 
              required 
            />
          </div>
          
          <div class="input-group">
            <input 
              v-model="loginData.password" 
              type="password" 
              placeholder="密碼" 
              required 
            />
          </div>

          <button 
            type="submit" 
            class="btn" 
            :class="{ 'btn-loading': isLoading }"
            :disabled="isLoading"
          >
            <span v-if="!isLoading">登入</span>
            <span v-else>正在進入您的情緒花園...</span>
          </button>
        </form>

        <div class="divider">
          <span>或使用以下方式</span>
        </div>

        <button 
          type="button" 
          @click="handleGoogleLogin" 
          class="btn-google" 
          :disabled="isLoading"
        >
          <svg class="google-icon" viewBox="0 0 48 48">
            <path fill="#EA4335" d="M24 9.5c3.54 0 6.71 1.22 9.21 3.6l6.85-6.85C35.9 2.38 30.47 0 24 0 14.62 0 6.51 5.38 2.56 13.22l7.98 6.19C12.43 13.72 17.74 9.5 24 9.5z"></path>
            <path fill="#4285F4" d="M46.98 24.55c0-1.57-.15-3.09-.38-4.55H24v9.02h12.94c-.58 2.96-2.26 5.48-4.78 7.18l7.73 6c4.51-4.18 7.09-10.36 7.09-17.65z"></path>
            <path fill="#FBBC05" d="M10.53 28.59c-.48-1.45-.76-2.99-.76-4.59s.27-3.14.76-4.59l-7.98-6.19C.92 16.46 0 20.12 0 24c0 3.88.92 7.54 2.56 10.78l7.97-6.19z"></path>
            <path fill="#34A853" d="M24 48c6.48 0 11.93-2.13 15.89-5.81l-7.73-6c-2.15 1.45-4.92 2.31-8.16 2.31-6.26 0-11.57-4.22-13.47-9.91l-7.98 6.19C6.51 42.62 14.62 48 24 48z"></path>
            <path fill="none" d="M0 0h48v48H0z"></path>
          </svg>
          Google 帳號登入
        </button>

        <footer class="auth-switch">
          <span>還沒有帳號嗎？</span>
          <router-link to="/register" class="link-text">立即註冊</router-link>
        </footer>
      </div>
    </main>
  </div>
</template>

<script setup>
import { onMounted, reactive, ref } from 'vue';
import { useRouter } from 'vue-router';
const router = useRouter();
const isLoading = ref(false);

const apiBaseUrl = import.meta.env.VITE_API_BASE_URL || 'http://localhost:3001';

const loginData = reactive({
  email: '',
  password: ''
});

const googleCodeClient = ref(null);

const loadGoogleScript = () => new Promise((resolve, reject) => {
  if (window.google?.accounts?.oauth2) {
    resolve();
    return;
  }

  const existing = document.querySelector('script[data-google-gis="true"]');
  if (existing) {
    existing.addEventListener('load', () => resolve());
    existing.addEventListener('error', () => reject(new Error('Failed to load Google script')));
    return;
  }

  const script = document.createElement('script');
  script.src = 'https://accounts.google.com/gsi/client';
  script.async = true;
  script.defer = true;
  script.dataset.googleGis = 'true';
  script.onload = () => resolve();
  script.onerror = () => reject(new Error('Failed to load Google script'));
  document.head.appendChild(script);
});

// 1. 處理一般登入
const handleLogin = async () => {
  const { email, password  } = loginData;
  if (!email || !password) {
    alert("請填寫完整的電子郵件和密碼");
    return;
  }
  if (isLoading.value) return;
  isLoading.value = true;
 try {
  const res = await fetch(`${apiBaseUrl}/api/auth/login`, {
    method: 'POST',
    credentials: 'include', 
    headers: { 'Content-Type': 'application/json' },
    // 1. 記得轉成 JSON 字串
    body: JSON.stringify({
      email: email,
      password: password,
    })
  });

  // 2. 解析後端回傳的資料
  const data = await res.json();
  console.log("後端回應:", data);
  console.log("HTTP 狀態碼:", res);
  if (!res.ok) {
    // 這裡就能拿到後端寫在 res.status(400).json({ message: "..." }) 裡的訊息
    console.error("登入失敗:", data.message);
    alert(`登入失敗: ${data.message || '請檢查帳號密碼'}`);
    return;
  }


  await new Promise((resolve) => setTimeout(resolve, 2000));
  router.push('/home');
} catch (e) {
  // 這裡通常是網路斷線、伺服器當掉才會觸發
  console.log("網路或系統錯誤:", e);
  alert("連線伺服器失敗，請稍後再試。");
} finally {
  isLoading.value = false;
}
  

};

// 2. 處理 Google OAuth 登入 (GIS Code Flow)
const handleGoogleLogin = async () => {
  try {
    if (!googleCodeClient.value) {
      alert('Google 登入初始化失敗，請稍後再試。');
      return;
    }
    if (isLoading.value) return;
    isLoading.value = true;
    googleCodeClient.value.requestCode();
  } catch (error) {
    console.error('Google 登入錯誤:', error);
    alert('Google 登入失敗，請稍後再試。');
    isLoading.value = false;
  }
};

const handleGoogleCode = async (response) => {
  const code = response?.code;
  if (!code) {
    alert('Google 登入失敗，未取得授權碼。');
    isLoading.value = false;
    return;
  }

  try {
    const res = await fetch(`${apiBaseUrl}/api/auth/google`, {
      method: 'POST',
      credentials: 'include',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ code })
    });

    if (!res.ok) {
      const data = await res.json().catch(() => ({}));
      alert(data?.message || 'Google 登入失敗');
      return;
    }

    await new Promise((resolve) => setTimeout(resolve, 1000));
    router.push('/home');
  } catch (error) {
    console.error('Google 登入錯誤:', error);
    alert('Google 登入失敗，請稍後再試。');
  } finally {
    isLoading.value = false;
  }
};

onMounted(async () => {
  const clientId = import.meta.env.VITE_GOOGLE_CLIENT_ID;
  if (!clientId) {
    console.warn('VITE_GOOGLE_CLIENT_ID 未設定');
    return;
  }

  try {
    await loadGoogleScript();
    googleCodeClient.value = window.google.accounts.oauth2.initCodeClient({
      client_id: clientId,
      scope: 'openid email profile',
      callback: handleGoogleCode
    });
  } catch (error) {
    console.error('Google GIS 初始化失敗:', error);
  }
});
</script>

<style scoped>
/* 共用 auth 樣式已抽到 main.css，此處僅保留頁面專屬樣式 */
.btn {
  margin-top: 5px;
}

/* 分隔線樣式 */
.divider {
  display: flex;
  align-items: center;
  margin: 25px 0;
  color: #8a9a8a;
  font-size: 13px;
}

.divider::before,
.divider::after {
  content: "";
  flex: 1;
  height: 1px;
  background: rgba(0, 0, 0, 0.08);
}

.divider span {
  padding: 0 15px;
}

/* Google 按鈕樣式 */
.btn-google {
  width: 100%;
  padding: 14px;
  background: #ffffff;
  color: #444;
  border: 1px solid rgba(0, 0, 0, 0.1);
  border-radius: 15px;
  cursor: pointer;
  font-size: 15px;
  font-weight: 500;
  display: flex;
  justify-content: center;
  gap: 12px;
  transition: all 0.3s ease;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.02);
}

.btn-google:hover:not(:disabled) {
  background: #fafafa;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.08);
  transform: translateY(-1px);
}

.google-icon {
  width: 18px;
  height: 18px;
}

/* 切換與連結 */
.auth-switch {
  margin-top: 25px;
  text-align: center;
  font-size: 14px;
  color: #556b55;
}

.link-text {
  color: #5d7a5d;
  font-weight: bold;
  text-decoration: none;
  margin-left: 5px;
  transition: border-bottom 0.3s;
}

.link-text:hover {
  border-bottom: 1px solid #5d7a5d;
}

/* 裝飾與動畫 */
.deco {
  position: absolute;
  font-size: 150px;
  opacity: 0.15;
  pointer-events: none;
  z-index: 1;
}

.leaf-left {
  bottom: -30px;
  left: -30px;
  transform: rotate(15deg);
}

.leaf-right {
  top: 20%;
  right: -50px;
  transform: rotate(-20deg);
  font-size: 120px;
}

@keyframes breathe {
  0%, 100% { transform: scale(1); opacity: 0.8; }
  50% { transform: scale(1.2); opacity: 1; }
}

.breathing-emoji {
  display: inline-block;
  margin-right: 10px;
  animation: breathe 3s infinite ease-in-out;
}

.btn:disabled, .btn-google:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}
</style>