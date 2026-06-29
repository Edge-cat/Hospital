<template>
  <el-card shadow="never" class="profile-sidebar">
    <div class="profile-header">
      <el-avatar :size="72">{{ avatarLetter }}</el-avatar>
      <h3>{{ displayName }}</h3>
      <p v-if="cardNo" class="profile-meta">就诊卡 {{ cardNo }}</p>
      <p v-if="maskedPhone" class="profile-meta">{{ maskedPhone }}</p>
      <p class="profile-tag">就诊人</p>
    </div>
    <el-menu :default-active="active" class="profile-menu" @select="(path) => $router.push(path)">
      <el-menu-item v-for="item in menuItems" :key="item.path" :index="item.path">
        <el-icon><component :is="item.icon" /></el-icon>
        <span>{{ item.title }}</span>
      </el-menu-item>
    </el-menu>
  </el-card>
</template>

<script setup>
import { computed, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { usePatientProfile } from '@/composables/usePatientProfile'

const route = useRoute()
const userStore = useUserStore()
const { profile, load } = usePatientProfile()

const active = computed(() => route.path)
const displayName = computed(() => profile.value?.name || userStore.userName)
const avatarLetter = computed(() => (displayName.value || '游').charAt(0))
const cardNo = computed(() => profile.value?.cardNo || userStore.userInfo?.cardNo)
const maskedPhone = computed(() => {
  const phone = profile.value?.phone || userStore.userInfo?.phone
  if (!phone || phone.length < 7) return ''
  return `${phone.slice(0, 3)}****${phone.slice(-4)}`
})

const menuItems = [
  { path: '/profile', title: '基本信息', icon: 'User' },
  { path: '/my-register', title: '我的挂号', icon: 'Tickets' },
  { path: '/my-appointment', title: '我的预约', icon: 'Calendar' },
  { path: '/records', title: '就诊记录', icon: 'Document' },
  { path: '/payment', title: '在线缴费', icon: 'Money' }
]

onMounted(() => {
  if (!profile.value) load({ silent: true })
})
</script>

<style scoped>
.profile-meta {
  margin: 4px 0 0;
  font-size: 13px;
  color: #8f959e;
}

.profile-tag {
  margin-top: 8px;
}
</style>
