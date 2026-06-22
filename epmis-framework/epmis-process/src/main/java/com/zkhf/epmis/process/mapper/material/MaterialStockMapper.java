package com.zkhf.epmis.process.mapper.material;

import com.zkhf.epmis.process.material.domain.MaterialStock;
import com.zkhf.epmis.process.material.domain.MaterialStockReq;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 物资库存Mapper接口
 */
public interface MaterialStockMapper {

    /**
     * 查询物资库存列表
     */
    List<MaterialStock> selectMaterialStockList(MaterialStockReq req);

    /**
     * 按物资ID批量统计总库存
     */
    List<MaterialStock> selectMaterialCurrentQtyByMaterialIds(@Param("materialIds") List<String> materialIds);
}
