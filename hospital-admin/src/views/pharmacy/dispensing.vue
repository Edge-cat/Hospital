<template>
  <PageContainer class="dispensing-page">
    <PageHeader title="配药管理" subtitle="优先级调度 · 医·药·患三方信息流闭合">
      <div class="header-actions">
        <el-input
          v-model="scanCode"
          placeholder="扫描条码 / 输入处方条码"
          clearable
          style="width: 240px"
          @keyup.enter="handleScan"
        >
          <template #prefix><el-icon><FullScreen /></el-icon></template>
        </el-input>
        <el-button @click="handleScan">扫码配药</el-button>
        <el-button type="primary" @click="openDialog()">新建配药单</el-button>
      </div>
    </PageHeader>

    <div class="priority-legend">
      <span v-for="(style, key) in DISPENSE_PRIORITY" :key="key" class="legend-item">
        <i :style="{ background: style.color }" />{{ style.label }}
      </span>
    </div>

    <SearchToolbar :query="query" @search="loadData" @reset="resetQuery">
      <el-form-item label="状态">
        <el-select v-model="query.status" placeholder="全部" clearable style="width: 120px">
          <el-option v-for="s in orderStatusOptions" :key="s.value" :label="s.label" :value="s.value" />
        </el-select>
      </el-form-item>
      <el-form-item label="优先级">
        <el-select v-model="query.priority" placeholder="全部" clearable style="width: 120px">
          <el-option label="急诊" value="急诊" />
          <el-option label="门诊" value="门诊" />
          <el-option label="住院" value="住院" />
        </el-select>
      </el-form-item>
    </SearchToolbar>

    <div v-loading="loading" class="dispense-list">
      <article
        v-for="row in tableData"
        :key="row.id"
        class="dispense-card"
        :style="{ borderLeftColor: DISPENSE_PRIORITY[row.priority]?.color || '#909399' }"
      >
        <div class="dispense-card__priority" :style="{ background: DISPENSE_PRIORITY[row.priority]?.bg, color: DISPENSE_PRIORITY[row.priority]?.color }">
          {{ row.priority }}
        </div>
        <div class="dispense-card__body">
          <div class="dispense-card__head">
            <strong>{{ row.patientName }}</strong>
            <span class="rx-no">{{ row.prescriptionNo }}</span>
            <StatusTag enum-key="orderStatus" :value="row.status" />
          </div>
          <p>{{ row.drugName }} × {{ row.quantity }} · 开方：{{ row.doctorName }}</p>
          <p class="dispense-card__time">{{ row.createTime }} · 条码 {{ row.barcode }}</p>
        </div>
        <div class="dispense-card__action">
          <el-button
            v-if="row.status < 2"
            type="primary"
            round
            :loading="completingId === row.id"
            @click="handleDispense(row, false)"
          >完成配药</el-button>
          <el-tag v-else type="success">已配药 · 已通知</el-tag>
        </div>
      </article>
      <el-empty v-if="!loading && !tableData.length" description="暂无配药单" />
    </div>

    <AppPagination
      v-model:page="query.page"
      v-model:page-size="query.pageSize"
      :total="total"
      @change="loadData"
    />

    <FormDialog v-model:visible="dialogVisible" title="新建配药单" width="520px" @confirm="handleSubmit">
      <el-form ref="formRef" :model="form" :rules="rules" label-width="90px">
        <el-form-item label="优先级" prop="priority">
          <el-radio-group v-model="form.priority">
            <el-radio value="急诊">急诊</el-radio>
            <el-radio value="门诊">门诊</el-radio>
            <el-radio value="住院">住院</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="患者姓名" prop="patientName"><el-input v-model="form.patientName" /></el-form-item>
        <el-form-item label="开方医生" prop="doctorName"><el-input v-model="form.doctorName" /></el-form-item>
        <el-form-item label="药品名称" prop="drugName"><el-input v-model="form.drugName" /></el-form-item>
        <el-form-item label="数量" prop="quantity"><el-input-number v-model="form.quantity" :min="1" style="width: 100%" /></el-form-item>
      </el-form>
    </FormDialog>
  </PageContainer>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox, ElNotification } from 'element-plus'
