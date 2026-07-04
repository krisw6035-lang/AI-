package com.xinzhuang.magicspace.controller;

import cn.dev33.satoken.stp.StpUtil;
import com.xinzhuang.magicspace.common.PageResult;
import com.xinzhuang.magicspace.common.Result;
import com.xinzhuang.magicspace.service.PointsService;
import com.xinzhuang.magicspace.vo.PointsBalanceVO;
import com.xinzhuang.magicspace.vo.PointsRecordVO;
import org.springframework.web.bind.annotation.*;

/**
 * 积分接口
 */
@RestController
@RequestMapping("/api/points")
public class PointsController {

    private final PointsService pointsService;

    public PointsController(PointsService pointsService) {
        this.pointsService = pointsService;
    }

    /**
     * 查询积分余额
     */
    @GetMapping("/balance")
    public Result<PointsBalanceVO> getBalance() {
        Long userId = StpUtil.getLoginIdAsLong();
        return Result.ok(pointsService.getBalance(userId));
    }

    /**
     * 查询积分流水
     */
    @GetMapping("/records")
    public Result<PageResult<PointsRecordVO>> getRecords(@RequestParam(defaultValue = "1") int page,
                                                          @RequestParam(defaultValue = "20") int pageSize) {
        Long userId = StpUtil.getLoginIdAsLong();
        return Result.ok(pointsService.getRecords(userId, page, pageSize));
    }
}
