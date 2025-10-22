<script lang="ts" setup>
import { computed } from 'vue'
import { RouterLink } from 'vue-router'

import logo from '@/assets/logo.png'
import ACCESS_ENUM, { type AccessEnum } from '@/access/accessEnum'
import checkAccess from '@/access/checkAccess'

export interface HeaderMenuItem {
  key: string
  label: string
  path: string
  access?: AccessEnum
  hideInMenu?: boolean
}

export interface HeaderUserProfile {
  name: string
  avatar?: string
}

const props = defineProps<{
  menuItems: HeaderMenuItem[]
  modelValue?: string[] // v-model 语法糖
  user?: HeaderUserProfile | null
  currentUser?: API.UserVO | null
}>()

const emit = defineEmits<{
  (event: 'update:modelValue', val: string[]): void
  (event: 'login'): void
  (event: 'profile'): void
  (event: 'logout'): void
}>()

const visibleMenuItems = computed(() =>
  props.menuItems.filter((item) => {
    if (item.hideInMenu) {
      return false
    }
    const needAccess = item.access ?? ACCESS_ENUM.NOT_LOGIN
    return checkAccess(props.currentUser, needAccess)
  }),
)

// 当点击菜单时，自动更新 v-model
const handleSelect = (info: { key: string }) => {
  emit('update:modelValue', [info.key])
}

const handleMenuClick = ({ key }: { key: string }) => {
  if (key === 'profile') {
    emit('profile')
  } else if (key === 'logout') {
    emit('logout')
  }
}
</script>

<template>
  <a-layout-header class="global-header">
    <RouterLink class="global-header__brand" to="/">
      <img :src="logo" alt="SaKi酱AI代码生成工具 logo" class="global-header__logo" />
      <span class="global-header__title">SaKi酱AI代码生成工具</span>
    </RouterLink>

    <a-menu
      :selectedKeys="modelValue"
      class="global-header__menu"
      mode="horizontal"
      theme="light"
      @select="handleSelect"
    >
      <a-menu-item v-for="item in visibleMenuItems" :key="item.path">
        <RouterLink :to="item.path">{{ item.label }}</RouterLink>
      </a-menu-item>
    </a-menu>

    <div class="global-header__actions">
      <template v-if="user">
        <a-dropdown placement="bottomRight" trigger="['click']">
          <a-space class="global-header__profile" size="small">
            <a-avatar :src="user.avatar" size="large">
              <span v-if="!user.avatar">{{ user.name.charAt(0).toUpperCase() }}</span>
            </a-avatar>
            <span class="global-header__username">{{ user.name }}</span>
          </a-space>
          <template #overlay>
            <a-menu @click="handleMenuClick">
              <a-menu-item key="profile">个人中心</a-menu-item>
              <a-menu-divider />
              <a-menu-item key="logout">注销</a-menu-item>
            </a-menu>
          </template>
        </a-dropdown>
      </template>
      <a-button v-else type="primary" @click="$emit('login')">登录/注册</a-button>
    </div>
  </a-layout-header>
</template>

<style scoped>
.global-header {
  display: flex;
  align-items: center;
  gap: 16px;
  padding: 0 24px;
  background-color: #ffffff;
  box-shadow: 0 1px 4px rgba(0, 0, 0, 0.08);
  position: sticky;
  top: 0;
  z-index: 100;
}

.global-header__brand {
  display: inline-flex;
  align-items: center;
  gap: 12px;
  color: #1f1f1f;
  font-weight: 600;
  font-size: 18px;
  text-decoration: none;
  white-space: nowrap;
}

.global-header__title {
  color: inherit;
}

.global-header__logo {
  width: 80px;
  height: 80px;
  object-fit: contain;
}

.global-header__menu {
  flex: 1 1 auto;
  min-width: 200px;
  background: transparent;
}

.global-header__actions {
  display: flex;
  align-items: center;
  gap: 12px;
  white-space: nowrap;
}

.global-header__profile {
  display: inline-flex;
  align-items: center;
  gap: 8px;
  cursor: pointer;
}

.global-header__username {
  font-size: 14px;
  color: #262626;
}

.global-header__avatar-fallback {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 100%;
  height: 100%;
  font-weight: 600;
  color: #1677ff;
}

:deep(.ant-menu) {
  border-bottom: none !important;
  background: transparent !important;
}

:deep(.ant-menu-item) {
  font-size: 15px;
}

@media (max-width: 992px) {
  .global-header {
    flex-wrap: wrap;
    padding: 8px 16px;
  }

  .global-header__menu {
    order: 3;
    width: 100%;
  }

  .global-header__actions {
    order: 2;
    margin-left: auto;
  }
}

@media (max-width: 640px) {
  .global-header {
    flex-direction: column;
    align-items: stretch;
    gap: 12px;
  }

  .global-header__actions {
    order: 3;
    justify-content: flex-start;
  }

  .global-header__menu {
    order: 2;
  }
}
</style>
