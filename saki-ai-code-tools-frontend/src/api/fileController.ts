// @ts-ignore
/* eslint-disable */

import request from '@/utils/request.ts'

/** 通用文件上传接口 POST /file/upload */
export async function uploadFile(
  body: FormData,
  params: { biz: string; userId?: number | string },
  options?: { [key: string]: any },
) {
  return request<API.BaseResponseString>('/file/upload', {
    method: 'POST',
    params,
    data: body,
    ...(options || {}),
  })
}
