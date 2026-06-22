package com.zkhf.epmis.auth.system.service.impl;

import com.zkhf.epmis.auth.core.domain.entity.SysEnt;
import com.zkhf.epmis.auth.core.domain.entity.SysEntTree;
import com.zkhf.epmis.auth.core.domain.entity.SysUser;
import com.zkhf.epmis.auth.mapper.system.SysUserEntMapper;
import com.zkhf.epmis.auth.system.service.ISysEntService;
import com.zkhf.epmis.auth.utils.SecurityUtils;
import com.zkhf.epmis.core.exception.ServiceException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;

/**
 * 企业 业务层处理
 */
@Service
public class SysEntServiceImpl implements ISysEntService {

    private SysUserEntMapper sysUserEntMapper;
    @Autowired
    public void setSysUserEntMapper(SysUserEntMapper sysUserEntMapper) {
        this.sysUserEntMapper = sysUserEntMapper;
    }

    @Override
    public void checkEntDataScope(List<SysEnt> entList) {
        if (entList == null || entList.isEmpty()) {
            return;
        }
        SysUser user = SecurityUtils.getLoginUser().getUser();
        List<SysEnt> authEntList;
        if (!user.isAdmin()) {
            authEntList = sysUserEntMapper.selectEntList(user.getUserId(), null);
            List<String> entCodes = new ArrayList<>();
            authEntList.forEach( e -> entCodes.add(e.getEntCode()));
            for (SysEnt ent : entList) {
                if (!entCodes.contains(ent.getEntCode())) {
                    throw new ServiceException("没有权限访问企业数据！");
                }
            }
        }
    }

    @Override
    public void insertUserEnt(Long userId, List<SysEnt> entCodeList) {
        if (null == userId || null == entCodeList || entCodeList.isEmpty()) {
            return;
        }
        sysUserEntMapper.batchInsertUserEnt(userId, entCodeList);
    }

    @Override
    public void deleteUserEntByUserId(Long userId) {
        sysUserEntMapper.deleteUserEntByUserId(userId);
    }

    @Override
    public List<SysEntTree> selectEntByUserId(Long userId, boolean removeNoAuth) {
        SysUser user = SecurityUtils.getLoginUser().getUser();
        // 1. 数据库查询登录账号和查询账号的权限列表（可能其他账号修改了登录账号的权限信息）
        // 2. 获取用户权限列表
        List<SysEnt> entList;
        if (user.isAdmin()) {
            if (null == userId || userId.equals(user.getUserId())) {
                entList = sysUserEntMapper.selectEntList(null, null);
            } else {
                entList = sysUserEntMapper.selectEntList(null, userId);
            }
        } else {
            if (null == userId || userId.equals(user.getUserId())) {
                entList = sysUserEntMapper.selectEntList(user.getUserId(), null);
            } else {
                entList = sysUserEntMapper.selectEntList(user.getUserId(), userId);
            }
        }
        if (entList.isEmpty()) {
            return new ArrayList<>();
        }
        // 判断是否查询登录账号的企业列表
        boolean isLogin = null == userId || userId.equals(user.getUserId());
        List<SysEntTree> loginEntList = new ArrayList<>();
        Set<String> selUserCodeList = new HashSet<>();
        entList.forEach( e -> {
            if (isLogin || userId.equals(e.getUserId())) {
                selUserCodeList.add(e.getEntCode());
            }
            if (user.isAdmin() || user.getUserId().equals(e.getUserId())) {
                loginEntList.add(SysEntTree.builder()
                                .entCode(e.getEntCode())
                                .entName(e.getEntName())
                                .parentCode(e.getParentCode())
                                .socialCreditCode(e.getSocialCreditCode())
                                .auth(false)
                                .subList(new ArrayList<>())
                        .build());
            }
        });
        // 3. 构建树形结构（不移除节点，用于展示完整结构；移除无权限部分，用于权限控制）
        return buildTreeWithAuthMarks(loginEntList, selUserCodeList, removeNoAuth);
    }

