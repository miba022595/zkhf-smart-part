package com.zkhf.epmis.process.plc.service.impl;

import com.zkhf.epmis.core.domain.AjaxResult;
import com.zkhf.epmis.core.enums.PlcDataTypeEnum;
import com.zkhf.epmis.core.enums.PlcPointTypeEnum;
import com.zkhf.epmis.core.utils.DateUtils;
import com.zkhf.epmis.core.utils.StringUtils;
import com.zkhf.epmis.process.facade.platform.PlatformFacade;
import com.zkhf.epmis.process.mapper.plc.PlcMapper;
import com.zkhf.epmis.process.plc.domain.PlcInfo;
import com.zkhf.epmis.process.plc.domain.PlcRawData;
import com.zkhf.epmis.process.plc.domain.PlcReq;
import com.zkhf.epmis.process.plc.domain.PointData;
import com.zkhf.epmis.process.plc.service.PlcService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.*;

@Slf4j
@Service
public class PlcServiceImpl implements PlcService {

    private PlcMapper plcMapper;
    @Autowired
    public void setPlcMapper(PlcMapper plcMapper) {
        this.plcMapper = plcMapper;
    }

    private PlatformFacade platformFacade;
    @Autowired
    public void setPlatformFacade(PlatformFacade platformFacade) {
        this.platformFacade = platformFacade;
    }

    @Override
    public AjaxResult getRealTimeData(PlcReq req) {
        List<PointData> dataList = new ArrayList<>();
        AjaxResult result = AjaxResult.success(dataList);
        result.put("point", new ArrayList<>());
        // 参数校验
        if (null == req || StringUtils.isEmpty(req.getEntCode())) {
            return result;
        }
        // 查询点位列表
        Map<String, List<PlcInfo>> plcMap = new HashMap<>();
        List<PlcInfo> pointList = selectPlcPoint(req, plcMap);
        if (plcMap.isEmpty()) {
            return result;
        }
        result.put("point", pointList);
        // 批量查询最新数据
        List<PlcRawData> rawDataList = plcMapper.selectRealTimeData(plcMap.keySet());
        // 将原始数据组装结果
        for (PlcRawData rawData : rawDataList) {
            List<PlcInfo> plcList = plcMap.get(rawData.getType());
            if (plcList != null && !plcList.isEmpty()) {
                convertToVO(dataList, plcList, rawData);
            }
        }
        return result;
    }

