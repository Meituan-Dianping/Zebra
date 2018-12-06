package com.dianping.zebra.administrator.dto;

/**
 * @author Created by tong.xin on 18/1/19.
 */
public class ResultDto<T> {
    String status;

    String message;

    T result;

    public ResultDto() {

    }

    public ResultDto(String status, String message) {
        this.status = status;
        this.message = message;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public T getResult() {
        return result;
    }

    public void setResult(T result) {
        this.result = result;
    }
}
