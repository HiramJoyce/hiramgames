package com.hiramgames.domain.enums;

/**
 * @author caohailiang
 * 返回结果类型枚举
 * code规则：成功为1，失败为0，后续改为不同状态码对应不同的提示
 */
public enum  ResultEnum {
    SUCCESS(0, "成功"),
    ERROR(-1, "失败"),
    NOT_LOGIN(-1, "未登录"),
    LOGIN_EXPIRY(-1, "登录失效，请重新登陆"),
    PERMISSION_DENIED(-1, "没有权限"),
    NO_DATA(-1, "暂无数据"),
    OPERATION_NOT_PERMITTED(-1, "操作受限"),
    OPERATION_TOO_FREQUENTLY(-1, "操作频繁，请五秒后尝试")
    ;

    private Integer code;
    private String msg;

    ResultEnum(Integer code, String msg) {
        this.code = code;
        this.msg = msg;
    }

    public Integer getCode() {
        return code;
    }

    public String getMsg() {
        return msg;
    }
}
