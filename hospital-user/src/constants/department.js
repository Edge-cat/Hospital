/** 科室列表视觉配置：图标 + 主题色 */
export const DEPT_VISUAL = {
  内科: { icon: 'FirstAidKit', color: '#3370ff', bg: '#e8f3ff' },
  外科: { icon: 'Scissor', color: '#f77234', bg: '#fff3e8' },
  儿科: { icon: 'User', color: '#00b42a', bg: '#e8ffea' },
  信息科: { icon: 'Monitor', color: '#86909c', bg: '#f2f3f5' },
  心血管内科: { icon: 'TrendCharts', color: '#f53f3f', bg: '#ffece8' }
}

export const DEFAULT_DEPT_VISUAL = { icon: 'OfficeBuilding', color: '#3370ff', bg: '#e8f3ff' }

export function getDeptVisual(name) {
  return DEPT_VISUAL[name] || DEFAULT_DEPT_VISUAL
}

/** 号源余量状态 */
export function slotStatus(slots) {
  if (slots === 0) return { label: '今日已满', type: 'danger' }
  if (slots < 10) return { label: `今日余${slots}号`, type: 'warning' }
  return { label: `今日余${slots}号`, type: 'success' }
}

/** 候诊等待状态 */
export function waitStatus(minutes) {
  if (minutes <= 15) return { type: 'success' }
  if (minutes <= 25) return { type: 'warning' }
  return { type: 'danger' }
}

/** 出诊时段筛选 */
export const SHIFT_FILTERS = [
  { key: 'all', label: '全部时段' },
  { key: 'morning', label: '上午' },
  { key: 'afternoon', label: '下午' }
]

/** 挂号费用标准 */
export const REGISTER_FEE_LIST = [
  { label: '普通号', value: '普通号', fee: 10 },
  { label: '专家号', value: '专家号', fee: 50 },
  { label: '急诊号', value: '急诊号', fee: 20 }
]

const WEEKDAY_LABELS = ['周一', '周二', '周三', '周四', '周五', '周六', '周日']

/** 根据职称推荐默认号别 */
export function suggestRegisterType(title = '') {
  if (/主任|副主任/.test(title)) return '专家号'
  return '普通号'
}

/** 按出诊时段筛选医生 */
export function filterDoctorsByShift(doctors, shift) {
  if (!shift || shift === 'all') return doctors
  return doctors.filter((doc) => {
    const schedule = doc.weekSchedule || []
    return schedule.some((d) => (shift === 'morning' ? d.morning : d.afternoon))
  })
}

/** 号源余量文案 */
export function slotSummary(doc) {
  const parts = []
  if (doc.morningSlots > 0) parts.push(`上午余${doc.morningSlots}号`)
  else if (doc.morningSlots === 0) parts.push('上午已满')
  if (doc.afternoonSlots > 0) parts.push(`下午余${doc.afternoonSlots}号`)
  else if (doc.afternoonSlots === 0) parts.push('下午已满')
  return parts.join(' · ')
}

/** 是否还有可挂号码 */
export function hasAvailableSlots(doc, shift = 'all') {
  if (shift === 'morning') return doc.morningSlots > 0
  if (shift === 'afternoon') return doc.afternoonSlots > 0
  return doc.morningSlots > 0 || doc.afternoonSlots > 0
}

/** 本周排班日历标签 */
export function weekScheduleLabels(schedule = []) {
  return schedule.map((d) => ({
    ...d,
    weekday: d.weekday || WEEKDAY_LABELS[(new Date(d.date).getDay() + 6) % 7] || d.weekday
  }))
}
