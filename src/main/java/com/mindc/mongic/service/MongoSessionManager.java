package com.mindc.mongic.service;

import com.mongodb.client.ClientSession;
import com.mongodb.client.MongoClient;

/**
 * @Title: MongoTransactionIntercepter.java
 * @Description: todo
 * @Author Hydra
 * @Date 2021/7/5 3:01 PM
 */

public class MongoSessionManager {

    private static MongoClient mongoClient;

    public void setMongoClient(MongoClient mongoClient) {
        MongoSessionManager.mongoClient = mongoClient;
    }

    private static ThreadLocal<ClientSession> sessionThreadLocal = new ThreadLocal<>();


    public static void startTransaction() {
        ClientSession clientSession = mongoClient.startSession();
        clientSession.startTransaction();
        sessionThreadLocal.set(clientSession);
    }

    public static void abortTransaction() {
        try (ClientSession clientSession = sessionThreadLocal.get()) {
            if (clientSession != null && clientSession.hasActiveTransaction()) {
                clientSession.abortTransaction();
            }
        } finally {
            sessionThreadLocal.remove();
        }
    }

    public static void commitTransaction() {
        try (ClientSession clientSession = sessionThreadLocal.get()) {
            if (clientSession != null && clientSession.hasActiveTransaction()) {
                clientSession.commitTransaction();
            }
        } finally {
            sessionThreadLocal.remove();
        }
    }
    public static ClientSession getClientSession(){
        return sessionThreadLocal.get();
    }

}
