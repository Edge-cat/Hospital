<template>
  <PageContainer class="search-panel-page">
    <PageHeader title="患者查询" subtitle="360° 综合检索 · 一站式决策支持" />

    <div class="search-hero">
      <el-input
        v-model="keyword"
        size="large"
        placeholder="输入姓名、编号、手机号或身份证号..."
        clearable
        class="search-hero__input"
        @keyup.enter="doSearch"
      >
        <template #prefix><el-icon><Search /></el-icon></template>
        <template #append>
          <el-button type="primary" :loading="loading" @click="doSearch">搜索</el-button>
        </template>
      </el-input>

      <div class="search-chips">
        <span class="search-chips__label">快捷筛选</span>
        <el-check-tag
          v-for="dept in quickDepts"
          :key="dept"
          :checked="activeDept === dept"
          @click="toggleDept(dept)"
        >{{ dept }}</el-check-tag>
      </div>

      <div v-if="recentSearches.length" class="search-history">
        <span class="search-chips__label">最近查询</span>
        <el-tag
          v-for="(item, i) in recentSearches"
          :key="i"
          class="history-tag"
          effect="plain"
          @click="applyHistory(item)"
        >{{ item }}</el-tag>
        <el-button link type="info" size="small" @click="clearHistory">清空</el-button>
      </div>
    </div>

    <div v-if="!searched && !loading" class="search-empty">
      <el-empty description="输入条件开始检索，或选择上方快捷筛选项" :image-size="100" />
    </div>

    <div v-loading="loading">
      <div v-if="searched && !results.length" class="search-empty">
        <el-empty description="未找到匹配患者，请调整检索条件" :image-size="80" />
      </div>

      <div v-if="results.length" class="result-cards">
        <article
          v-for="item in results"
          :key="item.id"
          class="result-card"
          @click="openDetail(item)"
        >
          <header class="result-card__head">
            <div class="result-card__avatar">{{ item.name?.charAt(0) }}</div>
            <div>
              <h3>{{ item.name }} <el-tag size="small">{{ item.gender === 1 ? '男' : '女' }} · {{ item.age }}岁</el-tag></h3>
              <p>{{ item.patientNo }} · {{ item.phone }} · {{ item.department }}</p>
            </div>
            <el-button type="primary" plain round size="small" @click.stop="openDetail(item)">查看全景</el-button>
          </header>

          <div v-if="item._detail" class="result-card__body">
            <el-row :gutter="16">
              <el-col :span="8">
                <div class="mini-panel">
                  <h4>基础信息</h4>
                  <p>身份证：{{ item._detail.idCard || '-' }}</p>
                  <p>地址：{{ item._detail.address || '-' }}</p>
                  <p>过敏：{{ item._detail.allergyHistory || '无' }}</p>
                </div>
              </el-col>
              <el-col :span="8">
                <div class="mini-panel">
                  <h4>最近就诊</h4>
                  <template v-if="item._detail.visitRecords?.length">
                    <p v-for="(v, i) in item._detail.visitRecords.slice(0, 2)" :key="i">
                      {{ v.visitTime?.slice(0, 10) }} · {{ v.diagnosis }}
                    </p>
                  </template>
                  <p v-else class="muted">暂无就诊记录</p>
                </div>
              </el-col>
              <el-col :span="8">
                <div class="mini-panel">
                  <h4>缴费状态</h4>
                  <p>
                    待缴
                    <strong class="pending">¥{{ (item._detail.paymentSummary?.pendingAmount || 0).toFixed(2) }}</strong>
                    · 已缴
                    <strong class="paid">¥{{ (item._detail.paymentSummary?.paidAmount || 0).toFixed(2) }}</strong>
                  </p>
                  <div class="result-card__actions">
                    <el-button size="small" @click.stop="$router.push('/business/payment')">办理缴费</el-button>
                    <el-button size="small" type="primary" @click.stop="$router.push('/patient/consultation')">开始就诊</el-button>
                  </div>
                </div>
              </el-col>
            </el-row>
          </div>
        </article>
      </div>
    </div>

    <PatientPanoramaDrawer v-model:visible="drawerVisible" :patient="currentPatient" />
  </PageContainer>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { patientApi } from '@/api'
import PatientPanoramaDrawer from '@/components/PatientPanoramaDrawer.vue'

