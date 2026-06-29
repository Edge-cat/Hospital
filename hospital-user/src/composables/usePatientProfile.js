import { ref } from 'vue'
import { userApi } from '@/api'
import { useUserStore } from '@/stores/user'
import { handleApiError } from '@/utils/apiError'

const profileCache = ref(null)

/** 患者档案 — 个人中心 / 预约页 / 侧栏共用 */
export function usePatientProfile() {
  const loading = ref(false)
  const error = ref(null)
  const userStore = useUserStore()

  async function load(options = {}) {
    const { force = false, silent = false } = options
    if (profileCache.value && !force) return profileCache.value

    loading.value = true
    error.value = null
    try {
      const res = await userApi.patientInfo()
      profileCache.value = res.data
      if (res.data) userStore.updateProfile(res.data)
      return res.data
    } catch (e) {
      const body = handleApiError(e, { silent, fallback: '加载患者档案失败' })
      error.value = body.message
      if (userStore.userInfo) {
        profileCache.value = {
          name: userStore.userName,
          phone: userStore.userInfo.phone,
          cardNo: userStore.userInfo.cardNo
        }
      }
      return profileCache.value
    } finally {
      loading.value = false
    }
  }

  function refresh() {
    return load({ force: true })
  }

  function clearCache() {
    profileCache.value = null
  }

  return {
    profile: profileCache,
    loading,
    error,
    load,
    refresh,
    clearCache
  }
}
