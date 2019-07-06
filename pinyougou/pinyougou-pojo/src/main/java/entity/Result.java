package entity;

import java.io.Serializable;
import java.util.List;

/**
 * 返回结果
 */
public class Result implements Serializable {

    private boolean success;//是否成功
    private String message;//返回信息
    private List<Long> haveChild;

    public Result(boolean success, String message, List<Long> haveChild) {
        this.success = success;
        this.message = message;
        this.haveChild = haveChild;
    }

    public Result(boolean success, String message) {
        this.success = success;
        this.message = message;
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public List<Long> getHaveChild() {
        return haveChild;
    }

    public void setHaveChild(List<Long> haveChild) {
        this.haveChild = haveChild;
    }
}
