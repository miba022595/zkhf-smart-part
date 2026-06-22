package com.zkhf.epmis.platform.envProtect.policy.service.impl;

import com.alibaba.fastjson2.JSONObject;
import com.github.f4b6a3.ulid.UlidCreator;
import com.zkhf.epmis.core.annotation.Log;
import com.zkhf.epmis.core.domain.AjaxResult;
import com.zkhf.epmis.core.enums.BusinessType;
import com.zkhf.epmis.core.utils.PageUtils;
import com.zkhf.epmis.core.utils.StringUtils;
import com.zkhf.epmis.platform.envProtect.policy.domain.*;
import com.zkhf.epmis.platform.envProtect.policy.service.EnvLearnService;
import com.zkhf.epmis.platform.global.GVarContainer;
import com.zkhf.epmis.platform.mapper.envProtect.policy.EnvLearnMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 环境政策法规信息学习Service业务层处理
 */
@Service
public class EnvLearnServiceImpl implements EnvLearnService {

    private EnvLearnMapper envLearnMapper;
    @Autowired
    public void setEnvLearnManageMapper(EnvLearnMapper envLearnMapper) {
        this.envLearnMapper = envLearnMapper;
    }

    @Override
    public AjaxResult selectEnvLearnList(EnvLearnReq req) {
        if (null == req) {
            req = new EnvLearnReq();
        }
        // 添加权限：非管理员时判断是不是企业管理员
        if (GVarContainer.isNotAdmin()) {
            req.setEntAdmin(GVarContainer.isEntAdmin());
            req.setUserId(GVarContainer.getUserId());
        } else {
            req.setUserId(null);
        }
        PageUtils.startPage();
        List<EnvLearn> list = envLearnMapper.selectEnvLearnList(req);
        // 设置信息
        if (!list.isEmpty()) {
            List<String> learnIdList = list.stream().map(EnvLearn::getLearnId).collect(Collectors.toList());
            // 获取学习的法规
            List<EnvLearnPolicy> lpList = envLearnMapper.selectEnvLearnPolicy(learnIdList);
            Map<String, List<EnvLearnPolicy>> lPMap = new HashMap<>();
            lpList.forEach( e -> lPMap.computeIfAbsent(e.getLearnId(), k -> new ArrayList<>()).add(e));
            // 获取学习的企业
            List<EnvLearnEnt> lEList = envLearnMapper.selectEnvLearnEnt(learnIdList);
            Map<String, List<EnvLearnEnt>> lEMap = new HashMap<>();
            lEList.forEach( e -> lEMap.computeIfAbsent(e.getLearnId(), k -> new ArrayList<>()).add(e));
            // 获取学习的人员
            List<EnvLearnUser> learnUsers = envLearnMapper.selectEnvLearnUser(learnIdList);
            Map<String, List<EnvLearnUser>> lUMap = new HashMap<>();
            learnUsers.forEach( e -> lUMap.computeIfAbsent(e.getLearnId(), k -> new ArrayList<>()).add(e));
            // 填充数据
            for (EnvLearn e : list) {
                e.setPolicyList(lPMap.get(e.getLearnId()));
                e.setEntList(lEMap.get(e.getLearnId()));
                List<EnvLearnUser> subList = lUMap.get(e.getLearnId());
                if (null == subList || subList.isEmpty()) {
                    continue;
                }
                e.setUserList(subList);
                e.setUserNum(subList.size());
                if (null == e.getRequiredDuration() || e.getRequiredDuration() <= 0) {
                    continue;
                }
                long total = 0;
                for (EnvLearnUser sub : subList) {
                    if (null == sub.getCompletedDuration()) {
                        sub.setCompletedDuration(0L);
                    }
                    // 超出要求时长时按要求的统计
                    if (sub.getCompletedDuration() > e.getRequiredDuration()) {
                        total += e.getRequiredDuration();
                    } else {
                        total += sub.getCompletedDuration();
                    }
                }
                // 设置均值
                e.setCompletedDuration(total / e.getUserNum());
                // 设置进度
                e.setLearnRate(Math.round(e.getCompletedDuration() * 10000.0 / e.getRequiredDuration()) / 100.0);
            }
        }
        return PageUtils.getAjaxResult(list, true);
    }

    @Override
    @Log(title = "环境政策法规信息学习", businessType = BusinessType.INSERT)
    public AjaxResult insertEnvLearn(EnvLearn info) {
        info.setLearnId(UlidCreator.getMonotonicUlid().toString());
        int count = envLearnMapper.insertEnvLearn(info);
        if (count > 0) {
            // 学习政策法规
            if (null != info.getPolicyList() && !info.getPolicyList().isEmpty()) {
                info.getPolicyList().forEach( e -> e.setLearnId(info.getLearnId()));
                envLearnMapper.insertEnvLearnPolicy(info.getPolicyList());
            }
            // 学习企业
            if (null != info.getEntList() && !info.getEntList().isEmpty()) {
                info.getEntList().forEach( e -> e.setLearnId(info.getLearnId()));
                envLearnMapper.insertEnvLearnEnt(info.getEntList());
            }
            // 学习人员
            if (null != info.getUserList() && !info.getUserList().isEmpty()) {
                info.getUserList().forEach( e -> {
                    e.setLearnUserId(UlidCreator.getMonotonicUlid().toString());
                    e.setLearnId(info.getLearnId());
                    e.setCompletedDuration(0L);
                });
                envLearnMapper.insertEnvLearnUser(info.getUserList());
            }
        }
        return AjaxResult.success(count);
    }

