<script setup>
import { onMounted, ref } from "vue";

const msg = ref("loading...");
onMounted(async () => {
  try {
    const res = await fetch("http://localhost:3001/api/health"); // ✅ 建議走 proxy
    const data = await res.json();
    msg.value = JSON.stringify(data);
  } catch (e) {
    msg.value = String(e);
    console.error(e);
  }
});
</script>

<template>
  <div>{{ msg }}</div>
</template>


<style scoped>
.logo {
  height: 6em;
  padding: 1.5em;
  will-change: filter;
  transition: filter 300ms;
}
.logo:hover {
  filter: drop-shadow(0 0 2em #646cffaa);
}
.logo.vue:hover {
  filter: drop-shadow(0 0 2em #42b883aa);
}
</style>
