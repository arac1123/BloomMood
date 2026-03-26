<template>
  <div class="page">
    <div class="deco leaf-left">🌱</div>
    <div class="deco leaf-right">🌻</div>

    <Header active-tab="garden" />

    <main class="content">
      <div class="container">
        <div class="garden-layout">
          <section class="mood-panel">
            <div class="glass-card">
              <div class="chat-display" ref="chatScroll">
                <transition-group name="chat">
                  <div v-for="(msg, index) in chatHistory" :key="index" :class="['bubble', msg.type]">
                    {{ msg.text }}
                  </div>
                </transition-group>
              </div>
              <div class="emotion-shortcuts">
                <button v-for="e in emotions" :key="e.label" @click="quickInput(e.emoji + e.label)" class="shortcut-btn">
                  {{ e.emoji }} {{ e.label }}
                </button>
              </div>
              <div class="input-group">
                <div class="input-wrapper">
                  <input v-model="userInput" placeholder="寫下現在的心情..." @keyup.enter="sendMessage" />
                  <button class="send-btn" @click="sendMessage">➔</button>
                </div>
                <button class="plant-btn" @click="saveDailyRecord" :disabled="chatHistory.length < 2 || hasPlantedToday || isPlanting || isInteracting">
                  {{ isPlanting ? '🌱 種植中...' : (hasPlantedToday ? '🌟 今天已灌溉' : '✨ 將情緒種入花園') }}
                </button>
              </div>
            </div>
          </section>

          <section class="garden-panel">
            <div class="garden-header">
              <h1 class="garden-main-title">情緒花園</h1>
              <p class="garden-sub-title">將今天的感受種下，靜待它開花</p>
            </div>
            <div class="glass-card garden-canvas">
              <div v-if="isRaining" class="rain-overlay">
                <div v-for="drop in staticRain" :key="drop.id" class="drop" :style="drop.style"></div>
              </div>
              <transition name="sun-fade">
                <div v-if="isShining" class="sun-overlay">
                  <div class="the-sun"><div class="sun-glow"></div><div class="sun-icon">☀️</div></div>
                </div>
              </transition>
              <div v-if="isFertilizing" class="fertilize-overlay">
                <div v-for="s in staticSparkles" :key="s.id" class="sparkle" :style="s.style">✨</div>
              </div>
              
              <div class="mascot-bubble">
                {{ isGardenWithered ? '花園太久沒喝水了，快澆點水救救它們吧！' : '每一個心情，都值得被溫柔灌溉。' }}
              </div>
              <div v-if="latestInteractionText" class="interaction-hint">{{ latestInteractionText }}</div>
              <div v-if="apiNotice" class="api-notice">{{ apiNotice }}</div>

              <div class="garden-container">
                <div class="garden-scene" :class="{ 'withered-state': isGardenWithered }" :style="{ backgroundImage: `url(${gardenBackground})` }">
                  <div v-for="i in TOTAL_SLOTS" :key="i-1" class="plant-spot" :style="getSpotStyle(i-1)">
                    <div class="spot-marker"></div>
                    <transition name="grow" mode="out-in">
                      <div
                        v-if="getPlantInSlot(i-1)"
                        class="plant-item"
                        :class="[`stage-${getPlantInSlot(i-1).stage}`, { 'is-magic': getPlantInSlot(i-1).isMagic }]"
                      >
                        <div v-if="getPlantInSlot(i-1).isMagic" class="magic-burst"></div>
                        <div class="plant-wrapper">
                          <div class="plant-image">
                            <img 
                              :src="getPlantImagePath(getPlantInSlot(i-1))" 
                              :alt="getPlantInSlot(i-1).type"
                            />
                          </div>
                        </div>
                        <div class="plant-shadow"></div>
                      </div>
                    </transition>
                  </div>
                </div>
              </div>
              
              <div class="garden-actions">
                <button class="action-btn water" @click="triggerInteraction('WATER')" :disabled="isInteracting || isPlanting">💧 澆水</button>
                <button class="action-btn sun" @click="triggerInteraction('SUN')" :disabled="isInteracting || isPlanting">☀️ 陽光</button>
                <button class="action-btn talk" @click="triggerInteraction('FERTILIZE')" :disabled="isInteracting || isPlanting">✨ 施肥</button>
              </div>
            </div>
          </section>
        </div>
      </div>
    </main>
  </div>
