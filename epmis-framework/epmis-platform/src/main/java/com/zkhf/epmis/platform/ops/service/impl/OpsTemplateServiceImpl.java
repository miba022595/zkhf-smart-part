package com.zkhf.epmis.platform.ops.service.impl;

import com.alibaba.fastjson2.JSONArray;
import com.github.f4b6a3.ulid.UlidCreator;
import com.zkhf.epmis.core.annotation.Log;
import com.zkhf.epmis.core.domain.AjaxResult;
import com.zkhf.epmis.core.enums.BusinessType;
import com.zkhf.epmis.core.utils.PageUtils;
import com.zkhf.epmis.core.utils.StringUtils;
import com.zkhf.epmis.platform.global.GVarContainer;
import com.zkhf.epmis.platform.mapper.ops.OpsTemplateMapper;
import com.zkhf.epmis.platform.ops.domain.OpsTemplate;
import com.zkhf.epmis.platform.ops.domain.OpsTemplateItem;
import com.zkhf.epmis.platform.ops.domain.OpsTemplateReq;
import com.zkhf.epmis.platform.ops.service.OpsTemplateService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 运维模板配置-运维类型Service业务层处理
 */
@Service
public class OpsTemplateServiceImpl implements OpsTemplateService {

    private OpsTemplateMapper opsTemplateMapper;
    @Autowired
    public void setOpsTemplateMapper(OpsTemplateMapper opsTemplateMapper) {
        this.opsTemplateMapper = opsTemplateMapper;
    }

    @Override
    public AjaxResult allTemplateType(String outPutId) {
        // 优先级：排口级 > 公共级
        Map<String, String> all;
        if (StringUtils.isEmpty(outPutId)) {
            all = allTemplateTypeReMap();
        } else {
            List<OpsTemplate> outTem = opsTemplateMapper.allTemplateTypeByOutId(outPutId);
            all = typeDeal(outTem);
        }
        List<Map<String, String>> allLit = new ArrayList<>();
        all.forEach( (k, v) -> {
            Map<String, String> map = new HashMap<>();
            map.put("templateCode", k);
            map.put("templateName", v);
            allLit.add(map);
        });
        return AjaxResult.success(allLit);
    }

    @Override
    public Map<String, String> allTemplateTypeReMap() {
        List<OpsTemplate> allType = opsTemplateMapper.allTemplateType();
        return typeDeal(allType);
    }

    private Map<String, String> typeDeal(List<OpsTemplate> typeList) {
        // 优先级：排口级 > 公共级
        Map<String, String> all = new HashMap<>();
        if (null == typeList || typeList.isEmpty()) {
            return all;
        }
        Map<String, String> pubMap = new HashMap<>();
        Map<String, String> entPriMap = new HashMap<>();
        for (OpsTemplate type : typeList) {
            if (pubSign.equals(type.getEntCode())) {
                pubMap.put(type.getTemplateCode(), type.getTemplateName());
            } else {
                entPriMap.put(type.getTemplateCode(), type.getTemplateName());
            }
        }
        all.putAll(pubMap);
        all.putAll(entPriMap);
        return all;
    }

    @Override
    public OpsTemplate selectOpsTemplateDetail(String entCode, String outPutId, String templateCode) {
        if (StringUtils.isEmpty(entCode) || StringUtils.isEmpty(outPutId) || StringUtils.isEmpty(templateCode)) {
            throw new RuntimeException("未知的查询参数") ;
        }
        // 取公共模板的时候，outPutId应该就是传的排口类型了
        OpsTemplate detail = opsTemplateMapper.selectOpsTemplateDetail(entCode, outPutId, templateCode);
        if (!pubSign.equals(entCode) && null == detail) {
            // 没取公共模板，且没取到私有模板时，获取公共模板
            detail = opsTemplateMapper.selectOpsPubTemplateDetail(entCode, outPutId, templateCode);
        }
        if (null == detail) {
            throw new RuntimeException("模板不匹配，请确认") ;
        }
        if (StringUtils.isNotEmpty(detail.getItemArray())) {
            detail.setItemList(JSONArray.parseArray(detail.getItemArray(), OpsTemplateItem.class));
        }
        return detail;
    }

