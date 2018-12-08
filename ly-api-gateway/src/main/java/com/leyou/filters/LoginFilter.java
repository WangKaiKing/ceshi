package com.leyou.filters;

import com.leyou.auth.utils.JwtUtils;
import com.leyou.common.utils.CookieUtils;
import com.leyou.config.FilterProperties;
import com.leyou.config.JwtProperties;
import com.netflix.zuul.ZuulFilter;
import com.netflix.zuul.context.RequestContext;
import com.netflix.zuul.exception.ZuulException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

@Component
@EnableConfigurationProperties({JwtProperties.class,FilterProperties.class})
public class LoginFilter extends ZuulFilter {

    @Autowired
    private FilterProperties filterProps;

    @Autowired
    private JwtProperties props;

    @Override
    public String filterType() {
        return "pre";
    }

    @Override
    public int filterOrder() {
        return 1;
    }

    @Override
    public boolean shouldFilter() {

        RequestContext currentContext = RequestContext.getCurrentContext();

        HttpServletRequest request = currentContext.getRequest();

        String requestURI = request.getRequestURI();

        List<String> allowPaths = filterProps.getAllowPaths();

        for (String allowPath : allowPaths) {
            //如果请求的uri在白名单中包含，则本次请求放行
            if (requestURI.startsWith(allowPath)) {
                return false;
            }
        }

        return true;
    }

    @Override
    public Object run() throws ZuulException {

        RequestContext currentContext = RequestContext.getCurrentContext();

        HttpServletRequest request = currentContext.getRequest();

        String token = CookieUtils.getCookieValue(request, props.getCookieName());

        try {
            // 校验通过什么都不做，即放行
            JwtUtils.getInfoFromToken(token, props.getPublicKey());
        } catch (Exception e) {
            // 校验出现异常，返回401
            currentContext.setSendZuulResponse(false);
            currentContext.setResponseStatusCode(401);
        }

        return null;
    }
}
