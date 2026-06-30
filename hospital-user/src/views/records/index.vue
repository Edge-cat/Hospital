<template>
  <ProfilePage>
    <div class="records-page">
      <PageHeader title="就诊记录" subtitle="缴费完成后可查看医生填写的完整诊疗报告" />

      <div v-loading="loading" class="records-list">
        <el-empty v-if="!loading && !list.length" description="暂无已解锁的就诊记录" :image-size="72">
          <template #description>
            <p class="empty-desc">医生完成接诊后，请先前往「缴费中心」支付检查费/药品费</p>
            <p class="empty-desc">全部诊疗费用缴清后，此处将展示诊断与处方信息</p>
          </template>
          <el-button type="primary" round @click="$router.push('/payment')">去缴费</el-button>
        </el-empty>

        <article v-for="row in enrichedList" :key="row.id" class="record-card">
          <header class="record-card__head">
            <div class="record-card__head-left">
              <span class="record-card__dept">{{ row.department }}</span>
              <span class="record-card__no">MR{{ String(row.id).padStart(8, '0') }}</span>
            </div>
            <el-tag type="success" size="small">已归档</el-tag>
          </header>

          <div class="record-card__meta">
            <span>{{ row.doctorName }} · 主治医师</span>
            <span>{{ row.visitTimeLabel }}</span>
          </div>

          <dl class="record-card__sections">
            <div v-if="row.chiefComplaint" class="record-card__section">
              <dt>主诉</dt>
              <dd>{{ row.chiefComplaint }}</dd>
            </div>
            <div class="record-card__section record-card__section--highlight">
              <dt>诊断结果</dt>
              <dd>{{ row.diagnosis || '—' }}</dd>
            </div>
            <div v-if="row.examItems" class="record-card__section">
              <dt>检查项目</dt>
              <dd>{{ row.examItems }}</dd>
            </div>
            <div v-if="row.prescription" class="record-card__section">
              <dt>处方 / 医嘱</dt>
              <dd class="record-card__prescription">{{ row.prescription }}</dd>
            </div>
            <div v-else-if="row.treatmentSummary" class="record-card__section">
              <dt>诊疗说明</dt>
              <dd>{{ row.treatmentSummary }}</dd>
            </div>
          </dl>

          <footer class="record-card__footer">
            <span class="record-card__hint">报告已解锁，可导出留存或 AI 问诊</span>
            <div class="record-card__actions">
              <el-button round class="record-card__ai" @click="onAiConsult(row)">
                AI 问诊
              </el-button>
              <el-button round class="record-card__export" @click="onExport(row)">
                导出诊疗报告
              </el-button>
            </div>
          </footer>
        </article>
      </div>
    </div>

    <AiConsultDialog
      v-model:visible="aiVisible"
      v-model:input="aiInput"
      :record="aiRecord"
      :messages="aiMessages"
      :loading="aiLoading"
      :disclaimer="aiDisclaimer"
      :demo-mode="aiDemoMode"
      @send="onAiSend"
      @close="closeAiConsult"
    />
  </ProfilePage>
</template>

<script setup>
import { computed, onActivated } from 'vue'
import { ElMessage } from 'element-plus'
import { userApi } from '@/api'
import { useListPage } from '@/composables/useListPage'
import { usePatientProfile } from '@/composables/usePatientProfile'
import { useAiConsult } from '@/composables/useAiConsult'
import AiConsultDialog from '@/components/AiConsultDialog.vue'
import { parseTreatment, formatVisitTime, downloadMedicalReport } from '@/utils/medicalReport'

const { loading, list, loadData } = useListPage(() => userApi.records({ pageSize: 100 }))
const { profile, load: loadProfile } = usePatientProfile()
const {
  visible: aiVisible,
  record: aiRecord,
  messages: aiMessages,
  loading: aiLoading,
  input: aiInput,
  disclaimer: aiDisclaimer,
  demoMode: aiDemoMode,
  open: openAiConsult,
  close: closeAiConsult,
  send: sendAiConsult
} = useAiConsult()

