package com.zkhf.epmis.platform.mapper.emergency;

import com.zkhf.epmis.platform.emergency.domain.EmergencyVehicle;
import com.zkhf.epmis.platform.emergency.domain.EmergencyVehicleReq;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 应急车辆Mapper。
 * 负责应急车辆数据的查询、维护和批量导入写入。
 */
public interface EmergencyVehicleMapper {

    /**
     * 新增应急车辆。
     *
     * @param emergencyVehicle 车辆信息
     * @return 影响行数
     */
    int insert(EmergencyVehicle emergencyVehicle);

    /**
     * 批量新增应急车辆。
     *
     * @param list 车辆列表
     * @return 影响行数
     */
    int batchInsert(@Param("list") List<EmergencyVehicle> list);

    /**
     * 更新应急车辆。
     *
     * @param emergencyVehicle 车辆信息
     * @return 影响行数
     */
    int update(EmergencyVehicle emergencyVehicle);

    /**
     * 根据车辆ID删除记录。
     *
     * @param vehicleId 车辆ID
     * @return 影响行数
     */
    int deleteById(@Param("vehicleId") String vehicleId);

    /**
     * 根据车辆ID查询详情。
     *
     * @param vehicleId 车辆ID
     * @return 车辆详情
     */
    EmergencyVehicle selectById(@Param("vehicleId") String vehicleId);

    /**
     * 按条件查询应急车辆列表。
     *
     * @param req 查询条件
     * @return 车辆列表
     */
    List<EmergencyVehicle> selectList(EmergencyVehicleReq req);

    /**
     * 查询全部应急车辆。
     *
     * @return 车辆列表
     */
    List<EmergencyVehicle> selectAll();
}
