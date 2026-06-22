package com.zkhf.epmis.process.base.utils;

import com.alibaba.fastjson2.JSONArray;
import com.zkhf.epmis.core.constant.CacheConstants;
import com.zkhf.epmis.core.redis.RedisCache;
import com.zkhf.epmis.core.utils.StringUtils;
import com.zkhf.epmis.process.base.domain.*;
import com.zkhf.epmis.process.facade.platform.PlatformFacade;
import com.zkhf.epmis.process.mqtt.domain.RealCacheData;
import com.zkhf.epmis.process.solidWaste.domain.WasteDict;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * redis缓存数据工具类
 */
@Slf4j
@Component
public class RedisCacheUtils {

    /** 排口信息缓存，供消费数据时使用 */
    public static Map<String, OutPutInfo> outPutInfoMap = new HashMap<>();
    /** 排口报警配置信息缓存，供消费数据时使用 */
    public static Map<String, Map<Integer, OutPutAlarmConf>> outPutAlarmConfMap = new HashMap<>();
    /** 排口污染物信息缓存，供消费数据时使用 */
    public static Map<String, Map<String, OutPutPollInfo>> outPutPollInfoMap = new HashMap<>();

    private final String pollDataKey = CacheConstants.DATA_CACHE_KEY + "poll:data";
    private final String entDataKey = CacheConstants.DATA_CACHE_KEY + "ent:data:ent";
    private final String outPutDataKey = CacheConstants.DATA_CACHE_KEY + "ent:data:outPut";
    private final String outPutPollDataKey = CacheConstants.DATA_CACHE_KEY + "ent:data:outPutPoll";
    private final String outPutOnlineKey = CacheConstants.DATA_CACHE_KEY + "ent:data:outPutOnline";
    private final String outPutAlarmConfKey = CacheConstants.DATA_CACHE_KEY + "ent:data:outPutAlarmConf";
    private final String realDataCacheKey = CacheConstants.DATA_CACHE_KEY + "ent:data:realCache";

    private RedisCache redisCache;
    @Autowired
    public void setRedisCache(RedisCache redisCache) {
        this.redisCache = redisCache;
    }

    private PlatformFacade platformFacade;
    @Autowired
    public void setPlatformFacade(PlatformFacade platformFacade) {
        this.platformFacade = platformFacade;
    }

    @PostConstruct // 启动后设置缓存
    public void initPostConstruct() {
        try {
            // 获取企业和排口信息
            List<OutPutInfo> outPutList = getAllOutPutList();
            if (null != outPutList) {
                // 更新内存
                outPutList.forEach( e -> outPutInfoMap.put(e.getMnNum(), e));
            }
        } catch (Exception e) {
            log.error("更新企业和排口缓存信息失败", e);
        }
        try {
            // 获取企业和排口信息
            List<OutPutPollInfo> outPutList = getAllOutPutPollList();
            if (null != outPutList) {
                // 更新内存
                outPutList.forEach( e -> {
                    if (!RedisCacheUtils.outPutPollInfoMap.containsKey(e.getMnNum())) {
                        RedisCacheUtils.outPutPollInfoMap.put(e.getMnNum(), new HashMap<>());
                    }
                    RedisCacheUtils.outPutPollInfoMap.get(e.getMnNum()).put(e.getPollutantCode(), e);
                });
            }
        } catch (Exception e) {
            log.error("更新企业和排口缓存信息失败", e);
        }
        try {
            // 获取排口报警配置信息
            List<OutPutAlarmConf> confList = getAllAllAlarmConf();
            if (null != confList) {
                // 更新内存
                setOutPutAlarmConfMap(confList);
            }
        } catch (Exception e) {
            log.error("更新企业和排口缓存信息失败", e);
        }
    }

