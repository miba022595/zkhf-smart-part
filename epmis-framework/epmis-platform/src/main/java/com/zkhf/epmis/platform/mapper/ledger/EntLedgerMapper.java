package com.zkhf.epmis.platform.mapper.ledger;

import com.zkhf.epmis.platform.ledger.domain.GovernFacility;
import com.zkhf.epmis.platform.ledger.domain.MonitorFacility;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Result;
import org.apache.ibatis.annotations.Results;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 企业台账 Mapper接口
 */
public interface EntLedgerMapper {

    /**
     * 监测设施台账
     */
    @Select("<script> " +
            " select " +
            "  d.mn_num, d.mn_name, d.device_brand, d.device_model, d.device_quantity, d.setup_time, d.lifespan, d.life_unit, " +
            "  e.ent_code, e.ent_name, " +
            "  f.facility_id, f.facility_code, f.facility_name " +
            " from t_device_info d " +
            " inner join t_ent_out_put_info o on d.mn_num = o.mn_num " +
            " inner join t_bas_enterprise e on e.ent_code = o.ent_code " +
            " left join t_ent_poll_control_facility_relate r on r.other_id = o.out_put_id and r.other_type = #{otherType} " +
            " left join t_ent_poll_control_facility f on f.facility_id = r.facility_id " +
            " <where> " +
            "   <if test='entCodes != null and entCodes.size() > 0'> " +
            "       and o.ent_code in " +
            "       <foreach collection='entCodes' item='entCode' open='(' separator=',' close=')'> " +
            "           #{entCode} " +
            "       </foreach> " +
            "   </if>" +
            " </where> " +
            " order by d.mn_num desc, f.facility_id desc " +
            "</script> ")
    @Results({
            @Result(property = "mnNum", column = "mn_num"),
            @Result(property = "mnName", column = "mn_name"),
            @Result(property = "deviceBrand", column = "device_brand"),
            @Result(property = "deviceModel", column = "device_model"),
            @Result(property = "deviceQuantity", column = "device_quantity"),
            @Result(property = "setupTime", column = "setup_time"),
            @Result(property = "lifespan", column = "lifespan"),
            @Result(property = "lifeUnit", column = "life_unit"),
            @Result(property = "entCode", column = "ent_code"),
            @Result(property = "entName", column = "ent_name"),
            @Result(property = "facilityId", column = "facility_id"),
            @Result(property = "facilityCode", column = "facility_code"),
            @Result(property = "facilityName", column = "facility_name")
    })
    List<MonitorFacility> monitorFacilityList(@Param("entCodes") List<String> entCodes, @Param("otherType") String otherType);

    /**
     * 治理设施台账
     */
    @Select("<script> " +
            " select " +
            "  f.facility_id, f.facility_code, f.facility_name, e.ent_code, e.ent_name, " +
            "  d.mn_num, d.mn_name, d.device_quantity, d.setup_time, d.lifespan, d.life_unit " +
            " from t_ent_poll_control_facility f " +
            " left join t_bas_enterprise e on e.ent_code = f.ent_code " +
            " left join t_ent_poll_control_facility_relate r on r.facility_id = f.facility_id and r.other_type = #{otherType} " +
            " left join t_ent_out_put_info o on o.out_put_id = r.other_id " +
            " left join t_device_info d on d.mn_num = o.mn_num " +
            " <where> " +
            "   <if test='entCodes != null and entCodes.size() > 0'> " +
            "       and f.ent_code in " +
            "       <foreach collection='entCodes' item='entCode' open='(' separator=',' close=')'> " +
            "           #{entCode} " +
            "       </foreach> " +
            "   </if>" +
            " </where> " +
            " order by f.facility_id desc, d.mn_num desc " +
            "</script> ")
    @Results({
            @Result(property = "facilityId", column = "facility_id"),
            @Result(property = "facilityCode", column = "facility_code"),
            @Result(property = "facilityName", column = "facility_name"),
            @Result(property = "entCode", column = "ent_code"),
            @Result(property = "entName", column = "ent_name"),
            @Result(property = "mnNum", column = "mn_num"),
            @Result(property = "mnName", column = "mn_name"),
            @Result(property = "deviceQuantity", column = "device_quantity"),
            @Result(property = "setupTime", column = "setup_time"),
            @Result(property = "lifespan", column = "lifespan"),
            @Result(property = "lifeUnit", column = "life_unit")
    })
    List<GovernFacility> governFacilityList(@Param("entCodes") List<String> entCodes, @Param("otherType") String otherType);

}
