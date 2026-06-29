<template>
  <view v-if="visit" class="status-bar" @click="goDetail">
    <view class="status-progress">
      <view
        v-for="(step, idx) in steps"
        :key="step.key"
        class="step"
        :class="{ done: step.done, active: step.active }"
      >
        <view class="step-dot" />
        <text class="step-label">{{ step.label }}</text>
        <view v-if="idx < steps.length - 1" class="step-line" :class="{ done: step.done }" />
      </view>
    </view>
    <view class="status-info">
      <view class="status-main">
        <MiniIcon type="clock" size="sm" />
        <text class="status-text">{{ visit.summary }}</text>
      </view>
      <text class="status-action">{{ visit.actionText }} ›</text>
    </view>
  </view>
</template>

<script setup>
import { computed } from 'vue'
import MiniIcon from './MiniIcon.vue'

const props = defineProps({
  visit: { type: Object, default: null }
})

const steps = computed(() => {
  if (!props.visit) return []
  return [
    { key: 'triage', label: '导诊', done: true, active: false },
    { key: 'register', label: '挂号', done: props.visit.step >= 1, active: props.visit.step === 1 },
    { key: 'payment', label: '缴费', done: props.visit.step >= 2, active: props.visit.step === 2 },
    { key: 'review', label: '回顾', done: props.visit.step >= 3, active: props.visit.step === 3 }
  ]
})

function goDetail() {
  if (!props.visit?.path) return
  uni.navigateTo({ url: props.visit.path })
}
</script>

<style lang="scss" scoped>
.status-bar {
  position: fixed;
  left: 24rpx;
  right: 24rpx;
  bottom: calc(100rpx + env(safe-area-inset-bottom) + 12rpx);
  background: #fff;
  border-radius: 20rpx;
  padding: 20rpx 24rpx;
  box-shadow: 0 8rpx 32rpx rgba(31, 35, 41, 0.12);
  border: 1rpx solid #e5e6eb;
  z-index: 998;
}

.status-progress {
  display: flex;
  justify-content: space-between;
  margin-bottom: 16rpx;
  padding: 0 8rpx;
}

.step {
  flex: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  position: relative;
}

.step-dot {
  width: 16rpx;
  height: 16rpx;
  border-radius: 50%;
  background: #e5e6eb;
  margin-bottom: 8rpx;
}

.step.done .step-dot { background: #3370ff; }
.step.active .step-dot {
  background: #3370ff;
  box-shadow: 0 0 0 6rpx rgba(51, 112, 255, 0.2);
}

.step-label {
  font-size: 20rpx;
  color: #8f959e;
}

.step.done .step-label,
.step.active .step-label {
  color: #3370ff;
  font-weight: 500;
}

.step-line {
  position: absolute;
  top: 7rpx;
  left: calc(50% + 12rpx);
  width: calc(100% - 24rpx);
  height: 3rpx;
  background: #e5e6eb;
}

.step-line.done { background: #3370ff; }

.status-info {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding-top: 12rpx;
  border-top: 1rpx solid #f0f1f3;
}

.status-main {
  display: flex;
  align-items: center;
  flex: 1;
  overflow: hidden;
}

.status-text {
  font-size: 26rpx;
  color: #1f2329;
  margin-left: 12rpx;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.status-action {
  font-size: 24rpx;
  color: #3370ff;
  flex-shrink: 0;
  margin-left: 16rpx;
}
</style>
