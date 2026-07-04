/**
 * 生成进度页面
 * 轮询任务状态，显示进度动画
 *
 * 策略：前 30s 快速轮询（2s），30-60s 中速（3s），60s+ 慢速（5s）
 * 最长等待 3 分半，覆盖 DashScope 90% 的生图场景
 */
const { get } = require('../../utils/request');
const { TASK_STATUS, PROGRESS_STEPS } = require('../../utils/constants');

// 分段轮询策略
const FAST_INTERVAL = 2000;   // 前 30 秒：2s
const MID_INTERVAL = 3000;    // 30-90 秒：3s
const SLOW_INTERVAL = 5000;   // 90 秒后：5s
const MAX_DURATION_MS = 210000;  // 最长 3.5 分钟

Page({
  data: {
    taskId: null,
    taskNo: '',
    status: '',
    progressText: '正在准备生成...',
    progressPercent: 0,
    activeStep: 0,
    steps: PROGRESS_STEPS,
    errorMessage: '',
    elapsed: 0,
    networkErrors: 0
  },

  onLoad(options) {
    const taskId = options.taskId;
    const taskNo = options.taskNo || '';
    this.setData({ taskId, taskNo });
    this._startTime = Date.now();
    // 立即发第一次查询，后续用定时器
    this.poll().then(() => this.schedulePoll());
  },

  onUnload() {
    if (this._timer) clearTimeout(this._timer);
  },

  /**
   * 动态调度下一次轮询（避免固定间隔 setInterval）
   */
  schedulePoll() {
    var elapsed = Date.now() - this._startTime;

    // 超时检查
    if (elapsed > MAX_DURATION_MS) {
      this.setData({ status: 'TIMEOUT', errorMessage: 'AI 生成超时，请返回重试' });
      return;
    }

    // 根据已用时间选择轮询间隔
    var nextDelay;
    if (elapsed < 30000) {
      nextDelay = FAST_INTERVAL;
    } else if (elapsed < 90000) {
      nextDelay = MID_INTERVAL;
    } else {
      nextDelay = SLOW_INTERVAL;
    }

    var that = this;
    this._timer = setTimeout(function() {
      that.poll().then(function() {
        that.schedulePoll();
      });
    }, nextDelay);
  },

  async poll() {
    var elapsed = Date.now() - this._startTime;
    var elapsedSec = Math.floor(elapsed / 1000);

    // 更新进度动画（前 90% 是动画，只有完成才到 100%）
    var stepIndex = Math.min(Math.floor(elapsedSec / 25), 3);
    var progressPercent = Math.min(elapsedSec / 2, 90);

    this.setData({
      elapsed: elapsed,
      activeStep: stepIndex,
      progressPercent: Math.round(progressPercent),
      progressText: '已等待 ' + elapsedSec + ' 秒...'
    });

    try {
      var task = await get('/api/generate/tasks/' + this.data.taskId + '/status');
      var status = task.status;

      if (status === TASK_STATUS.SUCCESS) {
        if (this._timer) clearTimeout(this._timer);
        this.setData({ status: status, progressPercent: 100, activeStep: 4 });
        var that = this;
        setTimeout(function() {
          wx.redirectTo({ url: '/pages/result/result?taskId=' + that.data.taskId });
        }, 800);
      } else if (status === TASK_STATUS.FAILED) {
        if (this._timer) clearTimeout(this._timer);
        this.setData({ status: status, errorMessage: task.errorMessage || '生成失败' });
      } else if (status === TASK_STATUS.RUNNING || status === TASK_STATUS.WAITING) {
        this.setData({ status: 'RUNNING', networkErrors: 0 });
      }
    } catch (err) {
      var errors = this.data.networkErrors + 1;
      this.setData({ networkErrors: errors });
      // 连续 10 次网络错误才放弃
      if (errors > 10) {
        if (this._timer) clearTimeout(this._timer);
        this.setData({ status: 'FAILED', errorMessage: '网络异常，请检查连接后重试' });
      }
    }
  },

  /**
   * 重试
   */
  goRetry() {
    wx.navigateBack();
  },

  /**
   * 返回首页
   */
  goHome() {
    wx.switchTab({ url: '/pages/index/index' });
  }
});
