package com.zkhf.epmis.platform.mapper.ent;

import com.zkhf.epmis.platform.ent.domain.EntProductionLine;
import com.zkhf.epmis.platform.ent.domain.EntProductionLineReq;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

/**
 * 企业生产线信息Mapper接口
 */
public interface EntProductionLineMapper {

    /**
     * 查询企业生产线信息
     */
    EntProductionLine selectEntProductionLineByLineId(@Param("lineId") String lineId);

    /**
     * 查询企业生产线信息列表
     */
    List<EntProductionLine> selectEntProductionLineList(EntProductionLineReq req);

    /**
     * 查询企业生产线关联的信息列表
     */
    List<Map<String, Object>> selectEntProductionLineRelateList(@Param("lineIds") List<String> lineIds,
                                                                @Param("relateProduceFacilityType") String relateProduceFacilityType,
                                                                @Param("relateGovernanceFacilityType") String relateGovernanceFacilityType,
                                                                @Param("relateOutPutType") String relateOutPutType);

    /**
     * 依据车间、生产线编号查询生产线id，从而判断生产线编码重复问题
     * 存在即重复
     */
    String selectLineIdByWorkLineCode(@Param("workshopId") String workshopId, @Param("lineCode") String lineCode);

    /**
     * 新增企业生产线信息
     */
    int insertEntProductionLine(EntProductionLine info);

    /**
     * 依据生产线id（确定生产车间）、生产线编号查询生产线id，从而判断生产线编码重复问题
     * id存在且不是当前的id则重复
     */
    String selectLineIdByLineIdCode(@Param("lineId") String lineId, @Param("lineCode") String lineCode);

    /**
     * 修改企业生产线信息
     */
    int updateEntProductionLine(EntProductionLine info);

    /**
     * 删除企业生产线信息
     */
    int deleteEntProductionLineById(String lineId);
}
