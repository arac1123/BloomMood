<template>
  <div class="page">
    <div class="deco leaf-left">🌱</div>
    <div class="deco leaf-right">🌻</div>

    <header class="navbar">
      <div class="logo">Bloommood</div>
      <nav>
        <span @click="router.push('/home')">今日</span>
        <span class="active">花園</span>
        <span @click="router.push('/journal')">日誌</span>
        <span @click="router.push('/statistics')">情緒脈動</span>
        <span @click="router.push('/setting')">設定</span>
      </nav>
      <div class="avatar" @click="handleLogout" title="登出">登出</div>
    </header>

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
              <div class="plant-zone">
                <transition-group name="grow">
                  <div v-for="plant in plants" :key="plant.id" class="plant-item">
                    <div class="plant-icon">{{ plant.icon }}</div>
                    <div class="plant-shadow"></div>
                  </div>
                </transition-group>
              </div>
              <div class="garden-actions">
                <button class="action-btn water" @click="triggerEffect('water')">💧 澆水 ({{ interactionStats.water }})</button>
                <button class="action-btn sun" @click="triggerEffect('sun')">☀️ 陽光 ({{ interactionStats.sun }})</button>
                <button class="action-btn talk" @click="triggerEffect('fertilize')">✨ 施肥 ({{ interactionStats.fertilize }})</button>
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
import { useRouter } from 'vue-router';

const router = useRouter();
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

onMounted(() => {
  const history = JSON.parse(localStorage.getItem('bloom_records') || '[]');
  if (history.length > 0) {
    const lastRecord = history[history.length - 1];
    const today = new Date().toLocaleDateString();
    if (lastRecord.date === today) {
      interactionStats.water = lastRecord.stats.water || 0;
      interactionStats.sun = lastRecord.stats.sun || 0;
      interactionStats.fertilize = lastRecord.stats.fertilize || 0;
    }
  }
});

const triggerEffect = (type) => {
  if (effectTimer) clearTimeout(effectTimer);
  isRaining.value = isShining.value = isFertilizing.value = false;

  if (type === 'water') {
    isRaining.value = true; interactionStats.water++;
  } else if (type === 'sun') {
    isShining.value = true; interactionStats.sun++;
  } else if (type === 'fertilize') {
    isFertilizing.value = true; interactionStats.fertilize++;
  }
  effectTimer = setTimeout(() => { isRaining.value = isShining.value = isFertilizing.value = false; }, 2500);
  
  // 即時同步至日誌紀錄
  syncInteractionToStore();
};

