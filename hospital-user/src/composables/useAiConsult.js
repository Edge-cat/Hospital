import { ref } from 'vue'
import { userApi } from '@/api'
import { AI_INITIAL_QUESTION } from '@/constants/aiConsult'

export function useAiConsult() {
  const visible = ref(false)
  const record = ref(null)
  const messages = ref([])
  const loading = ref(false)
  const input = ref('')
  const disclaimer = ref('')
  const demoMode = ref(false)

  async function open(row) {
    record.value = row
    messages.value = []
    input.value = ''
    disclaimer.value = ''
    demoMode.value = false
    visible.value = true
    await send(AI_INITIAL_QUESTION)
  }

  function close() {
    visible.value = false
    record.value = null
    messages.value = []
    input.value = ''
    loading.value = false
  }

  async function send(content) {
    const text = String(content || '').trim()
    if (!text || loading.value || !record.value?.id) return

    messages.value.push({ role: 'user', content: text })
    loading.value = true
    try {
      const res = await userApi.aiConsult({
        recordId: record.value.id,
        messages: messages.value.map(({ role, content: c }) => ({ role, content: c }))
      })
      disclaimer.value = res.data?.disclaimer || ''
      demoMode.value = !!res.data?.demoMode
      messages.value.push({ role: 'assistant', content: res.data?.reply || '暂无回复' })
    } catch (e) {
      messages.value.pop()
      throw e
    } finally {
      loading.value = false
      input.value = ''
    }
  }

  return {
    visible,
    record,
    messages,
    loading,
    input,
    disclaimer,
    demoMode,
    open,
    close,
    send
  }
}
