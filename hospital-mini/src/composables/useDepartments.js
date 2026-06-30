import { ref } from 'vue'
import { mpApi } from '@/api'
import { mapDeptList } from '@/utils/deptIcon'

/** 小程序科室列表 — 首页 / 科室页 / 就诊页共用 */
export function useDepartments() {
  const departments = ref([])
  const loading = ref(false)
  const error = ref(null)

  async function load() {
    loading.value = true
    error.value = null
    try {
      const res = await mpApi.departments()
      departments.value = mapDeptList(res.data?.list || res.data || [])
      return departments.value
    } catch (e) {
      error.value = e?.message || e?.data?.message || '科室加载失败，请稍后重试'
      departments.value = []
      return []
    } finally {
      loading.value = false
    }
  }

  function findById(id) {
    return departments.value.find((d) => d.id === Number(id))
  }

  function findByName(name) {
    return departments.value.find((d) => d.name === name)
  }

  return {
    departments,
    loading,
    error,
    load,
    refresh: load,
    findById,
    findByName
  }
}
