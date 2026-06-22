package com.zkhf.epmis.process.mapper.material;

import com.zkhf.epmis.process.material.domain.*;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 物资单据Mapper接口
 */
public interface MaterialOrderMapper {

    /**
     * 查询物资入库单列表
     */
    List<MaterialInOrder> selectMaterialInOrderList(MaterialBizReq req);

    /**
     * 查询物资入库单详情
     */
    MaterialInOrder selectMaterialInOrderById(String inId);

    /**
     * 查询物资入库单明细
     */
    List<MaterialInOrderItem> selectMaterialInOrderItems(String inId);

    /**
     * 新增物资入库单
     */
    int insertMaterialInOrder(MaterialInOrder info);

    /**
     * 修改物资入库单
     */
    void updateMaterialInOrder(MaterialInOrder info);

    /**
     * 入库单审批结果回写（仅处理指定旧状态，且可做幂等控制）
     */
    int updateMaterialInOrderByApproval(@Param("inId") String inId,
                                        @Param("status") Integer status,
                                        @Param("auditBy") String auditBy,
                                        @Param("auditTime") LocalDateTime auditTime,
                                        @Param("auditRemark") String auditRemark,
                                        @Param("stockEffectStatus") Integer stockEffectStatus,
                                        @Param("expectStatus") Integer expectStatus,
                                        @Param("expectStockEffectStatus") Integer expectStockEffectStatus);

    /**
     * 删除物资入库单
     */
    int deleteMaterialInOrderById(String inId);

    /**
     * 删除物资入库单明细
     */
    void deleteMaterialInOrderItems(String inId);

    /**
     * 批量新增物资入库单明细
     */
    void batchInsertMaterialInOrderItems(@Param("list") List<MaterialInOrderItem> list);

    /**
     * 查询物资申请单列表
     */
    List<MaterialApplyOrder> selectMaterialApplyOrderList(MaterialBizReq req);

    /**
     * 查询物资申请单详情
     */
    MaterialApplyOrder selectMaterialApplyOrderById(String applyId);

    /**
     * 查询物资申请单明细
     */
    List<MaterialApplyOrderItem> selectMaterialApplyOrderItems(String applyId);

    /**
     * 查询物资申请单明细详情
     */
    MaterialApplyOrderItem selectMaterialApplyOrderItemById(String applyItemId);

    /**
     * 新增物资申请单
     */
    int insertMaterialApplyOrder(MaterialApplyOrder info);

    /**
     * 修改物资申请单
     */
    void updateMaterialApplyOrder(MaterialApplyOrder info);

    /**
     * 申请单审批结果回写（仅处理指定旧状态）
     */
    int updateMaterialApplyOrderByAuditResult(@Param("applyId") String applyId,
                                              @Param("auditStatus") Integer auditStatus,
                                              @Param("auditBy") String auditBy,
                                              @Param("auditTime") LocalDateTime auditTime,
                                              @Param("auditRemark") String auditRemark,
                                              @Param("expectAuditStatus") Integer expectAuditStatus);

    /**
     * 删除物资申请单
     */
    int deleteMaterialApplyOrderById(String applyId);

    /**
     * 删除物资申请单明细
     */
    void deleteMaterialApplyOrderItems(String applyId);

    /**
     * 批量新增物资申请单明细
     */
    void batchInsertMaterialApplyOrderItems(@Param("list") List<MaterialApplyOrderItem> list);

    /**
     * 统计申请单被出库引用次数
     */
    int countMaterialApplyOrderRef(String applyId);

    /**
     * 增加申请明细出库数量
     */
    void increaseApplyItemOutQty(@Param("applyItemId") String applyItemId, @Param("qty") Double qty);

    /**
     * 回退申请明细出库数量
     */
    void decreaseApplyItemOutQty(@Param("applyItemId") String applyItemId, @Param("qty") Double qty);

    /**
     * 增加申请明细归还数量
     */
    void increaseApplyItemReturnQty(@Param("applyItemId") String applyItemId, @Param("qty") Double qty);

    /**
     * 回退申请明细归还数量
     */
    void decreaseApplyItemReturnQty(@Param("applyItemId") String applyItemId, @Param("qty") Double qty);

    /**
     * 查询物资出库单列表
     */
    List<MaterialOutOrder> selectMaterialOutOrderList(MaterialBizReq req);

    /**
     * 查询物资出库单详情
     */
    MaterialOutOrder selectMaterialOutOrderById(String outId);

