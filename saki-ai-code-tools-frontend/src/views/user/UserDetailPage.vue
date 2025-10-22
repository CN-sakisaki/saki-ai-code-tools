<script lang="ts" setup>
import { computed, onMounted, reactive, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import type { FormInstance } from 'ant-design-vue'
import { message } from 'ant-design-vue'

import { baseAdminGetUserById, updateUser } from '@/api/userController'

const route = useRoute()
const router = useRouter()

const userId = Number(route.params.id)

if (!userId) {
  router.replace('/user/userManage')
}

const loading = ref(false)
const saving = ref(false)
const detail = ref<API.UserVO | null>(null)
const formRef = ref<FormInstance>()
const formState = reactive<API.UserUpdateRequest>({
  id: userId,
  userAccount: '',
  userName: '',
  userEmail: '',
  userPhone: '',
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

const fetchDetail = async () => {
  if (!userId) return
  loading.value = true
  try {
    const { data } = await baseAdminGetUserById({ id: userId })
    if (data.code === 0 && data.data) {
      const user = data.data
      detail.value = {
        id: user.id,
        userAccount: user.userAccount,
        userName: user.userName,
        userEmail: user.userEmail,
        userPhone: user.userPhone,
        userAvatar: user.userAvatar,
        userProfile: user.userProfile,
        userRole: user.userRole,
        userStatus: user.userStatus,
        isVip: user.isVip,
        vipStartTime: user.vipStartTime,
        vipEndTime: user.vipEndTime,
        inviteCode: user.inviteCode,
        lastLoginTime: user.lastLoginTime,
        lastLoginIp: user.lastLoginIp,
        editTime: user.editTime,
        createTime: user.createTime,
        updateTime: user.updateTime,
      }

      formState.id = user.id
      formState.userAccount = user.userAccount
      formState.userName = user.userName
      formState.userEmail = user.userEmail
      formState.userPhone = user.userPhone
      formState.userRole = user.userRole
      formState.userStatus = user.userStatus
      formState.isVip = user.isVip
      formState.vipStartTime = user.vipStartTime
      formState.vipEndTime = user.vipEndTime
    } else {
      message.error(data.message ?? '获取用户信息失败')
    }
  } catch (error) {
    message.error('获取用户信息失败')
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
    const { data } = await updateUser(formState)
    if (data.code === 0) {
      message.success('用户信息已更新')
      fetchDetail()
    } else {
      message.error(data.message ?? '更新用户信息失败')
    }
  } catch (error) {
    message.error('更新用户信息失败')
  } finally {
    saving.value = false
  }
}

onMounted(() => {
  fetchDetail()
})
</script>

<template>
  <div class="user-detail">
    <a-page-header
      :ghost="false"
      title="用户详情"
      :sub-title="detail?.userName || detail?.userAccount || `ID: ${userId}`"
      @back="() => router.back()"
    >
      <template #extra>
        <a-button @click="() => router.back()">返回</a-button>
        <a-button type="primary" :loading="saving" @click="handleSubmit">保存修改</a-button>
      </template>
    </a-page-header>

    <a-spin :spinning="loading">
      <a-row :gutter="24" class="user-detail__content">
        <a-col :xs="24" :lg="8">
          <a-card title="基本信息" class="user-detail__card">
            <a-descriptions :column="1" bordered size="small">
              <a-descriptions-item label="用户ID">{{ detail?.id ?? '—' }}</a-descriptions-item>
              <a-descriptions-item label="账号">{{ detail?.userAccount ?? '—' }}</a-descriptions-item>
              <a-descriptions-item label="名称">{{ detail?.userName ?? '—' }}</a-descriptions-item>
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
              <a-descriptions-item label="最近登录时间">{{ formatDate(detail?.lastLoginTime) }}</a-descriptions-item>
              <a-descriptions-item label="最近登录 IP">{{ detail?.lastLoginIp ?? '—' }}</a-descriptions-item>
              <a-descriptions-item label="创建时间">{{ formatDate(detail?.createTime) }}</a-descriptions-item>
              <a-descriptions-item label="更新时间">{{ formatDate(detail?.updateTime) }}</a-descriptions-item>
            </a-descriptions>
          </a-card>
        </a-col>

        <a-col :xs="24" :lg="16">
          <a-card title="编辑信息" class="user-detail__card">
            <a-form
              ref="formRef"
              :model="formState"
              :rules="formRules"
              :label-col="{ span: 6 }"
              :wrapper-col="{ span: 16 }"
            >
              <a-form-item label="用户账号" name="userAccount">
                <a-input v-model:value="formState.userAccount" placeholder="请输入账号" />
              </a-form-item>
              <a-form-item label="用户名称" name="userName">
                <a-input v-model:value="formState.userName" placeholder="请输入名称" />
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
              <a-form-item v-if="formState.isVip === 1" label="会员开始时间" name="vipStartTime">
                <a-date-picker
                  v-model:value="formState.vipStartTime"
                  show-time
                  style="width: 100%"
                  value-format="YYYY-MM-DD HH:mm:ss"
                  placeholder="请选择开始时间"
                />
              </a-form-item>
              <a-form-item v-if="formState.isVip === 1" label="会员结束时间" name="vipEndTime">
                <a-date-picker
                  v-model:value="formState.vipEndTime"
                  show-time
                  style="width: 100%"
                  value-format="YYYY-MM-DD HH:mm:ss"
                  placeholder="请选择结束时间"
                />
              </a-form-item>
            </a-form>
          </a-card>
        </a-col>
      </a-row>
    </a-spin>
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
}

.user-detail__card {
  background: #ffffff;
}

:deep(.ant-descriptions-item-label) {
  width: 120px;
}
</style>
