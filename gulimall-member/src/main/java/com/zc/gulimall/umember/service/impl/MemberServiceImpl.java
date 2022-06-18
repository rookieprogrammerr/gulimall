package com.zc.gulimall.umember.service.impl;

import com.zc.gulimall.umember.dao.MemberLevelDao;
import com.zc.gulimall.umember.entity.MemberLevelEntity;
import com.zc.gulimall.umember.entity.vo.MemberLoginVo;
import com.zc.gulimall.umember.entity.vo.MemberRegistVo;
import com.zc.gulimall.umember.exception.EMailExistException;
import com.zc.gulimall.umember.exception.PhoneExistException;
import com.zc.gulimall.umember.exception.UserNameExistException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import java.util.Map;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.zc.common.utils.PageUtils;
import com.zc.common.utils.Query;

import com.zc.gulimall.umember.dao.MemberDao;
import com.zc.gulimall.umember.entity.MemberEntity;
import com.zc.gulimall.umember.service.MemberService;


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

}