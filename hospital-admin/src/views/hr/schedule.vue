<template>
  <PageContainer class="schedule-page">
    <PageHeader title="排班管理" subtitle="日历排班面板 · 资源调度与患者服务联动">
      <el-button type="primary" @click="openDialog()">新增排班</el-button>
    </PageHeader>

    <div class="schedule-toolbar">
      <el-radio-group v-model="viewMode" size="small">
        <el-radio-button value="week">周视图</el-radio-button>
        <el-radio-button value="month">月视图</el-radio-button>
      </el-radio-group>
      <el-button-group>
        <el-button size="small" @click="shiftPeriod(-1)"><el-icon><ArrowLeft /></el-icon></el-button>
        <el-button size="small" disabled>{{ periodLabel }}</el-button>
        <el-button size="small" @click="shiftPeriod(1)"><el-icon><ArrowRight /></el-icon></el-button>
      </el-button-group>
      <el-radio-group v-model="dimMode" size="small">
        <el-radio-button value="doctor">按医生</el-radio-button>
        <el-radio-button value="dept">按科室</el-radio-button>
      </el-radio-group>
      <DictSelect v-model="filters.department" dict-key="departments" placeholder="全部科室" clearable width="130px" @change="loadCalendar" />
      <el-input v-model="filters.doctorName" placeholder="医生姓名" clearable style="width: 140px" @keyup.enter="loadCalendar" />
      <el-button type="primary" size="small" @click="loadCalendar">查询</el-button>
    </div>

    <div class="shift-legend">
      <span v-for="(style, name) in SHIFT_COLORS" :key="name" class="legend-item">
        <i :style="{ background: style.bg, borderColor: style.border }" />{{ name }}
      </span>
    </div>

    <div v-loading="loading" class="calendar-wrap">
      <!-- 周视图 -->
      <table v-if="viewMode === 'week'" class="calendar-grid">
        <thead>
          <tr>
            <th class="calendar-grid__corner">{{ dimMode === 'doctor' ? '医生' : '科室' }}</th>
            <th v-for="d in weekDays" :key="d.date">{{ d.label }}</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="row in gridRows" :key="row.key">
            <td class="calendar-grid__row-label">{{ row.label }}</td>
            <td v-for="d in weekDays" :key="d.date" class="calendar-grid__cell">
              <div
                v-for="item in getCellItems(row.key, d.date)"
                :key="item.id"
                class="shift-block"
                :style="shiftStyle(item)"
                @click="onShiftClick(item)"
              >
                {{ item.shiftType }}
              </div>
            </td>
          </tr>
        </tbody>
      </table>

      <!-- 月视图 -->
      <div v-else class="month-grid">
        <div v-for="d in monthDays" :key="d.date" class="month-cell" :class="{ 'is-other': d.other }">
          <div class="month-cell__day">{{ d.day }}</div>
          <div
            v-for="item in getDayItems(d.date)"
            :key="item.id"
            class="shift-dot"
            :style="{ background: shiftStyle(item).background, borderColor: shiftStyle(item).borderColor }"
            :title="`${item.doctorName} ${item.shiftType}`"
            @click="onShiftClick(item)"
          />
        </div>
      </div>
    </div>

    <FormDialog v-model:visible="dialogVisible" :title="form.id ? '编辑排班' : '新增排班'" width="480px" @confirm="handleSubmit">
      <el-form ref="formRef" :model="form" :rules="rules" label-width="90px">
        <el-form-item label="医生姓名" prop="doctorName"><el-input v-model="form.doctorName" /></el-form-item>
        <el-form-item label="科室" prop="department">
          <DictSelect v-model="form.department" dict-key="departments" />
        </el-form-item>
        <el-form-item label="排班日期" prop="shiftDate">
          <el-date-picker v-model="form.shiftDate" type="date" value-format="YYYY-MM-DD" style="width: 100%" />
        </el-form-item>
        <el-form-item label="班次" prop="shiftType">
          <DictSelect v-model="form.shiftType" dict-key="shiftTypes" />
        </el-form-item>
        <el-form-item label="备注"><el-input v-model="form.remark" type="textarea" /></el-form-item>
      </el-form>
    </FormDialog>

    <el-dialog v-model="cancelVisible" title="取消排班 · 患者处理" width="480px">
      <p v-if="cancelTarget">
        将取消 <strong>{{ cancelTarget.doctorName }}</strong> 在 <strong>{{ cancelTarget.shiftDate }}</strong> 的排班。
      </p>
      <el-alert v-if="affectedCount" type="warning" :closable="false" style="margin: 12px 0">
        该时段有 <strong>{{ affectedCount }}</strong> 位已预约患者，请选择处理方式：
      </el-alert>
      <el-alert v-else type="info" :closable="false" style="margin: 12px 0">该时段暂无预约患者。</el-alert>
      <el-radio-group v-model="cancelAction" style="display: flex; flex-direction: column; gap: 10px">
        <el-radio value="notify">发送改期/停诊通知（保留预约记录）</el-radio>
        <el-radio value="refund" :disabled="!affectedCount">协助退号并退款</el-radio>
      </el-radio-group>
      <template #footer>
        <el-button @click="cancelVisible = false">返回</el-button>
        <el-button type="danger" :loading="canceling" @click="confirmCancel">确认取消排班</el-button>
      </template>
    </el-dialog>
  </PageContainer>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'
