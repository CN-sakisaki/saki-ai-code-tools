import { message } from 'ant-design-vue'

const ACCESS_TOKEN_KEY = 'accessToken'

const isBrowser = typeof window !== 'undefined'

const buildCookieAttributes = (maxAgeSeconds: number) => {
  const segments = [`path=/`, `max-age=${maxAgeSeconds}`, 'SameSite=Lax']
  if (isBrowser && window.location.protocol === 'https:') {
    segments.push('Secure')
  }
  return segments.join('; ')
}

const readTokenFromCookie = (): string | null => {
  if (!isBrowser) return null
  const cookies = document.cookie ? document.cookie.split(';') : []
  for (const cookie of cookies) {
    const [key, ...rest] = cookie.trim().split('=')
    if (key === ACCESS_TOKEN_KEY) {
      return decodeURIComponent(rest.join('='))
    }
  }
  return null
}

export const setAccessToken = (token: string, maxAgeSeconds = 60 * 60) => {
  if (!isBrowser) return
  document.cookie = `${ACCESS_TOKEN_KEY}=${encodeURIComponent(token)}; ${buildCookieAttributes(maxAgeSeconds)}`
}

export const getAccessToken = (): string | null => readTokenFromCookie()

export const clearAccessToken = () => {
  if (!isBrowser) return
  document.cookie = `${ACCESS_TOKEN_KEY}=; ${buildCookieAttributes(0)}`
}

/**
 * 统一处理登录失效跳转逻辑
 * @param msg 提示消息
 */
export const handleAuthExpired = (msg = '登录状态已过期，请重新登录') => {
  if (!isBrowser) return
  if (window.location.pathname.startsWith('/user/login')) {
    return
  }
  message.warning(msg)
  clearAccessToken()

  const redirect = encodeURIComponent(window.location.href)
  window.location.href = `/user/login?redirect=${redirect}`
}
