package com.mindc.mongic.aspect;


import com.mindc.mongic.annotation.MongoTransaction;
import com.mindc.mongic.service.BaseServiceImpl;
import com.mindc.mongic.service.MongoSessionManager;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.aop.aspectj.MethodInvocationProceedingJoinPoint;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;

/**
 * @author sanHydra
 * @date 2020/7/22 6:58 PM
 */
@Component
@Aspect
public class MongoTransactionAspect {

    @Value("${spring.data.mongodb.enable-transaction:false}")
    private Boolean enableTransaction;


    @Pointcut("@annotation(com.mindc.mongic.annotation.MongoTransaction)")
    public void logPointCut() {

    }

    @Around("logPointCut()")
    public Object around(ProceedingJoinPoint joinPoint) throws Throwable {
        if (!enableTransaction) {
            return joinPoint.proceed();
        }
        MethodInvocationProceedingJoinPoint point = (MethodInvocationProceedingJoinPoint) joinPoint;

        MethodSignature signature = (MethodSignature) point.getSignature();


        Class exceptionClass = Exception.class;

        Method method = signature.getMethod();

        MongoTransaction annotation = method.getAnnotation(MongoTransaction.class);
        if (annotation != null) {
            exceptionClass = annotation.rollbackFor();
        }
        //开启事务
        MongoSessionManager.startTransaction();
        Object result = null;
        //执行方法
        try {
            result = point.proceed();

        } catch (Throwable e) {
            //事务异常回滚
            if (exceptionClass.isAssignableFrom(e.getClass())) {
                //是回滚异常
                MongoSessionManager.abortTransaction();
                throw e;
            }
        }
        //事务完成，提交
        MongoSessionManager.commitTransaction();
        return result;

    }
}
