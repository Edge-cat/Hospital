/** 用户端 API / 路由 → 操作日志（业务类由后端落库） */

export const AUDIT_CLIENT = 'user'

const PATH_ACTION_RULES = [
  { re: /\/auth\/login$/, module: '登录', action: '登录了系统' }
]

const BACKEND_HANDLED = [
  /^\/register(\/\d+\/cancel)?$/,
  /^\/appointment(\/\d+\/cancel)?$/,
  /^\/payment$/
]

function normalizePath(url = '') {
  return url.split('?')[0].replace(/^\/api/, '')
}

export function resolveApiAudit(config) {
  const method = (config.method || 'get').toUpperCase()
  if (method === 'GET') return null

  const path = normalizePath(config.url || '')
  if (!path || path.startsWith('/audit/')) return null

  if (BACKEND_HANDLED.some((re) => re.test(path))) return null

  for (const rule of PATH_ACTION_RULES) {
    if (rule.re.test(path)) {
      return { module: rule.module, action: rule.action, path }
    }
  }

  return null
}

export function resolveRouteAudit(to) {
  if (!to?.path || to.path === '/login') return null
  const title = to.meta?.title || to.name || '页面'
  return {
    module: '页面访问',
    action: `访问了${title}`,
    path: to.fullPath
  }
}
