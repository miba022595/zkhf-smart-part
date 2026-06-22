package com.zkhf.epmis.process.solidWaste.service.impl;

import cn.hutool.core.map.MapUtil;
import com.github.f4b6a3.ulid.UlidCreator;
import com.zkhf.epmis.core.annotation.Log;
import com.zkhf.epmis.core.domain.AjaxResult;
import com.zkhf.epmis.core.enums.BusinessType;
import com.zkhf.epmis.core.utils.DateUtils;
import com.zkhf.epmis.core.utils.NumberUtils;
import com.zkhf.epmis.core.utils.PageUtils;
import com.zkhf.epmis.core.utils.StringUtils;
import com.zkhf.epmis.process.base.entity.SysUser;
import com.zkhf.epmis.process.facade.auth.AuthFacade;
import com.zkhf.epmis.process.facade.platform.PlatformFacade;
import com.zkhf.epmis.process.global.GVarContainer;
import com.zkhf.epmis.process.mapper.solidWaste.WasteStockMapper;
import com.zkhf.epmis.process.solidWaste.domain.*;
import com.zkhf.epmis.process.solidWaste.service.WasteCategoryService;
import com.zkhf.epmis.process.solidWaste.service.WasteDictService;
import com.zkhf.epmis.process.solidWaste.service.WasteStockService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 固废库存管理Service业务层处理
 * 包含产生、减量、入库、出库的记录管理
 */
@Service
public class WasteStockServiceImpl implements WasteStockService {

    private WasteStockMapper wasteStockMapper;
    @Autowired
    public void setWasteStockMapper(WasteStockMapper wasteStockMapper) {
        this.wasteStockMapper = wasteStockMapper;
    }

    private WasteDictService wasteDictService;
    @Autowired
    public void setWasteDictService(WasteDictService wasteDictService) {
        this.wasteDictService = wasteDictService;
    }

    private WasteCategoryService wasteCategoryService;
    @Autowired
    public void setWasteCategoryService(WasteCategoryService wasteCategoryService) {
        this.wasteCategoryService = wasteCategoryService;
    }

    private AuthFacade authFacade;
    @Autowired
    public void setAuthFacade(AuthFacade authFacade) {
        this.authFacade = authFacade;
    }

    private PlatformFacade platformFacade;
    @Autowired
    public void setPlatformFacade(PlatformFacade platformFacade) {
        this.platformFacade = platformFacade;
    }

    @Override
    public AjaxResult selectWasteGenerateList(WasteLibReq req) {
        // 请求参数转换
        req = initReq(req);
        if (null == req) {
            return AjaxResult.success(new ArrayList<>());
        }
        boolean page = PageUtils.startPageCheckExists();
        List<WasteGenerate> list = wasteStockMapper.selectWasteGenerateList(req);
        // 填充信息
        if (null != list && !list.isEmpty()) {
            // 转换分类信息-取固废分类根列表
            Map<String, WasteDict> dictMap = getWasteDict();
            List<SysUser> allUser = authFacade.allUserInfo();
            Map<Long, SysUser> userMap = new HashMap<>();
            if (null != allUser) {
                allUser.forEach( u -> {
                    if (null != u.getUserId()) {
                        userMap.put(u.getUserId(), u);
                    }
                });
            }
            for (WasteGenerate w : list) {
                SysUser user = userMap.get(w.getTempOperator());
                if (null != user) {
                    w.setTempOperatorName(user.getUserName());
                    w.setTempOperatorPhone(user.getPhonenumber());
                }
                user = userMap.get(w.getTranOperator());
                if (null != user) {
                    w.setTranOperatorName(user.getUserName());
                    w.setTranOperatorPhone(user.getPhonenumber());
                }
                // 转换精度
                w.setGenQty(NumberUtils.round(w.getGenQty(), dataScale));
                w.setRemainQty(NumberUtils.round(w.getRemainQty(), dataScale));
                if (StringUtils.isNotEmpty(w.getWasteDictId())) {
                    String[] ids = w.getWasteDictId().split(",");
                    WasteDict dict = dictMap.get(ids[0]);
                    if (null != dict) {
                        w.setWasteCategory(dict.getName());
                        dict = dictMap.get(ids[ids.length - 1]);
                        if (null != dict) {
                            w.setWasteType(dict.getName());
                            w.setWasteCode(dict.getTag());
                        }
                    }
                }
            }
        }
        return PageUtils.getAjaxResult(list, page);
    }

