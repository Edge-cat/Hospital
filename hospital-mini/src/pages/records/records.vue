<template>
  <view class="container">
    <view v-if="needLogin" class="state-box">
      <text class="state-tip">请先登录后查看就诊记录</text>
      <button class="retry-btn" size="mini" @click="goLogin">去登录</button>
    </view>
    <view v-else-if="loading" class="state-tip">加载中…</view>
    <view v-else-if="error" class="state-box">
      <text class="state-tip error">{{ error }}</text>
      <button class="retry-btn" size="mini" @click="loadRecords">重新加载</button>
    </view>
    <view v-else-if="list.length === 0" class="empty-box">
      <text class="empty-title">暂无已解锁的就诊记录</text>
      <text class="empty-desc">医生完成接诊后，请先前往「缴费中心」支付检查费/药品费</text>
      <text class="empty-desc">全部诊疗费用缴清后，此处将展示诊断与处方信息</text>
      <button class="retry-btn" size="mini" @click="goPayment">去缴费</button>
    </view>

    <view v-for="item in enrichedList" v-else :key="item.id" class="card record-card">
      <view class="record-header">
        <view class="record-head-left">
          <text class="dept">{{ item.department }}</text>
          <text class="record-no">MR{{ String(item.id).padStart(8, '0') }}</text>
        </view>
        <text class="tag tag-success">已归档</text>
      </view>

      <view class="record-meta">
        <text>{{ item.doctorName }} · 主治医师</text>
        <text>{{ item.visitTimeLabel }}</text>
      </view>

      <view v-if="item.chiefComplaint" class="record-section">
        <text class="section-label">主诉</text>
        <text class="section-value">{{ item.chiefComplaint }}</text>
      </view>

      <view class="record-section highlight">
        <text class="section-label">诊断结果</text>
        <text class="section-value">{{ item.diagnosis || '—' }}</text>
      </view>

      <view v-if="item.examItems" class="record-section">
        <text class="section-label">检查项目</text>
        <text class="section-value">{{ item.examItems }}</text>
      </view>

      <view v-if="item.prescription" class="record-section">
        <text class="section-label">处方 / 医嘱</text>
        <text class="section-value prescription">{{ item.prescription }}</text>
      </view>
      <view v-else-if="item.treatmentSummary" class="record-section">
        <text class="section-label">诊疗说明</text>
        <text class="section-value">{{ item.treatmentSummary }}</text>
      </view>

      <view class="record-actions">
        <button class="ai-btn-main" @click="onAiConsult(item)">
          AI 问诊 · 获取就诊后建议
        </button>
        <button class="export-btn-outline" size="mini" @click="onExport(item)">导出诊疗报告</button>
      </view>

      <view class="record-footer">
        <text class="footer-hint">报告已解锁 · 内容由医生填写，AI 建议仅供参考</text>
      </view>
    </view>

    <AiConsultPanel
      :visible="aiVisible"
      :record="aiRecord"
      :messages="aiMessages"
      :loading="aiLoading"
      :input="aiInput"
      :disclaimer="aiDisclaimer"
      :demo-mode="aiDemoMode"
      :error-msg="aiErrorMsg"
      @update:input="(v) => (aiInput = v)"
      @send="onAiSend"
      @close="closeAiConsult"
    />

    <PageNav variant="footer" />
  </view>
</template>

<script setup>
import { ref, computed } from 'vue'
import { onShow, onLoad } from '@dcloudio/uni-app'
import { mpApi } from '@/api'
import PageNav from '@/components/PageNav.vue'
import AiConsultPanel from '@/components/AiConsultPanel.vue'
import { checkLogin } from '@/utils/request'
import { navigateToLogin, safeNavigateTo } from '@/utils/nav'
import { usePatientProfile } from '@/composables/usePatientProfile'
import { useAiConsult } from '@/composables/useAiConsult'
import { parseTreatment, formatVisitTime, exportMedicalReport } from '@/utils/medicalReport'