    @Scheduled(initialDelay = 10000, fixedRate = 180000) // 首次延迟10秒，之后每3分钟执行
    public void init() {
        log.debug("执行任务测试");
        try {
            // 查询所有的报文污染源因子编码信息
            List<PollutantCode> pollCodeList = platformFacade.selectAllPollCodeList();
            if (null == pollCodeList) { // 设置默认值
                pollCodeList = new ArrayList<>();
            }
            // 更新缓存
            redisCache.setCacheObject(pollDataKey, JSONArray.from(pollCodeList));
        } catch (Exception e) {
            log.error("更新企业缓存信息失败", e);
        }
        try {
            // 获取所有企业信息
            List<EntInfo> entList = platformFacade.selectAllEntList();
            if (null == entList) { // 设置默认值
                entList = new ArrayList<>();
            }
            // 过滤企业的数据
            entList = entList.stream()
                    .filter(e -> StringUtils.isNotEmpty(e.getEntCode()))
                    .collect(Collectors.toList());
            // 更新缓存
            redisCache.setCacheObject(entDataKey, JSONArray.from(entList));
        } catch (Exception e) {
            log.error("更新企业缓存信息失败", e);
        }
        try {
            // 获取企业和排口信息
            List<OutPutInfo> outPutList = platformFacade.selectAllOutPutList();
            if (null == outPutList) { // 设置默认值
                outPutList = new ArrayList<>();
            }
            // 过滤企业、排口信息为空的数据
            outPutList = outPutList.stream()
                    .filter(e -> StringUtils.isNotEmpty(e.getEntCode()) && StringUtils.isNotEmpty(e.getOutPutId()))
                    .collect(Collectors.toList());
            // 更新内存
            outPutList.forEach( e -> outPutInfoMap.put(e.getMnNum(), e));
            // 更新缓存
            redisCache.setCacheObject(outPutDataKey, JSONArray.from(outPutList));
        } catch (Exception e) {
            log.error("更新企业和排口缓存信息失败", e);
        }
        try {
            // 获取企业排口的污染物信息
            List<OutPutPollInfo> outPutPollInfoList = platformFacade.selectAllOutPutPollList();
            if (null == outPutPollInfoList) { // 设置默认值
                outPutPollInfoList = new ArrayList<>();
            }
            // 过滤企业、排口信息为空的数据
            outPutPollInfoList = outPutPollInfoList.stream()
                    .filter(e -> StringUtils.isNotEmpty(e.getEntCode()) && StringUtils.isNotEmpty(e.getOutPutId()))
                    .collect(Collectors.toList());
            // 更新内存
            outPutPollInfoList.forEach( e -> {
                if (!outPutPollInfoMap.containsKey(e.getMnNum())) {
                    outPutPollInfoMap.put(e.getMnNum(), new HashMap<>());
                }
                outPutPollInfoMap.get(e.getMnNum()).put(e.getPollutantCode(), e);
            });
            // 更新缓存
            redisCache.setCacheObject(outPutPollDataKey, JSONArray.from(outPutPollInfoList));
        } catch (Exception e) {
            log.error("更新企业排口的污染物缓存信息失败", e);
        }
        try {
            // 查询所有排口报警参数
            List<OutPutAlarmConf> confList = platformFacade.selectAllAlarmConf();
            if (null == confList) { // 设置默认值
                confList = new ArrayList<>();
            }
            // 更新内存
            setOutPutAlarmConfMap(confList);
            // 更新缓存
            redisCache.setCacheObject(outPutAlarmConfKey, JSONArray.from(confList));
        } catch (Exception e) {
            log.error("更新企业缓存信息失败", e);
        }
    }

    private void setOutPutAlarmConfMap(List<OutPutAlarmConf> confList) {
        if (null == confList || confList.isEmpty()) {
            return;
        }
        confList.forEach( e -> {
            if (null != e.getAlarmCode()) {
                if (StringUtils.isNotEmpty(e.getDataType()) && !e.getDataType().endsWith(",")) {
                    e.setDataType(e.getDataType() + ",");
                }
                if (StringUtils.isNotEmpty(e.getOutPutStatus()) && !e.getOutPutStatus().endsWith(",")) {
                    e.setOutPutStatus(e.getOutPutStatus() + ",");
                }
                outPutAlarmConfMap.computeIfAbsent(e.getOutPutId(), k -> new HashMap<>()).put(e.getAlarmCode(), e);
            }
        });

    }

    /**
     * 获取企业详情列表
     * @return 列表
     */
    public List<EntInfo> getAllEntList(String entName) {
        List<EntInfo> entList = getAllEntList();
        if (null == entName) {
            return entList;
        }
        List<EntInfo> reList = null;
        if (null != entList) {
            reList = new ArrayList<>();
            for (EntInfo info : entList) {
                if (!info.getEntName().contains(entName)) {
                    continue;
                }
                reList.add(info);
            }
        }
        return reList;
    }

    /**
     * 获取排口详情列表
     * @param outPutId 排口主键id
     */
    public OutPutInfo getAllOutPutById(String outPutId) {
        if (null == outPutId) {
            return null;
        }
        List<OutPutInfo> outPutList = getAllOutPutList();
        if (null != outPutList) {
            for (OutPutInfo info : outPutList) {
                if (outPutId.equals(info.getOutPutId())) {
                    return info;
                }
            }
        }
        return null;
    }


