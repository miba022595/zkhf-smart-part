package com.zkhf.epmis.process.material.service.impl;

import com.alibaba.fastjson2.JSONObject;
import com.github.f4b6a3.ulid.UlidCreator;
import com.zkhf.epmis.core.annotation.Log;
import com.zkhf.epmis.core.domain.AjaxResult;
import com.zkhf.epmis.core.enums.BusinessType;
import com.zkhf.epmis.core.utils.DateUtils;
import com.zkhf.epmis.core.utils.PageUtils;
import com.zkhf.epmis.core.utils.StringUtils;
import com.zkhf.epmis.process.global.GVarContainer;
import com.zkhf.epmis.process.mapper.material.MaterialInfoMapper;
import com.zkhf.epmis.process.mapper.material.MaterialStockMapper;
import com.zkhf.epmis.process.material.domain.MaterialInfo;
import com.zkhf.epmis.process.material.domain.MaterialInfoReq;
import com.zkhf.epmis.process.material.domain.MaterialStock;
import com.zkhf.epmis.process.material.domain.MaterialStockReq;
import com.zkhf.epmis.process.material.service.MaterialInfoService;
import com.zkhf.epmis.process.material.service.MaterialOrderSupport;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 物资基础信息Service实现
 */
@Service
public class MaterialInfoServiceImpl implements MaterialInfoService {

    private MaterialInfoMapper materialInfoMapper;
    @Autowired
    public void setMaterialInfoMapper(MaterialInfoMapper materialInfoMapper) {
        this.materialInfoMapper = materialInfoMapper;
    }

    private MaterialStockMapper materialStockMapper;
    @Autowired
    public void setMaterialStockMapper(MaterialStockMapper materialStockMapper) {
        this.materialStockMapper = materialStockMapper;
    }

    private MaterialOrderSupport materialOrderSupport;
    @Autowired
    public void setMaterialOrderSupport(MaterialOrderSupport materialOrderSupport) {
        this.materialOrderSupport = materialOrderSupport;
    }

    @Override
    public AjaxResult selectMaterialInfoList(MaterialInfoReq req) {
        req = initReq(req);
        if (req == null) {
            return AjaxResult.success(new ArrayList<>());
        }
        boolean page = PageUtils.startPageCheckExists();
        List<MaterialInfo> list = materialInfoMapper.selectMaterialInfoList(req);
        fillCurrentQty(list);
        materialOrderSupport.fillEntNameByCode(list);
        return PageUtils.getAjaxResult(list, page);
    }

    @Override
    public AjaxResult selectMaterialInfoDetail(String materialId) {
        if (StringUtils.isEmpty(materialId)) {
            return AjaxResult.error("物资ID不能为空");
        }
        MaterialInfo info = materialInfoMapper.selectMaterialInfoById(materialId);
        if (info == null) {
            return AjaxResult.error("数据不存在");
        }
        if (notEntPermission(info.getEntCode())) {
            return AjaxResult.error("无权限操作该企业数据");
        }
        materialOrderSupport.fillEntNameByCode(List.of(info));
        MaterialStockReq req = new MaterialStockReq();
        req.setEntCode(info.getEntCode());
        req.setMaterialId(materialId);
        List<MaterialStock> stockList = materialStockMapper.selectMaterialStockList(req);

        List<JSONObject> slimStockList = new ArrayList<>();
        double currentQty = 0D;
        if (stockList != null) {
            for (MaterialStock s : stockList) {
                currentQty += s.getCurrentQty() == null ? 0D : s.getCurrentQty();
                JSONObject stock = new JSONObject();
                stock.put("stockId", s.getStockId());
                stock.put("warehouseId", s.getWarehouseId());
                stock.put("warehouseCode", s.getWarehouseCode());
                stock.put("warehouseName", s.getWarehouseName());
                stock.put("currentQty", s.getCurrentQty());
                stock.put("availableQty", s.getAvailableQty());
                stock.put("frozenQty", s.getFrozenQty());
                stock.put("minStock", s.getMinStock());
                stock.put("stockStatus", s.getStockStatus());
                stock.put("lastChangeTime", formatTime(s.getLastChangeTime()));
                stock.put("createTime", formatTime(s.getCreateTime()));
                stock.put("updateTime", formatTime(s.getUpdateTime()));
                slimStockList.add(stock);
            }
        }
        info.setCurrentQty(currentQty);
        info.setStockList(slimStockList);
        return AjaxResult.success(info);
    }

