<template>
  <div class="charts-panel">
    <div class="chart-card">
      <div class="chart-card__head">
        <span class="chart-card__title">今日就诊</span>
        <span class="chart-card__total">{{ todayVisit }} <small>人次</small></span>
      </div>
      <div class="bar-chart">
        <div v-for="item in visitBars" :key="item.label" class="bar-row">
          <span class="bar-label">{{ item.label }}</span>
          <div class="bar-track">
            <div
              class="bar-fill"
              :style="{ width: `${(item.value / maxVisitBar) * 100}%` }"
            />
          </div>
          <span class="bar-value">{{ item.value }}</span>
        </div>
      </div>
    </div>

    <div class="chart-card">
      <div class="chart-card__head">
        <span class="chart-card__title">服务患者</span>
        <span class="chart-card__total">{{ patientCount }} <small>累计</small></span>
      </div>
      <div class="pie-wrap">
        <svg class="pie-chart" viewBox="0 0 120 120" aria-hidden="true">
          <circle cx="60" cy="60" r="48" fill="#f0f4ff" />
          <template v-for="(seg, idx) in pieSegments" :key="seg.name">
            <path :d="seg.path" :fill="seg.color" />
          </template>
          <circle cx="60" cy="60" r="26" fill="#fff" />
        </svg>
        <ul class="pie-legend">
          <li v-for="(seg, idx) in pieSegments" :key="seg.name">
            <span class="legend-dot" :style="{ background: seg.color }" />
            <span class="legend-name">{{ seg.name }}</span>
            <span class="legend-value">{{ seg.value }}</span>
          </li>
        </ul>
      </div>
    </div>
  </div>
</template>

<script setup>
import { computed } from 'vue'

const props = defineProps({
  todayVisit: { type: Number, default: 0 },
  patientCount: { type: Number, default: 0 },
  visitBars: {
    type: Array,
    default: () => [
      { label: '08-10时', value: 98 },
      { label: '10-12时', value: 86 },
      { label: '14-16时', value: 95 },
      { label: '16-18时', value: 63 }
    ]
  },
  patientCategories: {
    type: Array,
    default: () => [
      { name: '内科', value: 320 },
      { name: '外科', value: 280 },
      { name: '儿科', value: 210 },
      { name: '骨科', value: 180 },
      { name: '其他', value: 296 }
    ]
  }
})

const COLORS = ['#3370ff', '#5b8fff', '#7eb3ff', '#a8ccff', '#d6e8ff']

const maxVisitBar = computed(() =>
  Math.max(...props.visitBars.map((b) => b.value), 1)
)

const pieSegments = computed(() => {
  const total = props.patientCategories.reduce((s, c) => s + c.value, 0) || 1
  const cx = 60
  const cy = 60
  const r = 48
  let startAngle = -Math.PI / 2

  return props.patientCategories.map((cat, idx) => {
    const slice = (cat.value / total) * Math.PI * 2
    const endAngle = startAngle + slice
    const x1 = cx + r * Math.cos(startAngle)
    const y1 = cy + r * Math.sin(startAngle)
    const x2 = cx + r * Math.cos(endAngle)
    const y2 = cy + r * Math.sin(endAngle)
    const large = slice > Math.PI ? 1 : 0
    const path = `M ${cx} ${cy} L ${x1} ${y1} A ${r} ${r} 0 ${large} 1 ${x2} ${y2} Z`
    startAngle = endAngle
    return { ...cat, path, color: COLORS[idx % COLORS.length] }
  })
})
</script>

<style scoped>
.charts-panel {
  display: flex;
  flex-direction: column;
  gap: 16px;
  height: 100%;
}

.chart-card {
  flex: 1;
  padding: 20px 22px;
  border: 1px solid var(--feishu-border-light);
  border-radius: var(--feishu-radius-lg);
  background: var(--feishu-bg-white, #fff);
  box-shadow: var(--feishu-shadow-sm);
  display: flex;
  flex-direction: column;
}

.chart-card__head {
  display: flex;
  align-items: baseline;
  justify-content: space-between;
  margin-bottom: 16px;
}

.chart-card__title {
  font-size: 15px;
  font-weight: 600;
  color: var(--feishu-text-primary);
}

.chart-card__total {
  font-size: 22px;
  font-weight: 700;
  color: var(--feishu-primary);
  font-variant-numeric: tabular-nums;
}

.chart-card__total small {
  font-size: 12px;
  font-weight: 500;
  color: var(--feishu-text-tertiary);
}

.bar-chart {
  display: flex;
  flex-direction: column;
  gap: 12px;
  flex: 1;
  justify-content: center;
}

.bar-row {
  display: grid;
  grid-template-columns: 56px 1fr 36px;
  align-items: center;
  gap: 10px;
}

.bar-label {
  font-size: 12px;
  color: var(--feishu-text-tertiary);
}

.bar-track {
  height: 10px;
  background: var(--feishu-primary-bg);
  border-radius: 5px;
  overflow: hidden;
}

.bar-fill {
  height: 100%;
  background: linear-gradient(90deg, #3370ff, #66b1ff);
  border-radius: 5px;
  transition: width 0.6s ease;
  min-width: 4px;
}

.bar-value {
  font-size: 13px;
  font-weight: 600;
  color: var(--feishu-text-primary);
  text-align: right;
  font-variant-numeric: tabular-nums;
}

.pie-wrap {
  display: flex;
  align-items: center;
  gap: 16px;
  flex: 1;
}

.pie-chart {
  width: 120px;
  height: 120px;
  flex-shrink: 0;
}

.pie-legend {
  list-style: none;
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 8px;
  min-width: 0;
}

.pie-legend li {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 12px;
}

.legend-dot {
  width: 8px;
  height: 8px;
  border-radius: 50%;
  flex-shrink: 0;
}

.legend-name {
  flex: 1;
  color: var(--feishu-text-secondary);
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.legend-value {
  font-weight: 600;
  color: var(--feishu-text-primary);
  font-variant-numeric: tabular-nums;
}
</style>
