<template>
  <view class="container">
    <view v-if="list.length === 0" class="empty-tip">暂无就诊记录</view>
    <view v-for="item in list" :key="item.id" class="card record-card">
      <view class="record-header">
        <text class="dept">{{ item.department }}</text>
        <text class="tag tag-success">已完成</text>
      </view>
      <view class="record-row"><text class="label">主治医生</text><text>{{ item.doctorName }}</text></view>
      <view class="record-row"><text class="label">诊断结果</text><text>{{ item.diagnosis }}</text></view>
      <view class="record-row"><text class="label">治疗方案</text><text>{{ item.treatment }}</text></view>
      <view class="record-row"><text class="label">就诊时间</text><text>{{ item.visitTime }}</text></view>
    </view>
  </view>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { mpApi } from '@/api'
import { checkLogin, navigateToLogin } from '@/utils/request'

const list = ref([])

onMounted(async () => {
  if (!checkLogin()) { navigateToLogin(); return }
  const res = await mpApi.records()
  list.value = res.data.list
})
</script>

<style lang="scss" scoped>
.record-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16rpx;
}

.dept {
  font-size: 32rpx;
  font-weight: 600;
  color: #303133;
}

.record-row {
  display: flex;
  padding: 12rpx 0;
  font-size: 28rpx;
  line-height: 1.5;
}

.record-row .label {
  width: 160rpx;
  color: #909399;
  flex-shrink: 0;
}
</style>
