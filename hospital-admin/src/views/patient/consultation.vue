<template>
  <PageContainer class="consultation-page">
    <PageHeader title="医生开始就诊" subtitle="候诊队列 · 诊疗工作台" />

    <div class="consultation-layout">
      <!-- 左侧候诊队列 -->
      <aside class="consultation-queue">
        <div class="queue-toolbar">
          <el-input v-model="query.keyword" placeholder="姓名/编号" clearable size="small" @keyup.enter="loadData" />
          <el-select v-model="query.status" placeholder="状态" size="small" style="width: 100px" @change="loadData">
            <el-option v-for="s in consultationStatusOptions" :key="s.value" :label="s.label" :value="s.value" />
          </el-select>
          <el-button size="small" type="primary" @click="loadData">刷新</el-button>
        </div>

        <div v-loading="loading" class="queue-list">
          <el-empty v-if="!loading && !tableData.length" description="暂无候诊患者" :image-size="56" />

          <div
            v-for="row in tableData"
            :key="row.id"
            class="queue-item"
            :class="{ active: activePatient?.id === row.id, consulting: row.status === 1 }"
            @click="selectPatient(row)"
          >
            <div class="queue-item__no">{{ row.patientNo?.slice(-4) }}</div>
            <div class="queue-item__info">
              <div class="queue-item__name">
                {{ row.name }}
                <StatusTag enum-key="consultationStatus" :value="row.status" />
              </div>
              <div class="queue-item__meta">{{ row.gender === 1 ? '男' : '女' }} · {{ row.age }}岁 · {{ row.department }}</div>
            </div>
            <el-button
              v-if="row.status === 0"
              type="primary"
              size="small"
              round
              @click.stop="startConsultation(row)"
            >接诊</el-button>
          </div>
        </div>

        <AppPagination
          v-model:page="query.page"
          v-model:page-size="query.pageSize"
          :total="total"
          layout="total, prev, pager, next"
          small
          @change="loadData"
        />
      </aside>

      <!-- 右侧诊疗工作台 -->
      <main v-loading="recordLoading" class="consultation-workspace">
        <template v-if="activePatient">
          <header class="workspace-header">
            <div>
              <h3>{{ activePatient.name }} · {{ activePatient.patientNo }}</h3>
              <p>{{ activePatient.gender === 1 ? '男' : '女' }} · {{ activePatient.age }}岁 · {{ activePatient.department }} · {{ activePatient.phone }}</p>
            </div>
            <div class="workspace-header__actions">
              <el-button
                v-if="canUseAiAssist"
                class="ai-assist-btn"
                :loading="aiLoading"
                @click="onOpenAiAssist"
              >
                AI 临床辅助
              </el-button>
              <el-button v-if="activePatient.status === 1" type="success" :loading="finishing" @click="finishConsultation">
                完成就诊 · 下一位
              </el-button>
            </div>
          </header>

          <el-alert
            v-if="lockLevel !== 'none'"
            :title="lockReason"
            :type="lockLevel === 'full' ? 'success' : 'warning'"
            :closable="false"
            show-icon
            class="workspace-lock-alert"
          />

          <el-tabs v-model="activeTab" class="workspace-tabs" :class="{ 'is-semi-locked': lockLevel === 'semi', 'is-full-locked': lockLevel === 'full' }">
            <el-tab-pane label="病历书写" name="record">
              <el-form label-width="80px" class="workspace-form">
                <el-form-item label="主诉">
                  <el-input
                    v-model="consultForm.chiefComplaint"
                    type="textarea"
                    :rows="2"
                    placeholder="患者主诉..."
                    :readonly="isLocked"
                    :class="{ 'field-locked': isLocked }"
                  />
                </el-form-item>
                <el-form-item label="现病史">
                  <el-input
                    v-model="consultForm.presentIllness"
                    type="textarea"
                    :rows="3"
                    placeholder="现病史描述..."
                    :readonly="isLocked"
                    :class="{ 'field-locked': isLocked }"
                  />
                </el-form-item>
                <el-form-item label="诊断">
                  <el-input
                    v-model="consultForm.diagnosis"
                    type="textarea"
                    :rows="2"
                    placeholder="初步/确定诊断..."
                    :readonly="isLocked"
                    :class="{ 'field-locked': isLocked }"
                  />
                </el-form-item>
              </el-form>
            </el-tab-pane>
            <el-tab-pane label="辅助检查" name="exam">
              <p v-if="!isLocked" class="form-hint">勾选常用检查或添加自定义项目，无需填写价格（由护士确认计价）</p>
              <div class="exam-section">
                <span class="exam-section__label">常用项目</span>
                <el-checkbox-group v-model="consultForm.exams" :disabled="isLocked" class="exam-checkboxes">
                  <el-checkbox v-for="e in examOptions" :key="e" :value="e">{{ e }}</el-checkbox>
                </el-checkbox-group>
              </div>
              <div class="exam-section">
                <span class="exam-section__label">自定义项目</span>
                <div v-for="(exam, idx) in consultForm.customExams" :key="idx" class="custom-exam-row">
                  <el-input
                    v-model="exam.name"
                    placeholder="输入检查项目名称，如 MRI、血糖、病理活检..."
                    :readonly="isLocked"
                    :class="{ 'field-locked': isLocked }"
                    @keyup.enter="addCustomExam"
                  />
                  <el-button v-if="!isLocked && consultForm.customExams.length > 1" link type="danger" @click="removeCustomExam(idx)">删除</el-button>
                </div>
                <el-button v-if="!isLocked" link type="primary" @click="addCustomExam">+ 添加自定义项目</el-button>
              </div>
              <el-input
                v-model="consultForm.examNote"
                type="textarea"
                :rows="3"
                placeholder="检查备注..."
                style="margin-top: 12px"
                :readonly="isLocked"
                :class="{ 'field-locked': isLocked }"
              />
            </el-tab-pane>
            <el-tab-pane label="处方下达" name="rx">
              <p v-if="!isLocked" class="form-hint">填写药品名称与用法，无需填写价格（由护士确认计价）</p>
              <div v-for="(drug, idx) in consultForm.drugs" :key="idx" class="drug-row">
                <el-input v-model="drug.name" placeholder="药品名称" :readonly="isLocked" :class="{ 'field-locked': isLocked }" />
                <el-input-number v-model="drug.qty" :min="1" :max="99" :disabled="isLocked" />
                <el-input v-model="drug.usage" placeholder="用法用量" :readonly="isLocked" :class="{ 'field-locked': isLocked }" />
                <el-button v-if="!isLocked && consultForm.drugs.length > 1" link type="danger" @click="removeDrug(idx)">删除</el-button>
              </div>
              <el-button v-if="!isLocked" link type="primary" @click="addDrug">+ 添加药品</el-button>
            </el-tab-pane>
          </el-tabs>
        </template>

        <div v-else class="workspace-empty">
          <el-empty description="请从左侧候诊队列选择患者开始诊疗" :image-size="120">
            <template #image>
              <el-icon :size="80" color="#dcdfe6"><FirstAidKit /></el-icon>
            </template>
          </el-empty>
        </div>
      </main>
    </div>

    <DoctorAiAssistDialog
      v-model:visible="aiVisible"
      v-model:input="aiInput"
      :patient="aiPatient"
      :messages="aiMessages"
      :loading="aiLoading"
      :disclaimer="aiDisclaimer"
      :demo-mode="aiDemoMode"
      @send="onAiSend"
      @close="closeAiAssist"
    />
  </PageContainer>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { patientApi, serviceApi } from '@/api'
