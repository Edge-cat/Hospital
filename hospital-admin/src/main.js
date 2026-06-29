import { createApp } from 'vue'
import { createPinia } from 'pinia'
import ElementPlus from 'element-plus'
import zhCn from 'element-plus/dist/locale/zh-cn.mjs'
import * as ElementPlusIconsVue from '@element-plus/icons-vue'
import 'element-plus/dist/index.css'
import './styles/feishu-theme.css'

import App from './App.vue'
import router from './router'

import PageContainer from '@/components/PageContainer.vue'
import PageHeader from '@/components/PageHeader.vue'
import SearchToolbar from '@/components/SearchToolbar.vue'
import TableCard from '@/components/TableCard.vue'
import DataTable from '@/components/DataTable.vue'
import AppPagination from '@/components/AppPagination.vue'
import ListPage from '@/components/ListPage.vue'
import FormCard from '@/components/FormCard.vue'
import FormDialog from '@/components/FormDialog.vue'
import StatOverview from '@/components/StatOverview.vue'
import StatusTag from '@/components/StatusTag.vue'
import DictSelect from '@/components/DictSelect.vue'

if (import.meta.env.DEV && import.meta.env.VITE_USE_MOCK === 'true') {
  import('./mock')
}

const app = createApp(App)

for (const [key, component] of Object.entries(ElementPlusIconsVue)) {
  app.component(key, component)
}

const globals = {
  PageContainer,
  PageHeader,
  SearchToolbar,
  TableCard,
  DataTable,
  AppPagination,
  ListPage,
  FormCard,
  FormDialog,
  StatOverview,
  StatusTag,
  DictSelect
}
Object.entries(globals).forEach(([name, comp]) => app.component(name, comp))

app.use(createPinia())
app.use(router)
app.use(ElementPlus, { locale: zhCn })
app.mount('#app')
