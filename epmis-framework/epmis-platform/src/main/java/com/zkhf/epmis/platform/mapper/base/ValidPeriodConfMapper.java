package com.zkhf.epmis.platform.mapper.base;

import com.zkhf.epmis.platform.base.domain.ValidPeriodConf;
import com.zkhf.epmis.platform.base.domain.ValidPeriodInfo;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 企业资质有效期预警配置(四色预警机制，频率格式统一为两位数字)Mapper接口
 */
public interface ValidPeriodConfMapper {

    /**
     * 查询企业资质有效期预警配置(四色预警机制，频率格式统一为两位数字)列表
     */
    List<ValidPeriodConf> selectValidPeriodConfList(@Param("entCodes") List<String> entCodes);

    /**
     * 修改企业资质有效期预警配置(四色预警机制，频率格式统一为两位数字)
     */
    int insertOrUpdateValidPeriodConf(ValidPeriodConf info);

    /**
     * 查询所有配置
     */
    List<ValidPeriodInfo> allValidPeriodInfoList();

    /**
     * 获取排污许可资质的有效期
     */
    List<ValidPeriodInfo> selectOutPollutantPermitList();

    /**
     * 获取其他证书的有效期
     */
    List<ValidPeriodInfo> selectOtherCertificateList();

    /**
     * 获取手工监测待执行状态下的任务列表
     * 待执行状态下的任务
     */
    List<ValidPeriodInfo> selectManualCheckTaskList();

    /**
     * 获取应急物资的有效期
     */
    List<ValidPeriodInfo> selectEmergencyMaterialList();
}
