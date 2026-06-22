package com.zkhf.epmis.platform.task.spider.handler;

import com.alibaba.fastjson2.JSON;
import com.github.f4b6a3.ulid.UlidCreator;
import com.xxl.job.core.handler.annotation.XxlJob;
import com.zkhf.epmis.core.config.EPMISConfig;
import com.zkhf.epmis.core.constant.Constants;
import com.zkhf.epmis.core.domain.AnnexInfo;
import com.zkhf.epmis.core.enums.AnnexTypeEnum;
import com.zkhf.epmis.core.utils.DateUtils;
import com.zkhf.epmis.core.utils.StringUtils;
import com.zkhf.epmis.core.utils.uuid.Seq;
import com.zkhf.epmis.platform.ent.domain.EnterprisePart;
import com.zkhf.epmis.platform.envProtect.domain.EntOutPollutantPermit;
import com.zkhf.epmis.platform.envProtect.service.EntOutPollutantPermitService;
import com.zkhf.epmis.platform.mapper.annex.AnnexMapper;
import com.zkhf.epmis.platform.mapper.ent.EnterpriseMapper;
import com.zkhf.epmis.platform.task.spider.converter.LicenseCodeConverter;
import com.zkhf.epmis.platform.task.spider.domain.Attachment;
import com.zkhf.epmis.platform.task.spider.domain.EmissionCategory;
import com.zkhf.epmis.platform.task.spider.domain.FailedDownloadTask;
import com.zkhf.epmis.platform.task.spider.domain.LicenseFullInfo;
import com.zkhf.epmis.platform.utils.file.FileUploadUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FilenameUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.util.*;

/**
 * 排污许可证信息爬取工具 - 最终完整版
 *
 * <p>功能说明：</p>
 * <ul>
 *   <li>1. 从全国排污许可证管理信息平台获取企业排污许可信息</li>
 *   <li>2. 解析基础信息（许可证编号、有效期、发证日期、管理类别、发证机关等）</li>
 *   <li>3. 解析产品信息（主要产品、产量、备注）</li>
 *   <li>4. 解析排放信息（废气、废水、扬尘、VOC、恶臭、噪声）</li>
 *   <li>5. 解析许可限值（污染物排放浓度限值、总量限值）</li>
 *   <li>6. 解析执行报告报送要求</li>
 *   <li>7. 下载正本PDF（直接下载）</li>
 *   <li>8. 下载副本（下载多张图片并合并为PDF）</li>
 * </ul>
 */
@Slf4j
@Component
public class LicenseSpiderJob {

    private EnterpriseMapper enterpriseMapper;
    @Autowired
    public void setEnterpriseMapper(EnterpriseMapper enterpriseMapper) {
        this.enterpriseMapper = enterpriseMapper;
    }

    private EntOutPollutantPermitService entOutPollutantPermitService;
    @Autowired
    public void setEntOutPollutantPermitService(EntOutPollutantPermitService entOutPollutantPermitService) {
        this.entOutPollutantPermitService = entOutPollutantPermitService;
    }

    private AnnexMapper annexMapper;
    @Autowired
    public void setAnnexMapper(AnnexMapper annexMapper) {
        this.annexMapper = annexMapper;
    }

    private LicenseSpiderClient spiderClient;
    @Autowired
    public void setSpiderClient(LicenseSpiderClient spiderClient) {
        this.spiderClient = spiderClient;
    }

    private LicenseCodeConverter codeConverter;
    @Autowired
    public void setCodeConverter(LicenseCodeConverter codeConverter) {
        this.codeConverter = codeConverter;
    }

    @XxlJob("licenseSpiderTaskHandler")
    public void licenseSpiderTaskHandler() throws IOException {
        // 获取所有需要爬取的企业
        List<EnterprisePart> all = enterpriseMapper.listAll();
        if (null == all || all.isEmpty()) {
            return;
        }
        // 执行爬取任务
        spiderClient.initSession();
        codeConverter.initCache(); // 任务执行时加载字典缓存
        spiderClient.setDownloadDir(EPMISConfig.getAnnexPath(AnnexTypeEnum.entOutPollutantPermit.name()));
        String downloadDir = spiderClient.getDownloadDir();

        // 获取所有需要爬取的企业
        List<String> companyNames = new ArrayList<>();
        Map<String, EnterprisePart> nameToEntMap = new HashMap<>();
        // 构建 name → entCode 映射
        Map<String, String> nameToEntCode = new HashMap<>();
        for (EnterprisePart e : all) {
            if (StringUtils.isNotEmpty(e.getEntName())) {
                companyNames.add(e.getEntName());
                nameToEntMap.put(e.getEntName(), e);
                nameToEntCode.put(e.getEntName(), e.getEntCode());
            }
        }

        // 执行爬取（下载附件到磁盘）
        List<LicenseFullInfo> results = spiderClient.crawlCompanies(companyNames, nameToEntCode);

        // 保存结果到数据库 & 处理附件
        int foundCount = 0;
        for (LicenseFullInfo info : results) {
            if (!info.isFound()) {
                continue;
            }
            foundCount++;
            EnterprisePart ent = nameToEntMap.get(info.getCompanyName());
            if (ent == null) {
                log.warn("未匹配到企业: {}", info.getCompanyName());
                continue;
            }
            // 持久化爬取的数据
            updateEntOutPollutantPermit(info, ent, downloadDir);
        }
        // 附件重试
        retryFile();
        // 记录批量的结果
        spiderClient.saveToJson(results, "license_full_data.json");

        log.info("全部完成: 处理{}家, 爬取成功{}家", results.size(), foundCount);
    }