</template>

<script setup>
import { ref, nextTick, onMounted, computed } from 'vue';
import Header from './header.vue';

// --- 1. 配置定義 ---
const PLANT_API_BASE_URL = 'http://localhost:3001/api/plant';
const ACTION_API_BASE_URL = 'http://localhost:3001/api/action';

const plantConfigs = {
  flower: { name: '花朵', prefix: 'sunflower' },
  cactus: { name: '仙人掌', prefix: 'cactus' },
  tree: { name: '樹', prefix: 'tree' }
};

const plantImageModules = import.meta.glob('../assets/image/*.{png,jpg,jpeg,webp,svg}', {
  eager: true,
  import: 'default'
});

const gardenBackground = plantImageModules['../assets/image/garden.png'] || '';

const PLANT_SPOTS = [
  { x: 18, y: 78 }, { x: 32, y: 76 }, { x: 48, y: 79 }, { x: 63, y: 77 },
  { x: 79, y: 78 }, { x: 24, y: 64 }, { x: 40, y: 62 }, { x: 57, y: 64 },
  { x: 73, y: 62 }, { x: 31, y: 50 }, { x: 50, y: 48 }, { x: 69, y: 50 }
];

const emotions = [
  { emoji: '💢', label: '生氣' }, { emoji: '☁️', label: '平靜' },
  { emoji: '😰', label: '焦慮' }, { emoji: '😢', label: '難過' }, { emoji: '✨', label: '開心' }
];

// --- 2. 狀態定義 ---
const userInput = ref('');
const chatScroll = ref(null);
const chatHistory = ref([{ type: 'system', text: '今天過得如何？跟我分享一點你的心情吧。' }]);
const plants = ref([]);
const TOTAL_SLOTS = 12;

const hasPlantedToday = ref(false); 
const isPlanting = ref(false);
const isInteracting = ref(false);
const apiNotice = ref('');

// 天氣與特效狀態
const isRaining = ref(false);
const isShining = ref(false);
const isFertilizing = ref(false);
let effectTimer = null;

// 計算目前整個花園是否有任何植物處於乾枯狀態
const isGardenWithered = computed(() => {
  return plants.value.some(p => p.status === 'WITHERED');
});

const latestInteractionText = computed(() => {
  if (!plants.value.length) return '';

  const withDays = plants.value.find(p => typeof p.daysSinceLastInteraction === 'number');
  if (withDays) {
    return `距離最近一次互動 ${withDays.daysSinceLastInteraction} 天`;
  }

  const withTime = plants.value.find(p => !!p.lastInteractionAt);
  if (withTime) {
    return `最近一次互動：${withTime.lastInteractionAt}`;
  }

  return '';
});

// --- 3. 核心邏輯方法 ---
const getPlantImagePath = (plant) => {
  // 確保無論後端回傳大寫還是小寫，都能正確對應到 plantConfigs
  const plantTypeKey = (plant.type || 'FLOWER').toLowerCase();
  const prefix = plantConfigs[plantTypeKey]?.prefix || 'sunflower';
  const fileName = `${prefix}_stage${plant.stage}.png`;
  
  return plantImageModules[`../assets/image/${fileName}`] || '';
};

const readErrorMessage = async (res, fallbackMessage) => {
  try {
    const data = await res.json();
    return data?.message || fallbackMessage;
  } catch (e) {
    return fallbackMessage;
  }
};

const getAvailableSlot = () => {
  const occupiedSlots = plants.value.map(p => p.slotIndex);
  for(let i = 0; i < TOTAL_SLOTS; i++) {
    if(!occupiedSlots.includes(i)) return i;
  }
  return null;
};

const getPlantInSlot = (index) => plants.value.find(p => p.slotIndex === index);

const getSpotStyle = (index) => {
  const spot = PLANT_SPOTS[index] || { x: 50, y: 75 };
  return { left: `${spot.x}%`, top: `${spot.y}%`, zIndex: Math.round(spot.y * 10) };
};

// --- 4. API 呼叫 ---

// 取得當前月份格式 (例如 '2026-03')
const getCurrentYearMonth = () => {
  const date = new Date();
  const year = date.getFullYear();
  const month = String(date.getMonth() + 1).padStart(2, '0');
  return `${year}-${month}`;
};

