package com.zkhf.epmis.process.solidWaste.domain;

import lombok.Data;

import java.util.List;

/**
 * 固废间管理查询对象
 */
@Data
public class WasteRoomReq {

    /** 固废间主键id */
    private String roomId;

    /** 归属企业 */
    private String entCode;
    private List<String> entCodes;

    /** 关联排口 */
    private String outPutId;

    /** 贮存间类型 */
    private String roomType;

    /** 废物类型 */
    private String wasteType;
}
