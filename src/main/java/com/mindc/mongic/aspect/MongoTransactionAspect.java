package com.mindc.mongic.aspect;


import com.mindc.mongic.annotation.MongoTransaction;
import com.mindc.mongic.service.BaseServiceImpl;
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
 *@author sanHydra
 *@date 2020/7/22 6:58 PM     
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
        if (!enableTransaction){
            return joinPoint.proceed();
        }
        MethodInvocationProceedingJoinPoint point = (MethodInvocationProceedingJoinPoint) joinPoint;

        Object thisObj = point.getTarget();

        MethodSignature signature = (MethodSignature) point.getSignature();

        if (thisObj instanceof BaseServiceImpl) {

            Class exceptionClass = Exception.class;


            BaseServiceImpl service = (BaseServiceImpl) thisObj;

            Method method = signature.getMethod();

            MongoTransaction annotation = method.getAnnotation(MongoTransaction.class);
            if (annotation != null) {
                exceptionClass = annotation.rollbackFor();
                String value = annotation.value();

            }
            //开启事务
            service.startTransaction();
            Object result = null;
            //执行方法
            try {
                result = point.proceed();

            } catch (Throwable e) {
                //事务异常回滚
                if (e.getClass().isAssignableFrom(exceptionClass)) {
                    //是回滚异常
                    service.abortTransaction();
                    throw e;
                }
            }
            //事务完成，提交
            service.commitTransaction();
            return result;
        } else {
            //非继承 baseService的类无法使用mongo事务
            return point.proceed();
        }
    }
}
