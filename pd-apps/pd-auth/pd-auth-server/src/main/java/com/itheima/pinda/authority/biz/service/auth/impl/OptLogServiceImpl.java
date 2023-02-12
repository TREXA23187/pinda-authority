package com.itheima.pinda.authority.biz.service.auth.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.itheima.pinda.authority.biz.dao.auth.OptLogMapper;
import com.itheima.pinda.authority.biz.service.auth.OptLogService;
import com.itheima.pinda.authority.entity.common.OptLog;
import com.itheima.pinda.dozer.DozerUtils;
import com.itheima.pinda.log.entity.OptLogDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;


@Service
@Slf4j
public class OptLogServiceImpl extends ServiceImpl<OptLogMapper, OptLog> implements OptLogService {

    @Autowired
    private DozerUtils dozerUtils;

    //    保存用户操作日志
    @Override
    public void save(OptLogDTO optLogDTO) {
        baseMapper.insert(dozerUtils.map(optLogDTO, OptLog.class));

    }
}
