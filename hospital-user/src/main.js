import { createApp } from 'vue'
import { createPinia } from 'pinia'
import ElementPlus from 'element-plus'
import zhCn from 'element-plus/dist/locale/zh-cn.mjs'
import * as ElementPlusIconsVue from '@element-plus/icons-vue'
import 'element-plus/dist/index.css'
import './styles/feishu-theme.css'

import App from './App.vue'
import router from './router'
import PageHeader from '@/components/PageHeader.vue'
import TableCard from '@/components/TableCard.vue'
import FormCard from '@/components/FormCard.vue'
import ProfileSidebar from '@/components/ProfileSidebar.vue'
import ProfilePage from '@/components/ProfilePage.vue'

if (import.meta.env.DEV && import.meta.env.VITE_USE_MOCK === 'true') {
  import('./mock')
}

const app = createApp(App)

for (const [key, component] of Object.entries(ElementPlusIconsVue)) {
  app.component(key, component)
}

app.component('PageHeader', PageHeader)
app.component('TableCard', TableCard)
app.component('FormCard', FormCard)
app.component('ProfileSidebar', ProfileSidebar)
app.component('ProfilePage', ProfilePage)

app.use(createPinia())
app.use(router)
app.use(ElementPlus, { locale: zhCn })
app.mount('#app')
