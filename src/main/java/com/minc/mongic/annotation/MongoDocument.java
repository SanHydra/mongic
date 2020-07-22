package com.minc.mongic.annotation;

import com.minc.mongic.service.BaseEntity;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 使用在继承 BaseEntity 的实体上，指明其集合名称
 * @see BaseEntity
 * @author SanHydra
 * @date 2020/7/18 4:15 PM
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface MongoDocument {

    String value();
}
