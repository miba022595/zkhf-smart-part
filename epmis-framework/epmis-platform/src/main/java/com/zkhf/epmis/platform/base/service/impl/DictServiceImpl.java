package com.zkhf.epmis.platform.base.service.impl;

import com.zkhf.epmis.core.annotation.Log;
import com.zkhf.epmis.core.domain.AjaxResult;
import com.zkhf.epmis.core.enums.BusinessType;
import com.zkhf.epmis.core.utils.CellUtils;
import com.zkhf.epmis.core.utils.MimeTypeUtils;
import com.zkhf.epmis.core.utils.PageUtils;
import com.zkhf.epmis.core.utils.StringUtils;
import com.zkhf.epmis.platform.base.domain.DictData;
import com.zkhf.epmis.platform.base.domain.DictType;
import com.zkhf.epmis.platform.base.domain.DictTypeReq;
import com.zkhf.epmis.platform.base.service.DictService;
import com.zkhf.epmis.platform.global.GVarContainer;
import com.zkhf.epmis.platform.mapper.base.DictMapper;
import com.zkhf.epmis.platform.utils.DictUtils;
import com.zkhf.epmis.platform.utils.ExcelUtils;
import jakarta.annotation.PostConstruct;
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
 * 字典 业务层处理
 */
@Slf4j
@Service
public class DictServiceImpl implements DictService {

    private DictMapper dictMapper;
    @Autowired
    public void setDictMapper(DictMapper dictMapper) {
        this.dictMapper = dictMapper;
    }

    /**
     * 项目启动时，初始化字典到缓存
     */
    @PostConstruct
    public void init() {
        loadingDictCache();
    }

    @Override
    public List<DictData> getDataListByTypes(List<String> dictTypes) {
        return dictMapper.getDataListByTypes(dictTypes);
    }

    @Override
    public Map<String, Map<String, String>> getDataMapByTypes(List<String> dictTypes) {
        List<DictData> dataList = dictMapper.getDataListByTypes(dictTypes);
        /* dictLabel: "工程车辆"; dictType: "ent_product"; dictValue: "gccl" */
        Map<String, Map<String, String>> dictMap = new HashMap<>();
        if (dataList != null && !dataList.isEmpty()) {
            dataList.forEach(e -> {
                String dictType = e.getDictType();
                Map<String, String> sub;
                if (dictMap.containsKey(dictType)) {
                    sub = dictMap.get(dictType);
                } else {
                    sub = new HashMap<>();
                    dictMap.put(dictType, sub);
                }
                sub.put(e.getDictValue(), e.getDictLabel());
            });
        }
        return dictMap;
    }

    @Override
    public AjaxResult selectTypeAll() {
        return AjaxResult.success(dictMapper.selectTypeAll());
    }

    @Override
    public AjaxResult selectTypeList(DictTypeReq req) {
        if (null == req) {
            req = new DictTypeReq();
        }
        PageUtils.startPage();
        List<DictType> list = dictMapper.selectTypeList(req);
        return PageUtils.getAjaxResult(list, true);
    }

    @Override
    public AjaxResult selectDataList(DictData req) {
        if (null == req) {
            req = new DictData();
        }
        PageUtils.startPage();
        List<DictData> list = dictMapper.selectDataList(req);
        return PageUtils.getAjaxResult(list, true);
    }

