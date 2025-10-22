<script lang="ts" setup>
import { computed, onMounted, reactive, ref, watch } from 'vue'
import { storeToRefs } from 'pinia'
import type { FormInstance } from 'ant-design-vue'
import { message } from 'ant-design-vue'

import { baseAdminGetUserById, baseUserGetUserById, updateEmail, updatePhone } from '@/api/userController'
import ACCESS_ENUM from '@/access/accessEnum'
import { useLoginUserStore } from '@/stores/loginUser'

const loginUserStore = useLoginUserStore()
const { currentUser } = storeToRefs(loginUserStore)

const loading = ref(false)

const profile = ref<(API.User & API.UserVO) | null>(null)

const phoneFormRef = ref<FormInstance>()
const emailFormRef = ref<FormInstance>()

const phoneForm = reactive<API.UserPhoneUpdateRequest>({
  id: undefined,
  userPassword: '',
  newPhone: '',
  phoneCode: '',
})

const emailForm = reactive<API.UserEmailUpdateRequest>({
  id: undefined,
  userPassword: '',
  newEmail: '',
  emailCode: '',
})

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

const isVip = computed(() => profile.value?.isVip === 1)

const loadProfile = async () => {
  if (!currentUser.value?.id) {
    return
  }
  loading.value = true
  try {
    if (currentUser.value.userRole === ACCESS_ENUM.ADMIN) {
      const { data } = await baseAdminGetUserById({ id: currentUser.value.id })
      if (data.code === 0 && data.data) {
        profile.value = { ...data.data } as (API.User & API.UserVO)
      } else {
        message.error(data.message ?? '获取用户信息失败')
      }
    } else {
      const { data } = await baseUserGetUserById({ id: currentUser.value.id })
      if (data.code === 0 && data.data) {
        profile.value = { ...data.data } as (API.User & API.UserVO)
      } else {
        message.error(data.message ?? '获取用户信息失败')
      }
    }
  } catch (error) {
    message.error('获取用户信息失败')
  } finally {
    loading.value = false
  }
}

const syncFormIds = () => {
  phoneForm.id = currentUser.value?.id
  emailForm.id = currentUser.value?.id
}

watch(
  () => currentUser.value?.id,
  () => {
    syncFormIds()
    if (currentUser.value?.id) {
      loadProfile()
    }
  },
)

onMounted(async () => {
  if (!currentUser.value) {
    await loginUserStore.fetchUser()
  }
  syncFormIds()
  if (currentUser.value?.id) {
    loadProfile()
  }
})

const handleUpdatePhone = async () => {
  try {
    await phoneFormRef.value?.validate()
  } catch (error) {
    return
  }

  try {
    const { data } = await updatePhone(phoneForm)
    if (data.code === 0) {
      message.success('手机号更新成功')
      phoneFormRef.value?.resetFields()
      phoneForm.userPassword = ''
      phoneForm.phoneCode = ''
      phoneForm.newPhone = ''
      loadProfile()
    } else {
      message.error(data.message ?? '手机号更新失败')
    }
  } catch (error) {
    message.error('手机号更新失败')
  }
}

const handleUpdateEmail = async () => {
  try {
    await emailFormRef.value?.validate()
  } catch (error) {
    return
  }

  try {
    const { data } = await updateEmail(emailForm)
    if (data.code === 0) {
      message.success('邮箱更新成功')
      emailFormRef.value?.resetFields()
      emailForm.userPassword = ''
      emailForm.emailCode = ''
      emailForm.newEmail = ''
      loadProfile()
    } else {
      message.error(data.message ?? '邮箱更新失败')
    }
  } catch (error) {
    message.error('邮箱更新失败')
  }
}

const phoneRules = {
  newPhone: [{ required: true, message: '请输入新手机号' }],
  phoneCode: [{ required: true, message: '请输入验证码' }],
  userPassword: [{ required: true, message: '请输入密码' }],
}

const emailRules = {
  newEmail: [{ required: true, message: '请输入新邮箱' }],
  emailCode: [{ required: true, message: '请输入验证码' }],
  userPassword: [{ required: true, message: '请输入密码' }],
}
</script>

