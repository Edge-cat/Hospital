/** 各角色工作台快捷入口（按优先级排序） */
export const ROLE_QUICK_LINKS = {
  admin: [
    'PatientInfo', 'PatientConsultation', 'BusinessRegister', 'BusinessPayment',
    'HrDoctor', 'PharmacyDrug', 'FinanceInfo', 'StatisticsAnalysis', 'AdminUser'
  ],
  doctor: [
    'PatientConsultation', 'PatientAdd', 'PatientSearch', 'HrRecord',
    'HrSchedule', 'HrService', 'BusinessNotice'
  ],
  nurse: [
    'PatientInfo', 'BusinessRegister', 'BusinessAppointment', 'BusinessPayment',
    'PatientConsultation', 'PatientSearch', 'BusinessBed', 'HrSchedule'
  ]
}

export const NOTICE_TYPE_STYLE = {
  紧急: { border: '#f56c6c', bg: '#fef0f0', tag: 'danger' },
  公告: { border: '#67c23a', bg: '#f0f9eb', tag: 'success' },
  通知: { border: '#409eff', bg: '#ecf5ff', tag: 'info' }
}
