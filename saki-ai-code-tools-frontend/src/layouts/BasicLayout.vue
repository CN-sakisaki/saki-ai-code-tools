<script lang="ts" setup>
import { computed, onMounted, ref } from 'vue'
import { useRoute } from 'vue-router'
import { storeToRefs } from 'pinia'
import { message } from 'ant-design-vue'

import GlobalFooter from '@/components/GlobalFooter.vue'
import GlobalHeader from '@/components/GlobalHeader.vue'
import router from '@/router'
import { useLoginUserStore } from '@/stores/loginUser'

// 自动生成菜单项
const menuItems = router.options.routes
  .filter((r) => r.meta?.showInMenu)
  .map((r) => ({
    key: r.name as string,
    label: r.meta?.label as string,
    path: r.path,
  }))

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
  if (!currentUser.value) {
    return null
  }
  const name =
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
  router.push('/user/userManage')
}

const handleLogout = () => {
  loginUserStore.logout()
  message.success('已注销登录')
  router.push('/user/login')
}
</script>

<template>
  <a-layout class="basic-layout">
    <GlobalHeader
      v-model:modelValue="selectedKeys"
      :menu-items="menuItems"
      :user="headerUser"
      @login="handleAuth"
      @logout="handleLogout"
      @profile="handleProfile"
    />
    <a-layout-content class="basic-layout__content">
      <RouterView />
    </a-layout-content>
    <GlobalFooter />
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

@media (max-width: 768px) {
  .basic-layout__content {
    padding: 16px;
  }
}
</style>
