import { formatDate } from './format'

/** 解析后端 treatment 字段（主诉 + 检查 + 处方） */
export function parseTreatment(treatment = '') {
  const text = String(treatment || '').trim()
  if (!text) {
    return { chiefComplaint: '', prescription: '', examItems: '', summary: '' }
  }

  const chiefMatch = text.match(/主诉[：:]\s*([\s\S]*?)(?=\n\s*(?:检查项目|处方)[：:]|$)/)
  const examMatch = text.match(/检查项目[：:]\s*([^\n]+)/)
  const rxMatch = text.match(/处方[：:]\s*\n?([\s\S]*)/)

  const chiefComplaint = chiefMatch?.[1]?.trim() || ''
  const examItems = examMatch?.[1]?.trim() || ''
  const prescription = rxMatch?.[1]?.trim() || ''

  if (!chiefComplaint && !prescription && !examItems) {
    return { chiefComplaint: '', prescription: '', examItems: '', summary: text }
  }

  return { chiefComplaint, prescription, examItems, summary: '' }
}

export function formatVisitTime(value) {
  if (!value) return '-'
  return formatDate(value, 'YYYY-MM-DD HH:mm')
}

function line(label, value) {
  const v = value != null && String(value).trim() ? String(value).trim() : '—'
  return `${label}：${v}`
}

/** 生成完整诊疗报告文本 */
export function buildMedicalReportExport(record, patientInfo = {}) {
  const parsed = parseTreatment(record.treatment)
  const visitTime = formatVisitTime(record.visitTime)
  const exportTime = formatDate(new Date(), 'YYYY-MM-DD HH:mm:ss')

  const sections = [
    '东软云医院 — 门诊诊疗报告',
    '══════════════════════════════════════',
    '',
    '【患者信息】',
    line('姓名', patientInfo.name || record.patientName),
    line('就诊卡号', patientInfo.cardNo),
    line('联系电话', patientInfo.phone),
    '',
    '【就诊信息】',
    line('记录编号', record.id ? `MR${String(record.id).padStart(8, '0')}` : null),
    line('就诊科室', record.department),
    line('主治医生', record.doctorName),
    line('就诊时间', visitTime),
    line('报告状态', '已归档（缴费后解锁）'),
    '',
    '【主诉】',
    parsed.chiefComplaint || parsed.summary || '—',
    '',
    '【诊断结果】',
    record.diagnosis || '—',
    '',
    '【检查项目】',
    parsed.examItems || '—',
    '',
    '【处方 / 医嘱】',
    parsed.prescription || (parsed.summary && !parsed.chiefComplaint ? parsed.summary : '—'),
    '',
    '══════════════════════════════════════',
    `导出时间：${exportTime}`,
    '本报告由东软云医院信息系统生成，仅供患者个人查阅与留存。'
  ]

  return sections.join('\n')
}

/** 小程序导出：写入本地文件并打开 */
export function exportMedicalReport(record, patientInfo = {}) {
  const content = buildMedicalReportExport(record, patientInfo)
  const visitLabel = formatVisitTime(record.visitTime).replace(/[:\s]/g, '-')
  const filename = `诊疗报告_${record.department || '门诊'}_${visitLabel}.txt`

  return new Promise((resolve, reject) => {
    // #ifdef MP-WEIXIN
    const fs = uni.getFileSystemManager()
    const filePath = `${wx.env.USER_DATA_PATH}/${filename}`
    fs.writeFile({
      filePath,
      data: content,
      encoding: 'utf8',
      success: () => {
        uni.openDocument({
          filePath,
          showMenu: true,
          success: () => resolve({ copied: false, opened: true }),
          fail: () => {
            uni.setClipboardData({
              data: content,
              success: () => resolve({ copied: true, opened: false })
            })
          }
        })
      },
      fail: reject
    })
    // #endif
    // #ifndef MP-WEIXIN
    uni.setClipboardData({
      data: content,
      success: () => resolve({ copied: true, opened: false }),
      fail: reject
    })
    // #endif
  })
}
