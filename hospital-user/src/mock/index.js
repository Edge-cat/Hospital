import Mock from 'mockjs'

const Random = Mock.Random

const SCHEDULE_SLOTS = ['08:00-09:00', '09:00-10:00', '10:00-11:00', '14:00-15:00', '15:00-16:00', '16:00-17:00']
const WEEKDAY_NAMES = ['周日', '周一', '周二', '周三', '周四', '周五', '周六']

function buildAppointmentSchedule(doctorId, days = 8) {
  const dates = []
  for (let i = 0; i < days; i++) {
    const d = new Date()
    d.setDate(d.getDate() + i)
    const dateStr = d.toISOString().slice(0, 10)
    const weekday = WEEKDAY_NAMES[d.getDay()]
    const slots = SCHEDULE_SLOTS.map((timeSlot, idx) => {
      const remaining = ((doctorId * 3 + idx + i) % 4) + 1
      const full = (doctorId + idx + i) % 7 === 0
      return { timeSlot, remaining: full ? 0 : remaining, available: !full }
    })
    dates.push({
      date: dateStr,
      weekday,
      shortDate: dateStr.slice(5),
      label: i === 0 ? '今日' : i === 1 ? '明天' : weekday,
      isToday: i === 0,
      slots
    })
  }
  return dates
}

function pageResult(list, params = {}) {
  const page = Number(params.page) || 1
  const pageSize = Number(params.pageSize) || 10
  let filtered = [...list]
  const keyword = params.keyword || params.name || params.patientName
  if (keyword) {
    filtered = filtered.filter((item) =>
      Object.values(item).some((v) => String(v).includes(keyword))
    )
  }
  if (params.status !== undefined && params.status !== '') {
    filtered = filtered.filter((item) => String(item.status) === String(params.status))
  }
  if (params.startDate) {
    filtered = filtered.filter((item) => {
      const t = (item.payTime || item.createTime || '').slice(0, 10)
      return t && t >= params.startDate
    })
  }
  if (params.endDate) {
    filtered = filtered.filter((item) => {
      const t = (item.payTime || item.createTime || item.publishTime || '').slice(0, 10)
      return t && t <= params.endDate
    })
  }
  if (params.type) {
    filtered = filtered.filter((item) => item.type === params.type)
  }
  if (params.department) {
    filtered = filtered.filter((item) => item.department === params.department)
  }
  const start = (page - 1) * pageSize
  return {
    code: 200,
    data: {
      list: filtered.slice(start, start + pageSize),
      total: filtered.length
    }
  }
}

const patients = Mock.mock({
  'list|30': [{
    id: '@increment',
    patientNo: /P\d{8}/,
    name: '@cname',
    gender: '@pick([1,2])',
    age: '@integer(1,90)',
    phone: /^1[3-9]\d{9}$/,
    idCard: /^\d{17}[\dX]$/,
    address: '@county(true)',
    department: '@pick(["内科","外科","儿科","骨科","眼科"])',
    status: '@pick([0,1,2])',
    createTime: '@datetime'
  }]
}).list

function buildWeekSchedule(patterns) {
  const today = new Date()
  const day = today.getDay()
  const mondayOffset = day === 0 ? -6 : 1 - day
  const labels = ['周一', '周二', '周三', '周四', '周五', '周六', '周日']
  return patterns.map((p, i) => {
    const d = new Date(today)
    d.setDate(today.getDate() + mondayOffset + i)
    return {
      date: d.toISOString().slice(0, 10),
      weekday: labels[i],
      morning: !!p.morning,
      afternoon: !!p.afternoon
    }
  })
}

