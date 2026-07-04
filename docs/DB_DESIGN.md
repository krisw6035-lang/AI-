# 数据库设计文档：房子魔法空间

> 版本：v0.1.0 | 数据库：house_magic_space | 字符集：utf8mb4

## 1. ER 概要

```
user ──1:N── upload_image
user ──1:N── generate_task ──1:N── task_soft_decoration ──N:1── design_option
user ──1:N── points_record
design_option (独立，分类管理)
case_item (独立，管理员维护)
page_content (独立，管理员维护)
user_feedback (独立，关联 user 可选)
system_config (独立，KV 结构)
```

## 2. 表结构

### 2.1 user（用户表）

| 字段 | 类型 | 说明 |
| --- | --- | --- |
| id | BIGINT PK | 主键自增 |
| openid | VARCHAR(64) UNIQUE | 微信 OpenID |
| unionid | VARCHAR(64) | 微信 UnionID |
| nickname | VARCHAR(64) | 昵称 |
| avatar_url | VARCHAR(512) | 头像 URL |
| points | INT DEFAULT 0 | 积分余额 |
| status | TINYINT DEFAULT 1 | 状态 1=正常 0=禁用 |
| created_at | DATETIME | 创建时间 |
| updated_at | DATETIME | 更新时间 |

### 2.2 upload_image（上传图片表）

| 字段 | 类型 | 说明 |
| --- | --- | --- |
| id | BIGINT PK | 主键 |
| user_id | BIGINT FK | 用户 ID |
| original_name | VARCHAR(255) | 原始文件名 |
| file_path | VARCHAR(512) | 存储路径 |
| file_url | VARCHAR(512) | 访问 URL |
| file_size | BIGINT | 字节数 |
| file_type | VARCHAR(20) | jpg/png/webp |
| width | INT | 宽 |
| height | INT | 高 |
| created_at | DATETIME | 上传时间 |

### 2.3 generate_task（生成任务表）

| 字段 | 类型 | 说明 |
| --- | --- | --- |
| id | BIGINT PK | 主键 |
| task_no | VARCHAR(32) UNIQUE | 任务编号 |
| user_id | BIGINT FK | 用户 ID |
| scene_type | VARCHAR(16) | INDOOR/OUTDOOR |
| image_id | BIGINT FK | 原图 ID |
| style_option_id | BIGINT | 风格选项 ID |
| texture_option_id | BIGINT | 肌理选项 ID |
| wall_color_id | BIGINT | 色号选项 ID |
| custom_requirement | VARCHAR(512) | 自定义要求 |
| prompt_text | TEXT | 完整 Prompt |
| negative_prompt | TEXT | Negative Prompt |
| status | VARCHAR(16) | WAITING/RUNNING/SUCCESS/FAILED/CANCELED/TIMEOUT |
| result_image_url | VARCHAR(512) | 结果图 URL |
| dashscope_task_id | VARCHAR(128) | DashScope 任务 ID |
| error_message | VARCHAR(512) | 失败原因 |
| points_cost | INT DEFAULT 10 | 消耗积分 |
| created_at | DATETIME | 创建时间 |
| updated_at | DATETIME | 更新时间 |
| completed_at | DATETIME | 完成时间 |

### 2.4 design_option（设计选项表）

| 字段 | 类型 | 说明 |
| --- | --- | --- |
| id | BIGINT PK | 主键 |
| option_type | VARCHAR(32) | STYLE/TEXTURE/WALL_COLOR/SOFT_DECORATION |
| option_key | VARCHAR(64) | 唯一标识 |
| option_name | VARCHAR(64) | 名称 |
| option_desc | VARCHAR(256) | 描述 |
| color_hex | VARCHAR(7) | 色值（仅 WALL_COLOR） |
| icon_url | VARCHAR(512) | 图标 URL |
| scene_type | VARCHAR(16) | INDOOR/OUTDOOR/null=通用 |
| sort_order | INT | 排序 |
| status | TINYINT | 1=启用 |

### 2.5-2.10 其他表

- `task_soft_decoration`：任务-ID + 软装选项-ID 关联
- `points_record`：积分变动记录（change_amount, balance_before/after, record_type, ref_task_id）
- `case_item`：案例（before/after 图片, scene_type, style_name）
- `page_content`：页面内容（page_key, section_key, content）
- `user_feedback`：反馈（feedback_type, content, contact）
- `system_config`：系统配置（config_key, config_value）

## 3. 索引策略

| 表 | 索引 | 类型 |
| --- | --- | --- |
| user | uk_openid | UNIQUE |
| generate_task | uk_task_no | UNIQUE |
| generate_task | idx_user_created (user_id, created_at) | NORMAL |
| generate_task | idx_status | NORMAL |
| points_record | idx_user_created (user_id, created_at) | NORMAL |
| upload_image | idx_user_created (user_id, created_at) | NORMAL |
| design_option | idx_type_sort (option_type, sort_order) | NORMAL |
| case_item | idx_scene_sort (scene_type, sort_order) | NORMAL |

## 4. 种子数据

详见 `backend/src/main/resources/db/seed.sql`：
- 9 种设计风格
- 5 种艺术肌理
- 5 种墙面色号（含色值）
- 10 种软装细节
- 系统配置项
- 关于我们/联系我们/探索初始内容
