<template>
  <div class="user-layout">
    <header class="header">
      <div class="header-inner">
        <div class="brand" @click="$router.push('/home')">
          <div class="brand-icon">
            <el-icon :size="20" color="#3370ff"><FirstAidKit /></el-icon>
          </div>
          <span class="brand-name">东软云医院</span>
          <span class="brand-tag">用户端</span>
        </div>
        <nav class="nav">
          <router-link v-for="item in permissionStore.navItems" :key="item.path" :to="item.path" class="nav-link" active-class="active">
            {{ item.title }}
          </router-link>
        </nav>
        <div class="header-actions">
          <template v-if="userStore.isLoggedIn">
            <el-dropdown @command="handleCommand">
              <span class="user-entry">
                <el-avatar :size="32">{{ userStore.userName.charAt(0) }}</el-avatar>
                <span>{{ userStore.userName }}</span>
              </span>
              <template #dropdown>
                <el-dropdown-menu>
                  <el-dropdown-item command="profile">个人中心</el-dropdown-item>
                  <el-dropdown-item command="my-register">我的挂号</el-dropdown-item>
                  <el-dropdown-item command="my-appointment">我的预约</el-dropdown-item>
                  <el-dropdown-item command="records">就诊记录</el-dropdown-item>
                  <el-dropdown-item divided command="logout">退出登录</el-dropdown-item>
                </el-dropdown-menu>
              </template>
            </el-dropdown>
          </template>
          <el-button v-else type="primary" @click="$router.push('/login')">登录</el-button>
        </div>
      </div>
    </header>

    <main class="main">
      <router-view v-slot="{ Component }">
        <transition name="fade-slide" mode="out-in">
          <component :is="Component" />
        </transition>
      </router-view>
    </main>

    <footer class="footer">
      <p>东软云医院HIS系统 · 用户端 · 智慧医疗 便捷就医</p>
      <p class="footer-sub">如需工作人员操作，请访问管理端</p>
    </footer>

    <AppTabBar />
  </div>
</template>

<script setup>
import { useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { usePermissionStore } from '@/stores/permission'
import AppTabBar from '@/components/layout/AppTabBar.vue'

const router = useRouter()
const userStore = useUserStore()
const permissionStore = usePermissionStore()

function handleCommand(cmd) {
  if (cmd === 'logout') {
    permissionStore.reset()
    userStore.logout()
    router.push('/home')
  } else {
    router.push(`/${cmd}`)
  }
}
</script>

<style scoped>
.user-layout {
  min-height: 100vh;
  display: flex;
  flex-direction: column;
  background: var(--feishu-bg-base);
}

.header {
  background: #fff;
  border-bottom: 1px solid var(--feishu-border);
  position: sticky;
  top: 0;
  z-index: 100;
}

.header-inner {
  max-width: 1200px;
  margin: 0 auto;
  padding: 0 24px;
  height: 56px;
  display: flex;
  align-items: center;
  gap: 32px;
}

.brand {
  display: flex;
  align-items: center;
  gap: 10px;
  cursor: pointer;
  flex-shrink: 0;
}

.brand-icon {
  width: 32px;
  height: 32px;
  background: var(--feishu-primary-bg);
  border-radius: var(--feishu-radius-md);
  display: flex;
  align-items: center;
  justify-content: center;
}

.brand-name { font-size: 16px; font-weight: 600; color: var(--feishu-text-primary); }

.brand-tag {
  font-size: 12px;
  background: var(--feishu-primary-bg);
  color: var(--feishu-primary);
  padding: 2px 8px;
  border-radius: 4px;
  font-weight: 500;
}

.nav { flex: 1; display: flex; gap: 4px; overflow-x: auto; }

.nav-link {
  padding: 6px 14px;
  color: var(--feishu-text-secondary);
  text-decoration: none;
  border-radius: var(--feishu-radius-sm);
  font-size: 14px;
  transition: all 0.15s;
  white-space: nowrap;
}

.nav-link:hover { color: var(--feishu-text-primary); background: var(--feishu-bg-base); }

.nav-link.active {
  color: var(--feishu-primary);
  background: var(--feishu-primary-bg);
  font-weight: 500;
}

.header-actions { flex-shrink: 0; }

.user-entry {
  display: flex;
  align-items: center;
  gap: 8px;
  cursor: pointer;
  color: var(--feishu-text-primary);
  padding: 4px 8px;
  border-radius: var(--feishu-radius-sm);
  font-size: 14px;
}

.user-entry:hover { background: var(--feishu-bg-base); }

.main {
  flex: 1;
  max-width: 1200px;
  width: 100%;
  margin: 0 auto;
  padding: 24px;
}

@media (max-width: 768px) {
  .main {
    padding: 16px 16px 72px;
  }
}

.footer {
  background: #fff;
  border-top: 1px solid var(--feishu-border);
  color: var(--feishu-text-tertiary);
  text-align: center;
  padding: 20px;
  font-size: 13px;
}

.footer-sub { margin-top: 6px; font-size: 12px; color: #bbbfc4; }

@media (max-width: 768px) {
  .header-inner { gap: 16px; padding: 0 16px; }
  .nav { display: none; }
}
</style>
