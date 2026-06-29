<template>
  <FormCard title="在线预约" subtitle="提前预约医生，减少排队等待">
    <el-form ref="formRef" :model="form" :rules="rules" label-width="100px" style="max-width: 640px">
      <el-form-item label="科室" prop="department">
        <el-select v-model="form.department" placeholder="请选择科室" style="width: 100%" @change="onDepartmentChange">
          <el-option v-for="d in departments" :key="d.id" :label="d.name" :value="d.name" />
        </el-select>
      </el-form-item>
      <el-form-item label="医生" prop="doctorName">
        <el-select
          v-model="form.doctorName"
          placeholder="请先选择科室"
          style="width: 100%"
          :disabled="!form.department"
        >
          <el-option v-for="doc in doctors" :key="doc.id" :label="`${doc.name} (${doc.title})`" :value="doc.name" />
        </el-select>
      </el-form-item>

      <el-form-item v-if="form.doctorName" label="预约日期" prop="appointmentDate">
        <div v-loading="loadingSchedule" class="schedule-block">
          <el-alert
            v-if="scheduleError && !loadingSchedule"
            type="warning"
            :title="scheduleError"
            show-icon
            :closable="false"
            class="schedule-alert"
          >
            <el-button link type="primary" @click="loadSchedule">重试</el-button>
          </el-alert>
          <el-empty v-else-if="!loadingSchedule && !selectableDates.length" description="该医生暂无可预约日期" :image-size="56" />
          <div v-else class="date-chips">
            <button
              v-for="day in selectableDates"
              :key="day.date"
              type="button"
              class="date-chip"
              :class="{ active: form.appointmentDate === day.date, today: day.isToday }"
              @click="selectDate(day.date)"
            >
              <span class="date-chip__label">{{ day.label || day.weekday }}</span>
              <span class="date-chip__date">{{ day.shortDate || day.date.slice(5) }}</span>
            </button>
          </div>
        </div>
      </el-form-item>

      <el-form-item v-if="form.appointmentDate" label="时段" prop="timeSlot">
        <p class="slot-hint">时段以系统标准排班为准，灰色表示已满</p>
        <div class="slot-grid">
          <button
            v-for="slot in timeSlotOptions"
            :key="slot.timeSlot"
            type="button"
            class="slot-btn"
            :class="{ active: form.timeSlot === slot.timeSlot, disabled: !slot.available }"
            :disabled="!slot.available"
            @click="selectSlot(slot.timeSlot)"
          >
            <span class="slot-btn__time">{{ slot.timeSlot }}</span>
            <span class="slot-btn__remain">{{ slot.available ? `余${slot.remaining}` : '已满' }}</span>
          </button>
        </div>
        <p v-if="!availableSlotCount" class="slot-empty">当日标准时段均已约满，请换一天</p>
      </el-form-item>

      <el-form-item v-if="scheduleSummary" label="已选">
        <el-tag type="primary" effect="plain">{{ scheduleSummary }}</el-tag>
      </el-form-item>

      <el-form-item label="患者姓名" prop="patientName">
        <el-input v-model="form.patientName" placeholder="请输入就诊人姓名" />
        <p v-if="profile?.name" class="name-hint">档案姓名：{{ profile.name }}（已自动填充）</p>
      </el-form-item>
      <el-form-item>
        <el-button type="primary" :loading="submitting" @click="handleSubmit">提交预约</el-button>
        <el-button @click="$router.push('/my-appointment')">查看我的预约</el-button>
      </el-form-item>
    </el-form>
  </FormCard>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'
import { userApi } from '@/api'
import { useUserStore } from '@/stores/user'
import { useDepartmentForm } from '@/composables/useDepartmentForm'
import { useAppointmentSchedule } from '@/composables/useAppointmentSchedule'
import { usePatientProfile } from '@/composables/usePatientProfile'

