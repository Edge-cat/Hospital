<template>
  <view class="page">
    <view class="summary-bar" v-if="tab === 'pending' && pendingTotal > 0">
      <view class="summary-left">
        <text class="summary-label">待缴合计</text>
        <text class="summary-amount">¥{{ pendingTotal.toFixed(2) }}</text>
      </view>
      <text class="summary-count">{{ list.length }} 笔待缴</text>
    </view>

    <view class="tabs">
      <view class="tab" :class="{ active: tab === 'pending' }" @click="switchTab('pending')">待缴费</view>
      <view class="tab" :class="{ active: tab === 'paid' }" @click="switchTab('paid')">已缴费</view>
    </view>

    <view class="container">
      <view v-if="list.length === 0" class="empty-tip">暂无缴费记录</view>

      <view
        v-for="item in list"
        :key="item.id"
        class="bill-card"
        :class="[`bill-card--${item.itemType || 'default'}`]"
      >
        <view class="bill-accent" />

        <view class="bill-header">
          <view class="bill-icon-wrap">
            <text class="bill-icon">{{ itemTypeIcon(item.itemType) }}</text>
          </view>
          <view class="bill-title-area">
            <text class="bill-title">{{ item.itemName }}</text>
            <text class="bill-meta">{{ item.department }} · {{ item.doctorName }}</text>
          </view>
          <text class="tag" :class="paymentStatusMap[item.status]?.class">
            {{ paymentStatusMap[item.status]?.label }}
          </text>
        </view>

        <view class="bill-amount-row">
          <text class="bill-amount-label">应付金额</text>
          <text class="bill-amount">¥{{ item.amount?.toFixed(2) }}</text>
        </view>

        <view class="breakdown-section">
          <view class="breakdown-header">
            <text class="breakdown-title">费用构成</text>
            <text class="breakdown-link" @click="openDetail(item)">查看明细 ›</text>
          </view>
          <view class="breakdown-preview">
            <view
              v-for="(fee, idx) in previewBreakdown(item)"
              :key="idx"
              class="breakdown-item"
            >
              <text class="breakdown-name">{{ fee.name }}</text>
              <text class="breakdown-fee">¥{{ fee.amount?.toFixed(2) }}</text>
            </view>
            <text v-if="(item.feeBreakdown?.length || 0) > 2" class="breakdown-more">
              共 {{ item.feeBreakdown.length }} 项
            </text>
          </view>
        </view>

        <view class="bill-info">
          <text class="bill-no">单号 {{ item.paymentNo }}</text>
          <text class="bill-time">{{ item.status === 1 ? item.payTime : item.createTime }}</text>
        </view>

        <template v-if="item.status === 0">
          <view class="advice-box">
            <text class="advice-label">医嘱说明</text>
            <text class="advice-text">{{ item.advice }}</text>
          </view>
          <button class="pay-btn" :loading="payingId === item.id" @click="handlePay(item)">
            立即支付 ¥{{ item.amount?.toFixed(2) }}
          </button>
        </template>

        <view v-else class="paid-actions">
          <button class="voucher-btn" @click="goResult(item)">查看支付凭证</button>
        </view>
      </view>
    </view>

    <view v-if="detailItem" class="detail-mask" @click="detailItem = null">
      <view class="detail-sheet" @click.stop>
        <view class="detail-handle" />
        <text class="detail-title">费用明细</text>
        <text class="detail-sub">{{ detailItem.itemName }} · {{ detailItem.paymentNo }}</text>

        <view class="detail-list">
          <view v-for="(fee, idx) in detailItem.feeBreakdown" :key="idx" class="detail-row">
            <text class="detail-name">{{ fee.name }}</text>
            <text class="detail-fee">¥{{ fee.amount?.toFixed(2) }}</text>
          </view>
          <view class="detail-total">
            <text>合计</text>
            <text class="detail-total-fee">¥{{ detailItem.amount?.toFixed(2) }}</text>
          </view>
        </view>

        <view v-if="detailItem.guideTip" class="detail-guide">
          <text class="detail-guide-label">就诊导引</text>
          <text class="detail-guide-text">{{ detailItem.guideTip }}</text>
        </view>

        <button class="detail-close" @click="detailItem = null">关闭</button>
      </view>
    </view>

    <TabBar current="/pages/payment/payment" />
  </view>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { onLoad } from '@dcloudio/uni-app'
import TabBar from '@/components/TabBar.vue'
import { mpApi } from '@/api'
import { paymentStatusMap, checkLogin, navigateToLogin } from '@/utils/request'

const tab = ref('pending')
const list = ref([])
const detailItem = ref(null)
const payingId = ref(null)

