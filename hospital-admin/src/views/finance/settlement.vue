<template>
  <ListPage
    title="结算管理"
    subtitle="清算-账管-业务放行 · 医疗支付全链路"
    :query="query"
    :loading="loading"
    :data="tableData"
    :total="total"
    @search="loadData"
    @reset="resetQuery"
    @page-change="loadData"
  >
    <template #actions>
      <el-button type="primary" @click="openDialog()">新建结算</el-button>
    </template>
    <template #filters>
      <el-form-item label="患者姓名">
        <el-input v-model="query.patientName" placeholder="患者姓名" clearable />
      </el-form-item>
      <el-form-item label="结算单号">
        <el-input v-model="query.settlementNo" placeholder="结算单号" clearable />
      </el-form-item>
    </template>

    <el-table-column prop="settlementNo" label="结算单号" width="140" class-name="col-mono" />
    <el-table-column prop="patientName" label="患者姓名" width="100" />
    <el-table-column label="总金额" width="110" align="right">
      <template #default="{ row }"><span class="price-cell">¥{{ row.totalAmount?.toFixed(2) }}</span></template>
    </el-table-column>
    <el-table-column label="已付" width="100" align="right">
      <template #default="{ row }">¥{{ row.paidAmount?.toFixed(2) }}</template>
    </el-table-column>
    <el-table-column label="待付(元)" width="120" align="right">
      <template #default="{ row }">
        <span :class="unpaidClass(row)">¥{{ unpaid(row).toFixed(2) }}</span>
        <el-tag v-if="row.overdue && unpaid(row) > 0" type="danger" size="small" style="margin-left: 4px">超期</el-tag>
      </template>
    </el-table-column>
    <el-table-column prop="settlementDate" label="结算日期" width="110" class-name="col-mono" />
    <el-table-column label="状态" width="90" align="center">
      <template #default="{ row }">
        <StatusTag enum-key="orderStatus" :value="Number(row.status)" />
      </template>
    </el-table-column>
    <el-table-column label="操作" width="120" fixed="right" align="center">
      <template #default="{ row }">
        <el-button v-if="Number(row.status) < 2" type="primary" size="small" round @click="openSettle(row)">结算</el-button>
        <el-tag v-else type="success" size="small">已结算</el-tag>
      </template>
    </el-table-column>

    <template #extra>
      <FormDialog v-model:visible="dialogVisible" title="新建结算单" width="480px" @confirm="handleSubmit">
        <el-form ref="formRef" :model="form" :rules="rules" label-width="90px">
          <el-form-item label="患者姓名" prop="patientName"><el-input v-model="form.patientName" /></el-form-item>
          <el-form-item label="总金额" prop="totalAmount"><el-input-number v-model="form.totalAmount" :min="0" :precision="2" style="width: 100%" /></el-form-item>
        </el-form>
      </FormDialog>

      <el-drawer v-model="settleVisible" title="费用清算面单" size="520px" destroy-on-close>
        <div v-loading="settleLoading">
          <template v-if="settleDetail">
            <section class="settle-hero">
              <h3>{{ settleDetail.patientName }}</h3>
              <p>{{ settleDetail.settlementNo }} · 待付 <strong>¥{{ settleDetail.unpaid?.toFixed(2) }}</strong></p>
            </section>

            <h4 class="section-title">费用构成</h4>
            <el-table :data="settleDetail.feeItems" size="small" stripe>
              <el-table-column prop="name" label="项目" />
              <el-table-column label="金额" align="right">
                <template #default="{ row }">¥{{ row.amount?.toFixed(2) }}</template>
              </el-table-column>
            </el-table>

            <el-descriptions :column="1" border size="small" style="margin-top: 16px">
              <el-descriptions-item label="医保统筹抵扣">¥{{ settleDetail.insuranceAmount?.toFixed(2) }}</el-descriptions-item>
              <el-descriptions-item label="自费应付">¥{{ settleDetail.selfPayAmount?.toFixed(2) }}</el-descriptions-item>
            </el-descriptions>

            <h4 class="section-title">支付渠道</h4>
            <el-radio-group v-model="payChannel">
              <el-radio v-for="c in PAYMENT_CHANNELS" :key="c" :value="c">{{ c }}</el-radio>
            </el-radio-group>
          </template>
        </div>
        <template #footer>
          <el-button @click="settleVisible = false">取消</el-button>
          <el-button type="primary" :loading="submitting" @click="confirmSettle">确认收款</el-button>
        </template>
      </el-drawer>
    </template>
  </ListPage>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElNotification } from 'element-plus'
