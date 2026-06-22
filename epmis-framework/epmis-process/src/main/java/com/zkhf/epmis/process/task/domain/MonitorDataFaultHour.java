package com.zkhf.epmis.process.task.domain;

import java.io.Serializable;
import java.util.Date;

/**
 * 小时数据缺失表---废水、废气
 */
public class MonitorDataFaultHour implements Serializable {
    /**
     * 主键id-自增
     */
    private Long alarmId;

    /**
     * 企业编码
     */
    private String entCode;

    /**
     * 企业名称
     */
    private String entName;

    /**
     * 排口编码
     */
    private String outPutCode;

    /**
     * 排口名称
     */
    private String outPutName;

    /**
     * 污染物编码：当报警类型为2时此字段有值
     */
    private String pollutantCode;

    /**
     * 污染物名称：当报警类型为2时此字段有值
     */
    private String pollutantName;

    /**
     * 数据类型；1：废气；2：废水
     */
    private String dataType;

    /**
     * 报警时间
     */
    private Date alarmTime;

    /**
     * 数据缺失时间
     */
    private Date faultTime;

    /**
     * 报警类型：1：小时数据整体缺失；2：小时数据单个污染因子缺失
     */
    private String alarmType;

    private static final long serialVersionUID = 1L;

    /**
     * 主键id-自增
     */
    public Long getAlarmId() {
        return alarmId;
    }

    /**
     * 主键id-自增
     */
    public void setAlarmId(Long alarmId) {
        this.alarmId = alarmId;
    }

    /**
     * 企业编码
     */
    public String getEntCode() {
        return entCode;
    }

    /**
     * 企业编码
     */
    public void setEntCode(String entCode) {
        this.entCode = entCode == null ? null : entCode.trim();
    }

    /**
     * 企业名称
     */
    public String getEntName() {
        return entName;
    }

    /**
     * 企业名称
     */
    public void setEntName(String entName) {
        this.entName = entName == null ? null : entName.trim();
    }

    /**
     * 排口编码
     */
    public String getOutPutCode() {
        return outPutCode;
    }

    /**
     * 排口编码
     */
    public void setOutPutCode(String outPutCode) {
        this.outPutCode = outPutCode == null ? null : outPutCode.trim();
    }

    /**
     * 排口名称
     */
    public String getOutPutName() {
        return outPutName;
    }

    /**
     * 排口名称
     */
    public void setOutPutName(String outPutName) {
        this.outPutName = outPutName == null ? null : outPutName.trim();
    }

    /**
     * 污染物编码：当报警类型为2时此字段有值
     */
    public String getPollutantCode() {
        return pollutantCode;
    }

    /**
     * 污染物编码：当报警类型为2时此字段有值
     */
    public void setPollutantCode(String pollutantCode) {
        this.pollutantCode = pollutantCode == null ? null : pollutantCode.trim();
    }

    /**
     * 污染物名称：当报警类型为2时此字段有值
     */
    public String getPollutantName() {
        return pollutantName;
    }

    /**
     * 污染物名称：当报警类型为2时此字段有值
     */
    public void setPollutantName(String pollutantName) {
        this.pollutantName = pollutantName == null ? null : pollutantName.trim();
    }

    /**
     * 数据类型；1：废气；2：废水
     */
    public String getDataType() {
        return dataType;
    }

    /**
     * 数据类型；1：废气；2：废水
     */
    public void setDataType(String dataType) {
        this.dataType = dataType == null ? null : dataType.trim();
    }

    /**
     * 报警时间
     */
    public Date getAlarmTime() {
        return alarmTime;
    }

    /**
     * 报警时间
     */
    public void setAlarmTime(Date alarmTime) {
        this.alarmTime = alarmTime;
    }

    /**
     * 数据缺失时间
     */
    public Date getFaultTime() {
        return faultTime;
    }

    /**
     * 数据缺失时间
     */
    public void setFaultTime(Date faultTime) {
        this.faultTime = faultTime;
    }

    /**
     * 报警类型：1：小时数据整体缺失；2：小时数据单个污染因子缺失
     */
    public String getAlarmType() {
        return alarmType;
    }

    /**
     * 报警类型：1：小时数据整体缺失；2：小时数据单个污染因子缺失
     */
    public void setAlarmType(String alarmType) {
        this.alarmType = alarmType == null ? null : alarmType.trim();
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(getClass().getSimpleName());
        sb.append(" [");
        sb.append("Hash = ").append(hashCode());
        sb.append(", alarmId=").append(alarmId);
        sb.append(", entCode=").append(entCode);
        sb.append(", entName=").append(entName);
        sb.append(", outPutCode=").append(outPutCode);
        sb.append(", outPutName=").append(outPutName);
        sb.append(", pollutantCode=").append(pollutantCode);
        sb.append(", pollutantName=").append(pollutantName);
        sb.append(", dataType=").append(dataType);
        sb.append(", alarmTime=").append(alarmTime);
        sb.append(", faultTime=").append(faultTime);
        sb.append(", alarmType=").append(alarmType);
        sb.append(", serialVersionUID=").append(serialVersionUID);
        sb.append("]");
        return sb.toString();
    }
}