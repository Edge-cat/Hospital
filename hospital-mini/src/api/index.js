import { apiRequest } from '../utils/api'

export const mpApi = {
  login: (data) => apiRequest({ url: '/auth/login', method: 'POST', data }),
  wxLogin: (data) => apiRequest({ url: '/auth/wx-login', method: 'POST', data }),
  overview: () => apiRequest({ url: '/common/home-overview' }),
  departments: () => apiRequest({ url: '/common/departments' }),
  registerTypes: () => apiRequest({ url: '/common/register-types' }),
  notices: (params) => apiRequest({ url: '/notice/list', data: params }),
  noticeDetail: (id) => apiRequest({ url: `/notice/${id}` }),
  doctors: (params) => apiRequest({ url: '/doctor/list', data: params }),
  register: (data) => apiRequest({ url: '/register', method: 'POST', data }),
  registerList: (params) => apiRequest({ url: '/register/list', data: params }),
  cancelRegister: (id) => apiRequest({ url: `/register/${id}/cancel`, method: 'POST' }),
  appointment: (data) => apiRequest({ url: '/appointment', method: 'POST', data }),
  appointmentSchedule: (params) => apiRequest({ url: '/appointment/schedule', data: params }),
  appointmentList: (params) => apiRequest({ url: '/appointment/list', data: params }),
  cancelAppointment: (id) => apiRequest({ url: `/appointment/${id}/cancel`, method: 'POST' }),
  paymentList: (params) => apiRequest({ url: '/payment/list', data: params }),
  paymentDetail: (id) => apiRequest({ url: `/payment/${id}`, method: 'GET' }),
  payment: (data) => apiRequest({ url: '/payment', method: 'POST', data }),
  records: (params) => apiRequest({ url: '/record/list', data: params }),
  patientInfo: () => apiRequest({ url: '/patient/info' })
}
