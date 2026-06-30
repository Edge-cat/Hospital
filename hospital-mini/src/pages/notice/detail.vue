<template>
  <view class="container">
    <view class="card">
      <text class="title">{{ notice.title }}</text>
      <view class="meta">
        <text class="tag tag-primary">{{ notice.type }}</text>
        <text class="meta-text">{{ notice.publisher }} · {{ notice.publishTime }}</text>
      </view>
      <view class="divider" />
      <text class="content">{{ notice.content }}</text>
    </view>
    <PageNav variant="footer" />
  </view>
</template>

<script setup>
import { ref } from 'vue'
import { onLoad } from '@dcloudio/uni-app'
import { mpApi } from '@/api'
import PageNav from '@/components/PageNav.vue'

const notice = ref({})

onLoad(async (options) => {
  const res = await mpApi.noticeDetail(options.id)
  notice.value = res.data || {}
})
</script>

<style lang="scss" scoped>
.title {
  display: block;
  font-size: 36rpx;
  font-weight: 700;
  color: #303133;
  line-height: 1.5;
}

.meta {
  display: flex;
  align-items: center;
  margin-top: 20rpx;
  gap: 16rpx;
}

.meta-text { font-size: 24rpx; color: #909399; }

.divider {
  height: 1rpx;
  background: #ebeef5;
  margin: 32rpx 0;
}

.content {
  font-size: 30rpx;
  color: #606266;
  line-height: 1.8;
}
</style>
