// @ts-ignore
/* eslint-disable */
import request from '@/utils/request.ts'

/** 此处后端没有提供注释 POST /static/deploy */
export async function deployApp(body: API.AppDeployRequest, options?: { [key: string]: any }) {
  return request<API.BaseResponseString>('/static/deploy', {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
    },
    data: body,
    ...(options || {}),
  })
}
