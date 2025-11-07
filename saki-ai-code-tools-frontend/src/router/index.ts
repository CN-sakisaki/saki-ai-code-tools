import { createRouter, createWebHistory } from 'vue-router'

import ACCESS_ENUM from '@/access/accessEnum'
import HomeView from '../views/HomeView.vue'
import NoAuth from '@/views/NoAuth.vue'
import UserLogin from '@/views/user/UserLogin.vue'
import UserRegister from '@/views/user/UserRegister.vue'
import UserManagePage from '@/views/user/UserManagePage.vue'
import UserDetailPage from '@/views/user/UserDetailPage.vue'
import UserProfilePage from '@/views/user/UserProfilePage.vue'
import AppChatPage from '@/views/app/AppChatPage.vue'
import AppManagePage from '@/views/app/AppManagePage.vue'
import AppEditPage from '@/views/app/AppEditPage.vue'

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
      path: '/user/userManage/:id',
      name: '用户详情',
      component: UserDetailPage,
      meta: {
        label: '用户详情',
        hideInMenu: true,
        access: ACCESS_ENUM.ADMIN,
      },
    },
    {
      path: '/user/profile',
      name: '个人中心',
      component: UserProfilePage,
      meta: {
        label: '个人中心',
        hideInMenu: true,
        access: ACCESS_ENUM.USER,
      },
    },
    {
      path: '/app/manage',
      name: '应用管理',
      component: AppManagePage,
      meta: {
        label: '应用管理',
        showInMenu: true,
        access: ACCESS_ENUM.ADMIN,
      },
    },
    {
      path: '/app/:id/chat',
      name: 'appChat',
      component: AppChatPage,
      meta: {
        label: '应用对话',
        hideInMenu: true,
        access: ACCESS_ENUM.USER,
      },
    },
    {
      path: '/app/:id/edit',
      name: 'appEdit',
      component: AppEditPage,
      meta: {
        label: '编辑应用',
        hideInMenu: true,
        access: ACCESS_ENUM.USER,
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
