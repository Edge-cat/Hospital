# 管理端公共组件

全局注册于 `main.js`，页面中可直接使用。

## 布局组件

| 组件 | 用途 |
|------|------|
| `PageContainer` | 页面根容器（`.page-container` 内边距） |
| `PageHeader` | 标题 + 副标题 + 右侧操作区 |
| `ListPage` | **列表页标准布局**（Header + 搜索 + 表格 + 分页） |
| `FormCard` | **表单页标准布局**（Header + 表单卡片） |

## 数据展示

| 组件 | 用途 |
|------|------|
| `SearchToolbar` | 通用搜索栏（查询/重置按钮 + 过滤器插槽） |
| `DataTable` | 带 loading、空状态、分页的标准表格 |
| `TableCard` | 纯表格卡片（无分页，供 DataTable 内部使用） |
| `AppPagination` | 统一分页条 |
| `StatOverview` | 统计概览卡片行 |
| `StatusTag` | 字典枚举 / 启用禁用 状态标签 |

## 表单与弹窗

| 组件 | 用途 |
|------|------|
| `FormDialog` | 标准 CRUD 弹窗（取消/确定） |
| `DictSelect` | 字典下拉（数据来自 `/api/common/meta`） |

## ListPage 用法

```vue
<ListPage
  title="页面标题"
  subtitle="可选副标题"
  :query="query"
  :loading="loading"
  :data="tableData"
  :total="total"
  @search="loadData"
  @reset="resetQuery"
  @page-change="loadData"
>
  <template #actions>
    <el-button type="primary">新增</el-button>
  </template>
  <template #filters>
    <el-form-item label="关键词">
      <el-input v-model="query.keyword" clearable />
    </el-form-item>
  </template>
  <el-table-column prop="name" label="名称" />
  <template #extra>
    <FormDialog v-model:visible="visible" title="编辑" @confirm="submit">
      <!-- 表单 -->
    </FormDialog>
  </template>
</ListPage>
```

## 主题变量

所有颜色使用 `src/styles/feishu-theme.css` 中的 CSS 变量：

- `--feishu-primary` / `--feishu-primary-hover` / `--feishu-primary-bg`
- `--feishu-success` / `--feishu-warning` / `--feishu-danger`
- `--feishu-text-primary` / `--feishu-text-secondary` / `--feishu-text-tertiary`
- `--feishu-border` / `--feishu-border-light` / `--feishu-bg-base` / `--feishu-bg-sub`

页面 scoped 样式应优先使用 `var(--feishu-*)`，避免硬编码色值。
