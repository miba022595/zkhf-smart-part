package com.zkhf.epmis.platform.plc.service.impl;

import com.alibaba.fastjson2.JSONObject;
import com.zkhf.epmis.core.annotation.Log;
import com.zkhf.epmis.core.domain.AjaxResult;
import com.zkhf.epmis.core.enums.BusinessType;
import com.zkhf.epmis.core.enums.PlcDataTypeEnum;
import com.zkhf.epmis.core.enums.PlcPointTypeEnum;
import com.zkhf.epmis.core.utils.CellUtils;
import com.zkhf.epmis.core.utils.DateUtils;
import com.zkhf.epmis.core.utils.MimeTypeUtils;
import com.zkhf.epmis.core.utils.StringUtils;
import com.zkhf.epmis.platform.ent.domain.EnterprisePart;
import com.zkhf.epmis.platform.ent.service.EnterpriseService;
import com.zkhf.epmis.platform.global.GVarContainer;
import com.zkhf.epmis.platform.mapper.plc.PlcMapper;
import com.zkhf.epmis.platform.plc.domain.PlcInfo;
import com.zkhf.epmis.platform.plc.service.PlcService;
import com.zkhf.epmis.platform.utils.ExcelUtils;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.HorizontalAlignment;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.OutputStream;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
public class PlcServiceImpl implements PlcService {

    private PlcMapper plcMapper;
    @Autowired
    public void setPlcMapper(PlcMapper plcMapper) {
        this.plcMapper = plcMapper;
    }

    private EnterpriseService enterpriseService;
    @Autowired
    public void setEnterpriseService(EnterpriseService enterpriseService) {
        this.enterpriseService = enterpriseService;
    }

    @Override
    public List<PlcInfo> plcPointList(String entCode) {
        List<PlcInfo> list;
        if (StringUtils.isNotEmpty(entCode)) {
            list = plcMapper.selectEntPlcItemList(entCode, enable);
        } else {
            list = new ArrayList<>();
        }
        if (!list.isEmpty()) {
            list.forEach( e -> {
                e.setPointTypeDesc(PlcPointTypeEnum.getDescByType(e.getPointType()));
                e.setDataTypeDesc(PlcDataTypeEnum.getDescByType(e.getDataType()));
            });
        }
        return list;
    }

    @Override
    public AjaxResult selectEntPlcList(JSONObject req) {
        List<PlcInfo> list = selectPlcList(req);
        AjaxResult result = AjaxResult.success(list);
        result.put("edit", false);
        if (null != req) {
            result.put("edit", req.getBooleanValue("edit"));
        }
        return result;
    }

