package com.zkhf.epmis.platform.mapper.ent;

import com.zkhf.epmis.platform.ent.domain.ExtUnit;
import com.zkhf.epmis.platform.ent.domain.ExtUnitReq;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 第三方单位库Mapper接口
 */
public interface ExtUnitLibMapper {

    /**
     * 查询第三方单位列表
     */
    List<ExtUnit> selectExtUnitList(ExtUnitReq req);

    /**
     * 新增第三方单位
     */
    int checkExistsUnitCode(String unitCode);

    /**
     * 新增第三方单位
     */
    int insertExtUnit(ExtUnit info);

    /**
     * 修改第三方单位
     */
    int updateExtUnit(ExtUnit info);

    /**
     * 删除旧的关联关系
     */
    void deleteUnitUser(Long userId);

    /**
     * 修改第三方单位
     */
    int insertUnitUser(@Param("userId") Long userId, @Param("unitCodes") List<String> unitCodes);
}
