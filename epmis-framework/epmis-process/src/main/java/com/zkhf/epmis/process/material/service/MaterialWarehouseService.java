package com.zkhf.epmis.process.material.service;

import com.zkhf.epmis.core.domain.AjaxResult;
import com.zkhf.epmis.process.material.domain.MaterialWarehouse;
import com.zkhf.epmis.process.material.domain.MaterialWarehouseReq;

/**
 * 仓库信息Service接口
 */
public interface MaterialWarehouseService {

    /**
     * 查询仓库信息列表
     */
    AjaxResult selectMaterialWarehouseList(MaterialWarehouseReq req);

    /**
     * 新增仓库信息
     */
    AjaxResult insertMaterialWarehouse(MaterialWarehouse info);

    /**
     * 修改仓库信息
     */
    AjaxResult updateMaterialWarehouse(MaterialWarehouse info);

    /**
     * 删除仓库信息
     */
    AjaxResult deleteMaterialWarehouse(MaterialWarehouse info);
}
