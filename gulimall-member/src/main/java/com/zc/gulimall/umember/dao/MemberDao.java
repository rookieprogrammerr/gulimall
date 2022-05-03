package com.zc.gulimall.umember.dao;

import com.zc.gulimall.umember.entity.MemberEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * 会员
 * 
 * @author zhaocan
 * @email zc1872751113@gmail.com
 * @date 2020-07-16 12:24:12
 */
@Mapper
public interface MemberDao extends BaseMapper<MemberEntity> {
	
}
