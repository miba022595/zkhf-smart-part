package com.zkhf.epmis.process.material.service.impl;

import com.github.f4b6a3.ulid.UlidCreator;
import com.zkhf.epmis.core.annotation.Log;
import com.zkhf.epmis.core.domain.AjaxResult;
import com.zkhf.epmis.core.enums.AnnexTypeEnum;
import com.zkhf.epmis.core.enums.BusinessType;
import com.zkhf.epmis.core.utils.DateUtils;
import com.zkhf.epmis.core.utils.PageUtils;
import com.zkhf.epmis.core.utils.StringUtils;
import com.zkhf.epmis.process.mapper.material.MaterialOrderMapper;
import com.zkhf.epmis.process.mapper.material.MaterialWarehouseMapper;
import com.zkhf.epmis.process.material.domain.*;
import com.zkhf.epmis.process.material.service.MaterialOrderSupport;
import com.zkhf.epmis.process.material.service.MaterialReturnOrderService;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
public class MaterialReturnOrderServiceImpl implements MaterialReturnOrderService {

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

    @Override
    public AjaxResult selectMaterialReturnOrderList(MaterialBizReq req) {
        req = materialOrderSupport.initReq(req);
        if (req == null) {
            return AjaxResult.success(new ArrayList<>());
        }
        boolean page = PageUtils.startPageCheckExists();
        List<MaterialReturnOrder> list = materialOrderMapper.selectMaterialReturnOrderList(req);
        materialOrderSupport.fillEntNameByCode(list);
        return PageUtils.getAjaxResult(list, page);
    }

    @Override
    public AjaxResult selectMaterialReturnOrderDetail(String returnId) {
        MaterialReturnOrder info = materialOrderMapper.selectMaterialReturnOrderById(returnId);
        if (info == null) {
            return AjaxResult.error("数据不存在");
        }
        if (materialOrderSupport.noEntPermission(info.getEntCode())) {
            return AjaxResult.error("无权限查看该企业数据");
        }
        info.setItemList(materialOrderMapper.selectMaterialReturnOrderItems(returnId));
        info.setAnnexInfoList(materialOrderSupport.listAnnex(info.getReturnId(), AnnexTypeEnum.materialReturnOrder.name));
        info.setOperateLogList(materialOrderSupport.listOperateLog(info.getReturnId(), info.getReturnNo()));
        return AjaxResult.success(info);
    }

