/** 班次色块配置 */
export const SHIFT_COLORS = {
  早班: { bg: '#e8f4ff', border: '#409eff', text: '#409eff' },
  中班: { bg: '#fdf6ec', border: '#e6a23c', text: '#e6a23c' },
  晚班: { bg: '#f0f9eb', border: '#67c23a', text: '#67c23a' },
  停诊: { bg: '#f4f4f5', border: '#909399', text: '#909399' }
}

/** 医疗服务分类树 */
export const SERVICE_CATEGORIES = [
  { id: 'all', label: '全部服务', icon: 'Grid' },
  { id: '检查', label: '检查类', icon: 'Monitor' },
  { id: '治疗', label: '治疗类', icon: 'FirstAidKit' },
  { id: '手术', label: '手术类', icon: 'Scissor' },
  { id: '其他', label: '其他服务', icon: 'More' }
]

export const SERVICE_CATEGORY_MAP = {
  体检套餐: '检查',
  专家会诊: '检查',
  门诊挂号: '其他',
  疫苗接种: '治疗',
  康复理疗: '治疗'
}

export const SPECIALTY_TAG_TYPE = {
  心血管: 'danger',
  骨科手术: 'warning',
  儿科保健: 'success',
  眼科疾病: 'primary',
  普外: 'info'
}
