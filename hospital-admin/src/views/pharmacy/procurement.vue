<template>
  <PageContainer class="procurement-page">
    <PageHeader title="采购管理" subtitle="采购生命周期 · 采销链路严密自治">
      <el-button type="primary" @click="openDialog()">新建采购单</el-button>
    </PageHeader>

    <el-tabs v-model="activePhase" class="phase-tabs" @tab-change="onPhaseChange">
      <el-tab-pane v-for="p in PROCUREMENT_PHASES" :key="String(p.value)" :name="String(p.value)">
        <template #label>
          <span>{{ p.label }}</span>
          <el-badge v-if="phaseCounts[p.value] != null" :value="phaseCounts[p.value]" :max="99" class="phase-badge" />
        </template>
      </el-tab-pane>
    </el-tabs>

    <div v-loading="loading">
      <el-table :data="tableData" stripe border>
        <el-table-column prop="orderNo" label="采购单号" width="140" class-name="col-mono" />
        <el-table-column label="药品" min-width="140">
          <template #default="{ row }">
            <div class="drug-name-cell">
              <span>{{ row.drugName }}</span>
              <el-tag v-if="row.urgent" type="danger" size="small" effect="dark">急需</el-tag>
              <el-tag v-if="isAbnormal(row)" type="warning" size="small">异常金额</el-tag>
            </div>
          </template>
        </el-table-column>
        <el-table-column prop="quantity" label="数量" width="80" align="right" />
        <el-table-column label="单价" width="100" align="right">
          <template #default="{ row }"><span class="price-cell">¥{{ row.unitPrice?.toFixed(2) }}</span></template>
        </el-table-column>
        <el-table-column label="总金额" width="110" align="right">
          <template #default="{ row }">
            <span class="price-cell" :class="{ abnormal: isAbnormal(row) }">
              ¥{{ (row.quantity * row.unitPrice).toFixed(2) }}
            </span>
          </template>
        </el-table-column>
        <el-table-column prop="supplier" label="供应商" width="120" show-overflow-tooltip />
        <el-table-column prop="orderDate" label="日期" width="110" class-name="col-mono" />
        <el-table-column label="状态" width="100" align="center">
          <template #default="{ row }">
            <el-tag :type="PROCUREMENT_PHASE_TAG[row.phase]?.type" size="small">
              {{ PROCUREMENT_PHASE_TAG[row.phase]?.label }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="200" fixed="right" align="center">
          <template #default="{ row }">
            <template v-if="row.phase < 3">
              <el-button link type="primary" @click="openDialog(row)">编辑</el-button>
              <el-button link type="primary" @click="advance(row)">推进</el-button>
              <el-button v-if="row.phase === 2" link type="success" @click="stockIn(row)">确认入库</el-button>
            </template>
            <template v-else>
              <el-button link type="primary" @click="showLogistics(row)">物流凭证</el-button>
            </template>
          </template>
        </el-table-column>
      </el-table>

      <AppPagination
        v-model:page="query.page"
        v-model:page-size="query.pageSize"
        :total="total"
        @change="loadData"
      />
    </div>

    <FormDialog v-model:visible="dialogVisible" :title="form.id ? '编辑采购单' : '新建采购单'" width="520px" @confirm="handleSubmit">
      <el-form ref="formRef" :model="form" :rules="rules" label-width="90px">
        <el-form-item label="药品名称" prop="drugName"><el-input v-model="form.drugName" /></el-form-item>
        <el-form-item label="采购数量" prop="quantity"><el-input-number v-model="form.quantity" :min="1" style="width: 100%" /></el-form-item>
        <el-form-item label="单价" prop="unitPrice"><el-input-number v-model="form.unitPrice" :min="0" :precision="2" style="width: 100%" /></el-form-item>
        <el-form-item label="供应商" prop="supplier"><el-input v-model="form.supplier" /></el-form-item>
        <el-form-item label="采购日期" prop="orderDate">
          <el-date-picker v-model="form.orderDate" type="date" value-format="YYYY-MM-DD" style="width: 100%" />
        </el-form-item>
        <el-form-item label="急需采购">
          <el-switch v-model="form.urgent" :active-value="1" :inactive-value="0" />
        </el-form-item>
      </el-form>
    </FormDialog>

    <el-dialog v-model="logisticsVisible" title="物流与凭证追踪" width="480px">
      <el-descriptions v-if="logistics" :column="1" border>
        <el-descriptions-item label="物流单号">{{ logistics.logisticsNo || '-' }}</el-descriptions-item>
        <el-descriptions-item label="供应商">{{ logistics.supplier }}</el-descriptions-item>
        <el-descriptions-item label="凭证备注">{{ logistics.receiptNote || '-' }}</el-descriptions-item>
      </el-descriptions>
      <el-timeline v-if="logistics?.timeline" style="margin-top: 16px">
        <el-timeline-item v-for="(t, i) in logistics.timeline" :key="i" :timestamp="t.time">{{ t.event }}</el-timeline-item>
      </el-timeline>
    </el-dialog>
  </PageContainer>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'
import { procurementApi } from '@/api'
import { PROCUREMENT_PHASES, PROCUREMENT_PHASE_TAG } from '@/constants/pharmacy'

const route = useRoute()
const loading = ref(false)
const tableData = ref([])
const allOrders = ref([])
const total = ref(0)
const activePhase = ref('')
const dialogVisible = ref(false)
const logisticsVisible = ref(false)
const logistics = ref(null)
const formRef = ref()
const query = reactive({ page: 1, pageSize: 10, phase: '', drugName: route.query.drugName || '' })

const form = reactive({
  id: null, drugName: '', quantity: 100, unitPrice: 0,
  supplier: '', orderDate: '', urgent: 0
})
const rules = { drugName: [{ required: true, message: '请输入药品名称', trigger: 'blur' }] }

const phaseCounts = computed(() => {
  const counts = { '': allOrders.value.length }
  PROCUREMENT_PHASES.filter((p) => p.value !== '').forEach((p) => {
    counts[p.value] = allOrders.value.filter((o) => o.phase === p.value).length
  })
  return counts
})

function isAbnormal(row) {
  return row.quantity * row.unitPrice > 20000
}

function onPhaseChange() {
  query.phase = activePhase.value === '' ? '' : Number(activePhase.value)
  query.page = 1
  loadData()
}

async function loadData() {
  loading.value = true
  try {
    const res = await procurementApi.list({ ...query, pageSize: 100 })
    allOrders.value = res.data.list || []
    let list = allOrders.value
    if (query.drugName) list = list.filter((o) => o.drugName === query.drugName)
    if (query.phase !== '') list = list.filter((o) => o.phase === query.phase)
    total.value = list.length
    const start = (query.page - 1) * query.pageSize
    tableData.value = list.slice(start, start + query.pageSize)
  } finally {
    loading.value = false
  }
}

function openDialog(row) {
  if (row?.phase === 3) {
    ElMessage.warning('已完成订单不可编辑')
    return
  }
  Object.assign(form, row || {
    id: null, drugName: query.drugName || '', quantity: 100,
    unitPrice: 0, supplier: '', orderDate: '', urgent: 0
  })
  dialogVisible.value = true
}

async function handleSubmit() {
  await formRef.value.validate()
  if (form.id) await procurementApi.update(form.id, form)
  else await procurementApi.create(form)
  ElMessage.success('操作成功')
  dialogVisible.value = false
  loadData()
}

async function advance(row) {
  await procurementApi.advance(row.id)
  ElMessage.success('状态已推进')
  if (row.phase === 2) {
    ElMessage.info('待入库状态，请确认入库以联动库存管理')
  }
  loadData()
}

async function stockIn(row) {
  await procurementApi.stockIn(row.id)
  ElMessage.success('已入库，库存管理模块数据已更新')
  loadData()
}

async function showLogistics(row) {
  const res = await procurementApi.getLogistics(row.id)
  logistics.value = res.data
  logisticsVisible.value = true
}

onMounted(loadData)
</script>

<style scoped>
.phase-tabs { margin-bottom: 16px; }
.phase-badge { margin-left: 6px; }
.drug-name-cell { display: flex; align-items: center; gap: 6px; flex-wrap: wrap; }
.price-cell {
  font-family: 'DIN Alternate', monospace;
  font-weight: 600;
  font-variant-numeric: tabular-nums;
}
.price-cell.abnormal { color: #f56c6c; }
:deep(.col-mono) { font-variant-numeric: tabular-nums; }
</style>
