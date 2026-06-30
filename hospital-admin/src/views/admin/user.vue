<template>
  <ListPage
    title="用户管理"
    subtitle="由系统管理员创建医生、护士等内部账号"
    :query="query"
    :loading="loading"
    :data="tableData"
    :total="total"
    @search="loadData"
    @reset="resetQuery"
    @page-change="loadData"
  >
    <template #actions>
      <el-button type="primary" @click="openUserDialog()">新增用户</el-button>
    </template>
    <template #filters>
      <el-form-item label="关键词">
        <el-input v-model="query.keyword" placeholder="用户名/姓名" clearable />
      </el-form-item>
      <el-form-item label="状态">
        <DictSelect v-model="query.status" dict-key="enableStatus" placeholder="全部" clearable width="100px" />
      </el-form-item>
    </template>

    <el-table-column prop="username" label="用户名" width="120" />
    <el-table-column prop="name" label="姓名" width="100" />
    <el-table-column prop="roleLabel" label="角色" width="120" />
    <el-table-column prop="department" label="所属部门" width="120" />
    <el-table-column prop="phone" label="手机号" width="130" />
    <el-table-column prop="status" label="状态" width="80">
      <template #default="{ row }">
        <StatusTag enable :value="row.status" />
      </template>
    </el-table-column>
    <el-table-column prop="createTime" label="创建时间" width="170" />
    <el-table-column label="操作" width="220" fixed="right">
      <template #default="{ row }">
        <el-button link type="primary" @click="openUserDialog(row)">编辑</el-button>
        <el-button link type="warning" @click="handleResetPwd(row)">重置密码</el-button>
        <el-button link type="danger" @click="handleDelete(row)">删除</el-button>
      </template>
    </el-table-column>

    <template #extra>
      <FormDialog v-model:visible="dialogVisible" :title="form.id ? '编辑用户' : '新增用户'" @confirm="handleSubmit">
        <el-form ref="formRef" :model="form" :rules="rules" label-width="90px">
          <el-form-item label="用户名" prop="username">
            <el-input v-model="form.username" :disabled="!!form.id" placeholder="登录账号，不可重复" />
          </el-form-item>
          <el-form-item label="姓名" prop="name"><el-input v-model="form.name" /></el-form-item>
          <el-form-item label="角色" prop="roleKey">
            <DictSelect v-model="form.roleKey" dict-key="staffRoles" />
          </el-form-item>
          <el-form-item label="部门" prop="department">
            <DictSelect v-model="form.department" dict-key="departments" placeholder="选填" clearable />
          </el-form-item>
          <el-form-item label="手机号" prop="phone"><el-input v-model="form.phone" /></el-form-item>
          <el-form-item v-if="!form.id" label="初始密码">
            <el-input v-model="form.password" type="password" show-password placeholder="留空则默认 123456" />
            <p class="form-hint">新建账号默认密码为 123456，建议创建后通知用户修改</p>
          </el-form-item>
          <el-form-item label="状态">
            <el-switch v-model="form.status" :active-value="1" :inactive-value="0" />
          </el-form-item>
        </el-form>
      </FormDialog>
    </template>
  </ListPage>
</template>

<script setup>
import { reactive } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { adminUserApi } from '@/api'
import { useCrudPage } from '@/composables/useCrudPage'

const emptyForm = {
  id: null,
  username: '',
  name: '',
  roleKey: 'doctor',
  department: '',
  phone: '',
  password: '',
  status: 1
}

const rules = {
  username: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
  name: [{ required: true, message: '请输入姓名', trigger: 'blur' }],
  roleKey: [{ required: true, message: '请选择角色', trigger: 'change' }]
}

const { loading, tableData, total, dialogVisible, formRef, query, form, loadData, resetQuery, openDialog, handleDelete } =
  useCrudPage(adminUserApi, { keyword: '', status: '' }, emptyForm)

function resolveRoleKey(row) {
  if (row.role === 'admin' && row.roleLabel === '财务') return 'finance'
  if (row.role === 'nurse' && row.roleLabel === '药师') return 'pharmacy'
  return row.role || 'doctor'
}

function openUserDialog(row) {
  if (row) {
    openDialog({ ...row, roleKey: resolveRoleKey(row), password: '' })
  } else {
    openDialog(null, emptyForm)
  }
}

async function handleSubmit() {
  await formRef.value.validate()
  const payload = {
    username: form.username,
    name: form.name,
    roleKey: form.roleKey,
    department: form.department,
    phone: form.phone,
    status: form.status
  }
  if (!form.id && form.password) payload.password = form.password
  if (form.id) await adminUserApi.update(form.id, payload)
  else await adminUserApi.create(payload)
  ElMessage.success('操作成功')
  dialogVisible.value = false
  loadData()
}

async function handleResetPwd(row) {
  await ElMessageBox.confirm(`确定重置用户「${row.username}」的密码？`, '提示', { type: 'warning' })
  await adminUserApi.resetPassword(row.id)
  ElMessage.success('密码已重置为 123456')
}
</script>

<style scoped>
.form-hint {
  margin: 6px 0 0;
  font-size: 12px;
  color: var(--feishu-text-tertiary);
  line-height: 1.5;
}
</style>
