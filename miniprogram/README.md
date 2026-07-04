# 房子魔法空间 · 新装 — 微信小程序前端

## 技术栈

- 微信小程序原生：WXML + WXSS + JavaScript
- 状态管理：页面 data + app.globalData + storage
- 网络请求：wx.request 二次封装
- 图片上传：wx.chooseMedia + wx.uploadFile

## 本地开发

### 1. 打开项目

1. 下载并安装 [微信开发者工具](https://developers.weixin.qq.com/miniprogram/dev/devtools/download.html)
2. 打开工具 → 导入项目
3. 选择 `miniprogram/` 目录
4. AppID 填写 `wxdb606d67584fb8eb`（或使用测试号）
5. 开发环境勾选「不校验合法域名」

### 2. 配置后端地址

编辑 `app.js` 中的 `apiBaseUrl`：
```js
apiBaseUrl: 'http://localhost:8080'  // 本地开发
// apiBaseUrl: 'https://api.your-domain.com'  // 生产环境
```

### 3. 编译预览

点击「编译」按钮，在模拟器中查看首页。

## 目录结构

```
miniprogram/
├── app.js / .json / .wxss   # 小程序入口
├── project.config.json      # 开发工具配置
├── sitemap.json             # 站点地图
├── utils/                   # 工具库
│   ├── request.js           # 网络请求封装
│   ├── auth.js              # 登录/鉴权
│   ├── upload.js            # 上传封装
│   └── constants.js         # 常量
├── components/              # 公共组件（阶段 2 扩展）
├── pages/
│   ├── index/               # 首页 ✅
│   ├── upload/              # 上传页（阶段 2）
│   ├── design/              # 设计页（阶段 2）
│   ├── generate/            # 进度页（阶段 2）
│   ├── result/              # 结果页（阶段 2）
│   ├── history/             # 历史记录（阶段 2）
│   ├── points/              # 积分明细（阶段 2）
│   ├── case/                # 案例列表（阶段 2）
│   ├── case-detail/         # 案例详情（阶段 2）
│   ├── explore/             # 探索页（阶段 2）
│   ├── about/               # 关于我们（阶段 2）
│   ├── contact/             # 联系我们（阶段 2）
│   ├── agreement/           # 用户协议（阶段 4）
│   └── privacy/             # 隐私政策（阶段 4）
└── images/                  # 图标/图片资源
```

## 上线前检查

- [ ] `app.js` 中 apiBaseUrl 切换为生产 HTTPS 域名
- [ ] 微信公众平台配置 request/uploadFile/downloadFile 合法域名
- [ ] tabBar 图标替换为实际图标文件
- [ ] 用户协议和隐私政策内容完善
- [ ] 小程序类目已选择
