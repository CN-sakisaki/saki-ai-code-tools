import 'ant-design-vue/dist/reset.css'
import './assets/base.css'

import { createApp } from 'vue'
import { createPinia } from 'pinia'
import Antd from 'ant-design-vue'

import App from './App.vue'
import setupAccessGuard from './access'
import router from './router'

const app = createApp(App)

const pinia = createPinia()

app.use(pinia)
setupAccessGuard(router)
app.use(router)
app.use(Antd)

app.mount('#app')
