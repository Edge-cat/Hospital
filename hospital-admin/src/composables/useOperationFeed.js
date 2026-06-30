import { ref, onMounted, onUnmounted } from 'vue'
import { ElNotification } from 'element-plus'
import { adminLogApi } from '@/api'
import { formatOperationTitle, formatOperationMessage } from '@/utils/operationText'

function shouldNotify(item) {
  if (item.client !== 'mini' && item.client !== 'user') return false
  return !!formatOperationMessage(item)
}

function notifyLog(item) {
  if (!shouldNotify(item)) return
  const message = formatOperationMessage(item)
  if (!message) return
  ElNotification({
    title: formatOperationTitle(item),
    message,
    type: 'info',
    duration: 6000,
    position: 'bottom-right'
  })
}

/** 管理端操作日志实时轮询（全局挂载于 MainLayout） */
export function useOperationFeed(options = {}) {
  const lastId = ref(0)
  const polling = ref(false)
  let timer = null
  const interval = options.interval ?? 4000

  async function poll() {
    if (polling.value) return
    polling.value = true
    try {
      const res = await adminLogApi.operationRecent({ sinceId: lastId.value, limit: 30 })
      const list = (res.data?.list || []).filter((item) => formatOperationMessage(item))
      if (!list.length) return
      lastId.value = list[list.length - 1].id
      if (options.notify !== false) {
        list.forEach((item) => notifyLog(item))
      }
      options.onNew?.(list)
    } catch {
      /* 静默 */
    } finally {
      polling.value = false
    }
  }

  async function bootstrap() {
    try {
      const res = await adminLogApi.operationRecent({ sinceId: 0, limit: 1 })
      const latest = res.data?.list?.[0]
      if (latest?.id) lastId.value = latest.id
    } catch {
      lastId.value = 0
    }
  }

  onMounted(async () => {
    await bootstrap()
    timer = setInterval(poll, interval)
  })

  onUnmounted(() => {
    if (timer) clearInterval(timer)
  })

  return { lastId, poll }
}
