<template>
  <ListPage
    title="财务信息管理"
    subtitle="总览-穿透-管控-审计 · 资金风控闭环"
    :query="query"
    :loading="loading"
    :data="tableData"
    :total="total"
    @search="loadData"
    @reset="resetQuery"
    @page-change="loadData"
  >
    <template #actions>
      <el-button type="primary" @click="openDialog()">新增账户</el-button>
    </template>
    <template #filters>
      <el-form-item label="开户银行">
        <el-select v-model="query.bank" placeholder="全部" clearable style="width: 140px">
          <el-option v-for="b in BANK_OPTIONS" :key="b" :label="b" :value="b" />
        </el-select>
      </el-form-item>
      <el-form-item label="账户状态">
        <el-select v-model="query.status" placeholder="全部" clearable style="width: 120px">
          <el-option label="正常" :value="1" />
          <el-option label="冻结" :value="0" />
        </el-select>
      </el-form-item>
    </template>

    <el-table-column label="账户名称" min-width="160">
      <template #default="{ row }">
        <el-button link type="primary" class="account-link" @click="openFlows(row)">{{ row.accountName }}</el-button>
      </template>
    </el-table-column>
    <el-table-column prop="accountNo" label="账户编号" width="120" class-name="col-mono" />
    <el-table-column label="余额(元)" width="150" align="right">
      <template #default="{ row }">
        <span :class="balanceClass(row)" class="balance-text">¥{{ row.balance?.toLocaleString(undefined, { minimumFractionDigits: 2 }) }}</span>
      </template>
    </el-table-column>
    <el-table-column prop="bank" label="开户银行" width="120" />
    <el-table-column prop="status" label="状态" width="90" align="center">
      <template #default="{ row }">
        <StatusTag :type="row.status === 1 ? 'success' : 'info'" :label="row.status === 1 ? '正常' : '冻结'" />
      </template>
    </el-table-column>
    <el-table-column prop="updateTime" label="更新时间" width="170" align="right" class-name="col-mono" />
    <el-table-column label="操作" width="180" fixed="right" align="center">
      <template #default="{ row }">
        <el-button link type="primary" @click="openDialog(row)">编辑</el-button>
        <el-button v-if="row.status === 1" link type="warning" @click="handleFreeze(row)">冻结</el-button>
        <el-button link type="info" @click="handleArchive(row)">注销</el-button>
      </template>
    </el-table-column>

    <template #extra>
      <FormDialog v-model:visible="dialogVisible" :title="form.id ? '编辑账户' : '新增账户'" width="480px" @confirm="handleSubmit">
        <el-form ref="formRef" :model="form" :rules="rules" label-width="90px">
          <el-form-item label="账户名称" prop="accountName"><el-input v-model="form.accountName" /></el-form-item>
          <el-form-item label="开户银行" prop="bank">
            <el-select v-model="form.bank" style="width: 100%">
              <el-option v-for="b in BANK_OPTIONS" :key="b" :label="b" :value="b" />
            </el-select>
          </el-form-item>
          <el-form-item label="余额" prop="balance"><el-input-number v-model="form.balance" :precision="2" style="width: 100%" /></el-form-item>
        </el-form>
      </FormDialog>

      <el-drawer v-model="flowVisible" :title="`${currentAccount?.accountName} · 流水明细`" size="520px">
        <div v-loading="flowLoading">
          <el-table :data="flows" size="small" stripe>
            <el-table-column prop="time" label="时间" width="155" />
            <el-table-column prop="type" label="类型" width="70" />
            <el-table-column label="金额" width="110" align="right">
              <template #default="{ row }">
                <span :class="row.amount >= 0 ? 'amt-in' : 'amt-out'">{{ row.amount >= 0 ? '+' : '' }}{{ row.amount?.toFixed(2) }}</span>
              </template>
            </el-table-column>
            <el-table-column prop="balance" label="余额" width="110" align="right" />
            <el-table-column prop="remark" label="摘要" show-overflow-tooltip />
          </el-table>
        </div>
      </el-drawer>
    </template>
  </ListPage>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { financeApi } from '@/api'
import { balanceClass, BANK_OPTIONS } from '@/constants/finance'

const loading = ref(false)
const flowLoading = ref(false)
const tableData = ref([])
const total = ref(0)
const dialogVisible = ref(false)
const flowVisible = ref(false)
const currentAccount = ref(null)
const flows = ref([])
const formRef = ref()
const query = reactive({ bank: '', status: '', page: 1, pageSize: 10 })
const form = reactive({ id: null, accountName: '', bank: '', balance: 0 })
const rules = { accountName: [{ required: true, message: '请输入账户名称', trigger: 'blur' }] }

function resetQuery() {
  query.bank = ''
  query.status = ''
  query.page = 1
  loadData()
}

async function loadData() {
  loading.value = true
  try {
    const res = await financeApi.list(query)
    tableData.value = res.data.list
    total.value = res.data.total
  } finally {
    loading.value = false
  }
}

function openDialog(row) {
  Object.assign(form, row || { id: null, accountName: '', bank: BANK_OPTIONS[0], balance: 0 })
  dialogVisible.value = true
}

async function handleSubmit() {
  await formRef.value.validate()
  if (form.id) await financeApi.update(form.id, form)
  else await financeApi.create(form)
  ElMessage.success('操作成功')
  dialogVisible.value = false
  loadData()
}

async function handleFreeze(row) {
  await ElMessageBox.confirm(`确定冻结账户「${row.accountName}」？`, '冻结账户', { type: 'warning' })
  await financeApi.freeze(row.id)
  ElMessage.success('账户已冻结')
  loadData()
}

async function handleArchive(row) {
  await ElMessageBox.confirm(`注销后账户将归档，流水仍可审计。确定注销「${row.accountName}」？`, '注销归档', { type: 'warning' })
  await financeApi.archive(row.id)
  ElMessage.success('已注销归档')
  loadData()
}

async function openFlows(row) {
  currentAccount.value = row
  flowVisible.value = true
  flowLoading.value = true
  try {
    const res = await financeApi.getFlows(row.id)
    flows.value = res.data.list || []
  } finally {
    flowLoading.value = false
  }
}

onMounted(loadData)
</script>

<style scoped>
.account-link { font-weight: 600; }
.balance-text { font-variant-numeric: tabular-nums; font-weight: 600; }
.balance-normal { color: var(--feishu-text-primary); }
.balance-warn { color: #e6a23c; }
.balance-danger { color: #f56c6c; }
.balance-frozen { color: var(--feishu-text-tertiary); text-decoration: line-through; }
.amt-in { color: #67c23a; font-weight: 600; }
.amt-out { color: #f56c6c; font-weight: 600; }
:deep(.col-mono) { font-variant-numeric: tabular-nums; }
</style>
