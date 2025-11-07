<script lang="ts" setup>
import { computed, onMounted, reactive, ref, watch } from 'vue'
import { useRouter } from 'vue-router'
import { storeToRefs } from 'pinia'
import { message, Modal } from 'ant-design-vue'

import {
  createApp,
  deleteMyApp,
  listFeaturedApps,
  listMyApps,
} from '@/api/appController'
import { useLoginUserStore } from '@/stores/loginUser'
import AppCard from '@/components/app/AppCard.vue'

const router = useRouter()
const loginUserStore = useLoginUserStore()
const { currentUser } = storeToRefs(loginUserStore)

const prompt = ref('')
const creating = ref(false)

const handleCreate = async () => {
  const value = prompt.value.trim()
  if (!value) {
    message.warning('请输入想要创建的应用描述')
    return
  }
  if (creating.value) return
  creating.value = true
  try {
    const { data } = await createApp({ initPrompt: value })
    if (data.code === 0 && data.data) {
      message.success('应用创建成功，开始生成页面')
      prompt.value = ''
      router.push({
        name: 'appChat',
        params: { id: String(data.data) },
        query: { auto: '1' },
      })
    } else {
      message.error(data.message || '创建应用失败')
    }
  } catch (error) {
    message.error('创建应用失败')
  } finally {
    creating.value = false
  }
}

const myApps = ref<API.AppVO[]>([])
const myPagination = reactive({
  current: 1,
  pageSize: 20,
  total: 0,
})
const myLoading = ref(false)
const myKeyword = ref('')

const featuredApps = ref<API.AppVO[]>([])
const featuredPagination = reactive({
  current: 1,
  pageSize: 20,
  total: 0,
})
const featuredLoading = ref(false)
const featuredKeyword = ref('')

const isLogin = computed(() => currentUser.value && currentUser.value.userRole !== 'notLogin')

const fetchMyApps = async () => {
  if (!isLogin.value) {
    myApps.value = []
    myPagination.total = 0
    return
  }
  myLoading.value = true
  try {
    const { data } = await listMyApps({
      pageNum: myPagination.current,
      pageSize: myPagination.pageSize,
      appName: myKeyword.value || undefined,
    })
    if (data.code === 0 && data.data) {
      myApps.value = data.data.records ?? []
      myPagination.total = data.data.totalRow ?? 0
    } else {
      message.error(data.message || '获取我的应用失败')
    }
  } catch (error) {
    message.error('获取我的应用失败')
  } finally {
    myLoading.value = false
  }
}

const fetchFeaturedApps = async () => {
  featuredLoading.value = true
  try {
    const { data } = await listFeaturedApps({
      pageNum: featuredPagination.current,
      pageSize: featuredPagination.pageSize,
      appName: featuredKeyword.value || undefined,
    })
    if (data.code === 0 && data.data) {
      featuredApps.value = data.data.records ?? []
      featuredPagination.total = data.data.totalRow ?? 0
    } else {
      message.error(data.message || '获取精选应用失败')
    }
  } catch (error) {
    message.error('获取精选应用失败')
  } finally {
    featuredLoading.value = false
  }
}

const handleDeleteApp = (app: API.AppVO) => {
  if (!app.id) return
  Modal.confirm({
    title: '删除应用',
    content: `确定要删除应用「${app.appName || app.id}」吗？`,
    okText: '删除',
    okButtonProps: { danger: true },
    cancelText: '取消',
    async onOk() {
      try {
        const { data } = await deleteMyApp({ id: app.id! })
        if (data.code === 0) {
          message.success('删除成功')
          fetchMyApps()
        } else {
          message.error(data.message || '删除失败')
        }
      } catch (error) {
        message.error('删除失败')
      }
    },
  })
}

const goToEdit = (app: API.AppVO) => {
  if (!app.id) return
  router.push({ name: 'appEdit', params: { id: String(app.id) } })
}

const goToChat = (app: API.AppVO) => {
  if (!app.id) return
  router.push({ name: 'appChat', params: { id: String(app.id) } })
}

const buildPreviewUrl = (app: API.AppVO) => {
  if (!app.codeGenType || !app.id) return ''
  return `http://localhost:8123/api/static/${app.codeGenType}_${app.id}/`
}

const openPreview = (app: API.AppVO) => {
  const url = buildPreviewUrl(app)
  if (url) {
    window.open(url, '_blank')
  } else {
    message.warning('暂未生成网页预览')
  }
}

const handleMyPaginationChange = (page: number, pageSize: number) => {
  myPagination.current = page
  myPagination.pageSize = pageSize
  fetchMyApps()
}

const handleFeaturedPaginationChange = (page: number, pageSize: number) => {
  featuredPagination.current = page
  featuredPagination.pageSize = pageSize
  fetchFeaturedApps()
}

watch(
  () => currentUser.value?.userRole,
  () => {
    myPagination.current = 1
    fetchMyApps()
  },
  { immediate: true },
)

onMounted(() => {
  fetchFeaturedApps()
})
</script>

