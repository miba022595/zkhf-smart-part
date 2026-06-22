package com.zkhf.epmis.process.material.service;

import com.zkhf.epmis.core.domain.AjaxResult;
import com.zkhf.epmis.process.material.domain.MaterialInfo;
import com.zkhf.epmis.process.material.domain.MaterialInfoReq;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.multipart.MultipartFile;

/**
 * 物资基础信息Service接口
 */
public interface MaterialInfoService {

    /**
     * 查询物资基础信息列表（含总库存）
     */
    AjaxResult selectMaterialInfoList(MaterialInfoReq req);

    /**
     * 查询物资基础信息详情（含总库存及各仓库库存明细）
     */
    AjaxResult selectMaterialInfoDetail(String materialId);

    /**
     * 新增物资基础信息
     */
    AjaxResult insertMaterialInfo(MaterialInfo info);

    /**
     * 修改物资基础信息
     */
    AjaxResult updateMaterialInfo(MaterialInfo info);

    /**
     * 删除物资基础信息
     */
    AjaxResult deleteMaterialInfo(MaterialInfo info);

    /**
     * 导出物资基础信息
     */
    void exportMaterialInfo(MaterialInfoReq req, HttpServletResponse response);

    /**
     * 下载物资导入模板
     */
    void downloadMaterialInfoTemplate(HttpServletResponse response);

    /**
     * 导入物资基础信息
     */
    AjaxResult importMaterialInfo(MultipartFile file);
}
