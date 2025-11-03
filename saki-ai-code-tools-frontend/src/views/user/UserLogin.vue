<script lang="ts" setup>
import { computed, onBeforeUnmount, reactive, ref } from 'vue'
import type { FormInstance } from 'ant-design-vue'
import { message } from 'ant-design-vue'
import type { Rule } from 'ant-design-vue/es/form'
import { useRoute, useRouter } from 'vue-router'

import backgroundVideo from '@/assets/background.mp4'
import logo from '@/assets/logo.png'
import { login, sendEmailLoginCode } from '@/api/userController'
import { useLoginUserStore } from '@/stores/loginUser'

type LoginTabKey = 'accountPassword' | 'emailCode'

type LoginFormMap = Record<LoginTabKey, Record<string, string>>

type FormRulesMap = Record<string, Rule[]>

type LoginType = 'ACCOUNT_PASSWORD' | 'EMAIL_CODE'

// 定义 LoginRequest 类型以替代 API.LoginRequest
type LoginRequest = {
  loginType?: string
  userAccount?: string
  userPassword?: string
  userPhone?: string
  userEmail?: string
  loginCode?: string
}

type LoginMeta = {
  key: LoginTabKey
  label: string
  description: string
  loginType: LoginType
}

const loginTabMeta: Record<LoginTabKey, LoginMeta> = {
  accountPassword: {
    key: 'accountPassword',
    label: '账号密码登录',
    description: '使用账号与密码快速登录',
    loginType: 'ACCOUNT_PASSWORD',
  },
  emailCode: {
    key: 'emailCode',
    label: '邮箱验证码登录',
    description: '通过邮箱验证码安全登录',
    loginType: 'EMAIL_CODE',
  },
}

const router = useRouter()
const route = useRoute()
const loginUserStore = useLoginUserStore()

const activeKey = ref<LoginTabKey>('accountPassword')
const currentTabMeta = computed(() => loginTabMeta[activeKey.value])
const loading = ref(false)