const router = useRouter()
const route = useRoute()
const userStore = useUserStore()
const formRef = ref()
const submitting = ref(false)
const form = reactive({
  department: '',
  doctorName: '',
  appointmentDate: '',
  timeSlot: '',
  patientName: userStore.userName === '游客' ? '' : userStore.userName
})

const rules = {
  department: [{ required: true, message: '请选择科室', trigger: 'change' }],
  doctorName: [{ required: true, message: '请选择医生', trigger: 'change' }],
  appointmentDate: [{ required: true, message: '请选择日期', trigger: 'change' }],
  timeSlot: [{ required: true, message: '请选择时段', trigger: 'change' }],
  patientName: [{ required: true, message: '请输入姓名', trigger: 'blur' }]
}

const { departments, doctors, loadDoctors, loadDepartments, initFromQuery } = useDepartmentForm(form, { autoLoad: false })
const {
  selectableDates,
  timeSlotOptions,
  availableSlotCount,
  scheduleSummary,
  loadingSchedule,
  scheduleError,
  loadSchedule,
  selectDate,
  selectSlot,
  resetSchedule
} = useAppointmentSchedule(form, doctors)
const { profile, load: loadProfile } = usePatientProfile()

function onDepartmentChange() {
  resetSchedule()
  loadDoctors()
}

onMounted(async () => {
  await Promise.all([loadDepartments(), loadProfile({ silent: true })])
  if (profile.value?.name) form.patientName = profile.value.name
  if (route.query.department) {
    await initFromQuery(String(route.query.department), route.query.doctor ? String(route.query.doctor) : '')
  }
})

async function handleSubmit() {
  await formRef.value.validate()
  submitting.value = true
  try {
    await userApi.appointment(form)
    ElMessage.success('预约成功')
    router.push('/my-appointment')
  } finally {
    submitting.value = false
  }
}
</script>

<style scoped>
.schedule-block {
  width: 100%;
  min-height: 48px;
}

.schedule-alert {
  margin-bottom: 0;
}

.date-chips {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

.date-chip {
  display: flex;
  flex-direction: column;
  align-items: center;
  min-width: 72px;
  padding: 8px 12px;
  border: 1px solid var(--feishu-border, #dee0e3);
  border-radius: 8px;
  background: #fff;
  cursor: pointer;
  transition: border-color 0.2s, background 0.2s;
}

.date-chip:hover {
  border-color: var(--feishu-primary, #3370ff);
}

.date-chip.active {
  border-color: var(--feishu-primary, #3370ff);
  background: #e8f3ff;
}

.date-chip.today .date-chip__label {
  color: var(--feishu-primary, #3370ff);
  font-weight: 600;
}

.date-chip__label {
  font-size: 13px;
  color: #646a73;
}

.date-chip__date {
  font-size: 14px;
  font-weight: 500;
  color: #1f2329;
  margin-top: 2px;
}

.slot-hint,
.name-hint,
.slot-empty {
  margin: 0 0 8px;
  font-size: 12px;
  color: #8f959e;
}

.slot-empty {
  margin-top: 8px;
  margin-bottom: 0;
  color: #f53f3f;
}

.slot-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(140px, 1fr));
  gap: 8px;
  width: 100%;
}

.slot-btn {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 10px 8px;
  border: 1px solid var(--feishu-border, #dee0e3);
  border-radius: 8px;
  background: #fff;
  cursor: pointer;
  transition: border-color 0.2s, background 0.2s;
}

.slot-btn:hover:not(.disabled) {
  border-color: var(--feishu-primary, #3370ff);
}

.slot-btn.active {
  border-color: var(--feishu-primary, #3370ff);
  background: #e8f3ff;
}

.slot-btn.disabled {
  opacity: 0.45;
  cursor: not-allowed;
  background: #f5f6f7;
}

.slot-btn__time {
  font-size: 14px;
  font-weight: 500;
  color: #1f2329;
}

.slot-btn__remain {
  font-size: 12px;
  color: #8f959e;
  margin-top: 4px;
}
</style>
