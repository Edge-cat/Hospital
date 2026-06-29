const departments = [
  {
    id: 1, name: '内科', code: 'NK', parentId: 0, leader: '张主任', phone: '010-12345601', status: 1, sort: 1,
    desc: '诊治心血管、呼吸、消化等内科疾病，配备完善检查与慢病管理体系。',
    commonDiseases: ['高血压', '糖尿病', '冠心病'], specialties: ['心内', '消化', '呼吸'],
    waitingCount: 15, avgWaitMinutes: 22, outpatient: true
  },
  {
    id: 2, name: '外科', code: 'WK', parentId: 0, leader: '李主任', phone: '010-12345602', status: 1, sort: 2,
    desc: '开展普外、微创及创伤急救等诊疗服务。',
    commonDiseases: ['阑尾炎', '疝气', '外伤'], specialties: ['普外', '微创', '创伤'],
    waitingCount: 8, avgWaitMinutes: 18, outpatient: true
  },
  {
    id: 3, name: '儿科', code: 'EK', parentId: 0, leader: '王主任', phone: '010-12345603', status: 1, sort: 3,
    desc: '专注儿童保健与儿科疾病诊疗。',
    commonDiseases: ['发热', '咳嗽', '腹泻'], specialties: ['呼吸', '保健', '新生儿'],
    waitingCount: 22, avgWaitMinutes: 28, outpatient: true
  },
  {
    id: 4, name: '骨科', code: 'GK', parentId: 0, leader: '赵主任', phone: '010-12345604', status: 1, sort: 4,
    desc: '骨骼、关节、运动损伤专业治疗。',
    commonDiseases: ['骨折', '关节炎', '运动损伤'], specialties: ['创伤', '关节', '脊柱'],
    waitingCount: 10, avgWaitMinutes: 20, outpatient: true
  },
  {
    id: 5, name: '眼科', code: 'YK', parentId: 0, leader: '刘主任', phone: '010-12345605', status: 1, sort: 5,
    desc: '眼部疾病检查与手术治疗。',
    commonDiseases: ['白内障', '近视', '干眼症'], specialties: ['白内障', '屈光', '眼底'],
    waitingCount: 12, avgWaitMinutes: 16, outpatient: true
  },
  {
    id: 6, name: '信息科', code: 'XXK', parentId: 0, leader: '赵主任', phone: '010-12345606', status: 1, sort: 6,
    desc: '医院信息系统运维与技术支持，不对外提供门诊挂号服务。',
    commonDiseases: [], specialties: ['系统运维'], waitingCount: 0, avgWaitMinutes: 0, outpatient: false
  }
]

const doctors = [
  { id: 1, name: '张明', department: '内科', title: '主任医师', specialty: '心血管', remaining: 8 },
  { id: 6, name: '陈静', department: '内科', title: '副主任医师', specialty: '呼吸内科', remaining: 15 },
  { id: 7, name: '周伟', department: '内科', title: '主治医师', specialty: '消化内科', remaining: 0 },
  { id: 2, name: '李华', department: '外科', title: '副主任医师', specialty: '普外', remaining: 12 },
  { id: 8, name: '孙磊', department: '外科', title: '主任医师', specialty: '肝胆外科', remaining: 5 },
  { id: 3, name: '王芳', department: '儿科', title: '主治医师', specialty: '儿科保健', remaining: 20 },
  { id: 9, name: '吴敏', department: '儿科', title: '副主任医师', specialty: '小儿呼吸', remaining: 11 },
  { id: 4, name: '赵强', department: '骨科', title: '主任医师', specialty: '骨科手术', remaining: 6 },
  { id: 10, name: '郑涛', department: '骨科', title: '主治医师', specialty: '运动损伤', remaining: 14 },
  { id: 5, name: '刘洋', department: '眼科', title: '主治医师', specialty: '眼科疾病', remaining: 18 },
  { id: 11, name: '黄丽', department: '眼科', title: '副主任医师', specialty: '白内障', remaining: 9 }
]

const weekdayNames = ['周日', '周一', '周二', '周三', '周四', '周五', '周六']
const scheduleSlots = ['08:00-09:00', '09:00-10:00', '10:00-11:00', '14:00-15:00', '15:00-16:00', '16:00-17:00']

