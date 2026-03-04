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
                <button class="plant-btn" @click="saveDailyRecord" :disabled="chatHistory.length < 2">
                  ✨ 將情緒種入花園
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
              
              <div class="mascot-bubble">每一個心情，都值得被溫柔灌溉。</div>

              <div class="garden-container">
                <div class="garden-fence-box">
                  <div class="grass-layer">
                    <div class="soil-layer">
                      <div v-for="i in TOTAL_SLOTS" :key="i-1" class="plant-slot">
                        <div class="soil-pit"></div>
                        <transition name="grow">
                          <div 
                            v-if="getPlantInSlot(i-1)"
                            class="plant-item"
                            :class="[getPlantInSlot(i-1).status, { 'is-magic': getPlantInSlot(i-1).isMagic }]"
                          >
                            <div v-if="getPlantInSlot(i-1).isMagic" class="magic-burst"></div>
                            <div class="plant-wrapper">
                              <div class="plant-icon">
                                {{ getPlantInSlot(i-1).status === 'withered' ? '🥀' : (getPlantInSlot(i-1).status === 'seedling' ? '🌱' : getPlantInSlot(i-1).icon) }}
                              </div>
                            </div>
                            <div class="plant-shadow"></div>
                          </div>
                        </transition>
                      </div>
                    </div>
                  </div>
                </div>
              </div>
              
              <div class="garden-actions">
                <button class="action-btn water" @click="triggerEffect('water')">💧 澆水 ({{ interactionStats.water }})</button>
                <button class="action-btn sun" @click="triggerEffect('sun')">☀️ 陽光 ({{ interactionStats.sun }})</button>
                <button class="action-btn talk" @click="triggerEffect('fertilize')">✨ 施肥 ({{ interactionStats.fertilize }})</button>
                <button class="action-btn clear-garden" @click="clearGarden">掃帚 剷除花園</button>
              </div>
            </div>
          </section>
        </div>
      </div>
    </main>
  </div>
</template>

<script setup>
import { ref, reactive, nextTick, onBeforeUnmount, onMounted } from 'vue';
import Header from './header.vue'; // 確保路徑正確

// --- 狀態定義 ---
const userInput = ref('');
const chatScroll = ref(null);
const chatHistory = ref([{ type: 'system', text: '今天過得如何？跟我分享一點你的心情吧。' }]);
const plants = ref([]);
const emotions = [
  { emoji: '💢', label: '生氣' }, { emoji: '☁️', label: '平靜' },
  { emoji: '😰', label: '焦慮' }, { emoji: '😢', label: '難過' }, { emoji: '✨', label: '開心' }
];

const interactionStats = reactive({ water: 0, sun: 0, fertilize: 0 });
const isRaining = ref(false);
const isShining = ref(false);
const isFertilizing = ref(false);
let effectTimer = null;

const TOTAL_SLOTS = 12;

// --- 邏輯方法 ---
const getAvailableSlot = () => {
  const occupiedSlots = plants.value.map(p => p.slotIndex);
  const available = [];
  for(let i = 0; i < TOTAL_SLOTS; i++) {
    if(!occupiedSlots.includes(i)) available.push(i);
  }
  return available.length > 0 ? available[0] : null;
};

const getPlantInSlot = (index) => plants.value.find(p => p.slotIndex === index);

onMounted(() => {
  loadGardenData();
});

const loadGardenData = () => {
  const history = JSON.parse(localStorage.getItem('bloom_records') || '[]');
  if (history.length > 0) {
    const lastRecord = history[history.length - 1];
    const lastTime = lastRecord.timestamp || new Date(lastRecord.date).getTime();
    const now = new Date().getTime();
    const diffDays = (now - lastTime) / (1000 * 60 * 60 * 24);

    let occupiedSlotsStr = [];
    
    plants.value = history
      .filter(record => !record.isHidden)
      .map((record, index) => {
        let slotIndex = record.slotIndex;
        if (slotIndex === undefined || slotIndex === null || occupiedSlotsStr.includes(String(slotIndex))) {
             slotIndex = index < TOTAL_SLOTS ? index : null;
        }
        if (slotIndex !== null) occupiedSlotsStr.push(String(slotIndex));

        return {
          id: record.id,
          icon: record.plant,
          status: diffDays > 7 ? 'withered' : (record.status || 'seedling'),
          slotIndex: slotIndex,
          isMagic: false
        };
      }).filter(p => p.slotIndex !== null);

    const today = new Date().toLocaleDateString();
    if (lastRecord.date === today && !lastRecord.isHidden) {
      Object.assign(interactionStats, lastRecord.stats);
    }
  }
};

