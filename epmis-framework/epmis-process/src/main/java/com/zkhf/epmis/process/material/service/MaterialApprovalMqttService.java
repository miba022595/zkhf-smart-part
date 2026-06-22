package com.zkhf.epmis.process.material.service;

import com.alibaba.fastjson2.JSONObject;
import com.zkhf.epmis.core.utils.StringUtils;
import com.zkhf.epmis.process.mapper.material.MaterialOrderMapper;
import com.zkhf.epmis.process.material.domain.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
public class MaterialApprovalMqttService {

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

    /**
     * 处理平台审批结果 MQTT 回调消息。
     * <p>
     * 根据业务类型分发到对应的处理逻辑（领用申请、入库、出库、归还），并按审批状态更新单据状态，必要时回滚库存相关变动。
     * </p>
     *
     * @param messageId MQTT 消息唯一标识
     * @param payload   MQTT 消息体（JSON 字符串）
     */
    @Transactional(rollbackFor = Exception.class)
    public void handleApprovalResult(String messageId, String payload) {
        MaterialApprovalResultMsg msg;
        try {
            msg = JSONObject.parseObject(payload, MaterialApprovalResultMsg.class);
        } catch (Exception e) {
            log.error("parse approval_result mqtt failed: messageId {} payload {}", messageId, payload, e);
            return;
        }
        if (msg == null || StringUtils.isEmpty(msg.getBusinessType()) || StringUtils.isEmpty(msg.getBusinessKey())) {
            return;
        }
        switch (msg.getBusinessType()) {
            case "material_apply" -> handleApply(msg);
            case "material_in" -> handleIn(msg);
            case "material_out" -> handleOut(msg);
            case "material_return" -> handleReturn(msg);
            default -> {
            }
        }
    }

    /**
     * 处理“物资领用申请”审批结果。
     *
     * @param msg 审批结果消息
     */
    private void handleApply(MaterialApprovalResultMsg msg) {
        MaterialApplyOrder order = materialOrderMapper.selectMaterialApplyOrderById(msg.getBusinessKey());
        if (order == null) {
            return;
        }
        if (StringUtils.isNotEmpty(msg.getEntCode()) && !msg.getEntCode().equals(order.getEntCode())) {
            return;
        }
        String status = msg.getStatus();
        LocalDateTime now = LocalDateTime.now();
        if ("PROCESSING".equals(status)) {
            int updated = materialOrderMapper.updateMaterialApplyOrderByAuditResult(order.getApplyId(), 2, null, null, msg.getComment(), 1);
            if (updated <= 0) {
                materialOrderMapper.updateMaterialApplyOrderByAuditResult(order.getApplyId(), 2, null, null, msg.getComment(), 6);
            }
        } else if ("APPROVED".equals(status)) {
            int updated = materialOrderMapper.updateMaterialApplyOrderByAuditResult(order.getApplyId(), 3, safeUserName(msg), now, msg.getComment(), 2);
            if (updated <= 0) {
                materialOrderMapper.updateMaterialApplyOrderByAuditResult(order.getApplyId(), 3, safeUserName(msg), now, msg.getComment(), 1);
                materialOrderMapper.updateMaterialApplyOrderByAuditResult(order.getApplyId(), 3, safeUserName(msg), now, msg.getComment(), 6);
            }
        } else if ("REJECTED".equals(status)) {
            int updated = materialOrderMapper.updateMaterialApplyOrderByAuditResult(order.getApplyId(), 4, safeUserName(msg), now, msg.getComment(), 2);
            if (updated <= 0) {
                materialOrderMapper.updateMaterialApplyOrderByAuditResult(order.getApplyId(), 4, safeUserName(msg), now, msg.getComment(), 1);
                materialOrderMapper.updateMaterialApplyOrderByAuditResult(order.getApplyId(), 4, safeUserName(msg), now, msg.getComment(), 6);
            }
        } else if ("CANCELLED".equals(status)) {
            int updated = materialOrderMapper.updateMaterialApplyOrderByAuditResult(order.getApplyId(), 5, safeUserName(msg), now, msg.getComment(), 2);
            if (updated <= 0) {
                materialOrderMapper.updateMaterialApplyOrderByAuditResult(order.getApplyId(), 5, safeUserName(msg), now, msg.getComment(), 1);
                materialOrderMapper.updateMaterialApplyOrderByAuditResult(order.getApplyId(), 5, safeUserName(msg), now, msg.getComment(), 6);
            }
        }
    }

