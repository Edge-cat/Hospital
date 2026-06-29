<template>
  <view class="page">
    <FlowBar :steps="flowSteps" :current="currentFlow" />

    <view class="container">
      <DoctorSelect
        :departments="departments"
        :doctors="doctors"
        :loading="loadingDoctors"
        :selected-dept="form.department"
        :selected-doctor-id="form.doctorId"
        @select-dept="selectDept"
        @select-doctor="selectDoctor"
      />

      <ScheduleTimeline
        v-if="form.doctorId"
        :dates="scheduleDates"
        :loading="loadingSchedule"
        :selected-date="form.visitDate"
        :selected-slot="form.timeSlot"
        :visit-type-label="visitTypeLabel"
        :visit-type-class="isRegisterMode ? 'register' : 'appointment'"
        @select-date="selectDate"
        @select-slot="selectSlot"
      />

      <view v-if="form.timeSlot" class="card form-card">
        <view class="form-card-title">就诊信息</view>
        <view class="compact-form">
          <view v-if="isRegisterMode" class="compact-row">
            <text class="compact-label">号别</text>
            <view class="type-chips">
              <text
                v-for="t in registerTypes"
                :key="t"
                class="type-chip"
                :class="{ active: form.registerType === t }"
                @click="form.registerType = t"
              >{{ t }}</text>
            </view>
          </view>
          <view class="compact-row">
            <text class="compact-label">姓名</text>
            <text class="compact-value">{{ form.patientName }}</text>
          </view>
          <view class="compact-row">
            <text class="compact-label">手机</text>
            <text class="compact-value">{{ form.patientPhone }}</text>
          </view>
          <view class="compact-row pay-row">
            <text class="compact-label">缴费</text>
            <text class="pay-hint" :class="isRegisterMode ? 'now' : 'later'">
              {{ isRegisterMode ? `¥${fee} · 立即支付` : '到院支付' }}
            </text>
          </view>
        </view>
        <text class="form-hint">信息已自动填充，如需修改请前往「我的 - 个人信息」</text>
      </view>

      <button
        class="btn-primary submit-btn"
        :class="{ disabled: !canSubmit }"
        :loading="submitting"
        @click="openPreview"
      >下一步 · 确认{{ isRegisterMode ? '挂号' : '预约' }}</button>
    </view>

    <view v-if="showPreview" class="preview-mask" @click="showPreview = false">
      <view class="preview-sheet" @click.stop>
        <view class="preview-handle" />
        <text class="preview-title">{{ isRegisterMode ? '挂号详情预览' : '预约信息确认' }}</text>
        <text class="preview-sub">
          {{ isRegisterMode ? '当日挂号需立即支付以锁定号源' : '预约成功后可到院缴费就诊' }}
        </text>

        <view class="preview-list">
          <view class="preview-item">
            <text class="preview-label">业务类型</text>
            <text class="preview-value">{{ visitTypeLabel }}</text>
          </view>
          <view class="preview-item">
            <text class="preview-label">就诊科室</text>
            <text class="preview-value">{{ form.department }}</text>
          </view>
          <view class="preview-item">
            <text class="preview-label">接诊医生</text>
            <text class="preview-value">{{ selectedDoctor?.name }} · {{ selectedDoctor?.title }}</text>
          </view>
          <view class="preview-item highlight">
            <text class="preview-label">就诊时间</text>
            <text class="preview-value time">{{ form.visitDate }} {{ form.timeSlot }}</text>
          </view>
          <view v-if="isRegisterMode" class="preview-item">
            <text class="preview-label">号别</text>
            <text class="preview-value">{{ form.registerType }}</text>
          </view>
          <view class="preview-item">
            <text class="preview-label">就诊人</text>
            <text class="preview-value">{{ form.patientName }}</text>
          </view>
          <view v-if="isRegisterMode" class="preview-item highlight">
            <text class="preview-label">挂号费用</text>
            <text class="preview-fee">¥{{ fee }}</text>
          </view>
          <view v-else class="preview-item">
            <text class="preview-label">缴费方式</text>
            <text class="preview-value">到院支付</text>
          </view>
        </view>

        <view class="preview-actions">
          <button class="preview-btn cancel" @click="showPreview = false">返回修改</button>
          <button class="preview-btn confirm" :loading="submitting" @click="confirmSubmit">
            {{ isRegisterMode ? '确认并去缴费' : '确认预约' }}
          </button>
        </view>
      </view>
    </view>

    <view v-if="showFeedback" class="feedback-mask">
      <view class="feedback-card">
        <view class="feedback-icon">✓</view>
        <text class="feedback-title">{{ isRegisterMode ? '挂号成功' : '预约成功' }}</text>
        <text class="feedback-desc">
          {{ isRegisterMode ? '请尽快完成缴费，以免号源释放' : '请按时到院就诊，可在「我的预约」查看详情' }}
        </text>
        <view class="feedback-detail">
          <view v-if="!isRegisterMode && resultNo" class="feedback-row">
            <text class="feedback-label">预约单号</text>
            <text class="feedback-no">{{ resultNo }}</text>
          </view>
          <view class="feedback-row">
            <text class="feedback-label">就诊时间</text>
            <text>{{ form.visitDate }} {{ form.timeSlot }}</text>
          </view>
          <view class="feedback-row">
            <text class="feedback-label">接诊医生</text>
            <text>{{ selectedDoctor?.name }} · {{ form.department }}</text>
          </view>
          <view v-if="isRegisterMode" class="feedback-row">
            <text class="feedback-label">待缴费用</text>
            <text class="feedback-fee">¥{{ fee }}</text>
          </view>
        </view>
        <button v-if="isRegisterMode" class="btn-primary" @click="goPayment">立即缴费</button>
        <button v-else class="btn-primary" @click="goMyAppointment">查看预约状态</button>
        <text class="feedback-skip" @click="closeFeedback">
          {{ isRegisterMode ? '稍后缴费，查看挂号' : '继续预约' }}
        </text>
      </view>
    </view>

    <TabBar current="/pages/visit/visit" />
  </view>
