<template>
  <div class="page page-bg-main">
    <div class="deco leaf-left">🌱</div>
    <div class="deco leaf-right">🌻</div>

    <Header active-tab="setting" />

    <main class="content">
      <div class="container settings-layout">
        <aside class="settings-sidebar">
          <div 
            v-for="menu in menus" 
            :key="menu.id" 
            :class="['menu-item', { active: activeTab === menu.id }]" 
            @click="activeTab = menu.id"
          >
            <span class="menu-icon">{{ menu.icon }}</span>
            <span class="menu-label">{{ menu.label }}</span>
          </div>
        </aside>

        <section class="settings-detail">
          <div class="glass-card detail-card">
            <transition name="fade-slide" mode="out-in">
              <div v-if="activeTab === 'account'" key="account" class="settings-section">
                <h2 class="section-title">帳號設定</h2>
                <div class="profile-header">
                  <div class="current-avatar">👤</div>
                  <button class="ghost-btn sm">更換頭像</button>
                </div>
                <div class="action-list">
                  <div class="modify-box" :class="{ expanding: isEditingName }">
                    <div class="action-item no-border">
                      <div class="info-label">
                        <span class="title">用戶名稱</span>
                        <span v-if="!isEditingName" class="current-value">{{ userForm.name }}</span>
                      </div>
                      <button v-if="!isEditingName" class="ghost-btn" @click="startEditName">修改名稱</button>
                    </div>
                    <transition name="expand">
                      <div v-if="isEditingName" class="inline-form">
                        <div class="form-group">
                          <label>新名稱</label>
                          <input type="text" v-model="tempName" />
                        </div>
                        <div class="form-actions">
                          <button class="primary-btn sm" @click="updateName" :disabled="isSavingName">確認修改</button>
                          <button class="ghost-btn sm" @click="isEditingName = false">取消</button>
                        </div>
                      </div>
                    </transition>
                  </div>
                  <div class="action-item">
                    <span>電子信箱</span>
                    <span class="static-val">{{ userForm.email }}</span>
                  </div>
                </div>
              </div>

              <div v-else-if="activeTab === 'privacy'" key="privacy" class="settings-section">
                <h2 class="section-title">隱私與安全</h2>
                <div class="action-list">
                  <div class="modify-box" :class="{ expanding: isEditingPassword }">
                    <div class="action-item no-border">
                      <span>登入密碼</span>
                      <button v-if="!isEditingPassword" class="ghost-btn" @click="isEditingPassword = true">修改密碼</button>
                    </div>
                    <transition name="expand">
                      <div v-if="isEditingPassword" class="inline-form">
                        <div class="form-group"><label>舊密碼</label><input type="password" v-model="passForm.old" /></div>
                        <div class="form-group"><label>新密碼</label><input type="password" v-model="passForm.new" /></div>
                        <div class="form-actions">
                          <button class="primary-btn sm" @click="updatePass" :disabled="isSavingPassword">儲存新密碼</button>
                          <button class="ghost-btn sm" @click="isEditingPassword = false">取消</button>
                        </div>
                      </div>
                    </transition>
                  </div>
                  <div class="action-item danger">
                    <span>清除資料</span>
                    <button class="danger-btn" @click="clearAllData">徹底清除</button>
                  </div>
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
// 2. 引入 Header 元件
import Header from '../components/header.vue'; 

const router = useRouter();
const activeTab = ref('account');
const isEditingName = ref(false);
const isEditingPassword = ref(false);
const tempName = ref('');
const isSavingName = ref(false);
const isSavingPassword = ref(false);

const apiBaseUrl = import.meta.env.VITE_API_BASE_URL || 'http://localhost:3001';

const userForm = reactive({ name: '用戶', email: 'user@bloommood.com' });
const passForm = reactive({ old: '', new: '' });
const menus = [
  { id: 'account', label: '帳號設定', icon: '👤' }, 
  { id: 'privacy', label: '隱私安全', icon: '🔒' }
];

onMounted(async () => {
  try {
    const res = await fetch(`${apiBaseUrl}/api/me/getInfo`, {
      method: 'GET',
      credentials: 'include'
    });
    if (!res.ok) {
      return;
    }
    const data = await res.json();
    userForm.name = data?.uname || userForm.name;
    userForm.email = data?.email || userForm.email;
  } catch (error) {
    console.error('取得使用者資訊失敗:', error);
  }
});

const startEditName = () => { 
  tempName.value = userForm.name; 
  isEditingName.value = true; 
};

const updateName = async () => {
  const nextName = tempName.value.trim();
  if (!nextName) return;
  if (isSavingName.value) return;
  isSavingName.value = true;
  try {
    const res = await fetch(`${apiBaseUrl}/api/me`, {
      method: 'PATCH',
      credentials: 'include',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ uname: nextName })
    });
    if (!res.ok) {
      const data = await res.json().catch(() => ({}));
      alert(data?.message || '名稱修改失敗');
      return;
    }
    userForm.name = nextName;
    isEditingName.value = false;
    alert('名稱已修改完成！');
  } catch (error) {
    console.error('更新名稱失敗:', error);
    alert('名稱修改失敗，請稍後再試。');
  } finally {
    isSavingName.value = false;
  }
};

