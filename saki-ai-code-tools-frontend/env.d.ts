/// <reference types="vite/client" />

import type { AccessEnum } from '@/access/accessEnum'

declare module 'vue-router' {
  interface RouteMeta {
    label?: string
    showInMenu?: boolean
    hideInMenu?: boolean
    hideLayout?: boolean
    access?: AccessEnum
  }
}
