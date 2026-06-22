package com.zkhf.epmis.platform.facade.auth.fallback;

import com.zkhf.epmis.platform.base.domain.model.LoginUser;
import com.zkhf.epmis.platform.facade.auth.AuthFacade;

public class AuthFacadeFallback implements AuthFacade {

    @Override
    public LoginUser getLoginUser() {
        return null;
    }
}
