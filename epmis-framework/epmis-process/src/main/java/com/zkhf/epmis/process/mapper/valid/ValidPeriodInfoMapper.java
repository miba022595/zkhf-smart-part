package com.zkhf.epmis.process.mapper.valid;

import com.zkhf.epmis.process.valid.domain.ValidPeriodInfo;
import com.zkhf.epmis.process.valid.domain.ValidPeriodReq;

import java.util.List;

/**
 * 企业资质有效期预警数据Mapper接口
 */
public interface ValidPeriodInfoMapper {

    /**
     * 查询企业资质有效期预警数据列表
     */
    List<ValidPeriodInfo> selectValidPeriodInfoList(ValidPeriodReq req);
}
