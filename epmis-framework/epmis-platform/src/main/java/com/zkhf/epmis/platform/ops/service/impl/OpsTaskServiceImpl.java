package com.zkhf.epmis.platform.ops.service.impl;

import com.github.f4b6a3.ulid.UlidCreator;
import com.zkhf.epmis.core.annotation.Log;
import com.zkhf.epmis.core.domain.AjaxResult;
import com.zkhf.epmis.core.enums.BusinessType;
import com.zkhf.epmis.core.utils.PageUtils;
import com.zkhf.epmis.core.utils.StringUtils;
import com.zkhf.epmis.platform.global.GVarContainer;
import com.zkhf.epmis.platform.mapper.ops.OpsTaskMapper;
import com.zkhf.epmis.platform.ops.domain.*;
import com.zkhf.epmis.platform.ops.enums.OpsTaskType;
import com.zkhf.epmis.platform.ops.service.OpsRecordService;
import com.zkhf.epmis.platform.ops.service.OpsTaskService;
import com.zkhf.epmis.platform.ops.service.OpsTemplateService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 运维任务Service业务层处理
 */
@Service("opsTaskService")
public class OpsTaskServiceImpl implements OpsTaskService {

    private OpsTaskMapper opsTaskMapper;
    @Autowired
    public void setOpsTaskMapper(OpsTaskMapper opsTaskMapper) {
        this.opsTaskMapper = opsTaskMapper;
    }

    private OpsTemplateService opsTemplateService;
    @Autowired
    public void setOpsTemplateService(OpsTemplateService opsTemplateService) {
        this.opsTemplateService = opsTemplateService;
    }

    private OpsRecordService opsRecordService;
    @Autowired
    public void setOpsRecordService(OpsRecordService opsRecordService) {
        this.opsRecordService = opsRecordService;
    }

    @Override
    public AjaxResult selectOpsTaskConfDetail(String entCode, String outPutId, String templateCode) {
        Map<String, Object> map = new HashMap<>();
        OpsTaskConf conf = opsTaskMapper.selectOpsTaskConfDetail(entCode, outPutId, templateCode);
        if (null == conf) {
            map.put("taskConf", null);
            map.put("template", null);
            return AjaxResult.success(map);
        }
        map.put("taskConf", conf);
        // 获取运维模板
        String templateName = setTemplateAndGetName(conf.getEntCode(), conf.getOutPutId(), conf.getTemplateCode(), map);
        conf.setTemplateName(templateName);
        return AjaxResult.success(map);
    }

    @Override
    public AjaxResult selectOpsTaskConfList(OpsTaskReq req) {
        // 请求参数转换
        req = initOpsTaskReq(req);
        if (null == req) {
            return AjaxResult.success(new ArrayList<>());
        }
        // 运维配置列表查询
        PageUtils.startPage();
        List<OpsTaskConf> list = opsTaskMapper.selectOpsTaskConfList(req);
        // 填充信息
        if (null != list && !list.isEmpty()) {
            // 获取模板类型列表
            Map<String, String> allTemplateType = opsTemplateService.allTemplateTypeReMap();
            list.forEach(e -> e.setTemplateName(allTemplateType.get(e.getTemplateCode())));
        }
        return PageUtils.getAjaxResult(list, true);
    }

    @Override
    @Log(title = "运维任务配置", businessType = BusinessType.INSERT)
    public AjaxResult insertOpsTaskConf(OpsTaskConf info) {
        if (StringUtils.isEmpty(info.getEntCode()) ||
                StringUtils.isEmpty(info.getOutPutId()) ||
                StringUtils.isEmpty(info.getTemplateCode())) {
            return AjaxResult.error("配置参数不能为空");
        }
        int count = opsTaskMapper.checkExistsTaskConf(info);
        if (count > 0) {
            return AjaxResult.error("运维任务已存在");
        }
        info.setCreateId(GVarContainer.getUserId());
        LocalDate nextWeek = LocalDate.now().plusWeeks(1);
        if (null != info.getBeginDate() && info.getBeginDate().isBefore(nextWeek)) {
            info.setBeginDate(nextWeek);
        }
        if (!taskEnable.equals(info.getEnabled())) {
            info.setEnabled(taskUnable);
        }
        count = opsTaskMapper.insertOpsTaskConf(info);
        return AjaxResult.success(count);
    }

