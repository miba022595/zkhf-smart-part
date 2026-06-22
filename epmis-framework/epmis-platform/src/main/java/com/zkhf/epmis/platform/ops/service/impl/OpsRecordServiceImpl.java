package com.zkhf.epmis.platform.ops.service.impl;

import com.alibaba.fastjson2.JSONArray;
import com.github.f4b6a3.ulid.UlidCreator;
import com.zkhf.epmis.core.annotation.Log;
import com.zkhf.epmis.core.domain.AjaxResult;
import com.zkhf.epmis.core.domain.AnnexInfo;
import com.zkhf.epmis.core.domain.AnnexReq;
import com.zkhf.epmis.core.enums.AnnexTypeEnum;
import com.zkhf.epmis.core.enums.BusinessType;
import com.zkhf.epmis.core.enums.GenApprovalType;
import com.zkhf.epmis.core.utils.PageUtils;
import com.zkhf.epmis.core.utils.StringUtils;
import com.zkhf.epmis.platform.annex.service.AnnexService;
import com.zkhf.epmis.platform.approval.domain.ApprovalHistory;
import com.zkhf.epmis.platform.approval.domain.ApprovalInstance;
import com.zkhf.epmis.platform.approval.enums.ApprovalFlowBusinessType;
import com.zkhf.epmis.platform.approval.service.ApprovalInstanceService;
import com.zkhf.epmis.platform.approval.service.ApprovalService;
import com.zkhf.epmis.platform.base.domain.Districts;
import com.zkhf.epmis.platform.base.service.DistrictsService;
import com.zkhf.epmis.platform.global.GVarContainer;
import com.zkhf.epmis.platform.mapper.ops.OpsRecordMapper;
import com.zkhf.epmis.platform.ops.domain.*;
import com.zkhf.epmis.platform.ops.service.OpsRecordService;
import com.zkhf.epmis.platform.ops.service.OpsTemplateService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 运维记录Service业务层处理
 */
@Service("opsRecordService")
public class OpsRecordServiceImpl implements OpsRecordService, ApprovalService {

    private OpsRecordMapper opsRecordMapper;
    @Autowired
    public void setOpsRecordMapper(OpsRecordMapper opsRecordMapper) {
        this.opsRecordMapper = opsRecordMapper;
    }

    private AnnexService annexService;
    @Autowired
    public void setAnnexService(AnnexService annexService) {
        this.annexService = annexService;
    }

    private OpsTemplateService opsTemplateService;
    @Autowired
    public void setOpsTemplateService(OpsTemplateService opsTemplateService) {
        this.opsTemplateService = opsTemplateService;
    }

    private ApprovalInstanceService approvalInstanceService;
    @Autowired
    public void setApprovalProcessService(ApprovalInstanceService approvalInstanceService) {
        this.approvalInstanceService = approvalInstanceService;
    }

    private DistrictsService districtsService;
    @Autowired
    public void setDistrictsService(DistrictsService districtsService) {
        this.districtsService = districtsService;
    }

    @Override
    public List<OpsRecord> getNewestRecordDateList(OpsTaskReq req) {
        return opsRecordMapper.selectNewestRecordDateList(req);
    }

    @Override
    public AjaxResult selectOpsRecordById(String recordId) {
        Map<String, Object> map = new HashMap<>();
        OpsRecord record = opsRecordMapper.selectOpsRecordById(recordId);
        if (null == record) {
            map.put("record", null);
            map.put("template", null);
            map.put("approvalList", new ArrayList<>());
            return AjaxResult.success(map);
        }
        if (StringUtils.isNotEmpty(record.getRecordArray())) {
            record.setRecordList(JSONArray.parseArray(record.getRecordArray(), OpsRecordItem.class));
        }
        // 获取附件信息
        record.setAnnexInfoList(annexService.selectAnnexList(recordId, AnnexTypeEnum.opsRecord.name));
        if (null != record.getRecordList() && !record.getRecordList().isEmpty()) {
            Map<String, OpsRecordItem> itemMap = new HashMap<>();
            record.getRecordList().forEach(e -> {
                e.setAnnexInfoList(new ArrayList<>());
                itemMap.put(e.getRecordItemId(), e);
            });
            AnnexReq req = new AnnexReq();
            req.setSourceType(AnnexTypeEnum.opsRecordItem.name);
            req.setSourceIds(new ArrayList<>(itemMap.keySet()));
            List<AnnexInfo> annexInfoList = annexService.selectAnnexList(req);
            if (null != annexInfoList && !annexInfoList.isEmpty()) {
                annexInfoList.forEach( e -> {
                    OpsRecordItem item = itemMap.get(e.getSourceId());
                    if (null != item) {
                        item.getAnnexInfoList().add(e);
                    }
                });
            }
        }
        // 转换地区信息
        if (StringUtils.isNotEmpty(record.getRegion())) {
            Map<Long, String> districtMap = getDistrictMap();
            fill(record, districtMap);
        }
        map.put("record", record);
        // 获取运维模板
        OpsTemplate template = opsTemplateService.selectOpsTemplateDetail(record.getEntCode(), record.getOutPutId(), record.getTemplateCode());
        map.put("template", template);
        if (null != template) {
            record.setTemplateName(template.getTemplateCode());
        }
        // 获取审批记录（非草稿状态下获取）
        String statusDesc = GenApprovalType.getNameByCode(record.getReviewStatus());
        List<ApprovalHistory> approvalList;
        if (StringUtils.isNotEmpty(statusDesc) && !GenApprovalType.DRAFT.code.equals(record.getReviewStatus())) {
            approvalList = approvalInstanceService.selectApprovalInstanceHistoryList(ApprovalFlowBusinessType.opsRecord.type, recordId);
        } else {
            approvalList = new ArrayList<>();
        }
        map.put("approvalList", approvalList);
        return AjaxResult.success(map);
    }

