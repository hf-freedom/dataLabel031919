import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import { userApi } from '../api/user'

export const useUserStore = defineStore('user', () => {
  let user = null
  const isLoggedIn = computed(() => !!user)
  const isAdmin = computed(() => user?.userType === 1)

  const setUser = (userData) => {
    user = userData
    if (userData) {
      sessionStorage.setItem('user', JSON.stringify(userData))
    } else {
      sessionStorage.removeItem('user')
    }
  }

  const loadUser = () => {
    const savedUser = sessionStorage.getItem('user')
    if (savedUser) {
      user = JSON.parse(savedUser)
    }
  }

  const logout = async () => {
    try {
      await userApi.logout()
    } catch (e) {
      console.error('Logout error:', e)
    }
    setUser(null)
  }

  return {
    user,
    isLoggedIn,
    isAdmin,
    setUser,
    loadUser,
    logout
  }
})
