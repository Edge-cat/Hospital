import { mockRequest } from './mock'
import { request } from './request'

const USE_MOCK = import.meta.env.VITE_USE_MOCK === 'true'

export function apiRequest(options) {
  if (USE_MOCK) {
    return mockRequest(options)
  }
  return request(options)
}