    /**
     * 执行单个爬取
     */
    public void licenseSpiderSingle(EnterprisePart part) throws IOException {
        if (null == part || StringUtils.isEmpty(part.getEntName())) {
            return;
        }
        spiderClient.initSession();
        codeConverter.initCache(); // 任务执行时加载字典缓存
        spiderClient.setDownloadDir(EPMISConfig.getAnnexPath(AnnexTypeEnum.entOutPollutantPermit.name()));
        String downloadDir = spiderClient.getDownloadDir();

        // 执行爬取（下载附件到磁盘）
        LicenseFullInfo info = spiderClient.crawlCompany(part.getEntName(), part.getEntCode());
        // 持久化爬取的数据
        updateEntOutPollutantPermit(info, part, downloadDir);
        // 附件重试
        retryFile();

        log.info("爬取完成: {}", info);
    }

    private void updateEntOutPollutantPermit(LicenseFullInfo info, EnterprisePart part, String downloadDir) {
        if (null == info || !info.isFound()) {
            return;
        }
        try {
            // 编码转换：中文名称 → 系统编码
            codeConverter.convertChineseNameToCode(info);

            // 持久化到排污许可主表
            EntOutPollutantPermit permit = new EntOutPollutantPermit();
            permit.setEntCode(part.getEntCode());
            permit.setPermitLevel(info.getManagementType());
            permit.setPermitNum(info.getLicenseNo());
            permit.setBeginDate(info.getValidStart());
            permit.setEndDate(info.getValidEnd());
            permit.setIssueOffice(info.getIssueOrg());
            permit.setIssueDate(info.getIssueDate());
            permit.setReportRequire(info.getReportRequirements());
            // 产品信息
            if (info.getMainProducts() != null && !info.getMainProducts().isEmpty()) {
                permit.setProductDesc(JSON.toJSONString(info.getMainProducts()));
            }
            permit.setProductOutput(info.getOutput());
            // 排放信息
            if (info.getEmissionInfo() != null) {
                // 废气
                EmissionCategory wasteGas = info.getEmissionInfo().get("废气");
                if (wasteGas != null) {
                    permit.setGasPollType(wasteGas.getPollutants());
                    permit.setGasEmissionRule(wasteGas.getEmissionPattern());
                    permit.setGasExecuteStandard(wasteGas.getExecStandard());
                }
                // 废水
                EmissionCategory wasteWater = info.getEmissionInfo().get("废水");
                if (wasteWater != null) {
                    permit.setWaterPollType(wasteWater.getPollutants());
                    permit.setWaterEmissionRule(wasteWater.getEmissionPattern());
                    permit.setWaterExecuteStandard(wasteWater.getExecStandard());
                }
                // 扬尘
//                    EmissionCategory dust = info.getEmissionInfo().get("扬尘");
//                    if (dust != null) {
//                        permit.setDustPollType(dust.getPollutants());
//                        permit.setDustEmissionRule(dust.getEmissionPattern());
//                        permit.setDustExecuteStandard(dust.getExecStandard());
//                    }
                // VOC
//                    EmissionCategory voc = info.getEmissionInfo().get("VOC");
//                    if (voc != null) {
//                        permit.setVocPollType(voc.getPollutants());
//                        permit.setVocExecuteStandard(voc.getExecStandard());
//                    }
                // 恶臭
//                    EmissionCategory odor = info.getEmissionInfo().get("恶臭");
//                    if (odor != null) {
//                        permit.setOdorPollType(odor.getPollutants());
//                        permit.setOdorEmissionRule(odor.getEmissionPattern());
//                        permit.setOdorExecuteStandard(odor.getExecStandard());
//                    }
                // 噪声
//                    EmissionCategory noise = info.getEmissionInfo().get("噪声");
//                    if (noise != null) {
//                        permit.setNoisePollType(noise.getPollutants());
//                        permit.setNoiseEmissionRule(noise.getEmissionPattern());
//                        permit.setNoiseExecuteStandard(noise.getExecStandard());
//                    }
            }
            entOutPollutantPermitService.updateEntOutPollutantPermit(permit);
            log.info("已保存: {}", info.getCompanyName());

            // 删除旧附件记录
            String sourceType = AnnexTypeEnum.entOutPollutantPermit.name();
            deleteOldAnnexes(part.getEntCode(), sourceType);

            // 为成功下载的文件注册附件记录
            if (info.getAttachments() != null && !info.getAttachments().isEmpty()) {
                String safeName = info.getCompanyName().replaceAll("[\\\\/:*?\"<>|]", "_");
                for (Attachment att : info.getAttachments()) {
                    if (att.getType() == null) continue;
                    String fileName = safeName + "_" + att.getType() + ".pdf";
                    File localFile = new File(downloadDir, fileName);
                    if (localFile.exists()) {
                        registerFileToAnnex(localFile, part.getEntCode());
                    }
                }
            }
        } catch (Exception e) {
            log.error("保存失败: {}", info.getCompanyName(), e);
        }
    }

