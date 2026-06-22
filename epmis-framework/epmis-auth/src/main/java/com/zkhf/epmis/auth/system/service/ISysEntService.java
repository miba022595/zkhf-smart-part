package com.zkhf.epmis.auth.system.service;

import com.zkhf.epmis.auth.core.domain.entity.SysEnt;
import com.zkhf.epmis.auth.core.domain.entity.SysEntTree;

import java.util.List;

/**
 * 企业业务层
 */
public interface ISysEntService {

    /**
     * 校验是否有企业权限
     *
     * @param entList 企业列表
     */
    void checkEntDataScope(List<SysEnt> entList);

    /**
     * 批量选择授权用户企业
     *
     * @param userId  用户ID
     * @param entCodeList 需要更新的用户企业
     */
    void insertUserEnt(Long userId, List<SysEnt> entCodeList);

    /**
     * 通过用户ID删除用户和企业关联
     *
     * @param userId 用户ID
     */
    void deleteUserEntByUserId(Long userId);

    /**
     * 根据用户ID查询企业列表
     */
    List<SysEntTree> selectEntByUserId(Long userId, boolean removeNoAuth);
}