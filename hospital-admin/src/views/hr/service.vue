<template>
  <PageContainer class="service-page">
    <PageHeader title="医疗服务管理" subtitle="服务配置-前台调用-账单结算 无缝闭环">
      <el-button type="primary" @click="openDialog()">新增服务</el-button>
    </PageHeader>

    <div class="service-layout">
      <aside class="service-tree">
        <div
          v-for="cat in SERVICE_CATEGORIES"
          :key="cat.id"
          class="tree-item"
          :class="{ active: activeCategory === cat.id }"
          @click="selectCategory(cat.id)"
        >
          <el-icon><component :is="cat.icon" /></el-icon>
          <span>{{ cat.label }}</span>
          <el-badge v-if="cat.id !== 'all'" :value="categoryCount(cat.id)" :max="99" type="primary" />
        </div>
      </aside>

      <main class="service-main" v-loading="loading">
        <el-table :data="tableData" stripe border>
          <el-table-column prop="serviceName" label="服务名称" min-width="140" />
          <el-table-column prop="category" label="类别" width="90">
            <template #default="{ row }">
              <el-tag size="small" effect="plain">{{ row.category || '其他' }}</el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="department" label="所属科室" width="100" />
          <el-table-column label="价格(元)" width="120" align="right">
            <template #default="{ row }">
              <span class="price-cell">¥{{ row.price?.toFixed(2) }}</span>
            </template>
          </el-table-column>
          <el-table-column prop="duration" label="时长(分)" width="90" align="right" class-name="col-mono" />
          <el-table-column prop="feeItem" label="收费科目" width="100">
            <template #default="{ row }">
              <el-tag v-if="row.feeItem" size="small" type="warning" effect="light">{{ row.feeItem }}</el-tag>
              <span v-else class="muted">未绑定</span>
            </template>
          </el-table-column>
          <el-table-column prop="description" label="服务描述" min-width="160" show-overflow-tooltip />
          <el-table-column prop="status" label="状态" width="80" align="center">
            <template #default="{ row }">
              <el-switch
                :model-value="row.status === 1"
                active-text="启用"
                inactive-text="禁用"
                inline-prompt
                @change="(v) => toggleStatus(row, v)"
              />
            </template>
          </el-table-column>
          <el-table-column label="操作" width="120" fixed="right" align="center">
            <template #default="{ row }">
              <el-button link type="primary" @click="openDialog(row)">编辑</el-button>
            </template>
          </el-table-column>
        </el-table>

        <AppPagination
          v-model:page="query.page"
          v-model:page-size="query.pageSize"
          :total="total"
          @change="loadData"
        />
      </main>
    </div>

    <FormDialog v-model:visible="dialogVisible" :title="form.id ? '编辑服务' : '新增服务'" width="560px" @confirm="handleSubmit">
      <el-form ref="formRef" :model="form" :rules="rules" label-width="100px">
        <el-form-item label="服务名称" prop="serviceName"><el-input v-model="form.serviceName" /></el-form-item>
        <el-form-item label="服务类别" prop="category">
          <el-select v-model="form.category" style="width: 100%">
            <el-option v-for="c in SERVICE_CATEGORIES.filter((x) => x.id !== 'all')" :key="c.id" :label="c.label" :value="c.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="所属科室" prop="department">
          <DictSelect v-model="form.department" dict-key="departments" />
        </el-form-item>
        <el-form-item label="价格(元)" prop="price">
          <el-input-number v-model="form.price" :min="0" :precision="2" controls-position="right" style="width: 100%" />
        </el-form-item>
        <el-form-item label="时长(分钟)" prop="duration">
          <el-input-number v-model="form.duration" :min="1" style="width: 100%" />
        </el-form-item>
        <el-form-item label="收费项绑定" prop="feeItem">
          <DictSelect v-model="form.feeItem" dict-key="paymentItems" placeholder="映射财务科目" />
          <div class="form-hint">启用后将同步至前台缴费系统对应收费项</div>
        </el-form-item>
        <el-form-item label="描述"><el-input v-model="form.description" type="textarea" :rows="3" /></el-form-item>
      </el-form>
    </FormDialog>
  </PageContainer>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { serviceApi } from '@/api'
