package com.zkhf.epmis.process.onlineMonitoring.service.impl;

import cn.hutool.core.map.MapUtil;
import com.alibaba.fastjson2.JSONObject;
import com.zkhf.epmis.core.domain.AjaxResult;
import com.zkhf.epmis.core.domain.HeadInfo;
import com.zkhf.epmis.core.domain.PollHead;
import com.zkhf.epmis.core.enums.*;
import com.zkhf.epmis.core.utils.*;
import com.zkhf.epmis.process.alarm.domain.DurAlarmInfo;
import com.zkhf.epmis.process.base.domain.OutPutInfo;
import com.zkhf.epmis.process.base.utils.ExcelUtils;
import com.zkhf.epmis.process.base.utils.RedisCacheUtils;
import com.zkhf.epmis.process.facade.platform.PlatformFacade;
import com.zkhf.epmis.process.global.GVarContainer;
import com.zkhf.epmis.process.mapper.onlineMonitoring.OutPutOnlineMapper;
import com.zkhf.epmis.process.mqtt.domain.RealCacheData;
import com.zkhf.epmis.process.onlineMonitoring.domain.*;
import com.zkhf.epmis.process.onlineMonitoring.service.OutPutOnlineService;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.OutputStream;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalAdjusters;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
public class OutPutOnlineServiceImpl implements OutPutOnlineService {

    private final BigDecimal b0 = BigDecimal.ZERO;
    private final BigDecimal b1 = new BigDecimal(1);

    private RedisCacheUtils redisCacheUtils;
    @Autowired
    public void setRedisCacheUtils(RedisCacheUtils redisCacheUtils) {
        this.redisCacheUtils = redisCacheUtils;
    }

    private OutPutOnlineMapper outPutOnlineMapper;
    @Autowired
    public void setOutPutOnlineMapper(OutPutOnlineMapper outPutOnlineMapper) {
        this.outPutOnlineMapper = outPutOnlineMapper;
    }

    private PlatformFacade platformFacade;
    @Autowired
    public void setPlatformFacade(PlatformFacade platformFacade) {
        this.platformFacade = platformFacade;
    }

    @Override
    public AjaxResult realChart(RealDataReq req) {
        List<RealData> realList = new ArrayList<>();
        Map<String, List<PollHead>> headMap = new HashMap<>();
        log.debug("实时一览-图表: {}", req);
        // 实时数据获取
        getRealData(req, realList, headMap);
        AjaxResult result = AjaxResult.success(realList);
        result.put("headMap", headMap);
        return result;
    }

    @Override
    public AjaxResult realList(RealDataReq req) {
        List<RealData> realList = new ArrayList<>();
        Map<String, List<PollHead>> headMap = new HashMap<>();
        log.debug("实时一览-列表: {}", req);
        // 实时数据获取，包含表头
        getRealData(req, realList, headMap);
        AjaxResult result = AjaxResult.success(realList);
        if (realList.isEmpty()) {
            return result;
        }
        List<PollHead> headList = new ArrayList<>();
        result.put("headList", headList);
        Set<String> keys = new HashSet<>();
        Map<String, PollHead> hMap = new HashMap<>();
        for (List<PollHead> hl : headMap.values()) {
            for (PollHead h : hl) {
                if (StringUtils.isEmpty(h.getPollCode())) {
                    continue;
                }
                PollHead oh = hMap.get(h.getPollCode());
                if (null != oh) {
                    List<HeadInfo> il = h.getHeadList();
                    for (HeadInfo i : il) {
                        String key = h.getPollCode() + "_" + i.getName();
                        if (keys.add(key)) {
                            oh.getHeadList().add(i);
                        }
                    }
                } else {
                    for (HeadInfo i : h.getHeadList()) {
                        keys.add(h.getPollCode() + "_" + i.getName());
                    }
                    hMap.put(h.getPollCode(), h);
                    headList.add(h);
                }
            }
        }
        return result;
    }

    private void getRealData(RealDataReq req, List<RealData> realList, Map<String, List<PollHead>> headMap) {
        // 获取权限下的企业和排口信息
        List<OutPutInfo> outPutList = redisCacheUtils.getAllOutPutList();
        if (null == outPutList || outPutList.isEmpty()) {
            return;
        }
        List<String> entCodes = null;
        if (GVarContainer.isNotAdmin()) {
            entCodes = GVarContainer.getEntCodes();
            if (entCodes.isEmpty()) {
                return;
            }
        }
        List<String> outIds = new ArrayList<>();
        Map<String, OutPutInfo> outMap = new HashMap<>();
        for (OutPutInfo info : outPutList) {
            if (StringUtils.isEmpty(info.getEntCode())) {
                continue;
            }
            if (null != req && StringUtils.isNotEmpty(req.getEntCode()) && !req.getEntCode().equals(info.getEntCode())) {
                continue;
            }
            if (null != entCodes && !entCodes.contains(info.getEntCode())) {
                continue;
            }
            if (null != req && StringUtils.isNotEmpty(req.getOutPutId()) && !req.getOutPutId().equals(info.getOutPutId())) {
                continue;
            }
            if (null != req && null != req.getOutPutType() && !req.getOutPutType().equals(info.getOutPutType())) {
                continue;
            }
            if (null != req && StringUtils.isNotEmpty(req.getRegion())) {// 地区匹配
                if (StringUtils.isEmpty(info.getRegion())) {
                    continue;
                }
                if (!info.getRegion().equals(req.getRegion()) && !info.getRegion().startsWith(req.getRegion() + ",")) {
                    continue;
                }
            }
            outIds.add(info.getOutPutId());
            outMap.put(info.getOutPutId(), info);
        }
        // 排口动态表头获取
        Map<String, List<PollHead>> autoHead = null;
        if (!outIds.isEmpty()) {
            autoHead = platformFacade.multipleAutoHeads(outIds, DataEnum.real.name());
        }
        if (null == autoHead || autoHead.isEmpty()) {
            return;
        }
        Map<String, RealCacheData> realData = redisCacheUtils.getAllRealDataList();
        for (String outPutId : autoHead.keySet()) {
            RealCacheData realCacheData = realData.get(outPutId);
            if (null == realCacheData) {
                continue;
            }
            OutPutInfo info = outMap.get(outPutId);
            if (null == info) {
                continue;
            }
            RealData data = new RealData();
            data.setOutPutId(outPutId);
            data.setOutPutCode(info.getOutPutCode());
            data.setOutPutName(info.getOutPutName());
            data.setEntCode(info.getEntCode());
            data.setEntName(info.getEntName());
            data.setData(realCacheData);
            realList.add(data);
            headMap.put(outPutId, autoHead.get(outPutId));
        }
    }

    @Override
    public AjaxResult selectDataList(OutPutOnlineReq req) {
        log.info("排口在线监测列表查询: {}", req);
        AjaxResult result = AjaxResult.success(new ArrayList<>());
        // 请求数据校验
        if (checkDataReq(req)) {
            return result;
        }
        // 判断是否存在表
        int size = outPutOnlineMapper.checkTableExistsByName(req.getTableName());
        if (size < 1) {
            return result;
        }
        long count = outPutOnlineMapper.selectDataCount(req);
        result.put("total", count);
        List<OutPutOnlineData> list;
        if (count > 0) {
            req.setPageSize(PageUtils.getPageSize(10));
            // 跳页处理
            skipPageProcess(req, count);
            list = outPutOnlineMapper.selectData(req);
            if (list != null && !list.isEmpty()) {
                // 查询报警信息
                LocalDateTime start = list.get(0).getMonitorDate();
                LocalDateTime end = start;
                for (OutPutOnlineData d : list) {
                    if (start.isAfter(d.getMonitorDate())) {
                        start = d.getMonitorDate();
                    }
                    if (end.isBefore(d.getMonitorDate())) {
                        end = d.getMonitorDate();
                    }
                }
                List<DurAlarmInfo> alarmList = outPutOnlineMapper.selectDataAlarmList(req.getOutPutId(),
                        start.truncatedTo(ChronoUnit.SECONDS), end.truncatedTo(ChronoUnit.SECONDS),
                        req.getDataType());
                // 报警信息设置
                list.forEach( e -> {
                    if (StringUtils.isNotEmpty(e.getDataInfoStr())) {
                        e.setDataMap(JSONObject.parseObject(e.getDataInfoStr()));
                        for (DurAlarmInfo alarm : alarmList) {
                            // 判断数据的时间点是否命中报警时间范围
                            if (e.getMonitorDate().isBefore(alarm.getStartTime())) {
                                continue;
                            }
                            if (null != alarm.getEndTime() && e.getMonitorDate().isAfter(alarm.getEndTime())) {
                                continue;
                            }
                            // 报警编码转为报警等级
                            int level = AlarmDetailTypeEnum.getNameByCode(alarm.getAlarmType()).level;
                            Object obj = e.getDataMap().get(alarm.getPollutantCode());
                            if (obj instanceof Map) {
                                @SuppressWarnings("unchecked")
                                Map<String, Object> subData = (Map<String, Object>)obj;
                                Integer oldLevel = MapUtil.getInt(subData, "alarm");
                                if (null == oldLevel || oldLevel > level) {
                                    subData.put("alarm", level);
                                }
                            }
                        }
                    }
                });
            }
        } else {
            list = new ArrayList<>();
        }
        result.put("data", list);
        return result;
    }

