package com.mindc.mongic.exception;

/**
 * 通用异常
 * @author SanHydra
 * @date 2020/7/21 10:27 AM
 */
public class MongicException extends RuntimeException{
    public MongicException(String message) {
        super(message);
    }
}
