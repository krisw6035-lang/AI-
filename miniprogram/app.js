/**
 * 房子魔法空间 · 新装 — 小程序入口
 */
App({
  globalData: {
    // 后端 API 基础地址（自动根据环境切换）
    apiBaseUrl: (function() {
      try {
        var accountInfo = wx.getAccountInfoSync();
        // release → 生产环境，trial → 体验版，develop → 开发版
        if (accountInfo.miniProgram && accountInfo.miniProgram.envVersion === 'release') {
          // TODO: 替换为生产环境 HTTPS 域名
          return 'https://api.xinzhuang.cn';
        }
      } catch (e) {
        // wx.getAccountInfoSync 可能在低版本基础库中不可用
      }
      // 开发环境：真机调试时 localhost 指向手机本身，必须用电脑的局域网 IP
      // 获取方式：命令行执行 ipconfig，找到无线局域网适配器的 IPv4 地址
      // 如果 IP 变动，修改下面这行即可
      return 'http://192.168.2.2:8080';
    })(),

    // 用户登录态
    token: null,
    userInfo: null,

    // 系统常量
    pointsPerGenerate: 10,
    maxUploadSizeMB: 10
  },

  onLaunch() {
    // 检查登录态
    var token = wx.getStorageSync('token');
    if (token) {
      this.globalData.token = token;
    }

    // 微信隐私授权检查（基础库 2.32.3+ 必须）
    if (wx.requirePrivacyAuthorize) {
      wx.requirePrivacyAuthorize({
        success: function() {
          console.log('[隐私] 用户已授权隐私政策');
        },
        fail: function() {
          console.warn('[隐私] 用户拒绝或未完成隐私授权');
        }
      });
    }

    console.log('房子魔法空间 · 新装 启动');
  },

  /**
   * 获取当前登录态 token
   */
  getToken() {
    return this.globalData.token || wx.getStorageSync('token');
  },

  /**
   * 设置登录态
   */
  setToken(token) {
    this.globalData.token = token;
    wx.setStorageSync('token', token);
  },

  /**
   * 清除登录态
   */
  clearToken() {
    this.globalData.token = null;
    this.globalData.userInfo = null;
    wx.removeStorageSync('token');
  }
});
