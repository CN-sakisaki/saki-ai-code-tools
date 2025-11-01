import { message } from 'ant-design-vue'

const isBrowser = typeof window !== 'undefined'

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

  const redirect = encodeURIComponent(window.location.href)
  window.location.href = `/user/login?redirect=${redirect}`
}
