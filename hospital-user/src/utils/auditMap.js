/** 用户端 API / 路由 → 操作日志 */

export const AUDIT_CLIENT = 'user'

const PATH_ACTION_RULES = [
  { re: /\/auth\/login$/, module: '用户端', action: '登录' },
  { re: /\/register$/, module: '挂号', action: '提交挂号' },
  { re: /\/register\/\d+\/cancel$/, module: '挂号', action: '取消挂号' },
  { re: /\/appointment$/, module: '预约', action: '提交预约' },
  { re: /\/appointment\/\d+\/cancel$/, module: '预约', action: '取消预约' },
  { re: /\/payment$/, module: '缴费', action: '在线缴费' }
]

function normalizePath(url = '') {
  return url.split('?')[0].replace(/^\/api/, '')
}

export function resolveApiAudit(config) {
  const method = (config.method || 'get').toUpperCase()
  if (method === 'GET') return null

  const path = normalizePath(config.url || '')
  if (!path || path.startsWith('/audit/')) return null

  for (const rule of PATH_ACTION_RULES) {
    if (rule.re.test(path)) {
      return { module: rule.module, action: rule.action, path }
    }
  }

  const segments = path.split('/').filter(Boolean)
  const root = segments[0] || '用户端'
  return {
    module: '用户端',
    action: `${method} ${root}`,
    path
  }
}

export function resolveRouteAudit(to) {
  if (!to?.path || to.path === '/login') return null
  return {
    module: '页面访问',
    action: to.meta?.title || to.name || to.path,
    path: to.fullPath
  }
}
