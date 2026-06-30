<template>
  <el-dialog
    v-model="visible"
    :title="dialogTitle"
    width="720px"
    class="doctor-ai-dialog"
    destroy-on-close
    @close="onClose"
  >
    <el-alert
      type="warning"
      :closable="false"
      show-icon
      class="ai-disclaimer"
      :title="disclaimer || AI_DOCTOR_DISCLAIMER"
    />
    <el-alert
      v-if="demoMode"
      type="info"
      :closable="false"
      show-icon
      class="ai-demo-tip"
      title="演示模式：未配置 DEEPSEEK_API_KEY，返回示例分析。配置后可接入真实 DeepSeek。"
    />

    <div ref="scrollRef" class="ai-chat">
      <div
        v-for="(msg, idx) in messages"
        :key="idx"
        class="ai-msg"
        :class="`ai-msg--${msg.role}`"
      >
        <span class="ai-msg__role">{{ msg.role === 'user' ? '医生' : 'AI 临床辅助' }}</span>
        <div class="ai-msg__bubble">{{ msg.content }}</div>
      </div>
      <div v-if="loading" class="ai-msg ai-msg--assistant">
        <span class="ai-msg__role">AI 临床辅助</span>
        <div class="ai-msg__bubble ai-msg__bubble--loading">正在分析病历…</div>
      </div>
    </div>

    <template #footer>
      <div class="ai-input-bar">
        <el-input
          v-model="input"
          type="textarea"
          :rows="2"
          resize="none"
          placeholder="继续追问，例如：还需排除哪些急症？检查方案是否充分？"
          :disabled="loading"
          @keydown.enter.exact.prevent="onSend"
        />
        <el-button type="primary" round :loading="loading" @click="onSend">发送</el-button>
      </div>
    </template>
  </el-dialog>
</template>

<script setup>
import { computed, nextTick, ref, watch } from 'vue'
import { AI_DOCTOR_DISCLAIMER } from '@/constants/aiDoctorAssist'

const props = defineProps({
  visible: { type: Boolean, default: false },
  patient: { type: Object, default: null },
  messages: { type: Array, default: () => [] },
  loading: { type: Boolean, default: false },
  input: { type: String, default: '' },
  disclaimer: { type: String, default: '' },
  demoMode: { type: Boolean, default: false }
})

const emit = defineEmits(['update:visible', 'update:input', 'send', 'close'])

const scrollRef = ref(null)

const visible = computed({
  get: () => props.visible,
  set: (v) => emit('update:visible', v)
})

const input = computed({
  get: () => props.input,
  set: (v) => emit('update:input', v)
})

const dialogTitle = computed(() => {
  const name = props.patient?.name || '患者'
  return `AI 临床辅助 · ${name}`
})

watch(
  () => [props.messages.length, props.loading],
  async () => {
    await nextTick()
    if (scrollRef.value) {
      scrollRef.value.scrollTop = scrollRef.value.scrollHeight
    }
  }
)

function onSend() {
  const text = input.value.trim()
  if (!text || props.loading) return
  emit('send', text)
}

function onClose() {
  emit('close')
}
</script>

<style scoped>
.ai-disclaimer {
  margin-bottom: 12px;
}

.ai-demo-tip {
  margin-bottom: 12px;
}

.ai-chat {
  max-height: 460px;
  overflow-y: auto;
  padding: 8px 4px;
  background: var(--feishu-bg-sub, #f5f7fa);
  border-radius: 12px;
}

.ai-msg {
  display: flex;
  flex-direction: column;
  margin-bottom: 14px;
}

.ai-msg--user {
  align-items: flex-end;
}

.ai-msg--assistant {
  align-items: flex-start;
}

.ai-msg__role {
  font-size: 11px;
  color: var(--feishu-text-tertiary);
  margin-bottom: 4px;
}

.ai-msg__bubble {
  max-width: 92%;
  padding: 10px 14px;
  border-radius: 12px;
  font-size: 14px;
  line-height: 1.65;
  white-space: pre-wrap;
  word-break: break-word;
}

.ai-msg--user .ai-msg__bubble {
  background: var(--feishu-primary, #3370ff);
  color: #fff;
  border-bottom-right-radius: 4px;
}

.ai-msg--assistant .ai-msg__bubble {
  background: #fff;
  color: var(--feishu-text-primary);
  border: 1px solid var(--feishu-border-light);
  border-bottom-left-radius: 4px;
}

.ai-msg__bubble--loading {
  color: var(--feishu-text-tertiary);
  font-style: italic;
}

.ai-input-bar {
  display: flex;
  gap: 12px;
  align-items: flex-end;
  width: 100%;
}

.ai-input-bar :deep(.el-textarea) {
  flex: 1;
}
</style>
