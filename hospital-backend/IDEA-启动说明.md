# IDEA / 本地启动说明

## 1. 先启动 MySQL（UTF-8 全量数据）

```powershell
powershell -ExecutionPolicy Bypass -File execution/scripts/restart-docker-mysql.ps1
```

MySQL 地址：`127.0.0.1:3307`，用户 `hospital` / `hospital123`

## 2. IDEA 运行后端

打开 `hospital-backend` 模块，运行配置 **HospitalBackend (prod)**（默认已启用）。

连接 Docker MySQL（`127.0.0.1:3307`），**重启后端不会清空业务数据**。

prod 环境变量（已在 `.idea/runConfigurations/HospitalBackend_prod.xml`）：

- `SPRING_PROFILES_ACTIVE=prod`
- `HOSPITAL_DB_URL=jdbc:mysql://127.0.0.1:3307/hospital_his?...utf8mb4...`
- `HOSPITAL_DB_USER=hospital`
- `HOSPITAL_DB_PASSWORD=hospital123`

### DeepSeek AI 密钥（本地，勿提交 Git）

1. 一次性写入（或复制 example 后改 key）：

```powershell
powershell -ExecutionPolicy Bypass -File execution/scripts/setup-ai-secrets.ps1 -ApiKey "sk-你的密钥"
```

生成 `hospital-backend/application-local.yml`（已在 `.gitignore`）。  
IDEA 运行 **HospitalBackend (prod)** 时会自动加载（`application.yml` → `optional:file:./application-local.yml`）。

2. 在 Cursor 中校验 AI 并释放 8080（避免与 IDEA 冲突）：

```powershell
powershell -ExecutionPolicy Bypass -File execution/scripts/verify-deepseek-ai.ps1 -ApiKey "sk-你的密钥"
```

校验通过后进程会自动结束，再在 IDEA 中正常启动即可。

## 3. 中文乱码说明

- **勿用** PowerShell 管道导入 SQL（会导致 `??` / 乱码）
- 使用 `execution/scripts/import-sql-utf8.ps1` 或 `restart-docker-mysql.ps1`
- 后端已强制 `server.servlet.encoding=UTF-8`
- JDBC 使用 `utf8mb4` collation

## 4. 前端

```powershell
# Web — 浏览器直接访问
cd hospital-admin && npm run dev   # http://localhost:5173
cd hospital-user  && npm run dev   # http://localhost:5175

# 小程序 — 另开终端
cd hospital-mini && npm run dev:mp-weixin
# 微信开发者工具导入 hospital-mini/dist/dev/mp-weixin
# 勾选「不校验合法域名」；.env.development → VITE_API_BASE=http://127.0.0.1:8080/api
```

登录验证：

| 端 | 账号 | 密码 |
|----|------|------|
| 管理端 | admin | 123456 |
| 用户端 | patient | 123456 |
| 小程序 | 13800138000 | 123456 |

完整小程序步骤见根目录 [`README.md`](../README.md) **§3.2**。
