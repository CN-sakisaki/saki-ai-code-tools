// @ts-ignore
/* eslint-disable */
import request from '@/request'

/** 用户创建应用 普通用户创建自己的应用，必须填写初始化 Prompt POST /app */
export async function createApp(body: API.AppCreateRequest, options?: { [key: string]: any }) {
  return request<API.BaseResponseLong>('/app', {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
    },
    data: body,
    ...(options || {}),
  })
}

/** 用户查看应用详情 普通用户根据 ID 查看自己应用的详情 GET /app/${param0} */
export async function getMyApp(
  // 叠加生成的Param类型 (非body参数swagger默认没有生成对象)
  params: API.getMyAppParams,
  options?: { [key: string]: any }
) {
  const { id: param0, ...queryParams } = params
  return request<API.BaseResponseAppVO>(`/app/${param0}`, {
    method: 'GET',
    params: { ...queryParams },
    ...(options || {}),
  })
}

/** 用户更新应用 普通用户根据 ID 更新自己的应用名称 PUT /app/${param0} */
export async function updateMyApp(
  // 叠加生成的Param类型 (非body参数swagger默认没有生成对象)
  params: API.updateMyAppParams,
  body: API.AppUpdateRequest,
  options?: { [key: string]: any }
) {
  const { id: param0, ...queryParams } = params
  return request<API.BaseResponseBoolean>(`/app/${param0}`, {
    method: 'PUT',
    headers: {
      'Content-Type': 'application/json',
    },
    params: { ...queryParams },
    data: body,
    ...(options || {}),
  })
}

/** 用户删除应用 普通用户根据 ID 删除自己的应用 DELETE /app/${param0} */
export async function deleteMyApp(
  // 叠加生成的Param类型 (非body参数swagger默认没有生成对象)
  params: API.deleteMyAppParams,
  options?: { [key: string]: any }
) {
  const { id: param0, ...queryParams } = params
  return request<API.BaseResponseBoolean>(`/app/${param0}`, {
    method: 'DELETE',
    params: { ...queryParams },
    ...(options || {}),
  })
}

/** 管理员查看应用详情 管理员根据 ID 查看应用详情 GET /app/admin/${param0} */
export async function adminGetAppDetail(
  // 叠加生成的Param类型 (非body参数swagger默认没有生成对象)
  params: API.adminGetAppDetailParams,
  options?: { [key: string]: any }
) {
  const { id: param0, ...queryParams } = params
  return request<API.BaseResponseApp>(`/app/admin/${param0}`, {
    method: 'GET',
    params: { ...queryParams },
    ...(options || {}),
  })
}

/** 管理员更新应用 管理员根据 ID 更新应用的名称、封面和优先级 PUT /app/admin/${param0} */
export async function adminUpdateApp(
  // 叠加生成的Param类型 (非body参数swagger默认没有生成对象)
  params: API.adminUpdateAppParams,
  body: API.AppAdminUpdateRequest,
  options?: { [key: string]: any }
) {
  const { id: param0, ...queryParams } = params
  return request<API.BaseResponseBoolean>(`/app/admin/${param0}`, {
    method: 'PUT',
    headers: {
      'Content-Type': 'application/json',
    },
    params: { ...queryParams },
    data: body,
    ...(options || {}),
  })
}

/** 管理员删除应用 管理员根据 ID 列表批量删除应用 POST /app/admin/delete */
export async function adminDeleteApps(
  body: API.AppAdminDeleteRequest,
  options?: { [key: string]: any }
) {
  return request<API.BaseResponseBoolean>('/app/admin/delete', {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
    },
    data: body,
    ...(options || {}),
  })
}

/** 管理员分页查询应用 管理员分页查询应用列表，支持除时间字段外的多条件查询 POST /app/admin/list/page */
export async function adminListApps(
  body: API.AppAdminQueryRequest,
  options?: { [key: string]: any }
) {
  return request<API.BaseResponsePageApp>('/app/admin/list/page', {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
    },
    data: body,
    ...(options || {}),
  })
}

/** 此处后端没有提供注释 GET /app/chat/gen/code */
export async function chatToGenCode(
  // 叠加生成的Param类型 (非body参数swagger默认没有生成对象)
  params: API.chatToGenCodeParams,
  options?: { [key: string]: any }
) {
  return request<API.ServerSentEventObject[]>('/app/chat/gen/code', {
    method: 'GET',
    params: {
      ...params,
    },
    ...(options || {}),
  })
}

/** 查询精选应用 公开接口，分页查询精选应用列表，支持名称模糊查询 POST /app/featured/list/page */
export async function listFeaturedApps(
  body: API.AppFeaturedListQueryRequest,
  options?: { [key: string]: any }
) {
  return request<API.BaseResponsePageAppVO>('/app/featured/list/page', {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
    },
    data: body,
    ...(options || {}),
  })
}

/** 用户分页查询应用 普通用户分页获取自己的应用列表，支持名称模糊查询 POST /app/list/page */
export async function listMyApps(
  body: API.AppMyListQueryRequest,
  options?: { [key: string]: any }
) {
  return request<API.BaseResponsePageAppVO>('/app/list/page', {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
    },
    data: body,
    ...(options || {}),
  })
}
