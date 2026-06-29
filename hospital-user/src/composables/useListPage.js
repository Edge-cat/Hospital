import { ref, onMounted } from 'vue'

/** 通用列表页数据加载 */
export function useListPage(fetchFn, { immediate = true, onMountArgs = [] } = {}) {
  const loading = ref(false)
  const list = ref([])

  async function loadData(...args) {
    loading.value = true
    try {
      const res = await fetchFn(...args)
      list.value = res.data?.list ?? res.data ?? []
    } finally {
      loading.value = false
    }
  }

  if (immediate) {
    onMounted(() => loadData(...onMountArgs))
  }

  return { loading, list, loadData }
}
