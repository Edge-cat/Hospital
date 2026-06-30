<template>
  <view class="page">
    <view class="success-header">
      <view class="success-icon">✓</view>
      <text class="success-title">支付成功</text>
      <text class="success-amount">¥{{ detail.amount?.toFixed(2) }}</text>
      <text class="success-desc">缴费已完成，请按导引继续就诊流程</text>
    </view>

    <view class="container">
      <view class="voucher-card">
        <view class="voucher-stamp">已支付</view>
        <text class="voucher-title">电子支付凭证</text>

        <view class="voucher-row">
          <text class="voucher-label">凭证编号</text>
          <text class="voucher-value">{{ voucherNo }}</text>
        </view>
        <view class="voucher-row">
          <text class="voucher-label">缴费单号</text>
          <text class="voucher-value">{{ detail.paymentNo }}</text>
        </view>
        <view class="voucher-row">
          <text class="voucher-label">收费项目</text>
          <text class="voucher-value">{{ detail.itemName }}</text>
        </view>
        <view class="voucher-row">
          <text class="voucher-label">就诊科室</text>
          <text class="voucher-value">{{ detail.department }} · {{ detail.doctorName }}</text>
        </view>
        <view class="voucher-row">
          <text class="voucher-label">支付方式</text>
          <text class="voucher-value">{{ detail.payMethod }}</text>
        </view>
        <view class="voucher-row">
          <text class="voucher-label">支付时间</text>
          <text class="voucher-value">{{ detail.payTime }}</text>
        </view>

        <view class="voucher-divider" />

        <view class="voucher-breakdown">
          <text class="breakdown-heading">费用明细</text>
          <view v-for="(fee, idx) in detail.feeBreakdown" :key="idx" class="breakdown-line">
            <text>{{ fee.name }}</text>
            <text>¥{{ fee.amount?.toFixed(2) }}</text>
          </view>
        </view>
      </view>

      <view v-if="detail.guideTip" class="guide-card">
        <text class="guide-title">导诊建议</text>
        <text class="guide-text">{{ detail.guideTip }}</text>
      </view>

      <view v-if="detail.advice" class="guide-card advice">
        <text class="guide-title">医嘱提醒</text>
        <text class="guide-text">{{ detail.advice }}</text>
      </view>

      <view class="action-group">
        <button class="action-btn primary" @click="goRecords">查看报告 / 就诊记录</button>
        <button v-if="detail.recordId" class="action-btn ai" @click="goAiConsult">
          AI 问诊 · 获取就诊后建议
        </button>
        <button class="action-btn secondary" @click="goTriage">导诊建议 · 科室导航</button>
        <text class="action-skip" @click="goHome">返回首页</text>
      </view>
    </view>
  </view>
</template>

<script setup>
import { ref } from 'vue'
import { onLoad } from '@dcloudio/uni-app'
import { mpApi } from '@/api'

const detail = ref({ feeBreakdown: [] })
const voucherNo = ref('')

onLoad(async (options) => {
  voucherNo.value = options.voucherNo || `PZ${Date.now()}`
  if (options.id) {
    const res = await mpApi.paymentDetail(Number(options.id))
    detail.value = res.data || {}
    if (!options.voucherNo) voucherNo.value = `PZ${detail.value.paymentNo?.slice(-8) || Date.now()}`
  }
})

function goRecords() {
  uni.navigateTo({ url: '/pages/records/records' })
}

function goAiConsult() {
  const id = detail.value.recordId
  if (id) {
    uni.navigateTo({ url: `/pages/records/records?aiRecordId=${id}` })
  } else {
    goRecords()
  }
}

function goTriage() {
  const dept = detail.value.department
  if (dept) {
    uni.navigateTo({ url: `/pages/department/department?keyword=${encodeURIComponent(dept)}` })
  } else {
    uni.navigateTo({ url: '/pages/department/department' })
  }
}

function goHome() {
  uni.redirectTo({ url: '/pages/index/index' })
}
</script>

<style lang="scss" scoped>
.page {
  min-height: 100vh;
  background: #f5f6f7;
}

