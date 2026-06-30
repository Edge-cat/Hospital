<template>
  <view class="login-page">
    <PageNav title="登录" fixed show-back />

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
      <view v-if="errorMsg" class="error-msg">{{ errorMsg }}</view>
      <button
        class="btn-primary login-btn"
        :loading="loading"
        :disabled="loading"
        hover-class="btn-hover"
        @tap="handleLogin"
      >登 录</button>
      <text class="demo-tip">演示账号：13800138000 / 123456</text>
      <button
        class="wx-btn"
        :loading="wxLoading"
        :disabled="wxLoading"
        hover-class="btn-hover"
        @tap="wxLogin"
      >
        微信快捷登录
      </button>
      <text class="env-tip">联调需启动后端 :8080，并勾选「不校验合法域名」</text>
    </view>
  </view>
</template>

<script setup>
import { reactive, ref } from 'vue'
import { mpApi } from '@/api'
import PageNav from '@/components/PageNav.vue'
import { navigateAfterAuth } from '@/utils/nav'

const loading = ref(false)
const wxLoading = ref(false)
const errorMsg = ref('')
const form = reactive({ username: '13800138000', password: '123456' })

function pickErrorMsg(e) {
  return e?.message || e?.data?.message || '操作失败，请稍后重试'
}

function saveSession(data) {
  if (!data?.token) {
    errorMsg.value = '登录失败：未返回凭证，请确认后端已启动'
    return false
  }
  errorMsg.value = ''
  uni.setStorageSync('his_token', data.token)
  uni.setStorageSync('his_user', JSON.stringify(data.user || {}))
  return true
}

async function handleLogin() {
  if (!form.username || !form.password) {
    errorMsg.value = '请输入账号和密码'
    return
  }
  loading.value = true
  errorMsg.value = ''
  try {
    const res = await mpApi.login(form)
    if (saveSession(res.data)) navigateAfterAuth('登录成功')
  } catch (e) {
    errorMsg.value = pickErrorMsg(e)
  } finally {
    loading.value = false
  }
}

async function wxLogin() {
  wxLoading.value = true
  errorMsg.value = ''
  try {
    const loginRes = await new Promise((resolve, reject) => {
      uni.login({ provider: 'weixin', success: resolve, fail: reject })
    })
    const res = await mpApi.wxLogin({ code: loginRes.code || 'demo' })
    if (saveSession(res.data)) navigateAfterAuth('微信登录成功')
  } catch (e) {
    errorMsg.value = pickErrorMsg(e)
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
  padding: 80rpx 0 60rpx;
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

.error-msg {
  color: #f54a45;
  font-size: 24rpx;
  text-align: center;
  margin-bottom: 16rpx;
  line-height: 1.5;
}

.login-btn { margin-top: 8rpx; }
.demo-tip { display: block; text-align: center; font-size: 22rpx; color: #c0c4cc; margin-top: 24rpx; }

.env-tip {
  display: block;
  text-align: center;
  font-size: 20rpx;
  color: #c0c4cc;
  margin-top: 16rpx;
  line-height: 1.5;
}

.wx-btn {
  margin-top: 32rpx;
  background: #07c160;
  color: #fff;
  border: none;
  border-radius: 44rpx;
  font-size: 30rpx;
}
.wx-btn::after { border: none; }

.btn-hover { opacity: 0.85; }
</style>
