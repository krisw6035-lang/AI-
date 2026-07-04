# 接口文档（API）：房子魔法空间

> 版本：v0.1.0 | 状态：初稿，阶段 2 细化

## 约定

- Base URL：`https://your-domain.com`（开发：`http://localhost:8080`）
- 统一返回：`{ "code": 0, "message": "success", "data": {} }`
- 鉴权方式：Header `satoken: {token}`（登录后获取）
- Content-Type：`application/json`（上传接口为 `multipart/form-data`）

## 错误码

| code | 含义 |
| --- | --- |
| 0 | 成功 |
| 40000 | 请求参数错误 |
| 40001 | 积分不足 |
| 40002 | 图片不存在或无权限 |
| 40003 | 生成任务不存在或无权限 |
| 40004 | 设计选项无效 |
| 40100 | 未登录或登录已过期 |
| 40300 | 无权限 |
| 42900 | 请求过于频繁 |
| 50000 | 系统异常 |
| 50001 | AI 服务调用失败 |
| 50002 | 文件上传失败 |
| 50003 | 任务生成超时 |

## 接口列表

### 1. 系统

| 方法 | 路径 | 说明 | 鉴权 |
| --- | --- | --- | --- |
| GET | `/api/health` | 健康检查 | 否 |

### 2. 认证

| 方法 | 路径 | 说明 | 鉴权 |
| --- | --- | --- | --- |
| POST | `/api/auth/wx-login` | 微信登录（body: {code}） | 否 |

**wx-login 请求：**
```json
{ "code": "wx_login_code" }
```

**wx-login 响应：**
```json
{
  "code": 0,
  "data": {
    "token": "sa-token-uuid",
    "userId": 1,
    "nickname": "用户昵称",
    "avatarUrl": "https://...",
    "points": 100
  }
}
```

### 3. 用户

| 方法 | 路径 | 说明 | 鉴权 |
| --- | --- | --- | --- |
| GET | `/api/user/me` | 获取当前用户信息 | 是 |
| PUT | `/api/user/profile` | 更新用户资料 | 是 |

### 4. 上传

| 方法 | 路径 | 说明 | 鉴权 |
| --- | --- | --- | --- |
| POST | `/api/upload/image` | 上传空间图片（multipart） | 是 |
| GET | `/api/upload/image/{id}` | 获取图片详情 | 是 |

### 5. 设计选项

| 方法 | 路径 | 说明 | 鉴权 |
| --- | --- | --- | --- |
| GET | `/api/design/options?type=STYLE` | 获取风格列表 | 否 |
| GET | `/api/design/options?type=TEXTURE` | 获取肌理列表 | 否 |
| GET | `/api/design/options?type=WALL_COLOR` | 获取色号列表 | 否 |
| GET | `/api/design/options?type=SOFT_DECORATION` | 获取软装列表 | 否 |

### 6. 生成任务

| 方法 | 路径 | 说明 | 鉴权 |
| --- | --- | --- | --- |
| POST | `/api/generate/tasks` | 创建生成任务 | 是 |
| GET | `/api/generate/tasks/{taskId}` | 查询任务详情 | 是 |
| GET | `/api/generate/tasks/{taskId}/status` | 查询任务状态（轮询用） | 是 |
| GET | `/api/generate/tasks/history` | 历史记录（分页） | 是 |
| POST | `/api/generate/tasks/{taskId}/retry` | 重新生成 | 是 |

**创建任务请求：**
```json
{
  "sceneType": "INDOOR",
  "imageId": 1,
  "styleOptionId": 2,
  "textureOptionId": 1,
  "wallColorOptionId": 3,
  "softDecorationOptionIds": [1, 6, 7],
  "customRequirement": "希望客厅更明亮一些"
}
```

**任务状态响应：**
```json
{
  "code": 0,
  "data": {
    "taskId": 123,
    "taskNo": "T20260703001",
    "status": "RUNNING",
    "resultImageUrl": null,
    "errorMessage": null
  }
}
```

### 7. 积分

| 方法 | 路径 | 说明 | 鉴权 |
| --- | --- | --- | --- |
| GET | `/api/points/balance` | 积分余额 | 是 |
| GET | `/api/points/records` | 积分流水（分页） | 是 |

### 8. 案例

| 方法 | 路径 | 说明 | 鉴权 |
| --- | --- | --- | --- |
| GET | `/api/cases` | 案例列表（分页，?sceneType=INDOOR） | 否 |
| GET | `/api/cases/{id}` | 案例详情 | 否 |

### 9. 内容页

| 方法 | 路径 | 说明 | 鉴权 |
| --- | --- | --- | --- |
| GET | `/api/page/about` | 关于我们 | 否 |
| GET | `/api/page/contact` | 联系我们 | 否 |
| GET | `/api/page/explore` | 探索内容 | 否 |

### 10. 反馈

| 方法 | 路径 | 说明 | 鉴权 |
| --- | --- | --- | --- |
| POST | `/api/feedback` | 提交反馈 | 否 |
