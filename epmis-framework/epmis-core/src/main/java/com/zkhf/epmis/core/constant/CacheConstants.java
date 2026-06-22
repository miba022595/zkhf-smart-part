package com.zkhf.epmis.core.constant;

/**
 * 缓存的key 常量
 */
public class CacheConstants {
    /**
     * 登录用户 redis key
     */
    public static final String LOGIN_TOKEN_KEY = "login_tokens:";

    /**
     * 验证码 redis key
     */
    public static final String CAPTCHA_CODE_KEY = "captcha_codes:";

    /**
     * 参数管理 cache key
     */
    public static final String SYS_CONFIG_KEY = "sys_config:";

    /**
     * 字典管理 cache key
     */
    public static final String DICT_CACHE_KEY = "dict_cache:";

    /**
     * 自定义字典管理 cache key
     */
    public static final String DICT_CUSTOM_CACHE_KEY = "dict_custom_cache:";

    /**
     * 防重提交 redis key
     */
    public static final String REPEAT_SUBMIT_KEY = "repeat_submit:";

    /**
     * 限流 redis key
     */
    public static final String RATE_LIMIT_KEY = "rate_limit:";

    /**
     * 登录账户密码错误次数 redis key
     */
    public static final String PWD_ERR_CNT_KEY = "pwd_err_cnt:";

    /**
     * 企业微信AccessToken
     */
    public static final String WE_COM_ACCESS_TOKEN_KEY = "WE_COM_ACCESS_TOKEN_KEY:";

    /**
     * 应急通知AccessToken
     */
    public static final String EMERGENCY_ACCESS_TOKEN_KEY = "EMERGENCY_ACCESS_TOKEN_KEY:";

    /**
     * 数据管理 cache key
     */
    public static final String DATA_CACHE_KEY = "data_cache:";

}
