<template>
  <PageContainer>
    <PageHeader :title="title" :subtitle="subtitle">
      <slot name="actions" />
    </PageHeader>
    <el-card shadow="never" class="form-card" v-loading="loading">
      <slot />
    </el-card>
  </PageContainer>
</template>

<script setup>
defineProps({
  title: { type: String, required: true },
  subtitle: { type: String, default: '' },
  loading: { type: Boolean, default: false }
})
</script>
