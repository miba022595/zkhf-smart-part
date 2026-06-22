package com.zkhf.epmis.platform.ent.domain;

import com.zkhf.epmis.core.enums.OutPutTypeEnum;
import lombok.Data;

import java.util.List;

/**
 * 企业排口对象 t_bas_ent_out_put_info
 */
@Data
public class EntOutPutReq {

    /**
     * 权限管理
     */
    private String entCode;
    private List<String> entCodes;

    /**
     * 企业名称(模糊)
     */
    private String entName;

    /**
     * 排放口名称(模糊)
     */
    private String outPutName;

    /**
     * 排放口类型
     * 参见 {@link OutPutTypeEnum}
     */
    private Integer outPutType;

    /**
     * 排放口状态，字典out_put_status
     */
    private Integer outPutStatus;

    /**
     * 排放口设备mn号(模糊)
     */
    private String mnNum;
}
