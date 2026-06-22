package com.zkhf.epmis.platform.envProtect.service;

import com.zkhf.epmis.core.domain.AjaxResult;
import com.zkhf.epmis.platform.envProtect.domain.OtherCertificate;
import com.zkhf.epmis.platform.envProtect.domain.OtherCertificateReq;

import jakarta.servlet.http.HttpServletResponse;

/**
 * 其他证书Service接口
 */
public interface OtherCertificateService {

    /**
     * 查询其他证书列表
     */
    AjaxResult selectOtherCertificateList(OtherCertificateReq req);

    /**
     * 导出其他证书列表
     */
    void exportOtherCertificate(OtherCertificateReq req, HttpServletResponse response);

    /**
     * 新增其他证书
     */
    AjaxResult insertOtherCertificate(OtherCertificate info);

    /**
     * 修改其他证书
     */
    AjaxResult updateOtherCertificate(OtherCertificate info);

    /**
     * 批量删除其他证书
     */
    AjaxResult deleteOtherCertificateById(String otherId);
}
