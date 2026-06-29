# 管理端 Mock 数据层

开发环境下由 `src/mock/index.js` 拦截 `/api/*` 请求，模拟 Spring Boot 后端响应。

## 响应结构（统一）

```json
{
  "code": 200,
  "message": "success",
  "data": { }
}
```

| 类型 | data 结构 |
|------|-----------|
| 单条 | `object` |
| 列表（分页） | `{ list: [], total: number, page: number, pageSize: number }` |
| 变更操作 | `null`（message 描述结果） |
| 字典 | `options` + `enums` 对象 |

## 目录结构

```
mock/
├── index.js              # 入口，注册全部路由
├── register.js           # Mock.mock 路由注册
├── utils/
│   ├── response.js       # success / fail / pageResult
│   └── query.js          # parseQuery / parseBody
└── data/
    ├── static.js         # 静态字典、统计快照、登录账号
    └── store.js          # MockJS 生成的内存数据集
```

## 公共字典 API（视图层禁止硬编码业务选项）

| 接口 | 说明 |
|------|------|
| `GET /api/common/meta` | 全部 options + enums |
| `GET /api/common/options/:key` | 单项下拉，如 departments、roles |
| `GET /api/common/enums/:key` | 单项状态枚举，如 registerStatus |

前端通过 `stores/dict.js` + `composables/useDict.js` 消费，登录后路由守卫自动加载。

---

## 页面 ↔ API 对照表

### 认证 / 布局

| 页面 | 路由 | API |
|------|------|-----|
| 登录 | `/login` | `POST /api/auth/login` |
| 工作台 | `/dashboard` | `GET /api/statistics/overview`、`GET /api/notice/list` |
| 403 | `/403` | — |

### 患者管理

| 页面 | 路由 | API |
|------|------|-----|
| 患者信息管理 | `/patient/info` | `GET/POST/PUT/DELETE /api/patient/*` |
| 医生添加患者 | `/patient/add` | `POST /api/patient` |
| 患者查询 | `/patient/search` | `GET /api/patient/search` |
| 医生开始就诊 | `/patient/consultation` | `GET /api/patient/search`、`POST /api/patient/:id/consultation` |

### 人事管理

| 页面 | 路由 | API |
|------|------|-----|
| 医生信息管理 | `/hr/doctor` | `/api/doctor/*` |
| 排班管理 | `/hr/schedule` | `/api/schedule/*` |
| 诊疗记录管理 | `/hr/record` | `/api/record/*` |
| 医疗服务管理 | `/hr/service` | `/api/service/*` |

### 药房管理

| 页面 | 路由 | API |
|------|------|-----|
| 药品信息管理 | `/pharmacy/drug` | `/api/drug/*` |
| 采购管理 | `/pharmacy/procurement` | `/api/procurement/*` |
| 库存管理 | `/pharmacy/inventory` | `/api/inventory/*` |
| 配药管理 | `/pharmacy/dispensing` | `/api/dispensing/*` |

### 财务管理

| 页面 | 路由 | API |
|------|------|-----|
| 财务信息管理 | `/finance/info` | `/api/finance/*` |
| 收支管理 | `/finance/income-expense` | `/api/income-expense/*` |
| 报销管理 | `/finance/reimbursement` | `/api/reimbursement/*` |
| 结算管理 | `/finance/settlement` | `/api/settlement/*` |

### 统计分析

| 页面 | 路由 | API |
|------|------|-----|
| 数据分析 | `/statistics/analysis` | `GET /api/statistics/analysis` |
| 报表生成 | `/statistics/report` | `GET /api/statistics/reports` |
| 决策支持 | `/statistics/decision` | `GET /api/statistics/decision` |

### 就医流程管理

| 页面 | 路由 | API |
|------|------|-----|
| 挂号管理 | `/business/register` | `/api/register/*` |
| 预约管理 | `/business/appointment` | `/api/appointment/*` |
| 缴费管理 | `/business/payment` | `/api/payment/*` |
| 床位管理 | `/business/bed` | `/api/bed/*` |
| 公告管理 | `/business/notice` | `/api/notice/*` |

### 系统管理

| 页面 | 路由 | API |
|------|------|-----|
| 用户管理 | `/admin/user` | `/api/admin/user/*` |
| 角色管理 | `/admin/role` | `/api/admin/role/*` |
| 科室管理 | `/admin/department` | `/api/admin/department/*` |
| 菜单权限 | `/admin/menu` | `/api/admin/menu/tree` |
| 数据字典 | `/admin/dict` | `/api/admin/dict/*` |
| 系统配置 | `/admin/config` | `/api/admin/config/*` |
| 操作日志 | `/admin/operation-log` | `GET /api/admin/log/operation` |
| 登录日志 | `/admin/login-log` | `GET /api/admin/log/login` |

---

## 切换真实后端

1. 移除或注释 `main.js` 中的 `import('./mock')`
2. 配置 `vite.config.js` 代理指向 Spring Boot
3. 保持 `api/index.js` 路径不变，后端需返回相同 `{ code, message, data }` 结构
4. 实现 `GET /api/common/meta` 字典接口（或拆分为 options/enums 子接口）
