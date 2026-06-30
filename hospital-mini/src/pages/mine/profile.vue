<template>
  <view class="container">
    <view class="profile-header">
      <view class="avatar-area" @click="chooseAvatar">
        <image v-if="avatarUrl" class="avatar" :src="avatarUrl" mode="aspectFill" />
        <view v-else class="avatar fallback">{{ profile.name?.charAt(0) || '患' }}</view>
        <text class="avatar-tip">点击更换头像</text>
      </view>
      <text class="refresh-link" @click="refresh">刷新档案</text>
    </view>

    <view v-if="error" class="alert-box">{{ error }}</view>

    <view class="card">
      <view class="form-item">
        <text class="form-label">姓名</text>
        <text class="form-value">{{ profile.name || '—' }}</text>
      </view>
      <view class="form-item">
        <text class="form-label">就诊卡号</text>
        <text class="form-value mono">{{ profile.cardNo || '—' }}</text>
      </view>
      <view class="form-item">
        <text class="form-label">绑定手机</text>
        <text class="form-value">{{ maskPhone(profile.phone) }}</text>
      </view>
      <view class="form-item">
        <text class="form-label">身份证号</text>
        <text class="form-value">{{ maskIdCard(profile.idCard) }}</text>
      </view>
      <view class="form-item">
        <text class="form-label">性别</text>
        <text class="form-value">{{ genderLabel(profile.gender) }}</text>
      </view>
      <view class="form-item">
        <text class="form-label">过敏史</text>
        <text v-if="profile.allergyHistory && profile.allergyHistory !== '无'" class="tag tag-danger">
          {{ profile.allergyHistory }}
        </text>
        <text v-else class="form-value">无</text>
      </view>
      <view class="form-item">
        <text class="form-label">慢性病</text>
        <text v-if="profile.chronicDisease && profile.chronicDisease !== '无'" class="tag tag-warning">
          {{ profile.chronicDisease }}
        </text>
        <text v-else class="form-value">无</text>
      </view>
      <view class="form-item" style="border: none">
        <text class="form-label">联系地址</text>
        <text class="form-value">{{ profile.address || '—' }}</text>
      </view>
    </view>

    <view v-if="healthTip" class="card health-card">
      <view class="card-title">健康提示</view>
      <text class="health-tip">{{ healthTip }}</text>
    </view>

    <PageNav variant="footer" />
  </view>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import PageNav from '@/components/PageNav.vue'
import { checkLogin, navigateToLogin } from '@/utils/request'
import { usePatientProfile } from '@/composables/usePatientProfile'

const { profile, load } = usePatientProfile()
const avatarUrl = ref('')
const error = ref(null)
const loading = ref(false)

const healthTip = computed(() => {
  if (!profile.value) return ''
  const tips = []
  if (profile.value.chronicDisease && profile.value.chronicDisease !== '无') {
    tips.push(`您有「${profile.value.chronicDisease}」慢病记录，请按时复诊并遵医嘱用药。`)
  }
  if (profile.value.allergyHistory && profile.value.allergyHistory !== '无') {
    tips.push(`过敏史：${profile.value.allergyHistory}，就诊或用药前请主动告知医生。`)
  }
  if (!tips.length) {
    tips.push('档案信息已同步，可用于挂号、预约时自动填充就诊人姓名。')
  }
  return tips.join(' ')
})

onMounted(async () => {
  if (!checkLogin()) { navigateToLogin(); return }
  avatarUrl.value = uni.getStorageSync('his_avatar') || ''
  await refresh()
})

async function refresh() {
  loading.value = true
  error.value = null
  try {
    const data = await load({ force: true })
    if (data?.avatar) avatarUrl.value = data.avatar
  } catch (e) {
    error.value = e?.message || '加载档案失败'
  } finally {
    loading.value = false
  }
}

function maskPhone(phone) {
  if (!phone || phone.length < 7) return phone || '—'
  return `${phone.slice(0, 3)}****${phone.slice(-4)}`
}

function maskIdCard(id) {
  if (!id || id.length < 10) return id || '—'
  return `${id.slice(0, 6)}********${id.slice(-4)}`
}

function genderLabel(gender) {
  if (gender === 1) return '男'
  if (gender === 2) return '女'
  return '—'
}

function chooseAvatar() {
  uni.chooseImage({
    count: 1,
    sizeType: ['compressed'],
    success(res) {
      const path = res.tempFilePaths[0]
      avatarUrl.value = path
      uni.setStorageSync('his_avatar', path)
      const user = uni.getStorageSync('his_user')
      if (user) {
        const parsed = JSON.parse(user)
        parsed.avatar = path
        uni.setStorageSync('his_user', JSON.stringify(parsed))
      }
      uni.showToast({ title: '头像已更新', icon: 'success' })
    }
  })
}
</script>

<style lang="scss" scoped>
.profile-header {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 32rpx 0 16rpx;
  position: relative;
}

.refresh-link {
  position: absolute;
  right: 24rpx;
  top: 32rpx;
  font-size: 26rpx;
  color: #3370ff;
}

.alert-box {
  margin: 0 24rpx 16rpx;
  padding: 20rpx 24rpx;
  background: #fff9eb;
  border: 1rpx solid #ffe7ba;
  border-radius: 12rpx;
  font-size: 26rpx;
  color: #ff8800;
}

.avatar-area {
  display: flex;
  flex-direction: column;
  align-items: center;
}

.avatar {
  width: 140rpx;
  height: 140rpx;
  border-radius: 50%;
  border: 4rpx solid #fff;
  box-shadow: 0 8rpx 24rpx rgba(51, 112, 255, 0.15);
}

.avatar.fallback {
  background: linear-gradient(135deg, #3370ff, #66b1ff);
  color: #fff;
  font-size: 56rpx;
  font-weight: 700;
  display: flex;
  align-items: center;
  justify-content: center;
}

.avatar-tip {
  font-size: 22rpx;
  color: #8f959e;
  margin-top: 12rpx;
}

.mono {
  font-family: monospace;
  letter-spacing: 1rpx;
}

.tag-warning {
  background: #fff9eb;
  color: #ff8800;
}

.health-card {
  margin-top: 24rpx;
}

.health-tip {
  font-size: 28rpx;
  line-height: 1.7;
  color: #646a73;
}
</style>
