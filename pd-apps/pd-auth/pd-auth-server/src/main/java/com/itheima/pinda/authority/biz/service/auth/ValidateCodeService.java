package com.itheima.pinda.authority.biz.service.auth;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

public interface ValidateCodeService {
    public void create(String key, HttpServletResponse response) throws IOException;
}