    @Override
    public void export(OutPutOnlineReq req, HttpServletResponse response) {
        log.info("导出排口在线监测列表: {}", req);
        OutputStream outputStream = null;
        try {
            XSSFWorkbook workbook = ExcelUtils.getSheetAt("排口在线监测数据模板.xlsx");
            if (workbook == null) {
                return;
            }
            OutPutInfo info = redisCacheUtils.getAllOutPutById(req.getOutPutId());
            // 请求数据校验
            boolean checkDataReq = checkDataReq(req);
            Sheet sheet = workbook.getSheetAt(0);
            CellStyle style = CellUtils.getCellStyle(workbook, sheet, 5, 1);
            CellStyle styleN3 = CellUtils.getCellStyle(workbook, sheet, 5, 2, 3);
            // 填充动态表头
            List<String> keyList = null;
            Map<String, CellStyle> placesMap = new HashMap<>();
            if (!checkDataReq) {
                keyList = fillExportHead(workbook, sheet, req, info, placesMap);
            }
            // 获取数据
            List<OutPutOnlineData> list = null;
            if (null != keyList && !keyList.isEmpty()) {
                // 判断是否存在表
                int tableExists = outPutOnlineMapper.checkTableExistsByName(req.getTableName());
                if (tableExists > 0) {
                    list = outPutOnlineMapper.selectData(req);
                }
            }
            // 插入数据
            int rowNm = 5;
            Row row;
            Cell cell;
            if (null != list && !list.isEmpty()) {
                int index = 1;
                // 行移动（获取单元格样式之后）
                CellUtils.shiftRows(sheet, rowNm, list.size());
                for (OutPutOnlineData data : list) {
                    row = sheet.createRow(rowNm++);

                    int cellIndex = 0;
                    // 序号
                    cell = CellUtils.getCell(row, cellIndex++, style);
                    cell.setCellValue(index++);
                    // 时间
                    cell = CellUtils.getCell(row, cellIndex++, style);
                    cell.setCellValue(data.getMonitorTime());

                    // 填充数据
                    setCellBigDecimal(keyList, row, cellIndex, styleN3, JSONObject.parseObject(data.getDataInfoStr()), placesMap);
                }
            }
            // 设置响应信息
            response.setContentType(MimeTypeUtils.EXCEL_XLSX);
            response.setHeader("Content-Disposition", "attachment;filename*=UTF-8''" + URLEncoder.encode(getExportFileName(req, info), StandardCharsets.UTF_8));
            outputStream = response.getOutputStream();
            workbook.write(outputStream);
        } catch (Exception e) {
            log.error("按模板导出文件失败", e);
        } finally {
            if (outputStream != null) {
                try {
                    outputStream.close();
                } catch (IOException e) {
                    log.error("close err", e);
                }
            }
        }
    }

    private List<String> fillExportHead(XSSFWorkbook workbook, Sheet sheet, OutPutOnlineReq req, OutPutInfo info, Map<String, CellStyle> placesMap) {
        List<String> keyList = new ArrayList<>();
        // 通过动态表头构建响应数据
        List<Map<String, Object>> autoHead = platformFacade.getAutoHead(req.getOutPutId(), req.getDataEnum());
        if (null == autoHead || autoHead.isEmpty()) {
            return keyList;
        }
        // 计算需要新增的总列数
        int totalNewColumns = 0;
        Map<Integer, CellStyle> styleMap = new HashMap<>();
        for (Map<String, Object> head : autoHead) {
            String pollutantCode = MapUtil.getStr(head, "pollutantCode");
            Integer decimalPlaces = MapUtil.getInt(head, "decimalPlaces");
            if (StringUtils.isNotEmpty(pollutantCode) && null != decimalPlaces && decimalPlaces >= 0) {
                if (!styleMap.containsKey(decimalPlaces)) {
                    styleMap.put(decimalPlaces, CellUtils.getCellStyle(workbook, sheet, 5, 2, decimalPlaces));
                }
                placesMap.put(pollutantCode, styleMap.get(decimalPlaces));
            }
            Object headList = head.get("headList");
            if (headList instanceof List) {
                @SuppressWarnings("unchecked")
                List<Map<String, String>> list = (List<Map<String, String>>) headList;
                for (Map<String, String> item : list) {
                    String name = MapUtil.getStr(item, "name");
                    if (DataEnum.real.name().equals(req.getDataEnum())) {
                        // 实时数据查看实测值值、折算实测值
                        if (DataFactorEnum.rtd.code.equals(name)
                                || DataFactorEnum.zsRtd.code.equals(name)) {
                            totalNewColumns++;
                        }
                    } else {
                        // 非实时数据不查看实测值
                        if (!DataFactorEnum.rtd.code.equals(name)
                                && !DataFactorEnum.zsRtd.code.equals(name)) {
                            totalNewColumns++;
                        }
                    }
                }
            }
        }
        if (totalNewColumns < 1) {
            return keyList;
        }
        CellStyle firstStyle = CellUtils.getCellStyle(workbook, sheet, 0, 0);
        CellStyle infoStyle = CellUtils.getCellStyle(workbook, sheet, 1, 1);
        infoStyle.setAlignment(HorizontalAlignment.LEFT); // 改为左对齐
        CellStyle titleStyle = CellUtils.getCellStyle(workbook, sheet, 3, 2);
        CellStyle headStyle = CellUtils.getCellStyle(workbook, sheet, 4, 2);

        // 模板文件去除合并，代码里进行合并
        // 列的索引
        int column = 2;
        // 行的索引
        int rowNm = 0;
        // 移动从 lastColumn 开始（获取单元格样式之后，防止获取不到样式）
        sheet.shiftColumns(column, column, totalNewColumns - 1);

        // 获取现有的行
        Row row = sheet.getRow(rowNm);
        // 设置首行的样式
        Cell cell = row.getCell(0);
        cell.setCellValue(getTitleByTypeExport(req, info));
        cell.setCellStyle(firstStyle);
        // 设置空格样式
        setDefaultStyle(row, firstStyle, totalNewColumns + column, 1);
        // 合并首行  (totalNewColumns - 1) + column
        sheet.addMergedRegion(new CellRangeAddress(rowNm, rowNm, 0, totalNewColumns + 1));
        rowNm++;
        // 企业名称
        row = sheet.getRow(rowNm);
        cell = CellUtils.getCell(row, 1, infoStyle);
        if (StringUtils.isNotEmpty(info.getEntName())) {
            cell.setCellValue(info.getEntName());
        }
        // 设置空格样式
        setDefaultStyle(row, infoStyle, totalNewColumns + column, 2);
        // 合并  (totalNewColumns - 1) + column
        sheet.addMergedRegion(new CellRangeAddress(rowNm, rowNm, 1, totalNewColumns + 1));
        rowNm++;
        // 排口名称
        row = sheet.getRow(rowNm);
        cell = CellUtils.getCell(row, 1, infoStyle);
        if (StringUtils.isNotEmpty(info.getOutPutName())) {
            cell.setCellValue(info.getOutPutName());
        }
        // 设置空格样式
        setDefaultStyle(row, infoStyle, totalNewColumns + column, 2);
        sheet.addMergedRegion(new CellRangeAddress(rowNm, rowNm, 1, totalNewColumns + 1));
        rowNm++;

        // 标题行
        Row titleRow = sheet.getRow(rowNm);
        rowNm++;
        Row headerRow = sheet.getRow(rowNm);
        List<String> headerContents = new ArrayList<>();
        for (Map<String, Object> head : autoHead) {
            Object headList = head.get("headList");
            if (!(headList instanceof List)) {
                continue;
            }
            @SuppressWarnings("unchecked")
            List<Map<String, String>> list = (List<Map<String, String>>) headList;
            if (list.isEmpty()) {
                continue;
            }
            String pollutantCode = MapUtil.getStr(head, "pollutantCode");
            String pollutantNameCn = MapUtil.getStr(head, "pollutantNameCn");
            String pollutantUnitEn = MapUtil.getStr(head, "pollutantUnitEn");
            String unitPfEn = MapUtil.getStr(head, "unitPfEn");
            int size = 0;
            for (Map<String, String> item : list) {
                String name = MapUtil.getStr(item, "name");
                String desc = MapUtil.getStr(item, "desc");

                if (DataEnum.real.name().equals(req.getDataEnum())) {
                    // 实时数据查看实测值值、折算实测值
                    if (!DataFactorEnum.rtd.code.equals(name)
                            && !DataFactorEnum.zsRtd.code.equals(name)) {
                        continue;
                    }
                } else {
                    // 非实时数据不查看实测值
                    if (DataFactorEnum.rtd.code.equals(name)
                            || DataFactorEnum.zsRtd.code.equals(name)) {
                        continue;
                    }
                }
                String displayDesc = desc;
                if ("cou".equals(name)) {
                    // 使用排放量单位
                    if (unitPfEn != null && !unitPfEn.isEmpty()) {
                        displayDesc = desc + "(" + unitPfEn + ")";
                    }
                } else {
                    // 使用排放单位
                    if (pollutantUnitEn != null && !pollutantUnitEn.isEmpty()) {
                        displayDesc = desc + "(" + pollutantUnitEn + ")";
                    }
                }
                keyList.add(pollutantCode + "_" + name);
                // 现在这些单元格是通过shift创建的，会保留样式
                Cell descCell = CellUtils.getCell(headerRow, column + size, headStyle);
                descCell.setCellValue(displayDesc);
                headerContents.add(displayDesc);
                size++;
            }
            if (size < 1) {
                continue;
            }
            // 设置第一行表头
            Cell titleCell = CellUtils.getCell(titleRow, column, titleStyle);
            titleCell.setCellValue(pollutantNameCn);

            // 合并第一行单元格
            if (size > 1) {
                sheet.addMergedRegion(new CellRangeAddress(rowNm - 1,rowNm - 1, column,column + size - 1));
            }
            column += size;
        }
        // 调整新扩展列的列宽
        if (headerContents.isEmpty()) {
            return keyList;
        }
        for (int i = 0; i < headerContents.size(); i++) {
            String content = headerContents.get(i);
            int byteLength = content.getBytes(StandardCharsets.UTF_8).length;
            int columnWidth = (byteLength + 2) * 256;

            // 根据内容长度智能限制
            if (byteLength <= 8) {
                columnWidth = Math.max(columnWidth, 3500);  // 短文本最小宽度
            } else if (byteLength >= 40) {
                columnWidth = Math.min(columnWidth, 10000); // 长文本最大宽度
            }
            sheet.setColumnWidth(i + 2, columnWidth);
        }
        return keyList;
    }

