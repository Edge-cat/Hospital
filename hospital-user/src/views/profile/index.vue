<template>
  <ProfilePage>
    <el-card v-loading="loading" shadow="never" class="profile-card">
      <template #header>
        <div class="profile-card__head">
          <span class="page-title">基本信息</span>
          <el-button link type="primary" :loading="loading" @click="refresh">
            <el-icon><Refresh /></el-icon> 刷新
          </el-button>
        </div>
      </template>

      <el-alert v-if="error" type="warning" :title="error" show-icon :closable="false" class="profile-alert" />

      <el-descriptions v-if="profile" :column="1" border>
        <el-descriptions-item label="姓名">{{ profile.name }}</el-descriptions-item>
        <el-descriptions-item label="就诊卡号">
          <span class="mono">{{ profile.cardNo || '—' }}</span>
        </el-descriptions-item>
        <el-descriptions-item label="绑定手机">{{ maskPhone(profile.phone) }}</el-descriptions-item>
        <el-descriptions-item label="身份证号">{{ maskIdCard(profile.idCard) }}</el-descriptions-item>
        <el-descriptions-item label="性别">{{ genderLabel(profile.gender) }}</el-descriptions-item>
        <el-descriptions-item label="过敏史">
          <el-tag v-if="profile.allergyHistory && profile.allergyHistory !== '无'" type="danger" size="small">
            {{ profile.allergyHistory }}
          </el-tag>
          <span v-else>无</span>
        </el-descriptions-item>
        <el-descriptions-item label="慢性病">
          <el-tag v-if="profile.chronicDisease && profile.chronicDisease !== '无'" type="warning" size="small">
            {{ profile.chronicDisease }}
          </el-tag>
          <span v-else>无</span>
        </el-descriptions-item>
        <el-descriptions-item label="联系地址">{{ profile.address || '—' }}</el-descriptions-item>
        <el-descriptions-item label="账户状态"><el-tag type="success">正常</el-tag></el-descriptions-item>
      </el-descriptions>

      <el-empty v-else-if="!loading" description="暂无患者档案" :image-size="72">
        <el-button type="primary" @click="refresh">重新加载</el-button>
      </el-empty>
    </el-card>

    <el-card v-if="healthTip" shadow="never" class="health-card">
      <template #header><span class="page-title">健康提示</span></template>
      <p class="health-tip">{{ healthTip }}</p>
    </el-card>
  </ProfilePage>
</template>

<script setup>
import { computed, onMounted } from 'vue'
import { Refresh } from '@element-plus/icons-vue'
import { usePatientProfile } from '@/composables/usePatientProfile'

const { profile, loading, error, load, refresh } = usePatientProfile()

const healthTip = computed(() => {
  if (!profile.value) return ''
  const tips = []
  if (profile.value.chronicDisease && profile.value.chronicDisease !== '无') {
    tips.push(`您有「${profile.value.chronicDisease}」慢病记录，请按时复诊并遵医嘱用药。`)
  }
  if (profile.value.allergyHistory && profile.value.allergyHistory !== '无') {
    tips.push(`过敏史：${profile.value.allergyHistory}，就诊或用药前请主动告知医生。`)
  }
  if (!tips.length) {
    tips.push('档案信息已同步，可用于挂号、预约时自动填充就诊人姓名。')
  }
  return tips.join(' ')
})

function maskPhone(phone) {
  if (!phone || phone.length < 7) return phone || '—'
  return `${phone.slice(0, 3)}****${phone.slice(-4)}`
}

function maskIdCard(id) {
  if (!id || id.length < 10) return id || '—'
  return `${id.slice(0, 6)}********${id.slice(-4)}`
}

function genderLabel(gender) {
  if (gender === 1) return '男'
  if (gender === 2) return '女'
  return '—'
}

onMounted(() => load())
</script>

<style scoped>
.profile-card__head {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.profile-alert {
  margin-bottom: 16px;
}

.mono {
  font-family: ui-monospace, monospace;
  letter-spacing: 0.5px;
}

.health-card {
  margin-top: 16px;
}

.health-tip {
  margin: 0;
  font-size: 14px;
  line-height: 1.7;
  color: #646a73;
}
</style>