const pendingTotal = computed(() =>
  list.value.reduce((sum, item) => sum + (item.amount || 0), 0)
)

onMounted(() => loadData())

onLoad((options) => {
  if (options?.tab === 'paid') tab.value = 'paid'
})

async function loadData() {
  const status = tab.value === 'pending' ? 0 : 1
  const res = await mpApi.paymentList({ status })
  list.value = res.data.list
}

function switchTab(t) {
  tab.value = t
  loadData()
}

function itemTypeIcon(type) {
  const map = { register: '📋', check: '🔬', medicine: '💊' }
  return map[type] || '💰'
}

function previewBreakdown(item) {
  return (item.feeBreakdown || []).slice(0, 2)
}

function openDetail(item) {
  detailItem.value = item
}

function goResult(item) {
  uni.navigateTo({ url: `/pages/payment/result?id=${item.id}` })
}

async function handlePay(item) {
  if (!checkLogin()) { navigateToLogin(); return }

  uni.showModal({
    title: '确认支付',
    content: `支付 ${item.itemName} ¥${item.amount.toFixed(2)}？`,
    success: async (res) => {
      if (!res.confirm) return
      payingId.value = item.id
      try {
        const payRes = await mpApi.payment({ id: item.id, payMethod: '微信' })
        const voucher = payRes.data || item
        uni.navigateTo({
          url: `/pages/payment/result?id=${item.id}&voucherNo=${voucher.voucherNo || ''}`
        })
        loadData()
      } finally {
        payingId.value = null
      }
    }
  })
}
</script>

