package com.zkhf.epmis.process.onlineMonitoring.service.impl;

import com.zkhf.epmis.core.domain.AjaxResult;
import com.zkhf.epmis.core.utils.DateUtils;
import com.zkhf.epmis.core.utils.StringUtils;
import com.zkhf.epmis.process.base.domain.OutPutInfo;
import com.zkhf.epmis.process.base.utils.RedisCacheUtils;
import com.zkhf.epmis.process.onlineMonitoring.domain.OriginalMessReq;
import com.zkhf.epmis.process.onlineMonitoring.domain.OriginalMessRes;
import com.zkhf.epmis.process.onlineMonitoring.service.OriginalMessService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.zip.GZIPInputStream;

@Slf4j
@Service
public class OriginalMessServiceImpl implements OriginalMessService {

    private static final String ELAPSED_KEY = "elapsed";
    private static final String TIP_KEY = "tip";
    private static final String LIMIT_REACHED_KEY = "limitReached";
    private static final String LOG_FILE_SUFFIX = ".log.gz";
    private static final String LOG_FILE_PREFIX = "app-";
    private static final String FIELD_SUFFIX = ";";
    private static final String ST_PREFIX = "ST=";
    private static final String CN_PREFIX = "CN=";
    private static final String MN_PREFIX = "MN=";
    private static final String MESSAGE_SIGN = "message: ";
    private static final int LOG_TIME_LENGTH = 23;
    private static final String LIMIT_HINT_TEMPLATE = "命中记录超过%s条，请手动缩小时间范围后重试";

    /** 日志基础目录 */
    @Value("${epmis.online-monitoring.original-mess.log-base-dir:logs/protocol/}")
    private String logBaseDir;

    /** 查询结果最大条数 */
    @Value("${epmis.online-monitoring.original-mess.max-result-count:1000}")
    private int maxResultCount;

    private RedisCacheUtils redisCacheUtils;
    @Autowired
    public void setRedisCacheUtils(RedisCacheUtils redisCacheUtils) {
        this.redisCacheUtils = redisCacheUtils;
    }

    @Override
    public AjaxResult selectList(OriginalMessReq req) {
        long startTime = System.currentTimeMillis();
        List<OriginalMessRes> results = new ArrayList<>();
        boolean limitReached = false;
        req = normalizeRequest(req);
        if (null != req.getFileList() && !req.getFileList().isEmpty()) {
            limitReached = collectMessages(req, results);
        }
        AjaxResult result = AjaxResult.success(results);
        if (limitReached) {
            result.put(TIP_KEY, String.format(LIMIT_HINT_TEMPLATE, maxResultCount));
        } else {
            result.put(TIP_KEY, "");
        }
        result.put(LIMIT_REACHED_KEY, limitReached);
        result.put(ELAPSED_KEY, System.currentTimeMillis() - startTime);
        return result;
    }

    private OriginalMessReq normalizeRequest(OriginalMessReq req) {
        if (null == req) {
            req = new OriginalMessReq();
        }
        if (StringUtils.isNotEmpty(req.getSt())) {
            req.setSt(ST_PREFIX + req.getSt() + FIELD_SUFFIX);
        }
        if (StringUtils.isNotEmpty(req.getCn())) {
            req.setCn(CN_PREFIX + req.getCn() + FIELD_SUFFIX);
        }
        // 替换mn号，用户可能会用排口id查询
        if (StringUtils.isNotEmpty(req.getOutPutId())) {
            OutPutInfo outInfo = redisCacheUtils.getAllOutPutById(req.getOutPutId());
            if (null != outInfo) {
                req.setMn(outInfo.getMnNum());
            }
        }
        if (StringUtils.isNotEmpty(req.getMn())) {
            req.setMn(MN_PREFIX + req.getMn() + FIELD_SUFFIX);
        }
        if (null == req.getStartTime() || null == req.getEndTime() || req.getStartTime().isAfter(req.getEndTime())) {
            req.setEndTime(LocalDateTime.now());
            req.setStartTime(req.getEndTime().toLocalDate().atStartOfDay());
        }
        loadLogFiles(req);
        return req;
    }

