import { defineStore } from 'pinia'
import { ref } from 'vue'

import { getUserInfo } from '@/api/userController'
import { clearAccessToken } from '@/utils/auth'

export const useLoginUserStore = defineStore('loginUser', () => {
  const currentUser = ref<API.UserVO | null>(null)
  const loading = ref(false)

  const setUser = (user: API.UserVO | null) => {
    currentUser.value = user
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
        currentUser.value = null
      }
    } catch (error) {
      currentUser.value = null
    } finally {
      loading.value = false
    }
  }

  const logout = () => {
    clearAccessToken()
    currentUser.value = null
  }

  return {
    currentUser,
    loading,
    setUser,
    fetchUser,
    logout,
  }
})
