# 部署文档：房子魔法空间

> 版本：v0.4.0 | 状态：正式版

## 1. 环境要求

| 组件 | 版本 | 说明 |
| --- | --- | --- |
| JDK | 21 | 后端运行环境 |
| Maven | 3.x | 后端构建 |
| MySQL | 8.x | 数据库 |
| Nginx | 1.x | 反向代理 + HTTPS |
| 微信开发者工具 | 最新稳定版 | 小程序开发/预览/上传 |

## 2. 本地开发

### 2.1 数据库初始化

```bash
mysql -u root -p < backend/src/main/resources/db/schema.sql
mysql -u root -p < backend/src/main/resources/db/seed.sql
```

### 2.2 后端启动

```bash
cd backend
# 设置环境变量
export DASHSCOPE_API_KEY=你的Key
export WX_APP_SECRET=你的AppSecret

# 启动
mvn spring-boot:run
```

### 2.3 小程序启动

1. 打开微信开发者工具
2. 导入项目 → 选择 `miniprogram/` 目录
3. AppID 填写 `wxdb606d67584fb8eb`
4. 开发环境勾选「不校验合法域名」

## 3. 生产部署

### 3.1 环境变量清单

详见 [ENV_VARIABLES.md](./ENV_VARIABLES.md)

### 3.2 后端打包

```bash
cd backend
mvn clean package -DskipTests
java -jar target/magic-space-0.1.0.jar --spring.profiles.active=prod
```

### 3.3 Nginx 配置示例

```nginx
server {
    listen 443 ssl;
    server_name api.your-domain.com;

    ssl_certificate     /etc/nginx/certs/fullchain.pem;
    ssl_certificate_key /etc/nginx/certs/privkey.pem;

    client_max_body_size 12m;

    location / {
        proxy_pass http://127.0.0.1:8080;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto $scheme;
    }

    location /upload/ {
        alias /path/to/upload/;
    }
}
```

### 3.4 微信小程序配置

1. 登录微信公众平台 → 开发管理 → 服务器域名
2. 配置 request 合法域名：`https://api.your-domain.com`
3. 配置 uploadFile 合法域名：`https://api.your-domain.com`
4. 配置 downloadFile 合法域名：`https://api.your-domain.com` + OSS 域名

## 4. OSS 配置（推荐）

- 阿里云 OSS 创建 Bucket
- 配置公共读或签名 URL 访问
- 配置 CNAME + HTTPS
- 后端 FileStorageConfig 切换为 oss 模式

## 5. SSL/HTTPS 配置

### 5.1 证书获取
- 推荐使用 Let's Encrypt 免费证书（certbot 自动化续期）
- 或购买商业 SSL 证书（阿里云/腾讯云）
- 证书文件：`fullchain.pem` + `privkey.pem`

### 5.2 Nginx HTTPS 强制跳转
```nginx
server {
    listen 80;
    server_name api.your-domain.com;
    return 301 https://$server_name$request_uri;
}
```

## 6. 备份策略

### 6.1 数据库备份
```bash
# 每日全量备份（crontab）
0 2 * * * mysqldump -u root -p'密码' house_magic_space | gzip > /backup/db_$(date +\%Y\%m\%d).sql.gz

# 保留最近 30 天的备份
find /backup/ -name "db_*.sql.gz" -mtime +30 -delete
```

### 6.2 文件备份
- 上传目录 `upload/` 建议使用 OSS 自动多副本
- 如使用本地存储，定期 rsync 到备份服务器

## 7. 监控与告警

### 7.1 健康检查
```bash
curl https://api.your-domain.com/api/health
# 预期返回：{"code":0,"data":{"status":"UP"}}
```

### 7.2 建议监控项
- 服务存活：定期请求 /api/health
- 磁盘使用率：upload 目录空间
- 数据库连接数：MySQL 连接池状态
- AI 服务可用性：DashScope 任务成功率
- JVM 内存/GC：通过 Spring Actuator 或 JMX

### 7.3 日志管理
- 生产日志级别：WARN（root），INFO（com.xinzhuang.magicspace）
- 建议使用 logrotate 或集中式日志平台（ELK/Loki）

## 8. 回滚方案

1. 保留最近 3 个版本的 jar 包
2. 出现问题时可快速切换：
```bash
java -jar target/magic-space-0.1.0.jar --spring.profiles.active=prod
```
3. 数据库回滚：从最近备份恢复

## 9. 上线前检查清单

参见 [WECHAT_REVIEW_CHECKLIST.md](./WECHAT_REVIEW_CHECKLIST.md)
