package com.zkhf.epmis.process.solidWaste.service.impl;

import com.zkhf.epmis.process.base.utils.RedisCacheUtils;
import com.zkhf.epmis.process.mapper.solidWaste.WasteDictMapper;
import com.zkhf.epmis.process.solidWaste.domain.WasteDict;
import com.zkhf.epmis.process.solidWaste.service.WasteDictService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * 固废分类字典Service业务层处理
 */
@Service
public class WasteDictServiceImpl implements WasteDictService {

    private WasteDictMapper wasteDictMapper;
    @Autowired
    public void setWasteDictMapper(WasteDictMapper wasteDictMapper) {
        this.wasteDictMapper = wasteDictMapper;
    }

    private RedisCacheUtils redisCacheUtils;
    @Autowired
    public void setRedisCacheUtils(RedisCacheUtils redisCacheUtils) {
        this.redisCacheUtils = redisCacheUtils;
    }

    @Override
    public List<WasteDict> selectWasteDictList(Long pid) {
        List<WasteDict> list;
        if (null == pid) {
            // 先判断是否要更新缓存，需要时更新
            if (redisCacheUtils.checkUpdateCache("wasteDictExistsKey", 24, TimeUnit.HOURS)) {
                list = wasteDictMapper.selectWasteDictList(null);
                if (null != list && !list.isEmpty()) {
                    redisCacheUtils.setWasteDictCache(list);
                }
            } else {
                list = redisCacheUtils.getWasteDictDictCache();
            }
        } else {
            list = wasteDictMapper.selectWasteDictList(pid);
        }
        return list;
    }
}
