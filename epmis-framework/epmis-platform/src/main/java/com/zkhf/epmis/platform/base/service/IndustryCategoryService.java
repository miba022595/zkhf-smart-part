package com.zkhf.epmis.platform.base.service;

import com.zkhf.epmis.core.domain.AjaxResult;
import com.zkhf.epmis.platform.base.domain.IndustryCategory;

import java.util.List;
import java.util.Map;

/**
 * 国民经济行业分类Service接口
 */
public interface IndustryCategoryService {

    /**
     * 查询国民经济行业分类列表
     */
    AjaxResult selectIndustryCategoryTree();

    Map<String, IndustryCategory> selectIndustryCategoryMap();

    void industrySet(Map<String, IndustryCategory> inMap, List<String> industryCategoryList,
                            List<String> industryCodeList, List<List<String>> industryList, String industryCategory);
}
