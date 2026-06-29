import { ref } from 'vue'
import { getReadNoticeIds, markNoticeRead } from '@/utils/noticeRead'

export function useNoticeRead() {
  const readIds = ref(getReadNoticeIds())

  function refresh() {
    readIds.value = getReadNoticeIds()
  }

  function markRead(id) {
    markNoticeRead(id)
    refresh()
  }

  function isRead(id) {
    return readIds.value.has(Number(id))
  }

  return { readIds, markRead, isRead, refresh }
}
