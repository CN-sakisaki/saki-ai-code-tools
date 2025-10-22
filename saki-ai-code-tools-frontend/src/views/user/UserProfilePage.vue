<script lang="ts" setup>
import { computed, nextTick, onMounted, onUnmounted, reactive, ref, watch } from 'vue'
import { storeToRefs } from 'pinia'
import { useRouter } from 'vue-router'
import type { FormInstance, UploadProps } from 'ant-design-vue'
import { message } from 'ant-design-vue'

import {
  baseAdminGetUserById,
  baseUserGetUserById,
  sendEmailCode,
  updateEmail,
  updatePhone,
  updateProfile,
  uploadCurrentUserAvatar,
} from '@/api/userController'
import ACCESS_ENUM from '@/access/accessEnum'
import { useLoginUserStore } from '@/stores/loginUser'

const router = useRouter()
const loginUserStore = useLoginUserStore()
const { currentUser } = storeToRefs(loginUserStore)

const loading = ref(false)

const profile = ref<(API.User & API.UserVO) | null>(null)

const phoneModalVisible = ref(false)
const emailModalVisible = ref(false)
const editModalVisible = ref(false)
const phoneCodeLoading = ref(false)
const emailCodeLoading = ref(false)
const phoneCountdown = ref(0)
const emailCountdown = ref(0)
const phoneFormRef = ref<FormInstance>()
const emailFormRef = ref<FormInstance>()
const profileFormRef = ref<FormInstance>()
const updatingPhone = ref(false)
const updatingEmail = ref(false)
const updatingProfile = ref(false)
const profileAvatarUploading = ref(false)
let phoneTimer: ReturnType<typeof setInterval> | undefined
let emailTimer: ReturnType<typeof setInterval> | undefined

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

const profileForm = reactive<API.UserProfileUpdateRequest>({
  id: undefined,
  userName: '',
  userAvatar: '',
  userProfile: '',
})

const normalizeId = (id?: string | number | null) => {
  if (id === undefined || id === null) return undefined
  return typeof id === 'string' ? id : id.toString()
}

const clearCountdown = (type: 'phone' | 'email') => {
  if (type === 'phone') {
    phoneCountdown.value = 0
    if (phoneTimer) {
      clearInterval(phoneTimer)
      phoneTimer = undefined
    }
  } else {
    emailCountdown.value = 0
    if (emailTimer) {
      clearInterval(emailTimer)
      emailTimer = undefined
    }
  }
}

