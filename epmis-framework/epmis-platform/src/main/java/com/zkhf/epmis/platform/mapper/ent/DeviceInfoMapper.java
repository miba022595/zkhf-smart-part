package com.zkhf.epmis.platform.mapper.ent;

import com.zkhf.epmis.platform.ent.domain.DeviceInfo;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 设备信息Mapper接口
 */
public interface DeviceInfoMapper {

    /**
     * 查询设备信息
     */
    DeviceInfo selectDeviceInfoByMnNum(String mnNum);

    /**
     * 查询设备信息列表
     */
    List<DeviceInfo> selectDeviceInfoList(@Param("entCodes") List<String> entCodes);

    /**
     * 新增或修改设备信息
     */
    int insertOrUpdateDeviceInfo(DeviceInfo info);

}
