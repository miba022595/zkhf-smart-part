package com.zkhf.epmis.platform.ent.domain;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.zkhf.epmis.core.domain.AnnexInfo;
import com.zkhf.epmis.core.enums.OutPutStatusEnum;
import com.zkhf.epmis.core.enums.OutPutTypeEnum;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * 企业排口对象 t_bas_ent_out_put_info
 */
@Data
public class EntOutPutInfo {

    /**
     * 企业排口主键id
     */
    private String outPutId;

    /**
     * 关联企业编码
     */
    private String entCode;

    /**
     * 企业名称
     */
    private String entName;

    /**
     * 排放口编码
     */
    private String outPutCode;

    /**
     * 排放口名称
     */
    private String outPutName;

    /**
     * 排放口类型
     * 参见 {@link OutPutTypeEnum}
     */
    private Integer outPutType;

    /**
     * 排放口设备mn号
     */
    private String mnNum;

    /**
     * 排放口状态，字典out_put_status
     * 参见 {@link OutPutStatusEnum}
     */
    private Integer outPutStatus;

    /**
     * 经度
     */
    private Double longitude;

    /**
     * 纬度
     */
    private Double latitude;

    /**
     * 打卡签到范围，默认100米
     */
    private Integer clockRange;

    /**
     * 排放口高度
     */
    private Double outPutHeight;

    /**
     * 排放口位置
     */
    private String outPutPosition;

    /**
     * 是否安装在线设备，1 是，0 否
     */
    private Integer installOnline;

    /**
     * 数据传输率，%
     */
    private Float transRate;

    /**
     * 数据有效率，%
     */
    private Float validRate;

    /**
     * 实时数据间隔，秒，n秒，一般配置为300秒，即5分钟一条
     */
    private Integer realDataInterval;

    /**
     * 分钟数据间隔，n分钟，一般配置为5、10分钟
     * 60/值，取整数部分当作小时的分钟数据条数
     */
    private Integer minuteDataInterval;

    /**
     * 污染物名称列表
     */
    private String pollutantCode;
    private String pollutantName;

    /**
     * 关注
     */
    private boolean attention;

    /**
     * 备注
     */
    private String remark;

    /**
     * 创建时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updateTime;

    /**
     * 附件列表（更新时用）
     */
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private List<String> annexIds;

    /**
     * 附件列表（查询时用）
     */
    private List<AnnexInfo> annexInfoList;

    /**
     * 附件列表（更新时用）-站房全景
     */
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private List<String> fullAnnexIds;

    /**
     * 附件列表（查询时用）-站房全景
     */
    private List<AnnexInfo> fullAnnexInfoList;

    /**
     * 关联污染治理设施id列表
     */
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private List<String> facilityIds;

    /**
     * 关联污染治理设施信息
     */
    private List<Map<String, Object>> relateFacilityList;

    /**
     * 控制级别（国控、省控、市控、无）
     */
    private String controlLevel;

    /**
     * 归属管理人员ID
     */
    private Long perId;

    /**
     * 归属管理人员名称
     */
    private String perName;

    /**
     * 归属管理人员联系电话
     */
    private String perPhone;

    /**
     * 归属管理人员联系邮箱
     */
    private String perEmail;

    /**
     * 报警对应人id
     */
    private Long perAlarmId;

    /**
     * 报警对应人姓名
     */
    private String perAlarmName;

    /**
     * 报警对应人联系方式-电话
     */
    private String perAlarmPhone;

    /**
     * 报警对应人联系方式-邮箱
     */
    private String perAlarmEmail;
}
