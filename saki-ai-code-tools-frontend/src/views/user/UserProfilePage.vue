<script lang="ts" setup>
import { computed, nextTick, onMounted, onUnmounted, reactive, ref, watch } from 'vue'
import { storeToRefs } from 'pinia'
import { useRouter } from 'vue-router'
import type { FormInstance, UploadProps } from 'ant-design-vue'
import { message } from 'ant-design-vue'
import { EditOutlined, CheckOutlined, CloseOutlined } from '@ant-design/icons-vue'

import {
  baseAdminGetUserById,
  baseUserGetUserById,
  sendEmailUpdateCode,
  updateEmail,
  updateProfile,
} from '@/api/userController'
import ACCESS_ENUM from '@/access/accessEnum'
import { useLoginUserStore } from '@/stores/loginUser'

const router = useRouter()
const loginUserStore = useLoginUserStore()
const { currentUser } = storeToRefs(loginUserStore)

const loading = ref(false)

const profile = ref<(API.User & API.UserVO) | null>(null)

const emailModalVisible = ref(false)
const emailCodeLoading = ref(false)
const emailCountdown = ref(0)
const emailFormRef = ref<FormInstance>()
const updatingEmail = ref(false)
const updatingProfile = ref(false)
const profileAvatarUploading = ref(false)
let emailTimer: ReturnType<typeof setInterval> | undefined

// 内联编辑状态
const editingField = ref<'userName' | 'userProfile' | null>(null)
const editingUserName = ref('')
const editingUserProfile = ref('')

const emailForm = reactive<API.UserEmailUpdateRequest>({
  id: undefined,
  userPassword: '',
  newEmail: '',
  emailCode: '',
})

const normalizeId = (id?: string | number | null) => {
  if (id === undefined || id === null) return undefined
  return typeof id === 'string' ? id : id.toString()
}

const clearEmailCountdown = () => {
  emailCountdown.value = 0
  if (emailTimer) {
    clearInterval(emailTimer)
    emailTimer = undefined
  }
}

const startEmailCountdown = () => {
  clearEmailCountdown()
  emailCountdown.value = 60
  emailTimer = setInterval(() => {
    if (emailCountdown.value <= 1) {
      clearEmailCountdown()
    } else {
      emailCountdown.value -= 1
    }
  }, 1000)
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

const resetEmailForm = () => {
  emailForm.userPassword = ''
  emailForm.emailCode = ''
  emailForm.newEmail = ''
}

const syncFormIds = () => {
  const id = normalizeId(currentUser.value?.id)
  emailForm.id = id
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
    await loadProfile()
  }
})

onUnmounted(() => {
  clearEmailCountdown()
})

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
  clearEmailCountdown()
}

// 开始编辑字段
const startEdit = (field: 'userName' | 'userProfile') => {
  if (!profile.value) return
  editingField.value = field
  if (field === 'userName') {
    editingUserName.value = profile.value.userName ?? ''
  } else {
    editingUserProfile.value = profile.value.userProfile ?? ''
  }
}

// 取消编辑
const cancelEdit = () => {
  editingField.value = null
  editingUserName.value = ''
  editingUserProfile.value = ''
}

// 保存编辑
const saveEdit = async () => {
  if (!editingField.value || !profile.value) return

  const currentId = normalizeId(currentUser.value?.id)
  if (!currentId) {
    message.error('未识别当前用户')
    return
  }

  // 验证昵称
  if (editingField.value === 'userName' && !editingUserName.value.trim()) {
    message.warning('昵称不能为空')
    return
  }

  updatingProfile.value = true
  try {
    const payload: API.UserProfileUpdateRequest = {
      id: currentId,
      userName: editingField.value === 'userName' ? editingUserName.value : profile.value.userName,
      userProfile:
        editingField.value === 'userProfile' ? editingUserProfile.value : profile.value.userProfile,
    }
    const { data } = await updateProfile(payload)
    if (data.code === 0) {
      message.success('个人信息已更新')
      cancelEdit()
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
    const { data } = await sendEmailUpdateCode(emailForm.newEmail)
    if (data.code === 0) {
      message.success('验证码已发送，请检查邮箱')
      startEmailCountdown()
    } else {
      message.error(data.message ?? '验证码发送失败')
    }
  } catch {
    message.error('验证码发送失败')
  } finally {
    emailCodeLoading.value = false
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
      await loadProfile()
    } else {
      message.error(data.message ?? '邮箱更新失败')
    }
  } catch {
    message.error('邮箱更新失败')
  } finally {
    updatingEmail.value = false
  }
}

