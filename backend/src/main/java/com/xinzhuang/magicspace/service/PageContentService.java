package com.xinzhuang.magicspace.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.xinzhuang.magicspace.entity.PageContent;
import com.xinzhuang.magicspace.mapper.PageContentMapper;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 页面内容服务
 */
@Service
public class PageContentService {

    private final PageContentMapper pageContentMapper;

    public PageContentService(PageContentMapper pageContentMapper) {
        this.pageContentMapper = pageContentMapper;
    }

    /**
     * 根据页面标识获取内容段落
     */
    public List<PageContent> getByPageKey(String pageKey) {
        return pageContentMapper.selectList(
                new LambdaQueryWrapper<PageContent>()
                        .eq(PageContent::getPageKey, pageKey)
                        .orderByAsc(PageContent::getSortOrder));
    }
}
