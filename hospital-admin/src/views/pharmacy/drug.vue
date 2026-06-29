<template>
  <ListPage
    title="药品信息管理"
    subtitle="图文并茂 · 物料档案与调度中枢"
    :query="query"
    :loading="loading"
    :data="tableData"
    :total="total"
    @search="loadData"
    @reset="resetQuery"
    @page-change="loadData"
  >
    <template #actions>
      <el-button type="primary" @click="openDialog()">新增药品</el-button>
    </template>
    <template #filters>
      <el-form-item label="关键词">
        <el-input v-model="query.keyword" placeholder="药品名称/编码" clearable />
      </el-form-item>
    </template>

    <el-table-column label="药品" min-width="220">
      <template #default="{ row }">
        <div class="drug-cell">
          <div class="drug-thumb" :style="{ background: drugThumbColor(row.drugName) }">
            <el-icon :size="20"><Goods /></el-icon>
          </div>
          <div>
            <div class="drug-cell__name">{{ row.drugName }}</div>
            <div class="drug-cell__code">{{ row.drugCode }} · {{ row.specification }}</div>
            <div class="drug-cell__tags">
              <el-tag size="small" :type="DRUG_TYPE_TAG[row.drugType]?.type || 'info'">
                {{ DRUG_TYPE_TAG[row.drugType]?.label || row.drugType }}
              </el-tag>
              <el-tag v-if="row.riskLevel === '高危'" size="small" type="danger" effect="dark">高危</el-tag>
            </div>
          </div>
        </div>
      </template>
    </el-table-column>
    <el-table-column prop="manufacturer" label="生产厂家" width="130" show-overflow-tooltip />
    <el-table-column prop="unit" label="单位" width="64" align="center" />
    <el-table-column label="单价(元)" width="110" align="right">
      <template #default="{ row }">
        <span class="price-cell">¥{{ row.price?.toFixed(2) }}</span>
      </template>
    </el-table-column>
    <el-table-column prop="status" label="状态" width="80" align="center">
      <template #default="{ row }">
        <StatusTag :type="row.status === 1 ? 'success' : 'info'" :label="row.status === 1 ? '在售' : '停售'" />
      </template>
    </el-table-column>
    <el-table-column label="操作" width="240" fixed="right" align="center">
      <template #default="{ row }">
        <el-button link type="primary" @click="showInventory(row)">当前库存</el-button>
        <el-button link type="primary" @click="showTrend(row)">采购趋势</el-button>
        <el-button link type="primary" @click="openDialog(row)">编辑</el-button>
        <el-button link type="warning" @click="handleArchive(row)">归档</el-button>
      </template>
    </el-table-column>

    <template #extra>
      <FormDialog v-model:visible="dialogVisible" :title="form.id ? '编辑药品' : '新增药品'" width="520px" @confirm="handleSubmit">
        <el-form ref="formRef" :model="form" :rules="rules" label-width="90px">
          <el-form-item label="药品名称" prop="drugName"><el-input v-model="form.drugName" /></el-form-item>
          <el-form-item label="药品类型" prop="drugType">
            <el-radio-group v-model="form.drugType">
              <el-radio value="处方药">处方药</el-radio>
              <el-radio value="OTC">OTC</el-radio>
            </el-radio-group>
          </el-form-item>
          <el-form-item label="风险等级" prop="riskLevel">
            <el-radio-group v-model="form.riskLevel">
              <el-radio value="普通">普通</el-radio>
              <el-radio value="高危">高危</el-radio>
            </el-radio-group>
          </el-form-item>
          <el-form-item label="规格" prop="specification"><el-input v-model="form.specification" /></el-form-item>
          <el-form-item label="生产厂家" prop="manufacturer"><el-input v-model="form.manufacturer" /></el-form-item>
          <el-form-item label="单位" prop="unit"><el-input v-model="form.unit" /></el-form-item>
          <el-form-item label="单价" prop="price"><el-input-number v-model="form.price" :min="0" :precision="2" style="width: 100%" /></el-form-item>
        </el-form>
      </FormDialog>

      <el-drawer v-model="inventoryVisible" :title="`${currentDrug?.drugName} · 当前库存`" size="420px">
        <div v-loading="drawerLoading">
          <el-statistic title="总库存" :value="inventoryData.totalStock || 0" />
          <el-table :data="inventoryData.list || []" size="small" style="margin-top: 16px">
            <el-table-column prop="warehouse" label="仓库" />
            <el-table-column prop="stock" label="库存" align="right" />
            <el-table-column prop="batchNo" label="批号" />
          </el-table>
          <el-button type="primary" style="margin-top: 16px" @click="goInventory">前往库存管理</el-button>
        </div>
      </el-drawer>

      <el-dialog v-model="trendVisible" :title="`${currentDrug?.drugName} · 历史采购趋势`" width="480px">
        <div v-loading="drawerLoading">
          <el-table :data="trendData" size="small">
            <el-table-column prop="month" label="月份" />
            <el-table-column prop="quantity" label="采购量" align="right" />
            <el-table-column label="金额" align="right">
              <template #default="{ row }">¥{{ row.amount?.toFixed(2) }}</template>
            </el-table-column>
          </el-table>
          <el-button type="primary" plain style="margin-top: 12px" @click="goProcurement">查看采购记录</el-button>
        </div>
      </el-dialog>
    </template>
  </ListPage>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { drugApi } from '@/api'
