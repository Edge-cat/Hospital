import { resolveApiAudit } from './auditMap'
import { reportOperation } from './operationReport'

const BASE_URL = import.meta.env.VITE_API_BASE || '/api'

export function request(options) {
  return new Promise((resolve, reject) => {
    const token = uni.getStorageSync('his_token') || ''
    uni.request({
      url: BASE_URL + options.url,
      method: options.method || 'GET',
      data: options.data || {},
      header: {
        'Content-Type': 'application/json',
        Authorization: token ? `Bearer ${token}` : ''
      },
      success(res) {
        if (res.statusCode === 200) {
          const data = res.data
          if (data.code !== undefined && data.code !== 200) {
            uni.showToast({ title: data.message || '请求失败', icon: 'none' })
            reject(data)
          } else {
            const audit = resolveApiAudit(options)
            if (audit) reportOperation(audit)
            resolve(data)
          }
        } else {
          uni.showToast({ title: '网络异常', icon: 'none' })
          reject(res)
        }
      },
      fail(err) {
        uni.showToast({ title: '网络连接失败', icon: 'none' })
        reject(err)
      }
    })
  })
}

export function checkLogin() {
  return !!uni.getStorageSync('his_token')
}

export function navigateToLogin() {
  uni.navigateTo({ url: '/pages/login/login' })
}

export const registerStatusMap = {
  0: { label: '待就诊', class: 'tag-warning' },
  1: { label: '就诊中', class: 'tag-primary' },
  2: { label: '已完成', class: 'tag-success' },
  3: { label: '已退号', class: 'tag-info' }
}

export const appointmentStatusMap = {
  0: { label: '待确认', class: 'tag-warning' },
  1: { label: '已确认', class: 'tag-primary' },
  2: { label: '已完成', class: 'tag-success' },
  3: { label: '已取消', class: 'tag-info' }
}

export const paymentStatusMap = {
  0: { label: '待支付', class: 'tag-warning' },
  1: { label: '已支付', class: 'tag-success' },
  2: { label: '已退款', class: 'tag-info' }
}
