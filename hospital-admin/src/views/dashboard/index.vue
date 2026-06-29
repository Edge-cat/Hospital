<template>
  <PageContainer v-loading="pageLoading" class="dashboard-page">
    <div class="dashboard-hero">
      <div class="dashboard-hero__text">
        <h2>{{ greeting }}，{{ userStore.userName }}</h2>
        <p>
          <el-tag size="small" effect="plain">{{ userStore.roleLabel }}</el-tag>
          东软云医院 HIS 管理端 · {{ today }}
        </p>
      </div>
      <div class="dashboard-hero__badge">
        <el-icon :size="28"><Odometer /></el-icon>
        <span>{{ roleBoardTitle }}</span>
      </div>
    </div>

    <el-row :gutter="16" class="stat-row">
      <el-col v-for="item in statCards" :key="item.key" :xs="12" :sm="8" :md="4">
        <div class="stat-card-v2" :style="{ '--accent': item.color }">
          <div class="stat-card-v2__icon">
            <el-icon :size="24"><component :is="item.icon" /></el-icon>
          </div>
          <div class="stat-card-v2__body">
            <div class="stat-card-v2__value">{{ item.value }}</div>
            <div class="stat-card-v2__label">{{ item.label }}</div>
            <div v-if="item.mom != null" class="stat-card-v2__mom" :class="item.mom >= 0 ? 'up' : 'down'">
              环比 {{ item.mom >= 0 ? '+' : '' }}{{ item.mom }}%
            </div>
          </div>
        </div>
      </el-col>
    </el-row>

    <el-row :gutter="16" class="console-row">
      <el-col :span="24">
        <el-card shadow="never" class="dashboard-panel">
          <template #header>
            <div class="panel-header">
              <span>操作控制台</span>
              <div class="panel-header__actions">
                <el-tag size="small" type="info" effect="plain">今日 {{ consoleData.todayTotal ?? 0 }} 条</el-tag>
                <el-button link type="primary" @click="$router.push('/admin/operation-log')">全部日志</el-button>
              </div>
            </div>
          </template>
          <el-row :gutter="16" class="console-stats">
            <el-col :xs="8" :sm="8">
              <div class="console-stat">
                <div class="console-stat__value">{{ consoleData.todayTotal ?? 0 }}</div>
                <div class="console-stat__label">今日操作</div>
              </div>
            </el-col>
            <el-col :xs="8" :sm="8">
              <div class="console-stat">
                <div class="console-stat__value accent-frontend">{{ consoleData.frontendCount ?? 0 }}</div>
                <div class="console-stat__label">前端上报</div>
              </div>
            </el-col>
            <el-col :xs="8" :sm="8">
              <div class="console-stat">
                <div class="console-stat__value accent-backend">{{ consoleData.backendCount ?? 0 }}</div>
                <div class="console-stat__label">后端记录</div>
              </div>
            </el-col>
          </el-row>
          <el-table v-if="consoleRecent.length" :data="consoleRecent" size="small" stripe class="console-table">
            <el-table-column prop="createTime" label="时间" width="170" />
            <el-table-column prop="source" label="来源" width="80">
              <template #default="{ row }">{{ row.source === 'frontend' ? '前端' : '后端' }}</template>
            </el-table-column>
            <el-table-column prop="client" label="端" width="80">
              <template #default="{ row }">{{ { admin: '管理端', user: '用户端', mini: '小程序' }[row.client] || row.client || '-' }}</template>
            </el-table-column>
            <el-table-column prop="operator" label="操作人" width="100" />
            <el-table-column prop="module" label="模块" width="110" />
            <el-table-column prop="action" label="动作" width="110" />
            <el-table-column prop="path" label="路径" min-width="140" show-overflow-tooltip />
          </el-table>
          <el-empty v-else description="暂无操作记录" :image-size="48" />
        </el-card>
      </el-col>
    </el-row>

    <el-row :gutter="16" class="dashboard-charts">
      <el-col :span="8">
        <el-card shadow="never" class="dashboard-panel">
          <DashboardTrendChart
            title="近7日就诊量"
            :data="visitTrendData"
            :labels="visitTrendLabels"
            color="#409eff"
            :mom="overview?.trends?.todayVisit?.mom"
          />
        </el-card>
      </el-col>
      <el-col :span="8">
        <el-card shadow="never" class="dashboard-panel">
          <DashboardTrendChart
            title="近7日收入(元)"
            :data="incomeTrendData"
            :labels="visitTrendLabels"
            color="#67c23a"
            :mom="overview?.trends?.todayIncome?.mom"
          />
        </el-card>
      </el-col>
      <el-col :span="8">
        <el-card shadow="never" class="dashboard-panel">
          <DashboardTrendChart
            title="待结算笔数"
            :data="settlementTrendData"
            :labels="visitTrendLabels"
            color="#e6a23c"
            :mom="overview?.trends?.pendingSettlement?.mom"
          />
        </el-card>
      </el-col>
    </el-row>

    <el-row :gutter="16">
      <el-col :span="14">
        <el-card shadow="never" class="dashboard-panel">
          <template #header>
            <div class="panel-header">
              <span>快捷入口</span>
              <el-tag size="small" type="primary" effect="light">{{ userStore.roleLabel }}专属</el-tag>
            </div>
          </template>
          <div v-if="quickLinks.length" class="quick-link-grid-v2">
            <div
              v-for="link in quickLinks"
              :key="link.path"
              class="quick-link-v2"
              @click="$router.push(link.path)"
            >
              <div class="quick-link-v2__icon">
                <el-icon><component :is="link.icon || 'Link'" /></el-icon>
              </div>
              <span>{{ link.title }}</span>
            </div>
          </div>
          <el-empty v-else description="暂无可用入口" :image-size="64" />
        </el-card>
      </el-col>
      <el-col :span="10">
        <el-card shadow="never" class="dashboard-panel">
          <template #header>
            <div class="panel-header">
              <span>系统公告</span>
              <el-button link type="primary" @click="$router.push('/business/notice')">全部</el-button>
            </div>
          </template>
          <div v-if="notices.length" class="notice-list-v2">
            <div
              v-for="item in notices"
              :key="item.id"
              class="notice-item-v2"
              :style="noticeStyle(item.type)"
            >
              <div class="notice-item-v2__head">
                <el-tag :type="NOTICE_TYPE_STYLE[item.type]?.tag || 'info'" size="small" effect="dark">
                  {{ item.type }}
                </el-tag>
                <span class="notice-item-v2__time">{{ item.publishTime }}</span>
              </div>
              <div class="notice-item-v2__title">{{ item.title }}</div>
            </div>
          </div>
          <el-empty v-else description="暂无公告" :image-size="64" />
        </el-card>
      </el-col>
    </el-row>
  </PageContainer>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { statisticsApi, noticeApi, consoleApi } from '@/api'
