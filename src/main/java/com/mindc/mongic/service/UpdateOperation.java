package com.mindc.mongic.service;

import com.mindc.mongic.utils.EntityUtils;
import org.bson.Document;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

/**
 * 更新集合操作
 *
 * @author SanHydra
 * @date 2020/7/20 3:57 PM
 */
public class UpdateOperation {

    private Document updateDocument = new Document();

    public static UpdateOperation create(){
        return new UpdateOperation();
    }


    /**
     * 累加
     * @param column 字段名
     * @param value 累加值
     * @return
     */
    public UpdateOperation inc(String column, Number value){
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
    public UpdateOperation push(String column, Object value){
        Document push = (Document) updateDocument.get("$push");
        if (push == null){
            push = new Document();
        }
        push.put(column,EntityUtils.parseObject(value));
        updateDocument.put("$push",push);
        return this;
    }

    public UpdateOperation pushMulti(String column, Collection value){
        Document push = (Document) updateDocument.get("$push");
        if (push == null){
            push = new Document();
        }
        Document each = new Document();
        Object o = EntityUtils.parseObject(value);
        each.put("$each",o);
        push.put(column, each);
        updateDocument.put("$push",push);
        return this;
    }

    /**
     * 列表添加元素 去重
     * @param column 列名
     * @param value 值
     * @return
     */
    public UpdateOperation addToSet(String column, Object value){
        Document addToSet = (Document) updateDocument.get("$addToSet");
        if (addToSet == null){
            addToSet = new Document();
        }
        addToSet.put(column,EntityUtils.parseObject(value));
        updateDocument.put("$addToSet",addToSet);
        return this;
    }

    public UpdateOperation addToSetMulti(String column, Collection value){
        Document push = (Document) updateDocument.get("$addToSet");
        if (push == null){
            push = new Document();
        }
        Document each = new Document();
        Object o = EntityUtils.parseObject(value);
        each.put("$each",o);
        push.put(column, each);
        updateDocument.put("$addToSet",push);
        return this;
    }

    /**
     * 列表删除元素
     * @param column 列名
     * @param values 元素数组
     * @return
     */
    public UpdateOperation pull(String column, Object ... values){
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
    public UpdateOperation pull(String column, QueryCondition pullCondition){
        Document pull = (Document) updateDocument.get("$pull");
        if (pull == null){
            pull = new Document();
        }
        pull.put(column,pullCondition.getQueryDocument());
        updateDocument.put("$pull",pull);
        return this;
    }

    /**
     * 常规设置值
     * @param column 列名
     * @param value 值
     * @return
     */
    public UpdateOperation set(String column, Object value){
        Document set = (Document) updateDocument.get("$set");
        if (set == null){
            set = new Document();
        }
        set.put(column,EntityUtils.parseObject(value));
        updateDocument.put("$set",set);
        return this;
    }

    public Document getUpdateDocument() {
        return updateDocument;
    }

}
