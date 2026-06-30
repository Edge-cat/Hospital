/**
 * 全局路由守卫：未登录拦截 + 动态路由注入 + 403 兜底
 *
 * 流程：
 * 1. 白名单直接放行
 * 2. 无 token → 跳转登录
 * 3. 有 token 未加载动态路由 → generateRoutes(role) → replace 重进
 * 4. 目标路由不在权限内 → 403
 */
import { ElMessage } from 'element-plus'
import { useUserStore } from '@/stores/user'
import { usePermissionStore } from '@/stores/permission'
import { useDictStore } from '@/stores/dict'
import { ROUTE_WHITE_LIST } from './constantRoutes'
import { resolveRouteAudit } from '@/utils/auditMap'
import { reportOperation } from '@/utils/operationReport'

export function setupPermissionGuard(router) {
  router.beforeEach(async (to, _from, next) => {
    document.title = to.meta.title ? `${to.meta.title} - HIS管理端` : '东软云医院HIS管理端'

    const userStore = useUserStore()
    const permissionStore = usePermissionStore()

    // ---- 1. 白名单 ----
    if (ROUTE_WHITE_LIST.includes(to.path)) {
      if (to.path === '/login' && userStore.token) {
        next({ path: '/dashboard' })
      } else {
        next()
      }
      return
    }

    // ---- 2. 未登录 ----
    if (!userStore.token) {
      next({ name: 'Login', query: { redirect: to.fullPath } })
      return
    }

    // ---- 3. 动态路由（按角色注入） ----
    if (!permissionStore.isRoutesLoaded) {
      try {
        const role = userStore.role
        if (!role) {
          userStore.logout()
          next({ name: 'Login' })
          return
        }
        permissionStore.generateRoutes(role)
        next({ ...to, replace: true })
      } catch {
        userStore.logout()
        permissionStore.resetRoutes()
        ElMessage.error('加载菜单失败，请重新登录')
        next({ name: 'Login' })
      }
      return
    }

    // ---- 4. 路由权限校验 ----
    if (userStore.role === 'nurse' && to.path === '/patient/consultation') {
      next({ path: '/patient/billing-confirm', replace: true })
      return
    }

    if (to.matched.length === 0 || (to.name && to.name !== 'Layout' && !router.hasRoute(to.name))) {
      next({ name: 'Forbidden' })
      return
    }

    // ---- 5. 公共字典（可选，与权限并行加载） ----
    const dictStore = useDictStore()
    if (!dictStore.loaded) {
      try {
        await dictStore.fetchMeta()
      } catch {
        ElMessage.warning('字典数据加载失败')
      }
    }

    next()
  })

  router.afterEach((to) => {
    const audit = resolveRouteAudit(to)
    if (audit) reportOperation(audit)
  })
}
