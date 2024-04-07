package star.api.exception;

import star.api.common.ErrorCode;

import java.io.Serializable;

/**
 * 自定义异常类
 *
 */
public class BusinessException extends RuntimeException implements Serializable {

    private static final long serialVersionUID = -2027177485939495762L;
    /**
     * 错误码
     */
    private int code;

    public BusinessException() {
    }

    public BusinessException(int code, String message) {
        super(message);
        this.code = code;
    }

    public BusinessException(ErrorCode errorCode) {
        super(errorCode.getMessage());
        this.code = errorCode.getCode();
    }

    public BusinessException(ErrorCode errorCode, String message) {
        super(message);
        this.code = errorCode.getCode();
    }

    public int getCode() {
        return code;
    }
}
