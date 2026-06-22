package com.zkhf.epmis.platform.envProtect.service;

import com.zkhf.epmis.core.domain.AjaxResult;
import com.zkhf.epmis.platform.envProtect.domain.EnvProPerson;
import com.zkhf.epmis.platform.envProtect.domain.EnvProPersonReq;
import org.springframework.web.multipart.MultipartFile;

import jakarta.servlet.http.HttpServletResponse;
import java.util.List;

/**
 * 企业环保人员Service接口
 */
public interface EnvProPersonService {

    /**
     * 获取企业下的环保人员
     */
    AjaxResult selectProPersonByEnt(String entCode);

    /**
     * 获取企业环保人员详细信息
     */
    AjaxResult selectProPersonById(String proPersonId);

    /**
     * 查询企业环保人员列表
     */
    AjaxResult selectProPersonList(EnvProPersonReq req);

    /**
     * 导出企业环保人员列表
     */
    void exportProPerson(EnvProPersonReq req, HttpServletResponse response);

    /**
     * 下载企业环保人员模板
     */
    void downloadTemplate(HttpServletResponse response);

    /**
     * 导入企业环保人员模板
     */
    AjaxResult importTemplate(MultipartFile file, String entCode);

    /**
     * 新增企业环保人员
     */
    AjaxResult insertProPerson(EnvProPerson info);

    /**
     * 修改企业环保人员
     */
    AjaxResult updateProPerson(EnvProPerson info);

    /**
     * 删除企业环保人员
     */
    AjaxResult deleteProPersonByIds(List<String> ids);
}
