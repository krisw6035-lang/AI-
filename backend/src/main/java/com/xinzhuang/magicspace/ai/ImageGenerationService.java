package com.xinzhuang.magicspace.ai;

/**
 * 图像生成服务接口
 * V1 实现：Mock 版本（阶段2），真实 DashScope 版本（阶段3）
 */
public interface ImageGenerationService {

    /**
     * 异步提交图生图任务
     *
     * @param request 生成请求参数
     * @return 远端任务 ID（DashScope task_id 或 mock_id）
     */
    String submit(ImageGenRequest request);

    /**
     * 查询任务状态
     *
     * @param remoteTaskId 远端任务 ID
     * @return 任务状态和结果
     */
    ImageGenResult query(String remoteTaskId);
}
