<template>
  <div class="page page-bg-main">
    <div class="deco leaf-left">🌱</div>
    <div class="deco leaf-right">🌻</div>

    <Header active-tab="journal" />

    <main class="content">
      <div class="container calendar-layout">
        <section class="calendar-main">
          <div class="calendar-header">
            <div class="year-nav">
              <button @click="changeYear(-1)" class="nav-btn">❮</button>
              <span class="year-text">{{ selectedYear }} 年</span>
              <button @click="changeYear(1)" class="nav-btn">❯</button>
            </div>
            <div class="month-nav">
              <button v-for="m in 12" :key="m" 
                :class="['m-item', { active: selectedMonth === m }]"
                @click="selectedMonth = m">
                {{ m }}月
              </button>
            </div>
          </div>

          <div class="calendar-grid">
            <div v-for="day in ['日', '一', '二', '三', '四', '五', '六']" :key="day" class="weekday-label">
              {{ day }}
            </div>
            
            <div v-for="empty in firstDayOffset" :key="'empty-'+empty" class="calendar-day empty"></div>

            <div 
              v-for="date in daysInMonth" 
              :key="date" 
              :class="['calendar-day', { 
                'has-record': hasPlantRecord(date),
                'selected': isSelected(date)
              }]"
              @click="selectDate(date)"
            >
              <span class="day-num">{{ date }}</span>
              <div v-if="hasPlantRecord(date)" class="plant-icon">
                <img
                  :src="getPlantImageByDate(date)"
                  alt="plant"
                  class="plant-thumb"
                />
              </div>
            </div>
          </div>
        </section>

        <aside class="detail-sidebar">
          <div v-if="currentRecord && currentRecord.hasPlant" class="glass-card detail-card">
            <div class="detail-header">
              <div class="date-badge">{{ currentRecord.date }}</div>
              <div class="plant-display">
                <img
                  :src="currentRecordPlantImage"
                  alt="plant"
                  class="big-plant-img"
                />
              </div>
            </div>

            <div class="stats-mini">
              <span>💧 {{ currentRecord.stats?.water || 0 }}</span>
              <span>☀️ {{ currentRecord.stats?.sun || 0 }}</span>
              <span>✨ {{ currentRecord.stats?.fertilize || 0 }}</span>
            </div>

            <button @click="deleteCurrentDayRecord" class="delete-btn" :disabled="isDeleting">
              {{ isDeleting ? '刪除中...' : '🗑️ 刪除當日紀錄' }}
            </button>

            <div class="chat-history-view">
              <h3>心靈對話</h3>
              <div class="chat-container">
                <div v-for="(msg, i) in currentChatLog" :key="i" :class="['bubble', msg.type]">
                  {{ msg.text }}
                </div>
              </div>
            </div>
          </div>
          
          <div v-else class="glass-card empty-card">
            <div class="empty-content">
              <span>🍃</span>
              <div v-if="isSelectedToday">
                <p>今天還沒有種下植物喔！</p>
                <button @click="plantToday" class="plant-btn" :disabled="isLoading">
                  {{ isLoading ? '種植中...' : '🌱 種下今天的植物' }}
                </button>
              </div>
              <p v-else>點選月曆上的日期<br>查看當時的心情</p>
            </div>
          </div>
        </aside>
      </div>
    </main>
  </div>
</template>

<script setup>
import { ref, onMounted, computed, watch } from 'vue';
import Header from '../components/header.vue'; // 請確認你的路徑是否正確

// --- 狀態管理 ---
const apiBaseUrl = import.meta.env.VITE_API_BASE_URL || 'http://localhost:3001';
const CHAT_LOG_PREFIX = 'bloommood.chatLog.';
const plantImageModules = import.meta.glob('../assets/image/*.{png,jpg,jpeg,webp,svg}', {
  eager: true,
  import: 'default'
});
const plantTypeImagePrefixMap = {
  flower: 'sunflower',
  cactus: 'cactus',
  tree: 'tree'
};
const defaultPlantImage = plantImageModules['../assets/image/sunflower_stage1.png'] || '';
const records = ref([]);
const selectedYear = ref(new Date().getFullYear());
const selectedMonth = ref(new Date().getMonth() + 1);
const selectedDateNum = ref(new Date().getDate()); // 預設選中今天
const isLoading = ref(false); // 控制按鈕載入狀態
const isDeleting = ref(false);
const todayPlant = ref(null);

const getChatStorageKey = (dateStr) => `${CHAT_LOG_PREFIX}${dateStr}`;

