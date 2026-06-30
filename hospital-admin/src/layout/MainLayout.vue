<template>
  <el-container class="layout-container">
    <el-aside :width="collapsed ? '64px' : '240px'" class="layout-aside">
      <div class="logo">
        <div class="logo-icon">
          <el-icon :size="22" class="logo-icon__svg"><FirstAidKit /></el-icon>
        </div>
        <span v-show="!collapsed" class="logo-text">HIS 管理端</span>
      </div>
      <el-menu
        :default-active="activeMenu"
        :collapse="collapsed"
        router
        class="feishu-menu"
        background-color="var(--feishu-bg-white)"
        text-color="var(--feishu-text-secondary)"
        active-text-color="var(--feishu-primary)"
      >
        <el-menu-item v-if="permissionStore.hasDashboard" index="/dashboard">
          <el-icon><Odometer /></el-icon>
          <template #title>工作台</template>
        </el-menu-item>

        <el-sub-menu v-for="group in permissionStore.menuGroups" :key="group.name" :index="group.name">
          <template #title>
            <el-icon><component :is="group.icon" /></el-icon>
            <span>{{ group.name }}</span>
          </template>
          <el-menu-item
            v-for="item in group.children"
            :key="item.path"
            :index="item.path"
          >
            {{ item.title }}
          </el-menu-item>
        </el-sub-menu>
      </el-menu>
    </el-aside>

    <el-container>
      <el-header class="layout-header">
        <div class="header-left">
          <el-icon class="collapse-btn" @click="collapsed = !collapsed">
            <Fold v-if="!collapsed" />
            <Expand v-else />
          </el-icon>
          <el-breadcrumb separator="/">
            <el-breadcrumb-item :to="{ path: '/dashboard' }">首页</el-breadcrumb-item>
            <el-breadcrumb-item v-if="currentGroup">{{ currentGroup }}</el-breadcrumb-item>
            <el-breadcrumb-item v-if="currentTitle">{{ currentTitle }}</el-breadcrumb-item>
          </el-breadcrumb>
        </div>
        <div class="header-right">
          <el-tag v-if="userStore.roleLabel" size="small" type="info" class="role-tag">
            {{ userStore.roleLabel }}
          </el-tag>
          <el-dropdown @command="handleCommand">
            <span class="user-info">
              <el-avatar :size="32" icon="UserFilled" />
              <span class="user-name">{{ userStore.userName }}</span>
            </span>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item command="logout">退出登录</el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </div>
      </el-header>

      <el-main class="layout-main">
        <router-view v-slot="{ Component }">
          <transition name="fade-slide" mode="out-in">
            <component :is="Component" />
          </transition>
        </router-view>
      </el-main>
    </el-container>
  </el-container>
</template>

<script setup>
import { ref, computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { usePermissionStore } from '@/stores/permission'
import { useDictStore } from '@/stores/dict'
import { useOperationFeed } from '@/composables/useOperationFeed'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()
const permissionStore = usePermissionStore()
const dictStore = useDictStore()
const collapsed = ref(false)

useOperationFeed()

const activeMenu = computed(() => route.path)
const currentTitle = computed(() => route.meta.title)
const currentGroup = computed(() => route.meta.group)

function handleCommand(cmd) {
  if (cmd === 'logout') {
    permissionStore.resetRoutes()
    dictStore.reset()
    userStore.logout()
    router.push('/login')
  }
}
</script>

<style scoped>
.layout-container { height: 100vh; }

.layout-aside {
  display: flex;
  flex-direction: column;
  height: 100vh;
  background: var(--feishu-bg-white);
  border-right: 1px solid var(--feishu-border);
  transition: width 0.2s;
  overflow: hidden;
}

.logo {
  height: 56px;
  flex-shrink: 0;
  display: flex;
  align-items: center;
  padding: 0 16px;
  gap: 10px;
  border-bottom: 1px solid var(--feishu-border-light);
}

.logo-icon {
  width: 32px;
  height: 32px;
  background: var(--feishu-primary-bg);
  border-radius: var(--feishu-radius-md);
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}

.logo-icon__svg { color: var(--feishu-primary); }

.logo-text {
  font-size: 15px;
  font-weight: 600;
  color: var(--feishu-text-primary);
  white-space: nowrap;
}

.layout-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  background: var(--feishu-bg-white);
  border-bottom: 1px solid var(--feishu-border);
  padding: 0 24px;
  height: 56px;
}

.header-left { display: flex; align-items: center; gap: 16px; }

.collapse-btn {
  font-size: 18px;
  cursor: pointer;
  color: var(--feishu-text-secondary);
  padding: 6px;
  border-radius: var(--feishu-radius-sm);
  transition: background 0.2s;
}

.collapse-btn:hover { background: var(--feishu-bg-base); }

.header-right { display: flex; align-items: center; gap: 12px; }

.role-tag {
  border: none;
  background: var(--feishu-primary-bg);
  color: var(--feishu-primary);
}

.user-info {
  display: flex;
  align-items: center;
  gap: 8px;
  cursor: pointer;
  padding: 4px 8px;
  border-radius: var(--feishu-radius-sm);
  transition: background 0.2s;
}

.user-info:hover { background: var(--feishu-bg-base); }

.user-name { color: var(--feishu-text-primary); font-size: 14px; }

.layout-main { background: var(--feishu-bg-base); padding: 0; }

.feishu-menu {
  flex: 1;
  min-height: 0;
  overflow-y: auto;
  overflow-x: hidden;
  border-right: none !important;
  padding: 8px;
}

.feishu-menu :deep(.el-menu-item),
.feishu-menu :deep(.el-sub-menu__title) {
  border-radius: var(--feishu-radius-sm);
  margin-bottom: 2px;
  height: 40px;
  line-height: 40px;
}

.feishu-menu :deep(.el-menu-item.is-active) {
  background: var(--feishu-primary-bg) !important;
  color: var(--feishu-primary) !important;
  font-weight: 500;
}

.feishu-menu :deep(.el-menu-item:hover),
.feishu-menu :deep(.el-sub-menu__title:hover) {
  background: var(--feishu-bg-base) !important;
}
</style>
