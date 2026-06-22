package com.zkhf.epmis.process.facade.auth;

import com.zkhf.epmis.process.base.entity.SysUser;
import com.zkhf.epmis.process.base.model.LoginUser;
import com.zkhf.epmis.process.facade.auth.fallback.AuthFacadeFallback;
import com.zkhf.epmis.process.facade.config.FeignConfig;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import java.util.List;

@FeignClient(name = "epmis-auth", url = "${feign.epmis-auth.url:}", path = "/"
        , configuration = FeignConfig.class
        , fallback = AuthFacadeFallback.class)
public interface AuthFacade {

    @RequestMapping(value = "/getLoginUser", method = RequestMethod.GET)
    LoginUser getLoginUser();

    @RequestMapping(value = "/system/user/allUserInfo", method = RequestMethod.GET)
    List<SysUser> allUserInfo();
}
