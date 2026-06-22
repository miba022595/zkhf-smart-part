package com.zkhf.epmis.platform.ent.service.impl;

import cn.hutool.core.map.MapUtil;
import com.github.f4b6a3.ulid.UlidCreator;
import com.zkhf.epmis.core.annotation.Log;
import com.zkhf.epmis.core.domain.AjaxResult;
import com.zkhf.epmis.core.enums.*;
import com.zkhf.epmis.core.utils.CellUtils;
import com.zkhf.epmis.core.utils.MimeTypeUtils;
import com.zkhf.epmis.core.utils.PageUtils;
import com.zkhf.epmis.core.utils.StringUtils;
import com.zkhf.epmis.platform.annex.service.AnnexService;
import com.zkhf.epmis.platform.ent.domain.*;
import com.zkhf.epmis.platform.ent.service.EntOutPutInfoService;
import com.zkhf.epmis.platform.global.GVarContainer;
import com.zkhf.epmis.platform.mapper.ent.EntOutPutMapper;
import com.zkhf.epmis.platform.utils.ExcelUtils;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.OutputStream;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 企业排口Service业务层处理
 */
@Slf4j
@Service
public class EntOutPutInfoServiceImpl implements EntOutPutInfoService {

    private EntOutPutMapper entOutPutMapper;

    @Autowired
    public void setEntOutPutMapper(EntOutPutMapper entOutPutMapper) {
        this.entOutPutMapper = entOutPutMapper;
    }

    private AnnexService annexService;

    @Autowired
    public void setAnnexService(AnnexService annexService) {
        this.annexService = annexService;
    }

    @Override
    public List<Map<String, Object>> listAll() {
        return entOutPutMapper.listAll();
    }

    @Override
    public List<EntOutPutPosition> listAllPosition() {
        List<String> entCodes = null;
        if (GVarContainer.isNotAdmin()) {
            entCodes = GVarContainer.getEntCodes();
        }
        return entOutPutMapper.listAllPosition(entCodes);
    }

    @Override
    public AjaxResult typeTreeWithEnt(Integer[] outPutTypes) {
        List<EntOutPutTreeInfo> list;
        if (GVarContainer.isAdmin()) {
            list = entOutPutMapper.typeTreeWithEnt(null, outPutTypes);
        } else {
            list = entOutPutMapper.typeTreeWithEnt(GVarContainer.getEntCodes(), outPutTypes);
        }
        // 初始化最终结果列表和缓存Map
        List<EntOutPutTreeEnt> result = new ArrayList<>();
        // 缓存企业对象，key: entCode
        Map<String, EntOutPutTreeEnt> map = new HashMap<>();
        // 缓存排口类型对象，key: entCode_outPutType
        Map<String, EntOutPutTreeOutType> typeMap = new HashMap<>();
        // 遍历原始数据列表
        for (EntOutPutTreeInfo e : list) {
            // 跳过关键字段为空的数据
            if (null == e.getEntCode() || null == e.getOutPutType()) {
                continue;
            }
            // 1. 处理企业层级
            EntOutPutTreeEnt ent;
            if (map.containsKey(e.getEntCode())) {
                // 从缓存中获取已存在的企业对象
                ent = map.get(e.getEntCode());
            } else {
                // 创建新的企业对象并添加到结果列表和缓存中
                ent = EntOutPutTreeEnt.builder()
                        .entCode(e.getEntCode())
                        .entName(e.getEntName())
                        .shorterName(e.getShorterName())
                        .typeList(new ArrayList<>())
                        .build();
                result.add(ent);
                map.put(e.getEntCode(), ent);
            }
            // 2. 处理排口类型层级  创建复合键
            String key = e.getEntCode() + "_" + e.getOutPutType();
            EntOutPutTreeOutType entOutType;
            if (typeMap.containsKey(key)) {
                // 从缓存中获取已存在的排口类型对象
                entOutType = typeMap.get(key);
            } else {
                // 创建新的排口类型对象并添加到企业的类型列表中
                entOutType = EntOutPutTreeOutType.builder()
                        .outPutType(e.getOutPutType())
                        .outPutTypeDesc(OutPutTypeEnum.getNameByCode(e.getOutPutType()))
                        .outList(new ArrayList<>())
                        .build();
                ent.getTypeList().add(entOutType);
                typeMap.put(key, entOutType);
            }
            // 3. 处理排口层级
            entOutType.getOutList().add(EntOutPutTreeOutInfo.builder()
                    .outPutId(e.getOutPutId())
                    .outPutCode(e.getOutPutCode())
                    .outPutName(e.getOutPutName())
                    .build());
        }
        // 4. 排序处理
        // 对企业按编码降序排序
        result.sort((o1, o2) -> o2.getEntCode().compareTo(o1.getEntCode()));
        // 对每个企业的排口类型按类型编码升序排序
        result.forEach( e -> e.getTypeList().sort(Comparator.comparing(EntOutPutTreeOutType::getOutPutType)));
        // 对每个排口类型下的排口按ID降序排序
        result.forEach( e -> e.getTypeList().forEach( f -> f.getOutList().sort((o1, o2) -> o2.getOutPutId().compareTo(o1.getOutPutId()))));
        return AjaxResult.success(result);
    }

