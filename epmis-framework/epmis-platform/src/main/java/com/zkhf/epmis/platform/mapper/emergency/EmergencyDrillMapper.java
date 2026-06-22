package com.zkhf.epmis.platform.mapper.emergency;

import com.zkhf.epmis.platform.emergency.domain.EmergencyDrill;
import com.zkhf.epmis.platform.emergency.domain.EmergencyDrillReq;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 应急演练Mapper。
 * 负责应急演练数据的查询、维护和批量导入写入。
 */
public interface EmergencyDrillMapper {

    /**
     * 新增应急演练。
     *
     * @param emergencyDrill 演练信息
     * @return 影响行数
     */
    int insert(EmergencyDrill emergencyDrill);

    /**
     * 批量新增应急演练。
     *
     * @param list 演练列表
     * @return 影响行数
     */
    int batchInsert(@Param("list") List<EmergencyDrill> list);

    /**
     * 更新应急演练。
     *
     * @param emergencyDrill 演练信息
     * @return 影响行数
     */
    int update(EmergencyDrill emergencyDrill);

    /**
     * 根据演练ID删除记录。
     *
     * @param drillId 演练ID
     * @return 影响行数
     */
    int deleteById(@Param("drillId") String drillId);

    /**
     * 根据演练ID查询详情。
     *
     * @param drillId 演练ID
     * @return 演练详情
     */
    EmergencyDrill selectById(@Param("drillId") String drillId);

    /**
     * 按条件查询应急演练列表。
     *
     * @param req 查询条件
     * @return 演练列表
     */
    List<EmergencyDrill> selectList(EmergencyDrillReq req);

    /**
     * 查询全部应急演练。
     *
     * @return 演练列表
     */
    List<EmergencyDrill> selectAll();
}
