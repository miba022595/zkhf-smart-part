package com.zkhf.epmis.process.facade.auth;

import com.alibaba.fastjson2.JSON;
import com.zkhf.epmis.auth.system.service.ISysUserService;
import com.zkhf.epmis.auth.web.service.SysLoginService;
import com.zkhf.epmis.core.utils.ServletUtils;
import com.zkhf.epmis.process.base.entity.SysUser;
import com.zkhf.epmis.process.base.model.LoginUser;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class AuthFacade {

    @Resource
    private SysLoginService sysLoginService;

    @Resource
    private ISysUserService userService;

    public LoginUser getLoginUser() {
        HttpServletRequest request = ServletUtils.getRequestAttributes() == null ? null : ServletUtils.getRequest();
        if (request == null) {
            return null;
        }
        return JSON.parseObject(JSON.toJSONString(sysLoginService.getLoginUser(request)), LoginUser.class);
    }

    public List<SysUser> allUserInfo() {
        return JSON.parseArray(JSON.toJSONString(userService.allUserInfo()), SysUser.class);
    }
}
