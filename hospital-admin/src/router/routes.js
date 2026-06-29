/**
 * 异步路由表（登录后按角色动态注入）
 * meta.roles: 可访问角色 admin | doctor | nurse
 */
import { filterByRole } from '@/utils/auth'

export const asyncRoutes = [
  {
    path: 'dashboard',
    name: 'Dashboard',
    component: () => import('@/views/dashboard/index.vue'),
    meta: { title: '工作台', icon: 'Odometer', roles: ['admin', 'doctor', 'nurse'] }
  },
  // 患者管理
  {
    path: 'patient/info',
    name: 'PatientInfo',
    component: () => import('@/views/patient/info.vue'),
    meta: { title: '患者信息管理', icon: 'User', group: '患者管理', groupIcon: 'User', roles: ['admin', 'nurse'] }
  },
  {
    path: 'patient/add',
    name: 'PatientAdd',
    component: () => import('@/views/patient/add.vue'),
    meta: { title: '医生添加患者', icon: 'Plus', group: '患者管理', groupIcon: 'User', roles: ['admin', 'doctor'] }
  },
  {
    path: 'patient/search',
    name: 'PatientSearch',
    component: () => import('@/views/patient/search.vue'),
    meta: { title: '患者查询', icon: 'Search', group: '患者管理', groupIcon: 'User', roles: ['admin', 'doctor', 'nurse'] }
  },
  {
    path: 'patient/consultation',
    name: 'PatientConsultation',
    component: () => import('@/views/patient/consultation.vue'),
    meta: { title: '医生开始就诊', icon: 'FirstAidKit', group: '患者管理', groupIcon: 'User', roles: ['admin', 'doctor', 'nurse'] }
  },
  // 人事管理
  {
    path: 'hr/doctor',
    name: 'HrDoctor',
    component: () => import('@/views/hr/doctor.vue'),
    meta: { title: '医生信息管理', icon: 'Avatar', group: '人事管理', groupIcon: 'Avatar', roles: ['admin'] }
  },
  {
    path: 'hr/schedule',
    name: 'HrSchedule',
    component: () => import('@/views/hr/schedule.vue'),
    meta: { title: '排班管理', icon: 'Calendar', group: '人事管理', groupIcon: 'Avatar', roles: ['admin', 'doctor', 'nurse'] }
  },
  {
    path: 'hr/record',
    name: 'HrRecord',
    component: () => import('@/views/hr/record.vue'),
    meta: { title: '诊疗记录管理', icon: 'Document', group: '人事管理', groupIcon: 'Avatar', roles: ['admin', 'doctor'] }
  },
  {
    path: 'hr/service',
    name: 'HrService',
    component: () => import('@/views/hr/service.vue'),
    meta: { title: '医疗服务管理', icon: 'Service', group: '人事管理', groupIcon: 'Avatar', roles: ['admin', 'doctor'] }
  },
  // 药房管理
  {
    path: 'pharmacy/drug',
    name: 'PharmacyDrug',
    component: () => import('@/views/pharmacy/drug.vue'),
    meta: { title: '药品信息管理', icon: 'Goods', group: '药房管理', groupIcon: 'Goods', roles: ['admin'] }
  },
  {
    path: 'pharmacy/procurement',
    name: 'PharmacyProcurement',
    component: () => import('@/views/pharmacy/procurement.vue'),
    meta: { title: '采购管理', icon: 'ShoppingCart', group: '药房管理', groupIcon: 'Goods', roles: ['admin'] }
  },
  {
    path: 'pharmacy/inventory',
    name: 'PharmacyInventory',
    component: () => import('@/views/pharmacy/inventory.vue'),
    meta: { title: '库存管理', icon: 'Box', group: '药房管理', groupIcon: 'Goods', roles: ['admin'] }
  },
  {
    path: 'pharmacy/dispensing',
    name: 'PharmacyDispensing',
    component: () => import('@/views/pharmacy/dispensing.vue'),
    meta: { title: '配药管理', icon: 'Medicine', group: '药房管理', groupIcon: 'Goods', roles: ['admin'] }
  },
  // 财务管理
  {
    path: 'finance/info',
    name: 'FinanceInfo',
    component: () => import('@/views/finance/info.vue'),
    meta: { title: '财务信息管理', icon: 'Wallet', group: '财务管理', groupIcon: 'Wallet', roles: ['admin'] }
  },
  {
    path: 'finance/income-expense',
    name: 'FinanceIncomeExpense',
    component: () => import('@/views/finance/income-expense.vue'),
    meta: { title: '收支管理', icon: 'Money', group: '财务管理', groupIcon: 'Wallet', roles: ['admin'] }
  },
  {
    path: 'finance/reimbursement',
    name: 'FinanceReimbursement',
    component: () => import('@/views/finance/reimbursement.vue'),
    meta: { title: '报销管理', icon: 'Tickets', group: '财务管理', groupIcon: 'Wallet', roles: ['admin'] }
  },
  {
    path: 'finance/settlement',
    name: 'FinanceSettlement',
    component: () => import('@/views/finance/settlement.vue'),
    meta: { title: '结算管理', icon: 'CreditCard', group: '财务管理', groupIcon: 'Wallet', roles: ['admin'] }
  },
  // 统计分析
  {
    path: 'statistics/analysis',
    name: 'StatisticsAnalysis',
    component: () => import('@/views/statistics/analysis.vue'),
    meta: { title: '数据分析', icon: 'DataAnalysis', group: '统计分析', groupIcon: 'DataAnalysis', roles: ['admin'] }
  },
  {
    path: 'statistics/report',
    name: 'StatisticsReport',
    component: () => import('@/views/statistics/report.vue'),
    meta: { title: '报表生成', icon: 'DocumentCopy', group: '统计分析', groupIcon: 'DataAnalysis', roles: ['admin'] }
  },
  {
    path: 'statistics/decision',
    name: 'StatisticsDecision',
    component: () => import('@/views/statistics/decision.vue'),
    meta: { title: '决策支持', icon: 'TrendCharts', group: '统计分析', groupIcon: 'DataAnalysis', roles: ['admin'] }
  },
  // 就医流程管理
  {
    path: 'business/register',
    name: 'BusinessRegister',
    component: () => import('@/views/business/register.vue'),
    meta: { title: '挂号管理', icon: 'Tickets', group: '就医流程管理', groupIcon: 'Guide', roles: ['admin', 'nurse'] }
  },
  {
    path: 'business/appointment',
    name: 'BusinessAppointment',
    component: () => import('@/views/business/appointment.vue'),
    meta: { title: '预约管理', icon: 'Calendar', group: '就医流程管理', groupIcon: 'Guide', roles: ['admin', 'nurse'] }
  },
  {
    path: 'business/payment',
    name: 'BusinessPayment',
    component: () => import('@/views/business/payment.vue'),
    meta: { title: '缴费管理', icon: 'Money', group: '就医流程管理', groupIcon: 'Guide', roles: ['admin', 'nurse'] }
  },
  {
    path: 'business/bed',
    name: 'BusinessBed',
    component: () => import('@/views/business/bed.vue'),
    meta: { title: '床位管理', icon: 'House', group: '就医流程管理', groupIcon: 'Guide', roles: ['admin', 'nurse'] }
  },
  {
    path: 'business/notice',
    name: 'BusinessNotice',
    component: () => import('@/views/business/notice.vue'),
    meta: { title: '公告管理', icon: 'Bell', group: '就医流程管理', groupIcon: 'Guide', roles: ['admin', 'doctor', 'nurse'] }
  },
  // 系统管理
  {
    path: 'admin/user',
    name: 'AdminUser',
    component: () => import('@/views/admin/user.vue'),
    meta: { title: '用户管理', icon: 'UserFilled', group: '系统管理', groupIcon: 'Setting', roles: ['admin'] }
  },
  {
    path: 'admin/role',
    name: 'AdminRole',
    component: () => import('@/views/admin/role.vue'),
    meta: { title: '角色管理', icon: 'Key', group: '系统管理', groupIcon: 'Setting', roles: ['admin'] }
  },
  {
    path: 'admin/department',
    name: 'AdminDepartment',
    component: () => import('@/views/admin/department.vue'),
    meta: { title: '科室管理', icon: 'OfficeBuilding', group: '系统管理', groupIcon: 'Setting', roles: ['admin'] }
  },
  {
    path: 'admin/menu',
    name: 'AdminMenu',
    component: () => import('@/views/admin/menu.vue'),
    meta: { title: '菜单权限', icon: 'Menu', group: '系统管理', groupIcon: 'Setting', roles: ['admin'] }
  },
  {
    path: 'admin/dict',
    name: 'AdminDict',
    component: () => import('@/views/admin/dict.vue'),
    meta: { title: '数据字典', icon: 'Collection', group: '系统管理', groupIcon: 'Setting', roles: ['admin'] }
  },
  {
    path: 'admin/config',
    name: 'AdminConfig',
    component: () => import('@/views/admin/config.vue'),
    meta: { title: '系统配置', icon: 'Setting', group: '系统管理', groupIcon: 'Setting', roles: ['admin'] }
  },
  {
    path: 'admin/operation-log',
    name: 'AdminOperationLog',
    component: () => import('@/views/admin/operation-log.vue'),
    meta: { title: '操作日志', icon: 'Document', group: '系统管理', groupIcon: 'Setting', roles: ['admin'] }
  },
  {
    path: 'admin/login-log',
    name: 'AdminLoginLog',
    component: () => import('@/views/admin/login-log.vue'),
    meta: { title: '登录日志', icon: 'Monitor', group: '系统管理', groupIcon: 'Setting', roles: ['admin'] }
  }
]

/** 按角色过滤路由（深拷贝后过滤，避免污染原表） */
export function filterRoutesByRole(routes, role) {
  return filterByRole(
    routes.map((r) => ({ ...r, meta: { ...r.meta } })),
    role
  )
}

/** 从路由列表构建侧边栏菜单分组 */
export function buildMenuGroups(routes) {
  const groupMap = new Map()

  routes.forEach((route) => {
    const { group, groupIcon, title } = route.meta || {}
    if (!group) return
    if (!groupMap.has(group)) {
      groupMap.set(group, { name: group, icon: groupIcon || 'Menu', children: [] })
    }
    groupMap.get(group).children.push({
      path: `/${route.path}`,
      title,
      name: route.name
    })
  })

  return Array.from(groupMap.values())
}

export const ROLE_LABELS = {
  admin: '管理员',
  doctor: '医生',
  nurse: '护士'
}
