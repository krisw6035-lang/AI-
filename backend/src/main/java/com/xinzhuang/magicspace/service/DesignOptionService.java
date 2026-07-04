package com.xinzhuang.magicspace.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.xinzhuang.magicspace.entity.DesignOption;
import com.xinzhuang.magicspace.mapper.DesignOptionMapper;
import com.xinzhuang.magicspace.vo.DesignOptionVO;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 设计选项服务
 */
@Service
public class DesignOptionService {

    private final DesignOptionMapper designOptionMapper;

    public DesignOptionService(DesignOptionMapper designOptionMapper) {
        this.designOptionMapper = designOptionMapper;
    }

    /**
     * 根据选项类型和场景获取选项列表
     */
    public List<DesignOptionVO> getOptions(String optionType, String sceneType) {
        LambdaQueryWrapper<DesignOption> query = new LambdaQueryWrapper<DesignOption>()
                .eq(DesignOption::getOptionType, optionType)
                .eq(DesignOption::getStatus, 1)
                .orderByAsc(DesignOption::getSortOrder);

        // 软装选项按场景过滤
        if (sceneType != null) {
            query.and(w -> w.isNull(DesignOption::getSceneType)
                    .or().eq(DesignOption::getSceneType, sceneType));
        }

        List<DesignOption> options = designOptionMapper.selectList(query);
        return options.stream().map(this::toVO).collect(Collectors.toList());
    }

    /**
     * 根据 ID 获取单个选项
     */
    public DesignOption getById(Long id) {
        return designOptionMapper.selectById(id);
    }

    private DesignOptionVO toVO(DesignOption opt) {
        DesignOptionVO vo = new DesignOptionVO();
        vo.setId(opt.getId());
        vo.setOptionKey(opt.getOptionKey());
        vo.setOptionName(opt.getOptionName());
        vo.setOptionDesc(opt.getOptionDesc());
        vo.setColorHex(opt.getColorHex());
        vo.setIconUrl(opt.getIconUrl());
        vo.setSceneType(opt.getSceneType());
        vo.setSortOrder(opt.getSortOrder());
        return vo;
    }
}
