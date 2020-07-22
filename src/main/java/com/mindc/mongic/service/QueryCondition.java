package com.mindc.mongic.service;


import org.bson.Document;

import java.util.*;

/**
 * mongo查询条件组装
 *
 * @author SanHydra
 * @date 2020/7/18 10:35 AM
 */
public class QueryCondition {
    /**
     * 内部累map，存储的所有配置的条件
     */
    private Map<String, InnerCondition> innerConditions = new HashMap<>();

    /**
     * 排序字段列表
     */
    private List<Order> orderFields = new ArrayList<>();

    /**
     * 查询时跳过的数据数量
     */
    private int skip =0;

    /**
     * 查询限制数量
     */
    private int limit = Integer.MAX_VALUE;

    /**
     * 空条件，用于不需要条件的查询
     */
    private static  final QueryCondition EMPTY = new QueryCondition();

    /**
     * 指定需要的字段
     */
    private  Set<String> chooseColumns = new HashSet<>();
    /**
     * 指定返回值不要的字段
     */
    private  Set<String> unChooseColumns = new HashSet<>();

    /**
     * 内部独立query，与其他的条件冲突
     */
    private Document query;

    /**
     * 静态创建condition对象
     * @return
     */
    public static QueryCondition create() {
        return new QueryCondition();
    }

    /**
     * 获取空的条件对象
     * @return
     */
    public static QueryCondition empty() {
        return EMPTY;
    }

    /**
     * 显式设定query文档，一旦设定，其他查询参数将不会生效
     * @param query
     * @return
     */
    public QueryCondition query(Document query) {
        this.query = query;
        return this;
    }

    /**
     * 相等
     * @param con 是否生效
     * @param field 字段名
     * @param value 字段值
     * @return
     */
    public QueryCondition eq(boolean con, String field, Object value) {
        if (con) {
            return eq(field, value);
        }
        return this;
    }

    /**
     * 相等
     * @param field 字段名
     * @param value 字段值
     * @return
     */
    public QueryCondition eq(String field, Object value) {
        getC(field).addEq(value);
        return this;
    }

    /**
     * 不等
     * @param con 是否生效
     * @param field 字段名
     * @param value 字段值
     * @return
     */
    public QueryCondition ne(boolean con, String field, Object value) {
        if (con) {
            return ne(field, value);
        }
        return this;
    }

    /**
     * 不等
     * @param field 字段名
     * @param value 字段值
     * @return
     */
    public QueryCondition ne(String field, Object value) {
        getC(field).addNe(value);
        return this;
    }

    /**
     * 小于
     * @param con 是否生效
     * @param field 字段名
     * @param value 字段值
     * @return
     */
    public QueryCondition lt(boolean con, String field, Object value) {
        if (con) {
            return lt(field, value);
        }
        return this;
    }

    /**
     * 小于
     * @param field 字段名
     * @param value 字段值
     * @return
     */
    public QueryCondition lt(String field, Object value) {
        getC(field).addLt(value);
        return this;
    }

    /**
     * 小于等于
     * @param con 是否生效
     * @param field 字段名
     * @param value 字段值
     * @return
     */
    public QueryCondition lte(boolean con, String field, Object value) {
        if (con) {
            return lte(field, value);
        }
        return this;
    }

    /**
     * 小于等于
     * @param field 字段名
     * @param value 字段值
     * @return
     */
    public QueryCondition lte(String field, Object value) {
        getC(field).addLte(value);
        return this;
    }

    /**
     * 大于
     * @param con 是否生效
     * @param field 字段名
     * @param value 字段值
     * @return
     */
    public QueryCondition gt(boolean con, String field, Object value) {
        if (con) {
            return gt(field, value);
        }
        return this;
    }

    /**
     * 大于
     * @param field 字段名
     * @param value 字段值
     * @return
     */
    public QueryCondition gt(String field, Object value) {
        getC(field).addGt(value);
        return this;
    }

    /**
     * 大于等于
     * @param con 是否生效
     * @param field 字段名
     * @param value 字段值
     * @return
     */
    public QueryCondition gte(boolean con, String field, Object value) {
        if (con) {
            return gte(field, value);
        }
        return this;
    }

    /**
     * 大于等于
     * @param field 字段名
     * @param value 字段值
     * @return
     */
    public QueryCondition gte(String field, Object value) {
        getC(field).addGte(value);
        return this;
    }

