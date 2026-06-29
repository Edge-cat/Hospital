/**
 * 统一 API 响应结构（模拟 Spring Boot 后端）
 * { code: number, message: string, data: T }
 */

export function success(data = null, message = 'success') {
  return { code: 200, message, data }
}

export function fail(message = 'error', code = 400, data = null) {
  return { code, message, data }
}

export function pageResult(list, params = {}, message = 'success') {
  const page = Number(params.page) || 1
  const pageSize = Number(params.pageSize) || 10
  let filtered = [...list]

  const keyword = params.keyword || params.name || params.patientName
  if (keyword) {
    filtered = filtered.filter((item) =>
      Object.values(item).some((v) => String(v).includes(keyword))
    )
  }
  if (params.patientNo) {
    filtered = filtered.filter((item) => String(item.patientNo || '').includes(params.patientNo))
  }
  if (params.department) {
    filtered = filtered.filter((item) => item.department === params.department)
  }
  if (params.status !== undefined && params.status !== '') {
    filtered = filtered.filter((item) => String(item.status) === String(params.status))
  }
  if (params.dictType) {
    filtered = filtered.filter((item) => item.dictType === params.dictType)
  }
  if (params.category) {
    filtered = filtered.filter((item) => item.category === params.category)
  }
  if (params.doctorName) {
    filtered = filtered.filter((item) => item.doctorName === params.doctorName)
  }
  if (params.startDate && params.endDate) {
    filtered = filtered.filter((item) => {
      const d =
        item.recordDate ||
        item.shiftDate ||
        item.visitTime?.slice(0, 10) ||
        item.settlementDate ||
        item.payTime?.slice(0, 10) ||
        item.createTime?.slice(0, 10)
      return d && d >= params.startDate && d <= params.endDate
    })
  } else if (params.startDate) {
    filtered = filtered.filter((item) => {
      const t = (item.payTime || item.createTime || '').slice(0, 10)
      return t && t >= params.startDate
    })
  } else if (params.endDate) {
    filtered = filtered.filter((item) => {
      const t = (item.payTime || item.createTime || item.publishTime || '').slice(0, 10)
      return t && t <= params.endDate
    })
  }
  if (params.phase !== undefined && params.phase !== '') {
    filtered = filtered.filter((item) => String(item.phase) === String(params.phase))
  }
  if (params.urgent !== undefined && params.urgent !== '') {
    filtered = filtered.filter((item) => String(item.urgent) === String(params.urgent))
  }
  if (params.priority) {
    filtered = filtered.filter((item) => item.priority === params.priority)
  }
  if (params.archived !== undefined && params.archived !== '') {
    filtered = filtered.filter((item) => String(item.archived) === String(params.archived))
  }
  if (params.expiryAlert === '1') {
    const today = new Date().toISOString().slice(0, 10)
    const soon = new Date()
    soon.setDate(soon.getDate() + 30)
    const soonStr = soon.toISOString().slice(0, 10)
    filtered = filtered.filter((item) => item.expiryDate && (item.expiryDate < today || item.expiryDate <= soonStr))
  }
  if (params.drugName) {
    filtered = filtered.filter((item) => item.drugName === params.drugName)
  }
  if (params.bank) {
    filtered = filtered.filter((item) => item.bank === params.bank)
  }
  if (params.settlementNo) {
    filtered = filtered.filter((item) => String(item.settlementNo || '').includes(params.settlementNo))
  }
  if (params.patientName && !params.keyword) {
    filtered = filtered.filter((item) => String(item.patientName || '').includes(params.patientName))
  }
  if (params.reimburseStatus !== undefined && params.reimburseStatus !== '') {
    filtered = filtered.filter((item) => String(item.status) === String(params.reimburseStatus))
  }

  const total = filtered.length
  const start = (page - 1) * pageSize

  return success(
    {
      list: filtered.slice(start, start + pageSize),
      total,
      page,
      pageSize
    },
    message
  )
}

export function mutateSuccess(message = 'success') {
  return success(null, message)
}