import { settlementApi } from '@/api'
import { SETTLEMENT_UNPAID_LEVEL, PAYMENT_CHANNELS } from '@/constants/finance'

const loading = ref(false)
const settleLoading = ref(false)
const submitting = ref(false)
const tableData = ref([])
const total = ref(0)
const dialogVisible = ref(false)
const settleVisible = ref(false)
const settleDetail = ref(null)
const settleTarget = ref(null)
const payChannel = ref('微信')
const formRef = ref()
const query = reactive({ patientName: '', settlementNo: '', page: 1, pageSize: 10 })
const form = reactive({ patientName: '', totalAmount: 0 })
const rules = { patientName: [{ required: true, message: '请输入患者姓名', trigger: 'blur' }] }

function unpaid(row) {
  return (row.totalAmount || 0) - (row.paidAmount || 0)
}

function unpaidClass(row) {
  const u = unpaid(row)
  const level = SETTLEMENT_UNPAID_LEVEL(u, row.overdue)
  return { 'unpaid-normal': level === 'normal', 'unpaid-pending': level === 'pending', 'unpaid-warn': level === 'warning', 'unpaid-danger': level === 'danger' }
}

function resetQuery() {
  query.patientName = ''
  query.settlementNo = ''
  query.page = 1
  loadData()
}

async function loadData() {
  loading.value = true
  try {
    const res = await settlementApi.list(query)
    tableData.value = res.data.list
    total.value = res.data.total
  } finally {
    loading.value = false
  }
}

function openDialog() {
  Object.assign(form, { patientName: '', totalAmount: 0 })
  dialogVisible.value = true
}

async function handleSubmit() {
  await formRef.value.validate()
  await settlementApi.create(form)
  ElMessage.success('结算单已创建')
  dialogVisible.value = false
  loadData()
}

async function openSettle(row) {
  settleTarget.value = row
  payChannel.value = '微信'
  settleVisible.value = true
  settleLoading.value = true
  try {
    const res = await settlementApi.getDetail(row.id)
    settleDetail.value = res.data
  } finally {
    settleLoading.value = false
  }
}

async function confirmSettle() {
  submitting.value = true
  try {
    const res = await settlementApi.settle(settleTarget.value.id, { channel: payChannel.value })
    ElNotification({
      title: '结算完成',
      message: `电子发票 ${res.data?.invoiceNo} 已开具，患者诊疗/取药权限已解封。`,
      type: 'success',
      duration: 6000
    })
    settleVisible.value = false
    loadData()
  } finally {
    submitting.value = false
  }
}

onMounted(loadData)
</script>

<style scoped>
.price-cell { font-weight: 600; font-variant-numeric: tabular-nums; }
.unpaid-normal { color: var(--feishu-text-tertiary); }
.unpaid-pending { color: var(--feishu-text-primary); font-weight: 500; }
.unpaid-warn { color: #e6a23c; font-weight: 600; }
.unpaid-danger { color: #f56c6c; font-weight: 700; }
.settle-hero {
  padding: 16px; margin-bottom: 16px; border-radius: 10px;
  background: linear-gradient(135deg, #ecf5ff, #fff);
}
.settle-hero h3 { font-size: 18px; margin-bottom: 4px; }
.section-title { font-size: 14px; font-weight: 600; margin: 16px 0 10px; padding-left: 8px; border-left: 3px solid var(--feishu-primary); }
:deep(.col-mono) { font-variant-numeric: tabular-nums; }
</style>
