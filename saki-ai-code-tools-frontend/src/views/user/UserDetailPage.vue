<script lang="ts" setup>
import { computed, nextTick, onMounted, reactive, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import type { FormInstance, UploadProps } from 'ant-design-vue'
import { message } from 'ant-design-vue'

import { baseAdminGetUserById, updateUser } from '@/api/userController'

const route = useRoute()
const router = useRouter()

const userId = ref<string>('')

if (route.params.id) {
  userId.value = String(route.params.id)
} else {
  router.replace('/user/userManage')
}

const loading = ref(false)
const saving = ref(false)
const editModalVisible = ref(false)
const detail = ref<API.UserVO | null>(null)
const formRef = ref<FormInstance>()
const formState = reactive<API.UserUpdateRequest>({
  id: undefined,
  userAccount: '',
  userName: '',
  userEmail: '',
  userPhone: '',
  userAvatar: '',
  userRole: 'user',
  userStatus: 1,
  isVip: 0,
  vipStartTime: undefined,
  vipEndTime: undefined,
})

const formRules = {
  userAccount: [{ required: true, message: '请输入账号' }],
  userName: [{ required: true, message: '请输入名称' }],
  userRole: [{ required: true, message: '请选择角色' }],
}

const formatDate = (value?: string) => {
  if (!value) return '—'
  const date = new Date(value)
  if (Number.isNaN(date.getTime())) {
    return value
  }
  return new Intl.DateTimeFormat('zh-CN', {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit',
  }).format(date)
}

const isVip = computed(() => detail.value?.isVip === 1)

const avatarInitial = computed(() => {
  const source = detail.value?.userName || detail.value?.userAccount || '用'
  return source.charAt(0).toUpperCase()
})

const syncFormState = (user: API.User | API.UserVO | null) => {
  formState.id = user?.id
    ? typeof user.id === 'string'
      ? user.id
      : user.id.toString()
    : undefined
  formState.userAccount = user?.userAccount ?? ''
  formState.userName = user?.userName ?? ''
  formState.userEmail = user?.userEmail ?? ''
  formState.userPhone = user?.userPhone ?? ''
  formState.userAvatar = user?.userAvatar ?? ''
  formState.userRole = user?.userRole ?? 'user'
  formState.userStatus = user?.userStatus ?? 1
  formState.isVip = user?.isVip ?? 0
  formState.vipStartTime = user?.vipStartTime ?? undefined
  formState.vipEndTime = user?.vipEndTime ?? undefined
}

const fetchDetail = async () => {
  if (!userId.value) return
  loading.value = true
  try {
    const { data } = await baseAdminGetUserById({ id: userId.value })
    if (data.code === 0 && data.data) {
      const user = data.data
      const normalizedId = user.id
        ? typeof user.id === 'string'
          ? user.id
          : user.id.toString()
        : undefined
      detail.value = {
        ...user,
        id: normalizedId,
      } as API.UserVO
      syncFormState(detail.value)
    } else {
      message.error(data.message ?? '获取用户信息失败')
    }
  } catch {
    message.error('获取用户信息失败')
  } finally {
    loading.value = false
  }
}

const openEditModal = () => {
  if (!detail.value) return
  syncFormState(detail.value)
  editModalVisible.value = true
  nextTick(() => {
    formRef.value?.clearValidate?.()
  })
}

const closeEditModal = () => {
  editModalVisible.value = false
}

const handleSubmit = async () => {
  try {
    await formRef.value?.validate()
  } catch {
    return
  }
  saving.value = true
  try {
    const payload: API.UserUpdateRequest = {
      ...formState,
      id: formState.id,
    }
    const { data } = await updateUser(payload)
    if (data.code === 0) {
      message.success('用户信息已更新')
      closeEditModal()
      fetchDetail()
    } else {
      message.error(data.message ?? '更新用户信息失败')
    }
  } catch {
    message.error('更新用户信息失败')
  } finally {
    saving.value = false
  }
}

const fileToDataUrl = (file: File): Promise<string> =>
  new Promise((resolve, reject) => {
    const reader = new FileReader()
    reader.onload = () => resolve((reader.result as string) || '')
    reader.onerror = () => reject(new Error('读取文件失败'))
    reader.readAsDataURL(file)
  })

const handleAvatarChange: UploadProps['onChange'] = async (info) => {
  const file = info.file.originFileObj as File | undefined
  if (!file) return
  if (!file.type.startsWith('image/')) {
    message.error('请选择图片文件')
    return
  }
  try {
    const preview = await fileToDataUrl(file)
    formState.userAvatar = preview
  } catch {
    message.error('读取图片失败，请重试')
  }
}

const clearAvatar = () => {
  formState.userAvatar = ''
}

onMounted(() => {
  if (userId.value) {
    fetchDetail()
  }
})
</script>

<template>
  <div class="user-detail">
    <a-page-header
      :ghost="false"
      title="用户详情"
      sub-title="查看与维护用户信息"
      @back="() => router.back()"
    >
      <template #extra>
        <a-button @click="() => router.back()">返回</a-button>
        <a-button type="primary" :disabled="!detail" @click="openEditModal">编辑信息</a-button>
      </template>
    </a-page-header>

    <a-spin :spinning="loading">
      <a-space direction="vertical" size="large" class="user-detail__content">
        <a-card class="user-detail__card">
          <template #title>基本信息</template>
          <template #extra>
            <a-avatar :size="64" :src="detail?.userAvatar">
              <span v-if="!detail?.userAvatar">{{ avatarInitial }}</span>
            </a-avatar>
          </template>
          <a-descriptions :column="1" bordered size="middle">
            <a-descriptions-item label="用户ID">{{ detail?.id ?? '—' }}</a-descriptions-item>
            <a-descriptions-item label="账号">{{ detail?.userAccount ?? '—' }}</a-descriptions-item>
            <a-descriptions-item label="名称">{{ detail?.userName ?? '—' }}</a-descriptions-item>
            <a-descriptions-item label="邮箱">{{ detail?.userEmail ?? '—' }}</a-descriptions-item>
            <a-descriptions-item label="手机号">{{ detail?.userPhone ?? '—' }}</a-descriptions-item>
            <a-descriptions-item label="个人简介">{{ detail?.userProfile ?? '—' }}</a-descriptions-item>
          </a-descriptions>
        </a-card>

        <a-card title="权限与会员" class="user-detail__card">
          <a-descriptions :column="1" size="middle">
            <a-descriptions-item label="角色">
              <a-tag :color="detail?.userRole === 'admin' ? 'magenta' : 'blue'">
                {{ detail?.userRole === 'admin' ? '管理员' : '用户' }}
              </a-tag>
            </a-descriptions-item>
            <a-descriptions-item label="状态">
              <a-tag :color="detail?.userStatus === 1 ? 'green' : 'red'">
                {{ detail?.userStatus === 1 ? '正常' : '禁用' }}
              </a-tag>
            </a-descriptions-item>
            <a-descriptions-item label="会员等级">
              <a-tag :color="isVip ? 'gold' : 'default'">{{ isVip ? 'VIP 会员' : '普通用户' }}</a-tag>
            </a-descriptions-item>
            <a-descriptions-item v-if="isVip" label="会员有效期">
              {{ formatDate(detail?.vipStartTime) }} ~ {{ formatDate(detail?.vipEndTime) }}
            </a-descriptions-item>
            <a-descriptions-item label="邀请码">{{ detail?.inviteCode ?? '—' }}</a-descriptions-item>
          </a-descriptions>
        </a-card>

        <a-card title="时间信息" class="user-detail__card">
          <a-descriptions :column="1" size="middle">
            <a-descriptions-item label="最近登录时间">{{ formatDate(detail?.lastLoginTime) }}</a-descriptions-item>
            <a-descriptions-item label="最近登录 IP">{{ detail?.lastLoginIp ?? '—' }}</a-descriptions-item>
            <a-descriptions-item label="最后编辑时间">{{ formatDate(detail?.editTime) }}</a-descriptions-item>
            <a-descriptions-item label="创建时间">{{ formatDate(detail?.createTime) }}</a-descriptions-item>
          </a-descriptions>
        </a-card>
      </a-space>
    </a-spin>

    <a-modal
      v-model:open="editModalVisible"
      title="编辑用户信息"
      :confirm-loading="saving"
      destroy-on-close
      @ok="handleSubmit"
      @cancel="closeEditModal"
    >
      <a-form
        ref="formRef"
        :model="formState"
        :rules="formRules"
        layout="vertical"
        class="user-detail__form"
      >
        <a-form-item label="用户账号" name="userAccount">
          <a-input v-model:value="formState.userAccount" placeholder="请输入账号" />
        </a-form-item>
        <a-form-item label="用户名称" name="userName">
          <a-input v-model:value="formState.userName" placeholder="请输入名称" />
        </a-form-item>
        <a-form-item label="用户头像">
          <div class="user-detail__avatar-upload">
            <a-avatar :size="96" :src="formState.userAvatar">
              <span v-if="!formState.userAvatar">{{ avatarInitial }}</span>
            </a-avatar>
            <div class="user-detail__avatar-actions">
              <a-upload
                accept="image/*"
                :show-upload-list="false"
                :before-upload="() => false"
                @change="handleAvatarChange"
              >
                <a-button>上传头像</a-button>
              </a-upload>
              <a-input v-model:value="formState.userAvatar" placeholder="或粘贴头像图片地址" />
              <a-button v-if="formState.userAvatar" type="link" danger @click="clearAvatar">
                移除头像
              </a-button>
            </div>
          </div>
        </a-form-item>
        <a-form-item label="用户邮箱" name="userEmail">
          <a-input v-model:value="formState.userEmail" placeholder="请输入邮箱" />
        </a-form-item>
        <a-form-item label="用户手机号" name="userPhone">
          <a-input v-model:value="formState.userPhone" placeholder="请输入手机号" />
        </a-form-item>
        <a-form-item label="用户角色" name="userRole">
          <a-select v-model:value="formState.userRole" placeholder="请选择角色">
            <a-select-option value="user">普通用户</a-select-option>
            <a-select-option value="admin">管理员</a-select-option>
          </a-select>
        </a-form-item>
        <a-form-item label="用户状态" name="userStatus">
          <a-select v-model:value="formState.userStatus" placeholder="请选择状态">
            <a-select-option :value="1">正常</a-select-option>
            <a-select-option :value="0">禁用</a-select-option>
          </a-select>
        </a-form-item>
        <a-form-item label="是否会员" name="isVip">
          <a-select v-model:value="formState.isVip" placeholder="请选择会员状态">
            <a-select-option :value="0">普通</a-select-option>
            <a-select-option :value="1">VIP</a-select-option>
          </a-select>
        </a-form-item>
        <template v-if="formState.isVip === 1">
          <a-form-item label="会员开始时间" name="vipStartTime">
            <a-date-picker
              v-model:value="formState.vipStartTime"
              show-time
              style="width: 100%"
              value-format="YYYY-MM-DD HH:mm:ss"
              placeholder="请选择开始时间"
            />
          </a-form-item>
          <a-form-item label="会员结束时间" name="vipEndTime">
            <a-date-picker
              v-model:value="formState.vipEndTime"
              show-time
              style="width: 100%"
              value-format="YYYY-MM-DD HH:mm:ss"
              placeholder="请选择结束时间"
            />
          </a-form-item>
        </template>
      </a-form>
    </a-modal>
  </div>
</template>

<style scoped>
.user-detail {
  display: flex;
  flex-direction: column;
  gap: 24px;
}

.user-detail__content {
  margin-top: 16px;
  display: flex;
  flex-direction: column;
}

.user-detail__card {
  background: #ffffff;
}

.user-detail__form {
  max-height: 70vh;
  overflow-y: auto;
}

:deep(.ant-descriptions-item-label) {
  width: 120px;
}

.user-detail__avatar-upload {
  display: flex;
  gap: 16px;
  align-items: center;
}

.user-detail__avatar-actions {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 8px;
}
</style>
