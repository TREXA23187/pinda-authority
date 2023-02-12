package com.itheima.pinda.zuul.filter;

import cn.hutool.core.util.StrUtil;
import com.itheima.pinda.authority.dto.auth.ResourceQueryDTO;
import com.itheima.pinda.authority.entity.auth.Resource;
import com.itheima.pinda.base.R;
import com.itheima.pinda.common.constant.CacheKey;
import com.itheima.pinda.context.BaseContextConstants;
import com.itheima.pinda.exception.code.ExceptionCode;
import com.itheima.pinda.zuul.api.ResourceApi;
import com.netflix.zuul.context.RequestContext;
import lombok.extern.slf4j.Slf4j;
import net.oschina.j2cache.CacheChannel;
import net.oschina.j2cache.CacheObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.cloud.netflix.zuul.filters.support.FilterConstants;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.stream.Collectors;

import static org.springframework.cloud.netflix.zuul.filters.support.FilterConstants.PRE_TYPE;

/**
 * 权限验证过滤器
 */
@Component
@Slf4j
public class AccessFilter extends BaseFilter {
    @Autowired
    private CacheChannel cacheChannel;
    @Qualifier("com.itheima.pinda.zuul.api.ResourceApi")
    @Autowired
    private ResourceApi resourceApi;

    @Override
    public String filterType() {
        return PRE_TYPE;
    }

    @Override
    public int filterOrder() {
        return FilterConstants.PRE_DECORATION_FILTER_ORDER + 10;
    }

    @Override
    public boolean shouldFilter() {
        return true;
    }

    /**
     * 验证当前用户是否拥有某个URI的访问权限
     */
    @Override
    public Object run() {
        // 不进行拦截的地址
        if (isIgnoreToken()) {
            return null;
        }

        HttpServletRequest request = RequestContext.getCurrentContext().getRequest();
        String requestURI = request.getRequestURI();
        requestURI = StrUtil.subSuf(requestURI, zuulPrefix.length());
        requestURI = StrUtil.subSuf(requestURI, requestURI.indexOf("/", 1));
        String method = request.getMethod();
        String permission = method + requestURI;

        CacheObject cacheObject = cacheChannel.get(CacheKey.RESOURCE, CacheKey.RESOURCE_NEED_TO_CHECK);
        List<String> list = (List<String>) cacheObject.getValue();
        if (list == null) {
            list = resourceApi.list().getData();
            if (list != null && list.size() > 0) {
                cacheChannel.set(CacheKey.RESOURCE, CacheKey.RESOURCE_NEED_TO_CHECK, list);
            }
        }

        long count = list.stream().filter((resource) -> {
            return permission.startsWith(resource);
        }).count();

        if (count == 0) {
            errorResponse(ExceptionCode.UNAUTHORIZED.getMsg(), ExceptionCode.UNAUTHORIZED.getCode(), 200);
            return null;
        }

        String userId = RequestContext.getCurrentContext().getZuulRequestHeaders().get(BaseContextConstants.JWT_KEY_USER_ID);
        List<String> visibleResource = (List<String>) cacheChannel.get(CacheKey.USER_RESOURCE, userId).getValue();
        if (visibleResource == null) {
            ResourceQueryDTO resourceQueryDTO = ResourceQueryDTO.builder().userId(Long.parseLong(userId)).build();
            List<Resource> resourceList = resourceApi.visible(resourceQueryDTO).getData();

            if (resourceList != null && resourceList.size() > 0) {
                visibleResource = resourceList.stream().map((resource -> {
                    return resource.getMethod() + resource.getUrl();
                })).collect(Collectors.toList());

                cacheChannel.set(CacheKey.USER_RESOURCE, userId, visibleResource);
            }
        }

        count = visibleResource.stream().filter((resource) -> {
            return permission.startsWith(resource);
        }).count();

        if (count > 0) {
            return null;
        } else {
            errorResponse(ExceptionCode.UNAUTHORIZED.getMsg(), ExceptionCode.UNAUTHORIZED.getCode(), 200);
            return null;
        }

    }
}