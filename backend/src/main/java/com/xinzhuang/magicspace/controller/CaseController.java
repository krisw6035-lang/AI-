package com.xinzhuang.magicspace.controller;

import com.xinzhuang.magicspace.common.PageResult;
import com.xinzhuang.magicspace.common.Result;
import com.xinzhuang.magicspace.service.CaseService;
import com.xinzhuang.magicspace.vo.CaseVO;
import org.springframework.web.bind.annotation.*;

/**
 * 案例接口
 */
@RestController
@RequestMapping("/api/cases")
public class CaseController {

    private final CaseService caseService;

    public CaseController(CaseService caseService) {
        this.caseService = caseService;
    }

    @GetMapping
    public Result<PageResult<CaseVO>> getCases(@RequestParam(required = false) String sceneType,
                                                @RequestParam(defaultValue = "1") int page,
                                                @RequestParam(defaultValue = "10") int pageSize) {
        return Result.ok(caseService.getCases(sceneType, page, pageSize));
    }

    @GetMapping("/{id}")
    public Result<CaseVO> getDetail(@PathVariable Long id) {
        return Result.ok(caseService.getDetail(id));
    }
}
