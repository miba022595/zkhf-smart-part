package com.zkhf.epmis.platform.exception;

/**
 * 文件名大小限制异常类
 */
public class FileSizeLimitException extends RuntimeException {
    private static final long serialVersionUID = 1L;

    /**
     * 错误提示
     */
    private String message;

    /**
     * 空构造方法，避免反序列化问题
     */
    public FileSizeLimitException() {
    }

    public FileSizeLimitException(long limit, long size) {
        this.message = "上传的文件大小超出限制的文件大小！" +
                " 允许(MB)：" + (limit / 1024 / 1024) +
                " 实际(MB)：" + (size / 1024 / 1024);
    }

    @Override
    public String getMessage() {
        return message;
    }

    public FileSizeLimitException setMessage(String message) {
        this.message = message;
        return this;
    }
}