package com.xinzhuang.magicspace.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.xinzhuang.magicspace.ai.ImageGenRequest;
import com.xinzhuang.magicspace.ai.ImageGenResult;
import com.xinzhuang.magicspace.ai.ImageGenerationService;
import com.xinzhuang.magicspace.ai.PromptBuilder;
import com.xinzhuang.magicspace.common.BizException;
import com.xinzhuang.magicspace.common.ErrorCode;
import com.xinzhuang.magicspace.common.PageResult;
import com.xinzhuang.magicspace.dto.CreateTaskDTO;
import com.xinzhuang.magicspace.entity.*;
import com.xinzhuang.magicspace.enums.TaskStatus;
import com.xinzhuang.magicspace.mapper.*;
import com.xinzhuang.magicspace.utils.TaskNoGenerator;
import com.xinzhuang.magicspace.vo.TaskVO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 生成任务服务
 */
@Service
public class GenerateTaskService {

    private static final Logger log = LoggerFactory.getLogger(GenerateTaskService.class);

    private final GenerateTaskMapper taskMapper;
    private final TaskSoftDecorationMapper softDecorationMapper;
    private final DesignOptionMapper designOptionMapper;
    private final UploadImageMapper uploadImageMapper;
    private final PointsService pointsService;
    private final PromptBuilder promptBuilder;
    private final ImageGenerationService imageGenerationService;

    public GenerateTaskService(GenerateTaskMapper taskMapper,
                               TaskSoftDecorationMapper softDecorationMapper,
                               DesignOptionMapper designOptionMapper,
                               UploadImageMapper uploadImageMapper,
                               PointsService pointsService,
                               PromptBuilder promptBuilder,
                               ImageGenerationService imageGenerationService) {
        this.taskMapper = taskMapper;
        this.softDecorationMapper = softDecorationMapper;
        this.designOptionMapper = designOptionMapper;
        this.uploadImageMapper = uploadImageMapper;
        this.pointsService = pointsService;
        this.promptBuilder = promptBuilder;
        this.imageGenerationService = imageGenerationService;
    }

    @Value("${dashscope.model:z-image-turbo}")
    private String aiModel;

