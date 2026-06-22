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
import com.zkhf.epmis.process.material.domain.MaterialApplyOrder;
import com.zkhf.epmis.process.material.domain.MaterialApplyOrderItem;
import com.zkhf.epmis.process.material.domain.MaterialBizReq;
import com.zkhf.epmis.process.material.domain.MaterialInfo;
import com.zkhf.epmis.process.material.service.MaterialApplyOrderService;
import com.zkhf.epmis.process.material.service.MaterialOrderSupport;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
public class MaterialApplyOrderServiceImpl implements MaterialApplyOrderService {

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

    private MaterialInfoMapper materialInfoMapper;
    @Autowired
    public void setMaterialInfoMapper(MaterialInfoMapper materialInfoMapper) {
        this.materialInfoMapper = materialInfoMapper;
    }

    @Override
    public AjaxResult selectMaterialApplyOrderList(MaterialBizReq req) {
        req = materialOrderSupport.initReq(req);
        if (req == null) {
            return AjaxResult.success(new ArrayList<>());
        }
        boolean page = PageUtils.startPageCheckExists();
        List<MaterialApplyOrder> list = materialOrderMapper.selectMaterialApplyOrderList(req);
        materialOrderSupport.fillEntNameByCode(list);
        return PageUtils.getAjaxResult(list, page);
    }

    @Override
    public AjaxResult selectMaterialApplyOrderDetail(String applyId) {
        MaterialApplyOrder info = materialOrderMapper.selectMaterialApplyOrderById(applyId);
        if (info == null) {
            return AjaxResult.error("数据不存在");
        }
        if (materialOrderSupport.noEntPermission(info.getEntCode())) {
            return AjaxResult.error("无权限查看该企业数据");
        }
        info.setItemList(materialOrderMapper.selectMaterialApplyOrderItems(applyId));
        info.setAnnexInfoList(materialOrderSupport.listAnnex(info.getApplyId(), AnnexTypeEnum.materialApplyOrder.name));
        info.setOperateLogList(materialOrderSupport.listOperateLog(info.getApplyId(), info.getApplyNo()));
        return AjaxResult.success(info);
    }

    @Override
    @Log(title = "物资申请单", businessType = BusinessType.INSERT)
    @Transactional(rollbackFor = Exception.class)
    public AjaxResult insertMaterialApplyOrder(MaterialApplyOrder info) {
        if (info == null) {
            return AjaxResult.error("未知的参数");
        }
        if (StringUtils.isEmpty(info.getEntCode())) {
            return AjaxResult.error("企业编码不能为空");
        }
        if (materialOrderSupport.noEntPermission(info.getEntCode())) {
            return AjaxResult.error("无权限操作该企业数据");
        }
        if (info.getItemList() == null || info.getItemList().isEmpty()) {
            return AjaxResult.error("申请明细不能为空");
        }
        try {
            Integer newStatus = Integer.valueOf(6).equals(info.getAuditStatus()) ? 6 : 1;
            info.setAuditStatus(newStatus);
            info.setAuditBy(null);
            info.setAuditTime(null);
            info.setAuditRemark(null);
            prepareApplyOrder(info);
            int count = materialOrderMapper.insertMaterialApplyOrder(info);
            materialOrderMapper.batchInsertMaterialApplyOrderItems(info.getItemList());
            if (count > 0) {
                materialOrderSupport.updateAnnex(info.getApplyId(), AnnexTypeEnum.materialApplyOrder.name, info.getAnnexIds());
            }
            return AjaxResult.success(info.getApplyId());
        } catch (RuntimeException e) {
            return AjaxResult.error(e.getMessage());
        }
    }

