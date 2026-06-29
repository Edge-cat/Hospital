<template>
  <PageContainer>
    <PageHeader :title="title" :subtitle="subtitle">
      <slot name="actions" />
    </PageHeader>

    <slot name="stats" />

    <SearchToolbar
      v-if="showSearch && $slots.filters"
      :query="query"
      :show-reset="showReset"
      @search="$emit('search')"
      @reset="$emit('reset')"
    >
      <slot name="filters" />
    </SearchToolbar>

    <DataTable
      :loading="loading"
      :data="data"
      :total="total"
      :show-pagination="showPagination"
      v-model:page="query.page"
      v-model:page-size="query.pageSize"
      v-bind="tableProps"
      @page-change="$emit('page-change')"
      @row-click="(row, col, e) => $emit('row-click', row, col, e)"
      @selection-change="(rows) => $emit('selection-change', rows)"
    >
      <slot />
    </DataTable>

    <slot name="extra" />
  </PageContainer>
</template>

<script setup>
defineProps({
  title: { type: String, required: true },
  subtitle: { type: String, default: '' },
  query: { type: Object, required: true },
  loading: { type: Boolean, default: false },
  data: { type: Array, default: () => [] },
  total: { type: Number, default: 0 },
  showSearch: { type: Boolean, default: true },
  showReset: { type: Boolean, default: true },
  showPagination: { type: Boolean, default: true },
  tableProps: { type: Object, default: () => ({}) }
})

defineEmits(['search', 'reset', 'page-change', 'row-click', 'selection-change'])
</script>
