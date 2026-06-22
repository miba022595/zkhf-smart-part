package com.zkhf.epmis.process.material.service;

import cn.hutool.json.JSONObject;
import com.github.f4b6a3.ulid.UlidCreator;
import com.zkhf.epmis.core.domain.AnnexInfo;
import com.zkhf.epmis.core.domain.AnnexReq;
import com.zkhf.epmis.core.utils.MimeTypeUtils;
import com.zkhf.epmis.core.utils.StringUtils;
import com.zkhf.epmis.process.base.domain.EntInfo;
import com.zkhf.epmis.process.base.entity.SysUser;
import com.zkhf.epmis.process.base.utils.RedisCacheUtils;
import com.zkhf.epmis.process.facade.platform.PlatformFacade;
import com.zkhf.epmis.process.global.GVarContainer;
import com.zkhf.epmis.process.mapper.material.MaterialInfoMapper;
import com.zkhf.epmis.process.mapper.material.MaterialOperateLogMapper;
import com.zkhf.epmis.process.mapper.material.MaterialOrderMapper;
import com.zkhf.epmis.process.material.domain.*;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.OutputStream;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Slf4j
@Component
public class MaterialOrderSupport {

    private MaterialOrderMapper materialOrderMapper;
    @Autowired
    public void setMaterialOrderMapper(MaterialOrderMapper materialOrderMapper) {
        this.materialOrderMapper = materialOrderMapper;
    }

    private MaterialInfoMapper materialInfoMapper;
    @Autowired
    public void setMaterialInfoMapper(MaterialInfoMapper materialInfoMapper) {
        this.materialInfoMapper = materialInfoMapper;
    }

    private MaterialOperateLogMapper materialOperateLogMapper;
    @Autowired
    public void setMaterialOperateLogMapper(MaterialOperateLogMapper materialOperateLogMapper) {
        this.materialOperateLogMapper = materialOperateLogMapper;
    }

    private RedisCacheUtils redisCacheUtils;
    @Autowired
    public void setRedisCacheUtils(RedisCacheUtils redisCacheUtils) {
        this.redisCacheUtils = redisCacheUtils;
    }

    private PlatformFacade platformFacade;
    @Autowired
    public void setPlatformFacade(PlatformFacade platformFacade) {
        this.platformFacade = platformFacade;
    }

