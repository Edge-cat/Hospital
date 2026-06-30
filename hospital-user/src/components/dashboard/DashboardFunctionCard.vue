<template>
  <button
    type="button"
    class="func-card"
    :class="{ 'is-auth': auth && !loggedIn }"
    @click="$emit('click')"
  >
    <div class="func-card__icon" :style="{ background: iconBg }">
      <component :is="icon" :size="22" :stroke-width="1.75" />
    </div>
    <div class="func-card__body">
      <span class="func-card__title">{{ title }}</span>
      <span v-if="description" class="func-card__desc">{{ description }}</span>
    </div>
    <ChevronRight :size="16" class="func-card__arrow" :stroke-width="1.75" />
  </button>
</template>

<script setup>
import { ChevronRight } from 'lucide-vue-next'

defineProps({
  title: { type: String, required: true },
  description: { type: String, default: '' },
  icon: { type: [Object, Function], required: true },
  iconBg: { type: String, default: 'var(--dash-icon-bg, #f4f4f5)' },
  auth: { type: Boolean, default: false },
  loggedIn: { type: Boolean, default: true }
})

defineEmits(['click'])
</script>

<style scoped>
.func-card {
  display: flex;
  align-items: center;
  gap: 14px;
  width: 100%;
  padding: 16px 18px;
  border: 1px solid var(--feishu-border-light);
  border-radius: var(--feishu-radius-lg);
  background: var(--feishu-bg-white, #fff);
  cursor: pointer;
  text-align: left;
  transition: transform 0.18s ease, box-shadow 0.18s ease, border-color 0.18s ease;
  box-shadow: var(--feishu-shadow-sm);
}

.func-card:hover {
  transform: translateY(-2px);
  border-color: var(--feishu-border);
  box-shadow: var(--feishu-shadow-md);
}

.func-card:active {
  transform: translateY(0) scale(0.99);
}

.func-card.is-auth {
  opacity: 0.92;
}

.func-card__icon {
  width: 44px;
  height: 44px;
  border-radius: var(--feishu-radius-md);
  display: flex;
  align-items: center;
  justify-content: center;
  color: var(--feishu-text-primary);
  flex-shrink: 0;
}

.func-card__body {
  flex: 1;
  min-width: 0;
  display: flex;
  flex-direction: column;
  gap: 2px;
}

.func-card__title {
  font-size: 15px;
  font-weight: 600;
  color: var(--feishu-text-primary);
}

.func-card__desc {
  font-size: 12px;
  color: var(--feishu-text-tertiary);
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.func-card__arrow {
  color: var(--feishu-text-tertiary);
  flex-shrink: 0;
  transition: transform 0.18s ease;
}

.func-card:hover .func-card__arrow {
  transform: translateX(2px);
  color: var(--feishu-primary);
}
</style>
