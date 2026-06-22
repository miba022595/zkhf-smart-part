package com.zkhf.epmis.process.mapper.statistics;

import com.zkhf.epmis.process.statistics.domain.EffectiveTransInfo;
import com.zkhf.epmis.process.statistics.domain.EffectiveTransReq;

import java.util.List;

/**
 * 企业数据有效率信息统计Mapper接口
 */
public interface EffectiveTransMapper {

    /**
     * 查询企业数据有效率信息统计列表
     */
    List<EffectiveTransInfo> selectEffectiveTransList(EffectiveTransReq req);
}
