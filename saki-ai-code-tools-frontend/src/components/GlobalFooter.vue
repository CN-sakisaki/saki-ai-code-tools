<script lang="ts" setup>
const githubUrl = 'https://github.com/CN-sakisaki'
</script>

<template>
  <a-layout-footer class="global-footer">
    <div class="global-footer__content">
      <svg aria-hidden="true" class="global-footer__icon" viewBox="0 0 24 24">
        <path
          d="M12 2a10 10 0 0 0-3.16 19.49c.5.09.68-.22.68-.48 0-.24-.01-.87-.01-1.71-2.78.6-3.37-1.34-3.37-1.34-.45-1.16-1.1-1.47-1.1-1.47-.9-.62.07-.61.07-.61 1 .07 1.53 1.05 1.53 1.05.89 1.53 2.34 1.09 2.91.83.09-.65.35-1.09.63-1.34-2.22-.25-4.56-1.11-4.56-4.94 0-1.09.39-1.98 1.03-2.68-.1-.25-.45-1.28.1-2.67 0 0 .84-.27 2.75 1.02A9.56 9.56 0 0 1 12 6.84c.85.01 1.71.12 2.51.35 1.9-1.29 2.74-1.02 2.74-1.02.56 1.39.21 2.42.11 2.67.64.7 1.03 1.59 1.03 2.68 0 3.84-2.34 4.68-4.57 4.93.36.31.68.91.68 1.84 0 1.33-.01 2.41-.01 2.73 0 .26.18.58.69.48A10 10 0 0 0 12 2Z"
          fill="currentColor"
        />
      </svg>
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

.global-footer__icon {
  width: 20px;
  height: 20px;
  color: #1f1f1f;
}

.global-footer__link {
  color: #1677ff;
}
</style>
