<template>
  <PageContainer class="income-expense-page">
    <PageHeader title="收支管理" subtitle="业财一体化动态对账中心">
      <el-button @click="openDialog()">手工补录</el-button>
    </PageHeader>

    <div class="period-bar">
      <span class="period-label">收支周期</span>
      <el-date-picker
        v-model="dateRange"
        type="daterange"
        range-separator="至"
        start-placeholder="开始"
        end-placeholder="结束"
        value-format="YYYY-MM-DD"
        @change="loadAll"
      />
      <el-button type="primary" size="small" @click="loadAll">查询</el-button>
    </div>

    <el-row :gutter="16" class="summary-row">
      <el-col :span="8" v-for="card in summaryCards" :key="card.key">
        <div class="summary-card">
          <div class="summary-card__head">
            <span>{{ card.label }}</span>
            <span v-if="card.mom != null" class="summary-mom" :class="card.mom >= 0 ? 'up' : 'down'">
              <el-icon><component :is="card.mom >= 0 ? 'Top' : 'Bottom'" /></el-icon>
              {{ Math.abs(card.mom) }}%
            </span>
          </div>
          <div class="summary-card__value" :style="{ color: card.color }">{{ card.value }}</div>
          <DashboardTrendChart :data="card.trend" :labels="[]" :color="card.color" />
        </div>
      </el-col>
    </el-row>

    <TableCard v-loading="loading" :data="tableData">
      <el-table-column prop="type" label="类型" width="80" align="center">
        <template #default="{ row }">
          <StatusTag :type="row.type === '收入' ? 'success' : 'danger'" :label="row.type" />
        </template>
      </el-table-column>
      <el-table-column prop="category" label="分类" width="110" />
      <el-table-column label="金额(元)" width="120" align="right">
        <template #default="{ row }">
          <span class="price-cell">¥{{ row.amount?.toFixed(2) }}</span>
        </template>
      </el-table-column>
      <el-table-column label="来源" width="110">
        <template #default="{ row }">
          <el-tag v-if="row.autoCollected" size="small" type="success" effect="plain">自动归集</el-tag>
          <el-tag v-else size="small" type="info" effect="plain">手工</el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="sourceModule" label="业务模块" width="110" />
      <el-table-column prop="operator" label="经办人" width="90" />
      <el-table-column prop="recordDate" label="日期" width="110" class-name="col-mono" />
      <el-table-column prop="remark" label="备注" min-width="140" show-overflow-tooltip />
      <el-table-column label="操作" width="100" fixed="right" align="center">
        <template #default="{ row }">
          <el-button v-if="row.autoCollected" link type="primary" @click="traceSource(row)">溯源</el-button>
          <span v-else class="muted">-</span>
        </template>
      </el-table-column>
      <template #footer>
        <AppPagination v-model:page="query.page" v-model:page-size="query.pageSize" :total="total" @change="loadList" />
      </template>
    </TableCard>

    <FormDialog v-model:visible="dialogVisible" title="手工补录" width="480px" @confirm="handleSubmit">
      <el-alert type="info" :closable="false" style="margin-bottom: 12px">建议优先通过业务模块自动归集；手工补录仅用于特殊调账。</el-alert>
      <el-form ref="formRef" :model="form" :rules="rules" label-width="80px">
        <el-form-item label="类型" prop="type">
          <el-radio-group v-model="form.type"><el-radio value="收入">收入</el-radio><el-radio value="支出">支出</el-radio></el-radio-group>
        </el-form-item>
        <el-form-item label="分类" prop="category"><el-input v-model="form.category" /></el-form-item>
        <el-form-item label="金额" prop="amount"><el-input-number v-model="form.amount" :min="0" :precision="2" style="width: 100%" /></el-form-item>
        <el-form-item label="备注"><el-input v-model="form.remark" type="textarea" /></el-form-item>
      </el-form>
    </FormDialog>

    <el-dialog v-model="traceVisible" title="业务单据溯源" width="440px">
      <el-descriptions v-if="traceData" :column="1" border>
        <el-descriptions-item label="业务模块">{{ traceData.sourceDoc?.module }}</el-descriptions-item>
        <el-descriptions-item label="单据编号">{{ traceData.sourceDoc?.docNo }}</el-descriptions-item>
        <el-descriptions-item label="分类">{{ traceData.category }}</el-descriptions-item>
        <el-descriptions-item label="金额">¥{{ traceData.amount?.toFixed(2) }}</el-descriptions-item>
      </el-descriptions>
      <template #footer>
        <el-button @click="traceVisible = false">关闭</el-button>
        <el-button type="primary" @click="goSource">跳转原始单据</el-button>
      </template>
    </el-dialog>
  </PageContainer>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { incomeExpenseApi } from '@/api'
