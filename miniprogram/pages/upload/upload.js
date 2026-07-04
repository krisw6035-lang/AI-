/**
 * 图片上传页面
 */
const { chooseImage, uploadImage } = require('../../utils/upload');
const { ensureLogin, reLogin } = require('../../utils/auth');
const app = getApp();

Page({
  data: {
    scene: 'INDOOR',
    sceneName: '室内改装',
    imagePath: '',
    uploaded: false,
    imageId: null,
    imageUrl: '',
    uploading: false
  },

  onLoad(options) {
    const scene = options.scene || 'INDOOR';
    this.setData({
      scene,
      sceneName: scene === 'INDOOR' ? '室内改装' : '室外改装'
    });
    wx.setNavigationBarTitle({ title: this.data.sceneName + ' - 上传照片' });
  },

  async chooseFromAlbum() {
    try {
      const path = await chooseImage('album');
      this.setData({ imagePath: path, uploaded: false });
    } catch (e) {
      if (e.message !== '用户取消选择') console.error(e);
    }
  },

  async takePhoto() {
    try {
      const path = await chooseImage('camera');
      this.setData({ imagePath: path, uploaded: false });
    } catch (e) {
      if (e.message !== '用户取消选择') console.error(e);
    }
  },

  previewImage() {
    const url = this.data.uploaded ? this.data.imageUrl : this.data.imagePath;
    wx.previewImage({ urls: [url], current: url });
  },

  async doUpload() {
    if (!this.data.imagePath) {
      wx.showToast({ title: '请先选择照片', icon: 'none' });
      return;
    }
    this.setData({ uploading: true });
    try {
      await ensureLogin();
      var result;
      try {
        result = await uploadImage(this.data.imagePath);
      } catch (uploadErr) {
        // 401 → token 无效，重新登录后重试一次
        if (uploadErr && (uploadErr.statusCode === 401 || uploadErr.code === 40100)) {
          console.log('[Upload] token 失效，重新登录后重试');
          await reLogin();
          result = await uploadImage(this.data.imagePath);
        } else {
          throw uploadErr;
        }
      }
      const fullUrl = result.fileUrl.startsWith('http')
        ? result.fileUrl
        : app.globalData.apiBaseUrl + result.fileUrl;
      this.setData({ uploaded: true, imageId: result.id, imageUrl: fullUrl, uploading: false });
      wx.showToast({ title: '上传成功', icon: 'success' });
    } catch (err) {
      this.setData({ uploading: false });
      console.error('[Upload] 上传失败:', err);
    }
  },

  reselect() {
    this.setData({ imagePath: '', uploaded: false, imageId: null, imageUrl: '' });
  },

  goDesign() {
    if (!this.data.uploaded || !this.data.imageId) {
      wx.showToast({ title: '请先上传照片', icon: 'none' });
      return;
    }
    wx.navigateTo({
      url: `/pages/design/design?scene=${this.data.scene}&imageId=${this.data.imageId}&imageUrl=${encodeURIComponent(this.data.imageUrl)}`
    });
  }
});