const doctors = [
  {
    id: 101, doctorNo: 'D100101', name: '张华', gender: 1, department: '内科', title: '主任医师',
    specialty: '擅长高血压、糖尿病、冠心病等慢性病的诊断与综合管理',
    morningSlots: 3, afternoonSlots: 5, status: 1,
    weekSchedule: buildWeekSchedule([
      { morning: true, afternoon: true }, { morning: true, afternoon: false },
      { morning: false, afternoon: true }, { morning: true, afternoon: true },
      { morning: true, afternoon: false }, { morning: false, afternoon: false },
      { morning: false, afternoon: false }
    ])
  },
  {
    id: 102, doctorNo: 'D100102', name: '李明', gender: 1, department: '内科', title: '副主任医师',
    specialty: '擅长心力衰竭、心律失常及心血管介入术后随访',
    morningSlots: 0, afternoonSlots: 2, status: 1,
    weekSchedule: buildWeekSchedule([
      { morning: false, afternoon: true }, { morning: false, afternoon: true },
      { morning: true, afternoon: false }, { morning: false, afternoon: true },
      { morning: false, afternoon: false }, { morning: false, afternoon: false },
      { morning: false, afternoon: false }
    ])
  },
  {
    id: 103, doctorNo: 'D100103', name: '王芳', gender: 2, department: '内科', title: '主治医师',
    specialty: '擅长慢性阻塞性肺病、支气管哮喘及呼吸道感染诊治',
    morningSlots: 8, afternoonSlots: 0, status: 1,
    weekSchedule: buildWeekSchedule([
      { morning: true, afternoon: false }, { morning: true, afternoon: false },
      { morning: true, afternoon: false }, { morning: false, afternoon: false },
      { morning: true, afternoon: false }, { morning: false, afternoon: false },
      { morning: false, afternoon: false }
    ])
  },
  {
    id: 104, doctorNo: 'D100104', name: '赵丽', gender: 2, department: '内科', title: '主任医师',
    specialty: '擅长胃炎、消化性溃疡、炎症性肠病及肝功能异常',
    morningSlots: 0, afternoonSlots: 0, status: 1,
    weekSchedule: buildWeekSchedule([
      { morning: true, afternoon: true }, { morning: true, afternoon: true },
      { morning: false, afternoon: false }, { morning: true, afternoon: true },
      { morning: true, afternoon: true }, { morning: false, afternoon: false },
      { morning: false, afternoon: false }
    ])
  },
  {
    id: 105, doctorNo: 'D100105', name: '陈伟', gender: 1, department: '内科', title: '住院医师',
    specialty: '擅长常见内科疾病初诊、体检报告解读及健康咨询',
    morningSlots: 12, afternoonSlots: 10, status: 1,
    weekSchedule: buildWeekSchedule([
      { morning: true, afternoon: true }, { morning: true, afternoon: true },
      { morning: true, afternoon: true }, { morning: true, afternoon: true },
      { morning: true, afternoon: true }, { morning: true, afternoon: false },
      { morning: false, afternoon: false }
    ])
  },
  {
    id: 201, doctorNo: 'D100201', name: '刘强', gender: 1, department: '外科', title: '主任医师',
    specialty: '擅长肝胆外科、腹腔镜微创手术及甲状腺肿瘤治疗',
    morningSlots: 2, afternoonSlots: 4, status: 1,
    weekSchedule: buildWeekSchedule([
      { morning: true, afternoon: true }, { morning: true, afternoon: false },
      { morning: false, afternoon: true }, { morning: true, afternoon: true },
      { morning: false, afternoon: false }, { morning: false, afternoon: false },
      { morning: false, afternoon: false }
    ])
  },
  {
    id: 202, doctorNo: 'D100202', name: '周敏', gender: 2, department: '外科', title: '副主任医师',
    specialty: '擅长胃肠道肿瘤、疝气修补及肛肠疾病诊治',
    morningSlots: 5, afternoonSlots: 0, status: 1,
    weekSchedule: buildWeekSchedule([
      { morning: true, afternoon: false }, { morning: true, afternoon: false },
      { morning: true, afternoon: false }, { morning: false, afternoon: false },
      { morning: true, afternoon: false }, { morning: false, afternoon: false },
      { morning: false, afternoon: false }
    ])
  },
  {
    id: 203, doctorNo: 'D100203', name: '孙磊', gender: 1, department: '外科', title: '主治医师',
    specialty: '擅长创伤急救、软组织感染及体表肿物切除',
    morningSlots: 0, afternoonSlots: 0, status: 1,
    weekSchedule: buildWeekSchedule([
      { morning: true, afternoon: true }, { morning: false, afternoon: true },
      { morning: true, afternoon: false }, { morning: false, afternoon: false },
      { morning: true, afternoon: true }, { morning: false, afternoon: false },
      { morning: false, afternoon: false }
    ])
  },
  {
    id: 301, doctorNo: 'D100301', name: '杨静', gender: 2, department: '儿科', title: '主任医师',
    specialty: '擅长小儿呼吸系统疾病、过敏性疾病及生长发育评估',
    morningSlots: 1, afternoonSlots: 3, status: 1,
    weekSchedule: buildWeekSchedule([
      { morning: true, afternoon: true }, { morning: true, afternoon: true },
      { morning: false, afternoon: true }, { morning: true, afternoon: false },
      { morning: true, afternoon: true }, { morning: true, afternoon: false },
      { morning: false, afternoon: false }
    ])
  },
  {
    id: 302, doctorNo: 'D100302', name: '吴涛', gender: 1, department: '儿科', title: '副主任医师',
    specialty: '擅长小儿腹泻、发热待查及儿童营养指导',
    morningSlots: 6, afternoonSlots: 4, status: 1,
    weekSchedule: buildWeekSchedule([
      { morning: true, afternoon: true }, { morning: true, afternoon: true },
      { morning: true, afternoon: false }, { morning: true, afternoon: true },
      { morning: false, afternoon: false }, { morning: true, afternoon: false },
      { morning: false, afternoon: false }
    ])
  },
  {
    id: 303, doctorNo: 'D100303', name: '郑琳', gender: 2, department: '儿科', title: '主治医师',
    specialty: '擅长新生儿黄疸、湿疹及儿童疫苗接种咨询',
    morningSlots: 0, afternoonSlots: 7, status: 1,
    weekSchedule: buildWeekSchedule([
      { morning: false, afternoon: true }, { morning: false, afternoon: true },
      { morning: false, afternoon: true }, { morning: false, afternoon: true },
      { morning: false, afternoon: true }, { morning: false, afternoon: false },
      { morning: false, afternoon: false }
    ])
  }
]

const schedules = Mock.mock({
  'list|25': [{
    id: '@increment',
    doctorName: '@cname',
    department: '@pick(["内科","外科","儿科"])',
    shiftDate: '@date',
    shiftType: '@pick(["早班","中班","晚班"])',
    status: '@pick([0,1])',
    remark: '@csentence(5,10)'
  }]
}).list

