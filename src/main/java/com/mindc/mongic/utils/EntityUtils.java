/*
 * 文 件 名:  EntityUtils.java
 * 版    权:  JobWen Technologies Co., Ltd. Copyright 2017-2020,  All rights reserved
 * 描    述:
 * 创建人  :  Aries
 * 创建时间:  2020年02月12日
 *
 */

package com.mindc.mongic.utils;

import org.bson.Document;
import org.bson.types.ObjectId;

/**
 * @author SanHydra
 * @date 2020-07-20
 */
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

        return JsonUtils.fromJson(JsonUtils.toJson(object), clazz);
    }

}
