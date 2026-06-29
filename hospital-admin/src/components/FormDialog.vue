<template>
  <el-dialog
    v-model="visible"
    :title="title"
    :width="width"
    destroy-on-close
    align-center
  >
    <slot />
    <template #footer>
      <el-button @click="visible = false">取消</el-button>
      <el-button type="primary" :loading="confirmLoading" @click="$emit('confirm')">
        {{ confirmText }}
      </el-button>
    </template>
  </el-dialog>
</template>

<script setup>
defineProps({
  title: { type: String, default: '' },
  width: { type: String, default: '520px' },
  confirmText: { type: String, default: '确定' },
  confirmLoading: { type: Boolean, default: false }
})
defineEmits(['confirm'])

const visible = defineModel('visible', { type: Boolean, default: false })
</script>
