# 环境变量说明：房子魔法空间

> 版本：v0.1.0

## 所有环境变量

### 必需（生产环境）

| 变量名 | 说明 | 示例 |
| --- | --- | --- |
| `DASHSCOPE_API_KEY` | 阿里云 DashScope API Key | `sk-xxxx` |
| `WX_APP_ID` | 微信小程序 AppID | `wxdb606d67584fb8eb` |
| `WX_APP_SECRET` | 微信小程序 AppSecret | `xxxx` |
| `MYSQL_URL` | 数据库连接 URL | `jdbc:mysql://localhost:3306/house_magic_space?...` |
| `MYSQL_USERNAME` | 数据库用户名 | `root` |
| `MYSQL_PASSWORD` | 数据库密码 | `xxxx` |

### 可选

| 变量名 | 说明 | 默认值 |
| --- | --- | --- |
| `SERVER_PORT` | 后端端口 | `8080` |
| `SPRING_PROFILES_ACTIVE` | 环境 | `dev` |
| `FILE_STORAGE_TYPE` | 存储类型 local/oss | `local`（生产建议 oss） |
| `OSS_ENDPOINT` | OSS Endpoint | - |
| `OSS_ACCESS_KEY_ID` | OSS AccessKey | - |
| `OSS_ACCESS_KEY_SECRET` | OSS AccessKey Secret | - |
| `OSS_BUCKET_NAME` | OSS Bucket 名 | - |

## 开发环境设置

```bash
# Windows (PowerShell)
$env:DASHSCOPE_API_KEY="你的Key"
$env:WX_APP_SECRET="你的AppSecret"

# macOS / Linux
export DASHSCOPE_API_KEY=你的Key
export WX_APP_SECRET=你的AppSecret
```

## 安全警告

- **绝对不要**将真实环境变量值写入代码仓库
- **绝对不要**在日志中输出 API Key
- **绝对不要**在截图、演示、README 示例中使用真实值
- application-dev.yml 中的数据库密码仅用于本地开发
- 生产环境密码必须通过环境变量注入
