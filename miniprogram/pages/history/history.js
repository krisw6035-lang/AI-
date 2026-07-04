const { get } = require('../../utils/request');
const { ensureLogin } = require('../../utils/auth');

Page({
  data: {
    tasks: [], loading: true, page: 1, pageSize: 10,
    total: 0, hasMore: true, userInfo: null, points: 0
  },

  onShow() { this.loadData(); },

  async loadData() {
    try {
      await ensureLogin();
      const [historyRes, userRes] = await Promise.all([
        get('/api/generate/tasks/history', { page: 1, pageSize: this.data.pageSize }),
        get('/api/auth/me')
      ]);
      this.setData({
        tasks: (historyRes && historyRes.list) || [],
        total: historyRes ? historyRes.total : 0,
        hasMore: historyRes ? historyRes.list.length < historyRes.total : false,
        page: 1, userInfo: userRes, points: userRes ? userRes.points : 0, loading: false
      });
    } catch (err) { this.setData({ loading: false }); }
  },

  async loadMore() {
    if (!this.data.hasMore) return;
    const page = this.data.page + 1;
    try {
      const res = await get('/api/generate/tasks/history', { page, pageSize: this.data.pageSize });
      const tasks = this.data.tasks.concat(res.list || []);
      this.setData({ tasks, page, hasMore: tasks.length < res.total });
    } catch (err) { /* ignore */ }
  },

  goResult(e) {
    const { id, status } = e.currentTarget.dataset;
    if (status === 'SUCCESS') {
      wx.navigateTo({ url: `/pages/result/result?taskId=${id}` });
    }
  },

  goPoints() {
    wx.navigateTo({ url: '/pages/points/points' });
  }
});