    @Override
    @Log(title = "固废产生记录", businessType = BusinessType.INSERT)
    public AjaxResult insertWasteGenerate(WasteGenerate info) {
        if (null == info) {
            return AjaxResult.error("未知的参数");
        }
        info.setId(UlidCreator.getMonotonicUlid().toString());
        // 初始化时间
        if (null == info.getGenTime()) {
            info.setGenTime(LocalDateTime.now());
        }
        String nowStr = info.getGenTime().toLocalDate().toString();
        // 获取今天最大的序列号
        Integer maxDaySeq = wasteStockMapper.selectWasteGenerateMaxDaySeq(nowStr + " 00:00:00", nowStr + " 23:59:59");
        // 序列号+1
        maxDaySeq = null == maxDaySeq ? 1 : (maxDaySeq + 1);
        info.setDaySeq(maxDaySeq);
        // 生成批次号
        String batchNo = getBatchNo(info.getCategoryId(), "CS", maxDaySeq, info.getGenTime());
        info.setBatchNo(batchNo);
        int count = wasteStockMapper.insertWasteGenerate(info);
        return AjaxResult.success(count);
    }

    @Override
    @Log(title = "固废产生记录", businessType = BusinessType.DELETE)
    public AjaxResult deleteWasteGenerate(WasteGenerate info) {
        if (null == info) {
            return AjaxResult.error("未知的参数");
        }
        // 判断是否以被使用
        int count = wasteStockMapper.selectWasteFlowRelSize(info.getId(), reductionGenerate, storageGenerate, outboundGenerate);
        if (count > 0) {
            return AjaxResult.error("记录已被引用，不能删除");
        }
        count = wasteStockMapper.deleteWasteGenerateById(info.getId());
        return AjaxResult.success(count);
    }

    @Override
    public AjaxResult selectWasteReductionList(WasteLibReq req) {
        // 请求参数转换
        req = initReq(req);
        if (null == req) {
            return AjaxResult.success(new ArrayList<>());
        }
        // 固废种类管理列表查询
        PageUtils.startPage();
        List<WasteReduction> list = wasteStockMapper.selectWasteReductionList(req);
        // 填充信息
        if (null != list && !list.isEmpty()) {
            // 转换分类信息-取固废分类根列表
            Map<String, WasteDict> dictMap = getWasteDict();
            List<SysUser> allUser = authFacade.allUserInfo();
            Map<Long, SysUser> userMap = new HashMap<>();
            if (null != allUser) {
                allUser.forEach( u -> {
                    if (null != u.getUserId()) {
                        userMap.put(u.getUserId(), u);
                    }
                });
            }
            for (WasteReduction w : list) {
                SysUser user = userMap.get(w.getOperator());
                if (null != user) {
                    w.setOperatorName(user.getUserName());
                    w.setOperatorPhone(user.getPhonenumber());
                }
                // 转换精度
                w.setReductionQty(NumberUtils.round(w.getReductionQty(), dataScale));
                if (StringUtils.isNotEmpty(w.getWasteDictId())) {
                    String[] ids = w.getWasteDictId().split(",");
                    WasteDict dict = dictMap.get(ids[0]);
                    if (null != dict) {
                        w.setWasteCategory(dict.getName());
                        dict = dictMap.get(ids[ids.length - 1]);
                        if (null != dict) {
                            w.setWasteType(dict.getName());
                            w.setWasteCode(dict.getTag());
                        }
                    }
                }
            }
        }
        return PageUtils.getAjaxResult(list, true);
    }

