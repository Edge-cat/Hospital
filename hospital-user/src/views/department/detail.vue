<template>
  <div v-loading="loading" class="dept-detail">
    <el-button class="detail-back" link type="primary" @click="$router.push('/department')">
      <el-icon><ArrowLeft /></el-icon> 返回科室列表
    </el-button>

    <template v-if="dept">
      <el-card shadow="never" class="dept-intro-card">
        <PageHeader :title="dept.name" :subtitle="dept.desc || `负责人：${dept.leader}`" />
        <p v-if="dept.desc" class="dept-desc">{{ dept.desc }}</p>

        <div v-if="dept.commonDiseases?.length" class="disease-row">
          <span class="disease-label">常见疾病：</span>
          <el-tag
            v-for="d in dept.commonDiseases"
            :key="d"
            size="small"
            effect="plain"
            class="disease-tag"
          >
            {{ d }}
          </el-tag>
        </div>

        <el-descriptions :column="2" border size="small" class="dept-meta">
          <el-descriptions-item label="科室编码">{{ dept.code || '—' }}</el-descriptions-item>
          <el-descriptions-item label="负责人">{{ dept.leader }}</el-descriptions-item>
          <el-descriptions-item label="联系电话">{{ dept.phone }}</el-descriptions-item>
        </el-descriptions>

        <el-button class="dept-quick-register" link type="primary" @click="goRegister()">
          前往挂号（快速入口）
        </el-button>
      </el-card>

      <div class="doctor-toolbar">
        <div>
          <h3 class="section-title">科室医生</h3>
          <p class="section-hint">点击医生或「履历」查看个人简介</p>
        </div>
        <el-radio-group v-model="shiftFilter" size="small">
          <el-radio-button v-for="s in SHIFT_FILTERS" :key="s.key" :value="s.key">
            {{ s.label }}
          </el-radio-button>
        </el-radio-group>
      </div>

      <div v-if="filteredDoctors.length" v-loading="loadingDoctors" class="doctor-list">
        <el-card
          v-for="doc in filteredDoctors"
          :key="doc.id"
          shadow="never"
          class="doctor-card"
          :class="{ 'doctor-card--full': !hasAvailableSlots(doc, shiftFilter) }"
        >
          <div class="doctor-head" @click="openProfile(doc)">
            <div class="doctor-info">
              <div class="doctor-avatar">{{ doc.name?.charAt(0) }}</div>
              <div>
                <span class="doctor-name">{{ doc.name }}</span>
                <el-tag size="small" type="info" effect="light">{{ doc.title }}</el-tag>
              </div>
            </div>
            <div class="doctor-head-actions">
              <el-button link type="primary" size="small" @click.stop="openProfile(doc)">履历</el-button>
              <div class="slot-summary" :class="{ 'slot-summary--warn': !hasAvailableSlots(doc, shiftFilter) }">
                {{ slotSummary(doc) }}
              </div>
            </div>
          </div>

          <p class="doctor-specialty">{{ doc.specialty }}</p>

          <div class="fee-row">
            <span class="fee-label">挂号费用：</span>
            <el-tag
              v-for="fee in REGISTER_FEE_LIST"
              :key="fee.value"
              size="small"
              :type="fee.value === suggestRegisterType(doc.title) ? 'primary' : 'info'"
              effect="plain"
            >
              {{ fee.label }} ¥{{ fee.fee }}
            </el-tag>
          </div>

          <div v-if="doc.weekSchedule?.length" class="schedule-block">
            <span class="schedule-label">本周出诊</span>
            <div class="schedule-grid">
              <div v-for="day in doc.weekSchedule" :key="day.date" class="schedule-day">
                <span class="day-name">{{ day.weekday }}</span>
                <div class="day-slots">
                  <span v-if="day.morning" class="slot-chip slot-chip--am">上午</span>
                  <span v-if="day.afternoon" class="slot-chip slot-chip--pm">下午</span>
                  <span v-if="!day.morning && !day.afternoon" class="slot-chip slot-chip--off">休</span>
                </div>
              </div>
            </div>
          </div>

          <div class="doctor-actions">
            <el-button
              v-if="hasAvailableSlots(doc, shiftFilter)"
              type="primary"
              @click="goRegister(doc)"
            >
              立即挂号
            </el-button>
            <template v-else>
              <el-button type="warning" plain @click="goRegister(doc, { waitlist: true })">
                候补挂号
              </el-button>
              <el-button @click="goAppointment(doc)">在线预约</el-button>
            </template>
          </div>
        </el-card>
      </div>
      <el-empty v-else-if="!loadingDoctors" description="该时段暂无出诊医生" :image-size="80" />
    </template>
    <el-empty v-else-if="!loading" description="科室不存在" :image-size="80" />

    <DoctorProfileDrawer
      v-model:visible="profileVisible"
      :doctor="profileDoctor"
      :dept-label="dept?.name || ''"
      book-label="预约该医生"
      @book="bookFromProfile"
    />
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { userApi } from '@/api'
import {
  SHIFT_FILTERS,
  REGISTER_FEE_LIST,
  suggestRegisterType,
  filterDoctorsByShift,
  slotSummary,
  hasAvailableSlots
} from '@/constants/department'
import { enrichDepartment } from '@/utils/deptEnrich'
import { mapDoctorList } from '@/utils/doctorProfile'
import DoctorProfileDrawer from '@/components/DoctorProfileDrawer.vue'

