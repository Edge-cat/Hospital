/** 管理端 API / 路由 → 操作日志模块与动作 */

export const AUDIT_CLIENT = 'admin'

const MODULE_BY_SEGMENT = {
  patient: '患者管理',
  doctor: '人事管理',
  schedule: '人事管理',
  record: '人事管理',
  service: '人事管理',
  drug: '药房管理',
  procurement: '药房管理',
  inventory: '药房管理',
  dispensing: '药房管理',
  finance: '财务管理',
  reimbursement: '财务管理',
  settlement: '财务管理',
  register: '挂号管理',
  appointment: '预约管理',
  payment: '缴费管理',
  bed: '床位管理',
  notice: '公告管理',
  statistics: '统计分析',
  admin: '系统管理',
  auth: '系统'
}

const ACTION_BY_METHOD = {
  POST: '提交',
  PUT: '更新',
  DELETE: '删除'
}

const PATH_ACTION_RULES = [
  { re: /\/cancel$/, action: '取消' },
  { re: /\/confirm$/, action: '确认' },
  { re: /\/approve$/, action: '审批' },
  { re: /\/refund$/, action: '退款' },
  { re: /\/stock-in$/, action: '入库' },
  { re: /\/advance$/, action: '推进流程' },
  { re: /\/archive$/, action: '归档' },
  { re: /\/batch-delete$/, action: '批量删除' },
  { re: /\/batch-import$/, action: '批量导入' },
  { re: /\/consultation$/, action: '开始就诊' },
  { re: /\/finish-consultation$/, action: '结束就诊' },
  { re: /\/login$/, action: '登录' }
]

function normalizePath(url = '') {
  return url.split('?')[0].replace(/^\/api/, '')
}

function resolveAction(method, path) {
  for (const rule of PATH_ACTION_RULES) {
    if (rule.re.test(path)) return rule.action
  }
  return ACTION_BY_METHOD[method] || method
}

/** 从 axios 请求配置解析审计信息（仅写操作） */
export function resolveApiAudit(config) {
  const method = (config.method || 'get').toUpperCase()
  if (method === 'GET') return null

  const path = normalizePath(config.url || '')
  if (!path || path.startsWith('/audit/')) return null

  const segments = path.split('/').filter(Boolean)
  const root = segments[0] || 'system'
  const module = MODULE_BY_SEGMENT[root] || '系统'

  return {
    module,
    action: resolveAction(method, path),
    path
  }
}

/** 路由访问上报 */
export function resolveRouteAudit(to) {
  if (!to?.path || to.path === '/login') return null
  return {
    module: '页面访问',
    action: to.meta?.title || to.name || to.path,
    path: to.fullPath
  }
}
