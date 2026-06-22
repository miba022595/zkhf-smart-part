package com.zkhf.epmis.generator.util;

import com.zkhf.epmis.generator.constant.GenConstants;
import com.zkhf.epmis.generator.domain.GenTable;
import com.zkhf.epmis.generator.domain.GenTableColumn;
import org.apache.velocity.VelocityContext;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

/**
 * Velocity 模板与上下文处理工具。
 */
public class VelocityUtils {

    /**
     * Java 文件相对输出目录
     */
    private static final String JAVA_PATH = "main/java";
    /**
     * Mapper XML 相对输出目录
     */
    private static final String MYBATIS_PATH = "main/resources/mapper";
    private static final String DOMAIN_TEMPLATE = """
            package ${packageName}.domain;
            
            import lombok.Data;
            #foreach($import in $importList)
            import $import;
            #end
            
            /**
             * ${functionName}
             *
             * @author ${author}
             * @date ${datetime}
             */
            @Data
            public class ${ClassName} {
            
            #foreach ($column in $columns)
                /** ${column.columnComment} */
            #if($column.javaType == 'Date')
                @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
            #end
                private ${column.javaType} ${column.javaField};
            
            #end
            }
            """;
    private static final String REQ_TEMPLATE = """
            package ${packageName}.domain;
            
            import lombok.Data;
            
            /**
             * ${functionName}查询对象
             *
             * @author ${author}
             * @date ${datetime}
             */
            @Data
            public class ${ClassName}Req {
            
            #foreach ($column in $columns)
                #if($column.query)
                /** ${column.columnComment} */
                private ${column.javaType} ${column.javaField};
            
                #end
            #end
            }
            """;
    private static final String MAPPER_TEMPLATE = """
            package ${packageName}.mapper;
            
            import ${packageName}.domain.${ClassName};
            import ${packageName}.domain.${ClassName}Req;
            import java.util.List;
            
            /**
             * ${functionName} Mapper
             *
             * @author ${author}
             * @date ${datetime}
             */
            public interface ${ClassName}Mapper {
            
                /**
                 * 查询${functionName}列表
                 */
                List<${ClassName}> select${ClassName}List(${ClassName}Req req);
            
                /**
                 * 查询${functionName}详情
                 */
                ${ClassName} select${ClassName}Info(${ClassName} info);
            
                /**
                 * 新增${functionName}
                 */
                int insert${ClassName}(${ClassName} info);
            
                /**
                 * 修改${functionName}
                 */
                int update${ClassName}(${ClassName} info);
            
                /**
                 * 删除${functionName}
                 */
                int delete${ClassName}(${ClassName} info);
            }
            """;
    private static final String SERVICE_TEMPLATE = """
            package ${packageName}.service;
            
            import ${packageName}.domain.${ClassName};
            import ${packageName}.domain.${ClassName}Req;
            import com.zkhf.epmis.core.domain.AjaxResult;
            import jakarta.servlet.http.HttpServletResponse;
            
            /**
             * ${functionName} Service
             *
             * @author ${author}
             * @date ${datetime}
             */
            public interface ${ClassName}Service {
            
                /**
                 * 查询${functionName}列表
                 */
                AjaxResult select${ClassName}List(${ClassName}Req req);
            
                /**
                 * 导出${functionName}列表
                 */
                void export${ClassName}(${ClassName}Req req, HttpServletResponse response);
            
                /**
                 * 查询${functionName}详情
                 */
                AjaxResult select${ClassName}Info(${ClassName} info);
            
                /**
                 * 新增${functionName}
                 */
                AjaxResult insert${ClassName}(${ClassName} info);
            
                /**
                 * 修改${functionName}
                 */
                AjaxResult update${ClassName}(${ClassName} info);
            
                /**
                 * 删除${functionName}
                 */
                AjaxResult delete${ClassName}(${ClassName} info);
            }
            """;
    private static final String SERVICE_IMPL_TEMPLATE = """
            package ${packageName}.service.impl;
            
            import ${packageName}.domain.${ClassName};
            import ${packageName}.domain.${ClassName}Req;
            import ${packageName}.mapper.${ClassName}Mapper;
            import ${packageName}.service.${ClassName}Service;
            import com.zkhf.epmis.core.domain.AjaxResult;
            import jakarta.servlet.http.HttpServletResponse;
            import org.springframework.beans.factory.annotation.Autowired;
            import org.springframework.stereotype.Service;
            
            /**
             * ${functionName} Service 实现
             *
             * @author ${author}
             * @date ${datetime}
             */
            @Service
            public class ${ClassName}ServiceImpl implements ${ClassName}Service {
            
                private ${ClassName}Mapper ${className}Mapper;
                @Autowired
                public void set${ClassName}Mapper(${ClassName}Mapper ${className}Mapper) {
                    this.${className}Mapper = ${className}Mapper;
                }
            
                @Override
                public AjaxResult select${ClassName}List(${ClassName}Req req) {
                    if (req == null) {
                        req = new ${ClassName}Req();
                    }
                    return AjaxResult.success(${className}Mapper.select${ClassName}List(req));
                }
            
                @Override
                public void export${ClassName}(${ClassName}Req req, HttpServletResponse response) {
                }
            
                @Override
                public AjaxResult select${ClassName}Info(${ClassName} info) {
                    if (info == null) {
                        return AjaxResult.success();
                    }
                    return AjaxResult.success(${className}Mapper.select${ClassName}Info(info));
                }
            
                @Override
                public AjaxResult insert${ClassName}(${ClassName} info) {
                    if (info == null) {
                        return AjaxResult.success(0);
                    }
                    return AjaxResult.success(${className}Mapper.insert${ClassName}(info));
                }
            
                @Override
                public AjaxResult update${ClassName}(${ClassName} info) {
                    if (info == null) {
                        return AjaxResult.success(0);
                    }
                    return AjaxResult.success(${className}Mapper.update${ClassName}(info));
                }
            
                @Override
                public AjaxResult delete${ClassName}(${ClassName} info) {
                    if (info == null) {
                        return AjaxResult.success(0);
                    }
                    return AjaxResult.success(${className}Mapper.delete${ClassName}(info));
                }
            }
            """;
    private static final String CONTROLLER_TEMPLATE = """
            package ${packageName}.controller;
            
            import ${packageName}.domain.${ClassName};
            import ${packageName}.domain.${ClassName}Req;
            import ${packageName}.service.${ClassName}Service;
            import com.zkhf.epmis.core.domain.AjaxResult;
            import jakarta.servlet.http.HttpServletResponse;
            import org.springframework.beans.factory.annotation.Autowired;
            import org.springframework.web.bind.annotation.PostMapping;
            import org.springframework.web.bind.annotation.RequestBody;
            import org.springframework.web.bind.annotation.RequestMapping;
            import org.springframework.web.bind.annotation.RestController;
            
            /**
             * ${functionName} Controller
             *
             * @author ${author}
             * @date ${datetime}
             */
            @RestController
            @RequestMapping("/platform/${businessName}")
            public class ${ClassName}Controller {
            
                private ${ClassName}Service ${className}Service;
                @Autowired
                public void set${ClassName}Service(${ClassName}Service ${className}Service) {
                    this.${className}Service = ${className}Service;
                }
            
                /**
                 * 查询${functionName}列表
                 */
                @PostMapping("/list")
                public AjaxResult list(@RequestBody(required = false) ${ClassName}Req req) {
                    return ${className}Service.select${ClassName}List(req);
                }
            
                /**
                 * 导出${functionName}列表
                 */
                @PostMapping("/export")
                public void export(@RequestBody(required = false) ${ClassName}Req req, HttpServletResponse response) {
                    ${className}Service.export${ClassName}(req, response);
                }
            
                /**
                 * 查询${functionName}详情
                 */
                @PostMapping("/info")
                public AjaxResult getInfo(@RequestBody(required = false) ${ClassName} info) {
                    return ${className}Service.select${ClassName}Info(info);
                }
            
                /**
                 * 新增${functionName}
                 */
                @PostMapping("/add")
                public AjaxResult add(@RequestBody(required = false) ${ClassName} info) {
                    return ${className}Service.insert${ClassName}(info);
                }
            
                /**
                 * 修改${functionName}
                 */
                @PostMapping("/edit")
                public AjaxResult edit(@RequestBody(required = false) ${ClassName} info) {
                    return ${className}Service.update${ClassName}(info);
                }
            
                /**
                 * 删除${functionName}
                 */
                @PostMapping("/remove")
                public AjaxResult remove(@RequestBody(required = false) ${ClassName} info) {
                    return ${className}Service.delete${ClassName}(info);
                }
            }
            """;
    private static final String MAPPER_XML_TEMPLATE = """
            <?xml version="1.0" encoding="UTF-8" ?>
            <!DOCTYPE mapper
                    PUBLIC "-//mybatis.org//DTD Mapper 3.0//EN"
                    "http://mybatis.org/dtd/mybatis-3-mapper.dtd">
            <mapper namespace="${packageName}.mapper.${ClassName}Mapper">
            
                <resultMap id="${ClassName}Result" type="${packageName}.domain.${ClassName}">
                #foreach ($column in $columns)
                    <result property="${column.javaField}" column="${column.columnName}"/>
                #end
                </resultMap>
            
                <sql id="select${ClassName}Vo">
                    select#foreach($column in $columns) $column.columnName#if($foreach.count != $columns.size()),#end#end from ${tableName}
                </sql>
            
                <select id="select${ClassName}List" parameterType="${packageName}.domain.${ClassName}Req" resultMap="${ClassName}Result">
                    <include refid="select${ClassName}Vo"/>
                    <where>
            #foreach($column in $columns)
                #if($column.query)
                    #if($column.queryType == "LIKE")
                        <if test="${column.javaField} != null and ${column.javaField}.trim() != ''">
                            and ${column.columnName} like concat('%', #{${column.javaField}}, '%')
                        </if>
                    #else
                        <if test="${column.javaField} != null#if($column.javaType == 'String') and ${column.javaField}.trim() != ''#end">
                            and ${column.columnName} = #{${column.javaField}}
                        </if>
                    #end
                #end
            #end
                    </where>
                    order by ${pkColumn.columnName} desc
                </select>
            
                <select id="select${ClassName}Info" parameterType="${packageName}.domain.${ClassName}"
                        resultMap="${ClassName}Result">
                    <include refid="select${ClassName}Vo"/>
                    where ${pkColumn.columnName} = #{${pkColumn.javaField}}
                </select>
            
                <insert id="insert${ClassName}" parameterType="${packageName}.domain.${ClassName}"#if($pkColumn.increment) useGeneratedKeys="true"
                        keyProperty="$pkColumn.javaField"#end>
                    insert into ${tableName}
                    <trim prefix="(" suffix=")" suffixOverrides=",">
                        #foreach($column in $columns)
                            #if($column.columnName != $pkColumn.columnName || !$pkColumn.increment)
                        <if test="$column.javaField != null#if($column.javaType == 'String' && $column.required) and $column.javaField != ''#end">$column.columnName,</if>
                            #end
                        #end
                    </trim>
                    <trim prefix="values (" suffix=")" suffixOverrides=",">
                        #foreach($column in $columns)
                            #if($column.columnName != $pkColumn.columnName || !$pkColumn.increment)
                        <if test="$column.javaField != null#if($column.javaType == 'String' && $column.required) and $column.javaField != ''#end">#{$column.javaField},</if>
                            #end
                        #end
                    </trim>
                </insert>
            
                <update id="update${ClassName}" parameterType="${packageName}.domain.${ClassName}">
                    update ${tableName}
                    <trim prefix="SET" suffixOverrides=",">
                        #foreach($column in $columns)
                            #if($column.columnName != $pkColumn.columnName)
                        <if test="$column.javaField != null#if($column.javaType == 'String' && $column.required) and $column.javaField != ''#end">$column.columnName = #{$column.javaField},</if>
                            #end
                        #end
                    </trim>
                    where ${pkColumn.columnName} = #{${pkColumn.javaField}}
                </update>
            
                <delete id="delete${ClassName}" parameterType="${packageName}.domain.${ClassName}">
                    delete from ${tableName} where ${pkColumn.columnName} = #{${pkColumn.javaField}}
                </delete>
            </mapper>
            """;

