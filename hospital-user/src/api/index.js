import request from '@/utils/request'

export const userApi = {
  login: (data) => request.post('/auth/login', data),
  overview: () => request.get('/common/home-overview'),
  departments: () => request.get('/common/departments'),
  registerTypes: () => request.get('/common/register-types'),
  doctors: (params) => request.get('/doctor/list', { params }),
  notices: (params) => request.get('/notice/list', { params }),
  noticeDetail: (id) => request.get(`/notice/${id}`),
  register: (data) => request.post('/register', data),
  registerList: (params) => request.get('/register/list', { params }),
  cancelRegister: (id) => request.post(`/register/${id}/cancel`),
  appointment: (data) => request.post('/appointment', data),
  appointmentSchedule: (params) => request.get('/appointment/schedule', { params }),
  appointmentList: (params) => request.get('/appointment/list', { params }),
  cancelAppointment: (id) => request.post(`/appointment/${id}/cancel`),
  paymentList: (params) => request.get('/payment/list', { params }),
  payment: (data) => request.post('/payment', data),
  records: (params) => request.get('/record/list', { params }),
  patientInfo: () => request.get('/patient/info')
}
