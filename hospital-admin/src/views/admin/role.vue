<template>
  <ListPage
    title="角色管理"
    :query="query"
    :loading="loading"
    :data="tableData"
    :total="total"
    :show-search="false"
    @search="loadData"
    @reset="resetQuery"
    @page-change="loadData"
  >
    <template #actions>
      <el-button type="primary" @click="openDialog()">新增角色</el-button>
    </template>

    <el-table-column prop="roleName" label="角色名称" width="140" />
    <el-table-column prop="roleCode" label="角色编码" width="120" />
    <el-table-column prop="description" label="描述" min-width="200" show-overflow-tooltip />
    <el-table-column prop="status" label="状态" width="80">
      <template #default="{ row }">
        <StatusTag enable :value="row.status" />
      </template>
    </el-table-column>
    <el-table-column prop="createTime" label="创建时间" width="170" />
    <el-table-column label="操作" width="160" fixed="right">
      <template #default="{ row }">
        <el-button link type="primary" @click="openDialog(row)">编辑</el-button>
        <el-button link type="danger" :disabled="row.roleCode === 'admin'" @click="handleDelete(row)">删除</el-button>
      </template>
    </el-table-column>

    <template #extra>
      <FormDialog v-model:visible="dialogVisible" :title="form.id ? '编辑角色' : '新增角色'" width="520px" @confirm="handleSubmit">
        <el-form ref="formRef" :model="form" :rules="rules" label-width="90px">
          <el-form-item label="角色名称" prop="roleName"><el-input v-model="form.roleName" /></el-form-item>
          <el-form-item label="角色编码" prop="roleCode"><el-input v-model="form.roleCode" :disabled="!!form.id" /></el-form-item>
          <el-form-item label="描述"><el-input v-model="form.description" type="textarea" /></el-form-item>
          <el-form-item label="状态"><el-switch v-model="form.status" :active-value="1" :inactive-value="0" /></el-form-item>
          <el-form-item label="菜单权限">
            <el-tree ref="treeRef" :data="menuTree" show-checkbox node-key="id" :props="{ label: 'name', children: 'children' }" default-expand-all />
          </el-form-item>
        </el-form>
      </FormDialog>
    </template>
  </ListPage>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { adminRoleApi, adminMenuApi } from '@/api'

const loading = ref(false)
const tableData = ref([])
const total = ref(0)
const dialogVisible = ref(false)
const formRef = ref()
const menuTree = ref([])
const query = reactive({ page: 1, pageSize: 10 })
const form = reactive({ id: null, roleName: '', roleCode: '', description: '', status: 1 })
const rules = {
  roleName: [{ required: true, message: '请输入角色名称', trigger: 'blur' }],
  roleCode: [{ required: true, message: '请输入角色编码', trigger: 'blur' }]
}

function resetQuery() {
  query.page = 1
  loadData()
}

async function loadData() {
  loading.value = true
  try { const res = await adminRoleApi.list(query); tableData.value = res.data.list; total.value = res.data.total }
  finally { loading.value = false }
}

async function loadMenuTree() {
  const res = await adminMenuApi.tree()
  menuTree.value = res.data
}

function openDialog(row) {
  Object.assign(form, row || { id: null, roleName: '', roleCode: '', description: '', status: 1 })
  dialogVisible.value = true
}

async function handleSubmit() {
  await formRef.value.validate()
  if (form.id) await adminRoleApi.update(form.id, form)
  else await adminRoleApi.create(form)
  ElMessage.success('操作成功')
  dialogVisible.value = false
  loadData()
}

async function handleDelete(row) {
  await ElMessageBox.confirm('确定删除该角色吗？', '提示', { type: 'warning' })
  await adminRoleApi.remove(row.id)
  ElMessage.success('删除成功')
  loadData()
}

onMounted(() => { loadData(); loadMenuTree() })
</script>
