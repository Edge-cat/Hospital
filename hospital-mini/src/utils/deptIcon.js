/** 科室名称 → 展示图标（API 不返回 icon 时客户端映射） */
export const DEPT_ICONS = {
  内科: '🫀',
  外科: '🏥',
  儿科: '👶',
  骨科: '🦴',
  眼科: '👁️',
  皮肤科: '🧴',
  口腔科: '🦷',
  信息科: '💻',
  心血管内科: '❤️'
}

export function withDeptIcon(dept) {
  return {
    ...dept,
    icon: dept.icon || DEPT_ICONS[dept.name] || '🏥'
  }
}

export function mapDeptList(list) {
  return (list || [])
    .filter((d) => d.parentId === 0 && d.outpatient !== false)
    .sort((a, b) => (a.sort ?? 0) - (b.sort ?? 0))
    .map(withDeptIcon)
}

export function waitLabel(dept) {
  if (!dept?.waitingCount) return ''
  return `候诊约${dept.avgWaitMinutes || '—'}分钟 · ${dept.waitingCount}人`
}
