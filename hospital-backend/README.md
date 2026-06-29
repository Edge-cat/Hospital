# 东软云医院 HIS — 后端服务

Spring Boot 3.2 + MyBatis-Plus + MySQL/H2，与三端前端 Mock 契约对齐。

## 技术栈

- Java 17、Spring Boot 3.2.5
- MyBatis-Plus 3.5.7
- JWT（jjwt 0.12）
- MySQL 8（生产）/ H2 内存库（本地默认）

## 快速启动

```bash
cd hospital-backend
mvn spring-boot:run
```

默认 **H2 内存库**（`spring.profiles.active=h2`），无需安装 MySQL 即可启动，端口 **8080**。

### 使用 MySQL

1. 创建数据库：`CREATE DATABASE hospital_his DEFAULT CHARSET utf8mb4;`
2. 修改 `application.yml` 中 mysql profile 的用户名密码
3. 启动：`mvn spring-boot:run -Dspring-boot.run.arguments=--spring.profiles.active=mysql`

`schema.sql` 与 `data.sql` 会在启动时自动执行。

## API 契约

与前端一致：

```json
{ "code": 200, "message": "success", "data": {} }
```

分页：`{ "list": [], "total": 0, "page": 1, "pageSize": 10 }`

除登录与公共字典外，请求头需携带：

```
Authorization: Bearer <token>
```

## 演示账号

| 账号 | 密码 | 角色 |
|------|------|------|
| admin | 123456 | 管理员 |
| doctor | 123456 | 医生 |
| patient | 123456 | 患者 |
| 13800138000 | 123456 | 患者（小程序） |

## 前端对接

三端 Vite 已配置 proxy → `http://localhost:8080`，`.env.development` 默认 `VITE_USE_MOCK=false`。

```bash
powershell -ExecutionPolicy Bypass -File scripts/smoke-test.ps1       # 18
powershell -ExecutionPolicy Bypass -File scripts/smoke-test-deep.ps1  # 10
powershell -ExecutionPolicy Bypass -File scripts/smoke-test-phase3.ps1 # 6
powershell -ExecutionPolicy Bypass -File scripts/smoke-test-p1p2.ps1    # 7
powershell -ExecutionPolicy Bypass -File scripts/smoke-test-p3.ps1      # 6
```

## 完成度

- ✅ Phase 1–3：Mock 100+ 路由 REST 对齐
- ✅ 深度操作：排班/药房/财务 22+ 端点
- ✅ 最后完善 P0–P3：配置/统计/财务/安全审计
- ⬜ 生产：关 wx.demo、MySQL init、合法域名
