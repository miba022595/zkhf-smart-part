package com.zkhf.epmis.process.mapper.base;

import com.zkhf.epmis.process.base.domain.EntInfo;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;

import java.util.List;

public interface CursorMapper {

    // 删除临时表
    @Delete("DROP TEMPORARY TABLE IF EXISTS temp_${tableName}")
    void dropTempTable(@Param("tableName") String tableName);

    /** 临时表创建 */
    @Update("CREATE TEMPORARY TABLE IF NOT EXISTS temp_${tableName} (" +
            " ent_code varchar(40) NOT NULL " +
            " PRIMARY KEY (`ent_code`)" +
            ")")
    void createEntDynamicTable(@Param("tableName") String tableName);
    /** 插入临时表数据 */
    @Insert("<script> " +
            "   INSERT INTO temp_${tableName}" +
            "       (ent_code) " +
            "   VALUES " +
            "   <foreach item='info' collection='list' separator=','> " +
            "       (#{info.entCode})" +
            "   </foreach> " +
            "</script> ")
    void batchInsertEntDynamicData(@Param("list") List<EntInfo> list, @Param("tableName") String tableName);

}
