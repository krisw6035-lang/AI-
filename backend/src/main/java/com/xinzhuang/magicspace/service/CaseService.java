package com.xinzhuang.magicspace.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.xinzhuang.magicspace.common.PageResult;
import com.xinzhuang.magicspace.entity.CaseItem;
import com.xinzhuang.magicspace.mapper.CaseItemMapper;
import com.xinzhuang.magicspace.vo.CaseVO;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 案例服务
 */
@Service
public class CaseService {

    private final CaseItemMapper caseItemMapper;

    public CaseService(CaseItemMapper caseItemMapper) {
        this.caseItemMapper = caseItemMapper;
    }

    public PageResult<CaseVO> getCases(String sceneType, int page, int pageSize) {
        int cappedPageSize = Math.min(pageSize, 50);
        LambdaQueryWrapper<CaseItem> query = new LambdaQueryWrapper<CaseItem>()
                .eq(CaseItem::getStatus, 1)
                .orderByAsc(CaseItem::getSortOrder);

        if (sceneType != null) {
            query.eq(CaseItem::getSceneType, sceneType);
        }

        Page<CaseItem> mpPage = new Page<>(page, cappedPageSize);
        Page<CaseItem> result = caseItemMapper.selectPage(mpPage, query);

        List<CaseVO> list = result.getRecords().stream().map(this::toVO).collect(Collectors.toList());
        return new PageResult<>(result.getTotal(), page, cappedPageSize, list);
    }

    public CaseVO getDetail(Long id) {
        CaseItem item = caseItemMapper.selectById(id);
        return item != null ? toVO(item) : null;
    }

    private CaseVO toVO(CaseItem item) {
        CaseVO vo = new CaseVO();
        vo.setId(item.getId());
        vo.setTitle(item.getTitle());
        vo.setDescription(item.getDescription());
        vo.setSceneType(item.getSceneType());
        vo.setStyleName(item.getStyleName());
        vo.setBeforeImageUrl(item.getBeforeImageUrl());
        vo.setAfterImageUrl(item.getAfterImageUrl());
        vo.setDesignParams(item.getDesignParams());
        vo.setSortOrder(item.getSortOrder());
        vo.setCreatedAt(item.getCreatedAt());
        return vo;
    }
}
