<template>
  <el-card shadow="never" class="table-card">
    <el-table v-loading="loading" :data="data" stripe v-bind="$attrs">
      <slot />
      <template #empty>
        <slot name="empty">
          <el-empty description="暂无数据" :image-size="72" />
        </slot>
      </template>
    </el-table>
    <slot name="footer" />
  </el-card>
</template>

<script setup>
defineProps({
  loading: { type: Boolean, default: false },
  data: { type: Array, default: () => [] }
})
</script>
