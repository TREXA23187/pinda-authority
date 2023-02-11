package com.itheima.pinda.authority.biz.service.auth.impl;

//处理认证相关

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.itheima.pinda.auth.server.utils.JwtTokenServerUtils;
import com.itheima.pinda.auth.utils.JwtUserInfo;
import com.itheima.pinda.auth.utils.Token;
import com.itheima.pinda.authority.biz.service.auth.UserService;
import com.itheima.pinda.authority.dto.auth.LoginDTO;
import com.itheima.pinda.authority.dto.auth.UserDTO;
import com.itheima.pinda.authority.entity.auth.User;
import com.itheima.pinda.base.R;
import com.itheima.pinda.dozer.DozerUtils;
import com.itheima.pinda.exception.code.ExceptionCode;
import org.apache.commons.codec.digest.DigestUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class AuthManager {

    @Autowired
    private UserService userService;

    @Autowired
    private DozerUtils dozerUtils;

    @Autowired
    private JwtTokenServerUtils jwtTokenServerUtils;

    public R<LoginDTO> login(String account, String password) {
        R<User> userR = check(account, password);
        Boolean isError = userR.getIsError();
        if (isError) {
            return R.fail("login fail");
        }

        User user = userR.getData();
        Token token = generateUserToken(user);


        // 缓存用户权限

        LoginDTO loginDTO = LoginDTO.builder().user(dozerUtils.map(userR.getData(), UserDTO.class)).token(token).build();
        return R.success(loginDTO);
    }

    public R<User> check(String account, String password) {
        // 校验账号、密码
        User user = userService.getOne(Wrappers.<User>lambdaQuery().eq(User::getAccount, account));
        String md5Hex = DigestUtils.md5Hex(password);

        if (user == null || !user.getPassword().equals(md5Hex)) {
            return R.fail(ExceptionCode.JWT_USER_INVALID);
        }

        return R.success(user);
    }


    // 生成jwt令牌
    public Token generateUserToken(User user) {
        JwtUserInfo jwtUserInfo = new JwtUserInfo(user.getId(), user.getAccount(), user.getName(), user.getOrgId(), user.getStationId());
        return jwtTokenServerUtils.generateUserToken(jwtUserInfo, null);
    }
}
