package com.itheima.test;

import com.wf.captcha.ArithmeticCaptcha;
import com.wf.captcha.ChineseCaptcha;
import com.wf.captcha.base.Captcha;

import java.io.File;
import java.io.FileOutputStream;
import java.nio.file.Files;

public class CaptchaTest {
    public static void main(String[] args) throws Exception {
//        new ChineseCaptcha(150, 60);

        //算术类型图片验证码
        Captcha captcha = new ArithmeticCaptcha(115, 42);//指定图片的宽度和高度
        captcha.setCharType(2);
//        captcha.out(Files.newOutputStream(new File("/Users/chenxingyang/Desktop/test.png").toPath()));
        String text = captcha.text();
        System.out.println(text);
    }
}
