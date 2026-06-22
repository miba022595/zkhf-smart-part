package com.zkhf.epmis.platform.base.service;

import com.zkhf.epmis.core.domain.AjaxResult;
import com.zkhf.epmis.platform.base.domain.DictData;
import com.zkhf.epmis.platform.base.domain.DictType;
import com.zkhf.epmis.platform.base.domain.DictTypeReq;

import jakarta.servlet.http.HttpServletResponse;
import java.util.List;
import java.util.Map;

/**
 * 字典 平台层
 */
public interface DictService {

    /**
     * 依据字典类型获取字典值
     */
    List<DictData> getDataListByTypes(List<String> dictTypes);

    /**
     * 依据字典类型获取字典值
     */
    Map<String, Map<String, String>> getDataMapByTypes(List<String> dictTypes);

    /**
     * 根据所有字典类型
     */
    AjaxResult selectTypeAll();

    /**
     * 根据条件分页查询字典类型
     */
    AjaxResult selectTypeList(DictTypeReq req);

    /**
     * 查询字典数据列表
     */
    AjaxResult selectDataList(DictData req);

    /**
     * 导出字典类型
     */
    void exportTypeList(DictTypeReq req, HttpServletResponse response);

    /**
     * 导出字典数据
     */
    void exportDataList(DictData req, HttpServletResponse response);

    /**
     * 根据字典类型ID查询信息
     */
    AjaxResult selectTypeById(Long id);

    /**
     * 根据字典数据ID查询信息
     */
    AjaxResult selectDataById(Long code);

    /**
     * 根据字典类型查询字典数据
     */
    List<DictData> selectDataByType(String type);

    /**
     * 新增字典类型信息
     */
    AjaxResult insertType(DictType type);

    /**
     * 新增字典数据信息
     */
    AjaxResult insertData(DictData data);

    /**
     * 修改字典类型信息
     */
    AjaxResult updateType(DictType type);

    /**
     * 修改字典数据信息
     */
    AjaxResult updateData(DictData data);

    /**
     * 批量删除字典类型信息
     */
    AjaxResult deleteTypeByIds(Long[] ids);

    /**
     * 批量删除字典数据信息
     */
    AjaxResult deleteDataByIds(Long[] codes);

    /**
     * 重置字典缓存数据
     */
    AjaxResult resetCache();

    /**
     * 新增字典数据信息-数据值为已有数据中的值+1
     */
    AjaxResult insertDataIntAuto(DictData data);

    /**
     * 根据字典类型查询字典自定义数据
     */
    List<DictData> selectCustomDataByType(String type);

    /**
     * 新增字典自定义数据信息
     */
    AjaxResult insertCustomData(DictData data);

    /**
     * 删除字典自定义数据信息
     */
    AjaxResult deleteCustomDataByCode(Long code);
}