    private static String getExportFileName(OutPutOnlineReq req, OutPutInfo info) {
        if (null != info) {
            return info.getOutPutName() + getTypeByDataEnum(req) + ".xlsx";
        } else {
            return getTypeByDataEnum(req) + ".xlsx";
        }
    }

    private static String getTitleByTypeExport(OutPutOnlineReq req, OutPutInfo info) {
        if (null != info) {
            return OutPutTypeEnum.getNameByCode(info.getOutPutType()) + "排口" + getTypeByDataEnum(req);
        } else {
            return "排口" + getTypeByDataEnum(req);
        }
    }

    private static String getTypeByDataEnum(OutPutOnlineReq req) {
        if (DataEnum.real.name().equals(req.getDataEnum())) {
            return "在线监测实时数据";
        } else if (DataEnum.minute.name().equals(req.getDataEnum())) {
            return "在线监测分钟数据";
        } else if (DataEnum.hour.name().equals(req.getDataEnum())) {
            return "在线监测小时数据";
        } else if (DataEnum.day.name().equals(req.getDataEnum())) {
            return "在线监测日数据";
        } else {
            return "在线监测数据";
        }
    }

    /**
     * 跳页处理
     */
    private void skipPageProcess(OutPutOnlineReq req, long count) {
        // 判断是否跳页
        int pageNum = PageUtils.getPageNum(1);
        if (count <= req.getPageSize()) { // 总量只够一页的不需要处理
            req.setOutPutIdF(null);
            req.setOutPutIdE(null);
            return;
        }
        // 非首页，且无本页的第一或最后一条参数时，当作跳页处理
        if (pageNum > 1 && StringUtils.isEmpty(req.getOutPutIdF()) && StringUtils.isEmpty(req.getOutPutIdE())) {
            // 偏移量设置
            req.setOffset(PageUtils.getOffset(pageNum, req.getPageSize(), count));
            String outPutIdE = outPutOnlineMapper.selectSkipPageSign(req);
            req.setOutPutIdE(outPutIdE);
        }
    }

    @Override
    public AjaxResult selectDataChart(OutPutOnlineReq req) {
        log.info("排口在线监测图表查询: {}", req);
        AjaxResult result = AjaxResult.success(new ArrayList<>());
        // 请求数据校验
        if (checkDataReq(req)) {
            return result;
        }
        // 名称列表
        List<String> legend = new ArrayList<>();
        // 数据列表
        List<OutPutOnlineChartSeries> series = new ArrayList<>();
        // 横坐标列表
        List<String> xAxis = new ArrayList<>();
        // 返回数据
        result = AjaxResult.success();
        result.put("legend", legend);
        result.put("series", series);
        result.put("xAxis", xAxis);

        // 通过动态表头构建响应数据
        List<Map<String, Object>> autoHead = platformFacade.autoHeadChart(req.getOutPutId());
        Map<String, String> seriesDataKey = new HashMap<>();
        Map<String, Integer> placesMap = new HashMap<>();
        autoHead.forEach(e -> {
            String pollutantCode = MapUtil.getStr(e, "pollutantCode");
            Integer decimalPlaces = MapUtil.getInt(e, "decimalPlaces");
            if (StringUtils.isNotEmpty(pollutantCode) && null != decimalPlaces && decimalPlaces >= 0) {
                placesMap.put(pollutantCode, decimalPlaces);
            }
            String monFactor = MapUtil.getStr(e, "monFactor");
            if (StringUtils.isNotEmpty(monFactor)) {
                for (String name : monFactor.split(",")) {
                    if (DataEnum.real.name().equals(req.getDataEnum())) { // 实测数据只看实测值
                        if ("rtd".equals(name)) {
                            addSeries(legend, series, seriesDataKey, "_rtd", "", e);
                        } else if ("zsRtd".equals(name)) {
                            addSeries(legend, series, seriesDataKey, "_zsRtd", "折算", e);
                        }
                    } else {
                        if ("avg".equals(name)) {
                            addSeries(legend, series, seriesDataKey, "_avg", "", e);
                        } else if ("zsAvg".equals(name)) {
                            addSeries(legend, series, seriesDataKey, "_zsAvg", "折算", e);
                        }
                    }
                }
            }
        });
        // 判断是否存在表
        int exists = outPutOnlineMapper.checkExists(req.getTableName());
        if (exists < 1) {
            return result;
        }
        // 数据获取
        List<OutPutOnlineData> dataList = outPutOnlineMapper.selectCharDataList(req);
        // 整理数据
        if (null == dataList || dataList.isEmpty()) {
            return result;
        }
        String lastTime = null;
        // 数据分类，xAxis添加时间戳，dataMap按时间+污染物编号+数据类型进行设置方便获取
        Map<String, BigDecimal> dataMap = new HashMap<>();
        String key;
        List<String> xAxisLocalDateTime = new ArrayList<>();
        for (OutPutOnlineData d : dataList) {
            // 判断是否下一个时间点
            if (null == lastTime || !lastTime.equals(d.getMonitorTime())) {
                lastTime = d.getMonitorTime();
                xAxisLocalDateTime.add(d.getMonitorTime());
            }
            // {"a01001": {"rtd": "25.0", "flag": "N"}, "a01002": {"rtd": "35.6", "flag": "N"}...
            if (StringUtils.isNotEmpty(d.getDataInfoStr())) {
                JSONObject infoMap = JSONObject.parseObject(d.getDataInfoStr());
                for (String pollCode : infoMap.keySet()) {
                    key = d.getMonitorTime() + "_" + pollCode + "_";
                    parseDataInfoStr(dataMap, key, infoMap, pollCode, placesMap.get(pollCode));
                }
            }
        }
        // 循环时间进行数据设置
        xAxisLocalDateTime.forEach(e -> {
            xAxis.add(e);
            series.forEach(f -> f.getList().add(dataMap.get(e + "_" + seriesDataKey.get(f.getName()))));
        });
        return result;
    }

