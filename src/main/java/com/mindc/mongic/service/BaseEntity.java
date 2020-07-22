package com.mindc.mongic.service;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;


/**
 * @author SanHydra
 * @date 2020/7/185:25 PM
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class BaseEntity{

    private String id;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }
}