const updatePass = async () => { 
  const oldPassword = passForm.old.trim();
  const newPassword = passForm.new.trim();
  if (!oldPassword || !newPassword) {
    alert('請輸入舊密碼與新密碼');
    return;
  }
  if (isSavingPassword.value) return;
  isSavingPassword.value = true;
  try {
    const res = await fetch(`${apiBaseUrl}/api/me`, {
      method: 'PATCH',
      credentials: 'include',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ oldPassword, newPassword })
    });
    if (!res.ok) {
      const data = await res.json().catch(() => ({}));
      alert(data?.message || '密碼修改失敗');
      return;
    }
    passForm.old = '';
    passForm.new = '';
    isEditingPassword.value = false; 
    alert('密碼已修改'); 
  } catch (error) {
    console.error('更新密碼失敗:', error);
    alert('密碼修改失敗，請稍後再試。');
  } finally {
    isSavingPassword.value = false;
  }
};

const clearAllData = () => { 
  if (confirm('確定要清除所有紀錄與設定嗎？此動作無法復原。')) {
    localStorage.clear(); 
    location.reload(); 
  }
};

// handleLogout 邏輯已搬移至元件內，此處移除
</script>

<style scoped>
/* 移除 Navbar 相關 CSS，保留基礎與設定頁面樣式 */
.content { flex: 1; padding: 40px; display: flex; justify-content: center; }
.container { width: 100%; max-width: 1000px; display: flex; gap: 30px; }

/* 側欄選單樣式 */
.settings-sidebar { flex: 3; display: flex; flex-direction: column; gap: 10px; }
.menu-item { padding: 15px 20px; border-radius: 16px; background: rgba(255, 255, 255, 0.3); cursor: pointer; display: flex; align-items: center; gap: 12px; transition: 0.3s; }
.menu-item:hover { background: rgba(255, 255, 255, 0.5); }
.menu-item.active { background: white; font-weight: 600; box-shadow: 0 4px 15px rgba(0,0,0,0.05); color: #5d7a5d; }

/* 詳細設定區樣式 */
.settings-detail { flex: 7; }
.glass-card { padding: 40px; height: 100%; overflow-y: auto; }

.section-title { font-size: 1.5rem; color: #2d3a2d; margin-bottom: 25px; }
.profile-header { display: flex; align-items: center; gap: 20px; margin-bottom: 30px; }
.current-avatar { font-size: 50px; background: white; width: 80px; height: 80px; display: flex; align-items: center; justify-content: center; border-radius: 50%; border: 2px solid #5d7a5d; }

.action-list { display: flex; flex-direction: column; }
.modify-box { border-bottom: 1px solid rgba(0,0,0,0.05); transition: 0.4s; padding: 10px 0; }
.modify-box.expanding { background: rgba(255, 255, 255, 0.3); border-radius: 15px; padding: 10px; margin: 0 -10px; }

.action-item { display: flex; justify-content: space-between; align-items: center; padding: 15px 0; }
.no-border { border-bottom: none; }
.title { font-weight: 500; color: #4a5d4a; }
.current-value, .static-val { font-size: 13px; color: #7a8a7a; margin-top: 4px; }

/* 表單樣式 */
.inline-form { padding-top: 10px; }
.form-group { display: flex; flex-direction: column; gap: 5px; margin-bottom: 15px; }
.form-group label { font-size: 12px; color: #7a8a7a; }
.form-group input { padding: 10px; border-radius: 8px; border: 1px solid #ddd; outline: none; transition: 0.3s; }
.form-group input:focus { border-color: #5d7a5d; box-shadow: 0 0 5px rgba(93, 122, 93, 0.2); }
.form-actions { display: flex; gap: 10px; }

/* 按鈕樣式 */
.primary-btn { background: #5d7a5d; color: white; border: none; padding: 8px 16px; border-radius: 8px; cursor: pointer; transition: 0.3s; }
.primary-btn:hover { background: #4a634a; }
.primary-btn.sm { font-size: 13px; }

.ghost-btn { background: transparent; border: 1px solid #5d7a5d; color: #5d7a5d; padding: 6px 12px; border-radius: 8px; cursor: pointer; font-size: 13px; transition: 0.3s; }
.ghost-btn:hover { background: rgba(93, 122, 93, 0.1); }
.ghost-btn.sm { padding: 4px 10px; }

.danger-btn { color: #d32f2f; border: 1px solid #ffcdd2; background: #fff5f5; padding: 6px 16px; border-radius: 8px; cursor: pointer; transition: 0.3s; }
.danger-btn:hover { background: #d32f2f; color: white; }
.danger { color: #d32f2f; font-weight: 600; }

/* 動畫 */
.expand-enter-active, .expand-leave-active { transition: all 0.3s ease-out; max-height: 250px; overflow: hidden; }
.expand-enter-from, .expand-leave-to { max-height: 0; opacity: 0; }

@media (max-width: 1024px) {
  .content {
    padding: 20px;
  }

  .container {
    gap: 16px;
  }

  .glass-card {
    padding: 24px;
  }
}

@media (max-width: 768px) {
  .content {
    padding: 12px;
  }

  .container {
    flex-direction: column;
  }

  .settings-sidebar {
    flex-direction: row;
    overflow-x: auto;
    gap: 8px;
  }

  .menu-item {
    flex: 0 0 auto;
    padding: 10px 14px;
    border-radius: 12px;
  }

  .settings-detail {
    min-height: 0;
  }

  .glass-card {
    padding: 18px;
    border-radius: 18px;
  }

  .profile-header {
    margin-bottom: 20px;
  }

  .current-avatar {
    width: 64px;
    height: 64px;
    font-size: 38px;
  }
}

@media (max-width: 480px) {
  .section-title {
    font-size: 1.2rem;
    margin-bottom: 16px;
  }

  .action-item {
    flex-direction: column;
    align-items: flex-start;
    gap: 8px;
  }

  .form-actions {
    width: 100%;
    flex-direction: column;
  }

  .form-actions .primary-btn,
  .form-actions .ghost-btn {
    width: 100%;
  }
}

</style>