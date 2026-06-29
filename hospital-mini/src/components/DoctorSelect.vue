<template>
  <view class="card cascade-card">
    <view class="cascade-panel" :style="{ height: panelHeight }">
      <view class="dept-column">
        <text class="column-title">科室</text>
        <scroll-view scroll-y class="dept-scroll">
          <view
            v-for="dept in departments"
            :key="dept.id"
            class="dept-item"
            :class="{ active: selectedDept === dept.name }"
            @click="$emit('select-dept', dept.name)"
          >
            <text class="dept-icon">{{ dept.icon }}</text>
            <text class="dept-name">{{ dept.name }}</text>
          </view>
        </scroll-view>
      </view>

      <view class="doctor-column">
        <text class="column-title">
          {{ selectedDept ? `${selectedDept} · 医生` : '选择医生' }}
        </text>
        <scroll-view scroll-y class="doctor-scroll">
          <view v-if="!selectedDept" class="guide-hint">
            <text class="guide-icon">←</text>
            <text class="guide-text">请先选择左侧科室</text>
          </view>
          <view v-else-if="loading" class="guide-hint">
            <text class="guide-text">加载中…</text>
          </view>
          <view v-else class="doctor-list visible">
            <view
              v-for="doc in doctors"
              :key="doc.id"
              class="doctor-card"
              :class="{
                active: selectedDoctorId === doc.id,
                disabled: showRemaining && doc.remaining === 0
              }"
              @click="onSelectDoctor(doc)"
            >
              <view class="doctor-avatar">{{ doc.name.slice(0, 1) }}</view>
              <view class="doctor-body">
                <view class="doctor-row">
                  <text class="doctor-name">{{ doc.name }}</text>
                  <text class="doctor-title-tag">{{ doc.title }}</text>
                </view>
                <text class="doctor-specialty">擅长：{{ doc.specialty }}</text>
                <view v-if="showRemaining" class="doctor-footer">
                  <text
                    class="remain-badge"
                    :class="doc.remaining > 0 ? 'has' : 'none'"
                  >{{ doc.remaining > 0 ? `余号 ${doc.remaining}` : '号源已满' }}</text>
                </view>
              </view>
              <text v-if="selectedDoctorId === doc.id" class="doctor-check">✓</text>
            </view>
          </view>
        </scroll-view>
      </view>
    </view>
  </view>
</template>

<script setup>
const props = defineProps({
  departments: { type: Array, default: () => [] },
  doctors: { type: Array, default: () => [] },
  loading: { type: Boolean, default: false },
  selectedDept: { type: String, default: '' },
  selectedDoctorId: { type: [Number, null], default: null },
  showRemaining: { type: Boolean, default: true },
  panelHeight: { type: String, default: '560rpx' }
})

const emit = defineEmits(['select-dept', 'select-doctor'])

function onSelectDoctor(doc) {
  if (props.showRemaining && doc.remaining === 0) {
    uni.showToast({ title: '该医生号源已满', icon: 'none' })
    return
  }
  emit('select-doctor', doc)
}
</script>

<style lang="scss" scoped>
.cascade-card { padding: 0; overflow: hidden; }

.cascade-panel { display: flex; }

.dept-column {
  width: 200rpx;
  background: #f7f8fa;
  border-right: 1rpx solid #e5e6eb;
  display: flex;
  flex-direction: column;
}

.doctor-column {
  flex: 1;
  display: flex;
  flex-direction: column;
  min-width: 0;
}

.column-title {
  font-size: 24rpx;
  font-weight: 600;
  color: #8f959e;
  padding: 20rpx 24rpx 12rpx;
  flex-shrink: 0;
}

.dept-scroll,
.doctor-scroll {
  flex: 1;
  height: 0;
}

.dept-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 24rpx 12rpx;
  border-left: 6rpx solid transparent;
}

.dept-item.active {
  background: #fff;
  border-left-color: #3370ff;
}

.dept-icon { font-size: 36rpx; }

.dept-name {
  font-size: 26rpx;
  color: #646a73;
  margin-top: 8rpx;
}

.dept-item.active .dept-name {
  color: #3370ff;
  font-weight: 600;
}

.guide-hint {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  height: 100%;
  padding: 40rpx;
}

.guide-icon {
  font-size: 48rpx;
  color: #c9cdd4;
  margin-bottom: 16rpx;
}

.guide-text {
  font-size: 26rpx;
  color: #8f959e;
}

.doctor-list {
  padding: 0 16rpx 16rpx;
  opacity: 0;
  transform: translateY(16rpx);
  transition: opacity 0.3s, transform 0.3s;
}

.doctor-list.visible {
  opacity: 1;
  transform: translateY(0);
}

.doctor-card {
  display: flex;
  align-items: flex-start;
  padding: 20rpx;
  background: #f7f8fa;
  border-radius: 16rpx;
  margin-bottom: 16rpx;
  border: 2rpx solid transparent;
  position: relative;
}

.doctor-card.active {
  background: #f0f4ff;
  border-color: #3370ff;
}

.doctor-card.disabled { opacity: 0.5; }

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
  margin-left: 16rpx;
  min-width: 0;
}

.doctor-row {
  display: flex;
  align-items: center;
  flex-wrap: wrap;
  gap: 8rpx;
}

.doctor-name {
  font-size: 30rpx;
  font-weight: 600;
  color: #1f2329;
}

.doctor-title-tag {
  font-size: 20rpx;
  color: #3370ff;
  background: #f0f4ff;
  padding: 4rpx 12rpx;
  border-radius: 8rpx;
}

.doctor-specialty {
  display: block;
  font-size: 24rpx;
  color: #8f959e;
  margin-top: 8rpx;
}

.doctor-footer { margin-top: 12rpx; }

.remain-badge {
  font-size: 22rpx;
  padding: 4rpx 14rpx;
  border-radius: 20rpx;
}

.remain-badge.has { color: #34c724; background: #eaffea; }
.remain-badge.none { color: #f54a45; background: #ffece8; }

.doctor-check {
  position: absolute;
  top: 20rpx;
  right: 20rpx;
  color: #3370ff;
  font-size: 32rpx;
  font-weight: 700;
}
</style>
