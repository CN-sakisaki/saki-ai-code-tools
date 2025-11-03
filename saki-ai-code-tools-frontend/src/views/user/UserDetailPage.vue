<script lang="ts" setup>
import { computed, onMounted, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import type { UploadProps } from 'ant-design-vue'
import { message } from 'ant-design-vue'
import { EditOutlined, CheckOutlined, CloseOutlined } from '@ant-design/icons-vue'

import { baseAdminGetUserById, updateUser } from '@/api/userController'
import { upload } from '@/api/fileController'

const route = useRoute()
const router = useRouter()

const userId = ref<string>('')

if (route.params.id) {
  userId.value = String(route.params.id)
} else {
  router.replace('/user/userManage')
}

const loading = ref(false)
const updating = ref(false)
const avatarUploading = ref(false)
const detail = ref<API.UserVO | null>(null)

// 内联编辑状态
const editingField = ref<
  | 'userAccount'
  | 'userName'
  | 'userEmail'
  | 'userRole'
  | 'userStatus'
  | 'isVip'
  | 'userProfile'
  | null
>(null)
const editingUserAccount = ref('')
const editingUserName = ref('')
const editingUserEmail = ref('')
const editingUserRole = ref<'user' | 'admin'>('user')
const editingUserStatus = ref(1)
const editingIsVip = ref(0)
const editingVipStartTime = ref<string | undefined>(undefined)
const editingVipEndTime = ref<string | undefined>(undefined)
const editingUserProfile = ref('')

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
    } else {
      message.error(data.message ?? '获取用户信息失败')
    }
  } catch {
    message.error('获取用户信息失败')
  } finally {
    loading.value = false
  }
}

// 开始编辑字段
const startEdit = (
  field:
    | 'userAccount'
    | 'userName'
    | 'userEmail'
    | 'userRole'
    | 'userStatus'
    | 'isVip'
    | 'userProfile',
) => {
  if (!detail.value || !field) return
  editingField.value = field
  switch (field) {
    case 'userAccount':
      editingUserAccount.value = detail.value.userAccount ?? ''
      break
    case 'userName':
      editingUserName.value = detail.value.userName ?? ''
      break
    case 'userEmail':
      editingUserEmail.value = detail.value.userEmail ?? ''
      break
    case 'userRole':
      editingUserRole.value = (detail.value.userRole ?? 'user') as 'user' | 'admin'
      break
    case 'userStatus':
      editingUserStatus.value = detail.value.userStatus ?? 1
      break
    case 'isVip':
      editingIsVip.value = detail.value.isVip ?? 0
      editingVipStartTime.value = detail.value.vipStartTime ?? undefined
      editingVipEndTime.value = detail.value.vipEndTime ?? undefined
      break
    case 'userProfile':
      editingUserProfile.value = detail.value.userProfile ?? ''
      break
  }
}

// 取消编辑
const cancelEdit = () => {
  editingField.value = null
  editingUserAccount.value = ''
  editingUserName.value = ''
  editingUserEmail.value = ''
  editingUserRole.value = 'user'
  editingUserStatus.value = 1
  editingIsVip.value = 0
  editingVipStartTime.value = undefined
  editingVipEndTime.value = undefined
  editingUserProfile.value = ''
}

// 保存编辑
const saveEdit = async () => {
  if (!editingField.value || !detail.value || !userId.value) return

  // 验证必填字段
  if (editingField.value === 'userAccount' && !editingUserAccount.value.trim()) {
    message.warning('账号不能为空')
    return
  }
  if (editingField.value === 'userName' && !editingUserName.value.trim()) {
    message.warning('名称不能为空')
    return
  }
  if (editingField.value === 'userRole' && !editingUserRole.value) {
    message.warning('请选择角色')
    return
  }

  updating.value = true
  try {
    const payload: API.UserUpdateRequest = {
      id: userId.value,
    }

    switch (editingField.value) {
      case 'userAccount':
        payload.userAccount = editingUserAccount.value
        break
      case 'userName':
        payload.userName = editingUserName.value
        break
      case 'userEmail':
        payload.userEmail = editingUserEmail.value
        break
      case 'userRole':
        payload.userRole = editingUserRole.value
        break
      case 'userStatus':
        payload.userStatus = editingUserStatus.value
        break
      case 'isVip':
        payload.isVip = editingIsVip.value
        payload.vipStartTime = editingVipStartTime.value
        payload.vipEndTime = editingVipEndTime.value
        break
      case 'userProfile':
        payload.userProfile = editingUserProfile.value
        break
    }

    // 保留其他字段
    payload.userAvatar = detail.value.userAvatar ?? ''

    const { data } = await updateUser(payload)
    if (data.code === 0) {
      message.success('用户信息已更新')
      cancelEdit()
      await fetchDetail()
    } else {
      message.error(data.message ?? '更新用户信息失败')
    }
  } catch {
    message.error('更新用户信息失败')
  } finally {
    updating.value = false
  }
}

