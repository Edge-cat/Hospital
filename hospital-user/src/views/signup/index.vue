<template>
  <div class="signup-page">
    <div class="signup-left">
      <div class="brand-block">
        <div class="brand-logo">
          <el-icon :size="36" color="#3370ff"><FirstAidKit /></el-icon>
        </div>
        <h1>东软云医院</h1>
        <p>用户端 · 在线挂号预约缴费</p>
      </div>
    </div>
    <div class="signup-right">
      <div class="signup-card">
        <h2>患者注册</h2>
        <p class="subtitle">注册后可在线挂号、预约与缴费</p>
        <el-form ref="formRef" :model="form" :rules="rules" size="large" label-position="top" @submit.prevent="handleSignup">
          <el-form-item label="手机号" prop="phone">
            <el-input v-model="form.phone" placeholder="11位手机号，将作为登录账号" maxlength="11" />
          </el-form-item>
          <el-form-item label="姓名" prop="name">
            <el-input v-model="form.name" placeholder="请输入真实姓名" />
          </el-form-item>
          <el-form-item label="性别" prop="gender">
            <el-radio-group v-model="form.gender">
              <el-radio :value="1">男</el-radio>
              <el-radio :value="2">女</el-radio>
            </el-radio-group>
          </el-form-item>
          <el-form-item label="身份证号" prop="idCard">
            <el-input v-model="form.idCard" placeholder="选填，便于就诊建档" maxlength="18" />
          </el-form-item>
          <el-form-item label="登录密码" prop="password">
            <el-input v-model="form.password" type="password" placeholder="至少6位" show-password />
          </el-form-item>
          <el-form-item label="确认密码" prop="confirmPassword">
            <el-input v-model="form.confirmPassword" type="password" placeholder="再次输入密码" show-password @keyup.enter="handleSignup" />
          </el-form-item>
          <el-button type="primary" :loading="loading" class="signup-btn" @click="handleSignup">注 册</el-button>
        </el-form>
        <p class="login-link">
          已有账号？
          <router-link to="/login">返回登录</router-link>
        </p>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { useUserStore } from '@/stores/user'
import { userApi } from '@/api'

const router = useRouter()
const userStore = useUserStore()
const formRef = ref()
const loading = ref(false)
const form = reactive({
  phone: '',
  name: '',
  gender: 1,
  idCard: '',
  password: '',
  confirmPassword: ''
})

const validateConfirm = (_rule, value, callback) => {
  if (value !== form.password) callback(new Error('两次输入的密码不一致'))
  else callback()
}

const rules = {
  phone: [
    { required: true, message: '请输入手机号', trigger: 'blur' },
    { pattern: /^1\d{10}$/, message: '请输入正确的11位手机号', trigger: 'blur' }
  ],
  name: [
    { required: true, message: '请输入姓名', trigger: 'blur' },
    { min: 2, message: '姓名至少2个字', trigger: 'blur' }
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 6, message: '密码至少6位', trigger: 'blur' }
  ],
  confirmPassword: [
    { required: true, message: '请确认密码', trigger: 'blur' },
    { validator: validateConfirm, trigger: 'blur' }
  ]
}

async function handleSignup() {
  await formRef.value.validate()
  loading.value = true
  try {
    const res = await userApi.signup({
      phone: form.phone,
      name: form.name,
      gender: form.gender,
      idCard: form.idCard || undefined,
      password: form.password
    })
    userStore.login(res.data)
    ElMessage.success(`注册成功，欢迎 ${res.data.user?.name || ''}`)
    router.push('/home')
  } catch {
    // 错误提示由 request 拦截器统一处理
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.signup-page { min-height: 100vh; display: flex; background: var(--feishu-bg-base); }
.signup-left {
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
.signup-right {
  width: 480px; display: flex; align-items: center; justify-content: center;
  background: #fff; border-left: 1px solid var(--feishu-border);
  overflow-y: auto;
}
.signup-card { width: 360px; padding: 24px 0; }
.signup-card h2 { font-size: 24px; font-weight: 600; color: var(--feishu-text-primary); margin-bottom: 8px; }
.subtitle { color: var(--feishu-text-tertiary); font-size: 14px; margin-bottom: 24px; }
.signup-btn { width: 100%; margin-top: 8px; height: 40px; }
.login-link {
  margin-top: 20px;
  text-align: center;
  font-size: 14px;
  color: var(--feishu-text-tertiary);
}
.login-link a {
  color: var(--feishu-primary);
  text-decoration: none;
}
.login-link a:hover { text-decoration: underline; }
</style>