<style lang="scss" scoped>
.page { min-height: 100vh; background: #f5f6f7; }

.summary-bar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  background: linear-gradient(135deg, #3370ff, #5b8fff);
  padding: 28rpx 32rpx;
  color: #fff;
}

.summary-label {
  font-size: 26rpx;
  opacity: 0.9;
  display: block;
}

.summary-amount {
  font-size: 44rpx;
  font-weight: 700;
  display: block;
  margin-top: 4rpx;
}

.summary-count {
  font-size: 24rpx;
  opacity: 0.85;
  background: rgba(255, 255, 255, 0.2);
  padding: 8rpx 20rpx;
  border-radius: 24rpx;
}

.tabs {
  display: flex;
  background: #fff;
  padding: 0 24rpx;
  border-bottom: 1rpx solid #e5e6eb;
}

.tab {
  flex: 1;
  text-align: center;
  padding: 24rpx 0;
  font-size: 30rpx;
  color: #646a73;
  position: relative;
}

.tab.active {
  color: #3370ff;
  font-weight: 600;
}

.tab.active::after {
  content: '';
  position: absolute;
  bottom: 0;
  left: 50%;
  transform: translateX(-50%);
  width: 60rpx;
  height: 4rpx;
  background: #3370ff;
  border-radius: 2rpx;
}

.bill-card {
  position: relative;
  background: #fff;
  border-radius: 20rpx;
  padding: 28rpx;
  margin-bottom: 24rpx;
  box-shadow: 0 8rpx 32rpx rgba(31, 35, 41, 0.06);
  border: 1rpx solid #e5e6eb;
  overflow: hidden;
}

.bill-accent {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  height: 6rpx;
  background: linear-gradient(90deg, #3370ff, #66b1ff);
}

.bill-card--check .bill-accent {
  background: linear-gradient(90deg, #7c3aed, #a78bfa);
}

.bill-card--medicine .bill-accent {
  background: linear-gradient(90deg, #34c724, #7be188);
}

.bill-card--register .bill-accent {
  background: linear-gradient(90deg, #3370ff, #66b1ff);
}

.bill-header {
  display: flex;
  align-items: flex-start;
  margin-bottom: 24rpx;
}

.bill-icon-wrap {
  width: 80rpx;
  height: 80rpx;
  background: #f0f4ff;
  border-radius: 20rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}

.bill-card--check .bill-icon-wrap { background: #f3f0ff; }
.bill-card--medicine .bill-icon-wrap { background: #eaffea; }

.bill-icon { font-size: 40rpx; }

.bill-title-area {
  flex: 1;
  margin-left: 20rpx;
  min-width: 0;
}

.bill-title {
  display: block;
  font-size: 32rpx;
  font-weight: 600;
  color: #1f2329;
}

.bill-meta {
  display: block;
  font-size: 24rpx;
  color: #8f959e;
  margin-top: 6rpx;
}

.bill-amount-row {
  display: flex;
  justify-content: space-between;
  align-items: baseline;
  padding: 20rpx 0;
  border-top: 1rpx solid #f0f1f3;
  border-bottom: 1rpx solid #f0f1f3;
}

.bill-amount-label {
  font-size: 26rpx;
  color: #8f959e;
}

.bill-amount {
  font-size: 40rpx;
  font-weight: 700;
  color: #f54a45;
}

.breakdown-section {
  padding: 20rpx 0;
}

.breakdown-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 12rpx;
}

.breakdown-title {
  font-size: 26rpx;
  font-weight: 600;
  color: #646a73;
}

.breakdown-link {
  font-size: 24rpx;
  color: #3370ff;
}

.breakdown-preview {
  background: #f7f8fa;
  border-radius: 12rpx;
  padding: 16rpx 20rpx;
}

.breakdown-item {
  display: flex;
  justify-content: space-between;
  padding: 8rpx 0;
  font-size: 26rpx;
}

.breakdown-name { color: #646a73; }
.breakdown-fee { color: #1f2329; font-weight: 500; }

.breakdown-more {
  display: block;
  font-size: 22rpx;
  color: #8f959e;
  margin-top: 8rpx;
}

.bill-info {
  display: flex;
  justify-content: space-between;
  font-size: 22rpx;
  color: #c9cdd4;
  margin-bottom: 20rpx;
}

.advice-box {
  background: #fff9eb;
  border: 1rpx solid #ffe7ba;
  border-radius: 12rpx;
  padding: 20rpx;
  margin-bottom: 20rpx;
}

.advice-label {
  display: block;
  font-size: 22rpx;
  color: #ff8800;
  font-weight: 600;
  margin-bottom: 8rpx;
}

.advice-text {
  font-size: 26rpx;
  color: #646a73;
  line-height: 1.6;
}

.pay-btn {
  background: linear-gradient(135deg, #3370ff, #5b8fff);
  color: #fff;
  border: none;
  border-radius: 48rpx;
  font-size: 32rpx;
  font-weight: 500;
  height: 88rpx;
  line-height: 88rpx;
  box-shadow: 0 8rpx 24rpx rgba(51, 112, 255, 0.3);
}

.pay-btn::after { border: none; }

.paid-actions { margin-top: 8rpx; }

.voucher-btn {
  background: #f0f4ff;
  color: #3370ff;
  border: none;
  border-radius: 40rpx;
  font-size: 28rpx;
  height: 72rpx;
  line-height: 72rpx;
}

.voucher-btn::after { border: none; }

.detail-mask {
  position: fixed;
  inset: 0;
  background: rgba(0, 0, 0, 0.45);
  z-index: 1000;
  display: flex;
  align-items: flex-end;
}

.detail-sheet {
  width: 100%;
  background: #fff;
  border-radius: 32rpx 32rpx 0 0;
  padding: 24rpx 32rpx calc(32rpx + env(safe-area-inset-bottom));
}

.detail-handle {
  width: 64rpx;
  height: 8rpx;
  background: #e5e6eb;
  border-radius: 4rpx;
  margin: 0 auto 24rpx;
}

.detail-title {
  display: block;
  font-size: 36rpx;
  font-weight: 700;
  color: #1f2329;
}

.detail-sub {
  display: block;
  font-size: 24rpx;
  color: #8f959e;
  margin: 8rpx 0 24rpx;
}

.detail-list {
  background: #f7f8fa;
  border-radius: 16rpx;
  padding: 8rpx 24rpx;
  margin-bottom: 24rpx;
}

.detail-row {
  display: flex;
  justify-content: space-between;
  padding: 20rpx 0;
  border-bottom: 1rpx solid #e5e6eb;
  font-size: 28rpx;
}

.detail-row:last-of-type { border-bottom: none; }

.detail-name { color: #646a73; }
.detail-fee { color: #1f2329; font-weight: 500; }

.detail-total {
  display: flex;
  justify-content: space-between;
  padding: 24rpx 0 12rpx;
  border-top: 1rpx solid #e5e6eb;
  font-size: 30rpx;
  font-weight: 600;
  color: #1f2329;
}

.detail-total-fee { color: #f54a45; }

.detail-guide {
  background: #f0f4ff;
  border-radius: 12rpx;
  padding: 20rpx;
  margin-bottom: 24rpx;
}

.detail-guide-label {
  display: block;
  font-size: 22rpx;
  color: #3370ff;
  font-weight: 600;
  margin-bottom: 8rpx;
}

.detail-guide-text {
  font-size: 26rpx;
  color: #646a73;
  line-height: 1.6;
}

.detail-close {
  background: #f7f8fa;
  color: #646a73;
  border: none;
  border-radius: 16rpx;
  font-size: 30rpx;
  height: 88rpx;
  line-height: 88rpx;
}

.detail-close::after { border: none; }
</style>