</template>

<script setup>
import { ref, reactive, computed } from 'vue'
import { onLoad } from '@dcloudio/uni-app'
import TabBar from '@/components/TabBar.vue'
import FlowBar from '@/components/FlowBar.vue'
import DoctorSelect from '@/components/DoctorSelect.vue'
import ScheduleTimeline from '@/components/ScheduleTimeline.vue'
import { mpApi } from '@/api'
import { mapDeptList } from '@/utils/deptIcon'
import { useDepartments } from '@/composables/useDepartments'
import { checkLogin, navigateToLogin } from '@/utils/request'

const flowSteps = [
  { key: 'doctor', label: '选医生' },
  { key: 'time', label: '定时间' },
  { key: 'confirm', label: '确认支付' },
  { key: 'feedback', label: '反馈' }
]

const todayStr = new Date().toISOString().slice(0, 10)
const { departments, load: loadDepartments } = useDepartments()
const doctors = ref([])
const scheduleDates = ref([])
const loadingDoctors = ref(false)
const loadingSchedule = ref(false)
const submitting = ref(false)
const showPreview = ref(false)
const showFeedback = ref(false)
const resultNo = ref('')
const registerTypes = ref(['普通号', '专家号', '急诊号'])
const registerFeeMap = ref({ 普通号: 10, 专家号: 50, 急诊号: 20 })

const form = reactive({
  department: '',
  doctorId: null,
  doctorName: '',
  visitDate: '',
  timeSlot: '',
  registerType: '普通号',
  patientName: '',
  patientPhone: ''
})

const selectedDoctor = computed(() => doctors.value.find((d) => d.id === form.doctorId))

const isRegisterMode = computed(() => form.visitDate === todayStr)

const visitTypeLabel = computed(() => {
  if (!form.visitDate) return ''
  return isRegisterMode.value ? '当日挂号' : '预约就诊'
})

const fee = computed(() => registerFeeMap.value[form.registerType] ?? 10)

async function loadRegisterTypes() {
  try {
    const res = await mpApi.registerTypes()
    const list = res.data || []
    if (list.length) {
      registerTypes.value = list.map((t) => t.value || t.label)
      const map = {}
      list.forEach((t) => { map[t.value || t.label] = Number(t.fee) || 10 })
      registerFeeMap.value = map
    }
  } catch (_) {}
}

const currentFlow = computed(() => {
  if (showFeedback.value) return 3
  if (showPreview.value || form.timeSlot) return 2
  if (form.doctorId) return 1
  return 0
})

const canSubmit = computed(() =>
  form.department && form.doctorId && form.visitDate && form.timeSlot && form.patientName
)

