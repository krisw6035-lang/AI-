/**
 * 图片工具
 *
 * iOS WKWebView 无法通过 <image> 组件直接加载 HTTP 图片（即使开发模式），
 * 需要先用 wx.downloadFile 下载到本地临时路径再显示。
 */

const app = getApp();

/**
 * 补全图片 URL（相对路径 → 完整 HTTP URL）
 */
function fullUrl(path) {
  if (!path) return '';
  if (path.startsWith('http')) return path;
  return app.globalData.apiBaseUrl + path;
}

/**
 * 下载单张图片到本地临时路径
 * @param {string} url — 远程 URL 或相对路径
 * @returns {Promise<string>} 本地临时文件路径（失败时返回原始 URL 作为 fallback）
 */
function downloadImage(url) {
  return new Promise(function(resolve) {
    var remoteUrl = fullUrl(url);
    if (!remoteUrl) { resolve(''); return; }
    wx.downloadFile({
      url: remoteUrl,
      success: function(res) {
        if (res.statusCode === 200) {
          resolve(res.tempFilePath);
        } else {
          console.warn('[Image] 下载失败, status:', res.statusCode, remoteUrl);
          resolve(remoteUrl); // fallback
        }
      },
      fail: function(err) {
        console.warn('[Image] 下载失败:', err, remoteUrl);
        resolve(remoteUrl); // fallback
      }
    });
  });
}

/**
 * 批量下载图片
 * @param {string[]} urls
 * @returns {Promise<string[]>} 本地临时路径数组
 */
function downloadImages(urls) {
  return Promise.all(urls.map(function(u) { return downloadImage(u); }));
}

module.exports = { fullUrl, downloadImage, downloadImages };
