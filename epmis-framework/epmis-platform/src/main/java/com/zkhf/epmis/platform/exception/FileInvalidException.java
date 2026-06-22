package com.zkhf.epmis.platform.exception;

import java.util.Arrays;

/**
 * 文件上传 误异常类
 */
public class FileInvalidException extends RuntimeException {
    private static final long serialVersionUID = 1L;

    /**
     * 错误提示
     */
    private String message;

    /**
     * 空构造方法，避免反序列化问题
     */
    public FileInvalidException() {
    }

    public FileInvalidException(String filename, String extension, String[] allowedExtension) {
        this.message = "文件[" + filename + "]后缀[" + extension + "]不正确，请上传" + Arrays.toString(allowedExtension) + "格式";
    }

    @Override
    public String getMessage() {
        return message;
    }

    public FileInvalidException setMessage(String message) {
        this.message = message;
        return this;
    }
}