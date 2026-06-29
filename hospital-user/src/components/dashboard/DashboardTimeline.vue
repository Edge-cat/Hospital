<template>
  <div class="timeline">
    <div
      v-for="(item, index) in items"
      :key="item.id"
      class="timeline__item"
      @click="$emit('select', item)"
    >
      <div class="timeline__track">
        <span class="timeline__dot" />
        <span v-if="index < items.length - 1" class="timeline__line" />
      </div>
      <div class="timeline__content">
        <div class="timeline__head">
          <span class="timeline__name">{{ item.name }}</span>
          <ChevronRight :size="14" :stroke-width="1.75" class="timeline__chev" />
        </div>
        <p class="timeline__meta">{{ item.leader || '专业诊疗团队' }}</p>
      </div>
    </div>
    <el-empty v-if="!items.length" description="暂无科室" :image-size="56" />
  </div>
</template>

<script setup>
import { ChevronRight } from 'lucide-vue-next'

defineProps({
  items: { type: Array, default: () => [] }
})

defineEmits(['select'])
</script>

<style scoped>
.timeline {
  display: flex;
  flex-direction: column;
  gap: 0;
}

.timeline__item {
  display: flex;
  gap: 16px;
  padding: 12px 4px;
  cursor: pointer;
  border-radius: var(--feishu-radius-md);
  transition: background 0.15s;
}

.timeline__item:hover {
  background: var(--feishu-bg-sub);
}

.timeline__track {
  display: flex;
  flex-direction: column;
  align-items: center;
  width: 12px;
  flex-shrink: 0;
  padding-top: 6px;
}

.timeline__dot {
  width: 8px;
  height: 8px;
  border-radius: 50%;
  background: var(--feishu-primary);
  box-shadow: 0 0 0 3px var(--feishu-primary-bg);
}

.timeline__line {
  flex: 1;
  width: 2px;
  min-height: 24px;
  margin-top: 6px;
  background: linear-gradient(180deg, var(--feishu-border) 0%, transparent 100%);
  border-radius: 1px;
}

.timeline__content {
  flex: 1;
  min-width: 0;
  padding-bottom: 8px;
  border-bottom: 1px solid var(--feishu-border-light);
}

.timeline__item:last-child .timeline__content {
  border-bottom: none;
}

.timeline__head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 8px;
}

.timeline__name {
  font-size: 15px;
  font-weight: 600;
  color: var(--feishu-text-primary);
}

.timeline__chev {
  color: var(--feishu-text-tertiary);
  flex-shrink: 0;
}

.timeline__item:hover .timeline__chev {
  color: var(--feishu-primary);
}

.timeline__meta {
  margin-top: 4px;
  font-size: 13px;
  color: var(--feishu-text-tertiary);
}
</style>
