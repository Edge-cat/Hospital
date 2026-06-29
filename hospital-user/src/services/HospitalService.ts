import request from '@/utils/request'
import type {
  ApiResponse,
  DepartmentDTO,
  DoctorDTO,
  NoticeFeedDTO,
  OverviewStatsDTO,
  PageResult,
  PaymentRecordDTO,
  PaymentRequestDTO,
  PaymentBatchRequestDTO,
  PaymentListParams,
  PaymentSummaryDTO,
  RegisterRecordDTO,
  RegisterRequestDTO,
  RegisterTypeDTO
} from '@/types/hospital'

/** 本地兜底 — 服务端不可用时使用 */
const FALLBACK_REGISTER_TYPES: RegisterTypeDTO[] = [
  { label: '普通号', value: '普通号', fee: 10 },
  { label: '专家号', value: '专家号', fee: 50 },
  { label: '急诊号', value: '急诊号', fee: 20 }
]

function normalizeFee(fee: unknown, fallback = 10): number {
  const n = Number(fee)
  return Number.isFinite(n) ? n : fallback
}

class HospitalService {
  async getOverview(): Promise<ApiResponse<OverviewStatsDTO>> {
    return request.get('/common/home-overview')
  }

  async getDepartments(): Promise<ApiResponse<PageResult<DepartmentDTO>>> {
    return request.get('/common/departments')
  }

  async getDoctors(department: string): Promise<ApiResponse<PageResult<DoctorDTO>>> {
    return request.get('/doctor/list', { params: { department } })
  }

  async getRegisterTypes(): Promise<RegisterTypeDTO[]> {
    try {
      const res = await request.get('/common/register-types', { silent: true })
      const list = (res.data || []) as RegisterTypeDTO[]
      if (!list.length) return [...FALLBACK_REGISTER_TYPES]
      return list.map((t) => ({
        label: t.label || t.value,
        value: t.value || t.label,
        fee: normalizeFee(t.fee)
      }))
    } catch {
      return [...FALLBACK_REGISTER_TYPES]
    }
  }

  getRegisterFee(registerType: string, types: RegisterTypeDTO[] = FALLBACK_REGISTER_TYPES): number {
    const fee = types.find((t) => t.value === registerType)?.fee
    return normalizeFee(fee)
  }

  async submitRegister(data: RegisterRequestDTO): Promise<ApiResponse<RegisterRecordDTO | null>> {
    return request.post('/register', data)
  }

  async getPaymentSummary(): Promise<ApiResponse<PaymentSummaryDTO>> {
    return request.get('/payment/summary')
  }

  async getPaymentList(params: PaymentListParams): Promise<ApiResponse<PageResult<PaymentRecordDTO>>> {
    return request.get('/payment/list', { params })
  }

  async payBill(data: PaymentRequestDTO): Promise<ApiResponse<PaymentRecordDTO | null>> {
    return request.post('/payment', data)
  }

  async payBillBatch(data: PaymentBatchRequestDTO): Promise<ApiResponse<{ list: PaymentRecordDTO[] }>> {
    return request.post('/payment/batch', data)
  }

  async getNotices(limit = 5): Promise<ApiResponse<PageResult<NoticeFeedDTO>>> {
    return request.get('/notice/list', { params: { pageSize: limit } })
  }
}

export const hospitalService = new HospitalService()
