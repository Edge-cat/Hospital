import { ref, computed, watch, onMounted, onUnmounted, type Ref } from 'vue'
import { hospitalService } from '@/services/HospitalService'
import { handleApiError, parseApiResponse } from '@/utils/apiError'
import type {
  DepartmentDTO,
  DoctorDTO,
  NoticeFeedDTO,
  OverviewStatsDTO,
  PaymentRecordDTO,
  PaymentListParams,
  RegisterRequestDTO,
  RegisterTypeDTO
} from '@/types/hospital'

/** 通用 async 状态包装 */
function useAsyncState<T>() {
  const loading = ref(false)
  const error = ref<string | null>(null)
  const data = ref<T | null>(null) as Ref<T | null>

  async function run(fn: () => Promise<T>, options?: { silent?: boolean }) {
    loading.value = true
    error.value = null
    try {
      data.value = await fn()
      return data.value
    } catch (e) {
      const body = handleApiError(e, { silent: options?.silent })
      error.value = body.message
      throw body
    } finally {
      loading.value = false
    }
  }

  return { loading, error, data, run }
}

/** 首页 Dashboard 数据 */
export function useHospitalDashboard() {
  const statsState = useAsyncState<OverviewStatsDTO>()
  const departments = ref<DepartmentDTO[]>([])
  const notices = ref<NoticeFeedDTO[]>([])
  const pageLoading = ref(false)

  async function loadDashboard() {
    pageLoading.value = true
    try {
      const [overviewRes, deptRes, noticeRes] = await Promise.all([
        hospitalService.getOverview(),
        hospitalService.getDepartments(),
        hospitalService.getNotices(5)
      ])
      statsState.data.value = parseApiResponse(overviewRes)
      departments.value = parseApiResponse(deptRes).list.filter((d) => d.parentId === 0).slice(0, 6)
      notices.value = parseApiResponse(noticeRes).list
    } catch (e) {
      handleApiError(e, { fallback: '加载首页数据失败' })
    } finally {
      pageLoading.value = false
    }
  }

  const stats = computed(() => statsState.data.value ?? { todayVisit: 0, patientCount: 0 })

  return { stats, departments, notices, pageLoading, loadDashboard }
}

/** 挂号页 ViewModel */
export function useHospitalRegister(initialPatientName = '') {
  const departments = ref<DepartmentDTO[]>([])
  const doctors = ref<DoctorDTO[]>([])
  const registerTypes = ref<RegisterTypeDTO[]>([])
  const loadingDepts = ref(false)
  const loadingDoctors = ref(false)
  const submitting = ref(false)
  /** 提交锁 — 防连点 */
  const submitLocked = ref(false)

  const form = ref<RegisterRequestDTO>({
    department: '',
    doctorName: '',
    registerType: '普通号',
    patientName: initialPatientName
  })

  const currentFee = computed(() => hospitalService.getRegisterFee(form.value.registerType, registerTypes.value))

  async function loadRegisterTypes() {
    registerTypes.value = await hospitalService.getRegisterTypes()
  }

  async function loadDepartments() {
    loadingDepts.value = true
    try {
      const res = await hospitalService.getDepartments()
      departments.value = parseApiResponse(res).list.filter((d) => !d.parentId || d.parentId === 0)
    } catch (e) {
      handleApiError(e, { fallback: '加载科室失败' })
    } finally {
      loadingDepts.value = false
    }
  }

  async function loadDoctors() {
    form.value.doctorName = ''
    if (!form.value.department) {
      doctors.value = []
      return
    }
    const dept = form.value.department
    loadingDoctors.value = true
    try {
      const res = await hospitalService.getDoctors(dept)
      if (form.value.department !== dept) return
      doctors.value = parseApiResponse(res).list
    } catch (e) {
      handleApiError(e, { fallback: '加载医生列表失败' })
    } finally {
      if (form.value.department === dept) loadingDoctors.value = false
    }
  }

  async function submitRegister(): Promise<boolean> {
    if (submitLocked.value || submitting.value) return false
    const payload = {
      ...form.value,
      patientName: form.value.patientName.trim()
    }
    if (!payload.patientName) return false
    submitLocked.value = true
    submitting.value = true
    try {
      const res = await hospitalService.submitRegister(payload)
      parseApiResponse(res)
      return true
    } catch (e) {
      handleApiError(e, { fallback: '挂号失败，请稍后重试' })
      return false
    } finally {
      submitting.value = false
      setTimeout(() => { submitLocked.value = false }, 800)
    }
  }

  return {
    form,
    departments,
    doctors,
    registerTypes,
    currentFee,
    loadingDepts,
    loadingDoctors,
    submitting,
    submitLocked,
    loadDepartments,
    loadDoctors,
    loadRegisterTypes,
    submitRegister
  }
}

