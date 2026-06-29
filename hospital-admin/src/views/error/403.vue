<template>
  <div class="error-page">
    <div class="error-card">
      <div class="error-code">403</div>
      <h2>无权访问</h2>
      <p>当前角色没有访问该页面的权限</p>
      <el-button type="primary" @click="$router.push('/dashboard')">返回工作台</el-button>
    </div>
  </div>
</template>

<style scoped>
.error-page {
  height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background: var(--feishu-bg-base);
}
.error-card { text-align: center; }
.error-code { font-size: 72px; font-weight: 700; color: var(--feishu-primary); line-height: 1; }
.error-card h2 { margin: 16px 0 8px; color: var(--feishu-text-primary); }
.error-card p { color: var(--feishu-text-tertiary); margin-bottom: 24px; }
</style>
