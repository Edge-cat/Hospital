# 东软云医院 HIS - 小程序端

面向 **患者** 的微信小程序，与用户端 Web 功能对齐。

## 三端说明

| 端 | 目录 | 使用者 |
|----|------|--------|
| 管理端 | `hospital-admin` | 管理员、医生、护士等工作人员 |
| 用户端 | `hospital-user` | 患者（Web 浏览器） |
| 小程序端 | `hospital-mini`（本目录） | 患者（微信小程序） |

全栈启动（MySQL + IDEA 后端 + Web + 小程序）见根目录 [`README.md`](../README.md) **§IDEA 一键部署 → 3.2 小程序端**。

## 技术栈

- uni-app 3.x
- Vue 3.4
- Vite 5
- Sass

## 环境要求

| 软件 | 用途 |
|------|------|
| Node.js 18+ | 编译 uni-app |
| [微信开发者工具](https://developers.weixin.qq.com/miniprogram/dev/devtools/download.html) | 预览 / 调试小程序 |
| Spring Boot :8080 | 联调真实 API（可选 Mock） |

## 快速开始

### 1. 安装依赖

```bash
cd hospital-mini
npm install
```

### 2. 环境变量

开发配置：`.env.development`

```env
# 联调本地后端（推荐，与 admin/user 同一数据源）
VITE_USE_MOCK=false
VITE_API_BASE=http://127.0.0.1:8080/api
```

| 场景 | `VITE_USE_MOCK` | `VITE_API_BASE` |
|------|-----------------|-----------------|
| 联调 IDEA 后端 | `false` | `http://127.0.0.1:8080/api` |
| 无后端看 UI | `true` | 任意 |
| H5 浏览器 | `false` | `/api`（走 Vite proxy） |

> 微信小程序**不能使用** `/api` 相对路径（无 Vite proxy），联调时必须用 `http://127.0.0.1:8080/api`。  
> 修改 env 后需重启 `npm run dev:mp-weixin`。

### 3. 开发调试（微信小程序）

```bash
npm run dev:mp-weixin
```

1. 编译产物：`dist/dev/mp-weixin`（保持终端运行，支持热更新）
2. 打开 **微信开发者工具** → **导入项目** → 选择上述目录
3. **详情 → 本地设置** → 勾选 **「不校验合法域名…」**（本地 HTTP 联调必需）
4. 确保根目录 [`hospital-backend`](../hospital-backend) 已在 IDEA 以 **`HospitalBackend (prod)`** 启动（:8080）

**H5 浏览器预览（可选）：**

```bash
npm run dev:h5
# http://localhost:5174 ，API 经 vite.config.js proxy 到 :8080
```

### 4. 生产构建

```bash
npm run build:mp-weixin
# 导入 dist/build/mp-weixin；须配置微信公众平台 request 合法域名
```

## 演示账号

| 方式 | 账号 | 密码 |
|------|------|------|
| 手机号登录 | `13800138000` | `123456` |
| 用户名登录 | `patient` | `123456` |
| 微信快捷登录 | — | 需后端 `HOSPITAL_WX_DEMO=true` |

Mock 模式（`VITE_USE_MOCK=true`）数据见 `src/utils/mock.js`，无需后端。

## 功能页面

### 底部 Tab

| Tab | 页面 | 功能 |
|-----|------|------|
| 首页 | `pages/index` | 快捷入口、症状导诊、科室推荐、公告 |
| 挂号 | `pages/visit` | 选科室/医生/时段（当日挂号） |
| 预约 | `pages/visit` | 预约挂号（同页模式切换） |
| 缴费 | `pages/payment` | 待缴 / 已缴 |
| 我的 | `pages/mine` | 个人中心、就诊回顾等 |

### 主要子页面

`login` · `department` · `department/detail` · `notice` · `records` · `mine/my-register` · `mine/my-appointment` · `mine/profile`

## 与管理端对应关系

| 小程序（患者端） | Web 管理端 |
|------------------|------------|
| 在线挂号 | 挂号管理 `/business/register` |
| 在线预约 | 预约管理 `/business/appointment` |
| 在线缴费 | 缴费管理 `/business/payment` |
| 就诊记录 | 诊疗记录 `/hr/record` |
| 医院公告 | 公告管理 `/business/notice` |
| 科室介绍 | 科室管理 `/admin/department` |

## 目录结构

```
src/
├── api/              # 接口定义
├── components/       # TabBar、DoctorSelect 等
├── composables/      # useDepartments 等
├── pages/            # 页面
├── utils/            # request、mock、api
├── pages.json        # 路由
└── manifest.json     # 小程序 AppID、网络超时
```

## 常见问题

| 现象 | 处理 |
|------|------|
| 科室列表为空 / 登录失败 | 检查 `VITE_API_BASE`、后端 :8080、开发者工具「不校验合法域名」 |
| 导入后白屏 | 确认导入的是 `dist/dev/mp-weixin`，不是本目录根路径 |
| 微信登录失败 | 后端需 `HOSPITAL_WX_DEMO=true`；或改用手机号登录 |
| env 改了无效 | 重启 `npm run dev:mp-weixin` 并重新编译 |

缺陷追踪：[`bug/fix-log/`](../bug/fix-log/README.md)

## 注意事项

1. `manifest.json` 中 `appid` 需替换为你的微信小程序 AppID（本地可用测试号）
2. 正式发布前在微信公众平台配置 **request 合法域名**（HTTPS）
3. 生产环境修改 `.env.production` 中的 `VITE_API_BASE` 为真实 API 域名