const records = Mock.mock({
  'list|25': [{
    id: '@increment',
    patientName: '@cname',
    doctorName: '@cname',
    department: '@pick(["内科","外科","儿科"])',
    diagnosis: '@csentence(5,15)',
    treatment: '@csentence(5,15)',
    visitTime: '@datetime',
    status: '@pick([0,1,2])'
  }]
}).list

const services = Mock.mock({
  'list|15': [{
    id: '@increment',
    serviceName: '@pick(["门诊挂号","专家会诊","体检套餐","疫苗接种","康复理疗"])',
    department: '@pick(["内科","外科","儿科"])',
    price: '@float(50,500,2,2)',
    duration: '@integer(15,120)',
    status: '@pick([0,1])',
    description: '@csentence(8,20)'
  }]
}).list

const drugs = Mock.mock({
  'list|30': [{
    id: '@increment',
    drugCode: /DR\d{6}/,
    drugName: '@pick(["阿莫西林","布洛芬","头孢克肟","维生素C","氯化钠"])',
    specification: '@pick(["0.25g*24粒","100ml","500mg*10片"])',
    manufacturer: '@pick(["华北制药","扬子江药业","石药集团"])',
    unit: '@pick(["盒","瓶","支"])',
    price: '@float(5,200,2,2)',
    status: '@pick([0,1])'
  }]
}).list

const procurements = Mock.mock({
  'list|20': [{
    id: '@increment',
    orderNo: /PO\d{10}/,
    drugName: '@pick(["阿莫西林","布洛芬","头孢克肟"])',
    quantity: '@integer(100,1000)',
    unitPrice: '@float(5,50,2,2)',
    supplier: '@pick(["国药控股","上药集团","华润医药"])',
    status: '@pick([0,1,2])',
    orderDate: '@date'
  }]
}).list

const inventories = drugs.map((d, i) => ({
  id: i + 1,
  drugId: d.id,
  drugName: d.drugName,
  drugCode: d.drugCode,
  stock: Random.integer(0, 500),
  minStock: 50,
  warehouse: Random.pick(['中心药房', '门诊药房', '住院药房']),
  updateTime: Random.datetime()
}))

const dispensings = Mock.mock({
  'list|20': [{
    id: '@increment',
    prescriptionNo: /RX\d{10}/,
    patientName: '@cname',
    doctorName: '@cname',
    drugName: '@pick(["阿莫西林","布洛芬","头孢克肟"])',
    quantity: '@integer(1,10)',
    status: '@pick([0,1,2])',
    createTime: '@datetime'
  }]
}).list

const finances = Mock.mock({
  'list|15': [{
    id: '@increment',
    accountName: '@pick(["门诊收入账户","住院收入账户","药品收入账户"])',
    accountNo: /AC\d{8}/,
    balance: '@float(10000,500000,2,2)',
    bank: '@pick(["工商银行","建设银行","农业银行"])',
    status: '@pick([0,1])',
    updateTime: '@datetime'
  }]
}).list

const incomeExpenses = Mock.mock({
  'list|30': [{
    id: '@increment',
    type: '@pick(["收入","支出"])',
    category: '@pick(["挂号费","药品费","检查费","采购支出","人员工资"])',
    amount: '@float(100,10000,2,2)',
    operator: '@cname',
    recordDate: '@date',
    remark: '@csentence(5,10)'
  }]
}).list

const reimbursements = Mock.mock({
  'list|20': [{
    id: '@increment',
    applicant: '@cname',
    department: '@pick(["内科","外科","行政部"])',
    amount: '@float(200,5000,2,2)',
    reason: '@csentence(5,15)',
    status: '@pick([0,1,2])',
    applyDate: '@date'
  }]
}).list

const settlements = Mock.mock({
  'list|15': [{
    id: '@increment',
    settlementNo: /ST\d{10}/,
    patientName: '@cname',
    totalAmount: '@float(100,5000,2,2)',
    paidAmount: '@float(0,5000,2,2)',
    status: '@pick([0,1,2])',
    settlementDate: '@date'
  }]
}).list

const PATIENT_PROFILE = {
  name: '张三',
  phone: '13800138000',
  idCard: '110101199001011234',
  cardNo: 'P2026001286',
  gender: 1,
  allergyHistory: '无',
  chronicDisease: '无',
  address: '北京市朝阳区'
}

Mock.mock(/\/api\/auth\/login/, 'post', (options) => {
  const body = JSON.parse(options.body || '{}')
  return {
    code: 200,
    data: {
      token: 'user-token-' + Random.string(16),
      user: {
        id: 2,
        name: body.username === 'patient' ? PATIENT_PROFILE.name : body.username,
        role: 'patient',
        phone: PATIENT_PROFILE.phone,
        cardNo: PATIENT_PROFILE.cardNo
      }
    }
  }
})

Mock.mock(/\/api\/patient\/info/, 'get', () => ({
  code: 200,
  data: { ...PATIENT_PROFILE }
}))

Mock.mock(/\/api\/appointment\/schedule/, 'get', (options) => {
  const params = parseQuery(options.url)
  const doctorId = Number(params.doctorId) || 1
  return { code: 200, data: { dates: buildAppointmentSchedule(doctorId) } }
})

