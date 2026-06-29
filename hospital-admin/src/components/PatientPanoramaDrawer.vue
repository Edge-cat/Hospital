<template>
  <el-drawer
    :model-value="visible"
    :title="patient ? `${patient.name} · 全景档案` : '患者全景档案'"
    size="520px"
    destroy-on-close
    @update:model-value="$emit('update:visible', $event)"
  >
    <div v-loading="loading" class="panorama">
      <template v-if="detail">
        <section class="panorama__hero">
          <div class="panorama__avatar">{{ patient?.name?.charAt(0) }}</div>
          <div>
            <h3>{{ detail.name }} <el-tag size="small">{{ detail.gender === 1 ? '男' : '女' }} · {{ detail.age }}岁</el-tag></h3>
            <p>{{ detail.patientNo }} · {{ detail.phone }}</p>
            <p class="panorama__dept">{{ detail.department }} · 建档 {{ detail.createTime }}</p>
          </div>
        </section>

        <el-descriptions :column="2" border size="small" class="panorama__base">
          <el-descriptions-item label="身份证" :span="2">{{ detail.idCard || '-' }}</el-descriptions-item>
          <el-descriptions-item label="地址" :span="2">{{ detail.address || '-' }}</el-descriptions-item>
          <el-descriptions-item label="过敏史">{{ detail.allergyHistory || '无' }}</el-descriptions-item>
          <el-descriptions-item label="慢性病史">{{ detail.chronicDisease || '无' }}</el-descriptions-item>
        </el-descriptions>

        <div class="panorama__section">
          <h4>既往病史</h4>
          <el-timeline v-if="detail.medicalHistory?.length">
            <el-timeline-item v-for="(h, i) in detail.medicalHistory" :key="i" :timestamp="h.date" placement="top">
              {{ h.content }}
            </el-timeline-item>
          </el-timeline>
          <el-empty v-else description="暂无病史记录" :image-size="48" />
        </div>

        <div class="panorama__section">
          <h4>就诊轨迹</h4>
          <el-table :data="detail.visitRecords || []" size="small" stripe>
            <el-table-column prop="visitTime" label="时间" width="155" class-name="col-mono" />
            <el-table-column prop="department" label="科室" width="80" />
            <el-table-column prop="doctorName" label="医生" width="80" />
            <el-table-column prop="diagnosis" label="诊断" show-overflow-tooltip />
          </el-table>
        </div>

        <div class="panorama__section">
          <h4>缴费概况</h4>
          <div class="panorama__pay-stats">
            <div class="panorama__pay-stat">
              <span class="label">待缴</span>
              <span class="value pending">¥{{ (detail.paymentSummary?.pendingAmount || 0).toFixed(2) }}</span>
            </div>
            <div class="panorama__pay-stat">
              <span class="label">已缴</span>
              <span class="value paid">¥{{ (detail.paymentSummary?.paidAmount || 0).toFixed(2) }}</span>
            </div>
          </div>
        </div>
      </template>
    </div>

    <template #footer>
      <el-button @click="$emit('update:visible', false)">关闭</el-button>
      <el-button v-if="showEdit" type="primary" @click="$emit('edit', patient)">编辑档案</el-button>
    </template>
  </el-drawer>
</template>

<script setup>
import { ref, watch } from 'vue'
import { patientApi } from '@/api'

const props = defineProps({
  visible: { type: Boolean, default: false },
  patient: { type: Object, default: null },
  showEdit: { type: Boolean, default: false }
})

defineEmits(['update:visible', 'edit'])

const loading = ref(false)
const detail = ref(null)

watch(
  () => [props.visible, props.patient?.id],
  async ([vis, id]) => {
    if (!vis || !id) {
      detail.value = null
      return
    }
    loading.value = true
    try {
      const res = await patientApi.getDetail(id)
      detail.value = res.data
    } finally {
      loading.value = false
    }
  },
  { immediate: true }
)
</script>

<style scoped>
.panorama__hero {
  display: flex;
  gap: 16px;
  margin-bottom: 20px;
  padding: 16px;
  border-radius: 12px;
  background: linear-gradient(135deg, #ecf5ff 0%, #fff 100%);
  box-shadow: 0 2px 12px rgba(64, 158, 255, 0.08);
}
.panorama__avatar {
  width: 56px;
  height: 56px;
  border-radius: 50%;
  background: var(--feishu-primary);
  color: #fff;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 22px;
  font-weight: 600;
  flex-shrink: 0;
}
.panorama__hero h3 {
  font-size: 18px;
  margin-bottom: 4px;
  display: flex;
  align-items: center;
  gap: 8px;
}
.panorama__hero p {
  font-size: 13px;
  color: var(--feishu-text-secondary);
}
.panorama__dept { margin-top: 2px; }
.panorama__base { margin-bottom: 20px; }
.panorama__section {
  margin-bottom: 24px;
}
.panorama__section h4 {
  font-size: 14px;
  font-weight: 600;
  margin-bottom: 12px;
  padding-left: 8px;
  border-left: 3px solid var(--feishu-primary);
}
.panorama__pay-stats {
  display: flex;
  gap: 16px;
}
.panorama__pay-stat {
  flex: 1;
  padding: 14px;
  border-radius: 10px;
  background: var(--feishu-bg-sub);
  text-align: center;
}
.panorama__pay-stat .label {
  display: block;
  font-size: 12px;
  color: var(--feishu-text-tertiary);
  margin-bottom: 4px;
}
.panorama__pay-stat .value {
  font-size: 20px;
  font-weight: 600;
  font-variant-numeric: tabular-nums;
}
.panorama__pay-stat .value.pending { color: #e6a23c; }
.panorama__pay-stat .value.paid { color: #67c23a; }
</style>
