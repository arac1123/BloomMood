<template>
  <div class="page page-bg-main">
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
                <button
                  class="plant-btn"
                  @click="saveDailyRecord"
                  :disabled="chatHistory.length < 2 || hasPlantedToday || isPlanting || isInteracting"
                >
                  {{ isPlanting ? '🌱 種植中...' : (hasPlantedToday ? '🌟 今天已經種下情緒囉，明天再來吧' : '✨ 將情緒種入花園') }}
                </button>
              </div>
            </div>
          </section>

          <section class="garden-panel">
            <div class="garden-header">
              <h1 class="garden-main-title">情緒花園</h1>
              <p class="garden-sub-title">小提示🌟：每日初次互動，植物會悄悄長大喔！</p>

            </div>
            <div class="glass-card garden-canvas">
              <transition name="sun-fade">
                <div v-if="isShining" class="sun-overlay">
                  <div class="the-sun"><div class="sun-glow"></div><div class="sun-icon">☀️</div></div>
                </div>
              </transition>

              <div class="mascot-bubble">
                {{ isGardenWithered ? '花園太久沒喝水了，快澆點水救救它們吧！' : '每一個心情，都值得被溫柔灌溉。' }}
              </div>
              <div v-if="latestInteractionText" class="interaction-hint">{{ latestInteractionText }}</div>
              <div v-if="apiNotice" class="api-notice">{{ apiNotice }}</div>

              <div class="garden-container">
                <div
                  class="garden-scene"
                  :class="{ 'withered-state': isGardenWithered }"
                  :style="{ backgroundImage: `url(${gardenBackground})` }"
                >
                  <div v-if="isRaining" class="rain-overlay">
                    <div v-for="drop in staticRain" :key="drop.id" class="drop" :style="drop.style"></div>
                  </div>
                  <div v-if="isFertilizing" class="fertilize-overlay">
                    <div v-for="s in staticSparkles" :key="s.id" class="sparkle" :style="s.style">✨</div>
                  </div>
                  <div v-for="i in TOTAL_SLOTS" :key="i - 1" class="plant-spot" :style="getSpotStyle(i - 1)">
                    <div class="spot-marker"></div>
                    <transition name="grow" mode="out-in">
                      <div
                        v-if="getPlantInSlot(i - 1)"
                        class="plant-item"
                        :class="[`stage-${getPlantInSlot(i - 1).stage}`, { 'is-magic': getPlantInSlot(i - 1).isMagic }]"
                      >
                        <div v-if="getPlantInSlot(i - 1).isMagic" class="magic-burst"></div>
                        <div class="plant-wrapper">
                          <div class="plant-image">
                            <img :src="getPlantImagePath(getPlantInSlot(i - 1))" :alt="getPlantInSlot(i - 1).type" />
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
const apiBaseUrl = import.meta.env.VITE_API_BASE_URL || 'http://localhost:3001';

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
  { x: 22, y: 74 }, { x: 32, y: 74 }, { x: 42, y: 74 }, { x: 52, y: 74 }, { x: 62, y: 74 }, { x: 72, y: 74 },
  { x: 26, y: 66 }, { x: 36, y: 66 }, { x: 46, y: 66 }, { x: 56, y: 66 }, { x: 66, y: 66 }, { x: 76, y: 66 },
  { x: 24, y: 58 }, { x: 34, y: 58 }, { x: 44, y: 58 }, { x: 54, y: 58 }, { x: 64, y: 58 }, { x: 74, y: 58 },
  { x: 28, y: 50 }, { x: 38, y: 50 }, { x: 48, y: 50 }, { x: 58, y: 50 }, { x: 68, y: 50 }, { x: 78, y: 50 },
  { x: 32, y: 42 }, { x: 44, y: 42 }, { x: 56, y: 42 }, { x: 68, y: 42 },
  { x: 40, y: 34 }, { x: 52, y: 34 }, { x: 64, y: 34 }
];

