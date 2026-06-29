<template>
  <ListPage
    title="诊疗记录管理"
    subtitle="医疗数据归档中心 · 严谨修订与全链路溯源"
    :query="query"
    :loading="loading"
    :data="tableData"
    :total="total"
    @search="loadData"
    @reset="resetQuery"
    @page-change="loadData"
  >
    <template #actions>
      <el-button type="primary" @click="openDialog()">新增记录</el-button>
    </template>

    <template #filters>
      <el-form-item label="关键词">
        <el-input v-model="query.keyword" placeholder="患者/医生姓名" clearable />
      </el-form-item>
      <el-form-item v-if="query.doctorName" label="主治医生">
        <el-tag closable @close="clearDoctorFilter">{{ query.doctorName }}</el-tag>
      </el-form-item>
    </template>

    <el-table-column prop="patientName" label="患者" width="100" />
    <el-table-column prop="doctorName" label="主治医生" width="100" />
    <el-table-column prop="department" label="科室" width="90" />
    <el-table-column label="诊断结果" min-width="160">
      <template #default="{ row }">
        <el-tooltip :content="row.diagnosis" placement="top" :show-after="300">
          <span class="text-ellipsis">{{ row.diagnosis }}</span>
        </el-tooltip>
      </template>
    </el-table-column>
    <el-table-column label="治疗方案" min-width="160">
      <template #default="{ row }">
        <el-tooltip :content="row.treatment" placement="top" :show-after="300">
          <span class="text-ellipsis">{{ row.treatment }}</span>
        </el-tooltip>
      </template>
    </el-table-column>
    <el-table-column prop="visitTime" label="就诊时间" width="170" align="right" class-name="col-mono" />
    <el-table-column prop="status" label="状态" width="90" align="center">
      <template #default="{ row }">
        <StatusTag v-if="row.status !== 3" enum-key="consultationStatus" :value="row.status" />
        <el-tag v-else type="info" size="small">已撤回</el-tag>
      </template>
    </el-table-column>
    <el-table-column label="操作" width="200" fixed="right" align="center">
      <template #default="{ row }">
        <el-button link type="primary" @click="openChain(row)">全链路</el-button>
        <el-button v-if="row.status !== 3" link type="warning" @click="openRevise(row)">病历修订</el-button>
        <el-button v-if="row.status !== 3" link type="danger" @click="openWithdraw(row)">撤回</el-button>
      </template>
    </el-table-column>

    <template #extra>
      <FormDialog v-model:visible="dialogVisible" title="新增诊疗记录" width="560px" @confirm="handleSubmit">
        <el-form ref="formRef" :model="form" :rules="rules" label-width="90px">
          <el-form-item label="患者姓名" prop="patientName"><el-input v-model="form.patientName" /></el-form-item>
          <el-form-item label="主治医生" prop="doctorName"><el-input v-model="form.doctorName" /></el-form-item>
          <el-form-item label="科室" prop="department">
            <DictSelect v-model="form.department" dict-key="departments" />
          </el-form-item>
          <el-form-item label="诊断结果" prop="diagnosis"><el-input v-model="form.diagnosis" type="textarea" /></el-form-item>
          <el-form-item label="治疗方案" prop="treatment"><el-input v-model="form.treatment" type="textarea" /></el-form-item>
        </el-form>
      </FormDialog>

      <el-dialog v-model="reviseVisible" title="病历修订（需审批）" width="500px">
        <el-alert type="warning" :closable="false" show-icon style="margin-bottom: 16px">
          医疗数据修订将留痕存档，提交后需科室主任审批生效。
        </el-alert>
        <el-form :model="reviseForm" label-width="90px">
          <el-form-item label="修订原因" required>
            <el-input v-model="reviseForm.reason" type="textarea" :rows="2" placeholder="请说明修订原因" />
          </el-form-item>
          <el-form-item label="诊断结果">
            <el-input v-model="reviseForm.diagnosis" type="textarea" />
          </el-form-item>
          <el-form-item label="治疗方案">
            <el-input v-model="reviseForm.treatment" type="textarea" />
          </el-form-item>
        </el-form>
        <template #footer>
          <el-button @click="reviseVisible = false">取消</el-button>
          <el-button type="primary" :loading="submitting" @click="submitRevise">提交审批</el-button>
        </template>
      </el-dialog>

      <el-dialog v-model="withdrawVisible" title="病历撤回申请" width="440px">
        <el-alert type="error" :closable="false" show-icon style="margin-bottom: 16px">
          撤回操作不可逆，需填写原因并经权限审批。
        </el-alert>
        <el-input v-model="withdrawReason" type="textarea" :rows="3" placeholder="撤回原因（必填）" />
        <template #footer>
          <el-button @click="withdrawVisible = false">取消</el-button>
          <el-button type="danger" :loading="submitting" @click="submitWithdraw">确认撤回</el-button>
        </template>
      </el-dialog>

      <RecordChainDrawer v-model:visible="chainVisible" :record-id="chainRecordId" />
    </template>
  </ListPage>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'
