Page({
  data: {
    title: '用户协议'
  },
  onLoad() {
    wx.setNavigationBarTitle({ title: this.data.title });
  }
});
