package com.zkhf.epmis.core.constant;

import java.util.Locale;

/**
 * 常量
 */
public class Constants {


    /**
     * UTF-8 字符集
     */
    public static final String UTF8 = "UTF-8";

    /**
     * GBK 字符集
     */
    public static final String GBK = "GBK";

    /**
     * 系统语言
     */
    public static final Locale DEFAULT_LOCALE = Locale.SIMPLIFIED_CHINESE;

    /**
     * www主域
     */
    public static final String WWW = "www.";

    /**
     * http请求
     */
    public static final String HTTP = "http://";

    /**
     * https请求
     */
    public static final String HTTPS = "https://";

    /**
     * 通用成功标识
     */
    public static final String SUCCESS = "0";

    /**
     * 通用失败标识
     */
    public static final String FAIL = "1";

    /**
     * 登录成功
     */
    public static final String LOGIN_SUCCESS = "Success";

    /**
     * 注销
     */
    public static final String LOGOUT = "Logout";

    /**
     * 注册
     */
    public static final String REGISTER = "Register";

    /**
     * 登录失败
     */
    public static final String LOGIN_FAIL = "Error";

    /**
     * 所有权限标识
     */
    public static final String ALL_PERMISSION = "*:*:*";

    /**
     * 管理员角色权限标识
     */
    public static final String SUPER_ADMIN = "admin";

    /**
     * 角色权限分隔符
     */
    public static final String ROLE_DELIMETER = ",";

    /**
     * 权限标识分隔符
     */
    public static final String PERMISSION_DELIMETER = ",";

    /**
     * 验证码有效期（分钟）
     */
    public static final Integer CAPTCHA_EXPIRATION = 2;

    /**
     * 令牌
     */
    public static final String TOKEN = "token";

    /**
     * 令牌前缀
     */
    public static final String TOKEN_PREFIX = "Bearer ";

    /**
     * 令牌前缀
     */
    public static final String LOGIN_USER_KEY = "login_user_key";

    /**
     * 用户ID
     */
    public static final String JWT_USERID = "userid";

    /**
     * 用户名称
     */
    public static final String JWT_USERNAME = "sub";

    /**
     * 用户头像
     */
    public static final String JWT_AVATAR = "avatar";

    /**
     * 创建时间
     */
    public static final String JWT_CREATED = "created";

    /**
     * 用户权限
     */
    public static final String JWT_AUTHORITIES = "authorities";

    /**
     * 资源映射路径 前缀
     */
    public static final String RESOURCE_PREFIX = "/profile";

    /**
     * RMI 远程方法调用
     */
    public static final String LOOKUP_RMI = "rmi:";

    /**
     * LDAP 远程方法调用
     */
    public static final String LOOKUP_LDAP = "ldap:";

    /**
     * LDAPS 远程方法调用
     */
    public static final String LOOKUP_LDAPS = "ldaps:";

    /**
     * 自动识别json对象白名单配置（仅允许解析的包名，范围越小越安全）
     */
    public static final String[] JSON_WHITELIST_STR = {"org.springframework", "com.zkhf.epmis"};

    /**
     * 线程缓存登录信息
     */
    public static final String LOGIN_USER_TV = "LOGIN_USER";

    /**
     * 排口状态；1运行，2停止，3检修
     */
    public static final Integer OUT_PUT_STATUS_RUN = 1;

    /** 排口污染物开启的标识，0不启动、1启动*/
    public static final Integer POLL_CONF_ON = 1;

    /** 排口报警配置的启动标志，0不启动、1启动*/
    public static final Integer ALARM_CONF_OFF = 0;
    /** 排口报警配置的启动标志，0不启动、1启动*/
    public static final Integer ALARM_CONF_ON = 1;

    /** 报警状态，0未解除；1已解除 */
    public static final Integer ALARM_STATUS_ACTIVE = 0;
    /** 报警状态，0未解除；1已解除 */
    public static final Integer ALARM_STATUS_RESOLVED = 1;

    /** 处理状态，0未处理；1已处理 */
    public static final Integer DEAL_STATUS_PENDING = 0;
    /** 处理状态，0未处理；1已处理 */
    public static final Integer DEAL_STATUS_COMPLETED = 1;

    public static final String APPROVAL_FLOW_ENV_MANUAL_CHECK_PLAN = "envManualCheckPlan";
    public static final String APPROVAL_FLOW_ENV_MANUAL_CHECK_TASK = "envManualCheckTask";
    public static final String APPROVAL_FLOW_OPS_RECORD = "opsRecord";

    public static final String APPROVAL_FLOW_MATERIAL_APPLY = "material_apply";
    public static final String APPROVAL_FLOW_MATERIAL_IN = "material_in";
    public static final String APPROVAL_FLOW_MATERIAL_OUT = "material_out";
    public static final String APPROVAL_FLOW_MATERIAL_RETURN = "material_return";

}
