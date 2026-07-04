# 技术设计文档：房子魔法空间

> 版本：v0.1.0 | 日期：2026-07-03

## 1. 系统架构

```
┌───────────────────┐     HTTPS      ┌───────────────────┐     HTTPS      ┌──────────────┐
│  微信小程序前端    │ ◄────────────► │  Spring Boot 后端  │ ◄────────────► │  DashScope    │
│  WXML/WXSS/JS     │                │  Java 21 / Maven  │               │  z-image-turbo│
│  原生开发          │                │  Sa-Token / MyBatis│               │  图生图 API   │
└───────────────────┘                └────────┬──────────┘               └──────────────┘
                                              │
                                              ▼
                                     ┌───────────────────┐
                                     │  MySQL 8.x        │
                                     │  house_magic_space│
                                     └───────────────────┘
```

## 2. 技术栈

| 层级 | 技术 | 版本 |
| --- | --- | --- |
| 前端 | 微信小程序原生 | - |
| 后端 | Java + Spring Boot | 21 + 3.2.5 |
| ORM | MyBatis-Plus | 3.5.6 |
| 鉴权 | Sa-Token | 1.38.0 |
| 数据库 | MySQL | 8.x |
| AI | DashScope SDK | 2.16.7 |
| 构建 | Maven | 3.x |

## 3. 目录结构

```
house-magic-space/
├── backend/           # Spring Boot 后端
├── miniprogram/       # 微信小程序前端
└── docs/              # 项目文档
```

详见项目根目录。

## 4. 数据模型

### 4.1 核心表（10 张）

1. `user` — 用户表（openid、积分余额）
2. `upload_image` — 上传图片表
3. `generate_task` — 生成任务表（任务编号、状态、AI Prompt）
4. `design_option` — 设计选项表（风格/肌理/色号/软装）
5. `task_soft_decoration` — 任务软装关联表
6. `points_record` — 积分流水表
7. `case_item` — 案例表
8. `page_content` — 页面内容表
9. `user_feedback` — 用户反馈表
10. `system_config` — 系统配置表

### 4.2 关键状态机

```
WAITING → RUNNING → SUCCESS
                  → FAILED
                  → TIMEOUT
        → CANCELED
```

## 5. 接口设计

详见 [API.md](./API.md)。

### 5.1 统一返回结构

```json
{ "code": 0, "message": "success", "data": {} }
```

### 5.2 鉴权策略

- 微信 code2Session 在后端完成
- 登录返回 Sa-Token token
- 前端所有 API 请求自动携带 token
- 公开接口：登录、健康检查、设计选项、案例、页面内容

## 6. AI 图生图集成

### 6.1 调用链路

```
前端 POST /api/generate/tasks
  → GenerateTaskService.createTask()
    → PointsService.deduct()          (事务)
    → DashScopeImageClient.generate() (异步)
    → Update task status + result
```

### 6.2 Prompt 策略

- PromptBuilder：将设计参数拼装成中文 Prompt
- DesignPromptAgent：基于空间类型、风格、肌理、色号、软装、自定义要求生成完整 Prompt
- Negative Prompt：防止改变空间结构、生成无关内容

### 6.3 容错

- API 超时重试
- 生成失败退回积分
- 任务状态轮询 + 超时兜底

## 7. 安全策略

- 密钥全部走环境变量
- 后端鉴权不依赖前端
- SQL 参数化查询
- 文件上传限制格式+大小
- 用户数据隔离（按 user_id 过滤）
- 积分事务一致性
- 防重复提交（前端锁定 + 后端频控）

## 8. 部署架构

```
用户 → 微信小程序 → HTTPS → Nginx → Spring Boot (8080) → MySQL (3306)
                                                  → DashScope API
                                                  → OSS（图片存储）
```
