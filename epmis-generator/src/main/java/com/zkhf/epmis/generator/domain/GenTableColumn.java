package com.zkhf.epmis.generator.domain;

import com.zkhf.epmis.generator.util.StringUtils;

/**
 * 业务表字段元数据
 */
public class GenTableColumn {
    /**
     * 数据库字段名
     */
    private String columnName;

    /**
     * 数据库字段注释
     */
    private String columnComment;

    /**
     * 数据库字段类型
     */
    private String columnType;

    /**
     * 生成后的 Java 类型
     */
    private String javaType;

    /**
     * 生成后的 Java 属性名
     */
    private String javaField;

    /**
     * 是否主键，1 表示是
     */
    private String isPk;

    /**
     * 是否自增，1 表示是
     */
    private String isIncrement;

    /**
     * 是否必填，1 表示是
     */
    private String isRequired;

    /**
     * 是否作为查询字段，1 表示是
     */
    private String isQuery;

    /**
     * 查询类型，例如 EQ、LIKE
     */
    private String queryType;

    /**
     * 设置数据库字段名
     */
    public void setColumnName(String columnName) {
        this.columnName = columnName;
    }

    /**
     * 获取数据库字段名
     */
    public String getColumnName() {
        return columnName;
    }

    /**
     * 设置数据库字段注释
     */
    public void setColumnComment(String columnComment) {
        this.columnComment = columnComment;
    }

    /**
     * 获取数据库字段注释
     */
    public String getColumnComment() {
        return columnComment;
    }

    /**
     * 设置数据库字段类型
     */
    public void setColumnType(String columnType) {
        this.columnType = columnType;
    }

    /**
     * 获取数据库字段类型
     */
    public String getColumnType() {
        return columnType;
    }

    /**
     * 设置生成后的 Java 类型
     */
    public void setJavaType(String javaType) {
        this.javaType = javaType;
    }

    /**
     * 获取生成后的 Java 类型
     */
    public String getJavaType() {
        return javaType;
    }

    /**
     * 设置生成后的 Java 属性名
     */
    public void setJavaField(String javaField) {
        this.javaField = javaField;
    }

    /**
     * 获取生成后的 Java 属性名
     */
    public String getJavaField() {
        return javaField;
    }

    /**
     * 获取首字母大写的 Java 属性名
     */
    public String getCapJavaField() {
        return StringUtils.capitalize(javaField);
    }

    /**
     * 设置是否主键
     */
    public void setIsPk(String isPk) {
        this.isPk = isPk;
    }

    /**
     * 获取是否主键标记
     */
    public String getIsPk() {
        return isPk;
    }

    /**
     * 当前字段是否主键
     */
    public boolean isPk() {
        return "1".equals(isPk);
    }

    /**
     * 获取是否自增标记
     */
    public String getIsIncrement() {
        return isIncrement;
    }

    /**
     * 设置是否自增
     */
    public void setIsIncrement(String isIncrement) {
        this.isIncrement = isIncrement;
    }

    /**
     * 当前字段是否自增
     */
    public boolean isIncrement() {
        return "1".equals(isIncrement);
    }

    /**
     * 设置是否必填
     */
    public void setIsRequired(String isRequired) {
        this.isRequired = isRequired;
    }

    /**
     * 获取是否必填标记
     */
    public String getIsRequired() {
        return isRequired;
    }

    /**
     * 当前字段是否必填
     */
    public boolean isRequired() {
        return "1".equals(isRequired);
    }

    /**
     * 设置是否作为查询字段
     */
    public void setIsQuery(String isQuery) {
        this.isQuery = isQuery;
    }

    /**
     * 获取是否作为查询字段标记
     */
    public String getIsQuery() {
        return isQuery;
    }

    /**
     * 当前字段是否参与查询
     */
    public boolean isQuery() {
        return "1".equals(isQuery);
    }

    /**
     * 设置查询类型
     */
    public void setQueryType(String queryType) {
        this.queryType = queryType;
    }

    /**
     * 获取查询类型
     */
    public String getQueryType() {
        return queryType;
    }

    /**
     * 是否为公共基类字段
     */
    public boolean isSuperColumn() {
        return StringUtils.equalsAnyIgnoreCase(javaField, "createBy", "createTime", "updateBy", "updateTime", "remark");
    }
}
