<template>
  <ListPage
    title="操作日志"
    :query="query"
    :loading="loading"
    :data="tableData"
    :total="total"
    @search="loadData"
    @reset="resetQuery"
    @page-change="loadData"
  >
    <template #filters>
      <el-form-item label="操作人">
        <el-input v-model="query.keyword" placeholder="操作人/模块/动作" clearable />
      </el-form-item>
      <el-form-item label="模块">
        <DictSelect v-model="query.module" dict-key="logModules" placeholder="全部" clearable width="140px" />
      </el-form-item>
      <el-form-item label="来源">
        <el-select v-model="query.source" placeholder="全部" clearable style="width: 120px">
          <el-option label="后端" value="backend" />
          <el-option label="前端" value="frontend" />
        </el-select>
      </el-form-item>
      <el-form-item label="客户端">
        <el-select v-model="query.client" placeholder="全部" clearable style="width: 120px">
          <el-option label="管理端" value="admin" />
          <el-option label="用户端" value="user" />
          <el-option label="小程序" value="mini" />
        </el-select>
      </el-form-item>
    </template>

    <el-table-column prop="operator" label="操作人" width="100" />
    <el-table-column prop="source" label="来源" width="80">
      <template #default="{ row }">
        <el-tag size="small" :type="row.source === 'frontend' ? 'warning' : 'info'" effect="plain">
          {{ sourceLabel(row.source) }}
        </el-tag>
      </template>
    </el-table-column>
    <el-table-column prop="client" label="客户端" width="90">
      <template #default="{ row }">
        {{ clientLabel(row.client) }}
      </template>
    </el-table-column>
    <el-table-column prop="module" label="操作模块" width="120" />
    <el-table-column prop="action" label="操作类型" width="120" />
    <el-table-column prop="path" label="路径" min-width="160" show-overflow-tooltip />
    <el-table-column prop="ip" label="IP地址" width="130" />
    <el-table-column prop="status" label="状态" width="80">
      <template #default="{ row }">
        <StatusTag :type="row.status === 1 ? 'success' : 'danger'" :label="loginStatusLabel(row.status)" />
      </template>
    </el-table-column>
    <el-table-column prop="createTime" label="操作时间" width="170" />
  </ListPage>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { adminLogApi } from '@/api'
import { useDict } from '@/composables/useDict'

const { options } = useDict()
const loginStatuses = options('loginStatus')

function loginStatusLabel(value) {
  return loginStatuses.value.find((s) => s.value === value)?.label ?? value
}

function sourceLabel(value) {
  return { backend: '后端', frontend: '前端' }[value] || value || '-'
}

function clientLabel(value) {
  return { admin: '管理端', user: '用户端', mini: '小程序' }[value] || value || '-'
}

const loading = ref(false)
const tableData = ref([])
const total = ref(0)
const query = reactive({ keyword: '', module: '', source: '', client: '', page: 1, pageSize: 10 })

function resetQuery() {
  query.keyword = ''
  query.module = ''
  query.source = ''
  query.client = ''
  query.page = 1
  loadData()
}

async function loadData() {
  loading.value = true
  try {
    const res = await adminLogApi.operationList(query)
    tableData.value = res.data.list
    total.value = res.data.total
  } finally {
    loading.value = false
  }
}

onMounted(loadData)
</script>
