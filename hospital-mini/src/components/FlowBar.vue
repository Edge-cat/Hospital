<template>
  <view class="flow-bar">
    <view
      v-for="(step, idx) in steps"
      :key="step.key || idx"
      class="flow-step"
      :class="{ active: current >= idx, current: current === idx }"
    >
      <view class="flow-dot">{{ idx + 1 }}</view>
      <text class="flow-label">{{ step.label }}</text>
      <view v-if="idx < steps.length - 1" class="flow-line" :class="{ active: current > idx }" />
    </view>
  </view>
</template>

<script setup>
defineProps({
  steps: { type: Array, required: true },
  current: { type: Number, default: 0 }
})
</script>

<style lang="scss" scoped>
.flow-bar {
  display: flex;
  justify-content: space-between;
  background: #fff;
  padding: 28rpx 32rpx;
  border-bottom: 1rpx solid #e5e6eb;
}

.flow-step {
  flex: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  position: relative;
}

.flow-dot {
  width: 40rpx;
  height: 40rpx;
  border-radius: 50%;
  background: #e5e6eb;
  color: #8f959e;
  font-size: 22rpx;
  font-weight: 600;
  display: flex;
  align-items: center;
  justify-content: center;
}

.flow-step.active .flow-dot { background: #3370ff; color: #fff; }
.flow-step.current .flow-dot { box-shadow: 0 0 0 6rpx rgba(51, 112, 255, 0.2); }

.flow-label {
  font-size: 22rpx;
  color: #8f959e;
  margin-top: 8rpx;
}

.flow-step.active .flow-label { color: #3370ff; font-weight: 500; }

.flow-line {
  position: absolute;
  top: 20rpx;
  left: calc(50% + 24rpx);
  width: calc(100% - 48rpx);
  height: 3rpx;
  background: #e5e6eb;
}

.flow-line.active { background: #3370ff; }
</style>
