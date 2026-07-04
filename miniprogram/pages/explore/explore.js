var request = require('../../utils/request');

Page({
  data: {
    styles: [],
    textures: [],
    wallColors: []
  },
  onLoad() {
    wx.setNavigationBarTitle({ title: '探索灵感' });
    this.loadDesignOptions();
  },
  loadDesignOptions() {
    var that = this;
    Promise.all([
      request.get('/api/design/options', { type: 'STYLE' }),
      request.get('/api/design/options', { type: 'TEXTURE' }),
      request.get('/api/design/options', { type: 'WALL_COLOR' })
    ]).then(function(results) {
      that.setData({
        styles: results[0].data || [],
        textures: results[1].data || [],
        wallColors: results[2].data || []
      });
    });
  },
  goDesign(e) {
    var style = e.currentTarget.dataset.style;
    wx.navigateTo({ url: '/pages/design/design?style=' + encodeURIComponent(style) });
  },
  startDesign() {
    wx.switchTab({ url: '/pages/index/index' });
  }
});
