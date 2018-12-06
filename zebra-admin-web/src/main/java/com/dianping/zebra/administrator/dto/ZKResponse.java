package com.dianping.zebra.administrator.dto;

/**
 * Created by taochen on 2018/11/4.
 */
public class ZKResponse {

    private String status;
    private Object result;
    private String message;

    public boolean isSuccess() {
        return "success".equalsIgnoreCase(status);
    }

    public boolean isExist() {
        return isSuccess() && result != null;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Object getResult() {
        return result;
    }

    public void setResult(Object result) {
        this.result = result;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    @Override
    public String toString() {
        return "ZKResponse{" +
                "status='" + status + '\'' +
                ", result='" + result + '\'' +
                ", message='" + message + '\'' +
                '}';
    }
}
