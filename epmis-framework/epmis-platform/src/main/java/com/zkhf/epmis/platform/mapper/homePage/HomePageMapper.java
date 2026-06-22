package com.zkhf.epmis.platform.mapper.homePage;

import io.lettuce.core.dynamic.annotation.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;
import java.util.Map;

/**
 * 主页的Mapper接口
 */
public interface HomePageMapper {

    /**
     * 环境治理设施统计
     */
    @Select(" select out_put_type as outPutType, count(facility_id) as governanceCount " +
            " from t_ent_poll_control_facility " +
            " where ent_code = #{entCode} " +
            " group by out_put_type" +
            " order by governanceCount desc ")
    List<Map<String, Object>> cockpitEnvGovernanceStatistics(@Param("entCode") String entCode);

    /**
     * 环境监测设施统计
     */
    @Select(" select o.out_put_type as outPutType, sum(d.device_quantity) as monitorCount " +
            " from t_device_info d, t_ent_out_put_info o " +
            " where o.ent_code = #{entCode} and d.mn_num = o.mn_num " +
            " group by o.out_put_type" +
            " order by monitorCount desc ")
    List<Map<String, Object>> cockpitEnvMonitorStatistics(@Param("entCode") String entCode);
}
