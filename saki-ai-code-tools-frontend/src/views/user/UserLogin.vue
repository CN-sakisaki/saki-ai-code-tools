<script lang="ts" setup>
import { onBeforeUnmount, reactive, ref } from 'vue'
import type { FormInstance } from 'ant-design-vue'
import { message } from 'ant-design-vue'
import type { Rule } from 'ant-design-vue/es/form'
import { useRoute, useRouter } from 'vue-router'

import { login, sendEmailLoginCode } from '@/api/userController'
import { useLoginUserStore } from '@/stores/loginUser'
import { setAccessToken } from '@/utils/auth'

type LoginTabKey = 'accountPassword' | 'emailCode' | 'phonePassword' | 'phoneCode'

type LoginFormMap = Record<LoginTabKey, Record<string, string>>

type FormRulesMap = Record<string, Rule[]>

type LoginMeta = {
  key: LoginTabKey
  label: string
  description: string
  loginType: Required<API.LoginRequest>['loginType']
}

const router = useRouter()
const route = useRoute()
const loginUserStore = useLoginUserStore()

const activeKey = ref<LoginTabKey>('accountPassword')
const loading = ref(false)

const formRefs = reactive<Record<LoginTabKey, FormInstance | null>>({
  accountPassword: null,
  emailCode: null,
  phonePassword: null,
  phoneCode: null,
})

const setFormRef = (key: LoginTabKey) => (instance: FormInstance | null) => {
  formRefs[key] = instance
}

const loginForms = reactive<LoginFormMap>({
  accountPassword: {
    userAccount: '',
    userPassword: '',
  },
  emailCode: {
    userEmail: '',
    loginCode: '',
  },
  phonePassword: {
    userPhone: '',
    userPassword: '',
  },
  phoneCode: {
    userPhone: '',
    loginCode: '',
  },
})

const rules: Record<LoginTabKey, FormRulesMap> = {
  accountPassword: {
    userAccount: [{ required: true, message: '请输入账号' }],
    userPassword: [{ required: true, message: '请输入密码' }],
  },
  emailCode: {
    userEmail: [
      { required: true, message: '请输入邮箱' },
      { type: 'email', message: '邮箱格式不正确' },
    ],
    loginCode: [{ required: true, message: '请输入验证码' }],
  },
  phonePassword: {
    userPhone: [
      { required: true, message: '请输入手机号' },
      { pattern: /^1\d{10}$/, message: '请输入正确的手机号' },
    ],
    userPassword: [{ required: true, message: '请输入密码' }],
  },
  phoneCode: {
    userPhone: [
      { required: true, message: '请输入手机号' },
      { pattern: /^1\d{10}$/, message: '请输入正确的手机号' },
    ],
    loginCode: [{ required: true, message: '请输入验证码' }],
  },
}

const tabs: LoginMeta[] = [
  {
    key: 'accountPassword',
    label: '账号密码登录',
    description: '使用账号与密码快速登录',
    loginType: 'ACCOUNT_PASSWORD',
  },
  {
    key: 'emailCode',
    label: '邮箱验证码登录',
    description: '通过邮箱验证码安全登录',
    loginType: 'EMAIL_CODE',
  },
  {
    key: 'phonePassword',
    label: '手机号密码登录',
    description: '使用绑定手机号与密码登录',
    loginType: 'PHONE_PASSWORD',
  },
  {
    key: 'phoneCode',
    label: '手机号验证码登录',
    description: '手机验证码快捷登录',
    loginType: 'PHONE_CODE',
  },
]

const emailCountdown = ref(0)
const phoneCountdown = ref(0)
let emailTimer: number | undefined
let phoneTimer: number | undefined

const startCountdown = (target: 'email' | 'phone') => {
  const countdownRef = target === 'email' ? emailCountdown : phoneCountdown
  countdownRef.value = 60
  const timer = window.setInterval(() => {
    countdownRef.value -= 1
    if (countdownRef.value <= 0) {
      window.clearInterval(timer)
      countdownRef.value = 0
    }
  }, 1000)

  if (target === 'email') {
    if (emailTimer) {
      window.clearInterval(emailTimer)
    }
    emailTimer = timer
  } else {
    if (phoneTimer) {
      window.clearInterval(phoneTimer)
    }
    phoneTimer = timer
  }
}

