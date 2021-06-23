package com.mindc.mongic.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @Title: MongoOption.java
 * @Description: 字段操作
 * @Author Hydra
 * @Date 2021/5/19 9:52 AM
 */
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface MongoOption {
    /**
     * 是否加密
     * @return
     */
    boolean encrypt() default false;

    /**
     * 加密的秘钥
     * @return
     */
    String pwd() default "";

    /**
     * 加密数据前缀
     * @return
     */
    String encPrefix() default "";

    /**
     * 版本号
     * @return
     */
    String version() default "1";
}