import { useUserStore } from '@/stores/user'
import { usePermissionStore } from '@/stores/permission'
import { STAT_COLORS } from '@/constants'
import { ROLE_QUICK_LINKS, NOTICE_TYPE_STYLE } from '@/constants/dashboard'
import DashboardTrendChart from '@/components/DashboardTrendChart.vue'

const userStore = useUserStore()
const permissionStore = usePermissionStore()
const pageLoading = ref(false)
const notices = ref([])
const overview = ref(null)
const consoleData = ref({ todayTotal: 0, frontendCount: 0, backendCount: 0, recent: [] })

const consoleRecent = computed(() => consoleData.value.recent || [])

const today = new Date().toLocaleDateString('zh-CN', {
  year: 'numeric', month: 'long', day: 'numeric', weekday: 'long'
})

const greeting = computed(() => {
  const h = new Date().getHours()
  if (h < 12) return '上午好'
  if (h < 18) return '下午好'
  return '晚上好'
})

const roleBoardTitle = computed(() => ({
  admin: '全院运营看板',
  doctor: '诊疗工作台',
  nurse: '护理服务看板'
}[userStore.role] || '工作台'))

const statCards = computed(() => {
  const d = overview.value || {}
  const t = d.trends || {}
  return [
    { key: 'patient', label: '患者总数', value: d.patientCount ?? '-', icon: 'User', color: STAT_COLORS.primary, mom: t.patientCount?.mom },
    { key: 'doctor', label: '在职医生', value: d.doctorCount ?? '-', icon: 'Avatar', color: STAT_COLORS.success, mom: null },
    { key: 'visit', label: '今日就诊', value: d.todayVisit ?? '-', icon: 'FirstAidKit', color: STAT_COLORS.warning, mom: t.todayVisit?.mom },
    { key: 'income', label: '今日收入(元)', value: d.todayIncome != null ? d.todayIncome.toLocaleString() : '-', icon: 'Money', color: STAT_COLORS.danger, mom: t.todayIncome?.mom },
    { key: 'drug', label: '药品库存', value: d.drugStock ?? '-', icon: 'Box', color: STAT_COLORS.neutral, mom: null },
    { key: 'settle', label: '待结算', value: d.pendingSettlement ?? '-', icon: 'CreditCard', color: STAT_COLORS.primary, mom: t.pendingSettlement?.mom }
  ]
})

