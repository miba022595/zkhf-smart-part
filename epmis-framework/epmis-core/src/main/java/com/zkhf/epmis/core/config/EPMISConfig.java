package com.zkhf.epmis.core.config;

import com.zkhf.epmis.core.utils.StringUtils;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * 读取项目相关配置
 */
@Component
@ConfigurationProperties(prefix = "epmis")
public class EPMISConfig {
    /**
     * 上传路径
     */
    private static String profile;
    /**
     * 获取地址开关
     */
    private static boolean addressEnabled;
    /**
     * 验证码类型
     */
    private static String captchaType;
    /**
     * 项目名称
     */
    private static String name;
    /**
     * 版本
     */
    private static String version;

    /**
     * 上传文件默认大小 120M
     */
    private static long maxFileSize = 120 * 1024 * 1024L;
    /**
     * 上传文件默认的文件名最大长度 100
     */
    private static int maxFileNameLength = 100;

    public static String getProfile() {
        return profile;
    }

    public static boolean isAddressEnabled() {
        return addressEnabled;
    }

    public static String getCaptchaType() {
        return captchaType;
    }

    /**
     * 获取导入上传路径
     */
    public static String getImportPath() {
        return getProfile() + "/import";
    }

    /**
     * 获取头像上传路径
     */
    public static String getAvatarPath() {
        return getProfile() + "/avatar";
    }

    /**
     * 获取附件上传路径
     */
    public static String getAnnexPath(String sourceType) {
        if (StringUtils.isEmpty(sourceType)) {
            return getProfile() + "/annex";
        }
        return getProfile() + "/annex/" + sourceType;
    }

    /**
     * 获取下载路径
     */
    public static String getDownloadPath() {
        return getProfile() + "/download/";
    }

    /**
     * 获取上传路径
     */
    public static String getUploadPath() {
        return getProfile() + "/upload";
    }

    public static String getName() {
        return name;
    }

    public static String getVersion() {
        return version;
    }

    public static long getMaxFileSize() {
        return maxFileSize;
    }

    public static int getMaxFileNameLength() {
        return maxFileNameLength;
    }

    public void setProfile(String profile) {
        EPMISConfig.profile = profile;
    }

    public void setAddressEnabled(boolean addressEnabled) {
        EPMISConfig.addressEnabled = addressEnabled;
    }

    public void setCaptchaType(String captchaType) {
        EPMISConfig.captchaType = captchaType;
    }

    public void setName(String name) {
        EPMISConfig.name = name;
    }

    public void setVersion(String version) {
        EPMISConfig.version = version;
    }

    public void setMaxFileSize(String maxFileSize) {
        if (null == maxFileSize) {
            return;
        }
        maxFileSize = maxFileSize.trim().toUpperCase();
        int len = maxFileSize.length();
        if (maxFileSize.endsWith("GB")) {
            EPMISConfig.maxFileSize = Integer.parseInt(maxFileSize.substring(0, len - 2)) * 1024 * 1024 * 1024;
        } else if (maxFileSize.endsWith("MB")) {
            EPMISConfig.maxFileSize = Integer.parseInt(maxFileSize.substring(0, len - 2)) * 1024 * 1024;
        } else if (maxFileSize.endsWith("KB")) {
            EPMISConfig.maxFileSize = Integer.parseInt(maxFileSize.substring(0, len - 2)) * 1024;
        } else if (maxFileSize.endsWith("B")) {
            EPMISConfig.maxFileSize = Integer.parseInt(maxFileSize.substring(0, len - 1));
        }
    }

    public void setMaxFileNameLength(int maxFileNameLength) {
        EPMISConfig.maxFileNameLength = maxFileNameLength;
    }
}
