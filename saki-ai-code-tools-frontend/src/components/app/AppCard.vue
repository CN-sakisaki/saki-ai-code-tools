<script lang="ts" setup>
import { computed } from 'vue'

const props = defineProps<{ app: API.AppVO }>()

const coverUrl = computed(() => props.app.cover || '')
const subtitle = computed(() => props.app.initPrompt || 'AI 生成应用')
</script>

<template>
  <a-card class="app-card" hoverable>
    <template #cover>
      <div class="app-card__cover">
        <img v-if="coverUrl" :src="coverUrl" alt="应用封面" />
        <div v-else class="app-card__cover-placeholder">
          <span>{{ app.appName?.charAt(0)?.toUpperCase() || 'A' }}</span>
        </div>
      </div>
    </template>
    <a-card-meta :title="app.appName || '未命名应用'">
      <template #description>
        <p class="app-card__desc">{{ subtitle }}</p>
        <div class="app-card__meta">
          <span v-if="app.priority !== undefined">优先级：{{ app.priority }}</span>
          <span v-if="app.deployedTime">已部署</span>
        </div>
      </template>
    </a-card-meta>
    <div class="app-card__actions">
      <slot name="actions" />
    </div>
  </a-card>
</template>

<style scoped>
.app-card {
  height: 100%;
  display: flex;
  flex-direction: column;
}

:deep(.ant-card-body) {
  flex: 1 1 auto;
  display: flex;
  flex-direction: column;
  padding: 16px;
}

.app-card__cover {
  position: relative;
  width: 100%;
  height: 160px;
  overflow: hidden;
  border-radius: 16px 16px 0 0;
  background: linear-gradient(135deg, #e0f2ff 0%, #f3e5ff 100%);
  display: flex;
  align-items: center;
  justify-content: center;
}

.app-card__cover img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.app-card__cover-placeholder {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 100%;
  height: 100%;
  font-size: 48px;
  font-weight: 700;
  color: rgba(255, 255, 255, 0.85);
  text-shadow: 0 10px 30px rgba(15, 23, 42, 0.25);
}

.app-card__desc {
  margin: 12px 0 8px;
  font-size: 13px;
  color: #5c5f66;
  min-height: 40px;
  white-space: pre-wrap;
}

.app-card__meta {
  display: flex;
  gap: 12px;
  font-size: 12px;
  color: #8b8d97;
}

.app-card__actions {
  margin-top: auto;
  padding-top: 12px;
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}
</style>
