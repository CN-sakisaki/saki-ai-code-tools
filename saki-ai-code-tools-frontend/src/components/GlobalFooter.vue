<script lang="ts" setup>
import logo from '@/assets/logo.png'

const githubUrl = 'https://github.com/CN-sakisaki'
</script>

<template>
  <a-layout-footer class="global-footer">
    <div class="global-footer__content">
      <img :src="logo" alt="SaKi酱AI代码生成工具 logo" class="global-footer__logo" />
      <span>© 2025 GitHub, Inc</span>
      <a :href="githubUrl" class="global-footer__link" rel="noopener" target="_blank">
        SaKi酱AI代码生成工具
      </a>
    </div>
  </a-layout-footer>
</template>

<style scoped>
.global-footer {
  text-align: center;
  padding: 24px 16px;
  background-color: #ffffff;
  color: #8c8c8c;
}

.global-footer__content {
  display: inline-flex;
  align-items: center;
  gap: 8px;
  flex-wrap: wrap;
  justify-content: center;
}

.global-footer__logo {
  width: 36px;
  height: 36px;
  object-fit: contain;
}

.global-footer__link {
  color: #1677ff;
}
</style>