const emotions = [
  { emoji: '💢', label: '生氣' }, { emoji: '☁️', label: '平靜' },
  { emoji: '😰', label: '焦慮' }, { emoji: '😢', label: '難過' }, { emoji: '✨', label: '開心' }
];


const userInput = ref('');
const chatScroll = ref(null);
const chatHistory = ref([{ type: 'system', text: '今天過得如何？跟我分享一點你的心情吧。' }]);
const plants = ref([]);
const TOTAL_SLOTS = 31;

const hasPlantedToday = ref(false);
const isPlanting = ref(false);
const isInteracting = ref(false);
const apiNotice = ref('');
const actionDateKeys = ref(new Set());
const latestActionDateKey = ref('');

const isRaining = ref(false);
const isShining = ref(false);
const isFertilizing = ref(false);
let effectTimer = null;

const getDateKey = (dateObj) => {
  const year = dateObj.getFullYear();
  const month = String(dateObj.getMonth() + 1).padStart(2, '0');
  const day = String(dateObj.getDate()).padStart(2, '0');
  return `${year}-${month}-${day}`;
};

const parseDateKey = (key) => {
  if (!key || typeof key !== 'string') return null;
  const parts = key.split('-').map(Number);
  if (parts.length !== 3 || parts.some(n => !Number.isFinite(n))) return null;
  const [year, month, day] = parts;
  return new Date(year, month - 1, day);
};

const addDaysToDateKey = (key, days) => {
  const d = parseDateKey(key);
  if (!d) return key;
  d.setDate(d.getDate() + days);
  return getDateKey(d);
};

const diffDaysByDateKey = (fromKey, toKey) => {
  const from = parseDateKey(fromKey);
  const to = parseDateKey(toKey);
  if (!from || !to) return 0;
  const ms = to.getTime() - from.getTime();
  return Math.floor(ms / (1000 * 60 * 60 * 24));
};

const toDateKeyFromApiTime = (value) => {
  if (!value || typeof value !== 'string') return '';
  return value.slice(0, 10);
};

const getLatestPlantDateKey = () => {
  return plants.value.reduce((latest, plant) => {
    const key = plant?.plantDate;
    if (!key) return latest;
    return !latest || key > latest ? key : latest;
  }, '');
};

const daysSinceLastInteraction = computed(() => {
  if (!plants.value.length) return null;
  const baseDateKey = latestActionDateKey.value || getLatestPlantDateKey();
  if (!baseDateKey) return null;
  return Math.max(0, diffDaysByDateKey(baseDateKey, getDateKey(new Date())));
});

const isGardenWithered = computed(() => {
  if (!plants.value.length) return false;
  const days = daysSinceLastInteraction.value;
  return typeof days === 'number' && days >= 7;
});

const latestInteractionText = computed(() => {
  const days = daysSinceLastInteraction.value;
  if (typeof days !== 'number') return '';
  return `距離最近一次互動 ${days} 天`;
});

const getDisplayStage = (plant) => {
  const stage = Number(plant?.stage);
  if (!Number.isFinite(stage)) return 1;
  return Math.min(4, Math.max(1, stage));
};

const getCurrentYearMonth = () => {
  const date = new Date();
  const year = date.getFullYear();
  const month = String(date.getMonth() + 1).padStart(2, '0');
  return `${year}-${month}`;
};

const getPreviousYearMonth = () => {
  const d = new Date();
  d.setMonth(d.getMonth() - 1);
  const year = d.getFullYear();
  const month = String(d.getMonth() + 1).padStart(2, '0');
  return `${year}-${month}`;
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
  for (let i = 0; i < TOTAL_SLOTS; i++) {
    if (!occupiedSlots.includes(i)) return i;
  }
  return null;
};

const getPlantInSlot = (index) => plants.value.find(p => p.slotIndex === index);

const getSpotStyle = (index) => {
  const spot = PLANT_SPOTS[index] || { x: 50, y: 75 };
  return { left: `${spot.x}%`, top: `${spot.y}%`, zIndex: Math.round(spot.y * 10) };
};

