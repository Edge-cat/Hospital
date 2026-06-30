import { ref, computed, watch } from 'vue'
import { userApi } from '@/api'
import { TIME_SLOTS } from '@/constants'
import { handleApiError } from '@/utils/apiError'
import { formatLocalDate, sumDateRemaining, patchDoctorRemaining, clearDoctorRemaining } from '@/utils/scheduleUtils'

/** 预约页：医生排班 + 未来日期余号（排除今日） */
export function useAppointmentSchedule(form, doctors, options = {}) {
  const futureOnly = options.futureOnly !== false
  const todayStr = formatLocalDate()

  const scheduleDates = ref([])
  const loadingSchedule = ref(false)
  const scheduleError = ref(null)

  const selectedDoctor = computed(() =>
    doctors.value.find((d) => d.name === form.doctorName)
  )

  const selectableDates = computed(() =>
    scheduleDates.value
      .filter((d) => {
        if (futureOnly && (d.isToday || d.date === todayStr)) return false
        return d.slots?.some((s) => s.available && TIME_SLOTS.includes(s.timeSlot))
      })
      .map((d) => ({
        ...d,
        dayRemaining: sumDateRemaining([d], d.date)
      }))
  )

  const timeSlotOptions = computed(() => {
    const day = scheduleDates.value.find((d) => d.date === form.appointmentDate)
    return TIME_SLOTS.map((timeSlot) => {
      const slot = day?.slots.find((s) => s.timeSlot === timeSlot)
      return {
        timeSlot,
        remaining: slot?.remaining ?? 0,
        available: !!slot?.available
      }
    })
  })

  const availableSlotCount = computed(() =>
    timeSlotOptions.value.filter((s) => s.available).length
  )

  const scheduleSummary = computed(() => {
    if (!form.appointmentDate || !form.timeSlot) return ''
    const slot = timeSlotOptions.value.find((s) => s.timeSlot === form.timeSlot)
    const day = selectableDates.value.find((d) => d.date === form.appointmentDate)
    const dateLabel = day?.label || form.appointmentDate
    return `${dateLabel} ${form.timeSlot}${slot?.available ? ` · 余${slot.remaining}号` : ''}`
  })

  function resetSchedule() {
    scheduleDates.value = []
    scheduleError.value = null
    form.appointmentDate = ''
    form.timeSlot = ''
    clearDoctorRemaining(doctors)
  }

  function syncDoctorRemaining(dateStr) {
    const doc = selectedDoctor.value
    if (!doc?.id || !dateStr) {
      clearDoctorRemaining(doctors)
      return
    }
    patchDoctorRemaining(doctors, doc.id, scheduleDates.value, dateStr)
  }

  async function loadSchedule() {
    const doc = selectedDoctor.value
    if (!doc?.id) {
      resetSchedule()
      return
    }

    scheduleError.value = null
    form.appointmentDate = ''
    form.timeSlot = ''
    clearDoctorRemaining(doctors)
    loadingSchedule.value = true

    try {
      const res = await userApi.appointmentSchedule({ doctorId: doc.id })
      scheduleDates.value = res.data?.dates || []
      const first = selectableDates.value[0]
      if (first) {
        selectDate(first.date)
      } else {
        scheduleError.value = futureOnly ? '该医生暂无可预约的未来号源' : '该医生暂无可预约时段'
      }
    } catch (e) {
      const body = handleApiError(e, { fallback: '加载排班失败' })
      scheduleError.value = body.message
      scheduleDates.value = []
    } finally {
      loadingSchedule.value = false
    }
  }

  function selectDate(date) {
    form.appointmentDate = date
    form.timeSlot = ''
    syncDoctorRemaining(date)
    const day = scheduleDates.value.find((d) => d.date === date)
    const first = day?.slots.find((s) => s.available && TIME_SLOTS.includes(s.timeSlot))
    if (first) form.timeSlot = first.timeSlot
  }

  function selectSlot(timeSlot) {
    const opt = timeSlotOptions.value.find((s) => s.timeSlot === timeSlot)
    if (opt?.available) form.timeSlot = timeSlot
  }

  watch(
    () => form.doctorName,
    (name) => {
      if (name) loadSchedule()
      else resetSchedule()
    }
  )

  return {
    scheduleDates,
    selectableDates,
    timeSlotOptions,
    availableSlotCount,
    scheduleSummary,
    loadingSchedule,
    scheduleError,
    loadSchedule,
    selectDate,
    selectSlot,
    resetSchedule,
    syncDoctorRemaining
  }
}
