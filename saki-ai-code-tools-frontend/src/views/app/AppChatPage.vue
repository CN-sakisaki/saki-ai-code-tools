<script lang="ts" setup>
import { computed, nextTick, onBeforeUnmount, ref, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { storeToRefs } from 'pinia'
import { message } from 'ant-design-vue'

import { adminGetAppDetail, getMyApp } from '@/api/appController'
import { deployApp } from '@/api/staticResourceController'
import ACCESS_ENUM from '@/access/accessEnum'
import { useLoginUserStore } from '@/stores/loginUser'

interface ChatMessage {
  id: string
  role: 'user' | 'assistant'
  content: string
  timestamp: string
  streaming?: boolean
}

const route = useRoute()
const router = useRouter()
const loginUserStore = useLoginUserStore()
const { currentUser } = storeToRefs(loginUserStore)

const appId = computed(() => route.params.id as string)
const isAdmin = computed(() => currentUser.value?.userRole === ACCESS_ENUM.ADMIN)

const appDetail = ref<API.AppVO | API.App | null>(null)
const loadingDetail = ref(false)
const generationLoading = ref(false)
const deployLoading = ref(false)
const previewKey = ref(Date.now())
const hasGenerated = ref(false)

const messageList = ref<ChatMessage[]>([])
const inputValue = ref('')
const messageListRef = ref<HTMLElement>()
let eventSource: EventSource | null = null

const previewUrl = computed(() => {
  if (!appDetail.value?.id || !appDetail.value?.codeGenType) {
    return ''
  }
  return `http://localhost:8123/api/static/${appDetail.value.codeGenType}_${appDetail.value.id}/index.html?ts=${previewKey.value}`
})

const formattedTitle = computed(() => appDetail.value?.appName || '未命名应用')

const pushMessage = (msg: ChatMessage) => {
  messageList.value.push(msg)
  nextTick(() => {
    if (messageListRef.value) {
      messageListRef.value.scrollTop = messageListRef.value.scrollHeight
    }
  })
}

const formatTime = () =>
  new Intl.DateTimeFormat('zh-CN', {
    hour: '2-digit',
    minute: '2-digit',
  }).format(new Date())

const closeStream = () => {
  if (eventSource) {
    eventSource.close()
    eventSource = null
  }
}

const finishStreaming = (assistantMsg: ChatMessage, isError = false) => {
  closeStream()
  generationLoading.value = false
  assistantMsg.streaming = false
  if (!isError) {
    previewKey.value = Date.now()
  }
}

const startStreaming = (userMessage: string) => {
  if (!appId.value) return
  closeStream()

  const userMsg: ChatMessage = {
    id: `${Date.now()}-user`,
    role: 'user',
    content: userMessage,
    timestamp: formatTime(),
  }
  pushMessage(userMsg)

  const assistantMsg: ChatMessage = {
    id: `${Date.now()}-assistant`,
    role: 'assistant',
    content: '',
    timestamp: formatTime(),
    streaming: true,
  }
  pushMessage(assistantMsg)

  generationLoading.value = true
  hasGenerated.value = true

  const baseUrl = 'http://localhost:8123/api/app/chat/gen/code'
  const params = new URLSearchParams({ appId: appId.value, message: userMessage })
  const url = `${baseUrl}?${params.toString()}`

  eventSource = new EventSource(url, { withCredentials: true })

  eventSource.onmessage = (evt) => {
    let chunk = evt.data
    if (chunk && chunk !== '[DONE]') {
      try {
        const parsed = JSON.parse(chunk)
        if (parsed?.d !== undefined) {
          chunk = parsed.d
        }
      } catch (error) {
        // ignore json parse error
      }
      assistantMsg.content += chunk
    }
  }

  eventSource.addEventListener('done', () => {
    finishStreaming(assistantMsg)
  })

  eventSource.onerror = () => {
    finishStreaming(assistantMsg, true)
    message.error('生成过程中出现问题，请稍后重试')
  }
}

const sendMessage = () => {
  const value = inputValue.value.trim()
  if (!value) {
    message.warning('请输入对话内容')
    return
  }
  if (generationLoading.value) {
    message.info('正在生成中，请稍候...')
    return
  }
  inputValue.value = ''
  startStreaming(value)
}

const triggerInitialGeneration = () => {
  if (!appDetail.value?.initPrompt) return
  if (route.query.auto === '1') {
    startStreaming(appDetail.value.initPrompt)
    router.replace({
      name: route.name || 'appChat',
      params: route.params,
    })
  }
}

const fetchDetail = async () => {
  if (!appId.value) return
  loadingDetail.value = true
  try {
    if (isAdmin.value) {
      const { data } = await adminGetAppDetail({ id: appId.value })
      if (data.code === 0 && data.data) {
        appDetail.value = data.data as API.App
      } else {
        message.error(data.message || '获取应用信息失败')
      }
    } else {
      const { data } = await getMyApp({ id: appId.value })
      if (data.code === 0 && data.data) {
        appDetail.value = data.data
      } else {
        message.error(data.message || '获取应用信息失败')
      }
    }
    if (appDetail.value?.codeGenType && appDetail.value?.id) {
      hasGenerated.value = true
      previewKey.value = Date.now()
    }
  } catch (error) {
    message.error('获取应用信息失败')
  } finally {
    loadingDetail.value = false
    triggerInitialGeneration()
  }
}

const handleDeploy = async () => {
  if (!appId.value) return
  deployLoading.value = true
  try {
    const { data } = await deployApp({ appId: appId.value })
    if (data.code === 0 && data.data) {
      message.success('部署成功，正在为您打开访问地址')
      window.open(data.data, '_blank')
    } else {
      message.error(data.message || '部署失败')
    }
  } catch (error) {
    message.error('部署失败')
  } finally {
    deployLoading.value = false
  }
}

watch(
  () => currentUser.value?.userRole,
  () => {
    fetchDetail()
  },
  { immediate: true },
)

onBeforeUnmount(() => {
  closeStream()
})
</script>

<template>
  <div class="app-chat">
    <header class="app-chat__header">
      <div>
        <h1>{{ formattedTitle }}</h1>
        <p class="app-chat__subtitle">与 AI 对话生成网页，实时预览效果</p>
      </div>
      <a-space>
        <a-button type="default" @click="fetchDetail">刷新信息</a-button>
        <a-button type="primary" :loading="deployLoading" @click="handleDeploy">部署应用</a-button>
      </a-space>
    </header>

    <section class="app-chat__body">
      <div class="app-chat__conversation">
        <a-spin :spinning="loadingDetail">
          <div ref="messageListRef" class="app-chat__messages">
            <template v-if="messageList.length">
              <div
                v-for="item in messageList"
                :key="item.id"
                class="app-chat__message"
                :class="[`app-chat__message--${item.role}`]"
              >
                <div class="app-chat__bubble">
                  <p class="app-chat__text">{{ item.content || (item.streaming ? '内容生成中...' : '') }}</p>
                  <span class="app-chat__time">{{ item.timestamp }}</span>
                </div>
              </div>
            </template>
            <div v-else class="app-chat__empty">
              <p>向 AI 描述您想要的网站，系统将自动为您生成。</p>
              <p v-if="appDetail?.initPrompt" class="app-chat__empty-tip">
                初始提示词：{{ appDetail.initPrompt }}
              </p>
            </div>
          </div>
        </a-spin>
        <div class="app-chat__input">
          <a-textarea
            v-model:value="inputValue"
            :auto-size="{ minRows: 3, maxRows: 6 }"
            placeholder="例如：帮我生成一个介绍我摄影作品的单页网站，包含作品集和联系方式"
            :disabled="generationLoading"
          />
          <div class="app-chat__input-actions">
            <a-button type="primary" :loading="generationLoading" @click="sendMessage">发送</a-button>
            <span class="app-chat__input-hint">生成过程中请耐心等待，完成后将自动刷新预览</span>
          </div>
        </div>
      </div>

      <div class="app-chat__preview">
        <h2>网页预览</h2>
        <div class="app-chat__preview-frame">
          <template v-if="hasGenerated">
            <iframe
              v-if="previewUrl"
              :key="previewKey"
              :src="previewUrl"
              frameborder="0"
              sandbox="allow-same-origin allow-scripts allow-forms allow-pointer-lock"
            ></iframe>
            <a-empty v-else description="暂无预览地址" />
          </template>
          <div v-else class="app-chat__preview-placeholder">
            <p>AI 完成生成后将在此展示网页效果</p>
          </div>
        </div>
      </div>
    </section>
  </div>
</template>

<style scoped>
.app-chat {
  display: flex;
  flex-direction: column;
  gap: 24px;
  height: 100%;
}

.app-chat__header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16px;
  padding: 24px;
  border-radius: 20px;
  background: linear-gradient(120deg, rgba(99, 102, 241, 0.12), rgba(236, 72, 153, 0.12));
}

