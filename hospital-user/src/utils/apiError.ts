import { ElMessage } from 'element-plus'
import type { ApiResponse } from '@/types/hospital'
import { useUserStore } from '@/stores/user'
import router from '@/router'

/** 统一 JSON 错误格式占位 — 与后端约定一致 */
export interface ApiErrorBody {
  code: number
  message: string
  data?: unknown
}

export const API_ERROR = {
  TOKEN_EXPIRED: 401,
  FORBIDDEN: 403,
  VALIDATION: 422,
  CONFLICT: 409,
  SERVER: 500
} as const

export function isApiErrorBody(err: unknown): err is ApiErrorBody {
  return (
    typeof err === 'object' &&
    err !== null &&
    'code' in err &&
    'message' in err
  )
}

/**
 * 全局错误处理占位 — 视图层 catch 中调用
 * @example catch (e) { handleApiError(e, { fallback: '挂号失败' }) }
 */
export function handleApiError(
  error: unknown,
  options: { fallback?: string; silent?: boolean } = {}
): ApiErrorBody {
  const body: ApiErrorBody = isApiErrorBody(error)
    ? error
    : {
        code: API_ERROR.SERVER,
        message: error instanceof Error ? error.message : options.fallback || '请求失败'
      }

  if (body.code === API_ERROR.TOKEN_EXPIRED) {
    const userStore = useUserStore()
    userStore.logout()
    router.push({ name: 'Login', query: { redirect: router.currentRoute.value.fullPath } })
    if (!options.silent) ElMessage.warning(body.message || '登录已过期，请重新登录')
    return body
  }

  if (!options.silent) {
    ElMessage.error(body.message || options.fallback || '操作失败')
  }
  return body
}

/** 从 axios / 业务层 reject 中解析标准响应 */
export function parseApiResponse<T>(res: ApiResponse<T>): T {
  if (res.code !== 200) {
    throw { code: res.code, message: res.message, data: res.data } satisfies ApiErrorBody
  }
  return res.data
}
