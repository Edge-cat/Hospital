/**
 * 用户端业务路由（Layout 子路由）
 *
 * meta.auth  — 需要登录
 * meta.nav   — 显示在顶部导航
 * meta.roles — 预留：后续按角色过滤（如 patient / vip）
 */
export const layoutRoutes = [
  { path: 'home', name: 'Home', component: () => import('@/views/home/index.vue'), meta: { title: '首页', nav: true } },
  { path: 'register', name: 'Register', component: () => import('@/views/register/index.vue'), meta: { title: '在线挂号', nav: true, auth: true } },
  { path: 'appointment', name: 'Appointment', component: () => import('@/views/appointment/index.vue'), meta: { title: '在线预约', nav: true, auth: true } },
  { path: 'payment', name: 'Payment', component: () => import('@/views/payment/index.vue'), meta: { title: '在线缴费', nav: true, auth: true } },
  { path: 'department', name: 'Department', component: () => import('@/views/department/index.vue'), meta: { title: '科室介绍', nav: true } },
  { path: 'department/:id', name: 'DepartmentDetail', component: () => import('@/views/department/detail.vue'), meta: { title: '科室详情' } },
  { path: 'notice', name: 'Notice', component: () => import('@/views/notice/index.vue'), meta: { title: '医院公告', nav: true } },
  { path: 'notice/:id', name: 'NoticeDetail', component: () => import('@/views/notice/detail.vue'), meta: { title: '公告详情' } },
  { path: 'records', name: 'Records', component: () => import('@/views/records/index.vue'), meta: { title: '就诊记录', auth: true } },
  { path: 'profile', name: 'Profile', component: () => import('@/views/profile/index.vue'), meta: { title: '个人中心', auth: true } },
  { path: 'my-register', name: 'MyRegister', component: () => import('@/views/my-register/index.vue'), meta: { title: '我的挂号', auth: true } },
  { path: 'my-appointment', name: 'MyAppointment', component: () => import('@/views/my-appointment/index.vue'), meta: { title: '我的预约', auth: true } }
]

/** 预留：可按角色扩展的异步路由表（当前与 layoutRoutes 一致） */
export const asyncRoutes = layoutRoutes

/** 从路由表构建导航项（登录态 + 角色过滤占位） */
export function buildNavItems(routes, { isLoggedIn, role } = {}) {
  return routes
    .filter((r) => r.meta?.nav)
    .filter((r) => !r.meta?.auth || isLoggedIn)
    .filter((r) => {
      const roles = r.meta?.roles
      if (!roles?.length) return true
      return role && roles.includes(role)
    })
    .map((r) => ({ path: `/${r.path}`, title: r.meta.title, name: r.name }))
}

export const ROLE_LABELS = {
  patient: '患者'
}