    /**
     * 初始化物资业务查询请求，并在非管理员场景下补充/校验企业权限范围。
     * <p>
     * - 若请求为空则创建默认请求对象；
     * - 非管理员时：若无任何可用企业编码则返回 null；若指定 entCode 且不在权限范围则返回 null；
     *   否则将可访问企业编码列表写入请求。
     * </p>
     *
     * @param req 业务请求
     * @return 初始化后的请求；无权限时返回 null
     */
    public MaterialBizReq initReq(MaterialBizReq req) {
        if (req == null) {
            req = new MaterialBizReq();
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

    /**
     * 判断当前登录用户是否对指定企业无权限。
     *
     * @param entCode 企业编码
     * @return true 表示无权限；false 表示有权限
     */
    public boolean noEntPermission(String entCode) {
        if (StringUtils.isEmpty(entCode)) {
            return true;
        }
        if (GVarContainer.isAdmin()) {
            return false;
        }
        List<String> entCodes = GVarContainer.getEntCodes();
        return !entCodes.contains(entCode);
    }

    /**
     * 生成业务单号。
     * <p>
     * 格式：prefix + yyyyMMdd + ULID 后 10 位（取子串）。
     * </p>
     *
     * @param prefix 业务前缀
     * @return 业务单号
     */
    public String buildBizNo(String prefix) {
        return prefix + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd")) + UlidCreator.getUlid().toString().substring(16, 26);
    }

    /**
     * 获取当前登录用户的展示名称。
     * <p>
     * 优先返回 nickName，其次 userName；异常或取不到用户时返回“系统”。
     * </p>
     *
     * @return 当前用户名称
     */
    public String getCurrentUserName() {
        try {
            SysUser user = GVarContainer.getLoginUser().getUser();
            if (user == null) {
                return "系统";
            }
            if (StringUtils.isNotEmpty(user.getNickName())) {
                return user.getNickName();
            }
            if (StringUtils.isNotEmpty(user.getUserName())) {
                return user.getUserName();
            }
        } catch (Exception ignored) {
        }
        return "系统";
    }

    /**
     * 为列表中的业务对象填充企业名称（根据 entCode 映射）。
     * <p>
     * 支持的对象类型：入库单、领用申请单、出库单、归还单、物资信息、库存、库存流水、仓库。
     * </p>
     *
     * @param list 业务对象列表
     */
    public void fillEntNameByCode(List<?> list) {
        if (list == null || list.isEmpty()) {
            return;
        }
        List<EntInfo> entList = redisCacheUtils.getAllEntList();
        if (entList == null || entList.isEmpty()) {
            return;
        }
        Map<String, String> entMap = new HashMap<>();
        entList.forEach(e -> entMap.put(e.getEntCode(), e.getEntName()));
        list.forEach(e -> {
            if (e instanceof MaterialInOrder o) {
                o.setEntName(entMap.get(o.getEntCode()));
            }
            if (e instanceof MaterialApplyOrder o) {
                o.setEntName(entMap.get(o.getEntCode()));
            }
            if (e instanceof MaterialOutOrder o) {
                o.setEntName(entMap.get(o.getEntCode()));
            }
            if (e instanceof MaterialReturnOrder o) {
                o.setEntName(entMap.get(o.getEntCode()));
            }
            if (e instanceof MaterialInfo o) {
                o.setEntName(entMap.get(o.getEntCode()));
            }
            if (e instanceof MaterialStock o) {
                o.setEntName(entMap.get(o.getEntCode()));
            }
            if (e instanceof MaterialStockFlow o) {
                o.setEntName(entMap.get(o.getEntCode()));
            }
            if (e instanceof MaterialWarehouse o) {
                o.setEntName(entMap.get(o.getEntCode()));
            }
        });
    }

    /**
     * 调整库存数量（当前库存与可用库存同时变更），并写入库存流水。
     * <p>
     * - 若库存不存在则初始化库存记录；
     * - qtyChange 为正表示入库/增加，为负表示出库/减少；
     * - 会校验变更后库存与可用库存不得为负。
     * </p>
     *
     * @param entCode     企业编码
     * @param warehouseId 仓库 ID
     * @param materialId  物资 ID
     * @param qtyChange   数量变更（可为负）
     * @param bizType     业务类型（写入库存流水）
     * @param bizId       业务单据 ID
     * @param bizItemId   业务明细 ID
     * @param bizNo       业务单号
     * @param remark      备注
     */
    public void adjustStock(String entCode, String warehouseId, String materialId, Double qtyChange, String bizType, String bizId, String bizItemId, String bizNo, String remark) {
        MaterialInfo material = materialInfoMapper.selectMaterialInfoById(materialId);
        MaterialStock stock = materialOrderMapper.selectMaterialStock(entCode, warehouseId, materialId);
        if (stock == null) {
            stock = new MaterialStock();
            stock.setStockId(UlidCreator.getMonotonicUlid().toString());
            stock.setEntCode(entCode);
            stock.setWarehouseId(warehouseId);
            stock.setMaterialId(materialId);
            stock.setCurrentQty(0D);
            stock.setAvailableQty(0D);
            stock.setFrozenQty(0D);
            stock.setMinStock(material == null || material.getMinStock() == null ? 0D : material.getMinStock());
            stock.setStockStatus(3);
            materialOrderMapper.insertMaterialStock(stock);
            stock = materialOrderMapper.selectMaterialStock(entCode, warehouseId, materialId);
        }
        double beforeQty = stock.getCurrentQty() == null ? 0D : stock.getCurrentQty();
        double beforeAvailable = stock.getAvailableQty() == null ? 0D : stock.getAvailableQty();
        double beforeFrozen = stock.getFrozenQty() == null ? 0D : stock.getFrozenQty();
        double afterQty = beforeQty + (qtyChange == null ? 0D : qtyChange);
        if (afterQty < 0) {
            throw new RuntimeException("物资库存不足");
        }
        double afterAvailable = beforeAvailable + (qtyChange == null ? 0D : qtyChange);
        if (afterAvailable < 0) {
            throw new RuntimeException("物资可用库存不足");
        }
        stock.setCurrentQty(afterQty);
        stock.setAvailableQty(afterAvailable);
        stock.setStockStatus(calcStockStatus(afterQty, stock.getMinStock()));
        stock.setLastChangeTime(LocalDateTime.now());
        materialOrderMapper.updateMaterialStock(stock);
        MaterialStockFlow flow = new MaterialStockFlow();
        flow.setFlowId(UlidCreator.getMonotonicUlid().toString());
        flow.setEntCode(entCode);
        flow.setWarehouseId(warehouseId);
        flow.setMaterialId(materialId);
        flow.setBizType(bizType);
        flow.setBizId(bizId);
        flow.setBizItemId(bizItemId);
        flow.setBizNo(bizNo);
        flow.setQtyChange(qtyChange);
        flow.setBeforeQty(beforeQty);
        flow.setAfterQty(afterQty);
        flow.setBeforeAvailableQty(beforeAvailable);
        flow.setAfterAvailableQty(afterAvailable);
        flow.setBeforeFrozenQty(beforeFrozen);
        flow.setAfterFrozenQty(beforeFrozen);
        flow.setOperateBy(getCurrentUserName());
        flow.setOperateTime(LocalDateTime.now());
        flow.setRemark(remark);
        materialOrderMapper.insertMaterialStockFlow(flow);
    }

    /**
     * 出库冻结：从可用库存扣减并增加冻结库存，写入冻结流水。
     * <p>
     * qty 小于等于 0 时不处理；若库存不存在则初始化库存记录。
     * </p>
     *
     * @param entCode     企业编码
     * @param warehouseId 仓库 ID
     * @param materialId  物资 ID
     * @param qty         冻结数量
     * @param bizId       业务单据 ID
     * @param bizItemId   业务明细 ID
     * @param bizNo       业务单号
     */
    public void freezeOut(String entCode, String warehouseId, String materialId, Double qty, String bizId, String bizItemId, String bizNo) {
        double v = qty == null ? 0D : qty;
        if (v <= 0D) {
            return;
        }
        MaterialInfo material = materialInfoMapper.selectMaterialInfoById(materialId);
        MaterialStock stock = materialOrderMapper.selectMaterialStock(entCode, warehouseId, materialId);
        if (stock == null) {
            stock = new MaterialStock();
            stock.setStockId(UlidCreator.getMonotonicUlid().toString());
            stock.setEntCode(entCode);
            stock.setWarehouseId(warehouseId);
            stock.setMaterialId(materialId);
            stock.setCurrentQty(0D);
            stock.setAvailableQty(0D);
            stock.setFrozenQty(0D);
            stock.setMinStock(material == null || material.getMinStock() == null ? 0D : material.getMinStock());
            stock.setStockStatus(3);
            materialOrderMapper.insertMaterialStock(stock);
            stock = materialOrderMapper.selectMaterialStock(entCode, warehouseId, materialId);
        }
        double beforeQty = stock.getCurrentQty() == null ? 0D : stock.getCurrentQty();
        double beforeAvailable = stock.getAvailableQty() == null ? 0D : stock.getAvailableQty();
        double beforeFrozen = stock.getFrozenQty() == null ? 0D : stock.getFrozenQty();
        double afterAvailable = beforeAvailable - v;
        if (afterAvailable < 0D) {
            throw new RuntimeException("物资可用库存不足");
        }
        double afterFrozen = beforeFrozen + v;
        stock.setAvailableQty(afterAvailable);
        stock.setFrozenQty(afterFrozen);
        stock.setStockStatus(calcStockStatus(beforeQty, stock.getMinStock()));
        stock.setLastChangeTime(LocalDateTime.now());
        materialOrderMapper.updateMaterialStock(stock);
        MaterialStockFlow flow = new MaterialStockFlow();
        flow.setFlowId(UlidCreator.getMonotonicUlid().toString());
        flow.setEntCode(entCode);
        flow.setWarehouseId(warehouseId);
        flow.setMaterialId(materialId);
        flow.setBizType("FREEZE");
        flow.setBizId(bizId);
        flow.setBizItemId(bizItemId);
        flow.setBizNo(bizNo);
        flow.setQtyChange(0D);
        flow.setBeforeQty(beforeQty);
        flow.setAfterQty(beforeQty);
        flow.setBeforeAvailableQty(beforeAvailable);
        flow.setAfterAvailableQty(afterAvailable);
        flow.setBeforeFrozenQty(beforeFrozen);
        flow.setAfterFrozenQty(afterFrozen);
        flow.setOperateBy(getCurrentUserName());
        flow.setOperateTime(LocalDateTime.now());
        flow.setRemark("出库冻结");
        materialOrderMapper.insertMaterialStockFlow(flow);
    }

    /**
     * 出库解冻：减少冻结库存并回补可用库存，写入解冻流水。
     * <p>
     * qty 小于等于 0 时不处理；库存不存在时直接返回。
     * </p>
     *
     * @param entCode     企业编码
     * @param warehouseId 仓库 ID
     * @param materialId  物资 ID
     * @param qty         解冻数量
     * @param bizId       业务单据 ID
     * @param bizItemId   业务明细 ID
     * @param bizNo       业务单号
     */
    public void unfreezeOut(String entCode, String warehouseId, String materialId, Double qty, String bizId, String bizItemId, String bizNo) {
        double v = qty == null ? 0D : qty;
        if (v <= 0D) {
            return;
        }
        MaterialStock stock = materialOrderMapper.selectMaterialStock(entCode, warehouseId, materialId);
        if (stock == null) {
            return;
        }
        double beforeQty = stock.getCurrentQty() == null ? 0D : stock.getCurrentQty();
        double beforeAvailable = stock.getAvailableQty() == null ? 0D : stock.getAvailableQty();
        double beforeFrozen = stock.getFrozenQty() == null ? 0D : stock.getFrozenQty();
        double afterFrozen = beforeFrozen - v;
        if (afterFrozen < 0D) {
            throw new RuntimeException("冻结库存不足");
        }
        double afterAvailable = beforeAvailable + v;
        stock.setAvailableQty(afterAvailable);
        stock.setFrozenQty(afterFrozen);
        stock.setStockStatus(calcStockStatus(beforeQty, stock.getMinStock()));
        stock.setLastChangeTime(LocalDateTime.now());
        materialOrderMapper.updateMaterialStock(stock);
        MaterialStockFlow flow = new MaterialStockFlow();
        flow.setFlowId(UlidCreator.getMonotonicUlid().toString());
        flow.setEntCode(entCode);
        flow.setWarehouseId(warehouseId);
        flow.setMaterialId(materialId);
        flow.setBizType("UNFREEZE");
        flow.setBizId(bizId);
        flow.setBizItemId(bizItemId);
        flow.setBizNo(bizNo);
        flow.setQtyChange(0D);
        flow.setBeforeQty(beforeQty);
        flow.setAfterQty(beforeQty);
        flow.setBeforeAvailableQty(beforeAvailable);
        flow.setAfterAvailableQty(afterAvailable);
        flow.setBeforeFrozenQty(beforeFrozen);
        flow.setAfterFrozenQty(afterFrozen);
        flow.setOperateBy(getCurrentUserName());
        flow.setOperateTime(LocalDateTime.now());
        flow.setRemark("出库解冻");
        materialOrderMapper.insertMaterialStockFlow(flow);
    }

    /**
     * 消耗冻结库存：将冻结数量转为实际出库（减少当前库存与冻结库存），写入出库流水。
     * <p>
     * qty 小于等于 0 时不处理；库存不存在或冻结/当前库存不足时抛出异常。
     * </p>
     *
     * @param entCode     企业编码
     * @param warehouseId 仓库 ID
     * @param materialId  物资 ID
     * @param qty         出库数量
     * @param bizId       业务单据 ID
     * @param bizItemId   业务明细 ID
     * @param bizNo       业务单号
     */
    public void consumeFrozenOut(String entCode, String warehouseId, String materialId, Double qty, String bizId, String bizItemId, String bizNo) {
        double v = qty == null ? 0D : qty;
        if (v <= 0D) {
            return;
        }
        MaterialStock stock = materialOrderMapper.selectMaterialStock(entCode, warehouseId, materialId);
        if (stock == null) {
            throw new RuntimeException("库存不存在");
        }
        double beforeQty = stock.getCurrentQty() == null ? 0D : stock.getCurrentQty();
        double beforeAvailable = stock.getAvailableQty() == null ? 0D : stock.getAvailableQty();
        double beforeFrozen = stock.getFrozenQty() == null ? 0D : stock.getFrozenQty();
        double afterFrozen = beforeFrozen - v;
        if (afterFrozen < 0D) {
            throw new RuntimeException("冻结库存不足");
        }
        double afterQty = beforeQty - v;
        if (afterQty < 0D) {
            throw new RuntimeException("物资库存不足");
        }
        stock.setCurrentQty(afterQty);
        stock.setFrozenQty(afterFrozen);
        stock.setStockStatus(calcStockStatus(afterQty, stock.getMinStock()));
        stock.setLastChangeTime(LocalDateTime.now());
        materialOrderMapper.updateMaterialStock(stock);
        MaterialStockFlow flow = new MaterialStockFlow();
        flow.setFlowId(UlidCreator.getMonotonicUlid().toString());
        flow.setEntCode(entCode);
        flow.setWarehouseId(warehouseId);
        flow.setMaterialId(materialId);
        flow.setBizType("OUT");
        flow.setBizId(bizId);
        flow.setBizItemId(bizItemId);
        flow.setBizNo(bizNo);
        flow.setQtyChange(-v);
        flow.setBeforeQty(beforeQty);
        flow.setAfterQty(afterQty);
        flow.setBeforeAvailableQty(beforeAvailable);
        flow.setAfterAvailableQty(beforeAvailable);
        flow.setBeforeFrozenQty(beforeFrozen);
        flow.setAfterFrozenQty(afterFrozen);
        flow.setOperateBy(getCurrentUserName());
        flow.setOperateTime(LocalDateTime.now());
        flow.setRemark("物资出库");
        materialOrderMapper.insertMaterialStockFlow(flow);
    }

    /**
     * 更新附件关联关系。
     *
     * @param sourceId   业务主键
     * @param sourceType 业务类型
     * @param annexIds   附件 ID 列表
     */
    public void updateAnnex(String sourceId, String sourceType, List<String> annexIds) {
        if (StringUtils.isEmpty(sourceId) || StringUtils.isEmpty(sourceType)) {
            return;
        }
        JSONObject req = new JSONObject();
        req.set("sourceId", sourceId);
        req.set("sourceType", sourceType);
        req.set("annexIds", annexIds);
        platformFacade.updateAnnex(req);
    }

    /**
     * 查询业务关联附件列表。
     *
     * @param sourceId   业务主键
     * @param sourceType 业务类型
     * @return 附件列表（不为 null）
     */
    public List<AnnexInfo> listAnnex(String sourceId, String sourceType) {
        if (StringUtils.isEmpty(sourceId) || StringUtils.isEmpty(sourceType)) {
            return new ArrayList<>();
        }
        AnnexReq req = new AnnexReq();
        req.setSourceType(sourceType);
        req.setSourceIds(Collections.singletonList(sourceId));
        List<AnnexInfo> list = platformFacade.annexList(req);
        return list == null ? new ArrayList<>() : list;
    }

    /**
     * 查询物资业务操作日志。
     *
     * @param bizId 业务单据 ID
     * @param bizNo 业务单号
     * @return 操作日志列表（不为 null）
     */
    public List<MaterialOperateLog> listOperateLog(String bizId, String bizNo) {
        List<MaterialOperateLog> list = materialOperateLogMapper.selectMaterialOperateLogList(bizId, bizNo);
        return list == null ? new ArrayList<>() : list;
    }

    /**
     * 导出简单 Excel（仅表头+文本行），直接写入 HTTP 响应输出流。
     *
     * @param fileName 导出文件名
     * @param headers  表头
     * @param rows     行数据（每行是字符串列表）
     * @param response HTTP 响应
     */
    public void exportSimpleExcel(String fileName, String[] headers, List<List<String>> rows, HttpServletResponse response) {
        try (XSSFWorkbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("sheet1");
            Row head = sheet.createRow(0);
            for (int i = 0; i < headers.length; i++) {
                head.createCell(i).setCellValue(headers[i]);
            }
            if (rows != null) {
                for (int i = 0; i < rows.size(); i++) {
                    Row row = sheet.createRow(i + 1);
                    List<String> data = rows.get(i);
                    for (int j = 0; j < data.size(); j++) {
                        row.createCell(j).setCellValue(data.get(j) == null ? "" : data.get(j));
                    }
                }
            }
            for (int i = 0; i < headers.length; i++) {
                sheet.setColumnWidth(i, 20 * 256);
            }
            response.setContentType(MimeTypeUtils.EXCEL_XLSX);
            response.setHeader("Content-Disposition",
                    "attachment;filename*=UTF-8''" + URLEncoder.encode(fileName, StandardCharsets.UTF_8));
            try (OutputStream outputStream = response.getOutputStream()) {
                workbook.write(outputStream);
            }
        } catch (Exception e) {
            log.error("物资模块导出失败", e);
        }
    }

    /**
     * 将 workbook 作为 xlsx 文件写入 HTTP 响应输出流。
     *
     * @param fileName 导出文件名
     * @param workbook 工作簿
     * @param response HTTP 响应
     */
    public void writeWorkbook(String fileName, XSSFWorkbook workbook, HttpServletResponse response) {
        try {
            response.setContentType(MimeTypeUtils.EXCEL_XLSX);
            response.setHeader("Content-Disposition",
                    "attachment;filename*=UTF-8''" + URLEncoder.encode(fileName, StandardCharsets.UTF_8));
            try (OutputStream outputStream = response.getOutputStream()) {
                workbook.write(outputStream);
            }
        } catch (Exception e) {
            log.error("物资模块模板下载失败", e);
        }
    }

    /**
     * 计算库存状态。
     *
     * @param qty      当前库存
     * @param minStock 最小库存
     * @return 1=正常；2=低库存；3=无库存
     */
    private Integer calcStockStatus(Double qty, Double minStock) {
        double v = qty == null ? 0D : qty;
        double m = minStock == null ? 0D : minStock;
        if (v <= 0) {
            return 3;
        }
        if (v <= m) {
            return 2;
        }
        return 1;
    }
}
