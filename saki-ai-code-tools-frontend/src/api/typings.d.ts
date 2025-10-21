declare namespace API {
  type baseAdminGetUserByIdParams = {
    id: number
  }

  type BaseResponseBoolean = {
    code?: number
    data?: boolean
    message?: string
  }

  type BaseResponseLong = {
    code?: number
    data?: number
    message?: string
  }

  type BaseResponsePageUser = {
    code?: number
    data?: PageUser
    message?: string
  }

  type BaseResponseString = {
    code?: number
    data?: string
    message?: string
  }

  type BaseResponseUser = {
    code?: number
    data?: User
    message?: string
  }

  type BaseResponseUserVO = {
    code?: number
    data?: UserVO
    message?: string
  }

  type baseUserGetUserByIdParams = {
    id: number
  }

  type LoginRequest = {
    /** 登录类型：ACCOUNT_PASSWORD、PHONE_PASSWORD、EMAIL_CODE */
    loginType?: string
    /** 用户账号 */
    userAccount?: string
    /** 用户密码 */
    userPassword?: string
    /** 用户手机号 */
    userPhone?: string
    /** 用户邮箱 */
    userEmail?: string
    /** 登录验证码 */
    loginCode?: string
  }

  type PageUser = {
    records?: User[]
    pageNumber?: number
    pageSize?: number
    totalPage?: number
    totalRow?: number
    optimizeCountQuery?: boolean
  }

  type RegisterRequest = {
    /** 用户账号 */
    userAccount: string
    /** 用户密码 */
    userPassword: string
    /** 确认密码 */
    confirmPassword: string
    /** 邀请码 */
    inviteCode?: string
  }

  type TokenRefreshRequest = {
    /** AccessToken */
    accessToken?: string
  }

  type User = {
    /** 主键 */
    id?: number
    /** 用户账号 */
    userAccount?: string
    /** 账号密码 */
    userPassword?: string
    /** 用户名 */
    userName?: string
    /** 用户邮箱 */
    userEmail?: string
    /** 用户手机号 */
    userPhone?: string
    /** 用户头像 */
    userAvatar?: string
    /** 用户简介 */
    userProfile?: string
    /** 用户角色（user/admin） */
    userRole?: string
    /** 用户状态（0-禁用，1-正常） */
    userStatus?: number
    /** 是否为会员（0-普通会员，1-vip 会员） */
    isVip?: number
    /** vip 会员开始时间 */
    vipStartTime?: string
    /** vip 会员结束时间 */
    vipEndTime?: string
    /** 邀请码 */
    inviteCode?: string
    /** 最近一次登录的时间 */
    lastLoginTime?: string
    /** 最近一次登录的 IP 地址 */
    lastLoginIp?: string
    /** 最近一次编辑时间 */
    editTime?: string
    /** 创建时间 */
    createTime?: string
    /** 更新时间 */
    updateTime?: string
    /** 盐值 */
    userSalt?: string
    /** 逻辑删除（0-未删除，1-已删除） */
    isDelete?: number
  }

  type UserAddRequest = {
    /** 用户账号 */
    userAccount?: string
    /** 账号密码 */
    userPassword?: string
    /** 用户名 */
    userName?: string
    /** 用户邮箱 */
    userEmail?: string
    /** 用户手机号 */
    userPhone?: string
    /** 用户头像 */
    userAvatar?: string
    /** 用户简介 */
    userProfile?: string
    /** 用户角色（user/admin） */
    userRole?: string
    /** 用户状态（0-禁用，1-正常） */
    userStatus?: number
    /** 是否为会员（0-普通会员，1-vip 会员） */
    isVip?: number
  }

  type UserDeleteRequest = {
    ids?: number[]
  }

  type UserEmailGetCodeRequest = {
    /** 邮箱 */
    email?: string
  }

  type UserEmailUpdateRequest = {
    /** 主键 */
    id?: number
    /** 账号密码 */
    userPassword?: string
    /** 新邮箱地址 */
    newEmail?: string
    /** 邮箱验证码 */
    emailCode?: string
  }

  type UserPhoneUpdateRequest = {
    /** 主键 */
    id?: number
    /** 账号密码 */
    userPassword?: string
    /** 新手机号 */
    newPhone?: string
    /** 短信验证码 */
    phoneCode?: string
  }

  type UserProfileUpdateRequest = {
    /** 主键 */
    id?: number
    /** 用户名 */
    userName?: string
    /** 用户头像 */
    userAvatar?: string
    /** 用户简介 */
    userProfile?: string
    /** 最近一次编辑时间 */
    editTime?: string
  }

  type UserQueryRequest = {
    pageNum?: number
    pageSize?: number
    sortField?: string
    sortOrder?: string
    /** 用户账号 */
    userAccount?: string
    /** 用户名 */
    userName?: string
    /** 用户邮箱 */
    userEmail?: string
    /** 用户手机号 */
    userPhone?: string
    /** 用户角色（user/admin） */
    userRole?: string
    /** 用户状态（0-禁用，1-正常） */
    userStatus?: number
    /** 是否为会员（0-普通会员，1-vip 会员） */
    isVip?: number
    /** vip 会员开始时间 */
    vipStartTime?: string
    /** vip 会员结束时间 */
    vipEndTime?: string
    /** 最近一次登录的时间 */
    lastLoginTime?: string
    /** 最近一次编辑时间 */
    editTime?: string
    /** 创建时间 */
    createTime?: string
  }

  type UserUpdateRequest = {
    /** 主键 */
    id?: number
    /** 用户账号 */
    userAccount?: string
    /** 用户名 */
    userName?: string
    /** 用户邮箱 */
    userEmail?: string
    /** 用户手机号 */
    userPhone?: string
    /** 用户头像 */
    userAvatar?: string
    /** 用户简介 */
    userProfile?: string
    /** 用户角色（user/admin） */
    userRole?: string
    /** 用户状态（0-禁用，1-正常） */
    userStatus?: number
    /** 是否为会员（0-普通会员，1-vip 会员） */
    isVip?: number
    /** vip 会员开始时间 */
    vipStartTime?: string
    /** vip 会员结束时间 */
    vipEndTime?: string
  }

  type UserVO = {
    id?: number
    userAccount?: string
    userEmail?: string
    userPhone?: string
    userAvatar?: string
    userProfile?: string
    userRole?: string
    userStatus?: number
    isVip?: number
    vipStartTime?: string
    vipEndTime?: string
    inviteCode?: string
    lastLoginTime?: string
    lastLoginIp?: string
    editTime?: string
    createTime?: string
    updateTime?: string
    accessToken?: string
  }
}
