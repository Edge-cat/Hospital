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

/** 科室标准编码（与后端 sys_department.code 一致） */
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

/** 后端仅返回 description 时的展示 enrich（与 mock / 管理端字段对齐） */
export const DEPT_META = {
  内科: {
    code: 'NK',
    leader: '张主任',
    desc: '诊治心血管、呼吸、消化等内科疾病，配备完善检查与慢病管理体系。',
    commonDiseases: ['高血压', '糖尿病', '冠心病']
  },
  外科: {
    code: 'WK',
    leader: '李主任',
    desc: '开展普外、微创及创伤急救等诊疗服务。',
    commonDiseases: ['阑尾炎', '疝气', '外伤']
  },
  儿科: {
    code: 'EK',
    leader: '王主任',
    desc: '专注儿童保健与儿科疾病诊疗。',
    commonDiseases: ['发热', '咳嗽', '腹泻']
  },
  骨科: {
    code: 'GK',
    leader: '赵主任',
    desc: '骨骼、关节、运动损伤专业治疗。',
    commonDiseases: ['骨折', '关节炎', '运动损伤']
  },
  眼科: {
    code: 'YK',
    leader: '刘主任',
    desc: '眼部疾病检查与手术治疗。',
    commonDiseases: ['白内障', '近视', '干眼症']
  },
  皮肤科: {
    code: 'PFK',
    leader: '陈主任',
    desc: '诊治湿疹、痤疮、皮炎等常见皮肤病，提供皮肤镜检测与激光治疗。',
    commonDiseases: ['湿疹', '痤疮', '皮炎']
  },
  口腔科: {
    code: 'KQK',
    leader: '周主任',
    desc: '口腔内科、修复、正畸及洁牙保健，关注儿童与成人口腔健康。',
    commonDiseases: ['龋齿', '牙周炎', '口腔溃疡']
  },
  信息科: {
    code: 'XXK',
    leader: '赵主任',
    desc: '医院信息系统运维与技术支持，不对外提供门诊挂号服务。',
    commonDiseases: [],
    outpatient: false
  }
}

export function withDeptIcon(dept) {
  const meta = DEPT_META[dept?.name] || {}
  const apiDesc = dept?.desc || dept?.description
  return {
    ...dept,
    icon: dept.icon || DEPT_ICONS[dept.name] || '🏥',
    code: dept.code || meta.code || DEPT_CODES[dept.name] || '',
    desc: meta.desc || apiDesc || `${dept.name}门诊与住院诊疗服务`,
    leader: dept.leader || meta.leader || '',
    commonDiseases: dept.commonDiseases || meta.commonDiseases || [],
    outpatient: dept.outpatient !== undefined ? dept.outpatient : meta.outpatient !== false
  }
}

export function mapDeptList(list) {
  return (list || [])
    .filter((d) => (!d.parentId || d.parentId === 0) && d.outpatient !== false)
    .sort((a, b) => (a.sort ?? 0) - (b.sort ?? 0))
    .map(withDeptIcon)
}

export function waitLabel(dept) {
  if (!dept?.waitingCount) return ''
  return `候诊约${dept.avgWaitMinutes || '—'}分钟 · ${dept.waitingCount}人`
}

/** 汇总指定日期各时段剩余号源；未传 dateStr 时汇总全部排班日 */
export function sumDateRemaining(scheduleDates, dateStr) {
  if (!scheduleDates?.length) return 0
  const days = dateStr
    ? scheduleDates.filter((d) => d.date === dateStr)
    : scheduleDates
  return days.reduce((total, day) => {
    const daySum = (day.slots || []).reduce((sum, s) => sum + (Number(s.remaining) || 0), 0)
    return total + daySum
  }, 0)
}

/** @deprecated 使用 sumDateRemaining(scheduleDates, dateStr) */
export function sumTodayRemaining(scheduleDates) {
  return sumDateRemaining(scheduleDates, scheduleDates?.[0]?.date)
}
