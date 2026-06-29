<template>
  <PageContainer>
    <PageHeader title="决策支持" subtitle="关键指标监控与智能决策建议" />

    <div v-loading="loading">
      <el-row :gutter="16">
        <el-col :span="6" v-for="kpi in data.kpis" :key="kpi.label">
          <el-card shadow="hover" class="kpi-card">
            <div class="kpi-label">{{ kpi.label }}</div>
            <div class="kpi-value">{{ kpi.value }}</div>
            <div class="kpi-trend" :class="kpi.trend.startsWith('+') ? 'up' : 'down'">
              {{ kpi.trend.startsWith('+') ? '↑' : '↓' }} {{ kpi.trend }}
            </div>
          </el-card>
        </el-col>
      </el-row>

      <el-card shadow="never" style="margin-top: 16px">
        <template #header><span>智能决策建议</span></template>
        <div v-if="data.suggestions.length" class="suggestions">
          <el-alert
            v-for="(item, index) in data.suggestions"
            :key="index"
            :title="item.title"
            :description="item.content"
            :type="item.level"
            show-icon
            :closable="false"
            style="margin-bottom: 12px"
          />
        </div>
        <el-empty v-else description="暂无决策建议" :image-size="64" />
      </el-card>
    </div>
  </PageContainer>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { statisticsApi } from '@/api'

const loading = ref(false)
const data = ref({ kpis: [], suggestions: [] })

onMounted(async () => {
  loading.value = true
  try {
    const res = await statisticsApi.decision()
    data.value = res.data
  } finally {
    loading.value = false
  }
})
</script>

<style scoped>
.kpi-card :deep(.el-card__body) {
  text-align: center;
  padding: 24px 16px;
}

.kpi-label {
  font-size: 13px;
  color: var(--feishu-text-tertiary);
  margin-bottom: 8px;
}

.kpi-value {
  font-size: 28px;
  font-weight: 700;
  color: var(--feishu-text-primary);
}

.kpi-trend {
  font-size: 13px;
  margin-top: 8px;
}

.kpi-trend.up { color: var(--feishu-success); }
.kpi-trend.down { color: var(--feishu-danger); }
</style>
