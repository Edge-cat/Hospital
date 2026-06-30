<template>
  <div v-loading="loading" class="doctor-picker">
    <p v-if="!department" class="picker-hint">请先选择科室</p>
    <el-empty v-else-if="!loading && !doctors.length" description="该科室暂无医生" :image-size="56" />
    <div v-else class="doctor-picker__list">
      <div
        v-for="doc in doctors"
        :key="doc.id"
        class="doctor-card"
        :class="{
          'doctor-card--active': selectedId === doc.id,
          'doctor-card--disabled': showRemaining && doc.remaining === 0
        }"
        @click="onSelect(doc)"
      >
        <div class="doctor-card__avatar">{{ doc.name?.charAt(0) }}</div>
        <div class="doctor-card__body">
          <div class="doctor-card__row">
            <span class="doctor-card__name">{{ doc.name }}</span>
            <el-tag size="small" type="primary" effect="light">{{ doc.title }}</el-tag>
          </div>
          <p class="doctor-card__specialty">擅长：{{ doc.specialty || '—' }}</p>
          <div v-if="showRemaining && doc.remaining != null" class="doctor-card__remain">
            <el-tag
              size="small"
              :type="doc.remaining > 0 ? 'success' : 'danger'"
              effect="light"
            >
              {{ doc.remaining > 0 ? `余号 ${doc.remaining}` : '号源已满' }}
            </el-tag>
          </div>
        </div>
        <el-button
          link
          type="primary"
          class="doctor-card__profile"
          @click.stop="$emit('profile', doc)"
        >
          履历
        </el-button>
        <el-icon v-if="selectedId === doc.id" class="doctor-card__check"><CircleCheckFilled /></el-icon>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ElMessage } from 'element-plus'
import { CircleCheckFilled } from '@element-plus/icons-vue'

const props = defineProps({
  department: { type: String, default: '' },
  doctors: { type: Array, default: () => [] },
  loading: { type: Boolean, default: false },
  selectedId: { type: [Number, null], default: null },
  showRemaining: { type: Boolean, default: true }
})

const emit = defineEmits(['select', 'profile'])

function onSelect(doc) {
  if (props.showRemaining && doc.remaining === 0) {
    ElMessage.warning('该医生在所选日期号源已满')
    return
  }
  emit('select', doc)
}
</script>

<style scoped>
.doctor-picker {
  width: 100%;
  min-height: 80px;
}

.picker-hint {
  margin: 0;
  font-size: 13px;
  color: var(--feishu-text-tertiary);
}

.doctor-picker__list {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.doctor-card {
  display: flex;
  align-items: flex-start;
  gap: 12px;
  padding: 14px;
  border: 1px solid var(--feishu-border-light);
  border-radius: var(--feishu-radius-md);
  background: var(--feishu-bg-sub);
  cursor: pointer;
  transition: border-color 0.15s, background 0.15s;
  position: relative;
}

.doctor-card:hover {
  border-color: var(--feishu-primary);
}

.doctor-card--active {
  border-color: var(--feishu-primary);
  background: var(--feishu-primary-bg);
}

.doctor-card--disabled {
  opacity: 0.55;
}

.doctor-card__avatar {
  width: 44px;
  height: 44px;
  border-radius: 50%;
  background: linear-gradient(135deg, var(--feishu-primary), #66b1ff);
  color: #fff;
  font-size: 18px;
  font-weight: 600;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}

.doctor-card__body {
  flex: 1;
  min-width: 0;
}

.doctor-card__row {
  display: flex;
  align-items: center;
  flex-wrap: wrap;
  gap: 8px;
}

.doctor-card__name {
  font-size: 15px;
  font-weight: 600;
}

.doctor-card__specialty {
  margin: 6px 0 0;
  font-size: 13px;
  color: var(--feishu-text-tertiary);
  line-height: 1.5;
}

.doctor-card__remain {
  margin-top: 8px;
}

.doctor-card__profile {
  flex-shrink: 0;
  font-size: 13px;
}

.doctor-card__check {
  position: absolute;
  top: 12px;
  right: 12px;
  color: var(--feishu-primary);
  font-size: 18px;
}
</style>
