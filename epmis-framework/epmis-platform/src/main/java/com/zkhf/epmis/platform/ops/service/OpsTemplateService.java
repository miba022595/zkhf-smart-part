package com.zkhf.epmis.platform.ops.service;

import com.zkhf.epmis.core.domain.AjaxResult;
import com.zkhf.epmis.platform.ops.domain.OpsTemplateReq;
import com.zkhf.epmis.platform.ops.domain.OpsTemplate;

import java.util.Map;

/**
 * 运维模板配置-运维类型Service接口
 */
public interface OpsTemplateService {

    String pubSign = "-1";

    /**
     * 获取运维类型信息
     */
    AjaxResult allTemplateType(String outPutId);
    Map<String, String> allTemplateTypeReMap();

    /**
     * 查询运维模板详情
     * 私有 > 全局公共
     */
    OpsTemplate selectOpsTemplateDetail(String entCode, String outPutId, String templateCode);

    /**
     * 查询运维模板列表
     * 说明：排口私有配置 > 企业的私有配置 > 公共配置，可切3个模块，分别查排口私有配置，企业私有配置，公共配置
     */
    AjaxResult selectOpsTemplateList(OpsTemplateReq req);

    /**
     * 新增运维模板
     */
    AjaxResult insertOpsTemplate(OpsTemplate info);

    /**
     * 修改运维模板
     */
    AjaxResult updateOpsTemplate(OpsTemplate info);

    /**
     * 删除运维模板
     */
    AjaxResult deleteOpsTemplate(String entCode, String outPutId, String templateCode);
}
