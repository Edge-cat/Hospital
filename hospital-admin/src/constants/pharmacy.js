/** 药品分类徽标 */
export const DRUG_TYPE_TAG = {
  处方药: { type: 'danger', label: 'Rx' },
  OTC: { type: 'success', label: 'OTC' }
}

export const DRUG_RISK_TAG = {
  高危: { type: 'danger', label: '高危' },
  普通: { type: 'info', label: '普通' }
}

/** 采购生命周期 */
export const PROCUREMENT_PHASES = [
  { value: '', label: '全部' },
  { value: 0, label: '待审批' },
  { value: 1, label: '采购中' },
  { value: 2, label: '待入库' },
  { value: 3, label: '已完成' }
]

export const PROCUREMENT_PHASE_TAG = {
  0: { label: '待审批', type: 'info' },
  1: { label: '采购中', type: 'warning' },
  2: { label: '待入库', type: 'primary' },
  3: { label: '已完成', type: 'success' }
}

/** 配药优先级 */
export const DISPENSE_PRIORITY = {
  急诊: { color: '#f56c6c', bg: '#fef0f0', label: '急诊加急' },
  门诊: { color: '#409eff', bg: '#ecf5ff', label: '普通门诊' },
  住院: { color: '#e6a23c', bg: '#fdf6ec', label: '住院发药' }
}

/** 库存调整类型 */
export const INVENTORY_ADJUST_TYPES = [
  { value: 'stocktake', label: '盘点盈亏' },
  { value: 'disposal', label: '效期销毁' },
  { value: 'transfer', label: '院内调拨' }
]

export const DRUG_COLORS = ['#409eff', '#67c23a', '#e6a23c', '#f56c6c', '#909399']

export function drugThumbColor(name = '') {
  let hash = 0
  for (let i = 0; i < name.length; i++) hash = name.charCodeAt(i) + ((hash << 5) - hash)
  return DRUG_COLORS[Math.abs(hash) % DRUG_COLORS.length]
}