const list = ref([])
const loading = ref(false)
const error = ref(null)
const needLogin = ref(false)
const { profile, load: loadProfile } = usePatientProfile()
const {
  visible: aiVisible,
  record: aiRecord,
  messages: aiMessages,
  loading: aiLoading,
  input: aiInput,
  disclaimer: aiDisclaimer,
  demoMode: aiDemoMode,
  errorMsg: aiErrorMsg,
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

function goLogin() {
  navigateToLogin()
}

function goPayment() {
  safeNavigateTo('/pages/payment/payment')
}

async function loadRecords() {
  if (!checkLogin()) {
    needLogin.value = true
    list.value = []
    error.value = null
    loading.value = false
    return
  }
  needLogin.value = false
  loading.value = true
  error.value = null
  try {
    const res = await mpApi.records({ pageSize: 100 })
    list.value = res.data?.list ?? []
  } catch (e) {
    error.value = e?.message || '加载失败，请稍后重试'
    list.value = []
  } finally {
    loading.value = false
  }
}

async function onExport(row) {
  const info = profile.value || (await loadProfile({ silent: true }))
  try {
    const result = await exportMedicalReport(row, info || {})
    uni.showToast({
      title: result.opened ? '报告已打开' : '报告已复制到剪贴板',
      icon: 'success'
    })
  } catch {
    uni.showToast({ title: '导出失败', icon: 'none' })
  }
}

async function onAiConsult(row) {
  try {
    await openAiConsult(row)
  } catch (e) {
    uni.showToast({ title: e?.message || 'AI 问诊失败', icon: 'none' })
  }
}

async function onAiSend(text) {
  try {
    await sendAiConsult(text)
  } catch (e) {
    uni.showToast({ title: e?.message || 'AI 回复失败', icon: 'none' })
  }
}

const pendingAiRecordId = ref(null)

onLoad((options) => {
  if (options?.aiRecordId) {
    pendingAiRecordId.value = Number(options.aiRecordId)
  }
})

async function tryOpenPendingAi() {
  if (!pendingAiRecordId.value || !list.value.length) return
  const row = list.value.find((r) => r.id === pendingAiRecordId.value)
  pendingAiRecordId.value = null
  if (row) await onAiConsult(row)
}

onShow(async () => {
  await loadRecords()
  await loadProfile({ silent: true })
  await tryOpenPendingAi()
})
</script>

<style lang="scss" scoped>
.state-tip {
  text-align: center;
  color: #909399;
  padding: 48rpx 0;
  font-size: 28rpx;
}

.state-tip.error {
  color: #f56c6c;
}

.state-box,
.empty-box {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 16rpx;
  padding: 48rpx 32rpx;
}

.empty-title {
  font-size: 30rpx;
  font-weight: 600;
  color: #303133;
}

.empty-desc {
  font-size: 26rpx;
  color: #909399;
  text-align: center;
  line-height: 1.6;
}

.retry-btn {
  background: #3370ff;
  color: #fff;
  border: none;
  border-radius: 32rpx;
  margin-top: 8rpx;
}

.record-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 12rpx;
}

.record-head-left {
  display: flex;
  align-items: center;
  gap: 12rpx;
}

.dept {
  font-size: 32rpx;
  font-weight: 600;
  color: #303133;
}

.record-no {
  font-size: 22rpx;
  color: #909399;
  font-family: monospace;
}

.record-meta {
  display: flex;
  flex-wrap: wrap;
  gap: 8rpx 24rpx;
  padding-bottom: 16rpx;
  margin-bottom: 16rpx;
  border-bottom: 1rpx solid #ebeef5;
  font-size: 24rpx;
  color: #909399;
}

.record-section {
  margin-bottom: 20rpx;
}

.section-label {
  display: block;
  font-size: 22rpx;
  font-weight: 600;
  color: #909399;
  margin-bottom: 8rpx;
}

.section-value {
  display: block;
  font-size: 28rpx;
  line-height: 1.65;
  color: #303133;
  white-space: pre-wrap;
  word-break: break-word;
}

.record-section.highlight .section-value {
  padding: 16rpx 20rpx;
  border-radius: 12rpx;
  background: #e8f3ff;
  color: #3370ff;
  font-weight: 500;
}

.section-value.prescription {
  padding: 16rpx 20rpx;
  border-radius: 12rpx;
  background: #f5f7fa;
}

.record-actions {
  display: flex;
  flex-direction: column;
  gap: 16rpx;
  margin-top: 8rpx;
}

.ai-btn-main {
  width: 100%;
  background: linear-gradient(135deg, #7c3aed, #a78bfa);
  color: #fff;
  border: none;
  border-radius: 48rpx;
  font-size: 30rpx;
  font-weight: 600;
  height: 88rpx;
  line-height: 88rpx;
  box-shadow: 0 8rpx 24rpx rgba(124, 58, 237, 0.25);
}

.ai-btn-main::after {
  border: none;
}

.export-btn-outline {
  align-self: flex-end;
  background: #fff;
  color: #3370ff;
  border: 1rpx solid #3370ff;
  border-radius: 32rpx;
}

.export-btn-outline::after {
  border: none;
}

.record-footer {
  margin-top: 16rpx;
  padding-top: 16rpx;
  border-top: 1rpx dashed #ebeef5;
}

.footer-hint {
  font-size: 22rpx;
  color: #909399;
  line-height: 1.5;
}
</style>
