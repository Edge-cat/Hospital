/** 小程序 API / 页面 → 操作日志（业务类由后端落库，前端不再重复上报） */

export const AUDIT_CLIENT = 'mini'

const PATH_ACTION_RULES = [
  { re: /\/auth\/(login|wx-login)$/, module: '登录', action: '登录了系统' }
]

/** 已由后端业务接口写入 sys_operation_log 的路径 */
const BACKEND_HANDLED = [
  /^\/register(\/\d+\/cancel)?$/,
  /^\/appointment(\/\d+\/cancel)?$/,
  /^\/payment$/
]

function normalizePath(url = '') {
  return url.split('?')[0].replace(/^\/api/, '')
}

export function resolveApiAudit(options) {
  const method = (options.method || 'GET').toUpperCase()
  if (method === 'GET') return null

  const path = normalizePath(options.url || '')
  if (!path || path.startsWith('/audit/')) return null

  if (BACKEND_HANDLED.some((re) => re.test(path))) return null

  for (const rule of PATH_ACTION_RULES) {
    if (rule.re.test(path)) {
      return { module: rule.module, action: rule.action, path }
    }
  }

  return null
}

export function resolvePageAudit(pagePath, title) {
  if (!pagePath || pagePath.includes('login')) return null
  return {
    module: '页面访问',
    action: `访问了${title || '页面'}`,
    path: pagePath
  }
}
