package com.minc.mongic.service;

import org.bson.Document;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * 更新集合操作
 *
 * @author SanHydra
 * @date 2020/7/20 3:57 PM
 */
public class UpdateListOperation {

    private Document updateDocument = new Document();

    public static UpdateListOperation create(){
        return new UpdateListOperation();
    }

    /**
     * 累加
     * @param column 字段名
     * @param value 累加值
     * @return
     */
    public UpdateListOperation inc(String column, Number value){
        Document inc = (Document) updateDocument.get("$inc");
        if (inc == null){
            inc = new Document();
        }
        inc.put(column,value);
        updateDocument.put("$inc",inc);
        return this;
    }

    /**
     * 列表添加元素
     * @param column 列名
     * @param value 元素值
     * @return
     */
    public UpdateListOperation push(String column, Object value){
        Document push = (Document) updateDocument.get("$push");
        if (push == null){
            push = new Document();
        }
        push.put(column,value);
        updateDocument.put("$push",push);
        return this;
    }

    /**
     * 列表添加元素 去重
     * @param column 列名
     * @param value 值
     * @return
     */
    public UpdateListOperation addToSet(String column, Object value){
        Document addToSet = (Document) updateDocument.get("$addToSet");
        if (addToSet == null){
            addToSet = new Document();
        }
        addToSet.put(column,value);
        updateDocument.put("$addToSet",addToSet);
        return this;
    }

    /**
     * 列表删除元素
     * @param column 列名
     * @param values 元素数组
     * @return
     */
    public UpdateListOperation pull(String column, Object ... values){
        Document pull = (Document) updateDocument.get("$pull");
        if (pull == null){
            pull = new Document();
        }
        Document inDoc = (Document) pull.get(column);
        if(inDoc == null){
            inDoc = new Document();
        }
        Set<Object> set = (Set<Object>) inDoc.get("$in");
        if (set == null){
            set=  new HashSet<>();
        }
        set.addAll(Arrays.asList(values));
        inDoc.put("$in",set);
        pull.put(column,inDoc);
        updateDocument.put("$pull",pull);
        return this;
    }

    /**
     * 列表去除元素
     * @param column 列名
     * @param pullCondition 移除元素的条件
     * @return
     */
    public UpdateListOperation pull(String column, QueryCondition pullCondition){
        Document pull = (Document) updateDocument.get("$pull");
        if (pull == null){
            pull = new Document();
        }
        pull.put(column,pullCondition.getQueryDocument());
        updateDocument.put("$pull",pull);
        return this;
    }

    public Document getUpdateDocument() {
        return updateDocument;
    }
}
