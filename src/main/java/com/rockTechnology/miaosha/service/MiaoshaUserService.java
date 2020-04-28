package com.rockTechnology.miaosha.service;

import com.rockTechnology.miaosha.Exception.GlobalException;
import com.rockTechnology.miaosha.dao.MiaoshaUserDao;
import com.rockTechnology.miaosha.domain.MiaoshaUser;
import com.rockTechnology.miaosha.redis.MiaoshaUserKey;
import com.rockTechnology.miaosha.redis.RedisService;
import com.rockTechnology.miaosha.result.CodeMsg;
import com.rockTechnology.miaosha.util.MD5Util;
import com.rockTechnology.miaosha.util.UUIDUtil;
import com.rockTechnology.miaosha.vo.LoginVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;


@Service
public class MiaoshaUserService {

    //todo final?
    public static final String COOKIE_NAME_TOKEN = "token";

    @Autowired
    RedisService redisService;

    @Autowired
    MiaoshaUserDao miaoshaUserDao;

    public MiaoshaUser getById(long id){
        //对象级缓存
        MiaoshaUser user = redisService.get(MiaoshaUserKey.getById, "" + id, MiaoshaUser.class);
        if(user != null){
            return user;
        }
        user = miaoshaUserDao.getById(id);
        if (user != null) {
            redisService.set(MiaoshaUserKey.getById, "" + id, user);
        }
        return user;
    }

    public String login(HttpServletResponse response, LoginVo loginVo){
        if (loginVo == null){
            throw new GlobalException(CodeMsg.MOBILE_NOT_EXIST);
        }
        String mobile = loginVo.getMobile();
        String formPass = loginVo.getPassword();
        //判断手机号是否存在
        MiaoshaUser user = getById(Long.parseLong(mobile));
        if (user == null){
            throw new GlobalException(CodeMsg.MOBILE_NOT_EXIST);
        }
        //验证密码
        String dbPass = user.getPassword();
        String salt = user.getSalt(); //db中存着随机的salt
        String str = MD5Util.formPassToDBPass(formPass, salt);
        if (!str.equals(dbPass)){
            throw new GlobalException(CodeMsg.PASSWORD_ERROR);
        }
        String token = UUIDUtil.uuid();
        addCookie(response, token,user);
        return token;

    }

    private void addCookie(HttpServletResponse response, String token, MiaoshaUser user){
        //生成cookie
        //把token信息存到缓存中
        redisService.set(MiaoshaUserKey.token, token, user);
        //把token信息添加到cookie中
        Cookie cookie = new Cookie(COOKIE_NAME_TOKEN, token);
        cookie.setMaxAge(MiaoshaUserKey.token.expireSeconds());
        cookie.setPath("/"); //网站根目录
        //将cookie写到response中
        response.addCookie(cookie);

    }

    //将原cookie覆盖并返回user
    public MiaoshaUser getByToken(HttpServletResponse response, String token) {
        //todo 为什么用isEmpty？
        if (StringUtils.isEmpty(token)){
            return null;
        }
        MiaoshaUser user = redisService.get(MiaoshaUserKey.token, token, MiaoshaUser.class);
        if (user != null) {
            addCookie(response, token, user);
        }
        return user;
    }
}