    @Override
    @Log(title = "固废减量记录", businessType = BusinessType.INSERT)
    public AjaxResult insertWasteReduction(WasteReduction info) {
        if (null == info) {
            return AjaxResult.error("未知的参数");
        }
        if (null == info.getRelList() || info.getRelList().isEmpty()) {
            return AjaxResult.error("数据来源为空");
        }
        info.setId(UlidCreator.getMonotonicUlid().toString());
        info.setReductionQty(null);
        // 获取剩余量
        Map<String, Double> remainQtyMap = getWasteGenerateRemainQtyMap();
        for (WasteFlowRel rel : info.getRelList()) {
            if (null == rel.getQty() || StringUtils.isEmpty(rel.getSourceId())) {
                return AjaxResult.error("数据来源为空");
            }
            if (rel.getQty() <= 0) {
                return AjaxResult.error("减量数据必须大于0");
            }
            Double leftQty = remainQtyMap.get(rel.getSourceId());
            if (null == leftQty) {
                return AjaxResult.error("无剩余量");
            }
            if (leftQty < rel.getQty()) {
                return AjaxResult.error("减量量超出剩余量");
            }
            // 流转类型：产生→减量
            rel.setFlowType(reductionGenerate);
            rel.setTargetId(info.getId());
            // 调整精度
            rel.setQty(NumberUtils.round(rel.getQty(), dataScale));
            if (null == info.getReductionQty()) {
                info.setReductionQty(rel.getQty());
            } else {
                info.setReductionQty(info.getReductionQty() + rel.getQty());
            }
        }
        if (null == info.getReductionQty()) {
            return AjaxResult.error("减量为空");
        }
        // 调整精度
        info.setReductionQty(NumberUtils.round(info.getReductionQty(), dataScale));
        // 初始化时间
        if (null == info.getReductionTime()) {
            info.setReductionTime(LocalDateTime.now());
        }
        String nowStr = info.getReductionTime().toLocalDate().toString();
        // 获取今天最大的序列号
        Integer maxDaySeq = wasteStockMapper.selectWasteReductionMaxDaySeq(nowStr + " 00:00:00", nowStr + " 23:59:59");
        // 序列号+1
        maxDaySeq = null == maxDaySeq ? 1 : (maxDaySeq + 1);
        info.setDaySeq(maxDaySeq);
        // 生成批次号
        String batchNo = getBatchNo(info.getCategoryId(), "JL", maxDaySeq, info.getReductionTime());
        info.setBatchNo(batchNo);
        int count = wasteStockMapper.insertWasteReduction(info);
        // 添加关联关系
        if (count > 0) {
            wasteStockMapper.insertWasteFlowRel(info.getRelList());
            // 更新剩余产量
            wasteStockMapper.updateWasteGenerateByTargetId(info.getId(), reductionGenerate, storageGenerate, outboundGenerate);
        }
        return AjaxResult.success(count);
    }

    @Override
    @Log(title = "固废减量记录", businessType = BusinessType.DELETE)
    public AjaxResult deleteWasteReduction(WasteReduction info) {
        if (null == info) {
            return AjaxResult.error("未知的参数");
        }
        int count = wasteStockMapper.deleteWasteReductionById(info.getId());
        // 删除关联关系
        if (count > 0) {
            wasteStockMapper.deleteWasteFlowRel(info.getId(), reductionGenerate);
            // 更新剩余产量
            wasteStockMapper.updateWasteGenerateByTargetId(info.getId(), reductionGenerate, storageGenerate, outboundGenerate);
        }
        return AjaxResult.success(count);
    }

