package com.zkhf.epmis.auth.web.controller.system;

import com.zkhf.epmis.auth.core.controller.BaseController;
import com.zkhf.epmis.auth.core.domain.model.RegisterBody;
import com.zkhf.epmis.auth.system.service.ISysConfigService;
import com.zkhf.epmis.auth.web.service.SysRegisterService;
import com.zkhf.epmis.core.domain.AjaxResult;
import com.zkhf.epmis.core.utils.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

/**
 * 注册验证
 */
@RestController
public class SysRegisterController extends BaseController {
    @Autowired
    private SysRegisterService registerService;

    @Autowired
    private ISysConfigService configService;

    @PostMapping("/register")
    public AjaxResult register(@RequestBody RegisterBody user) {
        if (!("true".equals(configService.selectConfigByKey("sys.account.registerUser")))) {
            return error("当前系统没有开启注册功能！");
        }
        String msg = registerService.register(user);
        return StringUtils.isEmpty(msg) ? success() : error(msg);
    }
}
