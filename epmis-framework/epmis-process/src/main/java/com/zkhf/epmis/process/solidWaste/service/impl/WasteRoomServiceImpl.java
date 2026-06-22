package com.zkhf.epmis.process.solidWaste.service.impl;

import cn.hutool.json.JSONObject;
import com.github.f4b6a3.ulid.UlidCreator;
import com.zkhf.epmis.core.annotation.Log;
import com.zkhf.epmis.core.domain.AjaxResult;
import com.zkhf.epmis.core.enums.AnnexTypeEnum;
import com.zkhf.epmis.core.enums.BusinessType;
import com.zkhf.epmis.core.utils.CellUtils;
import com.zkhf.epmis.core.utils.MimeTypeUtils;
import com.zkhf.epmis.core.utils.NumberUtils;
import com.zkhf.epmis.core.utils.PageUtils;
import com.zkhf.epmis.core.utils.StringUtils;
import com.zkhf.epmis.process.base.domain.EntInfo;
import com.zkhf.epmis.process.base.domain.OutPutInfo;
import com.zkhf.epmis.process.base.utils.ExcelUtils;
import com.zkhf.epmis.process.base.utils.RedisCacheUtils;
import com.zkhf.epmis.process.facade.platform.PlatformFacade;
import com.zkhf.epmis.process.global.GVarContainer;
import com.zkhf.epmis.process.mapper.solidWaste.WasteRoomMapper;
import com.zkhf.epmis.process.solidWaste.domain.WasteRoom;
import com.zkhf.epmis.process.solidWaste.domain.WasteRoomReq;
import com.zkhf.epmis.process.solidWaste.service.WasteRoomService;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
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
import java.util.*;

/**
 * 固废间管理Service业务层处理
 */
@Slf4j
@Service
public class WasteRoomServiceImpl implements WasteRoomService {

    private WasteRoomMapper wasteRoomMapper;

    @Autowired
    public void setWasteRoomMapper(WasteRoomMapper wasteRoomMapper) {
        this.wasteRoomMapper = wasteRoomMapper;
    }

    private PlatformFacade platformFacade;
    @Autowired
    public void setPlatformFacade(PlatformFacade platformFacade) {
        this.platformFacade = platformFacade;
    }

    private RedisCacheUtils redisCacheUtils;
    @Autowired
    public void setRedisCacheUtils(RedisCacheUtils redisCacheUtils) {
        this.redisCacheUtils = redisCacheUtils;
    }

    @Override
    public AjaxResult selectWasteRoomList(WasteRoomReq req) {
        // 请求参数转换
        req = initReq(req);
        if (null == req) {
            return AjaxResult.success(new ArrayList<>());
        }
        // 固废间管理列表查询
        boolean page = PageUtils.startPageCheckExists();
        List<WasteRoom> list = wasteRoomMapper.selectWasteRoomList(req);
        // 填充信息
        fill(list);
        return PageUtils.getAjaxResult(list, page);
    }

