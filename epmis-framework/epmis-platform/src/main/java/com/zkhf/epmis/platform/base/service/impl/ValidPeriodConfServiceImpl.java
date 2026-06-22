package com.zkhf.epmis.platform.base.service.impl;

import com.zkhf.epmis.core.annotation.Log;
import com.zkhf.epmis.core.domain.AjaxResult;
import com.zkhf.epmis.core.domain.ValidPeriodAlarmInfo;
import com.zkhf.epmis.core.enums.AlarmTypeEnum;
import com.zkhf.epmis.core.enums.BusinessType;
import com.zkhf.epmis.core.enums.ValidPeriodTypeEnum;
import com.zkhf.epmis.core.utils.StringUtils;
import com.zkhf.epmis.platform.base.domain.ValidPeriodConf;
import com.zkhf.epmis.platform.base.domain.ValidPeriodInfo;
import com.zkhf.epmis.platform.base.service.ValidPeriodConfService;
import com.zkhf.epmis.platform.ent.domain.EnterprisePart;
import com.zkhf.epmis.platform.ent.service.EnterpriseService;
import com.zkhf.epmis.platform.envManual.enums.CheckFrequencyType;
import com.zkhf.epmis.platform.global.GVarContainer;
import com.zkhf.epmis.platform.mapper.base.ValidPeriodConfMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.*;

/**
 * 企业资质有效期预警配置(四色预警机制，频率格式统一为两位数字)Service业务层处理
 */
@Slf4j
@Service
public class ValidPeriodConfServiceImpl implements ValidPeriodConfService {

    private static final List<String> DEFAULT_ENT_CONF = Collections.singletonList("-1");

    private ValidPeriodConfMapper validPeriodConfMapper;
    @Autowired
    public void setValidPeriodConfMapper(ValidPeriodConfMapper validPeriodConfMapper) {
        this.validPeriodConfMapper = validPeriodConfMapper;
    }

    private EnterpriseService enterpriseService;
    @Autowired
    public void setEnterpriseService(EnterpriseService enterpriseService) {
        this.enterpriseService = enterpriseService;
    }

    @Override
    public AjaxResult selectValidPeriodConfList(String entCode) {
        // 企业列表
        List<EnterprisePart> entList = enterpriseService.selectPartListAllByInternal(entCode);
        if (null == entList || entList.isEmpty()) {
            return AjaxResult.success();
        }
        // 已有的配置
        List<String> entCodes = null;
        if (StringUtils.isEmpty(entCode)) {
            if (GVarContainer.isNotAdmin()) {
                entCodes = GVarContainer.getEntCodes();
            }
        } else {
            entCodes = Collections.singletonList(entCode);
        }
        List<ValidPeriodConf> oldList = validPeriodConfMapper.selectValidPeriodConfList(entCodes);
        Map<String, ValidPeriodConf> oldMap = new HashMap<>();
        oldList.forEach( e -> oldMap.put(e.getEntCode() + "_" + e.getConfType(), e));
        // 公共的配置
        List<ValidPeriodConf> normal = validPeriodConfMapper.selectValidPeriodConfList(DEFAULT_ENT_CONF);
        // 所有的配置
        List<ValidPeriodConf> all = new ArrayList<>();
        entList.forEach( e -> normal.forEach(n -> {
            String key = e.getEntCode() + "_" + n.getConfType();
            if (oldMap.containsKey(key)) {
                all.add(oldMap.get(key));
            } else {
                ValidPeriodConf c = new ValidPeriodConf();
                c.setEntCode(e.getEntCode());
                c.setEntName(e.getEntName());
                c.setConfType(n.getConfType());
                c.setYellowThreshold(n.getYellowThreshold());
                c.setYellowNotifyFreq(n.getYellowNotifyFreq());
                c.setOrangeThreshold(n.getOrangeThreshold());
                c.setOrangeNotifyFreq(n.getOrangeNotifyFreq());
                c.setRedThreshold(n.getRedThreshold());
                c.setRedNotifyFreq(n.getRedNotifyFreq());
                c.setUpdateTime(n.getUpdateTime());
                all.add(c);
            }
        }));
        // 设置资质证件类型描述
        all.forEach( e -> e.setConfDesc(ValidPeriodTypeEnum.getDescByCode(e.getConfType())));
        // 先按 ent_code 降序，再按 conf_type 升序
        all.sort(Comparator
                .comparing(ValidPeriodConf::getEntCode).reversed()
                .thenComparing(ValidPeriodConf::getConfType)
        );
        return AjaxResult.success(all);
    }

    @Override
    @Log(title = "企业资质有效期预警配置(四色预警机制，频率格式统一为两位数字)", businessType = BusinessType.UPDATE)
    public AjaxResult updateValidPeriodConf(ValidPeriodConf info) {
        return AjaxResult.success(validPeriodConfMapper.insertOrUpdateValidPeriodConf(info));
    }