// API: 取得當月花園資料
const fetchMonthlyGarden = async () => {
  const ym = getCurrentYearMonth();
  try {
    const res = await fetch(`${PLANT_API_BASE_URL}/month?ym=${ym}`, {
      method: 'GET',
      credentials: 'include'
    });
    if (res.ok) {
      const monthRows = await res.json();
      const plantedRows = Array.isArray(monthRows)
        ? monthRows.filter(row => row?.hasPlant && row?.plant)
        : [];

      plants.value = plantedRows.slice(0, TOTAL_SLOTS).map((row, index) => {
        const plant = row.plant || {};
        return {
          ...plant,
          daysSinceLastInteraction: row.daysSinceLastInteraction ?? plant.daysSinceLastInteraction,
          lastInteractionAt: row.lastInteractionAt ?? plant.lastInteractionAt,
          slotIndex: Number.isInteger(plant.slotIndex) ? plant.slotIndex : index,
          isMagic: false
        };
      });
    }
  } catch (error) {
    console.error('取得當月花園失敗:', error);
  }
};

// API: 檢查今日是否已種植
const checkTodayPlant = async () => {
  try {
    const res = await fetch(`${PLANT_API_BASE_URL}/today`, {
      method: 'GET',
      credentials: 'include'
    });
    if (res.ok) {
      const data = await res.json();
      hasPlantedToday.value = data.hasPlant;
    }
  } catch (error) {
    console.error('取得今日植物狀態失敗:', error);
  }
};

// API: 建立今日的植物
const saveDailyRecord = async () => {
  if (hasPlantedToday.value) {
    alert("今天已經種下情緒囉，明天再來吧！");
    return;
  }

  if (isPlanting.value) return;
  isPlanting.value = true;
  apiNotice.value = '';

  const slot = getAvailableSlot();
  if (slot === null) {
    isPlanting.value = false;
    return alert("本月花園已經滿了！");
  }

  // 準備可用的植物類型 (配合後端的全大寫規格)
  const availableTypes = ['FLOWER', 'CACTUS', 'TREE'];
  const randomType = availableTypes[Math.floor(Math.random() * availableTypes.length)];

  try {
    const res = await fetch(`${PLANT_API_BASE_URL}/today`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({
        type: randomType
      }),
      credentials: 'include'
    });

    if (res.ok) {
      hasPlantedToday.value = true;
      await fetchMonthlyGarden(); // 重新拉取本月花園資料以更新畫面
    } else {
      apiNotice.value = await readErrorMessage(res, '種植失敗，請再試一次。');
    }
  } catch (error) {
    console.error('API 發生錯誤:', error);
    apiNotice.value = '種植失敗，請檢查網路連線。';
  } finally {
    isPlanting.value = false;
  }
};

// API: 與花園互動 (澆水、陽光、施肥)
const triggerInteraction = async (actionType) => {
  if (isInteracting.value) return;
  isInteracting.value = true;
  apiNotice.value = '';

  // 1. 觸發前端視覺天氣特效
  if (effectTimer) clearTimeout(effectTimer);
  isRaining.value = isShining.value = isFertilizing.value = false;

  if (actionType === 'WATER') isRaining.value = true;
  else if (actionType === 'SUN') isShining.value = true;
  else if (actionType === 'FERTILIZE') isFertilizing.value = true;

  effectTimer = setTimeout(() => { 
    isRaining.value = isShining.value = isFertilizing.value = false; 
  }, 2500);

  // 2. 呼叫後端 API 更新狀態
  try {
    const res = await fetch(`${ACTION_API_BASE_URL}`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ type: actionType }),
      credentials: 'include'
    });

    if (res.ok) {
      // 互動成功後，重新拉取最新資料 (讓前端顯示植物升級或解除枯萎)
      await fetchMonthlyGarden();
      
      // 給予所有植物短暫的魔法長大特效
      plants.value.forEach(p => p.isMagic = true);
      setTimeout(() => plants.value.forEach(p => p.isMagic = false), 1000);
    } else {
      apiNotice.value = await readErrorMessage(res, '互動失敗，請稍後再試。');
    }
  } catch (error) {
    console.error('互動失敗:', error);
    apiNotice.value = '互動失敗，請檢查網路連線。';
  } finally {
    isInteracting.value = false;
  }
};

