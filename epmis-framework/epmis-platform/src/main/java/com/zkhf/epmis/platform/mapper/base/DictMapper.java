package com.zkhf.epmis.platform.mapper.base;

import com.zkhf.epmis.platform.base.domain.DictData;
import com.zkhf.epmis.platform.base.domain.DictType;
import com.zkhf.epmis.platform.base.domain.DictTypeReq;
import com.zkhf.epmis.platform.base.domain.DictTypeSimple;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 字典表 数据层
 */
public interface DictMapper {

    /**
     * 依据字典类型获取字典值
     */
    List<DictData> getDataListByTypes(List<String> dictTypes);

    /**
     * 根据所有字典类型
     * 状态为0正常的数据
     */
    List<DictTypeSimple> selectTypeAll();

    /**
     * 查询字典类型列表
     */
    List<DictType> selectTypeList(DictTypeReq req);

    /**
     * 查询字典数据列表
     */
    List<DictData> selectDataList(DictData req);

    /**
     * 根据字典类型ID查询信息
     */
    DictType selectTypeById(Long dictId);

    /**
     * 根据字典数据ID查询信息
     */
    DictData selectDataById(Long dictCode);

    /**
     * 根据字典类型查询字典数据
     */
    List<DictData> selectDataByType(String dictType);

    /**
     * 新增字典类型信息
     */
    int insertType(DictType dictType);

    /**
     * 校验字典类型称是否唯一
     */
    DictType checkTypeUnique(String dictType);

    /**
     * 新增字典数据信息
     */
    int insertData(DictData dictData);

    /**
     * 修改字典类型信息
     */
    int updateType(DictType dictType);

    /**
     * 同步修改字典类型
     */
    void updateDataType(@Param("oldType") String oldType, @Param("newType") String newType);

    /**
     * 修改字典数据信息
     */
    int updateData(DictData data);

    /**
     * 查询字典数据
     */
    int countDataByType(String dictType);

    /**
     * 删除字典类型信息
     */
    void deleteTypeById(Long typeId);

    /**
     * 删除字典数据信息
     */
    void deleteDataById(Long dataId);

    /**
     * 根据字典类型查询字典自定义数据
     */
    List<DictData> selectCustomDataByType(@Param("dictType") String dictType, @Param("userId") Long userId);

    /**
     * 新增字典自定义数据信息
     */
    int insertCustomData(@Param("dictData") DictData dictData, @Param("userId") Long userId);

    /**
     * 根据字典自定义数据ID查询信息
     */
    DictData selectCustomDataById(@Param("dictCode") Long dictCode, @Param("userId") Long userId);

    /**
     * 删除字典自定义数据信息
     */
    void deleteCustomDataByCode(@Param("dictCode") Long dictCode, @Param("userId") Long userId);
}
