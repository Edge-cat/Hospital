<template>
  <div class="trend-chart">
    <div class="trend-chart__header">
      <span class="trend-chart__title">{{ title }}</span>
      <span v-if="mom != null" class="trend-chart__mom" :class="mom >= 0 ? 'up' : 'down'">
        环比 {{ mom >= 0 ? '+' : '' }}{{ mom }}%
      </span>
    </div>
    <svg :viewBox="`0 0 ${width} ${height}`" class="trend-chart__svg" preserveAspectRatio="none">
      <defs>
        <linearGradient :id="gradId" x1="0" y1="0" x2="0" y2="1">
          <stop offset="0%" :stop-color="color" stop-opacity="0.35" />
          <stop offset="100%" :stop-color="color" stop-opacity="0.02" />
        </linearGradient>
      </defs>
      <path :d="areaPath" :fill="`url(#${gradId})`" />
      <polyline :points="linePoints" fill="none" :stroke="color" stroke-width="2" stroke-linecap="round" stroke-linejoin="round" />
      <circle
        v-for="(p, i) in points"
        :key="i"
        :cx="p.x"
        :cy="p.y"
        r="3"
        :fill="color"
      />
    </svg>
    <div v-if="labels.length" class="trend-chart__labels">
      <span v-for="(lb, i) in labels" :key="i">{{ lb }}</span>
    </div>
  </div>
</template>

<script setup>
import { computed } from 'vue'

const props = defineProps({
  title: { type: String, default: '近7日趋势' },
  data: { type: Array, default: () => [] },
  labels: { type: Array, default: () => [] },
  color: { type: String, default: '#409eff' },
  mom: { type: Number, default: null }
})

const width = 280
const height = 80
const pad = 8
const gradId = `grad-${Math.random().toString(36).slice(2, 8)}`

const points = computed(() => {
  const vals = props.data.length ? props.data : [0]
  const max = Math.max(...vals, 1)
  const min = Math.min(...vals, 0)
  const range = max - min || 1
  const step = vals.length > 1 ? (width - pad * 2) / (vals.length - 1) : 0
  return vals.map((v, i) => ({
    x: pad + i * step,
    y: height - pad - ((v - min) / range) * (height - pad * 2)
  }))
})

const linePoints = computed(() => points.value.map((p) => `${p.x},${p.y}`).join(' '))

const areaPath = computed(() => {
  const pts = points.value
  if (!pts.length) return ''
  const base = height - pad
  return `M ${pts[0].x} ${base} ` +
    pts.map((p) => `L ${p.x} ${p.y}`).join(' ') +
    ` L ${pts[pts.length - 1].x} ${base} Z`
})
</script>

<style scoped>
.trend-chart__header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 8px;
}
.trend-chart__title {
  font-size: 13px;
  font-weight: 500;
  color: var(--feishu-text-secondary);
}
.trend-chart__mom {
  font-size: 12px;
  font-weight: 600;
}
.trend-chart__mom.up { color: #67c23a; }
.trend-chart__mom.down { color: #f56c6c; }
.trend-chart__svg {
  width: 100%;
  height: 80px;
  display: block;
}
.trend-chart__labels {
  display: flex;
  justify-content: space-between;
  margin-top: 6px;
  font-size: 11px;
  color: var(--feishu-text-tertiary);
}
</style>
