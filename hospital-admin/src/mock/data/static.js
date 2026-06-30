/**
 * 静态字典 / 枚举 / 选项（原 views 与 constants 中的硬编码数据）
 * 通过 GET /api/common/meta 提供给前端
 */

const opt = (arr) => arr.map((item) => (typeof item === 'string' ? { label: item, value: item } : item))

export const LOGIN_ACCOUNTS = {
  admin: { id: 1, name: '系统管理员', role: 'admin', roleLabel: '管理员' },
  doctor: { id: 2, name: '张医生', role: 'doctor', roleLabel: '医生', department: '内科' },
  nurse: { id: 3, name: '李护士', role: 'nurse', roleLabel: '护士', department: '内科' }
}

export const COMMON_META = {
  options: {
    departments: opt(['内科', '外科', '儿科', '骨科', '眼科', '皮肤科', '口腔科']),
    roles: opt(['系统管理员', '医生', '护士', '财务', '药房']),
    staffRoles: [
      { label: '系统管理员', value: 'admin' },
      { label: '医生', value: 'doctor' },
      { label: '护士', value: 'nurse' },
      { label: '财务', value: 'finance' },
      { label: '药房', value: 'pharmacy' }
    ],
    registerTypes: opt(['普通号', '专家号', '急诊号']),
    payMethods: opt(['现金', '微信', '支付宝', '银行卡']),
    paymentItems: opt(['挂号费', '检查费', '药品费', '住院押金']),
    bedTypes: opt(['普通床位', 'VIP床位', '监护床位']),
    timeSlots: opt(['08:00-09:00', '09:00-10:00', '10:00-11:00', '14:00-15:00', '15:00-16:00', '16:00-17:00']),
    shiftTypes: opt(['早班', '中班', '晚班']),
    noticeTypes: opt(['通知', '公告', '紧急']),
    doctorTitles: opt(['主任医师', '副主任医师', '主治医师', '住院医师']),
    logModules: opt(['患者管理', '系统管理', '财务管理', '药房管理']),
    gender: [
      { label: '男', value: 1 },
      { label: '女', value: 2 }
    ],
    enableStatus: [
      { label: '启用', value: 1 },
      { label: '禁用', value: 0 }
    ],
    loginStatus: [
      { label: '成功', value: 1 },
      { label: '失败', value: 0 }
    ]
  },
  enums: {
    registerStatus: {
      0: { label: '待就诊', type: 'warning' },
      1: { label: '就诊中', type: 'primary' },
      2: { label: '已完成', type: 'success' },
      3: { label: '已退号', type: 'info' }
    },
    appointmentStatus: {
      0: { label: '待确认', type: 'warning' },
      1: { label: '已确认', type: 'primary' },
      2: { label: '已完成', type: 'success' },
      3: { label: '已取消', type: 'info' }
    },
    paymentStatus: {
      0: { label: '待支付', type: 'warning' },
      1: { label: '已支付', type: 'success' },
      2: { label: '已退款', type: 'info' }
    },
    bedStatus: {
      0: { label: '空闲', type: 'success' },
      1: { label: '占用', type: 'warning' },
      2: { label: '维护', type: 'info' }
    },
    reimburseStatus: {
      0: { label: '待审批', type: 'warning' },
      1: { label: '已通过', type: 'success' },
      2: { label: '已驳回', type: 'danger' }
    },
    consultationStatus: {
      0: { label: '待就诊', type: 'info' },
      1: { label: '就诊中', type: 'warning' },
      2: { label: '已完成', type: 'success' }
    },
    orderStatus: {
      0: { label: '待处理', type: 'info' },
      1: { label: '处理中', type: 'warning' },
      2: { label: '已完成', type: 'success' },
      3: { label: '已取消', type: 'danger' }
    },
    noticeTypeTag: {
      通知: { label: '通知', type: 'info' },
      公告: { label: '公告', type: 'success' },
      紧急: { label: '紧急', type: 'danger' }
    },
    reportStatus: {
      0: { label: '生成中', type: 'warning' },
      1: { label: '已生成', type: 'success' }
    }
  }
}

export const ADMIN_ROLES = [
  { id: 1, roleName: '系统管理员', roleCode: 'admin', description: '拥有全部权限', status: 1, createTime: '2026-01-01 00:00:00' },
  { id: 2, roleName: '医生', roleCode: 'doctor', description: '患者、诊疗相关权限', status: 1, createTime: '2026-01-01 00:00:00' },
  { id: 3, roleName: '护士', roleCode: 'nurse', description: '挂号、预约相关权限', status: 1, createTime: '2026-01-01 00:00:00' },
  { id: 4, roleName: '财务', roleCode: 'finance', description: '财务模块权限', status: 1, createTime: '2026-01-01 00:00:00' },
  { id: 5, roleName: '药房', roleCode: 'pharmacy', description: '药房模块权限', status: 1, createTime: '2026-01-01 00:00:00' }
]

export const ADMIN_DEPARTMENTS = [
  { id: 1, name: '内科', code: 'NK', parentId: 0, leader: '张主任', phone: '010-12345601', status: 1, sort: 1 },
  { id: 2, name: '外科', code: 'WK', parentId: 0, leader: '李主任', phone: '010-12345602', status: 1, sort: 2 },
  { id: 3, name: '儿科', code: 'EK', parentId: 0, leader: '王主任', phone: '010-12345603', status: 1, sort: 3 },
  { id: 4, name: '信息科', code: 'XXK', parentId: 0, leader: '赵主任', phone: '010-12345604', status: 1, sort: 4 },
  { id: 5, name: '心血管内科', code: 'XGNK', parentId: 1, leader: '刘医生', phone: '010-12345605', status: 1, sort: 1 }
]

