<script lang="ts" setup>
import { reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { message, Modal } from 'ant-design-vue'

import { adminDeleteApps, adminListApps, adminUpdateApp } from '@/api/appController'

interface TablePagination {
  current: number
  pageSize: number
  total: number
}

const router = useRouter()

const loading = ref(false)
const tableData = ref<API.App[]>([])
const pagination = reactive<TablePagination>({
  current: 1,
  pageSize: 20,
  total: 0,
})

const queryForm = reactive<API.AppAdminQueryRequest>({
  pageNum: 1,
  pageSize: 20,
  id: undefined,
  appName: undefined,
  codeGenType: undefined,
  deployKey: undefined,
  priority: undefined,
  userId: undefined,
})

const fetchApps = async () => {
  loading.value = true
  try {
    const { data } = await adminListApps({
      ...queryForm,
      pageNum: pagination.current,
      pageSize: pagination.pageSize,
    })
    if (data.code === 0 && data.data) {
      tableData.value = data.data.records ?? []
      pagination.total = data.data.totalRow ?? 0
    } else {
      message.error(data.message || '获取应用列表失败')
    }
  } catch (error) {
    message.error('获取应用列表失败')
  } finally {
    loading.value = false
  }
}

const handleTableChange = (pag: { current: number; pageSize: number }) => {
  pagination.current = pag.current
  pagination.pageSize = pag.pageSize
  fetchApps()
}

const handleSearch = () => {
  pagination.current = 1
  fetchApps()
}

const handleReset = () => {
  queryForm.id = undefined
  queryForm.appName = undefined
  queryForm.codeGenType = undefined
  queryForm.deployKey = undefined
  queryForm.priority = undefined
  queryForm.userId = undefined
  pagination.current = 1
  fetchApps()
}

const goEdit = (record: API.App) => {
  if (!record.id) return
  router.push({ name: 'appEdit', params: { id: String(record.id) } })
}

const handleDelete = (record: API.App) => {
  if (!record.id) return
  Modal.confirm({
    title: '删除应用',
    content: `确定删除应用「${record.appName || record.id}」吗？`,
    okText: '删除',
    okButtonProps: { danger: true },
    cancelText: '取消',
    async onOk() {
      try {
        const { data } = await adminDeleteApps({ ids: [record.id!] })
        if (data.code === 0) {
          message.success('删除成功')
          fetchApps()
        } else {
          message.error(data.message || '删除失败')
        }
      } catch (error) {
        message.error('删除失败')
      }
    },
  })
}

const handleFeature = async (record: API.App) => {
  if (!record.id) return
  try {
    const { data } = await adminUpdateApp(
      { id: record.id },
      {
        priority: 1,
      },
    )
    if (data.code === 0) {
      message.success('已设置为精选应用')
      fetchApps()
    } else {
      message.error(data.message || '设置失败')
    }
  } catch (error) {
    message.error('设置失败')
  }
}

fetchApps()
</script>

<template>
  <div class="app-manage">
    <a-card class="app-manage__card" title="应用筛选">
      <a-form :model="queryForm" layout="inline">
        <a-form-item label="应用ID">
          <a-input v-model:value="queryForm.id" allow-clear placeholder="请输入应用ID" />
        </a-form-item>
        <a-form-item label="应用名称">
          <a-input v-model:value="queryForm.appName" allow-clear placeholder="请输入应用名称" />
        </a-form-item>
        <a-form-item label="生成类型">
          <a-input v-model:value="queryForm.codeGenType" allow-clear placeholder="请输入生成类型" />
        </a-form-item>
        <a-form-item label="部署标识">
          <a-input v-model:value="queryForm.deployKey" allow-clear placeholder="请输入部署标识" />
        </a-form-item>
        <a-form-item label="优先级">
          <a-input-number
            v-model:value="queryForm.priority"
            :min="0"
            placeholder="请输入优先级"
            style="width: 160px"
          />
        </a-form-item>
        <a-form-item label="用户ID">
          <a-input v-model:value="queryForm.userId" allow-clear placeholder="请输入用户ID" />
        </a-form-item>
        <a-form-item>
          <a-space>
            <a-button type="primary" @click="handleSearch">搜索</a-button>
            <a-button @click="handleReset">重置</a-button>
          </a-space>
        </a-form-item>
      </a-form>
    </a-card>

    <a-card class="app-manage__card" title="应用列表">
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
        <a-table-column title="封面" key="cover" width="120">
          <template #default="{ record }">
            <a-image
              v-if="record.cover"
              :src="record.cover"
              :width="80"
              :height="50"
              :preview="false"
              style="object-fit: cover; border-radius: 8px"
            />
            <span v-else>—</span>
          </template>
        </a-table-column>
        <a-table-column title="名称" dataIndex="appName" key="appName" />
        <a-table-column title="生成类型" dataIndex="codeGenType" key="codeGenType" />
        <a-table-column title="部署标识" dataIndex="deployKey" key="deployKey" />
        <a-table-column title="优先级" dataIndex="priority" key="priority" />
        <a-table-column title="用户ID" dataIndex="userId" key="userId" />
        <a-table-column title="更新时间" dataIndex="updateTime" key="updateTime" />
        <a-table-column key="actions" title="操作" width="220">
          <template #default="{ record }">
            <a-space>
              <a-button type="link" @click="goEdit(record)">编辑</a-button>
              <a-button type="link" @click="handleFeature(record)">精选</a-button>
              <a-button danger type="link" @click="handleDelete(record)">删除</a-button>
            </a-space>
          </template>
        </a-table-column>
      </a-table>
    </a-card>
  </div>
</template>

<style scoped>
.app-manage {
  display: flex;
  flex-direction: column;
  gap: 24px;
}

.app-manage__card {
  background: #ffffff;
}

:deep(.ant-form-item) {
  margin-bottom: 16px;
}
</style>
