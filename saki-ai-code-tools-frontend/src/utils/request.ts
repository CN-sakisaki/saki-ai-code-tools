import axios, { type AxiosRequestHeaders } from 'axios'
import { message } from 'ant-design-vue'

import { getAccessToken, handleAuthExpired, setAccessToken } from '@/utils/auth'
import { refreshAccessToken } from '@/api/userController.ts'

// 创建 Axios 实例
const appAxios = axios.create({
  baseURL: 'http://localhost:8123/api',
  timeout: 60000,
  withCredentials: true,
})

// 防重复刷新机制
let isRefreshing = false
let requestQueue: ((token: string) => void)[] = []

async function doRefreshAccessToken(oldToken: string): Promise<string> {
  const { data } = await refreshAccessToken({ accessToken: oldToken })

  if (data.code !== 0 || !data.data) {
    throw new Error(data.message || '刷新 Token 失败')
  }

  const newToken = data.data
  // 后端 AccessToken 有效期为 360 分钟（6小时）
  setAccessToken(newToken, 6 * 60 * 60)
  return newToken
}

// 全局请求拦截器
appAxios.interceptors.request.use(
  (config) => {
    const token = getAccessToken()
    if (token) {
      const headers = (config.headers || {}) as AxiosRequestHeaders
      headers.Authorization = `Bearer ${token}`
      config.headers = headers
    }
    return config
  },
  (error) => Promise.reject(error),
)

// ==============================
// 响应拦截器：自动刷新 + 登录处理
// ==============================
appAxios.interceptors.response.use(
  (response) => {
    const { data } = response

    if (data.code === 40100) {
      // accessToken 过期
      return Promise.reject({ response })
    }

    if (data.code === 40102) {
      // 登录凭证无效
      handleAuthExpired('登录凭证无效，请重新登录')
      return Promise.reject(new Error('Token Invalid'))
    }

    return response
  },
  async (error) => {
    const originalRequest = error.config
    if (!error.response) return Promise.reject(error)

    const { data } = error.response

    // accessToken 过期
    if ((data?.code === 40100 || error.response.status === 401) && !originalRequest._retry) {
      originalRequest._retry = true
      const oldToken = getAccessToken()

      if (!oldToken) {
        handleAuthExpired('登录状态已过期，请重新登录')
        return Promise.reject(error)
      }

      // 若正在刷新，则等待队列结果
      if (isRefreshing) {
        return new Promise((resolve) => {
          requestQueue.push((newToken) => {
            originalRequest.headers.Authorization = `Bearer ${newToken}`
            resolve(appAxios(originalRequest))
          })
        })
      }

      isRefreshing = true
      try {
        const newToken = await doRefreshAccessToken(oldToken)
        message.success('已自动续期登录')

        // 重放挂起的请求
        requestQueue.forEach((cb) => cb(newToken))
        requestQueue = []

        // 重试原请求
        originalRequest.headers.Authorization = `Bearer ${newToken}`
        return appAxios(originalRequest)
      } catch (err) {
        handleAuthExpired('登录状态已过期，请重新登录')
        return Promise.reject(err)
      } finally {
        isRefreshing = false
      }
    }

    // token 无效
    if (data?.code === 40102) {
      handleAuthExpired('登录凭证无效，请重新登录')
    }

    return Promise.reject(error)
  },
)

export default appAxios
