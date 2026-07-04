/**
 * 常量定义
 */

// 场景类型
const SCENE_TYPES = {
  INDOOR: 'INDOOR',
  OUTDOOR: 'OUTDOOR'
};

// 设计选项类型
const DESIGN_OPTION_TYPES = {
  STYLE: 'STYLE',
  TEXTURE: 'TEXTURE',
  WALL_COLOR: 'WALL_COLOR',
  SOFT_DECORATION: 'SOFT_DECORATION'
};

// 任务状态
const TASK_STATUS = {
  WAITING: 'WAITING',
  RUNNING: 'RUNNING',
  SUCCESS: 'SUCCESS',
  FAILED: 'FAILED',
  CANCELED: 'CANCELED',
  TIMEOUT: 'TIMEOUT'
};

// 任务状态中文映射
const TASK_STATUS_MAP = {
  WAITING: '等待中',
  RUNNING: '生成中',
  SUCCESS: '生成成功',
  FAILED: '生成失败',
  CANCELED: '已取消',
  TIMEOUT: '超时'
};

// 积分流水类型
const POINTS_TYPES = {
  REGISTER_GIFT: '注册赠送',
  GENERATE_CONSUME: '生成消耗',
  ADMIN_ADD: '后台增加',
  REFUND: '失败退回'
};

// 生成进度步骤
const PROGRESS_STEPS = [
  { key: 'analysis', label: '正在分析空间结构' },
  { key: 'style',    label: '正在匹配设计风格' },
  { key: 'generate', label: '正在生成装修效果' },
  { key: 'optimize', label: '正在优化画面细节' }
];

// 系统常量
const POINTS_PER_GENERATE = 10;
const MAX_UPLOAD_SIZE_MB = 10;
const POLL_INTERVAL_MS = 2000;
const POLL_MAX_TIMES = 90;    // 最多轮询 90 次（3 分钟），DashScope 生图通常 60-120s

module.exports = {
  SCENE_TYPES,
  DESIGN_OPTION_TYPES,
  TASK_STATUS,
  TASK_STATUS_MAP,
  POINTS_TYPES,
  PROGRESS_STEPS,
  POINTS_PER_GENERATE,
  MAX_UPLOAD_SIZE_MB,
  POLL_INTERVAL_MS,
  POLL_MAX_TIMES
};