.success-header {
  background: linear-gradient(160deg, #3370ff 0%, #5b8fff 100%);
  padding: 60rpx 40rpx 80rpx;
  text-align: center;
  color: #fff;
}

.success-icon {
  width: 96rpx;
  height: 96rpx;
  border-radius: 50%;
  background: rgba(255, 255, 255, 0.25);
  font-size: 48rpx;
  font-weight: 700;
  display: flex;
  align-items: center;
  justify-content: center;
  margin: 0 auto 24rpx;
}

.success-title {
  display: block;
  font-size: 36rpx;
  font-weight: 600;
}

.success-amount {
  display: block;
  font-size: 56rpx;
  font-weight: 700;
  margin-top: 16rpx;
}

.success-desc {
  display: block;
  font-size: 26rpx;
  opacity: 0.9;
  margin-top: 12rpx;
}

.container {
  margin-top: -40rpx;
  padding: 0 24rpx 48rpx;
}

.voucher-card {
  position: relative;
  background: #fff;
  border-radius: 20rpx;
  padding: 32rpx;
  box-shadow: 0 8rpx 32rpx rgba(31, 35, 41, 0.08);
  border: 1rpx solid #e5e6eb;
  margin-bottom: 24rpx;
}

.voucher-stamp {
  position: absolute;
  top: 24rpx;
  right: 24rpx;
  font-size: 22rpx;
  color: #34c724;
  border: 2rpx solid #34c724;
  padding: 4rpx 16rpx;
  border-radius: 8rpx;
  transform: rotate(-12deg);
  opacity: 0.8;
}

.voucher-title {
  display: block;
  font-size: 30rpx;
  font-weight: 600;
  color: #1f2329;
  margin-bottom: 24rpx;
}

.voucher-row {
  display: flex;
  justify-content: space-between;
  padding: 14rpx 0;
  font-size: 26rpx;
}

.voucher-label { color: #8f959e; }

.voucher-value {
  color: #1f2329;
  font-weight: 500;
  text-align: right;
  max-width: 60%;
}

.voucher-divider {
  height: 1rpx;
  background: repeating-linear-gradient(90deg, #e5e6eb, #e5e6eb 8rpx, transparent 8rpx, transparent 16rpx);
  margin: 20rpx 0;
}

.voucher-breakdown { margin-top: 8rpx; }

.breakdown-heading {
  display: block;
  font-size: 24rpx;
  color: #8f959e;
  margin-bottom: 12rpx;
}

.breakdown-line {
  display: flex;
  justify-content: space-between;
  font-size: 26rpx;
  color: #646a73;
  padding: 8rpx 0;
}

.guide-card {
  background: #f0f4ff;
  border-radius: 16rpx;
  padding: 24rpx;
  margin-bottom: 20rpx;
  border: 1rpx solid #d6e4ff;
}

.guide-card.advice {
  background: #fff9eb;
  border-color: #ffe7ba;
}

.guide-title {
  display: block;
  font-size: 26rpx;
  font-weight: 600;
  color: #3370ff;
  margin-bottom: 10rpx;
}

.guide-card.advice .guide-title { color: #ff8800; }

.guide-text {
  font-size: 26rpx;
  color: #646a73;
  line-height: 1.6;
}

.action-group {
  margin-top: 32rpx;
}

.action-btn {
  width: 100%;
  height: 88rpx;
  line-height: 88rpx;
  border-radius: 48rpx;
  font-size: 30rpx;
  font-weight: 500;
  border: none;
  margin-bottom: 20rpx;

  &::after { border: none; }
}

.action-btn.primary {
  background: linear-gradient(135deg, #3370ff, #5b8fff);
  color: #fff;
  box-shadow: 0 8rpx 24rpx rgba(51, 112, 255, 0.25);
}

.action-btn.ai {
  background: linear-gradient(135deg, #7c3aed, #a78bfa);
  color: #fff;
  box-shadow: 0 8rpx 24rpx rgba(124, 58, 237, 0.25);
}

.action-btn.secondary {
  background: #fff;
  color: #3370ff;
  border: 2rpx solid #3370ff;
}

.action-skip {
  display: block;
  text-align: center;
  font-size: 26rpx;
  color: #8f959e;
  margin-top: 16rpx;
}
</style>
