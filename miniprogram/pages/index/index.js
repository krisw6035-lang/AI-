/**
 * 首页
 */
const { get } = require('../../utils/request');
const { downloadImage } = require('../../utils/image');
const app = getApp();

Page({
  data: {
    brandName: '新装',
    productName: '房子魔法空间',
    slogan: '上传空间照片，AI 一键生成装修效果图',
    cases: [],
    loading: true
  },

  onLoad() {
    this.loadCases();
  },

  onShow() {
    // 每次回到首页刷新案例
  },

  /**
   * 加载精选案例
   */
  async loadCases() {
    try {
      const data = await get('/api/cases', { page: 1, pageSize: 4 });
      var cases = (data && data.list) || [];

      // iOS 无法直接加载 HTTP 图片，批量下载到本地
      for (var i = 0; i < cases.length; i++) {
        if (cases[i].afterImageUrl) {
          cases[i].afterImageUrl = await downloadImage(cases[i].afterImageUrl);
        }
      }

      this.setData({
        cases: cases,
        loading: false
      });
    } catch (err) {
      console.error('加载案例失败:', err);
      this.setData({ loading: false });
    }
  },

  /**
   * 进入室内改装
   */
  goIndoor() {
    wx.navigateTo({
      url: '/pages/upload/upload?scene=INDOOR'
    });
  },

  /**
   * 进入室外改装
   */
  goOutdoor() {
    wx.navigateTo({
      url: '/pages/upload/upload?scene=OUTDOOR'
    });
  },

  /**
   * 查看案例详情
   */
  goCaseDetail(e) {
    const id = e.currentTarget.dataset.id;
    wx.navigateTo({
      url: `/pages/case-detail/case-detail?id=${id}`
    });
  },

  /**
   * 查看更多案例
   */
  goMoreCases() {
    wx.switchTab({
      url: '/pages/case/case'
    });
  },

  /**
   * 分享
   */
  onShareAppMessage() {
    return {
      title: '房子魔法空间 · 新装 — AI 一键生成装修效果图',
      path: '/pages/index/index'
    };
  }
});
