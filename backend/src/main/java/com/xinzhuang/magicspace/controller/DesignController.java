package com.xinzhuang.magicspace.controller;

import com.xinzhuang.magicspace.common.BizException;
import com.xinzhuang.magicspace.common.ErrorCode;
import com.xinzhuang.magicspace.common.Result;
import com.xinzhuang.magicspace.enums.DesignOptionType;
import com.xinzhuang.magicspace.service.DesignOptionService;
import com.xinzhuang.magicspace.vo.DesignOptionVO;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.List;

/**
 * 设计选项接口
 */
@RestController
@RequestMapping("/api/design")
public class DesignController {

    private final DesignOptionService designOptionService;

    public DesignController(DesignOptionService designOptionService) {
        this.designOptionService = designOptionService;
    }

    /**
     * 获取设计选项
     * @param type  选项类型：STYLE / TEXTURE / WALL_COLOR / SOFT_DECORATION
     * @param scene 场景类型：INDOOR / OUTDOOR（可选，用于过滤软装）
     */
    @GetMapping("/options")
    public Result<List<DesignOptionVO>> getOptions(@RequestParam String type,
                                                    @RequestParam(required = false) String scene) {
        // 校验 type 是否为合法枚举值
        boolean validType = Arrays.stream(DesignOptionType.values())
                .anyMatch(t -> t.name().equalsIgnoreCase(type));
        if (!validType) {
            throw new BizException(ErrorCode.BAD_REQUEST.getCode(),
                    "无效的选项类型，支持: STYLE, TEXTURE, WALL_COLOR, SOFT_DECORATION");
        }
        List<DesignOptionVO> options = designOptionService.getOptions(type.toUpperCase(), scene);
        return Result.ok(options);
    }
}
