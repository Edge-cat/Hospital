<template>

  <FormCard title="在线挂号" subtitle="选择科室与医生，快速完成挂号">

    <el-alert
      v-if="isWaitlist"
      type="warning"
      :closable="false"
      show-icon
      title="当前医生号源已满，您将进入候补队列；有退号时将按顺序通知。"
      style="margin-bottom: 16px; max-width: 560px"
    />

    <el-form ref="formRef" :model="form" :rules="rules" label-width="100px" style="max-width: 560px" v-loading="loadingDepts">

      <el-form-item label="科室" prop="department">

        <el-select v-model="form.department" placeholder="请选择科室" style="width: 100%" @change="loadDoctors">

          <el-option v-for="d in departments" :key="d.id" :label="d.name" :value="d.name" />

        </el-select>

      </el-form-item>

      <el-form-item label="医生" prop="doctorName">

        <el-select

          v-model="form.doctorName"

          placeholder="请先选择科室"

          style="width: 100%"

          :disabled="!form.department"

          :loading="loadingDoctors"

        >

          <el-option v-for="doc in doctors" :key="doc.id" :label="`${doc.name} (${doc.title})`" :value="doc.name" />

        </el-select>

      </el-form-item>

      <el-form-item label="号别" prop="registerType">

        <el-radio-group v-model="form.registerType">

          <el-radio v-for="t in registerTypes" :key="t.value" :value="t.value">

            {{ t.label }} ¥{{ t.fee }}

          </el-radio>

        </el-radio-group>

      </el-form-item>

      <el-form-item label="挂号费用">

        <span class="fee-display">¥{{ displayFee }}</span>

      </el-form-item>

      <el-form-item label="患者姓名" prop="patientName">

        <el-input v-model="form.patientName" placeholder="请输入就诊人姓名" maxlength="20" show-word-limit />

      </el-form-item>

      <el-form-item>

        <el-button type="primary" :loading="submitting" :disabled="submitLocked" @click="handleSubmit">

          确认挂号

        </el-button>

        <el-button @click="$router.push('/my-register')">查看我的挂号</el-button>

      </el-form-item>

    </el-form>

  </FormCard>

</template>



<script setup>

import { ref, onMounted, computed } from 'vue'

import { useRouter, useRoute } from 'vue-router'

import { ElMessage } from 'element-plus'

import { useUserStore } from '@/stores/user'

import { useHospitalRegister } from '@/composables/useHospitalView'



const router = useRouter()

const route = useRoute()

const userStore = useUserStore()

const formRef = ref()

const isWaitlist = computed(() => route.query.waitlist === '1')

const displayFee = computed(() => {
  const fee = Number(currentFee.value)
  return Number.isFinite(fee) ? fee.toFixed(2) : '0.00'
})



const {

  form,

  departments,

  doctors,

  registerTypes,

  currentFee,

  loadingDepts,

  loadingDoctors,

  submitting,

  submitLocked,

  loadDepartments,

  loadDoctors,

  loadRegisterTypes,

  submitRegister

} = useHospitalRegister(userStore.userName === '游客' ? '' : userStore.userName)



const rules = {

  department: [{ required: true, message: '请选择科室', trigger: 'change' }],

  doctorName: [{ required: true, message: '请选择医生', trigger: 'change' }],

  patientName: [{ required: true, message: '请输入姓名', trigger: 'blur' }]

}



onMounted(async () => {
  await Promise.all([loadDepartments(), loadRegisterTypes()])

  if (route.query.department) {

    form.value.department = String(route.query.department)

    await loadDoctors()

    if (route.query.doctor) form.value.doctorName = String(route.query.doctor)

    if (route.query.registerType) form.value.registerType = String(route.query.registerType)

  }

})



async function handleSubmit() {

  try {

    await formRef.value.validate()

  } catch {

    return

  }

  const ok = await submitRegister()

  if (ok) {

    ElMessage.success('挂号成功')

    router.push('/my-register')

  }

}

</script>



<style scoped>

.fee-display {

  font-size: 20px;

  font-weight: 700;

  color: var(--feishu-danger);

  font-variant-numeric: tabular-nums;

}

</style>