import { dispensingApi } from '@/api'
import { useDictStore } from '@/stores/dict'
import { DISPENSE_PRIORITY } from '@/constants/pharmacy'

const dictStore = useDictStore()
const orderStatusOptions = computed(() =>
  Object.entries(dictStore.getEnum('orderStatus')).map(([value, { label }]) => ({
    label, value: Number(value)
  }))
)

const loading = ref(false)
const completingId = ref(null)
const scanCode = ref('')
const tableData = ref([])
const total = ref(0)
const dialogVisible = ref(false)
const formRef = ref()
const query = reactive({ status: '', priority: '', page: 1, pageSize: 10 })
const form = reactive({ patientName: '', doctorName: '', drugName: '', quantity: 1, priority: '门诊' })
const rules = {
  patientName: [{ required: true, message: '请输入患者姓名', trigger: 'blur' }],
  drugName: [{ required: true, message: '请输入药品名称', trigger: 'blur' }]
}

function resetQuery() {
  query.status = ''
  query.priority = ''
  query.page = 1
  loadData()
}

async function loadData() {
  loading.value = true
  try {
    const res = await dispensingApi.list(query)
    tableData.value = res.data.list
    total.value = res.data.total
  } finally {
    loading.value = false
  }
}

function openDialog() {
  Object.assign(form, { patientName: '', doctorName: '', drugName: '', quantity: 1, priority: '门诊' })
  dialogVisible.value = true
}

async function handleSubmit() {
  await formRef.value.validate()
  await dispensingApi.create(form)
  ElMessage.success('配药单已创建')
  dialogVisible.value = false
  loadData()
}

async function handleDispense(row, scanMode = false) {
  await ElMessageBox.confirm(
    scanMode ? `扫码确认配药：${row.patientName} · ${row.drugName}` : `确认完成配药：${row.patientName}？`,
    '完成配药',
    { type: 'info' }
  )
  completingId.value = row.id
  try {
    const res = await dispensingApi.complete(row.id, { scanMode })
    ElNotification({
      title: '取药通知已推送',
      message: `${row.patientName} 的 ${row.drugName} 已配好，请前往药房取药。库存已扣减。`,
      type: 'success',
      duration: 5000
    })
    ElMessage.success(res.message || res.data?.message || '配药完成')
    loadData()
  } finally {
    completingId.value = null
  }
}

async function handleScan() {
  if (!scanCode.value.trim()) {
    ElMessage.warning('请输入或扫描条码')
    return
  }
  try {
    const res = await dispensingApi.scan(scanCode.value.trim())
    await handleDispense(res.data, true)
    scanCode.value = ''
  } catch {
    ElMessage.error('未找到匹配的待配药处方')
  }
}

onMounted(loadData)
</script>

<style scoped>
.header-actions { display: flex; gap: 10px; align-items: center; }
.priority-legend {
  display: flex; gap: 16px; margin-bottom: 12px;
  font-size: 12px; color: var(--feishu-text-secondary);
}
.legend-item { display: flex; align-items: center; gap: 6px; }
.legend-item i { width: 12px; height: 12px; border-radius: 2px; display: inline-block; }
.dispense-list { display: flex; flex-direction: column; gap: 10px; margin-bottom: 16px; }
.dispense-card {
  display: flex; align-items: center; gap: 14px;
  padding: 14px 18px; border-radius: 10px;
  background: #fff; border: 1px solid var(--feishu-border-light);
  border-left-width: 4px; box-shadow: 0 2px 8px rgba(0,0,0,0.04);
}
.dispense-card__priority {
  padding: 4px 10px; border-radius: 6px;
  font-size: 12px; font-weight: 700; flex-shrink: 0;
}
.dispense-card__body { flex: 1; min-width: 0; }
.dispense-card__head {
  display: flex; align-items: center; gap: 10px; margin-bottom: 4px;
}
.rx-no { font-size: 12px; color: var(--feishu-text-tertiary); font-family: monospace; }
.dispense-card__body p { font-size: 13px; color: var(--feishu-text-secondary); }
.dispense-card__time { font-size: 12px; margin-top: 2px; }
.dispense-card__action { flex-shrink: 0; }
</style>
