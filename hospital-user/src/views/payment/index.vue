<template>
  <ProfilePage>
    <div class="payment-page">
      <PageHeader title="在线缴费" subtitle="查看待缴与已缴费用">
        <el-radio-group v-model="tab">
          <el-radio-button value="pending">待缴费</el-radio-button>
          <el-radio-button value="paid">已缴费</el-radio-button>
        </el-radio-group>
      </PageHeader>

      <div v-if="tab === 'pending' && summary.count" class="summary-card">
        <div class="summary-card__main">
          <span class="summary-card__label">待缴合计</span>
          <span class="summary-card__amount">¥{{ summary.totalAmount.toFixed(2) }}</span>
          <span class="summary-card__count">共 {{ summary.count }} 笔待缴</span>
        </div>
        <div v-if="selectedIds.length" class="summary-card__batch">
          <span>已选 {{ selectedIds.length }} 笔 · ¥{{ selectedAmount.toFixed(2) }}</span>
          <el-button type="primary" round :loading="payLocked" @click="onBatchPay">批量支付</el-button>
        </div>
      </div>

      <div v-if="tab === 'paid'" class="filter-bar">
        <span class="filter-label">支付时间</span>
        <el-date-picker
          v-model="dateRange"
          type="daterange"
          range-separator="至"
          start-placeholder="开始日期"
          end-placeholder="结束日期"
          value-format="YYYY-MM-DD"
          size="default"
          @change="onDateFilter"
        />
        <el-button link type="primary" @click="clearDateFilter">重置</el-button>
      </div>

      <div class="pay-method-bar">
        <span class="filter-label">支付渠道</span>
        <el-radio-group v-model="payMethod" size="small">
          <el-radio-button v-for="m in PAY_METHODS" :key="m" :value="m">{{ m }}</el-radio-button>
        </el-radio-group>
      </div>

      <div v-if="tab === 'pending' && list.length" class="select-all-bar">
        <el-checkbox
          :model-value="selectedIds.length === list.length && list.length > 0"
          :indeterminate="selectedIds.length > 0 && selectedIds.length < list.length"
          @change="toggleSelectAll"
        >全选本页</el-checkbox>
      </div>

      <div v-loading="loading" class="payment-list">
        <el-empty v-if="!loading && !list.length" description="暂无缴费记录" :image-size="72" />

        <article v-for="row in list" :key="row.id" class="payment-card" :class="{ overdue: isOverdue(row) }">
          <header class="payment-card__head">
            <div class="payment-card__head-left">
              <el-checkbox
                v-if="tab === 'pending'"
                :model-value="selectedIds.includes(row.id)"
                @change="(v) => toggleSelect(row.id, v)"
              />
              <span class="payment-card__no">{{ row.paymentNo }}</span>
            </div>
            <div class="payment-card__tags">
              <el-tag v-if="isOverdue(row)" type="danger" size="small">已逾期</el-tag>
              <el-tag :type="PAYMENT_STATUS[row.status]?.type" size="small">
                {{ PAYMENT_STATUS[row.status]?.label }}
              </el-tag>
            </div>
          </header>

          <dl class="payment-card__rows">
            <div class="payment-card__row">
              <dt>收费项目</dt>
              <dd>{{ row.itemName }}</dd>
            </div>
            <div class="payment-card__row">
              <dt>患者姓名</dt>
              <dd>{{ row.patientName }}</dd>
            </div>
            <div class="payment-card__row payment-card__row--amount">
              <dt>金额</dt>
              <dd>¥{{ row.amount?.toFixed(2) }}</dd>
            </div>
            <div v-if="row.status === 0 && row.dueDate" class="payment-card__row">
              <dt>缴费截止</dt>
              <dd :class="{ 'text-danger': isOverdue(row) }">{{ row.dueDate }}</dd>
            </div>
            <div v-if="row.createTime" class="payment-card__row">
              <dt>账单生成</dt>
              <dd>{{ row.createTime }}</dd>
            </div>
            <div v-if="row.status === 1" class="payment-card__row">
              <dt>支付方式</dt>
              <dd>{{ row.payMethod }}</dd>
            </div>
            <div v-if="row.status === 1 && row.payTime" class="payment-card__row">
              <dt>支付时间</dt>
              <dd>{{ row.payTime }}</dd>
            </div>
          </dl>

          <el-button
            v-if="row.status === 0"
            type="primary"
            round
            class="payment-card__btn"
            :loading="payingId === row.id"
            :disabled="payLocked && payingId !== row.id"
            @click="onPay(row)"
          >
            立即支付 ¥{{ row.amount?.toFixed(2) }}
          </el-button>

          <el-button
            v-if="row.status === 1"
            round
            class="payment-card__btn payment-card__btn--ghost"
            @click="downloadReceipt(row)"
          >
            下载缴费凭证
          </el-button>
        </article>
      </div>

      <el-pagination
        v-if="total > pageSize"
        class="payment-pagination"
        background
        layout="total, prev, pager, next"
        :total="total"
        :page-size="pageSize"
        :current-page="page"
        @current-change="onPageChange"
      />
    </div>
  </ProfilePage>
</template>

<script setup>
import { ElMessage, ElMessageBox } from 'element-plus'
import { useHospitalPayment } from '@/composables/useHospitalView'
import { PAYMENT_STATUS, PAY_METHODS } from '@/constants'

