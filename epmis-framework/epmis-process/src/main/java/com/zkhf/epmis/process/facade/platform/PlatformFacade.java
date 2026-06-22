package com.zkhf.epmis.process.facade.platform;

import cn.hutool.json.JSONObject;
import com.zkhf.epmis.core.domain.AnnexInfo;
import com.zkhf.epmis.core.domain.AnnexReq;
import com.zkhf.epmis.core.domain.PollHead;
import com.zkhf.epmis.core.domain.ValidPeriodAlarmInfo;
import com.zkhf.epmis.process.base.domain.*;
import com.zkhf.epmis.process.base.entity.DictData;
import com.zkhf.epmis.process.envManual.domain.EnvManualCheckTask;
import com.zkhf.epmis.process.facade.platform.fallback.PlatformFacadeFallback;
import com.zkhf.epmis.process.plc.domain.PlcInfo;
import com.zkhf.epmis.process.statistics.dto.PollutantInfo;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

@FeignClient(name = "epmis-platform", url = "${feign.epmis-platform.url:}", path = "/platform/feign"
        , fallback = PlatformFacadeFallback.class)
public interface PlatformFacade {

    /**
     * 修改附件信息
     */
    @RequestMapping(value = "/updateAnnex", method = RequestMethod.POST)
    void updateAnnex(@RequestBody(required = false) JSONObject req);

    /**
     * 获取字典数据
     */
    @RequestMapping(value = "/dict/data/type/{type}", method = RequestMethod.GET)
    List<DictData> dictDataByType(@PathVariable String type);

    /**
     * 获取所有企业列表
     */
    @RequestMapping(value = "/ent/allList", method = RequestMethod.GET)
    List<EntInfo> selectAllEntList();

    /**
     * 获取企业、排口的对应关系
     */
    @RequestMapping(value = "/outPut/allList", method = RequestMethod.GET)
    List<OutPutInfo> selectAllOutPutList();

    /**
     * 获取企业、排口、污染物的对应关系
     */
    @RequestMapping(value = "/outPutPoll/allList", method = RequestMethod.GET)
    List<OutPutPollInfo> selectAllOutPutPollList();

    /**
     * 查询用户收藏关注排口信息列表
     */
    @RequestMapping(value = "/userAttentionList", method = RequestMethod.GET)
    List<UserAttentionInfo> selectUserAttentionList(@RequestParam("userId") Long userId);

    /**
     * 获取排口动态表头的图表列表
     */
    @RequestMapping(value = "/autoHeadChart", method = RequestMethod.GET)
    List<Map<String, Object>> autoHeadChart(@RequestParam("outPutId") String outPutId);

    /**
     * 获取多个排口动态表头
     */
    @RequestMapping(value ="/multipleAutoHead", method = RequestMethod.GET)
    List<Map<String, Object>> multipleAutoHead(@RequestParam("outPutIds") List<String> outPutIds, @RequestParam("dataEnum") String dataEnum);

    /**
     * 各类资质数据有效期列表获取
     */
    @RequestMapping(value = "/validPeriodConf/allList", method = RequestMethod.GET)
    List<ValidPeriodAlarmInfo> validPeriodConfList();

    /**
     * 获取排口的污染物列表
     */
    @RequestMapping(value = "/selectPollutantCodesByOutPutId", method = RequestMethod.GET)
    List<PollutantInfo> selectPollutantCodesByOutPutId(@RequestParam("outPutId") String outPutId);

    /**
     * 依据排口类型查询企业下的排口
     */
    @RequestMapping(value = "/entOutPutListByType", method = RequestMethod.GET)
    List<Map<String, Object>> entOutPutListByType(@RequestParam("entCode") String entCode,
                                                  @RequestParam("outPutType") Integer outPutType);

    /**
     * 查询指定年份的企业排污许可总量
     */
    @RequestMapping(value = "/entOutPollutantPermitCount", method = RequestMethod.GET)
    List<Map<String, Object>> selectAllEntOutPollutantPermitCount(@RequestParam("permitYear") Integer permitYear);

    /**
     * 查询所有的报文污染源因子编码2017和2005对应关系
     */
    @RequestMapping(value = "/selectAllPollCodeList", method = RequestMethod.GET)
    List<PollutantCode> selectAllPollCodeList();

    /**
     * 上传附件
     * 添加文件时便指定文件归属 sourceType
     */
    @PostMapping("/uploadAnnex")
    AnnexInfo uploadAnnex(@RequestParam("file") MultipartFile file, @RequestParam("sourceType") String sourceType);

    /**
     * 依据附件归属获取附件列表
     */
    @GetMapping("/annexList")
    List<AnnexInfo> annexList(@RequestBody AnnexReq req);

    /**
     * 查询环境手工检测任务列表
     */
    @PostMapping("/selectEnvManualCheckTaskForReport")
    List<EnvManualCheckTask> selectEnvManualCheckTaskForReport(@RequestBody(required = false) List<String> taskIdList);

    /**
     * 环境手工检测任务报告导入
     */
    @PostMapping("/batchUpdateEnvManualCheckTask")
    void batchUpdateEnvManualCheckTask(@RequestBody(required = false) List<EnvManualCheckTask> taskList);

    /**
     * 获取排口动态表头的列表
     */
    @RequestMapping(value ="/getAutoHead", method = RequestMethod.GET)
    List<Map<String, Object>> getAutoHead(@RequestParam("outPutId") String outPutId, @RequestParam("dataEnum") String dataEnum);

    /**
     * 获取所有排口状态列表
     */
    @RequestMapping(value = "/outPutStatusList", method = RequestMethod.POST)
    List<Map<String, Object>> outPutStatusList(@RequestBody(required = false) List<String> entCodes);

    /**
     * 查询所有排口报警参数
     */
    @RequestMapping(value = "/selectAllAlarmConf", method = RequestMethod.GET)
    List<OutPutAlarmConf> selectAllAlarmConf();

    /**
     * 获取多个排口多个动态表头
     */
    @RequestMapping(value ="/multipleAutoHeads", method = RequestMethod.GET)
    Map<String, List<PollHead>> multipleAutoHeads(@RequestParam("outPutIds") List<String> outPutIds, @RequestParam("dataEnum") String dataEnum);

    /**
     * 查询点位列表
     */
    @RequestMapping(value ="/plcPointList", method = RequestMethod.GET)
    List<PlcInfo> plcPointList(@RequestParam("entCode") String entCode);


    /**
     * 查询第三方单位列表
     */
    @RequestMapping(value = "/allExtUnitList", method = RequestMethod.GET)
    List<Map<String, Object>> allExtUnitList();

    /**
     * 依据字典类型获取字典值
     */
    @PostMapping(value = "/dict/dataByTypes")
    Map<String, Map<String, String>> selectDataMapByTypes(@RequestBody List<String> dictTypes);
}
