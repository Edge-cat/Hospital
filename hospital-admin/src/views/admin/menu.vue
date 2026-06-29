<template>
  <PageContainer>
    <PageHeader title="菜单权限" subtitle="管理系统菜单与路由配置">
      <el-button type="primary" @click="openDialog()">新增菜单</el-button>
    </PageHeader>

    <TableCard :loading="loading" :data="menuTree" row-key="id" default-expand-all>
      <el-table-column prop="name" label="菜单名称" width="180" />
      <el-table-column prop="path" label="路由路径" width="200" />
      <el-table-column prop="icon" label="图标" width="100" />
      <el-table-column prop="sort" label="排序" width="70" />
      <el-table-column prop="status" label="状态" width="80">
        <template #default="{ row }">
          <StatusTag :type="row.status === 1 ? 'success' : 'info'" :label="row.status === 1 ? '显示' : '隐藏'" />
        </template>
      </el-table-column>
      <el-table-column label="操作" width="160" fixed="right">
        <template #default="{ row }">
          <el-button link type="primary" @click="openDialog(row)">编辑</el-button>
          <el-button link type="danger" @click="handleDelete(row)">删除</el-button>
        </template>
      </el-table-column>
    </TableCard>

    <FormDialog v-model:visible="dialogVisible" :title="form.id ? '编辑菜单' : '新增菜单'" @confirm="handleSubmit">
      <el-form ref="formRef" :model="form" :rules="rules" label-width="90px">
        <el-form-item label="菜单名称" prop="name"><el-input v-model="form.name" /></el-form-item>
        <el-form-item label="路由路径" prop="path"><el-input v-model="form.path" /></el-form-item>
        <el-form-item label="图标"><el-input v-model="form.icon" placeholder="Element Plus 图标名" /></el-form-item>
        <el-form-item label="排序"><el-input-number v-model="form.sort" :min="0" /></el-form-item>
        <el-form-item label="状态"><el-switch v-model="form.status" :active-value="1" :inactive-value="0" /></el-form-item>
      </el-form>
    </FormDialog>
  </PageContainer>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { adminMenuApi } from '@/api'

const loading = ref(false)
const menuTree = ref([])
const dialogVisible = ref(false)
const formRef = ref()
const form = reactive({ id: null, name: '', path: '', icon: '', sort: 0, status: 1, parentId: 0 })
const rules = { name: [{ required: true, message: '请输入菜单名称', trigger: 'blur' }] }

async function loadData() {
  loading.value = true
  try {
    const res = await adminMenuApi.tree()
    menuTree.value = res.data
  } finally {
    loading.value = false
  }
}

function openDialog(row) {
  Object.assign(form, row || { id: null, name: '', path: '', icon: '', sort: 0, status: 1, parentId: 0 })
  dialogVisible.value = true
}

async function handleSubmit() {
  await formRef.value.validate()
  if (form.id) await adminMenuApi.update(form.id, form)
  else await adminMenuApi.create(form)
  ElMessage.success('操作成功')
  dialogVisible.value = false
  loadData()
}

async function handleDelete(row) {
  await ElMessageBox.confirm('确定删除该菜单吗？', '提示', { type: 'warning' })
  await adminMenuApi.remove(row.id)
  ElMessage.success('删除成功')
  loadData()
}

onMounted(loadData)
</script>
