package com.zkhf.epmis.core.enums;

import com.zkhf.epmis.core.utils.MimeTypeUtils;

import java.util.Arrays;
import java.util.Map;
import java.util.stream.Collectors;

public enum AnnexTypeEnum {

    /** 附件类型-企业附件 */
    enterprise("enterprise", MimeTypeUtils.DEFAULT_ALLOWED_EXTENSION),
    /** 附件类型-标记附件 */
    entMark("entMark", MimeTypeUtils.DEFAULT_ALLOWED_EXTENSION),
    /** 附件类型-企业清洁生产附件 */
    entCleanProduce("entCleanProduce", MimeTypeUtils.DEFAULT_ALLOWED_EXTENSION),
    /** 附件类型-企业排污许可附件 */
    entOutPollutantPermit("entOutPollutantPermit", MimeTypeUtils.DEFAULT_ALLOWED_EXTENSION),
    /** 附件类型-企业排口附件 */
    entOutPut("entOutPut", MimeTypeUtils.IMAGE_EXTENSION),
    /** 附件类型-企业环保人员附件 */
    entEnvProPerson("entEnvProPerson", MimeTypeUtils.DEFAULT_ALLOWED_EXTENSION),
    /** 附件类型-其他证书 */
    otherCertificate("otherCertificate", MimeTypeUtils.DEFAULT_ALLOWED_EXTENSION),
    /** 附件类型-企业环评环保管理-项目附件 */
    entEnvMangeProject("entEnvMangeProject", MimeTypeUtils.DEFAULT_ALLOWED_EXTENSION),
    /** 附件类型-企业环评环保管理-环评附件 */
    entEnvMangeEvaluate("entEnvMangeEvaluate", MimeTypeUtils.DEFAULT_ALLOWED_EXTENSION),
    /** 附件类型-企业环评环保管理-环评批复附件 */
    entEnvMangeEvaluateP("entEnvMangeEvaluateP", MimeTypeUtils.DEFAULT_ALLOWED_EXTENSION),
    /** 附件类型-企业环评环保管理-环保验收附件 */
    entEnvMangeCheck("entEnvMangeCheck", MimeTypeUtils.DEFAULT_ALLOWED_EXTENSION),
    /** 附件类型-环境政策法规 */
    envPolicy("envPolicy", MimeTypeUtils.DEFAULT_ALLOWED_EXTENSION),
    /** 附件类型-环保投入 */
    envInvestment("envInvestment", MimeTypeUtils.DEFAULT_ALLOWED_EXTENSION),
    /** 附件类型-政府资金支持 */
    governFundSupport("governFundSupport", MimeTypeUtils.DEFAULT_ALLOWED_EXTENSION),
    /** 附件类型-企业污染治理设施附件 */
    entPollControlFacility("entPollControlFacility", MimeTypeUtils.IMAGE_EXTENSION),
    /** 附件类型-生产设施 */
    EntProduceFacility("entProduceFacility", MimeTypeUtils.DEFAULT_ALLOWED_EXTENSION),
    /** 附件类型-生产车间 */
    EntProduceWorkshop("entProduceWorkshop", MimeTypeUtils.DEFAULT_ALLOWED_EXTENSION),
    /** 附件类型-环保费用登记附件 */
    envFees("envFees", MimeTypeUtils.DEFAULT_ALLOWED_EXTENSION),
    /** 附件类型-环保费用发票附件 */
    envFeeInvoice("envFeeInvoice", MimeTypeUtils.DEFAULT_ALLOWED_EXTENSION),
    /** 附件类型-环保费用付款凭证附件 */
    envFeePayment("envFeePayment", MimeTypeUtils.DEFAULT_ALLOWED_EXTENSION),
    /** 附件类型-环保手工检测执行计划附件（执行标准附件列表） */
    envManualCheckPlan("envManualCheckPlan", MimeTypeUtils.DEFAULT_ALLOWED_EXTENSION),
    /** 附件类型-环保手工检测执行任务附件（执行报告附件） */
    envManualCheckTask("envManualCheckTask", MimeTypeUtils.DEFAULT_ALLOWED_EXTENSION),
    /** 附件类型-生产设施用电附件 */
    entLineProductionFacility("entLineProductionFacility", MimeTypeUtils.DEFAULT_ALLOWED_EXTENSION),
    /** 附件类型-治理设施用电图附件 */
    entLineGovernanceFacility("entLineGovernanceFacility", MimeTypeUtils.DEFAULT_ALLOWED_EXTENSION),
    /** 附件类型-运维任务记录 */
    opsRecord("opsRecord", MimeTypeUtils.DEFAULT_ALLOWED_EXTENSION),
    opsRecordItem("opsRecordItem", MimeTypeUtils.DEFAULT_ALLOWED_EXTENSION),
    /** 统一附件类型，生成sourceId，然后具体数据上保存sourceId */
    unifiedType("unifiedType", MimeTypeUtils.DEFAULT_ALLOWED_EXTENSION),
    /** 附件类型-第三方附件 */
    extUnitType("extUnitType", MimeTypeUtils.DEFAULT_ALLOWED_EXTENSION),
    /** 附件类型-企业排口站房全景附件 */
    entOutPutFull("entOutPutFull", MimeTypeUtils.DEFAULT_ALLOWED_EXTENSION),
    /** 附件类型-执法检查记录附件 */
    enforceRecord("enforceRecord", MimeTypeUtils.DEFAULT_ALLOWED_EXTENSION),
    /** 附件类型-固废间管理 */
    wasteRoom("wasteRoom", MimeTypeUtils.DEFAULT_ALLOWED_EXTENSION),
    /** 附件类型-物资申请单 */
    materialApplyOrder("materialApplyOrder", MimeTypeUtils.DEFAULT_ALLOWED_EXTENSION),
    /** 附件类型-物资入库单 */
    materialInOrder("materialInOrder", MimeTypeUtils.DEFAULT_ALLOWED_EXTENSION),
    /** 附件类型-物资出库单 */
    materialOutOrder("materialOutOrder", MimeTypeUtils.DEFAULT_ALLOWED_EXTENSION),
    /** 附件类型-物资归还单 */
    materialReturnOrder("materialReturnOrder", MimeTypeUtils.DEFAULT_ALLOWED_EXTENSION),
    emergencyMaterialPhoto("emergencyMaterialPhoto", MimeTypeUtils.IMAGE_EXTENSION),
    emergencyPlanAttachment("emergencyPlanAttachment", MimeTypeUtils.DEFAULT_ALLOWED_EXTENSION),
    emergencyDrillAttachment("emergencyDrillAttachment", MimeTypeUtils.DEFAULT_ALLOWED_EXTENSION),
    ;
    // 类型限值最长40位
    public final String name;
    public final String[] types;

    // 使用ConcurrentHashMap保证线程安全
    private static final Map<String, String[]> NAME_TYPES =
            Arrays.stream(AnnexTypeEnum.values())
                    .collect(Collectors.toConcurrentMap(
                            e -> e.name,
                            e -> e.types
                    ));

    AnnexTypeEnum(String name, String[] types) {
        this.name = name;
        this.types = types;
    }

    public static String[] getTypesByName(String name) {
        if (null != name && NAME_TYPES.containsKey(name)) {
            return NAME_TYPES.get(name);
        }
        return null;
    }
}
