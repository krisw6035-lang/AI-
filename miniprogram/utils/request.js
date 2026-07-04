/**
 * wx.request 二次封装
 * - 自动拼接 baseUrl
 * - 自动注入 token
 * - 统一错误处理
 * - 返回 Promise
 */

const app = getApp();

const request = (options) => {
  return new Promise((resolve, reject) => {
    const url = options.url.startsWith('http')
      ? options.url
      : app.globalData.apiBaseUrl + options.url;

    const header = {
      'Content-Type': 'application/json',
      ...options.header
    };

    // 自动注入 token
    const token = app.getToken();
    if (token) {
      header['satoken'] = token;
    }
    // 如果调用方显式传了 token，优先使用
    if (options.token) {
      header['satoken'] = options.token;
    }

    // 静默模式：不弹 toast（用于校验 token 等场景）
    const silent = options.silent === true;

    wx.request({
      url,
      method: options.method || 'GET',
      data: options.data || {},
      header,
      success(res) {
        if (res.statusCode === 200) {
          const body = res.data;
          if (body.code === 0) {
            resolve(body.data);
          } else if (body.code === 40100) {
            // 未登录，清除 token
            app.clearToken();
            if (!silent) wx.showToast({ title: '请先登录', icon: 'none' });
            reject(body);
          } else {
            // 业务错误
            if (!silent) wx.showToast({ title: body.message || '请求失败', icon: 'none' });
            reject(body);
          }
        } else if (res.statusCode === 401) {
          app.clearToken();
          if (!silent) wx.showToast({ title: '登录已过期，请重新登录', icon: 'none' });
          reject(res);
        } else {
          if (!silent) wx.showToast({ title: '网络异常，请稍后重试', icon: 'none' });
          reject(res);
        }
      },
      fail(err) {
        if (!silent) wx.showToast({ title: '网络连接失败', icon: 'none' });
        reject(err);
      }
    });
  });
};

// 便捷方法 — 第三个参数 opts 支持 { silent: true } 等
const get = (url, data, opts) => request(Object.assign({ url, method: 'GET', data }, opts));
const post = (url, data, opts) => request(Object.assign({ url, method: 'POST', data }, opts));
const put = (url, data, opts) => request(Object.assign({ url, method: 'PUT', data }, opts));
const del = (url, data, opts) => request(Object.assign({ url, method: 'DELETE', data }, opts));

module.exports = { request, get, post, put, del };
