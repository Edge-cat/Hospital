<template>
  <TableCard :loading="loading" :data="data" v-bind="$attrs" @row-click="(...args) => $emit('row-click', ...args)" @selection-change="(rows) => $emit('selection-change', rows)">
    <slot />
    <template v-if="showPagination" #footer>
      <AppPagination
        v-model:page="page"
        v-model:page-size="pageSize"
        :total="total"
        @change="$emit('page-change')"
      />
    </template>
  </TableCard>
</template>

<script setup>
defineProps({
  loading: { type: Boolean, default: false },
  data: { type: Array, default: () => [] },
  total: { type: Number, default: 0 },
  showPagination: { type: Boolean, default: true }
})
defineEmits(['page-change', 'row-click', 'selection-change'])

const page = defineModel('page', { type: Number, default: 1 })
const pageSize = defineModel('pageSize', { type: Number, default: 10 })
</script>
