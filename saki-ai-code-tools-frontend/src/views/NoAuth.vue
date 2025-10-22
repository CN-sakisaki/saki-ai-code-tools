<template>
  <div class="no-auth">
    <h1 class="no-auth__title">无权限访问</h1>
    <p class="no-auth__description">你没有权限访问当前页面，请联系管理员获取权限。</p>
    <RouterLink class="no-auth__action" to="/">返回首页</RouterLink>
  </div>
</template>

<script lang="ts" setup>
import { RouterLink } from 'vue-router'
</script>

<style scoped>
.no-auth {
  min-height: 60vh;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 16px;
  text-align: center;
  color: #1f1f1f;
}

.no-auth__title {
  font-size: 28px;
  font-weight: 600;
}

.no-auth__description {
  font-size: 16px;
  color: #595959;
  max-width: 480px;
}

.no-auth__action {
  margin-top: 12px;
  color: #1677ff;
}
</style>
