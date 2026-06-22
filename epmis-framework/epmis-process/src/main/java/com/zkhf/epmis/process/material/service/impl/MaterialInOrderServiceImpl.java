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
import com.zkhf.epmis.process.material.service.MaterialInOrderService;
import com.zkhf.epmis.process.material.service.MaterialOrderSupport;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
public class MaterialInOrderServiceImpl implements MaterialInOrderService {

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
    public AjaxResult selectMaterialInOrderList(MaterialBizReq req) {
        req = materialOrderSupport.initReq(req);
        if (req == null) {
            return AjaxResult.success(new ArrayList<>());
        }
        boolean page = PageUtils.startPageCheckExists();
        List<MaterialInOrder> list = materialOrderMapper.selectMaterialInOrderList(req);
        materialOrderSupport.fillEntNameByCode(list);
        return PageUtils.getAjaxResult(list, page);
    }

    @Override
    public AjaxResult selectMaterialInOrderDetail(String inId) {
        MaterialInOrder info = materialOrderMapper.selectMaterialInOrderById(inId);
        if (info == null) {
            return AjaxResult.error("数据不存在");
        }
        if (materialOrderSupport.noEntPermission(info.getEntCode())) {
            return AjaxResult.error("无权限查看该企业数据");
        }
        info.setItemList(materialOrderMapper.selectMaterialInOrderItems(inId));
        info.setAnnexInfoList(materialOrderSupport.listAnnex(info.getInId(), AnnexTypeEnum.materialInOrder.name));
        info.setOperateLogList(materialOrderSupport.listOperateLog(info.getInId(), info.getInNo()));
        return AjaxResult.success(info);
    }

