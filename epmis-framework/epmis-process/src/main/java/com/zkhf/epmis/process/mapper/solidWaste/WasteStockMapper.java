package com.zkhf.epmis.process.mapper.solidWaste;

import com.zkhf.epmis.process.solidWaste.domain.*;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 固废库存管理Mapper接口
 * 包含产生、减量、入库、出库的记录管理
 */
public interface WasteStockMapper {

    /**
     * 查询固废产生记录列表
     */
    List<WasteGenerate> selectWasteGenerateList(WasteLibReq req);

    /**
     * 固废产生当天最大序号
     */
    Integer selectWasteGenerateMaxDaySeq(@Param("startTime") String startTime, @Param("endTime") String endTime);

    /**
     * 新增固废产生记录
     */
    int insertWasteGenerate(WasteGenerate info);

    /**
     * 删除固废产生记录
     */
    int deleteWasteGenerateById(String id);

    /**
     * 查询固废减量记录列表
     */
    List<WasteReduction> selectWasteReductionList(WasteLibReq req);

    /**
     * 固废减量当天最大序号
     */
    Integer selectWasteReductionMaxDaySeq(@Param("startTime") String startTime, @Param("endTime") String endTime);

    /**
     * 新增固废减量记录
     */
    int insertWasteReduction(WasteReduction info);

    /**
     * 删除固废减量记录
     */
    int deleteWasteReductionById(String id);

    /**
     * 查询固废入库记录列表
     */
    List<WasteStorage> selectWasteStorageList(WasteLibReq req);

    /**
     * 固废入库当天最大序号
     */
    Integer selectWasteStorageMaxDaySeq(@Param("startTime") String startTime, @Param("endTime") String endTime);

    /**
     * 新增固废入库记录
     */
    int insertWasteStorage(WasteStorage info);

    /**
     * 删除固废入库记录
     */
    int deleteWasteStorageById(String id);

    /**
     * 查询固废出库记录
     */
    WasteOutbound selectWasteOutboundById(String id);

    /**
     * 查询固废出库记录列表
     */
    List<WasteOutbound> selectWasteOutboundList(WasteLibReq req);

    /**
     * 固废出库当天最大序号
     */
    Integer selectWasteOutboundMaxDaySeq(@Param("startTime") String startTime, @Param("endTime") String endTime);

    /**
     * 新增固废出库记录
     */
    int insertWasteOutbound(WasteOutbound info);

    /**
     * 删除固废出库记录
     */
    int deleteWasteOutboundById(String id);

    /**
     * 查询出库-入库关联列表
     */
    int selectWasteFlowRelSize(@Param("sourceId") String sourceId, @Param("flowTypes") Integer... flowTypes);

    /**
     * 新增关联
     */
    void insertWasteFlowRel(List<WasteFlowRel> relList);

    /**
     * 删除关联
     */
    void deleteWasteFlowRel(@Param("targetId") String targetId, @Param("flowTypes") Integer... flowTypes);

    /**
     * 更新产生记录的剩余量统计
     */
    void updateWasteGenerateByTargetId(@Param("targetId") String targetId, @Param("flowTypes") Integer... flowTypes);

    /**
     * 更新入库记录的剩余量统计
     */
    void updateWasteStorageByTargetId(@Param("targetId") String targetId, @Param("flowType") Integer flowType);
}
