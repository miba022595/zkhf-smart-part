package com.zkhf.epmis.platform.utils;

import com.zkhf.epmis.core.constant.CacheConstants;
import com.zkhf.epmis.core.redis.RedisCache;
import com.zkhf.epmis.core.spring.SpringUtils;
import com.zkhf.epmis.core.utils.StringUtils;
import com.zkhf.epmis.platform.base.domain.DictData;

import java.util.*;

/**
 * 字典工具类
 */
public class DictUtils {
    /**
     * 分隔符
     */
    public static final String SEPARATOR = ",";

    /**
     * 设置字典缓存
     *
     * @param key       参数键
     * @param dictDatas 字典数据列表
     */
    public static void setDictCache(String key, List<DictData> dictDatas) {
        SpringUtils.getBean(RedisCache.class).setCacheObject(getCacheKey(key), dictDatas);
    }

    /**
     * 获取字典缓存
     *
     * @param key 参数键
     * @return dictData 字典数据列表
     */
    public static Map<String, DictData> getDictCacheMap(String key) {
        List<DictData> list = getDictCache(key);
        Map<String, DictData> map = new HashMap<>();
        list.forEach( e -> map.put(e.getDictValue(), e));
        return map;
    }

    /**
     * 获取字典缓存
     *
     * @param key 参数键
     * @return dictData 字典数据列表
     */
    public static List<DictData> getDictCache(String key) {
        return SpringUtils.getBean(RedisCache.class).getCacheObject(getCacheKey(key));
    }

    /**
     * 获取字典缓存
     *
     * @param keys 参数键
     * @return dictData 字典数据列表
     */
    public static Map<String, List<DictData>> getDictCaches(List<String> keys) {
        Map<String, List<DictData>> result = new HashMap<>();
        if (keys == null || keys.isEmpty()) {
            return result;
        }
        List<String> cacheKeys = new ArrayList<>();
        Map<String, String> keyMap = new HashMap<>();
        keys.forEach( e -> {
            String cacheKey = getCacheKey(e);
            cacheKeys.add(cacheKey);
            keyMap.put(cacheKey, e);
        });
        Map<String, List<DictData>> objects = SpringUtils.getBean(RedisCache.class).getCacheObjects(cacheKeys);
        objects.forEach((k, v) -> result.put(keyMap.get(k), v));
        return result;
    }

    /**
     * 根据字典类型和字典值获取字典标签
     *
     * @param dictType  字典类型
     * @param dictValue 字典值
     * @return 字典标签
     */
    public static String getDictLabel(String dictType, String dictValue) {
        if (StringUtils.isEmpty(dictValue)) {
            return StringUtils.EMPTY;
        }
        return getDictLabel(dictType, dictValue, SEPARATOR);
    }

    /**
     * 根据字典类型和字典标签获取字典值
     *
     * @param dictType  字典类型
     * @param dictLabel 字典标签
     * @return 字典值
     */
    public static String getDictValue(String dictType, String dictLabel) {
        if (StringUtils.isEmpty(dictLabel)) {
            return StringUtils.EMPTY;
        }
        return getDictValue(dictType, dictLabel, SEPARATOR);
    }

    /**
     * 根据字典类型和字典值获取字典标签
     *
     * @param dictType  字典类型
     * @param dictValue 字典值
     * @param separator 分隔符
     * @return 字典标签
     */
    public static String getDictLabel(String dictType, String dictValue, String separator) {
        StringBuilder propertyString = new StringBuilder();
        List<DictData> datas = getDictCache(dictType);
        if (StringUtils.isNull(datas)) {
            return StringUtils.EMPTY;
        }
        if (StringUtils.containsAny(separator, dictValue)) {
            for (DictData dict : datas) {
                for (String value : dictValue.split(separator)) {
                    if (value.equals(dict.getDictValue())) {
                        propertyString.append(dict.getDictLabel()).append(separator);
                        break;
                    }
                }
            }
        } else {
            for (DictData dict : datas) {
                if (dictValue.equals(dict.getDictValue())) {
                    return dict.getDictLabel();
                }
            }
        }
        return StringUtils.stripEnd(propertyString.toString(), separator);
    }

