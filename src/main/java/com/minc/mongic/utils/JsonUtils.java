package com.minc.mongic.utils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.minc.mongic.exception.MongicException;

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
    public static  Map  fromJsonToMap(String json){

        return fromJson(json,Map.class);
    }

    public static List fromJsonToList(String json){

        return fromJson(json,List.class);
    }
}
