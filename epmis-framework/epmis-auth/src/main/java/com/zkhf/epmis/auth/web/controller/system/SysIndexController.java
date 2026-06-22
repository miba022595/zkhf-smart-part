package com.zkhf.epmis.auth.web.controller.system;

import com.zkhf.epmis.core.config.EPMISConfig;
import com.zkhf.epmis.core.utils.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 首页
 */
@RestController
public class SysIndexController {

    /**
     * 访问首页，提示语
     */
    @RequestMapping("/")
    public String index() {
        return StringUtils.format("欢迎使用{}后台管理框架，当前版本：v{}，请通过前端地址访问。", EPMISConfig.getName(), EPMISConfig.getVersion());
    }
}