const HISTORY_KEY = 'his_patient_search_history'
const quickDepts = ['内科', '外科', '儿科', '骨科', '眼科']
const keyword = ref('')
const activeDept = ref('')
const loading = ref(false)
const searched = ref(false)
const results = ref([])
const recentSearches = ref([])
const drawerVisible = ref(false)
const currentPatient = ref(null)

function loadHistory() {
  try {
    recentSearches.value = JSON.parse(localStorage.getItem(HISTORY_KEY) || '[]').slice(0, 8)
  } catch {
    recentSearches.value = []
  }
}

function saveHistory(kw) {
  if (!kw.trim()) return
  const list = [kw, ...recentSearches.value.filter((v) => v !== kw)].slice(0, 8)
  recentSearches.value = list
  localStorage.setItem(HISTORY_KEY, JSON.stringify(list))
}

function clearHistory() {
  recentSearches.value = []
  localStorage.removeItem(HISTORY_KEY)
}

function applyHistory(kw) {
  keyword.value = kw
  doSearch()
}

function toggleDept(dept) {
  activeDept.value = activeDept.value === dept ? '' : dept
  if (searched.value) doSearch()
}

async function doSearch() {
  loading.value = true
  searched.value = true
  try {
    const res = await patientApi.search({
      keyword: keyword.value,
      department: activeDept.value || undefined,
      page: 1,
      pageSize: 10
    })
    const list = res.data.list || []
    const enriched = await Promise.all(
      list.map(async (p) => {
        try {
          const detailRes = await patientApi.getDetail(p.id)
          return { ...p, _detail: detailRes.data }
        } catch {
          return { ...p, _detail: null }
        }
      })
    )
    results.value = enriched
    if (keyword.value.trim()) saveHistory(keyword.value.trim())
  } finally {
    loading.value = false
  }
}

function openDetail(item) {
  currentPatient.value = item
  drawerVisible.value = true
}

onMounted(loadHistory)
</script>

<style scoped>
.search-hero {
  padding: 28px;
  margin-bottom: 20px;
  border-radius: 16px;
  background: linear-gradient(135deg, #f0f7ff 0%, #fff 100%);
  box-shadow: 0 4px 16px rgba(64, 158, 255, 0.08);
  border: 1px solid rgba(64, 158, 255, 0.1);
}
.search-hero__input { max-width: 720px; }
.search-hero__input :deep(.el-input__wrapper) {
  border-radius: 12px 0 0 12px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.04);
}
.search-chips, .search-history {
  display: flex;
  align-items: center;
  flex-wrap: wrap;
  gap: 8px;
  margin-top: 16px;
}
.search-chips__label {
  font-size: 13px;
  color: var(--feishu-text-tertiary);
  margin-right: 4px;
}
.history-tag { cursor: pointer; }
.result-cards { display: flex; flex-direction: column; gap: 16px; }
.result-card {
  padding: 20px 24px;
  border-radius: 14px;
  background: #fff;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.05);
  border: 1px solid var(--feishu-border-light);
  cursor: pointer;
  transition: box-shadow 0.2s, border-color 0.2s;
}
.result-card:hover {
  border-color: var(--feishu-primary);
  box-shadow: 0 4px 20px rgba(64, 158, 255, 0.12);
}
.result-card__head {
  display: flex;
  align-items: center;
  gap: 16px;
  margin-bottom: 16px;
}
.result-card__avatar {
  width: 48px;
  height: 48px;
  border-radius: 50%;
  background: var(--feishu-primary);
  color: #fff;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 18px;
  font-weight: 600;
  flex-shrink: 0;
}
.result-card__head h3 {
  font-size: 17px;
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 4px;
}
.result-card__head p {
  font-size: 13px;
  color: var(--feishu-text-secondary);
}
.result-card__head .el-button { margin-left: auto; }
.mini-panel {
  padding: 14px;
  border-radius: 10px;
  background: var(--feishu-bg-sub);
  min-height: 120px;
}
.mini-panel h4 {
  font-size: 13px;
  font-weight: 600;
  margin-bottom: 10px;
  color: var(--feishu-text-secondary);
}
.mini-panel p {
  font-size: 13px;
  line-height: 1.7;
  color: var(--feishu-text-primary);
}
.mini-panel .muted { color: var(--feishu-text-tertiary); }
.mini-panel .pending { color: #e6a23c; }
.mini-panel .paid { color: #67c23a; }
.result-card__actions {
  display: flex;
  gap: 8px;
  margin-top: 10px;
}
.search-empty { padding: 40px 0; }
</style>
