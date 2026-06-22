package com.zkhf.epmis.platform.facade.auth;

import com.zkhf.epmis.platform.base.domain.model.LoginUser;
import com.zkhf.epmis.platform.facade.auth.fallback.AuthFacadeFallback;
import com.zkhf.epmis.platform.facade.config.FeignConfig;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

@FeignClient(name = "epmis-auth", url = "${feign.epmis-auth.url}", path = "/"
        , contextId = "loginClient"
        , configuration = FeignConfig.class
        , fallback = AuthFacadeFallback.class)
public interface AuthFacade {

    @RequestMapping(value = "/getLoginUser", method = RequestMethod.GET)
    LoginUser getLoginUser();
}
