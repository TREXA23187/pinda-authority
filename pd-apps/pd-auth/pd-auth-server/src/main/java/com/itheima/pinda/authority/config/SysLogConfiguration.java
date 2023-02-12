package com.itheima.pinda.authority.config;

// 系统操作日志配置类

import com.itheima.pinda.authority.biz.service.common.OptLogService;
import com.itheima.pinda.log.entity.OptLogDTO;
import com.itheima.pinda.log.event.SysLogListener;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;

import java.util.function.Consumer;

@Configuration
@EnableAsync
public class SysLogConfiguration {
    public SysLogListener sysLogListener(OptLogService optLogService){
        Consumer<OptLogDTO> consumer = (optLogDTO -> optLogService.save(optLogDTO));
        return new SysLogListener(consumer);
    }
}
