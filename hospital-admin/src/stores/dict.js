import { defineStore } from 'pinia'
import { ref } from 'vue'
import { commonApi } from '@/api'

/**
 * 公共字典 Store — 视图层通过此 Store 获取下拉选项与状态枚举，不硬编码业务数据
 */
export const useDictStore = defineStore('dict', () => {
  const loaded = ref(false)
  const loading = ref(false)
  const options = ref({})
  const enums = ref({})

  async function fetchMeta(force = false) {
    if (loaded.value && !force) return
    loading.value = true
    try {
      const res = await commonApi.meta()
      options.value = res.data.options || {}
      enums.value = res.data.enums || {}
      loaded.value = true
    } finally {
      loading.value = false
    }
  }

  /** 获取下拉选项列表 */
  function getOptions(key) {
    return options.value[key] || []
  }

  /** 获取状态枚举 Map（用于 el-tag） */
  function getEnum(key) {
    return enums.value[key] || {}
  }

  /** 根据枚举 key + value 取 label */
  function enumLabel(key, value) {
    return enums.value[key]?.[value]?.label ?? value
  }

  /** 根据枚举 key + value 取 tag type */
  function enumType(key, value) {
    return enums.value[key]?.[value]?.type ?? 'info'
  }

  function reset() {
    loaded.value = false
    options.value = {}
    enums.value = {}
  }

  return {
    loaded,
    loading,
    options,
    enums,
    fetchMeta,
    getOptions,
    getEnum,
    enumLabel,
    enumType,
    reset
  }
})
