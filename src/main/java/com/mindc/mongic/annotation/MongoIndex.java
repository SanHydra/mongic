package com.mindc.mongic.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 索引注解，使用后将自动创建索引
 * @author SanHydra
 * @date 2020/7/21 1:33 PM
 */
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface MongoIndex {

    String value() default "";
    boolean unique() default false;
    boolean sparse() default false;
    boolean background() default false;
}