    @Override
    @Log(title = "字典类型", businessType = BusinessType.EXPORT)
    public void exportTypeList(DictTypeReq req, HttpServletResponse response) {
        if (null == req) {
            req = new DictTypeReq();
        }
        OutputStream outputStream = null;
        try {
            XSSFWorkbook workbook = ExcelUtils.getSheetAt("字典类型列表模板.xlsx");
            if (workbook == null) {
                return;
            }
            List<DictType> list = dictMapper.selectTypeList(req);
            if (null != list && !list.isEmpty()) {
                Sheet sheet = workbook.getSheetAt(0);
                int rowIndex = 1;
                CellStyle style = CellUtils.getCellStyle(workbook, sheet, rowIndex);
                Row row;
                Cell cell;
                // 行移动（获取单元格样式之后）
                CellUtils.shiftRows(sheet, rowIndex, list.size());
                for (DictType type : list) {
                    row = sheet.createRow(rowIndex++);

                    int cellIndex = 0;
                    // 字典主键
                    cell = CellUtils.getCell(row, cellIndex++, style);
                    cell.setCellValue(type.getDictId());
                    // 字典名称
                    cell = CellUtils.getCell(row, cellIndex++, style);
                    cell.setCellValue(type.getDictName());
                    // 字典类型
                    cell = CellUtils.getCell(row, cellIndex++, style);
                    cell.setCellValue(type.getDictType());
                    // 状态
                    cell = CellUtils.getCell(row, cellIndex, style);
                    cell.setCellValue("0".equals(type.getStatus()) ? "正常" : "停用");
                }
            }
            response.setContentType(MimeTypeUtils.EXCEL_XLSX);
            response.setHeader("Content-Disposition", "attachment;filename*=UTF-8''" + URLEncoder.encode("字典类型列表.xlsx", StandardCharsets.UTF_8));
            outputStream = response.getOutputStream();
            workbook.write(outputStream);
        } catch (Exception e) {
            log.error("按模板导出文件失败", e);
        } finally {
            if (outputStream != null) {
                try {
                    outputStream.close();
                } catch (IOException e) {
                    log.error("outputStream close", e);
                }
            }
        }
    }

    @Override
    @Log(title = "字典数据", businessType = BusinessType.EXPORT)
    public void exportDataList(DictData req, HttpServletResponse response) {
        if (null == req) {
            req = new DictData();
        }
        OutputStream outputStream = null;
        try {
            XSSFWorkbook workbook = ExcelUtils.getSheetAt("字典数据列表模板.xlsx");
            if (workbook == null) {
                return;
            }
            List<DictData> list = dictMapper.selectDataList(req);
            if (null != list && !list.isEmpty()) {
                Sheet sheet = workbook.getSheetAt(0);
                int rowIndex = 1;

                CellStyle style = CellUtils.getCellStyle(workbook, sheet, rowIndex);
                Row row;
                Cell cell;
                // 行移动（获取单元格样式之后）
                CellUtils.shiftRows(sheet, rowIndex, list.size());
                for (DictData data : list) {
                    row = sheet.createRow(rowIndex++);

                    int cellIndex = 0;
                    // 字典编码
                    cell = CellUtils.getCell(row, cellIndex++, style);
                    cell.setCellValue(data.getDictCode());
                    // 字典排序
                    cell = CellUtils.getCell(row, cellIndex++, style);
                    cell.setCellValue(data.getDictSort());
                    // 字典标签
                    cell = CellUtils.getCell(row, cellIndex++, style);
                    cell.setCellValue(data.getDictLabel());
                    // 字典键值
                    cell = CellUtils.getCell(row, cellIndex++, style);
                    cell.setCellValue(data.getDictValue());
                    // 字典类型
                    cell = CellUtils.getCell(row, cellIndex++, style);
                    cell.setCellValue(data.getDictType());
                    // 是否默认
                    cell = CellUtils.getCell(row, cellIndex++, style);
                    cell.setCellValue("Y".equals(data.getIsDefault()) ? "是" : "否");
                    // 状态
                    cell = CellUtils.getCell(row, cellIndex, style);
                    cell.setCellValue("0".equals(data.getStatus()) ? "正常" : "停用");
                }
            }
            response.setContentType(MimeTypeUtils.EXCEL_XLSX);
            response.setHeader("Content-Disposition", "attachment;filename*=UTF-8''" + URLEncoder.encode("字典类型列表.xlsx", StandardCharsets.UTF_8));
            outputStream = response.getOutputStream();
            workbook.write(outputStream);
        } catch (Exception e) {
            log.error("按模板导出文件失败", e);
        } finally {
            if (outputStream != null) {
                try {
                    outputStream.close();
                } catch (IOException e) {
                    log.error("outputStream close", e);
                }
            }
        }
    }

