# 东软云医院 HIS 系统

综合性医疗信息管理系统，采用 **三端分离** 架构。

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
# 管理端
cd hospital-admin && npm install && npm run dev

# 用户端
cd hospital-user && npm install && npm run dev

# 小程序端
cd hospital-mini && npm install && npm run dev:mp-weixin
# 微信开发者工具导入 dist/dev/mp-weixin
```

> 联调真实后端请见文末 **「IDEA 一键部署与启动操作方案」**。

## 功能分工

### 管理端（hospital-admin）

医院内部后台，工作人员使用：

- 患者管理、人事管理、药房管理、财务管理、统计分析
- 就医流程管理（挂号/预约/缴费/床位/公告审核）
- 系统管理（用户/角色/科室/权限/日志/操作控制台）

演示账号：`admin` / `123456`（另有 `doctor`、`nurse` 等，密码均为 `123456`）

### 用户端（hospital-user）

患者 Web 自助服务：

- 在线挂号、在线预约、在线缴费
- 科室介绍、医院公告、就诊记录
- 个人中心、我的挂号/预约

演示账号：`patient` / `123456`

### 小程序端（hospital-mini）

与用户端功能对齐，适配微信生态：

- 底部 Tab：首页 / 挂号 / 预约 / 缴费 / 我的
- 支持微信快捷登录（演示模式见后端 `HOSPITAL_WX_DEMO`）

演示账号：`13800138000` / `123456`（或 `patient` / `123456`）

## 后端对接

三端共用同一套 REST API（`/api/*`），对接 Spring Boot 时：

1. 各项目 `vite.config.js` 配置 proxy 指向 `http://localhost:8080`
2. 确保 `.env.development` 中 `VITE_USE_MOCK=false`（默认已关）
3. 小程序 `src/utils/api.js` 中 `VITE_USE_MOCK=false`

## 项目结构

```
Hospital/
├── hospital-admin/      # 管理端
├── hospital-user/       # 用户端
├── hospital-mini/       # 小程序端
├── hospital-backend/    # Spring Boot 后端
├── execution/           # 部署脚本、全量 SQL、Docker、Nginx
├── log/                 # 开发历程与审计记录
└── README.md            # 本文件
```

> 说明：早期版本的 `hospital-web` 已拆分为 `hospital-admin` 与 `hospital-user`，请使用上述三端目录。

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

### 1. 启动 MySQL（Docker，推荐）

**Windows PowerShell：**

```powershell
powershell -ExecutionPolicy Bypass -File execution/scripts/restart-docker-mysql.ps1
```

连接信息：Host `127.0.0.1`，Port **3307**，库 `hospital_his`，用户/密码 `hospital` / `hospital123`。

> **重要**：勿用 PowerShell 管道导入中文 SQL（会乱码）。请使用 `execution/scripts/import-sql-utf8.ps1`。

已有旧库增量升级：

```powershell
Get-Content execution/database/alter_operation_log.sql -Raw |
  docker exec -i hospital-mysql mysql -uhospital -phospital123 hospital_his
```

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

### 3. 启动三端前端

```bash
cd hospital-admin && npm install && npm run dev   # http://localhost:5173
cd hospital-user  && npm install && npm run dev   # http://localhost:5175
cd hospital-mini  && npm install && npm run dev:mp-weixin
```

### 4. 登录验证

| 端 | 地址 | 账号 | 密码 |
|----|------|------|------|
| 管理端 | http://localhost:5173 | admin | 123456 |
| 用户端 | http://localhost:5175 | patient | 123456 |
| 小程序 | 微信开发者工具 | 13800138000 | 123456 |

### 5. 可选：本地网关与冒烟

```powershell
# 8088 聚合网关（需先 build 三端）
node execution/scripts/local-gateway.mjs

# API 冒烟（约 47 项）
powershell -ExecutionPolicy Bypass -File execution/scripts/smoke-test.ps1
```

### 6. 常见问题

| 现象 | 处理 |
|------|------|
| 中文 `??` | 用 `restart-docker-mysql.ps1` 重建；勿管道导入 SQL |
| 患者端 403 | 应调用 `/api/common/*`，勿调 `/api/admin/*` |
| 8080 登录 500 | 检查 Docker MySQL 与 prod 环境变量 |
| JAR 打包失败 | 先停止占用 8080 的后端进程 |

### 7. 生产与扩展

- 全量 SQL：`execution/database/hospital_his_full.sql`
- Docker + Nginx：`execution/docker-compose.yml`
- 审计记录：`log/16-最终检查.md`

**刻意保留的扩展项**（不影响演示验收）：第三方支付 SDK、生产微信 OAuth、前端 menuIds 动态路由。
