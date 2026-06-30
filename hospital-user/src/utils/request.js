import axios from 'axios'
import { ElMessage } from 'element-plus'
import { useUserStore } from '@/stores/user'
import router from '@/router'
import { resolveApiAudit } from '@/utils/auditMap'
import { reportOperation } from '@/utils/operationReport'

const request = axios.create({
  baseURL: import.meta.env.VITE_API_BASE || '/api',
  timeout: 15000,
  headers: {
    Accept: 'application/json;charset=UTF-8'
  },
  responseType: 'json'
})

request.interceptors.request.use((config) => {
  const userStore = useUserStore()
  if (userStore.token) {
    config.headers.Authorization = `Bearer ${userStore.token}`
  }
  config.headers['X-Audit-Client'] = 'user'
  return config
})

request.interceptors.response.use(
  (response) => {
    const res = response.data
    const silent = response.config?.silent
    if (res.code !== undefined && res.code !== 200) {
      if (!silent) ElMessage.error(res.message || '请求失败')
      return Promise.reject(new Error(res.message || '请求失败'))
    }
    const audit = resolveApiAudit(response.config)
    if (audit) reportOperation(audit)
    return res
  },
  (error) => {
    const silent = error.config?.silent
    const status = error.response?.status
    const body = error.response?.data

    if (status === 401) {
      const userStore = useUserStore()
      userStore.logout()
      router.push({ name: 'Login', query: { redirect: router.currentRoute.value.fullPath } })
      return Promise.reject({
        code: 401,
        message: body?.message || '登录已过期，请重新登录'
      })
    }

    if (!silent) {
      ElMessage.error(body?.message || error.message || '网络异常')
    }
    return Promise.reject(
      body?.code
        ? body
        : { code: status || 500, message: error.message || '网络异常' }
    )
  }
)

export default request