    @Override
    @Log(title = "运维任务配置", businessType = BusinessType.UPDATE)
    public AjaxResult updateOpsTaskConf(OpsTaskConf info) {
        OpsTaskConf old = opsTaskMapper.selectOpsTaskConfDetail(info.getEntCode(), info.getOutPutId(), info.getTemplateCode());
        if (null == old) {
            return AjaxResult.error("运维任务不存在");
        }
        Long loginId = GVarContainer.getUserId();
        if (null != old.getCreateId() && !old.getCreateId().equals(loginId)) {
            return AjaxResult.error("只能修改自己创建的配置");
        }
        LocalDate nextWeek = LocalDate.now().plusWeeks(1);
        // 之前未设置时间，或者之前的开始时间在一周后时可修改开始时间
        if (null == old.getBeginDate() || old.getBeginDate().isAfter(nextWeek)) {
            if (null != info.getBeginDate() && info.getBeginDate().isBefore(nextWeek)) {
                info.setBeginDate(nextWeek);
            }
        } else {
            info.setBeginDate(old.getBeginDate());
        }
        int count = opsTaskMapper.updateOpsTaskConf(info);
        return AjaxResult.success(count);
    }

    @Override
    @Log(title = "运维任务配置", businessType = BusinessType.DELETE)
    public AjaxResult deleteOpsTaskConf(String entCode, String outPutId, String templateCode) {
        if (StringUtils.isEmpty(entCode) || StringUtils.isEmpty(outPutId) || StringUtils.isEmpty(templateCode)) {
            return AjaxResult.success(0);
        }
        int count = opsTaskMapper.deleteOpsTaskConf(entCode, outPutId, templateCode);
        return AjaxResult.success(count);
    }

    @Override
    public AjaxResult selectOpsTaskByTaskId(String taskId) {
        Map<String, Object> map = new HashMap<>();
        OpsTask conf = opsTaskMapper.selectOpsTaskByTaskId(taskId);
        if (null == conf) {
            map.put("task", null);
            map.put("template", null);
            return AjaxResult.success(map);
        }
        map.put("taskConf", conf);
        // 获取运维模板
        String templateName = setTemplateAndGetName(conf.getEntCode(), conf.getOutPutId(), conf.getTemplateCode(), map);
        conf.setTemplateName(templateName);
        return AjaxResult.success(map);
    }

    private String setTemplateAndGetName(String entCode, String outPutId, String templateCode, Map<String, Object> map) {
        // 获取运维模板
        OpsTemplate template = opsTemplateService.selectOpsTemplateDetail(entCode, outPutId, templateCode);
        map.put("template", template);
        if (null != template) {
            return template.getTemplateName();
        }
        return null;
    }

    @Override
    public AjaxResult selectPersonOpsTaskList(OpsTaskReq req) {
        // 请求参数转换
        req = initOpsTaskReq(req);
        if (null == req) {
            return AjaxResult.success(new ArrayList<>());
        }
        req.setOperator(GVarContainer.getUserId());
        // 运维配置列表查询
        PageUtils.startPage();
        List<OpsTask> list = opsTaskMapper.selectPersonOpsTaskList(req);
        // 填充信息
        fillData(list, req);
        return PageUtils.getAjaxResult(list, true);
    }

    @Override
    public AjaxResult selectOpsTaskList(OpsTaskReq req) {
        // 请求参数转换
        req = initOpsTaskReq(req);
        if (null == req) {
            return AjaxResult.success(new ArrayList<>());
        }
        // 运维配置列表查询
        PageUtils.startPage();
        List<OpsTask> list = opsTaskMapper.selectOpsTaskList(req);
        // 填充信息
        fillData(list, req);
        return PageUtils.getAjaxResult(list, true);
    }

    private void fillData(List<OpsTask> list, OpsTaskReq req) {
        if (null == list || list.isEmpty()) {
            return;
        }
        // 获取模板类型列表
        Map<String, String> allTemplateType = opsTemplateService.allTemplateTypeReMap();
        // 获取最新运维记录时间
        List<OpsRecord> newestList = opsRecordService.getNewestRecordDateList(req);
        Map<String, Map<String, Map<String, LocalDateTime>>> newMap = new HashMap<>();
        newestList.forEach( e -> newMap.computeIfAbsent(e.getEntCode(), k -> new HashMap<>()).
                computeIfAbsent(e.getOutPutId(), k -> new HashMap<>()).
                put(e.getTemplateCode(), e.getRecordDate()));
        list.forEach(e -> {
            e.setTemplateName(allTemplateType.get(e.getTemplateCode()));
            Map<String, Map<String, LocalDateTime>> sub1 = newMap.get(e.getEntCode());
            if (null != sub1) {
                Map<String, LocalDateTime> sub2 = sub1.get(e.getOutPutId());
                if (null != sub2) {
                    e.setRecordDate(sub2.get(e.getTemplateCode()));
                }
            }
        });
    }

    private OpsTaskReq initOpsTaskReq(OpsTaskReq req) {
        // 请求参数转换
        if (null == req) {
            req = new OpsTaskReq();
        }
        // 添加权限
        if (GVarContainer.isNotAdmin()) {
            req.setEntCodes(GVarContainer.getEntCodes());
            if (null == req.getEntCodes() || req.getEntCodes().isEmpty()) {
                return null;
            }
        }
        return req;
    }

