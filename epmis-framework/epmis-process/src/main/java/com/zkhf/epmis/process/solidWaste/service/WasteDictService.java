package com.zkhf.epmis.process.solidWaste.service;

import com.zkhf.epmis.process.solidWaste.domain.WasteDict;

import java.util.List;

/**
 * 固废分类字典Service接口
 */
public interface WasteDictService {

    /**
     * 查询固废分类字典列表
     */
    List<WasteDict> selectWasteDictList(Long pid);
}
