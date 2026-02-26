<template>
  <div class="page">
    <div class="deco leaf-left">🌱</div>
    <div class="deco leaf-right">🌻</div>

    <header class="navbar">
      <div class="logo">Bloommood</div>
      <nav>
        <span @click="router.push('/home')">今日</span>
        <span @click="router.push('/garden')">花園</span>
        <span class="active">日誌</span>
        <span @click="router.push('/statistics')">情緒脈動</span>
        <span @click="router.push('/setting')">設定</span>
      </nav>
      <div class="avatar" @click="handleLogout">登出</div>
    </header>

    <main class="content">
      <div class="container journal-layout">
        <aside class="timeline-sidebar">
          <h2 class="sidebar-title">情緒足跡</h2>
          
          <div class="search-area">
            <input 
              type="date" 
              v-model="targetDate" 
              @change="jumpToDate"
              class="date-picker"
            />
          </div>

          <div class="timeline-list">
            <div 
              v-for="(record, index) in records" 
              :key="record.id" 
              :class="['timeline-item', { active: selectedIndex === index }]"
              @click="selectedIndex = index"
            >
              <div class="timeline-date">{{ record.date }}</div>
              <div class="timeline-plant">{{ record.plant }}</div>
            </div>
            <div v-if="records.length === 0" class="empty-state">尚無紀錄</div>
          </div>
        </aside>

        <section class="detail-panel">
          <div v-if="currentRecord" class="glass-card detail-card">
            <div class="detail-header">
              <div class="date-badge">{{ currentRecord.date }} 的回憶</div>
              <div class="plant-display">
                <span class="big-plant">{{ currentRecord.plant }}</span>
                <p>當天種下的心情</p>
              </div>
            </div>

            <div class="stats-row">
              <div class="stat-box">💧 <span>{{ currentRecord.stats?.water || 0 }}</span> 次澆水</div>
              <div class="stat-box">☀️ <span>{{ currentRecord.stats?.sun || 0 }}</span> 次陽光</div>
              <div class="stat-box">✨ <span>{{ currentRecord.stats?.fertilize || 0 }}</span> 次施肥</div>
            </div>

            <div class="chat-history-view">
              <h3>當時的對話</h3>
              <div class="chat-container">
                <div v-for="(msg, i) in currentRecord.chatLog" :key="i" :class="['bubble', msg.type]">
                  {{ msg.text }}
                </div>
              </div>
            </div>
          </div>
          <div v-else class="glass-card empty-card">
            <div class="empty-content">
              <span>📖</span>
              <p>{{ records.length > 0 ? '請選取日期或從清單中點擊' : '尚無紀錄，去種植第一棵植物吧！' }}</p>
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

const router = useRouter();
const records = ref([]);
const selectedIndex = ref(-1); // 預設不選取任何一項
const targetDate = ref(''); // 綁定日期選擇器

onMounted(() => {
  const saved = localStorage.getItem('bloom_records');
  if (saved) {
    // 假設儲存格式是新到舊，這裡我們將它解析出來
    records.value = JSON.parse(saved).reverse();
    // 預設顯示第一筆（最近的一筆）
    if (records.value.length > 0) selectedIndex.value = 0;
  }
});

const currentRecord = computed(() => records.value[selectedIndex.value] || null);

// 跳轉到特定日期的邏輯
const jumpToDate = () => {
  if (!targetDate.value) return;
  
  // 在 records 中尋找日期符合的 index
  // 提示：確保你的 record.date 格式與 HTML5 Date input 的 YYYY-MM-DD 一致
  const index = records.value.findIndex(r => r.date === targetDate.value);
  
  if (index !== -1) {
    selectedIndex.value = index;
    // 小優化：自動滾動到該元素 (可選)
  } else {
    alert(`找不到 ${targetDate.value} 的紀錄喔！`);
  }
};

const handleLogout = () => { if (confirm("確定登出嗎？")) router.push('/'); };
</script>

<style scoped>
/* 保持原有的樣式並新增以下部分 */

.search-area {
  margin-bottom: 20px;
}

