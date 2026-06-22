package com.zkhf.epmis.process.solidWaste.service;

import com.zkhf.epmis.core.domain.AjaxResult;
import com.zkhf.epmis.process.solidWaste.domain.*;

/**
 * 固废库存管理Service接口
 * 包含产生、减量、入库、出库的记录管理
 */
public interface WasteStockService {
    /** 流转类型：1-产生→减量 2-产生→入库 3-产生→出库 4-入库→出库 */
    Integer reductionGenerate = 1;
    /** 流转类型：1-产生→减量 2-产生→入库 3-产生→出库 4-入库→出库 */
    Integer storageGenerate = 2;
    /** 流转类型：1-产生→减量 2-产生→入库 3-产生→出库 4-入库→出库 */
    Integer outboundGenerate = 3;
    /** 流转类型：1-产生→减量 2-产生→入库 3-产生→出库 4-入库→出库 */
    Integer outboundStorage = 4;

    /** 出库类型，1贮存出库，2立产立清 */
    Integer storageOut = 1;
    /** 出库类型，1贮存出库，2立产立清 */
    Integer immediateClear = 2;

    /** 流转处理量保留精度 */
    Integer dataScale = 3;

    /**
     * 查询固废产生记录列表
     */
    AjaxResult selectWasteGenerateList(WasteLibReq req);

    /**
     * 新增固废产生记录
     */
    AjaxResult insertWasteGenerate(WasteGenerate info);

    /**
     * 删除固废产生记录信息
     */
    AjaxResult deleteWasteGenerate(WasteGenerate info);

    /**
     * 查询固废减量记录列表
     */
    AjaxResult selectWasteReductionList(WasteLibReq req);

    /**
     * 新增固废减量记录
     */
    AjaxResult insertWasteReduction(WasteReduction info);

    /**
     * 删除固废减量记录信息
     */
    AjaxResult deleteWasteReduction(WasteReduction info);

    /**
     * 查询固废入库记录列表
     */
    AjaxResult selectWasteStorageList(WasteLibReq req);

    /**
     * 新增固废入库记录
     */
    AjaxResult insertWasteStorage(WasteStorage info);

    /**
     * 删除固废入库记录信息
     */
    AjaxResult deleteWasteStorage(WasteStorage info);

    /**
     * 查询固废出库记录列表
     */
    AjaxResult selectWasteOutboundList(WasteLibReq req);

    /**
     * 新增固废出库记录
     */
    AjaxResult insertWasteOutbound(WasteOutbound info);

    /**
     * 删除固废出库记录信息
     */
    AjaxResult deleteWasteOutbound(WasteOutbound info);
}