    private void retryFile() {
        spiderClient.processRetryQueue();
        // 注册重试成功的文件
        List<FailedDownloadTask> retried = spiderClient.getRetriedTasks();
        if (!retried.isEmpty()) {
            log.info("注册 {} 个重试成功的文件", retried.size());
            for (FailedDownloadTask task : retried) {
                File file = new File(task.getFilePath());
                if (file.exists()) {
                    registerFileToAnnex(file, task.getEntCode());
                }
            }
        }
        spiderClient.shutdown();
    }

    // ==================== 附件注册（DB操作） ====================

    private void deleteOldAnnexes(String sourceId, String sourceType) {
        // 防护：sourceId 为空时不执行删除，避免误清空其他记录
        if (StringUtils.isEmpty(sourceId)) {
            log.warn("deleteOldAnnexes跳过: sourceId为空");
            return;
        }
        List<AnnexInfo> oldList = annexMapper.selectAnnexListBySource(sourceId, sourceType);
        if (oldList == null || oldList.isEmpty()) {
            return;
        }
        List<String> deleteIds = new ArrayList<>();
        List<String> deletePaths = new ArrayList<>();
        for (AnnexInfo old : oldList) {
            deleteIds.add(old.getAnnexId());
            if (old.getFilePath() != null) {
                deletePaths.add(old.getFilePath().replace(Constants.RESOURCE_PREFIX, ""));
            }
        }
        annexMapper.deleteAnnexByIds(deleteIds);
        if (!deletePaths.isEmpty()) {
            deletePaths.forEach(FileUploadUtils::deleteFile);
        }
    }

    /**
     * 将本地已下载的附件文件注册到附件管理系统
     *
     * @param localFile 本地文件
     * @param entCode 企业编码，用于关联附件与排污许可
     */
    private void registerFileToAnnex(File localFile, String entCode) {
        if (localFile == null || !localFile.exists()) {
            log.warn("文件不存在，跳过注册");
            return;
        }
        try {
            String sourceType = AnnexTypeEnum.entOutPollutantPermit.name();
            String baseDir = EPMISConfig.getAnnexPath(sourceType);
            String fileName = localFile.getName();
            String extension = FilenameUtils.getExtension(fileName);
            String saveFileName = StringUtils.format("{}/{}_{}.{}",
                    DateUtils.datePath(), FilenameUtils.getBaseName(fileName),
                    Seq.getId(Seq.uploadSeqType), extension);

            // 移动文件到附件结构化目录
            File destFile = FileUploadUtils.getAbsoluteFile(baseDir, saveFileName);
            Files.move(localFile.toPath(), destFile.toPath(), StandardCopyOption.REPLACE_EXISTING);

            // 生成附件记录，直接关联企业
            String annexId = UlidCreator.getMonotonicUlid().toString();
            String filePath = FileUploadUtils.getPathFileName(baseDir, saveFileName);

            AnnexInfo annexInfo = new AnnexInfo();
            annexInfo.setAnnexId(annexId);
            annexInfo.setSourceId(entCode);
            annexInfo.setSourceType(sourceType);
            annexInfo.setFileName(fileName);
            annexInfo.setFileType(extension);
            annexInfo.setFilePath(filePath);
            annexInfo.setFileSize(destFile.length());
            annexInfo.setCreateUser("system");
            annexInfo.setCreateTime(LocalDateTime.now());
            annexMapper.insertAnnex(annexInfo);

            log.debug("附件注册成功: {}", fileName);
        } catch (Exception e) {
            log.error("注册附件失败: {}", localFile.getName(), e);
        }
    }
}