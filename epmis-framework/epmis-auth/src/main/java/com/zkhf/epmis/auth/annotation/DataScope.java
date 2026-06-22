package com.zkhf.epmis.auth.annotation;

import java.lang.annotation.*;

/**
 * 数据权限过滤注解
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface DataScope {

    /**
     * 用户表的别名
     */
    String userAlias() default "";
}