    @Override
    @Log(title = "物资申请单", businessType = BusinessType.UPDATE)
    @Transactional(rollbackFor = Exception.class)
    public AjaxResult updateMaterialApplyOrder(MaterialApplyOrder info) {
        if (info == null || StringUtils.isEmpty(info.getApplyId())) {
            return AjaxResult.error("申请单ID不能为空");
        }
        MaterialApplyOrder old = materialOrderMapper.selectMaterialApplyOrderById(info.getApplyId());
        if (old == null) {
            return AjaxResult.error("数据不存在");
        }
        if (materialOrderSupport.noEntPermission(old.getEntCode())) {
            return AjaxResult.error("无权限操作该企业数据");
        }
        if (StringUtils.isNotEmpty(info.getApplyNo()) && !info.getApplyNo().equals(old.getApplyNo())) {
            return AjaxResult.error("申请单号不允许修改");
        }
        if (Integer.valueOf(5).equals(old.getAuditStatus())) {
            if (Integer.valueOf(1).equals(info.getAuditStatus()) && (info.getItemList() == null || info.getItemList().isEmpty())) {
                MaterialApplyOrder update = new MaterialApplyOrder();
                update.setApplyId(old.getApplyId());
                update.setAuditStatus(1);
                update.setAuditBy(null);
                update.setAuditTime(null);
                update.setRemark(info.getRemark());
                materialOrderMapper.updateMaterialApplyOrder(update);
                return AjaxResult.success(old.getApplyId());
            }
            return AjaxResult.error("已取消申请单不允许直接修改，请先回退到草稿再编辑");
        }
        if (Integer.valueOf(6).equals(old.getAuditStatus())) {
            if (Integer.valueOf(2).equals(info.getAuditStatus()) && (info.getItemList() == null || info.getItemList().isEmpty())) {
                MaterialApplyOrder update = new MaterialApplyOrder();
                update.setApplyId(old.getApplyId());
                update.setAuditStatus(2);
                update.setAuditBy(null);
                update.setAuditTime(null);
                update.setRemark(info.getRemark());
                materialOrderMapper.updateMaterialApplyOrder(update);
                return AjaxResult.success(old.getApplyId());
            }
            if (Integer.valueOf(1).equals(info.getAuditStatus()) && (info.getItemList() == null || info.getItemList().isEmpty())) {
                MaterialApplyOrder update = new MaterialApplyOrder();
                update.setApplyId(old.getApplyId());
                update.setAuditStatus(1);
                update.setAuditBy(null);
                update.setAuditTime(null);
                update.setRemark(info.getRemark());
                materialOrderMapper.updateMaterialApplyOrder(update);
                return AjaxResult.success(old.getApplyId());
            }
            return AjaxResult.error("已保存申请单不允许直接修改，请先回退到草稿再编辑");
        }
        if (Integer.valueOf(2).equals(old.getAuditStatus()) || Integer.valueOf(3).equals(old.getAuditStatus())) {
            return AjaxResult.error("待审核或已审核通过的申请单不允许修改");
        }
        if (materialOrderMapper.countMaterialApplyOrderRef(info.getApplyId()) > 0) {
            return AjaxResult.error("申请单已有关联出库，不允许修改");
        }
        if (Integer.valueOf(2).equals(info.getAuditStatus()) && (info.getItemList() == null || info.getItemList().isEmpty())) {
            MaterialApplyOrder update = new MaterialApplyOrder();
            update.setApplyId(old.getApplyId());
            update.setAuditStatus(2);
            update.setAuditBy(null);
            update.setAuditTime(null);
            update.setRemark(info.getRemark());
            materialOrderMapper.updateMaterialApplyOrder(update);
            return AjaxResult.success(old.getApplyId());
        }
        if (info.getItemList() == null || info.getItemList().isEmpty()) {
            return AjaxResult.error("申请明细不能为空");
        }
        try {
            Integer newStatus = Integer.valueOf(2).equals(info.getAuditStatus()) ? 2 : (Integer.valueOf(6).equals(info.getAuditStatus()) ? 6 : 1);
            info.setEntCode(old.getEntCode());
            info.setApplyNo(old.getApplyNo());
            info.setAuditStatus(newStatus);
            info.setAuditBy(null);
            info.setAuditTime(null);
            info.setAuditRemark(null);
            prepareApplyOrder(info);
            info.setOutStatus(null);
            info.setReturnStatus(null);
            materialOrderMapper.updateMaterialApplyOrder(info);
            materialOrderMapper.deleteMaterialApplyOrderItems(info.getApplyId());
            materialOrderMapper.batchInsertMaterialApplyOrderItems(info.getItemList());
            materialOrderSupport.updateAnnex(info.getApplyId(), AnnexTypeEnum.materialApplyOrder.name, info.getAnnexIds());
            return AjaxResult.success(info.getApplyId());
        } catch (RuntimeException e) {
            return AjaxResult.error(e.getMessage());
        }
    }