const handleAvatarUpload: UploadProps['onChange'] = async (info) => {
  console.log('upload triggered:', info)

  //  兼容 Ant Design Vue 不同版本
  const file = (info.file.originFileObj ?? info.file) as File
  if (!file) {
    message.error('文件获取失败，请重试')
    return
  }

  if (!file.type.startsWith('image/')) {
    message.error('请选择图片文件')
    return
  }

  if (avatarUploading.value) return
  avatarUploading.value = true

  try {
    const targetId = userId.value
    if (!targetId) {
      message.error('未识别用户ID，无法上传头像')
      return
    }

    // 上传到 COS
    const { data } = await upload({ biz: 'user_avatar', userId: Number(targetId) }, {}, file)
    console.log('upload result:', data)

    if (data.code !== 0 || !data.data) {
      message.error(data.message ?? '头像上传失败')
      return
    }

    //  兼容返回类型（string 或 FileUploadVO）
    const avatarUrl = typeof data.data === 'string' ? data.data : data.data.url
    message.success('头像上传成功')

    // 更新用户头像
    const { data: updateRes } = await updateUser({
      id: targetId,
      userAvatar: avatarUrl,
    })

    if (updateRes.code === 0) {
      message.success('用户头像已更新')
      await fetchDetail()
    } else {
      message.error(updateRes.message ?? '更新用户头像失败')
    }
  } catch (err) {
    console.error(err)
    message.error('上传或更新头像失败')
  } finally {
    avatarUploading.value = false
  }
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
    />
    <a-spin :spinning="loading">
      <a-space direction="vertical" size="large" class="user-detail__content">
        <a-card title="用户头像" class="user-detail__card">
          <div class="user-detail__avatar-section">
            <div class="user-detail__avatar-wrapper">
              <a-avatar :size="120" :src="detail?.userAvatar" class="user-detail__avatar">
                <span v-if="!detail?.userAvatar" class="user-detail__avatar-text">{{
                  avatarInitial
                }}</span>
              </a-avatar>
              <div class="user-detail__avatar-upload-overlay">
                <a-upload
                  accept="image/*"
                  :show-upload-list="false"
                  :before-upload="() => false"
                  :disabled="avatarUploading"
                  @change="handleAvatarUpload"
                >
                  <a-button type="primary" :loading="avatarUploading" size="small">
                    {{ avatarUploading ? '上传中...' : '更换头像' }}
                  </a-button>
                </a-upload>
              </div>
            </div>
            <div class="user-detail__avatar-info">
              <div class="user-detail__avatar-name">{{ detail?.userName ?? '用户' }}</div>
              <div class="user-detail__avatar-account">账号：{{ detail?.userAccount ?? '—' }}</div>
            </div>
          </div>
        </a-card>
        <a-card class="user-detail__card">
          <template #title>
            <span class="user-detail__card-title">
              <span class="user-detail__card-icon">👤</span>
              基础信息
            </span>
          </template>
          <a-descriptions :column="1" bordered size="middle" class="user-detail__descriptions">
            <a-descriptions-item label="用户ID">
              <span class="user-detail__info-value">{{ detail?.id ?? '—' }}</span>
            </a-descriptions-item>
            <a-descriptions-item label="账号">
              <div v-if="editingField !== 'userAccount'" class="user-detail__editable-field">
                <span class="user-detail__info-value">{{ detail?.userAccount ?? '—' }}</span>
                <EditOutlined class="user-detail__edit-icon" @click="startEdit('userAccount')" />
              </div>
              <div v-else class="user-detail__edit-input">
                <a-input
                  v-model:value="editingUserAccount"
                  placeholder="请输入账号"
                  :maxlength="50"
                  @press-enter="saveEdit"
                />
                <div class="user-detail__edit-actions">
                  <CheckOutlined
                    class="user-detail__action-icon save"
                    :class="{ disabled: updating }"
                    @click="saveEdit"
                  />
                  <CloseOutlined
                    class="user-detail__action-icon cancel"
                    :class="{ disabled: updating }"
                    @click="cancelEdit"
                  />
                  <span v-if="updating" class="user-detail__saving-text">保存中...</span>
                </div>
              </div>
            </a-descriptions-item>
            <a-descriptions-item label="名称">
              <div v-if="editingField !== 'userName'" class="user-detail__editable-field">
                <span class="user-detail__info-value">{{ detail?.userName ?? '—' }}</span>
                <EditOutlined class="user-detail__edit-icon" @click="startEdit('userName')" />
              </div>
              <div v-else class="user-detail__edit-input">
                <a-input
                  v-model:value="editingUserName"
                  placeholder="请输入名称"
                  :maxlength="50"
                  @press-enter="saveEdit"
                />
                <div class="user-detail__edit-actions">
                  <CheckOutlined
                    class="user-detail__action-icon save"
                    :class="{ disabled: updating }"
                    @click="saveEdit"
                  />
                  <CloseOutlined
                    class="user-detail__action-icon cancel"
                    :class="{ disabled: updating }"
                    @click="cancelEdit"
                  />
                  <span v-if="updating" class="user-detail__saving-text">保存中...</span>
                </div>
              </div>
            </a-descriptions-item>
            <a-descriptions-item label="邮箱">
              <div v-if="editingField !== 'userEmail'" class="user-detail__editable-field">
                <span class="user-detail__info-value">{{ detail?.userEmail ?? '—' }}</span>
                <EditOutlined class="user-detail__edit-icon" @click="startEdit('userEmail')" />
              </div>
              <div v-else class="user-detail__edit-input">
                <a-input
                  v-model:value="editingUserEmail"
                  placeholder="请输入邮箱"
                  @press-enter="saveEdit"
                />
                <div class="user-detail__edit-actions">
                  <CheckOutlined
                    class="user-detail__action-icon save"
                    :class="{ disabled: updating }"
                    @click="saveEdit"
                  />
                  <CloseOutlined
                    class="user-detail__action-icon cancel"
                    :class="{ disabled: updating }"
                    @click="cancelEdit"
                  />
                  <span v-if="updating" class="user-detail__saving-text">保存中...</span>
                </div>
              </div>
            </a-descriptions-item>
            <a-descriptions-item label="个人简介">
              <div v-if="editingField !== 'userProfile'" class="user-detail__editable-field">
                <span class="user-detail__bio">{{ detail?.userProfile || '—' }}</span>
                <EditOutlined class="user-detail__edit-icon" @click="startEdit('userProfile')" />
              </div>
              <div v-else class="user-detail__edit-input">
                <a-textarea
                  v-model:value="editingUserProfile"
                  placeholder="请输入个人简介"
                  :rows="4"
                  :maxlength="500"
                  show-count
                />
                <div class="user-detail__edit-actions">
                  <CheckOutlined
                    class="user-detail__action-icon save"
                    :class="{ disabled: updating }"
                    @click="saveEdit"
                  />
                  <CloseOutlined
                    class="user-detail__action-icon cancel"
                    :class="{ disabled: updating }"
                    @click="cancelEdit"
                  />
                  <span v-if="updating" class="user-detail__saving-text">保存中...</span>
                </div>
              </div>
            </a-descriptions-item>
          </a-descriptions>
        </a-card>

        <a-card class="user-detail__card">
          <template #title>
            <span class="user-detail__card-title">
              <span class="user-detail__card-icon">🔐</span>
              权限与会员
            </span>
          </template>
          <a-descriptions :column="1" size="middle" class="user-detail__descriptions">
            <a-descriptions-item label="角色">
              <div v-if="editingField !== 'userRole'" class="user-detail__editable-field">
                <a-tag
                  :color="detail?.userRole === 'admin' ? 'magenta' : 'blue'"
                  class="user-detail__tag"
                >
                  {{ detail?.userRole === 'admin' ? '管理员' : '普通用户' }}
                </a-tag>
                <EditOutlined class="user-detail__edit-icon" @click="startEdit('userRole')" />
              </div>
              <div v-else class="user-detail__edit-input">
                <a-select
                  v-model:value="editingUserRole"
                  placeholder="请选择角色"
                  style="width: 100%"
                >
                  <a-select-option value="user">普通用户</a-select-option>
                  <a-select-option value="admin">管理员</a-select-option>
                </a-select>
                <div class="user-detail__edit-actions">
                  <CheckOutlined
                    class="user-detail__action-icon save"
                    :class="{ disabled: updating }"
                    @click="saveEdit"
                  />
                  <CloseOutlined
                    class="user-detail__action-icon cancel"
                    :class="{ disabled: updating }"
                    @click="cancelEdit"
                  />
                  <span v-if="updating" class="user-detail__saving-text">保存中...</span>
                </div>
              </div>
            </a-descriptions-item>
            <a-descriptions-item label="状态">
              <div v-if="editingField !== 'userStatus'" class="user-detail__editable-field">
                <a-tag :color="detail?.userStatus === 1 ? 'green' : 'red'" class="user-detail__tag">
                  {{ detail?.userStatus === 1 ? '正常' : '禁用' }}
                </a-tag>
                <EditOutlined class="user-detail__edit-icon" @click="startEdit('userStatus')" />
              </div>
              <div v-else class="user-detail__edit-input">
                <a-select
                  v-model:value="editingUserStatus"
                  placeholder="请选择状态"
                  style="width: 100%"
                >
                  <a-select-option :value="1">正常</a-select-option>
                  <a-select-option :value="0">禁用</a-select-option>
                </a-select>
                <div class="user-detail__edit-actions">
                  <CheckOutlined
                    class="user-detail__action-icon save"
                    :class="{ disabled: updating }"
                    @click="saveEdit"
                  />
                  <CloseOutlined
                    class="user-detail__action-icon cancel"
                    :class="{ disabled: updating }"
                    @click="cancelEdit"
                  />
                  <span v-if="updating" class="user-detail__saving-text">保存中...</span>
                </div>
              </div>
            </a-descriptions-item>
            <a-descriptions-item label="会员等级">
              <div v-if="editingField !== 'isVip'" class="user-detail__editable-field">
                <a-tag :color="isVip ? 'gold' : 'default'" class="user-detail__tag">
                  {{ isVip ? 'VIP 会员' : '普通会员' }}
                </a-tag>
                <EditOutlined class="user-detail__edit-icon" @click="startEdit('isVip')" />
              </div>
              <div v-else class="user-detail__edit-input">
                <a-select
                  v-model:value="editingIsVip"
                  placeholder="请选择会员状态"
                  style="width: 100%"
                >
                  <a-select-option :value="0">普通会员</a-select-option>
                  <a-select-option :value="1">VIP</a-select-option>
                </a-select>
                <template v-if="editingIsVip === 1">
                  <a-date-picker
                    v-model:value="editingVipStartTime"
                    show-time
                    style="width: 100%; margin-top: 8px"
                    value-format="YYYY-MM-DD HH:mm:ss"
                    placeholder="请选择开始时间"
                  />
                  <a-date-picker
                    v-model:value="editingVipEndTime"
                    show-time
                    style="width: 100%; margin-top: 8px"
                    value-format="YYYY-MM-DD HH:mm:ss"
                    placeholder="请选择结束时间"
                  />
                </template>
                <div class="user-detail__edit-actions">
                  <CheckOutlined
                    class="user-detail__action-icon save"
                    :class="{ disabled: updating }"
                    @click="saveEdit"
                  />
                  <CloseOutlined
                    class="user-detail__action-icon cancel"
                    :class="{ disabled: updating }"
                    @click="cancelEdit"
                  />
                  <span v-if="updating" class="user-detail__saving-text">保存中...</span>
                </div>
              </div>
            </a-descriptions-item>
            <a-descriptions-item v-if="isVip && editingField !== 'isVip'" label="会员有效期">
              <span class="user-detail__info-value">
                {{ formatDate(detail?.vipStartTime) }} ~ {{ formatDate(detail?.vipEndTime) }}
              </span>
            </a-descriptions-item>
            <a-descriptions-item label="邀请码">
              <span class="user-detail__info-value user-detail__invite-code">{{
                detail?.inviteCode ?? '—'
              }}</span>
            </a-descriptions-item>
          </a-descriptions>
        </a-card>

        <a-card class="user-detail__card">
          <template #title>
            <span class="user-detail__card-title">
              <span class="user-detail__card-icon">📅</span>
              时间信息
            </span>
          </template>
          <a-descriptions :column="1" size="middle" class="user-detail__descriptions">
            <a-descriptions-item label="最近登录时间">
              <span class="user-detail__info-value">{{ formatDate(detail?.lastLoginTime) }}</span>
            </a-descriptions-item>
            <a-descriptions-item label="最近登录 IP">
              <span class="user-detail__info-value">{{ detail?.lastLoginIp ?? '—' }}</span>
            </a-descriptions-item>
            <a-descriptions-item label="最后编辑时间">
              <span class="user-detail__info-value">{{ formatDate(detail?.editTime) }}</span>
            </a-descriptions-item>
            <a-descriptions-item label="创建时间">
              <span class="user-detail__info-value">{{ formatDate(detail?.createTime) }}</span>
            </a-descriptions-item>
          </a-descriptions>
        </a-card>
      </a-space>
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
  display: flex;
  flex-direction: column;
  width: 100%;
}

