package com.zkhf.epmis.platform.mapper.envProtect;

import com.zkhf.epmis.platform.envProtect.domain.EntOutPollutantPermit;
import com.zkhf.epmis.platform.envProtect.domain.EntOutPollutantPermitCount;
import com.zkhf.epmis.platform.envProtect.domain.EntOutPollutantPermitCountReq;
import com.zkhf.epmis.platform.envProtect.domain.EntOutPollutantPermitReq;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

/**
 * 企业排污许可基础Mapper接口
 */
public interface EntOutPollutantPermitMapper {

    /**
     * 查询指定年份的企业排污许可总量
     */
    List<Map<String, Object>> selectAllEntOutPollutantPermitCount(@Param("permitYear") Integer permitYear);

    /**
     * 查询企业排污许可基础列表
     */
    List<EntOutPollutantPermit> selectEntOutPollutantPermitList(EntOutPollutantPermitReq req);

    /**
     * 判断是否已存在
     */
    int checkExistEntOutPollutantPermit(@Param("entCode") String entCode);

    /**
     * 新增企业排污许可基础
     */
    void insertEntOutPollutantPermit(EntOutPollutantPermit permit);

    /**
     * 修改企业排污许可基础
     */
    void updateEntOutPollutantPermit(EntOutPollutantPermit permit);

    /**
     * 查询企业排污许可总量基础列表
     */
    List<EntOutPollutantPermitCount> selectEntOutPollutantPermitCountList(EntOutPollutantPermitCountReq count);

    /**
     * 判断是否已存在排污许可总量基础
     */
    int existsEntOutPollutantPermitCount(EntOutPollutantPermitCount count);

    /**
     * 新增企业排污许可总量基础
     */
    void insertEntOutPollutantPermitCount(EntOutPollutantPermitCount count);

    /**
     * 修改企业排污许可总量基础
     */
    void updateEntOutPollutantPermitCount(EntOutPollutantPermitCount count);

    /**
     * 批量删除企业排污许可总量基础
     */
    void deleteEntOutPollutantPermitCountByPollPermitCountIds(List<String> pollPermitCountIds);
}
