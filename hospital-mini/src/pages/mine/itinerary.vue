<template>
  <view class="container">
    <view v-if="tabs.length" class="tabs">
      <view
        v-for="tab in tabs"
        :key="tab.key"
        class="tab"
        :class="{ active: activeTab === tab.key }"
        @click="activeTab = tab.key"
      >
        {{ tab.label }}
        <text v-if="tab.count" class="tab-badge">{{ tab.count }}</text>
      </view>
    </view>

    <view v-if="filteredList.length === 0" class="empty-tip">暂无就诊行程</view>

    <view v-for="item in filteredList" :key="item.key" class="trip-card">
      <view class="trip-header">
        <text class="trip-type" :class="item.type">{{ item.typeLabel }}</text>
        <text class="tag" :class="item.statusClass">{{ item.statusLabel }}</text>
      </view>
      <text class="trip-title">{{ item.department }} · {{ item.doctorName }}</text>
      <text class="trip-time">{{ item.timeText }}</text>
      <text class="trip-no">{{ item.orderNo }}</text>
      <view class="trip-actions">
        <button
          v-if="item.actionText"
          class="action-btn primary"
          size="mini"
          @click="handleAction(item)"
        >{{ item.actionText }}</button>
        <button
          v-if="item.canCancel"
          class="action-btn"
          size="mini"
          @click="handleCancel(item)"
        >{{ item.cancelText }}</button>
      </view>
    </view>
  </view>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { mpApi } from '@/api'
import { registerStatusMap, appointmentStatusMap, checkLogin, navigateToLogin } from '@/utils/request'

const activeTab = ref('all')
const list = ref([])

const tabs = computed(() => {
  const pending = list.value.filter((i) => i.isPending).length
  const register = list.value.filter((i) => i.type === 'register').length
  const appointment = list.value.filter((i) => i.type === 'appointment').length
  return [
    { key: 'all', label: '全部', count: list.value.length || 0 },
    { key: 'pending', label: '待处理', count: pending },
    { key: 'register', label: '挂号', count: register },
    { key: 'appointment', label: '预约', count: appointment }
  ].filter((t) => t.key === 'all' || t.count > 0)
})

const filteredList = computed(() => {
  if (activeTab.value === 'pending') return list.value.filter((i) => i.isPending)
  if (activeTab.value === 'register') return list.value.filter((i) => i.type === 'register')
  if (activeTab.value === 'appointment') return list.value.filter((i) => i.type === 'appointment')
  return list.value
})

onMounted(async () => {
  if (!checkLogin()) { navigateToLogin(); return }
  await loadData()
})

async function loadData() {
  const [registerRes, appointmentRes, paymentRes] = await Promise.all([
    mpApi.registerList(),
    mpApi.appointmentList(),
    mpApi.paymentList({ status: 0 })
  ])
  const hasUnpaid = paymentRes.data.list.length > 0

  const registerItems = registerRes.data.list.map((item) => ({
    key: `r-${item.id}`,
    type: 'register',
    typeLabel: '当日挂号',
    id: item.id,
    department: item.department,
    doctorName: item.doctorName,
    orderNo: item.registerNo,
    timeText: item.registerTime,
    status: item.status,
    statusLabel: item.status === 0 && hasUnpaid ? '待支付' : registerStatusMap[item.status]?.label,
    statusClass: item.status === 0 && hasUnpaid ? 'tag-warning' : registerStatusMap[item.status]?.class,
    isPending: item.status === 0,
    actionText: item.status === 0 && hasUnpaid ? '去缴费' : item.status === 0 ? '查看详情' : '',
    actionPath: item.status === 0 && hasUnpaid ? '/pages/payment/payment' : '',
    canCancel: item.status === 0,
    cancelText: '退号'
  }))

  const appointmentItems = appointmentRes.data.list.map((item) => ({
    key: `a-${item.id}`,
    type: 'appointment',
    typeLabel: '预约就诊',
    id: item.id,
    department: item.department,
    doctorName: item.doctorName,
    orderNo: item.appointmentNo,
    timeText: `${item.appointmentDate} ${item.timeSlot}`,
    status: item.status,
    statusLabel: appointmentStatusMap[item.status]?.label,
    statusClass: appointmentStatusMap[item.status]?.class,
    isPending: item.status < 2,
    actionText: item.status < 2 ? '查看预约' : '',
    actionPath: '',
    canCancel: item.status < 2,
    cancelText: '取消预约'
  }))

  list.value = [...registerItems, ...appointmentItems].sort((a, b) =>
    (b.timeText || '').localeCompare(a.timeText || '')
  )
}

function handleAction(item) {
  if (item.actionPath) {
    uni.navigateTo({ url: item.actionPath })
  }
}

function handleCancel(item) {
  const isRegister = item.type === 'register'
  uni.showModal({
    title: isRegister ? '退号确认' : '取消预约',
    content: isRegister
      ? `确定退掉「${item.department} ${item.doctorName}」的号？`
      : `确定取消 ${item.timeText} 的预约？`,
    success: async (res) => {
      if (!res.confirm) return
      if (isRegister) await mpApi.cancelRegister(item.id)
      else await mpApi.cancelAppointment(item.id)
      uni.showToast({ title: isRegister ? '退号成功' : '已取消', icon: 'success' })
      loadData()
    }
  })
}
</script>

<style lang="scss" scoped>
.tabs {
  display: flex;
  gap: 16rpx;
  margin-bottom: 24rpx;
  flex-wrap: wrap;
}

.tab {
  font-size: 26rpx;
  color: #646a73;
  background: #fff;
  padding: 12rpx 24rpx;
  border-radius: 32rpx;
  border: 1rpx solid #e5e6eb;
  display: flex;
  align-items: center;
  gap: 8rpx;
}

.tab.active {
  color: #3370ff;
  background: #f0f4ff;
  border-color: #3370ff;
}

.tab-badge {
  font-size: 20rpx;
  background: #3370ff;
  color: #fff;
  padding: 2rpx 10rpx;
  border-radius: 16rpx;
  min-width: 28rpx;
  text-align: center;
}

.trip-card {
  background: #fff;
  border-radius: 20rpx;
  padding: 28rpx;
  margin-bottom: 20rpx;
  box-shadow: 0 8rpx 24rpx rgba(31, 35, 41, 0.06);
  border: 1rpx solid #e5e6eb;
}

.trip-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 12rpx;
}

.trip-type {
  font-size: 22rpx;
  padding: 4rpx 14rpx;
  border-radius: 8rpx;
}

.trip-type.register {
  color: #3370ff;
  background: #f0f4ff;
}

.trip-type.appointment {
  color: #646a73;
  background: #f7f8fa;
}

.trip-title {
  display: block;
  font-size: 32rpx;
  font-weight: 600;
  color: #1f2329;
}

.trip-time {
  display: block;
  font-size: 26rpx;
  color: #646a73;
  margin-top: 8rpx;
}

.trip-no {
  display: block;
  font-size: 22rpx;
  color: #c9cdd4;
  margin-top: 8rpx;
}

.trip-actions {
  display: flex;
  gap: 16rpx;
  margin-top: 20rpx;
}

.action-btn {
  background: #f7f8fa;
  color: #646a73;
  border: none;
  border-radius: 32rpx;
  font-size: 24rpx;
}

.action-btn.primary {
  background: #f0f4ff;
  color: #3370ff;
}

.action-btn::after { border: none; }
</style>
