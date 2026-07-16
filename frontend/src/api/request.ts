import axios from 'axios'
import { MessagePlugin } from 'tdesign-vue-next'
import type { ApiResult } from '@/types'

const request = axios.create({
  baseURL: import.meta.env.VITE_API_BASE_URL || '/api',
  timeout: 15000,
})

request.interceptors.request.use((config) => {
  const token = localStorage.getItem('gov_token')
  if (token) {
    config.headers.Authorization = `Bearer ${token}`
  }
  return config
})

request.interceptors.response.use(
  (response) => {
    const data = response.data as ApiResult<unknown>
    if (data.code !== 0) {
      MessagePlugin.error(data.message || '请求失败')
      return Promise.reject(new Error(data.message))
    }
    return response
  },
  (error) => {
    // 后端未启动时静默处理，由调用方走 mock 回退
    if (error.response) {
      MessagePlugin.error(`服务异常: ${error.response.status}`)
    }
    return Promise.reject(error)
  }
)

export default request
