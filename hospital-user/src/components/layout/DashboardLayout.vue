<template>
  <div class="dashboard-layout">
    <header v-if="$slots.hero" class="dashboard-layout__hero">
      <slot name="hero" />
    </header>

    <div v-if="$slots.primary || $slots.secondary" class="dashboard-layout__body">
      <section v-if="$slots.primary" class="dashboard-layout__primary">
        <slot name="primary" />
      </section>
      <aside v-if="$slots.secondary" class="dashboard-layout__secondary">
        <slot name="secondary" />
      </aside>
    </div>

    <div v-if="$slots.main || $slots.side" class="dashboard-layout__footer">
      <section v-if="$slots.main" class="dashboard-layout__main">
        <slot name="main" />
      </section>
      <aside v-if="$slots.side" class="dashboard-layout__side">
        <slot name="side" />
      </aside>
    </div>

    <slot />
  </div>
</template>

<style scoped>
.dashboard-layout {
  display: flex;
  flex-direction: column;
  gap: 28px;
}

.dashboard-layout__hero {
  padding: 32px 36px;
  border: 1px solid var(--feishu-border-light);
  border-radius: var(--feishu-radius-lg);
  background: var(--feishu-bg-white, #fff);
  box-shadow: var(--feishu-shadow-sm);
}

.dashboard-layout__body {
  display: grid;
  grid-template-columns: 1fr;
  gap: 20px;
  align-items: stretch;
}

@media (min-width: 900px) {
  .dashboard-layout__body {
    grid-template-columns: 2fr 1fr;
    gap: 24px;
  }
}

.dashboard-layout__primary {
  display: grid;
  grid-template-columns: 1fr;
  gap: 12px;
}

@media (min-width: 640px) {
  .dashboard-layout__primary {
    grid-template-columns: repeat(2, 1fr);
    gap: 14px;
  }
}

.dashboard-layout__secondary {
  display: flex;
  flex-direction: column;
  min-height: 100%;
}

.dashboard-layout__footer {
  display: grid;
  grid-template-columns: 1fr;
  gap: 20px;
}

@media (min-width: 900px) {
  .dashboard-layout__footer {
    grid-template-columns: 1fr 1fr;
    gap: 24px;
  }
}

.dashboard-layout__main,
.dashboard-layout__side {
  display: flex;
  flex-direction: column;
}

.panel {
  padding: 20px 22px;
  border: 1px solid var(--feishu-border-light);
  border-radius: var(--feishu-radius-lg);
  background: var(--feishu-bg-white, #fff);
  box-shadow: var(--feishu-shadow-sm);
}

.panel__head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 16px;
}

.panel__title {
  font-size: 15px;
  font-weight: 600;
  color: var(--feishu-text-primary);
}
</style>
