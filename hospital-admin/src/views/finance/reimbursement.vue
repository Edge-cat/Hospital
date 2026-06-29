<template>
  <ListPage
    title="报销管理"
    subtitle="凭证上传-多级审批-自动清算-节点反馈"
    :query="query"
    :loading="loading"
    :data="tableData"
    :total="total"
    @search="loadData"
    @reset="resetQuery"
    @page-change="loadData"
  >
    <template #actions>
      <el-button type="primary" @click="openDialog()">提交报销</el-button>
    </template>
    <template #filters>
      <el-form-item label="状态">
        <el-select v-model="query.reimburseStatus" placeholder="全部" clearable style="width: 120px">
          <el-option label="待审批" :value="0" />
          <el-option label="已通过" :value="1" />
          <el-option label="已驳回" :value="2" />
        </el-select>
      </el-form-item>
    </template>

    <el-table-column label="申请人" width="120">
      <template #default="{ row }">
        <div class="applicant-cell">
          <span>{{ row.applicant }}</span>
          <el-tag v-if="row.overdue" type="danger" size="small" effect="dark">超期</el-tag>
          <el-tag v-else-if="row.urgent" type="warning" size="small">加急</el-tag>
        </div>
      </template>
    </el-table-column>
    <el-table-column prop="department" label="部门" width="100" />
    <el-table-column label="报销金额" width="120" align="right">
      <template #default="{ row }"><span class="price-cell">¥{{ row.amount?.toFixed(2) }}</span></template>
    </el-table-column>
    <el-table-column prop="reason" label="报销事由" min-width="180" show-overflow-tooltip />
    <el-table-column prop="applyDate" label="申请日期" width="110" class-name="col-mono" />
    <el-table-column label="状态" width="100" align="center">
      <template #default="{ row }">
        <StatusTag enum-key="reimburseStatus" :value="Number(row.status)" />
      </template>
    </el-table-column>
    <el-table-column label="操作" width="120" fixed="right" align="center">
      <template #default="{ row }">
        <el-button v-if="Number(row.status) === 0" type="primary" size="small" round @click="openApproval(row)">审批</el-button>
        <span v-else class="status-done">{{ statusLabel(row.status) }}</span>
      </template>
    </el-table-column>

    <template #extra>
      <FormDialog v-model:visible="dialogVisible" title="提交报销申请" width="480px" confirm-text="提交" @confirm="handleSubmit">
        <el-form ref="formRef" :model="form" :rules="rules" label-width="90px">
          <el-form-item label="报销金额" prop="amount"><el-input-number v-model="form.amount" :min="0" :precision="2" style="width: 100%" /></el-form-item>
          <el-form-item label="报销事由" prop="reason"><el-input v-model="form.reason" type="textarea" :rows="3" /></el-form-item>
        </el-form>
      </FormDialog>

      <el-dialog v-model="approvalVisible" title="全景审批台" width="560px" destroy-on-close>
        <div v-loading="approvalLoading">
          <template v-if="approvalDetail">
            <el-descriptions :column="2" border size="small">
              <el-descriptions-item label="申请人">{{ approvalDetail.applicant }}</el-descriptions-item>
              <el-descriptions-item label="部门">{{ approvalDetail.department }}</el-descriptions-item>
              <el-descriptions-item label="金额">¥{{ approvalDetail.amount?.toFixed(2) }}</el-descriptions-item>
              <el-descriptions-item label="发票号">{{ approvalDetail.invoiceNo }}</el-descriptions-item>
              <el-descriptions-item label="事由" :span="2">{{ approvalDetail.reason }}</el-descriptions-item>
            </el-descriptions>

            <h4 class="section-title">发票附件</h4>
            <div class="attach-list">
              <el-tag v-for="(f, i) in approvalDetail.attachments" :key="i" type="info">{{ f }}</el-tag>
            </div>

            <h4 class="section-title">审批流转</h4>
            <el-timeline>
              <el-timeline-item
                v-for="(node, i) in approvalDetail.workflow"
                :key="i"
                :type="node.status === 'done' ? 'success' : 'info'"
              >
                <strong>{{ node.node }}</strong> — {{ node.operator }}
                <span v-if="node.time" class="node-time">{{ node.time }}</span>
              </el-timeline-item>
            </el-timeline>

            <el-input v-model="approvalRemark" type="textarea" placeholder="审批意见（选填）" :rows="2" />
          </template>
        </div>
        <template #footer>
          <el-button @click="approvalVisible = false">取消</el-button>
          <el-button type="danger" @click="submitReject">驳回</el-button>
          <el-button type="primary" :loading="submitting" @click="submitApprove">通过并生成打款</el-button>
        </template>
      </el-dialog>
    </template>
  </ListPage>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElNotification } from 'element-plus'