    private boolean checkDataReq(OutPutOnlineReq req) {
        if (null == req || StringUtils.isEmpty(req.getBeginTime()) || StringUtils.isEmpty(req.getEndTime())
                || StringUtils.isEmpty(req.getOutPutId())) {
            return true;
        }
        if (DataEnum.real.name().equals(req.getDataEnum())) {
            req.setStart(DateUtils.strToLocalDateTime(req.getBeginTime(), DateUtils.dtf));
            req.setEnd(DateUtils.strToLocalDateTime(req.getEndTime(), DateUtils.dtf));
            req.setDataType(DataTypeEnum.real.code);
        } else if (DataEnum.minute.name().equals(req.getDataEnum())) {
            req.setStart(DateUtils.strToLocalDateTime(req.getBeginTime() + ":00", DateUtils.dtf));
            req.setEnd(DateUtils.strToLocalDateTime(req.getEndTime() + ":59", DateUtils.dtf));
            req.setDataType(DataTypeEnum.minute.code);
        } else if (DataEnum.hour.name().equals(req.getDataEnum())) {
            req.setStart(DateUtils.strToLocalDateTime(req.getBeginTime() + ":00:00", DateUtils.dtf));
            req.setEnd(DateUtils.strToLocalDateTime(req.getEndTime() + ":59:59", DateUtils.dtf));
            req.setDataType(DataTypeEnum.hour.code);
        } else if (DataEnum.day.name().equals(req.getDataEnum())) {
            req.setStart(DateUtils.strToLocalDateTime(req.getBeginTime() + " 00:00:00", DateUtils.dtf));
            req.setEnd(DateUtils.strToLocalDateTime(req.getEndTime() + " 23:59:59", DateUtils.dtf));
            req.setDataType(DataTypeEnum.day.code);
        } else {
            return true;
        }
        String suffix = DateUtils.getTableYear(req.getEnd()) + "_" + req.getOutPutId().toLowerCase();
        req.setTableName("t_data_out_" + suffix);
        return false;
    }

    @Override
    public AjaxResult multipleList(OutPutOnlineReq req) {
        log.info("多排口在线监测列表查询: {}", req);
        // 获取数据列表
        List<MultipleOutPutInfo> outList = new ArrayList<>();
        List<MultipleOutPutData> dataList = getDateList(req, outList);
        // 数据整理
        dataList.forEach( e -> {
            if (StringUtils.isNotEmpty(e.getDataInfoStr())) {
                e.setDataMap(JSONObject.parseObject(e.getDataInfoStr()));
            }
        });
        dataList.sort((o1, o2) -> {
            int compare = o2.getMonitorTime().compareTo(o1.getMonitorTime());
            if (compare == 0) {
                compare = o2.getOutPutId().compareTo(o1.getOutPutId());
            }
            return compare;
        });
        AjaxResult result = AjaxResult.success();
        result.put("info", outList);
        result.put("data", dataList);
        return result;
    }

    @Override
    public AjaxResult multipleChart(OutPutOnlineReq req) {
        log.info("多排口在线监测图表查询: {}", req);
        // 获取数据列表
        List<MultipleOutPutInfo> outList = new ArrayList<>();
        List<MultipleOutPutData> dataList = getDateList(req, outList);
        // 数据列表
        List<MultipleChartSeries> series = new ArrayList<>();
        // 横坐标列表
        List<String> xAxis = new ArrayList<>();
        // 返回数据
        AjaxResult result = AjaxResult.success();
        result.put("outList", outList); // 排口列表
        result.put("series", series);
        result.put("xAxis", xAxis);
        // 整理数据
        if (null == dataList || dataList.isEmpty()) {
            return result;
        }
        // 时间顺序排列
        dataList.sort(Comparator.comparing(MultipleOutPutData::getMonitorTime));
        // 通过动态表头构建响应数据
        List<Map<String, Object>> autoHead = platformFacade.multipleAutoHead(req.getOutPutIds(), req.getDataEnum());
        Map<String, String> seriesDataKey = new HashMap<>();
        Map<String, Integer> placesMap = new HashMap<>();
        autoHead.forEach(e -> {
            String pollutantCode = MapUtil.getStr(e, "pollutantCode");
            Object headList = e.get("headList");
            String pollutantNameCn = MapUtil.getStr(e, "pollutantNameCn");
            String pollutantUnitEn = MapUtil.getStr(e, "pollutantUnitEn");
            if (null != headList) {
                Integer decimalPlaces = MapUtil.getInt(e, "decimalPlaces");
                if (StringUtils.isNotEmpty(pollutantCode) && null != decimalPlaces && decimalPlaces >= 0) {
                    placesMap.put(pollutantCode, decimalPlaces);
                }
                @SuppressWarnings("unchecked")
                List<Map<String, String>> list = (List<Map<String, String>>)headList;
                for (Map<String, String> item : list) {
                    // 设置名称及单位
                    String name = MapUtil.getStr(item, "name");
                    String pollutantName = null;
                    if (DataEnum.real.name().equals(req.getDataEnum())) { // 实测数据只看实测值
                        if ("rtd".equals(name)) {
                            pollutantName = pollutantNameCn;
                        } else if ("zsRtd".equals(name)) {
                            pollutantName = pollutantNameCn + "折算";
                        }
                    } else {
                        if ("avg".equals(name)) {
                            pollutantName = pollutantNameCn;
                        } else if ("zsAvg".equals(name)) {
                            pollutantName = pollutantNameCn + "折算";
                        }
                    }
                    if (null == pollutantName) {
                        continue;
                    }
                    MultipleChartSeries ser = MultipleChartSeries.builder()
                            .name(pollutantName)
                            .unit(pollutantUnitEn)
                            .item(new ArrayList<>())
                            .build();
                    seriesDataKey.put(ser.getName(), pollutantCode + "_" + name); // 数据关联关系
                    for (MultipleOutPutInfo f : outList) {
                        ser.getItem().add(MultipleChartSeriesData.builder()
                                .outPutId(f.getOutPutId())
                                .list(new ArrayList<>())
                                .build());
                    }
                    series.add(ser); // 数据项
                }
            }
        });
        String lastTime = null;
        // 数据分类，xAxis添加时间戳，dataMap按时间+污染物编号+数据类型进行设置方便获取
        Map<String, BigDecimal> dataMap = new HashMap<>();
        String key;
        List<String> xAxisLocalDateTime = new ArrayList<>();
        for (MultipleOutPutData d : dataList) {
            // 判断是否下一个时间点
            if (null == lastTime || !lastTime.equals(d.getMonitorTime())) {
                lastTime = d.getMonitorTime();
                xAxisLocalDateTime.add(d.getMonitorTime());
            }
            // {"a01001": {"rtd": "25.0", "flag": "N"}, "a01002": {"rtd": "35.6", "flag": "N"}...
            if (StringUtils.isNotEmpty(d.getDataInfoStr())) {
                JSONObject infoMap = JSONObject.parseObject(d.getDataInfoStr());
                for (String pollCode : infoMap.keySet()) {
                    key = d.getOutPutId() + "_" + d.getMonitorTime() + "_" + pollCode + "_";
                    parseDataInfoStr(dataMap, key, infoMap, pollCode, placesMap.get(pollCode));
                }
            }
        }
        // 循环时间进行数据设置
        xAxisLocalDateTime.forEach(e -> {
            xAxis.add(e);
            series.forEach(f -> f.getItem().forEach( g ->
                    g.getList().add(dataMap.get(g.getOutPutId() + "_" + e + "_" + seriesDataKey.get(f.getName())))
            ));
        });
        return result;
    }

    private void parseDataInfoStr(Map<String, BigDecimal> dataMap, String key, JSONObject infoMap, String pollCode, Integer decimalPlaces) {
        JSONObject sub = infoMap.getJSONObject(pollCode);
        dataMap.put(key + "rtd", setScale(sub.getBigDecimal("rtd"), decimalPlaces));
        dataMap.put(key + "avg", setScale(sub.getBigDecimal("avg"), decimalPlaces));
        dataMap.put(key + "zsRtd", setScale(sub.getBigDecimal("zsRtd"), decimalPlaces));
        dataMap.put(key + "zsAvg", setScale(sub.getBigDecimal("zsAvg"), decimalPlaces));
    }

    private BigDecimal setScale(BigDecimal bigDecimal, Integer decimalPlaces) {
        if (null == bigDecimal || null == decimalPlaces || decimalPlaces < 0) {
            return bigDecimal;
        }
        return bigDecimal.setScale(decimalPlaces, RoundingMode.HALF_UP);
    }

