<template>
  <el-card shadow="never" class="search-bar">
    <el-form :inline="true" :model="query" class="search-form" @submit.prevent="$emit('search')">
      <slot />
      <el-form-item class="search-form__actions">
        <el-button type="primary" @click="$emit('search')">
          <el-icon><Search /></el-icon>
          查询
        </el-button>
        <el-button v-if="showReset" @click="$emit('reset')">
          <el-icon><Refresh /></el-icon>
          重置
        </el-button>
      </el-form-item>
    </el-form>
  </el-card>
</template>

<script setup>
defineProps({
  query: { type: Object, required: true },
  showReset: { type: Boolean, default: true }
})
defineEmits(['search', 'reset'])
</script>
