<template>
  <ProfilePage>
    <div>
      <PageHeader title="我的预约" subtitle="查看与管理预约记录" />
      <TableCard :loading="loading" :data="list">
        <el-table-column prop="appointmentNo" label="预约单号" width="150" />
        <el-table-column prop="department" label="科室" width="100" />
        <el-table-column prop="doctorName" label="医生" width="100" />
        <el-table-column label="预约时间" min-width="180">
          <template #default="{ row }">{{ row.appointmentDate }} {{ row.timeSlot }}</template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="90">
          <template #default="{ row }">
            <el-tag :type="APPOINTMENT_STATUS[row.status]?.type">{{ APPOINTMENT_STATUS[row.status]?.label }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="100">
          <template #default="{ row }">
            <el-button v-if="row.status < 2" link type="danger" @click="handleCancel(row)">取消</el-button>
          </template>
        </el-table-column>
      </TableCard>
    </div>
  </ProfilePage>
</template>

<script setup>
import { ElMessage, ElMessageBox } from 'element-plus'
import { userApi } from '@/api'
import { useListPage } from '@/composables/useListPage'
import { APPOINTMENT_STATUS } from '@/constants'

const { loading, list, loadData } = useListPage(() => userApi.appointmentList())

async function handleCancel(row) {
  await ElMessageBox.confirm('确认取消预约？', '提示', { type: 'warning' })
  await userApi.cancelAppointment(row.id)
  ElMessage.success('已取消')
  loadData()
}
</script>