// --- 5. 生命週期 ---
onMounted(() => {
  fetchMonthlyGarden(); // 載入本月花園
  checkTodayPlant();    // 檢查今天是否已種植
});

// --- 6. 聊天功能 ---
const sendMessage = async () => {
  if (!userInput.value.trim()) return;
  chatHistory.value.push({ type: 'user', text: userInput.value });
  userInput.value = '';
  await nextTick();
  chatScroll.value.scrollTop = chatScroll.value.scrollHeight;
  setTimeout(() => {
    chatHistory.value.push({ type: 'system', text: `謝謝你分享你的心情。` });
    nextTick(() => chatScroll.value.scrollTop = chatScroll.value.scrollHeight);
  }, 800);
};

const quickInput = (v) => userInput.value = v;
const staticRain = Array.from({ length: 30 }, (_, i) => ({ id: i, style: { left: Math.random() * 100 + '%', animationDelay: Math.random() * 2 + 's' } }));
const staticSparkles = Array.from({ length: 15 }, (_, i) => ({ id: i, style: { left: Math.random() * 100 + '%', animationDelay: Math.random() * 1 + 's' } }));
</script>

<style scoped>
/* --- 基礎佈局 --- */
.page { height: 100vh; width: 100vw; background: linear-gradient(135deg, #f0f4f0 0%, #fefae0 100%); display: flex; flex-direction: column; overflow: hidden; font-family: 'PingFang TC', sans-serif; }
.content { flex: 1; padding: 20px 40px; display: flex; justify-content: center; align-items: center; min-height: 0; }
.container { width: 100%; max-width: 1400px; height: 100%; display: flex; flex-direction: column; }
.garden-layout { display: flex; gap: 30px; flex: 1; min-height: 0; }
.glass-card { background: rgba(255, 255, 255, 0.5); backdrop-filter: blur(15px); border-radius: 24px; border: 1px solid rgba(255, 255, 255, 0.6); box-shadow: 0 10px 30px rgba(0, 0, 0, 0.05); display: flex; flex-direction: column; padding: 20px; height: 100%; overflow: hidden; }

/* --- 聊天面板 --- */
.mood-panel { flex: 4; }
.chat-display { flex: 1; overflow-y: auto; padding-right: 8px; margin-bottom: 15px; }
.bubble { padding: 10px 16px; border-radius: 18px; margin-bottom: 10px; font-size: 14px; line-height: 1.5; max-width: 85%; }
.system { background: #ffffff; color: #556b55; align-self: flex-start; border-bottom-left-radius: 4px; }
.user { background: #e8f0e8; color: #3e4e3e; align-self: flex-end; border-bottom-right-radius: 4px; margin-left: auto; }
.shortcut-btn { background: white; border: 1px solid #d0ddd0; padding: 5px 12px; border-radius: 50px; font-size: 12px; cursor: pointer; margin: 2px; }
.input-group { display: flex; flex-direction: column; gap: 10px; }
.input-wrapper { display: flex; background: white; border-radius: 12px; padding: 8px 12px; border: 1px solid #d0ddd0; }
.input-wrapper input { flex: 1; border: none; outline: none; }
.plant-btn { background: #5d7a5d; color: white; border: none; padding: 12px; border-radius: 12px; font-weight: bold; cursor: pointer; }
.plant-btn:disabled { background: #a0b2a0; cursor: not-allowed; }

/* --- 花園畫布 --- */
.garden-panel { flex: 7; display: flex; flex-direction: column; }
.garden-canvas { position: relative; background: rgba(255, 255, 255, 0.3); overflow: visible; }
.garden-main-title { font-size: 18px; color: #3e4e3e; margin: 0; }
.interaction-hint { font-size: 12px; color: #607360; margin: 8px 0 4px; text-align: center; }
.api-notice { font-size: 13px; color: #9b2d2d; background: rgba(255, 238, 238, 0.9); border: 1px solid rgba(214, 138, 138, 0.5); border-radius: 10px; padding: 8px 10px; margin: 6px 0 8px; text-align: center; }

/* --- 天氣效果 --- */
.rain-overlay, .sun-overlay, .fertilize-overlay { position: absolute; inset: 0; pointer-events: none; z-index: 10; }
.drop { position: absolute; width: 2px; height: 15px; background: rgba(174, 194, 224, 0.7); top: -20px; animation: rain 0.8s linear infinite; }
@keyframes rain { to { transform: translateY(800px); } }
.sun-icon { font-size: 60px; position: absolute; top: 10px; right: 20px; filter: drop-shadow(0 0 20px gold); }
.sparkle { position: absolute; bottom: 100px; animation: sparkleUp 2s ease-out forwards; }
@keyframes sparkleUp { from { transform: translateY(0) scale(0); opacity: 1; } to { transform: translateY(-100px) scale(1.5); opacity: 0; } }

/* --- 花園場景 --- */
.garden-container { flex: 1; display: flex; align-items: center; justify-content: center; padding: 10px 0 20px; }
.garden-scene {
  position: relative;
  width: 100%;
  max-width: 860px;
  aspect-ratio: 16 / 9;
  border-radius: 18px;
  background-size: cover;
  background-position: center;
  background-repeat: no-repeat;
  overflow: hidden;
}
.garden-scene::after {
  content: '';
  position: absolute;
  inset: 0;
  background: linear-gradient(to top, rgba(0, 0, 0, 0.12) 0%, rgba(0, 0, 0, 0) 45%);
  pointer-events: none;
}
.plant-spot {
  position: absolute;
  width: 78px;
  height: 90px;
  transform: translate(-50%, -100%);
  display: flex;
  justify-content: center;
  align-items: flex-end;
}
.spot-marker {
  position: absolute;
  bottom: 4px;
  width: 42px;
  height: 12px;
  border-radius: 50%;
  background: rgba(74, 45, 34, 0.35);
  box-shadow: inset 0 0 5px rgba(0, 0, 0, 0.2);
}

/* 乾枯狀態特效 */
.withered-state {
  filter: sepia(0.6) grayscale(0.5) brightness(0.8);
  transition: filter 1.5s ease-in-out;
}

/* --- 植物圖片與動畫 --- */
.plant-item { position: relative; z-index: 6; }
.plant-image { width: 65px; height: 65px; display: flex; justify-content: center; align-items: flex-end; transition: transform 0.5s cubic-bezier(0.175, 0.885, 0.32, 1.275); }
.plant-image img { max-width: 100%; max-height: 100%; object-fit: contain; pointer-events: none; }

/* 根據階段縮放 */
.stage-1 .plant-image { transform: scale(0.6); }
.stage-2 .plant-image { transform: scale(0.8); }
.stage-3 .plant-image { transform: scale(1.1) translateY(-5px); }
.stage-4 .plant-image { transform: scale(1.4) translateY(-12px); }

.plant-shadow { width: 40px; height: 10px; background: rgba(0,0,0,0.3); border-radius: 50%; margin-top: -5px; }

/* 魔法成長特效 */
.is-magic .plant-image { animation: plantGrowSmooth 5s cubic-bezier(0.2, 0.85, 0.25, 1); }
@keyframes plantGrowSmooth {
  0% { transform: scale(0.82) translateY(4px); filter: brightness(0.97); }
  45% { transform: scale(1) translateY(-2px); filter: brightness(1.08); }
  70% { transform: scale(1) translateY(0); filter: brightness(1.02); }
  100% { transform: scale(1) translateY(0); filter: brightness(1); }
}

.grow-enter-active,
.grow-leave-active {
  transition: opacity 0.45s ease, transform 0.45s ease;
}

.grow-enter-from,
.grow-leave-to {
  opacity: 0;
  transform: translateY(8px) scale(0.9);
}

/* --- 底部按鈕 --- */
.garden-actions { display: flex; gap: 10px; justify-content: center; padding: 15px; }
.action-btn { padding: 8px 20px; border-radius: 50px; border: none; cursor: pointer; font-weight: bold; }
.action-btn:disabled { opacity: 0.55; cursor: not-allowed; }
.water { background: #e3f2fd; color: #1976d2; }
.sun { background: #fffde7; color: #fbc02d; }
.talk { background: #e8f5e9; color: #2e7d32; }

.deco { position: absolute; font-size: 120px; opacity: 0.05; pointer-events: none; }
.leaf-left { bottom: -20px; left: -20px; }
.leaf-right { top: 10%; right: -20px; }
</style>