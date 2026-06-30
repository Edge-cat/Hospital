import request from '@/utils/request'

export const patientApi = {
  list: (params) => request.get('/patient/list', { params }),
  get: (id) => request.get(`/patient/${id}`),
  getDetail: (id) => request.get(`/patient/${id}/detail`),
  create: (data) => request.post('/patient', data),
  update: (id, data) => request.put(`/patient/${id}`, data),
  remove: (id) => request.delete(`/patient/${id}`),
  batchRemove: (ids) => request.post('/patient/batch-delete', { ids }),
  search: (params) => request.get('/patient/search', { params }),
  startConsultation: (id) => request.post(`/patient/${id}/consultation`),
  finishConsultation: (id, data) => request.post(`/patient/${id}/finish-consultation`, data),
  getConsultationRecord: (id) => request.get(`/patient/${id}/consultation-record`),
  joinQueue: (id) => request.post(`/patient/${id}/queue`)
}

export const doctorApi = {
  list: (params) => request.get('/doctor/list', { params }),
  create: (data) => request.post('/doctor', data),
  update: (id, data) => request.put(`/doctor/${id}`, data),
  remove: (id) => request.delete(`/doctor/${id}`),
  batchImport: (list) => request.post('/doctor/batch-import', { list })
}

export const scheduleApi = {
  list: (params) => request.get('/schedule/list', { params }),
  calendar: (params) => request.get('/schedule/calendar', { params }),
  create: (data) => request.post('/schedule', data),
  update: (id, data) => request.put(`/schedule/${id}`, data),
  remove: (id) => request.delete(`/schedule/${id}`),
  getAffected: (id) => request.get(`/schedule/${id}/affected`),
  cancel: (id, data) => request.post(`/schedule/${id}/cancel`, data)
}

export const recordApi = {
  list: (params) => request.get('/record/list', { params }),
  create: (data) => request.post('/record', data),
  update: (id, data) => request.put(`/record/${id}`, data),
  remove: (id) => request.delete(`/record/${id}`),
  getChain: (id) => request.get(`/record/${id}/chain`),
  revise: (id, data) => request.post(`/record/${id}/revise`, data),
  withdraw: (id, data) => request.post(`/record/${id}/withdraw`, data)
}

export const billingApi = {
  pending: (params) => request.get('/billing/pending', { params }),
  detail: (recordId) => request.get(`/billing/${recordId}`),
  confirm: (recordId, data) => request.post(`/billing/${recordId}/confirm`, data)
}

export const serviceApi = {
  list: (params) => request.get('/service/list', { params }),
  create: (data) => request.post('/service', data),
  update: (id, data) => request.put(`/service/${id}`, data),
  remove: (id) => request.delete(`/service/${id}`),
  toggle: (id, status) => request.put(`/service/${id}/toggle`, { status })
}

export const drugApi = {
  list: (params) => request.get('/drug/list', { params }),
  get: (id) => request.get(`/drug/${id}`),
  create: (data) => request.post('/drug', data),
  update: (id, data) => request.put(`/drug/${id}`, data),
  archive: (id) => request.post(`/drug/${id}/archive`),
  getInventory: (id) => request.get(`/drug/${id}/inventory`),
  getProcurementTrend: (id) => request.get(`/drug/${id}/procurement-trend`)
}

export const procurementApi = {
  list: (params) => request.get('/procurement/list', { params }),
  create: (data) => request.post('/procurement', data),
  update: (id, data) => request.put(`/procurement/${id}`, data),
  advance: (id) => request.post(`/procurement/${id}/advance`),
  stockIn: (id) => request.post(`/procurement/${id}/stock-in`),
  getLogistics: (id) => request.get(`/procurement/${id}/logistics`)
}

export const inventoryApi = {
  list: (params) => request.get('/inventory/list', { params }),
  update: (id, data) => request.put(`/inventory/${id}`, data),
  adjust: (id, data) => request.post(`/inventory/${id}/adjust`, data),
  createProcurement: (id, data) => request.post(`/inventory/${id}/procurement-request`, data)
}

export const dispensingApi = {
  list: (params) => request.get('/dispensing/list', { params }),
  create: (data) => request.post('/dispensing', data),
  update: (id, data) => request.put(`/dispensing/${id}`, data),
  complete: (id, data) => request.post(`/dispensing/${id}/complete`, data),
  scan: (barcode) => request.get('/dispensing/scan', { params: { barcode } })
}

export const financeApi = {
  list: (params) => request.get('/finance/list', { params }),
  create: (data) => request.post('/finance', data),
  update: (id, data) => request.put(`/finance/${id}`, data),
  freeze: (id) => request.post(`/finance/${id}/freeze`),
  archive: (id) => request.post(`/finance/${id}/archive`),
  getFlows: (id, params) => request.get(`/finance/${id}/flows`, { params })
}