onLoad(async (options) => {
  loadPatientInfo()
  await Promise.all([loadDepartments(), loadRegisterTypes()])
  if (options?.department) {
    await selectDept(decodeURIComponent(options.department))
    if (options?.doctor) {
      const name = decodeURIComponent(options.doctor)
      const doc = doctors.value.find((d) => d.name === name)
      if (doc) await selectDoctor(doc)
    }
  }
})

async function loadPatientInfo() {
  try {
    const res = await mpApi.patientInfo()
    form.patientName = res.data.name || ''
    form.patientPhone = res.data.phone || ''
  } catch {
    const user = uni.getStorageSync('his_user')
    if (user) {
      const parsed = JSON.parse(user)
      form.patientName = parsed.name || ''
      form.patientPhone = parsed.phone || ''
    }
  }
}

async function selectDept(name) {
  if (form.department === name) return
  form.department = name
  form.doctorId = null
  form.doctorName = ''
  resetSchedule()
  loadingDoctors.value = true
  doctors.value = []
  try {
    const res = await mpApi.doctors({ department: name })
    doctors.value = res.data.list
  } finally {
    loadingDoctors.value = false
  }
}

async function selectDoctor(doc) {
  form.doctorId = doc.id
  form.doctorName = doc.name
  resetSchedule()
  loadingSchedule.value = true
  try {
    const res = await mpApi.appointmentSchedule({ doctorId: doc.id })
    scheduleDates.value = res.data.dates
    const firstAvailable = scheduleDates.value.find((d) => d.slots.some((s) => s.available))
    if (firstAvailable) selectDate(firstAvailable)
  } finally {
    loadingSchedule.value = false
  }
}

function selectDate(day) {
  form.visitDate = day.date
  form.timeSlot = ''
  const firstSlot = day.slots.find((s) => s.available)
  if (firstSlot) form.timeSlot = firstSlot.timeSlot
}

function selectSlot(slot) {
  form.timeSlot = slot.timeSlot
}

function resetSchedule() {
  scheduleDates.value = []
  form.visitDate = ''
  form.timeSlot = ''
}

function openPreview() {
  if (!checkLogin()) { navigateToLogin(); return }
  if (!form.doctorId) { uni.showToast({ title: '请选择医生', icon: 'none' }); return }
  if (!form.timeSlot) { uni.showToast({ title: '请选择就诊时间', icon: 'none' }); return }
  if (!form.patientName) { uni.showToast({ title: '就诊人信息不完整', icon: 'none' }); return }
  showPreview.value = true
}

async function confirmSubmit() {
  submitting.value = true
  try {
    if (isRegisterMode.value) {
      await mpApi.register({
        department: form.department,
        doctorName: form.doctorName,
        registerType: form.registerType,
        patientName: form.patientName,
        fee: fee.value
      })
    } else {
      const res = await mpApi.appointment({
        department: form.department,
        doctorName: form.doctorName,
        appointmentDate: form.visitDate,
        timeSlot: form.timeSlot,
        patientName: form.patientName
      })
      resultNo.value = res.data?.appointmentNo || `YY${Date.now()}`
    }
    showPreview.value = false
    showFeedback.value = true
  } finally {
    submitting.value = false
  }
}

function goPayment() {
  showFeedback.value = false
  uni.redirectTo({ url: '/pages/payment/payment' })
}

function goMyAppointment() {
  showFeedback.value = false
  uni.redirectTo({ url: '/pages/mine/my-appointment' })
}

function closeFeedback() {
  showFeedback.value = false
  if (isRegisterMode.value) {
    uni.navigateTo({ url: '/pages/mine/my-register' })
  }
}
</script>