    @Override
    @Log(title = "物资基础信息", businessType = BusinessType.INSERT)
    public AjaxResult insertMaterialInfo(MaterialInfo info) {
        if (info == null) {
            return AjaxResult.error("未知的参数");
        }
        if (StringUtils.isEmpty(info.getEntCode())) {
            return AjaxResult.error("企业编码不能为空");
        }
        if (notEntPermission(info.getEntCode())) {
            return AjaxResult.error("无权限操作该企业数据");
        }
        if (StringUtils.isEmpty(info.getMaterialCode())) {
            return AjaxResult.error("物资编号不能为空");
        }
        if (StringUtils.isEmpty(info.getMaterialName())) {
            return AjaxResult.error("物资名称不能为空");
        }
        if (StringUtils.isEmpty(info.getUnit())) {
            return AjaxResult.error("计量单位不能为空");
        }
        String validateMsg = validateMaterialForSave(info, null);
        if (validateMsg != null) {
            return AjaxResult.error(validateMsg);
        }
        info.setMaterialId(UlidCreator.getMonotonicUlid().toString());
        if (info.getStatus() == null) {
            info.setStatus(0);
        }
        materialInfoMapper.insertMaterialInfo(info);
        return AjaxResult.success(info.getMaterialId());
    }

    @Override
    @Log(title = "物资基础信息", businessType = BusinessType.UPDATE)
    public AjaxResult updateMaterialInfo(MaterialInfo info) {
        if (info == null || StringUtils.isEmpty(info.getMaterialId())) {
            return AjaxResult.error("物资ID不能为空");
        }
        MaterialInfo old = materialInfoMapper.selectMaterialInfoById(info.getMaterialId());
        if (old == null) {
            return AjaxResult.error("数据不存在");
        }
        if (notEntPermission(old.getEntCode())) {
            return AjaxResult.error("无权限操作该企业数据");
        }
        if (StringUtils.isNotEmpty(info.getEntCode()) && !info.getEntCode().equals(old.getEntCode())) {
            return AjaxResult.error("所属企业不允许修改");
        }
        info.setEntCode(old.getEntCode());
        String validateMsg = validateMaterialForSave(info, old.getMaterialId());
        if (validateMsg != null) {
            return AjaxResult.error(validateMsg);
        }
        info.setEntCode(null);
        return AjaxResult.success(materialInfoMapper.updateMaterialInfo(info));
    }

    @Override
    @Log(title = "物资基础信息", businessType = BusinessType.DELETE)
    public AjaxResult deleteMaterialInfo(MaterialInfo info) {
        if (info == null || StringUtils.isEmpty(info.getMaterialId())) {
            return AjaxResult.error("物资ID不能为空");
        }
        MaterialInfo old = materialInfoMapper.selectMaterialInfoById(info.getMaterialId());
        if (old == null) {
            return AjaxResult.error("数据不存在");
        }
        if (notEntPermission(old.getEntCode())) {
            return AjaxResult.error("无权限操作该企业数据");
        }
        if (materialInfoMapper.countMaterialInfoRef(info.getMaterialId()) > 0) {
            return AjaxResult.error("物资已被业务数据引用，不能删除");
        }
        return AjaxResult.success(materialInfoMapper.deleteMaterialInfoById(info.getMaterialId()));
    }

    @Override
    @Log(title = "物资基础信息", businessType = BusinessType.EXPORT)
    public void exportMaterialInfo(MaterialInfoReq req, HttpServletResponse response) {
        req = initReq(req);
        if (req == null) {
            return;
        }
        List<MaterialInfo> list = materialInfoMapper.selectMaterialInfoList(req);
        fillCurrentQty(list);
        materialOrderSupport.fillEntNameByCode(list);
        List<List<String>> rows = new ArrayList<>();
        list.forEach(item -> rows.add(List.of(
                defaultVal(item.getEntName()),
                defaultVal(item.getMaterialCode()),
                defaultVal(item.getMaterialName()),
                defaultVal(item.getBrand()),
                defaultVal(item.getModelSpec()),
                defaultVal(item.getCategoryName()),
                numberVal(item.getCurrentQty()),
                defaultVal(item.getUnit()),
                numberVal(item.getUnitPrice()),
                statusDesc(item.getStatus()),
                stringVal(item.getCreateTime()),
                stringVal(item.getUpdateTime()),
                defaultVal(item.getRemark())
        )));
        materialOrderSupport.exportSimpleExcel("物资台账.xlsx",
                new String[]{"企业", "物资编号", "物资名称", "品牌", "规格型号", "物资分类", "当前库存", "单位", "单价", "状态", "创建时间", "更新时间", "备注"},
                rows, response);
    }

