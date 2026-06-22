package com.zkhf.epmis.process.filter;

import com.alibaba.fastjson2.JSON;
import com.zkhf.epmis.process.base.entity.SysRole;
import com.zkhf.epmis.process.base.entity.SysUser;
import com.zkhf.epmis.process.base.model.LoginUser;
import com.zkhf.epmis.process.facade.auth.AuthFacade;
import com.zkhf.epmis.process.global.GVarContainer;
import com.zkhf.epmis.core.constant.Constants;
import com.zkhf.epmis.core.constant.HttpStatus;
import com.zkhf.epmis.core.domain.AjaxResult;
import com.zkhf.epmis.core.utils.ServletUtils;
import com.zkhf.epmis.core.utils.StringUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;
import java.util.Set;

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
                || path.startsWith("/health")) {
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
        // 校验操作权限 todo 先不用
//        if (!hasPermission(loginUser, path, method)) {
//            log.warn("用户 {} 没有访问 {} {} 的权限", loginUser.getUser().getUserName(), method, path);
//            ServletUtils.renderString(response, HttpStatus.FORBIDDEN,
//                    JSON.toJSONString(AjaxResult.error(HttpStatus.FORBIDDEN, "权限不足，无法访问该接口")));
//            return;
//        }
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

    /**
     * 权限校验逻辑
     */
    private boolean hasPermission(LoginUser loginUser, String requestURI, String httpMethod) {
        // 如果是超级管理员，拥有所有权限
        if (isAdmin(loginUser)) {
            return true;
        }
        // 获取用户权限列表
        Set<String> permissions = loginUser.getPermissions();
        if (permissions == null || permissions.isEmpty()) {
            return false;
        }
        // 构建权限标识符：HTTP方法:URL路径
        String requiredPermission = buildPermissionIdentifier(httpMethod, requestURI);
        // 检查用户是否拥有该权限
        return permissions.contains(requiredPermission);
    }

    private boolean isAdmin(LoginUser loginUser) {
        SysUser user = loginUser.getUser();
        if (null == user || null == user.getRoles() || user.getRoles().size() < 1) {
            return false;
        } else {
            List<SysRole> list = user.getRoles();
            for (SysRole sysRole : list) {
                if (sysRole.isAdmin()) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * 构建权限标识符
     */
    private String buildPermissionIdentifier(String httpMethod, String path) {
        return httpMethod.toLowerCase() + ":" + normalizePath(path);
    }

    /**
     * 规范化路径
     */
    private static String normalizePath(String path) {
        if (path.startsWith("/") && path.endsWith("/")) {
            return path.substring(1, path.length() - 1);
        }
        if (path.startsWith("/")) {
            return path.substring(1);
        }
        if (path.endsWith("/")) {
            return path.substring(0, path.length() - 1);
        }
        return path;
    }
}
