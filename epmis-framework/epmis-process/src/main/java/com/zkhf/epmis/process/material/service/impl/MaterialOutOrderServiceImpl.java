package com.zkhf.epmis.process.material.service.impl;

import com.github.f4b6a3.ulid.UlidCreator;
import com.zkhf.epmis.core.annotation.Log;
import com.zkhf.epmis.core.domain.AjaxResult;
import com.zkhf.epmis.core.enums.AnnexTypeEnum;
import com.zkhf.epmis.core.enums.BusinessType;
import com.zkhf.epmis.core.utils.DateUtils;
import com.zkhf.epmis.core.utils.PageUtils;
import com.zkhf.epmis.core.utils.StringUtils;
import com.zkhf.epmis.process.mapper.material.MaterialInfoMapper;
import com.zkhf.epmis.process.mapper.material.MaterialOrderMapper;
import com.zkhf.epmis.process.mapper.material.MaterialWarehouseMapper;
import com.zkhf.epmis.process.material.domain.*;
import com.zkhf.epmis.process.material.service.MaterialOrderSupport;
import com.zkhf.epmis.process.material.service.MaterialOutOrderService;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
public class MaterialOutOrderServiceImpl implements MaterialOutOrderService {

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

    private MaterialWarehouseMapper materialWarehouseMapper;
    @Autowired
    public void setMaterialWarehouseMapper(MaterialWarehouseMapper materialWarehouseMapper) {
        this.materialWarehouseMapper = materialWarehouseMapper;
    }

    private MaterialInfoMapper materialInfoMapper;
    @Autowired
    public void setMaterialInfoMapper(MaterialInfoMapper materialInfoMapper) {
        this.materialInfoMapper = materialInfoMapper;
    }

    @Override
    public AjaxResult selectMaterialOutOrderList(MaterialBizReq req) {
        req = materialOrderSupport.initReq(req);
        if (req == null) {
            return AjaxResult.success(new ArrayList<>());
        }
        boolean page = PageUtils.startPageCheckExists();
        List<MaterialOutOrder> list = materialOrderMapper.selectMaterialOutOrderList(req);
        materialOrderSupport.fillEntNameByCode(list);
        return PageUtils.getAjaxResult(list, page);
    }

    @Override
    public AjaxResult selectMaterialOutOrderDetail(String outId) {
        MaterialOutOrder info = materialOrderMapper.selectMaterialOutOrderById(outId);
        if (info == null) {
            return AjaxResult.error("数据不存在");
        }
        if (materialOrderSupport.noEntPermission(info.getEntCode())) {
            return AjaxResult.error("无权限查看该企业数据");
        }
        info.setItemList(materialOrderMapper.selectMaterialOutOrderItems(outId));
        info.setAnnexInfoList(materialOrderSupport.listAnnex(info.getOutId(), AnnexTypeEnum.materialOutOrder.name));
        info.setOperateLogList(materialOrderSupport.listOperateLog(info.getOutId(), info.getOutNo()));
        return AjaxResult.success(info);
    }

    @Override
    @Log(title = "物资出库单", businessType = BusinessType.INSERT)
    @Transactional(rollbackFor = Exception.class)
    public AjaxResult insertMaterialOutOrder(MaterialOutOrder info) {
        if (info == null) {
            return AjaxResult.error("未知的参数");
        }
        if (StringUtils.isEmpty(info.getEntCode())) {
            return AjaxResult.error("企业编码不能为空");
        }
        if (materialOrderSupport.noEntPermission(info.getEntCode())) {
            return AjaxResult.error("无权限操作该企业数据");
        }
        if (StringUtils.isEmpty(info.getWarehouseId())) {
            return AjaxResult.error("仓库不能为空");
        }
        if (info.getItemList() == null || info.getItemList().isEmpty()) {
            return AjaxResult.error("出库明细不能为空");
        }
        try {
            Integer newStatus = Integer.valueOf(2).equals(info.getStatus()) ? 2 : (Integer.valueOf(6).equals(info.getStatus()) ? 6 : 1);
            info.setStatus(newStatus);
            info.setStockEffectStatus(Integer.valueOf(2).equals(newStatus) ? 1 : 0);
            info.setAuditBy(null);
            info.setAuditTime(null);
            info.setAuditRemark(null);
            prepareOutOrder(info);
            int count = materialOrderMapper.insertMaterialOutOrder(info);
            materialOrderMapper.batchInsertMaterialOutOrderItems(info.getItemList());
            if (count > 0) {
                materialOrderSupport.updateAnnex(info.getOutId(), AnnexTypeEnum.materialOutOrder.name, info.getAnnexIds());
            }
            if (Integer.valueOf(2).equals(newStatus)) {
                for (MaterialOutOrderItem item : info.getItemList()) {
                    materialOrderSupport.freezeOut(info.getEntCode(), info.getWarehouseId(), item.getMaterialId(),
                            item.getOutQty(), info.getOutId(), item.getOutItemId(), info.getOutNo());
                }
            }
            return AjaxResult.success(info.getOutId());
        } catch (RuntimeException e) {
            return AjaxResult.error(e.getMessage());
        }
    }