<template>
  <div class="home-page">
    <section class="home-hero">
      <div class="home-hero__content">
        <h1 class="home-hero__title">一句话 · 致所想</h1>
        <p class="home-hero__subtitle">与 AI 对话，瞬间生成创意网站应用</p>
        <a-input-search
          v-model:value="prompt"
          :loading="creating"
          :enter-button="creating ? '创建中...' : '立即生成'"
          size="large"
          placeholder="使用 NoCode 创建一个炫酷的博客站点，展示分析..."
          class="home-hero__search"
          @search="handleCreate"
        />
        <div class="home-hero__tips">
          <span>支持自然语言描述，无需编码经验</span>
          <span>每次最多生成一个全新应用</span>
        </div>
      </div>
    </section>

    <section v-if="isLogin" class="home-section">
      <div class="home-section__header">
        <h2>我的应用</h2>
        <a-space>
          <a-input-search
            v-model:value="myKeyword"
            allow-clear
            placeholder="搜索我的应用"
            :loading="myLoading"
            @search="() => {
              myPagination.current = 1
              fetchMyApps()
            }"
          />
          <a-button type="primary" @click="fetchMyApps">刷新</a-button>
        </a-space>
      </div>
      <div class="home-grid">
        <a-spin :spinning="myLoading">
          <template v-if="myApps.length">
            <div class="home-grid__list">
              <AppCard
                v-for="app in myApps"
                :key="app.id"
                :app="app"
              >
                <template #actions>
                  <a-space>
                    <a-button type="link" @click="goToChat(app)">继续创作</a-button>
                    <a-button type="link" @click="goToEdit(app)">编辑</a-button>
                    <a-button type="link" @click="openPreview(app)">预览</a-button>
                    <a-button danger type="link" @click="handleDeleteApp(app)">删除</a-button>
                  </a-space>
                </template>
              </AppCard>
            </div>
          </template>
          <a-empty v-else description="还没有创建应用，尝试输入提示词生成一个吧" />
        </a-spin>
      </div>
      <div class="home-pagination" v-if="myPagination.total > myPagination.pageSize">
        <a-pagination
          :current="myPagination.current"
          :page-size="myPagination.pageSize"
          :total="myPagination.total"
          show-size-changer
          @change="handleMyPaginationChange"
          @showSizeChange="handleMyPaginationChange"
        />
      </div>
    </section>

    <section class="home-section">
      <div class="home-section__header">
        <h2>精选应用</h2>
        <a-space>
          <a-input-search
            v-model:value="featuredKeyword"
            allow-clear
            placeholder="搜索精选应用"
            :loading="featuredLoading"
            @search="() => {
              featuredPagination.current = 1
              fetchFeaturedApps()
            }"
          />
          <a-button type="primary" @click="fetchFeaturedApps">刷新</a-button>
        </a-space>
      </div>
      <div class="home-grid">
        <a-spin :spinning="featuredLoading">
          <template v-if="featuredApps.length">
            <div class="home-grid__list">
              <AppCard v-for="app in featuredApps" :key="app.id" :app="app">
                <template #actions>
                  <a-space>
                    <a-button type="link" @click="openPreview(app)">查看效果</a-button>
                  </a-space>
                </template>
              </AppCard>
            </div>
          </template>
          <a-empty v-else description="精选应用正在准备中" />
        </a-spin>
      </div>
      <div class="home-pagination" v-if="featuredPagination.total > featuredPagination.pageSize">
        <a-pagination
          :current="featuredPagination.current"
          :page-size="featuredPagination.pageSize"
          :total="featuredPagination.total"
          show-size-changer
          @change="handleFeaturedPaginationChange"
          @showSizeChange="handleFeaturedPaginationChange"
        />
      </div>
    </section>
  </div>
</template>

<style scoped>
.home-page {
  display: flex;
  flex-direction: column;
  gap: 48px;
}

.home-hero {
  position: relative;
  padding: 96px 24px 72px;
  border-radius: 24px;
  background: linear-gradient(135deg, #d2e5ff 0%, #f4e5ff 45%, #fef6f3 100%);
  overflow: hidden;
  text-align: center;
}

.home-hero__content {
  max-width: 720px;
  margin: 0 auto;
  display: flex;
  flex-direction: column;
  gap: 24px;
  align-items: center;
}

.home-hero__title {
  font-size: 44px;
  font-weight: 700;
  color: #1f1f1f;
}

.home-hero__subtitle {
  font-size: 18px;
  color: #4c4c4c;
}

.home-hero__search {
  width: 100%;
  max-width: 560px;
  box-shadow: 0 20px 60px rgba(101, 126, 255, 0.15);
}

.home-hero__tips {
  display: flex;
  gap: 16px;
  flex-wrap: wrap;
  justify-content: center;
  color: #5c5f66;
  font-size: 14px;
}

.home-section {
  background: #fff;
  border-radius: 18px;
  padding: 32px;
  box-shadow: 0 12px 36px rgba(15, 23, 42, 0.08);
  display: flex;
  flex-direction: column;
  gap: 24px;
}

.home-section__header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16px;
}

.home-section__header h2 {
  font-size: 24px;
  font-weight: 600;
  margin: 0;
}

.home-grid {
  min-height: 200px;
}

.home-grid__list {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(260px, 1fr));
  gap: 20px;
}

.home-pagination {
  display: flex;
  justify-content: flex-end;
}

@media (max-width: 992px) {
  .home-hero {
    padding: 72px 16px 56px;
  }

  .home-hero__title {
    font-size: 32px;
  }

  .home-section {
    padding: 24px 20px;
  }
}

@media (max-width: 640px) {
  .home-section__header {
    flex-direction: column;
    align-items: stretch;
  }

  .home-section__header a-space {
    width: 100%;
  }

  .home-hero__content {
    gap: 16px;
  }

  .home-hero__subtitle {
    font-size: 16px;
  }
}
</style>