    @Override
    public AjaxResult getHistoryData(PlcReq req) {
        List<List<PointData>> dataList = new ArrayList<>();
        AjaxResult result = AjaxResult.success(dataList);
        result.put("point", new ArrayList<>());
        if (null == req || StringUtils.isEmpty(req.getEntCode())
                || null == req.getStart() || null == req.getEnd()) {
            return result;
        }
        LocalDate now = LocalDate.now();
        // 自动处理未来时间
        if (req.getEnd().isAfter(now)) {
            req.setEnd(now);
        }
        // 开始时间必须在当前时间之前
        if (req.getStart().isAfter(now)) {
            return result;
        }
        // 开始时间不能大于结束时间
        if (req.getStart().isAfter(req.getEnd())) {
            return result;
        }
        // 查询点位列表
        Map<String, List<PlcInfo>> plcMap = new HashMap<>();
        List<PlcInfo> pointList = selectPlcPoint(req, plcMap);
        result.put("point", pointList);
        if (plcMap.isEmpty()) {
            return result;
        }
        Set<String> types = plcMap.keySet();

        // 根据时间范围获取需要查询的分表（按时间倒序）
        List<String> suffixList = new ArrayList<>();
        List<String> allSuffixList = getSuffixList(req.getStart(), req.getEnd());
        List<String> tableNames = plcMapper.selectTableNames("t_plc_raw_data_%");
        if (null != tableNames && !tableNames.isEmpty() && !allSuffixList.isEmpty()) {
            for (String suffix : allSuffixList) {
                if (tableNames.contains("t_plc_raw_data_" + suffix)) {
                    suffixList.add(suffix);
                }
            }
        }
        if (suffixList.isEmpty()) {
            return result;
        }

        // 分页参数
        String firstId = req.getFirstId(), lastId = req.getLastId();
        PlcRawData first = null, last = null;
        boolean down = req.isDown();
        int pageSize = req.getPageSize() != null ? req.getPageSize() : 20;
        int querySize = pageSize + 1; // 多查一条用于判断是否有更多

        // 转换为字符串时间
        String startTimeStr = req.getStart().format(DateUtils.yy_m_d) + " 00:00:00";
        String endTimeStr = req.getEnd().format(DateUtils.yy_m_d) + " 23:59:59";

        // 判断是不是首页
        boolean isFirstPage = (firstId == null && lastId == null);

        // 存储多查的那条数据
        PlcRawData extraData = null;
        boolean hasPrev = false;
        boolean hasNext;

        // 确定起始表索引（根据时间定位）
        int startIndex = 0;
        if (!isFirstPage) {
            LocalDate reference = down ? req.getLastReport() : req.getFirstReport();
            if (reference != null) {
                String referenceMonth = reference.format(DateUtils.yym);
                for (int i = 0; i < suffixList.size(); i++) {
                    if (suffixList.get(i).equals(referenceMonth)) {
                        startIndex = i;
                        break;
                    }
                }
            }
        }
        // 逐表查询历史数据
        if (isFirstPage || down) {
            // 第一页：从最新的表开始查
            for (int i = startIndex; i < suffixList.size(); i++) {
                int need = querySize - dataList.size();
                if (need <= 0) {
                    break;
                }
                List<PlcRawData> rawDataList;
                if (i == startIndex) {
                    // 当前表：使用lastId游标继续查，首页查询时，startIndex为0，lastId为null
                    rawDataList = plcMapper.selectHistoryDataNext(suffixList.get(i), types, startTimeStr, endTimeStr, lastId, need);
                } else {
                    // 新表：从头查（按时间倒序）
                    rawDataList = plcMapper.selectHistoryDataNext(suffixList.get(i), types, startTimeStr, endTimeStr, null, need);
                }
                // 组装数据
                for (PlcRawData rawData : rawDataList) {
                    List<PlcInfo> plcList = plcMap.get(rawData.getType());
                    if (dataList.size() < pageSize) {
                        List<PointData> subList = new ArrayList<>();
                        if (plcList != null && !plcList.isEmpty()) {
                            convertToVO(subList, plcList, rawData);
                        }
                        if (null == first) {
                            first = rawData;
                        }
                        last = rawData;
                        dataList.add(subList);
                    } else {
                        extraData = rawData;
                        break;
                    }
                }
                if (extraData != null) {
                    break;
                }
            }
            // 第一页：有extraData表示有下一页
            hasNext = (extraData != null);
            // 上一页：非第一页肯定有上一页
            if (!isFirstPage) {
                hasPrev = true;
            }
        } else {
            // 上一页：查询比 firstId 更新的数据
            for (int i = startIndex; i >= 0; i--) {
                int need = querySize - dataList.size();
                if (need <= 0) {
                    break;
                }
                List<PlcRawData> rawDataList;
                if (i == startIndex) {
                    // 当前表：使用firstId游标查更新的数据
                    rawDataList = plcMapper.selectHistoryDataPrev(suffixList.get(i), types, startTimeStr, endTimeStr, firstId, need);
                } else {
                    // 更早的表：从最新的数据开始查（按时间顺序）
                    rawDataList = plcMapper.selectHistoryDataPrev(suffixList.get(i), types, startTimeStr, endTimeStr, null, need);
                }
                // 组装数据
                for (PlcRawData rawData : rawDataList) {
                    List<PlcInfo> plcList = plcMap.get(rawData.getType());
                    if (dataList.size() < pageSize) {
                        List<PointData> subList = new ArrayList<>();
                        if (plcList != null && !plcList.isEmpty()) {
                            convertToVO(subList, plcList, rawData);
                        }
                        if (null == first) {
                            first = rawData;
                        }
                        last = rawData;
                        dataList.add(subList);
                    } else {
                        extraData = rawData;
                        break;
                    }
                }
                if (extraData != null) {
                    break;
                }
            }
            // 上一页的数据需要反转，保持时间倒序
            Collections.reverse(dataList);
            // 交换顺序
            PlcRawData temp = first;
            first = last;
            last = temp;
            // 上一页：有extraData表示还有上一页
            hasPrev = (extraData != null);
            // 下一页：非第一页肯定有下一页
            hasNext = true;
        }
        // 返回结果
        if (null != first) {
            result.put("firstId", first.getId());
            if (null != first.getReportTime()) {
                result.put("firstReport", first.getReportTime().format(DateUtils.yy_m_d));
            } else {
                result.put("firstReport", null);
            }
        } else {
            result.put("firstId", null);
        }
        if (null != last) {
            result.put("lastId", last.getId());
            if (null != last.getReportTime()) {
                result.put("lastReport", last.getReportTime().format(DateUtils.yy_m_d));
            } else {
                result.put("lastReport", null);
            }
        } else {
            result.put("lastId", null);
        }
        result.put("hasPrev", hasPrev);
        result.put("hasNext", hasNext);

        return result;
    }

