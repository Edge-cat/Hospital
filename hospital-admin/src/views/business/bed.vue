<template>

  <ListPage

    title="床位管理"

    :query="query"

    :loading="loading"

    :data="tableData"

    :total="total"

    @search="loadData"

    @reset="resetQuery"

    @page-change="loadData"

  >

    <template #actions>

      <el-button type="primary" @click="openDialog()">新增床位</el-button>

    </template>

    <template #stats>

      <StatOverview :items="statItems" />

    </template>

    <template #filters>

      <el-form-item label="病区">

        <el-input v-model="query.keyword" placeholder="病区/床位号" clearable />

      </el-form-item>

      <el-form-item label="状态">

        <el-select v-model="query.status" placeholder="全部" clearable style="width: 120px">

          <el-option v-for="s in bedStatusOptions" :key="s.value" :label="s.label" :value="s.value" />

        </el-select>

      </el-form-item>

    </template>



    <el-table-column prop="bedNo" label="床位号" width="100" />

    <el-table-column prop="ward" label="所属病区" width="140" />

    <el-table-column prop="bedType" label="床位类型" width="120" />

    <el-table-column prop="price" label="日费用(元)" width="110" />

    <el-table-column prop="status" label="状态" width="90">

      <template #default="{ row }">

        <StatusTag enum-key="bedStatus" :value="row.status" />

      </template>

    </el-table-column>

    <el-table-column prop="patientName" label="当前患者" width="100" />

    <el-table-column label="操作" width="160" fixed="right">

      <template #default="{ row }">

        <el-button link type="primary" @click="openDialog(row)">编辑</el-button>

        <el-button link type="danger" :disabled="row.status === 1" @click="handleDelete(row)">删除</el-button>

      </template>

    </el-table-column>



    <template #extra>

      <FormDialog v-model:visible="dialogVisible" :title="form.id ? '编辑床位' : '新增床位'" width="480px" @confirm="handleSubmit">

        <el-form ref="formRef" :model="form" :rules="rules" label-width="90px">

          <el-form-item label="床位号" prop="bedNo"><el-input v-model="form.bedNo" /></el-form-item>

          <el-form-item label="所属病区" prop="ward"><el-input v-model="form.ward" /></el-form-item>

          <el-form-item label="床位类型" prop="bedType">

            <DictSelect v-model="form.bedType" dict-key="bedTypes" />

          </el-form-item>

          <el-form-item label="日费用" prop="price"><el-input-number v-model="form.price" :min="0" style="width: 100%" /></el-form-item>

        </el-form>

      </FormDialog>

    </template>

  </ListPage>

</template>



<script setup>

import { ref, reactive, computed, onMounted } from 'vue'

import { ElMessage, ElMessageBox } from 'element-plus'

import { bedApi } from '@/api'

import { STAT_COLORS } from '@/constants'

import { useDictStore } from '@/stores/dict'



const dictStore = useDictStore()

const bedStatusOptions = computed(() =>

  Object.entries(dictStore.getEnum('bedStatus')).map(([value, { label }]) => ({

    label,

    value: Number(value)

  }))

)

const loading = ref(false)

const tableData = ref([])

const total = ref(0)

const dialogVisible = ref(false)

const formRef = ref()

const stats = reactive({ total: 0, free: 0, occupied: 0, maintenance: 0 })

const query = reactive({ keyword: '', status: '', page: 1, pageSize: 10 })

const form = reactive({ id: null, bedNo: '', ward: '', bedType: '普通床位', price: 80 })

const rules = { bedNo: [{ required: true, message: '请输入床位号', trigger: 'blur' }], ward: [{ required: true, message: '请输入病区', trigger: 'blur' }] }



const statItems = computed(() => [

  { label: '总床位数', value: stats.total, color: STAT_COLORS.primary },

  { label: '空闲', value: stats.free, color: STAT_COLORS.success },

  { label: '占用', value: stats.occupied, color: STAT_COLORS.warning },

  { label: '维护中', value: stats.maintenance, color: STAT_COLORS.neutral }

])



function resetQuery() {

  query.keyword = ''

  query.status = ''

  query.page = 1

  loadData()

}



async function loadData() {

  loading.value = true

  try {

    const res = await bedApi.list(query)

    tableData.value = res.data.list

    total.value = res.data.total

    stats.total = res.data.total

    stats.free = tableData.value.filter((b) => b.status === 0).length

    stats.occupied = tableData.value.filter((b) => b.status === 1).length

    stats.maintenance = tableData.value.filter((b) => b.status === 2).length

  } finally { loading.value = false }

}



function openDialog(row) {

  Object.assign(form, row || { id: null, bedNo: '', ward: '', bedType: '普通床位', price: 80 })

  dialogVisible.value = true

}



async function handleSubmit() {

  await formRef.value.validate()

  if (form.id) await bedApi.update(form.id, form)

  else await bedApi.create(form)

  ElMessage.success('操作成功')

  dialogVisible.value = false

  loadData()

}



async function handleDelete(row) {

  await ElMessageBox.confirm('确定删除该床位吗？', '提示', { type: 'warning' })

  await bedApi.remove(row.id)

  ElMessage.success('删除成功')

  loadData()

}



onMounted(loadData)

</script>