    /**
     * 创建生成任务（事务：扣积分 + 建任务 + 提交 AI）
     */
    @Transactional
    public TaskVO createTask(Long userId, CreateTaskDTO dto) {
        // 0. 防重复提交：检查同一用户+图片在60秒内是否有进行中的任务
        LocalDateTime recentThreshold = LocalDateTime.now().minusSeconds(60);
        Long duplicateCount = taskMapper.selectCount(new LambdaQueryWrapper<GenerateTask>()
                .eq(GenerateTask::getUserId, userId)
                .eq(GenerateTask::getImageId, dto.getImageId())
                .in(GenerateTask::getStatus, TaskStatus.WAITING.name(), TaskStatus.RUNNING.name())
                .gt(GenerateTask::getCreatedAt, recentThreshold));
        if (duplicateCount != null && duplicateCount > 0) {
            throw new BizException(42900, "已有进行中的生成任务，请等待完成后再试");
        }

        // 1. 校验图片
        UploadImage image = uploadImageMapper.selectById(dto.getImageId());
        if (image == null || !image.getUserId().equals(userId)) {
            throw new BizException(ErrorCode.IMAGE_NOT_FOUND);
        }

        // 2. 获取设计选项信息
        DesignOption style = designOptionMapper.selectById(dto.getStyleOptionId());
        if (style == null || !"STYLE".equals(style.getOptionType())) {
            throw new BizException(ErrorCode.DESIGN_OPTION_INVALID);
        }

        DesignOption texture = null;
        if (dto.getTextureOptionId() != null) {
            texture = designOptionMapper.selectById(dto.getTextureOptionId());
        }

        DesignOption wallColor = null;
        if (dto.getWallColorId() != null) {
            wallColor = designOptionMapper.selectById(dto.getWallColorId());
        }

        // 3. 构建 Prompt
        PromptBuilder.PromptParams params = new PromptBuilder.PromptParams();
        params.sceneType = dto.getSceneType();
        params.styleName = style.getOptionName();
        params.textureName = texture != null ? texture.getOptionName() : "无";
        params.wallColorCode = wallColor != null ? wallColor.getOptionKey() : null;
        params.wallColorHex = wallColor != null ? wallColor.getColorHex() : null;
        params.customRequirement = dto.getCustomRequirement();

        // 4. 创建任务（先扣积分）
        GenerateTask task = new GenerateTask();
        task.setTaskNo(TaskNoGenerator.generate());
        task.setUserId(userId);
        task.setSceneType(dto.getSceneType());
        task.setImageId(dto.getImageId());
        task.setStyleOptionId(style.getId());
        task.setStyleName(style.getOptionName());

        if (texture != null) {
            task.setTextureOptionId(texture.getId());
            task.setTextureName(texture.getOptionName());
        }
        if (wallColor != null) {
            task.setWallColorId(wallColor.getId());
            task.setWallColorCode(wallColor.getOptionKey());
        }

        task.setCustomRequirement(dto.getCustomRequirement());
        task.setStatus(TaskStatus.WAITING.name());
        task.setPointsCost(10);

        // 软装选项
        List<String> softNames = Collections.emptyList();
        if (dto.getSoftDecorationIds() != null && !dto.getSoftDecorationIds().isEmpty()) {
            List<DesignOption> softOptions = designOptionMapper.selectBatchIds(dto.getSoftDecorationIds());
            softNames = softOptions.stream().map(DesignOption::getOptionName).collect(Collectors.toList());
            params.softDecorationNames = softNames;
        }

        String prompt = promptBuilder.buildPrompt(params);
        String negativePrompt = promptBuilder.buildNegativePrompt();
        task.setPromptText(prompt);
        task.setNegativePrompt(negativePrompt);

        taskMapper.insert(task);

        // 保存软装关联
        if (dto.getSoftDecorationIds() != null && !dto.getSoftDecorationIds().isEmpty()) {
            for (Long optId : dto.getSoftDecorationIds()) {
                DesignOption opt = designOptionMapper.selectById(optId);
                if (opt != null) {
                    TaskSoftDecoration tsd = new TaskSoftDecoration();
                    tsd.setTaskId(task.getId());
                    tsd.setOptionId(optId);
                    tsd.setOptionName(opt.getOptionName());
                    softDecorationMapper.insert(tsd);
                }
            }
        }

        // 5. 扣减积分
        pointsService.deductForGenerate(userId, task.getId());

        // 6. 提交 AI 生成任务（异步）
        try {
            task.setStatus(TaskStatus.RUNNING.name());
            taskMapper.updateById(task);

            ImageGenRequest aiRequest = ImageGenRequest.builder()
                    .imageUrl(image.getFileUrl())
                    .prompt(prompt)
                    .negativePrompt(negativePrompt)
                    .model(aiModel)
                    .build();

            String remoteTaskId = imageGenerationService.submit(aiRequest);
            task.setDashscopeTaskId(remoteTaskId);
            taskMapper.updateById(task);

            log.info("生成任务已提交: taskNo={}, userId={}, style={}, remoteTaskId={}",
                    task.getTaskNo(), userId, style.getOptionName(), remoteTaskId);
        } catch (Exception e) {
            // AI 提交失败：退回积分
            log.error("AI 提交失败: taskNo={}", task.getTaskNo(), e);
            task.setStatus(TaskStatus.FAILED.name());
            task.setErrorMessage("AI 服务提交失败: " + e.getMessage());
            taskMapper.updateById(task);
            pointsService.refundForFail(userId, task.getId());
        }

        return toTaskVO(task, image, softNames);
    }

    /**
     * 查询任务状态（轮询用）
     * 返回最新状态，如果 AI 任务完成则更新数据库
     */
    @Transactional
    public TaskVO queryStatus(Long taskId, Long userId) {
        GenerateTask task = taskMapper.selectById(taskId);
        if (task == null || !task.getUserId().equals(userId)) {
            throw new BizException(ErrorCode.TASK_NOT_FOUND);
        }

        // 如果任务正在运行，查询 AI 状态
        if (TaskStatus.RUNNING.name().equals(task.getStatus())
                && task.getDashscopeTaskId() != null) {

            ImageGenResult result = imageGenerationService.query(task.getDashscopeTaskId());

            if ("SUCCESS".equals(result.getStatus())) {
                task.setStatus(TaskStatus.SUCCESS.name());
                task.setResultImageUrl(result.getResultImageUrl());
                task.setCompletedAt(LocalDateTime.now());
                taskMapper.updateById(task);
                log.info("生成任务成功: taskNo={}, resultUrl={}", task.getTaskNo(), result.getResultImageUrl());
            } else if ("FAILED".equals(result.getStatus())) {
                // 防止并发重复退款：只在任务仍为 RUNNING 状态时才处理失败
                if (!TaskStatus.RUNNING.name().equals(task.getStatus())) {
                    log.warn("任务状态已变更，跳过重复失败处理: taskNo={}, currentStatus={}",
                            task.getTaskNo(), task.getStatus());
                    // 已被其他线程处理，直接返回当前状态
                    UploadImage image = uploadImageMapper.selectById(task.getImageId());
                    List<String> softNames = getSoftDecorationNames(task.getId());
                    return toTaskVO(task, image, softNames);
                }
                task.setStatus(TaskStatus.FAILED.name());
                task.setErrorMessage(result.getErrorMessage());
                task.setCompletedAt(LocalDateTime.now());
                taskMapper.updateById(task);
                // AI 服务失败，退回积分
                pointsService.refundForFail(userId, task.getId());
                log.warn("生成任务失败: taskNo={}, error={}", task.getTaskNo(), result.getErrorMessage());
            }
        }

        UploadImage image = uploadImageMapper.selectById(task.getImageId());
        List<String> softNames = getSoftDecorationNames(task.getId());

        return toTaskVO(task, image, softNames);
    }

