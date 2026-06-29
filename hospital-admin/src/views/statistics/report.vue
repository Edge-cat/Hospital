<template>

  <ListPage

    title="报表生成"

    :query="query"

    :loading="loading"

    :data="tableData"

    :total="total"

    :show-search="false"

    :show-pagination="false"

    @search="loadData"

    @reset="resetQuery"

    @page-change="loadData"

  >

    <template #actions>

      <el-button type="primary" @click="handleGenerate">生成报表</el-button>

    </template>



    <el-table-column prop="name" label="报表名称" min-width="180" />

    <el-table-column prop="type" label="报表类型" width="120" />

    <el-table-column prop="period" label="统计周期" width="120" />

    <el-table-column prop="status" label="状态" width="100">

      <template #default="{ row }">

        <StatusTag enum-key="reportStatus" :value="row.status" />

      </template>

    </el-table-column>

    <el-table-column prop="createTime" label="生成时间" width="170" />

    <el-table-column label="操作" width="160" fixed="right">

      <template #default="{ row }">

        <el-button link type="primary" @click="handlePreview(row)">预览</el-button>

        <el-button link type="success" @click="handleExport(row)">导出</el-button>

      </template>

    </el-table-column>



    <template #extra>

      <el-dialog v-model="previewVisible" :title="previewReport?.name" width="600px">

        <el-descriptions v-if="previewReport" :column="2" border>

          <el-descriptions-item label="报表类型">{{ previewReport.type }}</el-descriptions-item>

          <el-descriptions-item label="统计周期">{{ previewReport.period }}</el-descriptions-item>

          <el-descriptions-item label="生成时间" :span="2">{{ previewReport.createTime }}</el-descriptions-item>

          <el-descriptions-item label="报表摘要" :span="2">

            本报表统计了{{ previewReport.period }}期间{{ previewReport.type }}相关数据，包含明细汇总与趋势分析。

          </el-descriptions-item>

        </el-descriptions>

      </el-dialog>

    </template>

  </ListPage>

</template>



<script setup>

import { ref, reactive, onMounted } from 'vue'

import { ElMessage } from 'element-plus'

import { statisticsApi } from '@/api'



const loading = ref(false)

const tableData = ref([])

const total = ref(0)

const previewVisible = ref(false)

const previewReport = ref(null)

const query = reactive({ page: 1, pageSize: 10 })



function resetQuery() {

  query.page = 1

  loadData()

}



async function loadData() {

  loading.value = true

  try {

    const res = await statisticsApi.reports()

    tableData.value = res.data.list

    total.value = res.data.list.length

  } finally {

    loading.value = false

  }

}



function handleGenerate() {

  ElMessage.success('报表生成任务已提交，请稍后刷新查看')

}



function handlePreview(row) {

  previewReport.value = row

  previewVisible.value = true

}



function handleExport(row) {

  ElMessage.success(`「${row.name}」导出成功`)

}



onMounted(loadData)

</script>