    @Override
    @Log(title = "运维任务", businessType = BusinessType.INSERT)
    public AjaxResult insertOpsTask(OpsTask info) {
        if (StringUtils.isEmpty(info.getEntCode()) ||
                StringUtils.isEmpty(info.getOutPutId()) ||
                StringUtils.isEmpty(info.getTemplateCode())) {
            return AjaxResult.error("配置参数不能为空");
        }
        info.setTaskType(taskManual);
        // 默认状态设置为草稿
        if (null == info.getTaskStatus()) {
            info.setTaskStatus(OpsTaskType.DRAFT.code);
        }
        if (!OpsTaskType.DRAFT.code.equals(info.getTaskStatus())
                && !OpsTaskType.ISSUED.code.equals(info.getTaskStatus())) {
            return AjaxResult.error("新增的状态异常");
        }
        // 下发时，需要分配执行人
        if (OpsTaskType.ISSUED.code.equals(info.getTaskStatus())) {
            if (null == info.getOperator()) {
                return AjaxResult.error("提交时，需要分配执行人");
            }
        }
        // 有分配人时，设置分配时间
        if (null != info.getOperator()) {
            info.setAssignTime(LocalDateTime.now());
        }
        info.setTaskId(UlidCreator.getMonotonicUlid().toString());
        info.setCreateId(GVarContainer.getUserId());
        if (null == info.getPlanDate()) {
            info.setPlanDate(LocalDate.now());
        }
        int count = opsTaskMapper.insertOpsTask(info);
        return AjaxResult.success(count);
    }

    @Override
    @Log(title = "运维任务", businessType = BusinessType.UPDATE)
    public AjaxResult updateOpsTask(OpsTask info) {
        OpsTask conf = opsTaskMapper.selectOpsTaskByTaskId(info.getTaskId());
        if (null == conf) {
            return AjaxResult.error("未知的任务");
        }
        Long loginId = GVarContainer.getUserId();
        if (OpsTaskType.DRAFT.code.equals(conf.getTaskStatus()) || OpsTaskType.CANCELLED.code.equals(conf.getTaskStatus())) {
            // 草稿、已取消变更为已下发状态，只有创建人才能变更为已下发
            if (OpsTaskType.ISSUED.code.equals(info.getTaskStatus())) {
                if (null != loginId && !loginId.equals(conf.getCreateId())) {
                    return AjaxResult.error("无权限修改");
                }
            } else {
                return AjaxResult.error("不可变更的状态");
            }
        }
        if (OpsTaskType.ISSUED.code.equals(conf.getTaskStatus())) {
            if (OpsTaskType.CANCELLED.code.equals(info.getTaskStatus())) {
                // 下发状态变更为已取消状态，只有创建人才能变更为已取消
                if (null != loginId && !loginId.equals(conf.getCreateId())) {
                    return AjaxResult.error("无权限修改");
                }
            } else if (OpsTaskType.RECEIVED.code.equals(info.getTaskStatus())) {
                // 下发状态变更为已接收状态，只有执行人才能变更为已接收
                if (null != loginId && !loginId.equals(conf.getOperator())) {
                    return AjaxResult.error("无权限修改");
                }
            } else {
                return AjaxResult.error("不可变更的状态");
            }
        }
        // 已接收过了，不能修改了
        if (OpsTaskType.RECEIVED.code.equals(conf.getTaskStatus())) {
            return AjaxResult.error("不可编辑的状态");
        }
        // 默认状态设置为草稿
        if (null == info.getTaskStatus()) {
            info.setTaskStatus(OpsTaskType.DRAFT.code);
        }
        // 提交时，需要分配执行人
        if (OpsTaskType.ISSUED.code.equals(info.getTaskStatus())) {
            if (null == info.getOperator()) {
                return AjaxResult.error("提交时，需要分配执行人");
            }
        }
        // 有分配人时，设置分配时间
        if (null != info.getOperator()) {
            if (null == info.getAssignTime()) {
                info.setAssignTime(LocalDateTime.now());
            }
        }
        int count = opsTaskMapper.updateOpsTask(info);
        return AjaxResult.success(count);
    }

    @Override
    @Log(title = "运维任务", businessType = BusinessType.DELETE)
    public AjaxResult deleteOpsTaskByTaskId(String taskId) {
        OpsTask conf = opsTaskMapper.selectOpsTaskByTaskId(taskId);
        if (null == conf) {
            return AjaxResult.error("未知的任务");
        }
        if (!OpsTaskType.DRAFT.code.equals(conf.getTaskStatus()) &&
                !OpsTaskType.CANCELLED.code.equals(conf.getTaskStatus())) {
            return AjaxResult.error("只能删除草稿或已取消的任务");
        }
        int count = opsTaskMapper.deleteOpsTaskById(taskId);
        return AjaxResult.success(count);
    }
}
