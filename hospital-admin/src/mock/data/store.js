import Mock from 'mockjs'
import {
  ADMIN_ROLES,
  ADMIN_DEPARTMENTS,
  ADMIN_CONFIGS,
  MENU_TREE
} from './static.js'
import { enrichStore } from '../helpers/storeEnrich.js'

const Random = Mock.Random

function genList(template, count) {
  return Mock.mock({ [`list|${count}`]: [template] }).list
}

const drugs = genList({
  id: '@increment',
  drugCode: /DR\d{6}/,
  drugName: '@pick(["阿莫西林","布洛芬","头孢克肟","维生素C","氯化钠"])',
  specification: '@pick(["0.25g*24粒","100ml","500mg*10片"])',
  manufacturer: '@pick(["华北制药","扬子江药业","石药集团"])',
  unit: '@pick(["盒","瓶","支"])',
  price: '@float(5,200,2,2)',
  drugType: '@pick(["处方药","OTC"])',
  riskLevel: '@pick(["普通","普通","高危"])',
  archived: 0,
  status: '@pick([0,1])'
}, 30)

/** 内存数据仓库（Mock 服务端状态） */
export const store = {
  patients: genList({
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
  }, 30),

  doctors: genList({
    id: '@increment',
    doctorNo: /D\d{6}/,
    name: '@cname',
    gender: '@pick([1,2])',
    department: '@pick(["内科","外科","儿科","骨科","眼科"])',
    title: '@pick(["主任医师","副主任医师","主治医师","住院医师"])',
    specialty: '@pick(["心血管","骨科手术","儿科保健","眼科疾病","普外"])',
    phone: /^1[3-9]\d{9}$/,
    status: '@pick([0,1])',
    createTime: '@datetime'
  }, 20),

  schedules: genList({
    id: '@increment',
    doctorName: '@cname',
    department: '@pick(["内科","外科","儿科"])',
    shiftDate: '@date("yyyy-MM-dd")',
    shiftType: '@pick(["早班","中班","晚班"])',
    status: '@pick([0,1])',
    remark: '@csentence(5,10)'
  }, 25),

  records: genList({
    id: '@increment',
    patientName: '@cname',
    doctorName: '@cname',
    department: '@pick(["内科","外科","儿科"])',
    diagnosis: '@csentence(5,15)',
    treatment: '@csentence(5,15)',
    visitTime: '@datetime("yyyy-MM-dd HH:mm:ss")',
    status: '@pick([0,1,2])',
    revisionStatus: 0,
    revisions: []
  }, 25),

  services: genList({
    id: '@increment',
    serviceName: '@pick(["门诊挂号","专家会诊","体检套餐","疫苗接种","康复理疗","CT检查","阑尾切除术"])',
    category: '@pick(["检查","治疗","手术","其他"])',
    department: '@pick(["内科","外科","儿科"])',
    price: '@float(50,500,2,2)',
    duration: '@integer(15,120)',
    status: '@pick([0,1])',
    feeItem: '@pick(["挂号费","检查费","药品费","住院押金"])',
    description: '@csentence(8,20)'
  }, 15),

  drugs,

  procurements: genList({
    id: '@increment',
    orderNo: /PO\d{10}/,
    drugName: '@pick(["阿莫西林","布洛芬","头孢克肟"])',
    quantity: '@integer(100,1000)',
    unitPrice: '@float(5,50,2,2)',
    supplier: '@pick(["国药控股","上药集团","华润医药"])',
    phase: '@pick([0,1,2,3])',
    urgent: '@pick([0,0,1])',
    orderDate: '@date("yyyy-MM-dd")',
    logisticsNo: /LG\d{10}/,
    receiptNote: '@csentence(5,12)'
  }, 20),

  inventories: drugs.map((d, i) => {
    const days = Random.integer(-30, 180)
    const exp = new Date()
    exp.setDate(exp.getDate() + days)
    return {
      id: i + 1,
      drugId: d.id,
      drugName: d.drugName,
      drugCode: d.drugCode,
      stock: Random.integer(0, 500),
      minStock: 50,
      batchNo: 'BN' + String(10000000 + i),
      expiryDate: exp.toISOString().slice(0, 10),
      warehouse: Random.pick(['中心药房', '门诊药房', '住院药房']),
      updateTime: Random.datetime('yyyy-MM-dd HH:mm:ss')
    }
  }),

  dispensings: genList({
    id: '@increment',
    prescriptionNo: /RX\d{10}/,
    patientName: '@cname',
    doctorName: '@cname',
    drugName: '@pick(["阿莫西林","布洛芬","头孢克肟"])',
    quantity: '@integer(1,10)',
    priority: '@pick(["急诊","门诊","住院"])',
    barcode: /BC\d{12}/,
    status: '@pick([0,1,2])',
    createTime: '@datetime("yyyy-MM-dd HH:mm:ss")'
  }, 20),

  finances: genList({
    id: '@increment',
    accountName: '@pick(["门诊收入账户","住院收入账户","药品收入账户"])',
    accountNo: /AC\d{8}/,
    balance: '@float(10000,500000,2,2)',
    bank: '@pick(["工商银行","建设银行","农业银行"])',
    status: '@pick([0,1,1,1])',
    archived: 0,
    overdraft: '@pick([0,0,0,1])',
    warnThreshold: 20000,
    updateTime: '@datetime("yyyy-MM-dd HH:mm:ss")'
  }, 15),

  incomeExpenses: genList({
    id: '@increment',
    type: '@pick(["收入","支出"])',
    category: '@pick(["挂号费","药品费","检查费","采购支出","人员工资"])',
    amount: '@float(100,10000,2,2)',
    operator: '@cname',
    recordDate: '@date("yyyy-MM-dd")',
    remark: '@csentence(5,10)',
    sourceModule: '@pick(["挂号缴费","在线缴费","采购管理","报销管理","手工录入"])',
    sourceId: /BIZ\d{8}/,
    autoCollected: '@pick([0,1,1,1])'
  }, 30),

  reimbursements: genList({
    id: '@increment',
    applicant: '@cname',
    department: '@pick(["内科","外科","行政部"])',
    amount: '@float(200,5000,2,2)',
    reason: '@csentence(5,15)',
    status: '@pick([0,0,1,2])',
    applyDate: '@date("yyyy-MM-dd")',
    invoiceNo: /INV\d{10}/,
    urgent: '@pick([0,0,1])',
    overdue: '@pick([0,0,1])',
    attachments: ['发票扫描件.pdf'],
    workflow: []
  }, 20),

  settlements: genList({
    id: '@increment',
    settlementNo: /ST\d{10}/,
    patientName: '@cname',
    totalAmount: '@float(100,5000,2,2)',
    paidAmount: '@float(0,5000,2,2)',
    insuranceAmount: '@float(0,3000,2,2)',
    selfPayAmount: '@float(0,2000,2,2)',
    status: '@pick([0,1,2])',
    settlementDate: '@date("yyyy-MM-dd")',
    dueDate: '@date("yyyy-MM-dd")',
    overdue: '@pick([0,0,1])',
    feeItems: []
  }, 15),

  adminUsers: genList({
    id: '@increment',
    username: /admin|doctor|nurse|finance|pharmacy/,
    name: '@cname',
    roleName: '@pick(["系统管理员","医生","护士","财务","药房"])',
    department: '@pick(["信息科","内科","外科","财务科","药房"])',
    phone: /^1[3-9]\d{9}$/,
    status: '@pick([0,1])',
    createTime: '@datetime'
  }, 15),

  adminRoles: [...ADMIN_ROLES],
  adminDepartments: [...ADMIN_DEPARTMENTS],
  adminConfigs: [...ADMIN_CONFIGS],
  menuTree: JSON.parse(JSON.stringify(MENU_TREE)),

  adminDicts: genList({
    id: '@increment',
    dictType: '@pick(["gender","visit_type","payment_method","bed_type"])',
    dictLabel: '@pick(["男","女","普通门诊","专家门诊","微信支付","支付宝","普通床位","VIP床位"])',
    dictValue: '@pick(["1","2","1","2","wechat","alipay","1","2"])',
    sort: '@integer(1,10)',
    status: '@pick([0,1])',
    remark: '@csentence(3,8)'
  }, 20),

  operationLogs: genList({
    id: '@increment',
    operator: '@cname',
    module: '@pick(["患者管理","系统管理","财务管理","药房管理"])',
    action: '@pick(["新增","修改","删除","查询","导出"])',
    ip: '@ip',
    status: '@pick([0,1])',
    createTime: '@datetime'
  }, 30),

  loginLogs: genList({
    id: '@increment',
    username: /admin|doctor|nurse/,
    ip: '@ip',
    browser: '@pick(["Chrome","Edge","Firefox"])',
    os: '@pick(["Windows 10","Windows 11","macOS"])',
    status: '@pick([0,1])',
    message: '@pick(["登录成功","密码错误","账号已锁定"])',
    loginTime: '@datetime'
  }, 25),

  registers: genList({
    id: '@increment',
    registerNo: /GH\d{10}/,
    patientName: '@cname',
    department: '@pick(["内科","外科","儿科","骨科"])',
    doctorName: '@cname',
    registerType: '@pick(["普通号","专家号","急诊号"])',
    fee: '@pick([10,50,20])',
    status: '@pick([0,1,2,3])',
    registerTime: '@datetime'
  }, 25),

  appointments: genList({
    id: '@increment',
    appointmentNo: /YY\d{10}/,
    patientName: '@cname',
    department: '@pick(["内科","外科","儿科"])',
    doctorName: '@cname',
    appointmentDate: '@date',
    timeSlot: '@pick(["08:00-09:00","09:00-10:00","14:00-15:00"])',
    status: '@pick([0,1,2,3])',
    createTime: '@datetime'
  }, 20),

  payments: genList({
    id: '@increment',
    paymentNo: /JF\d{10}/,
    patientName: '@cname',
    itemName: '@pick(["挂号费","检查费","药品费","住院押金"])',
    amount: '@float(10,5000,2,2)',
    payMethod: '@pick(["现金","微信","支付宝","银行卡"])',
    status: '@pick([0,1,2])',
    payTime: '@datetime'
  }, 25),

  beds: genList({
    id: '@increment',
    bedNo: /B\d{3}/,
    ward: '@pick(["内科一病区","外科二病区","儿科病区"])',
    bedType: '@pick(["普通床位","VIP床位","监护床位"])',
    price: '@pick([80,200,500])',
    status: '@pick([0,1,2])',
    patientName: '@pick(["-","-","-","@cname"])'
  }, 30),

  notices: genList({
    id: '@increment',
    title: '@pick(["系统维护通知","节假日排班调整","新设备投入使用","疫苗接种提醒"])',
    type: '@pick(["通知","公告","紧急"])',
    publisher: '@cname',
    status: '@pick([0,1])',
    publishTime: '@datetime',
    content: '@csentence(20,50)'
  }, 10)
}

enrichStore(store)

export { Random }
