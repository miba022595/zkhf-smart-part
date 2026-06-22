package com.zkhf.epmis.process.homePage.domain;

import com.zkhf.epmis.core.enums.OutPutTypeEnum;
import lombok.Builder;
import lombok.Data;

import java.util.List;

/**
 * 首页排放量信息
 */
@Data
@Builder
public class HomePageEmissions {

    /**
     * 企业排口主键id
     */
    private String outPutId;

    /**
     * 企业编码
     */
    private String entCode;

    /**
     * 企业名称
     */
    private String entName;

    /**
     * 排口编码
     */
    private String outPutCode;

    /**
     * 排口名称
     */
    private String outPutName;

    /**
     * 设备mn号
     */
    private String mnNUm;

    /**
     * 排放口类型
     * 参见 {@link OutPutTypeEnum}
     */
    private Integer outPutType;

    /**
     * 污染物因子数据
     */
    private List<HomePageEmissionsPoll> pollList;
}
