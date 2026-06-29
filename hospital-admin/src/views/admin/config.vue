<template>
  <FormCard title="系统配置" subtitle="医院基础参数与业务规则">
    <template #actions>
      <el-button type="primary" :loading="saving" @click="handleSave">保存配置</el-button>
    </template>
    <el-form ref="formRef" :model="formData" label-width="160px" style="max-width: 640px">
      <el-form-item v-for="item in configList" :key="item.configKey" :label="item.configName">
        <el-input v-model="formData[item.configKey]" />
        <div class="config-remark">{{ item.remark }}</div>
      </el-form-item>
    </el-form>
  </FormCard>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { adminConfigApi } from '@/api'

const configList = ref([])
const formData = reactive({})
const saving = ref(false)

onMounted(async () => {
  const res = await adminConfigApi.list()
  configList.value = res.data
  res.data.forEach((item) => { formData[item.configKey] = item.configValue })
})

async function handleSave() {
  saving.value = true
  try {
    await adminConfigApi.update(formData)
    ElMessage.success('配置已保存')
  } finally {
    saving.value = false
  }
}
</script>
