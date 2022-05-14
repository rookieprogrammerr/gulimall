package com.zc.gulimall.search.service.impl;

import com.zc.gulimall.search.entity.vo.SearchParam;
import com.zc.gulimall.search.entity.vo.SearchResult;
import com.zc.gulimall.search.service.MallSearchService;
import org.springframework.stereotype.Service;

@Service
public class MallSearchServiceImpl implements MallSearchService {

    @Override
    public SearchResult search(SearchParam searchParam) {
        //  1、动态构建出查询需要的DSL语句
        return null;
    }
}