    @Override
    @Log(title = "环境政策法规信息学习", businessType = BusinessType.UPDATE)
    public AjaxResult updateEnvLearn(EnvLearn info) {
        int count = envLearnMapper.updateEnvLearn(info);
        if (count > 0) {
            // 学习政策法规
            envLearnMapper.deleteEnvLearnPolicy(info.getLearnId());
            if (null != info.getPolicyList() && !info.getPolicyList().isEmpty()) {
                info.getPolicyList().forEach( e -> e.setLearnId(info.getLearnId()));
                envLearnMapper.insertEnvLearnPolicy(info.getPolicyList());
            }
            // 学习企业
            envLearnMapper.deleteEnvLearnEnt(info.getLearnId());
            if (null != info.getEntList() && !info.getEntList().isEmpty()) {
                info.getEntList().forEach( e -> e.setLearnId(info.getLearnId()));
                envLearnMapper.insertEnvLearnEnt(info.getEntList());
            }
            // 查询已有进度
            List<EnvLearnUser> oldUsers = envLearnMapper.selectEnvLearnUserByLearnId(info.getLearnId());
            Map<Long, String> oldMap = new HashMap<>();
            if (null != oldUsers && !oldUsers.isEmpty()) {
                oldUsers.forEach( e -> oldMap.put(e.getUserId(), e.getLearnUserId()));
            }
            // 判断学习人员
            List<EnvLearnUser> addList = new ArrayList<>();
            // 学习人员
            if (null != info.getUserList() && !info.getUserList().isEmpty()) {
                info.getUserList().forEach( e -> {
                    // 在旧map时不处理，移除旧map；不在旧man时为添加
                    if (oldMap.containsKey(e.getUserId())) {
                        oldMap.remove(e.getUserId());
                    } else {
                        e.setLearnUserId(UlidCreator.getMonotonicUlid().toString());
                        e.setLearnId(info.getLearnId());
                        e.setCompletedDuration(0L);
                        addList.add(e);
                    }
                });
            }
            // 添加新学习进度
            if (!addList.isEmpty()) {
                envLearnMapper.insertEnvLearnUser(addList);
            }
            // 旧map还有的需要删除，表示不在新列表里
            if (!oldMap.isEmpty()) {
                List<String> learnUserIds = new ArrayList<>(oldMap.values());
                // 删除旧学习进度表
                envLearnMapper.deleteEnvLearnUser(learnUserIds);
                // 删除旧学习详情信息
                envLearnMapper.deleteEnvLearnDetail(learnUserIds);
            }
        }
        return AjaxResult.success(count);
    }

    @Override
    @Log(title = "环境政策法规信息学习", businessType = BusinessType.DELETE)
    public AjaxResult deleteEnvLearnById(String learnId) {
        int count = envLearnMapper.deleteEnvLearnByLearnId(learnId);
        if (count > 0) {
            // 删除学习法规
            envLearnMapper.deleteEnvLearnPolicy(learnId);
            // 删除学习企业
            envLearnMapper.deleteEnvLearnEnt(learnId);
            // 删除学习详情信息表
            envLearnMapper.deleteEnvLearnDetailByLearnId(learnId);
            // 删除学习用户信息表
            envLearnMapper.deleteEnvLearnUserByLearnId(learnId);
        }
        return AjaxResult.success(count);
    }

    @Override
    public AjaxResult learnCountList(String learnId) {
        boolean page = PageUtils.startPageCheckExists();
        List<EnvLearnUser> list = envLearnMapper.selectEnvLearnUserByLearnId(learnId);
        if (null != list && !list.isEmpty()) {
            list.forEach( e -> {
                if (null == e.getCompletedDuration() || e.getCompletedDuration() < 0) {
                    e.setCompletedDuration(0L);
                }
                if (null != e.getRequiredDuration() && e.getRequiredDuration() > 0) {
                    e.setLearnRate(Math.round(e.getCompletedDuration() * 10000.0 / e.getRequiredDuration()) / 100.0);
                } else {
                    e.setRequiredDuration(0L);
                    e.setLearnRate(0d);
                }
            });
        }
        return PageUtils.getAjaxResult(list, page);
    }

    @Override
    public AjaxResult learnDetailList(String learnUserId) {
        boolean page = PageUtils.startPageCheckExists();
        List<LearnDetail> list = envLearnMapper.learnDetailList(learnUserId);
        return PageUtils.getAjaxResult(list, page);
    }

    @Override
    @Log(title = "环境政策法规学习详情", businessType = BusinessType.DELETE)
    public AjaxResult learnDetailUpdate(EnvLearnReq req) {
        if (null == req || StringUtils.isEmpty(req.getLearnUserId())) {
            return AjaxResult.error("未知的数据参数");
        }
        EnvLearnUser user = envLearnMapper.getLearnUserById(req.getLearnUserId());
        if (null == user) {
            return AjaxResult.error("未知的学习记录");
        }
        Long loginUser = GVarContainer.getUserId();
        if (null == loginUser) {
            return AjaxResult.error("未知的用户，不能学习");
        }
        if (null == user.getUserId() || !loginUser.equals(user.getUserId())) {
            return AjaxResult.error("不能替别人学习");
        }
        if (StringUtils.isEmpty(req.getLearnDetailId())) {
            req.setLearnDetailId(UlidCreator.getMonotonicUlid().toString());
            req.setNewLearnDetail(1); // 添加新的
        }
        envLearnMapper.learnDetailUpdate(req);
        envLearnMapper.updateLearnUserCompleted(req.getLearnUserId());
        JSONObject result = new JSONObject();
        result.put("learnDetailId", req.getLearnDetailId());
        result.put("learnUserId", req.getLearnUserId());
        return AjaxResult.success(result);
    }

    @Override
    public int checkExistsLearnByPolicyId(String policyId) {
        return envLearnMapper.checkExistsLearnByPolicyId(policyId);
    }
}
