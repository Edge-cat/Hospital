<template>
  <el-tag v-if="enumKey" :type="dictStore.enumType(enumKey, value)" :size="size">
    {{ dictStore.enumLabel(enumKey, value) }}
  </el-tag>
  <el-tag v-else-if="enable" :type="Number(value) === 1 ? 'success' : 'info'" :size="size">
    {{ Number(value) === 1 ? '启用' : '禁用' }}
  </el-tag>
  <el-tag v-else :type="type" :size="size">
    <slot>{{ label }}</slot>
  </el-tag>
</template>

<script setup>
import { useDictStore } from '@/stores/dict'

defineProps({
  /** 字典枚举 key，如 registerStatus */
  enumKey: { type: String, default: '' },
  value: { type: [String, Number], default: '' },
  /** 启用/禁用二元状态 */
  enable: { type: Boolean, default: false },
  type: { type: String, default: 'info' },
  label: { type: String, default: '' },
  size: { type: String, default: 'default' }
})

const dictStore = useDictStore()
</script>