    @Override
    public AjaxResult selectOpsRecordList(OpsRecordReq req) {
        // 请求参数转换
        req = initOpsRecordReq(req);
        if (null == req) {
            return AjaxResult.success(new ArrayList<>());
        }
        // 运维配置列表查询
        PageUtils.startPage();
        List<OpsRecord> list = opsRecordMapper.selectOpsRecordList(req);
        // 填充信息
        fill(list);
        return PageUtils.getAjaxResult(list, true);
    }

    private void fill(List<OpsRecord> list) {
        if (null == list || list.isEmpty()) {
            return;
        }
        // 转换地区信息
        Map<Long, String> districtMap = getDistrictMap();
        // 获取模板类型列表
        Map<String, String> allTemplateType = opsTemplateService.allTemplateTypeReMap();
        list.forEach(e -> {
            fill(e, districtMap);
            e.setTemplateName(allTemplateType.get(e.getTemplateCode()));
        });
    }

    private void fill(OpsRecord single, Map<Long, String> districtMap) {
        // 转换地区信息
        single.setReviewStatusDesc(GenApprovalType.getNameByCode(single.getReviewStatus()));
        if (StringUtils.isNotEmpty(single.getRegion())) {
            StringBuilder bu = new StringBuilder();
            for (String s : single.getRegion().split(",")) {
                Long id = StringUtils.strToLong(s, null);
                if (districtMap.containsKey(id)) {
                    bu.append(districtMap.get(id));
                }
            }
            single.setRegionDesc(bu.toString());
        }
    }

    private Map<Long, String> getDistrictMap() {
        List<Districts> districts = districtsService.selectDistrictsList();
        Map<Long, String> districtMap = new HashMap<>();
        districts.forEach( e -> districtMap.put(e.getId(), e.getExtName()));
        return districtMap;
    }

    @Override
    @Log(title = "运维记录", businessType = BusinessType.INSERT)
    public AjaxResult insertOpsRecord(OpsRecord info) {
        if (StringUtils.isEmpty(info.getEntCode())) {
            return AjaxResult.error("企业编码不能为空");
        }
        if (StringUtils.isEmpty(info.getOutPutId())) {
            return AjaxResult.error("排放口不能为空");
        }
        if (StringUtils.isEmpty(info.getTemplateCode())) {
            return AjaxResult.error("未知的关联模板");
        }
        // 默认状态设置为草稿
        if (null == info.getReviewStatus()) {
            info.setReviewStatus(GenApprovalType.DRAFT.code);
        }
        if (!GenApprovalType.DRAFT.code.equals(info.getReviewStatus())
                && !GenApprovalType.SUBMITTED.code.equals(info.getReviewStatus())) {
            return AjaxResult.error("新增的状态异常");
        }
        info.setOpsUserId(GVarContainer.getUserId());
        info.setCreateBy(GVarContainer.getUserName());
        info.setRecordId(UlidCreator.getMonotonicUlid().toString());
        if (null == info.getRecordDate()) {
            info.setRecordDate(LocalDateTime.now());
        }
        // 设置配置项的id
        setRecordId(info, true);
        List<OpsRecordItem> recordList = info.getRecordList();
        if (null != recordList && !recordList.isEmpty()) {
            info.setRecordArray(JSONArray.toJSONString(recordList));
        }
        int count = opsRecordMapper.insertOpsRecord(info);
        if (count > 0) {
            updateAjaxResult(info, recordList);
        }
        return AjaxResult.success(count);
    }