Mock.mock(/\/api\/patient\/list/, 'get', (options) => pageResult(patients, parseQuery(options.url)))
Mock.mock(/\/api\/patient\/search/, 'get', (options) => pageResult(patients, parseQuery(options.url)))
Mock.mock(/\/api\/patient\/\d+\/consultation/, 'post', () => ({ code: 200, message: '已开始就诊' }))
Mock.mock(/\/api\/patient\/\d+/, 'get', (options) => {
  const id = Number(options.url.match(/\d+/)[0])
  return { code: 200, data: patients.find((p) => p.id === id) }
})
Mock.mock(/\/api\/patient/, 'post', () => ({ code: 200, message: '添加成功' }))
Mock.mock(/\/api\/patient\/\d+/, 'put', () => ({ code: 200, message: '更新成功' }))
Mock.mock(/\/api\/patient\/\d+/, 'delete', () => ({ code: 200, message: '删除成功' }))

Mock.mock(/\/api\/doctor\/list/, 'get', (options) => pageResult(doctors, parseQuery(options.url)))
Mock.mock(/\/api\/doctor/, 'post', () => ({ code: 200, message: '添加成功' }))
Mock.mock(/\/api\/doctor\/\d+/, 'put', () => ({ code: 200, message: '更新成功' }))
Mock.mock(/\/api\/doctor\/\d+/, 'delete', () => ({ code: 200, message: '删除成功' }))

Mock.mock(/\/api\/schedule\/list/, 'get', (options) => pageResult(schedules, parseQuery(options.url)))
Mock.mock(/\/api\/schedule/, 'post', () => ({ code: 200, message: '添加成功' }))
Mock.mock(/\/api\/schedule\/\d+/, 'put', () => ({ code: 200, message: '更新成功' }))
Mock.mock(/\/api\/schedule\/\d+/, 'delete', () => ({ code: 200, message: '删除成功' }))

Mock.mock(/\/api\/record\/list/, 'get', (options) => pageResult(records, parseQuery(options.url)))
Mock.mock(/\/api\/record/, 'post', () => ({ code: 200, message: '添加成功' }))
Mock.mock(/\/api\/record\/\d+/, 'put', () => ({ code: 200, message: '更新成功' }))
Mock.mock(/\/api\/record\/\d+/, 'delete', () => ({ code: 200, message: '删除成功' }))

Mock.mock(/\/api\/service\/list/, 'get', (options) => pageResult(services, parseQuery(options.url)))
Mock.mock(/\/api\/service/, 'post', () => ({ code: 200, message: '添加成功' }))
Mock.mock(/\/api\/service\/\d+/, 'put', () => ({ code: 200, message: '更新成功' }))
Mock.mock(/\/api\/service\/\d+/, 'delete', () => ({ code: 200, message: '删除成功' }))

Mock.mock(/\/api\/drug\/list/, 'get', (options) => pageResult(drugs, parseQuery(options.url)))
Mock.mock(/\/api\/drug/, 'post', () => ({ code: 200, message: '添加成功' }))
Mock.mock(/\/api\/drug\/\d+/, 'put', () => ({ code: 200, message: '更新成功' }))
Mock.mock(/\/api\/drug\/\d+/, 'delete', () => ({ code: 200, message: '删除成功' }))

Mock.mock(/\/api\/procurement\/list/, 'get', (options) => pageResult(procurements, parseQuery(options.url)))
Mock.mock(/\/api\/procurement/, 'post', () => ({ code: 200, message: '添加成功' }))
Mock.mock(/\/api\/procurement\/\d+/, 'put', () => ({ code: 200, message: '更新成功' }))
Mock.mock(/\/api\/procurement\/\d+/, 'delete', () => ({ code: 200, message: '删除成功' }))

Mock.mock(/\/api\/inventory\/list/, 'get', (options) => pageResult(inventories, parseQuery(options.url)))
Mock.mock(/\/api\/inventory\/\d+/, 'put', () => ({ code: 200, message: '更新成功' }))

Mock.mock(/\/api\/dispensing\/list/, 'get', (options) => pageResult(dispensings, parseQuery(options.url)))
Mock.mock(/\/api\/dispensing/, 'post', () => ({ code: 200, message: '添加成功' }))
Mock.mock(/\/api\/dispensing\/\d+/, 'put', () => ({ code: 200, message: '配药完成' }))

Mock.mock(/\/api\/finance\/list/, 'get', (options) => pageResult(finances, parseQuery(options.url)))
Mock.mock(/\/api\/finance/, 'post', () => ({ code: 200, message: '添加成功' }))
Mock.mock(/\/api\/finance\/\d+/, 'put', () => ({ code: 200, message: '更新成功' }))
Mock.mock(/\/api\/finance\/\d+/, 'delete', () => ({ code: 200, message: '删除成功' }))

Mock.mock(/\/api\/income-expense\/list/, 'get', (options) => pageResult(incomeExpenses, parseQuery(options.url)))
Mock.mock(/\/api\/income-expense/, 'post', () => ({ code: 200, message: '添加成功' }))