    @Override
    @Log(title = "物资入库单", businessType = BusinessType.INSERT)
    @Transactional(rollbackFor = Exception.class)
    public AjaxResult insertMaterialInOrder(MaterialInOrder info) {
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
            return AjaxResult.error("入库明细不能为空");
        }
        try {
            Integer newStatus = Integer.valueOf(2).equals(info.getStatus()) ? 2 : (Integer.valueOf(6).equals(info.getStatus()) ? 6 : 1);
            info.setStatus(newStatus);
            info.setStockEffectStatus(0);
            info.setAuditBy(null);
            info.setAuditTime(null);
            info.setAuditRemark(null);
            prepareInOrder(info);
            int count = materialOrderMapper.insertMaterialInOrder(info);
            materialOrderMapper.batchInsertMaterialInOrderItems(info.getItemList());
            if (count > 0) {
                materialOrderSupport.updateAnnex(info.getInId(), AnnexTypeEnum.materialInOrder.name, info.getAnnexIds());
            }
            return AjaxResult.success(info.getInId());
        } catch (RuntimeException e) {
            return AjaxResult.error(e.getMessage());
        }
    }

    @Override
    @Log(title = "物资入库单", businessType = BusinessType.UPDATE)
    @Transactional(rollbackFor = Exception.class)
    public AjaxResult updateMaterialInOrder(MaterialInOrder info) {
        if (info == null || StringUtils.isEmpty(info.getInId())) {
            return AjaxResult.error("入库单ID不能为空");
        }
        MaterialInOrder old = materialOrderMapper.selectMaterialInOrderById(info.getInId());
        if (old == null) {
            return AjaxResult.error("数据不存在");
        }
        if (materialOrderSupport.noEntPermission(old.getEntCode())) {
            return AjaxResult.error("无权限操作该企业数据");
        }
        if (StringUtils.isNotEmpty(info.getInNo()) && !info.getInNo().equals(old.getInNo())) {
            return AjaxResult.error("入库单号不允许修改");
        }
        if (Integer.valueOf(0).equals(old.getStatus())) {
            if (Integer.valueOf(1).equals(info.getStatus()) && (info.getItemList() == null || info.getItemList().isEmpty())) {
                if (Integer.valueOf(1).equals(old.getStockEffectStatus())) {
                    return AjaxResult.error("已生效入库单不允许回退");
                }
                MaterialInOrder update = new MaterialInOrder();
                update.setInId(old.getInId());
                update.setStatus(1);
                update.setAuditBy(null);
                update.setAuditTime(null);
                update.setRemark(info.getRemark());
                update.setStockEffectStatus(0);
                materialOrderMapper.updateMaterialInOrder(update);
                return AjaxResult.success(old.getInId());
            }
            return AjaxResult.error("已作废入库单不允许直接修改，请先回退到草稿再编辑");
        }
        if (Integer.valueOf(6).equals(old.getStatus())) {
            if (Integer.valueOf(2).equals(info.getStatus()) && (info.getItemList() == null || info.getItemList().isEmpty())) {
                MaterialInOrder update = new MaterialInOrder();
                update.setInId(old.getInId());
                update.setStatus(2);
                update.setAuditBy(null);
                update.setAuditTime(null);
                update.setRemark(info.getRemark());
                update.setStockEffectStatus(0);
                materialOrderMapper.updateMaterialInOrder(update);
                return AjaxResult.success(old.getInId());
            }
            if (Integer.valueOf(1).equals(info.getStatus()) && (info.getItemList() == null || info.getItemList().isEmpty())) {
                if (Integer.valueOf(1).equals(old.getStockEffectStatus())) {
                    return AjaxResult.error("已生效入库单不允许回退");
                }
                MaterialInOrder update = new MaterialInOrder();
                update.setInId(old.getInId());
                update.setStatus(1);
                update.setAuditBy(null);
                update.setAuditTime(null);
                update.setRemark(info.getRemark());
                update.setStockEffectStatus(0);
                materialOrderMapper.updateMaterialInOrder(update);
                return AjaxResult.success(old.getInId());
            }
            return AjaxResult.error("已保存入库单不允许直接修改，请先回退到草稿再编辑");
        }
        if (!Integer.valueOf(1).equals(old.getStatus())) {
            return AjaxResult.error("仅草稿入库单允许修改");
        }
        if (Integer.valueOf(1).equals(old.getStockEffectStatus())) {
            return AjaxResult.error("已生效入库单不允许修改");
        }
        if (Integer.valueOf(2).equals(info.getStatus()) && (info.getItemList() == null || info.getItemList().isEmpty())) {
            MaterialInOrder update = new MaterialInOrder();
            update.setInId(old.getInId());
            update.setStatus(2);
            update.setAuditBy(null);
            update.setAuditTime(null);
            update.setRemark(info.getRemark());
            update.setStockEffectStatus(0);
            materialOrderMapper.updateMaterialInOrder(update);
            return AjaxResult.success(old.getInId());
        }
        if (info.getItemList() == null || info.getItemList().isEmpty()) {
            return AjaxResult.error("入库明细不能为空");
        }
        try {
            Integer newStatus = Integer.valueOf(2).equals(info.getStatus()) ? 2 : (Integer.valueOf(6).equals(info.getStatus()) ? 6 : 1);
            info.setEntCode(old.getEntCode());
            info.setInNo(old.getInNo());
            info.setStatus(newStatus);
            info.setStockEffectStatus(0);
            info.setAuditBy(null);
            info.setAuditTime(null);
            info.setAuditRemark(null);
            prepareInOrder(info);
            materialOrderMapper.updateMaterialInOrder(info);
            materialOrderMapper.deleteMaterialInOrderItems(info.getInId());
            materialOrderMapper.batchInsertMaterialInOrderItems(info.getItemList());
            materialOrderSupport.updateAnnex(info.getInId(), AnnexTypeEnum.materialInOrder.name, info.getAnnexIds());
            return AjaxResult.success(info.getInId());
        } catch (RuntimeException e) {
            return AjaxResult.error(e.getMessage());
        }
    }

    @Override
    @Log(title = "物资入库单", businessType = BusinessType.DELETE)
    @Transactional(rollbackFor = Exception.class)
    public AjaxResult deleteMaterialInOrder(MaterialInOrder info) {
        if (info == null || StringUtils.isEmpty(info.getInId())) {
            return AjaxResult.error("入库单ID不能为空");
        }
        MaterialInOrder old = materialOrderMapper.selectMaterialInOrderById(info.getInId());
        if (old == null) {
            return AjaxResult.error("数据不存在");
        }
        if (materialOrderSupport.noEntPermission(old.getEntCode())) {
            return AjaxResult.error("无权限操作该企业数据");
        }
        if (!Integer.valueOf(1).equals(old.getStatus()) && !Integer.valueOf(6).equals(old.getStatus()) && !Integer.valueOf(0).equals(old.getStatus())) {
            return AjaxResult.error("仅草稿、已保存或已作废入库单允许删除");
        }
        if (Integer.valueOf(1).equals(old.getStockEffectStatus())) {
            return AjaxResult.error("已生效入库单不允许删除");
        }
        materialOrderMapper.deleteMaterialInOrderItems(info.getInId());
        materialOrderSupport.updateAnnex(info.getInId(), AnnexTypeEnum.materialInOrder.name, null);
        return AjaxResult.success(materialOrderMapper.deleteMaterialInOrderById(info.getInId()));
    }

    @Override
    @Log(title = "物资入库单导出", businessType = BusinessType.EXPORT)
    public void exportMaterialInOrder(MaterialBizReq req, HttpServletResponse response) {
        req = materialOrderSupport.initReq(req);
        if (req == null) {
            return;
        }
        List<MaterialInOrder> list = materialOrderMapper.selectMaterialInOrderList(req);
        materialOrderSupport.fillEntNameByCode(list);
        List<List<String>> rows = new ArrayList<>();
        list.forEach(item -> rows.add(List.of(
                defaultVal(item.getEntName()),
                defaultVal(item.getInNo()),
                stringVal(item.getInTime()),
                defaultVal(item.getWarehouseName()),
                defaultVal(item.getArrivalNo()),
                defaultVal(item.getPurchaser()),
                defaultVal(item.getInUser()),
                numberVal(item.getTotalQty()),
                numberVal(item.getTotalAmount()),
                statusDesc(item.getStatus()),
                stringVal(item.getAuditTime()),
                defaultVal(item.getRemark())
        )));
        materialOrderSupport.exportSimpleExcel("物资入库单.xlsx",
                new String[]{"企业", "入库单号", "入库时间", "仓库", "到货单号", "采购人员", "入库人员", "入库总数量", "入库总金额", "状态", "审核时间", "备注"},
                rows, response);
    }

    private void prepareInOrder(MaterialInOrder info) {
        MaterialWarehouse warehouse = materialWarehouseMapper.selectMaterialWarehouseById(info.getWarehouseId());
        if (warehouse == null) {
            throw new RuntimeException("仓库不存在");
        }
        if (Integer.valueOf(1).equals(warehouse.getStatus())) {
            throw new RuntimeException("停用仓库不能创建入库单");
        }
        if (info.getInTime() == null) {
            info.setInTime(LocalDateTime.now());
        }
        if (StringUtils.isEmpty(info.getInNo())) {
            info.setInNo(materialOrderSupport.buildBizNo("RK"));
        }
        if (info.getStatus() == null) {
            info.setStatus(1);
        }
        info.setStockEffectStatus(info.getStatus() >= 3 ? 1 : 0);
        if (StringUtils.isEmpty(info.getInId())) {
            info.setInId(UlidCreator.getMonotonicUlid().toString());
        }
        double totalQty = 0D;
        double totalAmount = 0D;
        int sort = 1;
        for (MaterialInOrderItem item : info.getItemList()) {
            MaterialInfo material = materialInfoMapper.selectMaterialInfoById(item.getMaterialId());
            if (material == null) {
                throw new RuntimeException("物资不存在");
            }
            if (Integer.valueOf(1).equals(material.getStatus())) {
                throw new RuntimeException("停用物资不能继续入库");
            }
            if (item.getInQty() == null || item.getInQty() <= 0) {
                throw new RuntimeException("入库数量必须大于0");
            }
            item.setInItemId(UlidCreator.getMonotonicUlid().toString());
            item.setInId(info.getInId());
            item.setMaterialCode(material.getMaterialCode());
            item.setMaterialName(material.getMaterialName());
            item.setBrand(material.getBrand());
            item.setModelSpec(material.getModelSpec());
            item.setCategoryName(material.getCategoryName());
            item.setUnit(material.getUnit());
            if (item.getUnitPrice() == null) {
                item.setUnitPrice(material.getUnitPrice() == null ? 0D : material.getUnitPrice());
            }
            item.setAmount(item.getInQty() * item.getUnitPrice());
            item.setSortNum(sort++);
            totalQty += item.getInQty();
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
}
