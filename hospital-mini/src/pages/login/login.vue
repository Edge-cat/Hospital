<template>
  <view class="login-page">
    <view class="login-bg">
      <text class="logo-icon">🏥</text>
      <text class="logo-title">东软云医院</text>
      <text class="logo-sub">患者端小程序</text>
    </view>

    <view class="login-form">
      <view class="input-group">
        <text class="input-icon">👤</text>
        <input v-model="form.username" class="input" placeholder="请输入手机号/用户名" />
      </view>
      <view class="input-group">
        <text class="input-icon">🔒</text>
        <input v-model="form.password" class="input" password placeholder="请输入密码" />
      </view>
      <button class="btn-primary login-btn" :loading="loading" @click="handleLogin">登 录</button>
      <text class="demo-tip">演示账号：13800138000 / 123456</text>
      <button class="wx-btn" :loading="wxLoading" @click="wxLogin">
        微信快捷登录
      </button>
    </view>
  </view>
</template>

<script setup>
import { reactive, ref } from 'vue'
import { mpApi } from '@/api'

const loading = ref(false)
const wxLoading = ref(false)
const form = reactive({ username: '13800138000', password: '123456' })

async function handleLogin() {
  if (!form.username || !form.password) {
    uni.showToast({ title: '请输入账号密码', icon: 'none' })
    return
  }
  loading.value = true
  try {
    const res = await mpApi.login(form)
    uni.setStorageSync('his_token', res.data.token)
    uni.setStorageSync('his_user', JSON.stringify(res.data.user))
    uni.showToast({ title: '登录成功', icon: 'success' })
    setTimeout(() => uni.navigateBack({ fail: () => uni.redirectTo({ url: '/pages/index/index' }) }), 1500)
  } finally {
    loading.value = false
  }
}

async function wxLogin() {
  wxLoading.value = true
  try {
    const loginRes = await new Promise((resolve, reject) => {
      uni.login({ provider: 'weixin', success: resolve, fail: reject })
    })
    const res = await mpApi.wxLogin({ code: loginRes.code || 'demo' })
    uni.setStorageSync('his_token', res.data.token)
    uni.setStorageSync('his_user', JSON.stringify(res.data.user))
    uni.showToast({ title: '微信登录成功', icon: 'success' })
    setTimeout(() => uni.navigateBack({ fail: () => uni.redirectTo({ url: '/pages/index/index' }) }), 1500)
  } catch {
    uni.showToast({ title: '微信登录失败', icon: 'none' })
  } finally {
    wxLoading.value = false
  }
}
</script>

<style lang="scss" scoped>
.login-page {
  min-height: 100vh;
  background: linear-gradient(180deg, #3370ff 0%, #4e83fd 40%, #f5f6f7 40%);
}

.login-bg {
  padding: 120rpx 0 80rpx;
  text-align: center;
}

.logo-icon { font-size: 100rpx; display: block; }
.logo-title { display: block; font-size: 44rpx; font-weight: 700; color: #fff; margin-top: 20rpx; }
.logo-sub { display: block; font-size: 26rpx; color: rgba(255,255,255,0.85); margin-top: 12rpx; }

.login-form {
  margin: 0 40rpx;
  background: #fff;
  border-radius: 24rpx;
  padding: 48rpx 40rpx;
  box-shadow: 0 8rpx 32rpx rgba(0,0,0,0.08);
}

.input-group {
  display: flex;
  align-items: center;
  background: #f5f7fa;
  border-radius: 44rpx;
  padding: 0 32rpx;
  margin-bottom: 24rpx;
  height: 88rpx;
}

.input-icon { font-size: 32rpx; margin-right: 16rpx; }
.input { flex: 1; font-size: 28rpx; }

.login-btn { margin-top: 16rpx; }
.demo-tip { display: block; text-align: center; font-size: 22rpx; color: #c0c4cc; margin-top: 24rpx; }

.wx-btn {
  margin-top: 32rpx;
  background: #07c160;
  color: #fff;
  border: none;
  border-radius: 44rpx;
  font-size: 30rpx;
}
.wx-btn::after { border: none; }
</style>
