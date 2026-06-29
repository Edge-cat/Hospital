<template>
  <div class="login-page">
    <div class="login-left">
      <div class="brand-block">
        <div class="brand-logo">
          <el-icon :size="36" class="brand-logo__icon"><FirstAidKit /></el-icon>
        </div>
        <h1>东软云医院</h1>
        <p>HIS 管理端 · 智慧医疗后台</p>
      </div>
    </div>
    <div class="login-right">
      <div class="login-card">
        <h2>欢迎登录</h2>
        <p class="subtitle">医院内部管理系统 · 按角色动态加载菜单</p>
        <el-form ref="formRef" :model="form" :rules="rules" size="large" @submit.prevent="handleLogin">
          <el-form-item prop="username">
            <el-input v-model="form.username" placeholder="用户名" prefix-icon="User" />
          </el-form-item>
          <el-form-item prop="password">
            <el-input v-model="form.password" type="password" placeholder="密码" prefix-icon="Lock" show-password @keyup.enter="handleLogin" />
          </el-form-item>
          <el-button type="primary" :loading="loading" class="login-btn" @click="handleLogin">登 录</el-button>
        </el-form>
        <div class="demo-accounts">
          <p class="demo-title">演示账号（密码均为 123456）</p>
          <div class="demo-list">
            <button
              v-for="item in demoAccounts"
              :key="item.username"
              type="button"
              class="demo-item"
              @click="fillAccount(item)"
            >
              <span class="demo-role">{{ item.roleLabel }}</span>
              <span class="demo-user">{{ item.username }}</span>
            </button>
          </div>
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
import { usePermissionStore } from '@/stores/permission'
import { useDictStore } from '@/stores/dict'
import { authApi } from '@/api'
import { reportOperation } from '@/utils/operationReport'

const router = useRouter()
const route = useRoute()
const userStore = useUserStore()
const permissionStore = usePermissionStore()
const dictStore = useDictStore()
const formRef = ref()
const loading = ref(false)
const form = reactive({ username: 'admin', password: '123456' })
const rules = {
  username: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
  password: [{ required: true, message: '请输入密码', trigger: 'blur' }]
}

const demoAccounts = [
  { username: 'admin', roleLabel: '管理员', desc: '全部功能' },
  { username: 'doctor', roleLabel: '医生', desc: '患者诊疗、排班、记录' },
  { username: 'nurse', roleLabel: '护士', desc: '挂号预约、床位、患者信息' }
]

function fillAccount(item) {
  form.username = item.username
  form.password = '123456'
}

async function handleLogin() {
  await formRef.value.validate()
  loading.value = true
  try {
    permissionStore.resetRoutes()
    dictStore.reset()
    const res = await authApi.login(form)
    userStore.login(res.data)
    reportOperation({ module: '系统', action: '登录', path: '/login' })
    ElMessage.success(`欢迎，${res.data.user.roleLabel || res.data.user.name}`)
    router.push(route.query.redirect || '/dashboard')
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.login-page {
  height: 100vh;
  display: flex;
  background: var(--feishu-bg-base);
}

.login-left {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(160deg, var(--feishu-primary-bg) 0%, var(--feishu-primary-light) 50%, var(--feishu-bg-base) 100%);
}

.brand-block { text-align: center; padding: 40px; }

.brand-logo {
  width: 72px;
  height: 72px;
  background: var(--feishu-bg-white);
  border-radius: var(--feishu-radius-lg);
  display: flex;
  align-items: center;
  justify-content: center;
  margin: 0 auto 24px;
  box-shadow: var(--feishu-shadow-md);
}

.brand-logo__icon { color: var(--feishu-primary); }

.brand-block h1 {
  font-size: 28px;
  color: var(--feishu-text-primary);
  font-weight: 600;
  margin-bottom: 8px;
}

.brand-block p { color: var(--feishu-text-secondary); font-size: 15px; }

.login-right {
  width: 480px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: var(--feishu-bg-white);
  border-left: 1px solid var(--feishu-border);
}

.login-card { width: 360px; }

.login-card h2 {
  font-size: 24px;
  font-weight: 600;
  color: var(--feishu-text-primary);
  margin-bottom: 8px;
}

.subtitle { color: var(--feishu-text-tertiary); font-size: 14px; margin-bottom: 32px; }

.login-btn { width: 100%; margin-top: 8px; height: 40px; }

.demo-accounts { margin-top: 28px; }

.demo-title {
  text-align: center;
  color: var(--el-text-color-placeholder);
  font-size: 12px;
  margin-bottom: 12px;
}

.demo-list { display: flex; flex-direction: column; gap: 8px; }

.demo-item {
  display: flex;
  align-items: center;
  justify-content: space-between;
  width: 100%;
  padding: 10px 14px;
  border: 1px solid var(--feishu-border-light);
  border-radius: var(--feishu-radius-md);
  background: var(--feishu-bg-sub);
  cursor: pointer;
  transition: border-color 0.2s, background 0.2s;
}

.demo-item:hover {
  border-color: var(--feishu-primary);
  background: var(--feishu-primary-bg);
}

.demo-role {
  font-size: 13px;
  font-weight: 500;
  color: var(--feishu-primary);
}

.demo-user {
  font-size: 12px;
  color: var(--feishu-text-tertiary);
  font-family: monospace;
}
</style>
