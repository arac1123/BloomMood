<!--這我拿來測試的 可以不用管-->

<template>
  <div style="padding: 16px;">
    <h2>Register Test</h2>

    <button @click="callRegister" :disabled="loading">
      {{ loading ? "Registering..." : "Call Register API" }}
    </button>

    <p v-if="error" style="color: red; margin-top: 12px;">
      {{ error }}
    </p>

    <pre v-if="result" style="margin-top: 12px; white-space: pre-wrap;">
{{ result }}
    </pre>
  </div>
</template>

<script setup>
import { onMounted, ref } from "vue";

const loading = ref(false);
const result = ref("");
const error = ref("");

// ✅ 你的後端 base url（用 Vite env 比較乾淨）
const API_BASE = import.meta.env.VITE_API_BASE_URL || "http://localhost:3001";

async function callRegister() {
  loading.value = true;
  error.value = "";
  result.value = "";

  try {
    // ⚠️ 測試用資料：email 要換成沒註冊過的，不然會撞 unique
    const payload = {
      email: `test_${Date.now()}@a.com`,
      uname: "FrontendTest",
      password: "123456",
    };

    const res = await fetch(`${API_BASE}/api/auth/register`, {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify(payload),
    });

    // 不是 2xx 也把 body 印出來，方便 debug
    const text = await res.text();
    if (!res.ok) {
      throw new Error(`HTTP ${res.status}: ${text}`);
    }

    // 如果後端回 JSON，這裡會更漂亮，但 text 也能看
    result.value = text;
  } catch (e) {
    error.value = e?.message || String(e);
  } finally {
    loading.value = false;
  }
}

// ✅ 進頁面就打
onMounted(() => {
  callRegister();
});
</script>
