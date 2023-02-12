package com.itheima.pinda.authority.controller.auth;

//登录


import com.itheima.pinda.authority.biz.service.auth.ValidateCodeService;
import com.itheima.pinda.authority.biz.service.auth.impl.AuthManager;
import com.itheima.pinda.authority.dto.auth.LoginDTO;
import com.itheima.pinda.authority.dto.auth.LoginParamDTO;
import com.itheima.pinda.base.BaseController;
import com.itheima.pinda.base.R;
import com.itheima.pinda.log.annotation.SysLog;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@RestController
@RequestMapping("/anno")
@Api(tags = "login controller", value = "LoginController")
public class LoginController extends BaseController {

    @Autowired
    private ValidateCodeService validateCodeService;

    @Autowired
    private AuthManager authManager;

    // 生成验证码
    @GetMapping(value = "/captcha", produces = "image/png")
    @ApiOperation(notes = "CAPTCHA", value = "CAPTCHA")
    @SysLog("generate captcha")
    public void captcha(@RequestParam(value = "key") String key, HttpServletResponse response) throws IOException {
        validateCodeService.create(key, response);
    }

    // 登录认证
    @PostMapping("/login")
    @ApiOperation(notes = "login", value = "login")
    @SysLog("login")
    public R<LoginDTO> login(@Validated @RequestBody LoginParamDTO loginParamDTO) {
        // 校验验证码
        boolean check = validateCodeService.check(loginParamDTO.getKey(), loginParamDTO.getCode());
        if (check) {
            R<LoginDTO> r = authManager.login(loginParamDTO.getAccount(), loginParamDTO.getPassword());
            return r;
        }
        return this.success(null);
    }

    @PostMapping("/check")
    @ApiOperation(notes = "check", value = "check")
    public boolean check(@RequestBody LoginParamDTO loginParamDTO) {
        return validateCodeService.check(loginParamDTO.getKey(), loginParamDTO.getCode());
    }
}