const getPlantImagePath = (plant) => {
  const plantTypeKey = (plant?.type || 'FLOWER').toLowerCase();
  const prefix = plantConfigs[plantTypeKey]?.prefix || 'sunflower';
  const fileName = `${prefix}_stage${getDisplayStage(plant)}.png`;
  return plantImageModules[`../assets/image/${fileName}`] || '';
};

const fetchRecentActions = async () => {
  const ym = getCurrentYearMonth();
  const prevYm = getPreviousYearMonth();

  try {
    const [resCurrent, resPrevious] = await Promise.all([
      fetch(`${apiBaseUrl}/api/plant/month?ym=${ym}`, { method: 'GET', credentials: 'include' }),
      fetch(`${apiBaseUrl}/api/action/month?ym=${prevYm}`, { method: 'GET', credentials: 'include' })
    ]);

    const rowsCurrent = resCurrent.ok ? await resCurrent.json() : [];
    const rowsPrevious = resPrevious.ok ? await resPrevious.json() : [];
    const allActions = [
      ...(Array.isArray(rowsPrevious) ? rowsPrevious : []),
      ...(Array.isArray(rowsCurrent) ? rowsCurrent : [])
    ];

    const keys = new Set();
    let latestKey = '';

    allActions.forEach((action) => {
      const key = toDateKeyFromApiTime(action?.actionTime);
      if (!key) return;
      keys.add(key);
      if (!latestKey || key > latestKey) latestKey = key;
    });

    actionDateKeys.value = keys;
    latestActionDateKey.value = latestKey;
  } catch (error) {
    console.error('取得互動紀錄失敗:', error);
    actionDateKeys.value = new Set();
    latestActionDateKey.value = '';
  }
};

const fetchMonthlyGarden = async () => {
  const ym = getCurrentYearMonth();
  try {
    const [res] = await Promise.all([
      fetch(`${apiBaseUrl}/api/plant/month?ym=${ym}`, { method: 'GET', credentials: 'include' }),
      fetchRecentActions()
    ]);

    if (!res.ok) return;

    const monthRows = await res.json();
    const plantedRows = Array.isArray(monthRows)
      ? monthRows.filter(row => row?.hasPlant && row?.plant)
      : [];

    plants.value = plantedRows.slice(0, TOTAL_SLOTS).map((row, index) => {
      const plant = row.plant || {};
      return {
        ...plant,
        stage: getDisplayStage(plant),
        slotIndex: Number.isInteger(plant.slotIndex) ? plant.slotIndex : index,
        isMagic: false
      };
    });
  } catch (error) {
    console.error('取得當月花園失敗:', error);
  }
};

