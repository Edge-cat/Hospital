import { enrichPaymentDetail } from './patientFacing.js'

const TIME_SLOTS = ['08:00-09:00', '09:00-10:00', '10:00-11:00', '14:00-15:00', '15:00-16:00', '16:00-17:00']
const REGISTER_FEES = { 普通号: 10, 专家号: 50, 急诊号: 20 }

function fmtNow(offsetDays = 0) {
  const d = new Date()
  d.setDate(d.getDate() + offsetDays)
  return d.toISOString().slice(0, 19).replace('T', ' ')
}

function fmtDate(offsetDays = 0) {
  const d = new Date()
  d.setDate(d.getDate() + offsetDays)
  return d.toISOString().slice(0, 10)
}

function enrichPayments(payments) {
  payments.forEach((p, idx) => {
    const pending = idx % 3 !== 2
    p.status = pending ? 0 : 1
    p.createTime = p.createTime || fmtNow(-(idx % 14))
    if (pending) {
      p.payTime = ''
      p.payMethod = ''
      const due = new Date()
      due.setDate(due.getDate() + (idx % 4 === 0 ? -1 : idx % 5))
      p.dueDate = due.toISOString().slice(0, 10)
    } else {
      p.payTime = p.payTime || fmtNow(-(idx % 7))
      p.payMethod = ['微信', '支付宝', '银行卡'][idx % 3]
      p.voucherNo = `PZ${String(p.paymentNo || p.id).slice(-8)}`
    }
    p.itemType =
      p.itemType ||
      (p.itemName?.includes('检查') ? 'check' : p.itemName?.includes('药') ? 'medicine' : 'register')
    Object.assign(p, enrichPaymentDetail(p))
  })
}

function enrichRegisters(registers) {
  registers.forEach((r, idx) => {
    r.registerType = r.registerType || '普通号'
    r.fee = REGISTER_FEES[r.registerType] ?? r.fee ?? 10
    r.registerTime = r.registerTime || fmtNow(-(idx % 10))
    if (r.status === undefined) r.status = idx % 5 === 0 ? 3 : idx % 4
  })
}

function enrichAppointments(appointments) {
  appointments.forEach((a, idx) => {
    a.appointmentDate = a.appointmentDate?.slice?.(0, 10) || fmtDate(idx % 7 + 1)
    a.timeSlot = TIME_SLOTS.includes(a.timeSlot) ? a.timeSlot : TIME_SLOTS[idx % TIME_SLOTS.length]
    a.createTime = a.createTime || fmtNow(-(idx % 5))
    if (a.status === undefined) a.status = idx % 4
  })
}

function enrichNotices(notices) {
  notices.forEach((n, idx) => {
    n.status = 1
    const d = new Date()
    d.setDate(d.getDate() - idx * 2)
    n.publishTime = n.publishTime || fmtNow(-idx * 2)
    if (idx < 2) n.type = '紧急'
    n.content = n.content || `${n.title}：请全院各部门知悉并遵照执行。`
  })
}

function enrichDoctors(doctors) {
  doctors.forEach((d, idx) => {
    d.status = d.status ?? 1
    d.morningSlots = d.morningSlots ?? (idx % 3 === 0 ? 0 : (idx % 5) + 3)
    d.afternoonSlots = d.afternoonSlots ?? (idx % 4 === 0 ? 0 : (idx % 4) + 2)
    d.specialty = d.specialty || `${d.department}常见病、多发病诊治`
  })
}

function enrichProcurements(procurements) {
  procurements.forEach((p, idx) => {
    p.phase = p.phase ?? idx % 4
    p.orderDate = p.orderDate?.slice?.(0, 10) || fmtDate(-(idx % 20))
    p.logisticsNo = p.logisticsNo || (p.phase >= 1 ? `LG${100000 + p.id}` : '')
    p.receiptNote = p.receiptNote || (p.phase >= 2 ? '在途运输中' : '')
    p.urgent = p.urgent ?? (idx % 7 === 0 ? 1 : 0)
  })
}

function enrichDispensings(dispensings) {
  dispensings.forEach((d, idx) => {
    d.priority = d.priority || ['门诊', '急诊', '住院'][idx % 3]
    d.barcode = d.barcode || `BC${1000000000 + d.id}`
    d.createTime = d.createTime || fmtNow(-(idx % 8))
  })
}

function enrichBeds(beds) {
  beds.forEach((b) => {
    if (b.patientName === '-' || !b.patientName) {
      b.patientName = b.status === 1 ? b.patientName : ''
    }
    if (b.status === 0) b.patientName = ''
  })
}

function enrichSettlements(settlements) {
  settlements.forEach((s, idx) => {
    s.dueDate = s.dueDate || fmtDate(7 - (idx % 5))
    s.overdue = s.overdue ?? (s.status === 0 && s.dueDate < fmtDate(0) ? 1 : 0)
    s.insuranceAmount = s.insuranceAmount ?? Math.round((s.totalAmount || 0) * 0.6 * 100) / 100
    s.selfPayAmount = s.selfPayAmount ?? Math.max(0, (s.totalAmount || 0) - (s.paidAmount || 0))
    if (!s.feeItems?.length && s.totalAmount) {
      s.feeItems = [
        { name: '挂号费', amount: Math.round(s.totalAmount * 0.1) },
        { name: '检查费', amount: Math.round(s.totalAmount * 0.35) },
        { name: '药品费', amount: Math.round(s.totalAmount * 0.55) }
      ]
    }
  })
}

