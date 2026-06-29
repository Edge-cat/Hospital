<template>
  <view class="page">
    <view class="header-bg" />

    <view class="ecard-wrap">
      <view class="ecard" @click="onCardTap">
        <view class="ecard-top">
          <view class="ecard-brand">
            <text class="brand-icon">🏥</text>
            <text class="brand-name">东软云医院</text>
          </view>
          <text class="ecard-tag">电子就诊卡</text>
        </view>

        <view class="ecard-body">
          <view class="avatar-wrap" @click.stop="chooseAvatar">
            <image v-if="avatarUrl" class="avatar-img" :src="avatarUrl" mode="aspectFill" />
            <view v-else class="avatar-fallback">{{ displayInitial }}</view>
            <view v-if="isLogin" class="avatar-edit">换</view>
          </view>
          <view class="ecard-info">
            <text class="ecard-name">{{ isLogin ? userName : '未登录' }}</text>
            <text class="ecard-phone">{{ isLogin ? maskPhone(userPhone) : '登录后查看就诊信息' }}</text>
            <text v-if="isLogin" class="ecard-no">就诊卡号 {{ cardNo }}</text>
          </view>
        </view>

        <view v-if="isLogin" class="ecard-footer">
          <text>身份建档</text>
          <text class="dot">·</text>
          <text>流程办理</text>
          <text class="dot">·</text>
          <text>诊后追溯</text>
        </view>
      </view>

      <button v-if="!isLogin" class="login-btn" @click="goLogin">登录 / 注册</button>
    </view>

    <view class="container">
      <view v-if="isLogin && pendingTasks.length" class="pending-section">
        <text class="section-title">待办提醒</text>
        <view
          v-for="task in pendingTasks"
          :key="task.key"
          class="pending-card"
          @click="goPage(task.path)"
        >
          <view class="pending-left">
            <text class="pending-badge" :class="task.badgeClass">{{ task.badge }}</text>
            <view class="pending-text">
              <text class="pending-title">{{ task.title }}</text>
              <text class="pending-desc">{{ task.desc }}</text>
            </view>
          </view>
          <text class="pending-action">{{ task.action }} ›</text>
        </view>
      </view>

      <view class="menu-group">
        <text class="section-title">流程办理</text>
        <view class="group-card">
          <view class="menu-item" @click="goPage('/pages/mine/itinerary')">
            <text class="menu-icon">🗓️</text>
            <text class="menu-text">就诊行程</text>
            <view class="menu-right">
              <text v-if="itineraryBadge" class="status-badge">{{ itineraryBadge }}</text>
              <text class="menu-arrow">›</text>
            </view>
          </view>
        </view>
      </view>

      <view class="menu-group">
        <text class="section-title">诊后追溯</text>
        <view class="group-card">
          <view class="menu-item" @click="goPage('/pages/records/records')">
            <text class="menu-icon">📄</text>
            <text class="menu-text">就诊记录</text>
            <text class="menu-arrow">›</text>
          </view>
          <view class="menu-item" @click="goPaidTab">
            <text class="menu-icon">🧾</text>
            <text class="menu-text">历史账单与凭证</text>
            <text class="menu-arrow">›</text>
          </view>
        </view>
      </view>

      <view class="menu-group">
        <text class="section-title">身份建档</text>
        <view class="group-card">
          <view class="menu-item" @click="goPage('/pages/mine/profile')">
            <text class="menu-icon">👤</text>
            <text class="menu-text">个人信息</text>
            <text class="menu-arrow">›</text>
          </view>
        </view>
      </view>

      <button v-if="isLogin" class="logout-btn" @click="handleLogout">退出登录</button>
    </view>

    <TabBar current="/pages/mine/mine" />
  </view>
</template>

<script setup>
import { ref, computed } from 'vue'
import { onShow } from '@dcloudio/uni-app'
import TabBar from '@/components/TabBar.vue'
import { mpApi } from '@/api'
import { checkLogin, navigateToLogin } from '@/utils/request'

const isLogin = ref(false)
const userName = ref('游客')
const userPhone = ref('')
const avatarUrl = ref('')
const cardNo = ref('P2026001286')
const pendingTasks = ref([])
const itineraryBadge = ref('')

const displayInitial = computed(() => {
  const name = userName.value || '游'
  return name.charAt(0)
})

onShow(() => {
  loadUser()
  if (isLogin.value) loadDashboard()
})

function loadUser() {
  isLogin.value = checkLogin()
  avatarUrl.value = uni.getStorageSync('his_avatar') || ''
  const user = uni.getStorageSync('his_user')
  if (user) {
    const parsed = JSON.parse(user)
    userName.value = parsed.name || '患者'
    userPhone.value = parsed.phone || ''
    if (parsed.avatar) avatarUrl.value = parsed.avatar
  } else {
    userName.value = '游客'
    userPhone.value = ''
  }
}