const visitTrendLabels = computed(() =>
  (overview.value?.visitTrend7d || []).map((v) => v.day)
)
const visitTrendData = computed(() =>
  (overview.value?.visitTrend7d || []).map((v) => v.count)
)
const incomeTrendData = computed(() => overview.value?.trends?.todayIncome?.series || [])
const settlementTrendData = computed(() => overview.value?.trends?.pendingSettlement?.series || [])

const quickLinks = computed(() => {
  const role = userStore.role
  const names = ROLE_QUICK_LINKS[role] || []
  const routeMap = Object.fromEntries(
    permissionStore.accessRoutes.filter((r) => r.name).map((r) => [r.name, r])
  )
  return names
    .map((name) => routeMap[name])
    .filter(Boolean)
    .map((r) => ({
      path: `/${r.path}`,
      title: r.meta.title,
      icon: r.meta.icon || 'Link'
    }))
})

function noticeStyle(type) {
  const s = NOTICE_TYPE_STYLE[type] || NOTICE_TYPE_STYLE['通知']
  return { borderLeftColor: s.border, background: s.bg }
}

async function loadDashboard() {
  pageLoading.value = true
  try {
    const [overviewRes, noticeRes, consoleRes] = await Promise.all([
      statisticsApi.overview(),
      noticeApi.list({ page: 1, pageSize: 6, status: 1 }),
      consoleApi.overview().catch(() => ({ data: {} }))
    ])
    overview.value = overviewRes.data
    consoleData.value = consoleRes.data || {}
    notices.value = (noticeRes.data.list || []).sort((a, b) => {
      const order = { 紧急: 0, 公告: 1, 通知: 2 }
      return (order[a.type] ?? 9) - (order[b.type] ?? 9)
    })
  } finally {
    pageLoading.value = false
  }
}

onMounted(loadDashboard)
</script>