<template>
  <div class="user-profile">
    <a-page-header :ghost="false" title="个人中心" sub-title="管理个人信息" />
    <a-spin :spinning="loading">
      <a-row :gutter="24" class="user-profile__content">
        <a-col :xs="24" :lg="12">
          <a-card title="基础信息" class="user-profile__card">
            <a-descriptions :column="1" bordered size="small">
              <a-descriptions-item label="账号">{{ profile?.userAccount ?? '—' }}</a-descriptions-item>
              <a-descriptions-item label="昵称">{{ profile?.userName ?? '—' }}</a-descriptions-item>
              <a-descriptions-item label="角色">
                <a-tag :color="profile?.userRole === 'admin' ? 'magenta' : 'blue'">
                  {{ profile?.userRole === 'admin' ? '管理员' : '用户' }}
                </a-tag>
              </a-descriptions-item>
              <a-descriptions-item label="状态">
                <a-tag :color="profile?.userStatus === 1 ? 'green' : 'red'">
                  {{ profile?.userStatus === 1 ? '正常' : '禁用' }}
                </a-tag>
              </a-descriptions-item>
              <a-descriptions-item label="会员状态">
                <a-tag :color="isVip ? 'gold' : 'default'">{{ isVip ? 'VIP 会员' : '普通用户' }}</a-tag>
              </a-descriptions-item>
              <a-descriptions-item v-if="isVip" label="会员起止">
                {{ formatDate(profile?.vipStartTime) }} ~ {{ formatDate(profile?.vipEndTime) }}
              </a-descriptions-item>
              <a-descriptions-item label="邮箱">{{ profile?.userEmail ?? '—' }}</a-descriptions-item>
              <a-descriptions-item label="手机号">{{ profile?.userPhone ?? '—' }}</a-descriptions-item>
              <a-descriptions-item label="邀请码">{{ profile?.inviteCode ?? '—' }}</a-descriptions-item>
              <a-descriptions-item label="最近登录">{{ formatDate(profile?.lastLoginTime) }}</a-descriptions-item>
              <a-descriptions-item label="创建时间">{{ formatDate(profile?.createTime) }}</a-descriptions-item>
              <a-descriptions-item label="更新时间">{{ formatDate(profile?.updateTime) }}</a-descriptions-item>
            </a-descriptions>
          </a-card>
        </a-col>

        <a-col :xs="24" :lg="12">
          <a-space direction="vertical" size="large" style="width: 100%">
            <a-card title="更新手机号" class="user-profile__card">
              <a-form
                ref="phoneFormRef"
                :model="phoneForm"
                :rules="phoneRules"
                :label-col="{ span: 6 }"
                :wrapper-col="{ span: 16 }"
              >
                <a-form-item label="新手机号" name="newPhone">
                  <a-input v-model:value="phoneForm.newPhone" placeholder="请输入新手机号" />
                </a-form-item>
                <a-form-item label="验证码" name="phoneCode">
                  <a-input v-model:value="phoneForm.phoneCode" placeholder="请输入短信验证码" />
                </a-form-item>
                <a-form-item label="密码" name="userPassword">
                  <a-input-password v-model:value="phoneForm.userPassword" placeholder="请输入密码" />
                </a-form-item>
                <a-form-item :wrapper-col="{ offset: 6, span: 16 }">
                  <a-button type="primary" block @click="handleUpdatePhone">更新手机号</a-button>
                </a-form-item>
              </a-form>
            </a-card>

            <a-card title="更新邮箱" class="user-profile__card">
              <a-form
                ref="emailFormRef"
                :model="emailForm"
                :rules="emailRules"
                :label-col="{ span: 6 }"
                :wrapper-col="{ span: 16 }"
              >
                <a-form-item label="新邮箱" name="newEmail">
                  <a-input v-model:value="emailForm.newEmail" placeholder="请输入新邮箱" />
                </a-form-item>
                <a-form-item label="验证码" name="emailCode">
                  <a-input v-model:value="emailForm.emailCode" placeholder="请输入邮箱验证码" />
                </a-form-item>
                <a-form-item label="密码" name="userPassword">
                  <a-input-password v-model:value="emailForm.userPassword" placeholder="请输入密码" />
                </a-form-item>
                <a-form-item :wrapper-col="{ offset: 6, span: 16 }">
                  <a-button type="primary" block @click="handleUpdateEmail">更新邮箱</a-button>
                </a-form-item>
              </a-form>
            </a-card>
          </a-space>
        </a-col>
      </a-row>
    </a-spin>
  </div>
</template>

<style scoped>
.user-profile {
  display: flex;
  flex-direction: column;
  gap: 24px;
}

.user-profile__content {
  margin-top: 16px;
}

.user-profile__card {
  background: #ffffff;
}

:deep(.ant-descriptions-item-label) {
  width: 120px;
}
</style>
