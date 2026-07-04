/**
 * 微信登录 / 鉴权工具
 */

const { get, post } = require('./request');
const app = getApp();

/**
 * 微信小程序登录
 * 1. 调用 wx.login 获取 code
 * 2. 后端 code2Session 换取 token
 * 3. 保存 token
 */
async function wxLogin() {
  try {
    // 真机上 wx.login 可能超时，加入超时保护（15s）
    const loginRes = await new Promise((resolve, reject) => {
      var done = false;
      var timer = setTimeout(function() {
        if (!done) {
          done = true;
          reject(new Error('wx.login 超时，请检查网络'));
        }
      }, 15000);

      wx.login({
        success: function(res) {
          if (!done) {
            done = true;
            clearTimeout(timer);
            resolve(res);
          }
        },
        fail: function(err) {
          if (!done) {
            done = true;
            clearTimeout(timer);
            reject(err);
          }
        }
      });
    });

    const code = loginRes.code;
    if (!code) {
      throw new Error('获取微信登录 code 失败');
    }

    // 调用后端登录接口
    const data = await post('/api/auth/wx-login', { code });
    // 后端返回 { token, userInfo }
    if (data && data.token) {
      app.setToken(data.token);
      app.globalData.userInfo = data.userInfo;
      console.log('[Auth] 登录成功');
      return data.userInfo;
    }
    throw new Error('登录失败');
  } catch (err) {
    console.error('微信登录失败:', err);
    throw err;
  }
}

/**
 * 验证当前 token 是否有效
 * @returns {Promise<boolean>}
 */
async function verifyToken() {
  try {
    await get('/api/auth/me', null, { silent: true });
    return true;
  } catch (e) {
    return false;
  }
}

/**
 * 检查登录态，未登录则触发登录
 * 真机调试时 Storage 中可能有旧 token，先验证有效性
 */
async function ensureLogin() {
  const token = app.getToken();
  if (token) {
    // 验证 token 有效性（真机可能残留开发工具的旧 token）
    var valid = await verifyToken();
    if (valid) {
      return app.globalData.userInfo;
    }
    // token 无效，清除后重新登录
    console.log('[Auth] 旧 token 已失效，重新登录');
    app.clearToken();
  }
  return wxLogin();
}

/**
 * 强制重新登录（用于 401 后重试）
 */
async function reLogin() {
  app.clearToken();
  return wxLogin();
}

/**
 * 退出登录
 */
function logout() {
  app.clearToken();
  wx.showToast({ title: '已退出登录', icon: 'success' });
}

module.exports = { wxLogin, ensureLogin, verifyToken, reLogin, logout };
