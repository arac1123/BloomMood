<template>
  <div class="page">
    <div class="deco leaf-left">🍃</div>
    <div class="deco leaf-right">🌿</div>

    <header class="navbar">
      <div class="logo">Bloommood</div>
    </header>

    <main class="content">
      <div class="card">
        <header class="card-header">
          <h2 class="title">歡迎回來</h2>
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
            <span v-else>🌿 正在進入花園...</span>
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
import { reactive, ref } from 'vue';
import { useRouter } from 'vue-router';

const router = useRouter();
const isLoading = ref(false);

const loginData = reactive({
  email: '',
  password: ''
});

// 1. 處理一般登入
const handleLogin = async () => {
  isLoading.value = true;
  try {
    // 模擬 API 驗證延遲
    await new Promise(resolve => setTimeout(resolve, 1500));
    
    alert("✨ 登入成功！準備進入花園。");
    router.push('/home'); 
  } catch (error) {
    alert("⚠️ 登入失敗，請檢查您的帳號密碼");
  } finally {
    isLoading.value = false;
  }
};

// 2. 處理 Google OAuth 登入
const handleGoogleLogin = () => {
  // 在實際應用中，這裡會跳轉到後端的 OAuth API 路由
  // 例如：window.location.href = `${process.env.VUE_APP_API_URL}/auth/google`;
  console.log("導向 Google 驗證頁面...");
  alert("正在導向 Google 安全登入... 🕊️");
};
</script>

<style scoped>
/* 核心版面設定 */
.page {
  min-height: 100vh;
  background: linear-gradient(135deg, #d4e0d4 0%, #fefae0 100%);
  display: flex;
  flex-direction: column;
  position: relative;
  overflow: hidden;
  font-family: 'PingFang TC', 'Microsoft JhengHei', sans-serif;
}

/* 導覽列風格 */
.navbar {
  display: flex;
  align-items: center;
  padding: 16px 40px;
  background: rgba(255, 255, 255, 0.3);
  backdrop-filter: blur(10px);
  border-bottom: 1px solid rgba(255, 255, 255, 0.2);
  z-index: 10;
}

.logo {
  font-weight: 700;
  font-size: 22px;
  color: #4a5d4a;
  letter-spacing: 1px;
}

/* 內容區塊與卡片 */
.content {
  flex: 1;
  display: flex;
  justify-content: center;
  align-items: center;
  padding: 40px 20px;
}

.card {
  background: rgba(255, 255, 255, 0.5);
  backdrop-filter: blur(20px);
  border: 1px solid rgba(255, 255, 255, 0.4);
  border-radius: 30px;
  padding: 50px;
  width: 100%;
  max-width: 450px;
  box-shadow: 0 20px 40px rgba(0, 0, 0, 0.05);
  z-index: 100;
}

/* 標題與副標題 */
.title {
  margin: 0 0 12px 0;
  font-size: 24px;
  font-weight: bold;
  color: #222;
}

.subtitle {
  margin-bottom: 35px;
  color: #6a7a6a;
  font-size: 18px;
  display: flex;
  align-items: center;
}

/* 表單與輸入框 */
.form {
  display: flex;
  flex-direction: column;
  gap: 18px;
}

.form input {
  width: 100%;
  padding: 15px 18px;
  border-radius: 15px;
  border: 1px solid rgba(0, 0, 0, 0.05);
  background: rgba(255, 255, 255, 0.7);
  font-size: 15px;
  outline: none;
  transition: all 0.3s ease;
  box-sizing: border-box;
}

.form input:focus {
  background: #ffffff;
  border-color: #8fb98f;
  box-shadow: 0 0 15px rgba(143, 185, 143, 0.25);
  transform: translateY(-1px);
}

/* 一般登入按鈕樣式 */
.btn {
  width: 100%;
  padding: 16px;
  background: #5d7a5d;
  color: #ffffff;
  border: none;
  border-radius: 15px;
  cursor: pointer;
  font-size: 16px;
  font-weight: 600;
  box-shadow: 0 8px 20px rgba(93, 122, 93, 0.2);
  transition: all 0.3s ease;
  margin-top: 5px;
}

.btn:hover:not(:disabled) {
  background: #4a634a;
  transform: translateY(-2px);
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
  align-items: center;
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