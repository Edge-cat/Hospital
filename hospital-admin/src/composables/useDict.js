/**
 * 视图层字典访问辅助（数据来自 GET /api/common/meta）
 */
import { computed } from 'vue'
import { useDictStore } from '@/stores/dict'

export function useDict() {
  const dictStore = useDictStore()

  const departments = computed(() => dictStore.getOptions('departments'))
  const roles = computed(() => dictStore.getOptions('roles'))

  return {
    dictStore,
    departments,
    roles,
    options: (key) => computed(() => dictStore.getOptions(key)),
    enums: (key) => computed(() => dictStore.getEnum(key))
  }
}
