package com.zkhf.epmis.process.facade.auth.fallback;

import com.zkhf.epmis.process.base.entity.SysUser;
import com.zkhf.epmis.process.base.model.LoginUser;
import com.zkhf.epmis.process.facade.auth.AuthFacade;

import java.util.List;

public class AuthFacadeFallback implements AuthFacade {

    @Override
    public LoginUser getLoginUser() {
        return null;
    }

    @Override
    public List<SysUser> allUserInfo() {
        return null;
    }
}
