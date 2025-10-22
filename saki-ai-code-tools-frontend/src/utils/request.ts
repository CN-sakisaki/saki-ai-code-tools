import axios, { type AxiosRequestHeaders } from 'axios'
import { message } from 'ant-design-vue'

import { clearAccessToken, getAccessToken, handleAuthExpired, setAccessToken } from '@/utils/auth'
import { refreshAccessToken } from '@/api/userController.ts'

// 创建 Axios 实例
const BIG_INT_REGEX = /([:\[,]\s*)(-?\d{16,})(?=[\s,}\]])/g

function parseJSONWithBigInt(data: string) {
  const sanitized = data.replace(
    BIG_INT_REGEX,
    (_match, prefix: string, value: string) => `${prefix}"${value}"`,
  )
  return JSON.parse(sanitized)
}

const appAxios = axios.create({
  baseURL: 'http://localhost:8123/api',
  timeout: 60000,
  withCredentials: true,
  transformResponse: [
    (data) => {
      if (typeof data !== 'string' || !data) {
        return data
      }
      try {
        return parseJSONWithBigInt(data)
      } catch {
        try {
          return JSON.parse(data)
        } catch {
          return data
        }
      }
    },
  ],
})

// 防重复刷新机制
let isRefreshing = false
let requestQueue: ((token: string) => void)[] = []

// 不需要携带 token 的接口（白名单）
const NO_AUTH_URLS = [
  '/user/login',
  '/user/register',
  '/user/login/send-email-code',
  '/user/token/refresh',
  '/user/sendEmailCode',
]

/**
 * 规范化 URL，提取请求路径（去除域名、查询参数）
 * @param url 请求地址
 *
 */
function normalizeRequestUrl(url?: string): string {
  if (!url) return ''
  if (/^https?:\/\//.test(url)) {
    try {
      return new URL(url).pathname
    } catch {
      return url
    }
  }
  return url.split('?')[0] || ''
}

/**
 * 判断请求是否需要携带 Authorization 头
 * @param url 请求地址
 */
function shouldAttachAuthHeader(url?: string): boolean {
  const normalized = normalizeRequestUrl(url)
  if (!normalized) return true
  return !NO_AUTH_URLS.some((path) => normalized.startsWith(path))
}

// 发送刷新 token 请求
async function doRefreshAccessToken(oldToken: string): Promise<string> {
  const { data } = await refreshAccessToken({ accessToken: oldToken })

  if (data.code !== 0 || !data.data) {
    throw new Error(data.message || '刷新 Token 失败')
  }

  const newToken = data.data
  clearAccessToken()
  // AccessToken 有效期为 30 分钟
  setAccessToken(newToken, 30 * 60)
  return newToken
}

// 全局请求拦截器
appAxios.interceptors.request.use(
  (config) => {
    if (shouldAttachAuthHeader(config.url)) {
      const token = getAccessToken()
      if (token) {
        const headers = (config.headers || {}) as AxiosRequestHeaders
        headers.Authorization = `Bearer ${token}`
        config.headers = headers
      }
    } else {
      const headers = (config.headers || {}) as AxiosRequestHeaders
      if ('Authorization' in headers) {
        delete headers.Authorization
        config.headers = headers
      }
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
      // 未登录
      handleAuthExpired(data.message || '未登录，请先登录')
      return Promise.reject(new Error('Not Logged In'))
    }

    if (data.code === 40106) {
      // 登录状态过期，需要刷新 Token
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

    if (data?.code === 40100) {
      handleAuthExpired(data.message || '未登录，请先登录')
      return Promise.reject(error)
    }

    // accessToken 过期
    if (
      (data?.code === 40106 || (!data?.code && error.response.status === 401)) &&
      !originalRequest._retry
    ) {
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
            const headers = (originalRequest.headers || {}) as AxiosRequestHeaders
            headers.Authorization = `Bearer ${newToken}`
            originalRequest.headers = headers
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
        const headers = (originalRequest.headers || {}) as AxiosRequestHeaders
        headers.Authorization = `Bearer ${newToken}`
        originalRequest.headers = headers
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
