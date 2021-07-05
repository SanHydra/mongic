package com.mindc.mongic.config;

import com.mindc.mongic.service.MongoSessionManager;
import com.mongodb.client.MongoClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

/**
 * @author sanHydra
 * @date 2020/7/22 8:25 PM
 */
@Configuration
@ComponentScan({"com.mindc.mongic"})
public class MongicConfig {


    @Bean
    public MongoSessionManager mongoSessionManager(MongoClient mongoClient){
        MongoSessionManager mongoSessionManager = new MongoSessionManager();
        mongoSessionManager.setMongoClient(mongoClient);
        return mongoSessionManager;
    }
}
