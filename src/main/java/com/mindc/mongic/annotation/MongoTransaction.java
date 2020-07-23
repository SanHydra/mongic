package com.mindc.mongic.annotation;

import com.mindc.mongic.exception.MongicException;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @author sanHydra
 * @date 2020/7/22 6:50 PM
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface MongoTransaction {

    Class rollbackFor() default Exception.class;
}
