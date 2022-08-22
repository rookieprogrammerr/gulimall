package com.zc.gulimall.member.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.zc.common.utils.HttpUtils;
import com.zc.gulimall.member.dao.MemberLevelDao;
import com.zc.gulimall.member.entity.MemberLevelEntity;
import com.zc.gulimall.member.entity.vo.MemberLoginVo;
import com.zc.gulimall.member.entity.vo.MemberRegistVo;
import com.zc.gulimall.member.entity.vo.SocialUser;
import com.zc.gulimall.member.exception.EMailExistException;
import com.zc.gulimall.member.exception.PhoneExistException;
import com.zc.gulimall.member.exception.UserNameExistException;
import org.apache.http.HttpResponse;
import org.apache.http.util.EntityUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.zc.common.utils.PageUtils;
import com.zc.common.utils.Query;

import com.zc.gulimall.member.dao.MemberDao;
import com.zc.gulimall.member.entity.MemberEntity;
import com.zc.gulimall.member.service.MemberService;


@Service("memberService")
public class MemberServiceImpl extends ServiceImpl<MemberDao, MemberEntity> implements MemberService {

    @Autowired
    private MemberLevelDao memberLevelDao;

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<MemberEntity> page = this.page(
                new Query<MemberEntity>().getPage(params),
                new QueryWrapper<MemberEntity>()
        );

        return new PageUtils(page);
    }

    /**
     * 注册会员
     * @param memberRegistVo
     */
    @Override
    public void regist(MemberRegistVo memberRegistVo) {
        MemberEntity entity = new MemberEntity();

        //设置默认等级
        MemberLevelEntity levelEntity = memberLevelDao.getDefaultLevel();
        entity.setLevelId(levelEntity.getId());

        //检查用户名和手机号是否唯一。为了让controller能感知异常，使用异常机制
        checkMobileUnique(memberRegistVo.getPhone());
        checkUsernameUnique(memberRegistVo.getUserName());

        entity.setMobile(memberRegistVo.getPhone());
        entity.setUsername(memberRegistVo.getUserName());
        entity.setNickname(memberRegistVo.getUserName());

        //密码要进行加密存储
        BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
        String encode = passwordEncoder.encode(memberRegistVo.getPassword());

        entity.setPassword(encode);

        //其他的默认信息

        //保存
        this.baseMapper.insert(entity);
    }

    @Override
    public void checkEmailUnique(String email) throws EMailExistException {
        Integer count = this.baseMapper.selectCount(new QueryWrapper<MemberEntity>().eq("email", email));
        if (count > 0) {
            throw new EMailExistException();
        }
    }

    @Override
    public void checkUsernameUnique(String username) throws UserNameExistException {
        Integer count = this.baseMapper.selectCount(new QueryWrapper<MemberEntity>().eq("username", username));
        if(count > 0){
            throw new UserNameExistException();
        }
    }

    @Override
    public void checkMobileUnique(String mobile) throws PhoneExistException {
        Integer count = this.baseMapper.selectCount(new QueryWrapper<MemberEntity>().eq("mobile", mobile));
        if(count > 0){
            throw new PhoneExistException();
        }
    }

    /**
     * 会员登陆
     * @param memberLoginVo
     * @return
     */
    @Override
    public MemberEntity login(MemberLoginVo memberLoginVo) {
        String loginacct = memberLoginVo.getLoginacct();
        String password = memberLoginVo.getPassword();

        //1、去数据库查询
        MemberEntity entity = this.baseMapper.selectOne(new QueryWrapper<MemberEntity>().eq("username", loginacct)
                .or().eq("mobile", loginacct));

        if(entity == null) {
            //登录失败
            return null;
        } else {
            //获取数据库的password
            String passwordDb = entity.getPassword();
            BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
            //密码匹配
            if (passwordEncoder.matches(password, passwordDb)) {
                //密码正确，返回用户信息
                return entity;
            } else {
                return null;
            }
        }
    }

    /**
     * 社交账号登录
     * @param socialUser
     * @return
     */
    @Override
    public MemberEntity login(SocialUser socialUser) throws Exception {
        //登录与注册合并逻辑
        String uid = socialUser.getUid();
        //1、判断当前社交用户是否已经登录过系统
        MemberEntity memberEntity = this.baseMapper.selectOne(new QueryWrapper<MemberEntity>().eq("social_uid", uid));
        if(memberEntity != null) {
            //用户已经注册
            MemberEntity update = new MemberEntity();
            update.setId(memberEntity.getId());
            update.setAccessToken(socialUser.getAccess_token());
            update.setExpiresIn(socialUser.getExpires_in());

            this.baseMapper.updateById(update);

            memberEntity.setAccessToken(socialUser.getAccess_token());
            memberEntity.setExpiresIn(socialUser.getExpires_in());
            return memberEntity;
        } else {
            //2、没有查到当前社交用户对应的记录我们就需要注册一个
            MemberEntity regist = new MemberEntity();
            try {
                //3、查询当前社交用户的社交账号信息（昵称、性别）
                Map<String, String> query = new HashMap<>();
                query.put("access_token", socialUser.getAccess_token());
                query.put("uid", uid);
                HttpResponse response = HttpUtils.doGet("https://api.weibo.com", "/2/users/show.json", "get", new HashMap<String, String>(), query);
                if(response.getStatusLine().getStatusCode() == 200) {
                    //查询成功
                    String json = EntityUtils.toString(response.getEntity());
                    JSONObject jsonObject = JSON.parseObject(json, JSONObject.class);
                    //昵称
                    regist.setNickname(jsonObject.getString("name"));
                    //性别
                    regist.setGender("m".equals(jsonObject.getString("gender"))?1:0);
                    //城市
                    regist.setCity(jsonObject.getString("location"));
                    //头像
                    regist.setHeader(jsonObject.getString("profile_image_url"));
                }
            } catch (Exception e) {}

            regist.setSocialUid(socialUser.getUid());
            regist.setAccessToken(socialUser.getAccess_token());
            regist.setExpiresIn(socialUser.getExpires_in());
            this.baseMapper.insert(regist);

            return regist;
        }
    }

}