import { useDictStore } from '@/stores/dict'
import DoctorAiAssistDialog from '@/components/DoctorAiAssistDialog.vue'
import { useDoctorAiAssist } from '@/composables/useDoctorAiAssist'

const DEFAULT_EXAMS = ['血常规', '尿常规', 'X光胸片', 'B超', '心电图', 'CT']

const dictStore = useDictStore()
const consultationStatusOptions = computed(() =>
  Object.entries(dictStore.getEnum('consultationStatus')).map(([value, { label }]) => ({
    label,
    value: Number(value)
  }))
)

const loading = ref(false)
const finishing = ref(false)
const tableData = ref([])
const total = ref(0)
const activePatient = ref(null)
const activeTab = ref('record')
const lockLevel = ref('none')
const lockReason = ref('')
const recordLoading = ref(false)
const examOptions = ref([...DEFAULT_EXAMS])

const isLocked = computed(() => lockLevel.value !== 'none')

const canUseAiAssist = computed(
  () => activePatient.value?.status === 1 && lockLevel.value === 'none'
)

const {
  visible: aiVisible,
  patient: aiPatient,
  messages: aiMessages,
  loading: aiLoading,
  input: aiInput,
  disclaimer: aiDisclaimer,
  demoMode: aiDemoMode,
  open: openAiAssist,
  close: closeAiAssist,
  send: sendAiAssist
} = useDoctorAiAssist()

const query = reactive({ keyword: '', status: 0, page: 1, pageSize: 10 })

const consultForm = reactive({
  chiefComplaint: '',
  presentIllness: '',
  diagnosis: '',
  exams: [],
  customExams: [{ name: '' }],
  examNote: '',
  drugs: [{ name: '', qty: 1, usage: '' }]
})

