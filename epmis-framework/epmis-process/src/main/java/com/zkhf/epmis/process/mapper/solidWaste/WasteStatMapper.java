package com.zkhf.epmis.process.mapper.solidWaste;

import java.util.List;

import com.zkhf.epmis.process.solidWaste.domain.WasteCategory;
import com.zkhf.epmis.process.solidWaste.domain.WasteStatInfo;
import org.apache.ibatis.annotations.Param;

/**
 * 固废月度统计汇总Mapper接口
 */
public interface WasteStatMapper {

    /**
     * 查询固废种类列表
     */
    List<WasteCategory> selectCategoryList(@Param("entCodes") List<String> entCodes);

    /**
     * 查询产生量汇总（按category_id分组）
     */
    List<WasteStatInfo> selectGenerateSum(@Param("categoryIds") List<String> categoryIds);

    /**
     * 查询减量量汇总（按category_id分组）
     */
    List<WasteStatInfo> selectReductionSum(@Param("categoryIds") List<String> categoryIds);

    /**
     * 查询入库量汇总（按category_id分组）
     */
    List<WasteStatInfo> selectStorageSum(@Param("categoryIds") List<String> categoryIds);

    /**
     * 查询出库量汇总（按category_id分组）
     */
    List<WasteStatInfo> selectOutboundSum(@Param("categoryIds") List<String> categoryIds);
}