    /**
     * 查询物资出库单明细
     */
    List<MaterialOutOrderItem> selectMaterialOutOrderItems(String outId);

    /**
     * 查询物资出库单明细详情
     */
    MaterialOutOrderItem selectMaterialOutOrderItemById(String outItemId);

    /**
     * 新增物资出库单
     */
    int insertMaterialOutOrder(MaterialOutOrder info);

    /**
     * 修改物资出库单
     */
    void updateMaterialOutOrder(MaterialOutOrder info);

    /**
     * 出库单审批结果回写（仅处理指定旧状态，且可做幂等控制）
     */
    int updateMaterialOutOrderByApproval(@Param("outId") String outId,
                                         @Param("status") Integer status,
                                         @Param("auditBy") String auditBy,
                                         @Param("auditTime") LocalDateTime auditTime,
                                         @Param("auditRemark") String auditRemark,
                                         @Param("stockEffectStatus") Integer stockEffectStatus,
                                         @Param("expectStatus") Integer expectStatus,
                                         @Param("expectStockEffectStatus") Integer expectStockEffectStatus);

    /**
     * 删除物资出库单
     */
    int deleteMaterialOutOrderById(String outId);

    /**
     * 删除物资出库单明细
     */
    void deleteMaterialOutOrderItems(String outId);

    /**
     * 批量新增物资出库单明细
     */
    void batchInsertMaterialOutOrderItems(@Param("list") List<MaterialOutOrderItem> list);

    /**
     * 统计出库单被归还引用次数
     */
    int countMaterialOutOrderRef(String outId);

    /**
     * 增加出库明细归还数量
     */
    void increaseOutItemReturnedQty(@Param("outItemId") String outItemId, @Param("qty") Double qty);

    /**
     * 回退出库明细归还数量
     */
    void decreaseOutItemReturnedQty(@Param("outItemId") String outItemId, @Param("qty") Double qty);

    /**
     * 查询物资归还单列表
     */
    List<MaterialReturnOrder> selectMaterialReturnOrderList(MaterialBizReq req);

    /**
     * 查询物资归还单详情
     */
    MaterialReturnOrder selectMaterialReturnOrderById(String returnId);

    /**
     * 查询物资归还单明细
     */
    List<MaterialReturnOrderItem> selectMaterialReturnOrderItems(String returnId);

    /**
     * 新增物资归还单
     */
    int insertMaterialReturnOrder(MaterialReturnOrder info);

    /**
     * 修改物资归还单
     */
    void updateMaterialReturnOrder(MaterialReturnOrder info);

    /**
     * 归还单审批结果回写（仅处理指定旧状态，且可做幂等控制）
     */
    int updateMaterialReturnOrderByApproval(@Param("returnId") String returnId,
                                            @Param("status") Integer status,
                                            @Param("auditBy") String auditBy,
                                            @Param("auditTime") LocalDateTime auditTime,
                                            @Param("auditRemark") String auditRemark,
                                            @Param("stockEffectStatus") Integer stockEffectStatus,
                                            @Param("expectStatus") Integer expectStatus,
                                            @Param("expectStockEffectStatus") Integer expectStockEffectStatus);

    /**
     * 删除物资归还单
     */
    int deleteMaterialReturnOrderById(String returnId);

    /**
     * 删除物资归还单明细
     */
    void deleteMaterialReturnOrderItems(String returnId);

    /**
     * 批量新增物资归还单明细
     */
    void batchInsertMaterialReturnOrderItems(@Param("list") List<MaterialReturnOrderItem> list);

    /**
     * 批量查询物资基础信息
     */
    List<MaterialInfo> selectMaterialInfoByIds(@Param("materialIds") List<String> materialIds);

    /**
     * 查询指定仓库物资库存
     */
    MaterialStock selectMaterialStock(@Param("entCode") String entCode, @Param("warehouseId") String warehouseId, @Param("materialId") String materialId);

    /**
     * 新增库存汇总记录
     */
    void insertMaterialStock(MaterialStock stock);

    /**
     * 更新库存汇总记录
     */
    void updateMaterialStock(MaterialStock stock);

    /**
     * 新增库存流水记录
     */
    void insertMaterialStockFlow(MaterialStockFlow flow);

    /**
     * 查询库存流水明细
     */
    List<MaterialStockFlow> selectMaterialStockFlowList(MaterialStockFlowReq req);
}
