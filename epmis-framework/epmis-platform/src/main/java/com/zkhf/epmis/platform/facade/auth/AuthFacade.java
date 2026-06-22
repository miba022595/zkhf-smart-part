package com.zkhf.epmis.platform.facade.auth;

import com.alibaba.fastjson2.JSON;
import com.zkhf.epmis.auth.web.service.SysLoginService;
import com.zkhf.epmis.core.utils.ServletUtils;
import com.zkhf.epmis.platform.base.domain.model.LoginUser;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Component;

@Component
public class AuthFacade {

    @Resource
    private SysLoginService sysLoginService;

    public LoginUser getLoginUser() {
        HttpServletRequest request = ServletUtils.getRequestAttributes() == null ? null : ServletUtils.getRequest();
        if (request == null) {
            return null;
        }
        return JSON.parseObject(JSON.toJSONString(sysLoginService.getLoginUser(request)), LoginUser.class);
    }
}