    /**
     * 构建模板渲染上下文。
     */
    public static VelocityContext prepareContext(GenTable genTable) {
        String packageName = genTable.getPackageName();
        String functionName = genTable.getFunctionName();

        VelocityContext velocityContext = new VelocityContext();
        velocityContext.put("tableName", genTable.getTableName());
        velocityContext.put("functionName", StringUtils.isNotEmpty(functionName) ? functionName : "【请填写功能名称】");
        velocityContext.put("ClassName", genTable.getClassName());
        velocityContext.put("className", StringUtils.uncapitalize(genTable.getClassName()));
        velocityContext.put("businessName", genTable.getBusinessName());
        velocityContext.put("packageName", packageName);
        velocityContext.put("author", genTable.getFunctionAuthor());
        velocityContext.put("datetime", StringUtils.today());
        velocityContext.put("pkColumn", genTable.getPkColumn());
        velocityContext.put("importList", getImportList(genTable));
        velocityContext.put("columns", genTable.getColumns());
        return velocityContext;
    }

    /**
     * 获取需要输出的模板集合。
     */
    public static List<TemplateMeta> getTemplateList() {
        List<TemplateMeta> templates = new ArrayList<>();
        templates.add(new TemplateMeta("domainJava", DOMAIN_TEMPLATE));
        templates.add(new TemplateMeta("reqJava", REQ_TEMPLATE));
        templates.add(new TemplateMeta("mapperJava", MAPPER_TEMPLATE));
        templates.add(new TemplateMeta("serviceJava", SERVICE_TEMPLATE));
        templates.add(new TemplateMeta("serviceImplJava", SERVICE_IMPL_TEMPLATE));
        templates.add(new TemplateMeta("controllerJava", CONTROLLER_TEMPLATE));
        templates.add(new TemplateMeta("mapperXml", MAPPER_XML_TEMPLATE));
        return templates;
    }