const buildDateKey = (year, month, day) => {
  const y = String(year).padStart(4, '0');
  const m = String(month).padStart(2, '0');
  const d = String(day).padStart(2, '0');
  return `${y}-${m}-${d}`;
};

const normalizeDateKey = (value) => {
  if (!value) return '';
  if (Array.isArray(value) && value.length >= 3) {
    return buildDateKey(value[0], value[1], value[2]);
  }
  if (typeof value === 'object' && value.year && value.month && value.day) {
    return buildDateKey(value.year, value.month, value.day);
  }
  if (typeof value === 'string') {
    const trimmed = value.slice(0, 10);
    const parts = trimmed.split('-');
    if (parts.length === 3) {
      return buildDateKey(parts[0], parts[1], parts[2]);
    }
  }
  return '';
};

const readChatLog = (dateStr) => {
  if (typeof window === 'undefined') return [];
  try {
    const raw = window.localStorage.getItem(getChatStorageKey(dateStr));
    const parsed = raw ? JSON.parse(raw) : [];
    return Array.isArray(parsed) ? parsed : [];
  } catch (e) {
    return [];
  }
};

const recordsByDate = computed(() => {
  const map = new Map();
  records.value.forEach((record) => {
    const key = normalizeDateKey(record?.date);
    if (key) map.set(key, record);
  });
  return map;
});

// --- API 呼叫 ---

// 1. 查今天有沒有種 (GET /api/plant/today)
const fetchTodayPlant = async () => {
  try {
    const res = await fetch(`${apiBaseUrl}/api/plant/today`, {
      method: "GET",
      credentials: "include"
    });

    if (!res.ok) throw new Error('取得今日資料失敗');

    const data = await res.json();
    todayPlant.value = data?.hasPlant ? data.plant : null;
  } catch (error) {
    console.error("今日植物 API 發生錯誤:", error);
  }
};

// 2. 建立今天的植物 (POST /api/plant/today)
const plantToday = async () => {
  isLoading.value = true;
  try {
    const res = await fetch(`${apiBaseUrl}/api/plant/today`, {
      method: "POST",
      credentials: "include",
      headers: {
        "Content-Type": "application/json"
      },
      body: JSON.stringify({ type: "FLOWER" }) // 依據你的需求決定傳什麼 type
    });

    if (!res.ok) throw new Error('種植失敗');
    
    console.log("種植成功！");
        await fetchTodayPlant();
    await fetchMonthData();
  } catch (error) {
    console.error("種植 API 發生錯誤:", error);
    alert("種植失敗，請稍後再試！");
  } finally {
    isLoading.value = false;
  }
};

// 3. 取得當月資料 (GET /api/plant/month?ym=YYYY-MM)
const fetchMonthData = async () => {
  try {
    const ym = `${selectedYear.value}-${String(selectedMonth.value).padStart(2, '0')}`;
    const res = await fetch(`${apiBaseUrl}/api/plant/month?ym=${ym}`, {
      method: "GET",
      credentials: "include"
    });

    if (!res.ok) throw new Error('取得資料失敗');
    
    // 假設後端回傳的是陣列格式，如果有包在 data 裡請改成 await res.json().then(res => res.data)
    records.value = await res.json(); 
  } catch (error) {
    console.error("月曆 API 發生錯誤:", error);
  }
};

// 4. 更新植物資料 (PATCH /api/plant/{pid})
// body: { status: "WITHERED", stage: 2, type: "TREE" }
const updatePlant = async (pid) => {
  try {
    const requestBody = {
      status: "WITHERED",
      stage: 2,
      type: "TREE"
    };

    const res = await fetch(`${apiBaseUrl}/api/plant/${pid}`, {
      method: "PATCH",
      credentials: "include",
      headers: {
        "Content-Type": "application/json"
      },
      body: JSON.stringify(requestBody)
    });

    if (!res.ok) throw new Error('更新植物失敗');

    const updatedPlant = await res.json();
    await fetchMonthData();
    return updatedPlant;
  } catch (error) {
    console.error("更新植物 API 發生錯誤:", error);
    alert("更新失敗，請稍後再試！");
    return null;
  }
};

// 5. 刪除植物資料 (DELETE /api/plant/{pid})
const deletePlant = async (pid) => {
  try {
    const res = await fetch(`${apiBaseUrl}/api/plant/${pid}`, {
      method: "DELETE",
      credentials: "include"
    });

    if (res.status === 404) {
      const errorBody = await res.json().catch(() => ({}));
      throw new Error(errorBody.message || 'plant not found');
    }

    if (!res.ok) throw new Error('刪除植物失敗');

    const result = await res.json();
    await fetchMonthData();
    return result;
  } catch (error) {
    console.error("刪除植物 API 發生錯誤:", error);
    alert("刪除失敗，請稍後再試！");
    return null;
  }
};

