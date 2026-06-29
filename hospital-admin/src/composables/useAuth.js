/**
 * 组件内权限判断（可选）
 *
 * @example
 * const { role, canAccess } = useAuth()
 * if (canAccess(['admin'])) { ... }
 */
import { computed } from 'vue'
import { useUserStore } from '@/stores/user'

export function useAuth() {
  const userStore = useUserStore()

  const role = computed(() => userStore.role)
  const isLoggedIn = computed(() => userStore.isLoggedIn)

  function canAccess(roles) {
    if (!roles?.length) return true
    return roles.includes(userStore.role)
  }

  return { role, isLoggedIn, canAccess }
}