const formRefs = reactive<Record<LoginTabKey, FormInstance | null>>({
  accountPassword: null,
  emailCode: null,
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
}

const emailCountdown = ref(0)
let emailTimer: number | undefined

const startCountdown = () => {
  emailCountdown.value = 60
  const timer = window.setInterval(() => {
    emailCountdown.value -= 1
    if (emailCountdown.value <= 0) {
      window.clearInterval(timer)
      emailCountdown.value = 0
    }
  }, 1000)

  if (emailTimer) {
    window.clearInterval(emailTimer)
  }
  emailTimer = timer
}

const sendEmailCode = async () => {
  const form = formRefs.emailCode
  if (!form) {
    return
  }
  try {
    await form.validateFields(['userEmail'])
    const requestBody: LoginRequest = {
      loginType: 'EMAIL_CODE',
      userEmail: loginForms.emailCode.userEmail,
    }
    const { data } = await sendEmailLoginCode(requestBody)
    if (data.code === 0) {
      message.success('验证码发送成功，请注意查收邮箱')
      startCountdown()
    } else {
      message.error(data.message ?? '验证码发送失败')
    }
  } catch (error: unknown) {
    if (error && typeof error === 'object' && 'errorFields' in error) {
      // 表单校验失败
      return
    }
    message.error('验证码发送失败')
  }
}

const buildLoginPayload = (key: LoginTabKey): LoginRequest => {
  const base: LoginRequest = {
    loginType: loginTabMeta[key].loginType,
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
  } catch {
    return
  }

  loading.value = true
  try {
    const payload = buildLoginPayload(key)
    const { data } = await login(payload)
    if (data.code === 0 && data.data) {
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
  } catch {
    message.error('登录失败，请稍后重试')
  } finally {
    loading.value = false
  }
}

onBeforeUnmount(() => {
  if (emailTimer) {
    window.clearInterval(emailTimer)
  }
})
</script>

<template>
  <div class="auth-page">
    <video autoplay class="auth-page__background" loop muted playsinline>
      <source :src="backgroundVideo" type="video/mp4" />
    </video>
    <div class="auth-page__overlay"></div>
    <div class="auth-page__content">
      <div class="auth-card">
        <div class="auth-card__brand">
          <img :src="logo" alt="SaKi酱AI代码生成工具 logo" class="auth-card__brand-logo" />
          <h1 class="auth-card__brand-title">AI代码生成工具</h1>
        </div>

        <a-tabs v-model:activeKey="activeKey" class="auth-card__tabs">
          <a-tab-pane key="accountPassword" :tab="loginTabMeta.accountPassword.label" />
          <a-tab-pane key="emailCode" :tab="loginTabMeta.emailCode.label" />
        </a-tabs>

        <p class="auth-card__description">{{ currentTabMeta.description }}</p>

        <a-form
          :key="activeKey"
          :ref="setFormRef(activeKey)"
          :model="loginForms[activeKey]"
          :rules="rules[activeKey]"
          autocomplete="off"
          layout="vertical"
          class="auth-card__form"
          @finish="() => handleSubmit(activeKey)"
        >
          <template v-if="activeKey === 'accountPassword'">
            <a-form-item label="账号" name="userAccount">
              <a-input
                v-model:value="loginForms.accountPassword.userAccount"
                placeholder="请输入账号"
                size="large"
              >
                <template #prefix>
                  <span aria-hidden="true" class="auth-input__icon">
                    <svg viewBox="0 0 24 24" xmlns="http://www.w3.org/2000/svg">
                      <path
                        d="M12 12a4 4 0 1 0 0-8 4 4 0 0 0 0 8Zm0 2c-4.42 0-8 2.24-8 5v1h16v-1c0-2.76-3.58-5-8-5Z"
                        fill="currentColor"
                      />
                    </svg>
                  </span>
                </template>
              </a-input>
            </a-form-item>
            <a-form-item label="密码" name="userPassword">
              <a-input-password
                v-model:value="loginForms.accountPassword.userPassword"
                placeholder="请输入密码"
                size="large"
              >
                <template #prefix>
                  <span aria-hidden="true" class="auth-input__icon">
                    <svg viewBox="0 0 24 24" xmlns="http://www.w3.org/2000/svg">
                      <path
                        d="M17 9h-1V7a4 4 0 0 0-8 0v2H7a2 2 0 0 0-2 2v8a2 2 0 0 0 2 2h10a2 2 0 0 0 2-2v-8a2 2 0 0 0-2-2Zm-6 8a1 1 0 0 1-2 0v-2a1 1 0 1 1 2 0v2Zm3-8H10V7a2 2 0 0 1 4 0v2Z"
                        fill="currentColor"
                      />
                    </svg>
                  </span>
                </template>
              </a-input-password>
            </a-form-item>
          </template>

          <template v-else-if="activeKey === 'emailCode'">
            <a-form-item label="邮箱" name="userEmail">
              <a-input
                v-model:value="loginForms.emailCode.userEmail"
                placeholder="请输入邮箱"
                size="large"
              >
                <template #prefix>
                  <span aria-hidden="true" class="auth-input__icon">
                    <svg viewBox="0 0 24 24" xmlns="http://www.w3.org/2000/svg">
                      <path
                        d="M4 6a2 2 0 0 0-2 2v8a2 2 0 0 0 2 2h16a2 2 0 0 0 2-2V8a2 2 0 0 0-2-2H4Zm8 6L4 8h16l-8 4Zm0 2 8-4.8V16H4v-6.8L12 14Z"
                        fill="currentColor"
                      />
                    </svg>
                  </span>
                </template>
              </a-input>
            </a-form-item>
            <a-form-item label="验证码" name="loginCode">
              <div class="auth-card__code-row">
                <a-input
                  v-model:value="loginForms.emailCode.loginCode"
                  class="auth-card__code-input"
                  placeholder="请输入验证码"
                  size="large"
                >
                  <template #prefix>
                    <span aria-hidden="true" class="auth-input__icon">
                      <svg viewBox="0 0 24 24" xmlns="http://www.w3.org/2000/svg">
                        <path
                          d="M12 2 4 5v6c0 5.25 3.4 10.05 8 11 4.6-.95 8-5.75 8-11V5l-8-3Zm0 4.18 4 1.5V11c0 3.44-1.91 6.83-4 7.64-2.09-.81-4-4.2-4-7.64V7.68l4-1.5Zm-2 5.82 2 2 3-3-1.4-1.42-1.6 1.58-.6-.58L10 12Z"
                          fill="currentColor"
                        />
                      </svg>
                    </span>
                  </template>
                </a-input>
                <a-button
                  :disabled="emailCountdown > 0"
                  class="auth-card__code-button"
                  ghost
                  size="large"
                  type="primary"
                  @click="sendEmailCode"
                >
                  {{ emailCountdown > 0 ? `${emailCountdown}s后重试` : '发送验证码' }}
                </a-button>
              </div>
            </a-form-item>
          </template>

          <div class="auth-card__actions">
            <div class="auth-card__actions-top">
              <RouterLink class="auth-card__link" to="/user/register"
                >没有账号？立即注册</RouterLink
              >
            </div>
            <a-button :loading="loading" block html-type="submit" size="large" type="primary">
              登录
            </a-button>
          </div>
        </a-form>
      </div>
    </div>
  </div>
</template>

<style scoped>
.auth-page {
  position: relative;
  min-height: 100vh;
  overflow: hidden;
  color: #ffffff;
}

.auth-page__background {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.auth-page__overlay {
  position: absolute;
  inset: 0;
  background: linear-gradient(
    135deg,
    rgba(22, 24, 61, 0.85) 0%,
    rgba(14, 11, 40, 0.7) 45%,
    rgba(6, 4, 24, 0.82) 100%
  );
}

.auth-page__content {
  position: relative;
  z-index: 1;
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: flex-end;
  padding: clamp(48px, 8vw, 120px) clamp(32px, 10vw, 160px);
  box-sizing: border-box;
}

.auth-card {
  width: min(420px, 100%);
  margin-left: auto;
  padding: 32px clamp(24px, 6vw, 40px);
  border-radius: 22px;
  border: 1px solid rgba(255, 255, 255, 0.25);
  background: rgba(255, 255, 255, 0.12);
  backdrop-filter: blur(16px);
  box-shadow:
    0 28px 50px rgba(5, 7, 19, 0.55),
    0 0 65px rgba(118, 54, 255, 0.4);
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.auth-card__brand {
  display: flex;
  align-items: center;
  gap: 14px;
}

.auth-card__brand-logo {
  width: 100px;
  height: 100px;
  object-fit: contain;
}

.auth-card__brand-title {
  margin: 0;
  font-size: 28px;
  font-weight: 700;
  letter-spacing: 1px;
  color: #ffffff;
}

.auth-card__tabs {
  width: 100%;
}

.auth-card__description {
  margin: 4px 0 0;
  color: rgba(255, 255, 255, 0.75);
  font-size: 14px;
}

.auth-card__form {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.auth-card__code-row {
  display: flex;
  gap: 12px;
  align-items: center;
}

.auth-card__code-input {
  flex: 1 1 auto;
}

.auth-card__code-button {
  min-width: 138px;
  border-radius: 10px;
}

.auth-card__actions {
  display: flex;
  flex-direction: column;
  gap: 16px;
  margin-top: 16px;
}

.auth-card__actions-top {
  display: flex;
  align-items: center;
  justify-content: flex-end;
  gap: 12px;
}

.auth-card__link {
  align-self: flex-end;
  color: rgba(255, 255, 255, 0.85);
  font-size: 13px;
}

.auth-card__link:hover {
  color: #ffffff;
}

.auth-input__icon {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  color: rgba(255, 255, 255, 0.85);
}

.auth-input__icon svg {
  width: 18px;
  height: 18px;
}

:deep(.auth-card__tabs .ant-tabs-nav) {
  margin-bottom: 20px;
}

:deep(.auth-card__tabs .ant-tabs-nav::before) {
  border-bottom-color: rgba(255, 255, 255, 0.2);
}

:deep(.auth-card__tabs .ant-tabs-tab) {
  color: rgba(255, 255, 255, 0.75);
  font-size: 15px;
  padding: 12px 24px;
}

:deep(.auth-card__tabs .ant-tabs-tab:hover) {
  color: rgba(255, 255, 255, 0.9);
}

:deep(.auth-card__tabs .ant-tabs-tab-active .ant-tabs-tab-btn) {
  color: #ffffff;
  font-weight: 600;
}

:deep(.auth-card__tabs .ant-tabs-ink-bar) {
  background: rgba(255, 255, 255, 0.85);
  height: 3px;
}

:deep(.auth-card__tabs .ant-tabs-content-holder) {
  padding-top: 0;
}

:deep(.ant-form-item-label > label) {
  color: rgba(255, 255, 255, 0.9);
  font-weight: 500;
}

:deep(.ant-form-item) {
  margin-bottom: 18px;
}

:deep(.ant-input),
:deep(.ant-input-affix-wrapper) {
  background: rgba(255, 255, 255, 0.08);
  border-color: rgba(255, 255, 255, 0.3);
  color: #ffffff;
}

:deep(.ant-input-affix-wrapper:hover),
:deep(.ant-input-affix-wrapper-focused) {
  border-color: rgba(186, 140, 255, 0.9);
  box-shadow: 0 0 0 2px rgba(135, 92, 255, 0.35);
}

:deep(.ant-input::placeholder),
:deep(.ant-input-affix-wrapper input::placeholder) {
  color: rgba(255, 255, 255, 0.55);
}

:deep(.ant-input-prefix) {
  margin-right: 8px;
}

:deep(.ant-input-password-icon),
:deep(.anticon-eye-invisible) {
  color: rgba(255, 255, 255, 0.7);
}

:deep(.ant-btn-primary.auth-card__code-button) {
  border-color: rgba(255, 255, 255, 0.75);
  color: #ffffff;
  box-shadow: 0 10px 25px rgba(118, 54, 255, 0.45);
}

:deep(.ant-btn-primary.auth-card__code-button[disabled]) {
  border-color: rgba(255, 255, 255, 0.4);
  box-shadow: none;
  color: rgba(255, 255, 255, 0.65);
}

:deep(.ant-btn-primary) {
  box-shadow: 0 12px 30px rgba(135, 92, 255, 0.4);
}

@media (max-width: 960px) {
  .auth-page__content {
    justify-content: center;
    padding: 72px 32px;
  }

  .auth-card {
    margin-left: 0;
  }
}

@media (max-width: 560px) {
  .auth-card {
    padding: 28px 20px;
    border-radius: 18px;
  }

  .auth-card__brand {
    gap: 12px;
  }

  .auth-card__brand-logo {
    width: 100px;
    height: 100px;
  }

  .auth-card__code-row {
    flex-direction: column;
    align-items: stretch;
  }

  .auth-card__code-button {
    width: 100%;
  }
}
</style>
