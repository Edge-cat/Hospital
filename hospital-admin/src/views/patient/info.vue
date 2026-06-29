<template>
  <ListPage
    title="患者信息管理"
    subtitle="维护患者档案与基本信息"
    :query="query"
    :loading="loading"
    :data="tableData"
    :total="total"
    :table-props="{ 'row-class-name': () => 'clickable-row' }"
    @search="loadData"
    @reset="resetQuery"
    @page-change="loadData"
    @row-click="onRowClick"
    @selection-change="onSelectionChange"
  >
    <template #actions>
      <el-button
        v-if="selectedRows.length"
        type="danger"
        plain
        @click="handleBatchDelete"
      >批量删除 ({{ selectedRows.length }})</el-button>
      <el-button type="primary" @click="openDialog()">新增患者</el-button>
    </template>

    <template #filters>
      <el-form-item label="关键词">
        <el-input v-model="query.keyword" placeholder="姓名/编号/手机号" clearable />
      </el-form-item>
    </template>

    <el-table-column type="selection" width="48" />
    <el-table-column prop="patientNo" label="编号" width="120" class-name="col-mono" />
    <el-table-column prop="name" label="姓名" width="100" />
    <el-table-column prop="gender" label="性别" width="64" align="center">
      <template #default="{ row }">{{ row.gender === 1 ? '男' : '女' }}</template>
    </el-table-column>
    <el-table-column prop="age" label="年龄" width="64" align="right" />
    <el-table-column prop="phone" label="手机号" width="130" class-name="col-mono" />
    <el-table-column prop="department" label="科室" width="90" />
    <el-table-column prop="createTime" label="建档时间" width="170" class-name="col-mono" align="right" />
    <el-table-column label="操作" width="120" fixed="right" align="center">
      <template #default="{ row }">
        <el-button link type="primary" @click.stop="openPanorama(row)">档案</el-button>
        <el-button link type="primary" @click.stop="openDialog(row)">编辑</el-button>
      </template>
    </el-table-column>

    <template #extra>
      <FormDialog v-model:visible="dialogVisible" :title="form.id ? '编辑患者' : '新增患者'" width="560px" @confirm="handleSubmit">
        <el-form ref="formRef" :model="form" :rules="rules" label-width="90px">
          <el-form-item label="姓名" prop="name"><el-input v-model="form.name" /></el-form-item>
          <el-form-item label="性别" prop="gender">
            <el-radio-group v-model="form.gender">
              <el-radio :value="1">男</el-radio>
              <el-radio :value="2">女</el-radio>
            </el-radio-group>
          </el-form-item>
          <el-form-item label="年龄" prop="age"><el-input-number v-model="form.age" :min="0" :max="150" /></el-form-item>
          <el-form-item label="手机号" prop="phone"><el-input v-model="form.phone" /></el-form-item>
          <el-form-item label="身份证号" prop="idCard"><el-input v-model="form.idCard" /></el-form-item>
          <el-form-item label="科室" prop="department">
            <DictSelect v-model="form.department" dict-key="departments" />
          </el-form-item>
          <el-form-item label="地址" prop="address"><el-input v-model="form.address" type="textarea" /></el-form-item>
        </el-form>
      </FormDialog>

      <PatientPanoramaDrawer
        v-model:visible="panoramaVisible"
        :patient="currentPatient"
        show-edit
        @edit="onEditFromPanorama"
      />
    </template>
  </ListPage>
</template>

<script setup>
import { ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { patientApi } from '@/api'
import { useCrudPage } from '@/composables/useCrudPage'
import PatientPanoramaDrawer from '@/components/PatientPanoramaDrawer.vue'

const emptyForm = { id: null, name: '', gender: 1, age: 30, phone: '', idCard: '', department: '内科', address: '' }
const rules = {
  name: [{ required: true, message: '请输入姓名', trigger: 'blur' }],
  phone: [{ required: true, message: '请输入手机号', trigger: 'blur' }]
}

const selectedRows = ref([])
const panoramaVisible = ref(false)
const currentPatient = ref(null)

const { loading, tableData, total, dialogVisible, formRef, query, form, loadData, resetQuery, openDialog, handleSubmit } =
  useCrudPage(patientApi, { keyword: '' }, emptyForm)

function onSelectionChange(rows) {
  selectedRows.value = rows
}

function onRowClick(row, _col, event) {
  if (event.target.closest('.el-checkbox, .el-button')) return
  openPanorama(row)
}

function openPanorama(row) {
  currentPatient.value = row
  panoramaVisible.value = true
}

function onEditFromPanorama(patient) {
  panoramaVisible.value = false
  openDialog(patient)
}

async function handleBatchDelete() {
  if (!selectedRows.value.length) return
  await ElMessageBox.confirm(`确定删除选中的 ${selectedRows.value.length} 条患者档案？`, '批量删除', { type: 'warning' })
  await patientApi.batchRemove(selectedRows.value.map((r) => r.id))
  ElMessage.success('批量删除成功')
  selectedRows.value = []
  loadData()
}
</script>

<style scoped>
:deep(.clickable-row) { cursor: pointer; }
:deep(.col-mono) { font-variant-numeric: tabular-nums; }
</style>
