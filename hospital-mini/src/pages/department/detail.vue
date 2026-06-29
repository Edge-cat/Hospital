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
        <view class="card-title">科室医生</view>
        <view v-if="!doctors.length" class="empty-tip">暂无医生信息</view>
        <view v-for="doc in doctors" :key="doc.id" class="doctor-row">
          <view>
            <text class="doc-name">{{ doc.name }}</text>
            <text class="doc-title">{{ doc.title }} · {{ doc.specialty }}</text>
          </view>
          <button class="reg-btn" size="mini" @click="goRegister(doc)">预约</button>
        </view>
      </view>
    </template>
  </view>
</template>

<script setup>
import { ref } from 'vue'
import { onLoad } from '@dcloudio/uni-app'
import { mpApi } from '@/api'
import { withDeptIcon, waitLabel } from '@/utils/deptIcon'
import { useDepartments } from '@/composables/useDepartments'

const dept = ref({})
const doctors = ref([])
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
      doctors.value = res.data.list || []
    }
  } finally {
    loading.value = false
  }
})

function goRegister(doc) {
  uni.navigateTo({
    url: `/pages/visit/visit?department=${encodeURIComponent(dept.value.name)}&doctor=${encodeURIComponent(doc.name)}`
  })
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

.doctor-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 20rpx 0;
  border-bottom: 1rpx solid #f0f0f0;
}

.doctor-row:last-child { border-bottom: none; }
.doc-name { display: block; font-size: 30rpx; font-weight: 600; color: #303133; }
.doc-title { display: block; font-size: 24rpx; color: #909399; margin-top: 6rpx; }

.reg-btn {
  background: #3370ff;
  color: #fff;
  border: none;
  border-radius: 32rpx;
  font-size: 24rpx;
}
.reg-btn::after { border: none; }
</style>
