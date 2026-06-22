package com.zkhf.epmis.process.mapper.material;

import com.zkhf.epmis.process.material.domain.MaterialWarehouse;
import com.zkhf.epmis.process.material.domain.MaterialWarehouseReq;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 仓库信息Mapper接口
 */
public interface MaterialWarehouseMapper {

    /**
     * 查询仓库信息列表
     */
    List<MaterialWarehouse> selectMaterialWarehouseList(MaterialWarehouseReq req);

    /**
     * 查询仓库信息详情
     */
    MaterialWarehouse selectMaterialWarehouseById(String warehouseId);

    /**
     * 按仓库编号批量查询仓库信息
     */
    List<MaterialWarehouse> selectMaterialWarehouseByCodes(@Param("entCode") String entCode, @Param("warehouseCodes") List<String> warehouseCodes);

    /**
     * 新增仓库信息
     */
    int insertMaterialWarehouse(MaterialWarehouse info);

    /**
     * 修改仓库信息
     */
    int updateMaterialWarehouse(MaterialWarehouse info);

    /**
     * 删除仓库信息
     */
    int deleteMaterialWarehouseById(String warehouseId);

    /**
     * 统计仓库信息被引用次数
     */
    int countMaterialWarehouseRef(String warehouseId);
}
