package com.zkhf.epmis.process.statistics.domain;

import lombok.Data;

import java.util.List;

/**
 * 年排量信息请求
 */
@Data
public class EmissionReq {

    /**
     * 所属企业-权限
     */
    private String entCode;
    private List<String> entCodes;

    /**
     * 排放年份
     */
    private Integer emissionYear;

    /**
     * 污染物编码
     */
    private String pollutantCode;
    private List<String> pollutantCodeList;
}
