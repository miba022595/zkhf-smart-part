package com.zkhf.epmis.platform.mapper.ops;

import com.zkhf.epmis.platform.ops.domain.OpsTemplate;
import com.zkhf.epmis.platform.ops.domain.OpsTemplateReq;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 运维模板配置-运维类型Mapper接口
 */
public interface OpsTemplateMapper {

    /**
     * 获取运维类型信息
     */
    List<OpsTemplate> allTemplateType();
    List<OpsTemplate> allTemplateTypeByOutId(String outPutId);

    /**
     * 查询运维模板详情
     */
    OpsTemplate selectOpsTemplateDetail(@Param("entCode") String entCode, @Param("outPutId") String outPutId,
                                        @Param("templateCode") String templateCode);

    /**
     * 查询运维模板详情
     */
    OpsTemplate selectOpsPubTemplateDetail(@Param("entCode") String entCode, @Param("outPutId") String outPutId,
                                        @Param("templateCode") String templateCode);

    /**
     * 查询运维模板列表
     */
    List<OpsTemplate> selectOpsTemplateList(OpsTemplateReq req);

    /**
     * 检查模板是否存在
     */
    int checkOpsTemplate(OpsTemplate info);

    /**
     * 新增运维模板类型
     */
    int insertOpsTemplate(OpsTemplate info);

    /**
     * 检查模板编码是否重复，企业、排口相等的
     */
    int checkOpsTemplateCode(OpsTemplate info);

    /**
     * 修改运维模板
     */
    int updateOpsTemplate(OpsTemplate info);

    /**
     * 删除运维模板
     */
    int deleteOpsTemplate(@Param("entCode") String entCode, @Param("outPutId") String outPutId,
                          @Param("templateCode") String templateCode);

}