    /**
     * 处理“物资入库”审批结果。
     *
     * @param msg 审批结果消息
     */
    private void handleIn(MaterialApprovalResultMsg msg) {
        MaterialInOrder order = materialOrderMapper.selectMaterialInOrderById(msg.getBusinessKey());
        if (order == null) {
            return;
        }
        if (StringUtils.isNotEmpty(msg.getEntCode()) && !msg.getEntCode().equals(order.getEntCode())) {
            return;
        }
        String status = msg.getStatus();
        LocalDateTime now = LocalDateTime.now();
        if ("PROCESSING".equals(status)) {
            int updated = materialOrderMapper.updateMaterialInOrderByApproval(order.getInId(), 2, null, null, msg.getComment(), null, 1, null);
            if (updated <= 0) {
                materialOrderMapper.updateMaterialInOrderByApproval(order.getInId(), 2, null, null, msg.getComment(), null, 6, null);
            }
        } else if ("APPROVED".equals(status)) {
            int updated = materialOrderMapper.updateMaterialInOrderByApproval(order.getInId(), 3, safeUserName(msg), now, msg.getComment(), 1, 2, 0);
            if (updated > 0) {
                List<MaterialInOrderItem> items = materialOrderMapper.selectMaterialInOrderItems(order.getInId());
                for (MaterialInOrderItem item : items) {
                    double qty = item.getInQty() == null ? 0D : item.getInQty();
                    if (qty == 0D) {
                        continue;
                    }
                    materialOrderSupport.adjustStock(order.getEntCode(), order.getWarehouseId(), item.getMaterialId(),
                            qty, "IN", order.getInId(), item.getInItemId(), order.getInNo(), "物资入库");
                }
            } else {
                materialOrderMapper.updateMaterialInOrderByApproval(order.getInId(), 3, safeUserName(msg), now, msg.getComment(), null, 2, null);
            }
        } else if ("REJECTED".equals(status) || "CANCELLED".equals(status)) {
            materialOrderMapper.updateMaterialInOrderByApproval(order.getInId(), 1, safeUserName(msg), now, msg.getComment(), 0, 2, null);
        }
    }

    /**
     * 处理“物资出库”审批结果。
     *
     * @param msg 审批结果消息
     */
    private void handleOut(MaterialApprovalResultMsg msg) {
        MaterialOutOrder order = materialOrderMapper.selectMaterialOutOrderById(msg.getBusinessKey());
        if (order == null) {
            return;
        }
        if (StringUtils.isNotEmpty(msg.getEntCode()) && !msg.getEntCode().equals(order.getEntCode())) {
            return;
        }
        String status = msg.getStatus();
        LocalDateTime now = LocalDateTime.now();
        if ("PROCESSING".equals(status)) {
            int updatedFromDraft = materialOrderMapper.updateMaterialOutOrderByApproval(order.getOutId(), 2, null, null, msg.getComment(), null, 1, null);
            if (updatedFromDraft > 0) {
                return;
            }
            materialOrderMapper.updateMaterialOutOrderByApproval(order.getOutId(), 2, null, null, msg.getComment(), null, 6, null);
        } else if ("APPROVED".equals(status)) {
            int updated = materialOrderMapper.updateMaterialOutOrderByApproval(order.getOutId(), 3, safeUserName(msg), now, msg.getComment(), 1, 2, null);
            if (updated > 0) {
                List<MaterialOutOrderItem> items = materialOrderMapper.selectMaterialOutOrderItems(order.getOutId());
                for (MaterialOutOrderItem item : items) {
                    materialOrderSupport.consumeFrozenOut(order.getEntCode(), order.getWarehouseId(), item.getMaterialId(),
                            item.getOutQty(), order.getOutId(), item.getOutItemId(), order.getOutNo());
                    if (StringUtils.isNotEmpty(item.getApplyItemId())) {
                        materialOrderMapper.increaseApplyItemOutQty(item.getApplyItemId(), item.getOutQty() == null ? 0D : item.getOutQty());
                    }
                }
                refreshApplyOrderStatus(order.getApplyId());
            }
        } else if ("REJECTED".equals(status) || "CANCELLED".equals(status)) {
            int updated = materialOrderMapper.updateMaterialOutOrderByApproval(order.getOutId(), 1, safeUserName(msg), now, msg.getComment(), 0, 2, null);
            if (updated > 0 && Integer.valueOf(1).equals(order.getStockEffectStatus())) {
                List<MaterialOutOrderItem> items = materialOrderMapper.selectMaterialOutOrderItems(order.getOutId());
                for (MaterialOutOrderItem item : items) {
                    materialOrderSupport.unfreezeOut(order.getEntCode(), order.getWarehouseId(), item.getMaterialId(),
                            item.getOutQty(), order.getOutId(), item.getOutItemId(), order.getOutNo());
                }
            }
        }
    }

