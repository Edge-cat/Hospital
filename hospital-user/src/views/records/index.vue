<template>
  <ProfilePage>
    <div>
      <PageHeader title="就诊记录" subtitle="查看历史诊疗信息" />
      <TableCard :loading="loading" :data="list">
        <el-table-column prop="department" label="科室" width="100" />
        <el-table-column prop="doctorName" label="医生" width="100" />
        <el-table-column prop="diagnosis" label="诊断" min-width="160" show-overflow-tooltip />
        <el-table-column prop="treatment" label="治疗" min-width="160" show-overflow-tooltip />
        <el-table-column prop="visitTime" label="就诊时间" width="170" />
      </TableCard>
    </div>
  </ProfilePage>
</template>

<script setup>
import { userApi } from '@/api'
import { useListPage } from '@/composables/useListPage'

const { loading, list } = useListPage(() => userApi.records())
</script>
