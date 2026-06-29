/**
 * 用户端权限 Store：导航菜单动态渲染占位
 *
 * 当前按登录态过滤；后续可在 asyncRoutes 上扩展 meta.roles
 */
import { defineStore } from 'pinia'
import { computed } from 'vue'
import { useUserStore } from './user'
import { asyncRoutes, buildNavItems } from '@/router/routes'

export const usePermissionStore = defineStore('permission', () => {
  const userStore = useUserStore()

  const navItems = computed(() =>
    buildNavItems(asyncRoutes, {
      isLoggedIn: userStore.isLoggedIn,
      role: userStore.role
    })
  )

  /** 预留：切换账号时重置（与管理端 resetRoutes 对齐） */
  function reset() {}

  return { navItems, reset }
})