function buildDoctorSchedule(doctorId) {
  const dates = []
  for (let i = 0; i <= 7; i++) {
    const d = new Date()
    d.setDate(d.getDate() + i)
    const dateStr = d.toISOString().slice(0, 10)
    const weekday = weekdayNames[d.getDay()]
    const slots = scheduleSlots.map((timeSlot, idx) => {
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

const notices = [
  { id: 1, title: '端午节门诊排班通知', type: '通知', publisher: '院办', publishTime: '2026-06-20 09:00:00', content: '端午节期间（6月22日-24日），我院门诊照常开放，急诊24小时服务。部分专家门诊时间有所调整，请提前预约。' },
  { id: 2, title: '新引进MRI设备投入使用', type: '公告', publisher: '设备科', publishTime: '2026-06-15 14:00:00', content: '我院新引进的3.0T MRI设备已正式投入使用，可提供更精准的影像诊断服务，欢迎有需要的患者预约检查。' },
  { id: 3, title: '夏季防暑健康提醒', type: '通知', publisher: '预防保健科', publishTime: '2026-06-10 08:00:00', content: '夏季高温来临，请注意防暑降温，多饮水，避免长时间户外活动。如有不适请及时就医。' }
]

const myRegisters = [
  { id: 1, registerNo: 'GH20260629001', patientName: '张三', department: '内科', doctorName: '张明', registerType: '普通号', fee: 10, status: 0, registerTime: '2026-06-29 08:30:00' },
  { id: 2, registerNo: 'GH20260625002', patientName: '张三', department: '儿科', doctorName: '王芳', registerType: '专家号', fee: 50, status: 2, registerTime: '2026-06-25 10:00:00' }
]

const myAppointments = [
  { id: 1, appointmentNo: 'YY20260630001', patientName: '张三', department: '外科', doctorName: '李华', appointmentDate: '2026-06-30', timeSlot: '09:00-10:00', status: 1, createTime: '2026-06-29 11:00:00' },
  { id: 2, appointmentNo: 'YY20260701001', patientName: '张三', department: '眼科', doctorName: '刘洋', appointmentDate: '2026-07-01', timeSlot: '14:00-15:00', status: 0, createTime: '2026-06-29 12:00:00' }
]

const myPayments = [
  {
    id: 1,
    paymentNo: 'JF20260629001',
    patientName: '张三',
    itemName: '挂号费',
    itemType: 'register',
    department: '内科',
    doctorName: '张明',
    amount: 10,
    payMethod: '微信',
    status: 1,
    payTime: '2026-06-29 08:31:00',
    createTime: '2026-06-29 08:30:00',
    advice: '请按时到内科门诊就诊，就诊前请携带身份证。',
    guideTip: '就诊后如需复查，可预约同科室随访。',
    feeBreakdown: [{ name: '普通门诊挂号费', amount: 10 }]
  },
  {
    id: 2,
    paymentNo: 'JF20260629002',
    patientName: '张三',
    itemName: '检查费',
    itemType: 'check',
    department: '内科',
    doctorName: '张明',
    amount: 280,
    payMethod: '微信',
    status: 0,
    payTime: '',
    createTime: '2026-06-29 10:15:00',
    advice: '请空腹前往检验科采血，检查前避免剧烈运动。报告预计 2 小时内出具。',
    guideTip: '缴费完成后请按导引单前往 2 楼检验科窗口排队。',
    feeBreakdown: [
      { name: '血常规', amount: 35 },
      { name: 'C反应蛋白', amount: 45 },
      { name: '胸部CT平扫', amount: 200 }
    ]
  },
  {
    id: 3,
    paymentNo: 'JF20260620003',
    patientName: '张三',
    itemName: '药品费',
    itemType: 'medicine',
    department: '内科',
    doctorName: '张明',
    amount: 156.5,
    payMethod: '支付宝',
    status: 1,
    payTime: '2026-06-20 15:20:00',
    createTime: '2026-06-20 15:18:00',
    advice: '请按医嘱服药，饭后服用，如有不适请及时复诊。',
    guideTip: '取药请前往门诊药房 1 号窗口，支持电子处方扫码取药。',
    feeBreakdown: [
      { name: '阿莫西林胶囊', amount: 28.5 },
      { name: '布洛芬缓释胶囊', amount: 32 },
      { name: '复方甘草片', amount: 18 },
      { name: '维生素C片', amount: 78 }
    ]
  }
]

const myRecords = [
  { id: 1, patientName: '张三', doctorName: '张明', department: '内科', diagnosis: '上呼吸道感染', treatment: '抗病毒治疗，多休息', visitTime: '2026-06-15 09:30:00', status: 2 },
  { id: 2, patientName: '张三', doctorName: '王芳', department: '儿科', diagnosis: '健康查体', treatment: '常规保健指导', visitTime: '2026-05-20 14:00:00', status: 2 }
]

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

const REGISTER_FEES = { 普通号: 10, 专家号: 50, 急诊号: 20 }

function nextId(list) {
  return list.length ? Math.max(...list.map((i) => i.id)) + 1 : 1
}

export function mockRequest(options) {
  return new Promise((resolve) => {
    setTimeout(() => {
      const url = options.url
      const method = options.method || 'GET'
      const data = options.data || {}

      if (url === '/auth/login' && method === 'POST') {
        const user = {
          id: 1,
          name: PATIENT_PROFILE.name,
          phone: PATIENT_PROFILE.phone,
          cardNo: PATIENT_PROFILE.cardNo
        }
        uni.setStorageSync('his_user', JSON.stringify(user))
        uni.setStorageSync('his_token', 'mp-token-demo')
        resolve({ code: 200, data: { token: 'mp-token-demo', user } })
        return
      }
      if (url === '/common/home-overview' || url === '/statistics/overview') {
        resolve({ code: 200, data: { patientCount: 1286, todayVisit: 342, todayIncome: 98560.5 } })
        return
      }
      if (url === '/common/departments' || url === '/admin/department/list') {
        resolve({ code: 200, data: { list: departments, total: departments.length } })
        return
      }
      if (url === '/common/register-types') {
        resolve({
          code: 200,
          data: Object.entries(REGISTER_FEES).map(([value, fee]) => ({ label: value, value, fee }))
        })
        return
      }
      if (url === '/notice/list') {
        resolve({ code: 200, data: { list: notices, total: notices.length } })
        return
      }
      if (url.startsWith('/notice/')) {
        const id = Number(url.split('/').pop())
        resolve({ code: 200, data: notices.find((n) => n.id === id) })
        return
      }
      if (url === '/doctor/list') {
        let list = doctors
        if (data.department) list = list.filter((d) => d.department === data.department)
        resolve({ code: 200, data: { list, total: list.length } })
        return
      }
      if (url === '/register/list') {
        resolve({ code: 200, data: { list: myRegisters, total: myRegisters.length } })
        return
      }
      if (url === '/register' && method === 'POST') {
        const fee = REGISTER_FEES[data.registerType] || data.fee || 10
        const now = new Date().toISOString().slice(0, 19).replace('T', ' ')
        const record = {
          id: nextId(myRegisters),
          registerNo: 'GH' + Date.now(),
          patientName: data.patientName || PATIENT_PROFILE.name,
          department: data.department,
          doctorName: data.doctorName,
          registerType: data.registerType || '普通号',
          fee,
          status: 0,
          registerTime: now
        }
        myRegisters.unshift(record)
        myPayments.unshift({
          id: nextId(myPayments),
          paymentNo: 'JF' + Date.now(),
          patientName: record.patientName,
          itemName: '挂号费',
          itemType: 'register',
          department: data.department,
          doctorName: data.doctorName,
          amount: fee,
          status: 0,
          payMethod: '',
          payTime: '',
          createTime: now,
          advice: '请按时到院就诊，携带身份证。',
          guideTip: '缴费完成后请前往对应科室候诊。',
          feeBreakdown: [{ name: '挂号费', amount: fee }]
        })
        resolve({ code: 200, message: '挂号成功', data: record })
        return
      }
      if (url.includes('/register/') && url.includes('/cancel')) {
        const id = Number(url.match(/\/register\/(\d+)/)[1])
        const item = myRegisters.find((r) => r.id === id)
        if (item) item.status = 3
        resolve({ code: 200, message: '退号成功' })
        return
      }
      if (url === '/appointment/schedule') {
        resolve({ code: 200, data: { dates: buildDoctorSchedule(Number(data.doctorId) || 1) } })
        return
      }
      if (url === '/appointment/list') {
        resolve({ code: 200, data: { list: myAppointments, total: myAppointments.length } })
        return
      }
      if (url === '/appointment' && method === 'POST') {
        const now = new Date().toISOString().slice(0, 19).replace('T', ' ')
        const record = {
          id: nextId(myAppointments),
          appointmentNo: 'YY' + Date.now(),
          patientName: data.patientName || PATIENT_PROFILE.name,
          department: data.department,
          doctorName: data.doctorName,
          appointmentDate: data.appointmentDate,
          timeSlot: data.timeSlot,
          status: 0,
          createTime: now
        }
        myAppointments.unshift(record)
        resolve({ code: 200, message: '预约成功', data: record })
        return
      }
      if (url.includes('/appointment/') && url.includes('/cancel')) {
        const id = Number(url.match(/\/appointment\/(\d+)/)[1])
        const item = myAppointments.find((a) => a.id === id)
        if (item) item.status = 3
        resolve({ code: 200, message: '取消成功' })
        return
      }
      if (url === '/payment/list') {
        let list = myPayments
        if (data.status !== undefined && data.status !== '') {
          list = list.filter((p) => p.status === Number(data.status))
        }
        resolve({ code: 200, data: { list, total: list.length } })
        return
      }
      if (url.startsWith('/payment/') && method === 'GET') {
        const id = Number(url.split('/').pop())
        const item = myPayments.find((p) => p.id === id)
        resolve({ code: 200, data: item || {} })
        return
      }
      if (url === '/payment' && method === 'POST') {
        const item = myPayments.find((p) => p.id === Number(data.id))
        if (item) {
          item.status = 1
          item.payMethod = data.payMethod || '微信'
          item.payTime = new Date().toISOString().slice(0, 19).replace('T', ' ')
        }
        resolve({
          code: 200,
          message: '支付成功',
          data: {
            ...item,
            voucherNo: `PZ${Date.now()}`
          }
        })
        return
      }
      if (url === '/record/list') {
        resolve({ code: 200, data: { list: myRecords, total: myRecords.length } })
        return
      }
      if (url === '/patient/info') {
        const user = uni.getStorageSync('his_user')
        resolve({
          code: 200,
          data: user ? { ...PATIENT_PROFILE, ...JSON.parse(user) } : { ...PATIENT_PROFILE }
        })
        return
      }

      resolve({ code: 200, data: {} })
    }, 300)
  })
}

export { departments, doctors, notices }
