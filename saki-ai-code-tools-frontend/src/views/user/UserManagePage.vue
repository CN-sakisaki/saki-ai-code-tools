<script lang="ts" setup>
import { reactive, ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import type { FormInstance } from 'ant-design-vue'
import { message, Modal } from 'ant-design-vue'

import { addUser, deleteUsers, listUsersByPage } from '@/api/userController'

interface TablePagination {
  current: number
  pageSize: number
  total: number
}

const router = useRouter()

const loading = ref(false)
const tableData = ref<API.User[]>([])
const pagination = reactive<TablePagination>({
  current: 1,
  pageSize: 10,
  total: 0,
})

const queryForm = reactive<API.UserQueryRequest>({
  pageNum: 1,
  pageSize: 10,
  userAccount: undefined,
  userName: undefined,
  userEmail: undefined,
  userRole: undefined,
  userStatus: undefined,
  isVip: undefined,
})

const addModalVisible = ref(false)
const addFormRef = ref<FormInstance>()
const addForm = reactive<API.UserAddRequest & { userPassword?: string }>({
  userAccount: '',
  userPassword: '',
  userName: '',
  userEmail: '',
  userRole: 'user',
  userStatus: 1,
  isVip: 0,
})

const resetAddForm = () => {
  addForm.userAccount = ''
  addForm.userPassword = ''
  addForm.userName = ''
  addForm.userEmail = ''
  addForm.userRole = 'user'
  addForm.userStatus = 1
  addForm.isVip = 0
}

const addFormRules = {
  userAccount: [{ required: true, message: '请输入用户账号' }],
  userPassword: [{ required: true, message: '请输入用户密码' }],
  userName: [{ required: true, message: '请输入用户名称' }],
  userRole: [{ required: true, message: '请选择用户角色' }],
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

const fetchUsers = async () => {
  loading.value = true
  try {
    queryForm.pageNum = pagination.current
    queryForm.pageSize = pagination.pageSize
    const { data } = await listUsersByPage({
      ...queryForm,
      pageNum: pagination.current,
      pageSize: pagination.pageSize,
    })
    if (data.code === 0 && data.data) {
      tableData.value = data.data.records ?? []
      pagination.total = data.data.totalRow ?? 0
    } else {
      message.error(data.message ?? '获取用户列表失败')
    }
  } catch (error) {
    message.error('获取用户列表失败')
  } finally {
    loading.value = false
  }
}

const handleTableChange = (pag: { current: number; pageSize: number }) => {
  pagination.current = pag.current
  pagination.pageSize = pag.pageSize
  fetchUsers()
}

const handleSearch = () => {
  pagination.current = 1
  fetchUsers()
}

const handleReset = () => {
  queryForm.userAccount = undefined
  queryForm.userName = undefined
  queryForm.userEmail = undefined
  queryForm.userRole = undefined
  queryForm.userStatus = undefined
  queryForm.isVip = undefined
  pagination.current = 1
  fetchUsers()
}

const openAddModal = () => {
  resetAddForm()
  addFormRef.value?.resetFields()
  addModalVisible.value = true
}

const closeAddModal = () => {
  addModalVisible.value = false
  addFormRef.value?.resetFields()
  resetAddForm()
}

const submitAddForm = async () => {
  try {
    await addFormRef.value?.validate()
  } catch (error) {
    return
  }

  try {
    const { data } = await addUser(addForm)
    if (data.code === 0) {
      message.success('新增用户成功')
      closeAddModal()
      fetchUsers()
    } else {
      message.error(data.message ?? '新增用户失败')
    }
  } catch (error) {
    message.error('新增用户失败')
  }
}

const handleDelete = (record: API.User) => {
  if (!record.id) return
  Modal.confirm({
    title: '删除用户',
    content: `确定要删除用户「${record.userName || record.userAccount || record.id}」吗？`,
    okText: '删除',
    okButtonProps: { danger: true },
    cancelText: '取消',
    async onOk() {
      try {
        const { data } = await deleteUsers({ ids: [record.id!] })
        if (data.code === 0) {
          message.success('删除成功')
          fetchUsers()
        } else {
          message.error(data.message ?? '删除失败')
        }
      } catch (error) {
        message.error('删除失败')
      }
    },
  })
}

const goDetail = (record: API.User) => {
  if (!record.id) return
  const targetId = typeof record.id === 'string' ? record.id : record.id.toString()
  router.push({
    name: '用户详情',
    params: { id: targetId },
  })
}

onMounted(() => {
  fetchUsers()
})
</script>

<template>
  <div class="user-manage">
    <a-card class="user-manage__card" title="用户筛选">
      <a-form :model="queryForm" layout="inline">
        <a-form-item label="账号">
          <a-input v-model:value="queryForm.userAccount" allow-clear placeholder="请输入账号" />
        </a-form-item>
        <a-form-item label="名称">
          <a-input v-model:value="queryForm.userName" allow-clear placeholder="请输入名称" />
        </a-form-item>
        <a-form-item label="邮箱">
          <a-input v-model:value="queryForm.userEmail" allow-clear placeholder="请输入邮箱" />
        </a-form-item>
        <a-form-item label="角色">
          <a-select
            v-model:value="queryForm.userRole"
            allow-clear
            placeholder="请选择角色"
            style="min-width: 120px"
          >
            <a-select-option value="user">普通用户</a-select-option>
            <a-select-option value="admin">管理员</a-select-option>
          </a-select>
        </a-form-item>
        <a-form-item label="状态">
          <a-select
            v-model:value="queryForm.userStatus"
            allow-clear
            placeholder="请选择状态"
            style="min-width: 120px"
          >
            <a-select-option :value="1">正常</a-select-option>
            <a-select-option :value="0">禁用</a-select-option>
          </a-select>
        </a-form-item>
        <a-form-item label="会员">
          <a-select
            v-model:value="queryForm.isVip"
            allow-clear
            placeholder="是否会员"
            style="min-width: 120px"
          >
            <a-select-option :value="1">VIP</a-select-option>
            <a-select-option :value="0">普通</a-select-option>
          </a-select>
        </a-form-item>
        <a-form-item>
          <a-space>
            <a-button type="primary" @click="handleSearch">搜索</a-button>
            <a-button @click="handleReset">重置</a-button>
            <a-button type="dashed" @click="openAddModal">新增用户</a-button>
          </a-space>
        </a-form-item>
      </a-form>
    </a-card>

    <a-card class="user-manage__card" title="用户列表">
      <a-table
        :data-source="tableData"
        :loading="loading"
        :pagination="{
          current: pagination.current,
          pageSize: pagination.pageSize,
          total: pagination.total,
        }"
        row-key="id"
        @change="handleTableChange"
      >
        <a-table-column title="ID" dataIndex="id" key="id" width="80" />
        <a-table-column title="账号" dataIndex="userAccount" key="userAccount" />
        <a-table-column title="名称" dataIndex="userName" key="userName" />
        <a-table-column title="邮箱" dataIndex="userEmail" key="userEmail" />
        <a-table-column title="角色" dataIndex="userRole" key="userRole">
          <template #default="{ text }">
            <a-tag :color="text === 'admin' ? 'magenta' : 'blue'">{{
              text === 'admin' ? '管理员' : '普通用户'
            }}</a-tag>
          </template>
        </a-table-column>
        <a-table-column title="状态" dataIndex="userStatus" key="userStatus">
          <template #default="{ text }">
            <a-tag :color="text === 1 ? 'green' : 'red'">{{ text === 1 ? '正常' : '禁用' }}</a-tag>
          </template>
        </a-table-column>
        <a-table-column title="会员" dataIndex="isVip" key="isVip">
          <template #default="{ text }">
            <a-tag :color="text === 1 ? 'gold' : 'default'">{{
              text === 1 ? 'VIP' : '普通会员'
            }}</a-tag>
          </template>
        </a-table-column>
        <a-table-column title="最近登录" dataIndex="lastLoginTime" key="lastLoginTime">
          <template #default="{ text }">{{ formatDate(text) }}</template>
        </a-table-column>
        <a-table-column title="创建时间" dataIndex="createTime" key="createTime">
          <template #default="{ text }">{{ formatDate(text) }}</template>
        </a-table-column>
        <a-table-column key="actions" title="操作" width="160">
          <template #default="{ record }">
            <a-space>
              <a-button type="link" @click="goDetail(record)">详情</a-button>
              <a-button danger type="link" @click="handleDelete(record)">删除</a-button>
            </a-space>
          </template>
        </a-table-column>
      </a-table>
    </a-card>

    <a-modal
      v-model:open="addModalVisible"
      title="新增用户"
      width="480px"
      :destroy-on-close="true"
      @cancel="closeAddModal"
      @ok="submitAddForm"
    >
      <a-form
        ref="addFormRef"
        :model="addForm"
        :rules="addFormRules"
        :label-col="{ span: 6 }"
        :wrapper-col="{ span: 16 }"
      >
        <a-form-item label="账号" name="userAccount">
          <a-input v-model:value="addForm.userAccount" placeholder="请输入账号" />
        </a-form-item>
        <a-form-item label="密码" name="userPassword">
          <a-input-password v-model:value="addForm.userPassword" placeholder="请输入密码" />
        </a-form-item>
        <a-form-item label="名称" name="userName">
          <a-input v-model:value="addForm.userName" placeholder="请输入名称" />
        </a-form-item>
        <a-form-item label="邮箱" name="userEmail">
          <a-input v-model:value="addForm.userEmail" placeholder="请输入邮箱" />
        </a-form-item>
        <a-form-item label="角色" name="userRole">
          <a-select v-model:value="addForm.userRole" placeholder="请选择角色">
            <a-select-option value="user">普通用户</a-select-option>
            <a-select-option value="admin">管理员</a-select-option>
          </a-select>
        </a-form-item>
        <a-form-item label="状态" name="userStatus">
          <a-select v-model:value="addForm.userStatus" placeholder="请选择状态">
            <a-select-option :value="1">正常</a-select-option>
            <a-select-option :value="0">禁用</a-select-option>
          </a-select>
        </a-form-item>
        <a-form-item label="会员" name="isVip">
          <a-select v-model:value="addForm.isVip" placeholder="是否会员">
            <a-select-option :value="0">普通</a-select-option>
            <a-select-option :value="1">VIP</a-select-option>
          </a-select>
        </a-form-item>
      </a-form>
    </a-modal>
  </div>
</template>

<style scoped>
.user-manage {
  display: flex;
  flex-direction: column;
  gap: 24px;
}

.user-manage__card {
  background: #ffffff;
}

:deep(.ant-form-item) {
  margin-bottom: 16px;
}
</style>