    /**
     * 根据时间范围获取需要查询的分表后缀列表（按时间倒序）
     */
    private List<String> getSuffixList(LocalDate startTime, LocalDate endTime) {
        List<String> suffixList = new ArrayList<>();
        LocalDate current = endTime;

        // 从结束时间往前推到开始时间
        while (!current.isBefore(startTime)) {
            String suffix = current.format(DateUtils.yym);
            suffixList.add(suffix);
            current = current.minusMonths(1);
        }

        return suffixList;
    }

    /**
     * 获取点位信息
     */
    private List<PlcInfo> selectPlcPoint(PlcReq req, Map<String, List<PlcInfo>> plcMap) {
        // 查询点位列表
        List<PlcInfo> pointList = platformFacade.plcPointList(req.getEntCode());
        if (null == pointList || pointList.isEmpty()) {
            return pointList;
        }

        // 按unitId和fnCode分组，便于批量查询
        for (PlcInfo point : pointList) {
            point.setPointTypeDesc(PlcPointTypeEnum.getDescByType(point.getPointType()));
            point.setDataTypeDesc(PlcDataTypeEnum.getDescByType(point.getDataType()));
            if (point.getRegisterAddress() == null) {
                continue;
            }
            String key = point.getUnitId() + "_" + getFnCodeByPointType(point.getPointType());
            if (!plcMap.containsKey(key)) {
                plcMap.put(key, new ArrayList<>());
            }
            plcMap.get(key).add(point);
        }
        return pointList;
    }

    /**
     * 根据点位类型获取对应的功能码
     */
    private Integer getFnCodeByPointType(Integer pointType) {
        switch (pointType) {
            case 1: // DI数字输入
            case 2: // DO数字输出
                return 15; // 写多个线圈，或者根据实际情况返回 5 或 15
            case 3: // AI模拟输入
            case 4: // AO模拟输出
            case 5: // 寄存器
                return 16; // 写多个寄存器，或者根据实际情况返回 6 或 16
            default:
                return 16;
        }
    }

    /**
     * 将点位配置和原始数据转换为VO
     */
    private void convertToVO(List<PointData> dataList, List<PlcInfo> pointList, PlcRawData rawData) {
        for (PlcInfo point : pointList) {
            PointData data = new PointData();
            data.setPointId(point.getId());
            data.setReportTime(rawData.getReportTime());

            // 解析该点位对应的值
            String pointValueHex;
            try {
                pointValueHex = parseHexData(rawData, point);
                data.setRawValueHex(pointValueHex);
            } catch (Exception e) {
                log.error("解析失败", e);
                continue;
            }
            // 转换值
            if (pointValueHex != null) {
                Object converted = convertValue(pointValueHex, point);
                data.setConvertedValue(converted);
                data.setDisplayValue(formatDisplayValue(converted, point));
            }
            dataList.add(data);
        }
    }

