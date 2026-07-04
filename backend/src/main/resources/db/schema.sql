-- ============================================
-- 房子魔法空间 · 新装 — 建表脚本
-- 数据库：house_magic_space
-- 字符集：utf8mb4
-- ============================================

CREATE DATABASE IF NOT EXISTS house_magic_space
  DEFAULT CHARACTER SET utf8mb4
  DEFAULT COLLATE utf8mb4_unicode_ci;

USE house_magic_space;

-- --------------------------------------------
-- 1. 用户表
-- --------------------------------------------
CREATE TABLE IF NOT EXISTS `user` (
    `id`            BIGINT          NOT NULL AUTO_INCREMENT  COMMENT '主键',
    `openid`        VARCHAR(64)     NOT NULL                 COMMENT '微信 OpenID',
    `unionid`       VARCHAR(64)     DEFAULT NULL             COMMENT '微信 UnionID',
    `nickname`      VARCHAR(64)     DEFAULT NULL             COMMENT '用户昵称',
    `avatar_url`    VARCHAR(512)    DEFAULT NULL             COMMENT '头像 URL',
    `points`        INT             NOT NULL DEFAULT 0       COMMENT '积分余额',
    `status`        TINYINT         NOT NULL DEFAULT 1       COMMENT '状态：1=正常, 0=禁用',
    `created_at`    DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at`    DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_openid` (`openid`),
    KEY `idx_created_at` (`created_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户表';

-- --------------------------------------------
-- 2. 上传图片表
-- --------------------------------------------
CREATE TABLE IF NOT EXISTS `upload_image` (
    `id`            BIGINT          NOT NULL AUTO_INCREMENT  COMMENT '主键',
    `user_id`       BIGINT          NOT NULL                 COMMENT '用户 ID',
    `original_name` VARCHAR(255)    DEFAULT NULL             COMMENT '原始文件名',
    `file_path`     VARCHAR(512)    NOT NULL                 COMMENT '文件存储路径',
    `file_url`      VARCHAR(512)    NOT NULL                 COMMENT '文件访问 URL',
    `file_size`     BIGINT          DEFAULT NULL             COMMENT '文件大小（字节）',
    `file_type`     VARCHAR(20)     DEFAULT NULL             COMMENT '文件类型（jpg/png/webp）',
    `width`         INT             DEFAULT NULL             COMMENT '图片宽度',
    `height`        INT             DEFAULT NULL             COMMENT '图片高度',
    `created_at`    DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '上传时间',
    PRIMARY KEY (`id`),
    KEY `idx_user_created` (`user_id`, `created_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='上传图片表';

-- --------------------------------------------
-- 3. 设计选项表
-- --------------------------------------------
CREATE TABLE IF NOT EXISTS `design_option` (
    `id`            BIGINT          NOT NULL AUTO_INCREMENT  COMMENT '主键',
    `option_type`   VARCHAR(32)     NOT NULL                 COMMENT '选项类型：STYLE/TEXTURE/WALL_COLOR/SOFT_DECORATION',
    `option_key`    VARCHAR(64)     NOT NULL                 COMMENT '选项唯一标识',
    `option_name`   VARCHAR(64)     NOT NULL                 COMMENT '选项名称',
    `option_desc`   VARCHAR(256)    DEFAULT NULL             COMMENT '选项描述',
    `color_hex`     VARCHAR(7)      DEFAULT NULL             COMMENT '色值（仅 WALL_COLOR 类型）',
    `icon_url`      VARCHAR(512)    DEFAULT NULL             COMMENT '图标/示例图 URL',
    `scene_type`    VARCHAR(16)     DEFAULT NULL             COMMENT '适用场景：INDOOR/OUTDOOR/null=通用',
    `sort_order`    INT             NOT NULL DEFAULT 0       COMMENT '排序',
    `status`        TINYINT         NOT NULL DEFAULT 1       COMMENT '状态：1=启用, 0=禁用',
    `created_at`    DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at`    DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    KEY `idx_type_sort` (`option_type`, `sort_order`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='设计选项表';

-- --------------------------------------------
-- 4. 生成任务表
-- --------------------------------------------
CREATE TABLE IF NOT EXISTS `generate_task` (
    `id`                BIGINT          NOT NULL AUTO_INCREMENT  COMMENT '主键',
    `task_no`           VARCHAR(32)     NOT NULL                 COMMENT '任务编号（全局唯一）',
    `user_id`           BIGINT          NOT NULL                 COMMENT '用户 ID',
    `scene_type`        VARCHAR(16)     NOT NULL                 COMMENT '场景类型：INDOOR/OUTDOOR',
    `image_id`          BIGINT          NOT NULL                 COMMENT '关联上传图片 ID',
    `style_option_id`   BIGINT          DEFAULT NULL             COMMENT '选中的风格选项 ID',
    `style_name`        VARCHAR(64)     DEFAULT NULL             COMMENT '风格名称（冗余）',
    `texture_option_id` BIGINT          DEFAULT NULL             COMMENT '选中的肌理选项 ID',
    `texture_name`      VARCHAR(64)     DEFAULT NULL             COMMENT '肌理名称（冗余）',
    `wall_color_id`     BIGINT          DEFAULT NULL             COMMENT '选中的墙面色号选项 ID',
    `wall_color_code`   VARCHAR(16)     DEFAULT NULL             COMMENT '墙面色号（冗余）',
    `custom_requirement` VARCHAR(512)   DEFAULT NULL             COMMENT '自定义改造要求',
    `prompt_text`       TEXT            DEFAULT NULL             COMMENT '实际发送给 AI 的完整 Prompt',
    `negative_prompt`   TEXT            DEFAULT NULL             COMMENT 'Negative Prompt',
    `status`            VARCHAR(16)     NOT NULL DEFAULT 'WAITING' COMMENT '任务状态：WAITING/RUNNING/SUCCESS/FAILED/CANCELED/TIMEOUT',
    `result_image_url`  VARCHAR(512)    DEFAULT NULL             COMMENT '生成结果图片 URL',
    `dashscope_task_id` VARCHAR(128)    DEFAULT NULL             COMMENT 'DashScope 返回的任务 ID',
    `error_message`     VARCHAR(512)    DEFAULT NULL             COMMENT '失败原因',
    `points_cost`       INT             NOT NULL DEFAULT 10      COMMENT '消耗积分',
    `created_at`        DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at`        DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `completed_at`      DATETIME        DEFAULT NULL             COMMENT '完成时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_task_no` (`task_no`),
    KEY `idx_user_created` (`user_id`, `created_at`),
    KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='生成任务表';

-- --------------------------------------------
-- 5. 任务软装关联表（室内场景多选软装）
-- --------------------------------------------
CREATE TABLE IF NOT EXISTS `task_soft_decoration` (
    `id`            BIGINT          NOT NULL AUTO_INCREMENT  COMMENT '主键',
    `task_id`       BIGINT          NOT NULL                 COMMENT '生成任务 ID',
    `option_id`     BIGINT          NOT NULL                 COMMENT '软装选项 ID',
    `option_name`   VARCHAR(64)     DEFAULT NULL             COMMENT '软装名称（冗余）',
    `created_at`    DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`id`),
    KEY `idx_task_id` (`task_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='任务软装关联表';

-- --------------------------------------------
-- 6. 积分流水表
-- --------------------------------------------
CREATE TABLE IF NOT EXISTS `points_record` (
    `id`                BIGINT          NOT NULL AUTO_INCREMENT  COMMENT '主键',
    `user_id`           BIGINT          NOT NULL                 COMMENT '用户 ID',
    `change_amount`     INT             NOT NULL                 COMMENT '变动积分（正=增加，负=减少）',
    `balance_before`    INT             NOT NULL                 COMMENT '变动前积分',
    `balance_after`     INT             NOT NULL                 COMMENT '变动后积分',
    `record_type`       VARCHAR(32)     NOT NULL                 COMMENT '流水类型：REGISTER_GIFT/GENERATE_CONSUME/ADMIN_ADD/REFUND',
    `ref_task_id`       BIGINT          DEFAULT NULL             COMMENT '关联生成任务 ID',
    `remark`            VARCHAR(256)    DEFAULT NULL             COMMENT '备注',
    `created_at`        DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`id`),
    KEY `idx_user_created` (`user_id`, `created_at`),
    KEY `idx_ref_task` (`ref_task_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='积分流水表';

-- --------------------------------------------
-- 7. 案例表
-- --------------------------------------------
CREATE TABLE IF NOT EXISTS `case_item` (
    `id`                BIGINT          NOT NULL AUTO_INCREMENT  COMMENT '主键',
    `title`             VARCHAR(128)    NOT NULL                 COMMENT '案例标题',
    `description`       VARCHAR(512)    DEFAULT NULL             COMMENT '案例描述',
    `scene_type`        VARCHAR(16)     NOT NULL                 COMMENT '场景类型：INDOOR/OUTDOOR',
    `style_name`        VARCHAR(64)     DEFAULT NULL             COMMENT '风格名称',
    `before_image_url`  VARCHAR(512)    NOT NULL                 COMMENT '改造前图片 URL',
    `after_image_url`   VARCHAR(512)    NOT NULL                 COMMENT '改造后图片 URL',
    `design_params`     JSON            DEFAULT NULL             COMMENT '设计参数（风格/肌理/色号等）',
    `sort_order`        INT             NOT NULL DEFAULT 0       COMMENT '排序',
    `status`            TINYINT         NOT NULL DEFAULT 1       COMMENT '状态：1=展示, 0=隐藏',
    `created_at`        DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at`        DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    KEY `idx_scene_sort` (`scene_type`, `sort_order`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='案例表';

-- --------------------------------------------
-- 8. 页面内容表
-- --------------------------------------------
CREATE TABLE IF NOT EXISTS `page_content` (
    `id`            BIGINT          NOT NULL AUTO_INCREMENT  COMMENT '主键',
    `page_key`      VARCHAR(64)     NOT NULL                 COMMENT '页面唯一标识：about/contact/explore',
    `section_key`   VARCHAR(64)     NOT NULL                 COMMENT '段落标识',
    `content`       TEXT            DEFAULT NULL             COMMENT '内容（支持 HTML 或纯文本）',
    `sort_order`    INT             NOT NULL DEFAULT 0       COMMENT '排序',
    `created_at`    DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at`    DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    KEY `idx_page_section` (`page_key`, `section_key`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='页面内容表';

-- --------------------------------------------
-- 9. 用户反馈表
-- --------------------------------------------
CREATE TABLE IF NOT EXISTS `user_feedback` (
    `id`            BIGINT          NOT NULL AUTO_INCREMENT  COMMENT '主键',
    `user_id`       BIGINT          DEFAULT NULL             COMMENT '用户 ID（可为空，允许未登录反馈）',
    `feedback_type` VARCHAR(32)     NOT NULL                 COMMENT '反馈类型：BUG/SUGGESTION/OTHER',
    `content`       TEXT            NOT NULL                 COMMENT '反馈内容',
    `contact`       VARCHAR(128)    DEFAULT NULL             COMMENT '联系方式',
    `status`        TINYINT         NOT NULL DEFAULT 0       COMMENT '处理状态：0=未处理, 1=已处理',
    `created_at`    DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`id`),
    KEY `idx_user_created` (`user_id`, `created_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户反馈表';

-- --------------------------------------------
-- 10. 系统配置表
-- --------------------------------------------
CREATE TABLE IF NOT EXISTS `system_config` (
    `id`            BIGINT          NOT NULL AUTO_INCREMENT  COMMENT '主键',
    `config_key`    VARCHAR(64)     NOT NULL                 COMMENT '配置键',
    `config_value`  VARCHAR(512)    NOT NULL                 COMMENT '配置值',
    `description`   VARCHAR(256)    DEFAULT NULL             COMMENT '说明',
    `created_at`    DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at`    DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_config_key` (`config_key`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='系统配置表';
