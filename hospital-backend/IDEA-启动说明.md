# IDEA / 本地启动说明

## 1. 先启动 MySQL（UTF-8 全量数据）

```powershell
powershell -ExecutionPolicy Bypass -File execution/scripts/restart-docker-mysql.ps1
```

MySQL 地址：`127.0.0.1:3307`，用户 `hospital` / `hospital123`

## 2. IDEA 运行后端

打开 `hospital-backend` 模块，选择运行配置：

| 配置 | 说明 |
|------|------|
| **HospitalBackend (prod)** | 连接 Docker MySQL，**推荐** |
| HospitalBackend (h2) | 内存库，仅快速调试 |

prod 环境变量（已在 `.idea/runConfigurations/HospitalBackend_prod.xml`）：

- `SPRING_PROFILES_ACTIVE=prod`
- `HOSPITAL_DB_URL=jdbc:mysql://127.0.0.1:3307/hospital_his?...utf8mb4...`
- `HOSPITAL_DB_USER=hospital`
- `HOSPITAL_DB_PASSWORD=hospital123`

## 3. 中文乱码说明

- **勿用** PowerShell 管道导入 SQL（会导致 `??` / 乱码）
- 使用 `execution/scripts/import-sql-utf8.ps1` 或 `restart-docker-mysql.ps1`
- 后端已强制 `server.servlet.encoding=UTF-8`
- JDBC 使用 `utf8mb4` collation

## 4. 前端

```powershell
cd hospital-admin && npm run dev   # 5173
cd hospital-user  && npm run dev   # 5175
```

登录：admin / 123456 → 应显示「晚上好，系统管理员」