    /**
     * 解析Modbus报文，提取指定点位的原始十六进制数据
     * @param raw 原始数据
     * @param point 点位配置
     * @return 该点位的十六进制值
     */
    private String parseHexData(PlcRawData raw, PlcInfo point) {
        if (raw == null || StringUtils.isEmpty(raw.getData())) {
            return null;
        }
        String hexData = raw.getData();
        // 格式: unitId_fnCode，例如 "1_6"
        String type = raw.getType();

        // 解析type获取功能码
        int fnCode = Integer.parseInt(type.split("_")[1]);
        // 报文长度检查
        if (hexData.length() < 4) {
            return null;
        }

        // 根据功能码和数据类型解析
        // 转换为协议地址（减去基数）
        int protocolAddress = point.getRegisterAddress() - PlcPointTypeEnum.getCardinalNumber(point.getPointType());
        int dataType = point.getDataType();

        // 计算每个点位占用的字节数
        int bytesPerPoint = getBytesPerDataType(dataType);
        int hexCharsPerPoint = bytesPerPoint * 2; // 每个字节2个十六进制字符

        /* 根据功能码处理
         * 0x05 写单个线圈   起始地址(2) | 值(2)
         * 0x06 写单个寄存器  起始地址(2) | 值(2)
         * 0x10 写多个寄存器  起始地址(2) | 寄存器数量(2) | 字节计数(1) | 寄存器值(N)
         * 0x0F 写多个线圈   起始地址(2) | 线圈数量(2) | 字节计数(1) | 线圈值(⌈数量/8⌉)
         */
        // 起始地址在前2 字节，数据值从第3 字节开始，
        int startAddress = Integer.parseInt(hexData.substring(0, 4), 16);
        // 计算偏移量（寄存器偏移）
        int registerOffset = protocolAddress - startAddress;
        if (registerOffset < 0) {
            return null; // 请求的地址不在这个报文中
        }
        if (0x05 == fnCode || 0x06 == fnCode) {
            if (protocolAddress == startAddress && hexData.length() >= 8) {
                return hexData.substring(4, 8);
            }
        } else if (0x0F == fnCode) {
            // 0x0F 写多个线圈: 起始地址(2) | 线圈数量(2) | 字节计数(1) | 线圈值(N)
            if (hexData.length() < 10) {
                return null;
            }

            int quantity = Integer.parseInt(hexData.substring(4, 8), 16);

            // 检查地址是否在范围内
            if (protocolAddress >= startAddress && protocolAddress < startAddress + quantity) {
                int byteCount = Integer.parseInt(hexData.substring(8, 10), 16);
                String dataBytes = hexData.substring(10);

                // 检查数据长度
                if (dataBytes.length() >= byteCount * 2) {
                    // 从字节数据中提取指定位
                    return extractBitValueFromBytes(dataBytes, registerOffset);
                }
            }
        } else if (fnCode == 0x10) {
            // 0x10 写多个寄存器: 起始地址(2) | 寄存器数量(2) | 字节计数(1) | 寄存器值(N)
            if (hexData.length() < 10) {
                return null;
            }

            int quantity = Integer.parseInt(hexData.substring(4, 8), 16);

            // 检查地址是否在范围内
            if (protocolAddress >= startAddress && protocolAddress < startAddress + quantity) {
                String dataBytes = hexData.substring(10);

                // 计算起始位置
                int startPos = registerOffset * hexCharsPerPoint;

                // 检查数据长度
                if (startPos + hexCharsPerPoint <= dataBytes.length()) {
                    return dataBytes.substring(startPos, startPos + hexCharsPerPoint);
                }
            }
        }
        return null;
    }

