<template>
  <div class="page">
    <div class="deco leaf-left">🌱</div>
    <div class="deco leaf-right">🌻</div>

    <Header active-tab="home" />

    <main class="content">
      <div class="container home-container">
        <section class="welcome-hero">
          <div class="date-tag">{{ todayDate }}</div>
          <h1 class="welcome-title">早安，{{ userName }}</h1>
          <p class="welcome-subtitle">今天的內心花園，也正等待著你的灌溉。</p>
          <button class="start-btn" @click="router.push('/garden')">
            開始今日的紀錄 <span>➔</span>
          </button>
        </section>

        <section class="quick-overview">
          <div class="glass-card quote-card">
            <div class="card-icon">✨</div>
            <p class="quote-text">「無論今天天氣如何，記得在心裡為自己留一片陽光。」</p>
            <span class="quote-author">— Bloommood Daily</span>
          </div>

          <div class="glass-card status-card">
            <h3 class="card-label">花園狀態</h3>
            <div class="status-grid">
              <div class="status-item">
                <span class="num">{{ plantsCount }}</span>
                <span class="desc">已種植</span>
              </div>
              <div class="status-item">
                <span class="num">5</span>
                <span class="desc">連續天數</span>
              </div>
            </div>
            <div class="last-mood">
              最近的心情：<span>{{ lastMood }}</span>
            </div>
          </div>

          <div class="glass-card mini-chart-card" @click="router.push('/statistics')">
            <h3 class="card-label">情緒脈動預覽</h3>
            <div class="mini-wave">
              <svg viewBox="0 0 200 60" class="svg-wave">
                <path d="M0,40 C30,10 70,50 100,20 C130,-10 170,40 200,10" fill="none" stroke="#5d7a5d" stroke-width="3" />
              </svg>
            </div>
            <p class="click-hint">查看詳細趨勢 ➔</p>
          </div>
        </section>
      </div>
    </main>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue';
import { useRouter } from 'vue-router';
// 1. 引入 Header 元件
import Header from '../components/header.vue'; 

const router = useRouter();
const userName = ref('用戶');
const plantsCount = ref(0);
const lastMood = ref('尚未紀錄');
const todayDate = ref(new Date().toLocaleDateString('zh-TW', { month: 'long', day: 'numeric', weekday: 'long' }));

onMounted(() => {
  // 讀取使用者名稱
  const savedName = localStorage.getItem('user_name');
  if (savedName) userName.value = savedName;

  // 讀取花園數據
  const savedRecords = localStorage.getItem('bloom_records');
  if (savedRecords) {
    const records = JSON.parse(savedRecords);
    plantsCount.value = records.length;
    if (records.length > 0) {
      lastMood.value = records[records.length - 1].plant;
    }
  }
});

// handleLogout 已經在 Header 元件內處理，這裡可以安全移除
</script>

<style scoped>
/* 樣式部分：已移除 Navbar 相關 CSS，僅保留頁面專屬樣式 */
* { box-sizing: border-box; }
.page { height: 100vh; width: 100vw; background: linear-gradient(135deg, #f0f4f0 0%, #fefae0 100%); display: flex; flex-direction: column; overflow: hidden; font-family: 'PingFang TC', sans-serif; }

.content { flex: 1; display: flex; justify-content: center; align-items: center; padding: 0 40px; }
.container { width: 100%; max-width: 1100px; display: flex; flex-direction: column; gap: 50px; }

.welcome-hero { text-align: center; }
.date-tag { color: #889988; font-size: 14px; margin-bottom: 10px; font-weight: 500; }
.welcome-title { font-size: 42px; color: #2d3a2d; font-weight: 700; margin: 0; }
.welcome-subtitle { font-size: 18px; color: #556b55; margin-top: 10px; }

.start-btn { margin-top: 30px; background: #5d7a5d; color: white; border: none; padding: 16px 40px; border-radius: 50px; font-size: 18px; font-weight: 600; cursor: pointer; transition: 0.3s; box-shadow: 0 10px 20px rgba(93, 122, 93, 0.2); }
.start-btn:hover { transform: translateY(-3px); box-shadow: 0 15px 25px rgba(93, 122, 93, 0.3); background: #4a634a; }

.quick-overview { display: grid; grid-template-columns: repeat(3, 1fr); gap: 25px; }
.glass-card { background: rgba(255, 255, 255, 0.4); backdrop-filter: blur(15px); border-radius: 24px; border: 1px solid rgba(255, 255, 255, 0.5); padding: 25px; transition: 0.3s; cursor: default; }

.quote-card { display: flex; flex-direction: column; justify-content: center; }
.quote-text { font-size: 16px; color: #4a5d4a; font-style: italic; line-height: 1.6; margin: 10px 0; }
.quote-author { font-size: 12px; color: #889988; align-self: flex-end; }

.card-label { font-size: 14px; color: #7a8a7a; margin-bottom: 15px; font-weight: 600; }
.status-grid { display: flex; justify-content: space-around; margin-bottom: 15px; }
.status-item { text-align: center; }
.status-item .num { font-size: 28px; font-weight: 700; color: #5d7a5d; display: block; }
.status-item .desc { font-size: 12px; color: #889988; }
.last-mood { font-size: 13px; color: #556b55; text-align: center; border-top: 1px solid rgba(0,0,0,0.05); padding-top: 10px; }

.mini-chart-card { cursor: pointer; }
.mini-chart-card:hover { transform: translateY(-5px); background: rgba(255, 255, 255, 0.6); }
.svg-wave { width: 100%; height: 60px; }
.click-hint { font-size: 12px; color: #889988; text-align: right; margin-top: 10px; }

.deco { position: absolute; font-size: 150px; opacity: 0.06; pointer-events: none; }
.leaf-left { bottom: -30px; left: -30px; transform: rotate(15deg); }
.leaf-right { top: 10%; right: -40px; transform: rotate(-10deg); }
</style>