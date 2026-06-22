package com.zkhf.epmis.platform.feign;

import com.alibaba.fastjson2.JSONObject;
import com.zkhf.epmis.core.domain.*;
import com.zkhf.epmis.platform.annex.service.AnnexService;
import com.zkhf.epmis.platform.base.domain.DictData;
import com.zkhf.epmis.platform.base.domain.PollutantCode;
import com.zkhf.epmis.platform.base.service.DictService;
import com.zkhf.epmis.platform.base.service.PollutantCodeService;
import com.zkhf.epmis.platform.base.service.ValidPeriodConfService;
import com.zkhf.epmis.platform.ent.domain.EntAutoHead;
import com.zkhf.epmis.platform.ent.domain.EnterprisePart;
import com.zkhf.epmis.platform.ent.domain.ExtUnit;
import com.zkhf.epmis.platform.ent.domain.OutPutAlarmConf;
import com.zkhf.epmis.platform.ent.service.EntOutPutInfoService;
import com.zkhf.epmis.platform.ent.service.EntOutputPollutantService;
import com.zkhf.epmis.platform.ent.service.EnterpriseService;
import com.zkhf.epmis.platform.ent.service.ExtUnitLibService;
import com.zkhf.epmis.platform.envManual.domain.EnvManualCheckTask;
import com.zkhf.epmis.platform.envManual.service.EnvManualCheckTaskService;
import com.zkhf.epmis.platform.envProtect.service.EntOutPollutantPermitService;
import com.zkhf.epmis.platform.plc.domain.PlcInfo;
import com.zkhf.epmis.platform.plc.service.PlcService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

/**
 * feign调用
 */
@RestController
@RequestMapping("/platform/feign")
public class FeignController {

    private DictService dictService;
    @Autowired
    public void setDictService(DictService dictService) {
        this.dictService = dictService;
    }

    private AnnexService annexService;
    @Autowired
    public void setAnnexService(AnnexService annexService) {
        this.annexService = annexService;
    }

    private EnterpriseService enterpriseService;
    @Autowired
    public void setEnterpriseService(EnterpriseService enterpriseService) {
        this.enterpriseService = enterpriseService;
    }

    private EntOutPutInfoService entOutPutInfoService;

    @Autowired
    public void setEntOutPutInfoService(EntOutPutInfoService entOutPutInfoService) {
        this.entOutPutInfoService = entOutPutInfoService;
    }

    private EntOutputPollutantService entOutputPollutantService;

    @Autowired
    public void setEntOutputPollutantService(EntOutputPollutantService entOutputPollutantService) {
        this.entOutputPollutantService = entOutputPollutantService;
    }

    private ValidPeriodConfService validPeriodConfService;

    @Autowired
    public void setValidPeriodConfService(ValidPeriodConfService validPeriodConfService) {
        this.validPeriodConfService = validPeriodConfService;
    }

    private EntOutPollutantPermitService entOutPollutantPermitService;
    @Autowired
    public void setEntOutPollutantPermitService(EntOutPollutantPermitService entOutPollutantPermitService) {
        this.entOutPollutantPermitService = entOutPollutantPermitService;
    }

    private PollutantCodeService pollutantCodeService;
    @Autowired
    public void setPollutantCodeService(PollutantCodeService pollutantCodeService) {
        this.pollutantCodeService = pollutantCodeService;
    }

    private EnvManualCheckTaskService envManualCheckTaskService;
    @Autowired
    public void setEnvManualCheckTaskService(EnvManualCheckTaskService envManualCheckTaskService) {
        this.envManualCheckTaskService = envManualCheckTaskService;
    }

    private PlcService plcService;
    @Autowired
    public void setPlcService(PlcService plcService) {
        this.plcService = plcService;
    }

    private ExtUnitLibService extUnitLibService;
    @Autowired
    public void setExtUnitLibService(ExtUnitLibService extUnitLibService) {
        this.extUnitLibService = extUnitLibService;
    }

    /**
     * 修改附件信息
     */
    @PostMapping("/updateAnnex")
    public AjaxResult updateAnnex(@RequestBody(required = false) JSONObject req) {
        String sourceId = req.getString("sourceId");
        String sourceType = req.getString("sourceType");
        List<String> annexIds = req.getList("annexIds", String.class);
        annexService.updateAnnex(sourceId, sourceType, annexIds);
        return AjaxResult.success();
    }

    /**
     * 依据附件归属获取附件列表
     */
    @PostMapping("/annexList")
    public List<AnnexInfo> annexList(@RequestBody(required = false) AnnexReq req) {
        return annexService.selectAnnexList(req);
    }

    /**
     * 根据字典类型查询字典数据信息
     */
    @GetMapping(value = "/dict/data/type/{type}")
    public List<DictData> dictType(@PathVariable String type) {
        return dictService.selectDataByType(type);
    }

    /**
     * 依据字典类型获取字典值
     */
    @PostMapping(value = "/dict/dataByTypes")
    public Map<String, Map<String, String>> getDataMapByTypes(@RequestBody List<String> dictTypes) {
        return dictService.getDataMapByTypes(dictTypes);
    }

    /**
     * 获取所有企业列表
     */
    @GetMapping("/ent/allList")
    public List<EnterprisePart> selectAllEntList() {
        return enterpriseService.listAll();
    }

