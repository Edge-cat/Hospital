<template>
  <el-drawer
    :model-value="visible"
    direction="btt"
    size="auto"
    :with-header="false"
    class="doctor-profile-drawer"
    @update:model-value="$emit('update:visible', $event)"
  >
    <div v-if="doctor" class="profile-sheet">
      <div class="profile-handle" />
      <div class="profile-head">
        <div class="profile-avatar">{{ doctor.name?.charAt(0) }}</div>
        <div class="profile-head-text">
          <h3 class="profile-name">{{ doctor.name }}</h3>
          <p class="profile-dept">{{ deptLabel }} · {{ doctor.title }}</p>
        </div>
      </div>

      <dl class="profile-list">
        <div class="profile-item">
          <dt>毕业院校</dt>
          <dd>{{ doctor.education || '资料完善中' }}</dd>
        </div>
        <div class="profile-item">
          <dt>学位</dt>
          <dd>{{ doctor.degree || '—' }}</dd>
        </div>
        <div class="profile-item">
          <dt>职称</dt>
          <dd>{{ doctor.title || '—' }}</dd>
        </div>
        <div class="profile-item profile-item--block">
          <dt>擅长领域</dt>
          <dd>{{ doctor.specialty || '—' }}</dd>
        </div>
      </dl>

      <el-button v-if="showBook" type="primary" class="profile-book-btn" @click="$emit('book', doctor)">
        {{ bookLabel }}
      </el-button>
      <el-button link class="profile-close" @click="$emit('update:visible', false)">关闭</el-button>
    </div>
  </el-drawer>
</template>

<script setup>
defineProps({
  visible: { type: Boolean, default: false },
  doctor: { type: Object, default: null },
  deptLabel: { type: String, default: '' },
  showBook: { type: Boolean, default: true },
  bookLabel: { type: String, default: '预约该医生' }
})

defineEmits(['update:visible', 'book'])
</script>

<style scoped>
.profile-sheet {
  padding: 8px 20px 24px;
  max-width: 640px;
  margin: 0 auto;
}

.profile-handle {
  width: 40px;
  height: 4px;
  margin: 0 auto 16px;
  border-radius: 2px;
  background: var(--feishu-border-light);
}

.profile-head {
  display: flex;
  gap: 14px;
  align-items: center;
  margin-bottom: 20px;
}

.profile-avatar {
  width: 52px;
  height: 52px;
  border-radius: 50%;
  background: linear-gradient(135deg, var(--feishu-primary), #66b1ff);
  color: #fff;
  font-size: 22px;
  font-weight: 600;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}

.profile-name {
  margin: 0 0 4px;
  font-size: 18px;
  font-weight: 600;
}

.profile-dept {
  margin: 0;
  font-size: 13px;
  color: var(--feishu-text-tertiary);
}

.profile-list {
  margin: 0 0 20px;
}

.profile-item {
  display: flex;
  justify-content: space-between;
  gap: 16px;
  padding: 12px 0;
  border-bottom: 1px solid var(--feishu-border-light);
  font-size: 14px;
}

.profile-item--block {
  flex-direction: column;
  gap: 8px;
}

.profile-item dt {
  color: var(--feishu-text-tertiary);
  font-weight: 500;
  flex-shrink: 0;
}

.profile-item dd {
  margin: 0;
  color: var(--feishu-text-primary);
  text-align: right;
  line-height: 1.6;
}

.profile-item--block dd {
  text-align: left;
}

.profile-book-btn {
  width: 100%;
  margin-bottom: 8px;
}

.profile-close {
  display: block;
  width: 100%;
  color: var(--feishu-text-tertiary);
}
</style>

<style>
.doctor-profile-drawer .el-drawer__body {
  padding: 12px 0 0;
}
</style>