function enrichReimbursements(reimbursements) {
  reimbursements.forEach((r, idx) => {
    r.applyDate = r.applyDate?.slice?.(0, 10) || fmtDate(-(idx % 15))
    r.invoiceNo = r.invoiceNo || `INV${100000 + r.id}`
    r.attachments = r.attachments?.length ? r.attachments : ['发票扫描件.pdf']
    r.workflow = r.workflow?.length
      ? r.workflow
      : [
          { node: '提交申请', time: r.applyDate, operator: r.applicant, status: 'done' },
          { node: '科室审批', time: r.applyDate, operator: '科室主任', status: r.status >= 1 ? 'done' : 'pending' },
          { node: '财务打款', time: '', operator: '财务部', status: r.status === 1 ? 'done' : 'pending' }
        ]
  })
}

function enrichAdminDepartments(departments) {
  departments.forEach((d, idx) => {
    if (d.outpatient === undefined) d.outpatient = d.name !== '信息科'
    d.desc = d.desc || `${d.name}提供门诊与住院诊疗服务。`
    d.commonDiseases = d.commonDiseases?.length ? d.commonDiseases : []
    d.specialties = d.specialties?.length ? d.specialties : []
    d.sort = d.sort ?? idx + 1
    d.waitingCount = d.waitingCount ?? (d.outpatient ? 5 + (d.id % 20) : 0)
    d.avgWaitMinutes = d.avgWaitMinutes ?? (d.outpatient ? 12 + (d.id % 18) : 0)
    d.todaySlots = d.todaySlots ?? (d.outpatient ? 10 + (d.id % 15) : 0)
  })
}

function enrichPatients(patients) {
  patients.forEach((p, idx) => {
    p.status = p.status ?? idx % 3
    p.phone = p.phone || `138${String(10000000 + p.id).slice(-8)}`
    p.patientNo = p.patientNo || `P${String(10000000 + p.id).slice(-8)}`
  })
}

function enrichServices(services) {
  services.forEach((s) => {
    s.status = s.status ?? 1
    s.category = s.category || '其他'
    s.feeItem = s.feeItem || '检查费'
  })
}

function enrichDrugs(drugs) {
  drugs.forEach((d) => {
    d.archived = d.archived ?? 0
    d.drugType = d.drugType || '处方药'
    d.riskLevel = d.riskLevel || '普通'
    d.status = d.status ?? 1
  })
}

function enrichFinances(finances) {
  finances.forEach((f) => {
    f.archived = f.archived ?? 0
    f.status = f.status ?? 1
    f.overdraft = f.overdraft ?? 0
    f.warnThreshold = f.warnThreshold ?? 20000
  })
}

function enrichIncomeExpenses(items) {
  items.forEach((ie, idx) => {
    ie.recordDate = ie.recordDate?.slice?.(0, 10) || fmtDate(-(idx % 30))
    ie.sourceModule = ie.sourceModule || '手工录入'
    ie.autoCollected = ie.autoCollected ?? (idx % 3 !== 0 ? 1 : 0)
    ie.operator = ie.operator || '系统管理员'
  })
}

function enrichSchedules(schedules) {
  schedules.forEach((s, idx) => {
    s.shiftDate = s.shiftDate?.slice?.(0, 10) || fmtDate(idx % 14)
    s.status = s.status ?? 1
  })
}

function enrichAdminUsers(users) {
  users.forEach((u) => {
    u.status = u.status ?? 1
    u.roleName = u.roleName || '医生'
  })
}

function enrichRecords(records) {
  records.forEach((r) => {
    r.visitTime = r.visitTime || fmtNow(-3)
    r.revisionStatus = r.revisionStatus ?? 0
    r.revisions = r.revisions || []
  })
}

function enrichInventories(inventories) {
  inventories.forEach((inv) => {
    inv.updateTime = inv.updateTime || fmtNow(-1)
    if (!inv.batchNo) inv.batchNo = `BN${100000 + inv.id}`
    if (!inv.expiryDate) {
      const exp = new Date()
      exp.setDate(exp.getDate() + 180)
      inv.expiryDate = exp.toISOString().slice(0, 10)
    }
  })
}

/** 启动时对内存 store 做字段补全，保证演示数据一致可用 */
export function enrichStore(store) {
  enrichPayments(store.payments)
  enrichRegisters(store.registers)
  enrichAppointments(store.appointments)
  enrichNotices(store.notices)
  enrichDoctors(store.doctors)
  enrichPatients(store.patients)
  enrichServices(store.services)
  enrichDrugs(store.drugs)
  enrichProcurements(store.procurements)
  enrichDispensings(store.dispensings)
  enrichBeds(store.beds)
  enrichSettlements(store.settlements)
  enrichReimbursements(store.reimbursements)
  enrichAdminDepartments(store.adminDepartments)
  enrichRecords(store.records)
  enrichInventories(store.inventories)
  enrichFinances(store.finances)
  enrichIncomeExpenses(store.incomeExpenses)
  enrichSchedules(store.schedules)
  enrichAdminUsers(store.adminUsers)
}
