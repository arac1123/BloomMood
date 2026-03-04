<template>
  <div class="page">
    <div class="deco leaf-left">🌱</div>
    <div class="deco leaf-right">🌻</div>

    <Header active-tab="statistics" />

    <main class="content">
      <div class="container pulse-layout">
        
        <section class="summary-section">
          <div class="glass-card summary-card">
            <div class="stat-item">
              <span class="label">觀察天數</span>
              <span class="value">{{ records.length }}</span>
            </div>
            <div class="stat-item">
              <span class="label">目前節律</span>
              <span class="value">{{ currentRhythmEmoji }}</span>
            </div>
            <div class="stat-item">
              <span class="label">心靈能量</span>
              <span class="value">{{ totalInteractions }} ✨</span>
            </div>
          </div>
        </section>

        <section class="chart-section">
          <div class="glass-card chart-card">
            <div class="card-header-row">
              <h2 class="card-title">近期情緒脈動</h2>
              <span class="pulse-indicator">● Live</span>
            </div>
            
            <div class="chart-container">
              <svg v-if="points.length > 1" width="100%" height="100%" viewBox="0 0 1000 300" preserveAspectRatio="none">
                <defs>
                  <linearGradient id="pulseGradient" x1="0" y1="0" x2="0" y2="1">
                    <stop offset="0%" stop-color="#5d7a5d" stop-opacity="0.25" />
                    <stop offset="100%" stop-color="#5d7a5d" stop-opacity="0" />
                  </linearGradient>
                </defs>

                <path :d="fillPath" fill="url(#pulseGradient)" />
                
                <path :d="linePath" fill="none" stroke="#5d7a5d" stroke-width="3" stroke-linecap="round" class="main-pulse-line" />

                <g v-for="(p, i) in points" :key="i">
                  <circle :cx="p.x" :cy="p.y" r="8" fill="rgba(93, 122, 93, 0.2)" class="pulse-ring" />
                  <circle :cx="p.x" :cy="p.y" r="4" fill="#5d7a5d" />
                </g>
              </svg>
              
              <div v-else class="empty-state">待種植紀錄增加後，脈動將會在此顯現</div>
            </div>

            <div class="chart-footer">
              <span>時間軸：由遠至近 (近 7 筆數據)</span>
            </div>
          </div>
        </section>

        <section class="energy-section">
          <div class="glass-card energy-card">
            <h2 class="card-title">心靈照顧分佈</h2>
            <div class="progress-container">
              <div class="bar-group">
                <div class="bar-label">💧 疏導 (澆水)</div>
                <div class="bar-track"><div class="bar-fill water" :style="{ width: getPercentage('water') + '%' }"></div></div>
              </div>
              <div class="bar-group">
                <div class="bar-label">☀️ 正念 (陽光)</div>
                <div class="bar-track"><div class="bar-fill sun" :style="{ width: getPercentage('sun') + '%' }"></div></div>
              </div>
              <div class="bar-group">
                <div class="bar-label">✨ 滋養 (施肥)</div>
                <div class="bar-track"><div class="bar-fill talk" :style="{ width: getPercentage('fertilize') + '%' }"></div></div>
              </div>
            </div>
          </div>
        </section>

      </div>
    </main>
  </div>
</template>

<script setup>
import { ref, onMounted, computed } from 'vue';
import { useRouter } from 'vue-router';
// 2. 引入封裝好的 Header
import Header from '../components/header.vue'; 

const router = useRouter();
const records = ref([]);

onMounted(() => {
  const saved = localStorage.getItem('bloom_records');
  if (saved) {
    // 取得最近 7 筆紀錄做分析
    records.value = JSON.parse(saved).slice(-7);
  }
});

// 定義不同植物對應的分數，用於畫曲線圖
const moodScores = {
  '✨': 2, '🍀': 1, '🌸': 1, '🌼': 1, '🌻': 1, '🌷': 1,
  '🌱': 0.5, '🌿': 0.5, '☁️': 0,
  '😰': -1, '😢': -1.5, '💢': -2, '🌵': -0.5
};

const points = computed(() => {
  if (records.value.length < 2) return [];
  const width = 1000;
  const height = 300;
  const padding = 60;
  const usableHeight = height - (padding * 2);
  
  return records.value.map((r, i) => {
    const score = moodScores[r.plant] || 0;
    const x = (i / (records.value.length - 1)) * width;
    const y = height / 2 - (score * (usableHeight / 4));
    return { x, y };
  });
});

const linePath = computed(() => {
  if (points.value.length === 0) return '';
  let d = `M ${points.value[0].x} ${points.value[0].y}`;
  for (let i = 0; i < points.value.length - 1; i++) {
    const p0 = points.value[i];
    const p1 = points.value[i + 1];
    const cpX = (p0.x + p1.x) / 2;
    d += ` C ${cpX} ${p0.y}, ${cpX} ${p1.y}, ${p1.x} ${p1.y}`;
  }
  return d;
});