    /**
     * 从字节数据中提取指定位的值（用于写多个线圈 0x0F）
     *
     * @param dataBytes 十六进制字符串格式的字节数据
     * @param registerOffset 寄存器偏移量（线圈地址偏移）
     * @return 位的十六进制值（"00" 或 "01"）
     */
    private String extractBitValueFromBytes(String dataBytes, int registerOffset) {
        if (dataBytes == null || dataBytes.isEmpty()) {
            return null;
        }

        // 计算字节索引和位索引
        int byteIndex = registerOffset / 8;
        int bitIndex = registerOffset % 8;

        // 检查数据长度是否足够
        if (byteIndex * 2 + 2 > dataBytes.length()) {
            return null;
        }

        // 获取对应的字节
        String byteHex = dataBytes.substring(byteIndex * 2, byteIndex * 2 + 2);
        int byteValue = Integer.parseInt(byteHex, 16);

        // Modbus协议中，线圈数据是低位在前
        int bitValue = (byteValue >> bitIndex) & 0x01;

        return String.format("%02X", bitValue);
    }

    /**
     * 获取数据类型占用的字节数
     */
    private int getBytesPerDataType(int dataType) {
        if (PlcDataTypeEnum.BOOL.type.equals(dataType)) {
            return 1;
        } else if (PlcDataTypeEnum.INT16.type.equals(dataType)) {
            return 2;
        } else if (PlcDataTypeEnum.UINT16.type.equals(dataType)) {
            return 2;
        } else if (PlcDataTypeEnum.INT32.type.equals(dataType)) {
            return 4;
        } else if (PlcDataTypeEnum.UINT32.type.equals(dataType)) {
            return 4;
        } else if (PlcDataTypeEnum.FLOAT32.type.equals(dataType)) {
            return 4;
        } else if (PlcDataTypeEnum.FLOAT64.type.equals(dataType)) {
            return 8;
        } else {
            return 2;
        }
    }

    /**
     * 转换值
     */
    private Object convertValue(String hexValue, PlcInfo point) {
        if (hexValue == null || hexValue.isEmpty()) {
            return null;
        }
        try {
            // 根据数据类型进行转换
            if (PlcDataTypeEnum.BOOL.type.equals(point.getDataType())) {
                int boolVal = Integer.parseInt(hexValue, 16);
                return boolVal != 0;
            } else if (PlcDataTypeEnum.INT16.type.equals(point.getDataType())) {
                short shortVal = (short) Integer.parseInt(hexValue, 16);
                return applyCoefficient(shortVal, point.getCoefficient());
            } else if (PlcDataTypeEnum.UINT16.type.equals(point.getDataType())) {
                int uintVal = Integer.parseInt(hexValue, 16);
                return applyCoefficient(uintVal, point.getCoefficient());
            } else if (PlcDataTypeEnum.INT32.type.equals(point.getDataType())) {
                long longVal = Long.parseLong(hexValue, 16);
                return applyCoefficient(longVal, point.getCoefficient());
            } else if (PlcDataTypeEnum.UINT32.type.equals(point.getDataType())) {
                long longVal = Long.parseLong(hexValue, 16);
                return applyCoefficient(longVal, point.getCoefficient());
            } else if (PlcDataTypeEnum.FLOAT32.type.equals(point.getDataType())) {
                long longVal = Long.parseLong(hexValue, 16);
                return applyCoefficient(longVal, point.getCoefficient());
            } else {
                return hexValue;
            }
        } catch (NumberFormatException e) {
            log.error("转换失败: hexValue={}, point={}", hexValue, point.getPointName(), e);
            return null;
        }
    }

    /**
     * 应用系数
     */
    private BigDecimal applyCoefficient(Number value, BigDecimal coefficient) {
        if (coefficient == null) {
            coefficient = BigDecimal.ONE;
        }
        return BigDecimal.valueOf(value.doubleValue()).multiply(coefficient);
    }

    /**
     * 格式化显示值
     */
    private String formatDisplayValue(Object value, PlcInfo point) {
        if (value == null) {
            return "--";
        }

        if (point.getDataType() == 1) { // bool
            return (Boolean) value ? "ON" : "OFF";
        }

        if (value instanceof BigDecimal) {
            BigDecimal bd = (BigDecimal) value;
            if (point.getPrecision() != null) {
                return bd.setScale(point.getPrecision(), RoundingMode.HALF_UP).toString();
            }
            // 去掉末尾多余的0
            return bd.stripTrailingZeros().toPlainString();
        }

        return value.toString();
    }
}
