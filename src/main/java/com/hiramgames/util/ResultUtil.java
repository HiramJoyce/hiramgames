package com.hiramgames.util;


import com.hiramgames.domain.Result;
import com.hiramgames.domain.enums.ResultEnum;

/**
 * @author caohailiang
 * 返回结果工具类
 */
public class ResultUtil {

    public static Result<?> success(Object object) {
        Result<Object> result = new Result<Object>();
        result.setCode(0);
        result.setMsg("成功");
        result.setData(object);
        return result;
    }

    public static Result<?> success(String msg, Object object) {
        Result<Object> result = new Result<Object>();
        result.setCode(0);
        result.setMsg(msg);
        result.setData(object);
        return result;
    }

    public static Result<?> success() {
        return success(null);
    }

    public static Result<?> error(ResultEnum resultEnum) {
        Result<?> result = new Result<Object>();
        result.setCode(resultEnum.getCode());
        result.setMsg(resultEnum.getMsg());
        return result;
    }

    public static Result<?> error(ResultEnum resultEnum, Object data) {
        Result<Object> result = new Result<Object>();
        result.setCode(resultEnum.getCode());
        result.setMsg(resultEnum.getMsg());
        result.setData(data);
        return result;
    }

    public static Result<?> error(Integer code, String msg) {
        Result<?> result = new Result<Object>();
        result.setCode(code);
        result.setMsg(msg);
        return result;
    }

    public static Result<?> error(Integer code, String msg, Object data) {
        Result<Object> result = new Result<Object>();
        result.setCode(code);
        result.setMsg(msg);
        result.setData(data);
        return result;
    }
}
