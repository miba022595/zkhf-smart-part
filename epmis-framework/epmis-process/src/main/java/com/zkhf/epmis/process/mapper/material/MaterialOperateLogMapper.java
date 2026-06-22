package com.zkhf.epmis.process.mapper.material;

import com.zkhf.epmis.process.material.domain.MaterialOperateLog;
import org.apache.ibatis.annotations.*;

import java.util.List;

/**
 * 物资操作日志Mapper接口
 */
public interface MaterialOperateLogMapper {

    /**
     * 查询物资相关操作日志列表
     */
    @Select("""
            <script>
            select oper_id, title, business_type, oper_name, oper_param, json_result, status, error_msg, oper_time, cost_time
            from sys_oper_log
            where title like '物资%'
              and (
                (#{bizId} is not null and #{bizId} != '' and oper_param like concat('%', #{bizId}, '%'))
                or
                (#{bizNo} is not null and #{bizNo} != '' and oper_param like concat('%', #{bizNo}, '%'))
              )
            order by oper_time desc, oper_id desc
            limit 100
            </script>
            """)
    @Results(id = "materialOperateLogResult", value = {
            @Result(property = "operId", column = "oper_id"),
            @Result(property = "businessType", column = "business_type"),
            @Result(property = "operName", column = "oper_name"),
            @Result(property = "operParam", column = "oper_param"),
            @Result(property = "jsonResult", column = "json_result"),
            @Result(property = "errorMsg", column = "error_msg"),
            @Result(property = "operTime", column = "oper_time"),
            @Result(property = "costTime", column = "cost_time")
    })
    List<MaterialOperateLog> selectMaterialOperateLogList(@Param("bizId") String bizId, @Param("bizNo") String bizNo);
}