    /**
     * 获取数据列表
     * @param outList 返回数据需要的信息
     */
    private List<MultipleOutPutData> getDateList(OutPutOnlineReq req, List<MultipleOutPutInfo> outList) {
        List<MultipleOutPutData> dataList = new ArrayList<>();
        // 需指定排口列表查询数据
        if (null == req || null == req.getOutPutIds() || req.getOutPutIds().isEmpty()) {
            return dataList;
        }
        // 需指定查询时间点以及时间偏移量
        if (StringUtils.isEmpty(req.getQueryTime()) || null == req.getQueryMargin() || req.getQueryMargin() < 1) {
            return dataList;
        }
        // 转换时间格式
        LocalDateTime time;
        if (DataEnum.real.name().equals(req.getDataEnum())) {
            time = LocalDateTime.parse(req.getQueryTime(), DateUtils.yy_m_d_h_m_s);
            req.setStart(time.minusMinutes(req.getQueryMargin()));
            req.setEnd(time.plusMinutes(req.getQueryMargin()));
            req.setDataType(DataTypeEnum.real.code);
        } else if (DataEnum.minute.name().equals(req.getDataEnum())) {
            time = LocalDateTime.parse(req.getQueryTime(), DateUtils.yy_m_d_h_m);
            req.setStart(time.minusMinutes(req.getQueryMargin()).truncatedTo(ChronoUnit.MINUTES));
            req.setEnd(time.plusMinutes(req.getQueryMargin() + 1).truncatedTo(ChronoUnit.MINUTES).minusSeconds(1));
            req.setDataType(DataTypeEnum.minute.code);
        } else if (DataEnum.hour.name().equals(req.getDataEnum())) {
            time = LocalDateTime.parse(req.getQueryTime(), DateUtils.yy_m_d_h);
            req.setStart(time.minusHours(req.getQueryMargin()).truncatedTo(ChronoUnit.HOURS));
            req.setEnd(time.plusHours(req.getQueryMargin() + 1).truncatedTo(ChronoUnit.HOURS).minusSeconds(1));
            req.setDataType(DataTypeEnum.hour.code);
        } else if (DataEnum.day.name().equals(req.getDataEnum())) {
            time = LocalDate.parse(req.getQueryTime(), DateUtils.yy_m_d).atStartOfDay();
            req.setStart(time.minusDays(req.getQueryMargin()).truncatedTo(ChronoUnit.DAYS));
            req.setEnd(time.plusDays(req.getQueryMargin() + 1).truncatedTo(ChronoUnit.DAYS).minusSeconds(1));
            req.setDataType(DataTypeEnum.day.code);
        } else {
            return dataList;
        }
        // 获取排口信息
        List<OutPutInfo> cacheOutList = redisCacheUtils.getAllOutPutList();
        if (null == cacheOutList || cacheOutList.isEmpty()) {
            return dataList;
        }
        cacheOutList = cacheOutList.stream().filter(item -> req.getOutPutIds().contains(item.getOutPutId()))
                .collect(Collectors.toList());
        // 未命中排口
        if (cacheOutList.isEmpty()) {
            return dataList;
        }
        // 获取要查询的表名
        Map<String, MultipleOutPutInfo> infoMap = new HashMap<>();
        for (OutPutInfo e : cacheOutList) {
            String tableName = "t_data_out_" + DateUtils.getTableYear(time) + "_" + e.getOutPutId().toLowerCase();
            infoMap.put(tableName, MultipleOutPutInfo.builder()
                    .outPutId(e.getOutPutId())
                    .outPutType(e.getOutPutType())
                    .entCode(e.getEntCode())
                    .entName(e.getEntName())
                    .outPutCode(e.getOutPutCode())
                    .outPutName(e.getOutPutName())
                    .mnNum(e.getMnNum())
                    .tableName(tableName)
                    .build());
        }
        // 判断是否存在表
        List<String> existTables = outPutOnlineMapper.selectTableNameList(new ArrayList<>(infoMap.keySet()));
        if (existTables.isEmpty()) {
            return dataList;
        }
        // 通过存在的表进行过滤
        infoMap.forEach( (k, v) -> {
            if (existTables.contains(k)) {
                outList.add(v);
            }
        });
        if (null == outList || outList.isEmpty()) {
            return dataList;
        }
        return outPutOnlineMapper.selectDataList(outList, req);
    }

    private void addSeries(List<String> legend, List<OutPutOnlineChartSeries> series,
                           Map<String, String> seriesDataKey, String pollutantCodeSuffix,
                           String pollutantNameCnSuffix, Map<String, Object> autoHead) {
        String pollutantNameCn = MapUtil.getStr(autoHead, "pollutantNameCn") + pollutantNameCnSuffix;
        String overMaxValue = MapUtil.getStr(autoHead, "overMaxValue");
        String pollutantUnitEn = MapUtil.getStr(autoHead, "pollutantUnitEn");
        String rtdUnit = MapUtil.getStr(autoHead, "rtdUnit");
        if (StringUtils.isNotEmpty(rtdUnit)) {
            pollutantUnitEn = rtdUnit;
        }
        legend.add(pollutantNameCn); // 列表项
        OutPutOnlineChartSeries ser = OutPutOnlineChartSeries.builder()
                .name(pollutantNameCn)
                .norm(StringUtils.isEmpty(overMaxValue) ? null : new BigDecimal(overMaxValue))
                .unit(pollutantUnitEn)
                .list(new ArrayList<>())
                .build();
        series.add(ser); // 数据项
        seriesDataKey.put(pollutantNameCn, MapUtil.getStr(autoHead, "pollutantCode") + pollutantCodeSuffix); // 数据关联关系
    }

    @Override
    public AjaxResult reportList(OutPutOnlineReq req) {
        log.info("企业下的排口在线监测数据报表查询: TimePeriod {}, Time {}, outId {}", req.getTimePeriodEnum(), req.getQueryTime(), req.getOutPutId());
        // 使用 LinkedHashMap 保持顺序，用于后续赋值操作
        Map<String, OutPutOnlineReportData> linkedMap = new LinkedHashMap<>();
        Map<String, JSONObject> countMap = new HashMap<>();
        getReportData(req, linkedMap, countMap);
        AjaxResult result = AjaxResult.success(linkedMap.values());
        result.putAll(countMap);
        return result;
    }

    @Override
    public void exportReport(OutPutOnlineReq req, HttpServletResponse response) {
        log.info("导出企业下的排口在线监测数据报表查询: TimePeriod {}, Time {}, outId {}", req.getTimePeriodEnum(), req.getQueryTime(), req.getOutPutId());
        // 使用 LinkedHashMap 保持顺序，用于后续赋值操作
        Map<String, OutPutOnlineReportData> linkedMap = new LinkedHashMap<>();
        Map<String, JSONObject> countMap = new HashMap<>();
        getReportData(req, linkedMap, countMap);
        OutputStream outputStream = null;
        try {
            XSSFWorkbook workbook = ExcelUtils.getSheetAt("在线监测数据报表模板.xlsx");
            if (workbook == null) {
                return;
            }
            OutPutInfo info = redisCacheUtils.getAllOutPutById(req.getOutPutId());
            // 表格信息填充
            fillSheet(req, info, workbook, linkedMap, countMap);
            // 设置响应信息
            response.setContentType(MimeTypeUtils.EXCEL_XLSX);
            response.setHeader("Content-Disposition", "attachment;filename*=UTF-8''" + URLEncoder.encode(getExportReportFileName(req, info), StandardCharsets.UTF_8));
            outputStream = response.getOutputStream();
            workbook.write(outputStream);
        } catch (Exception e) {
            log.error("按模板导出文件失败", e);
        } finally {
            if (outputStream != null) {
                try {
                    outputStream.close();
                } catch (IOException e) {
                    log.error("close err", e);
                }
            }
        }
    }

