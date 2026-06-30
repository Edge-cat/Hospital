<template>
  <view class="container">
    <view v-if="list.length === 0" class="empty-tip">暂无公告</view>
    <view v-for="item in list" :key="item.id" class="card notice-card" @click="goDetail(item.id)">
      <view class="notice-top">
        <text class="tag" :class="item.type === '紧急' ? 'tag-danger' : 'tag-primary'">{{ item.type }}</text>
        <text class="notice-time">{{ item.publishTime?.slice(0, 10) }}</text>
      </view>
      <text class="notice-title">{{ item.title }}</text>
      <text class="notice-publisher">发布：{{ item.publisher }}</text>
    </view>
    <PageNav variant="footer" />
  </view>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { mpApi } from '@/api'
import { safeNavigateTo } from '@/utils/nav'
import PageNav from '@/components/PageNav.vue'

const list = ref([])

onMounted(async () => {
  const res = await mpApi.notices()
  list.value = res.data.list
})

function goDetail(id) {
  safeNavigateTo(`/pages/notice/detail?id=${id}`)
}
</script>

<style lang="scss" scoped>
.notice-card { padding: 28rpx; }

.notice-top {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16rpx;
}

.notice-time { font-size: 24rpx; color: #c0c4cc; }

.notice-title {
  display: block;
  font-size: 32rpx;
  font-weight: 600;
  color: #303133;
  line-height: 1.5;
}

.notice-publisher {
  display: block;
  font-size: 24rpx;
  color: #909399;
  margin-top: 16rpx;
}
</style>