    @Override
    public void downloadMaterialInfoTemplate(HttpServletResponse response) {
        try (XSSFWorkbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("物资导入模板");
            Row head = sheet.createRow(0);
            String[] headers = {"企业编码", "物资编号", "物资名称", "品牌", "规格型号", "物资分类编码", "物资分类名称", "单位", "单价", "最低库存预警值", "状态(0启用 1停用)", "备注"};
            for (int i = 0; i < headers.length; i++) {
                head.createCell(i).setCellValue(headers[i]);
                sheet.setColumnWidth(i, 20 * 256);
            }
            Row example = sheet.createRow(1);
            example.createCell(0).setCellValue("ENT001");
            example.createCell(1).setCellValue("MAT001");
            example.createCell(2).setCellValue("防护手套");
            example.createCell(3).setCellValue("安防牌");
            example.createCell(4).setCellValue("L");
            example.createCell(5).setCellValue("LABOR");
            example.createCell(6).setCellValue("劳保用品");
            example.createCell(7).setCellValue("双");
            example.createCell(8).setCellValue("12.5");
            example.createCell(9).setCellValue("10");
            example.createCell(10).setCellValue("0");
            example.createCell(11).setCellValue("示例数据");
            materialOrderSupport.writeWorkbook("物资导入模板.xlsx", workbook, response);
        } catch (Exception ignored) {
        }
    }

