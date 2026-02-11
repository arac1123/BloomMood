<template>
  <div class="page">
    <div class="deco leaf-left">🌱</div>
    <div class="deco leaf-right">🌻</div>

    <header class="navbar">
      <div class="logo">Bloommood</div>
      <nav>
        <span @click="router.push('/home')">今日</span>
        <span @click="router.push('/garden')">花園</span>
        <span @click="router.push('/journal')">日誌</span>
        <span @click="router.push('/statistics')">情緒脈動</span>
        <span class="active">設定</span>
      </nav>
      <div class="avatar" @click="handleLogout">登出</div>
    </header>

    <main class="content">
      <div class="container settings-layout">
        <aside class="settings-sidebar">
          <div v-for="menu in menus" :key="menu.id" :class="['menu-item', { active: activeTab === menu.id }]" @click="activeTab = menu.id">
            <span class="menu-icon">{{ menu.icon }}</span>
            <span class="menu-label">{{ menu.label }}</span>
          </div>
        </aside>

        <section class="settings-detail">
          <div class="glass-card detail-card">
            <transition name="fade-slide" mode="out-in">
              <div v-if="activeTab === 'account'" key="account" class="settings-section">
                <h2 class="section-title">帳號設定</h2>
                <div class="profile-header"><div class="current-avatar">👤</div><button class="ghost-btn sm">更換頭像</button></div>
                <div class="action-list">
                  <div class="modify-box" :class="{ expanding: isEditingName }">
                    <div class="action-item no-border">
                      <div class="info-label"><span class="title">用戶名稱</span><span v-if="!isEditingName" class="current-value">{{ userForm.name }}</span></div>
                      <button v-if="!isEditingName" class="ghost-btn" @click="startEditName">修改名稱</button>
                    </div>
                    <transition name="expand">
                      <div v-if="isEditingName" class="inline-form">
                        <div class="form-group"><label>新名稱</label><input type="text" v-model="tempName" /></div>
                        <div class="form-actions"><button class="primary-btn sm" @click="updateName">確認修改</button><button class="ghost-btn sm" @click="isEditingName = false">取消</button></div>
                      </div>
                    </transition>
                  </div>
                  <div class="action-item"><span>電子信箱</span><span class="static-val">{{ userForm.email }}</span></div>
                </div>
              </div>

              <div v-else-if="activeTab === 'privacy'" key="privacy" class="settings-section">
                <h2 class="section-title">隱私與安全</h2>
                <div class="action-list">
                  <div class="modify-box" :class="{ expanding: isEditingPassword }">
                    <div class="action-item no-border"><span>登入密碼</span><button v-if="!isEditingPassword" class="ghost-btn" @click="isEditingPassword = true">修改密碼</button></div>
                    <transition name="expand">
                      <div v-if="isEditingPassword" class="inline-form">
                        <div class="form-group"><label>舊密碼</label><input type="password" v-model="passForm.old" /></div>
                        <div class="form-group"><label>新密碼</label><input type="password" v-model="passForm.new" /></div>
                        <div class="form-actions"><button class="primary-btn sm" @click="updatePass">儲存新密碼</button><button class="ghost-btn sm" @click="isEditingPassword = false">取消</button></div>
                      </div>
                    </transition>
                  </div>
                  <div class="action-item danger"><span>清除資料</span><button class="danger-btn" @click="clearAllData">徹底清除</button></div>
                </div>
              </div>
            </transition>
          </div>
        </section>
      </div>
    </main>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue';
import { useRouter } from 'vue-router';

const router = useRouter();
const activeTab = ref('account');
const isEditingName = ref(false);
const isEditingPassword = ref(false);
const tempName = ref('');

const userForm = reactive({ name: '用戶', email: 'user@bloommood.com' });
const passForm = reactive({ old: '', new: '' });
const menus = [{ id: 'account', label: '帳號設定', icon: '👤' }, { id: 'privacy', label: '隱私安全', icon: '🔒' }];

onMounted(() => {
  const savedName = localStorage.getItem('user_name');
  if (savedName) userForm.name = savedName;
});

