package com.zkhf.epmis.platform.ent.domain;

import lombok.Data;

import java.util.List;

/**
 * 企业生产车间对象 t_ent_produce_workshop
 */
@Data
public class EntProduceWorkshopReq {

    /** 所属企业 */
    private String entCode;
    private List<String> entCodes;

    /** 生产车间名称 */
    private String workshopName;

    /** 生产车间编号 */
    private String workshopCode;

}
