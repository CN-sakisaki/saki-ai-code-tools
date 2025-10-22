import { message } from 'ant-design-vue'

const ACCESS_TOKEN_KEY = 'accessToken'

export const setAccessToken = (token: string, maxAgeSeconds = 60 * 60) => {
  document.cookie = `${ACCESS_TOKEN_KEY}=${encodeURIComponent(token)}; path=/; max-age=${maxAgeSeconds}`
}

export const getAccessToken = (): string | null => {
  const cookies = document.cookie ? document.cookie.split(';') : []
  for (const cookie of cookies) {
    const [key, ...rest] = cookie.trim().split('=')
    if (key === ACCESS_TOKEN_KEY) {
      return decodeURIComponent(rest.join('='))
    }
  }
  return null
}

export const clearAccessToken = () => {
  document.cookie = `${ACCESS_TOKEN_KEY}=; path=/; max-age=0`
}

/**
 * 统一处理登录失效跳转逻辑
 * @param msg 提示消息
 */
export const handleAuthExpired = (msg = '登录状态已过期，请重新登录') => {
  message.warning(msg)
  clearAccessToken()
  const redirect = encodeURIComponent(window.location.href)
  window.location.href = `/user/login?redirect=${redirect}`
}