    @Override
    public AjaxResult selectOpsTemplateList(OpsTemplateReq req) {
        // 请求参数转换
        if (null == req) {
            req = new OpsTemplateReq();
        }
        // 添加权限
        List<String> codes = null;
        boolean isAdmin = GVarContainer.isAdmin();
        if (isAdmin) { // admin 账号配置只能看公共的
            req.setEntCode(pubSign);
        } else {
            codes = GVarContainer.getEntCodes();
            req.setEntCodes(codes);
        }
        // 运维配置列表查询
        boolean page = PageUtils.startPageCheckExists();
        List<OpsTemplate> list = opsTemplateMapper.selectOpsTemplateList(req);
        // 只有权限列表下的才可修改
        for (OpsTemplate t : list) {
            if (StringUtils.isNotEmpty(t.getItemArray())) {
                t.setItemList(JSONArray.parseArray(t.getItemArray(), OpsTemplateItem.class));
            }
            // admin账户可编辑公共的模板
            if (pubSign.equals(t.getEntCode())) {
                if (isAdmin) {
                    t.setModify(true);
                }
                continue;
            }
            if (null == codes || !codes.contains(t.getEntCode())) {
                continue;
            }
            t.setModify(true);
        }
        return PageUtils.getAjaxResult(list, page);
    }

    @Override
    @Log(title = "运维模板", businessType = BusinessType.INSERT)
    public AjaxResult insertOpsTemplate(OpsTemplate info) {
        // 公共模板不能添加
        if (null == info.getEntCode() || pubSign.equals(info.getEntCode())) {
            return AjaxResult.error("公共模板不能添加");
        }
        info.setCreateBy(GVarContainer.getUserName());
        info.setUpdateBy(GVarContainer.getUserName());
        int count = opsTemplateMapper.checkOpsTemplate(info);
        if (count > 0) {
            return AjaxResult.error("运维模板已存在，拒绝添加");
        }
        // 设置配置项的id
        setConfId(info, true);
        if (null != info.getItemList()) {
            info.setItemArray(JSONArray.toJSONString(info.getItemList()));
        } else {
            info.setItemArray("[]");
        }
        count = opsTemplateMapper.insertOpsTemplate(info);
        return AjaxResult.success(count);
    }

    @Override
    @Log(title = "运维模板", businessType = BusinessType.UPDATE)
    public AjaxResult updateOpsTemplate(OpsTemplate info) {
        // 可以直接修改，不用管有没有使用
        if (GVarContainer.isAdmin()) {
            info.setEntCode(pubSign);
        } else {
            if (pubSign.equals(info.getEntCode())) {
                return AjaxResult.error("公共模板不允许修改");
            }
        }
        // 校验编码是否重复
        int count = opsTemplateMapper.checkOpsTemplateCode(info);
        if (count > 0) {
            return AjaxResult.error("运维模板名称重复，拒绝修改");
        }
        info.setUpdateBy(GVarContainer.getUserName());
        // 设置配置项的id
        setConfId(info, false);
        if (null != info.getItemList()) {
            info.setItemArray(JSONArray.toJSONString(info.getItemList()));
        } else {
            info.setItemArray("[]");
        }
        count = opsTemplateMapper.updateOpsTemplate(info);
        return AjaxResult.success(count);
    }

    private void setConfId(OpsTemplate info, boolean init) {
        if (null == info.getItemList() || info.getItemList().isEmpty()) {
            return;
        }
        for (OpsTemplateItem item : info.getItemList()) {
            if (init || StringUtils.isEmpty(item.getTemplateItemId())) {
                item.setTemplateItemId(UlidCreator.getMonotonicUlid().toString());
            }
            if (null == item.getDetailList() || item.getDetailList().isEmpty()) {
                continue;
            }
            item.getDetailList().forEach( d -> {
                if (init || StringUtils.isEmpty(d.getTemplateDetailId())) {
                    d.setTemplateDetailId(UlidCreator.getMonotonicUlid().toString());
                }
            });
        }
    }

    @Override
    @Log(title = "运维模板", businessType = BusinessType.DELETE)
    public AjaxResult deleteOpsTemplate(String entCode, String outPutId, String templateCode) {
        if (StringUtils.isEmpty(entCode) || StringUtils.isEmpty(outPutId) || StringUtils.isEmpty(templateCode)) {
            return AjaxResult.success(0);
        }
        // admin账户只能管理公共模板
        if (pubSign.equals(entCode)) {
            return AjaxResult.error("公共模板不能删除");
        }
        int count = opsTemplateMapper.deleteOpsTemplate(entCode, outPutId, templateCode);
        return AjaxResult.success(count);
    }
}
