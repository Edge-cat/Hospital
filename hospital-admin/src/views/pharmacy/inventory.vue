<template>
  <PageContainer class="inventory-page">
    <PageHeader title="库存管理" subtitle="动态监控 · 智能补给仓储自治">
      <el-switch v-model="expiryOnly" active-text="仅看效期预警" @change="loadData" />
    </PageHeader>

    <SearchToolbar :query="query" @search="loadData" @reset="resetQuery">
      <el-form-item label="药品名称">
        <el-input v-model="query.keyword" placeholder="药品名称/编码" clearable />
      </el-form-item>
    </SearchToolbar>

    <TableCard v-loading="loading" :data="tableData">
      <el-table-column prop="drugCode" label="编码" width="100" class-name="col-mono" />
      <el-table-column prop="drugName" label="药品名称" width="130" />
      <el-table-column label="库存充裕度" min-width="200">
        <template #default="{ row }">
          <div class="stock-gauge">
            <el-progress
              :percentage="stockPercent(row)"
              :color="stockColor(row)"
              :stroke-width="10"
              :format="() => `${row.stock}/${row.minStock}`"
            />
          </div>
        </template>
      </el-table-column>
      <el-table-column label="效期" width="120">
        <template #default="{ row }">
          <el-tag v-if="isExpired(row)" type="danger" size="small">已过期</el-tag>
          <el-tag v-else-if="isNearExpiry(row)" type="warning" size="small">近效期</el-tag>
          <span v-else class="col-mono">{{ row.expiryDate }}</span>
        </template>
      </el-table-column>
      <el-table-column prop="warehouse" label="仓库" width="110" />
      <el-table-column prop="batchNo" label="批号" width="110" class-name="col-mono" />
      <el-table-column label="操作" width="200" fixed="right" align="center">
        <template #default="{ row }">
          <el-button link type="primary" @click="openAdjust(row)">标准调整</el-button>
          <el-button v-if="row.stock <= row.minStock" link type="warning" @click="createProcurement(row)">生成采购单</el-button>
        </template>
      </el-table-column>
      <template #footer>
        <AppPagination v-model:page="query.page" v-model:page-size="query.pageSize" :total="total" @change="loadData" />
      </template>
    </TableCard>

    <el-dialog v-model="adjustVisible" title="标准库存调整" width="480px">
      <el-form :model="adjustForm" label-width="100px">
        <el-form-item label="药品"><el-input :model-value="adjustForm.drugName" disabled /></el-form-item>
        <el-form-item label="调整类型" required>
          <el-radio-group v-model="adjustForm.type">
            <el-radio v-for="t in INVENTORY_ADJUST_TYPES" :key="t.value" :value="t.value">{{ t.label }}</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item v-if="adjustForm.type === 'stocktake'" label="盘点后数量">
          <el-input-number v-model="adjustForm.quantity" :min="0" style="width: 100%" />
        </el-form-item>
        <el-form-item v-if="adjustForm.type === 'disposal'" label="销毁数量">
          <el-input-number v-model="adjustForm.quantity" :min="1" :max="adjustForm.currentStock" style="width: 100%" />
        </el-form-item>
        <el-form-item v-if="adjustForm.type === 'transfer'" label="调入仓库">
          <el-select v-model="adjustForm.targetWarehouse" style="width: 100%">
            <el-option label="中心药房" value="中心药房" />
            <el-option label="门诊药房" value="门诊药房" />
            <el-option label="住院药房" value="住院药房" />
          </el-select>
        </el-form-item>
        <el-form-item label="备注">
          <el-input v-model="adjustForm.remark" type="textarea" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="adjustVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitting" @click="submitAdjust">确认调整</el-button>
      </template>
    </el-dialog>
  </PageContainer>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'
import { inventoryApi } from '@/api'
import { INVENTORY_ADJUST_TYPES } from '@/constants/pharmacy'

const route = useRoute()
const loading = ref(false)
const submitting = ref(false)
const expiryOnly = ref(false)
const tableData = ref([])
const total = ref(0)
const adjustVisible = ref(false)
const query = reactive({ keyword: route.query.keyword || '', page: 1, pageSize: 10 })

const adjustForm = reactive({
  id: null, drugName: '', currentStock: 0,
  type: 'stocktake', quantity: 0, targetWarehouse: '', remark: ''
})

function stockPercent(row) {
  const max = Math.max(row.minStock * 2, row.stock, 1)
  return Math.min(100, Math.round((row.stock / max) * 100))
}

function stockColor(row) {
  if (row.stock <= row.minStock) return '#f56c6c'
  if (row.stock <= row.minStock * 1.5) return '#e6a23c'
  return '#67c23a'
}

function isExpired(row) {
  return row.expiryDate && row.expiryDate < new Date().toISOString().slice(0, 10)
}

function isNearExpiry(row) {
  if (!row.expiryDate || isExpired(row)) return false
  const soon = new Date()
  soon.setDate(soon.getDate() + 30)
  return row.expiryDate <= soon.toISOString().slice(0, 10)
}

function resetQuery() {
  query.keyword = ''
  query.page = 1
  loadData()
}

async function loadData() {
  loading.value = true
  try {
    const res = await inventoryApi.list({
      ...query,
      expiryAlert: expiryOnly.value ? '1' : ''
    })
    tableData.value = res.data.list
    total.value = res.data.total
  } finally {
    loading.value = false
  }
}

function openAdjust(row) {
  Object.assign(adjustForm, {
    id: row.id,
    drugName: row.drugName,
    currentStock: row.stock,
    type: 'stocktake',
    quantity: row.stock,
    targetWarehouse: row.warehouse,
    remark: ''
  })
  adjustVisible.value = true
}

async function submitAdjust() {
  submitting.value = true
  try {
    await inventoryApi.adjust(adjustForm.id, { ...adjustForm })
    ElMessage.success('库存调整成功')
    adjustVisible.value = false
    loadData()
  } finally {
    submitting.value = false
  }
}

async function createProcurement(row) {
  const res = await inventoryApi.createProcurement(row.id, {})
  ElMessage.success(`采购申请单已生成（#${res.data.procurementId}）`)
}

onMounted(loadData)
</script>

<style scoped>
.stock-gauge { min-width: 160px; }
:deep(.col-mono) { font-variant-numeric: tabular-nums; }
</style>
