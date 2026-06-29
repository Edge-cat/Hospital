import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'

/**
 * 通用 CRUD 列表页逻辑
 * @param {object} api - { list, create?, update?, remove? }
 * @param {object} defaultQuery - 默认查询参数
 * @param {object} defaultForm - 默认表单
 */
export function useCrudPage(api, defaultQuery = {}, defaultForm = {}) {
  const loading = ref(false)
  const tableData = ref([])
  const total = ref(0)
  const dialogVisible = ref(false)
  const formRef = ref()
  const query = reactive({ page: 1, pageSize: 10, ...defaultQuery })
  const form = reactive({ ...defaultForm })

  async function loadData() {
    loading.value = true
    try {
      const res = await api.list(query)
      tableData.value = res.data.list
      total.value = res.data.total
    } finally {
      loading.value = false
    }
  }

  function resetQuery() {
    Object.keys(defaultQuery).forEach((key) => {
      query[key] = defaultQuery[key]
    })
    query.page = 1
    loadData()
  }

  function openDialog(row, emptyForm = defaultForm) {
    Object.assign(form, row ? { ...row } : { ...emptyForm })
    dialogVisible.value = true
  }

  async function handleSubmit(successMsg = '操作成功') {
    await formRef.value.validate()
    if (form.id && api.update) await api.update(form.id, form)
    else if (api.create) await api.create(form)
    ElMessage.success(successMsg)
    dialogVisible.value = false
    loadData()
  }

  async function handleDelete(row, message = '确定删除该记录吗？') {
    await ElMessageBox.confirm(message, '提示', { type: 'warning' })
    await api.remove(row.id)
    ElMessage.success('删除成功')
    loadData()
  }

  onMounted(loadData)

  return {
    loading,
    tableData,
    total,
    dialogVisible,
    formRef,
    query,
    form,
    loadData,
    resetQuery,
    openDialog,
    handleSubmit,
    handleDelete
  }
}
