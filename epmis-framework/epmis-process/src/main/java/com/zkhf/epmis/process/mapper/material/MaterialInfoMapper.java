package com.zkhf.epmis.process.mapper.material;

import com.zkhf.epmis.process.material.domain.MaterialInfo;
import com.zkhf.epmis.process.material.domain.MaterialInfoReq;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 物资基础信息Mapper接口
 */
public interface MaterialInfoMapper {

    /**
     * 查询物资基础信息列表
     */
    List<MaterialInfo> selectMaterialInfoList(MaterialInfoReq req);

    /**
     * 查询物资基础信息详情
     */
    MaterialInfo selectMaterialInfoById(String materialId);

    /**
     * 新增物资基础信息
     */
    void insertMaterialInfo(MaterialInfo info);

    /**
     * 批量新增物资基础信息
     */
    void batchInsertMaterialInfo(@Param("list") List<MaterialInfo> list);

    /**
     * 修改物资基础信息
     */
    int updateMaterialInfo(MaterialInfo info);

    /**
     * 删除物资基础信息
     */
    int deleteMaterialInfoById(String materialId);

    /**
     * 统计物资基础信息被引用次数
     */
    int countMaterialInfoRef(String materialId);

    /**
     * 查询已存在的物资编号（用于导入校验）
     */
    List<String> selectExistingMaterialCodes(@Param("entCode") String entCode, @Param("materialCodes") List<String> materialCodes);
}
