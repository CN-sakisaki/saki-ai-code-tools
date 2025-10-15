<script setup lang="ts">
import { computed, ref } from 'vue'
import { RouterView, useRoute } from 'vue-router'

import GlobalFooter from '@/components/GlobalFooter.vue'
import GlobalHeader, {
  type HeaderMenuItem,
  type HeaderUserProfile,
} from '@/components/GlobalHeader.vue'

const menuItems: HeaderMenuItem[] = [
  { key: 'home', label: '首页', path: '/' },
  { key: 'about', label: '关于我们', path: '/about' },
]

const route = useRoute()

const selectedKeys = computed<string[]>(() => {
  const active = menuItems.find((item) => {
    if (item.path === '/') {
      return route.path === item.path
    }

    return route.path === item.path || route.path.startsWith(`${item.path}/`)
  })
  return active ? [active.key] : []
})

const currentUser = ref<HeaderUserProfile | null>(null)

const handleAuth = () => {
  // TODO: integrate with real authentication flow
  console.info('Trigger login/register flow')
}
</script>

<template>
  <a-layout class="basic-layout">
    <GlobalHeader
      :menu-items="menuItems"
      :selected-keys="selectedKeys"
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
