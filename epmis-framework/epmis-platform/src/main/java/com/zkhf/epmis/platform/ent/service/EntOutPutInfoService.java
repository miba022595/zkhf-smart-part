package com.zkhf.epmis.platform.ent.service;

import com.zkhf.epmis.core.domain.AjaxResult;
import com.zkhf.epmis.platform.ent.domain.OutPutAlarmConf;
import com.zkhf.epmis.platform.ent.domain.EntOutPutInfo;
import com.zkhf.epmis.platform.ent.domain.EntOutPutPosition;
import com.zkhf.epmis.platform.ent.domain.EntOutPutReq;

import jakarta.servlet.http.HttpServletResponse;
import java.util.List;
import java.util.Map;

/**
 * 企业排口Service接口
 */
public interface EntOutPutInfoService {

    /**
     * 获取所有排口信息列表
     */
    List<Map<String, Object>> listAll();

    /**
     * 查询排口信息列表(包含排口位置)
     */
    List<EntOutPutPosition> listAllPosition();

    /**
     * 企业下排口，按排口类型分类
     */
    AjaxResult typeTreeWithEnt(Integer[] outPutTypes);

    /**
     * 依据排口类型查询企业下的排口
     */
    List<Map<String, Object>> listByEnterType(String entCode, Integer outPutType);

    /**
     * 查询企业排口详情
     */
    AjaxResult selectOutPutById(String outPutId);

    /**
     * 查询企业排口列表
     */
    AjaxResult selectOutPutList(EntOutPutReq req);

    /**
     * 导出企业企业排口列表
     * 按模板导出
     */
    void exportOutPut(EntOutPutReq req, HttpServletResponse response);

    /**
     * 新增企业排口
     */
    AjaxResult insertOutPut(EntOutPutInfo info);

    /**
     * 修改企业排口
     */
    AjaxResult updateOutPut(EntOutPutInfo info);

    /**
     * 删除企业排口
     */
    AjaxResult deleteOutPutById(String outPutId);

    /**
     * 用户关注排口
     */
    AjaxResult userAttentionAdd(String outPutId);

    /**
     * 用户取消关注排口
     */
    AjaxResult userAttentionDel(String outPutId);

    /**
     * 查询用户收藏关注排口信息列表
     */
    List<Map<String, Object>> userAttentionList(Long userId);

    /**
     * 获取所有排口状态列表
     */
    List<Map<String, Object>> outPutStatusList(List<String> entCodes);

    /**
     * 排口报警参数查询
     */
    AjaxResult selectAlarmConf(String outPutId);

    /**
     * 排口报警配置
     */
    AjaxResult alarmConfEdit(List<OutPutAlarmConf> list);

    /**
     * 查询所有排口报警参数
     */
    List<OutPutAlarmConf> selectAllAlarmConf();

}