const {
  tab,
  list,
  loading,
  payingId,
  payLocked,
  page,
  pageSize,
  total,
  summary,
  selectedIds,
  selectedAmount,
  payMethod,
  dateRange,
  payBill,
  payBatch,
  toggleSelect,
  toggleSelectAll,
  onPageChange,
  onDateFilter,
  refreshAll
} = useHospitalPayment()

function isOverdue(row) {
  if (row.status !== 0 || !row.dueDate) return false
  const today = new Date()
  today.setHours(0, 0, 0, 0)
  return new Date(row.dueDate) < today
}

function clearDateFilter() {
  dateRange.value = null
  onDateFilter()
}

async function onPay(row) {
  try {
    await ElMessageBox.confirm(
      `使用「${payMethod.value}」支付 ¥${row.amount.toFixed(2)}？`,
      '支付确认',
      { type: 'info' }
    )
  } catch {
    return
  }
  const ok = await payBill(row)
  if (ok) {
    ElMessage.success('支付成功，列表已同步')
    await refreshAll()
  }
}

async function onBatchPay() {
  if (!selectedIds.value.length) return
  try {
    await ElMessageBox.confirm(
      `使用「${payMethod.value}」批量支付 ${selectedIds.value.length} 笔，合计 ¥${selectedAmount.value.toFixed(2)}？`,
      '批量支付确认',
      { type: 'info' }
    )
  } catch {
    return
  }
  const ok = await payBatch()
  if (ok) {
    ElMessage.success('批量支付成功，列表已同步')
    await refreshAll()
  }
}

function downloadReceipt(row) {
  const content = [
    '东软云医院 — 电子缴费凭证',
    '────────────────────────',
    `凭证编号：${row.paymentNo}`,
    `收费项目：${row.itemName}`,
    `患者姓名：${row.patientName}`,
    `支付金额：¥${row.amount?.toFixed(2)}`,
    `支付方式：${row.payMethod || '-'}`,
    `支付时间：${row.payTime || '-'}`,
    '────────────────────────',
    '此凭证由系统自动生成，仅供个人查阅。'
  ].join('\n')
  const blob = new Blob([content], { type: 'text/plain;charset=utf-8' })
  const url = URL.createObjectURL(blob)
  const link = document.createElement('a')
  link.href = url
  link.download = `缴费凭证_${row.paymentNo}.txt`
  link.click()
  URL.revokeObjectURL(url)
  ElMessage.success('凭证已下载')
}
</script>

<style scoped>
.payment-page {
  max-width: 720px;
}

.summary-card {
  display: flex;
  flex-wrap: wrap;
  align-items: center;
  justify-content: space-between;
  gap: 16px;
  padding: 20px 24px;
  margin-bottom: 16px;
  border: 1px solid var(--feishu-border-light);
  border-radius: var(--feishu-radius-lg);
  background: linear-gradient(135deg, #f0f4ff 0%, #fff 100%);
  box-shadow: var(--feishu-shadow-sm);
}

.summary-card__label {
  display: block;
  font-size: 13px;
  color: var(--feishu-text-tertiary);
}

.summary-card__amount {
  display: block;
  font-size: 28px;
  font-weight: 700;
  color: var(--feishu-danger);
  font-variant-numeric: tabular-nums;
}

.summary-card__count {
  display: block;
  font-size: 12px;
  color: var(--feishu-text-secondary);
  margin-top: 4px;
}

.summary-card__batch {
  display: flex;
  align-items: center;
  gap: 12px;
  font-size: 14px;
  color: var(--feishu-text-secondary);
}

.filter-bar,
.pay-method-bar,
.select-all-bar {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 16px;
  flex-wrap: wrap;
}

.filter-label {
  font-size: 13px;
  color: var(--feishu-text-tertiary);
  flex-shrink: 0;
}

.payment-list {
  display: flex;
  flex-direction: column;
  gap: 16px;
  min-height: 200px;
}

.payment-card {
  padding: 20px 22px;
  border: 1px solid var(--feishu-border-light);
  border-radius: var(--feishu-radius-lg);
  background: var(--feishu-bg-white, #fff);
  box-shadow: var(--feishu-shadow-sm);
  transition: border-color 0.2s;
}

.payment-card.overdue {
  border-color: #fbc4c4;
  background: #fffafa;
}

.payment-card__head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 16px;
  gap: 12px;
}

.payment-card__head-left {
  display: flex;
  align-items: center;
  gap: 10px;
}

.payment-card__tags {
  display: flex;
  gap: 8px;
  flex-shrink: 0;
}

.payment-card__no {
  font-size: 13px;
  color: var(--feishu-text-tertiary);
  font-variant-numeric: tabular-nums;
}

.payment-card__rows {
  margin: 0;
}

.payment-card__row {
  display: flex;
  justify-content: space-between;
  padding: 8px 0;
  font-size: 14px;
  border-bottom: 1px solid var(--feishu-border-light);
}

.payment-card__row:last-child {
  border-bottom: none;
}

.payment-card__row dt {
  color: var(--feishu-text-tertiary);
  font-weight: 400;
}

.payment-card__row dd {
  margin: 0;
  color: var(--feishu-text-primary);
  font-weight: 500;
}

.payment-card__row--amount dd {
  font-size: 18px;
  font-weight: 700;
  color: var(--feishu-danger);
}

.text-danger {
  color: var(--feishu-danger) !important;
  font-weight: 600;
}

.payment-card__btn {
  width: 100%;
  margin-top: 16px;
}

.payment-card__btn--ghost {
  background: var(--feishu-primary-bg);
  color: var(--feishu-primary);
  border: none;
}

.payment-pagination {
  margin-top: 24px;
  justify-content: center;
}
</style>
