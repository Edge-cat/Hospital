<template>
  <div class="feed">
    <article
      v-for="item in items"
      :key="item.id"
      class="feed__item"
      @click="$emit('select', item)"
    >
      <div class="feed__aside">
        <span class="feed__type">{{ item.type }}</span>
        <time class="feed__time">{{ formatDate(item.publishTime) }}</time>
      </div>
      <h4 class="feed__title">{{ item.title }}</h4>
    </article>
    <el-empty v-if="!items.length" description="暂无公告" :image-size="56" />
  </div>
</template>

<script setup>
defineProps({
  items: { type: Array, default: () => [] }
})

defineEmits(['select'])

function formatDate(iso) {
  if (!iso) return ''
  return String(iso).slice(0, 10)
}
</script>

<style scoped>
.feed {
  display: flex;
  flex-direction: column;
  gap: 0;
}

.feed__item {
  padding: 14px 0;
  border-bottom: 1px solid var(--feishu-border-light);
  cursor: pointer;
  transition: opacity 0.15s;
}

.feed__item:last-child {
  border-bottom: none;
}

.feed__item:hover {
  opacity: 0.85;
}

.feed__aside {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  margin-bottom: 6px;
}

.feed__type {
  font-size: 11px;
  font-weight: 600;
  letter-spacing: 0.04em;
  text-transform: uppercase;
  color: var(--feishu-primary);
  background: var(--feishu-primary-bg);
  padding: 2px 8px;
  border-radius: 4px;
}

.feed__time {
  font-size: 12px;
  color: var(--feishu-text-tertiary);
  font-variant-numeric: tabular-nums;
}

.feed__title {
  font-size: 14px;
  font-weight: 500;
  color: var(--feishu-text-primary);
  line-height: 1.5;
  margin: 0;
}
</style>
