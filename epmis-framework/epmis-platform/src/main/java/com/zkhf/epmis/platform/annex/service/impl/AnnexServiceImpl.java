package com.zkhf.epmis.platform.annex.service.impl;

import com.alibaba.fastjson2.JSONObject;
import com.github.f4b6a3.ulid.UlidCreator;
import com.zkhf.epmis.core.config.EPMISConfig;
import com.zkhf.epmis.core.constant.Constants;
import com.zkhf.epmis.core.domain.AjaxResult;
import com.zkhf.epmis.core.domain.AnnexInfo;
import com.zkhf.epmis.core.domain.AnnexReq;
import com.zkhf.epmis.core.enums.AnnexTypeEnum;
import com.zkhf.epmis.core.utils.StringUtils;
import com.zkhf.epmis.platform.annex.service.AnnexService;
import com.zkhf.epmis.platform.base.domain.model.LoginUser;
import com.zkhf.epmis.platform.global.GVarContainer;
import com.zkhf.epmis.platform.mapper.annex.AnnexMapper;
import com.zkhf.epmis.platform.utils.file.FileUploadUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
public class AnnexServiceImpl implements AnnexService {

    private AnnexMapper annexMapper;

    @Autowired
    public void setAnnexMapper(AnnexMapper annexMapper) {
        this.annexMapper = annexMapper;
    }

    @Override
    public List<AnnexInfo> selectAnnexList(AnnexReq req) {
        return annexMapper.selectAnnexList(req);
    }

    @Override
    public List<AnnexInfo> selectAnnexList(String sourceId, String sourceType) {
        return annexMapper.selectAnnexListBySource(sourceId, sourceType);
    }

    @Override
    public AjaxResult insertAnnex(MultipartFile file, String sourceType) {
        AnnexInfo annexInfo = new AnnexInfo();
        String errInfo = dealFile(annexInfo, file, sourceType);
        if (StringUtils.isNotEmpty(errInfo)) {
            return AjaxResult.error(errInfo);
        }
        return AjaxResult.success(annexInfo);
    }

    @Override
    public AnnexInfo insertAnnexReturnId(MultipartFile file, String sourceType) {
        AnnexInfo annexInfo = new AnnexInfo();
        String errInfo = dealFile(annexInfo, file, sourceType);
        if (StringUtils.isNotEmpty(errInfo)) {
            return null;
        }
        return annexInfo;
    }

    private String dealFile(AnnexInfo annexInfo, MultipartFile file, String sourceType) {
        if (file.isEmpty()) {
            return "上传附件为空，请检查确认";
        }
        LoginUser loginUser = GVarContainer.getLoginUser();
        // 上传附件
        String imageUrl;
        try {
            imageUrl = FileUploadUtils.upload(EPMISConfig.getAnnexPath(sourceType), file, AnnexTypeEnum.getTypesByName(sourceType));
        } catch (Exception e) {
            log.error("上传文件失败", e);
            return "上传文件失败，请检查";
        }
        annexInfo.setAnnexId(UlidCreator.getMonotonicUlid().toString());
        annexInfo.setSourceType(sourceType);
        annexInfo.setFileName(file.getOriginalFilename());
        annexInfo.setFileType(FileUploadUtils.getExtension(file));
        annexInfo.setFilePath(imageUrl);
        annexInfo.setFileSize(file.getSize());
        annexInfo.setCreateUser(loginUser.getUser().getUserName());
        annexInfo.setCreateTime(LocalDateTime.now());
        annexMapper.insertAnnex(annexInfo);
        return null;
    }

    @Override
    public AjaxResult updateAnnex(JSONObject annexInfo) {
        if (null == annexInfo) {
            return AjaxResult.error("更新数据为空");
        }
        String sourceId = annexInfo.getString("sourceId");
        if (StringUtils.isEmpty(sourceId)) {
            return AjaxResult.error("未知的目标id");
        }
        String sourceType = annexInfo.getString("sourceType");
        List<String> annexIds = annexInfo.getList("annexIds", String.class);
        // 更新附件
        updateAnnex(sourceId, sourceType, annexIds);
        return AjaxResult.success();
    }

    @Override
    public AjaxResult deleteAnnex(List<String> annexIds) {
        if (StringUtils.isEmpty(annexIds)) {
            return AjaxResult.error("未知的参数");
        }
        // 先获取旧的配置
        List<AnnexInfo> deleteList = annexMapper.selectAnnexListByIds(annexIds);
        if (null == deleteList || deleteList.isEmpty()) {
            return AjaxResult.error("错误的附件id");
        }
        List<String> deletePath = new ArrayList<>();
        for (AnnexInfo old : deleteList) {
            if (null != old.getFilePath()) {
                deletePath.add(old.getFilePath().replace(Constants.RESOURCE_PREFIX, ""));
            }
        }
        // 删除附件
        int size = annexMapper.deleteAnnexByIds(annexIds);
        // 删除本地保存的数据
        if (size > 0 && !deletePath.isEmpty()) {
            deletePath.forEach(FileUploadUtils::deleteFile);
        }
        return AjaxResult.success();
    }

    @Override
    public void updateAnnex(String sourceId, String sourceType, List<String> annexIds) {
        if (StringUtils.isEmpty(sourceId)) {
            return;
        }
        // 先获取旧的配置
        List<AnnexInfo> oldList = annexMapper.selectAnnexListBySource(sourceId, sourceType);
        List<String> deletePath = new ArrayList<>();
        List<String> deleteAnnexId = new ArrayList<>();
        if (null != oldList && !oldList.isEmpty()) {
            for (AnnexInfo old : oldList) {
                // 更新时的id包含在旧的内无需更新；旧的不包含在新的列表内删除
                if (null != annexIds && annexIds.contains(old.getAnnexId())) {
                    annexIds.remove(old.getAnnexId());
                } else {
                    if (null != old.getFilePath()) {
                        deletePath.add(old.getFilePath().replace(Constants.RESOURCE_PREFIX, ""));
                    }
                    deleteAnnexId.add(old.getAnnexId());
                }
            }
        }
        // 删除不包含在新的内的配置
        if (!deleteAnnexId.isEmpty()) {
            annexMapper.deleteAnnexByIds(deleteAnnexId);
        }
        // 更新数据
        if (null != annexIds && !annexIds.isEmpty()) {
            annexMapper.updateAnnex(sourceId, sourceType, annexIds);
        }
        // 删除本地保存的数据
        if (!deletePath.isEmpty()) {
            deletePath.forEach(FileUploadUtils::deleteFile);
        }
    }
}