    private void fillSheet(OutPutOnlineReq req, OutPutInfo info, XSSFWorkbook workbook, Map<String, OutPutOnlineReportData> linkedMap, Map<String, JSONObject> countMap) {
        if (null == workbook || null == info) {
            return;
        }
        // 通过动态表头构建响应数据
        List<Map<String, Object>> autoHead = platformFacade.getAutoHead(req.getOutPutId(), DataEnum.day.name());
        if (null == autoHead || autoHead.isEmpty()) {
            return;
        }
        Sheet sheet = workbook.getSheetAt(0);

        // 计算需要新增的总列数
        int totalNewColumns = 0;
        Map<String, CellStyle> placesMap = new HashMap<>();
        Map<Integer, CellStyle> styleMap = new HashMap<>();
        for (Map<String, Object> head : autoHead) {
            Object headList = head.get("headList");
            if (headList instanceof List) {
                String pollutantCode = MapUtil.getStr(head, "pollutantCode");
                Integer decimalPlaces = MapUtil.getInt(head, "decimalPlaces");
                if (StringUtils.isNotEmpty(pollutantCode) && null != decimalPlaces && decimalPlaces >= 0) {
                    if (!styleMap.containsKey(decimalPlaces)) {
                        styleMap.put(decimalPlaces, CellUtils.getCellStyle(workbook, sheet, 6, 2, decimalPlaces));
                    }
                    placesMap.put(pollutantCode, styleMap.get(decimalPlaces));
                }
                @SuppressWarnings("unchecked")
                List<Map<String, String>> list = (List<Map<String, String>>) headList;
                for (Map<String, String> item : list) {
                    String name = MapUtil.getStr(item, "name");
                    if ("cou".equals(name) || "avg".equals(name) || "zsAvg".equals(name)) {
                        totalNewColumns++;
                    }
                }
            }
        }

        if (totalNewColumns <= 0) {
            return;
        }

        CellStyle firstStyle = CellUtils.getCellStyle(workbook, sheet, 0, 0);
        CellStyle infoStyle = CellUtils.getCellStyle(workbook, sheet, 1, 1);
        infoStyle.setAlignment(HorizontalAlignment.LEFT); // 改为左对齐
        CellStyle titleStyle = CellUtils.getCellStyle(workbook, sheet, 4, 2);
        CellStyle headStyle = CellUtils.getCellStyle(workbook, sheet, 5, 2);
        CellStyle style = CellUtils.getCellStyle(workbook, sheet, 6, 1);
        CellStyle styleN3 = CellUtils.getCellStyle(workbook, sheet, 6, 2, 3);

        // 需要重新合并首行
        // 安全地移除第1到3行（行索引0、1、2）的所有合并区域
        List<CellRangeAddress> mergedRegionsToRemove = new ArrayList<>();
        for (int i = 0; i < sheet.getNumMergedRegions(); i++) {
            CellRangeAddress mergedRegion = sheet.getMergedRegion(i);
            if (mergedRegion.getFirstRow() == 0 && mergedRegion.getLastRow() == 0) {
                mergedRegionsToRemove.add(mergedRegion);
            }
            if (mergedRegion.getFirstRow() == 1 && mergedRegion.getLastRow() == 1) {
                mergedRegionsToRemove.add(mergedRegion);
            }
            if (mergedRegion.getFirstRow() == 2 && mergedRegion.getLastRow() == 2) {
                mergedRegionsToRemove.add(mergedRegion);
            }
            if (mergedRegion.getFirstRow() == 3 && mergedRegion.getLastRow() == 3) {
                mergedRegionsToRemove.add(mergedRegion);
            }
        }

        for (CellRangeAddress region : mergedRegionsToRemove) {
            int regionIndex = sheet.getMergedRegions().indexOf(region);
            if (regionIndex >= 0) {
                sheet.removeMergedRegion(regionIndex);
            }
        }
        // 列的索引
        int column = 2;
        // 行的索引
        int rowNm = 0;
        // 移动从 lastColumn 开始（获取单元格样式之后，防止获取不到样式）
        sheet.shiftColumns(column, column, totalNewColumns - 1);

        // 获取现有的行
        Row row = sheet.getRow(rowNm);
        // 设置首行的样式
        Cell cell = row.getCell(0);
        cell.setCellValue(getTitleByType(req, info));
        cell.setCellStyle(firstStyle);
        // 设置空格样式
        setDefaultStyle(row, firstStyle, totalNewColumns + column, 1);
        // 合并首行  (totalNewColumns - 1) + column
        sheet.addMergedRegion(new CellRangeAddress(rowNm, rowNm, 0, totalNewColumns + 1));
        rowNm++;
        // 企业名称
        row = sheet.getRow(rowNm);
        cell = CellUtils.getCell(row, 1, infoStyle);
        if (StringUtils.isNotEmpty(info.getEntName())) {
            cell.setCellValue(info.getEntName());
        }
        // 设置空格样式
        setDefaultStyle(row, infoStyle, totalNewColumns + column, 2);
        // 合并  (totalNewColumns - 1) + column
        sheet.addMergedRegion(new CellRangeAddress(rowNm, rowNm, 1, totalNewColumns + 1));
        rowNm++;
        // 排口名称
        row = sheet.getRow(rowNm);
        cell = CellUtils.getCell(row, 1, infoStyle);
        if (StringUtils.isNotEmpty(info.getOutPutName())) {
            cell.setCellValue(info.getOutPutName());
        }
        // 设置空格样式
        setDefaultStyle(row, infoStyle, totalNewColumns + column, 2);
        sheet.addMergedRegion(new CellRangeAddress(rowNm, rowNm, 1, totalNewColumns + 1));
        rowNm++;
        // 报表日期
        row = sheet.getRow(rowNm);
        cell = CellUtils.getCell(row, 1, infoStyle);
        if (StringUtils.isNotEmpty(req.getQueryTime())) {
            cell.setCellValue(req.getQueryTime());
        }
        // 设置空格样式
        setDefaultStyle(row, infoStyle, totalNewColumns + column, 2);
        sheet.addMergedRegion(new CellRangeAddress(rowNm, rowNm, 1, totalNewColumns + 1));
        rowNm++;
        // 标题行
        Row titleRow = sheet.getRow(rowNm);
        rowNm++;
        Row headerRow = sheet.getRow(rowNm);
        List<String> keyList = new ArrayList<>();
        List<String> headerContents = new ArrayList<>();
        for (Map<String, Object> head : autoHead) {
            Object headList = head.get("headList");
            if (!(headList instanceof List)) {
                continue;
            }
            @SuppressWarnings("unchecked")
            List<Map<String, String>> list = (List<Map<String, String>>) headList;
            if (list.isEmpty()) {
                continue;
            }
            String pollutantCode = MapUtil.getStr(head, "pollutantCode");
            String pollutantNameCn = MapUtil.getStr(head, "pollutantNameCn");
            String pollutantUnitEn = MapUtil.getStr(head, "pollutantUnitEn");
            String rtdUnit = MapUtil.getStr(head, "rtdUnit");
            if (StringUtils.isNotEmpty(rtdUnit)) {
                pollutantUnitEn = rtdUnit;
            }
            String unitPfEn = MapUtil.getStr(head, "unitPfEn");
            String couUnit = MapUtil.getStr(head, "couUnit");
            if (StringUtils.isNotEmpty(couUnit)) {
                unitPfEn = couUnit;
            }
            int size = 0;
            for (Map<String, String> item : list) {
                String name = MapUtil.getStr(item, "name");
                String desc = MapUtil.getStr(item, "desc");

                String displayDesc = desc;
                if ("cou".equals(name)) {
                    // 使用排放量单位
                    if (unitPfEn != null && !unitPfEn.isEmpty()) {
                        displayDesc = desc + "(" + unitPfEn + ")";
                    }
                } else if ("avg".equals(name) || "zsAvg".equals(name)) {
                    // 使用排放单位
                    if (pollutantUnitEn != null && !pollutantUnitEn.isEmpty()) {
                        displayDesc = desc + "(" + pollutantUnitEn + ")";
                    }
                } else {
                    continue;
                }
                keyList.add(pollutantCode + "_" + name);
                // 现在这些单元格是通过shift创建的，会保留样式
                Cell descCell = CellUtils.getCell(headerRow, column + size, headStyle);
                descCell.setCellValue(displayDesc);
                headerContents.add(displayDesc);
                size++;
            }
            if (size < 1) {
                continue;
            }
            // 设置第一行表头
            Cell titleCell = CellUtils.getCell(titleRow, column, titleStyle);
            titleCell.setCellValue(pollutantNameCn);

            // 合并第一行单元格
            if (size > 1) {
                sheet.addMergedRegion(new CellRangeAddress(rowNm - 1,rowNm - 1, column,column + size - 1));
            }
            column += size;
        }
        // 调整新扩展列的列宽
        if (!headerContents.isEmpty()) {
            for (int i = 0; i < headerContents.size(); i++) {
                String content = headerContents.get(i);
                int byteLength = content.getBytes(StandardCharsets.UTF_8).length;
                int columnWidth = (byteLength + 2) * 256;

                // 根据内容长度智能限制
                if (byteLength <= 8) {
                    columnWidth = Math.max(columnWidth, 3500);  // 短文本最小宽度
                } else if (byteLength >= 40) {
                    columnWidth = Math.min(columnWidth, 10000); // 长文本最大宽度
                }
                sheet.setColumnWidth(i + 2, columnWidth);
            }
        }
        // 插入数据
        if (null == linkedMap || linkedMap.isEmpty()) {
            return;
        }
        int index = 1;
        rowNm++;// 行变化
        // 行移动（获取单元格样式之后）
        CellUtils.shiftRows(sheet, rowNm, linkedMap.size());
        for (OutPutOnlineReportData data : linkedMap.values()) {

            row = sheet.createRow(rowNm++);

            int cellIndex = 0;
            // 序号
            cell = CellUtils.getCell(row, cellIndex++, style);
            cell.setCellValue(index++);
            // 时间
            cell = CellUtils.getCell(row, cellIndex++, style);
            cell.setCellValue(data.getDataTime());

            // 填充数据
            setCellBigDecimal(keyList, row, cellIndex, styleN3, data.getDataMap(), placesMap);
        }
        // 填充统计数据 countMap
        // 平均值 avgCount
        setCellBigDecimal(keyList, sheet.getRow(rowNm++), 2, styleN3, countMap.get("avgCount"), placesMap);
        // 最大值
        setCellBigDecimal(keyList, sheet.getRow(rowNm++), 2, styleN3, countMap.get("maxCount"), placesMap);
        // 最小值
        setCellBigDecimal(keyList, sheet.getRow(rowNm++), 2, styleN3, countMap.get("minCount"), placesMap);
        // 排放量
        setCellBigDecimal(keyList, sheet.getRow(rowNm), 2, styleN3, countMap.get("sumCount"), placesMap);
    }