    @Override
    @Log(title = "物资申请单", businessType = BusinessType.DELETE)
    @Transactional(rollbackFor = Exception.class)
    public AjaxResult deleteMaterialApplyOrder(MaterialApplyOrder info) {
        if (info == null || StringUtils.isEmpty(info.getApplyId())) {
            return AjaxResult.error("申请单ID不能为空");
        }
        MaterialApplyOrder old = materialOrderMapper.selectMaterialApplyOrderById(info.getApplyId());
        if (old == null) {
            return AjaxResult.error("数据不存在");
        }
        if (materialOrderSupport.noEntPermission(old.getEntCode())) {
            return AjaxResult.error("无权限操作该企业数据");
        }
        if (Integer.valueOf(2).equals(old.getAuditStatus()) || Integer.valueOf(3).equals(old.getAuditStatus())) {
            return AjaxResult.error("待审核或已审核通过的申请单不允许删除");
        }
        if (!Integer.valueOf(1).equals(old.getAuditStatus()) && !Integer.valueOf(6).equals(old.getAuditStatus()) && !Integer.valueOf(5).equals(old.getAuditStatus())) {
            return AjaxResult.error("仅草稿、已保存或已取消申请单允许删除");
        }
        if (materialOrderMapper.countMaterialApplyOrderRef(info.getApplyId()) > 0) {
            return AjaxResult.error("申请单已有关联出库，不允许删除");
        }
        materialOrderMapper.deleteMaterialApplyOrderItems(info.getApplyId());
        materialOrderSupport.updateAnnex(info.getApplyId(), AnnexTypeEnum.materialApplyOrder.name, null);
        return AjaxResult.success(materialOrderMapper.deleteMaterialApplyOrderById(info.getApplyId()));
    }

    @Override
    @Log(title = "物资申请单导出", businessType = BusinessType.EXPORT)
    public void exportMaterialApplyOrder(MaterialBizReq req, HttpServletResponse response) {
        req = materialOrderSupport.initReq(req);
        if (req == null) {
            return;
        }
        List<MaterialApplyOrder> list = materialOrderMapper.selectMaterialApplyOrderList(req);
        materialOrderSupport.fillEntNameByCode(list);
        try (org.apache.poi.xssf.usermodel.XSSFWorkbook workbook = new org.apache.poi.xssf.usermodel.XSSFWorkbook()) {
            org.apache.poi.ss.usermodel.Sheet sheet1 = workbook.createSheet("申请单");
            String[] headers1 = new String[]{"企业", "申请单号", "申请时间", "申请事由", "申请人", "审核状态", "审核人", "审核时间", "出库状态", "归还状态", "申请总数", "备注"};
            org.apache.poi.ss.usermodel.Row head1 = sheet1.createRow(0);
            for (int i = 0; i < headers1.length; i++) {
                head1.createCell(i).setCellValue(headers1[i]);
                sheet1.setColumnWidth(i, 20 * 256);
            }
            int rowIdx1 = 1;
            for (MaterialApplyOrder item : list) {
                org.apache.poi.ss.usermodel.Row row = sheet1.createRow(rowIdx1++);
                List<String> data = List.of(
                        defaultVal(item.getEntName()),
                        defaultVal(item.getApplyNo()),
                        stringVal(item.getApplyTime()),
                        defaultVal(item.getApplyReason()),
                        defaultVal(item.getApplyUser()),
                        auditStatusDesc(item.getAuditStatus()),
                        defaultVal(item.getAuditBy()),
                        stringVal(item.getAuditTime()),
                        outStatusDesc(item.getOutStatus()),
                        returnStatusDesc(item.getReturnStatus()),
                        numberVal(item.getTotalQty()),
                        defaultVal(item.getRemark())
                );
                for (int j = 0; j < data.size(); j++) {
                    row.createCell(j).setCellValue(data.get(j) == null ? "" : data.get(j));
                }
            }

            org.apache.poi.ss.usermodel.Sheet sheet2 = workbook.createSheet("申请明细");
            String[] headers2 = new String[]{"企业", "申请单号", "物资编号", "物资名称", "品牌", "规格型号", "分类", "单位", "申请数量", "用途说明", "备注"};
            org.apache.poi.ss.usermodel.Row head2 = sheet2.createRow(0);
            for (int i = 0; i < headers2.length; i++) {
                head2.createCell(i).setCellValue(headers2[i]);
                sheet2.setColumnWidth(i, 20 * 256);
            }
            int rowIdx2 = 1;
            for (MaterialApplyOrder order : list) {
                List<MaterialApplyOrderItem> items = materialOrderMapper.selectMaterialApplyOrderItems(order.getApplyId());
                if (items == null) {
                    continue;
                }
                for (MaterialApplyOrderItem it : items) {
                    org.apache.poi.ss.usermodel.Row row = sheet2.createRow(rowIdx2++);
                    List<String> data = List.of(
                            defaultVal(order.getEntName()),
                            defaultVal(order.getApplyNo()),
                            defaultVal(it.getMaterialCode()),
                            defaultVal(it.getMaterialName()),
                            defaultVal(it.getBrand()),
                            defaultVal(it.getModelSpec()),
                            defaultVal(it.getCategoryName()),
                            defaultVal(it.getUnit()),
                            numberVal(it.getApplyQty()),
                            defaultVal(it.getPurposeDesc()),
                            defaultVal(it.getRemark())
                    );
                    for (int j = 0; j < data.size(); j++) {
                        row.createCell(j).setCellValue(data.get(j) == null ? "" : data.get(j));
                    }
                }
            }
            materialOrderSupport.writeWorkbook("物资申请单.xlsx", workbook, response);
        } catch (Exception e) {
            org.slf4j.LoggerFactory.getLogger(getClass()).error("物资申请单导出失败", e);
        }
    }

