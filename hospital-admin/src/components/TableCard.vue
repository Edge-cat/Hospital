<template>
  <el-card shadow="never" class="table-card">
    <el-table
      v-loading="loading"
      :data="data"
      stripe
      border
      v-bind="$attrs"
      @row-click="(row, column, e) => $emit('row-click', row, column, e)"
      @selection-change="(rows) => $emit('selection-change', rows)"
    >
      <slot />
      <template #empty>
        <el-empty description="暂无数据" :image-size="72" />
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
defineEmits(['row-click', 'selection-change'])
</script>
