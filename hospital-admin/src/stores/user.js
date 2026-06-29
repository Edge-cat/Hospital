/**
 * 用户 Store：登录态 + 角色信息
 *
 * role 供 permissionStore.generateRoutes 使用
 */
import { defineStore } from 'pinia'
import { ref, computed } from 'vue'

const TOKEN_KEY = 'his_token'
const USER_KEY = 'his_user'

export const useUserStore = defineStore('user', () => {
  const token = ref(localStorage.getItem(TOKEN_KEY) || '')
  const userInfo = ref(JSON.parse(localStorage.getItem(USER_KEY) || 'null'))

  const isLoggedIn = computed(() => !!token.value)
  const userName = computed(() => userInfo.value?.name || '用户')
  const role = computed(() => userInfo.value?.role || '')
  const roleLabel = computed(() => userInfo.value?.roleLabel || '')

  function login(data) {
    token.value = data.token
    userInfo.value = data.user
    localStorage.setItem(TOKEN_KEY, data.token)
    localStorage.setItem(USER_KEY, JSON.stringify(data.user))
  }

  function logout() {
    token.value = ''
    userInfo.value = null
    localStorage.removeItem(TOKEN_KEY)
    localStorage.removeItem(USER_KEY)
  }

  return { token, userInfo, isLoggedIn, userName, role, roleLabel, login, logout }
})