import { scheduleApi } from '@/api'
import { SHIFT_COLORS } from '@/constants/hr'

const route = useRoute()
const loading = ref(false)
const viewMode = ref('week')
const dimMode = ref('doctor')
const anchorDate = ref(new Date())
const calendarData = ref([])
const dialogVisible = ref(false)
const formRef = ref()
const cancelVisible = ref(false)
const cancelTarget = ref(null)
const affectedCount = ref(0)
const cancelAction = ref('notify')
const canceling = ref(false)

const filters = reactive({
  department: route.query.department || '',
  doctorName: route.query.doctorName || ''
})

const form = reactive({ id: null, doctorName: '', department: '', shiftDate: '', shiftType: '早班', remark: '', status: 1 })
const rules = {
  doctorName: [{ required: true, message: '请输入医生姓名', trigger: 'blur' }],
  shiftDate: [{ required: true, message: '请选择日期', trigger: 'change' }]
}

function fmt(d) {
  return d.toISOString().slice(0, 10)
}

function startOfWeek(d) {
  const x = new Date(d)
  const day = x.getDay() || 7
  x.setDate(x.getDate() - day + 1)
  return x
}

const weekDays = computed(() => {
  const start = startOfWeek(anchorDate.value)
  const labels = ['周一', '周二', '周三', '周四', '周五', '周六', '周日']
  return labels.map((label, i) => {
    const d = new Date(start)
    d.setDate(d.getDate() + i)
    return { label: `${label} ${d.getMonth() + 1}/${d.getDate()}`, date: fmt(d) }
  })
})

const periodLabel = computed(() => {
  if (viewMode.value === 'week') {
    const s = weekDays.value[0]?.date
    const e = weekDays.value[6]?.date
    return `${s} ~ ${e}`
  }
  const d = anchorDate.value
  return `${d.getFullYear()}年${d.getMonth() + 1}月`
})

const dateRange = computed(() => {
  if (viewMode.value === 'week') {
    return { startDate: weekDays.value[0]?.date, endDate: weekDays.value[6]?.date }
  }
  const d = new Date(anchorDate.value.getFullYear(), anchorDate.value.getMonth(), 1)
  const end = new Date(anchorDate.value.getFullYear(), anchorDate.value.getMonth() + 1, 0)
  return { startDate: fmt(d), endDate: fmt(end) }
})

const monthDays = computed(() => {
  const y = anchorDate.value.getFullYear()
  const m = anchorDate.value.getMonth()
  const first = new Date(y, m, 1)
  const last = new Date(y, m + 1, 0)
  const startPad = (first.getDay() || 7) - 1
  const cells = []
  for (let i = startPad; i > 0; i--) {
    const d = new Date(y, m, 1 - i)
    cells.push({ date: fmt(d), day: d.getDate(), other: true })
  }
  for (let i = 1; i <= last.getDate(); i++) {
    cells.push({ date: fmt(new Date(y, m, i)), day: i, other: false })
  }
  while (cells.length % 7 !== 0) {
    const d = new Date(y, m + 1, cells.length - last.getDate() - startPad + 1)
    cells.push({ date: fmt(d), day: d.getDate(), other: true })
  }
  return cells
})

const gridRows = computed(() => {
  const key = dimMode.value === 'doctor' ? 'doctorName' : 'department'
  const set = [...new Set(calendarData.value.map((s) => s[key]).filter(Boolean))]
  return set.map((label) => ({ key: label, label }))
})

function shiftStyle(item) {
  const type = item.status === 0 ? '停诊' : item.shiftType
  const s = SHIFT_COLORS[type] || SHIFT_COLORS['早班']
  return { background: s.bg, borderColor: s.border, color: s.text }
}

