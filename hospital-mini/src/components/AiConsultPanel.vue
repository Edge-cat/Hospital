<template>
  <view v-if="visible" class="ai-mask" @click="onClose">
    <view class="ai-panel" @click.stop>
      <view class="ai-header">
        <text class="ai-title">AI 问诊 · {{ record?.department || '就诊记录' }}</text>
        <text class="ai-close" @click="onClose">×</text>
      </view>

      <view class="ai-disclaimer">
        <text>{{ disclaimer || AI_DISCLAIMER }}</text>
      </view>
      <view v-if="demoMode" class="ai-demo-tip">
        演示模式：未配置 DEEPSEEK_API_KEY，返回示例建议
      </view>
      <view v-if="errorMsg" class="ai-error-tip">
        <text>{{ errorMsg }}</text>
      </view>

      <scroll-view scroll-y class="ai-chat" :scroll-into-view="scrollIntoView">
        <view
          v-for="(msg, idx) in messages"
          :id="'msg-' + idx"
          :key="idx"
          class="ai-msg"
          :class="'ai-msg--' + msg.role"
        >
          <text class="ai-role">{{ msg.role === 'user' ? '我' : 'AI 助手' }}</text>
          <view class="ai-bubble">{{ msg.content }}</view>
        </view>
        <view v-if="loading" id="msg-loading" class="ai-msg ai-msg--assistant">
          <text class="ai-role">AI 助手</text>
          <view class="ai-bubble loading">正在思考…</view>
        </view>
      </scroll-view>

      <view class="ai-input-bar">
        <input
          v-model="inputModel"
          class="ai-input"
          placeholder="继续追问，例如：饮食要注意什么？"
          confirm-type="send"
          :disabled="loading"
          @confirm="onSend"
        />
        <button class="ai-send" :loading="loading" :disabled="loading" @click="onSend">发送</button>
      </view>
    </view>
  </view>
</template>

<script setup>
import { computed, ref, watch } from 'vue'
import { AI_DISCLAIMER } from '@/constants/aiConsult'

const props = defineProps({
  visible: { type: Boolean, default: false },
  record: { type: Object, default: null },
  messages: { type: Array, default: () => [] },
  loading: { type: Boolean, default: false },
  input: { type: String, default: '' },
  disclaimer: { type: String, default: '' },
  demoMode: { type: Boolean, default: false },
  errorMsg: { type: String, default: '' }
})

const emit = defineEmits(['update:input', 'send', 'close'])

const scrollIntoView = ref('')

const inputModel = computed({
  get: () => props.input,
  set: (v) => emit('update:input', v)
})

watch(
  () => [props.messages.length, props.loading],
  () => {
    scrollIntoView.value = props.loading ? 'msg-loading' : `msg-${Math.max(props.messages.length - 1, 0)}`
  }
)

function onSend() {
  const text = inputModel.value.trim()
  if (!text || props.loading) return
  emit('send', text)
}

function onClose() {
  emit('close')
}
</script>

<style lang="scss" scoped>
.ai-mask {
  position: fixed;
  inset: 0;
  background: rgba(0, 0, 0, 0.45);
  z-index: 2000;
  display: flex;
  align-items: flex-end;
}

.ai-panel {
  width: 100%;
  max-height: 88vh;
  background: #fff;
  border-radius: 32rpx 32rpx 0 0;
  display: flex;
  flex-direction: column;
  padding-bottom: env(safe-area-inset-bottom);
}

.ai-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 28rpx 32rpx 16rpx;
}

.ai-title {
  font-size: 32rpx;
  font-weight: 700;
  color: #1f2329;
}

.ai-close {
  font-size: 48rpx;
  color: #8f959e;
  line-height: 1;
  padding: 0 8rpx;
}

.ai-disclaimer {
  margin: 0 32rpx 12rpx;
  padding: 16rpx 20rpx;
  background: #fff9eb;
  border: 1rpx solid #ffe7ba;
  border-radius: 12rpx;
  font-size: 22rpx;
  color: #ff8800;
  line-height: 1.5;
}

.ai-demo-tip {
  margin: 0 32rpx 12rpx;
  padding: 12rpx 20rpx;
  background: #e8f3ff;
  border-radius: 12rpx;
  font-size: 22rpx;
  color: #3370ff;
}

.ai-error-tip {
  margin: 0 32rpx 12rpx;
  padding: 12rpx 20rpx;
  background: #fef0f0;
  border: 1rpx solid #fbc4c4;
  border-radius: 12rpx;
  font-size: 22rpx;
  color: #f56c6c;
  line-height: 1.5;
}

.ai-chat {
  flex: 1;
  min-height: 360rpx;
  max-height: 52vh;
  padding: 16rpx 32rpx;
  background: #f5f6f7;
  box-sizing: border-box;
}

.ai-msg {
  display: flex;
  flex-direction: column;
  margin-bottom: 24rpx;
}

.ai-msg--user {
  align-items: flex-end;
}

.ai-msg--assistant {
  align-items: flex-start;
}

.ai-role {
  font-size: 20rpx;
  color: #8f959e;
  margin-bottom: 8rpx;
}

.ai-bubble {
  max-width: 88%;
  padding: 20rpx 24rpx;
  border-radius: 20rpx;
  font-size: 28rpx;
  line-height: 1.65;
  white-space: pre-wrap;
  word-break: break-word;
}

.ai-msg--user .ai-bubble {
  background: #3370ff;
  color: #fff;
  border-bottom-right-radius: 6rpx;
}

.ai-msg--assistant .ai-bubble {
  background: #fff;
  color: #1f2329;
  border: 1rpx solid #e5e6eb;
  border-bottom-left-radius: 6rpx;
}

.ai-bubble.loading {
  color: #8f959e;
  font-style: italic;
}

.ai-input-bar {
  display: flex;
  gap: 16rpx;
  padding: 16rpx 32rpx 24rpx;
  border-top: 1rpx solid #e5e6eb;
  background: #fff;
}

.ai-input {
  flex: 1;
  height: 72rpx;
  padding: 0 24rpx;
  background: #f5f6f7;
  border-radius: 36rpx;
  font-size: 28rpx;
}

.ai-send {
  background: linear-gradient(135deg, #7c3aed, #a78bfa);
  color: #fff;
  border: none;
  border-radius: 36rpx;
  font-size: 28rpx;
  padding: 0 32rpx;
  height: 72rpx;
  line-height: 72rpx;
}

.ai-send::after {
  border: none;
}
</style>
