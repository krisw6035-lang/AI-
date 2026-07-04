# 房子魔法空间 · 新装 — 后端服务

## 技术栈

- Java 21 + Spring Boot 3.2.5
- MyBatis-Plus 3.5.6
- Sa-Token 1.38.0
- MySQL 8.x
- DashScope SDK 2.16.7 (z-image-turbo)

## 本地运行

### 1. 准备 MySQL

```bash
mysql -u root -p < src/main/resources/db/schema.sql
mysql -u root -p < src/main/resources/db/seed.sql
```

### 2. 配置环境变量

```bash
# Windows PowerShell
$env:DASHSCOPE_API_KEY="你的Key"
$env:WX_APP_SECRET="你的AppSecret"

# macOS / Linux
export DASHSCOPE_API_KEY=你的Key
export WX_APP_SECRET=你的AppSecret
```

### 3. 启动

```bash
mvn spring-boot:run
```

### 4. 验证

```bash
curl http://localhost:8080/api/health
# → {"code":0,"message":"success","data":{"status":"UP",...}}
```

## 目录结构

```
src/main/java/com/xinzhuang/magicspace/
├── MagicSpaceApplication.java   # 启动类
├── common/       # Result / ErrorCode / BizException
├── config/       # SaToken / WebMvc / FileStorage / DashScope / MyBatisPlus
├── controller/   # 控制器（阶段 2 扩展）
├── service/      # 服务接口（阶段 2 扩展）
├── service/impl/ # 服务实现（阶段 2 扩展）
├── mapper/       # MyBatis Mapper（阶段 2 扩展）
├── entity/       # 数据库实体（阶段 2 扩展）
├── dto/          # 请求 DTO（阶段 2 扩展）
├── vo/           # 响应 VO（阶段 2 扩展）
├── enums/        # 枚举（TaskStatus/PointsType/SceneType/DesignOptionType）
├── exception/    # 全局异常处理
├── ai/           # AI 图像生成（阶段 3 实现）
└── utils/        # 工具类（阶段 2 扩展）
```

## 测试

```bash
mvn test
```

## 打包

```bash
mvn clean package -DskipTests
java -jar target/magic-space-0.1.0.jar --spring.profiles.active=prod
```