export const ADMIN_CONFIGS = [
  { id: 1, configKey: 'hospital_name', configValue: '东软云医院', configName: '医院名称', remark: '系统显示名称' },
  { id: 2, configKey: 'register_fee', configValue: '10', configName: '普通挂号费(元)', remark: '默认挂号费用' },
  { id: 3, configKey: 'expert_fee', configValue: '50', configName: '专家挂号费(元)', remark: '专家号费用' },
  { id: 4, configKey: 'appointment_days', configValue: '7', configName: '预约天数', remark: '可提前预约天数' },
  { id: 5, configKey: 'session_timeout', configValue: '30', configName: '会话超时(分钟)', remark: '登录超时时间' }
]

export const MENU_TREE = [
  { id: 1, name: '工作台', path: '/dashboard', icon: 'Odometer', sort: 1, status: 1, children: [] },
  {
    id: 2, name: '患者管理', path: '/patient', icon: 'User', sort: 2, status: 1,
    children: [
      { id: 21, name: '患者信息管理', path: '/patient/info', sort: 1, status: 1 },
      { id: 22, name: '医生添加患者', path: '/patient/add', sort: 2, status: 1 },
      { id: 23, name: '患者查询', path: '/patient/search', sort: 3, status: 1 },
      { id: 24, name: '医生开始就诊', path: '/patient/consultation', sort: 4, status: 1 }
    ]
  },
  {
    id: 10, name: '系统管理', path: '/admin', icon: 'Setting', sort: 10, status: 1,
    children: [
      { id: 101, name: '用户管理', path: '/admin/user', sort: 1, status: 1 },
      { id: 102, name: '角色管理', path: '/admin/role', sort: 2, status: 1 },
      { id: 103, name: '科室管理', path: '/admin/department', sort: 3, status: 1 },
      { id: 104, name: '菜单权限', path: '/admin/menu', sort: 4, status: 1 },
      { id: 105, name: '数据字典', path: '/admin/dict', sort: 5, status: 1 },
      { id: 106, name: '系统配置', path: '/admin/config', sort: 6, status: 1 },
      { id: 107, name: '操作日志', path: '/admin/operation-log', sort: 7, status: 1 },
      { id: 108, name: '登录日志', path: '/admin/login-log', sort: 8, status: 1 }
    ]
  }
]

export const STATISTICS_OVERVIEW = {
  patientCount: 1286,
  doctorCount: 86,
  todayVisit: 342,
  todayIncome: 98560.5,
  drugStock: 15680,
  pendingSettlement: 23,
  trends: {
    patientCount: { mom: 5.2, series: [1180, 1205, 1220, 1245, 1260, 1275, 1286] },
    todayVisit: { mom: 12.3, series: [280, 295, 310, 318, 325, 335, 342] },
    todayIncome: { mom: 8.6, series: [82000, 85000, 88000, 91000, 93000, 96000, 98560] },
    pendingSettlement: { mom: -15.0, series: [35, 32, 30, 28, 26, 25, 23] }
  },
  visitTrend7d: [
    { day: '周一', count: 298 },
    { day: '周二', count: 312 },
    { day: '周三', count: 305 },
    { day: '周四', count: 328 },
    { day: '周五', count: 342 },
    { day: '周六', count: 256 },
    { day: '周日', count: 198 }
  ]
}

export const STATISTICS_ANALYSIS = {
  visitTrend: [
    { month: '1月', count: 820 },
    { month: '2月', count: 932 },
    { month: '3月', count: 901 },
    { month: '4月', count: 934 },
    { month: '5月', count: 1290 },
    { month: '6月', count: 1330 }
  ],
  departmentRank: [
    { name: '内科', value: 320 },
    { name: '外科', value: 280 },
    { name: '儿科', value: 210 },
    { name: '骨科', value: 180 },
    { name: '眼科', value: 150 }
  ],
  incomeByCategory: [
    { name: '挂号费', value: 45000 },
    { name: '药品费', value: 68000 },
    { name: '检查费', value: 32000 },
    { name: '住院费', value: 89000 }
  ]
}

export const STATISTICS_REPORTS = [
  { id: 1, name: '月度就诊量报表', type: '就诊统计', period: '2026-06', status: 1, createTime: '2026-06-01 08:00:00' },
  { id: 2, name: '科室收入报表', type: '财务统计', period: '2026-06', status: 1, createTime: '2026-06-01 08:00:00' },
  { id: 3, name: '药品库存报表', type: '药房统计', period: '2026-06', status: 0, createTime: '2026-06-15 10:00:00' }
]

export const STATISTICS_DECISION = {
  suggestions: [
    { title: '儿科就诊量持续上升', content: '建议增加儿科排班人数，优化候诊流程。', level: 'warning' },
    { title: '药品库存预警', content: '阿莫西林、布洛芬库存低于安全线，建议尽快采购。', level: 'danger' },
    { title: '门诊收入稳步增长', content: '本月门诊收入同比增长12%，可考虑扩展专家门诊时段。', level: 'success' }
  ],
  kpis: [
    { label: '平均候诊时间', value: '18分钟', trend: '-5%' },
    { label: '床位使用率', value: '82%', trend: '+3%' },
    { label: '患者满意度', value: '94.2%', trend: '+1.2%' },
    { label: '药占比', value: '28.5%', trend: '-0.8%' }
  ]
}
