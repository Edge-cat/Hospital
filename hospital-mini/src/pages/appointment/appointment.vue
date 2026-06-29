<template>
  <view class="redirect-page" />
</template>

<script setup>
import { onLoad } from '@dcloudio/uni-app'

onLoad((options) => {
  const query = Object.keys(options || {})
    .map((k) => `${k}=${encodeURIComponent(options[k])}`)
    .join('&')
  uni.redirectTo({ url: `/pages/visit/visit${query ? `?${query}` : ''}` })
})
</script>
