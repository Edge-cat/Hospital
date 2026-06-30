<template>
  <view class="container">
    <view v-if="loading" class="loading-tip">加载中…</view>
    <template v-else>
      <view class="card">
        <view class="dept-header">
          <text class="dept-icon">{{ dept.icon }}</text>
          <view>
            <text class="dept-name">{{ dept.name }}</text>
            <text class="dept-code">编码：{{ dept.code || '—' }}</text>
          </view>
        </view>
        <view class="info-row"><text class="label">科室负责人</text><text>{{ dept.leader || '—' }}</text></view>
        <view v-if="waitLabel(dept)" class="info-row">
          <text class="label">候诊情况</text>
          <text class="wait-text">{{ waitLabel(dept) }}</text>
        </view>
        <view class="info-row"><text class="label">科室简介</text></view>
        <text class="desc">{{ dept.desc || '暂无简介' }}</text>
        <view v-if="dept.commonDiseases?.length" class="tag-block">
          <text class="tag-label">常见疾病</text>
          <view class="tags">
            <text v-for="tag in dept.commonDiseases" :key="tag" class="tag">{{ tag }}</text>
          </view>
        </view>
      </view>

      <view class="card">
        <view class="card-title-row">
          <text class="card-title">科室医生</text>
          <text class="card-hint">点击查看简介</text>
        </view>
        <view v-if="!doctors.length" class="empty-tip">暂无医生信息</view>
        <view
          v-for="doc in doctors"
          :key="doc.id"
          class="doctor-row"
          hover-class="doctor-row--hover"
          @click="openProfile(doc)"
        >
          <view class="doctor-avatar">{{ doc.name.slice(0, 1) }}</view>
          <view class="doctor-body">
            <text class="doc-name">{{ doc.name }}</text>
            <text class="doc-title">{{ doc.title }} · {{ doc.specialty }}</text>
          </view>
          <button class="reg-btn" size="mini" @click.stop="goRegister(doc)">预约</button>
        </view>
      </view>
    </template>

    <view v-if="profileDoc" class="profile-mask" @click="closeProfile">
      <view class="profile-sheet" @click.stop>
        <view class="profile-handle" />
        <view class="profile-head">
          <view class="profile-avatar">{{ profileDoc.name.slice(0, 1) }}</view>
          <view class="profile-head-text">
            <text class="profile-name">{{ profileDoc.name }}</text>
            <text class="profile-dept">{{ dept.name }} · {{ profileDoc.title }}</text>
          </view>
        </view>

        <view class="profile-list">
          <view class="profile-item">
            <text class="profile-label">毕业院校</text>
            <text class="profile-value">{{ profileDoc.education || '资料完善中' }}</text>
          </view>
          <view class="profile-item">
            <text class="profile-label">学位</text>
            <text class="profile-value">{{ profileDoc.degree || '—' }}</text>
          </view>
          <view class="profile-item">
            <text class="profile-label">职称</text>
            <text class="profile-value">{{ profileDoc.title || '—' }}</text>
          </view>
          <view class="profile-item profile-item--block">
            <text class="profile-label">擅长领域</text>
            <text class="profile-value profile-value--long">{{ profileDoc.specialty || '—' }}</text>
          </view>
        </view>

        <button class="profile-book-btn" @click="bookFromProfile">预约该医生</button>
        <text class="profile-close" @click="closeProfile">关闭</text>
      </view>
    </view>

    <PageNav variant="footer" />
  </view>
</template>

<script setup>
import { ref } from 'vue'
import { onLoad } from '@dcloudio/uni-app'
import { mpApi } from '@/api'
import PageNav from '@/components/PageNav.vue'
import { withDeptIcon, waitLabel } from '@/utils/deptIcon'
import { mapDoctorList } from '@/utils/doctorProfile'
import { useDepartments } from '@/composables/useDepartments'
import { safeNavigateTo } from '@/utils/nav'

const dept = ref({})
const doctors = ref([])
const profileDoc = ref(null)
const loading = ref(true)
const { load: loadDepartments, findById, findByName } = useDepartments()

onLoad(async (options) => {
  loading.value = true
  try {
    await loadDepartments()
    const { id, name } = options || {}
    const found =
      findById(id) ||
      findByName(decodeURIComponent(name || ''))
    dept.value = found ? withDeptIcon(found) : {}
    if (dept.value.name) {
      const res = await mpApi.doctors({ department: dept.value.name })
      doctors.value = mapDoctorList(res.data.list || [])
    }
  } finally {
    loading.value = false
  }
})

function openProfile(doc) {
  profileDoc.value = doc
}

function closeProfile() {
  profileDoc.value = null
}

function goRegister(doc) {
  safeNavigateTo(`/pages/visit/visit?department=${encodeURIComponent(dept.value.name)}&doctor=${encodeURIComponent(doc.name)}`)
}

function bookFromProfile() {
  if (!profileDoc.value) return
  const doc = profileDoc.value
  closeProfile()
  goRegister(doc)
}
</script>

