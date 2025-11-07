declare namespace API {
  type adminGetAppDetailParams = {
    id: number | string
  }

  type adminUpdateAppParams = {
    id: number | string
  }

  type App = {
    /** 主键 */
    id?: number | string
    /** 应用名称 */
    appName?: string
    /** 应用封面 */
    cover?: string
    /** 应用初始化的 prompt */
    initPrompt?: string
    /** 代码生成类型（枚举） */
    codeGenType?: string
    /** 部署标识 */
    deployKey?: string
    /** 部署时间 */
    deployedTime?: string
    /** 优先级 */
    priority?: number
    /** 创建用户 ID */
    userId?: number
    /** 编辑时间 */
    editTime?: string
    /** 创建时间 */
    createTime?: string
    /** 更新时间 */
    updateTime?: string
    /** 逻辑删除（0 - 未删除，1 - 删除） */
    isDelete?: number
  }

  type AppAdminDeleteRequest = {
    ids?: (string | number)[]
  }

  type AppAdminQueryRequest = {
    pageNum?: number
    pageSize?: number
    sortField?: string
    sortOrder?: string
    /** 应用主键 */
    id?: number | string
    /** 应用名称 */
    appName?: string
    /** 应用封面 */
    cover?: string
    /** 初始化 Prompt */
    initPrompt?: string
    /** 代码生成类型 */
    codeGenType?: string
    /** 部署标识 */
    deployKey?: string
    /** 优先级 */
    priority?: number
    /** 创建用户 ID */
    userId?: number | string
  }

  type AppAdminUpdateRequest = {
    /** 应用主键 */
    id?: number | string
    /** 应用名称 */
    appName?: string
    /** 应用封面 */
    cover?: string
    /** 应用优先级 */
    priority?: number
  }

  type AppCreateRequest = {
    /** 应用名称 */
    appName?: string
    /** 应用封面链接 */
    cover?: string
    /** 初始化 Prompt，必填 */
    initPrompt?: string
    /** 代码生成类型 */
    codeGenType?: string
  }

  type AppDeployRequest = {
    /** 应用Id */
    appId?: number | string
  }

  type AppFeaturedListQueryRequest = {
    pageNum?: number
    pageSize?: number
    sortField?: string
    sortOrder?: string
    /** 应用名称模糊查询 */
    appName?: string
  }

  type AppMyListQueryRequest = {
    pageNum?: number
    pageSize?: number
    sortField?: string
    sortOrder?: string
    /** 应用名称模糊查询 */
    appName?: string
  }

  type AppUpdateRequest = {
    /** 应用主键 */
    id?: number | string
    /** 应用名称 */
    appName?: string
  }

  type AppVO = {
    /** 主键 */
    id?: number | string
    /** 应用名称 */
    appName?: string
    /** 应用封面 */
    cover?: string
    /** 应用初始化 Prompt */
    initPrompt?: string
    /** 代码生成类型 */
    codeGenType?: string
    /** 部署标识 */
    deployKey?: string
    /** 部署时间 */
    deployedTime?: string
    /** 优先级 */
    priority?: number
    /** 创建用户 ID */
    userId?: number
    /** 编辑时间 */
    editTime?: string
    /** 创建时间 */
    createTime?: string
    /** 更新时间 */
    updateTime?: string
    userVO?: UserVO
  }

  type baseAdminGetUserByIdParams = {
    id: number | string
  }

  type BaseResponseApp = {
    code?: number
    data?: App
    message?: string
  }

  type BaseResponseAppVO = {
    code?: number
    data?: AppVO
    message?: string
  }

  type BaseResponseBoolean = {
    code?: number
    data?: boolean
    message?: string
  }

  type BaseResponseFileUploadVO = {
    code?: number
    data?: FileUploadVO
    message?: string
  }

  type BaseResponseLong = {
    code?: number
    data?: number
    message?: string
  }

  type BaseResponsePageApp = {
    code?: number
    data?: PageApp
    message?: string
  }

  type BaseResponsePageAppVO = {
    code?: number
    data?: PageAppVO
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
    id: number | string
  }

  type chatToGenCodeParams = {
    appId: number | string
    message: string
  }

  type deleteMyAppParams = {
    id: number | string
  }

  type FileUploadVO = {
    /** 文件访问 URL */
    url?: string
    /** COS 对象键（路径） */
    objectKey?: string
    /** 原始文件名 */
    originalName?: string
    /** 文件大小（字节） */
    fileSize?: number
    /** 文件后缀名 */
    extension?: string
  }

  type getMyAppParams = {
    id: number | string
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

  type PageApp = {
    records?: App[]
    pageNumber?: number
    pageSize?: number
    totalPage?: number
    totalRow?: number
    optimizeCountQuery?: boolean
  }

  type PageAppVO = {
    records?: AppVO[]
    pageNumber?: number
    pageSize?: number
    totalPage?: number
    totalRow?: number
    optimizeCountQuery?: boolean
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
    /** 用户邮箱 */
    userEmail: string
    /** 邮箱验证码 */
    code: string
    /** 邀请码 */
    inviteCode?: string
  }

  type SendCodeRequest = {
    /** 邮箱或手机号 */
    receiver?: string
    /** 渠道类型 */
    channel?: string
    /** 业务场景 */
    scene?: string
  }

  type ServerSentEventObject = true

  type updateMyAppParams = {
    id: number
  }

  type uploadParams = {
    biz: string
    userId?: number | string
  }

  type User = {
    /** 主键 */
    id?: number | string
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
    ids?: (number | string)[]
  }

  type UserEmailGetCodeRequest = {
    /** 邮箱 */
    email?: string
  }

  type UserEmailUpdateRequest = {
    /** 主键 */
    id?: number | string
    /** 账号密码 */
    userPassword?: string
    /** 新邮箱地址 */
    newEmail?: string
    /** 邮箱验证码 */
    emailCode?: string
  }

  type UserPhoneUpdateRequest = {
    /** 主键 */
    id?: number | string
    /** 账号密码 */
    userPassword?: string
    /** 新手机号 */
    newPhone?: string
    /** 短信验证码 */
    phoneCode?: string
  }

  type UserProfileUpdateRequest = {
    /** 主键 */
    id?: number | string
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
    id?: number | string
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
    id?: number | string
    userAccount?: string
    userName?: string
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
  }
}