/** 缴费页 ViewModel */
export function useHospitalPayment() {
  const tab = ref<'pending' | 'paid'>('pending')
  const list = ref<PaymentRecordDTO[]>([])
  const loading = ref(false)
  const payingId = ref<number | null>(null)
  const payLocked = ref(false)
  const page = ref(1)
  const pageSize = ref(10)
  const total = ref(0)
  const summary = ref({ count: 0, totalAmount: 0 })
  const selectedIds = ref<number[]>([])
  const payMethod = ref('微信')
  const dateRange = ref<[string, string] | null>(null)
  let pollTimer: ReturnType<typeof setInterval> | null = null

  const statusFilter = computed(() => (tab.value === 'pending' ? 0 : 1) as 0 | 1)

  async function loadSummary() {
    if (tab.value !== 'pending') return
    try {
      const res = await hospitalService.getPaymentSummary()
      summary.value = parseApiResponse(res)
    } catch {
      summary.value = { count: 0, totalAmount: 0 }
    }
  }

  async function loadPayments(resetPage = false) {
    if (resetPage) page.value = 1
    loading.value = true
    selectedIds.value = []
    try {
      const params: PaymentListParams = {
        status: statusFilter.value,
        page: page.value,
        pageSize: pageSize.value
      }
      if (tab.value === 'paid' && dateRange.value?.length === 2) {
        params.startDate = dateRange.value[0]
        params.endDate = dateRange.value[1]
      }
      const res = await hospitalService.getPaymentList(params)
      const data = parseApiResponse(res)
      list.value = data.list
      total.value = data.total
      if (tab.value === 'pending') await loadSummary()
    } catch (e) {
      handleApiError(e, { fallback: '加载缴费列表失败' })
    } finally {
      loading.value = false
    }
  }

  async function refreshAll() {
    await loadPayments()
    if (tab.value === 'pending') await loadSummary()
  }

  async function payBill(row: PaymentRecordDTO): Promise<boolean> {
    if (payLocked.value || payingId.value === row.id) return false
    if (row.status !== 0) {
      handleApiError({ code: 409, message: '该账单状态已变更，请刷新列表' })
      await refreshAll()
      return false
    }
    payLocked.value = true
    payingId.value = row.id
    try {
      await hospitalService.payBill({ id: row.id, payMethod: payMethod.value })
      page.value = 1
      await refreshAll()
      return true
    } catch (e) {
      handleApiError(e, { fallback: '支付失败，请重试' })
      return false
    } finally {
      payingId.value = null
      setTimeout(() => { payLocked.value = false }, 800)
    }
  }

  async function payBatch(): Promise<boolean> {
    if (!selectedIds.value.length || payLocked.value) return false
    payLocked.value = true
    try {
      await hospitalService.payBillBatch({ ids: [...selectedIds.value], payMethod: payMethod.value })
      selectedIds.value = []
      page.value = 1
      await refreshAll()
      return true
    } catch (e) {
      handleApiError(e, { fallback: '批量支付失败，请重试' })
      return false
    } finally {
      setTimeout(() => { payLocked.value = false }, 800)
    }
  }

  function toggleSelect(id: number, checked: boolean) {
    if (checked) {
      if (!selectedIds.value.includes(id)) selectedIds.value.push(id)
    } else {
      selectedIds.value = selectedIds.value.filter((v) => v !== id)
    }
  }

  function toggleSelectAll(checked: boolean) {
    selectedIds.value = checked ? list.value.map((r) => r.id) : []
  }

  const selectedAmount = computed(() =>
    list.value
      .filter((r) => selectedIds.value.includes(r.id))
      .reduce((sum, r) => sum + (r.amount || 0), 0)
  )

  function onPageChange(p: number) {
    page.value = p
    loadPayments()
  }

  function onTabChange() {
    dateRange.value = null
    loadPayments(true)
  }

  function onDateFilter() {
    loadPayments(true)
  }

  function startPolling() {
    stopPolling()
    pollTimer = setInterval(() => {
      if (tab.value === 'pending' && !payLocked.value) refreshAll()
    }, 30000)
  }

  function stopPolling() {
    if (pollTimer) {
      clearInterval(pollTimer)
      pollTimer = null
    }
  }

  watch(tab, onTabChange, { immediate: true })

  onMounted(startPolling)
  onUnmounted(stopPolling)

  return {
    tab,
    list,
    loading,
    payingId,
    payLocked,
    page,
    pageSize,
    total,
    summary,
    selectedIds,
    selectedAmount,
    payMethod,
    dateRange,
    loadPayments,
    refreshAll,
    payBill,
    payBatch,
    toggleSelect,
    toggleSelectAll,
    onPageChange,
    onDateFilter
  }
}
