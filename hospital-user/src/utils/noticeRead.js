const STORAGE_KEY = 'his_notice_read_ids'

export function getReadNoticeIds() {
  try {
    return new Set(JSON.parse(localStorage.getItem(STORAGE_KEY) || '[]').map(Number))
  } catch {
    return new Set()
  }
}

export function markNoticeRead(id) {
  const set = getReadNoticeIds()
  set.add(Number(id))
  localStorage.setItem(STORAGE_KEY, JSON.stringify([...set]))
}
