# 东软云医院 HIS 系统

综合性医疗信息管理系统，采用 **三端分离 + 统一后端** 架构。默认使用 **Docker MySQL + Spring Boot prod**，三端前端通过 REST API 联调（Mock 可选）。

## 设计规范

三端 UI 统一采用 **飞书 Feishu** 设计风格：

- 主色：`#3370FF`
- 背景：`#F5F6F7`
- 文字：`#1F2329` / `#646A73` / `#8F959E`
- 边框：`#DEE0E3` / `#E5E6EB`
- 圆角：6px / 8px，轻边框、少阴影

## 三端概览

| 端 | 目录 | 技术栈 | 端口 | 面向用户 |
|----|------|--------|------|----------|
| **管理端** | [`hospital-admin`](./hospital-admin) | Vue3 + Element Plus | 5173 | 管理员、医生、护士、财务、药房 |
| **用户端** | [`hospital-user`](./hospital-user) | Vue3 + Element Plus | 5175 | 患者（Web 浏览器） |
| **小程序端** | [`hospital-mini`](./hospital-mini) | uni-app + Vue3 | H5:5174 | 患者（微信小程序） |
| **后端** | [`hospital-backend`](./hospital-backend) | Spring Boot 3 + MyBatis-Plus | 8080 | REST API |

## 快速启动（仅前端 Mock）

```bash
# 管理端 — http://localhost:5173
cd hospital-admin && npm install && npm run dev

# 用户端 — http://localhost:5175
cd hospital-user && npm install && npm run dev

# 小程序端（Mock，无需后端）
cd hospital-mini && npm install
# 临时改 .env.development：VITE_USE_MOCK=true
npm run dev:mp-weixin
# 微信开发者工具导入 dist/dev/mp-weixin，勾选「不校验合法域名」
```

> **全栈联调**（MySQL + IDEA 后端 + 三端真实 API）请见 **「GitHub 克隆后快速启动」** 与 **「IDEA 一键部署与启动操作方案」**（§3.2 为小程序完整步骤）。

## 功能分工

### 管理端（hospital-admin）

医院内部后台，工作人员使用：

- 患者管理、人事管理、药房管理、财务管理、统计分析
- 就医流程管理（挂号/预约/缴费/床位/公告审核）
- 系统管理（用户/角色/科室/权限/日志/操作控制台）
- 医生接诊 **AI 临床辅助**（DeepSeek，可选）

演示账号：`admin` / `123456`（另有 `doctor`、`nurse` 等，密码均为 `123456`）

### 用户端（hospital-user）

患者 Web 自助服务：

- 在线挂号、在线预约、在线缴费
- 科室介绍、医院公告、就诊记录（含 **AI 问诊**）
- 个人中心、我的挂号/预约

演示账号：`patient` / `123456`

### 小程序端（hospital-mini）

与用户端功能对齐，适配微信生态：

- 底部 Tab：首页 / 预约（含当日挂号）/ 缴费 / 我的
- 就诊记录支持 **AI 问诊**、诊疗报告导出
- 支持微信快捷登录（演示模式见后端 `HOSPITAL_WX_DEMO`）

演示账号：`13800138000` / `123456`（或 `patient` / `123456`）

### AI 能力（DeepSeek，可选）

| 场景 | 端 | 入口 | API |
|------|-----|------|-----|
| 患者就诊后康复咨询 | 用户 Web / 小程序 | 就诊记录 → **AI 问诊** | `POST /api/ai/consult` |
| 医生接诊临床辅助 | 管理端 | 医生开始就诊 → **AI 临床辅助** | `POST /api/ai/doctor-assist` |

未配置 API Key 时后端返回**演示模式**示例回复；配置 Key 后接入真实 DeepSeek。详见下文 **§8**。

## 后端对接

三端共用同一套 REST API（`/api/*`），对接 Spring Boot 时各端配置如下：

| 端 | Mock 开关 | API 基址（开发） | 说明 |
|----|-----------|------------------|------|
| 管理端 | `.env.development` → `VITE_USE_MOCK=false` | `/api`（Vite proxy → `:8080`） | 浏览器直连 |
| 用户端 | 同上 | `/api` | 同上 |
| **小程序** | 同上 | **`http://127.0.0.1:8080/api`** | 微信环境无 Vite proxy，须用绝对地址 |

