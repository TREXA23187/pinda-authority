package com.itheima.pinda.authority.controller.auth;

//登录


import com.itheima.pinda.authority.biz.service.auth.ValidateCodeService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@RestController
@RequestMapping("/anno")
@Api(tags = "login controller", value = "LoginController")
public class LoginController {

    @Autowired
    private ValidateCodeService validateCodeService;

    @GetMapping(value = "/captcha", produces = "image/png")
    @ApiOperation(notes = "CAPTCHA", value = "CAPTCHA")
    public void captcha(@RequestParam(value = "key") String key, HttpServletResponse response) throws IOException {
        validateCodeService.create(key, response);
    }
}
