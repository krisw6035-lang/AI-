package com.xinzhuang.magicspace.controller;

import cn.dev33.satoken.stp.StpUtil;
import com.xinzhuang.magicspace.common.PageResult;
import com.xinzhuang.magicspace.common.Result;
import com.xinzhuang.magicspace.dto.CreateTaskDTO;
import com.xinzhuang.magicspace.service.GenerateTaskService;
import com.xinzhuang.magicspace.vo.TaskVO;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

/**
 * 生成任务接口
 */
@RestController
@RequestMapping("/api/generate")
public class GenerateController {

    private final GenerateTaskService generateTaskService;

    public GenerateController(GenerateTaskService generateTaskService) {
        this.generateTaskService = generateTaskService;
    }

    /**
     * 创建生成任务
     */
    @PostMapping("/tasks")
    public Result<TaskVO> createTask(@Valid @RequestBody CreateTaskDTO dto) {
        Long userId = StpUtil.getLoginIdAsLong();
        TaskVO task = generateTaskService.createTask(userId, dto);
        return Result.ok(task);
    }

    /**
     * 查询任务状态（轮询用）
     */
    @GetMapping("/tasks/{taskId}/status")
    public Result<TaskVO> queryStatus(@PathVariable Long taskId) {
        Long userId = StpUtil.getLoginIdAsLong();
        TaskVO task = generateTaskService.queryStatus(taskId, userId);
        return Result.ok(task);
    }

    /**
     * 查询任务详情
     */
    @GetMapping("/tasks/{taskId}")
    public Result<TaskVO> getTaskDetail(@PathVariable Long taskId) {
        Long userId = StpUtil.getLoginIdAsLong();
        TaskVO task = generateTaskService.getDetail(taskId, userId);
        return Result.ok(task);
    }

    /**
     * 用户历史生成记录（分页）
     */
    @GetMapping("/tasks/history")
    public Result<PageResult<TaskVO>> getHistory(@RequestParam(defaultValue = "1") int page,
                                                  @RequestParam(defaultValue = "10") int pageSize) {
        Long userId = StpUtil.getLoginIdAsLong();
        PageResult<TaskVO> result = generateTaskService.getHistory(userId, page, pageSize);
        return Result.ok(result);
    }

    /**
     * 重新生成
     */
    @PostMapping("/tasks/{taskId}/retry")
    public Result<TaskVO> retry(@PathVariable Long taskId) {
        Long userId = StpUtil.getLoginIdAsLong();
        TaskVO task = generateTaskService.retry(taskId, userId);
        return Result.ok(task);
    }
}
