<template>
  <PageContainer>
    <PageHeader title="医生添加患者" subtitle="快速登记新患者并创建档案" />

    <el-row :gutter="20">
      <el-col :span="16">
        <el-form ref="formRef" :model="form" :rules="rules" label-width="100px" class="add-form">
          <section class="form-block">
            <h3 class="form-block__title">基本信息</h3>
            <el-row :gutter="16">
              <el-col :span="12">
                <el-form-item label="患者姓名" prop="name">
                  <el-input v-model="form.name" placeholder="请输入患者姓名" />
                </el-form-item>
              </el-col>
              <el-col :span="12">
                <el-form-item label="手机号" prop="phone">
                  <el-input v-model="form.phone" placeholder="请输入联系电话" maxlength="11" />
                </el-form-item>
              </el-col>
              <el-col :span="12">
                <el-form-item label="身份证号" prop="idCard">
                  <el-input
                    v-model="form.idCard"
                    placeholder="输入后自动解析性别与年龄"
                    maxlength="18"
                    @blur="onIdCardBlur"
                  >
                    <template #append>
                      <el-button @click="simulateOcr">OCR识别</el-button>
                    </template>
                  </el-input>
                </el-form-item>
              </el-col>
              <el-col :span="6">
                <el-form-item label="性别" prop="gender">
                  <el-radio-group v-model="form.gender">
                    <el-radio :value="1">男</el-radio>
                    <el-radio :value="2">女</el-radio>
                  </el-radio-group>
                </el-form-item>
              </el-col>
              <el-col :span="6">
                <el-form-item label="年龄" prop="age">
                  <el-input-number v-model="form.age" :min="0" :max="150" controls-position="right" style="width: 100%" />
                </el-form-item>
              </el-col>
            </el-row>
          </section>

          <section class="form-block">
            <h3 class="form-block__title">就诊信息</h3>
            <el-row :gutter="16">
              <el-col :span="12">
                <el-form-item label="就诊科室" prop="department">
                  <DictSelect v-model="form.department" dict-key="departments" />
                </el-form-item>
              </el-col>
              <el-col :span="24">
                <el-form-item label="住址" prop="address">
                  <el-input v-model="form.address" type="textarea" :rows="2" placeholder="请输入住址" />
                </el-form-item>
              </el-col>
              <el-col :span="24">
                <el-form-item label="主诉" prop="chiefComplaint">
                  <el-input v-model="form.chiefComplaint" type="textarea" :rows="3" placeholder="请输入患者主诉" />
                </el-form-item>
              </el-col>
            </el-row>
          </section>

          <div class="form-actions">
            <el-button type="primary" size="large" :loading="submitting" @click="handleSubmit">提交登记</el-button>
            <el-button size="large" @click="handleReset">重置</el-button>
          </div>
        </el-form>
      </el-col>

      <el-col :span="8">
        <div class="add-side-card">
          <el-icon :size="32" color="#409eff"><InfoFilled /></el-icon>
          <h4>智能录入提示</h4>
          <ul>
            <li>输入18位身份证号可自动解析性别与年龄</li>
            <li>点击 OCR 识别可模拟证件拍照录入（演示）</li>
            <li>登记成功后可一键挂号或加入候诊队列</li>
          </ul>
        </div>
      </el-col>
    </el-row>

    <el-dialog v-model="successVisible" title="登记成功" width="420px" :close-on-click-modal="false">
      <p class="success-msg">
        患者 <strong>{{ createdPatient?.name }}</strong>（{{ createdPatient?.patientNo }}）档案已创建。
      </p>
      <p class="success-hint">是否立即为其办理后续业务？</p>
      <template #footer>
        <el-button @click="onSuccessClose">稍后处理</el-button>
        <el-button type="warning" @click="onJoinQueue">加入候诊队列</el-button>
        <el-button type="primary" @click="onRegister">立即挂号</el-button>
      </template>
    </el-dialog>
  </PageContainer>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { patientApi } from '@/api'
import { registerApi } from '@/api'
import { parseIdCard } from '@/utils/idCard'

const router = useRouter()
const formRef = ref()
const submitting = ref(false)
const successVisible = ref(false)
const createdPatient = ref(null)

const form = reactive({
  name: '',
  gender: 1,
  age: 30,
  phone: '',
  idCard: '',
  department: '',
  address: '',
  chiefComplaint: ''
})

const rules = {
  name: [{ required: true, message: '请输入患者姓名', trigger: 'blur' }],
  phone: [{ required: true, message: '请输入手机号', trigger: 'blur' }],
  department: [{ required: true, message: '请选择科室', trigger: 'change' }]
}

function onIdCardBlur() {
  const parsed = parseIdCard(form.idCard)
  if (parsed) {
    form.gender = parsed.gender
    form.age = parsed.age
    ElMessage.success('已从身份证解析性别与年龄')
  }
}

function simulateOcr() {
  form.idCard = '110101199003071234'
  form.name = form.name || '王小明'
  onIdCardBlur()
  ElMessage.success('OCR 识别完成（演示数据）')
}

async function handleSubmit() {
  await formRef.value.validate()
  submitting.value = true
  try {
    const res = await patientApi.create(form)
    createdPatient.value = res.data
    successVisible.value = true
  } finally {
    submitting.value = false
  }
}

function handleReset() {
  formRef.value.resetFields()
  createdPatient.value = null
}

function onSuccessClose() {
  successVisible.value = false
  handleReset()
}

async function onJoinQueue() {
  if (createdPatient.value?.id) {
    await patientApi.joinQueue(createdPatient.value.id)
    ElMessage.success('已加入候诊队列')
    successVisible.value = false
    router.push('/patient/consultation')
  }
}

async function onRegister() {
  if (createdPatient.value) {
    try {
      await registerApi.create({
        patientName: createdPatient.value.name,
        department: createdPatient.value.department,
        registerType: '普通号'
      })
      ElMessage.success('挂号成功')
    } catch {
      ElMessage.info('已跳转至挂号管理')
    }
    successVisible.value = false
    router.push('/business/register')
  }
}
</script>

<style scoped>
.add-form { max-width: 100%; }
.form-block {
  padding: 24px;
  margin-bottom: 16px;
  border-radius: 14px;
  background: #fff;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.05);
  border: 1px solid var(--feishu-border-light);
}
.form-block__title {
  font-size: 15px;
  font-weight: 600;
  margin-bottom: 20px;
  padding-left: 10px;
  border-left: 3px solid var(--feishu-primary);
}
.form-block :deep(.el-input__wrapper),
.form-block :deep(.el-textarea__inner) {
  border-radius: 8px;
  transition: box-shadow 0.2s;
}
.form-block :deep(.el-input__wrapper:focus-within) {
  box-shadow: 0 0 0 2px rgba(64, 158, 255, 0.15);
}
.form-actions {
  display: flex;
  gap: 12px;
  padding: 8px 0 24px;
}
.add-side-card {
  padding: 24px;
  border-radius: 14px;
  background: linear-gradient(180deg, #ecf5ff 0%, #fff 100%);
  border: 1px solid rgba(64, 158, 255, 0.15);
  box-shadow: 0 2px 12px rgba(64, 158, 255, 0.08);
}
.add-side-card h4 {
  margin: 12px 0;
  font-size: 15px;
}
.add-side-card ul {
  padding-left: 18px;
  font-size: 13px;
  color: var(--feishu-text-secondary);
  line-height: 1.8;
}
.success-msg { font-size: 15px; margin-bottom: 8px; }
.success-hint { font-size: 13px; color: var(--feishu-text-tertiary); }
</style>
