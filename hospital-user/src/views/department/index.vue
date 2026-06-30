<template>
  <div v-loading="loading" class="dept-page">
    <PageHeader title="科室介绍" subtitle="了解各科室专长，选择合适就诊" />

    <el-row v-if="list.length" :gutter="20">
      <el-col v-for="dept in list" :key="dept.id" :xs="24" :sm="12" :md="8">
        <div
          class="dept-card"
          :class="{ 'dept-card--inactive': dept.outpatient === false }"
          :style="cardStyle(dept)"
          @click="goDetail(dept)"
        >
          <div class="dept-card__icon">
            <el-icon :size="28">
              <component :is="getDeptVisual(dept.name).icon" />
            </el-icon>
          </div>

          <div class="dept-card__body">
            <div class="dept-card__head">
              <div>
                <h3>{{ dept.name }}</h3>
                <span v-if="dept.code" class="dept-code">{{ dept.code }}</span>
              </div>
              <el-tag
                v-if="dept.outpatient !== false"
                size="small"
                :type="slotStatus(dept.todaySlots).type"
                effect="light"
              >
                {{ slotStatus(dept.todaySlots).label }}
              </el-tag>
              <el-tag v-else size="small" type="info" effect="plain">不提供门诊</el-tag>
            </div>

            <div v-if="dept.specialties?.length" class="specialty-row">
              <el-tag
                v-for="tag in dept.specialties"
                :key="tag"
                size="small"
                effect="plain"
                class="specialty-tag"
              >
                {{ tag }}
              </el-tag>
            </div>

            <div v-if="dept.outpatient !== false" class="stats-row">
              <div class="stat-item">
                <span class="stat-value">{{ dept.waitingCount ?? 0 }}</span>
                <span class="stat-label">候诊人数</span>
              </div>
              <div class="stat-divider" />
              <div class="stat-item">
                <span class="stat-value" :class="`stat-value--${waitStatus(dept.avgWaitMinutes).type}`">
                  {{ dept.avgWaitMinutes ?? 0 }}<small>分</small>
                </span>
                <span class="stat-label">平均等待</span>
              </div>
            </div>

            <p class="dept-meta">
              <span>负责人：{{ dept.leader || '—' }}</span>
              <span>{{ dept.phone }}</span>
            </p>

            <el-button type="primary" link class="dept-link">
              查看详情 ›
            </el-button>
          </div>
        </div>
      </el-col>
    </el-row>
    <el-empty v-else description="暂无科室信息" :image-size="80" />
  </div>
</template>

<script setup>
import { useRouter } from 'vue-router'
import { userApi } from '@/api'
import { useListPage } from '@/composables/useListPage'
import { getDeptVisual, slotStatus, waitStatus } from '@/constants/department'
import { enrichDepartmentList } from '@/utils/deptEnrich'

const router = useRouter()

const { loading, list } = useListPage(async () => {
  const res = await userApi.departments()
  const rows = enrichDepartmentList(res.data.list.filter((d) => d.parentId === 0))
  return { data: { list: rows } }
})

function cardStyle(dept) {
  const visual = getDeptVisual(dept.name)
  return {
    '--dept-accent': visual.color,
    '--dept-bg': visual.bg
  }
}

function goDetail(dept) {
  router.push(`/department/${dept.id}`)
}
</script>

<style scoped>
.dept-page {
  max-width: 1080px;
}

.dept-card {
  display: flex;
  gap: 14px;
  padding: 18px;
  margin-bottom: 20px;
  background: #fff;
  border-radius: 12px;
  border: 1px solid var(--feishu-border-light);
  border-left: 4px solid var(--dept-accent, var(--feishu-primary));
  box-shadow: 0 1px 4px rgba(0, 0, 0, 0.04);
  cursor: pointer;
  transition: transform 0.15s, box-shadow 0.15s, border-color 0.15s;
}

.dept-card:hover {
  transform: translateY(-2px);
  box-shadow: 0 6px 20px rgba(0, 0, 0, 0.08);
  border-color: var(--dept-accent, var(--feishu-primary));
}

.dept-card--inactive {
  opacity: 0.85;
  border-left-color: var(--feishu-text-tertiary);
}

.dept-card--inactive:hover {
  transform: none;
}

.dept-card__icon {
  flex-shrink: 0;
  width: 52px;
  height: 52px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 12px;
  background: var(--dept-bg, var(--feishu-primary-bg));
  color: var(--dept-accent, var(--feishu-primary));
}

.dept-card__body {
  flex: 1;
  min-width: 0;
}

.dept-card__head {
  display: flex;
  align-items: center;
  gap: 8px;
  flex-wrap: wrap;
  margin-bottom: 10px;
}

.dept-card__head h3 {
  margin: 0;
  font-size: 17px;
  font-weight: 600;
}

.dept-code {
  display: inline-block;
  margin-top: 4px;
  padding: 1px 8px;
  font-size: 11px;
  font-weight: 600;
  letter-spacing: 0.04em;
  color: var(--feishu-text-tertiary);
  background: var(--feishu-bg-sub);
  border-radius: 4px;
  font-variant-numeric: tabular-nums;
}

.specialty-row {
  display: flex;
  flex-wrap: wrap;
  gap: 6px;
  margin-bottom: 12px;
}

.specialty-tag {
  border-color: var(--dept-accent, var(--feishu-primary));
  color: var(--dept-accent, var(--feishu-primary));
  background: var(--dept-bg, var(--feishu-primary-bg));
}

.stats-row {
  display: flex;
  align-items: center;
  gap: 16px;
  padding: 10px 12px;
  margin-bottom: 10px;
  background: var(--feishu-bg-sub);
  border-radius: 8px;
}

.stat-item {
  display: flex;
  flex-direction: column;
  gap: 2px;
}

.stat-value {
  font-size: 20px;
  font-weight: 700;
  color: var(--feishu-text-primary);
  font-variant-numeric: tabular-nums;
  line-height: 1.2;
}

.stat-value small {
  font-size: 12px;
  font-weight: 500;
  margin-left: 1px;
}

.stat-value--success { color: var(--feishu-success); }
.stat-value--warning { color: var(--feishu-warning); }
.stat-value--danger { color: var(--feishu-danger); }

.stat-label {
  font-size: 12px;
  color: var(--feishu-text-tertiary);
}

.stat-divider {
  width: 1px;
  height: 28px;
  background: var(--feishu-border-light);
}

.dept-meta {
  display: flex;
  flex-wrap: wrap;
  gap: 8px 16px;
  margin: 0 0 4px;
  font-size: 13px;
  color: var(--feishu-text-tertiary);
}

.dept-link {
  padding-left: 0;
  font-size: 13px;
}
</style>