    @Override
    @Log(title = "企业PLC设备点位", businessType = BusinessType.EXPORT)
    public void exportEntPlcList(JSONObject req, HttpServletResponse response) {
        OutputStream outputStream = null;
        try {
            XSSFWorkbook workbook = ExcelUtils.getSheetAt("企业PLC设备点位模板.xlsx");
            if (workbook == null) {
                return;
            }
            // 权限
            List<PlcInfo> list = selectPlcList(req);
            if (!list.isEmpty()) {
                Sheet sheet = workbook.getSheetAt(0);
                int rowIndex = 3;// 首行

                // 获取数字单元格格式
                CellStyle infoStyle = CellUtils.getCellStyle(workbook, sheet, 1, 2);
                infoStyle.setAlignment(HorizontalAlignment.LEFT); // 改为左对齐
                CellStyle intStyle = CellUtils.getCellStyle(workbook, sheet, rowIndex, 0, 0);
                CellStyle decStyle = CellUtils.getCellStyle(workbook, sheet, rowIndex, 0, 6);
                // 获取单元格格式
                CellStyle style = CellUtils.getCellStyle(workbook, sheet, rowIndex);
                int index = 1;
                Row row;
                // 企业名称
                String rootEntName = list.get(0).getEntName();
                if (StringUtils.isNotEmpty(rootEntName)) { // 直接填入数据，不需要创建单元格
                    sheet.getRow(1).getCell(2).setCellValue(rootEntName);
                }
                // 行移动（获取单元格样式之后）
                CellUtils.shiftRows(sheet, rowIndex, list.size());
                for (PlcInfo info : list) {
                    row = sheet.createRow(rowIndex++);

                    int cellIndex = 0;
                    // 序号
                    CellUtils.setIntegerVal(row, cellIndex++, index++, intStyle);
                    // 归属排口编码
                    CellUtils.setStringVal(row, cellIndex++, info.getOutPutCode(), style);
                    // 排口名称
                    CellUtils.setStringVal(row, cellIndex++, info.getOutPutName(), style);
                    // PLC单元编号
                    CellUtils.setIntegerVal(row, cellIndex++, info.getUnitId(), intStyle);
                    // 排序号
                    CellUtils.setIntegerVal(row, cellIndex++, info.getSortOrder(), intStyle);
                    // 点位名称
                    CellUtils.setStringVal(row, cellIndex++, info.getPointName(), style);
                    // 点位类型
                    CellUtils.setStringVal(row, cellIndex++, info.getPointTypeDesc(), style);
                    // 数据类型
                    CellUtils.setStringVal(row, cellIndex++, info.getDataTypeDesc(), style);
                    // 硬件地址
                    CellUtils.setIntegerVal(row, cellIndex++, info.getAddress(), intStyle);
                    // 通信地址
                    CellUtils.setIntegerVal(row, cellIndex++, info.getRegisterAddress(), intStyle);
                    // 转换系数
                    CellUtils.setBigDecimalVal(row, cellIndex++, info.getCoefficient(), decStyle);
                    // 计量单位
                    CellUtils.setStringVal(row, cellIndex++, info.getUnit(), style);
                    // 量程最小值
                    CellUtils.setBigDecimalVal(row, cellIndex++, info.getMinValue(), decStyle);
                    // 量程最大值
                    CellUtils.setBigDecimalVal(row, cellIndex++, info.getMaxValue(), decStyle);
                    // 小数精度
                    CellUtils.setIntegerVal(row, cellIndex++, info.getPrecision(), intStyle);
                    // 状态
                    CellUtils.setStringVal(row, cellIndex++, enable.equals(info.getStatus()) ? "启用" : "禁用", style);
                    // 更新时间
                    CellUtils.setLocalDateTimeStr(row, cellIndex++, info.getUpdateTime(), DateUtils.yy_m_d_h_m_s, style);
                    // 描述
                    CellUtils.setStringVal(row, cellIndex, info.getDescription(), style);
                }
            }
            response.setContentType(MimeTypeUtils.EXCEL_XLSX);
            response.setHeader("Content-Disposition", "attachment;filename*=UTF-8''" + URLEncoder.encode("企业PLC设备点位.xlsx", StandardCharsets.UTF_8));
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

    private List<PlcInfo> selectPlcList(JSONObject req) {
        List<PlcInfo> list = new ArrayList<>();
        if (null == req) {
            return list;
        }
        req.put("edit", false);
        String entCode = req.getString("entCode");
        if (StringUtils.isEmpty(entCode)) {
            return list;
        }
        Map<String, List<String>> rootEnt = organizeEnterpriseParts();
        if (GVarContainer.isNotAdmin()) {
            // 判断是不是根，并且有根下的全部权限，才能获取全部点位，否则只能获取自己的点位
            List<String> entCodes = GVarContainer.getEntCodes();
            if (!entCodes.contains(entCode)) {
                // 无企业权限
                return new ArrayList<>();
            }
            List<String> allItem = rootEnt.get(entCode);
            if (null != allItem) {
                // allItem为空时表示企业无子级
                for (String code : entCodes) {
                    allItem.remove(code);
                }
            }
            // allItem不为null表示是根企业，到这里为空表示权限包含所有子企业
            if (null != allItem && allItem.isEmpty()) {
                list = plcMapper.selectEntPlcRootList(entCode);
                req.put("edit", true);
            } else {
                list = plcMapper.selectEntPlcItemList(entCode, null);
            }
        } else {
            if (rootEnt.containsKey(entCode)) {
                list = plcMapper.selectEntPlcRootList(entCode);
                req.put("edit", true);
            } else {
                list = plcMapper.selectEntPlcItemList(entCode, null);
            }
        }
        if (null != list && !list.isEmpty()) {
            list.forEach( e -> {
                e.setPointTypeDesc(PlcPointTypeEnum.getDescByType(e.getPointType()));
                e.setDataTypeDesc(PlcDataTypeEnum.getDescByType(e.getDataType()));
            });
        } else {
            list = new ArrayList<>();
        }
        return list;
    }

    @Override
    @Log(title = "企业PLC设备单个点位", businessType = BusinessType.INSERT)
    public AjaxResult updateEntPlc(PlcInfo plc) {
        if (null == plc) {
            return AjaxResult.error("更新数据为空");
        }
        if (null == plc.getId()) {
            return AjaxResult.error("更新数据ID为空");
        }
        // 用ID查出所属企业全部点位列表
        List<PlcInfo> allList = plcMapper.selectEntPlcListById(plc.getId());
        if (null == allList || allList.isEmpty()) {
            return AjaxResult.error("点位信息不存在");
        }
        // 1. 基础参数校验：小数精度范围
        if (null != plc.getPrecision() && (plc.getPrecision() < 0 || plc.getPrecision() > 6)) {
            return AjaxResult.error("小数精度不准确");
        }
        // 2. 枚举类型校验
        if (null != plc.getPointType() && !PlcPointTypeEnum.containsType(plc.getPointType())) {
            return AjaxResult.error("未知的点位类型");
        }
        if (null != plc.getDataType() && !PlcDataTypeEnum.containsType(plc.getDataType())) {
            return AjaxResult.error("未知的数据类型");
        }
        // 3. 业务规则校验：同一单元内地址唯一性
        // 3.1 PLC绝对地址唯一性校验
        if (null != plc.getAddress()) {
            for (PlcInfo p : allList) {
                if (!p.getId().equals(plc.getId()) && plc.getAddress().equals(p.getAddress())) {
                    return AjaxResult.error("同一单元内PLC绝对地址唯一");
                }
            }
        }
        // 3.2 Modbus寄存器地址唯一性校验
        if (null != plc.getRegisterAddress()) {
            for (PlcInfo p : allList) {
                if (!p.getId().equals(plc.getId()) && plc.getRegisterAddress().equals(p.getRegisterAddress())) {
                    return AjaxResult.error("同一单元内Modbus寄存器地址唯一");
                }
            }
        }
        // 4. 状态默认值设置
        if (null != plc.getStatus() && !enable.equals(plc.getStatus())) {
            plc.setStatus(disable);
        }
        int count = plcMapper.updatePlc(plc);
        return AjaxResult.success(count);
    }

    @Override
    @Log(title = "企业PLC设备点位", businessType = BusinessType.INSERT)
    public AjaxResult updateEntPlc(JSONObject req) {
        if (null == req) {
            return AjaxResult.error("更新数据为空");
        }
        String rootCode = req.getString("entCode");
        if (StringUtils.isEmpty(rootCode)) {
            return AjaxResult.error("根企业未指定");
        }
        // 保证一个根企业（集团）分配一个PLC单元编号
        Integer unitId = req.getInteger("unitId");
        if (null == unitId || unitId < 1 || unitId > 255) {
            return AjaxResult.error("PLC单元编号不规范");
        }
        List<PlcInfo> plcList = req.getList("plcList", PlcInfo.class);
        if (null == plcList) {
            plcList = new ArrayList<>();
        }
        Map<String, List<String>> rootEnt = organizeEnterpriseParts();
        List<String> entCodes = null;
        if (GVarContainer.isNotAdmin()) {
            entCodes = GVarContainer.getEntCodes();
        }
        // 1. 根企业存在性校验
        List<String> allItem = rootEnt.get(rootCode);
        if (null == allItem) {
            return AjaxResult.error("只能根企业构建设备点位");
        }
        // 2. 权限校验
        if (null != entCodes) {
            if (!entCodes.contains(rootCode)) {
                return AjaxResult.error("无根企业权限");
            }
            // allItem为空时表示企业无子级
            for (String entCode : allItem) {
                if (!entCodes.contains(entCode)) {
                    return AjaxResult.error("权限不足");
                }
            }
        }
        /*
         * 一个企业绑定一个PLC单元编号
         * 同一单元内PLC绝对地址唯一
         * 同一单元内Modbus寄存器地址唯一
         */
        Map<Integer, List<Integer>> unitAddr = new HashMap<>();
        Map<Integer, List<Integer>> unitReAddr = new HashMap<>();
        List<Integer> checkList;
        // 基础校验、权限校验、业务规则校验
        for (PlcInfo plc : plcList) {
            plc.setEntCode(rootCode);
            plc.setUnitId(unitId);
            // 3. 基础参数校验
            if (null != plc.getPrecision() && (plc.getPrecision() < 0 || plc.getPrecision() > 6)) {
                return AjaxResult.error("小数精度不准确");
            }
            // 4. 枚举类型校验
            if (null != plc.getPointType() && !PlcPointTypeEnum.containsType(plc.getPointType())) {
                return AjaxResult.error("未知的点位类型");
            }
            if (null != plc.getDataType() && !PlcDataTypeEnum.containsType(plc.getDataType())) {
                return AjaxResult.error("未知的数据类型");
            }
            // 5. 业务规则校验（同一提交数据内的唯一性）
            // 5.1 PLC绝对地址唯一性校验
            if (null != plc.getAddress()) {
                checkList = unitAddr.computeIfAbsent(unitId, k -> new ArrayList<>());
                if (checkList.contains(plc.getAddress())) {
                    return AjaxResult.error("同一单元内PLC绝对地址唯一");
                }
                checkList.add(plc.getAddress());
            }
            // 5.2 Modbus寄存器地址唯一性校验
            if (null != plc.getRegisterAddress()) {
                checkList = unitReAddr.computeIfAbsent(unitId, k -> new ArrayList<>());
                if (checkList.contains(plc.getRegisterAddress())) {
                    return AjaxResult.error("同一单元内Modbus寄存器地址唯一");
                }
                checkList.add(plc.getRegisterAddress());
            }
            // 6. 状态默认值设置
            if (!enable.equals(plc.getStatus())) {
                plc.setStatus(disable);
            }
        }
        // 7. 数据库存在性校验（与已存在数据的冲突校验）
        String oldRootCode = plcMapper.selectEntCodeByUnitId(rootCode, unitId);
        if (StringUtils.isNotEmpty(oldRootCode)) {
            return AjaxResult.error("PLC单元编号已被企业使用，请确认");
        }
        // 8. 数据更新操作
        plcMapper.deleteEntPlc(rootCode);
        int count = 0;
        if (!plcList.isEmpty()) {
            count = plcMapper.batchInsertEntPlc(plcList);
        }
        return AjaxResult.success(count);
    }

    /**
     * 整理企业层级关系，返回根节点及其所有下级的映射
     * 根节点定义：parentCode为null，或者parentCode找不到对应的企业
     *
     * @return Map<根节点code, 该根节点下的所有子级code列表（包含多级）>
     */
    private Map<String, List<String>> organizeEnterpriseParts() {
        Map<String, List<String>> result = new HashMap<>();
        // 依据根ent获取所有的，用于后续判断
        List<EnterprisePart> entList = enterpriseService.listAll();
        if (entList == null || entList.isEmpty()) {
            return result;
        }
        // 1. 构建企业编码到企业的映射
        Map<String, EnterprisePart> entCodeMap = new HashMap<>();
        // 2. 构建父编码到子企业列表的映射（用于快速查找下级）
        Map<String, List<EnterprisePart>> parentToChildrenMap = new HashMap<>();
        for (EnterprisePart part : entList) {
            if (StringUtils.isEmpty(part.getEntCode())) {
                continue;
            }
            entCodeMap.put(part.getEntCode(), part);
            if (StringUtils.isEmpty(part.getParentCode())) {
                continue;
            }
            if (!parentToChildrenMap.containsKey(part.getParentCode())) {
                parentToChildrenMap.put(part.getParentCode(), new ArrayList<>());
            }
            parentToChildrenMap.get(part.getParentCode()).add(part);
        }
        // 3. 找出所有根节点（parentCode为null或找不到parentCode的企业）
        List<EnterprisePart> rootNodes = entList.stream()
                .filter(part -> StringUtils.isEmpty(part.getParentCode()) || !entCodeMap.containsKey(part.getParentCode()))
                .collect(Collectors.toList());
        // 5. 为每个根节点收集所有下级（包含多级）
        for (EnterprisePart root : rootNodes) {
            String rootCode = root.getEntCode();
            List<String> allDescendants = new ArrayList<>();

            // 收集根节点下的所有级联下级
            collectAllDescendants(rootCode, parentToChildrenMap, allDescendants);

            // 放入Map（即使没有下级，也放入空列表）
            result.put(rootCode, allDescendants);
        }
        return result;
    }

    /**
     * 递归收集指定企业编码的所有下级code（包含多级）
     *
     * @param parentCode 上级企业编码
     * @param parentToChildrenMap 父编码到子企业列表的映射
     * @param result 结果列表，用于收集所有下级code
     */
    private void collectAllDescendants(String parentCode,
                                       Map<String, List<EnterprisePart>> parentToChildrenMap,
                                       List<String> result) {
        List<EnterprisePart> children = parentToChildrenMap.get(parentCode);
        if (children == null || children.isEmpty()) {
            return;
        }

        // 添加直接下级
        for (EnterprisePart child : children) {
            String childCode = child.getEntCode();
            result.add(childCode);

            // 递归添加下级的下级
            collectAllDescendants(childCode, parentToChildrenMap, result);
        }
    }
}

