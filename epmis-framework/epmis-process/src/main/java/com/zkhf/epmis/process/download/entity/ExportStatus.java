package com.zkhf.epmis.process.download.entity;

public enum ExportStatus {
    /**
     * 待定
     */
    PENDING,
    /**
     * 正在处理
     */
    PROCESSING,
    /**
     * 已完成
     */
    COMPLETED,
    /**
     * 失败
     */
    FAILED,
    /**
     * 未找到
     */
    NOT_FOUND,
}