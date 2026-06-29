/** 标准后端 JSON 响应 */
export interface ApiResponse<T = unknown> {
  code: number
  message: string
  data: T
}

export interface PageResult<T> {
  list: T[]
  total: number
}

export interface DepartmentDTO {
  id: number
  name: string
  code?: string
  leader?: string
  parentId?: number
  phone?: string
  desc?: string
  commonDiseases?: string[]
  specialties?: string[]
  todaySlots?: number
  waitingCount?: number
  avgWaitMinutes?: number
}

export interface DoctorWeekScheduleDTO {
  date: string
  weekday: string
  morning: boolean
  afternoon: boolean
}

export interface DoctorDTO {
  id: number
  name: string
  title: string
  department: string
  specialty?: string
  morningSlots?: number
  afternoonSlots?: number
  weekSchedule?: DoctorWeekScheduleDTO[]
}

export interface RegisterTypeDTO {
  label: string
  value: string
  fee: number
}

/** 挂号提交请求体 */
export interface RegisterRequestDTO {
  department: string
  doctorName: string
  registerType: string
  patientName: string
}

export interface RegisterRecordDTO extends RegisterRequestDTO {
  id: number
  registerNo: string
  fee: number
  status: number
  registerTime: string
}

/** 缴费单据 */
export interface PaymentRecordDTO {
  id: number
  paymentNo: string
  patientName: string
  itemName: string
  amount: number
  status: 0 | 1 | 2
  payMethod?: string
  payTime?: string
  createTime?: string
  dueDate?: string
}

export interface PaymentSummaryDTO {
  count: number
  totalAmount: number
}

export interface PaymentListParams {
  status: 0 | 1
  page?: number
  pageSize?: number
  startDate?: string
  endDate?: string
}

export interface PaymentRequestDTO {
  id: number
  payMethod: string
}

export interface PaymentBatchRequestDTO {
  ids: number[]
  payMethod: string
}

export interface OverviewStatsDTO {
  todayVisit: number
  patientCount: number
  doctorCount?: number
  todayIncome?: number
}

export interface NoticeFeedDTO {
  id: number
  title: string
  type: string
  publishTime: string
  content?: string
}

export interface PatientInfoDTO {
  name: string
  phone: string
  idCard?: string
  cardNo?: string
  gender?: number
  allergyHistory?: string
  chronicDisease?: string
  address?: string
}

export interface AppointmentSlotDTO {
  timeSlot: string
  remaining: number
  available: boolean
}

export interface AppointmentScheduleDayDTO {
  date: string
  weekday: string
  shortDate?: string
  label?: string
  isToday?: boolean
  slots: AppointmentSlotDTO[]
}