    private void prepareApplyOrder(MaterialApplyOrder info) {
        if (info.getApplyTime() == null) {
            info.setApplyTime(LocalDateTime.now());
        }
        if (StringUtils.isEmpty(info.getApplyNo())) {
            info.setApplyNo(materialOrderSupport.buildBizNo("SQ"));
        }
        info.setAuditStatus(1);
        if (StringUtils.isEmpty(info.getApplyId())) {
            info.setApplyId(UlidCreator.getMonotonicUlid().toString());
        }
        double totalQty = 0D;
        int sort = 1;
        for (MaterialApplyOrderItem item : info.getItemList()) {
            MaterialInfo material = materialInfoMapper.selectMaterialInfoById(item.getMaterialId());
            if (material == null) {
                throw new RuntimeException("物资不存在");
            }
            if (Integer.valueOf(1).equals(material.getStatus())) {
                throw new RuntimeException("停用物资不能继续申请");
            }
            if (item.getApplyQty() == null || item.getApplyQty() <= 0) {
                throw new RuntimeException("申请数量必须大于0");
            }
            item.setApplyItemId(UlidCreator.getMonotonicUlid().toString());
            item.setApplyId(info.getApplyId());
            item.setMaterialCode(material.getMaterialCode());
            item.setMaterialName(material.getMaterialName());
            item.setBrand(material.getBrand());
            item.setModelSpec(material.getModelSpec());
            item.setCategoryName(material.getCategoryName());
            item.setUnit(material.getUnit());
            item.setOutQty(0D);
            item.setReturnQty(0D);
            item.setSortNum(sort++);
            totalQty += item.getApplyQty();
        }
        info.setOutStatus(0);
        info.setReturnStatus(0);
        info.setTotalQty(totalQty);
    }

    private MaterialApplyOrder getApplyOrder(MaterialApplyOrder info) {
        if (info == null || StringUtils.isEmpty(info.getApplyId())) {
            return null;
        }
        MaterialApplyOrder old = materialOrderMapper.selectMaterialApplyOrderById(info.getApplyId());
        if (old != null && materialOrderSupport.noEntPermission(old.getEntCode())) {
            throw new RuntimeException("无权限操作该企业数据");
        }
        return old;
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

    private String auditStatusDesc(Integer status) {
        if (Integer.valueOf(1).equals(status)) { return "草稿"; }
        if (Integer.valueOf(2).equals(status)) { return "待审核"; }
        if (Integer.valueOf(3).equals(status)) { return "审核通过"; }
        if (Integer.valueOf(4).equals(status)) { return "审核驳回"; }
        if (Integer.valueOf(5).equals(status)) { return "已取消"; }
        if (Integer.valueOf(6).equals(status)) { return "已保存"; }
        return "";
    }

    private String outStatusDesc(Integer status) {
        if (Integer.valueOf(1).equals(status)) { return "部分出库"; }
        if (Integer.valueOf(2).equals(status)) { return "已出库"; }
        return "未出库";
    }

    private String returnStatusDesc(Integer status) {
        if (Integer.valueOf(1).equals(status)) { return "部分归还"; }
        if (Integer.valueOf(2).equals(status)) { return "已归还"; }
        return "未归还";
    }
}