    private void setCellBigDecimal(List<String> keyList, Row row, int cellIndex, CellStyle style, JSONObject data, Map<String, CellStyle> placesMap) {
        Cell cell;
        for (String k : keyList) {
            String[] ks = k.split("_");
            String pollCode = ks[0];
            cell = CellUtils.getCell(row, cellIndex++, placesMap.getOrDefault(pollCode, style));
            JSONObject v = data.getJSONObject(pollCode);
            if (null == v) {
                cell.setCellValue("---");
                continue;
            }
            BigDecimal val = v.getBigDecimal(ks[1]);
            if (null == val) {
                cell.setCellValue("---");
                continue;
            }
            cell.setCellValue(val.doubleValue());
        }
    }

    private void setDefaultStyle(Row row, CellStyle style, int maxColumn, int start) {
        for (int i = start; i < maxColumn; i++) {
            CellUtils.getCell(row, i, style);
        }
    }

    private static String getTitleByType(OutPutOnlineReq req, OutPutInfo info) {
        if (null != info) {
            return OutPutTypeEnum.getNameByCode(info.getOutPutType()) + "排口" + getTypeInfo(req);
        } else {
            return "排口" + getTypeInfo(req);
        }
    }

    private static String getExportReportFileName(OutPutOnlineReq req, OutPutInfo info) {
        if (null != info) {
            return info.getOutPutName() + getTypeInfo(req) + ".xlsx";
        } else {
            return getTypeInfo(req) + ".xlsx";
        }
    }

    private static String getTypeInfo(OutPutOnlineReq req) {
        if (TimePeriodEnum.day.name().equals(req.getTimePeriodEnum())) { // 例如：2024-01-01
            return "在线监测日报表数据";
        } else if (TimePeriodEnum.week.name().equals(req.getTimePeriodEnum())) { // 例如：2024-01-01
            return "在线监测周报表数据";
        } else if (TimePeriodEnum.month.name().equals(req.getTimePeriodEnum())) { // 例如：2024-01
            return "在线监测月报表数据";
        } else if (TimePeriodEnum.quarter.name().equals(req.getTimePeriodEnum())) { // 例如：2024
            return "在线监测季度报表数据";
        } else if (TimePeriodEnum.year.name().equals(req.getTimePeriodEnum())) { // 例如：2024
            return "在线监测年报表数据";
        } else {
            return "在线监测报表数据";
        }
    }

