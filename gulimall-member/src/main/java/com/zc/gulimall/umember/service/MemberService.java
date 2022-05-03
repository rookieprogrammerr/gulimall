package com.zc.gulimall.umember.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.zc.common.utils.PageUtils;
import com.zc.gulimall.umember.entity.MemberEntity;

import java.util.Map;

/**
 * 会员
 *
 * @author zhaocan
 * @email zc1872751113@gmail.com
 * @date 2020-07-16 12:24:12
 */
public interface MemberService extends IService<MemberEntity> {

    PageUtils queryPage(Map<String, Object> params);
}

