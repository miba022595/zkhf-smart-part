package com.zkhf.epmis.core.utils;

/**
 * 数字工具类
 */
public class NumberUtils {

    /**
     * 使用Math.round动态保留小数位数
     * @param value 原始值
     * @param scale 小数位数
     * @return 处理后的double值
     */
    public static Double round(Double value, int scale) {
        if (null == value) {
            return null;
        }
        if (scale < 0) {
            throw new IllegalArgumentException("小数位数不能为负数");
        }
        double factor = Math.pow(10, scale);
        return Math.round(value * factor) / factor;
    }
}