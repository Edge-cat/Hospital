<template>

  <ListPage

    title="缴费管理"

    :query="query"

    :loading="loading"

    :data="tableData"

    :total="total"

    @search="loadData"

    @reset="resetQuery"

    @page-change="loadData"

  >

    <template #actions>

      <el-button type="primary" @click="openDialog()">收费登记</el-button>

    </template>

    <template #stats>

      <StatOverview :items="statItems" :col-span="8" />

    </template>

    <template #filters>

      <el-form-item label="患者姓名">

        <el-input v-model="query.keyword" placeholder="姓名/缴费单号" clearable />

      </el-form-item>

      <el-form-item label="支付方式">

        <DictSelect v-model="query.payMethod" dict-key="payMethods" placeholder="全部" clearable width="120px" />

      </el-form-item>

    </template>



    <el-table-column prop="paymentNo" label="缴费单号" width="140" />

    <el-table-column prop="patientName" label="患者姓名" width="100" />

    <el-table-column prop="itemName" label="收费项目" width="120" />

    <el-table-column prop="amount" label="金额(元)" width="110">

      <template #default="{ row }">¥{{ row.amount?.toFixed(2) }}</template>

    </el-table-column>

    <el-table-column prop="payMethod" label="支付方式" width="100" />

    <el-table-column prop="status" label="状态" width="90">

      <template #default="{ row }">

        <StatusTag enum-key="paymentStatus" :value="row.status" />

      </template>

    </el-table-column>

    <el-table-column prop="payTime" label="缴费时间" width="170" />

    <el-table-column label="操作" width="100" fixed="right">

      <template #default="{ row }">

        <el-button v-if="row.status === 1" link type="warning" @click="handleRefund(row)">退款</el-button>

        <StatusTag v-else enum-key="paymentStatus" :value="row.status" />

      </template>

    </el-table-column>



    <template #extra>

      <FormDialog v-model:visible="dialogVisible" title="收费登记" width="480px" confirm-text="确认收费" @confirm="handleSubmit">

        <el-form ref="formRef" :model="form" :rules="rules" label-width="90px">

          <el-form-item label="患者姓名" prop="patientName"><el-input v-model="form.patientName" /></el-form-item>

          <el-form-item label="收费项目" prop="itemName">

            <DictSelect v-model="form.itemName" dict-key="paymentItems" />

          </el-form-item>

          <el-form-item label="金额" prop="amount"><el-input-number v-model="form.amount" :min="0" :precision="2" style="width: 100%" /></el-form-item>

          <el-form-item label="支付方式" prop="payMethod">

            <DictSelect v-model="form.payMethod" dict-key="payMethods" />

          </el-form-item>

        </el-form>

      </FormDialog>

    </template>

  </ListPage>

</template>



<script setup>

import { ref, reactive, computed, onMounted } from 'vue'

import { ElMessage, ElMessageBox } from 'element-plus'

import { paymentApi } from '@/api'

import { STAT_COLORS } from '@/constants'



const loading = ref(false)

const tableData = ref([])

const total = ref(0)

const dialogVisible = ref(false)

const formRef = ref()

const stats = reactive({ count: 0, amount: 0, refund: 0 })

const query = reactive({ keyword: '', payMethod: '', page: 1, pageSize: 10 })

const form = reactive({ patientName: '', itemName: '', amount: 0, payMethod: '微信' })

const rules = { patientName: [{ required: true, message: '请输入患者姓名', trigger: 'blur' }], amount: [{ required: true, message: '请输入金额', trigger: 'blur' }] }



const statItems = computed(() => [

  { label: '今日收费笔数', value: stats.count, color: STAT_COLORS.primary },

  { label: '今日收费金额(元)', value: `¥${stats.amount.toFixed(2)}`, color: STAT_COLORS.success },

  { label: '待退款', value: stats.refund, color: STAT_COLORS.danger }

])



function resetQuery() {

  query.keyword = ''

  query.payMethod = ''

  query.page = 1

  loadData()

}



async function loadData() {

  loading.value = true

  try {

    const res = await paymentApi.list(query)

    tableData.value = res.data.list

    total.value = res.data.total

    stats.count = tableData.value.filter((p) => p.status === 1).length

    stats.amount = tableData.value.filter((p) => p.status === 1).reduce((s, p) => s + p.amount, 0)

    stats.refund = tableData.value.filter((p) => p.status === 2).length

  } finally { loading.value = false }

}



function openDialog() {

  Object.assign(form, { patientName: '', itemName: '挂号费', amount: 0, payMethod: '微信' })

  dialogVisible.value = true

}



async function handleSubmit() {

  await formRef.value.validate()

  await paymentApi.create(form)

  ElMessage.success('收费成功')

  dialogVisible.value = false

  loadData()

}



async function handleRefund(row) {

  await ElMessageBox.confirm(`确认为「${row.patientName}」办理退款 ¥${row.amount.toFixed(2)}？`, '退款', { type: 'warning' })

  await paymentApi.refund(row.id)

  ElMessage.success('退款成功')

  loadData()

}



onMounted(loadData)

</script>


