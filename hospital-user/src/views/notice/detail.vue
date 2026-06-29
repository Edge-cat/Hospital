<template>
  <div v-loading="loading">
    <el-button class="detail-back" link type="primary" @click="$router.push('/notice')">
      <el-icon><ArrowLeft /></el-icon> 返回公告列表
    </el-button>

    <el-card v-if="notice" shadow="never" class="notice-detail-card">
      <div class="notice-detail-head">
        <el-tag :type="NOTICE_TYPE_TAG[notice.type] || 'info'" size="small">{{ notice.type }}</el-tag>
        <h2>{{ notice.title }}</h2>
      </div>
      <p class="detail-meta">{{ notice.publisher }} · {{ notice.publishTime }}</p>
      <el-divider />
      <div class="detail-content">{{ notice.content }}</div>
    </el-card>
    <el-empty v-else-if="!loading" description="公告不存在" :image-size="80" />
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import { userApi } from '@/api'
import { useNoticeRead } from '@/composables/useNoticeRead'
import { NOTICE_TYPE_TAG } from '@/constants/notice'

const route = useRoute()
const loading = ref(false)
const notice = ref(null)
const { markRead } = useNoticeRead()

onMounted(async () => {
  loading.value = true
  try {
    const res = await userApi.noticeDetail(Number(route.params.id))
    notice.value = res.data || null
    if (notice.value) markRead(notice.value.id)
  } finally {
    loading.value = false
  }
})
</script>

<style scoped>
.notice-detail-head {
  display: flex;
  align-items: center;
  gap: 10px;
  margin-bottom: 8px;
}
.notice-detail-head h2 {
  font-size: 20px;
  font-weight: 600;
  margin: 0;
}
</style>