const triggerEffect = (type) => {
  if (effectTimer) clearTimeout(effectTimer);
  isRaining.value = isShining.value = isFertilizing.value = false;

  if (type === 'water') { isRaining.value = true; interactionStats.water++; }
  else if (type === 'sun') { isShining.value = true; interactionStats.sun++; }
  else if (type === 'fertilize') { isFertilizing.value = true; interactionStats.fertilize++; }

  plants.value = plants.value.map(p => {
    if (p.status === 'seedling' || (p.status === 'withered' && type === 'water')) {
      return { ...p, status: 'grown', isMagic: true };
    }
    return p;
  });

  setTimeout(() => {
    plants.value = plants.value.map(p => ({ ...p, isMagic: false }));
  }, 1500);

  effectTimer = setTimeout(() => { isRaining.value = isShining.value = isFertilizing.value = false; }, 2500);
  syncInteractionToStore();
};

const syncInteractionToStore = () => {
  const history = JSON.parse(localStorage.getItem('bloom_records') || '[]');
  if (history.length > 0) {
    const updatedHistory = history.map(record => {
      const livePlant = plants.value.find(p => p.id === record.id);
      if (livePlant) {
        return {
          ...record,
          status: livePlant.status,
          slotIndex: livePlant.slotIndex,
          stats: record.id === history[history.length - 1].id ? { ...interactionStats } : record.stats
        };
      }
      return record;
    });
    localStorage.setItem('bloom_records', JSON.stringify(updatedHistory));
  }
};

const clearGarden = () => {
  if (confirm("確定要將目前的植物從花園中剷除嗎？（您的心情日誌仍會保留）")) {
    const history = JSON.parse(localStorage.getItem('bloom_records') || '[]');
    const updatedHistory = history.map(record => ({ ...record, isHidden: true }));
    plants.value = [];
    interactionStats.water = 0;
    interactionStats.sun = 0;
    interactionStats.fertilize = 0;
    localStorage.setItem('bloom_records', JSON.stringify(updatedHistory));
  }
};

const sendMessage = async () => {
  if (!userInput.value.trim()) return;
  chatHistory.value.push({ type: 'user', text: userInput.value });
  userInput.value = '';
  await nextTick();
  if (chatScroll.value) chatScroll.value.scrollTop = chatScroll.value.scrollHeight;
  setTimeout(async () => {
    chatHistory.value.push({ type: 'system', text: `謝謝你。讓這份感受在你的花園裡轉化成養分吧。` });
    await nextTick();
    if (chatScroll.value) chatScroll.value.scrollTop = chatScroll.value.scrollHeight;
  }, 800);
};

const saveDailyRecord = () => {
  const availableSlot = getAvailableSlot();
  if (availableSlot === null) {
    alert("花園已經客滿囉！請先剷除一些舊的植物再種植新的心情。");
    return;
  }
  const plantIcons = ['🌿', '🌵', '🍀', '🌸', '🌼', '🌻', '🌷'];
  const randomIcon = plantIcons[Math.floor(Math.random() * plantIcons.length)];
  const newPlant = { id: Date.now() + Math.random(), icon: randomIcon, status: 'seedling', slotIndex: availableSlot, isMagic: false };
  plants.value.push(newPlant);
  const history = JSON.parse(localStorage.getItem('bloom_records') || '[]');
  history.push({
    id: newPlant.id,
    date: new Date().toLocaleDateString(),
    timestamp: new Date().getTime(),
    chatLog: [...chatHistory.value],
    stats: { ...interactionStats },
    plant: randomIcon,
    status: 'seedling',
    isHidden: false,
    slotIndex: availableSlot
  });
  localStorage.setItem('bloom_records', JSON.stringify(history));
};

const staticRain = Array.from({ length: 30 }, (_, i) => ({ id: i, style: { left: Math.random() * 100 + '%', animationDelay: Math.random() * 2 + 's' } }));
const staticSparkles = Array.from({ length: 15 }, (_, i) => ({ id: i, style: { left: Math.random() * 100 + '%', animationDelay: Math.random() * 1 + 's' } }));
const quickInput = (val) => { userInput.value = val; };

