package com.zkhf.epmis.process.facade.platform.fallback;

import cn.hutool.json.JSONObject;
import com.zkhf.epmis.core.domain.AnnexInfo;
import com.zkhf.epmis.core.domain.AnnexReq;
import com.zkhf.epmis.core.domain.PollHead;
import com.zkhf.epmis.core.domain.ValidPeriodAlarmInfo;
import com.zkhf.epmis.process.base.domain.*;
import com.zkhf.epmis.process.base.entity.DictData;
import com.zkhf.epmis.process.envManual.domain.EnvManualCheckTask;
import com.zkhf.epmis.process.facade.platform.PlatformFacade;
import com.zkhf.epmis.process.plc.domain.PlcInfo;
import com.zkhf.epmis.process.statistics.dto.PollutantInfo;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

public class PlatformFacadeFallback implements PlatformFacade {

    @Override
    public void updateAnnex(JSONObject req) {
    }

    @Override
    public List<DictData> dictDataByType(String type) {
        return null;
    }

    @Override
    public List<EntInfo> selectAllEntList() {
        return null;
    }

    @Override
    public List<OutPutInfo> selectAllOutPutList() {
        return null;
    }

    @Override
    public List<OutPutPollInfo> selectAllOutPutPollList() {
        return null;
    }

    @Override
    public List<UserAttentionInfo> selectUserAttentionList(Long userId) {
        return null;
    }

    @Override
    public List<Map<String, Object>> autoHeadChart(String outPutId) {
        return null;
    }

    @Override
    public List<Map<String, Object>> multipleAutoHead(List<String> outPutIds, String dataEnum) {
        return null;
    }

    @Override
    public List<ValidPeriodAlarmInfo> validPeriodConfList() {
        return null;
    }

    @Override
    public List<PollutantInfo> selectPollutantCodesByOutPutId(String outPutId) {
        return null;
    }

    @Override
    public List<Map<String, Object>> entOutPutListByType(String entCode, Integer outPutType) {
        return null;
    }

    @Override
    public List<Map<String, Object>> selectAllEntOutPollutantPermitCount(Integer permitYear) {
        return null;
    }

    @Override
    public List<PollutantCode> selectAllPollCodeList() {
        return null;
    }

    @Override
    public AnnexInfo uploadAnnex(MultipartFile file, String sourceType) {
        return null;
    }

    @Override
    public List<AnnexInfo> annexList(AnnexReq req) {
        return null;
    }

    @Override
    public List<EnvManualCheckTask> selectEnvManualCheckTaskForReport(List<String> taskIdList) {
        return null;
    }

    public void batchUpdateEnvManualCheckTask(List<EnvManualCheckTask> taskList) {
    }

    @Override
    public List<Map<String, Object>> getAutoHead(String outPutId, String dataEnum) {
        return null;
    }

    @Override
    public List<Map<String, Object>> outPutStatusList(List<String> entCodes) {
        return null;
    }

    @Override
    public List<OutPutAlarmConf> selectAllAlarmConf() {
        return null;
    }

    @Override
    public Map<String, List<PollHead>> multipleAutoHeads(List<String> outPutIds, String dataEnum) {
        return null;
    }

    @Override
    public List<PlcInfo> plcPointList(String entCode) {
        return null;
    }

    @Override
    public List<Map<String, Object>> allExtUnitList() {
        return null;
    }

    @Override
    public Map<String, Map<String, String>> selectDataMapByTypes(List<String> dictTypes) {
        return null;
    }
}