    private void getReportData(OutPutOnlineReq req, Map<String, OutPutOnlineReportData> linkedMap, Map<String, JSONObject> countMap) {
        // 初始化统计信息
        initCountMap(countMap, "maxCount");
        initCountMap(countMap, "minCount");
        initCountMap(countMap, "avgCount");
        initCountMap(countMap, "sumCount");
        if (null == req) {
            return;
        }
        // 请求数据校验
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime start;
        if (TimePeriodEnum.day.name().equals(req.getTimePeriodEnum())) { // 例如：2024-01-01
            req.setDataType(DataTypeEnum.hour.code);
            log.debug("日报表查询处理 {}", req.getQueryTime());
            start = LocalDate.parse(req.getQueryTime(), DateUtils.yy_m_d).atStartOfDay();
            req.setStart(start);
            req.setEnd(start.plusDays(1).minusSeconds(1));
            if (req.getEnd().isAfter(now)) {
                req.setEnd(now);
            }
            // 填充小时数据
            while (!start.isAfter(req.getEnd())) {
                initOutPutOnlineReportData(linkedMap, req.getTimePeriodEnum(), start);
                start = start.plusHours(1);
            }
        } else if (TimePeriodEnum.week.name().equals(req.getTimePeriodEnum())) { // 例如：2024-01-01
            req.setDataType(DataTypeEnum.day.code);
            log.debug("周报表查询处理 {}", req.getQueryTime());
            start = LocalDate.parse(req.getQueryTime(), DateUtils.yy_m_d).with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))
                    .atStartOfDay();
            req.setStart(start);
            req.setEnd(start.plusWeeks(1).minusSeconds(1));
            if (req.getEnd().isAfter(now)) {
                req.setEnd(now);
            }
            // 填充一周的数据
            while (!start.isAfter(req.getEnd())) {
                initOutPutOnlineReportData(linkedMap, req.getTimePeriodEnum(), start);
                start = start.plusDays(1);
            }
        } else if (TimePeriodEnum.month.name().equals(req.getTimePeriodEnum())) { // 例如：2024-01
            req.setDataType(DataTypeEnum.day.code);
            log.debug("月报表查询处理 {}", req.getQueryTime());
            start = YearMonth.parse(req.getQueryTime()).atDay(1).atStartOfDay();
            req.setStart(start);
            req.setEnd(start.plusMonths(1).minusSeconds(1));
            if (req.getEnd().isAfter(now)) {
                req.setEnd(now);
            }
            // 填充日数据
            while (!start.isAfter(req.getEnd())) {
                initOutPutOnlineReportData(linkedMap, req.getTimePeriodEnum(), start);
                start = start.plusDays(1);
            }
        } else if (TimePeriodEnum.quarter.name().equals(req.getTimePeriodEnum())) { // 例如：2024-01、-04、-07、-10
            req.setDataType(DataTypeEnum.day.code);
            log.debug("季度报表查询处理 {}", req.getQueryTime());
            // 解析查询时间
            String[] timeParts = req.getQueryTime().split("-");
            int year = Integer.parseInt(timeParts[0]);
            int month = Integer.parseInt(timeParts[1]);
            // 根据月份确定季度起始月份
            int quarterStartMonth;
            if (month >= 1 && month <= 3) {
                quarterStartMonth = 1;  // 第一个季度
            } else if (month >= 4 && month <= 6) {
                quarterStartMonth = 4;  // 第二个季度
            } else if (month >= 7 && month <= 9) {
                quarterStartMonth = 7;  // 第三个季度
            } else {
                quarterStartMonth = 10; // 第四个季度
            }
            // 设置季度开始时间
            start = LocalDateTime.of(year, quarterStartMonth, 1, 0, 0);
            req.setStart(start);
            // 设置季度结束时间
            req.setEnd(start.plusMonths(3).minusSeconds(1));
            if (req.getEnd().isAfter(now)) {
                req.setEnd(now);
            }
            // 按月填充季度数据
            while (!start.isAfter(req.getEnd())) {
                initOutPutOnlineReportData(linkedMap, req.getTimePeriodEnum(), start);
                start = start.plusMonths(1);
            }
        } else if (TimePeriodEnum.year.name().equals(req.getTimePeriodEnum())) { // 例如：2024
            req.setDataType(DataTypeEnum.day.code);
            log.debug("年报表查询处理 {}", req.getQueryTime());
            start = LocalDateTime.of(Integer.parseInt(req.getQueryTime()), 1, 1, 0, 0);
            req.setStart(start);
            req.setEnd(start.plusYears(1).minusSeconds(1));
            if (req.getEnd().isAfter(now)) {
                req.setEnd(now);
            }
            // 填充月数据
            while (!start.isAfter(req.getEnd())) {
                initOutPutOnlineReportData(linkedMap, req.getTimePeriodEnum(), start);
                start = start.plusMonths(1);
            }
        } else {
            return;
        }
        String suffix = DateUtils.getTableYear(req.getEnd()) + "_" + req.getOutPutId().toLowerCase();
        req.setTableName("t_data_out_" + suffix);
        // 判断是否存在表
        int size = outPutOnlineMapper.checkTableExistsByName(req.getTableName());
        if (size < 1) {
            return;
        }
        List<OutPutOnlineReportData> list = outPutOnlineMapper.selectReportData(req);
        Map<String, BigDecimal> codeSize = new HashMap<>();
        if (list != null && !list.isEmpty()) {
            for (OutPutOnlineReportData r : list) {
                if (StringUtils.isEmpty(r.getDataInfoStr())) {
                    continue;
                }
                String key = getTimeSegmentKey(req.getTimePeriodEnum(), r.getMonitorTime());
                if (null == key || !linkedMap.containsKey(key)) {
                    continue;
                }
                OutPutOnlineReportData data = linkedMap.get(key);
                JSONObject dataInfo = JSONObject.parseObject(r.getDataInfoStr());
                for (String k : dataInfo.keySet()) {
                    // k - pollCode
                    JSONObject v = dataInfo.getJSONObject(k);
                    // 按分类合并数据
                    setDataVal(codeSize, v.getBigDecimal("avg"), k, "avg", data);
                    setDataVal(codeSize, v.getBigDecimal("zsAvg"), k, "zsAvg", data);
                    setDataVal(codeSize, v.getBigDecimal("cou"), k, "cou", data);
                    // 初始化统计信息
                    setCountMap(countMap, "maxCount", k);
                    setCountMap(countMap, "minCount", k);
                    setCountMap(countMap, "avgCount", k);
                    setCountMap(countMap, "sumCount", k);
                }
            }
            linkedMap.values().forEach( e -> {
                // 需要先修改
                for (String k : e.getDataMap().keySet()) {
                    // k - pollCode
                    JSONObject v = e.getDataMap().getJSONObject(k);
                    // 修改平均值，子项合并到项后再把项设置成平均的
                    updateItemAvg(v, "avg");
                    updateItemAvg(v, "zsAvg");
                }
                for (String k : e.getDataMap().keySet()) {
                    // k - pollCode
                    JSONObject v = e.getDataMap().getJSONObject(k);
                    // 数据整理
                    updateCountMapVal(countMap, "maxCount", k, v, "avg", "max");
                    updateCountMapVal(countMap, "minCount", k, v, "avg", "min");
                    updateCountMapVal(countMap, "avgCount", k, v, "avg", "add");
                    updateCountMapVal(countMap, "maxCount", k, v, "zsAvg", "max");
                    updateCountMapVal(countMap, "minCount", k, v, "zsAvg", "min");
                    updateCountMapVal(countMap, "avgCount", k, v, "zsAvg", "add");
                    updateCountMapVal(countMap, "maxCount", k, v, "cou", "max");
                    updateCountMapVal(countMap, "minCount", k, v, "cou", "min");
                    updateCountMapVal(countMap, "avgCount", k, v, "cou", "add");
                    updateCountMapVal(countMap, "sumCount", k, v, "cou", "add");
                }
            });
            //  平均值转换
            JSONObject count = countMap.get("avgCount");
            for (String k : count.keySet()) {
                JSONObject v = count.getJSONObject(k);
                updateAvg(codeSize, "avg", k, v);
                updateAvg(codeSize, "zsAvg", k, v);
                updateAvg(codeSize, "cou", k, v);
            }
        }
    }

    private void initOutPutOnlineReportData(Map<String, OutPutOnlineReportData> linkedMap, String timePeriodEnum, LocalDateTime monitorTime) {
        OutPutOnlineReportData data = new OutPutOnlineReportData();
        data.setDataTime(getTimeSegmentKey(timePeriodEnum, monitorTime));
        data.setDataMap(new JSONObject());
        linkedMap.put(data.getDataTime(), data);
    }

    private void updateItemAvg(JSONObject item, String name) {
        // 修改平均值，子项合并到项后再把项设置成平均的
        String sk = name + "_size";
        BigDecimal size = item.getBigDecimal(sk);
        if (null == size) {
            return;
        }
        item.remove(sk);
        BigDecimal num = item.getBigDecimal(name);
        if (null == num) {
            return;
        }
        item.put(name, num.divide(size, 3, RoundingMode.HALF_UP));
    }

    private void updateAvg(Map<String, BigDecimal> codeSize, String name, String k, JSONObject v) {
        BigDecimal c = codeSize.get(k + "_" + name);
        BigDecimal val = v.getBigDecimal(name);
        if (null != c) {
            if (null != val) {
                v.put(name, val.divide(c, 3, RoundingMode.HALF_UP));
            }
        } else {
            // 没有个数删除对应的值
            if (null != val) {
                v.remove(name);
            }
        }
    }

    private void updateCountMapVal(Map<String, JSONObject> countMap, String name, String key, JSONObject v, String item, String type) {
        BigDecimal val = v.getBigDecimal(item);
        if (null == val) {
            return;
        }
        JSONObject dv = countMap.get(name).getJSONObject(key);
        BigDecimal old = null;
        if (dv.containsKey(item)) {
            old = dv.getBigDecimal(item);
        }
        if ("max".equals(type)) {
            if (null == old || old.compareTo(val) < 0) {
                dv.put(item, val);
            }
        } else if ("min".equals(type)) {
            if (null == old || old.compareTo(val) > 0) {
                dv.put(item, val);
            }
        } else if ("add".equals(type)) {
            dv.put(item, null == old ? val : old.add(val));
        }
    }

    private void initCountMap(Map<String, JSONObject> countMap, String key) {
        if (!countMap.containsKey(key)) {
            countMap.put(key, new JSONObject());
        }
    }

    private void setCountMap(Map<String, JSONObject> countMap, String key, String code) {
        if (!countMap.get(key).containsKey(code)) {
            countMap.get(key).put(code, new JSONObject());
        }
    }

    private void setDataVal(Map<String, BigDecimal> codeSize, BigDecimal val, String k, String name, OutPutOnlineReportData data) {
        BigDecimal old;
        if (null != val) {
            JSONObject dv = data.getDataMap().getJSONObject(k);
            if (null == dv) {
                dv = new JSONObject();
                data.getDataMap().put(k, dv);
            }
            old = dv.getBigDecimal(name);
            if (null == old) {
                String sizeKey = k + "_" + name;
                codeSize.put(sizeKey, codeSize.getOrDefault(sizeKey, b0).add(b1));
            }
            dv.put(name, null == old ? val : old.add(val));
            // 设置数量，用于计算多个子项的平均值
            if ("avg".equals(name) || "zsAvg".equals(name)) {
                String nameSize = name + "_size";
                old = dv.getBigDecimal(nameSize);
                dv.put(nameSize, null == old ? b1 : old.add(b1));
            }
        }
    }

    private String getTimeSegmentKey(String timePeriodEnum, LocalDateTime monitorTime) {
        String key = null;
        if (null == monitorTime || StringUtils.isEmpty(timePeriodEnum)) {
            return key;
        }
        if (TimePeriodEnum.day.name().equals(timePeriodEnum)) {
            // 小时段：00~01, 01~02, 02~03, ..., 23~24
            key = String.format("%02d~%02d", monitorTime.getHour(), monitorTime.getHour() + 1);
        } else if (TimePeriodEnum.week.name().equals(timePeriodEnum)) {
            // 周内日期：05-01, 05-02, ..., 05-31
            key = String.format("%02d-%02d", monitorTime.getMonthValue(), monitorTime.getDayOfMonth());
        } else if (TimePeriodEnum.month.name().equals(timePeriodEnum)) {
            // 月内日期：05-01, 05-02, ..., 05-31
            key = String.format("%02d-%02d", monitorTime.getMonthValue(), monitorTime.getDayOfMonth());
        } else if (TimePeriodEnum.quarter.name().equals(timePeriodEnum)) {
            // 季度内月份：2024-01, 2024-02, 2024-03
            key = String.format("%04d-%02d", monitorTime.getYear(), monitorTime.getMonthValue());
        } else if (TimePeriodEnum.year.name().equals(timePeriodEnum)) {
            // 年内月份：2024-01, 2024-02, ..., 2024-12
            key = String.format("%04d-%02d", monitorTime.getYear(), monitorTime.getMonthValue());
        }
        return key;
    }
}