    /**
     * 根据字典类型和字典标签获取字典值
     *
     * @param dictType  字典类型
     * @param dictLabel 字典标签
     * @param separator 分隔符
     * @return 字典值
     */
    public static String getDictValue(String dictType, String dictLabel, String separator) {
        StringBuilder propertyString = new StringBuilder();
        List<DictData> datas = getDictCache(dictType);
        if (StringUtils.isNull(datas)) {
            return StringUtils.EMPTY;
        }
        if (StringUtils.containsAny(separator, dictLabel)) {
            for (DictData dict : datas) {
                for (String label : dictLabel.split(separator)) {
                    if (label.equals(dict.getDictLabel())) {
                        propertyString.append(dict.getDictValue()).append(separator);
                        break;
                    }
                }
            }
        } else {
            for (DictData dict : datas) {
                if (dictLabel.equals(dict.getDictLabel())) {
                    return dict.getDictValue();
                }
            }
        }
        return StringUtils.stripEnd(propertyString.toString(), separator);
    }

    /**
     * 根据字典类型获取字典所有值
     *
     * @param dictType 字典类型
     * @return 字典值
     */
    public static String getDictValues(String dictType) {
        StringBuilder propertyString = new StringBuilder();
        List<DictData> dataList = getDictCache(dictType);
        if (StringUtils.isNull(dataList)) {
            return StringUtils.EMPTY;
        }
        for (DictData dict : dataList) {
            propertyString.append(dict.getDictValue()).append(SEPARATOR);
        }
        return StringUtils.stripEnd(propertyString.toString(), SEPARATOR);
    }

    /**
     * 根据字典类型获取字典所有标签
     *
     * @param dictType 字典类型
     * @return 字典值
     */
    public static String getDictLabels(String dictType) {
        StringBuilder propertyString = new StringBuilder();
        List<DictData> dataList = getDictCache(dictType);
        if (StringUtils.isNull(dataList)) {
            return StringUtils.EMPTY;
        }
        for (DictData dict : dataList) {
            propertyString.append(dict.getDictLabel()).append(SEPARATOR);
        }
        return StringUtils.stripEnd(propertyString.toString(), SEPARATOR);
    }

    /**
     * 删除指定字典缓存
     *
     * @param key 字典键
     */
    public static void removeDictCache(String key) {
        SpringUtils.getBean(RedisCache.class).deleteObject(getCacheKey(key));
    }

    /**
     * 清空字典缓存
     */
    public static void clearDictCache() {
        Collection<String> keys = SpringUtils.getBean(RedisCache.class).keys(CacheConstants.DICT_CACHE_KEY + "*");
        SpringUtils.getBean(RedisCache.class).deleteObject(keys);
    }

    /**
     * 设置cache key
     *
     * @param configKey 参数键
     * @return 缓存键key
     */
    public static String getCacheKey(String configKey) {
        return CacheConstants.DICT_CACHE_KEY + configKey;
    }

    /**
     * 获取自定义字典缓存
     *
     * @param key 参数键
     * @return dictData 字典数据列表
     */
    public static List<DictData> getDictCustomCache(String key, Long userId) {
        return SpringUtils.getBean(RedisCache.class).getCacheObject(getCustomCacheKey(key, userId));
    }

    /**
     * 设置自定义字典缓存
     *
     * @param key       参数键
     * @param dictData 字典数据列表
     */
    public static void setDictCustomCache(String key, Long userId, List<DictData> dictData) {
        SpringUtils.getBean(RedisCache.class).setCacheObject(getCustomCacheKey(key, userId), dictData);
    }

    /**
     * 设置cache key
     *
     * @param configKey 参数键
     * @return 缓存键key
     */
    public static String getCustomCacheKey(String configKey, Long userId) {
        return CacheConstants.DICT_CUSTOM_CACHE_KEY + configKey + ":" + userId;
    }

}