    @Override
    public List<Map<String, Object>> listByEnterType(String entCode, Integer outPutType) {
        return entOutPutMapper.listByEnterType(entCode, outPutType);
    }

    @Override
    public AjaxResult selectOutPutById(String outPutId) {
        EntOutPutInfo info = entOutPutMapper.selectOutPutById(outPutId);
        if (null == info) {
            return AjaxResult.error("排口信息为空");
        }
        info.setAnnexInfoList(annexService.selectAnnexList(outPutId, AnnexTypeEnum.entOutPut.name()));
        info.setFullAnnexInfoList(annexService.selectAnnexList(outPutId, AnnexTypeEnum.entOutPutFull.name()));
        List<Map<String, Object>> relateList = entOutPutMapper.selectRelatePollControlFacilityList(RelateEnum.outPut.name(), Collections.singletonList(outPutId));
        if (null != relateList && !relateList.isEmpty()) {
            info.setRelateFacilityList(new ArrayList<>());
            relateList.forEach( e -> {
                String outPutIdS = e.remove("outPutId") + "";
                if (outPutId.equals(outPutIdS)) {
                    info.getRelateFacilityList().add(e);
                }
            });
        }
        return AjaxResult.success(info);
    }

    @Override
    public AjaxResult selectOutPutList(EntOutPutReq req) {
        if (null == req) {
            req = new EntOutPutReq();
        }
        // 添加权限
        if (GVarContainer.isNotAdmin()) {
            req.setEntCodes(GVarContainer.getEntCodes());
        }
        boolean page = PageUtils.startPageCheckExists();
        List<EntOutPutInfo> list = entOutPutMapper.selectOutPutList(req);
        // 设置检测污染物列表、是否关注等信息
        fillPoll(list, req.getOutPutType());
        // 获取污染治理设施信息
        if (!list.isEmpty()) {
            Map<String, EntOutPutInfo> outPutMap = new HashMap<>();
            list.forEach( e -> {
                e.setRelateFacilityList(new ArrayList<>());
                outPutMap.put(e.getOutPutId(), e);
            });
            List<Map<String, Object>> relateList = entOutPutMapper.selectRelatePollControlFacilityList(RelateEnum.outPut.name(), new ArrayList<>(outPutMap.keySet()));
            relateList.forEach( e -> {
                String outPutId = e.remove("outPutId") + "";
                if (outPutMap.containsKey(outPutId)) {
                    outPutMap.get(outPutId).getRelateFacilityList().add(e);
                }
            });
        }
        return PageUtils.getAjaxResult(list, page);
    }

    private void fillPoll(List<EntOutPutInfo> list, Integer outPutType) {
        if (null == list || list.isEmpty()) {
            return;
        }
        /* 获取排口的污染物列表 */
        List<Map<String, String>> pollList = entOutPutMapper.selectPollutantByType(outPutType);
        Map<String, String> pollCodeName = pollList.stream()
                .collect(Collectors.toMap(e -> MapUtil.getStr(e, "pollutantCode"),
                        e -> MapUtil.getStr(e, "pollutantName"),
                        (k1, k2) -> k1));
        Map<String, EntOutPutInfo> outPutMap = new HashMap<>();
        list.forEach(e -> {
            outPutMap.put(e.getOutPutId(), e);
            StringBuilder pollutantName = new StringBuilder();
            if (null != e.getPollutantCode()) {
                for (String s : e.getPollutantCode().split(",")) {
                    if (pollCodeName.containsKey(s)) {
                        pollutantName.append("、").append(pollCodeName.get(s));
                    }
                }
            }
            if (pollutantName.length() > 0) {
                e.setPollutantName(pollutantName.substring(1));
            } else {
                e.setPollutantName(null);
            }
        });
        /* 获取是否关注信息，列表数据太多时全查 */
        List<Map<String, Object>> userList = entOutPutMapper.selectUserPutInfoList(GVarContainer.getUserId());
        userList.forEach(e -> {
            String outPutId = MapUtil.getStr(e, "outPutId");
            if (outPutMap.containsKey(outPutId)) {
                int count = MapUtil.getInt(e, "count", 0);
                outPutMap.get(outPutId).setAttention(count > 0);// 大于0表示关注了
            }
        });
    }

