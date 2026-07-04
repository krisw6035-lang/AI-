var request = require('../../utils/request');
var imageUtil = require('../../utils/image');

Page({
  data: { detail: null },
  onLoad(opts) {
    if (opts.id) this.loadDetail(opts.id);
  },
  loadDetail(id) {
    var that = this;
    wx.showLoading({ title: '加载中...' });
    request.get('/api/cases/' + id).then(function(detail) {
      // iOS: 下载案例图片到本地
      var imgUrl = detail.afterImageUrl || detail.beforeImageUrl;
      if (imgUrl) {
        return imageUtil.downloadImage(imgUrl).then(function(localUrl) {
          if (detail.afterImageUrl) detail.afterImageUrl = localUrl;
          if (detail.beforeImageUrl) detail.beforeImageUrl = localUrl;
          return detail;
        });
      }
      return detail;
    }).then(function(detail) {
      that.setData({ detail: detail });
      wx.hideLoading();
    }).catch(function() {
      wx.hideLoading();
    });
  },
  goBack() {
    wx.navigateBack();
  }
});