const emailRules = {
  newEmail: [{ required: true, message: '请输入新邮箱' }],
  emailCode: [{ required: true, message: '请输入验证码' }],
  userPassword: [{ required: true, message: '请输入密码' }],
}

import { upload } from '@/api/fileController'

const handleProfileAvatarChange: UploadProps['onChange'] = async (info) => {
  console.log('upload triggered:', info)

  //  兼容 Ant Design Vue 的 File 类型
  const file = (info.file.originFileObj ?? info.file) as File
  if (!file) {
    message.error('文件获取失败，请重试')
    return
  }

  if (!file.type.startsWith('image/')) {
    message.error('请选择图片文件')
    return
  }
  if (profileAvatarUploading.value) return

  profileAvatarUploading.value = true
  try {
    const currentId = currentUser.value?.id
    if (!currentId) {
      message.error('未识别当前用户，无法上传头像')
      return
    }

    // 上传文件到 COS
    const { data } = await upload({ biz: 'user_avatar', userId: Number(currentId) }, {}, file)
    console.log('upload result:', data)

    if (data.code !== 0 || !data.data) {
      message.error(data.message ?? '头像上传失败')
      return
    }

    //  兼容返回类型（string 或 FileUploadVO）
    const avatarUrl = typeof data.data === 'string' ? data.data : data.data.url
    message.success('头像上传成功')

    //  调用后端更新头像
    const updateRes = await updateProfile({
      id: currentId,
      userAvatar: avatarUrl,
    })

    if (updateRes.data.code === 0) {
      message.success('头像已更新')
      await loginUserStore.fetchUser()
      await loadProfile()
    } else {
      message.error(updateRes.data.message ?? '更新头像失败')
    }
  } catch (err) {
    console.error(err)
    message.error('上传或更新头像失败')
  } finally {
    profileAvatarUploading.value = false
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
    />
    <a-spin :spinning="loading">
      <a-space direction="vertical" size="large" class="user-profile__content">
        <a-card title="我的头像" class="user-profile__card">
          <div class="user-profile__avatar-section">
            <div class="user-profile__avatar-wrapper">
              <a-avatar :size="120" :src="profile?.userAvatar" class="user-profile__avatar">
                <span v-if="!profile?.userAvatar" class="user-profile__avatar-text">{{
                  avatarInitial
                }}</span>
              </a-avatar>
              <div class="user-profile__avatar-upload-overlay">
                <a-upload
                  accept="image/*"
                  :show-upload-list="false"
                  :before-upload="() => false"
                  :disabled="profileAvatarUploading"
                  @change="handleProfileAvatarChange"
                >
                  <a-button type="primary" :loading="profileAvatarUploading" size="small">
                    {{ profileAvatarUploading ? '上传中...' : '更换头像' }}
                  </a-button>
                </a-upload>
              </div>
            </div>
            <div class="user-profile__avatar-info">
              <div class="user-profile__avatar-name">{{ profile?.userName ?? '用户' }}</div>
              <div class="user-profile__avatar-account">
                账号：{{ profile?.userAccount ?? '—' }}
              </div>
            </div>
          </div>
        </a-card>
        <a-card class="user-profile__card">
          <template #title>
            <span class="user-profile__card-title">
              <span class="user-profile__card-icon">👤</span>
              基础信息
            </span>
          </template>
          <a-descriptions :column="1" bordered size="middle" class="user-profile__descriptions">
            <a-descriptions-item label="昵称">
              <div v-if="editingField !== 'userName'" class="user-profile__editable-field">
                <span class="user-profile__info-value">{{ profile?.userName ?? '—' }}</span>
                <EditOutlined class="user-profile__edit-icon" @click="startEdit('userName')" />
              </div>
              <div v-else class="user-profile__edit-input">
                <a-input
                  v-model:value="editingUserName"
                  placeholder="请输入昵称"
                  :maxlength="50"
                  @press-enter="saveEdit"
                />
                <div class="user-profile__edit-actions">
                  <CheckOutlined
                    class="user-profile__action-icon save"
                    :class="{ disabled: updatingProfile }"
                    @click="saveEdit"
                  />
                  <CloseOutlined
                    class="user-profile__action-icon cancel"
                    :class="{ disabled: updatingProfile }"
                    @click="cancelEdit"
                  />
                  <span v-if="updatingProfile" class="user-profile__saving-text">保存中...</span>
                </div>
              </div>
            </a-descriptions-item>
            <a-descriptions-item label="角色">
              <a-tag
                :color="profile?.userRole === 'admin' ? 'magenta' : 'blue'"
                class="user-profile__tag"
              >
                {{ profile?.userRole === 'admin' ? '管理员' : '用户' }}
              </a-tag>
            </a-descriptions-item>
            <a-descriptions-item label="状态">
              <a-tag :color="profile?.userStatus === 1 ? 'green' : 'red'" class="user-profile__tag">
                {{ profile?.userStatus === 1 ? '正常' : '禁用' }}
              </a-tag>
            </a-descriptions-item>
            <a-descriptions-item label="会员状态">
              <a-tag :color="isVip ? 'gold' : 'default'" class="user-profile__tag">
                {{ isVip ? 'VIP 会员' : '普通用户' }}
              </a-tag>
            </a-descriptions-item>
            <a-descriptions-item v-if="isVip" label="会员有效期">
              <span class="user-profile__info-value">
                {{ formatDate(profile?.vipStartTime) }} ~ {{ formatDate(profile?.vipEndTime) }}
              </span>
            </a-descriptions-item>
            <a-descriptions-item label="邀请码">
              <span class="user-profile__info-value user-profile__invite-code">{{
                profile?.inviteCode ?? '—'
              }}</span>
            </a-descriptions-item>
            <a-descriptions-item label="个人简介">
              <div v-if="editingField !== 'userProfile'" class="user-profile__editable-field">
                <span class="user-profile__bio">{{ profile?.userProfile || '—' }}</span>
                <EditOutlined class="user-profile__edit-icon" @click="startEdit('userProfile')" />
              </div>
              <div v-else class="user-profile__edit-input">
                <a-textarea
                  v-model:value="editingUserProfile"
                  placeholder="请输入个人简介"
                  :rows="4"
                  :maxlength="500"
                  show-count
                />
                <div class="user-profile__edit-actions">
                  <CheckOutlined
                    class="user-profile__action-icon save"
                    :class="{ disabled: updatingProfile }"
                    @click="saveEdit"
                  />
                  <CloseOutlined
                    class="user-profile__action-icon cancel"
                    :class="{ disabled: updatingProfile }"
                    @click="cancelEdit"
                  />
                  <span v-if="updatingProfile" class="user-profile__saving-text">保存中...</span>
                </div>
              </div>
            </a-descriptions-item>
          </a-descriptions>
        </a-card>

        <a-card class="user-profile__card">
          <template #title>
            <span class="user-profile__card-title">
              <span class="user-profile__card-icon">🔒</span>
              账号安全
            </span>
          </template>
          <div class="user-profile__security-list">
            <div class="user-profile__security-item">
              <div class="user-profile__security-content">
                <div class="user-profile__security-header">
                  <span class="user-profile__security-icon">📧</span>
                  <div class="user-profile__security-info">
                    <div class="user-profile__security-title">绑定邮箱</div>
                    <div class="user-profile__security-desc">
                      {{ profile?.userEmail ?? '未绑定' }}
                    </div>
                  </div>
                </div>
              </div>
              <a-button type="primary" ghost @click="openEmailModal">更改邮箱</a-button>
            </div>
          </div>
          <a-typography-paragraph type="secondary" class="user-profile__security-tip">
            更换邮箱时需要通过验证码验证身份，请确保可以正常接收验证码。
          </a-typography-paragraph>
        </a-card>

        <a-card class="user-profile__card">
          <template #title>
            <span class="user-profile__card-title">
              <span class="user-profile__card-icon">📅</span>
              登录与时间信息
            </span>
          </template>
          <a-descriptions :column="1" size="middle" class="user-profile__descriptions">
            <a-descriptions-item label="最近登录">
              <span class="user-profile__info-value">{{ formatDate(profile?.lastLoginTime) }}</span>
            </a-descriptions-item>
            <a-descriptions-item label="最近登录 IP">
              <span class="user-profile__info-value">{{ profile?.lastLoginIp ?? '—' }}</span>
            </a-descriptions-item>
            <a-descriptions-item label="最后编辑时间">
              <span class="user-profile__info-value">{{ formatDate(profile?.editTime) }}</span>
            </a-descriptions-item>
            <a-descriptions-item label="创建时间">
              <span class="user-profile__info-value">{{ formatDate(profile?.createTime) }}</span>
            </a-descriptions-item>
          </a-descriptions>
        </a-card>
      </a-space>
    </a-spin>

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
  border-radius: 12px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);
  transition: box-shadow 0.3s;
}