    @Override
    public AjaxResult selectWasteStorageList(WasteLibReq req) {
        // 请求参数转换
        req = initReq(req);
        if (null == req) {
            return AjaxResult.success(new ArrayList<>());
        }
        boolean page = PageUtils.startPageCheckExists();
        List<WasteStorage> list = wasteStockMapper.selectWasteStorageList(req);
        // 填充信息
        if (null != list && !list.isEmpty()) {
            // 转换分类信息-取固废分类根列表
            Map<String, WasteDict> dictMap = getWasteDict();
            List<SysUser> allUser = authFacade.allUserInfo();
            Map<Long, SysUser> userMap = new HashMap<>();
            if (null != allUser) {
                allUser.forEach( u -> {
                    if (null != u.getUserId()) {
                        userMap.put(u.getUserId(), u);
                    }
                });
            }
            for (WasteStorage w : list) {
                SysUser user = userMap.get(w.getStorageOperator());
                if (null != user) {
                    w.setStorageOperatorName(user.getUserName());
                    w.setStorageOperatorPhone(user.getPhonenumber());
                }
                user = userMap.get(w.getTranOperator());
                if (null != user) {
                    w.setTranOperatorName(user.getUserName());
                    w.setTranOperatorPhone(user.getPhonenumber());
                }
                // 转换精度
                w.setStorageQty(NumberUtils.round(w.getStorageQty(), dataScale));
                if (StringUtils.isNotEmpty(w.getWasteDictId())) {
                    String[] ids = w.getWasteDictId().split(",");
                    WasteDict dict = dictMap.get(ids[0]);
                    if (null != dict) {
                        w.setWasteCategory(dict.getName());
                        dict = dictMap.get(ids[ids.length - 1]);
                        if (null != dict) {
                            w.setWasteType(dict.getName());
                            w.setWasteCode(dict.getTag());
                        }
                    }
                }
            }
        }
        return PageUtils.getAjaxResult(list, page);
    }

    @Override
    @Log(title = "固废入库记录", businessType = BusinessType.INSERT)
    public AjaxResult insertWasteStorage(WasteStorage info) {
        if (null == info) {
            return AjaxResult.error("未知的参数");
        }
        if (null == info.getRelList() || info.getRelList().isEmpty()) {
            return AjaxResult.error("数据来源为空");
        }
        info.setId(UlidCreator.getMonotonicUlid().toString());
        info.setStorageQty(null);
        // 获取剩余量
        Map<String, Double> remainQtyMap = getWasteGenerateRemainQtyMap();
        for (WasteFlowRel rel : info.getRelList()) {
            if (null == rel.getQty() || StringUtils.isEmpty(rel.getSourceId())) {
                return AjaxResult.error("数据来源为空");
            }
            if (rel.getQty() <= 0) {
                return AjaxResult.error("入库量数据必须大于0");
            }
            Double leftQty = remainQtyMap.get(rel.getSourceId());
            if (null == leftQty) {
                return AjaxResult.error("无剩余量");
            }
            if (leftQty < rel.getQty()) {
                return AjaxResult.error("入库量超出剩余量");
            }
            // 流转类型：产生→入库
            rel.setFlowType(storageGenerate);
            rel.setTargetId(info.getId());
            // 调整精度
            rel.setQty(NumberUtils.round(rel.getQty(), dataScale));
            if (null == info.getStorageQty()) {
                info.setStorageQty(rel.getQty());
            } else {
                info.setStorageQty(info.getStorageQty() + rel.getQty());
            }
        }
        if (null == info.getStorageQty()) {
            return AjaxResult.error("入库量为空");
        }
        // 调整精度
        info.setStorageQty(NumberUtils.round(info.getStorageQty(), dataScale));
        // 初始化时间
        if (null == info.getStorageTime()) {
            info.setStorageTime(LocalDateTime.now());
        }
        String nowStr = info.getStorageTime().toLocalDate().toString();
        // 获取今天最大的序列号
        Integer maxDaySeq = wasteStockMapper.selectWasteStorageMaxDaySeq(nowStr + " 00:00:00", nowStr + " 23:59:59");
        // 序列号+1
        maxDaySeq = null == maxDaySeq ? 1 : (maxDaySeq + 1);
        info.setDaySeq(maxDaySeq);
        // 生成批次号
        String batchNo = getBatchNo(info.getCategoryId(), "RK", maxDaySeq, info.getStorageTime());
        info.setBatchNo(batchNo);
        int count = wasteStockMapper.insertWasteStorage(info);
        // 添加关联关系
        if (count > 0) {
            wasteStockMapper.insertWasteFlowRel(info.getRelList());
            // 更新剩余产量
            wasteStockMapper.updateWasteGenerateByTargetId(info.getId(), reductionGenerate, storageGenerate, outboundGenerate);
        }
        return AjaxResult.success(count);
    }

