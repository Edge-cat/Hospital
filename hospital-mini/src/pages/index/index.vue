<template>
  <view class="page" :class="{ 'has-status': activeVisit }">
    <view class="header">
      <text class="header-title">东软云医院</text>
      <text class="header-sub">智慧医疗 · 便捷就医</text>
    </view>

    <view class="search-wrap">
      <view class="search-bar" @click="focusSearch">
        <MiniIcon type="search" size="sm" />
        <input
          v-model="searchKey"
          class="search-input"
          placeholder="搜索症状、科室、医生"
          placeholder-class="search-placeholder"
          confirm-type="search"
          @confirm="onSearch"
          @focus="showSuggestions = true"
        />
        <text v-if="searchKey" class="search-clear" @click.stop="clearSearch">×</text>
      </view>
      <view v-if="showSuggestions && !searchKey" class="suggest-panel">
        <text class="suggest-label">常见症状</text>
        <view class="suggest-tags">
          <text
            v-for="item in symptomHints"
            :key="item.keyword"
            class="suggest-tag"
            @click="goSymptom(item)"
          >{{ item.keyword }}</text>
        </view>
      </view>
      <view v-if="showSuggestions && searchKey && searchResults.length" class="suggest-panel">
        <view
          v-for="item in searchResults"
          :key="item.id || item.keyword"
          class="suggest-item"
          @click="goSymptom(item)"
        >
          <MiniIcon type="triage" size="sm" />
          <view class="suggest-item-text">
            <text class="suggest-item-name">{{ item.name || item.keyword }}</text>
            <text class="suggest-item-desc">{{ item.desc }}</text>
          </view>
        </view>
      </view>
    </view>

    <view class="container">
      <view class="core-card">
        <text class="core-title">就诊服务</text>
        <view class="core-chain">
          <view
            v-for="(item, idx) in coreServices"
            :key="item.path"
            class="core-item"
            @click="goPage(item.path)"
          >
            <MiniIcon :type="item.icon" size="lg" />
            <text class="core-name">{{ item.name }}</text>
            <view v-if="idx < coreServices.length - 1" class="chain-arrow" />
          </view>
        </view>
      </view>

      <view class="card more-card">
        <view
          v-for="item in moreServices"
          :key="item.path"
          class="more-item"
          @click="goPage(item.path)"
        >
          <text class="more-name">{{ item.name }}</text>
          <text class="more-arrow">›</text>
        </view>
      </view>

      <view class="card">
        <view class="card-header">
          <text class="card-title">科室推荐</text>
          <text class="card-more" @click="goPage('/pages/department/department')">全部 ›</text>
        </view>
        <scroll-view scroll-x class="dept-scroll" :show-scrollbar="false">
          <view v-for="dept in featuredDepartments" :key="dept.id" class="dept-item" @click="goDept(dept)">
            <view class="dept-icon-wrap">
              <text class="dept-emoji">{{ dept.icon }}</text>
            </view>
            <text class="dept-name">{{ dept.name }}</text>
          </view>
        </scroll-view>
      </view>

      <view v-if="notices.length" class="card">
        <view class="card-header">
          <text class="card-title">医院公告</text>
          <text class="card-more" @click="goPage('/pages/notice/notice')">更多 ›</text>
        </view>
        <view v-for="item in notices" :key="item.id" class="notice-item" @click="goNotice(item.id)">
          <view class="notice-left">
            <text class="tag" :class="item.type === '紧急' ? 'tag-danger' : 'tag-primary'">{{ item.type }}</text>
            <text class="notice-title">{{ item.title }}</text>
          </view>
          <text class="notice-time">{{ item.publishTime?.slice(0, 10) }}</text>
        </view>
      </view>
    </view>

    <VisitStatusBar :visit="activeVisit" />
    <TabBar current="/pages/index/index" />
  </view>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import TabBar from '@/components/TabBar.vue'
import MiniIcon from '@/components/MiniIcon.vue'
import VisitStatusBar from '@/components/VisitStatusBar.vue'
import { mpApi } from '@/api'
import { useDepartments } from '@/composables/useDepartments'

const { departments, load: loadDepartments } = useDepartments()
const featuredDepartments = computed(() => departments.value.slice(0, 5))
const notices = ref([])
const searchKey = ref('')
const showSuggestions = ref(false)
const activeVisit = ref(null)

const coreServices = [
  { name: '智能导诊', icon: 'triage', path: '/pages/department/department' },
  { name: '预约挂号', icon: 'register', path: '/pages/visit/visit' },
  { name: '移动缴费', icon: 'payment', path: '/pages/payment/payment' },
  { name: '就诊回顾', icon: 'record', path: '/pages/records/records' }
]

