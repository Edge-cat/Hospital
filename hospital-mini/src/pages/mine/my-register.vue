<template>
  <view class="container">
    <view v-if="list.length === 0" class="empty-tip">暂无挂号记录</view>
    <view v-for="item in list" :key="item.id" class="card">
      <view class="item-header">
        <text class="no">{{ item.registerNo }}</text>
        <text class="tag" :class="registerStatusMap[item.status]?.class">{{ registerStatusMap[item.status]?.label }}</text>
      </view>
      <view class="row"><text class="label">科室</text><text>{{ item.department }}</text></view>
      <view class="row"><text class="label">医生</text><text>{{ item.doctorName }}</text></view>
      <view class="row"><text class="label">号别</text><text>{{ item.registerType }} · ¥{{ item.fee }}</text></view>
      <view class="row"><text class="label">挂号时间</text><text>{{ item.registerTime }}</text></view>
      <button v-if="item.status === 0" class="cancel-btn" size="mini" @click="handleCancel(item)">退号</button>
    </view>
  </view>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { mpApi } from '@/api'
import { registerStatusMap, checkLogin, navigateToLogin } from '@/utils/request'

const list = ref([])

onMounted(async () => {
  if (!checkLogin()) { navigateToLogin(); return }
  loadData()
})

async function loadData() {
  const res = await mpApi.registerList()
  list.value = res.data.list
}

function handleCancel(item) {
  uni.showModal({
    title: '退号确认',
    content: `确定退掉「${item.department} ${item.doctorName}」的号？`,
    success: async (res) => {
      if (res.confirm) {
        await mpApi.cancelRegister(item.id)
        uni.showToast({ title: '退号成功', icon: 'success' })
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