    /**
     * 计算模板对应的输出文件路径。
     */
    public static String getFileName(TemplateMeta template, GenTable genTable) {
        String fileName = "";
        String packageName = genTable.getPackageName();
        String className = genTable.getClassName();
        String javaPath = JAVA_PATH + "/" + StringUtils.replace(packageName, ".", "/");

        if ("domainJava".equals(template.name())) {
            fileName = StringUtils.format("{}/domain/{}.java", javaPath, className);
        } else if ("reqJava".equals(template.name())) {
            fileName = StringUtils.format("{}/domain/{}Req.java", javaPath, className);
        } else if ("mapperJava".equals(template.name())) {
            fileName = StringUtils.format("{}/mapper/{}Mapper.java", javaPath, className);
        } else if ("serviceJava".equals(template.name())) {
            fileName = StringUtils.format("{}/service/{}Service.java", javaPath, className);
        } else if ("serviceImplJava".equals(template.name())) {
            fileName = StringUtils.format("{}/service/impl/{}ServiceImpl.java", javaPath, className);
        } else if ("controllerJava".equals(template.name())) {
            fileName = StringUtils.format("{}/controller/{}Controller.java", javaPath, className);
        } else if ("mapperXml".equals(template.name())) {
            fileName = StringUtils.format("{}/{}Mapper.xml", MYBATIS_PATH, className);
        }
        return fileName;
    }

    /**
     * 根据字段类型收集生成实体所需 import。
     */
    public static HashSet<String> getImportList(GenTable genTable) {
        List<GenTableColumn> columns = genTable.getColumns();
        HashSet<String> importList = new HashSet<>();
        for (GenTableColumn column : columns) {
            if (!column.isSuperColumn() && GenConstants.TYPE_DATE.equals(column.getJavaType())) {
                importList.add("java.util.Date");
                importList.add("com.fasterxml.jackson.annotation.JsonFormat");
            } else if (!column.isSuperColumn() && GenConstants.TYPE_BIGDECIMAL.equals(column.getJavaType())) {
                importList.add("java.math.BigDecimal");
            }
        }
        return importList;
    }

    /**
     * 模板名称与模板内容定义。
     */
    public record TemplateMeta(String name, String content) {
    }
}
