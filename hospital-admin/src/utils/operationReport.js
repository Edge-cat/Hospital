import axios from 'axios'
import { useUserStore } from '@/stores/user'
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

/** 前端关键操作上报（静默失败，不阻塞主流程） */
export function reportOperation(payload, client = AUDIT_CLIENT) {
  if (!payload?.module || !payload?.action) return

  const userStore = useUserStore()
  if (!userStore.token) return

  const key = `${client}:${payload.module}:${payload.action}:${payload.path || ''}`
  if (shouldSkip(key)) return

  const base = import.meta.env.VITE_API_BASE || '/api'
  axios
    .post(
      `${base}/audit/report`,
      {
        module: payload.module,
        action: payload.action,
        path: payload.path,
        detail: payload.detail,
        client,
        status: payload.status ?? 1
      },
      {
        headers: { Authorization: `Bearer ${userStore.token}` },
        timeout: 5000
      }
    )
    .catch(() => {})
}