onBeforeUnmount(() => { if (effectTimer) clearTimeout(effectTimer); });
</script>

<style scoped>
/* --- 基礎佈局 --- */
* { box-sizing: border-box; }
.page { height: 100vh; width: 100vw; background: linear-gradient(135deg, #f0f4f0 0%, #fefae0 100%); display: flex; flex-direction: column; overflow: hidden; font-family: 'PingFang TC', sans-serif; }
.content { flex: 1; min-height: 0; padding: 20px 40px; display: flex; justify-content: center; align-items: center; }
.container { width: 100%; max-width: 1400px; height: 100%; display: flex; flex-direction: column; }
.garden-layout { display: flex; gap: 30px; flex: 1; min-height: 0; }
.glass-card { background: rgba(255, 255, 255, 0.5); backdrop-filter: blur(15px); border-radius: 24px; border: 1px solid rgba(255, 255, 255, 0.6); box-shadow: 0 10px 30px rgba(0, 0, 0, 0.05); display: flex; flex-direction: column; padding: 20px; height: 100%; overflow: hidden; }

/* --- 聊天面板 --- */
.mood-panel { flex: 4; }
.chat-display { flex: 1; overflow-y: auto; padding-right: 8px; margin-bottom: 15px; }
.bubble { padding: 10px 16px; border-radius: 18px; margin-bottom: 10px; font-size: 14px; line-height: 1.5; max-width: 85%; }
.system { background: #ffffff; color: #556b55; align-self: flex-start; border-bottom-left-radius: 4px; }
.user { background: #e8f0e8; color: #3e4e3e; align-self: flex-end; border-bottom-right-radius: 4px; margin-left: auto; }
.emotion-shortcuts { display: flex; flex-wrap: wrap; gap: 6px; margin-bottom: 15px; }
.shortcut-btn { background: white; border: 1px solid #d0ddd0; padding: 5px 12px; border-radius: 50px; font-size: 12px; cursor: pointer; }
.input-group { display: flex; flex-direction: column; gap: 10px; flex-shrink: 0; }
.input-wrapper { display: flex; background: white; border-radius: 12px; padding: 8px 12px; border: 1px solid #d0ddd0; }
.input-wrapper input { flex: 1; border: none; outline: none; font-size: 14px; }
.send-btn { background: none; border: none; font-size: 16px; color: #5d7a5d; cursor: pointer; }
.plant-btn { background: #5d7a5d; color: white; border: none; padding: 12px; border-radius: 12px; font-weight: bold; cursor: pointer; }

/* --- 花園面板與天氣 --- */
.garden-panel { flex: 7; display: flex; flex-direction: column; }
.garden-header { margin-bottom: 10px; }
.garden-main-title { font-size: 18px; margin: 0; color: #3e4e3e; }
.garden-sub-title { font-size: 13px; color: #888; margin-top: 4px; }
.garden-canvas { position: relative; background: rgba(255, 255, 255, 0.3); overflow: visible; }

.rain-overlay, .sun-overlay, .fertilize-overlay { position: absolute; top: 0; left: 0; width: 100%; height: 100%; pointer-events: none; z-index: 100; }
.sun-overlay { background: radial-gradient(circle at 85% 0%, rgba(255, 235, 59, 0.15) 0%, rgba(255, 255, 255, 0) 60%); }
.drop { position: absolute; width: 2px; height: 15px; background: rgba(174, 194, 224, 0.7); top: -120px; animation: rain-fall 0.8s linear infinite; }
@keyframes rain-fall { to { transform: translateY(800px); } }
.the-sun { position: absolute; top: -8%; right: 2%; display: flex; justify-content: center; align-items: center; }
.sun-icon { font-size: 80px; filter: drop-shadow(0 0 30px #ffeb3b) drop-shadow(0 0 50px #ffc107); }
.sun-glow { position: absolute; width: 250px; height: 250px; background: radial-gradient(circle, rgba(255,235,59,0.85) 0%, rgba(255,215,0,0.3) 40%, rgba(255,255,255,0) 70%); animation: pulse 2.5s ease-in-out infinite; }
@keyframes pulse { 0% { transform: scale(0.9); opacity: 0.7; } 50% { transform: scale(1.4); opacity: 1; } 100% { transform: scale(0.9); opacity: 0.7; } }
.sparkle { position: absolute; bottom: 150px; font-size: 14px; animation: sparkle-up 2s ease-out forwards; opacity: 0; }
@keyframes sparkle-up { 0% { transform: translateY(0) scale(0); opacity: 0; } 50% { opacity: 1; } 100% { transform: translateY(-150px) scale(1.5); opacity: 0; } }

/* --- 裝飾 --- */
.mascot-bubble { position: absolute; top: 30px; left: 50%; transform: translateX(-50%); background: white; padding: 8px 16px; border-radius: 15px; font-size: 13px; color: #666; box-shadow: 0 4px 10px rgba(0,0,0,0.05); z-index: 25; width: max-content;}
.deco { position: absolute; font-size: 150px; opacity: 0.06; pointer-events: none; }
.leaf-left { bottom: -30px; left: -30px; transform: rotate(15deg); }
.leaf-right { top: 10%; right: -40px; transform: rotate(-10deg); }

/* --- 按鈕區 --- */
.garden-actions { display: flex; gap: 12px; justify-content: center; margin-top: auto; padding-top: 20px; position: relative; z-index: 30; }
.action-btn { padding: 10px 24px; border-radius: 50px; border: none; font-weight: 600; cursor: pointer; transition: 0.2s; font-size: 13px; }
.water { background: #e3f2fd; color: #1976d2; }
.sun { background: #fffde7; color: #fbc02d; }
.talk { background: #e8f5e9; color: #2e7d32; }
.clear-garden { background: #f5f5f5; color: #888; border: 1px solid #ddd; }
.clear-garden:hover { background: #ffebee; color: #c62828; border-color: #ffcdd2; }

/* --- 3D 花園農場 --- */
.garden-container { position: relative; width: 95%; flex: 1; display: flex; align-items: flex-end; margin: 40px auto 10px auto; perspective: 1200px; z-index: 2; }
.garden-fence-box { width: 100%; transform: rotateX(20deg); transform-style: preserve-3d; background-color: #C19A6B; background-image: repeating-linear-gradient(90deg, transparent 0, transparent 28px, rgba(0,0,0,0.2) 28px, rgba(0,0,0,0.2) 30px); border: 4px solid #8D6E63; border-bottom-width: 14px; border-radius: 16px; padding: 12px; box-shadow: 0 25px 35px rgba(0,0,0,0.25); }
.grass-layer { background-color: #7CB342; padding: 10px; border-radius: 10px; box-shadow: inset 0 5px 15px rgba(0,0,0,0.3); border-bottom: 8px solid #558B2F; }
.soil-layer { width: 100%; background-color: #5D4037; border-radius: 8px; box-shadow: inset 0 10px 25px rgba(0,0,0,0.6); display: grid; grid-template-columns: repeat(4, 1fr); grid-template-rows: repeat(3, 1fr); padding: 20px; gap: 15px; }
.plant-slot { position: relative; height: 85px; display: flex; justify-content: center; align-items: flex-end; transform: rotateX(-20deg); transform-origin: bottom center; z-index: 3; }
.soil-pit { position: absolute; bottom: 2px; width: 55px; height: 18px; background: #3E2723; border-radius: 50%; box-shadow: inset 0 5px 10px rgba(0,0,0,0.8); transform: rotateX(20deg) scaleY(0.8); }
.plant-item { position: relative; display: flex; flex-direction: column; align-items: center; z-index: 2; }
.plant-icon { font-size: 52px; filter: drop-shadow(0 4px 3px rgba(0,0,0,0.3)); transition: all 0.5s ease; transform-origin: bottom center; }
.plant-shadow { width: 38px; height: 12px; background: rgba(0,0,0,0.4); border-radius: 50%; margin-top: -8px; transform: scaleY(0.7); }
.is-magic .plant-icon { animation: magic-pop 0.6s ease-out forwards; }
@keyframes magic-pop { 0% { transform: scale(0.5); } 50% { transform: scale(1.4) translateY(-30px); filter: brightness(1.2); } 100% { transform: scale(1); } }
.grow-enter-active { transition: all 0.6s cubic-bezier(0.175, 0.885, 0.32, 1.275); }
.grow-enter-from { transform: translateY(30px) scale(0.5); opacity: 0; }
</style>