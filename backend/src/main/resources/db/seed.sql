-- ============================================
-- 房子魔法空间 · 新装 — 种子数据
-- ============================================

USE house_magic_space;

-- --------------------------------------------
-- 设计选项：设计风格
-- --------------------------------------------
INSERT INTO `design_option` (`option_type`, `option_key`, `option_name`, `option_desc`, `scene_type`, `sort_order`) VALUES
('STYLE', 'quick_renovation',    '一键翻新',    '快速整体翻新，保持原有空间格局，优化墙面、地面和整体氛围',      NULL, 1),
('STYLE', 'new_chinese',         '新中式',      '融合传统中式元素与现代简约设计，打造雅致东方美学生活空间',      NULL, 2),
('STYLE', 'modern_minimalist',   '现代极简',    '去繁就简，以干净线条和中性色调营造宁静舒适的空间氛围',         NULL, 3),
('STYLE', 'mediterranean',       '地中海风格',  '蓝白配色、拱形元素、自然材质，带来阳光海岸的度假感受',         NULL, 4),
('STYLE', 'modern_european',     '现代欧式',    '简约欧式线条搭配现代材质，融合古典优雅与现代实用',            NULL, 5),
('STYLE', 'north_american',      '北美风',      '宽敞开放的空间布局，自然材质与柔和色彩，打造舒适大气的家居氛围', NULL, 6),
('STYLE', 'japanese_garden',     '日式庭院风',  '自然、静谧、禅意，以木石竹水为元素营造温馨庭院空间',          NULL, 7),
('STYLE', 'color_change',        '一键换色',    '保留现有装修风格，仅更换墙面颜色和部分材质色调',              NULL, 8),
('STYLE', 'custom',              '自定义',      '请输入您期望的改造风格要求',                              NULL, 9);

-- --------------------------------------------
-- 设计选项：艺术肌理
-- --------------------------------------------
INSERT INTO `design_option` (`option_type`, `option_key`, `option_name`, `option_desc`, `scene_type`, `sort_order`) VALUES
('TEXTURE', 'none',        '无',      '不做特殊肌理效果，保持平滑常规墙面',   NULL, 1),
('TEXTURE', 'velvet',      '天鹅绒',  '如天鹅绒般柔和细腻的墙面质感，漫反射光线温润高级', NULL, 2),
('TEXTURE', 'crystalite',  '雅晶石',  '天然矿物颗粒质感，粗犷中带精致，适合艺术涂料效果', NULL, 3),
('TEXTURE', 'limestone',   '莱姆石',  '天然石灰岩纹理，柔和自然的中性色调，营造温润氛围', NULL, 4),
('TEXTURE', 'mottled',     '斑驳感',  '做旧斑驳的复古质感，适合工业风、复古风、侘寂风', NULL, 5);

-- --------------------------------------------
-- 设计选项：墙面色号
-- --------------------------------------------
INSERT INTO `design_option` (`option_type`, `option_key`, `option_name`, `option_desc`, `color_hex`, `scene_type`, `sort_order`) VALUES
('WALL_COLOR', '1391', '色号 1391', '温暖奶油白', '#F5F0E8', NULL, 1),
('WALL_COLOR', '1371', '色号 1371', '高级灰',     '#C8C3BC', NULL, 2),
('WALL_COLOR', '1301', '色号 1301', '纯净白',     '#F8F8F6', NULL, 3),
('WALL_COLOR', '1622', '色号 1622', '暖浅咖',     '#D9C9B8', NULL, 4),
('WALL_COLOR', '0872', '色号 0872', '静谧灰蓝',   '#9BA9B5', NULL, 5);

-- --------------------------------------------
-- 设计选项：软装细节（仅室内场景）
-- --------------------------------------------
INSERT INTO `design_option` (`option_type`, `option_key`, `option_name`, `option_desc`, `scene_type`, `sort_order`) VALUES
('SOFT_DECORATION', 'furniture',     '家具',   '沙发、桌椅、床、柜体等主要家具',           'INDOOR', 1),
('SOFT_DECORATION', 'curtain',       '窗帘',   '窗帘、百叶窗、纱帘等窗饰',                 'INDOOR', 2),
('SOFT_DECORATION', 'coffee_table',  '茶几',   '茶几、边几等桌面家具',                     'INDOOR', 3),
('SOFT_DECORATION', 'tv_stand',      '电视柜', '电视柜、背景墙、影音收纳',                 'INDOOR', 4),
('SOFT_DECORATION', 'floor_lamp',    '落地灯', '落地灯、台灯、壁灯等氛围灯饰',             'INDOOR', 5),
('SOFT_DECORATION', 'sofa',          '沙发',   '沙发、躺椅、休闲椅等坐具',                 'INDOOR', 6),
('SOFT_DECORATION', 'carpet',        '地毯',   '地毯、地垫等地面软装',                     'INDOOR', 7),
('SOFT_DECORATION', 'greenery',      '绿植',   '绿植、花卉、盆栽等自然元素',               'INDOOR', 8),
('SOFT_DECORATION', 'decor_painting','装饰画', '装饰画、挂画、艺术墙饰',                   'INDOOR', 9),
('SOFT_DECORATION', 'storage',       '收纳柜', '收纳柜、书架、展示柜等收纳系统',           'INDOOR', 10);

-- --------------------------------------------
-- 系统配置
-- --------------------------------------------
INSERT INTO `system_config` (`config_key`, `config_value`, `description`) VALUES
('register_gift_points',   '100', '注册赠送积分数量'),
('generate_cost_points',   '10',  '每次生成消耗积分'),
('refund_on_fail',         'true','AI 服务失败是否退回积分'),
('max_upload_size_mb',     '10',  '最大上传图片大小（MB）'),
('task_poll_max_times',    '30',  '任务轮询最大次数'),
('task_poll_interval_sec', '2',   '任务轮询间隔（秒）'),
('task_timeout_sec',       '120', '任务超时时间（秒）'),
('custom_requirement_max_length', '200', '自定义要求最大字符数');

-- --------------------------------------------
-- 页面内容：关于我们
-- --------------------------------------------
INSERT INTO `page_content` (`page_key`, `section_key`, `content`, `sort_order`) VALUES
('about', 'intro',   '「房子魔法空间」是品牌「新装」旗下的 AI 空间改造效果预览平台。我们利用人工智能图像生成技术，让用户只需上传现有空间照片，即可在几秒内看到不同风格的装修改造效果。', 1),
('about', 'feature', '无需掌握专业设计软件，上传照片 → 选择风格 → AI 生成 → 查看效果图，四步即可快速预览改造方案。支持室内改装（客厅、卧室、餐厅等）和室外改装（建筑外立面、庭院、门头等）两大类场景。', 2);

-- --------------------------------------------
-- 页面内容：联系我们
-- --------------------------------------------
INSERT INTO `page_content` (`page_key`, `section_key`, `content`, `sort_order`) VALUES
('contact', 'service',  '如有使用问题、合作意向或建议反馈，欢迎通过以下方式联系我们。', 1),
('contact', 'feedback', '意见反馈：请在小程序内提交反馈，我们会尽快回复。', 2);

-- --------------------------------------------
-- 页面内容：探索
-- --------------------------------------------
INSERT INTO `page_content` (`page_key`, `section_key`, `content`, `sort_order`) VALUES
('explore', 'title', '探索装修灵感', 1);