    @Override
    @Log(title = "导出企业企业排口列表", businessType = BusinessType.EXPORT)
    public void exportOutPut(EntOutPutReq req, HttpServletResponse response) {
        if (null == req) {
            req = new EntOutPutReq();
        }
        OutputStream outputStream = null;
        try {
            XSSFWorkbook workbook = ExcelUtils.getSheetAt("企业污染排放口列表模板.xlsx");
            if (workbook == null) {
                return;
            }
            // 添加权限
            if (GVarContainer.isNotAdmin()) {
                req.setEntCodes(GVarContainer.getEntCodes());
            }
            List<EntOutPutInfo> list = entOutPutMapper.selectOutPutList(req);
            if (null != list && !list.isEmpty()) {
                // 设置检测污染物列表、是否关注等信息
                fillPoll(list, req.getOutPutType());
                Sheet sheet = workbook.getSheetAt(0);
                int rowIndex = 1;// 首行
                CellStyle style = CellUtils.getCellStyle(workbook, sheet, rowIndex);
                CellStyle styleN6 = CellUtils.getCellStyle(workbook, sheet, rowIndex, 0, 6);
                int index = 1;
                Row row;
                Cell cell;
                // 行移动（获取单元格样式之后）
                CellUtils.shiftRows(sheet, rowIndex, list.size());
                for (EntOutPutInfo info : list) {
                    row = sheet.createRow(rowIndex++);

                    int cellIndex = 0;
                    // 序号
                    cell = CellUtils.getCell(row, cellIndex++, style);
                    cell.setCellValue(index++);
                    // 企业名称
                    cell = CellUtils.getCell(row, cellIndex++, style);
                    cell.setCellValue(info.getEntName());
                    // 排口名称
                    cell = CellUtils.getCell(row, cellIndex++, style);
                    cell.setCellValue(info.getOutPutName());
                    // 排口编码
                    cell = CellUtils.getCell(row, cellIndex++, style);
                    cell.setCellValue(info.getOutPutCode());
                    // 监测点类型
                    cell = CellUtils.getCell(row, cellIndex++, style);
                    cell.setCellValue(OutPutTypeEnum.getNameByCode(info.getOutPutType()));
                    // 排放状态
                    cell = CellUtils.getCell(row, cellIndex++, style);
                    cell.setCellValue(OutPutStatusEnum.getNameByCode(info.getOutPutStatus()));
                    // 检测污染物
                    cell = CellUtils.getCell(row, cellIndex++, style);
                    cell.setCellValue(info.getPollutantName());
                    // 数采仪MN号
                    cell = CellUtils.getCell(row, cellIndex++, style);
                    cell.setCellValue(info.getMnNum());
                    // 中心经度
                    cell = CellUtils.getCell(row, cellIndex++, styleN6);
                    if (null != info.getLongitude()) {
                        cell.setCellValue(info.getLongitude());
                    }
                    // 中心纬度
                    cell = CellUtils.getCell(row, cellIndex++, styleN6);
                    if (null != info.getLatitude()) {
                        cell.setCellValue(info.getLatitude());
                    }
                    // 是否关注
                    cell = CellUtils.getCell(row, cellIndex++, style);
                    cell.setCellValue(info.isAttention() ? "关注" : "未关注");
                    // 备注
                    cell = CellUtils.getCell(row, cellIndex, style);
                    cell.setCellValue(info.getRemark());
                }
            }
            response.setContentType(MimeTypeUtils.EXCEL_XLSX);
            response.setHeader("Content-Disposition", "attachment;filename*=UTF-8''" + URLEncoder.encode("企业污染排放口列表.xlsx", StandardCharsets.UTF_8));
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

    @Override
    @Log(title = "新增企业排口", businessType = BusinessType.INSERT)
    public AjaxResult insertOutPut(EntOutPutInfo info) {
        if (StringUtils.isEmpty(info.getEntCode())) {
            return AjaxResult.error("未指定所属企业");
        }
        List<Map<String, String>> oldList = entOutPutMapper.selectOutPutCodeByEnt(info.getEntCode());
        for (Map<String, String> old : oldList) {
            if (null != info.getOutPutCode() && info.getOutPutCode().equals(MapUtil.getStr(old, ""))) {
                return AjaxResult.error("企业下已存在排口编码[" + info.getEntCode() + "]，请确定后添加");
            }
        }
        // 单调递增id (适合高并发)
        info.setOutPutId(UlidCreator.getMonotonicUlid().toString());
        info.setCreateTime(LocalDateTime.now());
        int result = entOutPutMapper.insertOutPut(info);
        if (result > 0) {
            // 设置附件
            if (info.getAnnexIds() != null && !info.getAnnexIds().isEmpty()) {
                annexService.updateAnnex(info.getOutPutId(), AnnexTypeEnum.entOutPut.name(), info.getAnnexIds());
            }
            // 设置站房全景
            if (info.getFullAnnexIds() != null && !info.getFullAnnexIds().isEmpty()) {
                annexService.updateAnnex(info.getOutPutId(), AnnexTypeEnum.entOutPutFull.name(), info.getFullAnnexIds());
            }
            // 添加新的关联污染治理设施关系
            if (null != info.getFacilityIds() && !info.getFacilityIds().isEmpty()) {
                entOutPutMapper.insertOutPutRelatePollControlFacility(RelateEnum.outPut.name(), info.getOutPutId(), info.getFacilityIds());
            }
        }
        return AjaxResult.success(info);
    }

    @Override
    @Log(title = "修改企业排口", businessType = BusinessType.UPDATE)
    public AjaxResult updateOutPut(EntOutPutInfo info) {
        List<Map<String, String>> oldList = entOutPutMapper.selectOutPutCodeFromThisId(info.getOutPutId());
        for (Map<String, String> old : oldList) {
            String outPutId = MapUtil.getStr(old, "outPutId");
            String outPutCode = MapUtil.getStr(old, "outPutCode");
            if (null == outPutCode) {
                continue;
            }
            if (outPutCode.equals(info.getOutPutCode()) && !outPutId.equals(info.getOutPutId())) {
                return AjaxResult.error("企业下已存在排口编码[" + info.getOutPutCode() + "]，请确定后修改");
            }
        }
        info.setUpdateTime(LocalDateTime.now());
        int result = entOutPutMapper.updateOutPut(info);
        // 修改附件信息
        if (result > 0) {
            annexService.updateAnnex(info.getOutPutId(), AnnexTypeEnum.entOutPut.name(), info.getAnnexIds());
            annexService.updateAnnex(info.getOutPutId(), AnnexTypeEnum.entOutPutFull.name(), info.getFullAnnexIds());
            // 删除旧的关联污染治理设施关系
            entOutPutMapper.deleteOutPutRelatePollControlFacilityById(RelateEnum.outPut.name(), info.getOutPutId());
            // 添加新的关联污染治理设施关系
            if (null != info.getFacilityIds() && !info.getFacilityIds().isEmpty()) {
                entOutPutMapper.insertOutPutRelatePollControlFacility(RelateEnum.outPut.name(), info.getOutPutId(), info.getFacilityIds());
            }
        }
        return AjaxResult.success(info);
    }

    @Override
    @Log(title = "删除企业排口", businessType = BusinessType.DELETE)
    public AjaxResult deleteOutPutById(String outPutId) {
        int result = entOutPutMapper.deleteOutPutById(outPutId);
        if (result > 0) {
            // 删除排口的关注
            entOutPutMapper.deleteOutputAtt(outPutId);
            // 删除排口对应的污染物
            entOutPutMapper.deleteOutPutPoll(outPutId);
            // 删除排口的附件
            annexService.updateAnnex(outPutId, AnnexTypeEnum.entOutPut.name(), null);
            annexService.updateAnnex(outPutId, AnnexTypeEnum.entOutPutFull.name(), null);
            // 删除旧的关联污染治理设施关系
            entOutPutMapper.deleteOutPutRelatePollControlFacilityById(RelateEnum.outPut.name(), outPutId);
        }
        return AjaxResult.success();
    }

    @Override
    @Log(title = "用户关注排口", businessType = BusinessType.GRANT)
    public AjaxResult userAttentionAdd(String outPutId) {
        if (null == outPutId) {
            return AjaxResult.error("未知的请求参数");
        }
        Long userId = GVarContainer.getUserId();
        int count = entOutPutMapper.userAttentionCount(userId);
        if (count >= 10) {
            return AjaxResult.error("最多关注10个排口");
        }
        entOutPutMapper.userAttentionAdd(userId, outPutId);
        return AjaxResult.success();
    }

    @Override
    @Log(title = "用户取消关注排口", businessType = BusinessType.GRANT)
    public AjaxResult userAttentionDel(String outPutId) {
        if (null == outPutId) {
            return AjaxResult.error("未知的请求参数");
        }
        entOutPutMapper.userAttentionDel(GVarContainer.getUserId(), outPutId);
        return AjaxResult.success();
    }

    @Override
    public List<Map<String, Object>> userAttentionList(Long userId) {
        return entOutPutMapper.userAttentionList(userId);
    }

    @Override
    public List<Map<String, Object>> outPutStatusList(List<String> entCodes) {
        return entOutPutMapper.outPutStatusList(entCodes);
    }

    @Override
    public AjaxResult selectAlarmConf(String outPutId) {
        List<OutPutAlarmConf> result = new ArrayList<>();
        if (StringUtils.isEmpty(outPutId)) {
            return AjaxResult.success(result);
        }
        // 获取排口的报警配置
        List<OutPutAlarmConf> list = entOutPutMapper.selectAlarmConf(outPutId);
        Map<Integer, OutPutAlarmConf> map = new HashMap<>();
        if (null != list && !list.isEmpty()) {
            for (OutPutAlarmConf e : list) {
                if (null == e.getAlarmCode() || map.containsKey(e.getAlarmCode())) {
                    continue;
                }
                map.put(e.getAlarmCode(), e);
                e.setAlarmDesc(AlarmDetailTypeEnum.getNameByCode(e.getAlarmCode()).name);
                setStatus(e);
                result.add(e);
            }
        }
        // 获取公共的报警配置
        List<OutPutAlarmConf> pub = entOutPutMapper.selectAlarmPubConf(outPutId);
        if (null != pub && !pub.isEmpty()) {
            for (OutPutAlarmConf e : pub) {
                if (null == e.getAlarmCode() || map.containsKey(e.getAlarmCode())) {
                    continue;
                }
                e.setAlarmDesc(AlarmDetailTypeEnum.getNameByCode(e.getAlarmCode()).name);
                e.setOutPutId(outPutId);
                setStatus(e);
                result.add(e);
            }
        }
        return AjaxResult.success(result);
    }

    private void setStatus(OutPutAlarmConf alarm) {
        if (null == alarm) {
            return;
        }
        if (StringUtils.isNotEmpty(alarm.getOutPutStatus())) {
            StringBuilder bu = new StringBuilder();
            for (String s : alarm.getOutPutStatus().split(",")) {
                if (bu.length() > 0) {
                    bu.append(",");
                }
                bu.append(OutPutStatusEnum.getNameByCode(Integer.parseInt(s)));
            }
            alarm.setOutPutStatusDesc(bu.toString());
        }
        if (StringUtils.isNotEmpty(alarm.getDataType())) {
            StringBuilder bu = new StringBuilder();
            for (String s : alarm.getDataType().split(",")) {
                if (bu.length() > 0) {
                    bu.append(",");
                }
                bu.append(DataTypeEnum.getNameByCode(Integer.parseInt(s)));
            }
            alarm.setDataTypeDesc(bu.toString());
        }
    }

    @Override
    public AjaxResult alarmConfEdit(List<OutPutAlarmConf> list) {
        if (null == list || list.isEmpty()) {
            return AjaxResult.success(0);
        }
        // 直接删除旧的（不管有没有）
        entOutPutMapper.alarmConfDel(list.get(0).getOutPutId());
        // 插入新的
        int count = entOutPutMapper.alarmConfEdit(list);
        return AjaxResult.success(count);
    }

    @Override
    public List<OutPutAlarmConf> selectAllAlarmConf() {
        return entOutPutMapper.selectAlarmConf(null);
    }

}

