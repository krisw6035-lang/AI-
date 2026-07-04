/**
 * 图片上传封装
 *
 * 说明：使用 wx.chooseImage 旧版 API 选择图片。
 * 待隐私协议审核通过后，可升级为 wx.chooseMedia 获得更好体验。
 */

const app = getApp();

/**
 * 从相册/相机选择图片（旧版 API，兼容性更好）
 * @param {'album'|'camera'} sourceType
 * @returns {Promise<string>} 临时文件路径
 */
function chooseImage(sourceType) {
  return new Promise((resolve, reject) => {
    wx.chooseImage({
      count: 1,
      sizeType: ['compressed'],
      sourceType: [sourceType],
      success(res) {
        const tempFilePath = res.tempFilePaths[0];
        // 前端大小校验（10MB）—— chooseImage 不返回 size，
        // 大小校验在上传阶段由后端二次校验
        resolve(tempFilePath);
      },
      fail(err) {
        if (err.errMsg && err.errMsg.includes('cancel')) {
          reject(new Error('用户取消选择'));
        } else {
          wx.showToast({ title: '选择图片失败', icon: 'none' });
          reject(err);
        }
      }
    });
  });
}

/**
 * 上传图片到后端
 * @param {string} filePath 本地临时文件路径
 * @returns {Promise<object>} 上传结果 { id, fileUrl, ... }
 */
function uploadImage(filePath) {
  return new Promise((resolve, reject) => {
    const token = app.getToken();
    wx.uploadFile({
      url: app.globalData.apiBaseUrl + '/api/upload/image',
      filePath,
      name: 'file',
      header: {
        'satoken': token || ''
      },
      success(res) {
        if (res.statusCode === 200) {
          const data = JSON.parse(res.data);
          if (data.code === 0) {
            resolve(data.data);
          } else {
            wx.showToast({ title: data.message || '上传失败', icon: 'none' });
            reject(data);
          }
        } else {
          wx.showToast({ title: '上传失败', icon: 'none' });
          reject(res);
        }
      },
      fail(err) {
        wx.showToast({ title: '上传失败，请检查网络', icon: 'none' });
        reject(err);
      }
    });
  });
}

module.exports = { chooseImage, uploadImage };
