<script setup lang="ts">
import { onMounted, ref } from 'vue'
import { useRoute } from 'vue-router'

import GlobalFooter from '@/components/GlobalFooter.vue'
import GlobalHeader, { type HeaderUserProfile } from '@/components/GlobalHeader.vue'
import router from '@/router'

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

const currentUser = ref<HeaderUserProfile | null>(null)

const handleAuth = () => {
  console.info('Trigger login/register flow')
}
</script>

<template>
  <a-layout class="basic-layout">
    <GlobalHeader
      :menu-items="menuItems"
      v-model:modelValue="selectedKeys"
      :user="currentUser"
      @login="handleAuth"
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
