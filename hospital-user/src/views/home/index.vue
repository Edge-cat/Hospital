<template>
  <div class="home-page" v-loading="pageLoading">
    <DashboardLayout>
      <template #hero>
        <div class="hero-inner">
          <div class="hero-content">
            <DoctorIllustration v-if="userStore.isLoggedIn" class="hero-doctor" />
            <div class="hero-text">
              <p class="hero-eyebrow">智慧医疗 · 便捷就医</p>
              <h1>{{ userStore.isLoggedIn ? `您好，${userStore.userName}` : '东软云医院' }}</h1>
              <p class="hero-sub">在线挂号、预约就诊、便捷缴费 — 一站式患者服务</p>
            </div>
          </div>
          <div class="hero-cta">
            <el-button type="primary" round @click="goTo('/register', true)">立即挂号</el-button>
            <el-button round @click="goTo('/appointment', true)">预约挂号</el-button>
          </div>
        </div>
      </template>

      <template #primary>
        <DashboardFunctionCard
          v-for="item in functionItems"
          :key="item.path"
          :title="item.title"
          :description="item.description"
          :icon="item.icon"
          :icon-bg="item.iconBg"
          :auth="item.auth"
          :logged-in="userStore.isLoggedIn"
          @click="goTo(item.path, item.auth)"
        />
      </template>

      <template #secondary>
        <DashboardCharts
          :today-visit="stats.todayVisit"
          :patient-count="stats.patientCount"
          :visit-bars="visitBars"
          :patient-categories="patientCategories"
        />
      </template>

      <template #main>
        <div class="panel">
          <div class="panel__head">
            <span class="panel__title">科室推荐</span>
            <router-link to="/department" class="panel__link">全部</router-link>
          </div>
          <DashboardTimeline :items="departments" @select="onDeptSelect" />
        </div>
      </template>

      <template #side>
        <div class="panel">
          <div class="panel__head">
            <span class="panel__title">医院公告</span>
            <router-link to="/notice" class="panel__link">更多</router-link>
          </div>
          <DashboardFeed :items="notices" @select="onNoticeSelect" />
        </div>
      </template>
    </DashboardLayout>
  </div>
</template>

<script setup>
import { computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ClipboardList, CalendarDays, Wallet, FileText, Building2, Megaphone, Ticket, Clock } from 'lucide-vue-next'
import DashboardLayout from '@/components/layout/DashboardLayout.vue'
import DashboardFunctionCard from '@/components/dashboard/DashboardFunctionCard.vue'
import DashboardCharts from '@/components/dashboard/DashboardCharts.vue'
import DoctorIllustration from '@/components/dashboard/DoctorIllustration.vue'
import DashboardTimeline from '@/components/dashboard/DashboardTimeline.vue'
import DashboardFeed from '@/components/dashboard/DashboardFeed.vue'
import { useHospitalDashboard } from '@/composables/useHospitalView'
import { useUserStore } from '@/stores/user'

const router = useRouter()
const userStore = useUserStore()
const { stats, departments, notices, pageLoading, loadDashboard } = useHospitalDashboard()

const functionItems = [
  { title: '在线挂号', description: '选择科室与医生', path: '/register', icon: ClipboardList, iconBg: 'var(--feishu-primary-bg)', auth: true },
  { title: '在线预约', description: '预约就诊时段', path: '/appointment', icon: CalendarDays, iconBg: 'var(--feishu-success-bg)', auth: true },
  { title: '在线缴费', description: '待缴与已缴账单', path: '/payment', icon: Wallet, iconBg: 'var(--feishu-warning-bg)', auth: true },
  { title: '就诊记录', description: '历史诊疗信息', path: '/records', icon: FileText, iconBg: 'var(--feishu-danger-bg)', auth: true },
  { title: '科室介绍', description: '了解各科室专长', path: '/department', icon: Building2, iconBg: 'var(--feishu-info-bg)', auth: false },
  { title: '医院公告', description: '通知与重要信息', path: '/notice', icon: Megaphone, iconBg: 'var(--feishu-primary-bg)', auth: false },
  { title: '我的挂号', description: '查看挂号记录', path: '/my-register', icon: Ticket, iconBg: 'var(--feishu-success-bg)', auth: true },
  { title: '我的预约', description: '管理预约订单', path: '/my-appointment', icon: Clock, iconBg: 'var(--feishu-warning-bg)', auth: true }
]

const visitBars = computed(() => {
  const total = stats.value.todayVisit || 342
  const ratios = [0.29, 0.25, 0.28, 0.18]
  const labels = ['08-10时', '10-12时', '14-16时', '16-18时']
  const values = ratios.map((r) => Math.round(total * r))
  values[0] += total - values.reduce((a, b) => a + b, 0)
  return labels.map((label, i) => ({ label, value: values[i] }))
})

const patientCategories = computed(() => {
  const total = stats.value.patientCount || 0
  if (!total) return []
  const ratios = [0.28, 0.22, 0.18, 0.14, 0.18]
  const names = ['内科', '外科', '儿科', '骨科', '其他']
  return names.map((name, i) => ({
    name,
    value: Math.round(total * ratios[i])
  }))
})

function goTo(path, auth) {
  if (auth && !userStore.isLoggedIn) {
    router.push({ name: 'Login', query: { redirect: path } })
    return
  }
  router.push(path)
}

function onDeptSelect(item) {
  router.push(`/department/${item.id}`)
}

function onNoticeSelect(item) {
  router.push(`/notice/${item.id}`)
}

onMounted(loadDashboard)
</script>

<style scoped>
.home-page {
  min-height: 200px;
}

.hero-inner {
  display: flex;
  flex-wrap: wrap;
  align-items: center;
  justify-content: space-between;
  gap: 24px;
}

.hero-content {
  display: flex;
  align-items: center;
  gap: 20px;
  flex: 1;
  min-width: 0;
}

.hero-text {
  min-width: 0;
}

.hero-eyebrow {
  font-size: 12px;
  font-weight: 600;
  letter-spacing: 0.06em;
  text-transform: uppercase;
  color: var(--feishu-text-tertiary);
  margin-bottom: 8px;
}

.hero-inner h1 {
  font-size: clamp(22px, 4vw, 28px);
  font-weight: 700;
  letter-spacing: -0.02em;
  color: var(--feishu-text-primary);
  margin: 0 0 8px;
}

.hero-sub {
  font-size: 14px;
  color: var(--feishu-text-secondary);
  margin: 0;
  max-width: 420px;
  line-height: 1.6;
}

.hero-cta {
  display: flex;
  gap: 10px;
  flex-shrink: 0;
}

.panel__link {
  font-size: 13px;
  color: var(--feishu-text-tertiary);
  text-decoration: none;
  transition: color 0.15s;
}

.panel__link:hover {
  color: var(--feishu-primary);
}
</style>
