import axios from 'axios'
import { handleAuthExpired } from '@/utils/auth'

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

appAxios.interceptors.response.use(
  (response) => {
    const { data } = response

    if (data?.code === 40100) {
      handleAuthExpired(data.message || '未登录，请先登录')
      return Promise.reject(new Error('Not Logged In'))
    }

    if (data?.code === 40102) {
      handleAuthExpired('登录凭证无效，请重新登录')
      return Promise.reject(new Error('Token Invalid'))
    }

    if (data?.code === 40106) {
      handleAuthExpired('登录状态已过期，请重新登录')
      return Promise.reject(new Error('Session Expired'))
    }

    return response
  },
  (error) => {
    if (!error.response) return Promise.reject(error)

    const { data, status } = error.response

    if (data?.code === 40100 || data?.code === 40102 || data?.code === 40106 || status === 401) {
      handleAuthExpired(data?.message || '登录状态已过期，请重新登录')
    }

    return Promise.reject(error)
  },
)

export default appAxios