    /**
     * 获取所有排口信息列表
     */
    @GetMapping("/outPut/allList")
    public List<Map<String, Object>> selectAllOutPutList() {
        return entOutPutInfoService.listAll();
    }

    /**
     * 获取所有排口污染物信息
     */
    @GetMapping("/outPutPoll/allList")
    public List<Map<String, Object>> selectAllOutPutPollList() {
        return entOutputPollutantService.listAll();
    }

    /**
     * 查询用户收藏关注排口信息列表
     */
    @GetMapping("/userAttentionList")
    public List<Map<String, Object>> selectUserAttentionList(Long userId) {
        return entOutPutInfoService.userAttentionList(userId);
    }

    /**
     * 获取排口动态表头的图表列表
     */
    @GetMapping("/autoHeadChart")
    public List<Map<String, Object>> autoHeadChart(String outPutId) {
        return entOutputPollutantService.autoHeadChart(outPutId);
    }

    /**
     * 获取多个排口动态表头
     */
    @GetMapping("/multipleAutoHead")
    public List<Map<String, Object>> multipleAutoHead(@RequestParam("outPutIds") List<String> outPutIds,
                                                      @RequestParam("dataEnum") String dataEnum) {
        return entOutputPollutantService.multipleAutoHead(outPutIds, dataEnum);
    }

    /**
     * 各类资质数据有效期列表获取
     */
    @GetMapping("/validPeriodConf/allList")
    public List<ValidPeriodAlarmInfo> validPeriodConfList() {
        return validPeriodConfService.feignConfList();
    }

    /**
     * 获取排口的污染物列表
     */
    @GetMapping("/selectPollutantCodesByOutPutId")
    public List<Map<String, Object>> selectPollutantCodesByOutPutId(String outPutId) {
        return entOutputPollutantService.selectPollutantCodesByOutPutId(outPutId);
    }

    /**
     * 依据排口类型查询企业下的排口
     */
    @GetMapping("/entOutPutListByType")
    public List<Map<String, Object>> entOutPutListByType(@RequestParam("entCode") String entCode,
                                                         @RequestParam("outPutType") Integer outPutType) {
        return entOutPutInfoService.listByEnterType(entCode, outPutType);
    }

    /**
     * 查询指定年份的企业排污许可总量
     */
    @GetMapping("/entOutPollutantPermitCount")
    public List<Map<String, Object>> selectAllEntOutPollutantPermitCount(@RequestParam("permitYear") Integer permitYear) {
        return entOutPollutantPermitService.selectAllEntOutPollutantPermitCount(permitYear);
    }

    /**
     * 查询所有的报文污染源因子编码2017和2005对应关系
     */
    @GetMapping("/selectAllPollCodeList")
    public List<PollutantCode> selectAllPollCodeList() {
        return pollutantCodeService.selectAllPollCodeList();
    }

    /**
     * 查询环境手工检测任务列表
     */
    @PostMapping("/selectEnvManualCheckTaskForReport")
    public List<EnvManualCheckTask> selectEnvManualCheckTaskForReport(@RequestBody(required = false) List<String> taskIdList) {
        return envManualCheckTaskService.selectEnvManualCheckTaskForReport(taskIdList);
    }

    /**
     * 环境手工检测任务报告导入
     */
    @PostMapping("/batchUpdateEnvManualCheckTask")
    public void batchUpdateEnvManualCheckTask(@RequestBody(required = false) List<EnvManualCheckTask> taskList) {
        envManualCheckTaskService.batchUpdateEnvManualCheckTask(taskList);
    }

    /**
     * 上传附件
     * 添加文件时便指定文件归属 sourceType
     */
    @PostMapping("/uploadAnnex")
    public AnnexInfo uploadAnnex(@RequestParam("file") MultipartFile file, @RequestParam("sourceType") String sourceType) {
        return annexService.insertAnnexReturnId(file, sourceType);
    }

    /**
     * 获取排口动态表头的列表
     */
    @GetMapping("/getAutoHead")
    public List<EntAutoHead> getAutoHead(String outPutId, String dataEnum) {
        return entOutputPollutantService.selectAutoHead(outPutId, dataEnum);
    }

    /**
     * 获取所有排口状态列表
     */
    @PostMapping("/outPutStatusList")
    public List<Map<String, Object>> outPutStatusList(@RequestBody(required = false) List<String> entCodes) {
        return entOutPutInfoService.outPutStatusList(entCodes);
    }

    /**
     * 查询所有排口报警参数
     */
    @GetMapping("/selectAllAlarmConf")
    public List<OutPutAlarmConf> selectAllAlarmConf() {
        return entOutPutInfoService.selectAllAlarmConf();
    }

    /**
     * 获取多个排口多个动态表头
     */
    @GetMapping("/multipleAutoHeads")
    public Map<String, List<PollHead>> multipleAutoHeads(@RequestParam("outPutIds") List<String> outPutIds,
                                                         @RequestParam("dataEnum") String dataEnum) {
        return entOutputPollutantService.multipleAutoHeads(outPutIds, dataEnum);
    }

    /**
     * 查询点位列表
     */
    @GetMapping("/plcPointList")
    public List<PlcInfo> plcPointList(@RequestParam("entCode") String entCode) {
        return plcService.plcPointList(entCode);
    }

    /**
     * 查询第三方单位列表
     */
    @GetMapping("/allExtUnitList")
    public List<ExtUnit> allList() {
        return extUnitLibService.selectAllExtUnitList();
    }
}
