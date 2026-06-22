package com.zkhf.epmis.process.facade.platform;

import cn.hutool.json.JSONObject;
import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.TypeReference;
import com.zkhf.epmis.core.domain.AnnexInfo;
import com.zkhf.epmis.core.domain.AnnexReq;
import com.zkhf.epmis.core.domain.PollHead;
import com.zkhf.epmis.core.domain.ValidPeriodAlarmInfo;
import com.zkhf.epmis.platform.feign.FeignController;
import com.zkhf.epmis.process.base.domain.*;
import com.zkhf.epmis.process.base.entity.DictData;
import com.zkhf.epmis.process.envManual.domain.EnvManualCheckTask;
import com.zkhf.epmis.process.plc.domain.PlcInfo;
import com.zkhf.epmis.process.statistics.dto.PollutantInfo;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

@Component
public class PlatformFacade {

    @Resource
    private FeignController feignController;

    public void updateAnnex(JSONObject req) {
        feignController.updateAnnex(JSON.parseObject(req.toString()));
    }

    public List<DictData> dictDataByType(String type) {
        return convertList(feignController.dictType(type), DictData.class);
    }

    public List<EntInfo> selectAllEntList() {
        return convertList(feignController.selectAllEntList(), EntInfo.class);
    }

    public List<OutPutInfo> selectAllOutPutList() {
        return convertList(feignController.selectAllOutPutList(), OutPutInfo.class);
    }

    public List<OutPutPollInfo> selectAllOutPutPollList() {
        return convertList(feignController.selectAllOutPutPollList(), OutPutPollInfo.class);
    }

    public List<UserAttentionInfo> selectUserAttentionList(Long userId) {
        return convertList(feignController.selectUserAttentionList(userId), UserAttentionInfo.class);
    }

    public List<Map<String, Object>> autoHeadChart(String outPutId) {
        return convertToMapList(feignController.autoHeadChart(outPutId));
    }

    public List<Map<String, Object>> multipleAutoHead(List<String> outPutIds, String dataEnum) {
        return convertToMapList(feignController.multipleAutoHead(outPutIds, dataEnum));
    }

    public List<ValidPeriodAlarmInfo> validPeriodConfList() {
        return feignController.validPeriodConfList();
    }

    public List<PollutantInfo> selectPollutantCodesByOutPutId(String outPutId) {
        return convertList(feignController.selectPollutantCodesByOutPutId(outPutId), PollutantInfo.class);
    }

    public List<Map<String, Object>> entOutPutListByType(String entCode, Integer outPutType) {
        return convertToMapList(feignController.entOutPutListByType(entCode, outPutType));
    }

    public List<Map<String, Object>> selectAllEntOutPollutantPermitCount(Integer permitYear) {
        return convertToMapList(feignController.selectAllEntOutPollutantPermitCount(permitYear));
    }

    public List<PollutantCode> selectAllPollCodeList() {
        return convertList(feignController.selectAllPollCodeList(), PollutantCode.class);
    }

    public AnnexInfo uploadAnnex(MultipartFile file, String sourceType) {
        return feignController.uploadAnnex(file, sourceType);
    }

    public List<AnnexInfo> annexList(AnnexReq req) {
        return feignController.annexList(req);
    }

    public List<EnvManualCheckTask> selectEnvManualCheckTaskForReport(List<String> taskIdList) {
        return convertList(feignController.selectEnvManualCheckTaskForReport(taskIdList), EnvManualCheckTask.class);
    }

    public void batchUpdateEnvManualCheckTask(List<EnvManualCheckTask> taskList) {
        feignController.batchUpdateEnvManualCheckTask(convertList(taskList, com.zkhf.epmis.platform.envManual.domain.EnvManualCheckTask.class));
    }

    public List<Map<String, Object>> getAutoHead(String outPutId, String dataEnum) {
        return convertToMapList(feignController.getAutoHead(outPutId, dataEnum));
    }

    public List<Map<String, Object>> outPutStatusList(List<String> entCodes) {
        return convertToMapList(feignController.outPutStatusList(entCodes));
    }

    public List<OutPutAlarmConf> selectAllAlarmConf() {
        return convertList(feignController.selectAllAlarmConf(), OutPutAlarmConf.class);
    }

    public Map<String, List<PollHead>> multipleAutoHeads(List<String> outPutIds, String dataEnum) {
        return feignController.multipleAutoHeads(outPutIds, dataEnum);
    }

    public List<PlcInfo> plcPointList(String entCode) {
        return convertList(feignController.plcPointList(entCode), PlcInfo.class);
    }

    public List<Map<String, Object>> allExtUnitList() {
        return convertToMapList(feignController.allList());
    }

    public Map<String, Map<String, String>> selectDataMapByTypes(List<String> dictTypes) {
        return feignController.getDataMapByTypes(dictTypes);
    }

    private <T> List<T> convertList(Object source, Class<T> targetClass) {
        if (source == null) {
            return null;
        }
        return JSON.parseArray(JSON.toJSONString(source), targetClass);
    }

    private List<Map<String, Object>> convertToMapList(Object source) {
        if (source == null) {
            return null;
        }
        return JSON.parseObject(JSON.toJSONString(source), new TypeReference<List<Map<String, Object>>>() {
        });
    }
}
