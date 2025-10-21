import type { Router } from 'vue-router'

import ACCESS_ENUM, { type AccessEnum } from './accessEnum'
import checkAccess from './checkAccess'
import { useLoginUserStore } from '@/stores/loginUser'

let hasInitLoginUser = false

const ensureLoginUser = async () => {
  const loginUserStore = useLoginUserStore()

  if (!hasInitLoginUser || !loginUserStore.currentUser?.userRole) {
    try {
      await loginUserStore.fetchUser()
    } finally {
      const loginUser = loginUserStore.currentUser
      if (!loginUser || !loginUser.userRole) {
        loginUserStore.setNotLogin()
      }
      hasInitLoginUser = true
    }
  }

  return loginUserStore.currentUser
}

const setupAccessGuard = (router: Router) => {
  router.beforeEach(async (to, from, next) => {
    const loginUser = await ensureLoginUser()

    const needAccess = (to.meta?.access as AccessEnum | undefined) ?? ACCESS_ENUM.NOT_LOGIN

    if (needAccess === ACCESS_ENUM.NOT_LOGIN) {
      next()
      return
    }

    if (checkAccess(loginUser, needAccess)) {
      next()
      return
    }

    if (!loginUser || loginUser.userRole === ACCESS_ENUM.NOT_LOGIN) {
      next({
        path: '/user/login',
        query: { redirect: to.fullPath },
        replace: true,
      })
      return
    }

    next({ path: '/no-auth', replace: true })
  })
}

export default setupAccessGuard
