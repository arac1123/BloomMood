import { createApp } from 'vue'
import App from './App.vue'
import router from './router' // 引入你的路由地圖
import './assets/main.css'   // 引入樣式

const app = createApp(App)
app.use(router)              // 啟用路由
app.mount('#app')