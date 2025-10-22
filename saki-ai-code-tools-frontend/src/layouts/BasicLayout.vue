<script lang="ts" setup>
import { computed, onMounted, ref } from 'vue'
import { useRoute } from 'vue-router'
import { storeToRefs } from 'pinia'
import { message } from 'ant-design-vue'

import ACCESS_ENUM from '@/access/accessEnum'
import GlobalFooter from '@/components/GlobalFooter.vue'
import GlobalHeader from '@/components/GlobalHeader.vue'
import router from '@/router'
import { useLoginUserStore } from '@/stores/loginUser'

// 自动生成菜单项
const menuItems = computed(() =>
  router.options.routes
    .filter((r) => r.meta?.showInMenu)
    .map((r) => ({
      key: r.name as string,
      label: r.meta?.label as string,
      path: r.path,
      access: r.meta?.access,
      hideInMenu: r.meta?.hideInMenu,
    })),
)

const route = useRoute()

// 当前选中菜单 key
const selectedKeys = ref<string[]>([])

// 刷新时同步一次当前路由
onMounted(() => {
  selectedKeys.value = [route.path]
})

// 每次路由变化自动更新选中菜单
router.afterEach((to) => {
  selectedKeys.value = [to.path]
})

const loginUserStore = useLoginUserStore()
const { currentUser } = storeToRefs(loginUserStore)

const headerUser = computed(() => {
  if (!currentUser.value || currentUser.value.userRole === ACCESS_ENUM.NOT_LOGIN) {
    return null
  }
  const name =
    currentUser.value.userName ||
    currentUser.value.userAccount ||
    currentUser.value.userEmail ||
    currentUser.value.userPhone ||
    currentUser.value.userProfile ||
    '用户'

  return {
    name,
    avatar: currentUser.value.userAvatar || undefined,
  }
})

const handleAuth = () => {
  router.push({
    path: '/user/login',
    query: { redirect: route.fullPath },
  })
}

const handleProfile = () => {
  router.push('/user/profile')
}

const handleLogout = () => {
  loginUserStore.logout()
  message.success('已注销登录')
  router.push('/user/login')
}

const hideGlobalChrome = computed(() => Boolean(route.meta?.hideLayout))
</script>

<template>
  <a-layout class="basic-layout">
    <GlobalHeader
      v-if="!hideGlobalChrome"
      v-model:modelValue="selectedKeys"
      :menu-items="menuItems"
      :current-user="currentUser"
      :user="headerUser"
      @login="handleAuth"
      @logout="handleLogout"
      @profile="handleProfile"
    />
    <a-layout-content
      :class="['basic-layout__content', { 'basic-layout__content--auth': hideGlobalChrome }]"
    >
      <RouterView />
    </a-layout-content>
    <GlobalFooter v-if="!hideGlobalChrome" />
  </a-layout>
</template>

<style scoped>
.basic-layout {
  min-height: 100vh;
  display: flex;
  flex-direction: column;
}

.basic-layout__content {
  flex: 1 1 auto;
  padding: 24px;
  background-color: #f5f5f5;
}

.basic-layout__content--auth {
  padding: 0;
  background-color: transparent;
}

@media (max-width: 768px) {
  .basic-layout__content {
    padding: 16px;
  }

  .basic-layout__content--auth {
    padding: 0;
  }
}
</style>