    @Override
    @Log(title = "固废间管理", businessType = BusinessType.EXPORT)
    public void exportWasteRoom(WasteRoomReq req, HttpServletResponse response) {
        // 请求参数转换
        req = initReq(req);
        OutputStream outputStream = null;
        try {
            XSSFWorkbook workbook = ExcelUtils.getSheetAt("固废间列表模板.xlsx");
            if (workbook == null) {
                return;
            }
            List<WasteRoom> list = null;
            if (null != req) {
                list = wasteRoomMapper.selectWasteRoomList(req);
            }
            if (null != list && !list.isEmpty()) {
                // 填充内容
                fill(list);
                Sheet sheet = workbook.getSheetAt(0);
                int rowIndex = 3;// 从第2行开始插入
                CellStyle style = CellUtils.getCellStyle(workbook, sheet, rowIndex);
                CellStyle styleN0 = CellUtils.getCellStyle(workbook, sheet, rowIndex, 0,0);
                CellStyle styleN6 = CellUtils.getCellStyle(workbook, sheet, rowIndex, 0,6);
                Row row;
                int index = 1;
                // 行移动（获取单元格样式之后）
                CellUtils.shiftRows(sheet, rowIndex, list.size());
                for (WasteRoom room : list) {
                    row = sheet.createRow(rowIndex++);

                    int cellIndex = 0;
                    // 序号
                    CellUtils.setIntegerVal(row, cellIndex++, index++, styleN0);
                    // 企业名称
                    CellUtils.setStringVal(row, cellIndex++, room.getEntName(), style);
                    // 归属排口
                    CellUtils.setStringVal(row, cellIndex++, room.getOutPutCode(), style);
                    // 归属排口
                    CellUtils.setStringVal(row, cellIndex++, room.getOutPutName(), style);
                    // 固/危废间名称
                    CellUtils.setStringVal(row, cellIndex++, room.getRoomName(), style);
                    // 贮存间编码
                    CellUtils.setStringVal(row, cellIndex++, room.getRoomCode(), style);
                    // 废物类型
                    CellUtils.setStringVal(row, cellIndex++, room.getWasteTypeDesc(), style);
                    // 贮存间类型
                    CellUtils.setStringVal(row, cellIndex++, room.getRoomTypeDesc(), style);
                    // 最大存放容量(t)
                    CellUtils.setDoubleVal(row, cellIndex++, room.getMaxCapacity(), styleN6);
                    // 当前库存(t)
                    CellUtils.setDoubleVal(row, cellIndex++, room.getCurrCapacity(), styleN6);
                    // 库存预警阈值(t)
                    CellUtils.setDoubleVal(row, cellIndex++, room.getWarnLimit(), styleN6);
                    // 面积(㎡)
                    CellUtils.setDoubleVal(row, cellIndex++, room.getArea(), styleN6);
                    // 是否安装摄像头（0否 1是）
                    CellUtils.setStringVal(row, cellIndex++, hasSign.equals(room.getHasCamera()) ? "是" : "否", style);
                    // 是否防渗（0否 1是）
                    CellUtils.setStringVal(row, cellIndex++, hasSign.equals(room.getHasLeakProof()) ? "是" : "否", style);
                    // 是否通风（0否 1是）
                    CellUtils.setStringVal(row, cellIndex++, hasSign.equals(room.getHasVentilation()) ? "是" : "否", style);
                    // 是否有消防设施（0否 1是）
                    CellUtils.setStringVal(row, cellIndex++, hasSign.equals(room.getHasFireControl()) ? "是" : "否", style);
                    // 是否有应急物资（0否 1是）
                    CellUtils.setStringVal(row, cellIndex++, hasSign.equals(room.getHasEmergencySupplies()) ? "是" : "否", style);
                    // 经度
                    CellUtils.setDoubleVal(row, cellIndex++, room.getLongitude(), styleN6);
                    // 纬度
                    CellUtils.setDoubleVal(row, cellIndex++, room.getLatitude(), styleN6);
                    // 备注
                    CellUtils.setStringVal(row, cellIndex, room.getRemark(), style);
                }
            }
            response.setContentType(MimeTypeUtils.EXCEL_XLSX);
            response.setHeader("Content-Disposition", "attachment;filename*=UTF-8''" + URLEncoder.encode("固废间列表.xlsx", StandardCharsets.UTF_8));
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

    private void fill(List<WasteRoom> list) {
        if (null == list || list.isEmpty()) {
            return;
        }
        List<String> roomIds = new ArrayList<>();
        list.forEach(e -> {
            if (StringUtils.isNotEmpty(e.getRoomId())) {
                roomIds.add(e.getRoomId());
            }
        });
        Map<String, Double> capacityMap = new HashMap<>();
        if (!roomIds.isEmpty()) {
            List<Map<String, Object>> capacityList = wasteRoomMapper.selectRoomCapacityBatch(roomIds);
            if (null != capacityList && !capacityList.isEmpty()) {
                for (Map<String, Object> map : capacityList) {
                    String roomId = (String) map.get("room_id");
                    Double capacity = ((Number) map.get("curr_capacity")).doubleValue();
                    capacityMap.put(roomId, capacity);
                }
            }
        }
        /*
        roomType: 贮存间类型, 字典 storage_room_type
        wasteType: 废物类型，字典  waste_type
        */
        List<String> dictTypes = Arrays.asList("storage_room_type", "waste_type");
        Map<String, Map<String, String>> dictMap = platformFacade.selectDataMapByTypes(dictTypes);
        List<EntInfo> entList = redisCacheUtils.getAllEntList();
        Map<String, EntInfo> entMap = new HashMap<>();
        if (null != entList && !entList.isEmpty()) {
            entList.forEach( e -> {
                if (StringUtils.isNotEmpty(e.getEntCode())) {
                    entMap.put(e.getEntCode(), e);
                }
            });
        }
        List<OutPutInfo> outList = redisCacheUtils.getAllOutPutList();
        Map<String, OutPutInfo> outMap = new HashMap<>();
        if (null != outList && !outList.isEmpty()) {
            outList.forEach( e -> {
                if (StringUtils.isNotEmpty(e.getOutPutId())) {
                    outMap.put(e.getOutPutId(), e);
                }
            });
        }
        list.forEach( e -> {
            if (capacityMap.containsKey(e.getRoomId())) {
                e.setCurrCapacity(NumberUtils.round(capacityMap.get(e.getRoomId()), dataScale));
            }
            EntInfo ent = entMap.get(e.getEntCode());
            if (null != ent) {
                e.setEntName(ent.getEntName());
                e.setEntDirectorName(ent.getEntDirectorName());
                e.setEntDirectorPhone(ent.getEntDirectorPhone());
                e.setEntDirectorEmail(ent.getEntDirectorEmail());
            }
            OutPutInfo out = outMap.get(e.getOutPutId());
            if (null != out) {
                e.setOutPutCode(out.getOutPutCode());
                e.setOutPutName(out.getOutPutName());
            }
            Map<String, String> sub = dictMap.get("storage_room_type");
            if (null != sub && sub.containsKey(e.getRoomType())) {
                e.setRoomTypeDesc(sub.get(e.getRoomType()));
            }
            sub = dictMap.get("waste_type");
            if (null != sub && sub.containsKey(e.getWasteType())) {
                e.setWasteTypeDesc(sub.get(e.getWasteType()));
            }
        });
    }

    private WasteRoomReq initReq(WasteRoomReq req) {
        if (null == req) {
            req = new WasteRoomReq();
        }
        // 添加权限
        if (GVarContainer.isNotAdmin()) {
            req.setEntCodes(GVarContainer.getEntCodes());
            if (null == req.getEntCodes() || req.getEntCodes().isEmpty()) {
                return null;
            }
            if (StringUtils.isNotEmpty(req.getEntCode()) && !req.getEntCodes().contains(req.getEntCode())) {
                return null;
            }
        }
        return req;
    }

    @Override
    @Log(title = "固废间管理", businessType = BusinessType.INSERT)
    public AjaxResult insertWasteRoom(WasteRoom info) {
        if (null == info) {
            return AjaxResult.error("未知的参数");
        }
        if (StringUtils.isEmpty(info.getEntCode())) {
            return AjaxResult.error("企业编码不能为空");
        }
        info.setRoomId(UlidCreator.getMonotonicUlid().toString());
        int count = wasteRoomMapper.insertWasteRoom(info);
        if (count > 0 && null != info.getAnnexIds() && !info.getAnnexIds().isEmpty()) {
            updateAnnex(info.getRoomId(), info.getAnnexIds());
        }
        return AjaxResult.success(count);
    }

    @Override
    @Log(title = "固废间管理", businessType = BusinessType.UPDATE)
    public AjaxResult updateWasteRoom(WasteRoom info) {
        if (null == info) {
            return AjaxResult.error("未知的参数");
        }
        // 判断是否被使用
        int count = wasteRoomMapper.selectUsedSizeRoom(info.getRoomId());
        if (count > 0) {
            return AjaxResult.error("该间已被使用，不能修改");
        }
        count = wasteRoomMapper.updateWasteRoom(info);
        if (count > 0) {
            updateAnnex(info.getRoomId(), info.getAnnexIds());
        }
        return AjaxResult.success(count);
    }

    @Override
    @Log(title = "固废间管理", businessType = BusinessType.DELETE)
    public AjaxResult deleteWasteRoom(WasteRoom info) {
        if (null == info) {
            return AjaxResult.error("未知的参数");
        }
        // 判断是否被使用
        int count = wasteRoomMapper.selectUsedSizeRoom(info.getRoomId());
        if (count > 0) {
            return AjaxResult.error("该间已被使用，不能删除");
        }
        count = wasteRoomMapper.deleteWasteRoomByRoomId(info.getRoomId());
        if (count > 0) {
            updateAnnex(info.getRoomId(), null);
        }
        return AjaxResult.success(count);
    }

    private void updateAnnex(String dealId, List<String> annexIds) {
        JSONObject req = new JSONObject();
        req.set("sourceId", dealId);
        req.set("sourceType", AnnexTypeEnum.wasteRoom.name);
        req.set("annexIds", annexIds);
        platformFacade.updateAnnex(req);
    }
}

