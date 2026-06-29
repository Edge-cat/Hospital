<template>
  <PageContainer>
    <PageHeader title="科室管理" subtitle="维护医院科室组织架构">
      <el-button type="primary" @click="openDialog()">新增科室</el-button>
    </PageHeader>

    <TableCard :loading="loading" :data="tableData" row-key="id" default-expand-all>
      <el-table-column prop="name" label="科室名称" width="180" />
      <el-table-column prop="code" label="科室编码" width="120" />
      <el-table-column prop="leader" label="负责人" width="100" />
      <el-table-column prop="phone" label="联系电话" width="140" />
      <el-table-column prop="sort" label="排序" width="70" />
      <el-table-column prop="status" label="状态" width="80">
        <template #default="{ row }">
          <StatusTag enable :value="row.status" />
        </template>
      </el-table-column>
      <el-table-column label="操作" width="160" fixed="right">
        <template #default="{ row }">
          <el-button link type="primary" @click="openDialog(row)">编辑</el-button>
          <el-button link type="danger" @click="handleDelete(row)">删除</el-button>
        </template>
      </el-table-column>
    </TableCard>

    <FormDialog v-model:visible="dialogVisible" :title="form.id ? '编辑科室' : '新增科室'" width="480px" @confirm="handleSubmit">
      <el-form ref="formRef" :model="form" :rules="rules" label-width="90px">
        <el-form-item label="科室名称" prop="name"><el-input v-model="form.name" /></el-form-item>
        <el-form-item label="科室编码" prop="code"><el-input v-model="form.code" /></el-form-item>
        <el-form-item label="上级科室">
          <el-select v-model="form.parentId" placeholder="无（顶级科室）" clearable style="width: 100%">
            <el-option v-for="d in topDepartments" :key="d.id" :label="d.name" :value="d.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="负责人"><el-input v-model="form.leader" /></el-form-item>
        <el-form-item label="联系电话"><el-input v-model="form.phone" /></el-form-item>
        <el-form-item label="排序"><el-input-number v-model="form.sort" :min="0" /></el-form-item>
      </el-form>
    </FormDialog>
  </PageContainer>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { adminDepartmentApi } from '@/api'

const loading = ref(false)
const tableData = ref([])
const dialogVisible = ref(false)
const formRef = ref()
const form = reactive({ id: null, name: '', code: '', parentId: 0, leader: '', phone: '', sort: 0, status: 1 })
const rules = { name: [{ required: true, message: '请输入科室名称', trigger: 'blur' }] }
const topDepartments = computed(() => tableData.value.filter((d) => d.parentId === 0))

async function loadData() {
  loading.value = true
  try {
    const res = await adminDepartmentApi.list({})
    tableData.value = buildTree(res.data.list)
  } finally {
    loading.value = false
  }
}

function buildTree(list) {
  const roots = list.filter((d) => d.parentId === 0)
  return roots.map((r) => ({
    ...r,
    children: list.filter((d) => d.parentId === r.id)
  }))
}

function openDialog(row) {
  Object.assign(form, row || { id: null, name: '', code: '', parentId: 0, leader: '', phone: '', sort: 0, status: 1 })
  dialogVisible.value = true
}

async function handleSubmit() {
  await formRef.value.validate()
  if (form.id) await adminDepartmentApi.update(form.id, form)
  else await adminDepartmentApi.create(form)
  ElMessage.success('操作成功')
  dialogVisible.value = false
  loadData()
}

async function handleDelete(row) {
  await ElMessageBox.confirm('确定删除该科室吗？', '提示', { type: 'warning' })
  await adminDepartmentApi.remove(row.id)
  ElMessage.success('删除成功')
  loadData()
}

onMounted(loadData)
</script>