const sendEmailCode = async () => {
  const form = formRefs.emailCode
  if (!form) {
    return
  }
  try {
    await form.validateFields(['userEmail'])
    const requestBody: API.LoginRequest = {
      loginType: 'EMAIL_CODE',
      userEmail: loginForms.emailCode.userEmail,
    }
    const { data } = await sendEmailLoginCode(requestBody)
    if (data.code === 0) {
      message.success('验证码发送成功，请注意查收邮箱')
      startCountdown('email')
    } else {
      message.error(data.message ?? '验证码发送失败')
    }
  } catch (error: any) {
    if (error?.errorFields) {
      // 表单校验失败
      return
    }
    message.error('验证码发送失败')
  }
}

const sendPhoneCode = async () => {
  const form = formRefs.phoneCode
  if (!form) {
    return
  }
  try {
    await form.validateFields(['userPhone'])
    message.success('验证码发送成功，请注意查收短信')
    startCountdown('phone')
  } catch (error) {
    // ignore validation errors
  }
}

const buildLoginPayload = (key: LoginTabKey): API.LoginRequest => {
  const base: API.LoginRequest = {
    loginType: tabs.find((tab) => tab.key === key)?.loginType,
  }

  const values = loginForms[key]

  switch (key) {
    case 'accountPassword':
      return {
        ...base,
        userAccount: values.userAccount,
        userPassword: values.userPassword,
      }
    case 'emailCode':
      return {
        ...base,
        userEmail: values.userEmail,
        loginCode: values.loginCode,
      }
    case 'phonePassword':
      return {
        ...base,
        userPhone: values.userPhone,
        userPassword: values.userPassword,
      }
    case 'phoneCode':
      return {
        ...base,
        userPhone: values.userPhone,
        loginCode: values.loginCode,
      }
    default:
      return base
  }
}

const handleSubmit = async (key: LoginTabKey) => {
  const form = formRefs[key]
  if (!form) {
    return
  }
  try {
    await form.validate()
  } catch (error) {
    return
  }

  loading.value = true
  try {
    const payload = buildLoginPayload(key)
    const { data } = await login(payload)
    if (data.code === 0 && data.data) {
      const token = data.data.accessToken
      if (token) {
        setAccessToken(token)
      }
      loginUserStore.setUser(data.data)
      message.success('登录成功')

      const redirect = route.query.redirect
      if (typeof redirect === 'string' && redirect) {
        router.replace(redirect)
      } else {
        router.replace('/')
      }
    } else {
      message.error(data.message ?? '登录失败，请稍后重试')
    }
  } catch (error) {
    message.error('登录失败，请稍后重试')
  } finally {
    loading.value = false
  }
}

onBeforeUnmount(() => {
  if (emailTimer) {
    window.clearInterval(emailTimer)
  }
  if (phoneTimer) {
    window.clearInterval(phoneTimer)
  }
})
</script>

