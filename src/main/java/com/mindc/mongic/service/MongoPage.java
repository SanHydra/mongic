package com.mindc.mongic.service;

import java.util.List;

/**
 * @author SanHydra
 * @date 2020/7/18 10:40 AM
 */
public class MongoPage<T> {
    private int current;
    private int size;
    private Long total;
    private Long pages;
    private List<T> records;

    public MongoPage() {
        this.current = 1;
        this.size = 10;
    }

    public MongoPage(int current, int size) {
        this.current = current;
        this.size = size;
    }

    public int getSkip(){
        return size * (current - 1);
    }

    public int getCurrent() {
        return current;
    }

    public void setCurrent(int current) {
        this.current = current;
    }

    public int getSize() {
        return size;
    }

    public void setSize(int size) {
        this.size = size;
    }

    public Long getTotal() {
        return total;
    }

    public MongoPage<T> setTotal(Long total) {
        this.total = total;
        long i = total / size;
        this.pages = total % size == 0?i:i+1;
        return this;
    }

    public Long getPages() {
        return pages;
    }

    public List<T> getRecords() {
        return records;
    }

    public MongoPage<T> setRecords(List<T> records) {
        this.records = records;
        return this;
    }
}
