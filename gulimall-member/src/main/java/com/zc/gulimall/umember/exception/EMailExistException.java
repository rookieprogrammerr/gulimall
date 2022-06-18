package com.zc.gulimall.umember.exception;

public class EMailExistException extends RuntimeException{
    public EMailExistException() {
        super("邮箱已存在");
    }
}
