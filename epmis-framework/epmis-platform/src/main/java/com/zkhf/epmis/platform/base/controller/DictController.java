package com.zkhf.epmis.platform.base.controller;

import com.zkhf.epmis.core.domain.AjaxResult;
import com.zkhf.epmis.platform.base.domain.DictData;
import com.zkhf.epmis.platform.base.domain.DictType;
import com.zkhf.epmis.platform.base.domain.DictTypeReq;
import com.zkhf.epmis.platform.base.service.DictService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletResponse;
import java.util.List;

/**
 * 数据字典信息类
 */
@RestController
@RequestMapping("/platform/dict")
public class DictController {

    private DictService dictService;
    @Autowired
    public void setDictService(DictService dictService) {
        this.dictService = dictService;
    }

    /**
     * 依据字典类型获取字典值
     */
    @GetMapping("/getDataListByTypes")
    public AjaxResult getDataListByTypes(@RequestParam List<String> type) {
        return AjaxResult.success(dictService.getDataListByTypes(type));
    }

    /**
     * 获取字典选择框列表
     */
    @GetMapping("/type/optionSelect")
    public AjaxResult optionSelect() {
        return dictService.selectTypeAll();
    }

    /**
     * 查询字典列表
     */
    @PostMapping("/type/list")
    public AjaxResult typeList(@RequestBody(required = false) DictTypeReq req) {
        return dictService.selectTypeList(req);
    }

    /**
     * 查询字典数据列表
     */
    @PostMapping("/data/list")
    public AjaxResult dataList(@RequestBody(required = false) DictData req) {
        return dictService.selectDataList(req);
    }

    /**
     * 导出字典列表
     */
    @PostMapping("/type/exportTemplate")
    public void typeExportTemplate(@RequestBody(required = false) DictTypeReq req, HttpServletResponse response) {
        dictService.exportTypeList(req, response);
    }

    /**
     * 导出字典数据列表
     */
    @PostMapping("/data/exportTemplate")
    public void dataExportTemplate(@RequestBody(required = false) DictData req, HttpServletResponse response) {
        dictService.exportDataList(req, response);
    }

    /**
     * 查询字典类型详细
     */
    @GetMapping(value = "/type/{id}")
    public AjaxResult getTypeInfo(@PathVariable Long id) {
        return dictService.selectTypeById(id);
    }

    /**
     * 查询字典数据详细
     */
    @GetMapping(value = "/data/{code}")
    public AjaxResult getDataInfo(@PathVariable Long code) {
        return dictService.selectDataById(code);
    }

    /**
     * 根据字典类型查询字典数据信息
     */
    @GetMapping(value = "/data/type/{type}")
    public AjaxResult dictType(@PathVariable String type) {
        return AjaxResult.success(dictService.selectDataByType(type));
    }

    /**
     * 新增字典类型
     */
    @PostMapping("/type")
    public AjaxResult addType(@RequestBody DictType type) {
        return dictService.insertType(type);
    }

    /**
     * 新增字典数据
     */
    @PostMapping("/data")
    public AjaxResult addData(@RequestBody DictData data) {
        return dictService.insertData(data);
    }

    /**
     * 修改字典类型
     */
    @PutMapping("/type")
    public AjaxResult editType(@RequestBody DictType type) {
        return dictService.updateType(type);
    }

    /**
     * 修改字典数据
     */
    @PutMapping("/data")
    public AjaxResult editData(@RequestBody DictData data) {
        return dictService.updateData(data);
    }

    /**
     * 删除字典类型
     */
    @DeleteMapping("/type/{ids}")
    public AjaxResult removeType(@PathVariable Long[] ids) {
        return dictService.deleteTypeByIds(ids);
    }

    /**
     * 删除字典数据
     */
    @DeleteMapping("/data/{codes}")
    public AjaxResult removeData(@PathVariable Long[] codes) {
        return dictService.deleteDataByIds(codes);
    }

    /**
     * 刷新字典缓存
     */
    @GetMapping("/type/refreshCache")
    public AjaxResult refreshCache() {
        return dictService.resetCache();
    }

    /**
     * 新增字典数据-自动生成键值和排序
     */
    @PostMapping("/dataIntAuto")
    public AjaxResult addDataIntAuto(@RequestBody DictData data) {
        return dictService.insertDataIntAuto(data);
    }

    /**
     * 根据字典类型查询字典自定义数据信息
     */
    @GetMapping(value = "/customData/type/{type}")
    public AjaxResult dictTypeCustom(@PathVariable String type) {
        return AjaxResult.success(dictService.selectCustomDataByType(type));
    }

    /**
     * 新增字典自定义数据
     */
    @PostMapping("/customData")
    public AjaxResult addCustomData(@RequestBody DictData data) {
        return dictService.insertCustomData(data);
    }

    /**
     * 删除字典自定义数据
     */
    @DeleteMapping("/customData/{code}")
    public AjaxResult removeCustomData(@PathVariable Long code) {
        return dictService.deleteCustomDataByCode(code);
    }
}