.date-picker {
  width: 100%;
  padding: 10px 15px;
  border-radius: 12px;
  border: 1px solid rgba(93, 122, 93, 0.3);
  background: rgba(255, 255, 255, 0.6);
  color: #4a5d4a;
  font-family: inherit;
  outline: none;
  cursor: pointer;
  transition: 0.3s;
}

.date-picker:focus {
  background: white;
  border-color: #5d7a5d;
  box-shadow: 0 0 10px rgba(93, 122, 93, 0.1);
}

/* 為了讓視覺更平衡，微調原有樣式 */
.timeline-sidebar {
  flex: 3;
  display: flex;
  flex-direction: column;
  max-height: calc(100vh - 120px); /* 限制高度避免溢出 */
}

.sidebar-title {
  color: #4a5d4a;
  margin-bottom: 15px;
  font-size: 1.2rem;
}

/* 原有樣式... (省略) */
* { box-sizing: border-box; }
.page { height: 100vh; width: 100vw; background: linear-gradient(135deg, #f0f4f0 0%, #fefae0 100%); display: flex; flex-direction: column; overflow: hidden; font-family: 'PingFang TC', sans-serif; }
.navbar { height: 60px; display: flex; align-items: center; justify-content: space-between; padding: 0 40px; background: rgba(255, 255, 255, 0.3); backdrop-filter: blur(10px); border-bottom: 1px solid rgba(255, 255, 255, 0.2); z-index: 10; }
.logo { font-weight: 700; font-size: 22px; color: #4a5d4a; letter-spacing: 1px; }
nav span { margin: 0 15px; color: #556b55; cursor: pointer; }
nav span.active { color: #2d3a2d; font-weight: bold; border-bottom: 2px solid #5d7a5d; }
.content { flex: 1; padding: 20px 40px; display: flex; justify-content: center; overflow: hidden; }
.journal-layout { display: flex; gap: 30px; width: 100%; max-width: 1200px; }
.timeline-list { flex: 1; overflow-y: auto; padding-right: 10px; }
.timeline-item { background: rgba(255, 255, 255, 0.4); margin-bottom: 12px; padding: 15px 20px; border-radius: 16px; display: flex; justify-content: space-between; cursor: pointer; transition: 0.3s; border: 1px solid transparent; }
.timeline-item.active { background: white; border-color: #5d7a5d; transform: translateX(5px); box-shadow: 0 4px 12px rgba(0,0,0,0.05); }
.detail-panel { flex: 7; height: 100%; overflow: hidden; }
.glass-card { background: rgba(255, 255, 255, 0.5); backdrop-filter: blur(15px); border-radius: 24px; padding: 30px; height: 100%; display: flex; flex-direction: column; border: 1px solid rgba(255,255,255,0.4); }
.stats-row { display: flex; gap: 15px; margin-bottom: 30px; }
.stat-box { flex: 1; background: rgba(255, 255, 255, 0.4); padding: 15px; border-radius: 15px; text-align: center; color: #4a5d4a; font-weight: 500; }
.chat-history-view { flex: 1; display: flex; flex-direction: column; min-height: 0; }
.chat-container { flex: 1; overflow-y: auto; background: rgba(255, 255, 255, 0.2); border-radius: 15px; padding: 15px; display: flex; flex-direction: column; }
.bubble { padding: 10px 16px; border-radius: 18px; margin-bottom: 10px; max-width: 85%; font-size: 14px; line-height: 1.5; }
.user { background: #e8f0e8; align-self: flex-end; color: #3e4d3e; }
.system { background: white; align-self: flex-start; color: #4a5d4a; border: 1px solid rgba(0,0,0,0.05); }
.deco { position: absolute; font-size: 150px; opacity: 0.06; pointer-events: none; }
.leaf-left { bottom: -30px; left: -30px; transform: rotate(15deg); }
.leaf-right { top: 10%; right: -40px; transform: rotate(-10deg); }
.empty-card { justify-content: center; align-items: center; text-align: center; color: #889a88; }
.empty-content span { font-size: 50px; display: block; margin-bottom: 10px; }
.date-badge { background: #5d7a5d; color: white; padding: 5px 15px; border-radius: 20px; font-size: 0.9rem; display: inline-block; margin-bottom: 15px; }
.big-plant { font-size: 4rem; display: block; margin-bottom: 5px; }
</style>