    @Override
    @Log(title = "物资出库单", businessType = BusinessType.UPDATE)
    @Transactional(rollbackFor = Exception.class)
    public AjaxResult updateMaterialOutOrder(MaterialOutOrder info) {
        if (info == null || StringUtils.isEmpty(info.getOutId())) {
            return AjaxResult.error("出库单ID不能为空");
        }
        MaterialOutOrder old = materialOrderMapper.selectMaterialOutOrderById(info.getOutId());
        if (old == null) {
            return AjaxResult.error("数据不存在");
        }
        if (materialOrderSupport.noEntPermission(old.getEntCode())) {
            return AjaxResult.error("无权限操作该企业数据");
        }
        if (StringUtils.isNotEmpty(info.getOutNo()) && !info.getOutNo().equals(old.getOutNo())) {
            return AjaxResult.error("出库单号不允许修改");
        }
        if (StringUtils.isNotEmpty(info.getApplyId()) && !info.getApplyId().equals(old.getApplyId())) {
            return AjaxResult.error("关联申请单不允许修改");
        }
        if (Integer.valueOf(0).equals(old.getStatus())) {
            if (Integer.valueOf(1).equals(info.getStatus()) && (info.getItemList() == null || info.getItemList().isEmpty())) {
                if (materialOrderMapper.countMaterialOutOrderRef(info.getOutId()) > 0) {
                    return AjaxResult.error("已有关联归还的出库单不允许回退");
                }
                if (Integer.valueOf(1).equals(old.getStockEffectStatus())) {
                    List<MaterialOutOrderItem> items = materialOrderMapper.selectMaterialOutOrderItems(old.getOutId());
                    for (MaterialOutOrderItem item : items) {
                        materialOrderSupport.unfreezeOut(old.getEntCode(), old.getWarehouseId(), item.getMaterialId(),
                                item.getOutQty(), old.getOutId(), item.getOutItemId(), old.getOutNo());
                    }
                }
                MaterialOutOrder update = new MaterialOutOrder();
                update.setOutId(old.getOutId());
                update.setStatus(1);
                update.setAuditBy(null);
                update.setAuditTime(null);
                update.setRemark(info.getRemark());
                update.setStockEffectStatus(0);
                materialOrderMapper.updateMaterialOutOrder(update);
                return AjaxResult.success(old.getOutId());
            }
            return AjaxResult.error("已作废出库单不允许直接修改，请先回退到草稿再编辑");
        }
        if (Integer.valueOf(6).equals(old.getStatus())) {
            if (Integer.valueOf(2).equals(info.getStatus()) && (info.getItemList() == null || info.getItemList().isEmpty())) {
                if (StringUtils.isNotEmpty(old.getApplyId())) {
                    MaterialApplyOrder applyOrder = materialOrderMapper.selectMaterialApplyOrderById(old.getApplyId());
                    if (applyOrder == null) {
                        return AjaxResult.error("关联申请单不存在");
                    }
                    if (!Integer.valueOf(3).equals(applyOrder.getAuditStatus())) {
                        return AjaxResult.error("关联申请单未审核通过，不能提交出库");
                    }
                }
                List<MaterialOutOrderItem> items = materialOrderMapper.selectMaterialOutOrderItems(old.getOutId());
                for (MaterialOutOrderItem item : items) {
                    materialOrderSupport.freezeOut(old.getEntCode(), old.getWarehouseId(), item.getMaterialId(),
                            item.getOutQty(), old.getOutId(), item.getOutItemId(), old.getOutNo());
                }
                MaterialOutOrder update = new MaterialOutOrder();
                update.setOutId(old.getOutId());
                update.setStatus(2);
                update.setAuditBy(null);
                update.setAuditTime(null);
                update.setRemark(info.getRemark());
                update.setStockEffectStatus(1);
                materialOrderMapper.updateMaterialOutOrder(update);
                return AjaxResult.success(old.getOutId());
            }
            if (Integer.valueOf(1).equals(info.getStatus()) && (info.getItemList() == null || info.getItemList().isEmpty())) {
                if (materialOrderMapper.countMaterialOutOrderRef(info.getOutId()) > 0) {
                    return AjaxResult.error("已有关联归还的出库单不允许回退");
                }
                if (Integer.valueOf(1).equals(old.getStockEffectStatus())) {
                    List<MaterialOutOrderItem> items = materialOrderMapper.selectMaterialOutOrderItems(old.getOutId());
                    for (MaterialOutOrderItem item : items) {
                        materialOrderSupport.unfreezeOut(old.getEntCode(), old.getWarehouseId(), item.getMaterialId(),
                                item.getOutQty(), old.getOutId(), item.getOutItemId(), old.getOutNo());
                    }
                }
                MaterialOutOrder update = new MaterialOutOrder();
                update.setOutId(old.getOutId());
                update.setStatus(1);
                update.setAuditBy(null);
                update.setAuditTime(null);
                update.setRemark(info.getRemark());
                update.setStockEffectStatus(0);
                materialOrderMapper.updateMaterialOutOrder(update);
                return AjaxResult.success(old.getOutId());
            }
            return AjaxResult.error("已保存出库单不允许直接修改，请先回退到草稿再编辑");
        }
        if (!Integer.valueOf(1).equals(old.getStatus())) {
            return AjaxResult.error("仅草稿出库单允许修改");
        }
        if (Integer.valueOf(1).equals(old.getStockEffectStatus()) || materialOrderMapper.countMaterialOutOrderRef(info.getOutId()) > 0) {
            return AjaxResult.error("已生效或已有关联归还的出库单不允许修改");
        }
        if (Integer.valueOf(6).equals(info.getStatus()) && (info.getItemList() == null || info.getItemList().isEmpty())) {
            MaterialOutOrder update = new MaterialOutOrder();
            update.setOutId(old.getOutId());
            update.setStatus(6);
            update.setAuditBy(null);
            update.setAuditTime(null);
            update.setRemark(info.getRemark());
            update.setStockEffectStatus(0);
            materialOrderMapper.updateMaterialOutOrder(update);
            return AjaxResult.success(old.getOutId());
        }
        if (Integer.valueOf(2).equals(info.getStatus()) && (info.getItemList() == null || info.getItemList().isEmpty())) {
            if (StringUtils.isNotEmpty(old.getApplyId())) {
                MaterialApplyOrder applyOrder = materialOrderMapper.selectMaterialApplyOrderById(old.getApplyId());
                if (applyOrder == null) {
                    return AjaxResult.error("关联申请单不存在");
                }
                if (!Integer.valueOf(3).equals(applyOrder.getAuditStatus())) {
                    return AjaxResult.error("关联申请单未审核通过，不能提交出库");
                }
            }
            List<MaterialOutOrderItem> items = materialOrderMapper.selectMaterialOutOrderItems(old.getOutId());
            for (MaterialOutOrderItem item : items) {
                materialOrderSupport.freezeOut(old.getEntCode(), old.getWarehouseId(), item.getMaterialId(),
                        item.getOutQty(), old.getOutId(), item.getOutItemId(), old.getOutNo());
            }
            MaterialOutOrder update = new MaterialOutOrder();
            update.setOutId(old.getOutId());
            update.setStatus(2);
            update.setAuditBy(null);
            update.setAuditTime(null);
            update.setRemark(info.getRemark());
            update.setStockEffectStatus(1);
            materialOrderMapper.updateMaterialOutOrder(update);
            return AjaxResult.success(old.getOutId());
        }
        if (info.getItemList() == null || info.getItemList().isEmpty()) {
            return AjaxResult.error("出库明细不能为空");
        }
        try {
            Integer newStatus = Integer.valueOf(2).equals(info.getStatus()) ? 2 : (Integer.valueOf(6).equals(info.getStatus()) ? 6 : 1);
            info.setEntCode(old.getEntCode());
            info.setOutNo(old.getOutNo());
            info.setApplyId(old.getApplyId());
            info.setStatus(newStatus);
            info.setStockEffectStatus(Integer.valueOf(2).equals(newStatus) ? 1 : 0);
            info.setAuditBy(null);
            info.setAuditTime(null);
            info.setAuditRemark(null);
            prepareOutOrder(info);
            materialOrderMapper.updateMaterialOutOrder(info);
            materialOrderMapper.deleteMaterialOutOrderItems(info.getOutId());
            materialOrderMapper.batchInsertMaterialOutOrderItems(info.getItemList());
            materialOrderSupport.updateAnnex(info.getOutId(), AnnexTypeEnum.materialOutOrder.name, info.getAnnexIds());
            if (Integer.valueOf(2).equals(newStatus)) {
                if (Integer.valueOf(2).equals(newStatus) && StringUtils.isNotEmpty(old.getApplyId())) {
                    MaterialApplyOrder applyOrder = materialOrderMapper.selectMaterialApplyOrderById(old.getApplyId());
                    if (applyOrder == null) {
                        throw new RuntimeException("关联申请单不存在");
                    }
                    if (!Integer.valueOf(3).equals(applyOrder.getAuditStatus())) {
                        throw new RuntimeException("关联申请单未审核通过，不能提交出库");
                    }
                }
                for (MaterialOutOrderItem item : info.getItemList()) {
                    materialOrderSupport.freezeOut(info.getEntCode(), info.getWarehouseId(), item.getMaterialId(),
                            item.getOutQty(), info.getOutId(), item.getOutItemId(), info.getOutNo());
                }
            }
            return AjaxResult.success(info.getOutId());
        } catch (RuntimeException e) {
            return AjaxResult.error(e.getMessage());
        }
    }

