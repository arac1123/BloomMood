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
          <h2 class="title">註冊</h2>
          <div class="subtitle">
            <span class="breathing-emoji">🌱</span>
            開始你的療癒之旅
          </div>
        </header>

        <form class="form" @submit.prevent="handleRegister">
          <div class="input-group">
            <input
              v-model.trim="formData.email"
              type="email"
              placeholder="電子郵件"
              required
            />
          </div>

          <div class="input-group">
            <input
              v-model.trim="formData.username"
              type="text"
              placeholder="使用者名稱"
              required
              minlength="3"
            />
          </div>

          <div class="input-group">
            <input
              v-model="formData.password"
              type="password"
              placeholder="密碼 (至少 8 碼)"
              required
              minlength="8"
            />
          </div>

          <div class="input-group">
            <input
              v-model="formData.confirmPassword"
              type="password"
              placeholder="確認密碼"
              required
            />
          </div>

          <button
            type="submit"
            class="btn"
            :class="{ 'btn-loading': isLoading }"
            :disabled="isLoading"
          >
            <span v-if="!isLoading">註冊</span>
            <span v-else>🌱 生長中...</span>
          </button>
        </form>

        <footer class="auth-switch">
          <span>已經有帳號了？</span>
          <router-link to="/" class="link-text">登入花園</router-link>
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
const apiBaseUrl = import.meta.env.VITE_API_BASE_URL || 'http://localhost:3001';

const apiBaseUrl = import.meta.env.VITE_API_BASE_URL || 'http://localhost:3001';

const formData = reactive({
  email: '',
  username: '',
  password: '',
  confirmPassword: '',
  agree: false
});

// 處理註冊邏輯
const handleRegister = async () => {
  console.log('註冊資料:', formData);
  if (formData.password !== formData.confirmPassword) {
    alert('密碼與確認密碼不一致！');
    return;
  } 
  isLoading.value = true;
  const res = await fetch(`${apiBaseUrl}/api/auth/register`, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({
      email: formData.email,
      uname: formData.username,
      password: formData.password
    }),
    credentials: 'include'  
  });
  alert(res.ok ? '註冊成功！請登入。' : '註冊失敗，請再試一次。');
  if (res.ok) router.push('/home');
};
</script>

<style scoped>
/* 共用 auth 樣式已抽到 main.css，此處僅保留頁面專屬樣式 */
.navbar {
  justify-content: space-between;
}

.avatar {
  width: 38px; height: 38px; border-radius: 50%;
  background: #3e4e3e; color: #fff;
  display: flex; align-items: center; justify-content: center; font-weight: bold;
}

.btn:disabled {
  opacity: 0.7;
  cursor: not-allowed;
  background: #8a9a8a;
}

@keyframes breathe {
  0%, 100% { transform: scale(1); opacity: 0.8; }
  50% { transform: scale(1.2); opacity: 1; }
}
.breathing-emoji { display: inline-block; margin-right: 10px; animation: breathe 3s infinite ease-in-out; }
</style>