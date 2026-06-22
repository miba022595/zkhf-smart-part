package com.zkhf.epmis.platform.mapper.ent;

import com.zkhf.epmis.platform.ent.domain.EntAutoHead;
import com.zkhf.epmis.platform.ent.domain.EntOutputPollutant;
import org.apache.ibatis.annotations.Param;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * 企业排口污染物信息Mapper接口
 */
public interface EntOutputPollutantMapper {

    /**
     * 获取排口的污染物列表
     */
    List<Map<String, Object>> selectPollutantCodesByOutPutId(@Param("outPutId") String outPutId);

    /**
     * 查询所有排口污染物列表
     */
    List<Map<String, Object>> listAll();

    /**
     * 查询企业排口污染物信息
     */
    List<EntOutputPollutant> selectOutputPollutantByOutPutId(@Param("outPutId") String outPutId);

    /**
     * 查询企业排口污染物信息
     */
    EntOutputPollutant selectOutputPollutantById(@Param("outPutPollId") String outPutPollId);

    /**
     * 查询基础信息--企业--废水排口污染物基本信息自动表头列表
     */
    List<EntAutoHead> selectAutoHead(@Param("outPutId") String outPutId);

    /**
     * 获取排口动态表头的图表列表
     */
    List<Map<String, Object>> autoHeadChart(@Param("outPutId") String outPutId);

    /**
     * 获取多个排口动态表头
     */
    List<Map<String, Object>> multipleAutoHead(@Param("outPutIds") List<String> outPutIds);

    /**
     * 新增企业排口污染物信息
     */
    int insertOutputPollutant(EntOutputPollutant poll);

    /**
     * 修改企业排口污染物信息
     */
    int updateOutputPollutant(EntOutputPollutant poll);

    /**
     * 删除企业排口污染物信息
     */
    int deleteOutputPollutantById(@Param("outPutPollId") String outPutPollId);

    /**
     * 修改排口信息中的污染物列表
     */
    void updateOutPutPollCodeById(@Param("outPutId") String outPutId);

    /**
     * 获取排污许可配置的污染物年排放量数值
     */
    BigDecimal selectEntOutPollutantPermitCount(@Param("outPutId") String outPutId, @Param("pollCode") String pollCode, @Param("year") Integer year);

    /**
     * 获取排口对应企业的月排放量限值配置
     */
    List<String> selectEntOutPollutantMonthlyLimit(@Param("outPutId") String outPutId, @Param("pollCode") String pollCode);
}
