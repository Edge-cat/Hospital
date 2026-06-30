/** 号源变更标记：缴费 / 退号后通知预约页刷新 */
export const SLOT_REFRESH_KEY = 'his_slot_refresh'

export function markSlotsStale() {
  uni.setStorageSync(SLOT_REFRESH_KEY, Date.now())
}

export function clearSlotsStale() {
  uni.removeStorageSync(SLOT_REFRESH_KEY)
}

export function formatLocalDate(d = new Date()) {
  const y = d.getFullYear()
  const m = String(d.getMonth() + 1).padStart(2, '0')
  const day = String(d.getDate()).padStart(2, '0')
  return `${y}-${m}-${day}`
}