import { STAT_COLORS } from '@/constants'
import DashboardTrendChart from '@/components/DashboardTrendChart.vue'

const router = useRouter()
const loading = ref(false)
const tableData = ref([])
const total = ref(0)
const dialogVisible = ref(false)
const traceVisible = ref(false)
const traceData = ref(null)
const formRef = ref()
const summary = ref({ income: 0, expense: 0, balance: 0, incomeMom: 0, expenseMom: 0, incomeTrend: [], expenseTrend: [] })

const now = new Date()
const monthStart = new Date(now.getFullYear(), now.getMonth(), 1).toISOString().slice(0, 10)
const dateRange = ref([monthStart, now.toISOString().slice(0, 10)])

const query = reactive({ page: 1, pageSize: 10 })
const form = reactive({ type: '收入', category: '', amount: 0, remark: '' })
const rules = {
  category: [{ required: true, message: '请输入分类', trigger: 'blur' }],
  amount: [{ required: true, message: '请输入金额', trigger: 'blur' }]
}

const summaryCards = computed(() => [
  { key: 'income', label: '周期收入', value: `¥${summary.value.income.toFixed(2)}`, color: STAT_COLORS.success, mom: summary.value.incomeMom, trend: summary.value.incomeTrend },
  { key: 'expense', label: '周期支出', value: `¥${summary.value.expense.toFixed(2)}`, color: STAT_COLORS.danger, mom: summary.value.expenseMom, trend: summary.value.expenseTrend },
  { key: 'balance', label: '周期结余', value: `¥${summary.value.balance.toFixed(2)}`, color: STAT_COLORS.primary, mom: null, trend: summary.value.incomeTrend.map((v, i) => v - (summary.value.expenseTrend[i] || 0)) }
])

function listParams() {
  const p = { ...query }
  if (dateRange.value?.length === 2) {
    p.startDate = dateRange.value[0]
    p.endDate = dateRange.value[1]
  }
  return p
}

async function loadSummary() {
  const p = {}
  if (dateRange.value?.length === 2) {
    p.startDate = dateRange.value[0]
    p.endDate = dateRange.value[1]
  }
  const res = await incomeExpenseApi.summary(p)
  summary.value = res.data
}

async function loadList() {
  loading.value = true
  try {
    const res = await incomeExpenseApi.list(listParams())
    tableData.value = res.data.list
    total.value = res.data.total
  } finally {
    loading.value = false
  }
}

async function loadAll() {
  await Promise.all([loadSummary(), loadList()])
}

function openDialog() {
  Object.assign(form, { type: '收入', category: '', amount: 0, remark: '' })
  dialogVisible.value = true
}

async function handleSubmit() {
  await formRef.value.validate()
  await incomeExpenseApi.create(form)
  ElMessage.success('补录成功')
  dialogVisible.value = false
  loadAll()
}

async function traceSource(row) {
  const res = await incomeExpenseApi.trace(row.id)
  traceData.value = res.data
  traceVisible.value = true
}

function goSource() {
  const path = traceData.value?.sourceDoc?.path
  if (path) router.push(path)
  traceVisible.value = false
}

onMounted(loadAll)
</script>

<style scoped>
.period-bar {
  display: flex; align-items: center; gap: 12px;
  margin-bottom: 16px; padding: 14px 16px;
  background: #fff; border-radius: 10px;
  box-shadow: 0 2px 8px rgba(0,0,0,0.04);
}
.period-label { font-size: 13px; color: var(--feishu-text-secondary); }
.summary-row { margin-bottom: 16px; }
.summary-card {
  padding: 18px; border-radius: 12px;
  background: linear-gradient(145deg, #fff, #fafbfc);
  box-shadow: 0 2px 12px rgba(0,0,0,0.05);
  border: 1px solid var(--feishu-border-light);
}
.summary-card__head {
  display: flex; justify-content: space-between; align-items: center;
  font-size: 13px; color: var(--feishu-text-secondary); margin-bottom: 8px;
}
.summary-card__value { font-size: 24px; font-weight: 700; margin-bottom: 8px; font-variant-numeric: tabular-nums; }
.summary-mom { display: flex; align-items: center; gap: 2px; font-size: 12px; font-weight: 600; }
.summary-mom.up { color: #67c23a; }
.summary-mom.down { color: #f56c6c; }
.price-cell { font-weight: 600; font-variant-numeric: tabular-nums; }
.muted { color: var(--feishu-text-tertiary); font-size: 12px; }
:deep(.col-mono) { font-variant-numeric: tabular-nums; }
</style>
