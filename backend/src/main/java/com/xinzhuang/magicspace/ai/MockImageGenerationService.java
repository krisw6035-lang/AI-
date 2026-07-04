package com.xinzhuang.magicspace.ai;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Mock 图像生成服务（阶段 2 使用）
 * 模拟 AI 生成过程：提交后立即返回 RUNNING，轮询 3-5 秒后返回 SUCCESS
 * 阶段 3 替换为真实 DashScope 实现
 */
@Service
@Profile("mock")
public class MockImageGenerationService implements ImageGenerationService {

    private static final Logger log = LoggerFactory.getLogger(MockImageGenerationService.class);

    /** Mock 任务存储 */
    private final Map<String, MockTask> taskStore = new ConcurrentHashMap<>();

    @Override
    public String submit(ImageGenRequest request) {
        String taskId = "mock_" + UUID.randomUUID().toString().substring(0, 8);
        long now = System.currentTimeMillis();
        long completeAt = now + 3000 + (long) (Math.random() * 2000); // 3~5 秒

        MockTask task = new MockTask();
        task.taskId = taskId;
        task.status = "RUNNING";
        task.completeAt = completeAt;
        task.resultUrl = "https://via.placeholder.com/800x600/C9A96E/FFFFFF?text=AI+Generated+Effect";

        taskStore.put(taskId, task);
        log.info("Mock 图像生成任务已创建: taskId={}, imageUrl={}", taskId, request.getImageUrl());
        return taskId;
    }

    @Override
    public ImageGenResult query(String remoteTaskId) {
        MockTask task = taskStore.get(remoteTaskId);
        if (task == null) {
            return ImageGenResult.builder()
                    .status("FAILED")
                    .errorMessage("任务不存在: " + remoteTaskId)
                    .build();
        }

        if (System.currentTimeMillis() >= task.completeAt) {
            task.status = "SUCCESS";
        }

        log.debug("Mock 任务状态查询: taskId={}, status={}", remoteTaskId, task.status);

        return ImageGenResult.builder()
                .status(task.status)
                .resultImageUrl("SUCCESS".equals(task.status) ? task.resultUrl : null)
                .errorMessage("FAILED".equals(task.status) ? task.errorMessage : null)
                .build();
    }

    private static class MockTask {
        String taskId;
        String status;
        long completeAt;
        String resultUrl;
        String errorMessage;
    }
}
