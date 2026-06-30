/**
 * 用户端路由守卫：未登录拦截（meta.auth）
 */
import { useUserStore } from '@/stores/user'
import { ROUTE_WHITE_LIST } from './constantRoutes'
import { resolveRouteAudit } from '@/utils/auditMap'
import { reportOperation } from '@/utils/operationReport'

export function setupPermissionGuard(router) {
  router.beforeEach((to, _from, next) => {
    document.title = to.meta.title ? `${to.meta.title} - 东软云医院` : '东软云医院用户端'

    const userStore = useUserStore()

    if (ROUTE_WHITE_LIST.includes(to.path)) {
      if ((to.path === '/login' || to.path === '/signup') && userStore.token) {
        next({ path: '/home' })
      } else {
        next()
      }
      return
    }

    if (to.meta.auth && !userStore.token) {
      next({ name: 'Login', query: { redirect: to.fullPath } })
      return
    }

    next()
  })

  router.afterEach((to) => {
    const audit = resolveRouteAudit(to)
    if (audit) reportOperation(audit)
  })
}
