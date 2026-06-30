<template>
  <nav class="app-tabbar" aria-label="主导航">
    <router-link
      v-for="tab in tabs"
      :key="tab.path"
      :to="tab.path"
      class="app-tabbar__item"
      :class="{ 'is-active': isActive(tab.path) }"
      @click="onTap"
    >
      <span class="app-tabbar__icon-wrap">
        <component :is="tab.icon" :size="20" :stroke-width="isActive(tab.path) ? 2.25 : 1.75" />
      </span>
      <span class="app-tabbar__label">{{ tab.label }}</span>
    </router-link>
  </nav>
</template>

<script setup>
import { useRoute } from 'vue-router'
import { Home, ClipboardList, CalendarDays, Wallet, User } from 'lucide-vue-next'

const route = useRoute()

const tabs = [
  { path: '/home', label: '首页', icon: Home },
  { path: '/register', label: '挂号', icon: ClipboardList },
  { path: '/appointment', label: '预约', icon: CalendarDays },
  { path: '/payment', label: '缴费', icon: Wallet },
  { path: '/profile', label: '我的', icon: User }
]

function isActive(path) {
  if (path === '/home') return route.path === '/home' || route.path === '/'
  return route.path.startsWith(path)
}

function onTap(e) {
  const el = e.currentTarget
  el?.classList.add('is-tap')
  setTimeout(() => el?.classList.remove('is-tap'), 200)
}
</script>

<style scoped>
.app-tabbar {
  display: none;
  position: fixed;
  bottom: 0;
  left: 0;
  right: 0;
  z-index: 200;
  height: 56px;
  padding-bottom: env(safe-area-inset-bottom, 0);
  background: rgba(255, 255, 255, 0.92);
  -webkit-backdrop-filter: blur(12px);
  backdrop-filter: blur(12px);
  border-top: 1px solid var(--feishu-border-light);
  grid-template-columns: repeat(5, 1fr);
}

@media (max-width: 768px) {
  .app-tabbar {
    display: grid;
  }
}

.app-tabbar__item {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 2px;
  text-decoration: none;
  color: var(--feishu-text-tertiary);
  transition: color 0.15s ease, transform 0.15s ease;
  -webkit-tap-highlight-color: transparent;
}

.app-tabbar__item.is-active {
  color: var(--feishu-text-primary);
}

.app-tabbar__item.is-tap {
  transform: scale(0.92);
}

.app-tabbar__icon-wrap {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 32px;
  height: 28px;
  border-radius: 8px;
  transition: background 0.15s ease;
}

.app-tabbar__item.is-active .app-tabbar__icon-wrap {
  background: var(--feishu-primary-bg);
  color: var(--feishu-primary);
}

.app-tabbar__label {
  font-size: 10px;
  font-weight: 500;
  letter-spacing: 0.02em;
}

.app-tabbar__item.is-active .app-tabbar__label {
  font-weight: 600;
}
</style>
