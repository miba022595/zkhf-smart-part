package com.zkhf.epmis.platform.annex.service;

import com.alibaba.fastjson2.JSONObject;
import com.zkhf.epmis.core.domain.AjaxResult;
import com.zkhf.epmis.core.domain.AnnexInfo;
import com.zkhf.epmis.core.domain.AnnexReq;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface AnnexService {

    /**
     * 查询附件
     */
    List<AnnexInfo> selectAnnexList(AnnexReq req);

    /**
     * 查询附件
     */
    List<AnnexInfo> selectAnnexList(String sourceId, String sourceType);

    /**
     * 添加附件
     */
    AjaxResult insertAnnex(MultipartFile file, String sourceType);

    /**
     * 添加附件
     * 返回附件的id
     */
    AnnexInfo insertAnnexReturnId(MultipartFile file, String sourceType);

    /**
     * 修改附件信息
     */
    AjaxResult updateAnnex(JSONObject annexInfo);

    /**
     * 删除附件
     */
    AjaxResult deleteAnnex(List<String> annexIds);

    /**
     * 修改附件信息
     * 更新时的id包含在旧的内无需更新；旧的不包含在新的列表内时删除记录及文件
     * annexId为空时表示直接删除
     */
    void updateAnnex(String sourceId, String sourceType, List<String> annexIds);
}
