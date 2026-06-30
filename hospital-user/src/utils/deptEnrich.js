/** 后端未 enrich 时的科室展示兜底（与 mini deptIcon 对齐） */

export const DEPT_CODES = {
  内科: 'NK',
  外科: 'WK',
  儿科: 'EK',
  骨科: 'GK',
  眼科: 'YK',
  皮肤科: 'PFK',
  口腔科: 'KQK',
  信息科: 'XXK'
}

export const DEPT_META = {
  内科: { code: 'NK', leader: '张主任', phone: '010-12345601', outpatient: true },
  外科: { code: 'WK', leader: '李主任', phone: '010-12345602', outpatient: true },
  儿科: { code: 'EK', leader: '王主任', phone: '010-12345603', outpatient: true },
  骨科: { code: 'GK', leader: '赵主任', phone: '010-12345604', outpatient: true },
  眼科: { code: 'YK', leader: '刘主任', phone: '010-12345605', outpatient: true },
  皮肤科: { code: 'PFK', leader: '陈主任', phone: '010-12345606', outpatient: true },
  口腔科: { code: 'KQK', leader: '周主任', phone: '010-12345607', outpatient: true },
  信息科: { code: 'XXK', leader: '赵主任', phone: '010-12345608', outpatient: false }
}

export function enrichDepartment(dept) {
  if (!dept) return dept
  const meta = DEPT_META[dept.name] || {}
  const slots = Number(dept.todaySlots)
  return {
    ...dept,
    code: dept.code || meta.code || DEPT_CODES[dept.name] || '',
    desc: dept.desc || dept.description || `${dept.name}门诊与住院诊疗服务`,
    leader: dept.leader || meta.leader || '',
    phone: dept.phone || meta.phone || '',
    outpatient: dept.outpatient !== undefined ? dept.outpatient : meta.outpatient !== false,
    todaySlots: Number.isFinite(slots) ? slots : 0,
    waitingCount: dept.waitingCount ?? 0,
    avgWaitMinutes: dept.avgWaitMinutes ?? 0,
    specialties: dept.specialties || [],
    commonDiseases: dept.commonDiseases || []
  }
}

export function enrichDepartmentList(list) {
  return (list || []).map(enrichDepartment)
}
