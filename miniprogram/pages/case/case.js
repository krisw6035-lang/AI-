var app = getApp();
var request = require('../../utils/request');
var imageUtil = require('../../utils/image');

Page({
  data: {
    cases: [],
    sceneType: '',
    page: 1,
    pageSize: 10,
    hasMore: true,
    loading: false
  },
  onLoad() {
    this.loadCases();
  },
  switchScene(e) {
    var scene = e.currentTarget.dataset.scene;
    this.setData({ sceneType: scene, cases: [], page: 1, hasMore: true });
    this.loadCases();
  },
  loadCases() {
    if (this.data.loading || !this.data.hasMore) return;
    this.setData({ loading: true });
    var that = this;
    request.get('/api/cases', {
      sceneType: this.data.sceneType || undefined,
      page: this.data.page,
      pageSize: this.data.pageSize
    }).then(function(res) {
      var list = (res && res.list) || (res && res.records) || [];
      var total = (res && res.total) || 0;

      // iOS: 下载案例封面图到本地
      Promise.all(list.map(function(item) {
        return imageUtil.downloadImage(item.afterImageUrl || item.beforeImageUrl)
          .then(function(localUrl) {
            if (item.afterImageUrl) item.afterImageUrl = localUrl;
            if (item.beforeImageUrl) item.beforeImageUrl = localUrl;
            return item;
          });
      })).then(function(downloadedList) {
        that.setData({
          cases: that.data.page === 1 ? downloadedList : that.data.cases.concat(downloadedList),
          hasMore: that.data.cases.length + downloadedList.length < total,
          loading: false
        });
      }).catch(function() {
        // 下载失败仍显示原始 URL
        that.setData({
          cases: that.data.page === 1 ? list : that.data.cases.concat(list),
          hasMore: that.data.cases.length + list.length < total,
          loading: false
        });
      });
    }).catch(function() {
      that.setData({ loading: false });
    });
  },
  loadMore() {
    this.setData({ page: this.data.page + 1 });
    this.loadCases();
  },
  goDetail(e) {
    var id = e.currentTarget.dataset.id;
    wx.navigateTo({ url: '/pages/case-detail/case-detail?id=' + id });
  }
});