import { recordApi } from '@/api'
import RecordChainDrawer from '@/components/RecordChainDrawer.vue'

const route = useRoute()
const loading = ref(false)
const submitting = ref(false)
const tableData = ref([])
const total = ref(0)
const dialogVisible = ref(false)
const reviseVisible = ref(false)
const withdrawVisible = ref(false)
const chainVisible = ref(false)
const chainRecordId = ref(null)
const formRef = ref()
const reviseTarget = ref(null)
const withdrawTarget = ref(null)
const withdrawReason = ref('')

const query = reactive({
  keyword: '',
  doctorName: route.query.doctorName || '',
  page: 1,
  pageSize: 10
})

const form = reactive({ id: null, patientName: '', doctorName: '', department: '', diagnosis: '', treatment: '' })
const reviseForm = reactive({ reason: '', diagnosis: '', treatment: '' })
const rules = {
  patientName: [{ required: true, message: '请输入患者姓名', trigger: 'blur' }],
  diagnosis: [{ required: true, message: '请输入诊断结果', trigger: 'blur' }]
}

function clearDoctorFilter() {
  query.doctorName = ''
  loadData()
}

function resetQuery() {
  query.keyword = ''
  query.doctorName = ''
  query.page = 1
  loadData()
}

async function loadData() {
  loading.value = true
  try {
    const res = await recordApi.list(query)
    tableData.value = res.data.list
    total.value = res.data.total
  } finally {
    loading.value = false
  }
}

function openDialog() {
  Object.assign(form, { id: null, patientName: '', doctorName: query.doctorName || '', department: '内科', diagnosis: '', treatment: '' })
  dialogVisible.value = true
}

async function handleSubmit() {
  await formRef.value.validate()
  await recordApi.create(form)
  ElMessage.success('记录已创建')
  dialogVisible.value = false
  loadData()
}

function openChain(row) {
  chainRecordId.value = row.id
  chainVisible.value = true
}

function openRevise(row) {
  reviseTarget.value = row
  reviseForm.reason = ''
  reviseForm.diagnosis = row.diagnosis
  reviseForm.treatment = row.treatment
  reviseVisible.value = true
}

function openWithdraw(row) {
  withdrawTarget.value = row
  withdrawReason.value = ''
  withdrawVisible.value = true
}

async function submitRevise() {
  if (!reviseForm.reason.trim()) {
    ElMessage.warning('请填写修订原因')
    return
  }
  submitting.value = true
  try {
    await recordApi.revise(reviseTarget.value.id, { ...reviseForm })
    ElMessage.success('修订申请已提交审批')
    reviseVisible.value = false
    loadData()
  } finally {
    submitting.value = false
  }
}

async function submitWithdraw() {
  if (!withdrawReason.value.trim()) {
    ElMessage.warning('请填写撤回原因')
    return
  }
  submitting.value = true
  try {
    await recordApi.withdraw(withdrawTarget.value.id, { reason: withdrawReason.value })
    ElMessage.success('撤回申请已提交')
    withdrawVisible.value = false
    loadData()
  } finally {
    submitting.value = false
  }
}

onMounted(loadData)
</script>

<style scoped>
.text-ellipsis {
  display: block;
  max-width: 100%;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}
:deep(.col-mono) { font-variant-numeric: tabular-nums; }
</style>
