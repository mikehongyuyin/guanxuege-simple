import { defineStore } from 'pinia'
import { ref } from 'vue'

// 用户登录态：token 持久化到 localStorage
export const useUserStore = defineStore('user', () => {
  const token = ref(localStorage.getItem('gx_token') || '')
  const user = ref(JSON.parse(localStorage.getItem('gx_user') || 'null'))

  function setAuth(t, u) {
    token.value = t
    user.value = u
    localStorage.setItem('gx_token', t)
    localStorage.setItem('gx_user', JSON.stringify(u))
  }

  function logout() {
    token.value = ''
    user.value = null
    localStorage.removeItem('gx_token')
    localStorage.removeItem('gx_user')
  }

  return { token, user, setAuth, logout }
})
