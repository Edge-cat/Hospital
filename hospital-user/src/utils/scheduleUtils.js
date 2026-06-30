/** 排班余号工具（与小程序 deptIcon.js 对齐） */

export function formatLocalDate(d = new Date()) {
  const y = d.getFullYear()
  const m = String(d.getMonth() + 1).padStart(2, '0')
  const day = String(d.getDate()).padStart(2, '0')
  return `${y}-${m}-${day}`
}

/** 汇总指定日期各时段剩余号源 */
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

/** 更新医生列表上的动态余号（仅选中医生，未来预约） */
export function patchDoctorRemaining(doctorsRef, doctorId, scheduleDates, dateStr) {
  if (!doctorsRef?.value) return
  const remaining = sumDateRemaining(scheduleDates, dateStr)
  doctorsRef.value = doctorsRef.value.map((d) =>
    d.id === doctorId ? { ...d, remaining } : { ...d, remaining: null }
  )
}

export function clearDoctorRemaining(doctorsRef) {
  if (!doctorsRef?.value) return
  doctorsRef.value = doctorsRef.value.map((d) => ({ ...d, remaining: null }))
}
