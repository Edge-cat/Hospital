<template>
  <PageContainer>
    <PageHeader title="数据分析" subtitle="就诊趋势、科室排名与收入结构分析" />

    <div v-loading="loading">
      <el-row :gutter="16">
        <el-col :span="12">
          <el-card shadow="never">
            <template #header><span>月度就诊量趋势</span></template>
            <div v-if="data.visitTrend.length" class="chart-bars">
              <div v-for="item in data.visitTrend" :key="item.month" class="bar-item">
                <div class="bar" :style="{ height: (item.count / maxVisit * 160) + 'px' }">
                  <span class="bar-value">{{ item.count }}</span>
                </div>
                <span class="bar-label">{{ item.month }}</span>
              </div>
            </div>
            <el-empty v-else description="暂无数据" :image-size="64" />
          </el-card>
        </el-col>
        <el-col :span="12">
          <el-card shadow="never">
            <template #header><span>科室就诊量排名</span></template>
            <el-table :data="data.departmentRank" stripe size="small">
              <el-table-column type="index" label="排名" width="60" />
              <el-table-column prop="name" label="科室" />
              <el-table-column prop="value" label="就诊量" width="100" />
              <el-table-column label="占比" width="120">
                <template #default="{ row }">
                  <el-progress :percentage="Math.round(row.value / maxDept * 100)" :stroke-width="10" />
                </template>
              </el-table-column>
            </el-table>
          </el-card>
        </el-col>
      </el-row>

      <el-row :gutter="16" style="margin-top: 16px">
        <el-col :span="24">
          <el-card shadow="never">
            <template #header><span>收入分类统计</span></template>
            <div v-if="data.incomeByCategory.length" class="income-grid">
              <div v-for="item in data.incomeByCategory" :key="item.name" class="income-item">
                <div class="income-name">{{ item.name }}</div>
                <div class="income-value">¥{{ item.value.toLocaleString() }}</div>
                <el-progress :percentage="Math.round(item.value / maxIncome * 100)" :stroke-width="8" />
              </div>
            </div>
            <el-empty v-else description="暂无数据" :image-size="64" />
          </el-card>
        </el-col>
      </el-row>
    </div>
  </PageContainer>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { statisticsApi } from '@/api'

const loading = ref(false)
const data = ref({ visitTrend: [], departmentRank: [], incomeByCategory: [] })
const maxVisit = computed(() => Math.max(...data.value.visitTrend.map((i) => i.count), 1))
const maxDept = computed(() => Math.max(...data.value.departmentRank.map((i) => i.value), 1))
const maxIncome = computed(() => Math.max(...data.value.incomeByCategory.map((i) => i.value), 1))

onMounted(async () => {
  loading.value = true
  try {
    const res = await statisticsApi.analysis()
    data.value = res.data
  } finally {
    loading.value = false
  }
})
</script>