    /**
     * 包含
     * @param con 是否生效
     * @param field 字段名
     * @param values 包含的字段集合
     * @return
     */
    public QueryCondition in(boolean con, String field, Collection values) {
        if (con) {
            return in(field, values);
        }
        return this;
    }

    /**
     * 包含
     * @param field 字段名
     * @param values 包含的字段集合
     * @return
     */
    public QueryCondition in(String field, Collection values) {
        if (values != null && values.size() > 0) {
            getC(field).addIn(values);
        }
        return this;
    }

    /**
     * 包含
     * @param con 是否生效
     * @param field 字段名
     * @param values 包含的字段集合
     * @return
     */
    public QueryCondition in(boolean con, String field, Object... values) {
        if (con) {
            return in(field, values);
        }
        return this;
    }

    /**
     * 包含
     * @param field 字段名
     * @param values 包含的字段集合
     * @return
     */
    public QueryCondition in(String field, Object... values) {
        if (values != null && values.length > 0) {
            getC(field).addIn(Arrays.asList(values));
        }
        return this;
    }

    /**
     * 不包含
     * @param con 是否生效
     * @param field 字段名
     * @param values 不包含的字段集合
     * @return
     */
    public QueryCondition notIn(boolean con, String field, Collection values) {
        if (con) {
            return notIn(field, values);
        }
        return this;
    }

    /**
     * 不包含
     * @param field 字段名
     * @param values 不包含的字段集合
     * @return
     */
    public QueryCondition notIn(String field, Collection values) {
        if (values != null && values.size() > 0) {
            getC(field).addNotIn(values);
        }
        return this;
    }

    /**
     * 不包含
     * @param con 是否生效
     * @param field 字段名
     * @param values 不包含的字段集合
     * @return
     */
    public QueryCondition notIn(boolean con, String field, Object... values) {
        if (con) {
            return notIn(field, values);
        }
        return this;
    }

    /**
     * 不包含
     * @param field 字段名
     * @param values 不包含的字段集合
     * @return
     */
    public QueryCondition notIn(String field, Object... values) {
        if (values != null && values.length > 0) {
            getC(field).addNotIn(Arrays.asList(values));
        }
        return this;
    }

    /**
     * 模糊搜索
     * @param con 是否生效
     * @param field 字段名
     * @param value 模糊搜索条件
     * @return
     */
    public QueryCondition like(boolean con, String field, String value) {
        if (con) {
            return like(field, value);
        }
        return this;
    }

    /**
     * 模糊搜索
     * @param field 字段名
     * @param value 模糊搜索条件
     * @return
     */
    public QueryCondition like(String field, String value) {
        if(value != null) {
            getC(field).addLike(value);
        }
        return this;
    }

    /**
     * 限制条数
     * @param limit 条数
     * @return
     */
    public QueryCondition limit(int limit){
        this.limit = limit;
        return this;
    }

    /**
     * 跳过数据条数
     * @param skip 数量
     * @return
     */
    public QueryCondition skip(int skip){
        this.skip = skip;
        return this;
    }

    /**
     * 排序方式
     * @param field 排序字段
     * @param asc true 顺序 false 倒序
     * @return
     */
    public QueryCondition orderBy(String field, boolean asc) {
        orderFields.add(new Order(asc, field));
        return this;
    }

    /**
     * 设置返回值包含的字段
     * @param columns 字段列表
     * @return
     */
    public QueryCondition columns(String ... columns){
        if (columns != null && columns.length > 0){
            this.chooseColumns.addAll(Arrays.asList(columns));
        }
        return this;
    }

    /**
     * 设置返回值包含的字段
     * @param columns 字段列表
     * @return
     */
    public QueryCondition columns(Collection<String> columns){
        if (columns != null && columns.size() > 0){
            this.chooseColumns.addAll(columns);
        }
        return this;
    }

    /**
     * 设置返回值包含的字段
     * @param column 字段
     * @return
     */
    public QueryCondition column(String column){
        if (column != null){
            this.chooseColumns.add(column);
        }
        return this;
    }

    /**
     * 设置返回值不包含的字段
     * @param columns 不包含的字段列表
     * @return
     */
    public QueryCondition exceptColumns(String ... columns){
        if (columns != null && columns.length > 0){
            this.unChooseColumns.addAll(Arrays.asList(columns));
        }
        return this;
    }

