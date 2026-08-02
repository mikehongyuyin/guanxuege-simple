import axios from 'axios'
import { ElMessage } from 'element-plus'
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
  (err) => {
    // 401：token 缺失/无效/过期（如旧的 mock token），清除登录态并回登录页
    if (err.response?.status === 401) {
      useUserStore().logout()
      if (!window.location.pathname.startsWith('/login')) {
        window.location.href = '/login'
      }
      return Promise.reject(err)
    }
    // 把后端真实的异常信息（R.fail 的 message）透出，避免只看到泛型 "status code 500"
    const serverMsg = err.response?.data?.message
    if (serverMsg) {
      ElMessage.error(serverMsg)
    } else if (err.code === 'ECONNABORTED') {
      ElMessage.error('请求超时，请确认后端服务已启动')
    } else if (err.response) {
      ElMessage.error(`请求失败（HTTP ${err.response.status}）`)
    } else {
      ElMessage.error('无法连接后端服务，请确认后端已启动（http://localhost:8080）')
    }
    return Promise.reject(err)
  }
)

export default request