const deleteCurrentDayRecord = async () => {
  const pid = currentRecord.value?.plant?.pid;
  if (!pid) return;

  isDeleting.value = true;
  try {
    const ok = confirm('確定要刪除這株植物嗎？');
    if (!ok) return;

    await deletePlant(pid);
  } finally {
    isDeleting.value = false;
  }
};

// --- 生命週期與監聽 ---

onMounted(() => {
  fetchTodayPlant();
  fetchMonthData(); // 一進畫面就抓資料
});

// 監聽年月改變，自動重新抓資料
watch([selectedYear, selectedMonth], () => {
  fetchMonthData();
  selectedDateNum.value = null; // 換月時清空選取的日期
});

// --- 月曆運算邏輯 ---

const firstDayOffset = computed(() => {
  return new Date(selectedYear.value, selectedMonth.value - 1, 1).getDay();
});

const daysInMonth = computed(() => {
  return new Date(selectedYear.value, selectedMonth.value, 0).getDate();
});

// 取得特定日期的資料物件
const getRecordByDate = (dateNum) => {
  const dateStr = buildDateKey(selectedYear.value, selectedMonth.value, dateNum);
  return recordsByDate.value.get(dateStr);
};

const getPlantImagePath = (plant) => {
  const plantType = (plant?.type || plant || 'FLOWER').toString().toLowerCase();
  const prefix = plantTypeImagePrefixMap[plantType] || 'sunflower';
  const fileName = `${prefix}_stage1.png`;
  return plantImageModules[`../assets/image/${fileName}`] || defaultPlantImage;
};

const getPlantImageByDate = (dateNum) => {
  const record = getRecordByDate(dateNum);
  return getPlantImagePath(record?.plant);
};

// 判斷該天是否有植物 (API 規格說沒種的日子也會回傳，所以要判斷 hasPlant)
const hasPlantRecord = (dateNum) => {
  const record = getRecordByDate(dateNum);
  return record && record.hasPlant;
};

// 取得當前選中日期的詳細資料
const currentRecord = computed(() => {
  if (!selectedDateNum.value) return null;
  return getRecordByDate(selectedDateNum.value);
});

const currentRecordPlantImage = computed(() => getPlantImagePath(currentRecord.value?.plant));

const currentChatLog = computed(() => {
  if (!selectedDateNum.value) return [];
  const dateStr = buildDateKey(selectedYear.value, selectedMonth.value, selectedDateNum.value);
  return readChatLog(dateStr);
});

// 判斷選中的是不是「現實世界的今天」
const isSelectedToday = computed(() => {
  if (!selectedDateNum.value) return false;
  const today = new Date();
  return selectedYear.value === today.getFullYear() && 
         selectedMonth.value === today.getMonth() + 1 && 
         selectedDateNum.value === today.getDate();
});

const selectDate = (date) => {
  selectedDateNum.value = date;
};

const isSelected = (date) => selectedDateNum.value === date;

const changeYear = (step) => {
  selectedYear.value += step;
};
</script>

<style scoped>
/* 原本的 CSS 保留 */
.content { flex: 1; padding: 20px; display: flex; justify-content: center; overflow: hidden; }
.calendar-layout { display: flex; gap: 20px; width: 100%; max-width: 1300px; height: 100%; }

.calendar-main { flex: 7; background: rgba(255, 255, 255, 0.5); backdrop-filter: blur(10px); border-radius: 30px; padding: 25px; display: flex; flex-direction: column; box-shadow: 0 10px 30px rgba(0,0,0,0.05); }

