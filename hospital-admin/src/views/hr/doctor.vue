<template>
  <ListPage
    title="医生信息管理"
    subtitle="医护档案中枢 · 人员录入-资质排班-绩效追踪"
    :query="query"
    :loading="loading"
    :data="tableData"
    :total="total"
    @search="loadData"
    @reset="resetQuery"
    @page-change="loadData"
  >
    <template #actions>
      <el-button @click="handleExport">导出</el-button>
      <el-button @click="triggerImport">批量导入</el-button>
      <input ref="fileInput" type="file" accept=".csv" hidden @change="handleImport" />
      <el-button type="primary" @click="openDialog()">新增医生</el-button>
    </template>

    <template #filters>
      <el-form-item label="关键词">
        <el-input v-model="query.keyword" placeholder="姓名/工号" clearable />
      </el-form-item>
      <el-form-item label="科室">
        <DictSelect v-model="query.department" dict-key="departments" placeholder="全部" clearable width="120px" />
      </el-form-item>
    </template>

    <el-table-column label="医护" min-width="200">
      <template #default="{ row }">
        <div class="doctor-cell">
          <el-avatar :size="40" class="doctor-avatar">{{ row.name?.charAt(0) }}</el-avatar>
          <div>
            <div class="doctor-cell__name">{{ row.name }}</div>
            <div class="doctor-cell__no">{{ row.doctorNo }}</div>
          </div>
        </div>
      </template>
    </el-table-column>
    <el-table-column prop="gender" label="性别" width="64" align="center">
      <template #default="{ row }">{{ row.gender === 1 ? '男' : '女' }}</template>
    </el-table-column>
    <el-table-column prop="department" label="科室" width="90" />
    <el-table-column prop="title" label="职称" width="110" />
    <el-table-column label="专长" min-width="140">
      <template #default="{ row }">
        <el-tag
          v-if="row.specialty"
          size="small"
          :type="SPECIALTY_TAG_TYPE[row.specialty] || 'info'"
          effect="light"
        >{{ row.specialty }}</el-tag>
      </template>
    </el-table-column>
    <el-table-column prop="phone" label="联系电话" width="130" class-name="col-mono" />
    <el-table-column prop="status" label="状态" width="80" align="center">
      <template #default="{ row }">
        <StatusTag :type="row.status === 1 ? 'success' : 'info'" :label="row.status === 1 ? '在职' : '离职'" />
      </template>
    </el-table-column>
    <el-table-column label="操作" width="220" fixed="right" align="center">
      <template #default="{ row }">
        <el-button link type="primary" @click="goSchedule(row)">排班设置</el-button>
        <el-button link type="primary" @click="goRecords(row)">接诊历史</el-button>
        <el-button link type="primary" @click="openDialog(row)">编辑</el-button>
      </template>
    </el-table-column>

    <template #extra>
      <FormDialog v-model:visible="dialogVisible" :title="form.id ? '编辑医生' : '新增医生'" @confirm="handleSubmit">
        <el-form ref="formRef" :model="form" :rules="rules" label-width="90px">
          <el-form-item label="姓名" prop="name"><el-input v-model="form.name" /></el-form-item>
          <el-form-item label="性别" prop="gender">
            <el-radio-group v-model="form.gender">
              <el-radio :value="1">男</el-radio>
              <el-radio :value="2">女</el-radio>
            </el-radio-group>
          </el-form-item>
          <el-form-item label="科室" prop="department">
            <DictSelect v-model="form.department" dict-key="departments" />
          </el-form-item>
          <el-form-item label="职称" prop="title"><el-input v-model="form.title" /></el-form-item>
          <el-form-item label="专业方向" prop="specialty"><el-input v-model="form.specialty" /></el-form-item>
          <el-form-item label="联系电话" prop="phone"><el-input v-model="form.phone" /></el-form-item>
        </el-form>
      </FormDialog>
    </template>
  </ListPage>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { doctorApi } from '@/api'
import { exportCsv, parseCsv } from '@/utils/csv'
import { SPECIALTY_TAG_TYPE } from '@/constants/hr'

const router = useRouter()
const loading = ref(false)
const tableData = ref([])
const total = ref(0)
const dialogVisible = ref(false)
const formRef = ref()
const fileInput = ref()
const query = reactive({ keyword: '', department: '', page: 1, pageSize: 10 })
const form = reactive({ id: null, name: '', gender: 1, department: '', title: '', specialty: '', phone: '' })
const rules = {
  name: [{ required: true, message: '请输入姓名', trigger: 'blur' }],
  department: [{ required: true, message: '请选择科室', trigger: 'change' }]
}

const exportColumns = [
  { key: 'doctorNo', label: '工号' },
  { key: 'name', label: '姓名' },
  { key: 'gender', label: '性别' },
  { key: 'department', label: '科室' },
  { key: 'title', label: '职称' },
  { key: 'specialty', label: '专业方向' },
  { key: 'phone', label: '联系电话' }
]

function resetQuery() {
  query.keyword = ''
  query.department = ''
  query.page = 1
  loadData()
}

async function loadData() {
  loading.value = true
  try {
    const res = await doctorApi.list(query)
    tableData.value = res.data.list
    total.value = res.data.total
  } finally {
    loading.value = false
  }
}

function openDialog(row) {
  Object.assign(form, row || { id: null, name: '', gender: 1, department: '', title: '', specialty: '', phone: '' })
  dialogVisible.value = true
}

async function handleSubmit() {
  await formRef.value.validate()
  if (form.id) await doctorApi.update(form.id, form)
  else await doctorApi.create(form)
  ElMessage.success('操作成功')
  dialogVisible.value = false
  loadData()
}

function goSchedule(row) {
  router.push({ path: '/hr/schedule', query: { doctorName: row.name, department: row.department } })
}

function goRecords(row) {
  router.push({ path: '/hr/record', query: { doctorName: row.name } })
}

function handleExport() {
  const rows = tableData.value.map((r) => ({
    ...r,
    gender: r.gender === 1 ? '男' : '女'
  }))
  exportCsv(`医生档案_${Date.now()}.csv`, rows, exportColumns)
  ElMessage.success('导出成功')
}

function triggerImport() {
  fileInput.value?.click()
}

async function handleImport(e) {
  const file = e.target.files?.[0]
  if (!file) return
  const text = await file.text()
  const list = parseCsv(text)
  if (!list.length) {
    ElMessage.warning('未解析到有效数据')
    return
  }
  await doctorApi.batchImport(list)
  ElMessage.success(`成功导入 ${list.length} 条`)
  e.target.value = ''
  loadData()
}

onMounted(loadData)
</script>

<style scoped>
.doctor-cell {
  display: flex;
  align-items: center;
  gap: 12px;
}
.doctor-avatar {
  background: linear-gradient(135deg, #409eff, #79bbff);
  color: #fff;
  font-weight: 600;
  flex-shrink: 0;
}
.doctor-cell__name {
  font-weight: 600;
  font-size: 14px;
}
.doctor-cell__no {
  font-size: 12px;
  color: var(--feishu-text-tertiary);
  font-variant-numeric: tabular-nums;
}
:deep(.col-mono) { font-variant-numeric: tabular-nums; }
</style>