    /**
     * 处理“物资归还”审批结果。
     *
     * @param msg 审批结果消息
     */
    private void handleReturn(MaterialApprovalResultMsg msg) {
        MaterialReturnOrder order = materialOrderMapper.selectMaterialReturnOrderById(msg.getBusinessKey());
        if (order == null) {
            return;
        }
        if (StringUtils.isNotEmpty(msg.getEntCode()) && !msg.getEntCode().equals(order.getEntCode())) {
            return;
        }
        String status = msg.getStatus();
        LocalDateTime now = LocalDateTime.now();
        if ("PROCESSING".equals(status)) {
            int updated = materialOrderMapper.updateMaterialReturnOrderByApproval(order.getReturnId(), 2, null, null, msg.getComment(), null, 1, null);
            if (updated <= 0) {
                materialOrderMapper.updateMaterialReturnOrderByApproval(order.getReturnId(), 2, null, null, msg.getComment(), null, 6, null);
            }
        } else if ("APPROVED".equals(status)) {
            int updated = materialOrderMapper.updateMaterialReturnOrderByApproval(order.getReturnId(), 3, safeUserName(msg), now, msg.getComment(), 1, 2, 0);
            if (updated > 0) {
                List<MaterialReturnOrderItem> items = materialOrderMapper.selectMaterialReturnOrderItems(order.getReturnId());
                for (MaterialReturnOrderItem item : items) {
                    double stockInQty = item.getStockInQty() == null ? 0D : item.getStockInQty();
                    if (stockInQty > 0D) {
                        materialOrderSupport.adjustStock(order.getEntCode(), order.getWarehouseId(), item.getMaterialId(),
                                stockInQty, "RETURN", order.getReturnId(), item.getReturnItemId(), order.getReturnNo(), "物资归还");
                    }
                    if (StringUtils.isNotEmpty(item.getOutItemId())) {
                        double returnQty = item.getReturnQty() == null ? 0D : item.getReturnQty();
                        if (returnQty != 0D) {
                            materialOrderMapper.increaseOutItemReturnedQty(item.getOutItemId(), returnQty);
                            MaterialOutOrderItem outItem = materialOrderMapper.selectMaterialOutOrderItemById(item.getOutItemId());
                            if (outItem != null && StringUtils.isNotEmpty(outItem.getApplyItemId())) {
                                materialOrderMapper.increaseApplyItemReturnQty(outItem.getApplyItemId(), returnQty);
                            }
                        }
                    }
                }
                refreshApplyOrderStatus(order.getApplyId());
            } else {
                materialOrderMapper.updateMaterialReturnOrderByApproval(order.getReturnId(), 3, safeUserName(msg), now, msg.getComment(), null, 2, null);
            }
        } else if ("REJECTED".equals(status) || "CANCELLED".equals(status)) {
            materialOrderMapper.updateMaterialReturnOrderByApproval(order.getReturnId(), 1, safeUserName(msg), now, msg.getComment(), 0, 2, null);
        }
    }

    /**
     * 获取操作人姓名（用于审批结果落库）。
     *
     * @param msg 审批结果消息
     * @return 操作人姓名，缺省返回“系统”
     */
    private String safeUserName(MaterialApprovalResultMsg msg) {
        if (msg == null || StringUtils.isEmpty(msg.getActionUserName())) {
            return "系统";
        }
        return msg.getActionUserName();
    }

    /**
     * 刷新领用申请单的出库/归还状态。
     * <p>
     * 通过汇总申请明细的申请数量、出库数量、归还数量，计算并更新申请单的出库状态与归还状态。
     * </p>
     *
     * @param applyId 申请单 ID
     */
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
