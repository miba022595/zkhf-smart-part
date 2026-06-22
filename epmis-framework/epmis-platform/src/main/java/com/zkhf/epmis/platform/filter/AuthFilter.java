package com.zkhf.epmis.platform.filter;

import com.alibaba.fastjson2.JSON;
import com.zkhf.epmis.core.constant.Constants;
import com.zkhf.epmis.core.constant.HttpStatus;
import com.zkhf.epmis.core.domain.AjaxResult;
import com.zkhf.epmis.core.utils.ServletUtils;
import com.zkhf.epmis.core.utils.StringUtils;
import com.zkhf.epmis.platform.base.domain.model.LoginUser;
import com.zkhf.epmis.platform.facade.auth.AuthFacade;
import com.zkhf.epmis.platform.global.GVarContainer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;

@Slf4j
@Order(1) // 定义过滤器执行顺序
@Component
public class AuthFilter implements Filter {

    // 令牌自定义标识
    @Value("${token.header}")
    private String header;

    private AuthFacade authFacade;

    @Autowired
    public void setAuthFacade(AuthFacade authFacade) {
        this.authFacade = authFacade;
    }

    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {
        HttpServletRequest request = (HttpServletRequest) servletRequest;
        HttpServletResponse response = (HttpServletResponse) servletResponse;
        this.addResponseHeader(request, response);
        String path = request.getRequestURI();
        // 忽略认证的地址
        if (path.startsWith("/api/public")
                || path.startsWith("/health")
                || path.startsWith("/profile") // 附件文件获取
                || path.startsWith("/platform/feign") // feign服务调用
        ) {
            filterChain.doFilter(servletRequest, servletResponse);
            return;
        }
        String method = request.getMethod();
        if ("OPTIONS".equals(method)) {
            ServletUtils.renderString(response, JSON.toJSONString(AjaxResult.error(HttpStatus.SUCCESS, "OPTIONS")));
            return;
        }
        String token = request.getHeader(header);
        if (StringUtils.isEmpty(token)) {
            log.info("Token is missing. ");
            ServletUtils.renderString(response, HttpStatus.UNAUTHORIZED,
                    JSON.toJSONString(AjaxResult.error(HttpStatus.UNAUTHORIZED, "token认证失败：")));
            return;
        }
        LoginUser loginUser = this.getLoginUser();
        if (null == loginUser) {
            ServletUtils.renderString(response, HttpStatus.UNAUTHORIZED,
                    JSON.toJSONString(AjaxResult.error(HttpStatus.UNAUTHORIZED, "token认证失败：")));
            return;
        }
        // 保存返回的信息，待后边做权限时使用
        GVarContainer.setVar(Constants.LOGIN_USER_TV, loginUser);
        try {
            filterChain.doFilter(request, response);
        } finally {
            GVarContainer.clearVar();
        }
    }

    private LoginUser getLoginUser() {
        try {
            return authFacade.getLoginUser();
        } catch (Exception var13) {
            log.error("鉴权服务异常", var13);
        }
        return null;
    }

    public void addResponseHeader(HttpServletRequest request, HttpServletResponse response) {
        response.setHeader("Access-Control-Allow-Origin", "*");
        response.setHeader("Access-Control-Allow-Headers", "*");
        response.setHeader("Access-Control-Allow-Methods", "*");
        response.setHeader("Access-Control-Max-Age", "1728000");
        // 暴露自定义header，对前端可见
        response.setHeader("Access-Control-Expose-Headers", "Content-Disposition, Origin, X-Requested-With, Content-Type");
    }
}
