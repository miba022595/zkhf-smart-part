package com.zkhf.epmis.process.mapper.solidWaste;

import com.zkhf.epmis.process.solidWaste.domain.WasteDict;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 固废分类字典Mapper接口
 */
public interface WasteDictMapper {

    /**
     * 查询固废分类字典列表
     */
    @Select("<script> " +
            " select `id`, `pid`, `name`, `code`, `tag`, `ext_name` " +
            " from t_waste_dict " +
            " <where> " +
            "    <if test='pid != null'> " +
            "        and pid = #{pid} " +
            "    </if> " +
            " </where> " +
            " order by " +
            " <choose>" +
            "   <when test='pid == null'>" +
            "       pid, id" +
            "   </when>" +
            "   <otherwise>" +
            "       id " +
            "   </otherwise>" +
            " </choose>" +
            "</script> ")
    List<WasteDict> selectWasteDictList(Long pid);
}