/** 余额展示：仅透支/异常用警示色 */
export function balanceClass(row) {
  if (row.status === 0) return 'balance-frozen'
  if (row.balance < 0 || row.overdraft) return 'balance-danger'
  if (row.balance < (row.warnThreshold || 10000)) return 'balance-warn'
  return 'balance-normal'
}

export const BANK_OPTIONS = ['工商银行', '建设银行', '农业银行']

export const REIMBURSE_PRIORITY = {
  overdue: { label: '超期', type: 'danger' },
  urgent: { label: '加急', type: 'warning' }
}

export const SETTLEMENT_UNPAID_LEVEL = (unpaid, overdue) => {
  if (unpaid <= 0) return 'normal'
  if (overdue) return 'danger'
  if (unpaid > 1000) return 'warning'
  return 'pending'
}

export const PAYMENT_CHANNELS = ['微信', '支付宝', '银行卡', '医保统筹', '现金']
