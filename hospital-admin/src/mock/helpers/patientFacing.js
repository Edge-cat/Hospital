const WEEKDAY_NAMES = ['周日', '周一', '周二', '周三', '周四', '周五', '周六']
const SCHEDULE_SLOTS = ['08:00-09:00', '09:00-10:00', '10:00-11:00', '14:00-15:00', '15:00-16:00', '16:00-17:00']

export const REGISTER_FEES = { 普通号: 10, 专家号: 50, 急诊号: 20 }

export function buildAppointmentSchedule(doctorId, days = 8) {
  const dates = []
  for (let i = 0; i < days; i++) {
    const d = new Date()
    d.setDate(d.getDate() + i)
    const dateStr = d.toISOString().slice(0, 10)
    const weekday = WEEKDAY_NAMES[d.getDay()]
    const slots = SCHEDULE_SLOTS.map((timeSlot, idx) => {
      const remaining = ((doctorId * 3 + idx + i) % 4) + 1
      const full = (doctorId + idx + i) % 7 === 0
      return { timeSlot, remaining: full ? 0 : remaining, available: !full }
    })
    dates.push({
      date: dateStr,
      weekday,
      shortDate: dateStr.slice(5),
      label: i === 0 ? '今日' : i === 1 ? '明天' : weekday,
      isToday: i === 0,
      slots
    })
  }
  return dates
}

export function enrichPaymentDetail(payment) {
  if (!payment) return null
  const type = payment.itemName?.includes('检查')
    ? 'check'
    : payment.itemName?.includes('药')
      ? 'medicine'
      : 'register'
  const breakdown =
    payment.feeBreakdown ||
    (type === 'check'
      ? [
          { name: '血常规', amount: Math.round(payment.amount * 0.15) },
          { name: '影像检查', amount: Math.round(payment.amount * 0.85) }
        ]
      : [{ name: payment.itemName, amount: payment.amount }])
  return {
    ...payment,
    itemType: payment.itemType || type,
    advice:
      payment.advice ||
      (payment.status === 0
        ? '缴费完成后请按导引单前往相应科室，如有疑问请咨询导诊台。'
        : '缴费已完成，请保留凭证以备查验。'),
    guideTip: payment.guideTip || '支持电子凭证，可在「我的-缴费记录」中查看。',
    feeBreakdown: breakdown,
    voucherNo: payment.status === 1 ? payment.voucherNo || `PZ${payment.paymentNo?.slice(-8) || Date.now()}` : undefined
  }
}

export function pendingPaymentSummary(payments) {
  const pending = payments.filter((p) => p.status === 0)
  return {
    count: pending.length,
    totalAmount: pending.reduce((s, p) => s + (p.amount || 0), 0)
  }
}

export function nextId(list) {
  return list.length ? Math.max(...list.map((item) => item.id)) + 1 : 1
}