function getCellItems(rowKey, date) {
  const key = dimMode.value === 'doctor' ? 'doctorName' : 'department'
  return calendarData.value.filter((s) => s[key] === rowKey && s.shiftDate === date)
}

function getDayItems(date) {
  return calendarData.value.filter((s) => s.shiftDate === date)
}

function shiftPeriod(dir) {
  const d = new Date(anchorDate.value)
  if (viewMode.value === 'week') d.setDate(d.getDate() + dir * 7)
  else d.setMonth(d.getMonth() + dir)
  anchorDate.value = d
  loadCalendar()
}

async function loadCalendar() {
  loading.value = true
  try {
    const res = await scheduleApi.calendar({
      ...filters,
      ...dateRange.value
    })
    calendarData.value = res.data.list || []
  } finally {
    loading.value = false
  }
}

function openDialog(row) {
  Object.assign(form, row || {
    id: null,
    doctorName: filters.doctorName || '',
    department: filters.department || '内科',
    shiftDate: '',
    shiftType: '早班',
    remark: '',
    status: 1
  })
  dialogVisible.value = true
}

async function handleSubmit() {
  await formRef.value.validate()
  if (form.id) await scheduleApi.update(form.id, form)
  else await scheduleApi.create({ ...form, status: 1 })
  ElMessage.success('操作成功')
  dialogVisible.value = false
  loadCalendar()
}

async function onShiftClick(item) {
  if (item.status === 0) {
    openDialog(item)
    return
  }
  cancelTarget.value = item
  cancelAction.value = 'notify'
  const res = await scheduleApi.getAffected(item.id)
  affectedCount.value = res.data.count || 0
  cancelVisible.value = true
}

async function confirmCancel() {
  if (!cancelTarget.value) return
  canceling.value = true
  try {
    await scheduleApi.cancel(cancelTarget.value.id, { action: cancelAction.value })
    ElMessage.success('排班已取消')
    cancelVisible.value = false
    loadCalendar()
  } finally {
    canceling.value = false
  }
}

onMounted(loadCalendar)
</script>

<style scoped>
.schedule-toolbar {
  display: flex;
  flex-wrap: wrap;
  align-items: center;
  gap: 12px;
  margin-bottom: 12px;
  padding: 16px;
  border-radius: 12px;
  background: #fff;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.04);
}
.shift-legend {
  display: flex;
  gap: 16px;
  margin-bottom: 12px;
  font-size: 12px;
  color: var(--feishu-text-secondary);
}
.legend-item {
  display: flex;
  align-items: center;
  gap: 6px;
}
.legend-item i {
  width: 14px;
  height: 14px;
  border-radius: 3px;
  border: 1px solid;
  display: inline-block;
}
.calendar-wrap {
  padding: 16px;
  border-radius: 12px;
  background: #fff;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.05);
  overflow-x: auto;
}
.calendar-grid {
  width: 100%;
  border-collapse: collapse;
  min-width: 800px;
}
.calendar-grid th,
.calendar-grid td {
  border: 1px solid var(--feishu-border-light);
  padding: 8px;
  vertical-align: top;
}
.calendar-grid th {
  background: var(--feishu-bg-sub);
  font-size: 13px;
  font-weight: 600;
}
.calendar-grid__corner { width: 100px; }
.calendar-grid__row-label {
  font-weight: 500;
  font-size: 13px;
  white-space: nowrap;
}
.calendar-grid__cell { min-height: 64px; min-width: 100px; }
.shift-block {
  padding: 4px 8px;
  margin-bottom: 4px;
  border-radius: 6px;
  border: 1px solid;
  font-size: 12px;
  font-weight: 500;
  cursor: pointer;
  text-align: center;
  transition: transform 0.15s;
}
.shift-block:hover { transform: scale(1.03); }
.month-grid {
  display: grid;
  grid-template-columns: repeat(7, 1fr);
  gap: 4px;
}
.month-cell {
  min-height: 72px;
  padding: 6px;
  border: 1px solid var(--feishu-border-light);
  border-radius: 6px;
}
.month-cell.is-other { opacity: 0.45; }
.month-cell__day {
  font-size: 12px;
  font-weight: 600;
  margin-bottom: 4px;
}
.shift-dot {
  width: 8px;
  height: 8px;
  border-radius: 50%;
  border: 1px solid;
  display: inline-block;
  margin: 2px;
  cursor: pointer;
}
</style>
