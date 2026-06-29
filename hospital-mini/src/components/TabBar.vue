<template>
  <view class="tab-bar">
    <view
      v-for="item in tabs"
      :key="item.path"
      class="tab-item"
      :class="{ active: current === item.path }"
      @click="switchTab(item)"
    >
      <text class="tab-icon">{{ item.icon }}</text>
      <text class="tab-text">{{ item.text }}</text>
    </view>
  </view>
</template>

<script setup>
import { computed } from 'vue'

const props = defineProps({
  current: { type: String, default: '/pages/index/index' }
})

const tabs = [
  { path: '/pages/index/index', text: '首页', icon: '🏠' },
  { path: '/pages/visit/visit', text: '预约', icon: '📋' },
  { path: '/pages/payment/payment', text: '缴费', icon: '💰' },
  { path: '/pages/mine/mine', text: '我的', icon: '👤' }
]

function switchTab(item) {
  if (props.current === item.path) return
  uni.redirectTo({ url: item.path })
}
</script>

<style lang="scss" scoped>
.tab-bar {
  position: fixed;
  bottom: 0;
  left: 0;
  right: 0;
  height: 100rpx;
  padding-bottom: env(safe-area-inset-bottom);
  background: #fff;
  display: flex;
  border-top: 1rpx solid #dee0e3;
  z-index: 999;
}

.tab-item {
  flex: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  color: #8f959e;
}

.tab-item.active {
  color: #3370ff;
}

.tab-icon {
  font-size: 40rpx;
  line-height: 1.2;
}

.tab-text {
  font-size: 22rpx;
  margin-top: 4rpx;
}
</style>
