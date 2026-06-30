<template>
  <ProfilePage>
    <div>
      <PageHeader title="我的挂号" subtitle="查看与管理挂号记录" />
      <TableCard :loading="loading" :data="list">
        <el-table-column prop="registerNo" label="挂号单号" width="150" />
        <el-table-column prop="department" label="科室" width="100" />
        <el-table-column prop="doctorName" label="医生" width="100" />
        <el-table-column prop="registerType" label="号别" width="90" />
        <el-table-column prop="status" label="状态" width="90">
          <template #default="{ row }">
            <el-tag :type="REGISTER_STATUS[row.status]?.type">{{ REGISTER_STATUS[row.status]?.label }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="registerTime" label="时间" width="170" />
        <el-table-column label="操作" width="100">
          <template #default="{ row }">
            <el-button v-if="row.status === 0" link type="danger" @click="handleCancel(row)">退号</el-button>
          </template>
        </el-table-column>
      </TableCard>
    </div>
  </ProfilePage>
</template>

<script setup>
import { onActivated } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { userApi } from '@/api'
import { useListPage } from '@/composables/useListPage'
import { REGISTER_STATUS } from '@/constants'

const { loading, list, loadData } = useListPage(() => userApi.registerList())

onActivated(loadData)

async function handleCancel(row) {
  await ElMessageBox.confirm('确认退号？', '提示', { type: 'warning' })
  await userApi.cancelRegister(row.id)
  ElMessage.success('退号成功')
  loadData()
}
</script>