const enrichedList = computed(() =>
  list.value.map((row) => {
    const parsed = parseTreatment(row.treatment)
    return {
      ...row,
      visitTimeLabel: formatVisitTime(row.visitTime),
      chiefComplaint: parsed.chiefComplaint,
      examItems: parsed.examItems,
      prescription: parsed.prescription,
      treatmentSummary: parsed.summary
    }
  })
)

async function onExport(row) {
  const info = profile.value || (await loadProfile({ silent: true }))
  downloadMedicalReport(row, info || {})
  ElMessage.success('诊疗报告已导出')
}

async function onAiConsult(row) {
  try {
    await openAiConsult(row)
  } catch (e) {
    ElMessage.error(e?.message || 'AI 问诊失败')
  }
}

async function onAiSend(text) {
  try {
    await sendAiConsult(text)
  } catch (e) {
    ElMessage.error(e?.message || 'AI 回复失败')
  }
}

onActivated(async () => {
  await loadData()
  await loadProfile({ silent: true })
})
</script>

<style scoped>
.records-page {
  max-width: 720px;
}

.records-list {
  min-height: 200px;
}

.empty-desc {
  margin: 4px 0;
  font-size: 13px;
  color: var(--feishu-text-secondary);
}

.record-card {
  padding: 20px;
  margin-bottom: 16px;
  border-radius: 14px;
  background: #fff;
  border: 1px solid var(--feishu-border-light);
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.04);
}

.record-card__head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 10px;
}

.record-card__head-left {
  display: flex;
  align-items: center;
  gap: 10px;
}

.record-card__dept {
  font-size: 17px;
  font-weight: 600;
  color: var(--feishu-text-primary);
}

.record-card__no {
  font-size: 12px;
  color: var(--feishu-text-tertiary);
  font-family: ui-monospace, monospace;
}

.record-card__meta {
  display: flex;
  flex-wrap: wrap;
  gap: 8px 16px;
  margin-bottom: 16px;
  padding-bottom: 14px;
  border-bottom: 1px solid var(--feishu-border-light);
  font-size: 13px;
  color: var(--feishu-text-secondary);
}

.record-card__sections {
  margin: 0;
}

.record-card__section {
  margin-bottom: 14px;
}

.record-card__section:last-child {
  margin-bottom: 0;
}

.record-card__section dt {
  margin-bottom: 6px;
  font-size: 12px;
  font-weight: 600;
  color: var(--feishu-text-tertiary);
  letter-spacing: 0.02em;
}

.record-card__section dd {
  margin: 0;
  font-size: 14px;
  line-height: 1.65;
  color: var(--feishu-text-primary);
  white-space: pre-wrap;
  word-break: break-word;
}

.record-card__section--highlight dd {
  padding: 10px 12px;
  border-radius: 8px;
  background: var(--feishu-primary-bg, #e8f3ff);
  color: var(--feishu-primary, #3370ff);
  font-weight: 500;
}

.record-card__prescription {
  padding: 12px;
  border-radius: 8px;
  background: var(--feishu-bg-sub, #f5f7fa);
  font-family: inherit;
}

.record-card__footer {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  margin-top: 18px;
  padding-top: 16px;
  border-top: 1px dashed var(--feishu-border-light);
  flex-wrap: wrap;
}

.record-card__actions {
  display: flex;
  gap: 8px;
  flex-shrink: 0;
}

.record-card__hint {
  font-size: 12px;
  color: var(--feishu-text-tertiary);
  flex: 1;
  min-width: 140px;
}

.record-card__ai {
  flex-shrink: 0;
  background: linear-gradient(135deg, #7c3aed, #a78bfa);
  color: #fff;
  border: none;
}

.record-card__export {
  flex-shrink: 0;
}
</style>
