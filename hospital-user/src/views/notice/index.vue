<template>
  <div v-loading="loading" class="notice-page">
    <PageHeader title="医院公告" subtitle="了解医院最新通知与安排" />

    <div class="notice-toolbar">
      <el-tabs v-model="activeType" class="notice-tabs" @tab-change="onTabChange">
        <el-tab-pane v-for="tab in NOTICE_TABS" :key="tab.key" :name="tab.key">
          <template #label>
            <span class="tab-label">{{ tab.label }}</span>
            <el-badge
              v-if="unreadByType(tab.key)"
              :value="unreadByType(tab.key)"
              :max="99"
              class="tab-badge"
            />
          </template>
        </el-tab-pane>
      </el-tabs>

      <el-select v-model="sortMode" size="small" class="notice-sort" @change="applyFilter">
        <el-option v-for="opt in NOTICE_SORT_OPTIONS" :key="opt.value" :label="opt.label" :value="opt.value" />
      </el-select>
    </div>

    <el-card v-if="displayList.length" shadow="never" class="notice-card">
      <div
        v-for="item in displayList"
        :key="item.id"
        class="notice-row"
        :class="{
          'notice-row--urgent': item.type === '紧急',
          'notice-row--unread': !isRead(item.id)
        }"
        @click="openNotice(item)"
      >
        <span v-if="!isRead(item.id)" class="notice-dot" aria-hidden="true" />
        <el-tag :type="NOTICE_TYPE_TAG[item.type] || 'info'" size="small" effect="light">{{ item.type }}</el-tag>
        <span class="title" :class="{ 'title--unread': !isRead(item.id) }">{{ item.title }}</span>
        <span class="time">{{ formatDate(item.publishTime) }}</span>
      </div>
    </el-card>
    <el-empty v-else-if="!loading" description="暂无公告" :image-size="80" />
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { userApi } from '@/api'
import { useNoticeRead } from '@/composables/useNoticeRead'
import { NOTICE_TABS, NOTICE_TYPE_TAG, NOTICE_SORT_OPTIONS, sortNotices } from '@/constants/notice'

const router = useRouter()
const loading = ref(false)
const allNotices = ref([])
const activeType = ref('all')
const sortMode = ref('priority')
const { isRead, markRead, refresh } = useNoticeRead()

const filteredList = computed(() => {
  let list = allNotices.value.filter((n) => n.status === 1 || n.status === undefined)
  if (activeType.value && activeType.value !== 'all') {
    list = list.filter((n) => n.type === activeType.value)
  }
  return sortNotices(list, sortMode.value)
})

const displayList = computed(() => filteredList.value)

function unreadByType(typeKey) {
  const list = typeKey && typeKey !== 'all'
    ? allNotices.value.filter((n) => n.type === typeKey && (n.status === 1 || n.status === undefined))
    : allNotices.value.filter((n) => n.status === 1 || n.status === undefined)
  return list.filter((n) => !isRead(n.id)).length
}

function formatDate(t) {
  return t ? String(t).slice(0, 10) : ''
}

function onTabChange() {
  applyFilter()
}

function applyFilter() {
  /* computed 自动更新 */
}

function openNotice(item) {
  markRead(item.id)
  router.push(`/notice/${item.id}`)
}

async function loadData() {
  loading.value = true
  try {
    const res = await userApi.notices({ page: 1, pageSize: 50, status: 1 })
    allNotices.value = res.data?.list ?? []
    refresh()
  } finally {
    loading.value = false
  }
}

onMounted(loadData)
</script>

<style scoped>
.notice-toolbar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16px;
  margin-bottom: 16px;
  flex-wrap: wrap;
}
.notice-tabs { flex: 1; min-width: 280px; }
.notice-tabs :deep(.el-tabs__header) { margin-bottom: 0; }
.tab-label { margin-right: 4px; }
.tab-badge { vertical-align: middle; }
.notice-sort { width: 130px; flex-shrink: 0; }
.notice-card { border-radius: 12px; }
.notice-row {
  position: relative;
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 14px 12px 14px 16px;
  border-bottom: 1px solid var(--feishu-border-light);
  cursor: pointer;
  transition: background 0.15s;
}
.notice-row:last-child { border-bottom: none; }
.notice-row:hover { background: var(--feishu-bg-sub); }
.notice-row--urgent {
  border: 1px solid #fde2e2;
  border-left: 4px solid #f56c6c;
  border-radius: 8px;
  margin-bottom: 8px;
  background: linear-gradient(90deg, #fef0f0 0%, #fff 100%);
}
.notice-row--urgent:last-child { margin-bottom: 0; }
.notice-row--unread .title--unread {
  font-weight: 700;
  color: var(--feishu-text-primary);
}
.notice-dot {
  width: 8px;
  height: 8px;
  border-radius: 50%;
  background: #f56c6c;
  flex-shrink: 0;
}
.notice-row .title {
  flex: 1;
  font-size: 14px;
  color: var(--feishu-text-secondary);
  min-width: 0;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}
.notice-row .time {
  color: var(--feishu-text-tertiary);
  font-size: 13px;
  flex-shrink: 0;
  font-variant-numeric: tabular-nums;
}
</style>
