package com.zkhf.epmis.auth.security.handle;

import com.alibaba.fastjson2.JSON;
import com.zkhf.epmis.auth.core.domain.model.LoginUser;
import com.zkhf.epmis.auth.manager.AsyncManager;
import com.zkhf.epmis.auth.manager.factory.AsyncFactory;
import com.zkhf.epmis.auth.utils.MessageUtils;
import com.zkhf.epmis.auth.web.service.TokenService;
import com.zkhf.epmis.core.constant.Constants;
import com.zkhf.epmis.core.domain.AjaxResult;
import com.zkhf.epmis.core.utils.ServletUtils;
import com.zkhf.epmis.core.utils.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.logout.LogoutSuccessHandler;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * 自定义退出处理类 返回成功
 */
@Configuration
public class LogoutSuccessHandlerImpl implements LogoutSuccessHandler {
    @Autowired
    private TokenService tokenService;

    /**
     * 退出处理
     *
     * @return
     */
    @Override
    public void onLogoutSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication)
            throws IOException, ServletException {
        LoginUser loginUser = tokenService.getLoginUser(request);
        if (StringUtils.isNotNull(loginUser)) {
            String userName = loginUser.getUsername();
            // 删除用户缓存记录
            tokenService.delLoginUser(loginUser.getToken());
            // 记录用户退出日志
            AsyncManager.me().execute(AsyncFactory.recordLogininfor(userName, Constants.LOGOUT, MessageUtils.message("user.logout.success")));
        }
        ServletUtils.renderString(response, JSON.toJSONString(AjaxResult.success(MessageUtils.message("user.logout.success"))));
    }
}
