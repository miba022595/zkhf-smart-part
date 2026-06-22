package com.zkhf.epmis.platform.ent.service;

import com.zkhf.epmis.core.domain.AjaxResult;
import com.zkhf.epmis.core.domain.PollHead;
import com.zkhf.epmis.platform.ent.domain.EntAutoHead;
import com.zkhf.epmis.platform.ent.domain.EntOutputPollutant;

import java.util.List;
import java.util.Map;

/**
 * 企业排口污染物信息Service接口
 */
public interface EntOutputPollutantService {

    /**
     * 获取排口的污染物列表
     */
    List<Map<String, Object>> selectPollutantCodesByOutPutId(String outPutId);

    /**
     * 获取所有排口污染物信息
     */
    List<Map<String, Object>> listAll();

    /**
     * 通过企业排口id查询对应的污染物信息
     */
    List<EntOutputPollutant> selectOutputPollutantByOutPutId(String outPutId);

    /**
     * 查询基础信息--企业--废水排口污染物基本信息自动表头列表
     */
    List<EntAutoHead> selectAutoHead(String outPutId, String dataEnum);

    /**
     * 获取排口动态表头的图表列表
     */
    List<Map<String, Object>> autoHeadChart(String outPutId);

    /**
     * 排口自动表头列表-多排口
     * 取公共的
     */
    List<Map<String, Object>> multipleAutoHead(List<String> outPutIds, String dataEnum);

    /**
     * 排队动态表头
     */
    Map<String, List<PollHead>> multipleAutoHeads(List<String> outPutIds, String dataEnum);

    /**
     * 通过企业排口的污染物id查询对应的污染物信息
     */
    AjaxResult selectOutputPollutantById(String outPutPollId);

    /**
     * 新增企业排口污染物信息
     */
    AjaxResult insertOutputPollutant(EntOutputPollutant poll);

    /**
     * 修改企业排口污染物信息
     */
    AjaxResult updateOutputPollutant(EntOutputPollutant poll);

    /**
     * 删除企业排口污染物信息
     */
    AjaxResult deleteOutputPollutantById(String outPutPollId);
}