const route = useRoute()
const router = useRouter()
const loading = ref(false)
const loadingDoctors = ref(false)
const dept = ref(null)
const doctors = ref([])
const shiftFilter = ref('all')
const profileVisible = ref(false)
const profileDoctor = ref(null)

const filteredDoctors = computed(() => filterDoctorsByShift(doctors.value, shiftFilter.value))

function goRegister(doc, extra = {}) {
  const query = { department: dept.value.name }
  if (doc) {
    query.doctor = doc.name
    query.registerType = suggestRegisterType(doc.title)
  }
  if (extra.waitlist) query.waitlist = '1'
  router.push({ path: '/register', query })
}

function goAppointment(doc) {
  router.push({
    path: '/appointment',
    query: { department: dept.value.name, doctor: doc.name }
  })
}

function openProfile(doc) {
  profileDoctor.value = doc
  profileVisible.value = true
}

function bookFromProfile(doc) {
  profileVisible.value = false
  goAppointment(doc)
}

onMounted(async () => {
  loading.value = true
  try {
    const res = await userApi.departments()
    const raw = res.data.list.find((d) => d.id === Number(route.params.id))
    dept.value = enrichDepartment(raw)
    if (dept.value) {
      loadingDoctors.value = true
      try {
        const docRes = await userApi.doctors({ department: dept.value.name, pageSize: 50 })
        doctors.value = mapDoctorList(docRes.data.list.filter((d) => d.status !== 0))
      } finally {
        loadingDoctors.value = false
      }
    }
  } finally {
    loading.value = false
  }
})
</script>

<style scoped>
.dept-detail {
  max-width: 960px;
}

.dept-intro-card {
  margin-bottom: 20px;
}

.dept-desc {
  margin: 0 0 12px;
  font-size: 14px;
  line-height: 1.7;
  color: var(--feishu-text-secondary);
}

.disease-row {
  display: flex;
  flex-wrap: wrap;
  align-items: center;
  gap: 8px;
  margin-bottom: 16px;
}

.disease-label {
  font-size: 13px;
  color: var(--feishu-text-tertiary);
}

.disease-tag {
  border-radius: 4px;
}

.dept-meta {
  margin-bottom: 12px;
}

.dept-quick-register {
  font-size: 13px;
  padding-left: 0;
}

.doctor-toolbar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  flex-wrap: wrap;
  gap: 12px;
  margin-bottom: 16px;
}

.section-title {
  margin: 0;
  font-size: 16px;
  font-weight: 600;
}

.section-hint {
  margin: 4px 0 0;
  font-size: 12px;
  color: var(--feishu-text-tertiary);
}

.doctor-list {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.doctor-card {
  border: 1px solid var(--feishu-border);
  transition: box-shadow 0.2s;
}

.doctor-card:hover {
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.06);
}

.doctor-card--full {
  border-color: var(--feishu-warning);
  background: #fffbf0;
}

.doctor-head {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  gap: 12px;
  margin-bottom: 8px;
  cursor: pointer;
}

.doctor-info {
  display: flex;
  align-items: center;
  gap: 10px;
}

.doctor-avatar {
  width: 40px;
  height: 40px;
  border-radius: 50%;
  background: linear-gradient(135deg, var(--feishu-primary), #66b1ff);
  color: #fff;
  font-size: 16px;
  font-weight: 600;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}

.doctor-head-actions {
  display: flex;
  flex-direction: column;
  align-items: flex-end;
  gap: 4px;
}

.doctor-name {
  font-size: 17px;
  font-weight: 600;
  color: var(--feishu-text-primary);
}

.slot-summary {
  font-size: 13px;
  color: var(--feishu-success);
  white-space: nowrap;
  font-variant-numeric: tabular-nums;
}

.slot-summary--warn {
  color: var(--feishu-warning);
}

.doctor-specialty {
  margin: 0 0 12px;
  font-size: 14px;
  line-height: 1.6;
  color: var(--feishu-text-secondary);
}

.fee-row {
  display: flex;
  flex-wrap: wrap;
  align-items: center;
  gap: 8px;
  margin-bottom: 14px;
}

.fee-label {
  font-size: 13px;
  color: var(--feishu-text-tertiary);
}

.schedule-block {
  margin-bottom: 16px;
}

.schedule-label {
  display: block;
  font-size: 13px;
  color: var(--feishu-text-tertiary);
  margin-bottom: 8px;
}

.schedule-grid {
  display: grid;
  grid-template-columns: repeat(7, 1fr);
  gap: 6px;
}

.schedule-day {
  text-align: center;
  padding: 6px 4px;
  background: var(--feishu-bg-base);
  border-radius: 6px;
  font-size: 12px;
}

.day-name {
  display: block;
  color: var(--feishu-text-tertiary);
  margin-bottom: 4px;
}

.day-slots {
  display: flex;
  flex-direction: column;
  gap: 2px;
  align-items: center;
}

.slot-chip {
  padding: 1px 6px;
  border-radius: 4px;
  font-size: 11px;
}

.slot-chip--am {
  background: #e8f3ff;
  color: var(--feishu-primary);
}

.slot-chip--pm {
  background: #fff7e8;
  color: var(--feishu-warning);
}

.slot-chip--off {
  background: #f0f0f0;
  color: var(--feishu-text-tertiary);
}

.doctor-actions {
  display: flex;
  gap: 10px;
  flex-wrap: wrap;
}

@media (max-width: 640px) {
  .schedule-grid {
    grid-template-columns: repeat(4, 1fr);
  }

  .doctor-head {
    flex-direction: column;
  }
}
</style>