const moreServices = [
  { name: '我的挂号', path: '/pages/mine/my-register' },
  { name: '我的预约', path: '/pages/mine/my-appointment' },
  { name: '科室介绍', path: '/pages/department/department' },
  { name: '医院公告', path: '/pages/notice/notice' }
]

const symptomHints = [
  { keyword: '发热咳嗽', name: '发热咳嗽', desc: '推荐内科', department: '内科', deptId: 1 },
  { keyword: '头痛头晕', name: '头痛头晕', desc: '推荐内科', department: '内科', deptId: 1 },
  { keyword: '腹痛腹泻', name: '腹痛腹泻', desc: '推荐内科', department: '内科', deptId: 1 },
  { keyword: '骨伤骨折', name: '骨伤骨折', desc: '推荐骨科', department: '骨科', deptId: 4 },
  { keyword: '视力模糊', name: '视力模糊', desc: '推荐眼科', department: '眼科', deptId: 5 },
  { keyword: '儿童发热', name: '儿童发热', desc: '推荐儿科', department: '儿科', deptId: 3 }
]

const searchResults = computed(() => {
  const key = searchKey.value.trim()
  if (!key) return []
  const deptMatches = departments.value
    .filter((d) => d.name.includes(key) || (d.desc && d.desc.includes(key)))
    .map((d) => ({ ...d, desc: (d.desc || '').slice(0, 16) + '…' }))
  const symptomMatches = symptomHints.filter(
    (s) => s.keyword.includes(key) || s.department.includes(key)
  )
  return [...symptomMatches, ...deptMatches].slice(0, 6)
})

onMounted(async () => {
  const [, noticeRes, registerRes, paymentRes] = await Promise.all([
    loadDepartments(),
    mpApi.notices(),
    mpApi.registerList(),
    mpApi.paymentList({ status: 0 })
  ])
  notices.value = noticeRes.data.list.slice(0, 2)
  activeVisit.value = buildActiveVisit(registerRes.data.list, paymentRes.data.list)
})

function buildActiveVisit(registers, unpaidPayments) {
  const pendingRegister = registers.find((r) => r.status === 0)
  if (pendingRegister) {
    const hasUnpaid = unpaidPayments.some((p) => p.status === 0)
    return {
      step: hasUnpaid ? 2 : 1,
      summary: `${pendingRegister.department} · ${pendingRegister.doctorName} · 今日待就诊`,
      actionText: hasUnpaid ? '去缴费' : '查看挂号',
      path: hasUnpaid ? '/pages/payment/payment' : '/pages/mine/my-register'
    }
  }
  const unpaid = unpaidPayments.find((p) => p.status === 0)
  if (unpaid) {
    return {
      step: 2,
      summary: `${unpaid.itemName} ¥${unpaid.amount} · 待支付`,
      actionText: '立即缴费',
      path: '/pages/payment/payment'
    }
  }
  return null
}

function focusSearch() {
  showSuggestions.value = true
}

function clearSearch() {
  searchKey.value = ''
}

function onSearch() {
  const results = searchResults.value
  if (results.length) goSymptom(results[0])
  else uni.showToast({ title: '未找到相关科室', icon: 'none' })
}

function goSymptom(item) {
  showSuggestions.value = false
  searchKey.value = ''
  const dept = departments.value.find((d) => d.id === item.deptId || d.name === item.department || d.name === item.name)
  if (dept) {
    uni.navigateTo({ url: `/pages/department/detail?id=${dept.id}&name=${dept.name}` })
  } else {
    uni.navigateTo({ url: '/pages/department/department' })
  }
}

function goPage(path) {
  showSuggestions.value = false
  uni.navigateTo({ url: path })
}

function goDept(dept) {
  showSuggestions.value = false
  uni.navigateTo({ url: `/pages/department/detail?id=${dept.id}&name=${dept.name}` })
}

function goNotice(id) {
  uni.navigateTo({ url: `/pages/notice/detail?id=${id}` })
}
</script>

<style lang="scss" scoped>
.page {
  min-height: 100vh;
  background: #f5f6f7;

  &.has-status .container {
    padding-bottom: calc(220rpx + env(safe-area-inset-bottom));
  }
}

