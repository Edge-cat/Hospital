import axios from 'axios'
import { ElMessage } from 'element-plus'
import { useUserStore } from '@/stores/user'
import { usePermissionStore } from '@/stores/permission'
import { useDictStore } from '@/stores/dict'
import router from '@/router'
import { resolveApiAudit } from '@/utils/auditMap'
import { reportOperation } from '@/utils/operationReport'

const request = axios.create({
  baseURL: import.meta.env.VITE_API_BASE || '/api',
  timeout: 15000
})

request.interceptors.request.use((config) => {
  const userStore = useUserStore()
  if (userStore.token) {
    config.headers.Authorization = `Bearer ${userStore.token}`
  }
  return config
})

request.interceptors.response.use(
  (response) => {
    const res = response.data
    if (res.code !== undefined && res.code !== 200) {
      ElMessage.error(res.message || '请求失败')
      return Promise.reject(new Error(res.message || '请求失败'))
    }
    const audit = resolveApiAudit(response.config)
    if (audit) reportOperation(audit)
    return res
  },
  (error) => {
    if (error.response?.status === 401) {
      const userStore = useUserStore()
      const permissionStore = usePermissionStore()
      const dictStore = useDictStore()
      permissionStore.resetRoutes()
      dictStore.reset()
      userStore.logout()
      router.push('/login')
    }
    ElMessage.error(error.message || '网络异常')
    return Promise.reject(error)
  }
)

export default request