.calendar-header { margin-bottom: 20px; }
.year-nav { display: flex; align-items: center; justify-content: center; gap: 15px; margin-bottom: 15px; }
.year-text { font-size: 1.4rem; font-weight: bold; color: #4a5d4a; }
.nav-btn { background: none; border: none; font-size: 1.2rem; color: #5d7a5d; cursor: pointer; }

.month-nav { display: flex; justify-content: space-between; border-bottom: 1px solid rgba(0,0,0,0.05); padding-bottom: 10px; }
.m-item { background: none; border: none; padding: 5px 10px; cursor: pointer; color: #889a88; border-radius: 8px; transition: 0.3s; }
.m-item.active { background: #5d7a5d; color: white; }

.calendar-grid { display: grid; grid-template-columns: repeat(7, 1fr); gap: 10px; flex: 1; }
.weekday-label { text-align: center; font-weight: bold; color: #889a88; padding: 10px 0; }
.calendar-day { background: rgba(255, 255, 255, 0.3); border-radius: 15px; position: relative; min-height: 80px; padding: 8px; cursor: pointer; transition: 0.3s; border: 2px solid transparent; }
.calendar-day:hover { background: rgba(255, 255, 255, 0.8); }
.calendar-day.selected { border-color: #5d7a5d; background: white; }
.calendar-day.empty { background: none; cursor: default; }

.day-num { font-size: 0.9rem; color: #4a5d4a; }
.plant-icon { font-size: 2rem; text-align: center; margin-top: 5px; }
.plant-thumb { width: 38px; height: 38px; object-fit: contain; display: block; margin: 4px auto 0; }

.detail-sidebar { flex: 3; height: 100%; }
.detail-card { height: 100%; display: flex; flex-direction: column; padding: 25px; }
.date-badge { background: #5d7a5d; color: white; padding: 5px 12px; border-radius: 12px; font-size: 0.85rem; display: inline-block; margin-bottom: 10px; }
.big-plant { font-size: 3.5rem; display: block; text-align: center; }
.big-plant-img { width: 100px; height: 100px; object-fit: contain; display: block; margin: 0 auto; }

.stats-mini { display: flex; justify-content: space-around; background: rgba(255,255,255,0.4); padding: 10px; border-radius: 12px; margin: 15px 0; font-size: 0.9rem; }

.chat-history-view { flex: 1; display: flex; flex-direction: column; min-height: 0; }
.chat-container { flex: 1; overflow-y: auto; background: rgba(255, 255, 255, 0.3); border-radius: 15px; padding: 12px; }
.bubble { padding: 8px 12px; border-radius: 15px; margin-bottom: 8px; font-size: 0.85rem; max-width: 90%; }
.user { background: #e8f0e8; align-self: flex-end; margin-left: auto; }
.system { background: white; align-self: flex-start; }

.empty-card { height: 100%; display: flex; justify-content: center; align-items: center; text-align: center; color: #889a88; padding: 25px; }
.empty-content { display: flex; flex-direction: column; align-items: center; gap: 15px; }
.empty-content span { font-size: 3rem; }

/* 新增：種植按鈕的樣式 */
.plant-btn { background: #5d7a5d; color: white; border: none; padding: 12px 24px; border-radius: 20px; font-size: 1rem; cursor: pointer; transition: 0.3s; margin-top: 10px; }
.plant-btn:hover { background: #4a5d4a; transform: translateY(-2px); }
.plant-btn:disabled { background: #a0b0a0; cursor: not-allowed; transform: none; }

.delete-btn { background: #ffffff; color: #5d7a5d; border: 1px solid #dbe4db; padding: 10px 16px; border-radius: 14px; font-size: 0.9rem; cursor: pointer; transition: 0.3s; margin-bottom: 12px; }
.delete-btn:hover { background: #f7fbf7; transform: translateY(-1px); }
.delete-btn:disabled { background: #f1f4f1; color: #9aa99a; cursor: not-allowed; transform: none; }

@media (max-width: 1100px) {
  .calendar-layout {
    flex-direction: column;
    height: auto;
  }

  .calendar-main,
  .detail-sidebar {
    width: 100%;
    flex: unset;
  }

  .detail-sidebar {
    height: auto;
  }

  .detail-card,
  .empty-card {
    min-height: 320px;
  }
}

@media (max-width: 768px) {
  .content {
    padding: 12px;
  }

  .calendar-main {
    padding: 16px;
    border-radius: 20px;
  }

  .month-nav {
    gap: 6px;
    justify-content: flex-start;
    flex-wrap: wrap;
  }

  .m-item {
    padding: 4px 8px;
    font-size: 13px;
  }

  .calendar-grid {
    gap: 6px;
  }

  .calendar-day {
    min-height: 62px;
    padding: 6px;
  }

  .plant-thumb {
    width: 30px;
    height: 30px;
  }

  .detail-card,
  .empty-card {
    padding: 16px;
    border-radius: 18px;
  }
}

@media (max-width: 480px) {
  .year-text {
    font-size: 1.1rem;
  }

  .weekday-label {
    font-size: 12px;
    padding: 6px 0;
  }

  .day-num {
    font-size: 0.8rem;
  }

  .stats-mini {
    font-size: 0.8rem;
  }

  .chat-container {
    padding: 10px;
  }

  .bubble {
    font-size: 0.8rem;
  }
}

</style>