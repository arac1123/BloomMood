import { createRouter, createWebHistory } from 'vue-router';
import Login from '../components/Login.vue';
import Register from '../components/register.vue';
import Home from '../components/home.vue';
import garden from '../components/garden.vue';
import journal from '../components/journal.vue';
import statistics from '../components/statistics.vue';
import setting from '../components/setting.vue';
import test from '../components/test.vue';
import header from '../components/header.vue';

const routes = [
  { path: '/', name: 'Login', component: Login }, // 💡 確保這裡 path 是 /
  { path: '/register', name: 'Register', component: Register },
  { path: '/home', name: 'Home', component: Home },
  { path: '/garden', name: 'Garden', component: garden },
  { path: '/journal', name: 'Journal', component: journal   },
  { path: '/statistics', name: 'Statistics', component: statistics },
  {path: '/setting', name: 'Setting', component: setting },
  {path: '/test', name: 'Test', component: test },
  {path: '/header', name: 'Header', component: header }
];

const router = createRouter({
  history: createWebHistory(),
  routes
});

export default router;