小程序环境变量见 [`hospital-mini/.env.development`](./hospital-mini/.env.development)。本地联调时在微信开发者工具中勾选 **「不校验合法域名」**（详见文末启动方案 §3.2）。

## 项目结构

```
Hospital/
├── hospital-admin/      # 管理端
├── hospital-user/       # 用户端
├── hospital-mini/       # 小程序端
├── hospital-backend/    # Spring Boot 后端
├── execution/           # 部署脚本、全量 SQL、Docker、Nginx
│   ├── scripts/         # restart-docker-mysql、setup-ai-secrets、smoke-test 等
│   └── database/        # hospital_his_full.sql 与 alter_*.sql 增量脚本
├── log/                 # 开发历程与审计记录
└── README.md            # 本文件
```

> 说明：早期版本的 `hospital-web` 已拆分为 `hospital-admin` 与 `hospital-user`，请使用上述三端目录。

---

## GitHub 克隆后快速启动（推荐顺序）

> 面向**第一次拉取仓库的开发者**，按下列顺序执行即可在 15–30 分钟内完成全栈联调。  
> 详细运维部署见 [`execution/01-完整部署流程.md`](./execution/01-完整部署流程.md)。

| 步骤 | 命令 / 操作 | 说明 |
|------|-------------|------|
| 0 | 安装 JDK 17、Maven、Node 18+、Docker Desktop、IDEA（可选） | 小程序另需 [微信开发者工具](https://developers.weixin.qq.com/miniprogram/dev/devtools/download.html) |
| 1 | `powershell -ExecutionPolicy Bypass -File execution/scripts/restart-docker-mysql.ps1` | 启动 MySQL `:3307`，库 `hospital_his` |
| 2 | （可选）配置 DeepSeek → 见 **§8** | 不配置也可运行，AI 为演示模式 |
| 3 | IDEA 运行 **`HospitalBackend (prod)`** 或见 **§2** 命令行 | 端口 **8080** |
| 4 | `cd hospital-admin && npm i && npm run dev` | http://localhost:5173 |
| 5 | `cd hospital-user && npm i && npm run dev` | http://localhost:5175 |
| 6 | `cd hospital-mini && npm i && npm run dev:mp-weixin` | 导入 `dist/dev/mp-weixin` 至微信开发者工具 |
| 7 | `powershell -ExecutionPolicy Bypass -File execution/scripts/smoke-test.ps1` | 可选，API 冒烟 |

**上传 GitHub 前请注意**（见 **§9**）：勿提交 `application-local.yml`、`.env.local` 及任何真实 API Key。

---

## IDEA 一键部署与启动操作方案

> 面向**第一次在本机拉取项目的开发者**，约 15–30 分钟可完成全栈联调。  
> 详细运维部署见 [`execution/01-完整部署流程.md`](./execution/01-完整部署流程.md)。

### 0. 环境准备

| 软件 | 版本 | 用途 |
|------|------|------|
| **IntelliJ IDEA** | 2023+ | 后端开发与运行 |
| **JDK** | 17 | Spring Boot 3 |
| **Maven** | 3.9+ | 后端构建（IDEA 自带亦可） |
| **Node.js** | 18+ | 三端前端 |
| **Docker Desktop** | 最新 | MySQL 容器（推荐） |
| **微信开发者工具** | [稳定版](https://developers.weixin.qq.com/miniprogram/dev/devtools/download.html) | 小程序编译产物预览与调试 |

### 1. 启动 MySQL（Docker，推荐）

**Windows PowerShell：**

```powershell
powershell -ExecutionPolicy Bypass -File execution/scripts/restart-docker-mysql.ps1
```

连接信息：Host `127.0.0.1`，Port **3307**，库 `hospital_his`，用户/密码 `hospital` / `hospital123`。

> **重要**：勿用 PowerShell 管道导入中文 SQL（会乱码）。请使用 `execution/scripts/import-sql-utf8.ps1` 或 `restart-docker-mysql.ps1`。

**已有旧 Docker 卷、需增量升级时**（新克隆一般不需要，重建库用上面脚本即可）：

```powershell
# 推荐：复制 SQL 进容器再 source，避免中文编码问题
docker cp execution/database/alter_sys_department_code.sql hospital-mysql:/tmp/
docker exec hospital-mysql mysql -uhospital -phospital123 hospital_his -e "source /tmp/alter_sys_department_code.sql"
```

常用增量脚本（按需执行，位于 `execution/database/`）：

| 脚本 | 用途 |
|------|------|
| `alter_sys_department_code.sql` | 科室 `code` 字段（修复 `Unknown column 'code'`） |
| `alter_medical_record_order_items.sql` | 医嘱 JSON 字段 |
| `alter_payment_record_id.sql` | 缴费关联病历 |
| `alter_nurse_billing_menu.sql` | 护士医嘱扣费菜单 |
| `alter_operation_log.sql` | 操作日志表 |
| `alter_register_time_slot.sql` | 挂号时段 |
| `alter_payment_register_id.sql` / `alter_payment_register_no.sql` | 缴费关联挂号 |

全量重建（会清空数据）：再次运行 `restart-docker-mysql.ps1`（导入 `execution/database/hospital_his_full.sql`）。

### 2. IDEA 导入并启动后端

1. **File → Open** → 选择 `Hospital/hospital-backend`
2. 等待 Maven 依赖下载
3. 运行配置选择 **`HospitalBackend (prod)`**（见 `.idea/runConfigurations/HospitalBackend_prod.xml`）
4. 点击 **Run** ▶，确认控制台 `Started HospitalApplication`，端口 **8080**

**验证登录：**

```bash
curl -X POST http://127.0.0.1:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d "{\"username\":\"admin\",\"password\":\"123456\"}"
```

**命令行备选：**

```powershell
cd hospital-backend
mvn package -DskipTests
$env:HOSPITAL_DB_URL='jdbc:mysql://127.0.0.1:3307/hospital_his?useUnicode=true&characterEncoding=utf8&connectionCollation=utf8mb4_unicode_ci&serverTimezone=Asia/Shanghai&allowPublicKeyRetrieval=true&useSSL=false'
$env:HOSPITAL_DB_USER='hospital'
$env:HOSPITAL_DB_PASSWORD='hospital123'
$env:HOSPITAL_JWT_SECRET='local-dev-jwt-secret-min-256-bits-long-enough-for-his'
$env:HOSPITAL_WX_DEMO='true'
java -jar target/hospital-backend-1.0.0-SNAPSHOT.jar --spring.profiles.active=prod
```

更多说明：[`hospital-backend/IDEA-启动说明.md`](./hospital-backend/IDEA-启动说明.md)

> 小程序联调需后端 **`HOSPITAL_WX_DEMO=true`**（已在上述 prod 运行配置 / 命令行示例中设置），用于演示微信快捷登录。

### 3. 启动前端（Web + 小程序）

全栈联调推荐顺序：**MySQL → IDEA 后端 :8080 → Web 两端口 → 小程序 dev 编译 → 微信开发者工具导入**。

#### 3.1 Web 端（IDEA 内置终端 / VS Code / Cursor 均可）

在 IDEA 中打开 **Terminal**，或任意编辑器终端执行：

```bash
# 管理端 — http://localhost:5173
cd hospital-admin && npm install && npm run dev

# 用户端 — http://localhost:5175
cd hospital-user && npm install && npm run dev
```

两端的 `.env.development` 已配置 `VITE_USE_MOCK=false`，请求经 Vite proxy 转发到 `http://localhost:8080`。

#### 3.2 小程序端（uni-app + 微信开发者工具）

小程序**不能**像 Web 一样在浏览器里直接打开，需要 **Node 编译** + **微信开发者工具** 两步。

**（1）安装依赖并启动 watch 编译**

```bash
cd hospital-mini
npm install
npm run dev:mp-weixin
```

保持该终端运行；源码变更后会自动输出到 `hospital-mini/dist/dev/mp-weixin`。

**（2）配置 API（联调真实后端，默认已写好）**

[`hospital-mini/.env.development`](./hospital-mini/.env.development)：

```env
VITE_USE_MOCK=false
VITE_API_BASE=http://127.0.0.1:8080/api
```

| 模式 | `VITE_USE_MOCK` | `VITE_API_BASE` | 适用场景 |
|------|-----------------|-----------------|----------|
| **联调后端（推荐）** | `false` | `http://127.0.0.1:8080/api` | 与 admin/user 同一套数据 |
| 纯前端 Mock | `true` | 任意 | 后端未启动时快速看 UI |
| H5 浏览器预览 | `false` | `/api` | `npm run dev:h5` → http://localhost:5174 |

修改 `.env.development` 后需 **重启** `npm run dev:mp-weixin`。

**（3）微信开发者工具导入项目**

1. 打开 [微信开发者工具](https://developers.weixin.qq.com/miniprogram/dev/devtools/download.html)
2. **导入项目** → 目录选择：`Hospital/hospital-mini/dist/dev/mp-weixin`（不是 `hospital-mini` 根目录）
3. AppID：选 **测试号** 或填入你在 `src/manifest.json` 中配置的 AppID
4. **详情 → 本地设置** → 勾选 **「不校验合法域名、web-view（业务域名）、TLS 版本以及 HTTPS 证书」**（本地 `127.0.0.1:8080` 必需）
5. 编译完成后在模拟器中访问首页 Tab

**（4）生产构建（可选，用于提审或真机预览包）**

```bash
cd hospital-mini
npm run build:mp-weixin
# 导入目录改为 dist/build/mp-weixin
```

**（5）在 IDEA 中操作小程序**

IDEA 本身不编译 uni-app，常用方式：

- 使用 IDEA **Terminal** 运行 `npm run dev:mp-weixin`（与后端同窗口或分屏）
- 或使用 **VS Code / Cursor** 编辑 `hospital-mini`，IDEA 专注跑 `HospitalBackend (prod)`

更细的页面说明见 [`hospital-mini/README.md`](./hospital-mini/README.md)；已知缺陷与修复记录见 [`bug/fix-log/00-总览.md`](./bug/fix-log/00-总览.md)。

### 4. 登录验证

| 端 | 访问方式 | 账号 | 密码 | 备注 |
|----|----------|------|------|------|
| 管理端 | http://localhost:5173 | admin | 123456 | Web |
| 用户端 | http://localhost:5175 | patient | 123456 | Web |
| 小程序 | 微信开发者工具模拟器 | 13800138000 | 123456 | 手机号登录 |
| 小程序 | 同上 | patient | 123456 | 用户名登录亦可 |
| 小程序 | 「微信快捷登录」 | — | — | 需后端 `HOSPITAL_WX_DEMO=true` |

**小程序冒烟路径（联调通过参考）**：

1. 首页 → 常见症状标签 → 进入科室详情  
2. 底部 Tab「预约」→ 左侧出现科室列表 → 选医生  
3. 我的 → 登录 → 返回后「就诊回顾」有数据  
4. 管理端操作（如审核公告）→ 小程序刷新可见（同一后端）

### 5. 运维脚本速查（execution/scripts）

| 脚本 | 用途 |
|------|------|
| `restart-docker-mysql.ps1` | 重建 Docker MySQL 容器并导入全量种子数据 |
| `import-sql-utf8.ps1 -SqlFile <path>` | UTF-8 安全导入单个 SQL（避免 PowerShell 管道乱码） |
| `setup-ai-secrets.ps1 -ApiKey "sk-xxx"` | 写入 gitignore 的 `application-local.yml` |
| `verify-deepseek-ai.ps1 [-ApiKey "sk-xxx"]` | 临时启动后端 → 校验 AI Key → 释放 8080 供 IDEA 使用 |
| `build-frontends.ps1` | 三端前端生产构建 |
| `smoke-test.ps1` | API 冒烟测试（约 47 项） |

示例：

```powershell
# 增量 SQL
powershell -ExecutionPolicy Bypass -File execution/scripts/import-sql-utf8.ps1 `
  -SqlFile execution/database/alter_sys_department_code.sql

# DeepSeek 密钥 + 校验（校验结束后自动停后端，8080 留给 IDEA）
powershell -ExecutionPolicy Bypass -File execution/scripts/setup-ai-secrets.ps1 -ApiKey "sk-your-key"
powershell -ExecutionPolicy Bypass -File execution/scripts/verify-deepseek-ai.ps1
```

### 6. 可选：本地网关与冒烟

```powershell
# 8088 聚合网关（需先 build 三端）
node execution/scripts/local-gateway.mjs

# API 冒烟（约 47 项）
powershell -ExecutionPolicy Bypass -File execution/scripts/smoke-test.ps1
```

### 7. 常见问题

| 现象 | 处理 |
|------|------|
| 中文 `??` | 用 `restart-docker-mysql.ps1` 重建；勿管道导入 SQL |
| 患者端 403 | 应调用 `/api/common/*`，勿调 `/api/admin/*` |
| 8080 登录 500 | 检查 Docker MySQL 与 prod 环境变量 |
| JAR 打包失败 | 先停止占用 8080 的后端进程 |
| **小程序科室/登录全失败** | 确认 `.env.development` 为 `http://127.0.0.1:8080/api`；开发者工具勾选「不校验合法域名」；后端 :8080 已启动 |
| **小程序导入目录错误** | 必须导入 `dist/dev/mp-weixin`，不是项目根目录 |
| **微信登录失败** | IDEA 运行配置需 `HOSPITAL_WX_DEMO=true`；或改用账号 `13800138000` / `123456` |
| **修改 env 不生效** | 重启 `npm run dev:mp-weixin` 后在开发者工具中重新编译 |
| **H5 预览与小程序行为不一致** | H5 可用 `/api` proxy；小程序必须用绝对 API 地址 |
| **`Unknown column 'code'`** | 执行 `alter_sys_department_code.sql`（见 §1） |
| **AI 始终演示模式** | 配置 `application-local.yml` 或运行 `setup-ai-secrets.ps1`；IDEA 工作目录须为 `hospital-backend` |
| **DeepSeek 402 余额不足** | Key 已生效，需在 [DeepSeek 平台](https://platform.deepseek.com) 充值；演示模式仍可用 |
| **小程序无 AI 问诊按钮** | 源码已含功能，需 `npm run dev:mp-weixin` 或 `build:mp-weixin` 后重新导入 `dist/dev` 或 `dist/build` |

### 8. DeepSeek AI 配置（可选）

后端通过 `hospital.ai.*` 接入 DeepSeek。**未配置 Key 时**接口仍可用，返回演示回复。

**方式 A：PowerShell 脚本（推荐）**

```powershell
powershell -ExecutionPolicy Bypass -File execution/scripts/setup-ai-secrets.ps1 -ApiKey "sk-your-deepseek-api-key"
```

**方式 B：手动复制模板**

```powershell
cd hospital-backend
copy application-local.yml.example application-local.yml
# 编辑 application-local.yml，填入 api-key
```

`application.yml` 已包含 `spring.config.import: optional:file:./application-local.yml`，IDEA 运行 **`HospitalBackend (prod)`** 时工作目录设为 `hospital-backend` 即可加载。

**校验 Key 并释放 8080：**

```powershell
powershell -ExecutionPolicy Bypass -File execution/scripts/verify-deepseek-ai.ps1
# 或一步写入并校验：
powershell -ExecutionPolicy Bypass -File execution/scripts/verify-deepseek-ai.ps1 -ApiKey "sk-your-key"
```

脚本会：启动后端 → 调用 `GET /api/ai/status` → 尝试 `POST /api/ai/consult` → 停止 Java 进程。若 DeepSeek 返回 **402**，说明 Key 有效但余额不足，脚本仍 exit 0。

**前端入口：**

| 端 | 路径 |
|----|------|
| 用户 Web | 就诊记录页 → **AI 问诊** |
| 小程序 | 我的 → 就诊回顾 → **AI 问诊**（需已解锁就诊记录） |
| 管理端 | 医生接诊页 → **AI 临床辅助**（就诊中且未锁定时） |

更多后端说明：[`hospital-backend/IDEA-启动说明.md`](./hospital-backend/IDEA-启动说明.md)

### 9. 上传 GitHub 前检查清单

| 检查项 | 说明 |
|--------|------|
| **勿提交密钥** | `hospital-backend/application-local.yml` 已在 `.gitignore`；确认 `git status` 中无该文件 |
| **勿提交本地 env** | `.env.local` 已忽略；各端 `.env.development` 可提交（无真实 Key） |
| **DeepSeek Key** | 仅通过 `setup-ai-secrets.ps1` 或本地 `application-local.yml` 配置，勿写入仓库 |
| **演示数据** | 全量 SQL 在 `execution/database/hospital_his_full.sql`，克隆后运行 `restart-docker-mysql.ps1` |
| **子项目 README** | 小程序细节见 [`hospital-mini/README.md`](./hospital-mini/README.md)；缺陷修复见 [`bug/fix-log/00-总览.md`](./bug/fix-log/00-总览.md) |

克隆后第三者按本文 **「GitHub 克隆后快速启动」** 表格顺序操作即可。

### 10. 生产与扩展

- 全量 SQL：`execution/database/hospital_his_full.sql`
- Docker + Nginx：`execution/docker-compose.yml`
- 审计记录：`log/16-最终检查.md`

**刻意保留的扩展项**（不影响演示验收）：第三方支付 SDK、生产微信 OAuth、前端 menuIds 动态路由。
