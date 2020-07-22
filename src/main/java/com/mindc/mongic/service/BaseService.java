package com.mindc.mongic.service;

import org.bson.Document;

import java.util.List;
import java.util.Map;

/**
 * mongo base service
 *
 * @author SanHydra
 * @date 2020/7/18 10:33 AM
 */
public interface BaseService<T extends BaseEntity> {

    /**
     * insert one record
     * @param t  the record
     * @return  the record with complete
     */
    T insert(T t);

    /**
     * 插入多条记录
     * @param t  记录列表
     * @return  添加了uuid的记录列表
     */
    List<T> insertBatch(List<T> t);

    /**
     * 根据id查询记录
     * @param id  记录id
     * @return  记录
     */
    T selectById(String id);

    /**
     * 根据条件查询记录
     * @param condition 条件
     * @return 记录列表
     */
    List<T> selectList(QueryCondition condition);

    /**
     * 查询记录条数
     * @param condition 条件
     * @return count of records
     */
    long selectCount(QueryCondition condition);

    /**
     * 查询一条记录 default the first index of records
     * @param condition 条件
     * @return 记录
     */
    T selectOne(QueryCondition condition);

    /**
     * 查询分页列表
     * @param mongoPage 分页参数
     * @param condition 条件
     * @return 返回值
     */
    MongoPage selectPage(MongoPage mongoPage, QueryCondition condition);


    /**
     * 根据mongo的id更新非空字段，排除uuid
     * @param t
     * @return
     */
    long updateById(T t);


    /**
     * 根据id 更新所有字段 排除uuid
     * @param t 实体
     * @return 更新成功条数
     */
    long updateAllColumnById(T t);
    /**
     * 更新实体 非空字段
     * @param t 实体
     * @param condition 条件
     * @return 更新成功条数
     */
    long update(T t, QueryCondition condition);

    /**
     * 集合更新
     * @param update 集合更新内容
     * @param condition 条件
     * @return 更新成功条数
     */
    long update(UpdateListOperation update, QueryCondition condition);

    /**
     * 根据条件 更新所有字段
     * @param t 更新内容
     * @param condition 更新查询条件
     * @return 更新成功数量
     */
    long updateAllColumn(T t, QueryCondition condition);

    /**
     * 通用更新方法
     * @param updateDocument 更新文档
     * @param condition 更新条件
     * @param multi 是否多条更新
     * @return
     */
    long update(Document updateDocument, QueryCondition condition, boolean multi);

    /**
     * 删除
     * @param condition 删除条件
     * @return
     */
    long remove(QueryCondition condition);

    /**
     * 根据id删除记录
     * @param id mongo唯一主键
     * @return
     */
    long removeById(String id);

    /**
     * 聚合分组查询
     * @param queryCondition match 参数 + sort参数 +skip+limit参数
     * @param groupCondition group 参数
     * @param clazz 返回值映射实体class
     * @param <K> 返回值映射实体
     * @return 聚合结果
     */
    <K> List<K> aggregate(QueryCondition queryCondition, AggregateCondition groupCondition, Class<K> clazz);

    /**
     * 分组查询并分页
     * @param page 分页参数
     * @param queryCondition match 参数 + sort参数
     * @param groupCondition group 参数
     * @param clazz 返回值映射实体class
     * @param <K>
     * @return
     */
    <K> MongoPage<K> aggregatePage(MongoPage page, QueryCondition queryCondition, AggregateCondition groupCondition, Class<K> clazz);

    /**
     * 聚合分组查询
     * @param queryCondition match 参数 + sort参数 +skip+limit参数
     * @param groupCondition group 参数
     * @return 聚合结果
     */
    List<Map> aggregate(QueryCondition queryCondition, AggregateCondition groupCondition);

}
