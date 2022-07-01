package com.zc.gulimall.umember.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.zc.common.utils.PageUtils;
import com.zc.gulimall.umember.entity.MemberEntity;
import com.zc.gulimall.umember.entity.vo.MemberLoginVo;
import com.zc.gulimall.umember.entity.vo.MemberRegistVo;
import com.zc.gulimall.umember.entity.vo.SocialUser;
import com.zc.gulimall.umember.exception.EMailExistException;
import com.zc.gulimall.umember.exception.PhoneExistException;
import com.zc.gulimall.umember.exception.UserNameExistException;

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

    /**
     * 注册会员
     * @param memberRegistVo
     */
    void regist(MemberRegistVo memberRegistVo);

    /**
     * 校验邮箱是否唯一
     */
    void checkEmailUnique(String email) throws EMailExistException;

    /**
     * 校验用户名是否唯一
     */
    void checkUsernameUnique(String username) throws UserNameExistException;

    /**
     * 校验手机号是否唯一
     */
    void checkMobileUnique(String mobile) throws PhoneExistException;

    /**
     * 会员登录
     * @param memberLoginVo
     * @return
     */
    MemberEntity login(MemberLoginVo memberLoginVo);

    /**
     * 社交帐号登录
     * @param socialUser
     * @return
     */
    MemberEntity login(SocialUser socialUser) throws Exception;
}

