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
      <main class="consultation-workspace">
        <template v-if="activePatient">
          <header class="workspace-header">
            <div>
              <h3>{{ activePatient.name }} · {{ activePatient.patientNo }}</h3>
              <p>{{ activePatient.gender === 1 ? '男' : '女' }} · {{ activePatient.age }}岁 · {{ activePatient.department }} · {{ activePatient.phone }}</p>
            </div>
            <div class="workspace-header__actions">
              <el-button v-if="activePatient.status === 1" type="success" :loading="finishing" @click="finishConsultation">
                完成就诊 · 下一位
              </el-button>
            </div>
          </header>

          <el-tabs v-model="activeTab" class="workspace-tabs">
            <el-tab-pane label="病历书写" name="record">
              <el-form label-width="80px" class="workspace-form">
                <el-form-item label="主诉">
                  <el-input v-model="consultForm.chiefComplaint" type="textarea" :rows="2" placeholder="患者主诉..." />
                </el-form-item>
                <el-form-item label="现病史">
                  <el-input v-model="consultForm.presentIllness" type="textarea" :rows="3" placeholder="现病史描述..." />
                </el-form-item>
                <el-form-item label="诊断">
                  <el-input v-model="consultForm.diagnosis" type="textarea" :rows="2" placeholder="初步/确定诊断..." />
                </el-form-item>
              </el-form>
            </el-tab-pane>
            <el-tab-pane label="辅助检查" name="exam">
              <el-checkbox-group v-model="consultForm.exams">
                <el-checkbox v-for="e in examOptions" :key="e" :value="e">{{ e }}</el-checkbox>
              </el-checkbox-group>
              <el-input v-model="consultForm.examNote" type="textarea" :rows="3" placeholder="检查备注..." style="margin-top: 12px" />
            </el-tab-pane>
            <el-tab-pane label="处方下达" name="rx">
              <el-input v-model="consultForm.prescription" type="textarea" :rows="6" placeholder="Rp.&#10;药品名称 规格 用法用量..." />
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
  </PageContainer>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { patientApi } from '@/api'
import { useDictStore } from '@/stores/dict'

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
const examOptions = ['血常规', '尿常规', 'X光胸片', 'B超', '心电图', 'CT']

const query = reactive({ keyword: '', status: 0, page: 1, pageSize: 10 })

const consultForm = reactive({
  chiefComplaint: '',
  presentIllness: '',
  diagnosis: '',
  exams: [],
  examNote: '',
  prescription: ''
})

function resetConsultForm(patient) {
  consultForm.chiefComplaint = patient?.chiefComplaint || ''
  consultForm.presentIllness = ''
  consultForm.diagnosis = patient?.diagnosis || ''
  consultForm.exams = []
  consultForm.examNote = ''
  consultForm.prescription = patient?.prescription || ''
}

async function loadData() {
  loading.value = true
  try {
    const res = await patientApi.list(query)
    tableData.value = res.data.list
    total.value = res.data.total
    if (activePatient.value) {
      const updated = tableData.value.find((p) => p.id === activePatient.value.id)
      if (updated) activePatient.value = updated
    }
  } finally {
    loading.value = false
  }
}

function selectPatient(row) {
  activePatient.value = row
  resetConsultForm(row)
  if (row.status === 0) startConsultation(row)
}

async function startConsultation(row) {
  if (row.status !== 0) {
    activePatient.value = row
    resetConsultForm(row)
    return
  }
  await ElMessageBox.confirm(`确认为患者「${row.name}」开始就诊？`, '开始就诊', { type: 'info' })
  await patientApi.startConsultation(row.id)
  ElMessage.success('已开始就诊')
  activePatient.value = { ...row, status: 1 }
  resetConsultForm(activePatient.value)
  loadData()
}

async function finishConsultation() {
  if (!activePatient.value) return
  finishing.value = true
  try {
    await patientApi.finishConsultation(activePatient.value.id, {
      chiefComplaint: consultForm.chiefComplaint,
      diagnosis: consultForm.diagnosis,
      prescription: consultForm.prescription
    })
    ElMessage.success('就诊已完成')
    activePatient.value = null
    query.status = 0
    loadData()
  } finally {
    finishing.value = false
  }
}

onMounted(loadData)
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
.workspace-tabs :deep(.el-tabs__content) { padding-top: 16px; }
.workspace-form { max-width: 720px; }
.workspace-empty {
  display: flex;
  align-items: center;
  justify-content: center;
  min-height: 400px;
}
</style>
