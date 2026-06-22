package com.zkhf.epmis.platform.base.service.impl;

import com.zkhf.epmis.core.domain.AjaxResult;
import com.zkhf.epmis.platform.base.domain.Districts;
import com.zkhf.epmis.platform.base.service.DistrictsService;
import com.zkhf.epmis.platform.mapper.base.DistrictsMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 地区Service业务层处理
 */
@Service
public class DistrictsServiceImpl implements DistrictsService {

    private DistrictsMapper districtsMapper;

    @Autowired
    public void setDistrictsMapper(DistrictsMapper districtsMapper) {
        this.districtsMapper = districtsMapper;
    }

    @Override
    public AjaxResult selectDistrictsSingleList(Long pid) {
        if (null == pid || pid < 0) {
            return AjaxResult.error("错误的查询参数");
        }
        return AjaxResult.success(districtsMapper.selectDistrictsSingleListByPid(pid));
    }

    @Override
    public List<Districts> selectDistrictsList() {
        return districtsMapper.selectDistrictsList();
    }

    @Override
    public AjaxResult insertDistricts(Districts info) {
        districtsMapper.insertDistricts(info);
        return AjaxResult.success();
    }

    @Override
    public AjaxResult updateDistricts(Districts info) {
        districtsMapper.updateDistricts(info);
        return AjaxResult.success();
    }

    @Override
    public AjaxResult deleteDistrictsById(Long id) {
        if (null == id || id < 0) {
            return AjaxResult.error("错误的参数");
        }
        deleteById(id);
        return AjaxResult.success();
    }

    private void deleteById(Long id) {
        if (null == id || id < 0) {
            return;
        }
        List<Districts> sub = districtsMapper.selectDistrictsSingleListByPid(id);
        if (null != sub && sub.size() > 0) {
            sub.forEach(e -> deleteById(e.getId()));
        }
        districtsMapper.deleteDistrictsById(id);
    }
}
