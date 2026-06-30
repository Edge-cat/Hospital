/** 患者端操作日志 → 管理端可读中文文案 */

export const CLIENT_LABEL = { admin: '管理端', user: '用户Web', mini: '小程序' }

/** 是否为技术性兜底日志（如 POST payment） */
export function isTechnicalLog(item) {
  if (!item?.action) return true
  return /^(GET|POST|PUT|DELETE|PATCH)\s/i.test(item.action)
}

/** 根据路径/英文动作推断中文说明（兼容历史脏数据） */
function inferChineseAction(item) {
  const op = item?.operator || '用户'
  const path = (item?.path || '').replace(/^\/api/, '')
  const raw = (item?.action || '').trim()
  const detail = item?.detail ? `（${item.detail}）` : ''
  const root = path.split('/').filter(Boolean)[0] || ''

  const byPath = {
    payment: `${op}已完成支付`,
    register: `${op}提交了挂号申请`,
    appointment: `${op}提交了预约申请`
  }
  if (path.includes('/cancel')) {
    if (path.startsWith('/register')) return `${op}已取消挂号${detail}`
    if (path.startsWith('/appointment')) return `${op}已取消预约${detail}`
  }
  if (byPath[root]) return `${byPath[root]}${detail}`

  const m = raw.match(/^(POST|PUT|DELETE|PATCH)\s+(\w+)/i)
  if (m && byPath[m[2].toLowerCase()]) return `${byPath[m[2].toLowerCase()]}${detail}`

  return ''
}

/** 通知标题：客户端 · 业务模块 */
export function formatOperationTitle(item) {
  const client = CLIENT_LABEL[item?.client] || '患者端'
  let module = item?.module || '操作'
  if (module === '小程序') module = '患者操作'
  return `${client} · ${module}`
}

/** 通知/列表正文：完整中文操作说明 */
export function formatOperationMessage(item) {
  if (!item) return ''

  if (isTechnicalLog(item)) {
    return inferChineseAction(item)
  }

  const op = item.operator || '用户'
  const action = item.action || ''
  const detail = item.detail ? `（${item.detail}）` : ''

  // 动作本身已是完整句子（以了/过/录等结尾）
  if (/[了过]$/.test(action) || action.includes('申请')) {
    return `${op}${action}${detail}`
  }

  // 兼容旧数据
  const legacy = {
    提交挂号: `${op}提交了挂号申请`,
    在线缴费: `${op}已完成支付`,
    取消挂号: `${op}已取消挂号`,
    提交预约: `${op}提交了预约申请`,
    取消预约: `${op}已取消预约`,
    登录: `${op}登录了系统`
  }
  if (legacy[action]) {
    return `${legacy[action]}${detail}`
  }

  return detail ? `${op}${action}${detail}` : `${op}${action}`
}

/** 表格「操作说明」列 */
export function formatOperationSummary(item) {
  const msg = formatOperationMessage(item)
  if (msg) return msg
  if (isTechnicalLog(item)) return inferChineseAction(item) || '-'
  return item?.action ? `${item.operator || ''} ${item.action}`.trim() : '-'
}
