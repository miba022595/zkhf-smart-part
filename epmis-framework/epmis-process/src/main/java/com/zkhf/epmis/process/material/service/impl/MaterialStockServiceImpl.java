package com.zkhf.epmis.process.material.service.impl;

import com.zkhf.epmis.core.domain.AjaxResult;
import com.zkhf.epmis.core.utils.DateUtils;
import com.zkhf.epmis.core.utils.PageUtils;
import com.zkhf.epmis.core.utils.StringUtils;
import com.zkhf.epmis.process.global.GVarContainer;
import com.zkhf.epmis.process.mapper.material.MaterialOrderMapper;
import com.zkhf.epmis.process.mapper.material.MaterialStockMapper;
import com.zkhf.epmis.process.material.domain.MaterialStock;
import com.zkhf.epmis.process.material.domain.MaterialStockFlow;
import com.zkhf.epmis.process.material.domain.MaterialStockFlowReq;
import com.zkhf.epmis.process.material.domain.MaterialStockReq;
import com.zkhf.epmis.process.material.service.MaterialOrderSupport;
import com.zkhf.epmis.process.material.service.MaterialStockService;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * 物资库存Service实现
 */
@Service
public class MaterialStockServiceImpl implements MaterialStockService {

    private MaterialStockMapper materialStockMapper;
    @Autowired
    public void setMaterialStockMapper(MaterialStockMapper materialStockMapper) {
        this.materialStockMapper = materialStockMapper;
    }

    private MaterialOrderMapper materialOrderMapper;
    @Autowired
    public void setMaterialOrderMapper(MaterialOrderMapper materialOrderMapper) {
        this.materialOrderMapper = materialOrderMapper;
    }

    private MaterialOrderSupport materialOrderSupport;
    @Autowired
    public void setMaterialOrderSupport(MaterialOrderSupport materialOrderSupport) {
        this.materialOrderSupport = materialOrderSupport;
    }

    @Override
    public AjaxResult selectMaterialStockList(MaterialStockReq req) {
        req = initReq(req);
        if (req == null) {
            return AjaxResult.success(new ArrayList<>());
        }
        boolean page = PageUtils.startPageCheckExists();
        List<MaterialStock> list = materialStockMapper.selectMaterialStockList(req);
        materialOrderSupport.fillEntNameByCode(list);
        return PageUtils.getAjaxResult(list, page);
    }

    @Override
    public AjaxResult selectMaterialStockFlowList(MaterialStockFlowReq req) {
        req = initFlowReq(req);
        if (req == null) {
            return AjaxResult.success(new ArrayList<>());
        }
        boolean page = PageUtils.startPageCheckExists();
        List<MaterialStockFlow> list = materialOrderMapper.selectMaterialStockFlowList(req);
        materialOrderSupport.fillEntNameByCode(list);
        return PageUtils.getAjaxResult(list, page);
    }

    @Override
    public void exportMaterialStock(MaterialStockReq req, HttpServletResponse response) {
        req = initReq(req);
        if (req == null) {
            return;
        }
        List<MaterialStock> list = materialStockMapper.selectMaterialStockList(req);
        materialOrderSupport.fillEntNameByCode(list);
        List<List<String>> rows = new ArrayList<>();
        list.forEach(item -> rows.add(List.of(
                defaultVal(item.getEntName()),
                defaultVal(item.getWarehouseName()),
                defaultVal(item.getMaterialCode()),
                defaultVal(item.getMaterialName()),
                defaultVal(item.getBrand()),
                defaultVal(item.getModelSpec()),
                defaultVal(item.getCategoryName()),
                numberVal(item.getCurrentQty()),
                numberVal(item.getAvailableQty()),
                numberVal(item.getFrozenQty()),
                defaultVal(item.getUnit()),
                numberVal(item.getMinStock()),
                stockStatusDesc(item.getStockStatus()),
                stringVal(item.getLastChangeTime())
        )));
        materialOrderSupport.exportSimpleExcel("物资库存汇总.xlsx",
                new String[]{"企业", "仓库", "物资编号", "物资名称", "品牌", "规格型号", "物资分类", "当前库存", "可用库存", "冻结库存", "单位", "最低库存", "库存状态", "最近变动时间"},
                rows, response);
    }

    @Override
    public void exportMaterialStockFlow(MaterialStockFlowReq req, HttpServletResponse response) {
        req = initFlowReq(req);
        if (req == null) {
            return;
        }
        List<MaterialStockFlow> list = materialOrderMapper.selectMaterialStockFlowList(req);
        materialOrderSupport.fillEntNameByCode(list);
        List<List<String>> rows = new ArrayList<>();
        list.forEach(item -> rows.add(List.of(
                defaultVal(item.getEntName()),
                defaultVal(item.getWarehouseName()),
                defaultVal(item.getMaterialCode()),
                defaultVal(item.getMaterialName()),
                bizTypeDesc(item.getBizType()),
                defaultVal(item.getBizNo()),
                numberVal(item.getQtyChange()),
                numberVal(item.getBeforeQty()),
                numberVal(item.getAfterQty()),
                defaultVal(item.getOperateBy()),
                stringVal(item.getOperateTime()),
                defaultVal(item.getRemark())
        )));
        materialOrderSupport.exportSimpleExcel("物资库存明细.xlsx",
                new String[]{"企业", "仓库", "物资编号", "物资名称", "业务类型", "业务单号", "变动数量", "变动前库存", "变动后库存", "操作人", "操作时间", "备注"},
                rows, response);
    }

    private MaterialStockReq initReq(MaterialStockReq req) {
        if (req == null) {
            req = new MaterialStockReq();
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

    private MaterialStockFlowReq initFlowReq(MaterialStockFlowReq req) {
        if (req == null) {
            req = new MaterialStockFlowReq();
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

    private String defaultVal(String value) {
        return value == null ? "" : value;
    }

    private String stringVal(Object value) {
        if (value == null) {
            return "";
        }
        if (value instanceof LocalDateTime time) {
            return time.format(DateUtils.dtf);
        }
        return value.toString();
    }

    private String numberVal(Double value) {
        return value == null ? "" : value.toString();
    }

    private String stockStatusDesc(Integer status) {
        if (Integer.valueOf(2).equals(status)) {
            return "低库存";
        }
        if (Integer.valueOf(3).equals(status)) {
            return "无库存";
        }
        return "正常";
    }

    private String bizTypeDesc(String bizType) {
        if ("IN".equals(bizType)) {
            return "入库";
        }
        if ("OUT".equals(bizType)) {
            return "出库";
        }
        if ("RETURN".equals(bizType)) {
            return "归还";
        }
        if ("ADJUST".equals(bizType)) {
            return "调整";
        }
        if ("FREEZE".equals(bizType)) {
            return "冻结";
        }
        if ("UNFREEZE".equals(bizType)) {
            return "解冻";
        }
        return defaultVal(bizType);
    }
}
