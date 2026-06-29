/** 小程序 API / 页面 → 操作日志 */

export const AUDIT_CLIENT = 'mini'

const PATH_ACTION_RULES = [
  { re: /\/auth\/(login|wx-login)$/, module: '小程序', action: '登录' },
  { re: /\/register$/, module: '挂号', action: '提交挂号' },
  { re: /\/register\/\d+\/cancel$/, module: '挂号', action: '取消挂号' },
  { re: /\/appointment$/, module: '预约', action: '提交预约' },
  { re: /\/appointment\/\d+\/cancel$/, module: '预约', action: '取消预约' },
  { re: /\/payment$/, module: '缴费', action: '在线缴费' }
]

function normalizePath(url = '') {
  return url.split('?')[0].replace(/^\/api/, '')
}

export function resolveApiAudit(options) {
  const method = (options.method || 'GET').toUpperCase()
  if (method === 'GET') return null

  const path = normalizePath(options.url || '')
  if (!path || path.startsWith('/audit/')) return null

  for (const rule of PATH_ACTION_RULES) {
    if (rule.re.test(path)) {
      return { module: rule.module, action: rule.action, path }
    }
  }

  const segments = path.split('/').filter(Boolean)
  const root = segments[0] || '小程序'
  return {
    module: '小程序',
    action: `${method} ${root}`,
    path
  }
}

export function resolvePageAudit(pagePath, title) {
  if (!pagePath || pagePath.includes('login')) return null
  return {
    module: '页面访问',
    action: title || pagePath,
    path: pagePath
  }
}