const syncInteractionToStore = () => {
  const history = JSON.parse(localStorage.getItem('bloom_records') || '[]');
  if (history.length > 0) {
    history[history.length - 1].stats = { ...interactionStats };
    localStorage.setItem('bloom_records', JSON.stringify(history));
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
  const plantIcons = ['🌱', '🌿', '🌵', '🍀', '🌸', '🌼', '🌻', '🌷'];
  const randomIcon = plantIcons[Math.floor(Math.random() * plantIcons.length)];
  plants.value.push({ id: Date.now() + Math.random(), icon: randomIcon });

  const newRecord = {
    id: Date.now(),
    date: new Date().toLocaleDateString(),
    chatLog: [...chatHistory.value],
    stats: { ...interactionStats },
    plant: randomIcon
  };

  const history = JSON.parse(localStorage.getItem('bloom_records') || '[]');
  history.push(newRecord);
  localStorage.setItem('bloom_records', JSON.stringify(history));
  alert('情緒已成功種入花園！');
};

const staticRain = Array.from({ length: 30 }, (_, i) => ({ id: i, style: { left: Math.random() * 100 + '%', animationDelay: Math.random() * 2 + 's' } }));
const staticSparkles = Array.from({ length: 15 }, (_, i) => ({ id: i, style: { left: Math.random() * 100 + '%', animationDelay: Math.random() * 1 + 's' } }));
const quickInput = (val) => { userInput.value = val; };
const handleLogout = () => { if (confirm("確定要離開花園嗎？")) router.push('/'); };
onBeforeUnmount(() => { if (effectTimer) clearTimeout(effectTimer); });
</script>

<style scoped>
/* 樣式保持不變，已確認符合 100vh 佈局 */
* { box-sizing: border-box; }
.page { height: 100vh; width: 100vw; background: linear-gradient(135deg, #f0f4f0 0%, #fefae0 100%); display: flex; flex-direction: column; overflow: hidden; font-family: 'PingFang TC', sans-serif; }
.navbar { height: 60px; display: flex; align-items: center; justify-content: space-between; padding: 0 40px; background: rgba(255, 255, 255, 0.3); backdrop-filter: blur(10px); border-bottom: 1px solid rgba(255, 255, 255, 0.2); z-index: 10; }
.logo { font-weight: 700; font-size: 22px; color: #4a5d4a; letter-spacing: 1px; }
nav span { margin: 0 15px; color: #556b55; cursor: pointer; }
nav span.active { color: #2d3a2d; font-weight: bold; border-bottom: 2px solid #5d7a5d; }
.avatar { cursor: pointer; color: #556b55; font-size: 14px; }
.content { flex: 1; min-height: 0; padding: 20px 40px; display: flex; justify-content: center; align-items: center; }
.container { width: 100%; max-width: 1400px; height: 100%; display: flex; flex-direction: column; }
.garden-layout { display: flex; gap: 30px; flex: 1; min-height: 0; }
.glass-card { background: rgba(255, 255, 255, 0.5); backdrop-filter: blur(15px); border-radius: 24px; border: 1px solid rgba(255, 255, 255, 0.6); box-shadow: 0 10px 30px rgba(0, 0, 0, 0.05); display: flex; flex-direction: column; padding: 20px; height: 100%; overflow: hidden; }
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
.plant-btn { background: #5d7a5d; color: white; border: none; padding: 12px; border-radius: 12px; font-weight: bold; cursor: pointer; }
.garden-panel { flex: 7; display: flex; flex-direction: column; }
.garden-header { margin-bottom: 10px; }
.garden-main-title { font-size: 18px; margin: 0; color: #3e4e3e; }
.garden-canvas { position: relative; background: rgba(255, 255, 255, 0.3); }
.rain-overlay, .sun-overlay, .fertilize-overlay { position: absolute; top: 0; left: 0; width: 100%; height: 100%; pointer-events: none; z-index: 10; }
.drop { position: absolute; width: 2px; height: 15px; background: rgba(174, 194, 224, 0.6); top: -20px; animation: rain-fall 0.8s linear infinite; }
@keyframes rain-fall { to { transform: translateY(600px); } }
.the-sun { position: absolute; top: 15%; right: 15%; display: flex; justify-content: center; align-items: center; }
.sun-icon { font-size: 80px; filter: drop-shadow(0 0 20px #ffeb3b); }
.sparkle { position: absolute; bottom: 80px; font-size: 14px; animation: sparkle-up 2s ease-out forwards; opacity: 0; }
@keyframes sparkle-up { 0% { transform: translateY(0) scale(0); opacity: 0; } 50% { opacity: 1; } 100% { transform: translateY(-100px) scale(1.5); opacity: 0; } }
.plant-zone { flex: 1; display: flex; justify-content: center; align-items: flex-end; gap: 15px; padding-bottom: 50px; flex-wrap: wrap; }
.plant-icon { font-size: 50px; z-index: 2; filter: drop-shadow(0 4px 4px rgba(0,0,0,0.1)); }
.plant-shadow { width: 35px; height: 8px; background: rgba(0,0,0,0.05); border-radius: 50%; margin-top: -8px; }
.garden-actions { display: flex; gap: 12px; justify-content: center; margin-top: auto; padding-top: 10px; }
.action-btn { padding: 10px 24px; border-radius: 50px; border: none; font-weight: 600; cursor: pointer; transition: 0.2s; }
.water { background: #e3f2fd; color: #1976d2; }
.sun { background: #fffde7; color: #fbc02d; }
.talk { background: #e8f5e9; color: #2e7d32; }
.mascot-bubble { position: absolute; top: 20px; left: 50%; transform: translateX(-50%); background: white; padding: 8px 16px; border-radius: 15px; font-size: 13px; color: #666; box-shadow: 0 4px 10px rgba(0,0,0,0.05); z-index: 5; }
.deco { position: absolute; font-size: 150px; opacity: 0.06; pointer-events: none; }
.leaf-left { bottom: -30px; left: -30px; transform: rotate(15deg); }
.leaf-right { top: 10%; right: -40px; transform: rotate(-10deg); }
</style>