    /**
     * 获取任务详情
     */
    public TaskVO getDetail(Long taskId, Long userId) {
        return queryStatus(taskId, userId);
    }

    /**
     * 用户历史生成记录（分页）
     */
    public PageResult<TaskVO> getHistory(Long userId, int page, int pageSize) {
        int cappedPageSize = Math.min(pageSize, 50);
        LambdaQueryWrapper<GenerateTask> query = new LambdaQueryWrapper<GenerateTask>()
                .eq(GenerateTask::getUserId, userId)
                .orderByDesc(GenerateTask::getCreatedAt);

        Page<GenerateTask> mpPage = new Page<>(page, cappedPageSize);
        Page<GenerateTask> result = taskMapper.selectPage(mpPage, query);

        List<TaskVO> list = result.getRecords().stream().map(task -> {
            UploadImage image = uploadImageMapper.selectById(task.getImageId());
            List<String> softNames = getSoftDecorationNames(task.getId());
            return toTaskVO(task, image, softNames);
        }).collect(Collectors.toList());

        return new PageResult<>(result.getTotal(), page, cappedPageSize, list);
    }

    /**
     * 重新生成（复用原有设计参数，重新创建任务）
     */
    @Transactional
    public TaskVO retry(Long taskId, Long userId) {
        GenerateTask original = taskMapper.selectById(taskId);
        if (original == null || !original.getUserId().equals(userId)) {
            throw new BizException(ErrorCode.TASK_NOT_FOUND);
        }

        CreateTaskDTO dto = new CreateTaskDTO();
        dto.setSceneType(original.getSceneType());
        dto.setImageId(original.getImageId());
        dto.setStyleOptionId(original.getStyleOptionId());
        dto.setTextureOptionId(original.getTextureOptionId());
        dto.setWallColorId(original.getWallColorId());
        dto.setCustomRequirement(original.getCustomRequirement());

        List<TaskSoftDecoration> softList = softDecorationMapper.selectList(
                new LambdaQueryWrapper<TaskSoftDecoration>().eq(TaskSoftDecoration::getTaskId, taskId));
        dto.setSoftDecorationIds(softList.stream().map(TaskSoftDecoration::getOptionId).collect(Collectors.toList()));

        return createTask(userId, dto);
    }

    private List<String> getSoftDecorationNames(Long taskId) {
        List<TaskSoftDecoration> list = softDecorationMapper.selectList(
                new LambdaQueryWrapper<TaskSoftDecoration>().eq(TaskSoftDecoration::getTaskId, taskId));
        return list.stream().map(TaskSoftDecoration::getOptionName).collect(Collectors.toList());
    }

    private TaskVO toTaskVO(GenerateTask task, UploadImage image, List<String> softNames) {
        TaskVO vo = new TaskVO();
        vo.setId(task.getId());
        vo.setTaskNo(task.getTaskNo());
        vo.setSceneType(task.getSceneType());
        vo.setImageId(task.getImageId());
        vo.setOriginalImageUrl(image != null ? image.getFileUrl() : null);
        vo.setStyleName(task.getStyleName());
        vo.setTextureName(task.getTextureName());
        vo.setWallColorCode(task.getWallColorCode());
        vo.setSoftDecorationNames(softNames);
        vo.setCustomRequirement(task.getCustomRequirement());
        vo.setStatus(task.getStatus());
        vo.setResultImageUrl(task.getResultImageUrl());
        vo.setErrorMessage(task.getErrorMessage());
        vo.setPointsCost(task.getPointsCost());
        vo.setCreatedAt(task.getCreatedAt());
        vo.setUpdatedAt(task.getUpdatedAt());
        vo.setCompletedAt(task.getCompletedAt());
        return vo;
    }
}
