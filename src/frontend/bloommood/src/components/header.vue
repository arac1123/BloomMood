<template>
  <header class="navbar">
    <div class="logo">Bloommood</div>
    <nav>
      <span :class="{ active: activeTab === 'home' }" @click="router.push('/home')">今日</span>
      <span :class="{ active: activeTab === 'garden' }" @click="router.push('/garden')">花園</span>
      <span :class="{ active: activeTab === 'journal' }" @click="router.push('/journal')">日誌</span>
      <span :class="{ active: activeTab === 'statistics' }" @click="router.push('/statistics')">情緒脈動</span>
      <span :class="{ active: activeTab === 'setting' }" @click="router.push('/setting')">設定</span>
    </nav>
    <div class="avatar" @click="handleLogout" title="登出">登出</div>
  </header>
</template>

<script setup>
import { useRouter } from 'vue-router';

// 定義 Props，接收目前所在的頁面名稱
const props = defineProps({
  activeTab: {
    type: String,
    default: 'home'
  }
});

const router = useRouter();

const handleLogout = async () => {
  if (!confirm("確定要登出嗎？")) return;
  try {
    const res = await fetch("http://localhost:3001/api/auth/logout", {
      method: "POST",
      credentials: "include"
    });
    if (res.ok) {
      alert("已成功登出");
      router.push("/"); // 登出後跳轉回登入頁
    } else {
      alert("登出失敗");
    }
  } catch (err) {
    console.error(err);
    alert("登出發生錯誤");
  }
};
</script>

<style scoped>
.navbar {
  height: 60px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 40px;
  background: rgba(255, 255, 255, 0.3);
  backdrop-filter: blur(10px);
  border-bottom: 1px solid rgba(255, 255, 255, 0.2);
  z-index: 100; /* 確保在最上層 */
}

.logo {
  font-weight: 700;
  font-size: 22px;
  color: #4a5d4a;
  letter-spacing: 1px;
}

nav span {
  margin: 0 15px;
  color: #556b55;
  cursor: pointer;
  transition: color 0.3s;
}

nav span:hover {
  color: #2d3a2d;
}

nav span.active {
  color: #2d3a2d;
  font-weight: bold;
  border-bottom: 2px solid #5d7a5d;
}

.avatar {
  cursor: pointer;
  color: #556b55;
  font-size: 14px;
}
</style>