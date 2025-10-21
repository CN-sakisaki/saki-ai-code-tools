import type { AccessEnum } from './accessEnum'
import ACCESS_ENUM from './accessEnum'

type LoginUser = API.UserVO | null | undefined

const checkAccess = (loginUser: LoginUser, needAccess: AccessEnum = ACCESS_ENUM.NOT_LOGIN) => {
  const loginUserRole = loginUser?.userRole ?? ACCESS_ENUM.NOT_LOGIN

  if (needAccess === ACCESS_ENUM.NOT_LOGIN) {
    return true
  }

  if (needAccess === ACCESS_ENUM.ADMIN) {
    return loginUserRole === ACCESS_ENUM.ADMIN
  }

  if (needAccess === ACCESS_ENUM.USER) {
    return loginUserRole === ACCESS_ENUM.USER || loginUserRole === ACCESS_ENUM.ADMIN
  }

  return false
}

export default checkAccess
