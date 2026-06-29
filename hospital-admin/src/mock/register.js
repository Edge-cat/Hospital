import Mock from 'mockjs'
import { success, fail, pageResult, mutateSuccess } from './utils/response.js'
import { parseQuery, parseBody, matchId } from './utils/query.js'
import { store, Random } from './data/store.js'
import {
  LOGIN_ACCOUNTS,
  COMMON_META,
  STATISTICS_OVERVIEW,
  STATISTICS_ANALYSIS,
  STATISTICS_REPORTS,
  STATISTICS_DECISION
} from './data/static.js'
import {
  REGISTER_FEES,
  buildAppointmentSchedule,
  enrichPaymentDetail,
  pendingPaymentSummary,
  nextId
} from './helpers/patientFacing.js'

function registerCrud(urlBase, listKey, messages = {}) {
  const {
    create = '添加成功',
    update = '更新成功',
    remove = '删除成功'
  } = messages

  Mock.mock(new RegExp(`${urlBase}/list`), 'get', (options) =>
    pageResult(store[listKey], parseQuery(options.url))
  )
  Mock.mock(new RegExp(`${urlBase}$`), 'post', () => mutateSuccess(create))
  Mock.mock(new RegExp(`${urlBase}/\\d+$`), 'put', () => mutateSuccess(update))
  Mock.mock(new RegExp(`${urlBase}/\\d+$`), 'delete', () => mutateSuccess(remove))
}

