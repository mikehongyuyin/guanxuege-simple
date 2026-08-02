import axios from 'axios'
import { API_BASE } from '@/config'
import { useUserStore } from '@/store/user'

const request = axios.create({
  baseURL: API_BASE,
  timeout: 8000
})

// 请求拦截：注入 Bearer Token
request.interceptors.request.use((config) => {
  const userStore = useUserStore()
  if (userStore.token) {
    config.headers.Authorization = `Bearer ${userStore.token}`
  }
  return config
})

// 响应拦截：统一解包 R{code,data,msg}，401 清登录态
request.interceptors.response.use(
  (resp) => {
    const body = resp.data
    if (body && typeof body.code === 'number') {
      if (body.code === 0) return body.data
      if (body.code === 401) {
        useUserStore().logout()
        return Promise.reject(new Error(body.message || '未登录'))
      }
      return Promise.reject(new Error(body.message || '请求失败'))
    }
    return body
  },
  (err) => Promise.reject(err)
)

export default request
