<template>
  <view class="container">
    <view v-if="loading && !departments.length" class="state-tip">加载中…</view>
    <view v-else-if="error" class="state-box">
      <text class="state-tip error">{{ error }}</text>
      <button class="retry-btn" size="mini" @click="refresh">重新加载</button>
    </view>
    <view v-else-if="!departments.length" class="state-tip">暂无科室信息</view>
    <view v-for="dept in departments" :key="dept.id" class="card dept-card" @click="goDetail(dept)">
      <view class="dept-left">
        <text class="dept-icon">{{ dept.icon }}</text>
        <view class="dept-info">
          <text class="dept-name">{{ dept.name }}</text>
          <text v-if="dept.code" class="dept-code">编码 {{ dept.code }}</text>
          <text class="dept-desc">{{ dept.desc || '提供门诊与住院诊疗服务' }}</text>
          <text class="dept-leader">负责人：{{ dept.leader || '—' }}</text>
          <text v-if="waitLabel(dept)" class="dept-wait">{{ waitLabel(dept) }}</text>
        </view>
      </view>
      <text class="arrow">›</text>
    </view>
    <PageNav variant="footer" />
  </view>
</template>

<script setup>
import { onPullDownRefresh } from '@dcloudio/uni-app'
import { onMounted } from 'vue'
import PageNav from '@/components/PageNav.vue'
import { useDepartments } from '@/composables/useDepartments'
import { safeNavigateTo } from '@/utils/nav'
import { waitLabel } from '@/utils/deptIcon'

const { departments, loading, error, load, refresh } = useDepartments()

onMounted(() => load())

onPullDownRefresh(async () => {
  await refresh()
  uni.stopPullDownRefresh()
})

function goDetail(dept) {
  safeNavigateTo(`/pages/department/detail?id=${dept.id}&name=${encodeURIComponent(dept.name)}`)
}
</script>

<style lang="scss" scoped>
.state-tip {
  text-align: center;
  color: #909399;
  padding: 48rpx 0;
  font-size: 28rpx;
}

.state-tip.error {
  color: #f56c6c;
}

.state-box {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 16rpx;
  padding: 48rpx 0;
}

.retry-btn {
  background: #3370ff;
  color: #fff;
  border: none;
  border-radius: 32rpx;
}

.dept-card {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.dept-left {
  display: flex;
  align-items: flex-start;
  flex: 1;
}

.dept-icon {
  font-size: 56rpx;
  margin-right: 20rpx;
}

.dept-name {
  display: block;
  font-size: 32rpx;
  font-weight: 600;
  color: #303133;
}

.dept-code {
  display: block;
  font-size: 22rpx;
  color: #909399;
  margin-top: 6rpx;
  letter-spacing: 0.04em;
}

.dept-desc {
  display: block;
  font-size: 26rpx;
  color: #909399;
  margin-top: 8rpx;
  line-height: 1.5;
}

.dept-leader,
.dept-wait {
  display: block;
  font-size: 24rpx;
  color: #c0c4cc;
  margin-top: 8rpx;
}

.dept-wait {
  color: #3370ff;
}

.arrow {
  font-size: 40rpx;
  color: #c0c4cc;
}
</style>