const checkTodayPlant = async () => {
  try {
    const res = await fetch(`${apiBaseUrl}/api/plant/today`, {
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

const getLatestPlant = () => {
  if (!plants.value.length) return null;

  return [...plants.value].sort((a, b) => {
    const dateDiff = (a.plantDate || '').localeCompare(b.plantDate || '');
    if (dateDiff !== 0) return dateDiff;
    return (Number(a.pid) || 0) - (Number(b.pid) || 0);
  }).at(-1) || null;
};

const saveDailyRecord = async () => {
  if (hasPlantedToday.value) {
    alert('今天已經種下情緒囉，明天再來吧！');
    return;
  }

  if (isPlanting.value) return;
  isPlanting.value = true;
  apiNotice.value = '';

  const slot = getAvailableSlot();
  if (slot === null) {
    isPlanting.value = false;
    alert('本月花園已經滿了！');
    return;
  }

  const availableTypes = ['FLOWER', 'CACTUS', 'TREE'];
  const randomType = availableTypes[Math.floor(Math.random() * availableTypes.length)];

  try {
    const res = await fetch(`${apiBaseUrl}/api/plant/today`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ type: randomType }),
      credentials: 'include'
    });

    if (res.ok) {
      hasPlantedToday.value = true;
      await fetchMonthlyGarden();
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

const triggerInteraction = async (actionType) => {
  if (isInteracting.value) return;
  isInteracting.value = true;
  apiNotice.value = '';

  const stageBeforeByPid = new Map(
    plants.value
      .filter(p => p && p.pid != null)
      .map(p => [p.pid, Number(p.stage) || 0])
  );

  if (effectTimer) clearTimeout(effectTimer);
  isRaining.value = isShining.value = isFertilizing.value = false;

  if (actionType === 'WATER') isRaining.value = true;
  else if (actionType === 'SUN') isShining.value = true;
  else if (actionType === 'FERTILIZE') isFertilizing.value = true;

  effectTimer = setTimeout(() => {
    isRaining.value = isShining.value = isFertilizing.value = false;
  }, 2500);

  try {
    const latestPlant = getLatestPlant();
    if (!latestPlant?.pid || !latestPlant?.plantDate) {
      apiNotice.value = '目前沒有可成長的植物，請先種植。';
      return;
    }

    const actionRes = await fetch(`${apiBaseUrl}/api/action`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ type: actionType }),
      credentials: 'include'
    });

    if (!actionRes.ok) {
      apiNotice.value = await readErrorMessage(actionRes, '互動失敗，動作未記錄。');
      return;
    }

    const todayKey = getDateKey(new Date());
    const plantDateKey = String(latestPlant.plantDate || '');
    const daysSincePlant = Math.max(0, diffDaysByDateKey(plantDateKey, todayKey));
    const maxStageToday = Math.min(4, 2 + daysSincePlant);
    const currentStage = Number(latestPlant.stage) || 1;
    const expectedStage = Math.min(maxStageToday, currentStage + 1);

    if (expectedStage > currentStage) {
      const patchRes = await fetch(`${apiBaseUrl}/api/plant/${latestPlant.pid}`, {
        method: 'PATCH',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ stage: expectedStage }),
        credentials: 'include'
      });

      if (!patchRes.ok) {
        apiNotice.value = await readErrorMessage(patchRes, '互動成功，但成長更新失敗。');
      }
    } else {
      apiNotice.value = currentStage >= 4
        ? '植物已達最高階段。'
        : '今天已完成該階段成長，明天再來互動會繼續長大。';
    }

    await fetchMonthlyGarden();

    plants.value.forEach((p) => {
      if (p.pid == null) {
        p.isMagic = false;
        return;
      }
      const prevStage = stageBeforeByPid.get(p.pid);
      p.isMagic = prevStage != null ? Number(p.stage) > prevStage : false;
    });
    setTimeout(() => plants.value.forEach(p => p.isMagic = false), 1000);
  } catch (error) {
    console.error('互動失敗:', error);
    apiNotice.value = '互動失敗，請檢查網路連線。';
  } finally {
    isInteracting.value = false;
  }
};

onMounted(() => {
  fetchMonthlyGarden();
  checkTodayPlant();
});

const sendMessage = async () => {
  if (!userInput.value.trim()) return;
  chatHistory.value.push({ type: 'user', text: userInput.value });
  userInput.value = '';
  await nextTick();
  chatScroll.value.scrollTop = chatScroll.value.scrollHeight;
  setTimeout(() => {
    chatHistory.value.push({ type: 'system', text: '謝謝你分享你的心情。' });
    nextTick(() => {
      chatScroll.value.scrollTop = chatScroll.value.scrollHeight;
    });
  }, 800);
};

const quickInput = (v) => {
  userInput.value = v;
};

const staticRain = Array.from({ length: 30 }, (_, i) => ({
  id: i,
  style: { left: Math.random() * 100 + '%', animationDelay: Math.random() * 2 + 's' }
}));

const staticSparkles = Array.from({ length: 15 }, (_, i) => ({
  id: i,
  style: { left: Math.random() * 100 + '%', animationDelay: Math.random() * 1 + 's' }
}));
</script>