    /**
     * 构建树形结构并设置权限标记
     * @param flatList 扁平的企业列表
     * @param authEntCodes 有权限的企业编码列表
     * @param removeNoAuth 是否移除无权限的子树或整棵树
     * @return 处理后的树形结构
     */
    public List<SysEntTree> buildTreeWithAuthMarks(List<SysEntTree> flatList, Set<String> authEntCodes, boolean removeNoAuth) {
        if (flatList == null || flatList.isEmpty()) {
            return Collections.emptyList();
        }
        // 1. 构建基础树形结构
        List<SysEntTree> tree = buildBaseTree(flatList);
        // 2. 设置权限标记
        for (SysEntTree root : tree) {
            markNodeAuth(root, authEntCodes);
        }
        // 3. 根据参数决定是否移除
        if (removeNoAuth) {
            return filterTreeByAuth(tree);
        }
        return tree;
    }

    /**
     * 构建基础树形结构
     */
    private List<SysEntTree> buildBaseTree(List<SysEntTree> flatList) {
        // 转换为Map便于查找
        Map<String, SysEntTree> entMap = new HashMap<>();
        for (SysEntTree ent : flatList) {
            // 初始化子节点列表
            ent.setSubList(new ArrayList<>());
            entMap.put(ent.getEntCode(), ent);
        }
        // 构建父子关系
        List<SysEntTree> roots = new ArrayList<>();
        for (SysEntTree ent : flatList) {
            String parentCode = ent.getParentCode();
            // 判断是否为根节点：父节点不存在；父节点不在列表中的为游离节点，忽略
            if (parentCode == null || parentCode.isEmpty()) {
                roots.add(ent);
            } else if (entMap.containsKey(parentCode)){
                // 添加到父节点的子节点列表
                SysEntTree parent = entMap.get(parentCode);
                parent.getSubList().add(ent);
            }
        }
        return roots;
    }

    /**
     * 标记节点自身权限（不传播到父节点）
     */
    private void markNodeAuth(SysEntTree node, Set<String> authSet) {
        // 只标记节点自身是否有权限
        node.setAuth(authSet.contains(node.getEntCode()));
        // 递归标记子节点
        if (node.getSubList() != null && !node.getSubList().isEmpty()) {
            for (SysEntTree child : node.getSubList()) {
                markNodeAuth(child, authSet);
            }
        }
    }

    /**
     * 根据权限过滤树
     */
    private List<SysEntTree> filterTreeByAuth(List<SysEntTree> nodes) {
        List<SysEntTree> result = new ArrayList<>();
        for (SysEntTree node : nodes) {
            // 检查当前节点及其子树是否有权限
            if (checkSubtreeAuth(node)) {
                // 创建过滤后的节点
                SysEntTree filteredNode = createFilteredNode(node);
                result.add(filteredNode);
            }
            // 整棵子树都无权限，完全跳过
        }

        return result;
    }

    /**
     * 检查子树中是否有权限节点
     */
    private boolean checkSubtreeAuth(SysEntTree node) {
        // 当前节点有权限
        if (node.isAuth()) {
            return true;
        }
        // 检查子节点
        if (node.getSubList() != null && !node.getSubList().isEmpty()) {
            for (SysEntTree child : node.getSubList()) {
                if (checkSubtreeAuth(child)) {
                    return true;
                }
            }
        }

        return false;
    }

    /**
     * 创建过滤后的节点（包含符合条件的子节点）
     */
    private SysEntTree createFilteredNode(SysEntTree original) {
        // 复制基本信息
        SysEntTree filtered = copyNodeWithoutChildren(original);

        // 递归过滤子节点
        if (original.getSubList() != null && !original.getSubList().isEmpty()) {
            List<SysEntTree> filteredChildren = new ArrayList<>();

            for (SysEntTree child : original.getSubList()) {
                // 只保留子树中有权限的子节点
                if (checkSubtreeAuth(child)) {
                    filteredChildren.add(createFilteredNode(child));
                }
            }
            filtered.setSubList(filteredChildren);
        }
        return filtered;
    }


    /**
     * 复制节点（不包含子节点）
     */
    private SysEntTree copyNodeWithoutChildren(SysEntTree original) {
        return SysEntTree.builder()
                .entCode(original.getEntCode())
                .entName(original.getEntName())
                .parentCode(original.getParentCode())
                .socialCreditCode(original.getSocialCreditCode())
                .auth(original.isAuth())
                .subList(new ArrayList<>())
                .build();
    }
}
