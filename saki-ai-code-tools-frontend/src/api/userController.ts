import request from '@/utils/request.ts'

/** 用户根据 ID 获取用户详情 GET /user/${param0} */
export async function baseUserGetUserById(
  // 叠加生成的Param类型 (非body参数swagger默认没有生成对象)
  params: API.baseUserGetUserByIdParams,
  options?: { [key: string]: unknown },
) {
  const { id: param0, ...queryParams } = params
  return request<API.BaseResponseUserVO>(`/user/${param0}`, {
    method: 'GET',
    params: { ...queryParams },
    ...(options || {}),
  })
}

/** 管理员根据 ID 获取用户详情 GET /user/admin/${param0} */
export async function baseAdminGetUserById(
  // 叠加生成的Param类型 (非body参数swagger默认没有生成对象)
  params: API.baseAdminGetUserByIdParams,
  options?: { [key: string]: unknown },
) {
  const { id: param0, ...queryParams } = params
  return request<API.BaseResponseUser>(`/user/admin/${param0}`, {
    method: 'GET',
    params: { ...queryParams },
    ...(options || {}),
  })
}

/** 管理员创建用户 POST /user/admin/add */
export async function addUser(body: API.UserAddRequest, options?: { [key: string]: unknown }) {
  return request<API.BaseResponseLong>('/user/admin/add', {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
    },
    data: body,
    ...(options || {}),
  })
}

/** 管理员删除用户 POST /user/admin/delete */
export async function deleteUsers(
  body: API.UserDeleteRequest,
  options?: { [key: string]: unknown },
) {
  return request<API.BaseResponseBoolean>('/user/admin/delete', {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
    },
    data: body,
    ...(options || {}),
  })
}

/** 管理员分页获取用户列表 POST /user/admin/list/page */
export async function listUsersByPage(
  body: API.UserQueryRequest,
  options?: { [key: string]: unknown },
) {
  return request<API.BaseResponsePageUser>('/user/admin/list/page', {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
    },
    data: body,
    ...(options || {}),
  })
}

/** 管理员更新用户信息 PUT /user/admin/update */
export async function updateUser(
  body: API.UserUpdateRequest,
  options?: { [key: string]: unknown },
) {
  return request<API.BaseResponseBoolean>('/user/admin/update', {
    method: 'PUT',
    headers: {
      'Content-Type': 'application/json',
    },
    data: body,
    ...(options || {}),
  })
}

/** 更新个人邮箱 PUT /user/email */
export async function updateEmail(
  body: API.UserEmailUpdateRequest,
  options?: { [key: string]: unknown },
) {
  return request<API.BaseResponseBoolean>('/user/email', {
    method: 'PUT',
    headers: {
      'Content-Type': 'application/json',
    },
    data: body,
    ...(options || {}),
  })
}

/** 获取当前登录用户 GET /user/get/info */
export async function getUserInfo(options?: { [key: string]: unknown }) {
  return request<API.BaseResponseUserVO>('/user/get/info', {
    method: 'GET',
    ...(options || {}),
  })
}

/** 用户登录 POST /user/login */
export async function login(body: API.LoginRequest, options?: { [key: string]: unknown }) {
  return request<API.BaseResponseUserVO>('/user/login', {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
    },
    data: body,
    ...(options || {}),
  })
}

/** 退出登录 POST /user/logout */
export async function logout(options?: { [key: string]: unknown }) {
  return request<API.BaseResponseBoolean>('/user/logout', {
    method: 'POST',
    ...(options || {}),
  })
}

/** 更新个人手机号 PUT /user/phone */
export async function updatePhone(
  body: API.UserPhoneUpdateRequest,
  options?: { [key: string]: unknown },
) {
  return request<API.BaseResponseBoolean>('/user/phone', {
    method: 'PUT',
    headers: {
      'Content-Type': 'application/json',
    },
    data: body,
    ...(options || {}),
  })
}

/** 更新个人资料 PUT /user/profile */
export async function updateProfile(
  body: API.UserProfileUpdateRequest,
  options?: { [key: string]: unknown },
) {
  return request<API.BaseResponseBoolean>('/user/profile', {
    method: 'PUT',
    headers: {
      'Content-Type': 'application/json',
    },
    data: body,
    ...(options || {}),
  })
}

/** 用户注册 POST /user/register */
export async function register(body: API.RegisterRequest, options?: { [key: string]: unknown }) {
  return request<API.BaseResponseLong>('/user/register', {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
    },
    data: body,
    ...(options || {}),
  })
}

/** 邮箱登录验证码发送 POST /user/sendCode */
export async function sendEmailLoginCode(
  body: API.LoginRequest,
  options?: { [key: string]: unknown },
) {
  // 将 LoginRequest 转换为 SendCodeRequest 格式
  const sendCodeRequest: API.SendCodeRequest = {
    receiver: body.userEmail,
    channel: 'EMAIL',
    scene: 'LOGIN',
  }
  return request<API.BaseResponseBoolean>('/user/sendCode', {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
    },
    data: sendCodeRequest,
    ...(options || {}),
  })
}

/** 邮箱注册验证码发送 POST /user/sendCode */
export async function sendEmailRegisterCode(
  userEmail: string,
  options?: { [key: string]: unknown },
) {
  const sendCodeRequest: API.SendCodeRequest = {
    receiver: userEmail,
    channel: 'EMAIL',
    scene: 'REGISTER',
  }
  return request<API.BaseResponseBoolean>('/user/sendCode', {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
    },
    data: sendCodeRequest,
    ...(options || {}),
  })
}

/** 邮箱更新验证码发送 POST /user/sendCode */
export async function sendEmailUpdateCode(userEmail: string, options?: { [key: string]: unknown }) {
  const sendCodeRequest: API.SendCodeRequest = {
    receiver: userEmail,
    channel: 'EMAIL',
    scene: 'UPDATE_EMAIL',
  }
  return request<API.BaseResponseBoolean>('/user/sendCode', {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
    },
    data: sendCodeRequest,
    ...(options || {}),
  })
}

/** 邮箱验证码发送 POST /user/sendCode */
export async function sendCode(body: API.SendCodeRequest, options?: { [key: string]: unknown }) {
  return request<API.BaseResponseBoolean>('/user/sendCode', {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
    },
    data: body,
    ...(options || {}),
  })
}