const startCountdown = (type: 'phone' | 'email') => {
  clearCountdown(type)
  if (type === 'phone') {
    phoneCountdown.value = 60
    phoneTimer = setInterval(() => {
      if (phoneCountdown.value <= 1) {
        clearCountdown('phone')
      } else {
        phoneCountdown.value -= 1
      }
    }, 1000)
  } else {
    emailCountdown.value = 60
    emailTimer = setInterval(() => {
      if (emailCountdown.value <= 1) {
        clearCountdown('email')
      } else {
        emailCountdown.value -= 1
      }
    }, 1000)
  }
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

const isVip = computed(() => profile.value?.isVip === 1)

const avatarInitial = computed(() => {
  const source = profile.value?.userName || profile.value?.userAccount || '用'
  return source.charAt(0).toUpperCase()
})

const loadProfile = async () => {
  if (!currentUser.value?.id) {
    return
  }
  loading.value = true
  try {
    if (currentUser.value.userRole === ACCESS_ENUM.ADMIN) {
      const { data } = await baseAdminGetUserById({ id: normalizeId(currentUser.value.id)! })
      if (data.code === 0 && data.data) {
        profile.value = { ...data.data, id: normalizeId(data.data.id) } as API.User & API.UserVO
      } else {
        message.error(data.message ?? '获取用户信息失败')
      }
    } else {
      const { data } = await baseUserGetUserById({ id: normalizeId(currentUser.value.id)! })
      if (data.code === 0 && data.data) {
        profile.value = { ...data.data, id: normalizeId(data.data.id) } as API.User & API.UserVO
      } else {
        message.error(data.message ?? '获取用户信息失败')
      }
    }
  } catch {
    message.error('获取用户信息失败')
  } finally {
    loading.value = false
  }
}

const resetPhoneForm = () => {
  phoneForm.userPassword = ''
  phoneForm.phoneCode = ''
  phoneForm.newPhone = ''
}

const resetEmailForm = () => {
  emailForm.userPassword = ''
  emailForm.emailCode = ''
  emailForm.newEmail = ''
}

const resetProfileForm = () => {
  profileForm.id = normalizeId(currentUser.value?.id)
  profileForm.userName = profile.value?.userName ?? ''
  profileForm.userAvatar = profile.value?.userAvatar ?? ''
  profileForm.userProfile = profile.value?.userProfile ?? ''
}

const syncFormIds = () => {
  const id = normalizeId(currentUser.value?.id)
  phoneForm.id = id
  emailForm.id = id
  profileForm.id = id
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

onUnmounted(() => {
  clearCountdown('phone')
  clearCountdown('email')
})

const openPhoneModal = () => {
  syncFormIds()
  phoneModalVisible.value = true
  nextTick(() => {
    phoneFormRef.value?.clearValidate?.()
  })
}

const closePhoneModal = () => {
  phoneModalVisible.value = false
  phoneFormRef.value?.resetFields()
  resetPhoneForm()
  clearCountdown('phone')
}

const openEmailModal = () => {
  syncFormIds()
  emailModalVisible.value = true
  nextTick(() => {
    emailFormRef.value?.clearValidate?.()
  })
}

const closeEmailModal = () => {
  emailModalVisible.value = false
  emailFormRef.value?.resetFields()
  resetEmailForm()
  clearCountdown('email')
}

const openEditModal = () => {
  if (!profile.value) return
  resetProfileForm()
  editModalVisible.value = true
  nextTick(() => {
    profileFormRef.value?.clearValidate?.()
  })
}

const closeEditModal = () => {
  editModalVisible.value = false
}

const handleSendPhoneCode = () => {
  if (!phoneForm.newPhone) {
    message.warning('请先输入新手机号')
    return
  }
  message.warning('短信验证码服务暂未接入，请联系管理员')
}

const handleSendEmailCode = async () => {
  if (!emailForm.newEmail) {
    message.warning('请先输入新邮箱')
    return
  }
  if (emailCountdown.value > 0 || emailCodeLoading.value) {
    return
  }
  emailCodeLoading.value = true
  try {
    const { data } = await sendEmailCode({ email: emailForm.newEmail })
    if (data.code === 0) {
      message.success('验证码已发送，请检查邮箱')
      startCountdown('email')
    } else {
      message.error(data.message ?? '验证码发送失败')
    }
  } catch {
    message.error('验证码发送失败')
  } finally {
    emailCodeLoading.value = false
  }
}

const handleUpdatePhone = async () => {
  try {
    await phoneFormRef.value?.validate()
  } catch {
    return
  }

  updatingPhone.value = true
  try {
    const { data } = await updatePhone(phoneForm)
    if (data.code === 0) {
      message.success('手机号更新成功')
      closePhoneModal()
      loadProfile()
    } else {
      message.error(data.message ?? '手机号更新失败')
    }
  } catch {
    message.error('手机号更新失败')
  } finally {
    updatingPhone.value = false
  }
}

const handleUpdateEmail = async () => {
  try {
    await emailFormRef.value?.validate()
  } catch {
    return
  }

  updatingEmail.value = true
  try {
    const { data } = await updateEmail(emailForm)
    if (data.code === 0) {
      message.success('邮箱更新成功')
      closeEmailModal()
      loadProfile()
    } else {
      message.error(data.message ?? '邮箱更新失败')
    }
  } catch {
    message.error('邮箱更新失败')
  } finally {
    updatingEmail.value = false
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

const profileRules = {
  userName: [{ required: true, message: '请输入昵称' }],
}

const handleProfileAvatarChange: UploadProps['onChange'] = async (info) => {
  const file = info.file.originFileObj as File | undefined
  if (!file) return
  if (!file.type.startsWith('image/')) {
    message.error('请选择图片文件')
    return
  }
  if (profileAvatarUploading.value) return

  const formData = new FormData()
  formData.append('file', file)

  profileAvatarUploading.value = true
  try {
    const { data } = await uploadCurrentUserAvatar(formData)
    if (data.code === 0 && data.data) {
      profileForm.userAvatar = data.data
      message.success('头像上传成功')
    } else {
      message.error(data.message ?? '头像上传失败')
    }
  } catch {
    message.error('头像上传失败')
  } finally {
    profileAvatarUploading.value = false
  }
}

const clearProfileAvatar = () => {
  if (profileAvatarUploading.value) {
    message.warning('头像上传中，请稍后再试')
    return
  }
  profileForm.userAvatar = ''
}

const handleUpdateProfile = async () => {
  try {
    await profileFormRef.value?.validate()
  } catch {
    return
  }

  updatingProfile.value = true
  try {
    const payload: API.UserProfileUpdateRequest = {
      ...profileForm,
      id: profileForm.id ?? normalizeId(currentUser.value?.id),
    }
    const { data } = await updateProfile(payload)
    if (data.code === 0) {
      message.success('个人信息已更新')
      closeEditModal()
      await loginUserStore.fetchUser()
      await loadProfile()
    } else {
      message.error(data.message ?? '更新个人信息失败')
    }
  } catch {
    message.error('更新个人信息失败')
  } finally {
    updatingProfile.value = false
  }
}
</script>

<template>
  <div class="user-profile">
    <a-page-header
      :ghost="false"
      title="个人中心"
      sub-title="管理个人信息"
      @back="() => router.back()"
    >
      <template #extra>
        <a-button type="primary" :disabled="!profile" @click="openEditModal">编辑信息</a-button>
      </template>
    </a-page-header>
    <a-spin :spinning="loading">
      <a-space direction="vertical" size="large" class="user-profile__content">
        <a-card class="user-profile__card">
          <template #title>基础信息</template>
          <template #extra>
            <a-avatar :size="64" :src="profile?.userAvatar">
              <span v-if="!profile?.userAvatar">{{ avatarInitial }}</span>
            </a-avatar>
          </template>
          <a-descriptions :column="1" bordered size="middle">
            <a-descriptions-item label="账号">{{
              profile?.userAccount ?? '—'
            }}</a-descriptions-item>
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
              <a-tag :color="isVip ? 'gold' : 'default'">{{
                isVip ? 'VIP 会员' : '普通用户'
              }}</a-tag>
            </a-descriptions-item>
            <a-descriptions-item v-if="isVip" label="会员有效期">
              {{ formatDate(profile?.vipStartTime) }} ~ {{ formatDate(profile?.vipEndTime) }}
            </a-descriptions-item>
            <a-descriptions-item label="邀请码">{{
              profile?.inviteCode ?? '—'
            }}</a-descriptions-item>
            <a-descriptions-item label="个人简介">
              <span class="user-profile__bio">{{ profile?.userProfile || '—' }}</span>
            </a-descriptions-item>
          </a-descriptions>
        </a-card>

        <a-card title="账号安全" class="user-profile__card">
          <div class="user-profile__security-list">
            <div class="user-profile__security-item">
              <div>
                <div class="user-profile__security-title">绑定手机号</div>
                <div class="user-profile__security-desc">{{ profile?.userPhone ?? '未绑定' }}</div>
              </div>
              <a-button type="default" @click="openPhoneModal">更改手机号</a-button>
            </div>
            <div class="user-profile__security-item">
              <div>
                <div class="user-profile__security-title">绑定邮箱</div>
                <div class="user-profile__security-desc">{{ profile?.userEmail ?? '未绑定' }}</div>
              </div>
              <a-button type="default" @click="openEmailModal">更改邮箱</a-button>
            </div>
          </div>
          <a-typography-paragraph type="secondary" class="user-profile__security-tip">
            更换手机号或邮箱时需要通过验证码验证身份，请确保可以正常接收验证码。
          </a-typography-paragraph>
        </a-card>

        <a-card title="登录与时间信息" class="user-profile__card">
          <a-descriptions :column="1" size="middle">
            <a-descriptions-item label="最近登录">{{
              formatDate(profile?.lastLoginTime)
            }}</a-descriptions-item>
            <a-descriptions-item label="最近登录 IP">{{
              profile?.lastLoginIp ?? '—'
            }}</a-descriptions-item>
            <a-descriptions-item label="最后编辑时间">{{
              formatDate(profile?.editTime)
            }}</a-descriptions-item>
            <a-descriptions-item label="创建时间">{{
              formatDate(profile?.createTime)
            }}</a-descriptions-item>
          </a-descriptions>
        </a-card>
      </a-space>
    </a-spin>

    <a-modal
      v-model:open="editModalVisible"
      title="编辑个人信息"
      ok-text="保存"
      cancel-text="取消"
      :confirm-loading="updatingProfile"
      destroy-on-close
      @ok="handleUpdateProfile"
      @cancel="closeEditModal"
    >
      <a-form ref="profileFormRef" :model="profileForm" :rules="profileRules" layout="vertical">
        <a-form-item label="头像">
          <div class="user-profile__avatar-upload">
            <a-avatar :size="96" :src="profileForm.userAvatar">
              <span v-if="!profileForm.userAvatar">{{ avatarInitial }}</span>
            </a-avatar>
            <div class="user-profile__avatar-actions">
              <a-upload
                accept="image/*"
                :show-upload-list="false"
                :before-upload="() => false"
                :disabled="profileAvatarUploading"
                @change="handleProfileAvatarChange"
              >
                <a-button :loading="profileAvatarUploading">上传头像</a-button>
              </a-upload>
              <a-input v-model:value="profileForm.userAvatar" placeholder="或粘贴头像图片地址" />
              <a-button
                v-if="profileForm.userAvatar"
                type="link"
                danger
                :disabled="profileAvatarUploading"
                @click="clearProfileAvatar"
              >
                移除头像
              </a-button>
            </div>
          </div>
        </a-form-item>
        <a-form-item label="昵称" name="userName">
          <a-input v-model:value="profileForm.userName" placeholder="请输入昵称" />
        </a-form-item>
        <a-form-item label="个人简介" name="userProfile">
          <a-textarea
            v-model:value="profileForm.userProfile"
            :rows="4"
            placeholder="请输入个人简介"
          />
        </a-form-item>
      </a-form>
    </a-modal>

    <a-modal
      v-model:open="phoneModalVisible"
      title="更改手机号"
      ok-text="确认更改"
      cancel-text="取消"
      :confirm-loading="updatingPhone"
      destroy-on-close
      @ok="handleUpdatePhone"
      @cancel="closePhoneModal"
    >
      <a-form ref="phoneFormRef" :model="phoneForm" :rules="phoneRules" layout="vertical">
        <a-form-item label="新手机号" name="newPhone">
          <a-input v-model:value="phoneForm.newPhone" placeholder="请输入新手机号" />
        </a-form-item>
        <a-form-item label="验证码" name="phoneCode">
          <a-row :gutter="8">
            <a-col :span="16">
              <a-input v-model:value="phoneForm.phoneCode" placeholder="请输入短信验证码" />
            </a-col>
            <a-col :span="8">
              <a-button
                block
                type="primary"
                ghost
                :disabled="phoneCountdown > 0 || phoneCodeLoading"
                :loading="phoneCodeLoading"
                @click="handleSendPhoneCode"
              >
                {{ phoneCountdown > 0 ? `${phoneCountdown}s后重试` : '获取验证码' }}
              </a-button>
            </a-col>
          </a-row>
        </a-form-item>
        <a-form-item label="密码" name="userPassword">
          <a-input-password v-model:value="phoneForm.userPassword" placeholder="请输入密码" />
        </a-form-item>
      </a-form>
    </a-modal>

    <a-modal
      v-model:open="emailModalVisible"
      title="更改邮箱"
      ok-text="确认更改"
      cancel-text="取消"
      :confirm-loading="updatingEmail"
      destroy-on-close
      @ok="handleUpdateEmail"
      @cancel="closeEmailModal"
    >
      <a-form ref="emailFormRef" :model="emailForm" :rules="emailRules" layout="vertical">
        <a-form-item label="新邮箱" name="newEmail">
          <a-input v-model:value="emailForm.newEmail" placeholder="请输入新邮箱" />
        </a-form-item>
        <a-form-item label="验证码" name="emailCode">
          <a-row :gutter="8">
            <a-col :span="16">
              <a-input v-model:value="emailForm.emailCode" placeholder="请输入邮箱验证码" />
            </a-col>
            <a-col :span="8">
              <a-button
                block
                type="primary"
                ghost
                :disabled="emailCountdown > 0 || emailCodeLoading"
                :loading="emailCodeLoading"
                @click="handleSendEmailCode"
              >
                {{ emailCountdown > 0 ? `${emailCountdown}s后重试` : '获取验证码' }}
              </a-button>
            </a-col>
          </a-row>
        </a-form-item>
        <a-form-item label="密码" name="userPassword">
          <a-input-password v-model:value="emailForm.userPassword" placeholder="请输入密码" />
        </a-form-item>
      </a-form>
    </a-modal>
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
  display: flex;
  flex-direction: column;
  width: 100%;
}

.user-profile__card {
  background: #ffffff;
}

.user-profile__bio {
  white-space: pre-wrap;
}

.user-profile__security-list {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.user-profile__security-item {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16px;
  padding: 16px;
  border: 1px solid #f0f0f0;
  border-radius: 8px;
  background: #fafafa;
}

.user-profile__security-title {
  font-weight: 600;
  margin-bottom: 4px;
}

.user-profile__security-desc {
  color: rgba(0, 0, 0, 0.65);
}

.user-profile__security-tip {
  margin-top: 16px;
}

:deep(.ant-descriptions-item-label) {
  width: 120px;
}

.user-profile__avatar-upload {
  display: flex;
  gap: 16px;
  align-items: center;
}

.user-profile__avatar-actions {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 8px;
}
</style>
