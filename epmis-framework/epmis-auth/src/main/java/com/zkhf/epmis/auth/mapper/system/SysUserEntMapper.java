package com.zkhf.epmis.auth.mapper.system;

import com.zkhf.epmis.auth.core.domain.entity.SysEnt;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 用户与企业关联表 数据层
 */
public interface SysUserEntMapper {

    /**
     * 查询用户的企业列表
     */
    List<SysEnt> selectEntList(@Param("loginUserId") Long loginUserId, @Param("queryUserId") Long queryUserId);

    /**
     * 通过用户ID删除用户和企业关联
     */
    void deleteUserEntByUserId(@Param("userId") Long userId);

    /**
     * 批量新增用户企业关联
     */
    void batchInsertUserEnt(@Param("userId") Long userId, @Param("entCodeList") List<SysEnt> entCodeList);
}
