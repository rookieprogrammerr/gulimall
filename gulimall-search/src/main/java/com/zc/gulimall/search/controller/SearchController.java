package com.zc.gulimall.search.controller;

import com.zc.gulimall.search.entity.vo.SearchParam;
import com.zc.gulimall.search.entity.vo.SearchResult;
import com.zc.gulimall.search.service.MallSearchService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class SearchController {

    @Autowired
    private MallSearchService mallSearchService;

    /**
     * 自动将页面提交过来的所有查询参数封装成指定的对象
     * @param searchParam
     * @return
     */
    @GetMapping("/list.html")
    public String listPage(SearchParam searchParam, Model model) {

        //  1、根据传递来的页面的查询参数，去es中检索商品
        SearchResult result = mallSearchService.search(searchParam);
        model.addAttribute("result", result);

        return "list";
    }
}