.header {
  background: linear-gradient(160deg, #3370ff 0%, #5b8fff 100%);
  padding: 48rpx 40rpx 100rpx;
}

.header-title {
  display: block;
  font-size: 44rpx;
  font-weight: 700;
  color: #fff;
  letter-spacing: 2rpx;
}

.header-sub {
  display: block;
  font-size: 26rpx;
  color: rgba(255, 255, 255, 0.85);
  margin-top: 8rpx;
}

.search-wrap {
  position: relative;
  margin: -72rpx 24rpx 0;
  z-index: 10;
}

.search-bar {
  display: flex;
  align-items: center;
  background: #fff;
  border-radius: 48rpx;
  padding: 0 28rpx;
  height: 88rpx;
  box-shadow: 0 8rpx 32rpx rgba(51, 112, 255, 0.15);
  border: 1rpx solid #e5e6eb;
}

.search-input {
  flex: 1;
  font-size: 30rpx;
  color: #1f2329;
  margin-left: 16rpx;
  height: 88rpx;
}

.search-placeholder { color: #8f959e; font-size: 30rpx; }

.search-clear {
  font-size: 40rpx;
  color: #c9cdd4;
  padding: 0 8rpx;
  line-height: 1;
}

.suggest-panel {
  position: absolute;
  left: 0;
  right: 0;
  top: 100rpx;
  background: #fff;
  border-radius: 20rpx;
  padding: 24rpx;
  box-shadow: 0 12rpx 40rpx rgba(31, 35, 41, 0.1);
  border: 1rpx solid #e5e6eb;
}

.suggest-label {
  font-size: 24rpx;
  color: #8f959e;
  margin-bottom: 16rpx;
  display: block;
}

.suggest-tags {
  display: flex;
  flex-wrap: wrap;
  gap: 16rpx;
}

.suggest-tag {
  font-size: 26rpx;
  color: #3370ff;
  background: #f0f4ff;
  padding: 12rpx 24rpx;
  border-radius: 32rpx;
}

.suggest-item {
  display: flex;
  align-items: center;
  padding: 20rpx 0;
  border-bottom: 1rpx solid #f0f1f3;

  &:last-child { border-bottom: none; }
}

.suggest-item-text {
  margin-left: 16rpx;
  flex: 1;
}

.suggest-item-name {
  display: block;
  font-size: 30rpx;
  color: #1f2329;
  font-weight: 500;
}

.suggest-item-desc {
  display: block;
  font-size: 24rpx;
  color: #8f959e;
  margin-top: 4rpx;
}

.core-card {
  background: #fff;
  border-radius: 24rpx;
  padding: 32rpx 20rpx;
  margin-bottom: 24rpx;
  box-shadow: 0 8rpx 32rpx rgba(31, 35, 41, 0.08);
  border: 1rpx solid #e5e6eb;
}

.core-title {
  display: block;
  font-size: 32rpx;
  font-weight: 600;
  color: #1f2329;
  padding: 0 12rpx 24rpx;
}

.core-chain {
  display: flex;
  align-items: flex-start;
}

.core-item {
  flex: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  position: relative;
}

.core-name {
  font-size: 28rpx;
  font-weight: 500;
  color: #1f2329;
  margin-top: 16rpx;
  text-align: center;
}

.chain-arrow {
  position: absolute;
  right: -10rpx;
  top: 44rpx;
  width: 12rpx;
  height: 12rpx;
  border-top: 3rpx solid #c9cdd4;
  border-right: 3rpx solid #c9cdd4;
  transform: rotate(45deg);
}

.container {
  padding-top: 24rpx;
}

.more-card {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 0;
  padding: 0;
  overflow: hidden;
}

.more-item {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 28rpx 24rpx;
  border-bottom: 1rpx solid #f0f1f3;
  border-right: 1rpx solid #f0f1f3;

  &:nth-child(2n) { border-right: none; }
  &:nth-last-child(-n+2) { border-bottom: none; }
}

.more-name {
  font-size: 28rpx;
  color: #646a73;
}

.more-arrow {
  font-size: 28rpx;
  color: #c9cdd4;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20rpx;
}

.card-more {
  font-size: 26rpx;
  color: #3370ff;
}

.dept-scroll {
  white-space: nowrap;
}

.dept-item {
  display: inline-flex;
  flex-direction: column;
  align-items: center;
  width: 140rpx;
  margin-right: 16rpx;
}

.dept-icon-wrap {
  width: 88rpx;
  height: 88rpx;
  background: #f7f8fa;
  border-radius: 20rpx;
  display: flex;
  align-items: center;
  justify-content: center;
}

.dept-emoji { font-size: 40rpx; }

.dept-name {
  font-size: 26rpx;
  color: #646a73;
  margin-top: 12rpx;
}

.notice-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 20rpx 0;
  border-bottom: 1rpx solid #f0f0f0;
}

.notice-item:last-child { border-bottom: none; }

.notice-left {
  display: flex;
  align-items: center;
  flex: 1;
  overflow: hidden;
}

.notice-title {
  font-size: 28rpx;
  color: #303133;
  margin-left: 12rpx;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.notice-time {
  font-size: 22rpx;
  color: #c0c4cc;
  flex-shrink: 0;
  margin-left: 16rpx;
}
</style>
