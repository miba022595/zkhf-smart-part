package com.zkhf.epmis.process.material.service.impl;

import com.github.f4b6a3.ulid.UlidCreator;
import com.zkhf.epmis.core.annotation.Log;
import com.zkhf.epmis.core.domain.AjaxResult;
import com.zkhf.epmis.core.enums.BusinessType;
import com.zkhf.epmis.core.utils.PageUtils;
import com.zkhf.epmis.core.utils.StringUtils;
import com.zkhf.epmis.process.global.GVarContainer;
import com.zkhf.epmis.process.mapper.material.MaterialWarehouseMapper;
import com.zkhf.epmis.process.material.domain.MaterialWarehouse;
import com.zkhf.epmis.process.material.domain.MaterialWarehouseReq;
import com.zkhf.epmis.process.material.service.MaterialOrderSupport;
import com.zkhf.epmis.process.material.service.MaterialWarehouseService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * 仓库信息Service实现
 */
@Service
public class MaterialWarehouseServiceImpl implements MaterialWarehouseService {

    private MaterialWarehouseMapper materialWarehouseMapper;
    @Autowired
    public void setMaterialWarehouseMapper(MaterialWarehouseMapper materialWarehouseMapper) {
        this.materialWarehouseMapper = materialWarehouseMapper;
    }

    private MaterialOrderSupport materialOrderSupport;
    @Autowired
    public void setMaterialOrderSupport(MaterialOrderSupport materialOrderSupport) {
        this.materialOrderSupport = materialOrderSupport;
    }

    @Override
    public AjaxResult selectMaterialWarehouseList(MaterialWarehouseReq req) {
        req = initReq(req);
        if (req == null) {
            return AjaxResult.success(new ArrayList<>());
        }
        boolean page = PageUtils.startPageCheckExists();
        List<MaterialWarehouse> list = materialWarehouseMapper.selectMaterialWarehouseList(req);
        materialOrderSupport.fillEntNameByCode(list);
        return PageUtils.getAjaxResult(list, page);
    }

    @Override
    @Log(title = "仓库信息", businessType = BusinessType.INSERT)
    public AjaxResult insertMaterialWarehouse(MaterialWarehouse info) {
        if (info == null) {
            return AjaxResult.error("未知的参数");
        }
        if (StringUtils.isEmpty(info.getEntCode())) {
            return AjaxResult.error("企业编码不能为空");
        }
        if (materialOrderSupport.noEntPermission(info.getEntCode())) {
            return AjaxResult.error("无权限操作该企业数据");
        }
        if (StringUtils.isEmpty(info.getWarehouseCode())) {
            return AjaxResult.error("仓库编号不能为空");
        }
        if (StringUtils.isEmpty(info.getWarehouseName())) {
            return AjaxResult.error("仓库名称不能为空");
        }
        info.setWarehouseId(UlidCreator.getMonotonicUlid().toString());
        if (info.getStatus() == null) {
            info.setStatus(0);
        }
        if (info.getSortNum() == null) {
            info.setSortNum(0);
        }
        return AjaxResult.success(materialWarehouseMapper.insertMaterialWarehouse(info));
    }

    @Override
    @Log(title = "仓库信息", businessType = BusinessType.UPDATE)
    public AjaxResult updateMaterialWarehouse(MaterialWarehouse info) {
        if (info == null || StringUtils.isEmpty(info.getWarehouseId())) {
            return AjaxResult.error("仓库ID不能为空");
        }
        MaterialWarehouse old = materialWarehouseMapper.selectMaterialWarehouseById(info.getWarehouseId());
        if (old == null) {
            return AjaxResult.error("数据不存在");
        }
        if (materialOrderSupport.noEntPermission(old.getEntCode())) {
            return AjaxResult.error("无权限操作该企业数据");
        }
        if (StringUtils.isNotEmpty(info.getEntCode()) && !info.getEntCode().equals(old.getEntCode())) {
            return AjaxResult.error("所属企业不允许修改");
        }
        info.setEntCode(null);
        return AjaxResult.success(materialWarehouseMapper.updateMaterialWarehouse(info));
    }

    @Override
    @Log(title = "仓库信息", businessType = BusinessType.DELETE)
    public AjaxResult deleteMaterialWarehouse(MaterialWarehouse info) {
        if (info == null || StringUtils.isEmpty(info.getWarehouseId())) {
            return AjaxResult.error("仓库ID不能为空");
        }
        MaterialWarehouse old = materialWarehouseMapper.selectMaterialWarehouseById(info.getWarehouseId());
        if (old == null) {
            return AjaxResult.error("数据不存在");
        }
        if (materialOrderSupport.noEntPermission(old.getEntCode())) {
            return AjaxResult.error("无权限操作该企业数据");
        }
        if (materialWarehouseMapper.countMaterialWarehouseRef(info.getWarehouseId()) > 0) {
            return AjaxResult.error("仓库已被业务数据引用，不能删除");
        }
        return AjaxResult.success(materialWarehouseMapper.deleteMaterialWarehouseById(info.getWarehouseId()));
    }

    private MaterialWarehouseReq initReq(MaterialWarehouseReq req) {
        if (req == null) {
            req = new MaterialWarehouseReq();
        }
        if (GVarContainer.isNotAdmin()) {
            List<String> entCodes = GVarContainer.getEntCodes();
            if (entCodes == null || entCodes.isEmpty()) {
                return null;
            }
            if (StringUtils.isNotEmpty(req.getEntCode())) {
                if (!entCodes.contains(req.getEntCode())) {
                    return null;
                }
            } else {
                req.setEntCodes(entCodes);
            }
        }
        return req;
    }

}