    @Override
    @Log(title = "物资基础信息导入", businessType = BusinessType.IMPORT)
    public AjaxResult importMaterialInfo(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            return AjaxResult.error("导入文件不能为空");
        }
        int success = 0;
        int fail = 0;
        List<String> failMsg = new ArrayList<>();
        DataFormatter formatter = new DataFormatter();
        try (InputStream inputStream = file.getInputStream(); XSSFWorkbook workbook = new XSSFWorkbook(inputStream)) {
            Sheet sheet = workbook.getNumberOfSheets() > 0 ? workbook.getSheetAt(0) : null;
            if (sheet == null) {
                return AjaxResult.error("导入文件内容为空");
            }
            List<MaterialInfo> parsed = new ArrayList<>();
            List<Integer> parsedRowNums = new ArrayList<>();
            for (int i = 1; i <= sheet.getLastRowNum(); i++) {
                Row row = sheet.getRow(i);
                if (isEmptyRow(row, formatter)) {
                    continue;
                }
                try {
                    MaterialInfo info = new MaterialInfo();
                    info.setEntCode(readCell(row, 0, formatter));
                    info.setMaterialCode(readCell(row, 1, formatter));
                    info.setMaterialName(readCell(row, 2, formatter));
                    info.setBrand(readCell(row, 3, formatter));
                    info.setModelSpec(readCell(row, 4, formatter));
                    info.setCategoryCode(readCell(row, 5, formatter));
                    info.setCategoryName(readCell(row, 6, formatter));
                    info.setUnit(readCell(row, 7, formatter));
                    info.setUnitPrice(parseDouble(readCell(row, 8, formatter), "第" + (i + 1) + "行单价格式不正确"));
                    info.setMinStock(parseDouble(readCell(row, 9, formatter), "第" + (i + 1) + "行最低库存格式不正确"));
                    info.setStatus(parseStatus(readCell(row, 10, formatter), "第" + (i + 1) + "行状态值不正确"));
                    info.setRemark(readCell(row, 11, formatter));
                    parsed.add(info);
                    parsedRowNums.add(i + 1);
                } catch (RuntimeException e) {
                    fail++;
                    failMsg.add(e.getMessage());
                }
            }

            boolean[] ok = new boolean[parsed.size()];
            Arrays.fill(ok, true);

            Map<String, Integer> fileDedup = new HashMap<>();
            Map<String, List<String>> entToCodes = new HashMap<>();

            for (int i = 0; i < parsed.size(); i++) {
                MaterialInfo info = parsed.get(i);
                Integer rowNum = parsedRowNums.get(i);
                String rowValidateMsg = validateImportRow(info);
                if (rowValidateMsg != null) {
                    ok[i] = false;
                    fail++;
                    failMsg.add("第" + rowNum + "行" + rowValidateMsg);
                    continue;
                }
                String key = info.getEntCode() + "||" + info.getMaterialCode();
                if (fileDedup.containsKey(key)) {
                    ok[i] = false;
                    fail++;
                    failMsg.add("第" + rowNum + "行同企业下物资编号在导入文件中重复");
                    continue;
                }
                fileDedup.put(key, i);
                entToCodes.computeIfAbsent(info.getEntCode(), k -> new ArrayList<>()).add(info.getMaterialCode());
            }

            Map<String, Boolean> existsMap = new HashMap<>();
            for (Map.Entry<String, List<String>> e : entToCodes.entrySet()) {
                List<String> codes = e.getValue();
                if (codes == null || codes.isEmpty()) {
                    continue;
                }
                List<String> exists = materialInfoMapper.selectExistingMaterialCodes(e.getKey(), codes);
                if (exists != null) {
                    for (String c : exists) {
                        existsMap.put(e.getKey() + "||" + c, true);
                    }
                }
            }
            for (int i = 0; i < parsed.size(); i++) {
                if (!ok[i]) {
                    continue;
                }
                MaterialInfo info = parsed.get(i);
                Integer rowNum = parsedRowNums.get(i);
                if (Boolean.TRUE.equals(existsMap.get(info.getEntCode() + "||" + info.getMaterialCode()))) {
                    ok[i] = false;
                    fail++;
                    failMsg.add("第" + rowNum + "行同企业下物资编号已存在");
                }
            }

            List<MaterialInfo> toInsert = new ArrayList<>();
            List<Integer> toInsertRowNums = new ArrayList<>();
            for (int i = 0; i < parsed.size(); i++) {
                if (!ok[i]) {
                    continue;
                }
                MaterialInfo info = parsed.get(i);
                if (info.getStatus() == null) {
                    info.setStatus(0);
                }
                info.setMaterialId(UlidCreator.getMonotonicUlid().toString());
                toInsert.add(info);
                toInsertRowNums.add(parsedRowNums.get(i));
            }

            int batchSize = 200;
            for (int start = 0; start < toInsert.size(); start += batchSize) {
                int end = Math.min(start + batchSize, toInsert.size());
                List<MaterialInfo> chunk = toInsert.subList(start, end);
                List<Integer> chunkRowNums = toInsertRowNums.subList(start, end);
                try {
                    materialInfoMapper.batchInsertMaterialInfo(chunk);
                    success += chunk.size();
                } catch (Exception ex) {
                    for (int j = 0; j < chunk.size(); j++) {
                        MaterialInfo info = chunk.get(j);
                        Integer rowNum = chunkRowNums.get(j);
                        try {
                            materialInfoMapper.insertMaterialInfo(info);
                            success++;
                        } catch (RuntimeException e) {
                            fail++;
                            failMsg.add("第" + rowNum + "行" + e.getMessage());
                        }
                    }
                }
            }
        } catch (Exception e) {
            return AjaxResult.error("导入失败：" + e.getMessage());
        }
        Map<String, Object> result = new HashMap<>();
        result.put("successCount", success);
        result.put("failCount", fail);
        result.put("failMessages", failMsg);
        return AjaxResult.success(result);
    }

    private MaterialInfoReq initReq(MaterialInfoReq req) {
        if (req == null) {
            req = new MaterialInfoReq();
        }
        if (GVarContainer.isNotAdmin()) {
            List<String> entCodes = GVarContainer.getEntCodes();
            if (entCodes.isEmpty()) {
                return null;
            }
            if (StringUtils.isNotEmpty(req.getEntCode())) {
                if (!entCodes.contains(req.getEntCode())) {
                    return null;
                }
            } else {
                req.setEntCodes(entCodes);
            }
        }
        return req;
    }

    private boolean notEntPermission(String entCode) {
        if (StringUtils.isEmpty(entCode)) {
            return true;
        }
        if (GVarContainer.isAdmin()) {
            return false;
        }
        List<String> entCodes = GVarContainer.getEntCodes();
        return !entCodes.contains(entCode);
    }

    private void fillCurrentQty(List<MaterialInfo> list) {
        if (list == null || list.isEmpty()) {
            return;
        }
        List<String> materialIds = list.stream()
                .map(MaterialInfo::getMaterialId)
                .filter(StringUtils::isNotEmpty)
                .distinct()
                .collect(Collectors.toList());
        if (materialIds.isEmpty()) {
            return;
        }
        List<MaterialStock> stockList = materialStockMapper.selectMaterialCurrentQtyByMaterialIds(materialIds);
        if (null == stockList || stockList.isEmpty()) {
            return;
        }
        Map<String, Double> qtyMap = new HashMap<>();
        stockList.forEach(s -> {
            if (StringUtils.isNotEmpty(s.getMaterialId())) {
                qtyMap.put(s.getMaterialId(), s.getCurrentQty() == null ? 0D : s.getCurrentQty());
            }
        });
        list.forEach(item -> item.setCurrentQty(qtyMap.getOrDefault(item.getMaterialId(), 0D)));
    }

    private String validateMaterialForSave(MaterialInfo info, String excludeMaterialId) {
        if (info.getUnitPrice() != null && info.getUnitPrice() < 0) {
            return "单价不能小于0";
        }
        if (info.getMinStock() != null && info.getMinStock() < 0) {
            return "最低库存预警值不能小于0";
        }
        if (StringUtils.isEmpty(info.getEntCode())) {
            return "企业编码不能为空";
        }
        if (StringUtils.isEmpty(info.getMaterialCode())) {
            return "物资编号不能为空";
        }
        if (existsMaterialCode(info.getEntCode(), info.getMaterialCode(), excludeMaterialId)) {
            return "同企业下物资编号已存在";
        }
        return null;
    }

    private String validateImportRow(MaterialInfo info) {
        if (StringUtils.isEmpty(info.getEntCode())) {
            return "企业编码不能为空";
        }
        if (notEntPermission(info.getEntCode())) {
            return "无权限导入该企业数据";
        }
        if (StringUtils.isEmpty(info.getMaterialCode())) {
            return "物资编号不能为空";
        }
        if (StringUtils.isEmpty(info.getMaterialName())) {
            return "物资名称不能为空";
        }
        if (StringUtils.isEmpty(info.getUnit())) {
            return "单位不能为空";
        }
        if (info.getUnitPrice() != null && info.getUnitPrice() < 0) {
            return "单价不能小于0";
        }
        if (info.getMinStock() != null && info.getMinStock() < 0) {
            return "最低库存预警值不能小于0";
        }
        return null;
    }

    private boolean existsMaterialCode(String entCode, String materialCode, String excludeMaterialId) {
        MaterialInfoReq req = new MaterialInfoReq();
        req.setEntCode(entCode);
        req.setMaterialCode(materialCode);
        List<MaterialInfo> list = materialInfoMapper.selectMaterialInfoList(req);
        if (list == null || list.isEmpty()) {
            return false;
        }
        for (MaterialInfo item : list) {
            if (materialCode.equals(item.getMaterialCode())
                    && (excludeMaterialId == null || !excludeMaterialId.equals(item.getMaterialId()))) {
                return true;
            }
        }
        return false;
    }

    private boolean isEmptyRow(Row row, DataFormatter formatter) {
        if (row == null) {
            return true;
        }
        for (int i = 0; i <= 11; i++) {
            if (StringUtils.isNotEmpty(readCell(row, i, formatter))) {
                return false;
            }
        }
        return true;
    }

    private String readCell(Row row, int index, DataFormatter formatter) {
        if (row == null || row.getCell(index) == null) {
            return null;
        }
        String value = formatter.formatCellValue(row.getCell(index));
        return StringUtils.isEmpty(value) ? null : value.trim();
    }

    private Double parseDouble(String value, String errMsg) {
        if (StringUtils.isEmpty(value)) {
            return null;
        }
        try {
            return Double.parseDouble(value);
        } catch (Exception e) {
            throw new RuntimeException(errMsg);
        }
    }

    private Integer parseStatus(String value, String errMsg) {
        if (StringUtils.isEmpty(value)) {
            return null;
        }
        if ("启用".equals(value)) {
            return 0;
        }
        if ("停用".equals(value)) {
            return 1;
        }
        if ("0".equals(value) || "1".equals(value)) {
            return Integer.parseInt(value);
        }
        throw new RuntimeException(errMsg);
    }

    private String defaultVal(String value) {
        return value == null ? "" : value;
    }

    private String stringVal(Object value) {
        if (value == null) {
            return "";
        }
        if (value instanceof LocalDateTime time) {
            return time.format(DateUtils.dtf);
        }
        return value.toString();
    }

    private String numberVal(Double value) {
        return value == null ? "" : value.toString();
    }

    private String statusDesc(Integer status) {
        return Integer.valueOf(1).equals(status) ? "停用" : "启用";
    }

    private String formatTime(LocalDateTime time) {
        return time == null ? null : time.format(DateUtils.dtf);
    }
}