import { SERVICE_CATEGORIES } from '@/constants/hr'

const loading = ref(false)
const tableData = ref([])
const allServices = ref([])
const total = ref(0)
const activeCategory = ref('all')
const dialogVisible = ref(false)
const formRef = ref()
const query = reactive({ page: 1, pageSize: 10, category: '' })

const form = reactive({
  id: null,
  serviceName: '',
  category: '检查',
  department: '',
  price: 0,
  duration: 30,
  feeItem: '',
  description: '',
  status: 1
})

const rules = {
  serviceName: [{ required: true, message: '请输入服务名称', trigger: 'blur' }],
  category: [{ required: true, message: '请选择类别', trigger: 'change' }],
  feeItem: [{ required: true, message: '请绑定收费科目', trigger: 'change' }]
}

function categoryCount(catId) {
  return allServices.value.filter((s) => s.category === catId).length
}

function selectCategory(id) {
  activeCategory.value = id
  query.category = id === 'all' ? '' : id
  query.page = 1
  loadData()
}

async function loadData() {
  loading.value = true
  try {
    const res = await serviceApi.list({ ...query, pageSize: 100 })
    allServices.value = res.data.list || []
    let list = allServices.value
    if (query.category) list = list.filter((s) => s.category === query.category)
    total.value = list.length
    const start = (query.page - 1) * query.pageSize
    tableData.value = list.slice(start, start + query.pageSize)
  } finally {
    loading.value = false
  }
}

function openDialog(row) {
  Object.assign(form, row || {
    id: null,
    serviceName: '',
    category: activeCategory.value === 'all' ? '检查' : activeCategory.value,
    department: '内科',
    price: 0,
    duration: 30,
    feeItem: '',
    description: '',
    status: 1
  })
  dialogVisible.value = true
}

async function handleSubmit() {
  await formRef.value.validate()
  if (form.id) await serviceApi.update(form.id, form)
  else await serviceApi.create(form)
  ElMessage.success(form.feeItem ? '保存成功，收费项已绑定' : '保存成功')
  dialogVisible.value = false
  loadData()
}

async function toggleStatus(row, enabled) {
  const status = enabled ? 1 : 0
  if (enabled && !row.feeItem) {
    ElMessage.warning('请先绑定收费科目后再启用')
    return
  }
  await serviceApi.toggle(row.id, status)
  row.status = status
  ElMessage.success(status === 1 ? '已启用，前台缴费系统已同步' : '已禁用')
}

onMounted(loadData)
</script>

<style scoped>
.service-layout {
  display: flex;
  gap: 16px;
  min-height: 500px;
}
.service-tree {
  width: 200px;
  flex-shrink: 0;
  padding: 12px;
  border-radius: 12px;
  background: #fff;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.04);
  border: 1px solid var(--feishu-border-light);
}
.tree-item {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 12px 14px;
  margin-bottom: 4px;
  border-radius: 8px;
  cursor: pointer;
  font-size: 14px;
  transition: all 0.2s;
}
.tree-item:hover { background: var(--feishu-bg-sub); }
.tree-item.active {
  background: var(--feishu-primary-bg);
  color: var(--feishu-primary);
  font-weight: 600;
}
.service-main {
  flex: 1;
  padding: 16px;
  border-radius: 12px;
  background: #fff;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.04);
  border: 1px solid var(--feishu-border-light);
}
.price-cell {
  font-family: 'DIN Alternate', 'Helvetica Neue', monospace;
  font-weight: 700;
  font-size: 15px;
  color: #e6a23c;
  font-variant-numeric: tabular-nums;
}
.muted { color: var(--feishu-text-tertiary); font-size: 12px; }
.form-hint {
  font-size: 12px;
  color: var(--feishu-text-tertiary);
  margin-top: 4px;
}
:deep(.col-mono) { font-variant-numeric: tabular-nums; }
</style>
