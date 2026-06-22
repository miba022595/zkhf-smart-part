package com.zkhf.epmis.platform.envProtect.domain;

import lombok.Data;

import java.util.List;

/**
 * 企业排污许可基础请求对象 t_bas_ent_out_pollutant_permit
 */
@Data
public class EntOutPollutantPermitReq {

    /**
     * 企业名称，模糊
     */
    private String entName;

    /**
     * 许可证编号，模糊
     */
    private String permitNum;

    /**
     * 权限管理
     */
    private String entCode;
    private List<String> entCodes;

}