<style lang="scss" scoped>
.page { min-height: 100vh; background: #f5f6f7; }

.form-card { padding: 24rpx; }

.form-card-title {
  font-size: 30rpx;
  font-weight: 600;
  color: #1f2329;
  margin-bottom: 20rpx;
}

.compact-form { display: flex; flex-direction: column; gap: 20rpx; }

.compact-row {
  display: flex;
  align-items: center;
}

.compact-label {
  width: 80rpx;
  font-size: 28rpx;
  color: #646a73;
  flex-shrink: 0;
}

.compact-value {
  flex: 1;
  text-align: right;
  font-size: 28rpx;
  color: #1f2329;
  font-weight: 500;
}

.type-chips {
  display: flex;
  gap: 12rpx;
  flex: 1;
  flex-wrap: wrap;
}

.type-chip {
  font-size: 26rpx;
  color: #646a73;
  background: #f7f8fa;
  padding: 12rpx 24rpx;
  border-radius: 32rpx;
  border: 2rpx solid transparent;
}

.type-chip.active {
  color: #3370ff;
  background: #f0f4ff;
  border-color: #3370ff;
}

.pay-row { padding-top: 8rpx; border-top: 1rpx solid #f0f1f3; }

.pay-hint {
  flex: 1;
  text-align: right;
  font-size: 28rpx;
  font-weight: 600;
}

.pay-hint.now { color: #f54a45; }
.pay-hint.later { color: #8f959e; }

.form-hint {
  display: block;
  font-size: 22rpx;
  color: #c9cdd4;
  margin-top: 16rpx;
}

.submit-btn {
  margin-top: 8rpx;

  &.disabled { opacity: 0.5; }
}

.preview-mask,
.feedback-mask {
  position: fixed;
  inset: 0;
  background: rgba(0, 0, 0, 0.45);
  z-index: 1000;
  display: flex;
  align-items: flex-end;
}

.feedback-mask {
  align-items: center;
  justify-content: center;
}

.preview-sheet {
  width: 100%;
  background: #fff;
  border-radius: 32rpx 32rpx 0 0;
  padding: 24rpx 32rpx calc(32rpx + env(safe-area-inset-bottom));
}

.preview-handle {
  width: 64rpx;
  height: 8rpx;
  background: #e5e6eb;
  border-radius: 4rpx;
  margin: 0 auto 24rpx;
}

.preview-title {
  display: block;
  font-size: 36rpx;
  font-weight: 700;
  color: #1f2329;
}

.preview-sub {
  display: block;
  font-size: 26rpx;
  color: #8f959e;
  margin: 8rpx 0 32rpx;
}

.preview-list {
  background: #f7f8fa;
  border-radius: 16rpx;
  padding: 8rpx 24rpx;
  margin-bottom: 32rpx;
}

.preview-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 20rpx 0;
  border-bottom: 1rpx solid #e5e6eb;

  &:last-child { border-bottom: none; }
}

.preview-label { font-size: 28rpx; color: #8f959e; }

.preview-value {
  font-size: 28rpx;
  color: #1f2329;
  font-weight: 500;
  text-align: right;
  max-width: 60%;

  &.time { color: #3370ff; }
}

.preview-fee {
  font-size: 40rpx;
  font-weight: 700;
  color: #f54a45;
}

.preview-actions {
  display: flex;
  gap: 20rpx;
}

.preview-btn {
  flex: 1;
  height: 88rpx;
  line-height: 88rpx;
  border-radius: 16rpx;
  font-size: 30rpx;
  font-weight: 500;
  border: none;

  &::after { border: none; }
}

.preview-btn.cancel { background: #f7f8fa; color: #646a73; }
.preview-btn.confirm { background: #3370ff; color: #fff; }

.feedback-card {
  width: 620rpx;
  background: #fff;
  border-radius: 24rpx;
  padding: 48rpx 40rpx;
  text-align: center;
}

.feedback-icon {
  width: 96rpx;
  height: 96rpx;
  border-radius: 50%;
  background: #eaffea;
  color: #34c724;
  font-size: 48rpx;
  font-weight: 700;
  display: flex;
  align-items: center;
  justify-content: center;
  margin: 0 auto 24rpx;
}

.feedback-title {
  display: block;
  font-size: 36rpx;
  font-weight: 700;
  color: #1f2329;
}

.feedback-desc {
  display: block;
  font-size: 26rpx;
  color: #8f959e;
  margin-top: 12rpx;
}

.feedback-detail {
  background: #f7f8fa;
  border-radius: 12rpx;
  padding: 24rpx;
  margin: 32rpx 0;
  text-align: left;
}

.feedback-row {
  display: flex;
  justify-content: space-between;
  padding: 12rpx 0;
  font-size: 26rpx;
  color: #1f2329;
}

.feedback-label { color: #8f959e; }
.feedback-no { color: #3370ff; font-weight: 600; }
.feedback-fee { color: #f54a45; font-weight: 600; }

.feedback-skip {
  display: block;
  font-size: 26rpx;
  color: #8f959e;
  margin-top: 24rpx;
}
</style>
