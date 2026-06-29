export const REGISTER_STATUS = {
  0: { label: '待就诊', type: 'warning' },
  1: { label: '就诊中', type: 'primary' },
  2: { label: '已完成', type: 'success' },
  3: { label: '已退号', type: 'info' }
}

export const APPOINTMENT_STATUS = {
  0: { label: '待确认', type: 'warning' },
  1: { label: '已确认', type: 'primary' },
  2: { label: '已完成', type: 'success' },
  3: { label: '已取消', type: 'info' }
}

export const PAYMENT_STATUS = {
  0: { label: '待支付', type: 'warning' },
  1: { label: '已支付', type: 'success' }
}

export const PAY_METHODS = ['微信', '支付宝', '银行卡']

export const TIME_SLOTS = ['08:00-09:00', '09:00-10:00', '10:00-11:00', '14:00-15:00', '15:00-16:00', '16:00-17:00']

export const REGISTER_TYPES = [
  { label: '普通号', value: '普通号', fee: 10 },
  { label: '专家号', value: '专家号', fee: 50 }
]

export const STAT_COLORS = {
  primary: 'var(--feishu-primary)',
  success: 'var(--feishu-success)',
  warning: 'var(--feishu-warning)',
  danger: 'var(--feishu-danger)',
  neutral: 'var(--feishu-text-secondary)'
}
