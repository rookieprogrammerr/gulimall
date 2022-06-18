package com.zc.gulimall.umember.exception;

public class UserNameExistException extends RuntimeException {
    public UserNameExistException() {
        super("用户名已存在");
    }
}
