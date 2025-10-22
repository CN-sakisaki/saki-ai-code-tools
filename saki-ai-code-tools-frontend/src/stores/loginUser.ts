import { defineStore } from 'pinia'
import { ref } from 'vue'

import ACCESS_ENUM from '@/access/accessEnum'
import { getUserInfo } from '@/api/userController'
import { clearAccessToken } from '@/utils/auth'

export const useLoginUserStore = defineStore('loginUser', () => {
  const currentUser = ref<API.UserVO | null>(null)
  const loading = ref(false)

  const setUser = (user: API.UserVO | null) => {
    currentUser.value = user
  }

  const setNotLogin = () => {
    currentUser.value = {
      userRole: ACCESS_ENUM.NOT_LOGIN,
    } as API.UserVO
  }

  const fetchUser = async () => {
    if (loading.value) {
      return
    }
    loading.value = true
    try {
      const { data } = await getUserInfo()
      if (data?.code === 0 && data.data) {
        currentUser.value = data.data
      } else {
        setNotLogin()
      }
    } catch (error) {
      setNotLogin()
    } finally {
      loading.value = false
    }
  }

  const logout = () => {
    clearAccessToken()
    setNotLogin()
  }

  return {
    currentUser,
    loading,
    setUser,
    setNotLogin,
    fetchUser,
    logout,
  }
})
