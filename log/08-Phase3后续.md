# Phase 3 后续 — 持久化 / 缓存 / 微信 OAuth / 真机部署

> 日期：2026-06-29  
> 前置：Phase 1–2 联调 + 深度回溯（冒烟 28/28）

## 目标

| 项 | 说明 | 状态 |
|----|------|------|
| 菜单持久化 | `sys_menu` 表 + CRUD + 树构建 | ✅ |
| 字典持久化 | `remark` 字段 + `dictType` 过滤 + 种子 | ✅ |
| 财务流水关联 | `income_expense.account_id` + 按账户过滤 | ✅ |
| Redis 缓存 | `spring-boot-starter-cache`（默认 simple）+ redis profile | ✅ |
| 微信 OAuth | `POST /api/auth/wx-login` + 演示模式 | ✅ |
| 小程序真机 | `VITE_API_BASE` + `.env.production` + manifest 超时 | ✅ |

## 后端变更

### 1. 菜单持久化

- **表**：`schema.sql` → `sys_menu`（id, parent_id, name, path, icon, sort, status）
- **实体/服务**：`SysMenu` / `SysMenuMapper` / `SysMenuServiceImpl`
- **控制器**：`SysMenuController` 从 `MenuDataProvider` 内存 stub 改为 DB 读写
- **种子**：`data.sql` 插入 43 条（与 `MenuDataProvider` 对齐）

### 2. 字典增强

- **字段**：`sys_dict.remark`
- **查询**：`PageQuery.dictType` → `SysDictServiceImpl` 按类型过滤
- **种子**：gender / register_type / pay_method / bed_type / notice_type 共 11 条

### 3. 财务流水 account_id

- **字段**：`income_expense.account_id`, `source_module`, `source_id`
- **逻辑**：`FinanceServiceImpl.getFlows(id)` 优先按 `account_id` 过滤，无数据时回退全局
- **种子**：4 条流水分别关联账户 1/2/1/3

### 4. 缓存

- **依赖**：`spring-boot-starter-cache` + `spring-boot-starter-data-redis`
- **配置**：默认 `spring.cache.type=simple`；`redis` profile 启用 Redis
- **服务**：`CommonMetaServiceImpl` + `@Cacheable`（meta / options / enums）
- **H2/MySQL profile**：排除 Redis 自动配置，避免无 Redis 时启动失败

### 5. 微信 OAuth

- **端点**：`POST /api/auth/wx-login`
- **DTO**：`WxLoginRequest`（code, phone）
- **演示模式**：`hospital.wx.demo=true`，默认绑定手机 `13800138000` → patient 用户
- **拦截器**：`/api/auth/wx-login` 免鉴权

## 小程序变更

| 文件 | 变更 |
|------|------|
| `src/utils/request.js` | `BASE_URL = import.meta.env.VITE_API_BASE \|\| '/api'` |
| `.env.development` | `VITE_API_BASE=/api` |
| `.env.production` | `VITE_API_BASE=https://your-api-domain.com/api` |
| `src/api/index.js` | 新增 `wxLogin` |
| `src/pages/login/login.vue` | `uni.login` → `mpApi.wxLogin` 对接后端 |
| `src/manifest.json` | `networkTimeout` 60s；生产 `urlCheck: true` |

### 真机部署说明

1. 在微信公众平台 → 开发管理 → 开发设置 → 服务器域名，添加 **request 合法域名**（如 `https://your-api-domain.com`）
2. 修改 `.env.production` 中 `VITE_API_BASE` 为实际上线 API 地址
3. 使用 `uni build -p mp-weixin` 构建后上传体验版
4. 本地 H5 开发仍可通过 devServer proxy 访问 `/api`

## 冒烟测试

```powershell
cd hospital-backend/scripts
.\smoke-test.ps1          # 18 项基础
.\smoke-test-deep.ps1     # 10 项深度
.\smoke-test-phase3.ps1   # 6 项 Phase3
```

| 脚本 | 项数 | 覆盖 |
|------|------|------|
| smoke-test.ps1 | 18 | 认证/患者/挂号/缴费等 |
| smoke-test-deep.ps1 | 10 | 排班/药房/财务深度 |
| smoke-test-phase3.ps1 | 6 | 菜单/字典/流水/缓存/微信 |

**合计：34 项**

## 配置参考

```yaml
# application.yml
hospital:
  wx:
    demo: true
    demo-phone: "13800138000"
    app-id: wx0000000000000000
    app-secret: demo-secret

# 启用 Redis 缓存（需本地 Redis）
spring.profiles.active: h2,redis
```

## 遗留 / 生产建议

- 关闭 `hospital.wx.demo`，接入微信 `jscode2session` 真实换 openId
- MySQL 生产环境关闭 `spring.sql.init.mode=always`
- Redis profile 与 K8s/云 Redis 地址对接
- 菜单 CRUD 前端管理页可进一步对接真实 POST/PUT/DELETE（后端已支持）
