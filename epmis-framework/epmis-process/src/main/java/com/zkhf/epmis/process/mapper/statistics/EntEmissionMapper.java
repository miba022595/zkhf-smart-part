package com.zkhf.epmis.process.mapper.statistics;

import com.zkhf.epmis.process.statistics.domain.EmissionReq;
import com.zkhf.epmis.process.statistics.domain.EntEmission;
import com.zkhf.epmis.process.statistics.domain.OutEmission;

import java.util.List;

/**
 * 企业年排量信息记录Mapper接口
 */
public interface EntEmissionMapper {

    /**
     * 查询企业年排量信息记录列表
     */
    List<EntEmission> selectEntEmissionList(EmissionReq req);

    /**
     * 查询排口年排量信息记录列表
     */
    List<OutEmission> selectOutEmissionList(EmissionReq req);

}