import { reimbursementApi } from '@/api'
import { useDictStore } from '@/stores/dict'

const router = useRouter()
const dictStore = useDictStore()
const loading = ref(false)
const approvalLoading = ref(false)
const submitting = ref(false)
const tableData = ref([])
const total = ref(0)
const dialogVisible = ref(false)
const approvalVisible = ref(false)
const approvalDetail = ref(null)
const approvalTarget = ref(null)
const approvalRemark = ref('')
const formRef = ref()
const query = reactive({ reimburseStatus: '', page: 1, pageSize: 10 })
const form = reactive({ amount: 0, reason: '' })
const rules = {
  amount: [{ required: true, message: '请输入金额', trigger: 'blur' }],
  reason: [{ required: true, message: '请输入事由', trigger: 'blur' }]
}

function statusLabel(status) {
  return dictStore.enumLabel('reimburseStatus', Number(status)) || '-'
}

function resetQuery() {
  query.reimburseStatus = ''
  query.page = 1
  loadData()
}

async function loadData() {
  loading.value = true
  try {
    const res = await reimbursementApi.list(query)
    tableData.value = res.data.list
    total.value = res.data.total
  } finally {
    loading.value = false
  }
}

function openDialog() {
  Object.assign(form, { amount: 0, reason: '' })
  dialogVisible.value = true
}

async function handleSubmit() {
  await formRef.value.validate()
  await reimbursementApi.create(form)
  ElMessage.success('提交成功')
  dialogVisible.value = false
  loadData()
}

async function openApproval(row) {
  approvalTarget.value = row
  approvalRemark.value = ''
  approvalVisible.value = true
  approvalLoading.value = true
  try {
    const res = await reimbursementApi.getDetail(row.id)
    approvalDetail.value = res.data
  } finally {
    approvalLoading.value = false
  }
}

async function submitApprove() {
  submitting.value = true
  try {
    await reimbursementApi.approve(approvalTarget.value.id, { remark: approvalRemark.value })
    ElNotification({
      title: '审批完成',
      message: `已向 ${approvalTarget.value.applicant} 推送进度通知，打款指令已联动结算管理。`,
      type: 'success'
    })
    approvalVisible.value = false
    loadData()
    router.push('/finance/settlement')
  } finally {
    submitting.value = false
  }
}

async function submitReject() {
  await reimbursementApi.reject(approvalTarget.value.id, { remark: approvalRemark.value })
  ElMessage.warning('已驳回并通知申请人')
  approvalVisible.value = false
  loadData()
}

onMounted(loadData)
</script>

<style scoped>
.applicant-cell { display: flex; align-items: center; gap: 6px; flex-wrap: wrap; }
.price-cell { font-weight: 600; font-variant-numeric: tabular-nums; color: #e6a23c; }
.status-done { font-size: 13px; color: var(--feishu-text-tertiary); }
.section-title { font-size: 14px; font-weight: 600; margin: 16px 0 10px; }
.attach-list { display: flex; gap: 8px; flex-wrap: wrap; }
.node-time { font-size: 12px; color: var(--feishu-text-tertiary); margin-left: 8px; }
:deep(.col-mono) { font-variant-numeric: tabular-nums; }
</style>