    @Override
    public List<ValidPeriodAlarmInfo> feignConfList() {
        LocalDate nowDate = LocalDate.now();
        // 获取配置信息
        List<ValidPeriodInfo> confList = validPeriodConfMapper.allValidPeriodInfoList();
        Map<String, ValidPeriodInfo> confMap = new HashMap<>();
        confList.forEach( e -> confMap.put(e.getEntCode() + "_" + e.getConfType(), e));
        // 结果数据列表
        List<ValidPeriodAlarmInfo> dataList = new ArrayList<>();
        // 查询排序许可资质有效期信息
        List<ValidPeriodInfo> outList = validPeriodConfMapper.selectOutPollutantPermitList();
        // 排污许可处理有效期
        handleValidPeriod(confMap, outList, dataList, nowDate, ValidPeriodTypeEnum.P_W_X_K);
        // 查询其他证书列表
        List<ValidPeriodInfo> otherList = validPeriodConfMapper.selectOtherCertificateList();
        // 其他证书处理有效期
        handleValidPeriod(confMap, otherList, dataList, nowDate, ValidPeriodTypeEnum.Q_T_Z_S);
        // 查询获取手工监测待执行状态下的任务列表
        List<ValidPeriodInfo> taskList = validPeriodConfMapper.selectManualCheckTaskList();
        taskList.forEach( e -> e.setItemName(e.getOutPutName() + " - " + e.getPollutantNameCn() + " - " + CheckFrequencyType.getNameByCode(e.getCheckFrequency())));
        // 获取手工监测待执行状态下的任务列表处理执行期限
        handleValidPeriod(confMap, taskList, dataList, nowDate, ValidPeriodTypeEnum.J_C_R_W);
        // 查询应急物资列表
        List<ValidPeriodInfo> materialList = validPeriodConfMapper.selectEmergencyMaterialList();
        // 应急物资处理有效期
        handleValidPeriod(confMap, materialList, dataList, nowDate, ValidPeriodTypeEnum.Y_J_W_Z);
        return dataList;
    }

    private void handleValidPeriod(Map<String, ValidPeriodInfo> confMap, List<ValidPeriodInfo> list,
                                   List<ValidPeriodAlarmInfo> dataList, LocalDate nowDate, ValidPeriodTypeEnum typeEnum) {
        if (null == list || list.isEmpty()) {
            return;
        }
        for (ValidPeriodInfo e : list) {
            if (null == e.getEndDate()) {
                if (null == e.getItemName()) {
                    log.error("企业编码 {} 下 {} 无有效期结束时间，不处理", e.getEntCode(), typeEnum.desc);
                    continue;
                } else {
                    log.error("企业编码 {} 下 {} {} 无有效期结束时间，不处理", e.getEntCode(), typeEnum.desc, e.getItemName());
                }
                continue;
            }
            String key = e.getEntCode() + "_" + typeEnum.code;
            ValidPeriodInfo conf = confMap.get(key);
            if (null == conf) {
                log.error("企业编码 {} 下未配置 {} 的有效期预警配置", e.getEntCode(), typeEnum.desc);
                continue;
            }
            ValidPeriodAlarmInfo info = ValidPeriodAlarmInfo.builder()
                    .entCode(e.getEntCode())
                    .entName(conf.getEntName())
                    .weComMsg(conf.getWeComMsg())
                    .confType(typeEnum.code)
                    .confDesc(typeEnum.desc)
                    .itemId(e.getItemId())
                    .itemName(e.getItemName())
                    .beginDate(e.getBeginDate())
                    .endDate(e.getEndDate())
                    .build();
            long days = ChronoUnit.DAYS.between(nowDate, e.getEndDate());
            info.setLeftDays(days);
            if (days <= 0) { // 已过期
                info.setAlarmType(AlarmTypeEnum.ALARM_RED.code);
                info.setAlarmRage(conf.getRedNotifyFreq());
            } else if (days <= conf.getRedThreshold()) { // 达到红色报警阈值
                info.setAlarmType(AlarmTypeEnum.ALARM_RED.code);
                info.setAlarmRage(conf.getRedNotifyFreq());
            } else if (days <= conf.getOrangeThreshold()) { // 达到橙色预警阈值
                info.setAlarmType(AlarmTypeEnum.ALARM_ORANGE.code);
                info.setAlarmRage(conf.getOrangeNotifyFreq());
            } else if (days <= conf.getYellowThreshold()) { // 达到黄色提醒阈值
                info.setAlarmType(AlarmTypeEnum.ALARM_YELLOW.code);
                info.setAlarmRage(conf.getYellowNotifyFreq());
            } else { // 未触发
                info.setAlarmType(AlarmTypeEnum.ALARM_GREEN.code);
            }
            dataList.add(info);
        }
    }
}
