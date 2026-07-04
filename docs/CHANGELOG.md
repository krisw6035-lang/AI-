# 变更日志（CHANGELOG）

## v0.1.0（2026-07-03）

### Added
- 项目骨架搭建：Spring Boot 后端 + 微信小程序前端 + 文档中心
- 后端：pom.xml、启动类、统一返回体（Result/PageResult）、错误码枚举（ErrorCode）、业务异常（BizException）
- 后端：Sa-Token 鉴权配置、WebMvc 配置、文件存储配置、DashScope 配置、MyBatis-Plus 配置
- 后端：4 个核心枚举（TaskStatus/PointsType/SceneType/DesignOptionType）
- 后端：全局异常处理（GlobalExceptionHandler）、健康检查接口（/api/health）
- 后端：application.yml / application-dev.yml / application-prod.yml 三套配置
- 数据库：10 张核心表的 schema.sql
- 数据库：设计选项（9 风格 + 5 肌理 + 5 色号 + 10 软装）、系统配置、页面内容的 seed.sql
- 小程序：app.js/json/wxss、project.config.json、sitemap.json
- 小程序：utils 层（request.js / auth.js / upload.js / constants.js）
- 小程序：14 个页面路由 + 首页完整静态页面 + 13 个占位页面
- 文档：PRD、TECH_DESIGN、API、DB_DESIGN、TEST_PLAN、DEPLOY、ENV_VARIABLES、CHANGELOG 共 8 份

### Changed
- 无（新项目）

### Fixed
- 无（新项目）

---

---

## v0.4.0（2026-07-03）

### Added
- 频率限制：RateLimitInterceptor 实现登录/生成/上传接口的内存滑动窗口限流
- 日志安全：LogSanitizer 工具类，防止 CRLF 注入
- 单元测试：27 个测试用例（PromptBuilder/PointsService/ImageService/SmokeTest）
- 微信审核清单：WECHAT_REVIEW_CHECKLIST.md 完整检查项
- 小程序 tabBar 图标：8 个 PNG 占位图标文件
- 小程序隐私授权：wx.requirePrivacyAuthorize 集成
- 真实微信 code2Session 实现：AuthController 完整对接微信 API

### Changed
- **安全**：CORS allowedOrigins 从通配符改为可配置白名单
- **安全**：DEV_MODE 从硬编码 true 改为 Spring profile 驱动
- **安全**：敏感配置从硬编码改为环境变量引用（dev.yml）
- **安全**：文件上传新增魔数校验，防止伪造扩展名
- **安全**：AI Prompt 新增 customRequirement 清洗，防注入
- **安全**：分页接口新增 max pageSize=50 上限
- **安全**：用户资料更新新增 nickname/avatarUrl 长度和格式校验
- **安全**：DesignController type 参数新增枚举值校验
- **安全**：退款新增幂等检查，防止并发重复退款
- **安全**：生成任务新增防重复提交（60s 窗口）
- **安全**：新增 4 种异常处理器（HttpMessageNotReadable/TypeMismatch/Bind/ConstraintViolation）
- **配置**：生产环境模型从 z-image-turbo 更新为 qwen-image-edit-plus
- **文档**：DEPLOY.md 新增 SSL/备份/监控/回滚章节
- **前端**：app.js 自动环境切换（开发 localhost / 生产 HTTPS）
- **前端**：用户协议和隐私政策页面填充完整内容
- **前端**：5 个占位页面（关于/案例/案例详情/联系/探索）完整实现
- **前端**：app.json 新增 __usePrivacyCheck__ 标记

### Fixed
- 无

---

格式参考：[Keep a Changelog](https://keepachangelog.com/zh-CN/1.0.0/)
