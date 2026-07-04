package com.xinzhuang.magicspace.controller;

import com.xinzhuang.magicspace.common.Result;
import com.xinzhuang.magicspace.entity.PageContent;
import com.xinzhuang.magicspace.service.PageContentService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 页面内容接口
 */
@RestController
@RequestMapping("/api/page")
public class PageController {

    private final PageContentService pageContentService;

    public PageController(PageContentService pageContentService) {
        this.pageContentService = pageContentService;
    }

    @GetMapping("/about")
    public Result<List<PageContent>> getAbout() {
        return Result.ok(pageContentService.getByPageKey("about"));
    }

    @GetMapping("/contact")
    public Result<List<PageContent>> getContact() {
        return Result.ok(pageContentService.getByPageKey("contact"));
    }

    @GetMapping("/explore")
    public Result<List<PageContent>> getExplore() {
        return Result.ok(pageContentService.getByPageKey("explore"));
    }
}
