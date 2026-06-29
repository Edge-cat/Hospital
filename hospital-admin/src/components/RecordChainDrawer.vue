<template>
  <el-drawer
    :model-value="visible"
    title="诊疗全链路回溯"
    size="560px"
    destroy-on-close
    @update:model-value="$emit('update:visible', $event)"
  >
    <div v-loading="loading" class="chain-drawer">
      <template v-if="chain">
        <section class="chain-summary">
          <h3>{{ chain.patientName }} · {{ chain.department }}</h3>
          <p>主治：{{ chain.doctorName }} · {{ chain.visitTime }}</p>
        </section>

        <el-timeline>
          <el-timeline-item
            v-for="(step, i) in chain.steps"
            :key="i"
            :type="step.type"
            :timestamp="step.time"
            placement="top"
          >
            <div class="chain-step">
              <strong>{{ step.title }}</strong>
              <p>{{ step.content }}</p>
              <el-tag v-if="step.status" size="small" :type="step.tagType">{{ step.status }}</el-tag>
            </div>
          </el-timeline-item>
        </el-timeline>

        <el-descriptions v-if="chain.revisions?.length" title="修订记录" :column="1" border size="small">
          <el-descriptions-item v-for="(r, i) in chain.revisions" :key="i" :label="r.time">
            {{ r.reason }} — {{ r.operator }}
          </el-descriptions-item>
        </el-descriptions>
      </template>
    </div>
  </el-drawer>
</template>

<script setup>
import { ref, watch } from 'vue'
import { recordApi } from '@/api'

const props = defineProps({
  visible: { type: Boolean, default: false },
  recordId: { type: Number, default: null }
})

defineEmits(['update:visible'])

const loading = ref(false)
const chain = ref(null)

watch(
  () => [props.visible, props.recordId],
  async ([vis, id]) => {
    if (!vis || !id) {
      chain.value = null
      return
    }
    loading.value = true
    try {
      const res = await recordApi.getChain(id)
      chain.value = res.data
    } finally {
      loading.value = false
    }
  },
  { immediate: true }
)
</script>

<style scoped>
.chain-summary {
  padding: 16px;
  margin-bottom: 20px;
  border-radius: 10px;
  background: linear-gradient(135deg, #ecf5ff, #fff);
}
.chain-summary h3 { font-size: 17px; margin-bottom: 4px; }
.chain-summary p { font-size: 13px; color: var(--feishu-text-secondary); }
.chain-step strong { display: block; margin-bottom: 4px; }
.chain-step p { font-size: 13px; color: var(--feishu-text-secondary); margin-bottom: 6px; }
</style>
