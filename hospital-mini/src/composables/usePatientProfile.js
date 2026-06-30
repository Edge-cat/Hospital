import { ref } from 'vue'
import { mpApi } from '@/api'
import { checkLogin } from '@/utils/request'

const profile = ref(null)
let loadedUserId = null

export function usePatientProfile() {
  async function load(options = {}) {
    const { silent = false, force = false } = options
    if (!checkLogin()) {
      profile.value = null
      return null
    }
    const user = uni.getStorageSync('his_user')
    const userId = user ? JSON.parse(user).id : null
    if (!force && profile.value && loadedUserId === userId) {
      return profile.value
    }
    try {
      const res = await mpApi.patientInfo()
      profile.value = res.data || null
      loadedUserId = userId
      return profile.value
    } catch (e) {
      if (!silent) {
        uni.showToast({ title: e?.message || '加载档案失败', icon: 'none' })
      }
      if (user) {
        const parsed = JSON.parse(user)
        profile.value = {
          name: parsed.name,
          phone: parsed.phone,
          cardNo: parsed.cardNo
        }
      }
      return profile.value
    }
  }

  function clearProfileCache() {
    profile.value = null
    loadedUserId = null
  }

  return { profile, load, clearProfileCache }
}
