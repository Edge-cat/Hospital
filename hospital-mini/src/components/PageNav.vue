<template>
  <view class="page-nav" :class="[`page-nav--${variant}`, { 'page-nav--fixed': fixed }]">
    <view class="page-nav__left">
      <text v-if="showBackBtn" class="page-nav__btn" @tap="onBack">‹ 返回</text>
    </view>
    <text v-if="title" class="page-nav__title">{{ title }}</text>
    <view class="page-nav__right">
      <text class="page-nav__btn page-nav__home" @tap="onHome">首页</text>
    </view>
  </view>
</template>

<script setup>
import { computed } from 'vue'
import { goBackOrHome, goHome } from '@/utils/nav'

const props = defineProps({
  title: { type: String, default: '' },
  fixed: { type: Boolean, default: false },
  variant: { type: String, default: 'header' },
  showBack: { type: Boolean, default: undefined }
})

const canBack = computed(() => getCurrentPages().length > 1)
const showBackBtn = computed(() =>
  props.showBack !== undefined ? props.showBack : canBack.value
)

function onBack() {
  goBackOrHome()
}

function onHome() {
  goHome()
}
</script>

<style lang="scss" scoped>
.page-nav {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 16rpx 24rpx;
  min-height: 72rpx;
}

.page-nav--header {
  background: #3370ff;
  color: #fff;
}

.page-nav--footer {
  background: #fff;
  color: #3370ff;
  border-top: 1rpx solid #e5e6eb;
  margin-top: 24rpx;
}

.page-nav--fixed {
  position: sticky;
  top: 0;
  z-index: 100;
}

.page-nav__title {
  flex: 1;
  text-align: center;
  font-size: 32rpx;
  font-weight: 600;
}

.page-nav__left,
.page-nav__right {
  min-width: 120rpx;
}

.page-nav__right {
  text-align: right;
}

.page-nav__btn {
  font-size: 28rpx;
  padding: 8rpx 0;
}

.page-nav__home {
  opacity: 0.95;
}
</style>