    @Override
    @Log(title = "物资归还单", businessType = BusinessType.INSERT)
    @Transactional(rollbackFor = Exception.class)
    public AjaxResult insertMaterialReturnOrder(MaterialReturnOrder info) {
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
            return AjaxResult.error("归还明细不能为空");
        }
        try {
            Integer newStatus = Integer.valueOf(2).equals(info.getStatus()) ? 2 : (Integer.valueOf(6).equals(info.getStatus()) ? 6 : 1);
            info.setStatus(newStatus);
            info.setStockEffectStatus(0);
            info.setAuditBy(null);
            info.setAuditTime(null);
            info.setAuditRemark(null);
            prepareReturnOrder(info);
            int count = materialOrderMapper.insertMaterialReturnOrder(info);
            materialOrderMapper.batchInsertMaterialReturnOrderItems(info.getItemList());
            if (count > 0) {
                materialOrderSupport.updateAnnex(info.getReturnId(), AnnexTypeEnum.materialReturnOrder.name, info.getAnnexIds());
            }
            return AjaxResult.success(info.getReturnId());
        } catch (RuntimeException e) {
            return AjaxResult.error(e.getMessage());
        }
    }

    @Override
    @Log(title = "物资归还单", businessType = BusinessType.UPDATE)
    @Transactional(rollbackFor = Exception.class)
    public AjaxResult updateMaterialReturnOrder(MaterialReturnOrder info) {
        if (info == null || StringUtils.isEmpty(info.getReturnId())) {
            return AjaxResult.error("归还单ID不能为空");
        }
        MaterialReturnOrder old = materialOrderMapper.selectMaterialReturnOrderById(info.getReturnId());
        if (old == null) {
            return AjaxResult.error("数据不存在");
        }
        if (materialOrderSupport.noEntPermission(old.getEntCode())) {
            return AjaxResult.error("无权限操作该企业数据");
        }
        if (StringUtils.isNotEmpty(info.getReturnNo()) && !info.getReturnNo().equals(old.getReturnNo())) {
            return AjaxResult.error("归还单号不允许修改");
        }
        if (StringUtils.isNotEmpty(info.getApplyId()) && !info.getApplyId().equals(old.getApplyId())) {
            return AjaxResult.error("关联申请单不允许修改");
        }
        if (StringUtils.isNotEmpty(info.getOutId()) && !info.getOutId().equals(old.getOutId())) {
            return AjaxResult.error("关联出库单不允许修改");
        }
        if (Integer.valueOf(0).equals(old.getStatus())) {
            if (Integer.valueOf(1).equals(info.getStatus()) && (info.getItemList() == null || info.getItemList().isEmpty())) {
                if (Integer.valueOf(1).equals(old.getStockEffectStatus())) {
                    return AjaxResult.error("已生效归还单不允许回退");
                }
                MaterialReturnOrder update = new MaterialReturnOrder();
                update.setReturnId(old.getReturnId());
                update.setStatus(1);
                update.setAuditBy(null);
                update.setAuditTime(null);
                update.setRemark(info.getRemark());
                update.setStockEffectStatus(0);
                materialOrderMapper.updateMaterialReturnOrder(update);
                return AjaxResult.success(old.getReturnId());
            }
            return AjaxResult.error("已作废归还单不允许直接修改，请先回退到草稿再编辑");
        }
        if (Integer.valueOf(6).equals(old.getStatus())) {
            if (Integer.valueOf(2).equals(info.getStatus()) && (info.getItemList() == null || info.getItemList().isEmpty())) {
                if (StringUtils.isNotEmpty(old.getOutId())) {
                    MaterialOutOrder outOrder = materialOrderMapper.selectMaterialOutOrderById(old.getOutId());
                    if (outOrder == null) {
                        return AjaxResult.error("关联出库单不存在");
                    }
                    if (!Integer.valueOf(3).equals(outOrder.getStatus()) && !Integer.valueOf(4).equals(outOrder.getStatus())) {
                        return AjaxResult.error("关联出库单未生效，不能提交归还");
                    }
                }
                MaterialReturnOrder update = new MaterialReturnOrder();
                update.setReturnId(old.getReturnId());
                update.setStatus(2);
                update.setAuditBy(null);
                update.setAuditTime(null);
                update.setRemark(info.getRemark());
                update.setStockEffectStatus(0);
                materialOrderMapper.updateMaterialReturnOrder(update);
                return AjaxResult.success(old.getReturnId());
            }
            if (Integer.valueOf(1).equals(info.getStatus()) && (info.getItemList() == null || info.getItemList().isEmpty())) {
                if (Integer.valueOf(1).equals(old.getStockEffectStatus())) {
                    return AjaxResult.error("已生效归还单不允许回退");
                }
                MaterialReturnOrder update = new MaterialReturnOrder();
                update.setReturnId(old.getReturnId());
                update.setStatus(1);
                update.setAuditBy(null);
                update.setAuditTime(null);
                update.setRemark(info.getRemark());
                update.setStockEffectStatus(0);
                materialOrderMapper.updateMaterialReturnOrder(update);
                return AjaxResult.success(old.getReturnId());
            }
            return AjaxResult.error("已保存归还单不允许直接修改，请先回退到草稿再编辑");
        }
        if (!Integer.valueOf(1).equals(old.getStatus())) {
            return AjaxResult.error("仅草稿归还单允许修改");
        }
        if (Integer.valueOf(1).equals(old.getStockEffectStatus())) {
            return AjaxResult.error("已生效归还单不允许修改");
        }
        if (Integer.valueOf(2).equals(info.getStatus()) && (info.getItemList() == null || info.getItemList().isEmpty())) {
            if (StringUtils.isNotEmpty(old.getOutId())) {
                MaterialOutOrder outOrder = materialOrderMapper.selectMaterialOutOrderById(old.getOutId());
                if (outOrder == null) {
                    return AjaxResult.error("关联出库单不存在");
                }
                if (!Integer.valueOf(3).equals(outOrder.getStatus()) && !Integer.valueOf(4).equals(outOrder.getStatus())) {
                    return AjaxResult.error("关联出库单未生效，不能提交归还");
                }
            }
            MaterialReturnOrder update = new MaterialReturnOrder();
            update.setReturnId(old.getReturnId());
            update.setStatus(2);
            update.setAuditBy(null);
            update.setAuditTime(null);
            update.setRemark(info.getRemark());
            update.setStockEffectStatus(0);
            materialOrderMapper.updateMaterialReturnOrder(update);
            return AjaxResult.success(old.getReturnId());
        }
        if (info.getItemList() == null || info.getItemList().isEmpty()) {
            return AjaxResult.error("归还明细不能为空");
        }
        try {
            Integer newStatus = Integer.valueOf(2).equals(info.getStatus()) ? 2 : (Integer.valueOf(6).equals(info.getStatus()) ? 6 : 1);
            info.setEntCode(old.getEntCode());
            info.setReturnNo(old.getReturnNo());
            info.setApplyId(old.getApplyId());
            info.setOutId(old.getOutId());
            info.setStatus(newStatus);
            info.setStockEffectStatus(0);
            info.setAuditBy(null);
            info.setAuditTime(null);
            info.setAuditRemark(null);
            if (Integer.valueOf(2).equals(newStatus) && StringUtils.isNotEmpty(old.getOutId())) {
                MaterialOutOrder outOrder = materialOrderMapper.selectMaterialOutOrderById(old.getOutId());
                if (outOrder == null) {
                    throw new RuntimeException("关联出库单不存在");
                }
                if (!Integer.valueOf(3).equals(outOrder.getStatus()) && !Integer.valueOf(4).equals(outOrder.getStatus())) {
                    throw new RuntimeException("关联出库单未生效，不能提交归还");
                }
            }
            prepareReturnOrder(info);
            materialOrderMapper.updateMaterialReturnOrder(info);
            materialOrderMapper.deleteMaterialReturnOrderItems(info.getReturnId());
            materialOrderMapper.batchInsertMaterialReturnOrderItems(info.getItemList());
            materialOrderSupport.updateAnnex(info.getReturnId(), AnnexTypeEnum.materialReturnOrder.name, info.getAnnexIds());
            return AjaxResult.success(info.getReturnId());
        } catch (RuntimeException e) {
            return AjaxResult.error(e.getMessage());
        }
    }

    @Override
    @Log(title = "物资归还单", businessType = BusinessType.DELETE)
    @Transactional(rollbackFor = Exception.class)
    public AjaxResult deleteMaterialReturnOrder(MaterialReturnOrder info) {
        if (info == null || StringUtils.isEmpty(info.getReturnId())) {
            return AjaxResult.error("归还单ID不能为空");
        }
        MaterialReturnOrder old = materialOrderMapper.selectMaterialReturnOrderById(info.getReturnId());
        if (old == null) {
            return AjaxResult.error("数据不存在");
        }
        if (materialOrderSupport.noEntPermission(old.getEntCode())) {
            return AjaxResult.error("无权限操作该企业数据");
        }
        if (!Integer.valueOf(1).equals(old.getStatus()) && !Integer.valueOf(6).equals(old.getStatus()) && !Integer.valueOf(0).equals(old.getStatus())) {
            return AjaxResult.error("仅草稿、已保存或已作废归还单允许删除");
        }
        if (Integer.valueOf(1).equals(old.getStockEffectStatus())) {
            return AjaxResult.error("已生效归还单不允许删除");
        }
        materialOrderMapper.deleteMaterialReturnOrderItems(info.getReturnId());
        materialOrderSupport.updateAnnex(info.getReturnId(), AnnexTypeEnum.materialReturnOrder.name, null);
        return AjaxResult.success(materialOrderMapper.deleteMaterialReturnOrderById(info.getReturnId()));
    }

    @Override
    @Log(title = "物资归还单导出", businessType = BusinessType.EXPORT)
    public void exportMaterialReturnOrder(MaterialBizReq req, HttpServletResponse response) {
        req = materialOrderSupport.initReq(req);
        if (req == null) {
            return;
        }
        List<MaterialReturnOrder> list = materialOrderMapper.selectMaterialReturnOrderList(req);
        materialOrderSupport.fillEntNameByCode(list);
        List<List<String>> rows = new ArrayList<>();
        list.forEach(item -> rows.add(List.of(
                defaultVal(item.getEntName()),
                defaultVal(item.getReturnNo()),
                stringVal(item.getApplyTime()),
                stringVal(item.getReturnTime()),
                defaultVal(item.getReturnUser()),
                defaultVal(item.getAuditBy()),
                defaultVal(item.getHandlerUser()),
                defaultVal(item.getOutId()),
                numberVal(item.getTotalQty()),
                numberVal(item.getStockInQty()),
                statusDesc(item.getStatus()),
                defaultVal(item.getRemark())
        )));
        materialOrderSupport.exportSimpleExcel("物资归还单.xlsx",
                new String[]{"企业", "归还单号", "申请时间", "归还时间", "归还人员", "审核人员", "物资处理人员", "关联出库单ID", "归还总数量", "回补库存数量", "状态", "备注"},
                rows, response);
    }

    private void prepareReturnOrder(MaterialReturnOrder info) {
        MaterialWarehouse warehouse = materialWarehouseMapper.selectMaterialWarehouseById(info.getWarehouseId());
        if (warehouse == null) {
            throw new RuntimeException("仓库不存在");
        }
        if (Integer.valueOf(1).equals(warehouse.getStatus())) {
            throw new RuntimeException("停用仓库不能创建归还单");
        }
        if (info.getReturnTime() == null) {
            info.setReturnTime(LocalDateTime.now());
        }
        if (StringUtils.isEmpty(info.getReturnNo())) {
            info.setReturnNo(materialOrderSupport.buildBizNo("GH"));
        }
        if (info.getStatus() == null) {
            info.setStatus(1);
        }
        info.setStockEffectStatus(info.getStatus() >= 3 ? 1 : 0);
        if (StringUtils.isEmpty(info.getReturnId())) {
            info.setReturnId(UlidCreator.getMonotonicUlid().toString());
        }
        double totalQty = 0D;
        double stockInQty = 0D;
        int sort = 1;
        for (MaterialReturnOrderItem item : info.getItemList()) {
            if (item.getReturnQty() == null || item.getReturnQty() <= 0) {
                throw new RuntimeException("归还数量必须大于0");
            }
            if (item.getStockInQty() == null) {
                item.setStockInQty(item.getReturnQty());
            }
            if (item.getStockInQty() > item.getReturnQty()) {
                throw new RuntimeException("回补库存数量不能大于归还数量");
            }
            item.setReturnItemId(UlidCreator.getMonotonicUlid().toString());
            item.setReturnId(info.getReturnId());
            item.setSortNum(sort++);
            if (StringUtils.isNotEmpty(item.getOutItemId())) {
                MaterialOutOrderItem outItem = materialOrderMapper.selectMaterialOutOrderItemById(item.getOutItemId());
                if (outItem == null) {
                    throw new RuntimeException("关联出库明细不存在");
                }
                double canReturnQty = (outItem.getOutQty() == null ? 0D : outItem.getOutQty()) - (outItem.getReturnedQty() == null ? 0D : outItem.getReturnedQty());
                if (canReturnQty < item.getReturnQty()) {
                    throw new RuntimeException("归还数量超出可归还数量");
                }
                item.setMaterialId(outItem.getMaterialId());
                item.setMaterialCode(outItem.getMaterialCode());
                item.setMaterialName(outItem.getMaterialName());
                item.setBrand(outItem.getBrand());
                item.setModelSpec(outItem.getModelSpec());
                item.setCategoryName(outItem.getCategoryName());
                item.setUnit(outItem.getUnit());
                item.setOutQty(outItem.getOutQty());
                item.setCanReturnQty(canReturnQty);
            }
            totalQty += item.getReturnQty();
            stockInQty += item.getStockInQty();
        }
        info.setTotalQty(totalQty);
        info.setStockInQty(stockInQty);
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
