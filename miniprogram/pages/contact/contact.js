var request = require('../../utils/request');

Page({
  data: { content: '' },
  onLoad() {
    wx.setNavigationBarTitle({ title: '联系我们' });
  },
  onInput(e) {
    this.setData({ content: e.detail.value });
  },
  submitFeedback() {
    var content = this.data.content.trim();
    if (!content) {
      wx.showToast({ title: '请输入反馈内容', icon: 'none' });
      return;
    }
    var that = this;
    request.post('/api/feedback', { content: content }).then(function() {
      wx.showToast({ title: '感谢您的反馈！', icon: 'success' });
      that.setData({ content: '' });
    });
  },
  goAgreement() {
    wx.navigateTo({ url: '/pages/agreement/agreement' });
  },
  goPrivacy() {
    wx.navigateTo({ url: '/pages/privacy/privacy' });
  },
  goAbout() {
    wx.navigateTo({ url: '/pages/about/about' });
  }
});
