<template>

  <ListPage

    title="公告管理"

    :query="query"

    :loading="loading"

    :data="tableData"

    :total="total"

    :show-search="false"

    @search="loadData"

    @reset="resetQuery"

    @page-change="loadData"

  >

    <template #actions>

      <el-button type="primary" @click="openDialog()">发布公告</el-button>

    </template>



    <el-table-column prop="title" label="公告标题" min-width="200" show-overflow-tooltip />

    <el-table-column prop="type" label="类型" width="90">

      <template #default="{ row }">

        <StatusTag enum-key="noticeTypeTag" :value="row.type" />

      </template>

    </el-table-column>

    <el-table-column prop="publisher" label="发布人" width="100" />

    <el-table-column prop="status" label="状态" width="80">

      <template #default="{ row }">

        <StatusTag :type="row.status === 1 ? 'success' : 'info'" :label="row.status === 1 ? '已发布' : '草稿'" />

      </template>

    </el-table-column>

    <el-table-column prop="publishTime" label="发布时间" width="170" />

    <el-table-column label="操作" width="200" fixed="right">

      <template #default="{ row }">

        <el-button link type="primary" @click="handleView(row)">查看</el-button>

        <el-button link type="primary" @click="openDialog(row)">编辑</el-button>

        <el-button link type="danger" @click="handleDelete(row)">删除</el-button>

      </template>

    </el-table-column>



    <template #extra>

      <FormDialog v-model:visible="dialogVisible" :title="form.id ? '编辑公告' : '发布公告'" width="600px" @confirm="handleSubmit">

        <el-form ref="formRef" :model="form" :rules="rules" label-width="80px">

          <el-form-item label="标题" prop="title"><el-input v-model="form.title" /></el-form-item>

          <el-form-item label="类型" prop="type">

            <DictSelect v-model="form.type" dict-key="noticeTypes" />

          </el-form-item>

          <el-form-item label="内容" prop="content"><el-input v-model="form.content" type="textarea" :rows="6" /></el-form-item>

          <el-form-item label="状态"><el-switch v-model="form.status" :active-value="1" :inactive-value="0" active-text="发布" inactive-text="草稿" /></el-form-item>

        </el-form>

      </FormDialog>



      <el-dialog v-model="viewVisible" title="公告详情" width="560px">

        <h3 style="margin-bottom: 12px">{{ currentNotice?.title }}</h3>

        <el-descriptions :column="2" border size="small" style="margin-bottom: 16px">

          <el-descriptions-item label="类型">{{ currentNotice?.type }}</el-descriptions-item>

          <el-descriptions-item label="发布人">{{ currentNotice?.publisher }}</el-descriptions-item>

          <el-descriptions-item label="发布时间" :span="2">{{ currentNotice?.publishTime }}</el-descriptions-item>

        </el-descriptions>

        <div class="notice-content">{{ currentNotice?.content }}</div>

      </el-dialog>

    </template>

  </ListPage>

</template>



<script setup>

import { ref, reactive, onMounted } from 'vue'

import { ElMessage, ElMessageBox } from 'element-plus'

import { noticeApi } from '@/api'



const loading = ref(false)

const tableData = ref([])

const total = ref(0)

const dialogVisible = ref(false)

const viewVisible = ref(false)

const currentNotice = ref(null)

const formRef = ref()

const query = reactive({ page: 1, pageSize: 10 })

const form = reactive({ id: null, title: '', type: '通知', content: '', status: 1 })

const rules = { title: [{ required: true, message: '请输入标题', trigger: 'blur' }], content: [{ required: true, message: '请输入内容', trigger: 'blur' }] }



function resetQuery() {

  query.page = 1

  loadData()

}



async function loadData() {

  loading.value = true

  try { const res = await noticeApi.list(query); tableData.value = res.data.list; total.value = res.data.total }

  finally { loading.value = false }

}



function openDialog(row) {

  Object.assign(form, row || { id: null, title: '', type: '通知', content: '', status: 1 })

  dialogVisible.value = true

}



function handleView(row) {

  currentNotice.value = row

  viewVisible.value = true

}



async function handleSubmit() {

  await formRef.value.validate()

  if (form.id) await noticeApi.update(form.id, form)

  else await noticeApi.create(form)

  ElMessage.success('操作成功')

  dialogVisible.value = false

  loadData()

}



async function handleDelete(row) {

  await ElMessageBox.confirm('确定删除该公告吗？', '提示', { type: 'warning' })

  await noticeApi.remove(row.id)

  ElMessage.success('删除成功')

  loadData()

}



onMounted(loadData)

</script>



<style scoped>

.notice-content { line-height: 1.8; color: var(--feishu-text-secondary); white-space: pre-wrap; }

</style>


