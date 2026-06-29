<template>
  <div class="login-page">
    <div class="login-left">
      <div class="brand-block">
        <div class="brand-logo">
          <el-icon :size="36" color="#3370ff"><FirstAidKit /></el-icon>
        </div>
        <h1>东软云医院</h1>
        <p>用户端 · 在线挂号预约缴费</p>
      </div>
    </div>
    <div class="login-right">
      <div class="login-card">
        <h2>欢迎登录</h2>
        <p class="subtitle">患者在线服务平台</p>
        <el-form ref="formRef" :model="form" :rules="rules" size="large" @submit.prevent="handleLogin">
          <el-form-item prop="username">
            <el-input v-model="form.username" placeholder="手机号 / 用户名" prefix-icon="User" />
          </el-form-item>
          <el-form-item prop="password">
            <el-input v-model="form.password" type="password" placeholder="密码" prefix-icon="Lock" show-password @keyup.enter="handleLogin" />
          </el-form-item>
          <el-button type="primary" :loading="loading" class="login-btn" @click="handleLogin">登 录</el-button>
        </el-form>
        <div class="demo-box">
          <p class="demo-title">演示账号（密码 123456）</p>
          <button type="button" class="demo-btn" @click="fillDemo">patient · 患者账号</button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'
import { useUserStore } from '@/stores/user'
import { userApi } from '@/api'

const router = useRouter()
const route = useRoute()
const userStore = useUserStore()
const formRef = ref()
const loading = ref(false)
const form = reactive({ username: 'patient', password: '123456' })
const rules = {
  username: [{ required: true, message: '请输入账号', trigger: 'blur' }],
  password: [{ required: true, message: '请输入密码', trigger: 'blur' }]
}

function fillDemo() {
  form.username = 'patient'
  form.password = '123456'
}

async function handleLogin() {
  await formRef.value.validate()
  loading.value = true
  try {
    const res = await userApi.login(form)
    userStore.login(res.data)
    ElMessage.success(`欢迎，${res.data.user?.name || '用户'}`)
    router.push(route.query.redirect || '/home')
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.login-page { min-height: 100vh; display: flex; background: var(--feishu-bg-base); }
.login-left {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(160deg, var(--feishu-primary-bg) 0%, #e8eeff 50%, var(--feishu-bg-base) 100%);
}
.brand-block { text-align: center; padding: 40px; }
.brand-logo {
  width: 72px; height: 72px; background: #fff; border-radius: 16px;
  display: flex; align-items: center; justify-content: center;
  margin: 0 auto 24px; box-shadow: 0 4px 16px rgba(51, 112, 255, 0.12);
}
.brand-block h1 { font-size: 28px; color: var(--feishu-text-primary); font-weight: 600; margin-bottom: 8px; }
.brand-block p { color: var(--feishu-text-secondary); font-size: 15px; }
.login-right {
  width: 480px; display: flex; align-items: center; justify-content: center;
  background: #fff; border-left: 1px solid var(--feishu-border);
}
.login-card { width: 360px; }
.login-card h2 { font-size: 24px; font-weight: 600; color: var(--feishu-text-primary); margin-bottom: 8px; }
.subtitle { color: var(--feishu-text-tertiary); font-size: 14px; margin-bottom: 32px; }
.login-btn { width: 100%; margin-top: 8px; height: 40px; }
.demo-box { margin-top: 28px; }
.demo-title { text-align: center; color: #bbbfc4; font-size: 12px; margin-bottom: 10px; }
.demo-btn {
  width: 100%; padding: 10px; border: 1px solid var(--feishu-border-light);
  border-radius: 8px; background: var(--feishu-bg-sub); cursor: pointer;
  font-size: 13px; color: var(--feishu-primary); transition: all 0.2s;
}
.demo-btn:hover { border-color: var(--feishu-primary); background: var(--feishu-primary-bg); }
</style>
