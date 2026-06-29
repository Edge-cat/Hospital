/**
 * 用户 Store：登录态 + 角色占位
 */
import { defineStore } from 'pinia'
import { ref, computed } from 'vue'

const TOKEN_KEY = 'his_user_token'
const USER_KEY = 'his_user_info'

export const useUserStore = defineStore('user', () => {
  const token = ref(localStorage.getItem(TOKEN_KEY) || localStorage.getItem('his_token') || '')
  const userInfo = ref(JSON.parse(localStorage.getItem(USER_KEY) || localStorage.getItem('his_user') || 'null'))

  const isLoggedIn = computed(() => !!token.value)
  const userName = computed(() => userInfo.value?.name || '游客')
  /** 预留：patient | vip 等，供 permissionStore 按角色过滤 */
  const role = computed(() => userInfo.value?.role || 'patient')

  function login(data) {
    token.value = data.token
    userInfo.value = data.user
    localStorage.setItem(TOKEN_KEY, data.token)
    localStorage.setItem(USER_KEY, JSON.stringify(data.user))
    localStorage.removeItem('his_token')
    localStorage.removeItem('his_user')
  }

  function logout() {
    token.value = ''
    userInfo.value = null
    localStorage.removeItem(TOKEN_KEY)
    localStorage.removeItem(USER_KEY)
    localStorage.removeItem('his_token')
    localStorage.removeItem('his_user')
  }

  function updateProfile(partial) {
    if (!userInfo.value) userInfo.value = {}
    userInfo.value = { ...userInfo.value, ...partial }
    localStorage.setItem(USER_KEY, JSON.stringify(userInfo.value))
  }

  return { token, userInfo, isLoggedIn, userName, role, login, logout, updateProfile }
})
