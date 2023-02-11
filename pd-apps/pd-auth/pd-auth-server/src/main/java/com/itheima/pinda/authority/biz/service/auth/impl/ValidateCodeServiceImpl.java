package com.itheima.pinda.authority.biz.service.auth.impl;

import com.itheima.pinda.authority.biz.service.auth.ValidateCodeService;
import com.itheima.pinda.common.constant.CacheKey;
import com.itheima.pinda.exception.BizException;
import com.wf.captcha.ArithmeticCaptcha;
import com.wf.captcha.base.Captcha;
import net.oschina.j2cache.CacheChannel;
import net.oschina.j2cache.CacheObject;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@Service
public class ValidateCodeServiceImpl implements ValidateCodeService {

    @Autowired
    private CacheChannel cacheChannel;

    @Override
    public void create(String key, HttpServletResponse response) throws IOException {
        if (StringUtils.isBlank(key)) {
            throw BizException.validFail("Key cannot be blank!");
        }

        Captcha captcha = new ArithmeticCaptcha(115, 42);
        captcha.setCharType(2);
        String text = captcha.text();

        System.out.println(text);

        cacheChannel.set(CacheKey.CAPTCHA, key, text);

        response.setContentType(MediaType.IMAGE_PNG_VALUE);
        response.setHeader(HttpHeaders.PRAGMA, "No-cache");
        response.setHeader(HttpHeaders.CACHE_CONTROL, "No-cache");
        response.setDateHeader(HttpHeaders.EXPIRES, 0L);

        captcha.out(response.getOutputStream());
    }

    @Override
    public boolean check(String key, String code) {
        if (StringUtils.isBlank(key)) {
            throw BizException.validFail("Key cannot be blank");
        }

        CacheObject cacheObject = cacheChannel.get(CacheKey.CAPTCHA, key);
        if (cacheObject.getValue() == null) {
            throw BizException.validFail("code expired");
        }

        String codeInCache = (String) cacheObject.getValue();
        if (!codeInCache.equals(code)) {
            throw BizException.validFail("incorrect code");
        }

        // 验证成功，清除缓存
        cacheChannel.evict(CacheKey.CAPTCHA, key);

        return true;
    }
}