.app-chat__header h1 {
  font-size: 24px;
  font-weight: 600;
  margin-bottom: 4px;
}

.app-chat__subtitle {
  margin: 0;
  color: #5b6070;
}

.app-chat__body {
  display: grid;
  grid-template-columns: 1.1fr 0.9fr;
  gap: 24px;
  align-items: stretch;
}

.app-chat__conversation {
  background: #fff;
  border-radius: 20px;
  padding: 24px;
  display: flex;
  flex-direction: column;
  gap: 16px;
  box-shadow: 0 10px 30px rgba(15, 23, 42, 0.08);
}

.app-chat__messages {
  flex: 1 1 auto;
  overflow-y: auto;
  display: flex;
  flex-direction: column;
  gap: 12px;
  max-height: 480px;
}

.app-chat__message {
  display: flex;
}

.app-chat__message--user {
  justify-content: flex-end;
}

.app-chat__message--assistant {
  justify-content: flex-start;
}

.app-chat__bubble {
  max-width: 80%;
  background: #f0f4ff;
  padding: 12px 16px;
  border-radius: 16px;
  position: relative;
  display: flex;
  flex-direction: column;
  gap: 6px;
}

.app-chat__message--user .app-chat__bubble {
  background: #1677ff;
  color: #fff;
  border-bottom-right-radius: 4px;
}