export function registerMockRoutes() {
  // ---------- 公共字典 ----------
  Mock.mock(/\/api\/common\/meta/, 'get', () => success(COMMON_META))
  Mock.mock(/\/api\/common\/options\/(\w+)/, 'get', (options) => {
    const key = options.url.match(/options\/(\w+)/)[1]
    return success(COMMON_META.options[key] || [])
  })
  Mock.mock(/\/api\/common\/enums\/(\w+)/, 'get', (options) => {
    const key = options.url.match(/enums\/(\w+)/)[1]
    return success(COMMON_META.enums[key] || {})
  })

  // ---------- 认证 ----------
  Mock.mock(/\/api\/auth\/login/, 'post', (options) => {
    const body = parseBody(options)
    const user = LOGIN_ACCOUNTS[body.username]
    if (!user || body.password !== '123456') {
      return { code: 401, message: '用户名或密码错误', data: null }
    }
    return success({
      token: 'mock-token-' + Random.string(16),
      user
    }, '登录成功')
  })

  // ---------- 患者 ----------
  function buildPatientDetail(patient) {
    if (!patient) return null
    const visits = store.records.filter((r) => r.patientName === patient.name).slice(0, 8)
    const pays = store.payments.filter((p) => p.patientName === patient.name)
    const pending = pays.filter((p) => p.status === 0)
    const paid = pays.filter((p) => p.status === 1)
    return {
      ...patient,
      allergyHistory: Random.pick(['无', '青霉素过敏', '海鲜过敏', '无']),
      chronicDisease: Random.pick(['无', '高血压', '糖尿病', '无', '无']),
      medicalHistory: visits.slice(0, 5).map((v) => ({
        date: v.visitTime?.slice(0, 10) || Random.date(),
        content: `${v.diagnosis}（${v.department}）`
      })),
      visitRecords: visits,
      paymentSummary: {
        pendingAmount: pending.reduce((s, p) => s + (p.amount || 0), 0),
        paidAmount: paid.reduce((s, p) => s + (p.amount || 0), 0),
        pendingCount: pending.length,
        paidCount: paid.length
      },
      recentPayments: pays.slice(0, 5)
    }
  }

  Mock.mock(/\/api\/patient\/list/, 'get', (o) => pageResult(store.patients, parseQuery(o.url)))
  Mock.mock(/\/api\/patient\/search/, 'get', (o) => pageResult(store.patients, parseQuery(o.url)))
  Mock.mock(/\/api\/patient\/\d+\/detail/, 'get', (o) => {
    const id = matchId(o.url)
    const patient = store.patients.find((p) => p.id === id)
    return patient ? success(buildPatientDetail(patient)) : { code: 404, message: '患者不存在', data: null }
  })
  Mock.mock(/\/api\/patient\/\d+\/consultation/, 'post', (o) => {
    const id = matchId(o.url)
    const item = store.patients.find((p) => p.id === id)
    if (item) {
      item.status = 1
      item.consultStartTime = new Date().toISOString().slice(0, 19).replace('T', ' ')
    }
    return mutateSuccess('已开始就诊')
  })
  Mock.mock(/\/api\/patient\/\d+\/finish-consultation/, 'post', (o) => {
    const id = matchId(o.url)
    const body = parseBody(o)
    const item = store.patients.find((p) => p.id === id)
    if (item) {
      item.status = 2
      item.diagnosis = body.diagnosis || item.diagnosis
      item.prescription = body.prescription || item.prescription
      item.chiefComplaint = body.chiefComplaint || item.chiefComplaint
    }
    return mutateSuccess('就诊已完成')
  })
  Mock.mock(/\/api\/patient\/\d+\/queue/, 'post', (o) => {
    const id = matchId(o.url)
    const item = store.patients.find((p) => p.id === id)
    if (item) item.status = 0
    return mutateSuccess('已加入候诊队列')
  })
  Mock.mock(/\/api\/patient\/batch-delete/, 'post', (o) => {
    const { ids = [] } = parseBody(o)
    store.patients = store.patients.filter((p) => !ids.includes(p.id))
    return mutateSuccess('批量删除成功')
  })
  Mock.mock(/\/api\/patient\/\d+$/, 'get', (o) => {
    const id = matchId(o.url)
    return success(store.patients.find((p) => p.id === id))
  })
  Mock.mock(/\/api\/patient\/\d+$/, 'put', (o) => {
    const id = matchId(o.url)
    const body = parseBody(o)
    const idx = store.patients.findIndex((p) => p.id === id)
    if (idx >= 0) Object.assign(store.patients[idx], body)
    return mutateSuccess('更新成功')
  })
  Mock.mock(/\/api\/patient\/\d+$/, 'delete', (o) => {
    const id = matchId(o.url)
    store.patients = store.patients.filter((p) => p.id !== id)
    return mutateSuccess('删除成功')
  })
  Mock.mock(/\/api\/patient$/, 'post', (o) => {
    const body = parseBody(o)
    const id = store.patients.length ? Math.max(...store.patients.map((p) => p.id)) + 1 : 1
    const patient = {
      id,
      patientNo: 'P' + String(Date.now()).slice(-8),
      name: body.name,
      gender: body.gender ?? 1,
      age: body.age ?? 30,
      phone: body.phone,
      idCard: body.idCard || '',
      address: body.address || '',
      department: body.department || '内科',
      chiefComplaint: body.chiefComplaint || '',
      status: body.status ?? 0,
      createTime: new Date().toISOString().slice(0, 19).replace('T', ' ')
    }
    store.patients.unshift(patient)
    return success(patient, '添加成功')
  })

  // ---------- 人事 ----------
  Mock.mock(/\/api\/doctor\/list/, 'get', (o) => pageResult(store.doctors, parseQuery(o.url)))
  Mock.mock(/\/api\/doctor\/batch-import/, 'post', (o) => {
    const { list = [] } = parseBody(o)
    list.forEach((item) => {
      const id = store.doctors.length ? Math.max(...store.doctors.map((d) => d.id)) + 1 : 1
      store.doctors.push({
        id,
        doctorNo: 'D' + String(Date.now()).slice(-6) + id,
        name: item.name || item['姓名'],
        gender: item.gender ? Number(item.gender) : (item['性别'] === '女' ? 2 : 1),
        department: item.department || item['科室'] || '内科',
        title: item.title || item['职称'] || '住院医师',
        specialty: item.specialty || item['专业方向'] || '普外',
        phone: item.phone || item['联系电话'] || '',
        status: 1,
        createTime: new Date().toISOString().slice(0, 19).replace('T', ' ')
      })
    })
    return mutateSuccess(`成功导入 ${list.length} 条`)
  })
  registerCrud('/api/doctor', 'doctors')

  Mock.mock(/\/api\/schedule\/calendar/, 'get', (o) => {
    const q = parseQuery(o.url)
    let list = store.schedules.filter((s) => {
      if (q.department && s.department !== q.department) return false
      if (q.doctorName && s.doctorName !== q.doctorName) return false
      if (q.startDate && q.endDate) {
        return s.shiftDate >= q.startDate && s.shiftDate <= q.endDate
      }
      return true
    })
    return success({ list })
  })
  Mock.mock(/\/api\/schedule\/\d+\/affected/, 'get', (o) => {
    const id = matchId(o.url)
    const schedule = store.schedules.find((s) => s.id === id)
    const affected = schedule
      ? store.appointments.filter(
          (a) => a.doctorName === schedule.doctorName && a.appointmentDate === schedule.shiftDate && a.status !== 3
        ).slice(0, 3)
      : []
    return success({ list: affected, count: affected.length })
  })
  Mock.mock(/\/api\/schedule\/\d+\/cancel/, 'post', (o) => {
    const id = matchId(o.url)
    const body = parseBody(o)
    const schedule = store.schedules.find((s) => s.id === id)
    if (schedule) {
      schedule.status = 0
      schedule.shiftType = '停诊'
      if (body.action === 'refund') {
        store.appointments.forEach((a) => {
          if (a.doctorName === schedule.doctorName && a.appointmentDate === schedule.shiftDate) {
            a.status = 3
          }
        })
      }
    }
    return mutateSuccess(body.action === 'refund' ? '已取消排班并处理退号' : '已取消排班并发送通知')
  })
  Mock.mock(/\/api\/schedule\/list/, 'get', (o) => pageResult(store.schedules, parseQuery(o.url)))
  Mock.mock(/\/api\/schedule$/, 'post', (o) => {
    const body = parseBody(o)
    const id = store.schedules.length ? Math.max(...store.schedules.map((s) => s.id)) + 1 : 1
    store.schedules.push({ id, status: 1, ...body })
    return mutateSuccess('添加成功')
  })
  Mock.mock(/\/api\/schedule\/\d+$/, 'put', (o) => {
    const id = matchId(o.url)
    const body = parseBody(o)
    const idx = store.schedules.findIndex((s) => s.id === id)
    if (idx >= 0) Object.assign(store.schedules[idx], body)
    return mutateSuccess('更新成功')
  })
  Mock.mock(/\/api\/schedule\/\d+$/, 'delete', (o) => {
    const id = matchId(o.url)
    store.schedules = store.schedules.filter((s) => s.id !== id)
    return mutateSuccess('删除成功')
  })

  Mock.mock(/\/api\/record\/\d+\/chain/, 'get', (o) => {
    const id = matchId(o.url)
    const record = store.records.find((r) => r.id === id)
    if (!record) return { code: 404, message: '记录不存在', data: null }
    const pays = store.payments.filter((p) => p.patientName === record.patientName).slice(0, 2)
    return success({
      ...record,
      steps: [
        { title: '初诊挂号', time: record.visitTime, content: `${record.department} · 普通号`, status: '已完成', tagType: 'success', type: 'primary' },
        { title: '医生接诊', time: record.visitTime, content: `主治 ${record.doctorName}`, status: '已完成', tagType: 'success', type: 'success' },
        { title: '诊断开方', time: record.visitTime, content: record.diagnosis, status: '已归档', tagType: 'info', type: 'warning' },
        { title: '处方缴费', time: pays[0]?.payTime || record.visitTime, content: pays.length ? `已缴 ¥${pays[0].amount}` : '待缴费', status: pays.length ? '已支付' : '待支付', tagType: pays.length ? 'success' : 'warning', type: 'danger' }
      ],
      revisions: record.revisions || []
    })
  })
  Mock.mock(/\/api\/record\/\d+\/revise/, 'post', (o) => {
    const id = matchId(o.url)
    const body = parseBody(o)
    const record = store.records.find((r) => r.id === id)
    if (record) {
      if (!record.revisions) record.revisions = []
      record.revisions.push({
        time: new Date().toISOString().slice(0, 19).replace('T', ' '),
        reason: body.reason,
        operator: '系统管理员'
      })
      if (body.diagnosis) record.diagnosis = body.diagnosis
      if (body.treatment) record.treatment = body.treatment
      record.revisionStatus = 1
    }
    return mutateSuccess('病历修订已提交审批')
  })
  Mock.mock(/\/api\/record\/\d+\/withdraw/, 'post', (o) => {
    const id = matchId(o.url)
    const body = parseBody(o)
    const record = store.records.find((r) => r.id === id)
    if (record) {
      record.status = 3
      record.revisionStatus = 2
      if (!record.revisions) record.revisions = []
      record.revisions.push({
        time: new Date().toISOString().slice(0, 19).replace('T', ' '),
        reason: `撤回申请：${body.reason}`,
        operator: '系统管理员'
      })
    }
    return mutateSuccess('病历撤回申请已提交')
  })
  Mock.mock(/\/api\/record\/list/, 'get', (o) => pageResult(store.records, parseQuery(o.url)))
  Mock.mock(/\/api\/record$/, 'post', (o) => {
    const body = parseBody(o)
    const id = store.records.length ? Math.max(...store.records.map((r) => r.id)) + 1 : 1
    store.records.unshift({
      id,
      visitTime: new Date().toISOString().slice(0, 19).replace('T', ' '),
      status: 2,
      revisionStatus: 0,
      revisions: [],
      ...body
    })
    return mutateSuccess('添加成功')
  })
  Mock.mock(/\/api\/record\/\d+$/, 'put', (o) => {
    const id = matchId(o.url)
    const body = parseBody(o)
    const idx = store.records.findIndex((r) => r.id === id)
    if (idx >= 0) Object.assign(store.records[idx], body)
    return mutateSuccess('更新成功')
  })
  Mock.mock(/\/api\/record\/\d+$/, 'delete', (o) => {
    const id = matchId(o.url)
    store.records = store.records.filter((r) => r.id !== id)
    return mutateSuccess('删除成功')
  })

  Mock.mock(/\/api\/service\/list/, 'get', (o) => pageResult(store.services, parseQuery(o.url)))
  Mock.mock(/\/api\/service$/, 'post', (o) => {
    const body = parseBody(o)
    const id = store.services.length ? Math.max(...store.services.map((s) => s.id)) + 1 : 1
    store.services.push({ id, status: 0, ...body })
    return mutateSuccess('添加成功')
  })
  Mock.mock(/\/api\/service\/\d+$/, 'put', (o) => {
    const id = matchId(o.url)
    const body = parseBody(o)
    const idx = store.services.findIndex((s) => s.id === id)
    if (idx >= 0) Object.assign(store.services[idx], body)
    return mutateSuccess('更新成功')
  })
  Mock.mock(/\/api\/service\/\d+$/, 'delete', (o) => {
    const id = matchId(o.url)
    store.services = store.services.filter((s) => s.id !== id)
    return mutateSuccess('删除成功')
  })
  Mock.mock(/\/api\/service\/\d+\/toggle/, 'put', (o) => {
    const id = matchId(o.url)
    const body = parseBody(o)
    const item = store.services.find((s) => s.id === id)
    if (item) item.status = body.status
    return mutateSuccess(item?.status === 1 ? '已启用，前台缴费系统已同步' : '已禁用')
  })

  // ---------- 药房 ----------
  Mock.mock(/\/api\/drug\/list/, 'get', (o) => pageResult(store.drugs.filter((d) => !d.archived), parseQuery(o.url)))
  Mock.mock(/\/api\/drug\/\d+\/inventory/, 'get', (o) => {
    const id = matchId(o.url)
    const drug = store.drugs.find((d) => d.id === id)
    const inv = store.inventories.filter((i) => i.drugId === id || i.drugName === drug?.drugName)
    return success({ list: inv, totalStock: inv.reduce((s, i) => s + i.stock, 0) })
  })
  Mock.mock(/\/api\/drug\/\d+\/procurement-trend/, 'get', (o) => {
    const id = matchId(o.url)
    const drug = store.drugs.find((d) => d.id === id)
    const orders = store.procurements.filter((p) => p.drugName === drug?.drugName).slice(0, 6)
    return success({
      list: orders.map((p) => ({ month: p.orderDate?.slice(0, 7), quantity: p.quantity, amount: p.quantity * p.unitPrice }))
    })
  })
  Mock.mock(/\/api\/drug\/\d+\/archive/, 'post', (o) => {
    const id = matchId(o.url)
    const drug = store.drugs.find((d) => d.id === id)
    if (drug) {
      drug.archived = 1
      drug.status = 0
    }
    return mutateSuccess('已归档，历史处方仍可追溯')
  })
  Mock.mock(/\/api\/drug\/\d+$/, 'get', (o) => success(store.drugs.find((d) => d.id === matchId(o.url))))
  Mock.mock(/\/api\/drug$/, 'post', (o) => {
    const body = parseBody(o)
    const id = store.drugs.length ? Math.max(...store.drugs.map((d) => d.id)) + 1 : 1
    store.drugs.push({ id, drugCode: 'DR' + String(Date.now()).slice(-6), archived: 0, status: 1, drugType: '处方药', riskLevel: '普通', ...body })
    return mutateSuccess('添加成功')
  })
  Mock.mock(/\/api\/drug\/\d+$/, 'put', (o) => {
    const id = matchId(o.url)
    const body = parseBody(o)
    const idx = store.drugs.findIndex((d) => d.id === id)
    if (idx >= 0) Object.assign(store.drugs[idx], body)
    return mutateSuccess('更新成功')
  })

  Mock.mock(/\/api\/procurement\/list/, 'get', (o) => pageResult(store.procurements, parseQuery(o.url)))
  Mock.mock(/\/api\/procurement\/\d+\/logistics/, 'get', (o) => {
    const item = store.procurements.find((p) => p.id === matchId(o.url))
    return success({
      logisticsNo: item?.logisticsNo,
      receiptNote: item?.receiptNote,
      supplier: item?.supplier,
      timeline: [
        { time: item?.orderDate, event: '采购下单' },
        { time: item?.orderDate, event: '供应商发货' },
        { time: item?.orderDate, event: '物流在途' }
      ]
    })
  })
  Mock.mock(/\/api\/procurement\/\d+\/advance/, 'post', (o) => {
    const id = matchId(o.url)
    const item = store.procurements.find((p) => p.id === id)
    if (item && item.phase < 3) item.phase += 1
    return mutateSuccess('状态已推进')
  })
  Mock.mock(/\/api\/procurement\/\d+\/stock-in/, 'post', (o) => {
    const id = matchId(o.url)
    const item = store.procurements.find((p) => p.id === id)
    if (item) {
      item.phase = 3
      const inv = store.inventories.find((i) => i.drugName === item.drugName)
      if (inv) inv.stock += item.quantity
      else {
        store.inventories.push({
          id: store.inventories.length + 1,
          drugId: null,
          drugName: item.drugName,
          drugCode: 'DR' + String(Date.now()).slice(-6),
          stock: item.quantity,
          minStock: 50,
          batchNo: 'BN' + Date.now(),
          expiryDate: new Date(Date.now() + 365 * 86400000).toISOString().slice(0, 10),
          warehouse: '中心药房',
          updateTime: new Date().toISOString().slice(0, 19).replace('T', ' ')
        })
      }
    }
    return mutateSuccess('已入库，库存已更新')
  })
  Mock.mock(/\/api\/procurement$/, 'post', (o) => {
    const body = parseBody(o)
    const id = store.procurements.length ? Math.max(...store.procurements.map((p) => p.id)) + 1 : 1
    store.procurements.unshift({
      id,
      orderNo: 'PO' + Date.now(),
      phase: 0,
      urgent: body.urgent || 0,
      logisticsNo: '',
      receiptNote: '',
      ...body
    })
    return mutateSuccess('采购单已创建')
  })
  Mock.mock(/\/api\/procurement\/\d+$/, 'put', (o) => {
    const id = matchId(o.url)
    const item = store.procurements.find((p) => p.id === id)
    if (item?.phase === 3) return fail('已完成订单不可编辑')
    const body = parseBody(o)
    Object.assign(item, body)
    return mutateSuccess('更新成功')
  })

  Mock.mock(/\/api\/inventory\/list/, 'get', (o) => pageResult(store.inventories, parseQuery(o.url)))
  Mock.mock(/\/api\/inventory\/\d+\/adjust/, 'post', (o) => {
    const id = matchId(o.url)
    const body = parseBody(o)
    const inv = store.inventories.find((i) => i.id === id)
    if (!inv) return fail('库存记录不存在')
    if (body.type === 'stocktake') inv.stock = body.quantity ?? inv.stock
    else if (body.type === 'disposal') inv.stock = Math.max(0, inv.stock - (body.quantity || 0))
    else if (body.type === 'transfer') {
      inv.warehouse = body.targetWarehouse || inv.warehouse
      if (body.quantity != null) inv.stock = body.quantity
    }
    inv.updateTime = new Date().toISOString().slice(0, 19).replace('T', ' ')
    return mutateSuccess('库存调整成功')
  })
  Mock.mock(/\/api\/inventory\/\d+\/procurement-request/, 'post', (o) => {
    const id = matchId(o.url)
    const body = parseBody(o)
    const inv = store.inventories.find((i) => i.id === id)
    if (!inv) return fail('库存记录不存在')
    const qty = body.quantity || Math.max(inv.minStock * 2 - inv.stock, 100)
    const pid = store.procurements.length ? Math.max(...store.procurements.map((p) => p.id)) + 1 : 1
    store.procurements.unshift({
      id: pid,
      orderNo: 'PO' + Date.now(),
      drugName: inv.drugName,
      quantity: qty,
      unitPrice: 10,
      supplier: '国药控股',
      phase: 0,
      urgent: inv.stock <= inv.minStock ? 1 : 0,
      orderDate: new Date().toISOString().slice(0, 10),
      logisticsNo: '',
      receiptNote: '由库存告警自动生成'
    })
    return success({ procurementId: pid }, '采购申请单已生成')
  })
  Mock.mock(/\/api\/inventory\/\d+/, 'put', (o) => {
    const id = matchId(o.url)
    const body = parseBody(o)
    const idx = store.inventories.findIndex((i) => i.id === id)
    if (idx >= 0) Object.assign(store.inventories[idx], body)
    return mutateSuccess('更新成功')
  })

  Mock.mock(/\/api\/dispensing\/scan/, 'get', (o) => {
    const q = parseQuery(o.url)
    const item = store.dispensings.find((d) => d.barcode === q.barcode && d.status < 2)
    return item ? success(item) : fail('未找到待配药处方')
  })
  Mock.mock(/\/api\/dispensing\/list/, 'get', (o) => {
    const params = parseQuery(o.url)
    let list = [...store.dispensings]
    const priorityOrder = { 急诊: 0, 门诊: 1, 住院: 2 }
    list.sort((a, b) => {
      const pa = priorityOrder[a.priority] ?? 9
      const pb = priorityOrder[b.priority] ?? 9
      if (pa !== pb) return pa - pb
      return (a.createTime || '').localeCompare(b.createTime || '')
    })
    return pageResult(list, params)
  })
  Mock.mock(/\/api\/dispensing\/\d+\/complete/, 'post', (o) => {
    const id = matchId(o.url)
    const body = parseBody(o)
    const item = store.dispensings.find((d) => d.id === id)
    if (item) {
      item.status = 2
      const inv = store.inventories.find((i) => i.drugName === item.drugName)
      if (inv) inv.stock = Math.max(0, inv.stock - (item.quantity || 0))
    }
    return success({
      notified: true,
      message: body.scanMode ? '扫码配药完成，已扣减库存并推送取药通知' : '配药完成，已扣减库存并推送取药通知'
    }, '配药完成')
  })
  Mock.mock(/\/api\/dispensing$/, 'post', (o) => {
    const body = parseBody(o)
    const id = store.dispensings.length ? Math.max(...store.dispensings.map((d) => d.id)) + 1 : 1
    store.dispensings.unshift({
      id,
      prescriptionNo: 'RX' + Date.now(),
      barcode: 'BC' + Date.now(),
      status: 0,
      priority: body.priority || '门诊',
      createTime: new Date().toISOString().slice(0, 19).replace('T', ' '),
      ...body
    })
    return mutateSuccess('配药单已创建')
  })
  Mock.mock(/\/api\/dispensing\/\d+/, 'put', (o) => {
    const id = matchId(o.url)
    const body = parseBody(o)
    const item = store.dispensings.find((d) => d.id === id)
    if (item) Object.assign(item, body)
    return mutateSuccess('更新成功')
  })

  // ---------- 财务 ----------
  Mock.mock(/\/api\/finance\/list/, 'get', (o) => pageResult(store.finances.filter((f) => !f.archived), parseQuery(o.url)))
  Mock.mock(/\/api\/finance\/\d+\/flows/, 'get', (o) => {
    const id = matchId(o.url)
    const account = store.finances.find((f) => f.id === id)
    const flows = store.incomeExpenses.slice(0, 12).map((ie, i) => ({
      id: i + 1,
      time: ie.recordDate + ' 10:00:00',
      type: ie.type,
      amount: ie.type === '收入' ? ie.amount : -ie.amount,
      balance: (account?.balance || 0) + (ie.type === '收入' ? ie.amount : -ie.amount),
      remark: `${ie.category} · ${ie.sourceModule || '系统'}`
    }))
    return success({ list: flows, accountName: account?.accountName })
  })
  Mock.mock(/\/api\/finance\/\d+\/freeze/, 'post', (o) => {
    const item = store.finances.find((f) => f.id === matchId(o.url))
    if (item) item.status = 0
    return mutateSuccess('账户已冻结')
  })
  Mock.mock(/\/api\/finance\/\d+\/archive/, 'post', (o) => {
    const item = store.finances.find((f) => f.id === matchId(o.url))
    if (item) { item.archived = 1; item.status = 0 }
    return mutateSuccess('账户已注销归档，流水仍可审计')
  })
  Mock.mock(/\/api\/finance$/, 'post', (o) => {
    const body = parseBody(o)
    const id = store.finances.length ? Math.max(...store.finances.map((f) => f.id)) + 1 : 1
    store.finances.push({ id, accountNo: 'AC' + Date.now(), status: 1, archived: 0, ...body })
    return mutateSuccess('添加成功')
  })
  Mock.mock(/\/api\/finance\/\d+$/, 'put', (o) => {
    const idx = store.finances.findIndex((f) => f.id === matchId(o.url))
    if (idx >= 0) Object.assign(store.finances[idx], parseBody(o))
    return mutateSuccess('更新成功')
  })

  Mock.mock(/\/api\/income-expense\/summary/, 'get', (o) => {
    const q = parseQuery(o.url)
    let list = [...store.incomeExpenses]
    if (q.startDate && q.endDate) {
      list = list.filter((i) => i.recordDate >= q.startDate && i.recordDate <= q.endDate)
    }
    const income = list.filter((i) => i.type === '收入').reduce((s, i) => s + i.amount, 0)
    const expense = list.filter((i) => i.type === '支出').reduce((s, i) => s + i.amount, 0)
    return success({
      income, expense, balance: income - expense,
      incomeMom: 8.2, expenseMom: -3.1,
      incomeTrend: [8200, 8500, 8800, 9100, 9300, 9600, income],
      expenseTrend: [5200, 5100, 4900, 5000, 4800, 4700, expense]
    })
  })
  Mock.mock(/\/api\/income-expense\/\d+\/trace/, 'get', (o) => {
    const item = store.incomeExpenses.find((i) => i.id === matchId(o.url))
    return success({
      ...item,
      sourceDoc: {
        module: item?.sourceModule || '手工录入',
        docNo: item?.sourceId || '-',
        path: item?.sourceModule === '挂号缴费' ? '/business/register' : item?.sourceModule === '在线缴费' ? '/business/payment' : '/pharmacy/procurement'
      }
    })
  })
  Mock.mock(/\/api\/income-expense\/list/, 'get', (o) => pageResult(store.incomeExpenses, parseQuery(o.url)))
  Mock.mock(/\/api\/income-expense$/, 'post', (o) => {
    const body = parseBody(o)
    const id = store.incomeExpenses.length ? Math.max(...store.incomeExpenses.map((i) => i.id)) + 1 : 1
    store.incomeExpenses.unshift({
      id, recordDate: new Date().toISOString().slice(0, 10),
      operator: '系统管理员', sourceModule: '手工录入', autoCollected: 0, ...body
    })
    return mutateSuccess('记录成功')
  })

  Mock.mock(/\/api\/reimbursement\/\d+\/detail/, 'get', (o) => {
    const item = store.reimbursements.find((r) => r.id === matchId(o.url))
    if (!item) return fail('记录不存在')
    return success({
      ...item,
      workflow: item.workflow?.length ? item.workflow : [
        { node: '提交申请', time: item.applyDate, operator: item.applicant, status: 'done' },
        { node: '科室审批', time: item.applyDate, operator: '科室主任', status: item.status >= 1 ? 'done' : 'pending' },
        { node: '财务打款', time: '', operator: '财务部', status: item.status === 1 ? 'done' : 'pending' }
      ],
      attachments: item.attachments || ['发票扫描件.pdf']
    })
  })
  Mock.mock(/\/api\/reimbursement\/\d+\/approve/, 'post', (o) => {
    const id = matchId(o.url)
    const body = parseBody(o)
    const item = store.reimbursements.find((r) => r.id === id)
    if (item) {
      item.status = 1
      if (!item.workflow) item.workflow = []
      item.workflow.push({ node: '审批通过', time: new Date().toISOString().slice(0, 19).replace('T', ' '), operator: '系统管理员', remark: body.remark })
      const sid = store.settlements.length ? Math.max(...store.settlements.map((s) => s.id)) + 1 : 1
      store.settlements.unshift({
        id: sid,
        settlementNo: 'ST' + Date.now(),
        patientName: item.applicant,
        totalAmount: item.amount,
        paidAmount: 0,
        insuranceAmount: 0,
        selfPayAmount: item.amount,
        status: 0,
        settlementDate: new Date().toISOString().slice(0, 10),
        dueDate: new Date().toISOString().slice(0, 10),
        overdue: 0,
        feeItems: [{ name: '报销打款', amount: item.amount }],
        remark: '报销审批自动生成'
      })
    }
    return mutateSuccess('审批通过，已生成打款指令并通知申请人')
  })
  Mock.mock(/\/api\/reimbursement\/\d+\/reject/, 'post', (o) => {
    const item = store.reimbursements.find((r) => r.id === matchId(o.url))
    if (item) item.status = 2
    return mutateSuccess('已驳回并通知申请人')
  })
  Mock.mock(/\/api\/reimbursement\/list/, 'get', (o) => pageResult(store.reimbursements, parseQuery(o.url)))
  Mock.mock(/\/api\/reimbursement$/, 'post', (o) => {
    const body = parseBody(o)
    const id = store.reimbursements.length ? Math.max(...store.reimbursements.map((r) => r.id)) + 1 : 1
    store.reimbursements.unshift({
      id, applicant: '张医生', department: '内科', status: 0, applyDate: new Date().toISOString().slice(0, 10),
      invoiceNo: 'INV' + Date.now(), urgent: 0, overdue: 0, attachments: ['发票.pdf'], workflow: [], ...body
    })
    return mutateSuccess('提交成功')
  })

  Mock.mock(/\/api\/settlement\/\d+\/detail/, 'get', (o) => {
    const item = store.settlements.find((s) => s.id === matchId(o.url))
    if (!item) return fail('结算单不存在')
    const unpaid = (item.totalAmount || 0) - (item.paidAmount || 0)
    const feeItems = item.feeItems?.length ? item.feeItems : [
      { name: '挂号费', amount: 50 },
      { name: '检查费', amount: item.totalAmount * 0.4 },
      { name: '药品费', amount: item.totalAmount * 0.5 }
    ]
    return success({
      ...item,
      unpaid,
      feeItems,
      insuranceAmount: item.insuranceAmount ?? item.totalAmount * 0.6,
      selfPayAmount: item.selfPayAmount ?? unpaid,
      paymentChannels: ['医保统筹', '微信', '银行卡']
    })
  })
  Mock.mock(/\/api\/settlement\/\d+\/settle/, 'post', (o) => {
    const id = matchId(o.url)
    const body = parseBody(o)
    const item = store.settlements.find((s) => s.id === id)
    if (item) {
      item.status = 2
      item.paidAmount = item.totalAmount
      item.paymentChannel = body.channel || '微信'
      item.invoiceNo = 'FP' + Date.now()
      item.patientUnlocked = true
    }
    return success({
      invoiceNo: item?.invoiceNo,
      message: '结算完成，电子发票已开具，患者诊疗/取药权限已解封'
    }, '结算完成')
  })
  Mock.mock(/\/api\/settlement\/list/, 'get', (o) => pageResult(store.settlements, parseQuery(o.url)))
  Mock.mock(/\/api\/settlement$/, 'post', (o) => {
    const body = parseBody(o)
    const id = store.settlements.length ? Math.max(...store.settlements.map((s) => s.id)) + 1 : 1
    store.settlements.unshift({
      id,
      settlementNo: 'ST' + Date.now(),
      paidAmount: 0,
      insuranceAmount: 0,
      selfPayAmount: body.totalAmount || 0,
      status: 0,
      settlementDate: new Date().toISOString().slice(0, 10),
      dueDate: new Date(Date.now() + 7 * 86400000).toISOString().slice(0, 10),
      overdue: 0,
      feeItems: [],
      ...body
    })
    return mutateSuccess('创建成功')
  })

  // ---------- 统计 ----------
  Mock.mock(/\/api\/statistics\/overview/, 'get', () => success(STATISTICS_OVERVIEW))
  Mock.mock(/\/api\/statistics\/analysis/, 'get', () => success(STATISTICS_ANALYSIS))
  Mock.mock(/\/api\/statistics\/reports/, 'get', () => success({ list: STATISTICS_REPORTS }))
  Mock.mock(/\/api\/statistics\/decision/, 'get', () => success(STATISTICS_DECISION))

  // ---------- 系统管理 ----------
  Mock.mock(/\/api\/admin\/user\/list/, 'get', (o) => pageResult(store.adminUsers, parseQuery(o.url)))
  Mock.mock(/\/api\/admin\/user\/\d+\/reset-password/, 'post', () => mutateSuccess('密码已重置为123456'))
  Mock.mock(/\/api\/admin\/user$/, 'post', () => mutateSuccess('添加成功'))
  Mock.mock(/\/api\/admin\/user\/\d+/, 'put', () => mutateSuccess('更新成功'))
  Mock.mock(/\/api\/admin\/user\/\d+/, 'delete', () => mutateSuccess('删除成功'))

  Mock.mock(/\/api\/admin\/role\/list/, 'get', (o) => pageResult(store.adminRoles, parseQuery(o.url)))
  registerCrud('/api/admin/role', 'adminRoles')

  Mock.mock(/\/api\/admin\/department\/list/, 'get', (o) => pageResult(store.adminDepartments, parseQuery(o.url)))
  Mock.mock(/\/api\/admin\/department\/tree/, 'get', () => success(store.adminDepartments))
  registerCrud('/api/admin/department', 'adminDepartments')

  Mock.mock(/\/api\/admin\/menu\/tree/, 'get', () => success(store.menuTree))
  Mock.mock(/\/api\/admin\/menu$/, 'post', () => mutateSuccess('添加成功'))
  Mock.mock(/\/api\/admin\/menu\/\d+/, 'put', () => mutateSuccess('更新成功'))
  Mock.mock(/\/api\/admin\/menu\/\d+/, 'delete', () => mutateSuccess('删除成功'))

  Mock.mock(/\/api\/admin\/dict\/list/, 'get', (o) => pageResult(store.adminDicts, parseQuery(o.url)))
  registerCrud('/api/admin/dict', 'adminDicts')

  Mock.mock(/\/api\/admin\/config\/list/, 'get', () => success(store.adminConfigs))
  Mock.mock(/\/api\/admin\/config$/, 'put', () => mutateSuccess('配置已保存'))

  Mock.mock(/\/api\/admin\/log\/operation/, 'get', (o) => pageResult(store.operationLogs, parseQuery(o.url)))
  Mock.mock(/\/api\/admin\/log\/login/, 'get', (o) => pageResult(store.loginLogs, parseQuery(o.url)))

  // ---------- 就医流程（患者端与管理端共用） ----------
  Mock.mock(/\/api\/patient\/info/, 'get', () =>
    success({
      name: '张三',
      phone: '13800138000',
      idCard: '110101199001011234',
      cardNo: 'P2026001286',
      gender: 1,
      allergyHistory: '无',
      chronicDisease: '无',
      address: '北京市朝阳区'
    })
  )

  Mock.mock(/\/api\/register\/list/, 'get', (o) => pageResult(store.registers, parseQuery(o.url)))
  Mock.mock(/\/api\/register\/\d+\/cancel/, 'post', (o) => {
    const id = matchId(o.url)
    const item = store.registers.find((r) => r.id === id)
    if (!item) return fail('挂号记录不存在', 404)
    if (item.status === 3) return fail('该号已退', 409)
    item.status = 3
    return mutateSuccess('已退号')
  })
  Mock.mock(/\/api\/register$/, 'post', (o) => {
    const body = parseBody(o)
    const fee = REGISTER_FEES[body.registerType] || 10
    const id = nextId(store.registers)
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
    store.registers.unshift(record)
    store.payments.unshift({
      id: nextId(store.payments),
      paymentNo: 'JF' + Date.now(),
      patientName: body.patientName,
      itemName: '挂号费',
      itemType: 'register',
      department: body.department,
      doctorName: body.doctorName,
      amount: fee,
      status: 0,
      payMethod: '',
      payTime: '',
      createTime: now,
      dueDate: now.slice(0, 10)
    })
    return success(record, '挂号成功')
  })

  Mock.mock(/\/api\/appointment\/schedule/, 'get', (o) => {
    const q = parseQuery(o.url)
    const doctorId = Number(q.doctorId) || 1
    return success({ dates: buildAppointmentSchedule(doctorId) })
  })
  Mock.mock(/\/api\/appointment\/list/, 'get', (o) => pageResult(store.appointments, parseQuery(o.url)))
  Mock.mock(/\/api\/appointment\/\d+\/confirm/, 'post', (o) => {
    const item = store.appointments.find((a) => a.id === matchId(o.url))
    if (item) item.status = 1
    return mutateSuccess('预约已确认')
  })
  Mock.mock(/\/api\/appointment\/\d+\/cancel/, 'post', (o) => {
    const item = store.appointments.find((a) => a.id === matchId(o.url))
    if (!item) return fail('预约不存在', 404)
    item.status = 3
    return mutateSuccess('预约已取消')
  })
  Mock.mock(/\/api\/appointment$/, 'post', (o) => {
    const body = parseBody(o)
    const id = nextId(store.appointments)
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
    store.appointments.unshift(record)
    return success(record, '预约成功')
  })

  Mock.mock(/\/api\/payment\/summary/, 'get', () => success(pendingPaymentSummary(store.payments)))

  Mock.mock(/\/api\/payment\/batch/, 'post', (o) => {
    const { ids = [], payMethod = '微信' } = parseBody(o)
    const now = new Date().toISOString().slice(0, 19).replace('T', ' ')
    const paid = []
    ids.forEach((rawId) => {
      const item = store.payments.find((p) => p.id === Number(rawId))
      if (item && item.status === 0) {
        item.status = 1
        item.payMethod = payMethod
        item.payTime = now
        item.voucherNo = 'PZ' + Date.now() + item.id
        paid.push(enrichPaymentDetail(item))
      }
    })
    if (!paid.length) return fail('所选账单不可支付', 409)
    return success({ list: paid }, '批量支付成功')
  })

  Mock.mock(/\/api\/payment\/list/, 'get', (o) => {
    const params = parseQuery(o.url)
    let list = [...store.payments]
    if (String(params.status) === '1') {
      list.sort((a, b) => (b.payTime || '').localeCompare(a.payTime || ''))
    } else if (String(params.status) === '0') {
      list.sort((a, b) => (a.dueDate || a.createTime || '').localeCompare(b.dueDate || b.createTime || ''))
    }
    return pageResult(list, params)
  })

  Mock.mock(/\/api\/payment\/\d+\/refund/, 'post', (o) => {
    const item = store.payments.find((p) => p.id === matchId(o.url))
    if (!item) return fail('账单不存在', 404)
    if (item.status !== 1) return fail('仅已支付账单可退款', 409)
    item.status = 2
    return mutateSuccess('退款成功')
  })

  Mock.mock(/\/api\/payment\/\d+$/, 'get', (o) => {
    const item = store.payments.find((p) => p.id === matchId(o.url))
    return item ? success(enrichPaymentDetail(item)) : fail('账单不存在', 404)
  })

  Mock.mock(/\/api\/payment$/, 'post', (o) => {
    const body = parseBody(o)
    const item = store.payments.find((p) => p.id === Number(body.id))
    if (!item) return fail('账单不存在', 404)
    if (item.status !== 0) return fail('该账单已支付', 409)
    item.status = 1
    item.payMethod = body.payMethod || '微信'
    item.payTime = new Date().toISOString().slice(0, 19).replace('T', ' ')
    item.voucherNo = 'PZ' + Date.now()
    return success(enrichPaymentDetail(item), '缴费成功')
  })

  registerCrud('/api/bed', 'beds')

  Mock.mock(/\/api\/notice\/\d+$/, 'get', (o) => {
    const notice = store.notices.find((n) => n.id === matchId(o.url))
    return notice ? success(notice) : fail('公告不存在', 404)
  })
  registerCrud('/api/notice', 'notices', { create: '发布成功' })
}
