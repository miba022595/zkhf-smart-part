package com.zkhf.epmis.auth.web.controller.system;

import com.zkhf.epmis.auth.core.domain.entity.SysMenu;
import com.zkhf.epmis.auth.core.domain.entity.SysUser;
import com.zkhf.epmis.auth.core.domain.model.LoginBody;
import com.zkhf.epmis.auth.core.domain.model.LoginUser;
import com.zkhf.epmis.auth.system.service.ISysMenuService;
import com.zkhf.epmis.auth.utils.SecurityUtils;
import com.zkhf.epmis.auth.web.service.SysLoginService;
import com.zkhf.epmis.auth.web.service.SysPermissionService;
import com.zkhf.epmis.core.constant.Constants;
import com.zkhf.epmis.core.domain.AjaxResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Set;

/**
 * 登录验证
 */
@RestController
public class SysLoginController {
    @Resource
    private SysLoginService loginService;

    @Resource
    private ISysMenuService menuService;

    @Resource
    private SysPermissionService permissionService;

    @GetMapping("/version")
    public AjaxResult getVersion() {
        return AjaxResult.success("hello world,version:1.0.0.2025011101");
    }


    /**
     * 登录方法
     *
     * @param loginBody 登录信息
     * @return 结果
     */
    @PostMapping("/login")
    public AjaxResult login(@RequestBody LoginBody loginBody) {
        AjaxResult ajax = AjaxResult.success();
        // 生成令牌
        String token = loginService.login(loginBody.getUsername(), loginBody.getPassword(), loginBody.getCode(),
                loginBody.getUuid());
        ajax.put(Constants.TOKEN, token);
        return ajax;
    }

    /**
     * 获取登录信息
     */
    @GetMapping("/getLoginUser")
    public LoginUser getLoginUser(HttpServletRequest request) {
        return loginService.getLoginUser(request);
    }

    /**
     * 获取用户信息
     *
     * @return 用户信息
     */
    @GetMapping("getInfo")
    public AjaxResult getInfo() {
        SysUser user = SecurityUtils.getLoginUser().getUser();
        // 角色集合
        Set<String> roles = permissionService.getRolePermission(user);
        // 权限集合
        Set<String> permissions = permissionService.getMenuPermission(user);
        AjaxResult ajax = AjaxResult.success();
        ajax.put("user", user);
        ajax.put("roles", roles);
        ajax.put("permissions", permissions);
        return ajax;
    }

    /**
     * 获取路由信息
     *
     * @return 路由信息
     */
    @GetMapping("getRouters")
    public AjaxResult getRouters() {
        Long userId = SecurityUtils.getUserId();
        List<SysMenu> menus = menuService.selectMenuTreeByUserId(userId);
        return AjaxResult.success(menuService.buildMenus(menus));
    }

    /**
     * 获取路由信息
     *
     * @return 路由信息
     */
    @GetMapping("v2/getRouters")
    public AjaxResult getRouters_v2() {
        Long userId = SecurityUtils.getUserId();
        List<SysMenu> menus = menuService.selectMenuTreeByUserId(userId);
        return AjaxResult.success(menuService.buildMenus(menus));
    }
}
