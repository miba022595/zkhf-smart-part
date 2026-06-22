package com.zkhf.epmis.platform.mapper.emergency;

import com.zkhf.epmis.platform.emergency.domain.EmergencyExpert;
import com.zkhf.epmis.platform.emergency.domain.EmergencyExpertReq;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 应急专家Mapper。
 * 负责应急专家数据的查询、维护和批量导入写入。
 */
public interface EmergencyExpertMapper {

    /**
     * 新增应急专家。
     *
     * @param emergencyExpert 专家信息
     * @return 影响行数
     */
    int insert(EmergencyExpert emergencyExpert);

    /**
     * 批量新增应急专家。
     *
     * @param list 专家列表
     * @return 影响行数
     */
    int batchInsert(@Param("list") List<EmergencyExpert> list);

    /**
     * 更新应急专家。
     *
     * @param emergencyExpert 专家信息
     * @return 影响行数
     */
    int update(EmergencyExpert emergencyExpert);

    /**
     * 根据专家ID删除记录。
     *
     * @param expertId 专家ID
     * @return 影响行数
     */
    int deleteById(@Param("expertId") String expertId);

    /**
     * 根据专家ID查询详情。
     *
     * @param expertId 专家ID
     * @return 专家详情
     */
    EmergencyExpert selectById(@Param("expertId") String expertId);

    /**
     * 按条件查询应急专家列表。
     *
     * @param req 查询条件
     * @return 专家列表
     */
    List<EmergencyExpert> selectList(EmergencyExpertReq req);

    /**
     * 查询全部应急专家。
     *
     * @return 专家列表
     */
    List<EmergencyExpert> selectAll();
}
