<template>
  <el-row :gutter="16" class="stat-overview">
    <el-col v-for="item in items" :key="item.label" :span="colSpan">
      <el-card shadow="hover" class="stat-overview-card">
        <div class="stat-overview-card__value" :style="{ color: item.color || 'var(--feishu-text-primary)' }">
          {{ item.value ?? '-' }}
        </div>
        <div class="stat-overview-card__label">{{ item.label }}</div>
      </el-card>
    </el-col>
  </el-row>
</template>

<script setup>
defineProps({
  items: { type: Array, default: () => [] },
  colSpan: { type: Number, default: 6 }
})
</script>
