import { message } from 'ant-design-vue'

const ACCESS_TOKEN_KEY = 'accessToken'
const ACCESS_TOKEN_EXPIRE_KEY = 'accessTokenExpireAt'

const isBrowser = typeof window !== 'undefined'

export const setAccessToken = (token: string, maxAgeSeconds = 30 * 60) => {
  if (!isBrowser) return

  const expiresAt = Date.now() + maxAgeSeconds * 1000
  localStorage.setItem(ACCESS_TOKEN_KEY, token)
  localStorage.setItem(ACCESS_TOKEN_EXPIRE_KEY, expiresAt.toString())

  // 兼容旧逻辑，继续写入 Cookie，方便服务端读取
  document.cookie = `${ACCESS_TOKEN_KEY}=${encodeURIComponent(token)}; path=/; max-age=${maxAgeSeconds}`
}

const readTokenFromCookie = () => {
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

export const getAccessToken = (): string | null => {
  if (!isBrowser) return null

  const token = localStorage.getItem(ACCESS_TOKEN_KEY)
  if (token) {
    return token
  }

  return readTokenFromCookie()
}

export const clearAccessToken = () => {
  if (!isBrowser) return
  localStorage.removeItem(ACCESS_TOKEN_KEY)
  localStorage.removeItem(ACCESS_TOKEN_EXPIRE_KEY)
  document.cookie = `${ACCESS_TOKEN_KEY}=; path=/; max-age=0`
}

/**
 * 统一处理登录失效跳转逻辑
 * @param msg 提示消息
 */
export const handleAuthExpired = (msg = '登录状态已过期，请重新登录') => {
  // 当前就在登录页时，直接返回，不再重定向
  if (window.location.pathname.startsWith('/user/login')) {
    return
  }
  message.warning(msg)
  clearAccessToken()

  const redirect = encodeURIComponent(window.location.href)
  window.location.href = `/user/login?redirect=${redirect}`
}
