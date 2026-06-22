package com.zkhf.epmis.generator.domain;

import java.util.List;

/**
 * 业务表元数据
 */
public class GenTable {
    /**
     * 数据库表名
     */
    private String tableName;

    /**
     * 数据库表注释
     */
    private String tableComment;

    /**
     * 生成后的 Java 类名
     */
    private String className;

    /**
     * 生成代码使用的基础包名
     */
    private String packageName;

    /**
     * 业务名称，通常取表名最后一段
     */
    private String businessName;

    /**
     * 生成功能名称
     */
    private String functionName;

    /**
     * 生成人
     */
    private String functionAuthor;

    /**
     * 主键字段
     */
    private GenTableColumn pkColumn;

    /**
     * 全部字段定义
     */
    private List<GenTableColumn> columns;

    /**
     * 获取数据库表名
     */
    public String getTableName() {
        return tableName;
    }

    /**
     * 设置数据库表名
     */
    public void setTableName(String tableName) {
        this.tableName = tableName;
    }

    /**
     * 获取数据库表注释
     */
    public String getTableComment() {
        return tableComment;
    }

    /**
     * 设置数据库表注释
     */
    public void setTableComment(String tableComment) {
        this.tableComment = tableComment;
    }

    /**
     * 获取生成后的 Java 类名
     */
    public String getClassName() {
        return className;
    }

    /**
     * 设置生成后的 Java 类名
     */
    public void setClassName(String className) {
        this.className = className;
    }

    /**
     * 获取生成代码使用的基础包名
     */
    public String getPackageName() {
        return packageName;
    }

    /**
     * 设置生成代码使用的基础包名
     */
    public void setPackageName(String packageName) {
        this.packageName = packageName;
    }

    /**
     * 获取业务名称
     */
    public String getBusinessName() {
        return businessName;
    }

    /**
     * 设置业务名称
     */
    public void setBusinessName(String businessName) {
        this.businessName = businessName;
    }

    /**
     * 获取生成功能名称
     */
    public String getFunctionName() {
        return functionName;
    }

    /**
     * 设置生成功能名称
     */
    public void setFunctionName(String functionName) {
        this.functionName = functionName;
    }

    /**
     * 获取生成人
     */
    public String getFunctionAuthor() {
        return functionAuthor;
    }

    /**
     * 设置生成人
     */
    public void setFunctionAuthor(String functionAuthor) {
        this.functionAuthor = functionAuthor;
    }

    /**
     * 获取主键字段
     */
    public GenTableColumn getPkColumn() {
        return pkColumn;
    }

    /**
     * 设置主键字段
     */
    public void setPkColumn(GenTableColumn pkColumn) {
        this.pkColumn = pkColumn;
    }

    /**
     * 获取全部字段定义
     */
    public List<GenTableColumn> getColumns() {
        return columns;
    }

    /**
     * 设置全部字段定义
     */
    public void setColumns(List<GenTableColumn> columns) {
        this.columns = columns;
    }
}
