<template>
  <PageContainer>
    <PageHeader title="医嘱扣费确认" subtitle="确认医生开具的检查/药品项目，认定单价并生成患者待缴账单" />

    <TableCard :loading="loading" :data="list">
      <el-table-column prop="patientName" label="患者" width="100" />
      <el-table-column prop="department" label="科室" width="90" />
      <el-table-column prop="doctorName" label="医生" width="90" />
      <el-table-column prop="diagnosis" label="诊断" min-width="140" show-overflow-tooltip />
      <el-table-column label="医嘱" width="120">
        <template #default="{ row }">检查 {{ row.examCount }} 项 · 药品 {{ row.drugCount }} 种</template>
      </el-table-column>
      <el-table-column prop="visitTime" label="就诊时间" width="170" />
      <el-table-column label="操作" width="120" fixed="right">
        <template #default="{ row }">
          <el-button type="primary" link @click="openConfirm(row.recordId)">确认计价</el-button>
        </template>
      </el-table-column>
    </TableCard>

    <el-drawer v-model="drawerVisible" title="确认计价" size="520px" destroy-on-close>
      <div v-loading="detailLoading">
        <template v-if="detail">
          <el-descriptions :column="1" border size="small" class="detail-block">
            <el-descriptions-item label="患者">{{ detail.patientName }}</el-descriptions-item>
            <el-descriptions-item label="诊断">{{ detail.diagnosis }}</el-descriptions-item>
          </el-descriptions>

          <h4 class="section-title">检查项目</h4>
          <el-table v-if="detail.exams?.length" :data="detail.exams" size="small" border>
            <el-table-column prop="name" label="项目" />
            <el-table-column label="单价(元)" width="120">
              <template #default="{ row }">
                <el-input-number v-model="row.unitPrice" :min="0" :precision="2" :step="5" size="small" controls-position="right" @change="recalcExam(row)" />
              </template>
            </el-table-column>
            <el-table-column label="小计" width="90">
              <template #default="{ row }">¥{{ Number(row.amount || 0).toFixed(2) }}</template>
            </el-table-column>
          </el-table>
          <el-empty v-else description="无检查项目" :image-size="48" />

          <h4 class="section-title">药品清单</h4>
          <el-table v-if="detail.drugs?.length" :data="detail.drugs" size="small" border>
            <el-table-column prop="name" label="药品" />
            <el-table-column prop="usage" label="用法" min-width="100" show-overflow-tooltip />
            <el-table-column label="数量" width="80">
              <template #default="{ row }">
                <el-input-number v-model="row.qty" :min="1" :max="99" size="small" controls-position="right" @change="recalcDrug(row)" />
              </template>
            </el-table-column>
            <el-table-column label="单价(元)" width="110">
              <template #default="{ row }">
                <el-input-number v-model="row.unitPrice" :min="0" :precision="2" :step="1" size="small" controls-position="right" @change="recalcDrug(row)" />
              </template>
            </el-table-column>
            <el-table-column label="小计" width="90">
              <template #default="{ row }">¥{{ Number(row.amount || 0).toFixed(2) }}</template>
            </el-table-column>
          </el-table>
          <el-empty v-else description="无药品" :image-size="48" />

          <div class="total-bar">
            <span>合计</span>
            <strong>¥{{ totalAmount.toFixed(2) }}</strong>
          </div>

          <el-button type="primary" class="confirm-btn" :loading="confirming" @click="handleConfirm">
            确认计价并生成账单
          </el-button>
        </template>
      </div>
    </el-drawer>
  </PageContainer>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { billingApi } from '@/api'

const loading = ref(false)
const list = ref([])
const drawerVisible = ref(false)
const detailLoading = ref(false)
const confirming = ref(false)
const detail = ref(null)
const activeRecordId = ref(null)

const totalAmount = computed(() => {
  if (!detail.value) return 0
  const examSum = (detail.value.exams || []).reduce((s, r) => s + Number(r.amount || 0), 0)
  const drugSum = (detail.value.drugs || []).reduce((s, r) => s + Number(r.amount || 0), 0)
  return examSum + drugSum
})

function recalcExam(row) {
  row.amount = Number(row.unitPrice || 0)
}

function recalcDrug(row) {
  row.amount = Number(row.unitPrice || 0) * Number(row.qty || 1)
}

async function loadData() {
  loading.value = true
  try {
    const res = await billingApi.pending({ pageSize: 50 })
    list.value = res.data?.list ?? []
  } finally {
    loading.value = false
  }
}

async function openConfirm(recordId) {
  activeRecordId.value = recordId
  drawerVisible.value = true
  detailLoading.value = true
  try {
    const res = await billingApi.detail(recordId)
    detail.value = res.data
  } finally {
    detailLoading.value = false
  }
}

async function handleConfirm() {
  if (!detail.value) return
  await ElMessageBox.confirm(
    `确认为患者「${detail.value.patientName}」生成待缴账单，合计 ¥${totalAmount.value.toFixed(2)}？`,
    '确认计价',
    { type: 'info' }
  )
  confirming.value = true
  try {
    await billingApi.confirm(activeRecordId.value, {
      exams: (detail.value.exams || []).map((r) => ({
        name: r.name,
        unitPrice: r.unitPrice,
        qty: 1,
        amount: r.amount
      })),
      drugs: (detail.value.drugs || []).map((r) => ({
        name: r.name,
        usage: r.usage,
        qty: r.qty,
        unitPrice: r.unitPrice,
        amount: r.amount
      }))
    })
    ElMessage.success('计价已确认，患者端可查看分项账单')
    drawerVisible.value = false
    loadData()
  } finally {
    confirming.value = false
  }
}

onMounted(loadData)
</script>

<style scoped>
.detail-block { margin-bottom: 16px; }
.section-title {
  margin: 20px 0 10px;
  font-size: 14px;
  font-weight: 600;
}
.total-bar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin: 20px 0;
  padding: 12px 16px;
  border-radius: 8px;
  background: var(--feishu-primary-bg, #e8f3ff);
  font-size: 15px;
}
.confirm-btn { width: 100%; }
</style>
