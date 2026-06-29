<template>

  <ListPage

    title="挂号管理"

    subtitle="窗口挂号、退号与挂号统计"

    :query="query"

    :loading="loading"

    :data="tableData"

    :total="total"

    @search="loadData"

    @reset="resetQuery"

    @page-change="loadData"

  >

    <template #actions>

      <el-button type="primary" @click="openDialog()">窗口挂号</el-button>

    </template>

    <template #stats>

      <StatOverview :items="statItems" />

    </template>

    <template #filters>

      <el-form-item label="患者姓名">

        <el-input v-model="query.keyword" placeholder="姓名/挂号单号" clearable />

      </el-form-item>

      <el-form-item label="科室">

        <DictSelect v-model="query.department" dict-key="departments" placeholder="全部" clearable width="120px" />

      </el-form-item>

    </template>



    <el-table-column prop="registerNo" label="挂号单号" width="140" />

    <el-table-column prop="patientName" label="患者姓名" width="100" />

    <el-table-column prop="department" label="科室" width="100" />

    <el-table-column prop="doctorName" label="医生" width="100" />

    <el-table-column prop="registerType" label="号别" width="100" />

    <el-table-column prop="fee" label="挂号费(元)" width="100" />

    <el-table-column prop="status" label="状态" width="90">

      <template #default="{ row }">

        <StatusTag enum-key="registerStatus" :value="row.status" />

      </template>

    </el-table-column>

    <el-table-column prop="registerTime" label="挂号时间" width="170" />

    <el-table-column label="操作" width="100" fixed="right">

      <template #default="{ row }">

        <el-button v-if="row.status < 2" link type="danger" @click="handleCancel(row)">退号</el-button>

        <el-tag v-else type="info">—</el-tag>

      </template>

    </el-table-column>



    <template #extra>

      <FormDialog v-model:visible="dialogVisible" title="窗口挂号" confirm-text="确认挂号" @confirm="handleSubmit">

        <el-form ref="formRef" :model="form" :rules="rules" label-width="90px">

          <el-form-item label="患者姓名" prop="patientName"><el-input v-model="form.patientName" /></el-form-item>

          <el-form-item label="科室" prop="department">

            <DictSelect v-model="form.department" dict-key="departments" />

          </el-form-item>

          <el-form-item label="医生" prop="doctorName"><el-input v-model="form.doctorName" /></el-form-item>

          <el-form-item label="号别" prop="registerType">

            <DictSelect v-model="form.registerType" dict-key="registerTypes" />

          </el-form-item>

        </el-form>

      </FormDialog>

    </template>

  </ListPage>

</template>



<script setup>

import { ref, reactive, computed, onMounted } from 'vue'

import { ElMessage, ElMessageBox } from 'element-plus'

import { registerApi } from '@/api'

import { STAT_COLORS } from '@/constants'



const loading = ref(false)

const tableData = ref([])

const total = ref(0)

const dialogVisible = ref(false)

const formRef = ref()

const stats = reactive({ today: 0, waiting: 0, done: 0, cancelled: 0 })

const query = reactive({ keyword: '', department: '', page: 1, pageSize: 10 })

const form = reactive({ patientName: '', department: '', doctorName: '', registerType: '普通号' })

const rules = {

  patientName: [{ required: true, message: '请输入患者姓名', trigger: 'blur' }],

  department: [{ required: true, message: '请选择科室', trigger: 'change' }]

}



const statItems = computed(() => [

  { label: '今日挂号', value: stats.today, color: STAT_COLORS.primary },

  { label: '待就诊', value: stats.waiting, color: STAT_COLORS.warning },

  { label: '已就诊', value: stats.done, color: STAT_COLORS.success },

  { label: '已退号', value: stats.cancelled, color: STAT_COLORS.neutral }

])



function resetQuery() {

  query.keyword = ''

  query.department = ''

  query.page = 1

  loadData()

}



async function loadData() {

  loading.value = true

  try {

    const res = await registerApi.list(query)

    tableData.value = res.data.list

    total.value = res.data.total

    stats.today = res.data.total

    stats.waiting = tableData.value.filter((r) => r.status === 0).length

    stats.done = tableData.value.filter((r) => r.status === 2).length

    stats.cancelled = tableData.value.filter((r) => r.status === 3).length

  } finally {

    loading.value = false

  }

}



function openDialog() {

  Object.assign(form, { patientName: '', department: '', doctorName: '', registerType: '普通号' })

  dialogVisible.value = true

}



async function handleSubmit() {

  await formRef.value.validate()

  await registerApi.create(form)

  ElMessage.success('挂号成功')

  dialogVisible.value = false

  loadData()

}



async function handleCancel(row) {

  await ElMessageBox.confirm(`确认为「${row.patientName}」办理退号？`, '退号', { type: 'warning' })

  await registerApi.cancel(row.id)

  ElMessage.success('退号成功')

  loadData()

}



onMounted(loadData)

</script>


