<template>
  <div class="stat-card" :class="{ 'is-loaded': loaded }">
    <span class="stat-card__label">{{ label }}</span>
    <span class="stat-card__value" ref="valueEl">{{ displayValue }}</span>
    <span v-if="suffix" class="stat-card__suffix">{{ suffix }}</span>
  </div>
</template>

<script setup>
import { ref, watch, onMounted } from 'vue'

const props = defineProps({
  label: { type: String, required: true },
  value: { type: Number, default: 0 },
  suffix: { type: String, default: '' },
  duration: { type: Number, default: 900 }
})

const displayValue = ref(0)
const loaded = ref(false)

function animateTo(target) {
  const start = displayValue.value
  const diff = target - start
  const startTime = performance.now()

  function step(now) {
    const progress = Math.min((now - startTime) / props.duration, 1)
    const eased = 1 - Math.pow(1 - progress, 3)
    displayValue.value = Math.round(start + diff * eased)
    if (progress < 1) requestAnimationFrame(step)
  }
  requestAnimationFrame(step)
}

watch(() => props.value, (v) => animateTo(v ?? 0), { immediate: true })

onMounted(() => {
  requestAnimationFrame(() => { loaded.value = true })
})
</script>

<style scoped>
.stat-card {
  padding: 20px 22px;
  border: 1px solid var(--feishu-border-light);
  border-radius: var(--feishu-radius-lg);
  background: var(--feishu-bg-white, #fff);
  box-shadow: var(--feishu-shadow-sm);
  opacity: 0;
  transform: translateY(8px);
  transition: opacity 0.4s ease, transform 0.4s ease;
}

.stat-card.is-loaded {
  opacity: 1;
  transform: translateY(0);
}

.stat-card__label {
  display: block;
  font-size: 13px;
  color: var(--feishu-text-tertiary);
  margin-bottom: 8px;
}

.stat-card__value {
  font-size: 36px;
  font-weight: 700;
  letter-spacing: -0.02em;
  color: var(--feishu-text-primary);
  font-variant-numeric: tabular-nums;
  line-height: 1.1;
}

.stat-card__suffix {
  display: block;
  margin-top: 4px;
  font-size: 12px;
  color: var(--feishu-text-secondary);
}
</style>
