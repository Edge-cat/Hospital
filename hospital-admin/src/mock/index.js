/**
 * Mock 数据层入口
 * 开发环境下拦截 /api 请求，返回标准后端响应结构
 */
import { registerMockRoutes } from './register.js'

registerMockRoutes()

export { store } from './data/store.js'
export { COMMON_META } from './data/static.js'