    @Override
    public AjaxResult selectTypeById(Long id) {
        return AjaxResult.success(dictMapper.selectTypeById(id));
    }

    @Override
    public AjaxResult selectDataById(Long code) {
        return AjaxResult.success(dictMapper.selectDataById(code));
    }

    @Override
    public List<DictData> selectDataByType(String type) {
        // 1. 先查缓存
        List<DictData> dataList = DictUtils.getDictCache(type);
        if (StringUtils.isEmpty(dataList)) {
            // 2. 查询数据库
            dataList = dictMapper.selectDataByType(type);
            // 3. 更新缓存
            if (StringUtils.isNotEmpty(dataList)) {
                DictUtils.setDictCache(type, dataList);
            }
        }
        if (StringUtils.isEmpty(dataList)) {
            dataList = new ArrayList<>();
        }
        return dataList;
    }

    @Override
    @Log(title = "字典类型", businessType = BusinessType.INSERT)
    public AjaxResult insertType(DictType type) {
        DictType check = dictMapper.checkTypeUnique(type.getDictType());
        if (null != check) {
            return AjaxResult.error("新增字典'" + type.getDictName() + "'失败，字典类型已存在");
        }
        type.setCreateBy(GVarContainer.getUserName());
        type.setCreateTime(LocalDateTime.now());
        int row = dictMapper.insertType(type);
        if (row > 0) {
            DictUtils.setDictCache(type.getDictType(), null);
        }
        return AjaxResult.success(row);
    }

    @Override
    @Log(title = "字典数据", businessType = BusinessType.INSERT)
    public AjaxResult insertData(DictData data) {
        data.setCreateBy(GVarContainer.getUserName());
        data.setCreateTime(LocalDateTime.now());
        int row = dictMapper.insertData(data);
        if (row > 0) {
            List<DictData> dataList = dictMapper.selectDataByType(data.getDictType());
            DictUtils.setDictCache(data.getDictType(), dataList);
        }
        return AjaxResult.success(row);
    }

    @Override
    public AjaxResult updateType(DictType dict) {
        DictType check = dictMapper.checkTypeUnique(dict.getDictType());
        if (null != check && !check.getDictId().equals(dict.getDictId())) {
            return AjaxResult.error("修改字典'" + dict.getDictName() + "'失败，字典类型已存在");
        }
        dict.setUpdateBy(GVarContainer.getUserName());
        dict.setUpdateTime(LocalDateTime.now());
        DictType oldDict = dictMapper.selectTypeById(dict.getDictId());
        int row = dictMapper.updateType(dict);
        if (row > 0) {
            dictMapper.updateDataType(oldDict.getDictType(), dict.getDictType());
            List<DictData> dataList = dictMapper.selectDataByType(dict.getDictType());
            DictUtils.setDictCache(dict.getDictType(), dataList);
        }
        return AjaxResult.success(row);
    }

    @Override
    public AjaxResult updateData(DictData data) {
        data.setUpdateBy(GVarContainer.getUserName());
        data.setUpdateTime(LocalDateTime.now());
        int row = dictMapper.updateData(data);
        if (row > 0) {
            List<DictData> dataList = dictMapper.selectDataByType(data.getDictType());
            DictUtils.setDictCache(data.getDictType(), dataList);
        }
        return AjaxResult.success(row);
    }

    @Override
    public AjaxResult deleteTypeByIds(Long[] ids) {
        for (Long id : ids) {
            DictType type = dictMapper.selectTypeById(id);
            if (null == type) {
                return AjaxResult.error("未知的字典类型");
            }
            if (dictMapper.countDataByType(type.getDictType()) > 0) {
                return AjaxResult.error(type.getDictName() + "已分配,不能删除");
            }
            dictMapper.deleteTypeById(id);
            DictUtils.removeDictCache(type.getDictType());
        }
        return AjaxResult.success();
    }

