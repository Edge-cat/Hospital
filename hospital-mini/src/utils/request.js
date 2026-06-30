import { resolveApiAudit } from './auditMap'
import { reportOperation } from './operationReport'

export { navigateToLogin } from './nav'

const BASE_URL = import.meta.env.VITE_API_BASE || '/api'

export function request(options) {
  const silent = options.silent === true
  return new Promise((resolve, reject) => {
    const token = uni.getStorageSync('his_token') || ''
    uni.request({
      url: BASE_URL + options.url,
      method: options.method || 'GET',
      data: options.data || {},
      timeout: options.timeout ?? 15000,
      header: {
        'Content-Type': 'application/json',
        Authorization: token ? `Bearer ${token}` : '',
        'X-Audit-Client': 'mini'
      },
      success(res) {
        if (res.statusCode === 200) {
          const data = res.data
          if (data.code !== undefined && data.code !== 200) {
            const msg = data.message || '请求失败'
            if (!silent) uni.showToast({ title: msg, icon: 'none' })
            reject({ message: msg, ...data })
          } else {
            const audit = resolveApiAudit(options)
            if (audit) reportOperation(audit)
            resolve(data)
          }
        } else {
          const body = typeof res.data === 'object' && res.data ? res.data : {}
          const msg = body.message || (res.statusCode === 401 ? '请先登录' : `请求失败(${res.statusCode})`)
          if (!silent) uni.showToast({ title: msg, icon: 'none' })
          reject({ message: msg, statusCode: res.statusCode, data: body })
        }
      },
      fail(err) {
        const errMsg = err?.errMsg || ''
        const msg = errMsg.includes('timeout')
          ? '请求超时，请确认后端 :8080 已启动'
          : '网络连接失败，请检查后端是否启动'
        if (!silent) uni.showToast({ title: msg, icon: 'none' })
        reject({ message: msg })
      }
    })
  })
}

export function checkLogin() {
  return !!uni.getStorageSync('his_token')
}

// navigateToLogin 由 ./nav.js 导出

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
