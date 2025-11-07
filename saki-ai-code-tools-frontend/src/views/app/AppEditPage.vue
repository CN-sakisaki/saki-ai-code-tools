<script lang="ts" setup>
import { computed, reactive, ref, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import type { FormInstance } from 'ant-design-vue'
import { message } from 'ant-design-vue'
import { storeToRefs } from 'pinia'

import { adminGetAppDetail, adminUpdateApp, getMyApp, updateMyApp } from '@/api/appController'
import ACCESS_ENUM from '@/access/accessEnum'
import { useLoginUserStore } from '@/stores/loginUser'

const route = useRoute()
const router = useRouter()
const loginUserStore = useLoginUserStore()
const { currentUser } = storeToRefs(loginUserStore)

const appId = computed(() => route.params.id as string)
const isAdmin = computed(() => currentUser.value?.userRole === ACCESS_ENUM.ADMIN)

const formRef = ref<FormInstance>()
const loading = ref(false)
const saving = ref(false)

const formModel = reactive({
  appName: '',
  cover: '',
  priority: 0,
})

const rules = computed(() => ({
  appName: [{ required: true, message: '请输入应用名称' }],
  ...(isAdmin.value
    ? {
        priority: [{ required: true, message: '请输入优先级' }],
      }
    : {}),
}))

const fetchDetail = async () => {
  if (!appId.value) return
  loading.value = true
  try {
    if (isAdmin.value) {
      const { data } = await adminGetAppDetail({ id: appId.value })
      if (data.code === 0 && data.data) {
        formModel.appName = data.data.appName ?? ''
        formModel.cover = data.data.cover ?? ''
        formModel.priority = data.data.priority ?? 0
      } else {
        message.error(data.message || '获取应用信息失败')
      }
    } else {
      const { data } = await getMyApp({ id: appId.value })
      if (data.code === 0 && data.data) {
        formModel.appName = data.data.appName ?? ''
      } else {
        message.error(data.message || '获取应用信息失败')
      }
    }
  } catch (error) {
    message.error('获取应用信息失败')
  } finally {
    loading.value = false
  }
}

const handleSubmit = async () => {
  try {
    await formRef.value?.validate()
  } catch (error) {
    return
  }
  saving.value = true
  try {
    if (isAdmin.value) {
      const { data } = await adminUpdateApp(
        { id: appId.value },
        {
          appName: formModel.appName,
          cover: formModel.cover || undefined,
          priority: formModel.priority,
        },
      )
      if (data.code === 0) {
        message.success('更新成功')
        router.back()
      } else {
        message.error(data.message || '更新失败')
      }
    } else {
      const { data } = await updateMyApp(
        { id: Number(appId.value) },
        {
          appName: formModel.appName,
        },
      )
      if (data.code === 0) {
        message.success('更新成功')
        router.back()
      } else {
        message.error(data.message || '更新失败')
      }
    }
  } catch (error) {
    message.error('更新失败')
  } finally {
    saving.value = false
  }
}

watch(
  () => currentUser.value?.userRole,
  () => {
    fetchDetail()
  },
  { immediate: true },
)
</script>

<template>
  <div class="app-edit">
    <a-page-header
      :title="isAdmin ? '应用信息维护' : '编辑应用'"
      sub-title="更新应用基础信息"
      @back="router.back()"
    />

    <a-card class="app-edit__card" :loading="loading">
      <a-form ref="formRef" :model="formModel" :rules="rules" layout="vertical">
        <a-form-item label="应用名称" name="appName">
          <a-input v-model:value="formModel.appName" placeholder="请输入应用名称" />
        </a-form-item>
        <template v-if="isAdmin">
          <a-form-item label="应用封面">
            <a-input v-model:value="formModel.cover" placeholder="请输入封面链接" />
          </a-form-item>
          <a-form-item label="优先级" name="priority">
            <a-input-number v-model:value="formModel.priority" :min="0" style="width: 160px" />
          </a-form-item>
        </template>
        <a-space size="middle">
          <a-button type="primary" :loading="saving" @click="handleSubmit">保存</a-button>
          <a-button @click="router.back()">取消</a-button>
        </a-space>
      </a-form>
    </a-card>
  </div>
</template>

<style scoped>
.app-edit {
  display: flex;
  flex-direction: column;
  gap: 24px;
}

.app-edit__card {
  background: #ffffff;
}
</style>
