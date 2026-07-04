const { get } = require('../../utils/request');
const { ensureLogin } = require('../../utils/auth');

Page({
  data: {
    balance: 0, costPerGenerate: 10, remainingGenerations: 0,
    records: [], loading: true, page: 1, hasMore: true
  },

  onLoad() { this.loadData(); },

  async loadData() {
    try {
      await ensureLogin();
      const [balanceRes, recordsRes] = await Promise.all([
        get('/api/points/balance'),
        get('/api/points/records', { page: 1, pageSize: 20 })
      ]);
      this.setData({
        balance: balanceRes.balance,
        costPerGenerate: balanceRes.costPerGenerate,
        remainingGenerations: balanceRes.remainingGenerations,
        records: (recordsRes && recordsRes.list) || [],
        hasMore: recordsRes ? recordsRes.list.length < recordsRes.total : false,
        page: 1, loading: false
      });
    } catch (err) { this.setData({ loading: false }); }
  },

  async loadMore() {
    if (!this.data.hasMore) return;
    const page = this.data.page + 1;
    try {
      const res = await get('/api/points/records', { page, pageSize: 20 });
      const records = this.data.records.concat(res.list || []);
      this.setData({ records, page, hasMore: records.length < res.total });
    } catch (err) { /* ignore */ }
  }
});
