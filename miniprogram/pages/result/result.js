/**
 * 生成结果页面
 */
const { get, post } = require('../../utils/request');
const app = getApp();

Page({
  data: {
    taskId: null,
    task: null,
    showOriginal: true,
    // 本地缓存的图片路径（解决 iOS 无法加载 HTTP 图片的问题）
    localOriginalUrl: '',
    localResultUrl: ''
  },

  onLoad(options) {
    this.setData({ taskId: options.taskId });
    this.loadTask();
  },

  /**
   * 下载远程图片到本地临时路径
   * iOS WKWebView 无法直接通过 <image> 加载 HTTP 图片，
   * 必须先用 wx.downloadFile 下载到本地再显示。
   */
  downloadImage(url) {
    return new Promise((resolve) => {
      if (!url) { resolve(''); return; }
      wx.downloadFile({
        url: url,
        success: function(res) {
          if (res.statusCode === 200) {
            resolve(res.tempFilePath);
          } else {
            console.warn('[Result] 图片下载失败, statusCode:', res.statusCode);
            resolve(url); // fallback 到原始 URL
          }
        },
        fail: function(err) {
          console.warn('[Result] 图片下载失败:', err);
          resolve(url); // fallback 到原始 URL
        }
      });
    });
  },

  async loadTask() {
    try {
      wx.showLoading({ title: '加载中...' });
      const task = await get(`/api/generate/tasks/${this.data.taskId}`);

      // 构造完整 URL
      var originalUrl = task.originalImageUrl || '';
      var resultUrl = task.resultImageUrl || '';
      if (originalUrl && !originalUrl.startsWith('http')) {
        originalUrl = app.globalData.apiBaseUrl + originalUrl;
      }
      if (resultUrl && !resultUrl.startsWith('http')) {
        resultUrl = app.globalData.apiBaseUrl + resultUrl;
      }

      // 下载到本地临时路径（解决 iOS HTTP 图片加载黑屏问题）
      var localOriginal = originalUrl;
      var localResult = resultUrl;
      try {
        var results = await Promise.all([
          this.downloadImage(originalUrl),
          this.downloadImage(resultUrl)
        ]);
        localOriginal = results[0] || originalUrl;
        localResult = results[1] || resultUrl;
      } catch (e) {
        console.warn('[Result] 批量下载图片异常:', e);
      }

      // 更新 task 对象中的图片 URL 为本地路径
      task.originalImageUrl = localOriginal;
      task.resultImageUrl = localResult;
      task._remoteOriginalUrl = originalUrl;  // 保留远程 URL 用于预览/保存
      task._remoteResultUrl = resultUrl;

      this.setData({ task: task });
      wx.hideLoading();
    } catch (err) {
      wx.hideLoading();
      wx.showToast({ title: '加载失败', icon: 'none' });
    }
  },

  previewImage(e) {
    const type = e.currentTarget.dataset.type;
    // 预览时用远程 URL（wx.previewImage 需要 HTTP URL 或本地路径均可）
    var url = type === 'original'
      ? (this.data.task._remoteOriginalUrl || this.data.task.originalImageUrl)
      : (this.data.task._remoteResultUrl || this.data.task.resultImageUrl);
    wx.previewImage({ urls: [url], current: url });
  },

  toggleView() {
    this.setData({ showOriginal: !this.data.showOriginal });
  },

  saveImage() {
    var task = this.data.task;
    if (!task) return;
    // 用远程 URL 下载保存
    var url = task._remoteResultUrl || task.resultImageUrl;
    if (!url) return;

    wx.showLoading({ title: '保存中...' });
    wx.downloadFile({
      url: url,
      success: (res) => {
        if (res.statusCode === 200) {
          wx.saveImageToPhotosAlbum({
            filePath: res.tempFilePath,
            success: () => { wx.hideLoading(); wx.showToast({ title: '已保存到相册', icon: 'success' }); },
            fail: () => { wx.hideLoading(); wx.showToast({ title: '请授权相册权限', icon: 'none' }); }
          });
        } else {
          wx.hideLoading();
          wx.showToast({ title: '下载失败', icon: 'none' });
        }
      },
      fail: () => { wx.hideLoading(); wx.showToast({ title: '下载失败', icon: 'none' }); }
    });
  },

  async retry() {
    try {
      const task = await post(`/api/generate/tasks/${this.data.taskId}/retry`);
      wx.redirectTo({ url: `/pages/generate/generate?taskId=${task.id}&taskNo=${task.taskNo}` });
    } catch (err) { /* handled by request.js */ }
  },

  goHome() {
    wx.switchTab({ url: '/pages/index/index' });
  }
});
