package com.zkhf.epmis.platform.mapper.annex;

import com.zkhf.epmis.core.domain.AnnexInfo;
import com.zkhf.epmis.core.domain.AnnexReq;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface AnnexMapper {

    /**
     * 查询附件
     */
    List<AnnexInfo> selectAnnexList(AnnexReq req);

    /**
     * 查询附件
     */
    List<AnnexInfo> selectAnnexListBySource(@Param("sourceId") String sourceId, @Param("sourceType") String sourceType);

    /**
     * 查询附件
     */
    List<AnnexInfo> selectAnnexListByIds(List<String> ids);

    /**
     * 添加附件
     */
    void insertAnnex(AnnexInfo annexInfo);

    /**
     * 修改附件信息
     */
    void updateAnnex(@Param("sourceId") String sourceId, @Param("sourceType") String sourceType, @Param("annexIds") List<String> annexIds);

    /**
     * 批量删除附件
     */
    int deleteAnnexByIds(@Param("ids") List<String> ids);
}
