package com.zkhf.epmis.platform.base.service.impl;

import com.zkhf.epmis.core.domain.AjaxResult;
import com.zkhf.epmis.core.utils.StringUtils;
import com.zkhf.epmis.platform.base.domain.IndustryCategory;
import com.zkhf.epmis.platform.base.service.IndustryCategoryService;
import com.zkhf.epmis.platform.mapper.base.IndustryCategoryMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 国民经济行业分类Service业务层处理
 */
@Slf4j
@Service
public class IndustryCategoryServiceImpl implements IndustryCategoryService {

    private IndustryCategoryMapper industryCategoryMapper;

    @Autowired
    public void setIndustryCategoryMapper(IndustryCategoryMapper industryCategoryMapper) {
        this.industryCategoryMapper = industryCategoryMapper;
    }

    @Override
    public AjaxResult selectIndustryCategoryTree() {
        List<IndustryCategory> list = industryCategoryMapper.selectIndustryCategoryList();
        if (null == list || list.size() < 1) {
            return AjaxResult.success();
        }
        Map<String, List<IndustryCategory>> map = new HashMap<>();
        list.forEach(e -> {
            List<IndustryCategory> sub;
            if (map.containsKey(e.getPid())) {
                sub = map.get(e.getPid());
            } else {
                sub = new ArrayList<>();
                map.put(e.getPid(), sub);
            }
            sub.add(e);
        });
        // 获取父级列表
        List<IndustryCategory> parent = map.get("-1");
        fill(map, parent);
        // o1.compare o2
        parent.sort(Comparator.comparing(IndustryCategory::getCode));
        return AjaxResult.success(parent);
    }

    private void fill(Map<String, List<IndustryCategory>> map, List<IndustryCategory> list) {
        if (null == list || list.size() < 1) {
            return;
        }
        for (IndustryCategory in : list) {
            if (map.containsKey(in.getId())) {
                List<IndustryCategory> sub = map.get(in.getId());
                fill(map, sub);
                sub.sort(Comparator.comparing(IndustryCategory::getCode));
                in.setSub(sub);
            }
        }
    }

    @Override
    public Map<String, IndustryCategory> selectIndustryCategoryMap() {
        List<IndustryCategory> list = industryCategoryMapper.selectIndustryCategoryList();
        if (null == list || list.isEmpty()) {
            return new HashMap<>();
        }
        return list.stream().collect(Collectors.toMap(
                IndustryCategory::getId,  // Key 映射器
                Function.identity(),      // Value 就是对象本身
                (k1, k2) -> k1 // 键冲突时的合并策略
        ));
    }

    @Override
    public void industrySet(Map<String, IndustryCategory> inMap, List<String> industryCategoryList,
                            List<String> industryCodeList, List<List<String>> industryList, String industryCategory) {
        if (StringUtils.isEmpty(industryCategory)) {
            return;
        }
        /* 类别示例
            "industryCategory": "94,610",
            "industryCategoryList": ["铜矿采选", "汽柴油车整车制造"],
            "industryCodeList": ["C1234","A12344"],
            "industryList": [[12,79,92,94],[12,79,92,610]],
            */
        for (String id : industryCategory.split(",")) {
            List<String> ids = new ArrayList<>();
            try {
                if (inMap.containsKey(id)) {
                    IndustryCategory in = inMap.get(id);
                    if ("-1".equals(in.getPid())) {
                        industryCodeList.add(in.getCode());
                    } else {
                        String cCode = industryCodes(inMap, ids, id);
                        industryCodeList.add(cCode + in.getCode());
                    }
                    // 反转列表
                    Collections.reverse(ids);
                    industryList.add(ids);
                    industryCategoryList.add(in.getName());
                }
            } catch (Exception ex) {
                log.error("", ex);
            }
        }
    }

    private String industryCodes(Map<String, IndustryCategory> inMap, List<String> ids, String id) {
        if (null == id || !inMap.containsKey(id)) {
            return "";
        }
        IndustryCategory in = inMap.get(id);
        ids.add(in.getId());
        if ("-1".equals(in.getPid())) {
            return in.getCode();
        }
        return industryCodes(inMap, ids, in.getPid());
    }
}
