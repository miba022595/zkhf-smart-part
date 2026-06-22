package com.zkhf.epmis.protocol.util;

import com.zkhf.epmis.protocol.base.PollutantCode;
import com.zkhf.epmis.protocol.common.utils.StringUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

/**
 * redis缓存数据工具类
 */
@Slf4j
@Component
public class PollCacheUtils {

    @Value("${feign.epmis-platform.url:}")
    private String platformUrl;
    @Value("${netty.server.enabled:false}")
    private boolean nettyServerEnabled;

    public static final Map<String, String> pollCodeMap = new HashMap<>();

    private final RestTemplate restTemplate = new RestTemplate();

    private boolean writeLog = true;
    @Scheduled(initialDelay = 0, fixedRate = 180000) // 启动后开始执行，之后每3分钟执行
    public void init() {
        // 通过配置开关控制是否执行
        if (!nettyServerEnabled) {
            if (writeLog) { // 只打印一次就行了
                log.debug("Netty服务未启用，跳过污染因子关系更新");
            }
            writeLog = false;
            return;
        }
        try {
            PollutantCode[] array = restTemplate.getForObject(
                    platformUrl + "/platform/feign/selectAllPollCodeList",
                    PollutantCode[].class
            );
            if (array != null) {
                pollCodeMap.clear();
                for (PollutantCode code : array) {
                    if (StringUtil.isNotEmpty(code.getCode2005())) {
                        pollCodeMap.put(code.getCode2005(), code.getPollutantCode());
                    }
                    if (StringUtil.isNotEmpty(code.getCode2017())) {
                        pollCodeMap.put(code.getCode2017(), code.getPollutantCode());
                    }
                }
            }
            log.info("更新数采报文对应的污染因子关系 {}", pollCodeMap);
        } catch (Exception e) {
            log.error("更新数采报文对应的污染因子关系失败", e);
        }
    }
}
