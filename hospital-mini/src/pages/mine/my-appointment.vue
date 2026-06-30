<template>
  <view class="container">
    <view v-if="list.length === 0" class="empty-tip">暂无预约记录</view>
    <view v-for="item in list" :key="item.id" class="card">
      <view class="item-header">
        <text class="no">{{ item.appointmentNo }}</text>
        <text class="tag" :class="appointmentStatusMap[item.status]?.class">{{ appointmentStatusMap[item.status]?.label }}</text>
      </view>
      <view class="row"><text class="label">科室</text><text>{{ item.department }}</text></view>
      <view class="row"><text class="label">医生</text><text>{{ item.doctorName }}</text></view>
      <view class="row"><text class="label">预约时间</text><text>{{ item.appointmentDate }} {{ item.timeSlot }}</text></view>
      <view class="row"><text class="label">创建时间</text><text>{{ item.createTime }}</text></view>
      <button v-if="item.status < 2" class="cancel-btn" size="mini" @click="handleCancel(item)">取消预约</button>
    </view>
    <PageNav variant="footer" />
  </view>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { mpApi } from '@/api'
import PageNav from '@/components/PageNav.vue'
import { appointmentStatusMap, checkLogin, navigateToLogin } from '@/utils/request'
import { markSlotsStale } from '@/utils/slotRefresh'

const list = ref([])

onMounted(async () => {
  if (!checkLogin()) { navigateToLogin(); return }
  loadData()
})

async function loadData() {
  const res = await mpApi.appointmentList()
  list.value = res.data.list
}

function handleCancel(item) {
  uni.showModal({
    title: '取消预约',
    content: `确定取消 ${item.appointmentDate} 的预约？`,
    success: async (res) => {
      if (res.confirm) {
        await mpApi.cancelAppointment(item.id)
        markSlotsStale()
        uni.showToast({ title: '已取消，号源已释放', icon: 'success' })
        loadData()
      }
    }
  })
}
</script>

<style lang="scss" scoped>
.item-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16rpx;
}
.no { font-size: 26rpx; color: #909399; }
.row {
  display: flex;
  justify-content: space-between;
  padding: 10rpx 0;
  font-size: 28rpx;
}
.row .label { color: #909399; }
.cancel-btn {
  margin-top: 16rpx;
  background: #fff;
  color: #f56c6c;
  border: 1rpx solid #fbc4c4;
  border-radius: 32rpx;
}
.cancel-btn::after { border: none; }
</style>
