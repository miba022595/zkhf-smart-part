package com.zkhf.epmis.process.mapper.solidWaste;

import com.zkhf.epmis.process.solidWaste.domain.WasteCategory;
import com.zkhf.epmis.process.solidWaste.domain.WasteCategoryReq;

import java.util.List;

/**
 * 固废种类管理Mapper接口
 */
public interface WasteCategoryMapper {

    /**
     * 查询固废种类管理
     */
    WasteCategory selectWasteCategoryById(String categoryId);

    /**
     * 查询固废种类管理列表
     */
    List<WasteCategory> selectWasteCategoryList(WasteCategoryReq req);

    /**
     * 新增固废种类管理
     */
    int insertWasteCategory(WasteCategory info);

    /**
     * 修改固废种类管理
     */
    int updateWasteCategory(WasteCategory info);

    /**
     * 删除固废种类管理
     */
    int deleteWasteCategoryByCategoryId(String categoryId);

    /**
     * 获取固废种类被使用的次数
     */
    int selectUsedSizeCategory(String categoryId);

    /**
     * 获取固废种类被使用的次数（被计划使用）
     */
    int selectUsedSizeFromPlan(String categoryId);

}
