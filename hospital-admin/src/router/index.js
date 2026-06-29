import { createRouter, createWebHistory } from 'vue-router'
import { constantRoutes } from './constantRoutes'
import { setupPermissionGuard } from './permission'

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes: constantRoutes
})

setupPermissionGuard(router)

export default router
