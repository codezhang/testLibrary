package org.example.core;

public class KernelResp<T> {

    private Integer code;
    private String msg;
    private T data;

    public KernelResp() {
    }

    public KernelResp(Integer code, String msg, T data) {
        this.code = code;
        this.msg = msg;
        this.data = data;
    }

    public static <T> KernelResp<T> ok(T data) {
        return new KernelResp<>(0, "ok", data);
    }

    public static <T> KernelResp<T> error(int code, String msg) {
        return new KernelResp<>(code, msg, null);
    }

    public Integer getCode() {
        return code;
    }

    public void setCode(Integer code) {
        this.code = code;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }
}