    @Override
    @Log(title = "物资出库单", businessType = BusinessType.DELETE)
    @Transactional(rollbackFor = Exception.class)
    public AjaxResult deleteMaterialOutOrder(MaterialOutOrder info) {
        if (info == null || StringUtils.isEmpty(info.getOutId())) {
            return AjaxResult.error("出库单ID不能为空");
        }
        MaterialOutOrder old = materialOrderMapper.selectMaterialOutOrderById(info.getOutId());
        if (old == null) {
            return AjaxResult.error("数据不存在");
        }
        if (materialOrderSupport.noEntPermission(old.getEntCode())) {
            return AjaxResult.error("无权限操作该企业数据");
        }
        if (!Integer.valueOf(1).equals(old.getStatus()) && !Integer.valueOf(6).equals(old.getStatus()) && !Integer.valueOf(0).equals(old.getStatus())) {
            return AjaxResult.error("仅草稿、已保存或已作废出库单允许删除");
        }
        if (materialOrderMapper.countMaterialOutOrderRef(info.getOutId()) > 0) {
            return AjaxResult.error("已有关联归还的出库单不允许删除");
        }
        if (Integer.valueOf(1).equals(old.getStockEffectStatus()) && !Integer.valueOf(6).equals(old.getStatus())) {
            return AjaxResult.error("已生效出库单不允许删除");
        }
        if (Integer.valueOf(6).equals(old.getStatus())) {
            if (Integer.valueOf(1).equals(old.getStockEffectStatus())) {
                List<MaterialOutOrderItem> items = materialOrderMapper.selectMaterialOutOrderItems(old.getOutId());
                for (MaterialOutOrderItem item : items) {
                    materialOrderSupport.unfreezeOut(old.getEntCode(), old.getWarehouseId(), item.getMaterialId(),
                            item.getOutQty(), old.getOutId(), item.getOutItemId(), old.getOutNo());
                }
            }
        }
        materialOrderMapper.deleteMaterialOutOrderItems(info.getOutId());
        materialOrderSupport.updateAnnex(info.getOutId(), AnnexTypeEnum.materialOutOrder.name, null);
        return AjaxResult.success(materialOrderMapper.deleteMaterialOutOrderById(info.getOutId()));
    }

