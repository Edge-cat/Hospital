# 东软云医院 HIS — 部署执行手册

> 本目录供第三方/运维直接按步骤完成生产部署。  
> 项目开发日志见 `../log/`；本目录聚焦**可执行部署**。

| 文件 | 说明 |
|------|------|
| [01-完整部署流程.md](./01-完整部署流程.md) | 从环境准备到验收的完整步骤 |
| [database/hospital_his_full.sql](./database/hospital_his_full.sql) | **全量 MySQL 脚本**（建库 + 表结构 + 充盈种子） |
| [database/hospital_his_seed_demo.sql](./database/hospital_his_seed_demo.sql) | 已有库演示数据增量补丁 |
| [nginx/hospital.conf](./nginx/hospital.conf) | Nginx 反代示例（三端 Web + API） |
| [docker-compose.yml](./docker-compose.yml) | MySQL + 后端可选一键启动 |
| [scripts/build-frontends.ps1](./scripts/build-frontends.ps1) | 三端前端构建脚本 |
| [scripts/local-gateway.mjs](./scripts/local-gateway.mjs) | Windows 本地网关（无 Nginx 时联调） |
| [docker-compose.local.yml](./docker-compose.local.yml) | 3306 占用时 MySQL 改 3307 |
| [docker-compose.nginx.yml](./docker-compose.nginx.yml) | Docker Nginx 联调（需可拉取镜像） |
| [nginx/hospital.local.conf](./nginx/hospital.local.conf) | 本地 HTTP Nginx 配置 |

## 快速开始（本地验证生产配置）

```powershell
# 1. 初始化 MySQL
mysql -u root -p < execution/database/hospital_his_full.sql

# 2. 启动后端（prod profile）
cd hospital-backend
$env:HOSPITAL_DB_PASSWORD="your_password"
$env:HOSPITAL_JWT_SECRET="your-256-bit-secret-key-change-me"
mvn -q spring-boot:run -Dspring-boot.run.arguments="--spring.profiles.active=prod"

# 3. 构建前端
powershell -ExecutionPolicy Bypass -File execution/scripts/build-frontends.ps1

# 4. 冒烟
cd hospital-backend/scripts
powershell -ExecutionPolicy Bypass -File smoke-test.ps1

# 5. 本地网关联调（无 Nginx 时）
node execution/scripts/local-gateway.mjs
# 访问 http://127.0.0.1:8088/admin/ 与 /user/
```

## 默认演示账号（初始化后）

| 账号 | 密码 | 端 |
|------|------|-----|
| admin | 123456 | 管理端 |
| patient / 13800138000 | 123456 | 用户 Web / 小程序 |

生产环境请首次登录后立即修改密码。