async function loadDashboard() {
  try {
    const [registerRes, appointmentRes, paymentRes] = await Promise.all([
      mpApi.registerList(),
      mpApi.appointmentList(),
      mpApi.paymentList({ status: 0 })
    ])
    const registers = registerRes.data.list.filter((r) => r.status === 0)
    const appointments = appointmentRes.data.list.filter((a) => a.status < 2)
    const unpaid = paymentRes.data.list

    const tasks = []
    if (unpaid.length) {
      tasks.push({
        key: 'pay',
        badge: '待支付',
        badgeClass: 'warning',
        title: unpaid[0].itemName,
        desc: `¥${unpaid[0].amount} · 请尽快完成缴费`,
        action: '去缴费',
        path: '/pages/payment/payment'
      })
    }
    registers.forEach((r) => {
      tasks.push({
        key: `r-${r.id}`,
        badge: unpaid.length ? '待支付' : '待就诊',
        badgeClass: unpaid.length ? 'warning' : 'primary',
        title: `${r.department} · ${r.doctorName}`,
        desc: r.registerTime,
        action: unpaid.length ? '去缴费' : '查看行程',
        path: unpaid.length ? '/pages/payment/payment' : '/pages/mine/itinerary'
      })
    })
    appointments.forEach((a) => {
      tasks.push({
        key: `a-${a.id}`,
        badge: a.status === 0 ? '待确认' : '待就诊',
        badgeClass: 'primary',
        title: `${a.department} · ${a.doctorName}`,
        desc: `${a.appointmentDate} ${a.timeSlot}`,
        action: '查看行程',
        path: '/pages/mine/itinerary'
      })
    })
    pendingTasks.value = tasks.slice(0, 3)

    const pendingCount = registers.length + appointments.length
    if (unpaid.length) itineraryBadge.value = '待支付'
    else if (pendingCount) itineraryBadge.value = '待就诊'
    else itineraryBadge.value = ''
  } catch {
    pendingTasks.value = []
    itineraryBadge.value = ''
  }
}

function maskPhone(phone) {
  if (!phone) return '--'
  if (phone.includes('*')) return phone
  return phone.replace(/(\d{3})\d{4}(\d{4})/, '$1****$2')
}

function chooseAvatar() {
  if (!isLogin.value) { goLogin(); return }
  uni.chooseImage({
    count: 1,
    sizeType: ['compressed'],
    success(res) {
      const path = res.tempFilePaths[0]
      avatarUrl.value = path
      uni.setStorageSync('his_avatar', path)
      const user = uni.getStorageSync('his_user')
      if (user) {
        const parsed = JSON.parse(user)
        parsed.avatar = path
        uni.setStorageSync('his_user', JSON.stringify(parsed))
      }
      uni.showToast({ title: '头像已更新', icon: 'success' })
    }
  })
}

function onCardTap() {
  if (!isLogin.value) goLogin()
  else goPage('/pages/mine/profile')
}

function goLogin() {
  navigateToLogin()
}

function goPage(path) {
  if (!checkLogin()) { navigateToLogin(); return }
  uni.navigateTo({ url: path })
}

function goPaidTab() {
  if (!checkLogin()) { navigateToLogin(); return }
  uni.navigateTo({ url: '/pages/payment/payment?tab=paid' })
}

function handleLogout() {
  uni.showModal({
    title: '提示',
    content: '确定退出登录？',
    success(res) {
      if (res.confirm) {
        uni.removeStorageSync('his_token')
        uni.removeStorageSync('his_user')
        uni.removeStorageSync('his_avatar')
        isLogin.value = false
        userName.value = '游客'
        userPhone.value = ''
        avatarUrl.value = ''
        pendingTasks.value = []
        itineraryBadge.value = ''
        uni.showToast({ title: '已退出', icon: 'none' })
      }
    }
  })
}
</script>

<style lang="scss" scoped>
.page {
  min-height: 100vh;
  background: #f5f6f7;
}

