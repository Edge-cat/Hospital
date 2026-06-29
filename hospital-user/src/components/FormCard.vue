<template>
  <div>
    <PageHeader :title="title" :subtitle="subtitle" />
    <el-card shadow="never" class="form-card" v-loading="loading">
      <slot />
    </el-card>
  </div>
</template>

<script setup>
defineProps({
  title: { type: String, required: true },
  subtitle: { type: String, default: '' },
  loading: { type: Boolean, default: false }
})
</script>
