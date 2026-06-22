package com.zkhf.epmis.platform.mapper.plc;

import com.zkhf.epmis.platform.plc.domain.PlcInfo;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * PLC设备点位信息Mapper接口
 */
public interface PlcMapper {

    /**
     * 查询企业PLC设备点位列表（根企业查全部的）
     */
    List<PlcInfo> selectEntPlcRootList(String entCode);

    /**
     * 查询企业PLC设备点位列表（查归属企业的）
     */
    List<PlcInfo> selectEntPlcItemList(@Param("entCode") String entCode, @Param("status") Integer status);

    /**
     * 企业和单元编号唯一关联校验
     */
    String selectEntCodeByUnitId(@Param("entCode") String entCode, @Param("unitId") Integer unitId);

    /**
     * 根据点位ID查询所属企业的全部点位列表
     */
    List<PlcInfo> selectEntPlcListById(Long id);

    /**
     * 修改单个PLC点位
     */
    int updatePlc(PlcInfo info);

    /**
     * 删除企业PLC设备点位
     */
    void deleteEntPlc(@Param("entCode") String entCode);

    /**
     * 更新企业的PLC设备点位
     */
    int batchInsertEntPlc(@Param("plcList") List<PlcInfo> plcList);
}