.user-detail__card {
  background: #ffffff;
  border-radius: 12px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);
  transition: box-shadow 0.3s;
}

.user-detail__card:hover {
  box-shadow: 0 4px 16px rgba(0, 0, 0, 0.1);
}

.user-detail__card-title {
  display: flex;
  align-items: center;
  gap: 8px;
  font-weight: 600;
  font-size: 16px;
}

.user-detail__card-icon {
  font-size: 18px;
}

.user-detail__avatar-section {
  display: flex;
  gap: 24px;
  align-items: center;
  padding: 8px;
}

.user-detail__avatar-wrapper {
  position: relative;
  display: flex;
  align-items: center;
  justify-content: center;
}

.user-detail__avatar {
  border: 3px solid #f0f0f0;
  transition: border-color 0.3s;
}

.user-detail__avatar-wrapper:hover .user-detail__avatar {
  border-color: #1890ff;
}

.user-detail__avatar-text {
  font-size: 48px;
  font-weight: 600;
  color: #1890ff;
}

.user-detail__avatar-upload-overlay {
  position: absolute;
  bottom: 0;
  right: 0;
  opacity: 0;
  transition: opacity 0.3s;
}

.user-detail__avatar-wrapper:hover .user-detail__avatar-upload-overlay {
  opacity: 1;
}

