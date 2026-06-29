# 东软云医院 HIS - 小程序端

面向 **患者** 的微信小程序，与用户端 Web 功能对齐。

## 三端说明

| 端 | 目录 | 使用者 |
|----|------|--------|
| 管理端 | `hospital-admin` | 管理员、医生、护士等工作人员 |
| 用户端 | `hospital-user` | 患者（Web 浏览器） |
| 小程序端 | `hospital-mini`（本目录） | 患者（微信小程序） |

## 技术栈

- uni-app 3.x
- Vue 3.4
- Vite 5
- Sass

## 功能页面（14 个）

### 底部 Tab（5 个）

| Tab | 页面 | 功能 |
|-----|------|------|
| 首页 | `pages/index` | 快捷入口、科室推荐、公告、数据统计 |
| 挂号 | `pages/register` | 选科室/医生/号别，在线挂号 |
| 预约 | `pages/appointment` | 选日期时段，在线预约 |
| 缴费 | `pages/payment` | 待缴费/已缴费列表，在线支付 |
| 我的 | `pages/mine` | 个人中心、功能菜单 |

### 子页面（9 个）

| 页面 | 功能 |
|------|------|
| `pages/login` | 账号登录 / 微信快捷登录 |
| `pages/department` | 科室列表 |
| `pages/department/detail` | 科室详情、医生列表、快捷挂号 |
| `pages/notice` | 医院公告列表 |
| `pages/notice/detail` | 公告详情 |
| `pages/records` | 就诊记录查询 |
| `pages/mine/my-register` | 我的挂号、退号 |
| `pages/mine/my-appointment` | 我的预约、取消预约 |
| `pages/mine/profile` | 个人信息、就诊卡 |

## 与管理端对应关系

| 小程序（患者端） | Web 管理端 |
|------------------|------------|
| 在线挂号 | 挂号管理 `/business/register` |
| 在线预约 | 预约管理 `/business/appointment` |
| 在线缴费 | 缴费管理 `/business/payment` |
| 就诊记录 | 诊疗记录管理 `/hr/record` |
| 医院公告 | 公告管理 `/business/notice` |
| 科室介绍 | 科室管理 `/admin/department` |

## 快速开始

### 1. 安装依赖

```bash
cd hospital-mini
npm install
```

### 2. 开发调试

**微信小程序：**

```bash
npm run dev:mp-weixin
```

编译产物在 `dist/dev/mp-weixin`，用 [微信开发者工具](https://developers.weixin.qq.com/miniprogram/dev/devtools/download.html) 导入该目录。

**H5 浏览器预览：**

```bash
npm run dev:h5
```

访问 http://localhost:5174

### 3. 生产构建

```bash
npm run build:mp-weixin
```

## 演示账号

- 账号：`13800138000`
- 密码：`123456`

开发环境内置 Mock 数据（`src/utils/mock.js`），无需后端即可体验。对接 Spring Boot 时：

1. 修改 `src/utils/api.js` 中 `USE_MOCK = false`
2. 在微信公众平台配置服务器域名
3. 确保 API 路径与 `hospital-web` 一致

## 目录结构

```
src/
├── api/           # 接口定义
├── components/    # TabBar 等公共组件
├── pages/         # 页面
│   ├── index/     register/  appointment/  payment/  mine/
│   ├── login/     department/  notice/  records/
├── utils/         # 请求、Mock
├── styles/        # 全局样式
├── pages.json     # 页面路由
└── manifest.json  # 小程序配置
```

## 注意事项

1. `manifest.json` 中 `appid` 需替换为你的微信小程序 AppID
2. 正式发布前在微信公众平台配置 request 合法域名
3. 微信快捷登录需后端配合实现 code2Session
