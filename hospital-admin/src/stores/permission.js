/**
 * 权限 Store：动态路由注入 + 侧边栏菜单
 *
 * 与 userStore.role 配合，在路由守卫中调用 generateRoutes(role)
 */
import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import router from '@/router'
import { asyncRoutes, filterRoutesByRole, buildMenuGroups } from '@/router/routes'

export const usePermissionStore = defineStore('permission', () => {
  const isRoutesLoaded = ref(false)
  const accessRoutes = ref([])
  const addedRouteNames = ref([])

  const menuGroups = computed(() => buildMenuGroups(accessRoutes.value))

  const hasDashboard = computed(() =>
    accessRoutes.value.some((r) => r.name === 'Dashboard')
  )

  /** 根据角色过滤并注册动态路由 */
  function generateRoutes(role) {
    resetRoutes()
    const filtered = filterRoutesByRole(asyncRoutes, role)
    filtered.forEach((route) => {
      router.addRoute('Layout', route)
      if (route.name) addedRouteNames.value.push(route.name)
    })
    accessRoutes.value = filtered
    isRoutesLoaded.value = true
    return filtered
  }

  /** 登出或切换账号时清除动态路由 */
  function resetRoutes() {
    addedRouteNames.value.forEach((name) => {
      if (router.hasRoute(name)) router.removeRoute(name)
    })
    addedRouteNames.value = []
    accessRoutes.value = []
    isRoutesLoaded.value = false
  }

  return {
    isRoutesLoaded,
    accessRoutes,
    menuGroups,
    hasDashboard,
    generateRoutes,
    resetRoutes
  }
})