const fillPath = computed(() => {
  if (points.value.length === 0) return '';
  return linePath.value + ` L ${points.value[points.value.length - 1].x} 300 L 0 300 Z`;
});

const statsTotal = computed(() => {
  return records.value.reduce((acc, r) => {
    acc.water += r.stats?.water || 0;
    acc.sun += r.stats?.sun || 0;
    acc.fertilize += r.stats?.fertilize || 0;
    return acc;
  }, { water: 0, sun: 0, fertilize: 0 });
});

const totalInteractions = computed(() => statsTotal.value.water + statsTotal.value.sun + statsTotal.value.fertilize);

const getPercentage = (type) => {
  if (totalInteractions.value === 0) return 0;
  return (statsTotal.value[type] / totalInteractions.value) * 100;
};

const currentRhythmEmoji = computed(() => {
  if (records.value.length === 0) return '待測';
  return records.value[records.value.length - 1].plant;
});

// handleLogout 已搬移至元件內，此處移除
</script>

<style scoped>
* { box-sizing: border-box; }

.page { height: 100vh; width: 100vw; background: linear-gradient(135deg, #f0f4f0 0%, #fefae0 100%); display: flex; flex-direction: column; overflow: hidden; font-family: 'PingFang TC', sans-serif; }

.content { flex: 1; min-height: 0; padding: 20px 40px; }
.container { width: 100%; max-width: 1000px; height: 100%; display: flex; flex-direction: column; gap: 20px; margin: 0 auto; }

.glass-card { 
  background: rgba(255, 255, 255, 0.5); backdrop-filter: blur(15px); 
  border-radius: 24px; border: 1px solid rgba(255, 255, 255, 0.6); 
  padding: 20px;
}

.card-title { font-size: 15px; color: #4a5d4a; margin: 0; font-weight: 600; }

/* 脈動指示燈動畫 */
.card-header-row { display: flex; justify-content: space-between; align-items: center; margin-bottom: 15px; }
.pulse-indicator { font-size: 12px; color: #5d7a5d; animation: blink 2s infinite; }
@keyframes blink { 0%, 100% { opacity: 1; } 50% { opacity: 0.3; } }

/* 概覽區域 */
.summary-card { display: flex; justify-content: space-around; padding: 20px; }
.stat-item { text-align: center; }
.stat-item .label { display: block; font-size: 12px; color: #7a8a7a; margin-bottom: 4px; }
.stat-item .value { font-size: 22px; font-weight: bold; color: #4a5d4a; }

/* 圖表區域 */
.chart-section { flex: 1; min-height: 0; }
.chart-card { height: 100%; display: flex; flex-direction: column; }
.chart-container { flex: 1; min-height: 0; padding: 10px 0; position: relative; }

.pulse-ring { animation: ring-grow 2s infinite; transform-origin: center; }
@keyframes ring-grow { 0% { r: 4; opacity: 0.5; } 100% { r: 12; opacity: 0; } }

.chart-footer { text-align: center; padding-top: 10px; font-size: 11px; color: #7a8a7a; border-top: 1px solid rgba(0,0,0,0.03); }

/* 下方進度條 */
.progress-container { display: flex; flex-direction: column; gap: 12px; padding-top: 10px; }
.bar-group { display: flex; flex-direction: column; }
.bar-label { font-size: 12px; color: #556b55; margin-bottom: 4px; }
.bar-track { height: 8px; background: rgba(0,0,0,0.05); border-radius: 10px; overflow: hidden; }
.bar-fill { height: 100%; border-radius: 10px; transition: width 1s cubic-bezier(0.1, 0.5, 0.5, 1); }

.water { background: #90caf9; box-shadow: 0 0 10px rgba(144, 202, 249, 0.4); }
.sun { background: #fff59d; box-shadow: 0 0 10px rgba(255, 245, 157, 0.4); }
.talk { background: #a5d6a7; box-shadow: 0 0 10px rgba(165, 214, 167, 0.4); }

.empty-state { position: absolute; top: 50%; left: 50%; transform: translate(-50%, -50%); color: #889a88; font-size: 14px; }

/* 裝飾元件 */
.deco { position: absolute; font-size: 150px; opacity: 0.06; pointer-events: none; }
.leaf-left { bottom: -30px; left: -30px; transform: rotate(15deg); }
.leaf-right { top: 10%; right: -40px; transform: rotate(-10deg); }

@media (max-width: 768px) {
  .summary-card { flex-direction: column; gap: 15px; }
}
</style>