    /**
     * 加载日期范围内的日志文件
     */
    private void loadLogFiles(OriginalMessReq req) {
        // 有终端号时才查询
        if (null == req || StringUtils.isEmpty(req.getMn())) {
            return;
        }
        File dir = new File(logBaseDir);

        if (!dir.exists() || !dir.isDirectory()) {
            return;
        }
        List<File> files = new ArrayList<>();
        File[] allFiles = dir.listFiles((d, name) -> name.endsWith(LOG_FILE_SUFFIX));

        if (allFiles != null) {
            LocalDate startDate = req.getStartTime().toLocalDate();
            LocalDate endDate = req.getEndTime().toLocalDate();
            Map<String, File> matchedFiles = new TreeMap<>();
            for (File file : allFiles) {
                String sortFileName = parseLogFileName(file.getName(), startDate, endDate);
                if (sortFileName != null) {
                    matchedFiles.put(sortFileName, file);
                }
            }
            // TreeMap 按 key 自然升序排列，保证日志按日期和分片序号顺序读取
            files.addAll(matchedFiles.values());
        }
        req.setFileList(files);
    }

    /**
     * 从文件名提取日期和分片序号
     */
    private static String parseLogFileName(String filename, LocalDate startDate, LocalDate endDate) {
        if (StringUtils.isEmpty(filename) || !filename.startsWith(LOG_FILE_PREFIX) || !filename.endsWith(LOG_FILE_SUFFIX)) {
            return null;
        }
        String[] parts = filename.substring(LOG_FILE_PREFIX.length(), filename.length() - LOG_FILE_SUFFIX.length()).split("-");
        if (parts.length < 4) {
            return null;
        }
        try {
            LocalDate fileDate = LocalDate.parse(parts[0] + "-" + parts[1] + "-" + parts[2]);
            if (fileDate.isBefore(startDate) || fileDate.isAfter(endDate)) {
                return null;
            }
            int index = Integer.parseInt(parts[3]);
            return String.format("%s-%010d", fileDate, index);
        } catch (Exception e) {
            log.warn("extract log file info failed, filename={}", filename);
            return null;
        }
    }

    /**
     * 执行搜索
     */
    private boolean collectMessages(OriginalMessReq request, List<OriginalMessRes> results){
        for (File file : request.getFileList()) {
            if (collectMessagesFromFile(file, request, results)) {
                return true;
            }
        }
        return false;
    }

    /**
     * 在单个压缩文件中搜索
     */
    private boolean collectMessagesFromFile(File file, OriginalMessReq req, List<OriginalMessRes> results){
        BufferedReader reader = null;
        try {
            reader = new BufferedReader(
                    new InputStreamReader(new GZIPInputStream(new FileInputStream(file))));
            String line;
            while ((line = reader.readLine()) != null) {
                OriginalMessRes res = parseMessageLine(line);
                if (res == null) continue;

                if (res.getDataTime().isBefore(req.getStartTime()) || res.getDataTime().isAfter(req.getEndTime())) {
                    continue;
                }
                if (StringUtils.isNotEmpty(req.getSt()) && !res.getMessage().contains(req.getSt())) {
                    continue;
                }
                if (StringUtils.isNotEmpty(req.getCn()) && !res.getMessage().contains(req.getCn())) {
                    continue;
                }
                if (StringUtils.isNotEmpty(req.getMn()) && !res.getMessage().contains(req.getMn())) {
                    continue;
                }
                results.add(res);
                if (results.size() >= maxResultCount) {
                    return true;
                }
            }
        } catch (Exception e) {
            log.error("error ", e);
        } finally {
            if (null != reader) {
                try {
                    reader.close();
                } catch (IOException e) {
                    log.error("close error ", e);
                }
            }
        }
        return false;
    }

    /**
     * 从日志行提取信息
     */
    private static OriginalMessRes parseMessageLine(String line) {
        if (line == null || line.length() < LOG_TIME_LENGTH) {
            return null;
        }
        int messIndex = line.indexOf(MESSAGE_SIGN);
        if (messIndex == -1) {
            return null;
        }
        try {
            String timeStr = line.substring(0, LOG_TIME_LENGTH);
            LocalDateTime dataTime = LocalDateTime.parse(timeStr, DateUtils.yy_m_d_h_m_s_S);
            String message = line.substring(messIndex + MESSAGE_SIGN.length());
            return new OriginalMessRes(dataTime, message);
        } catch (Exception e) {
            return null;
        }
    }

}
