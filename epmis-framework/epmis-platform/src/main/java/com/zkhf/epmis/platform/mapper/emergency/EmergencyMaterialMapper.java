package com.zkhf.epmis.platform.mapper.emergency;

import com.zkhf.epmis.platform.emergency.domain.EmergencyMaterial;
import com.zkhf.epmis.platform.emergency.domain.EmergencyMaterialReq;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 应急物资Mapper。
 * 负责应急物资数据的查询、维护以及预警列表读取。
 */
public interface EmergencyMaterialMapper {

    /**
     * 新增应急物资。
     *
     * @param emergencyMaterial 物资信息
     * @return 影响行数
     */
    int insert(EmergencyMaterial emergencyMaterial);

    /**
     * 更新应急物资。
     *
     * @param emergencyMaterial 物资信息
     * @return 影响行数
     */
    int update(EmergencyMaterial emergencyMaterial);

    /**
     * 根据物资ID删除记录。
     *
     * @param materialId 物资ID
     * @return 影响行数
     */
    int deleteById(@Param("materialId") String materialId);

    /**
     * 根据物资ID查询详情。
     *
     * @param materialId 物资ID
     * @return 物资详情
     */
    EmergencyMaterial selectById(@Param("materialId") String materialId);

    /**
     * 按条件查询应急物资列表。
     *
     * @param req 查询条件
     * @return 物资列表
     */
    List<EmergencyMaterial> selectList(EmergencyMaterialReq req);

    /**
     * 查询预警中的应急物资。
     *
     * @param entCodes 企业编码范围
     * @return 预警物资列表
     */
    List<EmergencyMaterial> selectWarnList(@Param("entCodes") List<String> entCodes);

    /**
     * 查询全部应急物资。
     *
     * @return 物资列表
     */
    List<EmergencyMaterial> selectAll();
}