    @Override
    @Log(title = "物资出库单导出", businessType = BusinessType.EXPORT)
    public void exportMaterialOutOrder(MaterialBizReq req, HttpServletResponse response) {
        req = materialOrderSupport.initReq(req);
        if (req == null) {
            return;
        }
        List<MaterialOutOrder> list = materialOrderMapper.selectMaterialOutOrderList(req);
        materialOrderSupport.fillEntNameByCode(list);
        List<List<String>> rows = new ArrayList<>();
        list.forEach(item -> rows.add(List.of(
                defaultVal(item.getEntName()),
                defaultVal(item.getOutNo()),
                stringVal(item.getOutTime()),
                defaultVal(item.getWarehouseName()),
                defaultVal(item.getReceiveUser()),
                defaultVal(item.getOutUser()),
                defaultVal(item.getAuditBy()),
                defaultVal(item.getApplyNo()),
                numberVal(item.getTotalQty()),
                numberVal(item.getTotalAmount()),
                statusDesc(item.getStatus()),
                defaultVal(item.getRemark())
        )));
        materialOrderSupport.exportSimpleExcel("物资出库单.xlsx",
                new String[]{"企业", "出库单号", "出库时间", "仓库", "领用人员", "出库人员", "审核人员", "关联申请单号", "出库总数量", "出库总金额", "状态", "备注"},
                rows, response);
    }

