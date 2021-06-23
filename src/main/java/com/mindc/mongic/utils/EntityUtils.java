/*
 * 文 件 名:  EntityUtils.java
 * 版    权:  JobWen Technologies Co., Ltd. Copyright 2017-2020,  All rights reserved
 * 描    述:
 * 创建人  :  Aries
 * 创建时间:  2020年02月12日
 *
 */

package com.mindc.mongic.utils;

import com.mindc.mongic.annotation.MongoOption;
import org.bson.Document;
import org.bson.types.ObjectId;
import org.springframework.util.StringUtils;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

public class EntityUtils {

    /**
     * 转换实体为document
     * @param object 实体
     * @return document对象
     */
    public static Document toDocument(Object object) {
        if (object == null){
            return null;
        }
        //加密-
        Field[] declaredFields = object.getClass().getDeclaredFields();
        for (Field declaredField : declaredFields) {
            if(!declaredField.getType().equals(String.class)){
                continue;
            }
            if (declaredField.isAnnotationPresent(MongoOption.class)){
                MongoOption annotation = declaredField.getAnnotation(MongoOption.class);
                if (annotation.encrypt() && !StringUtils.isEmpty(annotation.pwd())){
                    String version = annotation.version();
                    //加密数据处理
                    String prefix = annotation.encPrefix();
                    declaredField.setAccessible(true);

                    String realPrefix = version+"_"+prefix;
                    //判断字段值是否以该前缀打头
                    try {
                        //必须是string才可以解密
                        String originData = (String) declaredField.get(object);
                        if (originData != null){

                            String result = AESUtil.encode(originData, annotation.pwd());
                            declaredField.set(object,realPrefix+result);
                        }

                    } catch (IllegalAccessException e) {
                        e.printStackTrace();
                    }
                }

            }
        }
        return Document.parse(JsonUtils.toJson(object));
    }

    /**
     * 解析document 为实体
     * @param object 文档
     * @param clazz 实体类class
     * @param <T>
     * @return
     */
    public static <T> T fromDocument(Document object, Class<T> clazz) {
        if (object == null){
            return null;
        }
        Object id = object.get("_id");
        if (id instanceof ObjectId) {
            object.put("id", ((ObjectId)id).toHexString());
        }

        T t = JsonUtils.fromJson(object.toJson(), clazz);
        //查询mongo option 并处理数据
        Field[] declaredFields = clazz.getDeclaredFields();
        for (Field declaredField : declaredFields) {
            if(!declaredField.getType().equals(String.class)){
                continue;
            }
           if (declaredField.isAnnotationPresent(MongoOption.class)){
               MongoOption annotation = declaredField.getAnnotation(MongoOption.class);
               if (annotation.encrypt() && !StringUtils.isEmpty(annotation.pwd())){
                   String version = annotation.version();
                   //加密数据处理
                   String prefix = annotation.encPrefix();
                   declaredField.setAccessible(true);

                   String realPrefix = version+"_"+prefix;
                   //判断字段值是否以该前缀打头
                   try {
                       //必须是string才可以解密
                       String encData = (String) declaredField.get(t);
                       if (encData != null && encData.startsWith(realPrefix)){
                           encData = encData.replaceAll(realPrefix,"");
                           String result = AESUtil.decode(encData, annotation.pwd());
                           declaredField.set(t,result);
                       }

                   } catch (IllegalAccessException e) {
                       e.printStackTrace();
                   }
               }

           }
        }
        return t;
    }

    public static Object parseObject(Object object){
        if (object == null){
            return null;
        }else if (object instanceof Number){
            return object;
        }else if (object instanceof String){
            return object;
        }else if (object instanceof Boolean){
            return object;
        }else if (object instanceof Collection){
            Iterator iterator = ((Collection) object).iterator();
            List<Object> list = new ArrayList<>();
            while (iterator.hasNext()){
                Object next = iterator.next();
                Object o = parseObject(next);
                list.add(o);
            }
            return list;
        }
        return toDocument(object);
    }
}
