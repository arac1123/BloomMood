<template>
  <div class="page">
    <div class="deco leaf-left">🍃</div>
    <div class="deco leaf-right">🌿</div>

    <header class="navbar">
      <div class="logo">Bloommood</div>
      <div class="avatar">YT</div>
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

          <label class="checkbox">
            <input v-model="formData.agree" type="checkbox" required />
            <span>我同意服務條款與隱私政策</span>
          </label>

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
  const res = await fetch(`http://localhost:3001/api/auth/register`, {
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
/* 樣式部分保持不變 */
.page {
  min-height: 100vh;
  background: linear-gradient(135deg, #d4e0d4 0%, #fefae0 100%);
  display: flex;
  flex-direction: column;
  position: relative;
  overflow: hidden;
  font-family: 'PingFang TC', 'Microsoft JhengHei', sans-serif;
}

.navbar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  background: rgba(255, 255, 255, 0.3);
  backdrop-filter: blur(10px);
  padding: 16px 40px;
  border-bottom: 1px solid rgba(255, 255, 255, 0.2);
  z-index: 10;
}

.logo { font-weight: 700; font-size: 22px; color: #4a5d4a; letter-spacing: 1px; }

.avatar {
  width: 38px; height: 38px; border-radius: 50%;
  background: #3e4e3e; color: #fff;
  display: flex; align-items: center; justify-content: center; font-weight: bold;
}

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
  z-index: 5;
}

.title { margin: 0 0 12px 0; font-size: 24px; font-weight: bold; color: #222; }
.subtitle { margin-bottom: 35px; color: #6a7a6a; font-size: 18px; display: flex; align-items: center; }

.form { display: flex; flex-direction: column; gap: 18px; }

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

.checkbox {
  display: flex;
  align-items: center;
  gap: 10px;
  cursor: pointer;
  font-size: 14px;
  color: #556b55;
  margin: 5px 0 10px 0;
}

.checkbox input {
  width: 18px; height: 18px; accent-color: #5d7a5d; flex-shrink: 0;
}

.btn {
  width: 100%; padding: 16px;
  background: #5d7a5d; color: #ffffff;
  border: none; border-radius: 15px;
  cursor: pointer; font-size: 16px; font-weight: 600;
  box-shadow: 0 8px 20px rgba(93, 122, 93, 0.2);
  transition: all 0.3s ease;
}

.btn:hover:not(:disabled) {
  background: #4a634a;
  transform: translateY(-2px);
}

.btn:disabled {
  opacity: 0.7;
  cursor: not-allowed;
  background: #8a9a8a;
}

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

.link-text:hover { border-bottom: 1px solid #5d7a5d; }

.deco { position: absolute; font-size: 150px; opacity: 0.15; pointer-events: none; z-index: 1; filter: blur(1px); }
.leaf-left { bottom: -30px; left: -30px; transform: rotate(15deg); }
.leaf-right { top: 20%; right: -50px; transform: rotate(-20deg); font-size: 120px; }

@keyframes breathe {
  0%, 100% { transform: scale(1); opacity: 0.8; }
  50% { transform: scale(1.2); opacity: 1; }
}
.breathing-emoji { display: inline-block; margin-right: 10px; animation: breathe 3s infinite ease-in-out; }
</style>