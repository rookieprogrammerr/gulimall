package com.zc.common.exception;

/**
 * @author zc
 * @Data 2020/7/20
 */

/**
 * 错误码和错误信息自定义类
 * 1、错误码定义规则为5位数字
 * 2、前两位表示业务场景，最后三位表示错误码。例如：100001。 10：通用 001：系统未知异常
 * 3、维护错误码后需要维护错误描述，将他们定义为枚举形式
 * 错误码列表：
 *      10：通用
 *          001：参数格式校验
 *          002：短信验证码频率太高
 *      11：商品
 *      12：订单
 *      13：购物车
 *      14：物流
 *      15：用户
 *      21：库存
 */
public enum BizCodeEnum {
    UNKONW_EXCEPTON(10000, "系统未知异常"),
    VALID_EXCEPTION(10001, "参数格式校验失败"),
    VALID_SMS_CODE(10002, "验证码获取频率太高，请稍后重试"),
    PRODUCT_UP_EXCEPTION(11000, "商品上架异常"),
    USER_EXIST_EXCEPTION(15001, "用户存在"),
    PHONE_EXIST_EXCEPTION(15002, "手机号存在"),
    LOGINACCT_PASSWORD_INVAILD_EXCEPTION(15002, "账号密码错误"),
    NO_STOCK_EXCEPTION(21000, "商品库存不足");

    Integer code;
    String msg;

    BizCodeEnum(Integer code, String msg) {
        this.code = code;
        this.msg = msg;
    }

    public int getCode() {
        return code;
    }

    public String getMsg() {
        return msg;
    }
}
