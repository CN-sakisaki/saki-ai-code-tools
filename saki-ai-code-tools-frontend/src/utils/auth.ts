const ACCESS_TOKEN_KEY = 'accessToken'

const buildCookie = (key: string, value: string, maxAgeSeconds?: number) => {
  const encodedValue = encodeURIComponent(value)
  let cookie = `${key}=${encodedValue}; path=/`
  if (maxAgeSeconds && Number.isFinite(maxAgeSeconds)) {
    cookie += `; max-age=${Math.max(0, Math.floor(maxAgeSeconds))}`
  }
  document.cookie = cookie
}

export const setAccessToken = (token: string, maxAgeSeconds = 7 * 24 * 60 * 60) => {
  buildCookie(ACCESS_TOKEN_KEY, token, maxAgeSeconds)
}

export const getAccessToken = (): string | null => {
  const cookies = document.cookie ? document.cookie.split(';') : []
  for (const cookie of cookies) {
    const [rawKey, ...rest] = cookie.trim().split('=')
    if (rawKey === ACCESS_TOKEN_KEY) {
      return decodeURIComponent(rest.join('='))
    }
  }
  return null
}

export const clearAccessToken = () => {
  document.cookie = `${ACCESS_TOKEN_KEY}=; path=/; max-age=0`
}

export const hasAccessToken = () => Boolean(getAccessToken())

export default {
  setAccessToken,
  getAccessToken,
  clearAccessToken,
  hasAccessToken,
}
