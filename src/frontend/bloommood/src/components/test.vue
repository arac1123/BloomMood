<!--這我拿來測試的 可以不用管-->

<template>
  <div style="padding: 16px;">
    <h2>Google OAuth Code Flow Test (Cookie JWT)</h2>

    <div style="margin: 12px 0; display: flex; gap: 8px; flex-wrap: wrap;">
      <button @click="startGoogleLogin" :disabled="loading">
        {{ loading ? "Working..." : "Login with Google (get code)" }}
      </button>

      <button @click="callMe" :disabled="loading">
        Call /api/me/getInfo
      </button>

      <button @click="showCookies" :disabled="loading">
        Show document.cookie
      </button>

      <button @click="logout" :disabled="loading">
        Logout
      </button>
    </div>

    <p style="margin: 8px 0;">
      API_BASE: <code>{{ API_BASE }}</code>
    </p>
    <p style="margin: 8px 0;">
      CLIENT_ID: <code>{{ clientId ? clientId : '(missing VITE_GOOGLE_CLIENT_ID)' }}</code>
    </p>

    <p v-if="error" style="color: red; margin-top: 12px;">
      {{ error }}
    </p>

    <pre v-if="result" style="margin-top: 12px; white-space: pre-wrap;">
{{ result }}
    </pre>
  </div>
</template>

<script setup>
import { ref } from "vue";
import { useRouter } from "vue-router";

const router = useRouter();

const loading = ref(false);
const result = ref("");
const error = ref("");

const API_BASE = import.meta.env.VITE_API_BASE_URL || "http://localhost:3001";
const clientId = import.meta.env.VITE_GOOGLE_CLIENT_ID;

async function sendCodeToBackend(code) {
  loading.value = true;
  error.value = "";
  result.value = "";

  try {
    const res = await fetch(`${API_BASE}/api/auth/google`, {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      // ✅ cookie-based JWT: must include credentials so browser stores Set-Cookie
      credentials: "include",
      body: JSON.stringify({ code }),
    });

    const text = await res.text();
    if (!res.ok) {
      throw new Error(`HTTP ${res.status}: ${text}`);
    }

    result.value = text || "ok";

    // ✅ 登入成功就導向 /home
    // await router.push("/home");
  } catch (e) {
    error.value = e?.message || String(e);
  } finally {
    loading.value = false;
  }
}

async function callMe() {
  loading.value = true;
  error.value = "";
  result.value = "";

  try {
    const res = await fetch(`${API_BASE}/api/me/getInfo`, {
      method: "GET",
      credentials: "include",
    });

    const text = await res.text();
    if (!res.ok) {
      throw new Error(`HTTP ${res.status}: ${text}`);
    }

    result.value = text;
  } catch (e) {
    error.value = e?.message || String(e);
  } finally {
    loading.value = false;
  }
}

async function logout() {
  loading.value = true;
  error.value = "";
  result.value = "";

  try {
    const res = await fetch(`${API_BASE}/api/auth/logout`, {
      method: "POST",
      credentials: "include",
    });
    const text = await res.text();
    result.value = text || "ok";
  } catch (e) {
    error.value = e?.message || String(e);
  } finally {
    loading.value = false;
  }
}

function startGoogleLogin() {
  error.value = "";
  result.value = "";

  if (!clientId) {
    error.value = "Missing VITE_GOOGLE_CLIENT_ID. Create .env.local (see .env.example).";
    return;
  }

  const oauth2 = window.google?.accounts?.oauth2;
  if (!oauth2?.initCodeClient) {
    error.value = "Google GIS script not loaded. Check index.html includes https://accounts.google.com/gsi/client";
    return;
  }

  // Teaching version: no PKCE, backend exchanges code with client_secret.
  const codeClient = oauth2.initCodeClient({
    client_id: clientId,
    scope: "openid email profile",
    ux_mode: "popup",
    callback: (response) => {
      if (response?.error) {
        error.value = `${response.error}: ${response.error_description || ""}`.trim();
        return;
      }
      const code = response?.code;
      if (!code) {
        error.value = `Missing code in response: ${JSON.stringify(response)}`;
        return;
      }
      sendCodeToBackend(code);
    },
    error_callback: (err) => {
      // err.type: popup_failed_to_open | popup_closed | unknown
      error.value = `GIS error: ${err?.type || "unknown"}`;
    },
  });

  codeClient.requestCode();
}

function showCookies() {
  // Note: HttpOnly cookies (like our JWT cookie) are NOT readable by JS.
  // This button is mainly for confirming that behavior.
  const c = document.cookie;
  result.value = c && c.length ? c : "(document.cookie is empty)\n\nIf you expect accessToken here: it's HttpOnly, so JS can't read it.\nCheck DevTools -> Application -> Cookies instead.";
}
</script>
