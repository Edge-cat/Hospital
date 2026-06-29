export const NOTICE_TABS = [
  { key: 'all', label: '全部' },
  { key: '紧急', label: '紧急' },
  { key: '通知', label: '通知' },
  { key: '公告', label: '公告' }
]

export const NOTICE_TYPE_TAG = {
  紧急: 'danger',
  通知: 'info',
  公告: 'success'
}

export const NOTICE_SORT_OPTIONS = [
  { value: 'priority', label: '重要程度' },
  { value: 'time', label: '发布时间' }
]

const TYPE_ORDER = { 紧急: 0, 通知: 1, 公告: 2 }

export function sortNotices(list, mode = 'priority') {
  const sorted = [...list]
  if (mode === 'time') {
    sorted.sort((a, b) => (b.publishTime || '').localeCompare(a.publishTime || ''))
  } else {
    sorted.sort((a, b) => {
      const ta = TYPE_ORDER[a.type] ?? 9
      const tb = TYPE_ORDER[b.type] ?? 9
      if (ta !== tb) return ta - tb
      return (b.publishTime || '').localeCompare(a.publishTime || '')
    })
  }
  return sorted
}
