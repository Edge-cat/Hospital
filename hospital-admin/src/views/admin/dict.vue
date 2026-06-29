<template>
  <ListPage
    title="数据字典"
    :query="query"
    :loading="loading"
    :data="tableData"
    :total="total"
    @search="loadData"
    @reset="resetQuery"
    @page-change="loadData"
  >
    <template #actions>
      <el-button type="primary" @click="openDialog()">新增字典</el-button>
    </template>
    <template #filters>
      <el-form-item label="字典类型">
        <el-input v-model="query.dictType" placeholder="如 gender" clearable />
      </el-form-item>
    </template>

    <el-table-column prop="dictType" label="字典类型" width="140" />
    <el-table-column prop="dictLabel" label="字典标签" width="140" />
    <el-table-column prop="dictValue" label="字典值" width="120" />
    <el-table-column prop="sort" label="排序" width="70" />
    <el-table-column prop="status" label="状态" width="80">
      <template #default="{ row }">
        <StatusTag enable :value="row.status" />
      </template>
    </el-table-column>
    <el-table-column prop="remark" label="备注" min-width="160" show-overflow-tooltip />
    <el-table-column label="操作" width="160" fixed="right">
      <template #default="{ row }">
        <el-button link type="primary" @click="openDialog(row)">编辑</el-button>
        <el-button link type="danger" @click="handleDelete(row)">删除</el-button>
      </template>
    </el-table-column>

    <template #extra>
      <FormDialog v-model:visible="dialogVisible" :title="form.id ? '编辑字典' : '新增字典'" width="480px" @confirm="handleSubmit">
        <el-form ref="formRef" :model="form" :rules="rules" label-width="90px">
          <el-form-item label="字典类型" prop="dictType"><el-input v-model="form.dictType" /></el-form-item>
          <el-form-item label="字典标签" prop="dictLabel"><el-input v-model="form.dictLabel" /></el-form-item>
          <el-form-item label="字典值" prop="dictValue"><el-input v-model="form.dictValue" /></el-form-item>
          <el-form-item label="排序"><el-input-number v-model="form.sort" :min="0" /></el-form-item>
          <el-form-item label="备注"><el-input v-model="form.remark" /></el-form-item>
        </el-form>
      </FormDialog>
    </template>
  </ListPage>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { adminDictApi } from '@/api'

const loading = ref(false)
const tableData = ref([])
const total = ref(0)
const dialogVisible = ref(false)
const formRef = ref()
const query = reactive({ dictType: '', page: 1, pageSize: 10 })
const form = reactive({ id: null, dictType: '', dictLabel: '', dictValue: '', sort: 0, remark: '', status: 1 })
const rules = {
  dictType: [{ required: true, message: '请输入字典类型', trigger: 'blur' }],
  dictLabel: [{ required: true, message: '请输入字典标签', trigger: 'blur' }]
}

function resetQuery() {
  query.dictType = ''
  query.page = 1
  loadData()
}

async function loadData() {
  loading.value = true
  try { const res = await adminDictApi.list(query); tableData.value = res.data.list; total.value = res.data.total }
  finally { loading.value = false }
}

function openDialog(row) {
  Object.assign(form, row || { id: null, dictType: '', dictLabel: '', dictValue: '', sort: 0, remark: '', status: 1 })
  dialogVisible.value = true
}

async function handleSubmit() {
  await formRef.value.validate()
  if (form.id) await adminDictApi.update(form.id, form)
  else await adminDictApi.create(form)
  ElMessage.success('操作成功')
  dialogVisible.value = false
  loadData()
}

async function handleDelete(row) {
  await ElMessageBox.confirm('确定删除该字典项吗？', '提示', { type: 'warning' })
  await adminDictApi.remove(row.id)
  ElMessage.success('删除成功')
  loadData()
}

onMounted(loadData)
</script>
