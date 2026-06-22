package com.zkhf.epmis.process.task.domain;

import lombok.Data;

import java.util.Map;

/**
 * 企业数据有效率校验字段表
 */
@Data
public class CheckFieldInfo {

    /**
     * 企业编码
     */
    private String entCode;

    /**
     * 有效率时使用，字段分组
     */
    private String groupType;

    /**
     * 污染物编码
     */
    private String pollutantCode;

    /**
     * 统计字段
     */
    private String statField;

    private String[] fields;
    private Map<String, Limit> fieldLimit;

    public static class Limit {
        private final double min;
        private final double max;

        public Limit(double min, double max) {
            this.min = min;
            this.max = max;
        }

        public double getMin() {
            return this.min;
        }

        public double getMax() {
            return this.max;
        }
    }
}