    /**
     * 设置返回值不包含的字段
     * @param columns 不包含字段列表
     * @return
     */
    public QueryCondition exceptColumns(Collection<String> columns){
        if (columns != null && columns.size() > 0){
            this.unChooseColumns.addAll(columns);
        }
        return this;
    }

    /**
     * 设置返回值不包含的字段
     * @param column 不包含字段
     * @return
     */
    public QueryCondition exceptColumn(String column){
        if (column != null){
            this.unChooseColumns.add(column);
        }
        return this;
    }

    /**
     * 根据字段获取内部条件对象
     * @param field 字段名
     * @return
     */
    private InnerCondition getC(String field) {
        InnerCondition innerCondition = innerConditions.get(field);
        if (innerCondition == null) {
            innerCondition = new InnerCondition();
            innerConditions.put(field, innerCondition);
        }
        return innerCondition;
    }

    /**
     * 获取排序文档
     * @return 排序文档
     */
    public Document getSortDocument(){
        Document sortDoc = new Document();
        if (orderFields.size() > 0){
            for (Order order : orderFields) {
                sortDoc.put(order.field,order.asc?1:-1);
            }
        }
        return sortDoc;
    }

    public int getSkip() {
        return skip;
    }

    public int getLimit() {
        return limit;
    }

    public Set<String> getChooseColumns() {
        return chooseColumns;
    }
    public Set<String> getUnChooseColumns() {
        return unChooseColumns;
    }

    /**
     * 获取projection 文档
     * @return
     */
    public Document getProjection(){
        if (chooseColumns.size() > 0 || unChooseColumns.size() > 0){
            Document document = new Document();
            for (String chooseColumn : chooseColumns) {
                document.put(chooseColumn,1);
            }
            for (String unChooseColumn : unChooseColumns) {
                document.put(unChooseColumn,0);
            }
            return document;
        }
        return null;
    }

    /**
     * 获取查询文档
     * @return
     */
    public Document getQueryDocument() {
        if (query != null) {
            return query;
        }
        //处理所有条件
        query = new Document();
        for (String field : innerConditions.keySet()) {
            Document criteria = new Document();

            InnerCondition c = innerConditions.get(field);

            List<BaseOperation> operations = c.operations;
            if (!operations.isEmpty()){
                for (BaseOperation operation : operations) {
                    criteria.put(operation.operateSymbol,operation.value);
                }
            }
            query.put(field,criteria);
        }
        return query;
    }

    /**
     * 正则标准字符转换
     * @param str
     * @return
     */
    private String toStandardRegex(String str) {
        return str.replace("\\", "\\\\").replace("*", "\\*")
                .replace("+", "\\+").replace("|", "\\|")
                .replace("{", "\\{").replace("}", "\\}")
                .replace("(", "\\(").replace(")", "\\)")
                .replace("^", "\\^").replace("$", "\\$")
                .replace("[", "\\[").replace("]", "\\]")
                .replace("?", "\\?").replace(",", "\\,")
                .replace(".", "\\.").replace("&", "\\&");
    }

    /**
     * 排序简易实体
     */
    private class Order {
        private boolean asc;
        private String field;

        Order(boolean asc, String field) {
            this.asc = asc;
            this.field = field;
        }
    }


    private class InnerCondition {
        List<BaseOperation> operations = new ArrayList<>();

        void addEq(Object value) {
            operations.add(new BaseOperation("$eq",value));
        }

        void addNe(Object value) {
            operations.add(new BaseOperation("$ne",value));
        }

        void addLt(Object value) {
            operations.add(new BaseOperation("$lt",value));
        }

        void addLte(Object value) {
            operations.add(new BaseOperation("$lte",value));
        }

        void addGt(Object value) {
            operations.add(new BaseOperation("$gt",value));
        }

        void addGte(Object value) {
            operations.add(new BaseOperation("$gte",value));
        }

        void addIn(Collection value) {
            operations.add(new BaseOperation("$in",value));
        }

        void addNotIn(Collection value) {
            operations.add(new BaseOperation("$nin",value));
        }

        void addLike(String value) {
            String reg = ".*" + toStandardRegex(value) + ".*";
            operations.add(new BaseOperation("$regex",reg));
        }

    }


    private class BaseOperation {
        private String operateSymbol;
        private Object value;

        private BaseOperation(String operateSymbol, Object value) {
            this.operateSymbol = operateSymbol;
            this.value = value;
        }
    }

}
