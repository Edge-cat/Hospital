<template>
  <view class="mini-icon" :class="[`mini-icon--${type}`, size]">
    <view v-if="type === 'triage'" class="glyph triage" />
    <view v-else-if="type === 'register'" class="glyph register" />
    <view v-else-if="type === 'payment'" class="glyph payment" />
    <view v-else-if="type === 'record'" class="glyph record" />
    <view v-else-if="type === 'search'" class="glyph search" />
    <view v-else-if="type === 'arrow'" class="glyph arrow" />
    <view v-else-if="type === 'clock'" class="glyph clock" />
    <view v-else-if="type === 'location'" class="glyph location" />
  </view>
</template>

<script setup>
defineProps({
  type: { type: String, required: true },
  size: { type: String, default: 'md' }
})
</script>

<style lang="scss" scoped>
.mini-icon {
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 20rpx;
  background: #f0f4ff;

  &.sm { width: 48rpx; height: 48rpx; border-radius: 12rpx; }
  &.md { width: 88rpx; height: 88rpx; }
  &.lg { width: 104rpx; height: 104rpx; border-radius: 24rpx; }

  &--register { background: #e8f0ff; }
  &--payment { background: #fff5eb; }
  &--record { background: #eaffea; }
  &--triage { background: #f0f4ff; }
}

.glyph {
  position: relative;
}

/* 导诊：十字 */
.triage::before,
.triage::after {
  content: '';
  position: absolute;
  background: #3370ff;
  border-radius: 2rpx;
}
.triage::before { width: 28rpx; height: 6rpx; top: 11rpx; left: -14rpx; }
.triage::after { width: 6rpx; height: 28rpx; top: 0; left: -3rpx; }

/* 挂号：文档 */
.register {
  width: 24rpx;
  height: 30rpx;
  border: 4rpx solid #3370ff;
  border-radius: 4rpx;
}
.register::after {
  content: '';
  position: absolute;
  width: 14rpx;
  height: 3rpx;
  background: #3370ff;
  top: 8rpx;
  left: 5rpx;
  box-shadow: 0 8rpx 0 #3370ff;
}

/* 缴费：圆+线 */
.payment {
  width: 28rpx;
  height: 28rpx;
  border: 4rpx solid #ff8800;
  border-radius: 50%;
}
.payment::after {
  content: '¥';
  position: absolute;
  font-size: 18rpx;
  font-weight: 700;
  color: #ff8800;
  top: -2rpx;
  left: 6rpx;
}

/* 记录：折线 */
.record {
  width: 28rpx;
  height: 20rpx;
  border-left: 4rpx solid #34c724;
  border-bottom: 4rpx solid #34c724;
}
.record::after {
  content: '';
  position: absolute;
  width: 12rpx;
  height: 4rpx;
  background: #34c724;
  top: 4rpx;
  left: 4rpx;
  transform: rotate(-30deg);
}

.search {
  width: 24rpx;
  height: 24rpx;
  border: 4rpx solid #8f959e;
  border-radius: 50%;
}
.search::after {
  content: '';
  position: absolute;
  width: 10rpx;
  height: 4rpx;
  background: #8f959e;
  bottom: -6rpx;
  right: -8rpx;
  transform: rotate(45deg);
  border-radius: 2rpx;
}

.arrow {
  width: 16rpx;
  height: 16rpx;
  border-top: 4rpx solid #c9cdd4;
  border-right: 4rpx solid #c9cdd4;
  transform: rotate(45deg);
}

.clock {
  width: 24rpx;
  height: 24rpx;
  border: 4rpx solid #3370ff;
  border-radius: 50%;
}
.clock::after {
  content: '';
  position: absolute;
  width: 3rpx;
  height: 10rpx;
  background: #3370ff;
  top: 4rpx;
  left: 10rpx;
}

.location {
  width: 20rpx;
  height: 20rpx;
  border: 4rpx solid #3370ff;
  border-radius: 50% 50% 50% 0;
  transform: rotate(-45deg);
}
</style>