<style scoped>
/* --- 基礎佈局 --- */
.content { flex: 1; padding: 20px 40px; display: flex; justify-content: center; align-items: center; min-height: 0; }
.container { width: 100%; max-width: 1400px; height: 100%; display: flex; flex-direction: column; }
.garden-layout { display: flex; gap: 30px; flex: 1; min-height: 0; }
.glass-card { box-shadow: 0 10px 30px rgba(0, 0, 0, 0.05); display: flex; flex-direction: column; padding: 20px; height: 100%; overflow: hidden; }

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
.rain-overlay, .sun-overlay, .fertilize-overlay { position: absolute; pointer-events: none; z-index: 10; }
.rain-overlay { left: 0; right: 0; top: 0; height: 60%; }
.sun-overlay { inset: 0; }
.fertilize-overlay { inset: 0; }
.drop { position: absolute; width: 2px; height: 15px; background: rgba(174, 194, 224, 0.7); top: -20px; animation: rain 0.8s linear infinite; }
@keyframes rain { to { transform: translateY(800px); } }
.sun-icon { font-size: 80px; position: absolute; top: 50px; right: 50px; filter: drop-shadow(0 0 20px gold); }
.sparkle { position: absolute; top: 65%; animation: sparkleUp 2s ease-out forwards; }
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
  width: 90px;
  height: 104px;
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
.plant-image { width: 78px; height: 78px; display: flex; justify-content: center; align-items: flex-end; transition: transform 0.5s cubic-bezier(0.175, 0.885, 0.32, 1.275); }
.plant-image img { max-width: 100%; max-height: 100%; object-fit: contain; pointer-events: none; }

/* 根據階段定義目標大小與高度 */
.stage-1 { --target-scale: 0.6; --target-translate-y: 0px; }
.stage-2 { --target-scale: 0.8; --target-translate-y: 0px; }
.stage-3 { --target-scale: 1.1; --target-translate-y: -5px; }
.stage-4 { --target-scale: 1.4; --target-translate-y: -12px; }

.plant-image {
  transform: scale(var(--target-scale, 1)) translateY(var(--target-translate-y, 0px));
}


/* 魔法成長特效 */
.is-magic .plant-image { animation: plantGrowSmooth 5s cubic-bezier(0.2, 0.85, 0.25, 1); }
@keyframes plantGrowSmooth {
  0% {
    transform: scale(calc(var(--target-scale, 1) * 0.82)) translateY(calc(var(--target-translate-y, 0px) + 4px));
    filter: brightness(0.97);
  }
  45% {
    transform: scale(calc(var(--target-scale, 1) * 1.03)) translateY(calc(var(--target-translate-y, 0px) - 2px));
    filter: brightness(1.08);
  }
  70% {
    transform: scale(var(--target-scale, 1)) translateY(var(--target-translate-y, 0px));
    filter: brightness(1.02);
  }
  100% {
    transform: scale(var(--target-scale, 1)) translateY(var(--target-translate-y, 0px));
    filter: brightness(1);
  }
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

@media (max-width: 1200px) {
  .content {
    padding: 16px 20px;
  }

  .garden-layout {
    gap: 18px;
  }

  .mood-panel {
    flex: 5;
  }

  .garden-panel {
    flex: 7;
  }
}

@media (max-width: 900px) {
  .content {
    align-items: flex-start;
  }

  .garden-layout {
    flex-direction: column;
  }

  .mood-panel,
  .garden-panel {
    flex: unset;
    width: 100%;
  }

  .mood-panel {
    min-height: 320px;
  }

  .garden-container {
    padding-bottom: 12px;
  }

  .garden-scene {
    max-width: 100%;
  }

  .garden-actions {
    flex-wrap: wrap;
  }
}

@media (max-width: 640px) {
  .content {
    padding: 12px;
  }

  .glass-card {
    padding: 14px;
    border-radius: 18px;
  }

  .bubble {
    font-size: 13px;
    padding: 9px 12px;
  }

  .shortcut-btn {
    font-size: 11px;
    padding: 5px 10px;
  }

  .garden-main-title {
    font-size: 16px;
  }

  .sun-icon {
    font-size: 44px;
  }

  .action-btn {
    font-size: 13px;
    padding: 8px 14px;
  }
}

</style>