    private void prepareOutOrder(MaterialOutOrder info) {
        MaterialWarehouse warehouse = materialWarehouseMapper.selectMaterialWarehouseById(info.getWarehouseId());
        if (warehouse == null) {
            throw new RuntimeException("仓库不存在");
        }
        if (Integer.valueOf(1).equals(warehouse.getStatus())) {
            throw new RuntimeException("停用仓库不能创建出库单");
        }
        if (info.getOutTime() == null) {
            info.setOutTime(LocalDateTime.now());
        }
        if (StringUtils.isEmpty(info.getOutNo())) {
            info.setOutNo(materialOrderSupport.buildBizNo("CK"));
        }
        if (info.getStatus() == null) {
            info.setStatus(1);
        }
        info.setStockEffectStatus(info.getStatus() >= 3 ? 1 : 0);
        if (StringUtils.isEmpty(info.getOutId())) {
            info.setOutId(UlidCreator.getMonotonicUlid().toString());
        }
        double totalQty = 0D;
        double totalAmount = 0D;
        int sort = 1;
        for (MaterialOutOrderItem item : info.getItemList()) {
            MaterialInfo material = materialInfoMapper.selectMaterialInfoById(item.getMaterialId());
            if (material == null) {
                throw new RuntimeException("物资不存在");
            }
            if (Integer.valueOf(1).equals(material.getStatus())) {
                throw new RuntimeException("停用物资不能继续出库");
            }
            if (item.getOutQty() == null || item.getOutQty() <= 0) {
                throw new RuntimeException("出库数量必须大于0");
            }
            item.setOutItemId(UlidCreator.getMonotonicUlid().toString());
            item.setOutId(info.getOutId());
            item.setMaterialCode(material.getMaterialCode());
            item.setMaterialName(material.getMaterialName());
            item.setBrand(material.getBrand());
            item.setModelSpec(material.getModelSpec());
            item.setCategoryName(material.getCategoryName());
            item.setUnit(material.getUnit());
            if (item.getUnitPrice() == null) {
                item.setUnitPrice(material.getUnitPrice() == null ? 0D : material.getUnitPrice());
            }
            item.setAmount(item.getOutQty() * item.getUnitPrice());
            item.setReturnedQty(0D);
            item.setSortNum(sort++);
            if (StringUtils.isNotEmpty(item.getApplyItemId())) {
                MaterialApplyOrderItem applyItem = materialOrderMapper.selectMaterialApplyOrderItemById(item.getApplyItemId());
                if (applyItem == null) {
                    throw new RuntimeException("关联申请明细不存在");
                }
                double leftQty = (applyItem.getApplyQty() == null ? 0D : applyItem.getApplyQty()) - (applyItem.getOutQty() == null ? 0D : applyItem.getOutQty());
                if (leftQty < item.getOutQty()) {
                    throw new RuntimeException("出库数量超出申请剩余数量");
                }
                item.setApplyQty(applyItem.getApplyQty());
            }
            totalQty += item.getOutQty();
            totalAmount += item.getAmount();
        }
        info.setTotalQty(totalQty);
        info.setTotalAmount(totalAmount);
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

    private String statusDesc(Integer status) {
        if (Integer.valueOf(2).equals(status)) { return "待审核"; }
        if (Integer.valueOf(3).equals(status)) { return "已审核"; }
        if (Integer.valueOf(4).equals(status)) { return "已完成"; }
        if (Integer.valueOf(6).equals(status)) { return "已保存"; }
        if (Integer.valueOf(0).equals(status)) { return "已作废"; }
        return "草稿";
    }

    private void refreshApplyOrderStatus(String applyId) {
        if (StringUtils.isEmpty(applyId)) {
            return;
        }
        MaterialApplyOrder order = materialOrderMapper.selectMaterialApplyOrderById(applyId);
        if (order == null) {
            return;
        }
        List<MaterialApplyOrderItem> items = materialOrderMapper.selectMaterialApplyOrderItems(applyId);
        double total = 0D;
        double out = 0D;
        double returned = 0D;
        for (MaterialApplyOrderItem item : items) {
            total += item.getApplyQty() == null ? 0D : item.getApplyQty();
            out += item.getOutQty() == null ? 0D : item.getOutQty();
            returned += item.getReturnQty() == null ? 0D : item.getReturnQty();
        }
        MaterialApplyOrder update = new MaterialApplyOrder();
        update.setApplyId(applyId);
        update.setOutStatus(out <= 0 ? 0 : (out < total ? 1 : 2));
        update.setReturnStatus(returned <= 0 ? 0 : (returned < out ? 1 : 2));
        materialOrderMapper.updateMaterialApplyOrder(update);
    }
}
