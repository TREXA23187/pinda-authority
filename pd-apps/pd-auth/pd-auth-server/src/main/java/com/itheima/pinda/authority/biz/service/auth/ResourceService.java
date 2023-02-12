package com.itheima.pinda.authority.biz.service.auth;

import com.baomidou.mybatisplus.extension.service.IService;
import com.itheima.pinda.authority.dto.auth.ResourceQueryDTO;
import com.itheima.pinda.authority.entity.auth.Resource;

import java.util.List;

public interface ResourceService extends IService<Resource> {
    public List<Resource> findVisibleResource(ResourceQueryDTO resourceQueryDTO);
}
