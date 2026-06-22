package com.zkhf.epmis.process.task.domain;


/**
 * @Description:
 * @Author: yanakai@126.com
 * @CreateDate: 2024-07-14 17:54
 * @UpdateUser: yanakai@126.com
 * @UpdateDate: 2024-07-14 17:54
 * @UpdateRemark: 修改内容
 * @Version: 1.0
 */
public class MonitorDensityControl {
    /**
     * 主键id
     */
    private Long id;

    /**
     * 企业编码
     */
    private String entCode;

    /**
     * 企业名称
     */
    private String entName;

    /**
     * 企业排口编码
     */
    private String outPutCode;

    /**
     * 企业排口名称
     */
    private String outPutName;

    /**
     * 污染物编码
     */
    private String pollutantCode;

    /**
     * 污染物英文名称
     */
    private String pollutantNameEn;

    /**
     * 污染物中文名称
     */
    private String pollutantNameCn;

    /**
     * 污染物排放标准值
     */
    private String standardValue;

    /**
     * 污染物排放累计均值
     */
    private String avgValue;


    /**
     * 污染物小时排放剩余控制值
     */
    private String surplusValue;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getEntCode() {
        return entCode;
    }

    public void setEntCode(String entCode) {
        this.entCode = entCode;
    }

    public String getEntName() {
        return entName;
    }

    public void setEntName(String entName) {
        this.entName = entName;
    }

    public String getOutPutCode() {
        return outPutCode;
    }

    public void setOutPutCode(String outPutCode) {
        this.outPutCode = outPutCode;
    }

    public String getOutPutName() {
        return outPutName;
    }

    public void setOutPutName(String outPutName) {
        this.outPutName = outPutName;
    }

    public String getPollutantCode() {
        return pollutantCode;
    }

    public void setPollutantCode(String pollutantCode) {
        this.pollutantCode = pollutantCode;
    }

    public String getPollutantNameEn() {
        return pollutantNameEn;
    }

    public void setPollutantNameEn(String pollutantNameEn) {
        this.pollutantNameEn = pollutantNameEn;
    }

    public String getPollutantNameCn() {
        return pollutantNameCn;
    }

    public void setPollutantNameCn(String pollutantNameCn) {
        this.pollutantNameCn = pollutantNameCn;
    }

    public String getStandardValue() {
        return standardValue;
    }

    public void setStandardValue(String standardValue) {
        this.standardValue = standardValue;
    }

    public String getAvgValue() {
        return avgValue;
    }

    public void setAvgValue(String avgValue) {
        this.avgValue = avgValue;
    }

    public String getSurplusValue() {
        return surplusValue;
    }

    public void setSurplusValue(String surplusValue) {
        this.surplusValue = surplusValue;
    }

    @Override
    public String toString() {
        return "MonitorDensityControlTask{" +
                "id=" + id +
                ", entCode='" + entCode + '\'' +
                ", entName='" + entName + '\'' +
                ", outPutCode='" + outPutCode + '\'' +
                ", outPutName='" + outPutName + '\'' +
                ", pollutantCode='" + pollutantCode + '\'' +
                ", pollutantNameEn='" + pollutantNameEn + '\'' +
                ", pollutantNameCn='" + pollutantNameCn + '\'' +
                ", standardValue='" + standardValue + '\'' +
                ", avgValue='" + avgValue + '\'' +
                ", surplusValue='" + surplusValue + '\'' +
                '}';
    }
}