.user-detail__avatar-info {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.user-detail__avatar-name {
  font-size: 20px;
  font-weight: 600;
  color: rgba(0, 0, 0, 0.85);
}

.user-detail__avatar-account {
  font-size: 14px;
  color: rgba(0, 0, 0, 0.65);
}

.user-detail__bio {
  white-space: pre-wrap;
  color: rgba(0, 0, 0, 0.85);
  line-height: 1.6;
}

:deep(.ant-descriptions-item-label) {
  width: 140px;
  font-weight: 500;
  color: rgba(0, 0, 0, 0.85);
}

.user-detail__descriptions {
  border-radius: 8px;
  overflow: hidden;
}

.user-detail__info-value {
  color: rgba(0, 0, 0, 0.85);
  font-weight: 500;
}

.user-detail__invite-code {
  font-family: 'Monaco', 'Menlo', 'Courier New', monospace;
  background: #f5f5f5;
  padding: 4px 8px;
  border-radius: 4px;
  font-size: 13px;
}

.user-detail__tag {
  font-weight: 500;
  padding: 2px 12px;
  border-radius: 12px;
}

.user-detail__editable-field {
  display: flex;
  align-items: center;
  gap: 8px;
  width: 100%;
}

.user-detail__edit-icon {
  color: rgba(0, 0, 0, 0.45);
  cursor: pointer;
  font-size: 14px;
  transition: all 0.3s;
  opacity: 0;
}

.user-detail__editable-field:hover .user-detail__edit-icon {
  opacity: 1;
}

.user-detail__edit-icon:hover {
  color: #1890ff;
}

.user-detail__edit-input {
  display: flex;
  flex-direction: column;
  gap: 8px;
  width: 100%;
}

.user-detail__edit-actions {
  display: flex;
  gap: 8px;
  align-items: center;
}

.user-detail__action-icon {
  font-size: 16px;
  cursor: pointer;
  padding: 4px;
  border-radius: 4px;
  transition: all 0.3s;
}

.user-detail__action-icon.save {
  color: #52c41a;
}

.user-detail__action-icon.save:hover {
  background-color: #f6ffed;
  color: #389e0d;
}

.user-detail__action-icon.cancel {
  color: rgba(0, 0, 0, 0.45);
}

.user-detail__action-icon.cancel:hover {
  background-color: #fff1f0;
  color: #ff4d4f;
}

.user-detail__action-icon.disabled {
  opacity: 0.5;
  cursor: not-allowed;
  pointer-events: none;
}

.user-detail__saving-text {
  font-size: 12px;
  color: rgba(0, 0, 0, 0.45);
  margin-left: 8px;
}
</style>