    @Override
    @Log(title = "运维记录", businessType = BusinessType.UPDATE)
    public AjaxResult updateOpsRecord(OpsRecord info) {
        OpsRecord old = opsRecordMapper.selectOpsRecordById(info.getRecordId());
        if (null == old) {
            return AjaxResult.error("未知的记录");
        }
        // 提交后的修改只能修改为已取消
        if (GenApprovalType.SUBMITTED.code.equals(old.getReviewStatus())) {
            if (!GenApprovalType.CANCELLED.code.equals(info.getReviewStatus())) {
                return AjaxResult.error("提交后只能取消");
            }
        }
        if (GenApprovalType.REVIEWING.code.equals(old.getReviewStatus())
                || GenApprovalType.APPROVED.code.equals(old.getReviewStatus())
                || GenApprovalType.REJECTED.code.equals(old.getReviewStatus())) {
            return AjaxResult.error("不可编辑的状态");
        }
        // 默认状态设置为草稿
        if (null == info.getReviewStatus()) {
            info.setReviewStatus(GenApprovalType.DRAFT.code);
        }
        if (GenApprovalType.REVIEWING.code.equals(info.getReviewStatus())
                || GenApprovalType.APPROVED.code.equals(info.getReviewStatus())
                || GenApprovalType.REJECTED.code.equals(info.getReviewStatus())) {
            return AjaxResult.error("不可直接变更的状态");
        }
        info.setUpdateBy(GVarContainer.getUserName());
        // 设置配置项的id
        setRecordId(info, false);
        List<OpsRecordItem> recordList = info.getRecordList();
        if (null != recordList && !recordList.isEmpty()) {
            info.setRecordArray(JSONArray.toJSONString(recordList));
        }
        int count = opsRecordMapper.updateOpsRecord(info);
        if (count > 0) {
            updateAjaxResult(info, recordList);
        }
        return AjaxResult.success(count);
    }

    private void setRecordId(OpsRecord info, boolean init) {
        if (null == info.getRecordList() || info.getRecordList().isEmpty()) {
            return;
        }
        for (OpsRecordItem item : info.getRecordList()) {
            if (init || StringUtils.isEmpty(item.getRecordItemId())) {
                item.setRecordItemId(UlidCreator.getMonotonicUlid().toString());
            }
        }
    }

    private void updateAjaxResult(OpsRecord info, List<OpsRecordItem> recordList) {
        // 设置附件
        if (info.getAnnexIds() != null && !info.getAnnexIds().isEmpty()) {
            annexService.updateAnnex(info.getRecordId(), AnnexTypeEnum.opsRecord.name, info.getAnnexIds());
        }
        if (null != recordList && !recordList.isEmpty()) {
            recordList.forEach( e -> {
                if (e.getAnnexIds() != null && !e.getAnnexIds().isEmpty()) {
                    annexService.updateAnnex(e.getRecordItemId(), AnnexTypeEnum.opsRecordItem.name, e.getAnnexIds());
                }
            });
        }
    }

    @Override
    @Log(title = "运维记录", businessType = BusinessType.DELETE)
    public AjaxResult deleteOpsRecordById(String recordId) {
        OpsRecord old = opsRecordMapper.selectOpsRecordById(recordId);
        if (null == old) {
            return AjaxResult.error("未知的记录");
        }
        if (!GenApprovalType.DRAFT.code.equals(old.getReviewStatus()) &&
                !GenApprovalType.CANCELLED.code.equals(old.getReviewStatus())) {
            return AjaxResult.error("只能删除草稿或已取消的记录");
        }
        int count = opsRecordMapper.deleteOpsRecordById(recordId);
        if (count > 0) {
            // 设置附件
            annexService.updateAnnex(recordId, AnnexTypeEnum.opsRecord.name, null);
            if (StringUtils.isNotEmpty(old.getRecordArray())) {
                List<OpsRecordItem> recordList = JSONArray.parseArray(old.getRecordArray(), OpsRecordItem.class);
                if (null != recordList && !recordList.isEmpty()) {
                    recordList.forEach( e -> {
                        if (e.getAnnexIds() != null && !e.getAnnexIds().isEmpty()) {
                            annexService.updateAnnex(e.getRecordItemId(), AnnexTypeEnum.opsRecordItem.name, null);
                        }
                    });
                }
            }
        }
        return AjaxResult.success(count);
    }

    @Override
    public AjaxResult selectOpsTaskStat(OpsRecordReq req) {
        // 请求参数转换
        req = initOpsRecordReq(req);
        if (null == req) {
            return AjaxResult.success(new ArrayList<>());
        }
        List<OpsStat> list = opsRecordMapper.selectOpsTaskStat(req);
        // 获取模板类型列表
        Map<String, String> allTemplateType = opsTemplateService.allTemplateTypeReMap();
        list.forEach(e -> e.setTemplateName(allTemplateType.get(e.getTemplateCode())));
        return AjaxResult.success(list);
    }

    private OpsRecordReq initOpsRecordReq(OpsRecordReq req) {
        if (null == req) {
            req = new OpsRecordReq();
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

    @Async
    @Override
    public void approval(ApprovalInstance instance) {
        if (instance == null) {
            return;
        }
        Integer newStatus = statusTransToGenApprovalType(instance.getStatus());
        if (null == newStatus) {
            return;
        }
        OpsRecord record = new OpsRecord();
        record.setRecordId(instance.getBusinessKey());
        record.setReviewStatus(newStatus);
        opsRecordMapper.updateOpsRecord(record);
    }
}