Mock.mock(/\/api\/reimbursement\/list/, 'get', (options) => pageResult(reimbursements, parseQuery(options.url)))
Mock.mock(/\/api\/reimbursement/, 'post', () => ({ code: 200, message: '提交成功' }))
Mock.mock(/\/api\/reimbursement\/\d+\/approve/, 'post', () => ({ code: 200, message: '审批通过' }))

Mock.mock(/\/api\/settlement\/list/, 'get', (options) => pageResult(settlements, parseQuery(options.url)))
Mock.mock(/\/api\/settlement/, 'post', () => ({ code: 200, message: '创建成功' }))
Mock.mock(/\/api\/settlement\/\d+\/settle/, 'post', () => ({ code: 200, message: '结算完成' }))

Mock.mock(/\/api\/statistics\/overview/, 'get', () => ({
  code: 200,
  data: {
    patientCount: 1286,
    doctorCount: 86,
    todayVisit: 342,
    todayIncome: 98560.5,
    drugStock: 15680,
    pendingSettlement: 23
  }
}))

Mock.mock(/\/api\/statistics\/analysis/, 'get', () => ({
  code: 200,
  data: {
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
}))

Mock.mock(/\/api\/statistics\/reports/, 'get', () => ({
  code: 200,
  data: {
    list: [
      { id: 1, name: '月度就诊量报表', type: '就诊统计', period: '2026-06', status: 1, createTime: '2026-06-01 08:00:00' },
      { id: 2, name: '科室收入报表', type: '财务统计', period: '2026-06', status: 1, createTime: '2026-06-01 08:00:00' },
      { id: 3, name: '药品库存报表', type: '药房统计', period: '2026-06', status: 0, createTime: '2026-06-15 10:00:00' }
    ]
  }
}))

const adminUsers = Mock.mock({
  'list|15': [{
    id: '@increment',
    username: /admin|doctor|nurse|finance|pharmacy/,
    name: '@cname',
    roleName: '@pick(["系统管理员","医生","护士","财务","药房"])',
    department: '@pick(["信息科","内科","外科","财务科","药房"])',
    phone: /^1[3-9]\d{9}$/,
    status: '@pick([0,1])',
    createTime: '@datetime'
  }]
}).list

const adminRoles = [
  { id: 1, roleName: '系统管理员', roleCode: 'admin', description: '拥有全部权限', status: 1, createTime: '2026-01-01 00:00:00' },
  { id: 2, roleName: '医生', roleCode: 'doctor', description: '患者、诊疗相关权限', status: 1, createTime: '2026-01-01 00:00:00' },
  { id: 3, roleName: '护士', roleCode: 'nurse', description: '挂号、预约相关权限', status: 1, createTime: '2026-01-01 00:00:00' },
  { id: 4, roleName: '财务', roleCode: 'finance', description: '财务模块权限', status: 1, createTime: '2026-01-01 00:00:00' },
  { id: 5, roleName: '药房', roleCode: 'pharmacy', description: '药房模块权限', status: 1, createTime: '2026-01-01 00:00:00' }
]

const adminDepartments = [
  {
    id: 1, name: '内科', code: 'NK', parentId: 0, leader: '张主任', phone: '010-12345601', status: 1, sort: 1,
    desc: '内科承担成人常见内科疾病的门诊与住院诊疗，涵盖心血管、呼吸、消化、内分泌等亚专业，配备完善检查与慢病管理体系。',
    commonDiseases: ['高血压', '糖尿病', '冠心病', '肺炎', '胃炎', '慢阻肺'],
    specialties: ['心内', '消化', '呼吸', '内分泌'],
    waitingCount: 15, avgWaitMinutes: 22
  },
  {
    id: 2, name: '外科', code: 'WK', parentId: 0, leader: '李主任', phone: '010-12345602', status: 1, sort: 2,
    desc: '外科开展普外、微创及创伤急救等诊疗服务，重点开展腹腔镜手术、胃肠道肿瘤及甲状腺疾病治疗。',
    commonDiseases: ['阑尾炎', '疝气', '胆囊结石', '甲状腺结节', '外伤'],
    specialties: ['普外', '微创', '创伤', '甲状腺'],
    waitingCount: 8, avgWaitMinutes: 18
  },
  {
    id: 3, name: '儿科', code: 'EK', parentId: 0, leader: '王主任', phone: '010-12345603', status: 1, sort: 3,
    desc: '儿科专注 0-14 岁儿童常见病、多发病的诊治与保健，提供生长发育评估、过敏管理及疫苗接种咨询。',
    commonDiseases: ['发热', '咳嗽', '腹泻', '湿疹', '生长发育迟缓'],
    specialties: ['呼吸', '保健', '新生儿', '过敏'],
    waitingCount: 22, avgWaitMinutes: 28
  },
  {
    id: 4, name: '信息科', code: 'XXK', parentId: 0, leader: '赵主任', phone: '010-12345604', status: 1, sort: 4,
    desc: '信息科负责医院信息系统运维与技术支持，不对外提供门诊挂号服务。',
    commonDiseases: [],
    specialties: ['系统运维', '网络安全'],
    waitingCount: 0, avgWaitMinutes: 0, outpatient: false
  },
  { id: 5, name: '心血管内科', code: 'XGNK', parentId: 1, leader: '刘医生', phone: '010-12345605', status: 1, sort: 1,
    desc: '心血管内科亚专科，专注冠心病、心律失常及高血压的精细化诊疗。',
    commonDiseases: ['高血压', '心律失常', '心绞痛', '心力衰竭'],
    specialties: ['冠心病', '心律失常', '高血压'],
    waitingCount: 6, avgWaitMinutes: 16
  }
]

function enrichDepartment(dept) {
  const deptDoctors = doctors.filter((d) => d.department === dept.name && d.status !== 0)
  const todaySlots = dept.outpatient === false
    ? 0
    : deptDoctors.reduce((sum, d) => sum + (d.morningSlots || 0) + (d.afternoonSlots || 0), 0)
  return { ...dept, todaySlots }
}

const adminDicts = Mock.mock({
  'list|20': [{
    id: '@increment',
    dictType: '@pick(["gender","visit_type","payment_method","bed_type"])',
    dictLabel: '@pick(["男","女","普通门诊","专家门诊","微信支付","支付宝","普通床位","VIP床位"])',
    dictValue: '@pick(["1","2","1","2","wechat","alipay","1","2"])',
    sort: '@integer(1,10)',
    status: '@pick([0,1])',
    remark: '@csentence(3,8)'
  }]
}).list

const adminConfigs = [
  { id: 1, configKey: 'hospital_name', configValue: '东软云医院', configName: '医院名称', remark: '系统显示名称' },
  { id: 2, configKey: 'register_fee', configValue: '10', configName: '普通挂号费(元)', remark: '默认挂号费用' },
  { id: 3, configKey: 'expert_fee', configValue: '50', configName: '专家挂号费(元)', remark: '专家号费用' },
  { id: 4, configKey: 'appointment_days', configValue: '7', configName: '预约天数', remark: '可提前预约天数' },
  { id: 5, configKey: 'session_timeout', configValue: '30', configName: '会话超时(分钟)', remark: '登录超时时间' }
]

const operationLogs = Mock.mock({
  'list|30': [{
    id: '@increment',
    operator: '@cname',
    module: '@pick(["患者管理","系统管理","财务管理","药房管理"])',
    action: '@pick(["新增","修改","删除","查询","导出"])',
    ip: '@ip',
    status: '@pick([0,1])',
    createTime: '@datetime'
  }]
}).list

const loginLogs = Mock.mock({
  'list|25': [{
    id: '@increment',
    username: /admin|doctor|nurse/,
    ip: '@ip',
    browser: '@pick(["Chrome","Edge","Firefox"])',
    os: '@pick(["Windows 10","Windows 11","macOS"])',
    status: '@pick([0,1])',
    message: '@pick(["登录成功","密码错误","账号已锁定"])',
    loginTime: '@datetime'
  }]
}).list

const registers = Mock.mock({
  'list|25': [{
    id: '@increment',
    registerNo: /GH\d{10}/,
    patientName: '@cname',
    department: '@pick(["内科","外科","儿科","骨科"])',
    doctorName: '@cname',
    registerType: '@pick(["普通号","专家号","急诊号"])',
    fee: '@pick([10,50,20])',
    status: '@pick([0,1,2,3])',
    registerTime: '@datetime'
  }]
}).list

const appointments = Mock.mock({
  'list|20': [{
    id: '@increment',
    appointmentNo: /YY\d{10}/,
    patientName: '@cname',
    department: '@pick(["内科","外科","儿科"])',
    doctorName: '@cname',
    appointmentDate: '@date',
    timeSlot: '@pick(["08:00-09:00","09:00-10:00","14:00-15:00"])',
    status: '@pick([0,1,2,3])',
    createTime: '@datetime'
  }]
}).list

const payments = Mock.mock({
  'list|25': [{
    id: '@increment',
    paymentNo: /JF\d{10}/,
    patientName: '@cname',
    itemName: '@pick(["挂号费","检查费","药品费","住院押金"])',
    amount: '@float(10,5000,2,2)',
    payMethod: '@pick(["微信","支付宝","银行卡"])',
    status: 0,
    createTime: '@datetime',
    dueDate: '@date',
    payTime: ''
  }]
}).list

payments.forEach((item, idx) => {
  item.status = idx < 12 ? 0 : 1
  if (item.status === 0) {
    item.payTime = ''
    const due = new Date()
    due.setDate(due.getDate() + (idx % 4 === 0 ? -2 : idx % 3))
    item.dueDate = due.toISOString().slice(0, 10)
  } else {
    item.payTime = Random.datetime('yyyy-MM-dd HH:mm:ss')
    item.payMethod = Random.pick(['微信', '支付宝', '银行卡'])
  }
})

const beds = Mock.mock({
  'list|30': [{
    id: '@increment',
    bedNo: /B\d{3}/,
    ward: '@pick(["内科一病区","外科二病区","儿科病区"])',
    bedType: '@pick(["普通床位","VIP床位","监护床位"])',
    price: '@pick([80,200,500])',
    status: '@pick([0,1,2])',
    patientName: '@pick(["-","-","-","@cname"])'
  }]
}).list

const notices = Mock.mock({
  'list|12': [{
    id: '@increment',
    title: '@pick(["系统维护通知","节假日排班调整","新设备投入使用","疫苗接种提醒","门诊时间调整","停诊公告"])',
    type: '@pick(["通知","公告","紧急"])',
    publisher: '@cname',
    status: 1,
    publishTime: '@datetime("yyyy-MM-dd HH:mm:ss")',
    content: '@csentence(20,50)'
  }]
}).list

notices.forEach((n, i) => {
  const d = new Date()
  d.setDate(d.getDate() - i * 2)
  n.publishTime = d.toISOString().slice(0, 19).replace('T', ' ')
  if (i < 2) n.type = '紧急'
})

const menuTree = [
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

Mock.mock(/\/api\/admin\/user\/list/, 'get', (options) => pageResult(adminUsers, parseQuery(options.url)))
Mock.mock(/\/api\/admin\/user\/\d+\/reset-password/, 'post', () => ({ code: 200, message: '密码已重置为123456' }))
Mock.mock(/\/api\/admin\/user/, 'post', () => ({ code: 200, message: '添加成功' }))
Mock.mock(/\/api\/admin\/user\/\d+/, 'put', () => ({ code: 200, message: '更新成功' }))
Mock.mock(/\/api\/admin\/user\/\d+/, 'delete', () => ({ code: 200, message: '删除成功' }))

Mock.mock(/\/api\/admin\/role\/list/, 'get', (options) => pageResult(adminRoles, parseQuery(options.url)))
Mock.mock(/\/api\/admin\/role/, 'post', () => ({ code: 200, message: '添加成功' }))
Mock.mock(/\/api\/admin\/role\/\d+/, 'put', () => ({ code: 200, message: '更新成功' }))
Mock.mock(/\/api\/admin\/role\/\d+/, 'delete', () => ({ code: 200, message: '删除成功' }))

Mock.mock(/\/api\/admin\/department\/list/, 'get', (options) => {
  const result = pageResult(adminDepartments, parseQuery(options.url))
  result.data.list = result.data.list.map(enrichDepartment)
  return result
})
Mock.mock(/\/api\/admin\/department\/tree/, 'get', () => ({
  code: 200,
  data: adminDepartments.map(enrichDepartment)
}))
Mock.mock(/\/api\/admin\/department/, 'post', () => ({ code: 200, message: '添加成功' }))
Mock.mock(/\/api\/admin\/department\/\d+/, 'put', () => ({ code: 200, message: '更新成功' }))
Mock.mock(/\/api\/admin\/department\/\d+/, 'delete', () => ({ code: 200, message: '删除成功' }))

Mock.mock(/\/api\/admin\/menu\/tree/, 'get', () => ({ code: 200, data: menuTree }))
Mock.mock(/\/api\/admin\/menu/, 'post', () => ({ code: 200, message: '添加成功' }))
Mock.mock(/\/api\/admin\/menu\/\d+/, 'put', () => ({ code: 200, message: '更新成功' }))
Mock.mock(/\/api\/admin\/menu\/\d+/, 'delete', () => ({ code: 200, message: '删除成功' }))

Mock.mock(/\/api\/admin\/dict\/list/, 'get', (options) => pageResult(adminDicts, parseQuery(options.url)))
Mock.mock(/\/api\/admin\/dict/, 'post', () => ({ code: 200, message: '添加成功' }))
Mock.mock(/\/api\/admin\/dict\/\d+/, 'put', () => ({ code: 200, message: '更新成功' }))
Mock.mock(/\/api\/admin\/dict\/\d+/, 'delete', () => ({ code: 200, message: '删除成功' }))

Mock.mock(/\/api\/admin\/config\/list/, 'get', () => ({ code: 200, data: adminConfigs }))
Mock.mock(/\/api\/admin\/config/, 'put', () => ({ code: 200, message: '配置已保存' }))

Mock.mock(/\/api\/admin\/log\/operation/, 'get', (options) => pageResult(operationLogs, parseQuery(options.url)))
Mock.mock(/\/api\/admin\/log\/login/, 'get', (options) => pageResult(loginLogs, parseQuery(options.url)))

Mock.mock(/\/api\/register\/list/, 'get', (options) => pageResult(registers, parseQuery(options.url)))
Mock.mock(/\/api\/register\/\d+\/cancel/, 'post', (options) => {
  const id = Number(options.url.match(/\/register\/(\d+)/)[1])
  const item = registers.find((r) => r.id === id)
  if (item) item.status = 3
  return { code: 200, message: '已退号' }
})
Mock.mock(/\/api\/register$/, 'post', (options) => {
  const body = JSON.parse(options.body || '{}')
  const feeMap = { 普通号: 10, 专家号: 50, 急诊号: 20 }
  const fee = feeMap[body.registerType] || 10
  const id = registers.length ? Math.max(...registers.map((r) => r.id)) + 1 : 1
  const now = new Date().toISOString().slice(0, 19).replace('T', ' ')
  const record = {
    id,
    registerNo: 'GH' + Date.now(),
    patientName: body.patientName,
    department: body.department,
    doctorName: body.doctorName,
    registerType: body.registerType || '普通号',
    fee,
    status: 0,
    registerTime: now
  }
  registers.unshift(record)
  payments.unshift({
    id: payments.length ? Math.max(...payments.map((p) => p.id)) + 1 : 1,
    paymentNo: 'JF' + Date.now(),
    patientName: body.patientName,
    itemName: '挂号费',
    amount: fee,
    status: 0,
    payMethod: '',
    payTime: '',
    createTime: now,
    dueDate: now.slice(0, 10)
  })
  return { code: 200, message: '挂号成功', data: record }
})

Mock.mock(/\/api\/appointment\/list/, 'get', (options) => pageResult(appointments, parseQuery(options.url)))
Mock.mock(/\/api\/appointment\/\d+\/confirm/, 'post', () => ({ code: 200, message: '预约已确认' }))
Mock.mock(/\/api\/appointment\/\d+\/cancel/, 'post', (options) => {
  const id = Number(options.url.match(/\/appointment\/(\d+)/)[1])
  const item = appointments.find((a) => a.id === id)
  if (item) item.status = 3
  return { code: 200, message: '预约已取消' }
})
Mock.mock(/\/api\/appointment$/, 'post', (options) => {
  const body = JSON.parse(options.body || '{}')
  const id = appointments.length ? Math.max(...appointments.map((a) => a.id)) + 1 : 1
  const now = new Date().toISOString().slice(0, 19).replace('T', ' ')
  const record = {
    id,
    appointmentNo: 'YY' + Date.now(),
    patientName: body.patientName,
    department: body.department,
    doctorName: body.doctorName,
    appointmentDate: body.appointmentDate,
    timeSlot: body.timeSlot,
    status: 0,
    createTime: now
  }
  appointments.unshift(record)
  return { code: 200, message: '预约成功', data: record }
})

Mock.mock(/\/api\/payment\/summary/, 'get', () => {
  const pending = payments.filter((p) => p.status === 0)
  return {
    code: 200,
    data: {
      count: pending.length,
      totalAmount: pending.reduce((sum, p) => sum + (p.amount || 0), 0)
    }
  }
})

Mock.mock(/\/api\/payment\/batch/, 'post', (options) => {
  const body = JSON.parse(options.body || '{}')
  const ids = body.ids || []
  const payMethod = body.payMethod || '微信'
  const now = new Date().toISOString().slice(0, 19).replace('T', ' ')
  const paid = []
  ids.forEach((id) => {
    const item = payments.find((p) => p.id === Number(id))
    if (item && item.status === 0) {
      item.status = 1
      item.payMethod = payMethod
      item.payTime = now
      paid.push(item)
    }
  })
  return { code: 200, message: '批量支付成功', data: { list: paid } }
})

Mock.mock(/\/api\/payment\/list/, 'get', (options) => {
  const params = parseQuery(options.url)
  let list = [...payments]
  if (String(params.status) === '1') {
    list.sort((a, b) => (b.payTime || '').localeCompare(a.payTime || ''))
  } else if (String(params.status) === '0') {
    list.sort((a, b) => (a.dueDate || '').localeCompare(b.dueDate || ''))
  }
  return pageResult(list, params)
})

Mock.mock(/\/api\/payment\/\d+\/refund/, 'post', () => ({ code: 200, message: '退款成功' }))

Mock.mock(/\/api\/payment$/, 'post', (options) => {
  const body = JSON.parse(options.body || '{}')
  const item = payments.find((p) => p.id === Number(body.id))
  if (!item) return { code: 404, message: '账单不存在' }
  if (item.status !== 0) return { code: 409, message: '该账单已支付' }
  item.status = 1
  item.payMethod = body.payMethod || '微信'
  item.payTime = new Date().toISOString().slice(0, 19).replace('T', ' ')
  return { code: 200, message: '缴费成功', data: item }
})

Mock.mock(/\/api\/bed\/list/, 'get', (options) => pageResult(beds, parseQuery(options.url)))
Mock.mock(/\/api\/bed/, 'post', () => ({ code: 200, message: '添加成功' }))
Mock.mock(/\/api\/bed\/\d+/, 'put', () => ({ code: 200, message: '更新成功' }))
Mock.mock(/\/api\/bed\/\d+/, 'delete', () => ({ code: 200, message: '删除成功' }))

Mock.mock(/\/api\/notice\/list/, 'get', (options) => pageResult(notices, parseQuery(options.url)))
Mock.mock(/\/api\/notice\/\d+$/, 'get', (options) => {
  const id = Number(options.url.match(/\/notice\/(\d+)/)[1])
  const item = notices.find((n) => n.id === id)
  return item ? { code: 200, data: item } : { code: 404, message: '公告不存在', data: null }
})
Mock.mock(/\/api\/notice$/, 'post', () => ({ code: 200, message: '发布成功' }))
Mock.mock(/\/api\/notice\/\d+/, 'put', () => ({ code: 200, message: '更新成功' }))
Mock.mock(/\/api\/notice\/\d+/, 'delete', () => ({ code: 200, message: '删除成功' }))

Mock.mock(/\/api\/statistics\/decision/, 'get', () => ({
  code: 200,
  data: {
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
}))

function parseQuery(url) {
  const query = {}
  const qs = url.split('?')[1]
  if (!qs) return query
  qs.split('&').forEach((pair) => {
    const [k, v] = pair.split('=')
    query[decodeURIComponent(k)] = decodeURIComponent(v || '')
  })
  return query
}

export default Mock