    @Override
    @Log(title = "固废入库记录", businessType = BusinessType.DELETE)
    public AjaxResult deleteWasteStorage(WasteStorage info) {
        if (null == info) {
            return AjaxResult.error("未知的参数");
        }
        // 判断是否以被使用
        int count = wasteStockMapper.selectWasteFlowRelSize(info.getId(), outboundStorage);
        if (count > 0) {
            return AjaxResult.error("记录已被引用，不能删除");
        }
        count = wasteStockMapper.deleteWasteStorageById(info.getId());
        // 删除关联关系
        if (count > 0) {
            wasteStockMapper.deleteWasteFlowRel(info.getId(), storageGenerate);
            // 更新剩余产量
            wasteStockMapper.updateWasteGenerateByTargetId(info.getId(), reductionGenerate, storageGenerate, outboundGenerate);
        }
        return AjaxResult.success(count);
    }

    @Override
    public AjaxResult selectWasteOutboundList(WasteLibReq req) {
        // 请求参数转换
        req = initReq(req);
        if (null == req) {
            return AjaxResult.success(new ArrayList<>());
        }
        PageUtils.startPage();
        List<WasteOutbound> list = wasteStockMapper.selectWasteOutboundList(req);
        // 填充信息
        if (null != list && !list.isEmpty()) {
            // 转换分类信息-取固废分类根列表
            Map<String, WasteDict> dictMap = getWasteDict();
            List<SysUser> allUser = authFacade.allUserInfo();
            Map<Long, SysUser> userMap = new HashMap<>();
            if (null != allUser) {
                allUser.forEach( u -> {
                    if (null != u.getUserId()) {
                        userMap.put(u.getUserId(), u);
                    }
                });
            }
            // 获取第三方单位
            List<Map<String, Object>> extList = platformFacade.allExtUnitList();
            Map<String, String> extMap = new HashMap<>();
            if (null != extList && !extList.isEmpty()) {
                extList.forEach( u -> {
                    String unitCode = MapUtil.getStr(u, "unitCode");
                    if (StringUtils.isNotEmpty(unitCode)) {
                        extMap.put(unitCode, MapUtil.getStr(u, "unitName"));
                    }
                });
            }
            for (WasteOutbound w : list) {
                SysUser user = userMap.get(w.getOutOperator());
                if (null != user) {
                    w.setOutOperatorName(user.getUserName());
                    w.setOutOperatorPhone(user.getPhonenumber());
                }
                user = userMap.get(w.getTranOperator());
                if (null != user) {
                    w.setTranOperatorName(user.getUserName());
                    w.setTranOperatorPhone(user.getPhonenumber());
                }
                w.setTranUnitName(extMap.get(w.getTranUnit()));
                w.setDisposalUnitName(extMap.get(w.getDisposalUnit()));
                // 转换精度
                w.setOutQty(NumberUtils.round(w.getOutQty(), dataScale));
                if (StringUtils.isNotEmpty(w.getWasteDictId())) {
                    String[] ids = w.getWasteDictId().split(",");
                    WasteDict dict = dictMap.get(ids[0]);
                    if (null != dict) {
                        w.setWasteCategory(dict.getName());
                        dict = dictMap.get(ids[ids.length - 1]);
                        if (null != dict) {
                            w.setWasteType(dict.getName());
                            w.setWasteCode(dict.getTag());
                        }
                    }
                }
            }
        }
        return PageUtils.getAjaxResult(list, true);
    }

