<template>
  <ListPage
    title="登录日志"
    :query="query"
    :loading="loading"
    :data="tableData"
    :total="total"
    @search="loadData"
    @reset="resetQuery"
    @page-change="loadData"
  >
    <template #filters>
      <el-form-item label="用户名">
        <el-input v-model="query.keyword" placeholder="用户名" clearable />
      </el-form-item>
      <el-form-item label="状态">
        <DictSelect v-model="query.status" dict-key="loginStatus" placeholder="全部" clearable width="100px" />
      </el-form-item>
    </template>

    <el-table-column prop="username" label="用户名" width="120" />
    <el-table-column prop="ip" label="IP地址" width="140" />
    <el-table-column prop="browser" label="浏览器" width="100" />
    <el-table-column prop="os" label="操作系统" width="120" />
    <el-table-column prop="status" label="状态" width="80">
      <template #default="{ row }">
        <StatusTag :type="row.status === 1 ? 'success' : 'danger'" :label="row.status === 1 ? '成功' : '失败'" />
      </template>
    </el-table-column>
    <el-table-column prop="message" label="提示信息" min-width="120" />
    <el-table-column prop="loginTime" label="登录时间" width="170" />
  </ListPage>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { adminLogApi } from '@/api'

const loading = ref(false)
const tableData = ref([])
const total = ref(0)
const query = reactive({ keyword: '', status: '', page: 1, pageSize: 10 })

function resetQuery() {
  query.keyword = ''
  query.status = ''
  query.page = 1
  loadData()
}

async function loadData() {
  loading.value = true
  try { const res = await adminLogApi.loginList(query); tableData.value = res.data.list; total.value = res.data.total }
  finally { loading.value = false }
}

onMounted(loadData)
</script>
