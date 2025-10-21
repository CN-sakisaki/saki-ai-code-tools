import { createRouter, createWebHistory } from 'vue-router'

import ACCESS_ENUM from '@/access/accessEnum'
import HomeView from '../views/HomeView.vue'
import NoAuth from '@/views/NoAuth.vue'
import UserLogin from '@/views/user/UserLogin.vue'
import UserRegister from '@/views/user/UserRegister.vue'
import UserManagePage from '@/views/user/UserManagePage.vue'

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes: [
    {
      path: '/',
      name: 'home',
      component: HomeView,
      meta: {
        label: '首页',
        showInMenu: true,
      },
    },
    {
      path: '/user/login',
      name: '用户登录',
      component: UserLogin,
      meta: {
        hideLayout: true,
        hideInMenu: true,
      },
    },
    {
      path: '/user/register',
      name: '用户注册',
      component: UserRegister,
      meta: {
        hideLayout: true,
        hideInMenu: true,
      },
    },
    {
      path: '/user/userManage',
      name: '用户管理',
      component: UserManagePage,
      meta: {
        label: '用户管理',
        showInMenu: true,
        access: ACCESS_ENUM.ADMIN,
      },
    },
    {
      path: '/about',
      name: 'about',
      component: () => import('../views/AboutView.vue'),
      meta: {
        label: '关于我们',
        showInMenu: true,
      },
    },
    {
      path: '/no-auth',
      name: 'noAuth',
      component: NoAuth,
      meta: {
        label: '无权限',
        hideInMenu: true,
      },
    },
  ],
})

export default router
