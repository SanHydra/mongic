package com.mindc.mongic.service;

import org.bson.Document;

import java.util.HashMap;
import java.util.Map;

/**
 * 聚合的group 封装实体
 * @author lxf
 * @date 2020/7/18 6:18 PM
 */
public class AggregateCondition {

    private static final String OPER_SUM = "$sum";
    private static final String OPER_MAX = "$max";
    private static final String OPER_MIN = "$min";
    private static final String OPER_AVG = "$avg";
    private static final String OPER_COUNT = "$count";
    private static final String OPER_ADD_TO_SET = "$addToSet";
    private static final String OPER_PUSH = "$push";

    /**
     * 进行分组的字段
     */
    private String groupByColumn;
    /**
     * 操作文档
     */
    private Map<String,Map<String,Object>> aggs = new HashMap<>();

    public String getGroupByColumn() {
        return groupByColumn;
    }

    public static AggregateCondition create(){
        return new AggregateCondition();
    }

    /**
     * 分组
     * @param field 分组的字段名
     * @return
     */
    public AggregateCondition groupBy(String field){
        this.groupByColumn = field;
        return this;
    }

    /**
     * 计数
     * @param resultField 结果字段名
     * @return
     */
    public AggregateCondition count(String resultField){
        return oper(null,resultField,OPER_COUNT);
    }

    /**
     * 求和
     * @param field 字段名/结果字段名
     * @return
     */
    public AggregateCondition sum(String field){
        return oper(field,field,OPER_SUM);
    }

    /**
     * 求和
     * @param field 字段名
     * @param resultField 结果字段名
     * @return
     */
    public AggregateCondition sum(String field,String resultField){
        return oper(field,resultField,OPER_SUM);
    }
    /**
     * 最大值
     * @param field 字段名/结果字段名
     * @return
     */
    public AggregateCondition max(String field){
        return oper(field,field,OPER_MAX);
    }
    /**
     * 最大值
     * @param field 字段名
     * @param resultField 结果字段名
     * @return
     */
    public AggregateCondition max(String field,String resultField){
        return oper(field,resultField,OPER_MAX);
    }

    /**
     * 最小值
     * @param field 字段名
     * @param resultField 结果字段名
     * @return
     */
    public AggregateCondition min(String field,String resultField){
        return oper(field,resultField,OPER_MIN);
    }

    /**
     * 平均值
     * @param field 字段名
     * @param resultField 结果字段名
     * @return
     */
    public AggregateCondition avg(String field,String resultField){
        return oper(field,resultField,OPER_AVG);
    }

    public AggregateCondition addToSet(String field,String resultField){
        return oper(field,resultField,OPER_ADD_TO_SET);
    }
    public AggregateCondition push(String field,String resultField){
        return oper(field,resultField,OPER_PUSH);
    }
    /**
     * 通用操作新增
     * @param field 字段名
     * @param resultField 返回字段吗
     * @param oper 操作符
     * @return
     */
    public AggregateCondition oper(String field,String resultField,String oper){
        Map<String,Object> map = new HashMap<>(1);
        if (oper.equalsIgnoreCase(OPER_COUNT)){
            map.put(OPER_SUM, 1);
        }else {
            map.put(oper, "$" + field);
        }
        aggs.put(resultField,map);
        return this;
    }

    public Document getDocument(){
        Document document = new Document();
        document.put("_id","$"+ groupByColumn);
        document.putAll(aggs);
        return document;
    }

}