<style lang="scss" scoped>
.loading-tip,
.empty-tip {
  text-align: center;
  color: #909399;
  padding: 32rpx 0;
  font-size: 28rpx;
}

.dept-header {
  display: flex;
  align-items: center;
  margin-bottom: 24rpx;
}

.dept-icon { font-size: 72rpx; margin-right: 24rpx; }
.dept-name { display: block; font-size: 36rpx; font-weight: 700; color: #303133; }
.dept-code { display: block; font-size: 24rpx; color: #909399; margin-top: 8rpx; }

.info-row {
  display: flex;
  justify-content: space-between;
  padding: 16rpx 0;
  font-size: 28rpx;
}

.info-row .label { color: #909399; }
.wait-text { color: #3370ff; }
.desc { font-size: 28rpx; color: #606266; line-height: 1.8; }

.tag-block { margin-top: 20rpx; }
.tag-label { font-size: 26rpx; color: #909399; }
.tags { display: flex; flex-wrap: wrap; gap: 12rpx; margin-top: 12rpx; }
.tag {
  font-size: 24rpx;
  color: #3370ff;
  background: #ecf5ff;
  padding: 6rpx 16rpx;
  border-radius: 24rpx;
}

.card-title-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 8rpx;
}

.card-title {
  font-size: 32rpx;
  font-weight: 600;
  color: #303133;
}

.card-hint {
  font-size: 22rpx;
  color: #909399;
}

.doctor-row {
  display: flex;
  align-items: center;
  padding: 20rpx 0;
  border-bottom: 1rpx solid #f0f0f0;
}

.doctor-row:last-child { border-bottom: none; }
.doctor-row--hover { opacity: 0.85; }

.doctor-avatar {
  width: 72rpx;
  height: 72rpx;
  border-radius: 50%;
  background: linear-gradient(135deg, #3370ff, #66b1ff);
  color: #fff;
  font-size: 32rpx;
  font-weight: 600;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}

.doctor-body {
  flex: 1;
  margin-left: 20rpx;
  min-width: 0;
}

.doc-name { display: block; font-size: 30rpx; font-weight: 600; color: #303133; }
.doc-title {
  display: block;
  font-size: 24rpx;
  color: #909399;
  margin-top: 6rpx;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.reg-btn {
  background: #3370ff;
  color: #fff;
  border: none;
  border-radius: 32rpx;
  font-size: 24rpx;
  flex-shrink: 0;
  margin-left: 16rpx;
}
.reg-btn::after { border: none; }

.profile-mask {
  position: fixed;
  inset: 0;
  background: rgba(0, 0, 0, 0.45);
  z-index: 1000;
  display: flex;
  align-items: flex-end;
}

.profile-sheet {
  width: 100%;
  background: #fff;
  border-radius: 32rpx 32rpx 0 0;
  padding: 24rpx 32rpx calc(32rpx + env(safe-area-inset-bottom));
  max-height: 78vh;
  overflow-y: auto;
}

.profile-handle {
  width: 64rpx;
  height: 8rpx;
  background: #e5e6eb;
  border-radius: 4rpx;
  margin: 0 auto 28rpx;
}

.profile-head {
  display: flex;
  align-items: center;
  margin-bottom: 32rpx;
}

.profile-avatar {
  width: 96rpx;
  height: 96rpx;
  border-radius: 50%;
  background: linear-gradient(135deg, #3370ff, #5b8fff);
  color: #fff;
  font-size: 40rpx;
  font-weight: 700;
  display: flex;
  align-items: center;
  justify-content: center;
}

.profile-head-text {
  margin-left: 24rpx;
}

.profile-name {
  display: block;
  font-size: 36rpx;
  font-weight: 700;
  color: #1f2329;
}

.profile-dept {
  display: block;
  font-size: 26rpx;
  color: #646a73;
  margin-top: 8rpx;
}

.profile-list {
  background: #f7f8fa;
  border-radius: 16rpx;
  padding: 8rpx 24rpx;
  margin-bottom: 32rpx;
}

.profile-item {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  padding: 20rpx 0;
  border-bottom: 1rpx solid #eef0f3;
  gap: 24rpx;
}

.profile-item:last-child { border-bottom: none; }
.profile-item--block { flex-direction: column; gap: 12rpx; }

.profile-label {
  font-size: 26rpx;
  color: #8f959e;
  flex-shrink: 0;
  min-width: 120rpx;
}

.profile-value {
  font-size: 28rpx;
  color: #1f2329;
  text-align: right;
  flex: 1;
}

.profile-value--long {
  text-align: left;
  line-height: 1.7;
  color: #303133;
}

.profile-book-btn {
  background: #3370ff;
  color: #fff;
  border: none;
  border-radius: 48rpx;
  font-size: 30rpx;
  height: 88rpx;
  line-height: 88rpx;
}

.profile-book-btn::after { border: none; }

.profile-close {
  display: block;
  text-align: center;
  font-size: 26rpx;
  color: #8f959e;
  margin-top: 24rpx;
}
</style>