    /**
     * 获取排口详情列表
     * @param outPutType 污染物类型，1废水；2废气
     * @return 列表
     */
    public List<OutPutInfo> getAllOutPutList(Integer outPutType) {
        if (null == outPutType) {
            return null;
        }
        List<OutPutInfo> outPutList = getAllOutPutList();
        List<OutPutInfo> reList = null;
        if (null != outPutList) {
            reList = new ArrayList<>();
            for (OutPutInfo info : outPutList) {
                if (!outPutType.equals(info.getOutPutType())) {
                    continue;
                }
                reList.add(info);
            }
        }
        return reList;
    }

    public List<OutPutPollInfo> getAllOutPutPollList(Integer outPutType) {
        if (null == outPutType) {
            return null;
        }
        List<OutPutPollInfo> outPutPollList = getAllOutPutPollList();
        List<OutPutPollInfo> reList = null;
        if (null != outPutPollList) {
            reList = new ArrayList<>();
            for (OutPutPollInfo info : outPutPollList) {
                if (!outPutType.equals(info.getOutPutType())) {
                    continue;
                }
                reList.add(info);
            }
        }
        return reList;
    }

    public List<PollutantCode> getAllPollDataList() {
        return ((JSONArray)redisCache.getCacheObject(pollDataKey)).toList(PollutantCode.class);
    }

    public List<EntInfo> getAllEntList() {
        return ((JSONArray)redisCache.getCacheObject(entDataKey)).toList(EntInfo.class);
    }

    public List<OutPutInfo> getAllOutPutList() {
        return ((JSONArray)redisCache.getCacheObject(outPutDataKey)).toList(OutPutInfo.class);
    }

    public List<OutPutPollInfo> getAllOutPutPollList() {
        return ((JSONArray)redisCache.getCacheObject(outPutPollDataKey)).toList(OutPutPollInfo.class);
    }

    public List<String> getAllOutPutOnlineList() {
        return redisCache.getCacheObject(outPutOnlineKey);
    }

    public void setAllOutPutOnlineList(List<String> onlineDeviceList, Integer expireTime) {
        redisCache.setCacheObject(outPutOnlineKey, onlineDeviceList, expireTime, TimeUnit.MINUTES);
    }

    /**
     * 通过缓存判断是否可以发送
     * 同一小时只能1次
     */
    public boolean ifSendByCache(String... keys) {
        String key = CacheConstants.DATA_CACHE_KEY + "alarm:send:" +
                String.join("_", keys) + "_" + LocalDateTime.now().getHour();
        // 存在时不能发送
        if (redisCache.hasKey(key)) {
            return false;
        }
        // 不存在时设置缓存，防止下次发送
        redisCache.setCacheObject(key, "EMP_INFO", 61, TimeUnit.MINUTES);
        return true;
    }

    public List<OutPutAlarmConf> getAllAllAlarmConf() {
        return ((JSONArray)redisCache.getCacheObject(outPutAlarmConfKey)).toList(OutPutAlarmConf.class);
    }

    public Map<String, RealCacheData> getAllRealDataList() {
        return redisCache.getCacheMap(realDataCacheKey);
    }

    public void setRealData(String outPutId, RealCacheData realCacheData) {
        if (null == realCacheData || StringUtils.isEmpty(outPutId)) {
            return;
        }
        redisCache.setCacheMapValue(realDataCacheKey, outPutId, realCacheData);
    }

    /**
     * 设置固废分类字典缓存
     */
    public void setWasteDictCache(List<WasteDict> dictList) {
        redisCache.setCacheObject(getWasteDictCacheKey(), dictList);
    }

    /**
     * 获取固废分类字典字典缓存
     */
    public List<WasteDict> getWasteDictDictCache() {
        return redisCache.getCacheObject(getWasteDictCacheKey());
    }

    public static String getWasteDictCacheKey() {
        return "dict_waste_dict_cache";
    }

    /**
     * 判断是否更新缓存
     */
    public boolean checkUpdateCache(String cacheKey, Integer timeout, TimeUnit timeUnit) {
        Object obj = redisCache.getCacheObject(cacheKey);
        if (null == obj) {
            redisCache.setCacheObject("existsKey:" + cacheKey, "yes", timeout, timeUnit);
            return true;
        }
        return false;
    }

}