.header-bg {
  height: 200rpx;
  background: linear-gradient(160deg, #3370ff 0%, #5b8fff 100%);
}

.ecard-wrap {
  margin: -120rpx 24rpx 0;
  position: relative;
  z-index: 2;
}

.ecard {
  background: linear-gradient(135deg, #ffffff 0%, #f8faff 100%);
  border-radius: 24rpx;
  padding: 28rpx;
  box-shadow: 0 12rpx 40rpx rgba(51, 112, 255, 0.15);
  border: 1rpx solid rgba(255, 255, 255, 0.8);
}

.ecard-top {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 28rpx;
}

.ecard-brand {
  display: flex;
  align-items: center;
  gap: 8rpx;
}

.brand-icon { font-size: 32rpx; }

.brand-name {
  font-size: 24rpx;
  color: #646a73;
  font-weight: 500;
}

.ecard-tag {
  font-size: 20rpx;
  color: #3370ff;
  background: #f0f4ff;
  padding: 6rpx 16rpx;
  border-radius: 20rpx;
}

.ecard-body {
  display: flex;
  align-items: center;
}

.avatar-wrap {
  position: relative;
  flex-shrink: 0;
}

.avatar-img,
.avatar-fallback {
  width: 112rpx;
  height: 112rpx;
  border-radius: 50%;
  border: 4rpx solid #fff;
  box-shadow: 0 4rpx 16rpx rgba(51, 112, 255, 0.2);
}

.avatar-fallback {
  background: linear-gradient(135deg, #3370ff, #66b1ff);
  color: #fff;
  font-size: 44rpx;
  font-weight: 700;
  display: flex;
  align-items: center;
  justify-content: center;
}

.avatar-edit {
  position: absolute;
  right: 0;
  bottom: 0;
  width: 36rpx;
  height: 36rpx;
  background: #3370ff;
  color: #fff;
  font-size: 18rpx;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  border: 2rpx solid #fff;
}

.ecard-info {
  flex: 1;
  margin-left: 24rpx;
}

.ecard-name {
  display: block;
  font-size: 36rpx;
  font-weight: 700;
  color: #1f2329;
}

.ecard-phone {
  display: block;
  font-size: 28rpx;
  color: #646a73;
  margin-top: 8rpx;
  letter-spacing: 1rpx;
}

.ecard-no {
  display: block;
  font-size: 22rpx;
  color: #8f959e;
  margin-top: 12rpx;
  font-family: monospace;
}

.ecard-footer {
  display: flex;
  align-items: center;
  justify-content: center;
  margin-top: 24rpx;
  padding-top: 20rpx;
  border-top: 1rpx dashed #e5e6eb;
  font-size: 22rpx;
  color: #8f959e;
}

.dot { margin: 0 12rpx; }

.login-btn {
  margin-top: 24rpx;
  background: #fff;
  color: #3370ff;
  border: none;
  border-radius: 48rpx;
  font-size: 30rpx;
  font-weight: 500;
  height: 88rpx;
  line-height: 88rpx;
  box-shadow: 0 8rpx 24rpx rgba(51, 112, 255, 0.12);
}

.login-btn::after { border: none; }

.container {
  padding-top: 32rpx;
}

.section-title {
  display: block;
  font-size: 26rpx;
  color: #8f959e;
  margin-bottom: 16rpx;
  padding-left: 8rpx;
}

.pending-section { margin-bottom: 32rpx; }

.pending-card {
  display: flex;
  align-items: center;
  justify-content: space-between;
  background: #fff;
  border-radius: 16rpx;
  padding: 24rpx;
  margin-bottom: 16rpx;
  box-shadow: 0 6rpx 20rpx rgba(31, 35, 41, 0.06);
  border: 1rpx solid #e5e6eb;
}

.pending-left {
  display: flex;
  align-items: center;
  flex: 1;
  min-width: 0;
}

.pending-badge {
  font-size: 20rpx;
  padding: 6rpx 12rpx;
  border-radius: 8rpx;
  flex-shrink: 0;
}

.pending-badge.warning {
  color: #ff8800;
  background: #fff5eb;
}

.pending-badge.primary {
  color: #3370ff;
  background: #f0f4ff;
}

.pending-text {
  margin-left: 16rpx;
  flex: 1;
  min-width: 0;
}

.pending-title {
  display: block;
  font-size: 28rpx;
  color: #1f2329;
  font-weight: 500;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.pending-desc {
  display: block;
  font-size: 22rpx;
  color: #8f959e;
  margin-top: 4rpx;
}

.pending-action {
  font-size: 26rpx;
  color: #3370ff;
  flex-shrink: 0;
  margin-left: 16rpx;
}

.menu-group { margin-bottom: 28rpx; }

.group-card {
  background: #fff;
  border-radius: 20rpx;
  overflow: hidden;
  box-shadow: 0 8rpx 24rpx rgba(31, 35, 41, 0.06);
  border: 1rpx solid #e5e6eb;
}

.menu-item {
  display: flex;
  align-items: center;
  padding: 32rpx 28rpx;
  border-bottom: 1rpx solid #f0f1f3;
}

.menu-item:last-child { border-bottom: none; }

.menu-icon {
  font-size: 36rpx;
  margin-right: 20rpx;
}

.menu-text {
  flex: 1;
  font-size: 30rpx;
  color: #1f2329;
}

.menu-right {
  display: flex;
  align-items: center;
  gap: 12rpx;
}

.status-badge {
  font-size: 20rpx;
  color: #fff;
  background: #f54a45;
  padding: 4rpx 12rpx;
  border-radius: 16rpx;
}

.menu-arrow {
  color: #c9cdd4;
  font-size: 36rpx;
}

.logout-btn {
  margin-top: 16rpx;
  background: #fff;
  color: #f54a45;
  border: 1rpx solid #ffece8;
  border-radius: 48rpx;
  font-size: 30rpx;
  box-shadow: 0 4rpx 16rpx rgba(31, 35, 41, 0.04);
}

.logout-btn::after { border: none; }
</style>
