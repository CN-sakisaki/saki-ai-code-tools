import { defineStore } from 'pinia'
import { ref } from 'vue'

import ACCESS_ENUM from '@/access/accessEnum'
import { getUserInfo, logout as logoutApi } from '@/api/userController'

export const useLoginUserStore = defineStore('loginUser', () => {
  const currentUser = ref<API.UserVO | null>(null)
  const loading = ref(false)
  const loggingOut = ref(false)

  const setUser = (user: API.UserVO | null) => {
    currentUser.value = user
  }

  const setNotLogin = () => {
    currentUser.value = null
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

  const logout = async () => {
    if (loggingOut.value) {
      return
    }
    loggingOut.value = true
    try {
      await logoutApi()
    } catch (error) {
      // ignore logout errors
    } finally {
      loggingOut.value = false
      setNotLogin()
    }
  }

  return {
    currentUser,
    loading,
    loggingOut,
    setUser,
    setNotLogin,
    fetchUser,
    logout,
  }
})
