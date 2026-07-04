/**
 * 设计选择页面
 * 选择风格、肌理、墙面色号、软装细节、自定义要求
 */
const { get, post } = require('../../utils/request');
const { ensureLogin } = require('../../utils/auth');
const { downloadImage } = require('../../utils/image');
const { DESIGN_OPTION_TYPES } = require('../../utils/constants');
const app = getApp();

Page({
  data: {
    scene: 'INDOOR',
    sceneName: '室内改装',
    imageId: null,
    imageUrl: '',

    // 设计选项数据
    styles: [],
    textures: [],
    wallColors: [],
    softDecorations: [],

    // 用户选择
    selectedStyleId: null,
    selectedStyleName: '',
    selectedTextureId: null,
    selectedTextureName: '',
    selectedWallColorId: null,
    selectedWallColorCode: '',
    selectedSoftIds: {},    // { id: true/false }
    customRequirement: '',

    // 状态
    loading: true,
    submitting: false,
    customStyle: false      // 是否选了"自定义"风格
  },

  async onLoad(options) {
    const scene = options.scene || 'INDOOR';
    const imageId = options.imageId;
    const remoteUrl = decodeURIComponent(options.imageUrl || '');

    this.setData({
      scene,
      sceneName: scene === 'INDOOR' ? '室内改装' : '室外改装',
      imageId,
      imageUrl: remoteUrl
    });
    wx.setNavigationBarTitle({ title: '选择设计风格' });

    // iOS 无法直接加载 HTTP 图片，先下载到本地
    if (remoteUrl) {
      const localUrl = await downloadImage(remoteUrl);
      this.setData({ imageUrl: localUrl });
    }

    this.loadOptions();
  },

  async loadOptions() {
    try {
      await ensureLogin();
      const [styles, textures, wallColors, softDecorations] = await Promise.all([
        get('/api/design/options', { type: 'STYLE' }),
        get('/api/design/options', { type: 'TEXTURE' }),
        get('/api/design/options', { type: 'WALL_COLOR' }),
        this.data.scene === 'INDOOR'
          ? get('/api/design/options', { type: 'SOFT_DECORATION', scene: this.data.scene })
          : Promise.resolve([])
      ]);

      // 默认选中第一个
      const defaultTexture = textures && textures.length > 0 ? textures[0] : null;

      this.setData({
        styles: styles || [],
        textures: textures || [],
        wallColors: wallColors || [],
        softDecorations: softDecorations || [],
        selectedTextureId: defaultTexture ? defaultTexture.id : null,
        selectedTextureName: defaultTexture ? defaultTexture.optionName : '无',
        loading: false
      });
    } catch (err) {
      console.error('加载设计选项失败:', err);
      this.setData({ loading: false });
    }
  },

  // --- 选择事件 ---
  selectStyle(e) {
    const { id, name } = e.currentTarget.dataset;
    this.setData({
      selectedStyleId: id,
      selectedStyleName: name,
      customStyle: name === '自定义'
    });
  },

  selectTexture(e) {
    const { id, name } = e.currentTarget.dataset;
    this.setData({ selectedTextureId: id, selectedTextureName: name });
  },

  selectWallColor(e) {
    const { id, code } = e.currentTarget.dataset;
    if (this.data.selectedWallColorId === id) {
      this.setData({ selectedWallColorId: null, selectedWallColorCode: '' });
    } else {
      this.setData({ selectedWallColorId: id, selectedWallColorCode: code });
    }
  },

  toggleSoft(e) {
    const { id } = e.currentTarget.dataset;
    const selectedSoftIds = { ...this.data.selectedSoftIds };
    if (selectedSoftIds[id]) {
      delete selectedSoftIds[id];
    } else {
      selectedSoftIds[id] = true;
    }
    this.setData({ selectedSoftIds });
  },

  onInputRequirement(e) {
    this.setData({ customRequirement: e.detail.value });
  },

  // --- 提交生成 ---
  async submitGenerate() {
    if (!this.data.selectedStyleId) {
      wx.showToast({ title: '请选择设计风格', icon: 'none' });
      return;
    }
    if (this.data.customStyle && !this.data.customRequirement.trim()) {
      wx.showToast({ title: '请输入自定义风格要求', icon: 'none' });
      return;
    }

    const softIds = Object.keys(this.data.selectedSoftIds).map(Number);
    const payload = {
      sceneType: this.data.scene,
      imageId: Number(this.data.imageId),
      styleOptionId: this.data.selectedStyleId,
      textureOptionId: this.data.selectedTextureId,
      wallColorId: this.data.selectedWallColorId || null,
      softDecorationIds: softIds.length > 0 ? softIds : [],
      customRequirement: this.data.customRequirement.trim() || null
    };

    this.setData({ submitting: true });
    try {
      const task = await post('/api/generate/tasks', payload);
      wx.navigateTo({
        url: `/pages/generate/generate?taskId=${task.id}&taskNo=${task.taskNo}`
      });
    } catch (err) {
      console.error('创建任务失败:', err);
    } finally {
      this.setData({ submitting: false });
    }
  }
});
