<script lang="ts" setup>
import { reactive, ref } from 'vue'
import type { FormInstance } from 'ant-design-vue'
import { message } from 'ant-design-vue'
import type { Rule } from 'ant-design-vue/es/form'
import { useRouter } from 'vue-router'

import { register } from '@/api/userController'
import backgroundVideo from '@/assets/background.mp4'
import logo from '@/assets/logo.png'

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
          return Promise.resolve()
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

        <a-form
          ref="formRef"
          :model="formState"
          :rules="rules"
          autocomplete="off"
          layout="vertical"
          class="auth-card__form"
          @finish="handleSubmit"
        >
          <a-form-item label="账号" name="userAccount">
            <a-input v-model:value="formState.userAccount" placeholder="请输入账号" size="large">
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
              v-model:value="formState.userPassword"
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

          <a-form-item label="确认密码" name="confirmPassword">
            <a-input-password
              v-model:value="formState.confirmPassword"
              placeholder="请再次输入密码"
              size="large"
            >
              <template #prefix>
                <span aria-hidden="true" class="auth-input__icon">
                  <svg viewBox="0 0 24 24" xmlns="http://www.w3.org/2000/svg">
                    <path
                      d="M12 2 4 5v6c0 5.25 3.4 10.05 8 11 4.6-.95 8-5.75 8-11V5l-8-3Zm0 4.18 4 1.5V11c0 3.44-1.91 6.83-4 7.64-2.09-.81-4-4.2-4-7.64V7.68l4-1.5Zm-1 5.82L9.6 12.6 8.2 14l3 3 5-5-1.4-1.42L11 11.99Z"
                      fill="currentColor"
                    />
                  </svg>
                </span>
              </template>
            </a-input-password>
          </a-form-item>

          <a-form-item label="邀请码（可选）" name="inviteCode">
            <a-input
              v-model:value="formState.inviteCode"
              placeholder="如有邀请码，请输入"
              size="large"
            >
              <template #prefix>
                <span aria-hidden="true" class="auth-input__icon">
                  <svg viewBox="0 0 24 24" xmlns="http://www.w3.org/2000/svg">
                    <path
                      d="M19 3h-3.18A3 3 0 0 0 9 4.35 3 3 0 0 0 3 7v13l7.5-3 7.5 3V9h1a2 2 0 0 0 0-4Zm-3 14.12-5.5-2.2-5.5 2.2V7a1 1 0 0 1 1-1h10a2.98 2.98 0 0 0 .5 1.65V17.12Zm3-10.12h-1V7a1 1 0 1 1 1 1Z"
                      fill="currentColor"
                    />
                  </svg>
                </span>
              </template>
            </a-input>
          </a-form-item>

          <div class="auth-card__actions">
            <RouterLink class="auth-card__link" to="/user/login">已有账号？立即登录</RouterLink>
            <a-button :loading="loading" block html-type="submit" size="large" type="primary">
              注册
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
  padding: clamp(48px, 8vw, 120px) clamp(32px, 9vw, 150px);
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

.auth-card__form {
  display: flex;
  flex-direction: column;
}

.auth-card__actions {
  display: flex;
  flex-direction: column;
  gap: 12px;
  margin-top: 12px;
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
}
</style>
