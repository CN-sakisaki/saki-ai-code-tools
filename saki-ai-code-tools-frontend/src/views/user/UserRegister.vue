<script lang="ts" setup>
import { reactive, ref } from 'vue'
import type { FormInstance } from 'ant-design-vue'
import { message } from 'ant-design-vue'
import type { Rule } from 'ant-design-vue/es/form'
import { useRouter } from 'vue-router'

import { register } from '@/api/userController'

const router = useRouter()

const formRef = ref<FormInstance | null>(null)
const loading = ref(false)

const formState = reactive({
  userAccount: '',
  userPassword: '',
  confirmPassword: '',
  inviteCode: '',
})

type FormRulesMap = Record<string, Rule[]>

const rules: FormRulesMap = {
  userAccount: [
    { required: true, message: '请输入账号', trigger: 'blur' },
    {
      min: 4,
      max: 32,
      message: '账号长度需在 4-32 个字符',
      trigger: 'blur',
    },
  ],
  userPassword: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    {
      min: 6,
      message: '密码长度至少 6 位',
      trigger: 'blur',
    },
  ],
  confirmPassword: [
    { required: true, message: '请确认密码', trigger: 'blur' },
    {
      validator: (_rule: Rule, value: string) => {
        if (!value) {
          return Promise.reject('请确认密码')
        }
        if (value !== formState.userPassword) {
          return Promise.reject('两次输入的密码不一致')
        }
        return Promise.resolve()
      },
      trigger: 'blur',
    },
  ],
}

const handleSubmit = async () => {
  if (!formRef.value) {
    return
  }
  try {
    await formRef.value.validate()
  } catch (error) {
    return
  }

  loading.value = true
  try {
    const payload: API.RegisterRequest = {
      userAccount: formState.userAccount,
      userPassword: formState.userPassword,
      confirmPassword: formState.confirmPassword,
      ...(formState.inviteCode ? { inviteCode: formState.inviteCode } : {}),
    }
    const { data } = await register(payload)
    if (data.code === 0) {
      message.success('注册成功，请前往登录')
      router.replace('/user/login')
    } else {
      message.error(data.message ?? '注册失败，请稍后重试')
    }
  } catch (error) {
    message.error('注册失败，请稍后重试')
  } finally {
    loading.value = false
  }
}
</script>

<template>
  <div class="register-page">
    <a-card bordered="false" class="register-card">
      <div class="register-card__header">
        <div>
          <h1 class="register-card__title">创建新账户</h1>
          <p class="register-card__subtitle">完善信息，即刻加入 SaKi 酱 AI 代码生成工具</p>
        </div>
      </div>

      <a-form
        ref="formRef"
        :model="formState"
        :rules="rules"
        autocomplete="off"
        layout="vertical"
        @finish="handleSubmit"
      >
        <a-form-item label="账号" name="userAccount">
          <a-input v-model:value="formState.userAccount" placeholder="请输入账号" />
        </a-form-item>
        <a-form-item label="密码" name="userPassword">
          <a-input-password v-model:value="formState.userPassword" placeholder="请输入密码" />
        </a-form-item>
        <a-form-item label="确认密码" name="confirmPassword">
          <a-input-password
            v-model:value="formState.confirmPassword"
            placeholder="请再次输入密码"
          />
        </a-form-item>
        <a-form-item label="邀请码（可选）" name="inviteCode">
          <a-input v-model:value="formState.inviteCode" placeholder="如有邀请码，请输入" />
        </a-form-item>

        <div class="register-card__actions">
          <RouterLink class="register-card__link" to="/user/login">已有账号？立即登录</RouterLink>
          <a-button :loading="loading" block html-type="submit" size="large" type="primary">
            注册
          </a-button>
        </div>
      </a-form>
    </a-card>
  </div>
</template>

<style scoped>
.register-page {
  min-height: calc(100vh - 160px);
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 48px 16px;
  background: linear-gradient(135deg, #fff1f0 0%, #e6fffb 100%);
}

.register-card {
  width: 100%;
  max-width: 520px;
  box-shadow: 0 18px 45px rgba(15, 23, 42, 0.08);
  border-radius: 16px;
}

.register-card__header {
  margin-bottom: 24px;
}

.register-card__title {
  margin: 0;
  font-size: 28px;
  font-weight: 700;
  color: #1d2129;
}

.register-card__subtitle {
  margin: 4px 0 0;
  color: #86909c;
}

.register-card__actions {
  display: flex;
  flex-direction: column;
  gap: 8px;
  margin-top: 12px;
}

.register-card__link {
  align-self: flex-end;
  font-size: 13px;
}

@media (max-width: 480px) {
  .register-card {
    border-radius: 12px;
  }
}
</style>
