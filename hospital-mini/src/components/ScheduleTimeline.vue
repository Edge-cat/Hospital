<template>
  <view class="card timeline-card">
    <view class="card-title-row">
      <text class="card-title">选择就诊时间</text>
      <text v-if="visitTypeLabel" class="type-badge" :class="visitTypeClass">{{ visitTypeLabel }}</text>
    </view>

    <view v-if="loading" class="timeline-loading">排班加载中…</view>
    <template v-else-if="dates.length">
      <scroll-view scroll-x class="date-timeline" :show-scrollbar="false">
        <view class="timeline-track">
          <view
            v-for="(day, idx) in dates"
            :key="day.date"
            class="date-node"
            :class="{ active: selectedDate === day.date, today: day.isToday }"
            @click="$emit('select-date', day)"
          >
            <view class="node-dot" />
            <text class="node-label">{{ day.label }}</text>
            <text class="node-date">{{ day.shortDate }}</text>
            <view v-if="idx < dates.length - 1" class="node-line" />
          </view>
        </view>
      </scroll-view>

      <view v-if="selectedDay" class="slot-section">
        <view class="slot-group">
          <text class="slot-group-title">上午</text>
          <view class="slot-grid">
            <view
              v-for="slot in morningSlots"
              :key="slot.timeSlot"
              class="slot-chip"
              :class="{ active: selectedSlot === slot.timeSlot, disabled: !slot.available }"
              @click="onSelectSlot(slot)"
            >
              <text class="slot-time">{{ slot.timeSlot }}</text>
              <text class="slot-remain">{{ slot.available ? `余${slot.remaining}` : '已满' }}</text>
            </view>
          </view>
        </view>
        <view class="slot-group">
          <text class="slot-group-title">下午</text>
          <view class="slot-grid">
            <view
              v-for="slot in afternoonSlots"
              :key="slot.timeSlot"
              class="slot-chip"
              :class="{ active: selectedSlot === slot.timeSlot, disabled: !slot.available }"
              @click="onSelectSlot(slot)"
            >
              <text class="slot-time">{{ slot.timeSlot }}</text>
              <text class="slot-remain">{{ slot.available ? `余${slot.remaining}` : '已满' }}</text>
            </view>
          </view>
        </view>
      </view>
    </template>
  </view>
</template>

<script setup>
import { computed } from 'vue'

const props = defineProps({
  dates: { type: Array, default: () => [] },
  loading: { type: Boolean, default: false },
  selectedDate: { type: String, default: '' },
  selectedSlot: { type: String, default: '' },
  visitTypeLabel: { type: String, default: '' },
  visitTypeClass: { type: String, default: '' }
})

const emit = defineEmits(['select-date', 'select-slot'])

const selectedDay = computed(() => props.dates.find((d) => d.date === props.selectedDate))

const morningSlots = computed(() =>
  (selectedDay.value?.slots || []).filter((s) => parseInt(s.timeSlot) < 12)
)

const afternoonSlots = computed(() =>
  (selectedDay.value?.slots || []).filter((s) => parseInt(s.timeSlot) >= 12)
)

function onSelectSlot(slot) {
  if (!slot.available) {
    uni.showToast({ title: '该时段已满', icon: 'none' })
    return
  }
  emit('select-slot', slot)
}
</script>

<style lang="scss" scoped>
.timeline-card { padding: 24rpx; }

.card-title-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 8rpx;
}

.card-title {
  font-size: 32rpx;
  font-weight: 600;
  color: #1f2329;
}

.type-badge {
  font-size: 22rpx;
  padding: 6rpx 16rpx;
  border-radius: 20rpx;
}

.type-badge.register {
  color: #3370ff;
  background: #f0f4ff;
}

.type-badge.appointment {
  color: #646a73;
  background: #f7f8fa;
}

.timeline-loading {
  text-align: center;
  color: #8f959e;
  font-size: 26rpx;
  padding: 40rpx 0;
}

.date-timeline {
  white-space: nowrap;
  margin: 20rpx 0 28rpx;
}

.timeline-track {
  display: inline-flex;
  padding: 16rpx 8rpx 8rpx;
}

.date-node {
  display: inline-flex;
  flex-direction: column;
  align-items: center;
  width: 120rpx;
  position: relative;
  flex-shrink: 0;
}

.node-dot {
  width: 20rpx;
  height: 20rpx;
  border-radius: 50%;
  background: #e5e6eb;
  border: 4rpx solid #fff;
  box-shadow: 0 0 0 2rpx #e5e6eb;
  margin-bottom: 12rpx;
}

.date-node.active .node-dot {
  background: #3370ff;
  box-shadow: 0 0 0 4rpx rgba(51, 112, 255, 0.25);
}

.date-node.today .node-dot {
  background: #ff8800;
  box-shadow: 0 0 0 2rpx #ff8800;
}

.date-node.today.active .node-dot {
  background: #3370ff;
  box-shadow: 0 0 0 4rpx rgba(51, 112, 255, 0.25);
}

.node-line {
  position: absolute;
  top: 10rpx;
  left: calc(50% + 14rpx);
  width: calc(100% - 28rpx);
  height: 3rpx;
  background: #e5e6eb;
}

.date-node.active .node-line,
.date-node.active ~ .date-node .node-line {
  background: #3370ff;
}

.node-label {
  font-size: 24rpx;
  color: #8f959e;
}

.node-date {
  font-size: 26rpx;
  color: #646a73;
  margin-top: 4rpx;
  font-weight: 500;
}

.date-node.active .node-label,
.date-node.active .node-date {
  color: #3370ff;
  font-weight: 600;
}

.slot-group { margin-bottom: 24rpx; }

.slot-group-title {
  display: block;
  font-size: 24rpx;
  color: #8f959e;
  margin-bottom: 16rpx;
}

.slot-grid {
  display: flex;
  flex-wrap: wrap;
  gap: 16rpx;
}

.slot-chip {
  width: calc(33.33% - 12rpx);
  padding: 20rpx 12rpx;
  background: #f7f8fa;
  border-radius: 12rpx;
  text-align: center;
  border: 2rpx solid transparent;
}

.slot-chip.active {
  background: #f0f4ff;
  border-color: #3370ff;
}

.slot-chip.disabled { opacity: 0.45; }

.slot-time {
  display: block;
  font-size: 26rpx;
  color: #1f2329;
  font-weight: 500;
}

.slot-remain {
  display: block;
  font-size: 20rpx;
  color: #8f959e;
  margin-top: 6rpx;
}

.slot-chip.active .slot-time,
.slot-chip.active .slot-remain {
  color: #3370ff;
}
</style>
