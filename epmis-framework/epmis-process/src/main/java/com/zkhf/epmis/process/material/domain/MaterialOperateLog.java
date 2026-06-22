package com.zkhf.epmis.process.material.domain;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 物资操作日志对象
 */
@Data
public class MaterialOperateLog {

    /** 日志ID */
    private Long operId;

    /** 模块标题 */
    private String title;

    /** 业务类型 */
    private Integer businessType;

    /** 操作人 */
    private String operName;

    /** 请求参数 */
    private String operParam;

    /** 返回结果 */
    private String jsonResult;

    /** 状态 */
    private Integer status;

    /** 错误消息 */
    private String errorMsg;

    /** 操作时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime operTime;

    /** 耗时（毫秒） */
    private Long costTime;
}