    @Override
    @Log(title = "固废出库记录", businessType = BusinessType.INSERT)
    public AjaxResult insertWasteOutbound(WasteOutbound info) {
        if (null == info) {
            return AjaxResult.error("未知的参数");
        }
        if (null == info.getRelList() || info.getRelList().isEmpty()) {
            return AjaxResult.error("数据来源为空");
        }
        Integer flowType = null;
        if (storageOut.equals(info.getOutType())) {
            flowType = outboundStorage; // 贮存出库 入库→出库
        } else if (immediateClear.equals(info.getOutType())) {
            flowType = outboundGenerate; // 立产立清 产生→出库
        }
        if (null == flowType) {
            return AjaxResult.error("未知的出库类型");
        }
        info.setId(UlidCreator.getMonotonicUlid().toString());
        info.setOutQty(null);
        // 获取剩余量
        Map<String, Double> remainQtyMap;
        if (outboundStorage.equals(flowType)) {
            remainQtyMap = getWasteStorageRemainQtyMap();
        } else {
            remainQtyMap = getWasteGenerateRemainQtyMap();
        }
        for (WasteFlowRel rel : info.getRelList()) {
            if (null == rel.getQty() || StringUtils.isEmpty(rel.getSourceId())) {
                return AjaxResult.error("数据来源为空");
            }
            if (rel.getQty() <= 0) {
                return AjaxResult.error("出库量数据必须大于0");
            }
            Double leftQty = remainQtyMap.get(rel.getSourceId());
            if (null == leftQty) {
                return AjaxResult.error("无剩余量");
            }
            if (leftQty < rel.getQty()) {
                return AjaxResult.error("出库量超出剩余量");
            }
            // 流转类型：产生→出库 入库→出库
            rel.setFlowType(flowType);
            rel.setTargetId(info.getId());
            // 调整精度
            rel.setQty(NumberUtils.round(rel.getQty(), dataScale));
            if (null == info.getOutQty()) {
                info.setOutQty(rel.getQty());
            } else {
                info.setOutQty(info.getOutQty() + rel.getQty());
            }
        }
        if (null == info.getOutQty()) {
            return AjaxResult.error("出库量为空");
        }
        // 调整精度
        info.setOutQty(NumberUtils.round(info.getOutQty(), dataScale));
        // 初始化时间
        if (null == info.getOutTime()) {
            info.setOutTime(LocalDateTime.now());
        }
        String nowStr = info.getOutTime().toLocalDate().toString();
        // 获取今天最大的序列号
        Integer maxDaySeq = wasteStockMapper.selectWasteOutboundMaxDaySeq(nowStr + " 00:00:00", nowStr + " 23:59:59");
        // 序列号+1
        maxDaySeq = null == maxDaySeq ? 1 : (maxDaySeq + 1);
        info.setDaySeq(maxDaySeq);
        // 生成批次号
        String batchNo = getBatchNo(info.getCategoryId(), "CK", maxDaySeq, info.getOutTime());
        info.setBatchNo(batchNo);
        int count = wasteStockMapper.insertWasteOutbound(info);
        // 添加关联关系
        if (count > 0) {
            wasteStockMapper.insertWasteFlowRel(info.getRelList());
            if (outboundStorage.equals(flowType)) {
                // 更新剩余入库量，入库→出库，更新入库的剩余量
                wasteStockMapper.updateWasteStorageByTargetId(info.getId(), outboundStorage);
            } else {
                // 更新剩余产量，产生→出库，更新产生的剩余量
                wasteStockMapper.updateWasteGenerateByTargetId(info.getId(), reductionGenerate, storageGenerate, outboundGenerate);
            }
        }
        return AjaxResult.success(count);
    }

    @Override
    @Log(title = "固废出库记录", businessType = BusinessType.DELETE)
    public AjaxResult deleteWasteOutbound(WasteOutbound info) {
        if (null == info) {
            return AjaxResult.error("未知的参数");
        }
        WasteOutbound out = wasteStockMapper.selectWasteOutboundById(info.getId());
        if (null == out) {
            return AjaxResult.error("无出库记录");
        }
        int count = wasteStockMapper.deleteWasteOutboundById(info.getId());
        // 删除关联关系
        if (count > 0) {
            // 贮存出库 入库→出库；立产立清 产生→出库
            if (storageOut.equals(out.getOutType())) {
                wasteStockMapper.deleteWasteFlowRel(info.getId(), outboundStorage);
                wasteStockMapper.updateWasteStorageByTargetId(info.getId(), outboundStorage);
            } else if (immediateClear.equals(out.getOutType())) {
                wasteStockMapper.deleteWasteFlowRel(info.getId(), outboundGenerate);
                wasteStockMapper.updateWasteGenerateByTargetId(info.getId(), reductionGenerate, storageGenerate, outboundGenerate);
            }
        }
        return AjaxResult.success(count);
    }