import { DRUG_TYPE_TAG, drugThumbColor } from '@/constants/pharmacy'

const router = useRouter()
const loading = ref(false)
const drawerLoading = ref(false)
const tableData = ref([])
const total = ref(0)
const dialogVisible = ref(false)
const inventoryVisible = ref(false)
const trendVisible = ref(false)
const currentDrug = ref(null)
const inventoryData = ref({ list: [], totalStock: 0 })
const trendData = ref([])
const formRef = ref()
const query = reactive({ keyword: '', page: 1, pageSize: 10 })
const form = reactive({
  id: null, drugName: '', drugType: '处方药', riskLevel: '普通',
  specification: '', manufacturer: '', unit: '盒', price: 0
})
const rules = { drugName: [{ required: true, message: '请输入药品名称', trigger: 'blur' }] }

function resetQuery() {
  query.keyword = ''
  query.page = 1
  loadData()
}

async function loadData() {
  loading.value = true
  try {
    const res = await drugApi.list(query)
    tableData.value = res.data.list
    total.value = res.data.total
  } finally {
    loading.value = false
  }
}

function openDialog(row) {
  Object.assign(form, row || {
    id: null, drugName: '', drugType: '处方药', riskLevel: '普通',
    specification: '', manufacturer: '', unit: '盒', price: 0
  })
  dialogVisible.value = true
}

async function handleSubmit() {
  await formRef.value.validate()
  if (form.id) await drugApi.update(form.id, form)
  else await drugApi.create(form)
  ElMessage.success('操作成功')
  dialogVisible.value = false
  loadData()
}

async function handleArchive(row) {
  await ElMessageBox.confirm(
    `确定归档「${row.drugName}」？归档后停售但历史处方仍可追溯。`,
    '停用/归档',
    { type: 'warning' }
  )
  await drugApi.archive(row.id)
  ElMessage.success('已归档')
  loadData()
}

async function showInventory(row) {
  currentDrug.value = row
  inventoryVisible.value = true
  drawerLoading.value = true
  try {
    const res = await drugApi.getInventory(row.id)
    inventoryData.value = res.data
  } finally {
    drawerLoading.value = false
  }
}

async function showTrend(row) {
  currentDrug.value = row
  trendVisible.value = true
  drawerLoading.value = true
  try {
    const res = await drugApi.getProcurementTrend(row.id)
    trendData.value = res.data.list || []
  } finally {
    drawerLoading.value = false
  }
}

function goInventory() {
  router.push({ path: '/pharmacy/inventory', query: { keyword: currentDrug.value?.drugName } })
}

function goProcurement() {
  router.push({ path: '/pharmacy/procurement', query: { drugName: currentDrug.value?.drugName } })
}

onMounted(loadData)
</script>

<style scoped>
.drug-cell { display: flex; gap: 12px; align-items: flex-start; }
.drug-thumb {
  width: 48px; height: 48px; border-radius: 10px;
  display: flex; align-items: center; justify-content: center;
  color: #fff; flex-shrink: 0;
}
.drug-cell__name { font-weight: 600; font-size: 14px; }
.drug-cell__code { font-size: 12px; color: var(--feishu-text-tertiary); margin: 2px 0 6px; }
.drug-cell__tags { display: flex; gap: 4px; }
.price-cell {
  font-family: 'DIN Alternate', monospace;
  font-weight: 700;
  font-variant-numeric: tabular-nums;
  color: #e6a23c;
}
</style>