.app-chat__message--assistant .app-chat__bubble {
  background: #f4f5f9;
  color: #1f2937;
  border-bottom-left-radius: 4px;
}

.app-chat__text {
  margin: 0;
  white-space: pre-wrap;
  word-break: break-word;
}

.app-chat__time {
  font-size: 12px;
  color: rgba(255, 255, 255, 0.8);
  align-self: flex-end;
}

.app-chat__message--assistant .app-chat__time {
  color: #8f95a3;
}

.app-chat__empty {
  min-height: 220px;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 8px;
  color: #6b7280;
  text-align: center;
  padding: 24px;
}

.app-chat__empty-tip {
  background: rgba(99, 102, 241, 0.08);
  padding: 12px 18px;
  border-radius: 12px;
  color: #4f46e5;
}

.app-chat__input {
  border-top: 1px solid #f0f0f0;
  padding-top: 16px;
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.app-chat__input-actions {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
}

.app-chat__input-hint {
  color: #8f95a3;
  font-size: 13px;
}

.app-chat__preview {
  background: #fff;
  border-radius: 20px;
  padding: 24px;
  display: flex;
  flex-direction: column;
  gap: 16px;
  box-shadow: 0 10px 30px rgba(15, 23, 42, 0.08);
}

.app-chat__preview h2 {
  margin: 0;
  font-size: 20px;
  font-weight: 600;
}

.app-chat__preview-frame {
  flex: 1 1 auto;
  border-radius: 16px;
  background: #f9fafb;
  overflow: hidden;
  min-height: 360px;
  position: relative;
}

.app-chat__preview-frame iframe {
  width: 100%;
  height: 100%;
  border: none;
}

.app-chat__preview-placeholder {
  height: 100%;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  color: #9ca3af;
}

@media (max-width: 1200px) {
  .app-chat__body {
    grid-template-columns: 1fr;
  }

  .app-chat__messages {
    max-height: 320px;
  }
}

@media (max-width: 640px) {
  .app-chat__header {
    flex-direction: column;
    align-items: flex-start;
  }

  .app-chat__input-actions {
    flex-direction: column;
    align-items: stretch;
  }
}
</style>