    private WasteLibReq initReq(WasteLibReq req) {
        if (null == req) {
            req = new WasteLibReq();
        }
        // 添加权限
        if (GVarContainer.isNotAdmin()) {
            req.setEntCodes(GVarContainer.getEntCodes());
            if (null == req.getEntCodes() || req.getEntCodes().isEmpty()) {
                return null;
            }
        }
        return req;
    }

    private Map<String, Double> getWasteGenerateRemainQtyMap() {
        Map<String, Double> remainQtyMap = new HashMap<>();
        WasteLibReq req = new WasteLibReq();
        // 添加权限
        if (GVarContainer.isNotAdmin()) {
            req.setEntCodes(GVarContainer.getEntCodes());
            if (null == req.getEntCodes() || req.getEntCodes().isEmpty()) {
                return remainQtyMap;
            }
        }
        req.setLeft(1);
        List<WasteGenerate> list = wasteStockMapper.selectWasteGenerateList(req);
        if (null != list && !list.isEmpty()) {
            for (WasteGenerate gen : list) {
                if (null != gen.getRemainQty() && gen.getRemainQty() > 0) {
                    remainQtyMap.put(gen.getId(), gen.getRemainQty());
                }
            }
        }
        return remainQtyMap;
    }

    private Map<String, Double> getWasteStorageRemainQtyMap() {
        Map<String, Double> remainQtyMap = new HashMap<>();
        WasteLibReq req = new WasteLibReq();
        // 添加权限
        if (GVarContainer.isNotAdmin()) {
            req.setEntCodes(GVarContainer.getEntCodes());
            if (null == req.getEntCodes() || req.getEntCodes().isEmpty()) {
                return remainQtyMap;
            }
        }
        req.setLeft(1);
        List<WasteStorage> list = wasteStockMapper.selectWasteStorageList(req);
        if (null != list && !list.isEmpty()) {
            for (WasteStorage gen : list) {
                if (null != gen.getRemainQty() && gen.getRemainQty() > 0) {
                    remainQtyMap.put(gen.getId(), gen.getRemainQty());
                }
            }
        }
        return remainQtyMap;
    }

    private String getBatchNo(String categoryId, String type, Integer daySeq, LocalDateTime time) {
        WasteCategory category = wasteCategoryService.selectWasteCategoryById(categoryId);
        String pidCode = "", subTag = "";
        if (null != category && StringUtils.isNotEmpty(category.getWasteDictId())) {
            // 分类信息-取固废分类根列表
            Map<String, WasteDict> dictMap = getWasteDict();
            String[] ids = category.getWasteDictId().split(",");
            if (ids.length > 0) {
                WasteDict dict = dictMap.get(ids[0]);
                if (null != dict) {
                    pidCode = dict.getCode();
                    if (ids.length > 1) {
                        dict = dictMap.get(ids[ids.length - 1]);
                        if (null != dict) {
                            subTag = dict.getTag();
                        }
                    }
                }
            }
        }
        // 构建批次号
        StringBuilder batchNo = new StringBuilder();
        // 添加分类编码
        if (StringUtils.isNotEmpty(pidCode)) {
            batchNo.append(pidCode);
        }
        // 添加类型
        if (StringUtils.isNotEmpty(type)) {
            batchNo.append(type);
        }
        // 添加子标签
        if (StringUtils.isNotEmpty(subTag)) {
            if (batchNo.length() > 0) {
                batchNo.append(" ");
            }
            batchNo.append(subTag);
        }
        if (batchNo.length() > 0) {
            batchNo.append(" ");
        }
        // 添加时间戳
        batchNo.append(time.format(DateUtils.yymdH));
        // 添加日序号（3位，前补0）
        if (daySeq != null) {
            batchNo.append(" ").append(String.format("%03d", daySeq));
        }
        return batchNo.toString();
    }

    /**
     * 分类信息-取固废分类列表
     */
    private Map<String, WasteDict> getWasteDict() {
        Map<String, WasteDict> dictMap = new HashMap<>();
        List<WasteDict> dictList = wasteDictService.selectWasteDictList(null);
        if (null != dictList && !dictList.isEmpty()) {
            dictList.forEach( e -> dictMap.put(e.getId() + "", e));
        }
        return dictMap;
    }
}