const startEditName = () => { tempName.value = userForm.name; isEditingName.value = true; };

const updateName = () => {
  if (!tempName.value.trim()) return;
  userForm.name = tempName.value;
  localStorage.setItem('user_name', tempName.value); // 關鍵同步步驟
  isEditingName.value = false;
  alert('名稱已修改完成！');
};

const updatePass = () => { alert('密碼已修改'); isEditingPassword.value = false; };
const clearAllData = () => { if (confirm('確定清除？')) localStorage.clear(); location.reload(); };
const handleLogout = () => router.push('/');
</script>

<style scoped>
/* 樣式邏輯與 Home 一致 */
* { box-sizing: border-box; }
.page { height: 100vh; width: 100vw; background: linear-gradient(135deg, #f0f4f0 0%, #fefae0 100%); display: flex; flex-direction: column; overflow: hidden; font-family: 'PingFang TC', sans-serif;}
.navbar { height: 60px; display: flex; align-items: center; justify-content: space-between; padding: 0 40px; background: rgba(255, 255, 255, 0.3); backdrop-filter: blur(10px); border-bottom: 1px solid rgba(255, 255, 255, 0.2); z-index: 10; }
.logo { font-weight: 700; font-size: 22px; color: #4a5d4a; }
nav span { margin: 0 15px; color: #556b55; cursor: pointer; }
nav span.active { color: #2d3a2d; font-weight: bold; border-bottom: 2px solid #5d7a5d; }
.content { flex: 1; padding: 40px; display: flex; justify-content: center; }
.container { width: 100%; max-width: 1000px; display: flex; gap: 30px; }
.settings-sidebar { flex: 3; display: flex; flex-direction: column; gap: 10px; }
.menu-item { padding: 15px 20px; border-radius: 16px; background: rgba(255, 255, 255, 0.3); cursor: pointer; display: flex; align-items: center; gap: 12px; }
.menu-item.active { background: white; font-weight: 600; box-shadow: 0 4px 15px rgba(0,0,0,0.05); }
.settings-detail { flex: 7; }
.glass-card { background: rgba(255, 255, 255, 0.5); backdrop-filter: blur(15px); border-radius: 24px; border: 1px solid rgba(255, 255, 255, 0.6); padding: 40px; height: 100%; overflow-y: auto; }
.modify-box { border-bottom: 1px solid rgba(0,0,0,0.05); transition: 0.4s; padding: 10px 0; }
.modify-box.expanding { background: rgba(255, 255, 255, 0.3); border-radius: 15px; padding: 10px; margin: 0 -10px; }
.action-item { display: flex; justify-content: space-between; align-items: center; padding: 15px 0; }
.no-border { border-bottom: none; }
.info-label { display: flex; flex-direction: column; }
.current-value, .static-val { font-size: 13px; color: #7a8a7a; margin-top: 4px; }
.form-group { display: flex; flex-direction: column; gap: 5px; margin-bottom: 10px; }
.form-group input { padding: 8px; border-radius: 8px; border: 1px solid #ddd; outline: none; }
.form-actions { display: flex; gap: 10px; }
.primary-btn { background: #5d7a5d; color: white; border: none; padding: 8px 16px; border-radius: 8px; cursor: pointer; }
.ghost-btn { background: transparent; border: 1px solid #5d7a5d; color: #5d7a5d; padding: 6px 12px; border-radius: 8px; cursor: pointer; font-size: 13px; }
.danger-btn { color: #d32f2f; border: 1px solid #ffcdd2; background: #fff5f5; padding: 6px 12px; border-radius: 8px; cursor: pointer; }
.expand-enter-active, .expand-leave-active { transition: all 0.3s ease-out; max-height: 200px; }
.expand-enter-from, .expand-leave-to { max-height: 0; opacity: 0; }
.deco { position: absolute; font-size: 150px; opacity: 0.06; pointer-events: none; }
.leaf-left { bottom: -30px; left: -30px; transform: rotate(15deg); }
.leaf-left { bottom: -30px; left: -30px; transform: rotate(15deg); }
.leaf-right { top: 10%; right: -40px; transform: rotate(-10deg); }
</style>