<template>
  <el-select
    v-model="model"
    :placeholder="placeholder"
    :clearable="clearable"
    :disabled="disabled"
    :style="{ width }"
  >
    <el-option
      v-for="item in dictStore.getOptions(dictKey)"
      :key="item.value"
      :label="item.label"
      :value="item.value"
    />
  </el-select>
</template>

<script setup>
import { useDictStore } from '@/stores/dict'

defineProps({
  dictKey: { type: String, required: true },
  placeholder: { type: String, default: '请选择' },
  clearable: { type: Boolean, default: false },
  disabled: { type: Boolean, default: false },
  width: { type: String, default: '100%' }
})

const model = defineModel({ type: [String, Number], default: '' })
const dictStore = useDictStore()
</script>
