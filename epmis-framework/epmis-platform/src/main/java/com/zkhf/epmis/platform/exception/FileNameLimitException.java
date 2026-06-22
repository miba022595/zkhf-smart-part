package com.zkhf.epmis.platform.exception;

/**
 * 文件名称超长限制异常类
 */
public class FileNameLimitException extends RuntimeException {
    private static final long serialVersionUID = 1L;

    /**
     * 错误提示
     */
    private String message;

    /**
     * 空构造方法，避免反序列化问题
     */
    public FileNameLimitException() {
    }

    public FileNameLimitException(int limit, long size) {
        this.message = "上传的文件名最长字符" +
                " 允许：" + limit +
                " 实际：" + size;
    }

    @Override
    public String getMessage() {
        return message;
    }

    public FileNameLimitException setMessage(String message) {
        this.message = message;
        return this;
    }
}