.user-profile__card:hover {
  box-shadow: 0 4px 16px rgba(0, 0, 0, 0.1);
}

.user-profile__card-title {
  display: flex;
  align-items: center;
  gap: 8px;
  font-weight: 600;
  font-size: 16px;
}

.user-profile__card-icon {
  font-size: 18px;
}

.user-profile__avatar-section {
  display: flex;
  gap: 24px;
  align-items: center;
  padding: 8px;
}

.user-profile__avatar-wrapper {
  position: relative;
  display: flex;
  align-items: center;
  justify-content: center;
}

.user-profile__avatar {
  border: 3px solid #f0f0f0;
  transition: border-color 0.3s;
}

.user-profile__avatar-wrapper:hover .user-profile__avatar {
  border-color: #1890ff;
}

.user-profile__avatar-text {
  font-size: 48px;
  font-weight: 600;
  color: #1890ff;
}

.user-profile__avatar-upload-overlay {
  position: absolute;
  bottom: 0;
  right: 0;
  opacity: 0;
  transition: opacity 0.3s;
}

.user-profile__avatar-wrapper:hover .user-profile__avatar-upload-overlay {
  opacity: 1;
}

.user-profile__avatar-info {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.user-profile__avatar-name {
  font-size: 20px;
  font-weight: 600;
  color: rgba(0, 0, 0, 0.85);
}

.user-profile__avatar-account {
  font-size: 14px;
  color: rgba(0, 0, 0, 0.65);
}

.user-profile__bio {
  white-space: pre-wrap;
  color: rgba(0, 0, 0, 0.85);
  line-height: 1.6;
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
  padding: 20px;
  border: 1px solid #e8e8e8;
  border-radius: 12px;
  background: linear-gradient(135deg, #fafafa 0%, #ffffff 100%);
  transition: all 0.3s;
}

.user-profile__security-item:hover {
  border-color: #1890ff;
  box-shadow: 0 2px 8px rgba(24, 144, 255, 0.1);
  transform: translateY(-2px);
}

.user-profile__security-content {
  flex: 1;
}

.user-profile__security-header {
  display: flex;
  align-items: center;
  gap: 12px;
}

.user-profile__security-icon {
  font-size: 24px;
}

.user-profile__security-info {
  flex: 1;
}

.user-profile__security-title {
  font-weight: 600;
  font-size: 16px;
  margin-bottom: 4px;
  color: rgba(0, 0, 0, 0.85);
}

.user-profile__security-desc {
  font-size: 14px;
  color: rgba(0, 0, 0, 0.65);
}

.user-profile__security-tip {
  margin-top: 16px;
  padding-top: 16px;
  border-top: 1px solid #f0f0f0;
}

:deep(.ant-descriptions-item-label) {
  width: 140px;
  font-weight: 500;
  color: rgba(0, 0, 0, 0.85);
}

.user-profile__descriptions {
  border-radius: 8px;
  overflow: hidden;
}

.user-profile__info-value {
  color: rgba(0, 0, 0, 0.85);
  font-weight: 500;
}

.user-profile__invite-code {
  font-family: 'Monaco', 'Menlo', 'Courier New', monospace;
  background: #f5f5f5;
  padding: 4px 8px;
  border-radius: 4px;
  font-size: 13px;
}

.user-profile__tag {
  font-weight: 500;
  padding: 2px 12px;
  border-radius: 12px;
}

.user-profile__editable-field {
  display: flex;
  align-items: center;
  gap: 8px;
  width: 100%;
}

.user-profile__edit-icon {
  color: rgba(0, 0, 0, 0.45);
  cursor: pointer;
  font-size: 14px;
  transition: all 0.3s;
  opacity: 0;
}

.user-profile__editable-field:hover .user-profile__edit-icon {
  opacity: 1;
}

.user-profile__edit-icon:hover {
  color: #1890ff;
}

.user-profile__edit-input {
  display: flex;
  flex-direction: column;
  gap: 8px;
  width: 100%;
}

.user-profile__edit-actions {
  display: flex;
  gap: 8px;
  align-items: center;
}

.user-profile__action-icon {
  font-size: 16px;
  cursor: pointer;
  padding: 4px;
  border-radius: 4px;
  transition: all 0.3s;
}

.user-profile__action-icon.save {
  color: #52c41a;
}

.user-profile__action-icon.save:hover {
  background-color: #f6ffed;
  color: #389e0d;
}

.user-profile__action-icon.cancel {
  color: rgba(0, 0, 0, 0.45);
}

.user-profile__action-icon.cancel:hover {
  background-color: #fff1f0;
  color: #ff4d4f;
}

.user-profile__action-icon.disabled {
  opacity: 0.5;
  cursor: not-allowed;
  pointer-events: none;
}

.user-profile__saving-text {
  font-size: 12px;
  color: rgba(0, 0, 0, 0.45);
  margin-left: 8px;
}
</style>
