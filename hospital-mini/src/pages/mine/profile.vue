<template>
  <view class="container">
    <view class="profile-header">
      <view class="avatar-area" @click="chooseAvatar">
        <image v-if="avatarUrl" class="avatar" :src="avatarUrl" mode="aspectFill" />
        <view v-else class="avatar fallback">{{ profile.name?.charAt(0) || '患' }}</view>
        <text class="avatar-tip">点击更换头像</text>
      </view>
    </view>

    <view class="card">
      <view class="form-item">
        <text class="form-label">姓名</text>
        <text class="form-value">{{ profile.name }}</text>
      </view>
      <view class="form-item">
        <text class="form-label">手机号</text>
        <text class="form-value">{{ maskPhone(profile.phone) }}</text>
      </view>
      <view class="form-item" style="border: none">
        <text class="form-label">身份证号</text>
        <text class="form-value">{{ maskIdCard(profile.idCard) }}</text>
      </view>
    </view>

    <view class="card">
      <view class="card-title">就诊卡信息</view>
      <view class="form-item">
        <text class="form-label">就诊卡号</text>
        <text class="form-value">P2026001286</text>
      </view>
      <view class="form-item" style="border: none">
        <text class="form-label">绑定状态</text>
        <text class="tag tag-success">已绑定</text>
      </view>
    </view>
  </view>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { mpApi } from '@/api'
import { checkLogin, navigateToLogin } from '@/utils/request'

const profile = ref({ name: '', phone: '', idCard: '' })
const avatarUrl = ref('')

onMounted(async () => {
  if (!checkLogin()) { navigateToLogin(); return }
  avatarUrl.value = uni.getStorageSync('his_avatar') || ''
  const res = await mpApi.patientInfo()
  profile.value = { ...profile.value, ...res.data, idCard: res.data.idCard || '110101199001011234' }
  if (res.data.avatar) avatarUrl.value = res.data.avatar
})

function maskPhone(phone) {
  if (!phone) return '--'
  if (phone.includes('*')) return phone
  return phone.replace(/(\d{3})\d{4}(\d{4})/, '$1****$2')
}

function chooseAvatar() {
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

function maskIdCard(id) {
  if (!id || id.length < 10) return id
  return id.slice(0, 6) + '********' + id.slice(-4)
}
</script>

<style lang="scss" scoped>
.profile-header {
  display: flex;
  justify-content: center;
  padding: 32rpx 0 16rpx;
}

.avatar-area {
  display: flex;
  flex-direction: column;
  align-items: center;
}

.avatar {
  width: 140rpx;
  height: 140rpx;
  border-radius: 50%;
  border: 4rpx solid #fff;
  box-shadow: 0 8rpx 24rpx rgba(51, 112, 255, 0.15);
}

.avatar.fallback {
  background: linear-gradient(135deg, #3370ff, #66b1ff);
  color: #fff;
  font-size: 56rpx;
  font-weight: 700;
  display: flex;
  align-items: center;
  justify-content: center;
}

.avatar-tip {
  font-size: 22rpx;
  color: #8f959e;
  margin-top: 12rpx;
}
</style>
