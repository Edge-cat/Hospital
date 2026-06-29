import { AUDIT_CLIENT } from './auditMap'

const recentKeys = new Map()
const DEDUP_MS = 3000

function shouldSkip(key) {
  const now = Date.now()
  const last = recentKeys.get(key)
  if (last && now - last < DEDUP_MS) return true
  recentKeys.set(key, now)
  return false
}

export function reportOperation(payload, client = AUDIT_CLIENT) {
  if (!payload?.module || !payload?.action) return

  const token = uni.getStorageSync('his_token')
  if (!token) return

  const key = `${client}:${payload.module}:${payload.action}:${payload.path || ''}`
  if (shouldSkip(key)) return

  const base = import.meta.env.VITE_API_BASE || '/api'
  uni.request({
    url: `${base}/audit/report`,
    method: 'POST',
    data: {
      module: payload.module,
      action: payload.action,
      path: payload.path,
      detail: payload.detail,
      client,
      status: payload.status ?? 1
    },
    header: {
      'Content-Type': 'application/json',
      Authorization: `Bearer ${token}`
    },
    fail() {}
  })
}