<template>
  <div class="login-page">
    <a-card bordered="false" class="login-card">
      <div class="login-card__header">
        <div>
          <h1 class="login-card__title">欢迎登录</h1>
          <p class="login-card__subtitle">请选择合适的方式登录 SaKi 酱 AI 代码生成工具</p>
        </div>
      </div>

      <a-tabs v-model:activeKey="activeKey" size="large">
        <a-tab-pane v-for="tab in tabs" :key="tab.key" :tab="tab.label">
          <p class="login-card__description">{{ tab.description }}</p>
          <a-form
            :ref="setFormRef(tab.key)"
            :model="loginForms[tab.key]"
            :rules="rules[tab.key]"
            autocomplete="off"
            layout="vertical"
            @finish="() => handleSubmit(tab.key)"
          >
            <template v-if="tab.key === 'accountPassword'">
              <a-form-item label="账号" name="userAccount">
                <a-input
                  v-model:value="loginForms.accountPassword.userAccount"
                  placeholder="请输入账号"
                />
              </a-form-item>
              <a-form-item label="密码" name="userPassword">
                <a-input-password
                  v-model:value="loginForms.accountPassword.userPassword"
                  placeholder="请输入密码"
                />
              </a-form-item>
            </template>

            <template v-else-if="tab.key === 'emailCode'">
              <a-form-item label="邮箱" name="userEmail">
                <a-input v-model:value="loginForms.emailCode.userEmail" placeholder="请输入邮箱" />
              </a-form-item>
              <a-form-item label="验证码" name="loginCode">
                <a-input-group compact>
                  <a-input
                    v-model:value="loginForms.emailCode.loginCode"
                    class="login-card__code-input"
                    placeholder="请输入验证码"
                  />
                  <a-button
                    :disabled="emailCountdown > 0"
                    class="login-card__code-button"
                    type="link"
                    @click="sendEmailCode"
                  >
                    {{ emailCountdown > 0 ? `${emailCountdown}s后重试` : '发送验证码' }}
                  </a-button>
                </a-input-group>
              </a-form-item>
            </template>

            <template v-else-if="tab.key === 'phonePassword'">
              <a-form-item label="手机号" name="userPhone">
                <a-input
                  v-model:value="loginForms.phonePassword.userPhone"
                  placeholder="请输入手机号"
                />
              </a-form-item>
              <a-form-item label="密码" name="userPassword">
                <a-input-password
                  v-model:value="loginForms.phonePassword.userPassword"
                  placeholder="请输入密码"
                />
              </a-form-item>
            </template>

            <template v-else>
              <a-form-item label="手机号" name="userPhone">
                <a-input
                  v-model:value="loginForms.phoneCode.userPhone"
                  placeholder="请输入手机号"
                />
              </a-form-item>
              <a-form-item label="验证码" name="loginCode">
                <a-input-group compact>
                  <a-input
                    v-model:value="loginForms.phoneCode.loginCode"
                    class="login-card__code-input"
                    placeholder="请输入验证码"
                  />
                  <a-button
                    :disabled="phoneCountdown > 0"
                    class="login-card__code-button"
                    type="link"
                    @click="sendPhoneCode"
                  >
                    {{ phoneCountdown > 0 ? `${phoneCountdown}s后重试` : '发送验证码' }}
                  </a-button>
                </a-input-group>
              </a-form-item>
            </template>

            <div class="login-card__actions">
              <RouterLink class="login-card__link" to="/user/register"
                >没有账号？立即注册</RouterLink
              >
              <a-button :loading="loading" block html-type="submit" size="large" type="primary">
                登录
              </a-button>
            </div>
          </a-form>
        </a-tab-pane>
      </a-tabs>
    </a-card>
  </div>
</template>

<style scoped>
.login-page {
  min-height: calc(100vh - 160px);
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 48px 16px;
  background: linear-gradient(135deg, #f0f5ff 0%, #e6fffb 100%);
}

.login-card {
  width: 100%;
  max-width: 520px;
  box-shadow: 0 18px 45px rgba(15, 23, 42, 0.08);
  border-radius: 16px;
}

.login-card__header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 24px;
}

.login-card__title {
  margin: 0;
  font-size: 28px;
  font-weight: 700;
  color: #1d2129;
}

.login-card__subtitle {
  margin: 4px 0 0;
  color: #86909c;
}

.login-card__description {
  margin-bottom: 12px;
  color: #4e5969;
}

.login-card__actions {
  display: flex;
  flex-direction: column;
  align-items: stretch;
  gap: 8px;
  margin-top: 12px;
}

.login-card__link {
  align-self: flex-end;
  font-size: 13px;
}

.login-card__code-input {
  width: calc(100% - 140px);
}

.login-card__code-button {
  width: 140px;
  text-align: right;
}

@media (max-width: 480px) {
  .login-card {
    border-radius: 12px;
  }

  .login-card__code-input {
    width: calc(100% - 120px);
  }

  .login-card__code-button {
    width: 120px;
  }
}
</style>