function addDrug() {
  consultForm.drugs.push({ name: '', qty: 1, usage: '' })
}

function addCustomExam() {
  consultForm.customExams.push({ name: '' })
}

function removeCustomExam(idx) {
  consultForm.customExams.splice(idx, 1)
}

function collectExamNames() {
  const preset = consultForm.exams.filter(Boolean)
  const custom = consultForm.customExams.map((e) => e.name?.trim()).filter(Boolean)
  return [...new Set([...preset, ...custom])]
}

function applyExamList(examNames = []) {
  const presetSet = new Set(examOptions.value)
  const checked = []
  const custom = []
  for (const name of examNames) {
    const n = String(name).trim()
    if (!n) continue
    if (presetSet.has(n)) checked.push(n)
    else custom.push({ name: n })
  }
  consultForm.exams = checked
  consultForm.customExams = custom.length ? custom : [{ name: '' }]
}

function removeDrug(idx) {
  consultForm.drugs.splice(idx, 1)
}

function resetConsultForm(data = {}) {
  consultForm.chiefComplaint = data.chiefComplaint || ''
  consultForm.presentIllness = data.presentIllness || ''
  consultForm.diagnosis = data.diagnosis || ''
  applyExamList(Array.isArray(data.exams) ? data.exams : [])
  consultForm.examNote = data.examNote || ''
  if (Array.isArray(data.drugs) && data.drugs.length) {
    consultForm.drugs = data.drugs.map((d) => ({
      name: d.name || '',
      qty: d.qty || 1,
      usage: d.usage || ''
    }))
  } else {
    consultForm.drugs = [{ name: '', qty: 1, usage: '' }]
  }
  lockLevel.value = data.lockLevel || 'none'
  lockReason.value = data.lockReason || ''
}

async function loadConsultationRecord(patientId) {
  recordLoading.value = true
  try {
    const res = await patientApi.getConsultationRecord(patientId)
    resetConsultForm(res.data || {})
  } catch {
    resetConsultForm({})
  } finally {
    recordLoading.value = false
  }
}

async function loadData() {
  loading.value = true
  try {
    const res = await patientApi.list(query)
    tableData.value = res.data.list
    total.value = res.data.total
    if (activePatient.value) {
      const updated = tableData.value.find((p) => p.id === activePatient.value.id)
      if (updated) {
        activePatient.value = updated
        if (updated.status === 2) {
          await loadConsultationRecord(updated.id)
        }
      }
    }
  } finally {
    loading.value = false
  }
}

async function selectPatient(row) {
  activePatient.value = row
  if (row.status === 2) {
    await loadConsultationRecord(row.id)
    return
  }
  resetConsultForm({})
  if (row.status === 0) {
    startConsultation(row)
  }
}

async function startConsultation(row) {
  if (row.status !== 0) {
    activePatient.value = row
    if (row.status === 2) {
      await loadConsultationRecord(row.id)
    } else {
      resetConsultForm({})
    }
    return
  }
  await ElMessageBox.confirm(`确认为患者「${row.name}」开始就诊？`, '开始就诊', { type: 'info' })
  await patientApi.startConsultation(row.id)
  ElMessage.success('已开始就诊')
  activePatient.value = { ...row, status: 1 }
  resetConsultForm({})
  loadData()
}

async function finishConsultation() {
  if (!activePatient.value) return
  finishing.value = true
  try {
    await patientApi.finishConsultation(activePatient.value.id, {
      chiefComplaint: consultForm.chiefComplaint,
      presentIllness: consultForm.presentIllness,
      diagnosis: consultForm.diagnosis,
      examNote: consultForm.examNote,
      exams: collectExamNames(),
      drugs: consultForm.drugs.filter((d) => d.name?.trim())
    })
    ElMessage.success('就诊已完成，医嘱已提交护士确认计价')
    closeAiAssist()
    activePatient.value = null
    query.status = 0
    loadData()
  } finally {
    finishing.value = false
  }
}

async function onOpenAiAssist() {
  if (!activePatient.value) return
  try {
    await openAiAssist(activePatient.value, consultForm, collectExamNames)
  } catch (e) {
    ElMessage.error(e?.message || 'AI 临床辅助失败')
  }
}

async function onAiSend(text) {
  try {
    await sendAiAssist(text)
  } catch (e) {
    ElMessage.error(e?.message || 'AI 回复失败')
  }
}