export const incomeExpenseApi = {
  list: (params) => request.get('/income-expense/list', { params }),
  summary: (params) => request.get('/income-expense/summary', { params }),
  create: (data) => request.post('/income-expense', data),
  trace: (id) => request.get(`/income-expense/${id}/trace`)
}

export const reimbursementApi = {
  list: (params) => request.get('/reimbursement/list', { params }),
  create: (data) => request.post('/reimbursement', data),
  getDetail: (id) => request.get(`/reimbursement/${id}/detail`),
  approve: (id, data) => request.post(`/reimbursement/${id}/approve`, data),
  reject: (id, data) => request.post(`/reimbursement/${id}/reject`, data)
}

export const settlementApi = {
  list: (params) => request.get('/settlement/list', { params }),
  create: (data) => request.post('/settlement', data),
  getDetail: (id) => request.get(`/settlement/${id}/detail`),
  settle: (id, data) => request.post(`/settlement/${id}/settle`, data)
}

export const statisticsApi = {
  overview: () => request.get('/statistics/overview'),
  analysis: (params) => request.get('/statistics/analysis', { params }),
  reports: (params) => request.get('/statistics/reports', { params }),
  decision: () => request.get('/statistics/decision')
}

export const authApi = {
  login: (data) => request.post('/auth/login', data)
}

/** 公共字典 / 选项（与后端字典接口对齐） */
export const commonApi = {
  meta: () => request.get('/common/meta'),
  options: (key) => request.get(`/common/options/${key}`),
  enums: (key) => request.get(`/common/enums/${key}`)
}

export const adminUserApi = {
  list: (params) => request.get('/admin/user/list', { params }),
  create: (data) => request.post('/admin/user', data),
  update: (id, data) => request.put(`/admin/user/${id}`, data),
  remove: (id) => request.delete(`/admin/user/${id}`),
  resetPassword: (id) => request.post(`/admin/user/${id}/reset-password`)
}

export const adminRoleApi = {
  list: (params) => request.get('/admin/role/list', { params }),
  create: (data) => request.post('/admin/role', data),
  update: (id, data) => request.put(`/admin/role/${id}`, data),
  remove: (id) => request.delete(`/admin/role/${id}`)
}

export const adminDepartmentApi = {
  list: (params) => request.get('/admin/department/list', { params }),
  tree: () => request.get('/admin/department/tree'),
  create: (data) => request.post('/admin/department', data),
  update: (id, data) => request.put(`/admin/department/${id}`, data),
  remove: (id) => request.delete(`/admin/department/${id}`)
}

export const adminMenuApi = {
  tree: () => request.get('/admin/menu/tree'),
  create: (data) => request.post('/admin/menu', data),
  update: (id, data) => request.put(`/admin/menu/${id}`, data),
  remove: (id) => request.delete(`/admin/menu/${id}`)
}

export const adminDictApi = {
  list: (params) => request.get('/admin/dict/list', { params }),
  create: (data) => request.post('/admin/dict', data),
  update: (id, data) => request.put(`/admin/dict/${id}`, data),
  remove: (id) => request.delete(`/admin/dict/${id}`)
}

export const adminConfigApi = {
  list: () => request.get('/admin/config/list'),
  update: (data) => request.put('/admin/config', data)
}

export const adminLogApi = {
  operationList: (params) => request.get('/admin/log/operation', { params }),
  operationRecent: (params) => request.get('/admin/log/operation/recent', { params }),
  loginList: (params) => request.get('/admin/log/login', { params })
}

export const auditApi = {
  report: (data) => request.post('/audit/report', data)
}

export const aiApi = {
  status: () => request.get('/ai/status'),
  doctorAssist: (data) => request.post('/ai/doctor-assist', data, { timeout: 60000 })
}

export const consoleApi = {
  overview: () => request.get('/admin/console/overview')
}

export const registerApi = {
  list: (params) => request.get('/register/list', { params }),
  create: (data) => request.post('/register', data),
  cancel: (id) => request.post(`/register/${id}/cancel`)
}

export const appointmentApi = {
  list: (params) => request.get('/appointment/list', { params }),
  create: (data) => request.post('/appointment', data),
  confirm: (id) => request.post(`/appointment/${id}/confirm`),
  cancel: (id) => request.post(`/appointment/${id}/cancel`)
}

export const paymentApi = {
  list: (params) => request.get('/payment/list', { params }),
  create: (data) => request.post('/payment', data),
  refund: (id) => request.post(`/payment/${id}/refund`)
}

export const bedApi = {
  list: (params) => request.get('/bed/list', { params }),
  create: (data) => request.post('/bed', data),
  update: (id, data) => request.put(`/bed/${id}`, data),
  remove: (id) => request.delete(`/bed/${id}`)
}

export const noticeApi = {
  list: (params) => request.get('/notice/list', { params }),
  create: (data) => request.post('/notice', data),
  update: (id, data) => request.put(`/notice/${id}`, data),
  remove: (id) => request.delete(`/notice/${id}`)
}
