<template>

  <ListPage

    title="预约管理"

    :query="query"

    :loading="loading"

    :data="tableData"

    :total="total"

    @search="loadData"

    @reset="resetQuery"

    @page-change="loadData"

  >

    <template #actions>

      <el-button type="primary" @click="openDialog()">新增预约</el-button>

    </template>

    <template #filters>

      <el-form-item label="患者姓名">

        <el-input v-model="query.keyword" placeholder="姓名/预约单号" clearable />

      </el-form-item>

      <el-form-item label="状态">

        <el-select v-model="query.status" placeholder="全部" clearable style="width: 120px">

          <el-option v-for="s in appointmentStatusOptions" :key="s.value" :label="s.label" :value="s.value" />

        </el-select>

      </el-form-item>

    </template>



    <el-table-column prop="appointmentNo" label="预约单号" width="140" />

    <el-table-column prop="patientName" label="患者姓名" width="100" />

    <el-table-column prop="department" label="科室" width="100" />

    <el-table-column prop="doctorName" label="医生" width="100" />

    <el-table-column prop="appointmentDate" label="预约日期" width="120" />

    <el-table-column prop="timeSlot" label="时段" width="130" />

    <el-table-column prop="status" label="状态" width="90">

      <template #default="{ row }">

        <StatusTag enum-key="appointmentStatus" :value="row.status" />

      </template>

    </el-table-column>

    <el-table-column prop="createTime" label="创建时间" width="170" />

    <el-table-column label="操作" width="160" fixed="right">

      <template #default="{ row }">

        <el-button v-if="row.status === 0" link type="primary" @click="handleConfirm(row)">确认</el-button>

        <el-button v-if="row.status < 2" link type="danger" @click="handleCancel(row)">取消</el-button>

        <StatusTag v-if="row.status >= 2" enum-key="appointmentStatus" :value="row.status" />

      </template>

    </el-table-column>



    <template #extra>

      <FormDialog v-model:visible="dialogVisible" title="新增预约" width="520px" confirm-text="提交预约" @confirm="handleSubmit">

        <el-form ref="formRef" :model="form" :rules="rules" label-width="90px">

          <el-form-item label="患者姓名" prop="patientName"><el-input v-model="form.patientName" /></el-form-item>

          <el-form-item label="科室" prop="department">

            <DictSelect v-model="form.department" dict-key="departments" />

          </el-form-item>

          <el-form-item label="医生" prop="doctorName"><el-input v-model="form.doctorName" /></el-form-item>

          <el-form-item label="预约日期" prop="appointmentDate"><el-date-picker v-model="form.appointmentDate" type="date" value-format="YYYY-MM-DD" style="width: 100%" /></el-form-item>

          <el-form-item label="时段" prop="timeSlot">

            <DictSelect v-model="form.timeSlot" dict-key="timeSlots" />

          </el-form-item>

        </el-form>

      </FormDialog>

    </template>

  </ListPage>

</template>



<script setup>

import { ref, reactive, computed, onMounted } from 'vue'

import { ElMessage, ElMessageBox } from 'element-plus'

import { appointmentApi } from '@/api'

import { useDictStore } from '@/stores/dict'



const dictStore = useDictStore()

const appointmentStatusOptions = computed(() =>

  Object.entries(dictStore.getEnum('appointmentStatus')).map(([value, { label }]) => ({

    label,

    value: Number(value)

  }))

)

const loading = ref(false)

const tableData = ref([])

const total = ref(0)

const dialogVisible = ref(false)

const formRef = ref()

const query = reactive({ keyword: '', status: '', page: 1, pageSize: 10 })

const form = reactive({ patientName: '', department: '', doctorName: '', appointmentDate: '', timeSlot: '' })

const rules = { patientName: [{ required: true, message: '请输入患者姓名', trigger: 'blur' }], appointmentDate: [{ required: true, message: '请选择日期', trigger: 'change' }] }



function resetQuery() {

  query.keyword = ''

  query.status = ''

  query.page = 1

  loadData()

}



async function loadData() {

  loading.value = true

  try { const res = await appointmentApi.list(query); tableData.value = res.data.list; total.value = res.data.total }

  finally { loading.value = false }

}



function openDialog() {

  Object.assign(form, { patientName: '', department: '', doctorName: '', appointmentDate: '', timeSlot: '08:00-09:00' })

  dialogVisible.value = true

}



async function handleSubmit() {

  await formRef.value.validate()

  await appointmentApi.create(form)

  ElMessage.success('预约成功')

  dialogVisible.value = false

  loadData()

}



async function handleConfirm(row) {

  await appointmentApi.confirm(row.id)

  ElMessage.success('预约已确认')

  loadData()

}



async function handleCancel(row) {

  await ElMessageBox.confirm('确定取消该预约吗？', '提示', { type: 'warning' })

  await appointmentApi.cancel(row.id)

  ElMessage.success('预约已取消')

  loadData()

}



onMounted(loadData)

</script>


