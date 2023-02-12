package com.itheima.pinda.authority.biz.service.auth.impl;

//处理认证相关

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.itheima.pinda.auth.server.utils.JwtTokenServerUtils;
import com.itheima.pinda.auth.utils.JwtUserInfo;
import com.itheima.pinda.auth.utils.Token;
import com.itheima.pinda.authority.biz.service.auth.ResourceService;
import com.itheima.pinda.authority.biz.service.auth.UserService;
import com.itheima.pinda.authority.dto.auth.LoginDTO;
import com.itheima.pinda.authority.dto.auth.ResourceQueryDTO;
import com.itheima.pinda.authority.dto.auth.UserDTO;
import com.itheima.pinda.authority.entity.auth.Resource;
import com.itheima.pinda.authority.entity.auth.User;
import com.itheima.pinda.base.R;
import com.itheima.pinda.common.constant.CacheKey;
import com.itheima.pinda.dozer.DozerUtils;
import com.itheima.pinda.exception.code.ExceptionCode;
import net.oschina.j2cache.CacheChannel;
import org.apache.commons.codec.digest.DigestUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class AuthManager {

    @Autowired
    private UserService userService;

    @Autowired
    private DozerUtils dozerUtils;

    @Autowired
    private JwtTokenServerUtils jwtTokenServerUtils;

    @Autowired
    private ResourceService resourceService;

    @Autowired
    private CacheChannel cacheChannel;

    public R<LoginDTO> login(String account, String password) {
        R<User> userR = check(account, password);
        Boolean isError = userR.getIsError();
        if (isError) {
            return R.fail("login fail");
        }

        User user = userR.getData();
        Token token = generateUserToken(user);

        // 查询用户可以访问的资源权限
        List<Resource> userResource = resourceService.findVisibleResource(ResourceQueryDTO.builder().userId(user.getId()).build());
        List<String> permissionList = null;
        if (userResource != null && userResource.size() > 0) {
            // 用户对应的权限（前端用的）
            permissionList = userResource.stream().map(Resource::getCode).collect(Collectors.toList());

            // 将用户对应的权限（后端网关用的）缓存
            List<String> visibleResource = userResource.stream().map((resource -> {
                return resource.getMethod() + resource.getUrl();
            })).collect(Collectors.toList());
            cacheChannel.set(CacheKey.USER_RESOURCE, user.getId().toString(), visibleResource);

        }


        LoginDTO loginDTO = LoginDTO.builder().
                user(dozerUtils.map(userR.getData(), UserDTO.class)).
                token(token).
                permissionsList(permissionList).
                build();

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