<style scoped>
.dashboard-hero {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 28px 32px;
  margin-bottom: 16px;
  border-radius: 16px;
  background: linear-gradient(135deg, #e8f3ff 0%, #f5f9ff 50%, #fff 100%);
  box-shadow: 0 4px 20px rgba(64, 158, 255, 0.1);
  border: 1px solid rgba(64, 158, 255, 0.12);
}
.dashboard-hero h2 {
  font-size: 22px;
  font-weight: 600;
  margin-bottom: 8px;
}
.dashboard-hero p {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 14px;
  color: var(--feishu-text-secondary);
}
.dashboard-hero__badge {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 6px;
  padding: 16px 24px;
  border-radius: 12px;
  background: rgba(255, 255, 255, 0.8);
  color: var(--feishu-primary);
  font-size: 13px;
  font-weight: 500;
}
.stat-card-v2 {
  display: flex;
  align-items: center;
  gap: 14px;
  padding: 18px;
  margin-bottom: 16px;
  border-radius: 14px;
  background: linear-gradient(145deg, #fff 0%, #fafbfc 100%);
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.06);
  border: 1px solid var(--feishu-border-light);
  transition: transform 0.2s, box-shadow 0.2s;
}
.stat-card-v2:hover {
  transform: translateY(-2px);
  box-shadow: 0 6px 20px rgba(0, 0, 0, 0.08);
}
.stat-card-v2__icon {
  width: 48px;
  height: 48px;
  border-radius: 12px;
  background: var(--accent);
  color: #fff;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}
.stat-card-v2__value {
  font-size: 22px;
  font-weight: 700;
  font-variant-numeric: tabular-nums;
  line-height: 1.2;
}
.stat-card-v2__label {
  font-size: 12px;
  color: var(--feishu-text-tertiary);
  margin-top: 2px;
}
.stat-card-v2__mom {
  font-size: 11px;
  font-weight: 600;
  margin-top: 4px;
}
.stat-card-v2__mom.up { color: #67c23a; }
.stat-card-v2__mom.down { color: #f56c6c; }
.dashboard-charts { margin-bottom: 0; }
.dashboard-panel {
  margin-bottom: 16px;
  border-radius: 14px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.04);
}
.dashboard-panel :deep(.el-card__header) {
  padding: 14px 20px;
  border-bottom: 1px solid var(--feishu-border-light);
}
.panel-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
}
.quick-link-grid-v2 {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 12px;
}
.quick-link-v2 {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 10px;
  padding: 18px 12px;
  border-radius: 12px;
  cursor: pointer;
  background: linear-gradient(180deg, #fafbfc 0%, #fff 100%);
  border: 1px solid var(--feishu-border-light);
  transition: all 0.2s;
  text-align: center;
  font-size: 13px;
}
.quick-link-v2:hover {
  border-color: var(--feishu-primary);
  background: var(--feishu-primary-bg);
  color: var(--feishu-primary);
  box-shadow: 0 4px 12px rgba(64, 158, 255, 0.15);
}
.quick-link-v2__icon {
  width: 40px;
  height: 40px;
  border-radius: 10px;
  background: var(--feishu-primary-bg);
  color: var(--feishu-primary);
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 20px;
}
.notice-list-v2 { display: flex; flex-direction: column; gap: 10px; }
.notice-item-v2 {
  padding: 12px 14px;
  border-radius: 8px;
  border-left: 4px solid;
}
.notice-item-v2__head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 6px;
}
.notice-item-v2__time {
  font-size: 11px;
  color: var(--feishu-text-tertiary);
}
.notice-item-v2__title {
  font-size: 14px;
  font-weight: 500;
  line-height: 1.4;
}
.console-row { margin-bottom: 0; }
.panel-header__actions {
  display: flex;
  align-items: center;
  gap: 12px;
}
.console-stats { margin-bottom: 12px; }
.console-stat {
  text-align: center;
  padding: 12px 8px;
  border-radius: 10px;
  background: var(--feishu-bg-sub);
}
.console-stat__value {
  font-size: 24px;
  font-weight: 700;
  font-variant-numeric: tabular-nums;
  color: var(--feishu-primary);
}
.console-stat__value.accent-frontend { color: #e6a23c; }
.console-stat__value.accent-backend { color: #909399; }
.console-stat__label {
  margin-top: 4px;
  font-size: 12px;
  color: var(--feishu-text-tertiary);
}
.console-table { margin-top: 4px; }
</style>