    @Override
    public AjaxResult deleteDataByIds(Long[] codes) {
        for (Long code : codes) {
            DictData data = dictMapper.selectDataById(code);
            if (null == data) {
                return AjaxResult.error("未知的字典数据");
            }
            dictMapper.deleteDataById(code);
            List<DictData> dataList = dictMapper.selectDataByType(data.getDictType());
            DictUtils.setDictCache(data.getDictType(), dataList);
        }
        return AjaxResult.success();
    }

    @Override
    @Log(title = "字典类型", businessType = BusinessType.CLEAN)
    public AjaxResult resetCache() {
        // 清空字典缓存数据
        DictUtils.clearDictCache();
        loadingDictCache();
        return AjaxResult.success();
    }

    /**
     * 加载字典缓存数据
     */
    private void loadingDictCache() {
        DictData dictData = new DictData();
        dictData.setStatus("0");
        Map<String, List<DictData>> dictDataMap = dictMapper.selectDataList(dictData).stream().collect(Collectors.groupingBy(DictData::getDictType));
        for (Map.Entry<String, List<DictData>> entry : dictDataMap.entrySet()) {
            DictUtils.setDictCache(entry.getKey(), entry.getValue().stream().sorted(Comparator.comparing(DictData::getDictSort)).collect(Collectors.toList()));
        }
    }

    @Override
    @Log(title = "字典数据", businessType = BusinessType.INSERT)
    public AjaxResult insertDataIntAuto(DictData data) {
        List<DictData> dataList = DictUtils.getDictCache(data.getDictType());
        int maxSort = 0, maxValue = 0;
        for (DictData d : dataList) {
            if (null != d.getDictSort()) {
                maxSort = Math.max(maxSort, d.getDictSort());
            }
            maxValue = Math.max(maxValue, StringUtils.strToInt(d.getDictValue(), 0));
        }
        data.setDictSort(maxSort + 1);
        data.setDictValue(String.valueOf(maxValue + 1));
        data.setCssClass("default");
        data.setStatus("0");
        int row = dictMapper.insertData(data);
        if (row > 0) {
            dataList = dictMapper.selectDataByType(data.getDictType());
            DictUtils.setDictCache(data.getDictType(), dataList);
        }
        return AjaxResult.success(row);
    }

    @Override
    public List<DictData> selectCustomDataByType(String type) {
        Long userId = GVarContainer.getUserId();
        // 1. 先查缓存
        List<DictData> dataList = DictUtils.getDictCustomCache(type, userId);
        if (StringUtils.isEmpty(dataList)) {
            // 2. 查询数据库
            dataList = dictMapper.selectCustomDataByType(type, userId);
            // 3. 更新缓存
            if (StringUtils.isNotEmpty(dataList)) {
                DictUtils.setDictCustomCache(type, userId, dataList);
            }
        }
        if (StringUtils.isEmpty(dataList)) {
            dataList = new ArrayList<>();
        }
        return dataList;
    }

    @Override
    @Log(title = "字典自定义数据", businessType = BusinessType.INSERT)
    public AjaxResult insertCustomData(DictData data) {
        Long userId = GVarContainer.getUserId();
        data.setCreateBy(GVarContainer.getUserName());
        data.setCreateTime(LocalDateTime.now());
        int row = dictMapper.insertCustomData(data, userId);
        if (row > 0) {
            List<DictData> dataList = dictMapper.selectCustomDataByType(data.getDictType(), userId);
            DictUtils.setDictCustomCache(data.getDictType(), userId, dataList);
        }
        return AjaxResult.success(row);
    }

    @Override
    public AjaxResult deleteCustomDataByCode(Long code) {
        Long userId = GVarContainer.getUserId();
        DictData data = dictMapper.selectCustomDataById(code, userId);
        if (null == data) {
            return AjaxResult.error("未知的字典数据");
        }
        dictMapper.deleteCustomDataByCode(code, userId);
        List<DictData> dataList = dictMapper.selectCustomDataByType(data.getDictType(), userId);
        DictUtils.setDictCustomCache(data.getDictType(), userId, dataList);
        return AjaxResult.success();
    }
}

