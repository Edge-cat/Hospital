import { layoutRoutes } from './routes'

/**
 * 静态路由
 */
export const constantRoutes = [
  {
    path: '/login',
    name: 'Login',
    component: () => import('@/views/login/index.vue'),
    meta: { title: '登录', public: true }
  },
  {
    path: '/',
    name: 'Layout',
    component: () => import('@/layout/UserLayout.vue'),
    redirect: '/home',
    children: layoutRoutes
  }
]

export const ROUTE_WHITE_LIST = ['/login']