async function loadExamOptions() {
  try {
    const res = await serviceApi.list({ pageSize: 100, status: 1 })
    const fromApi = (res.data?.list ?? [])
      .filter((s) => !s.category || s.category.includes('检查'))
      .map((s) => s.serviceName)
      .filter(Boolean)
    examOptions.value = [...new Set([...DEFAULT_EXAMS, ...fromApi])]
  } catch {
    examOptions.value = [...DEFAULT_EXAMS]
  }
}

onMounted(() => {
  loadExamOptions()
  loadData()
})
</script>

<style scoped>
.consultation-layout {
  display: flex;
  gap: 16px;
  min-height: calc(100vh - 180px);
}
.consultation-queue {
  width: 340px;
  flex-shrink: 0;
  display: flex;
  flex-direction: column;
  padding: 16px;
  border-radius: 14px;
  background: #fff;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.05);
  border: 1px solid var(--feishu-border-light);
}
.queue-toolbar {
  display: flex;
  gap: 8px;
  margin-bottom: 12px;
}
.queue-list {
  flex: 1;
  overflow-y: auto;
  margin-bottom: 12px;
  max-height: calc(100vh - 280px);
}
.queue-item {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 12px;
  margin-bottom: 8px;
  border-radius: 10px;
  border: 1px solid var(--feishu-border-light);
  cursor: pointer;
  transition: all 0.2s;
}
.queue-item:hover { background: var(--feishu-bg-sub); }
.queue-item.active {
  border-color: var(--feishu-primary);
  background: var(--feishu-primary-bg);
}
.queue-item.consulting { border-left: 3px solid #e6a23c; }
.queue-item__no {
  width: 36px;
  height: 36px;
  border-radius: 8px;
  background: var(--feishu-primary);
  color: #fff;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 12px;
  font-weight: 600;
  flex-shrink: 0;
}
.queue-item__name {
  font-size: 14px;
  font-weight: 600;
  display: flex;
  align-items: center;
  gap: 6px;
}
.queue-item__meta {
  font-size: 12px;
  color: var(--feishu-text-tertiary);
  margin-top: 2px;
}
.consultation-workspace {
  flex: 1;
  padding: 24px;
  border-radius: 14px;
  background: #fff;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.05);
  border: 1px solid var(--feishu-border-light);
}
.workspace-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  padding-bottom: 16px;
  margin-bottom: 16px;
  border-bottom: 1px solid var(--feishu-border-light);
}
.workspace-header h3 { font-size: 18px; margin-bottom: 4px; }
.workspace-header p { font-size: 13px; color: var(--feishu-text-secondary); }
.workspace-header__actions {
  display: flex;
  gap: 10px;
  flex-shrink: 0;
}
.ai-assist-btn {
  background: linear-gradient(135deg, #7c3aed, #a78bfa);
  border: none;
  color: #fff;
}
.ai-assist-btn:hover,
.ai-assist-btn:focus {
  background: linear-gradient(135deg, #6d28d9, #9333ea);
  color: #fff;
}
.workspace-tabs :deep(.el-tabs__content) { padding-top: 16px; }
.workspace-form { max-width: 720px; }
.workspace-lock-alert { margin-bottom: 16px; }
.field-locked :deep(.el-textarea__inner),
.field-locked :deep(.el-input__inner) {
  background: var(--feishu-bg-sub, #f5f7fa);
  color: var(--feishu-text-primary, #303133);
  cursor: default;
}
.is-semi-locked :deep(.el-tabs__content) {
  border: 1px dashed #e6a23c;
  border-radius: 8px;
  padding: 16px;
  background: #fffbf0;
}
.is-full-locked :deep(.el-tabs__content) {
  border: 1px solid #67c23a;
  border-radius: 8px;
  padding: 16px;
  background: #f0f9eb;
}
.workspace-empty {
  display: flex;
  align-items: center;
  justify-content: center;
  min-height: 400px;
}
.form-hint {
  margin: 0 0 12px;
  font-size: 12px;
  color: var(--feishu-text-tertiary);
}
.drug-row {
  display: grid;
  grid-template-columns: 1fr 100px 1fr auto;
  gap: 8px;
  margin-bottom: 8px;
  align-items: center;
}
.exam-section {
  margin-bottom: 16px;
}
.exam-section__label {
  display: block;
  margin-bottom: 8px;
  font-size: 13px;
  font-weight: 600;
  color: var(--feishu-text-secondary);
}
.exam-checkboxes {
  display: flex;
  flex-wrap: wrap;
  gap: 4px 16px;
}
.custom-exam-row {
  display: grid;
  grid-template-columns: 1fr auto;
  gap: 8px;
  margin-bottom: 8px;
  align-items: center;
  max-width: 560px;
}
</style>
