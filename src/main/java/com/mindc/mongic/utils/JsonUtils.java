package com.mindc.mongic.utils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mindc.mongic.exception.MongicException;
import com.mindc.mongic.service.BaseEntity;

import java.util.List;
import java.util.Map;

/**
 * @author SanHydra
 * @date 2020/7/22 11:05 AM
 */
public class JsonUtils {

    private static ObjectMapper objectMapper = new ObjectMapper();

    public static String toJson(Object obj){
        try {
            return objectMapper.writeValueAsString(obj);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
        return null;
    }
    public static  <T> T  fromJson(String json,Class<T> cls){
        try {
           return objectMapper.readValue(json,cls);
        } catch (JsonProcessingException e) {
            throw new MongicException("json parse error source : "+json);
        }
    }

    public static <T> List<T> fromJsonToList(String json,Class<T> cls){
        JavaType javaType = objectMapper.getTypeFactory().constructParametricType(List.class, cls);
        try {
            return objectMapper.readValue(json, javaType);
        } catch (JsonProcessingException e) {
            throw new MongicException("json parse error source : "+json);
        }
    }
    public static  Map  fromJsonToMap(String json){

        return fromJson(json,Map.class);
    }

    public static List fromJsonToList(String json){

        return fromJson(json,List.class);
    }

    public static void main(String[] args) {
        List<BaseEntity> baseEntities = fromJsonToList("[{\"id\":\"3\"}]", BaseEntity.class);
        for (BaseEntity baseEntity : baseEntities) {
            System.out.println(baseEntity.getId());
        }
    }
}
