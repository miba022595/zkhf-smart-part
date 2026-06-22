package com.zkhf.epmis.platform.mapper.envProtect;

import com.zkhf.epmis.platform.envProtect.domain.OtherCertificate;
import com.zkhf.epmis.platform.envProtect.domain.OtherCertificateReq;

import java.util.List;

/**
 * 其他证书Mapper接口
 */
public interface OtherCertificateMapper {

    /**
     * 查询其他证书列表
     */
    List<OtherCertificate> selectOtherCertificateList(OtherCertificateReq req);

    /**
     * 新增其他证书
     */
    int insertOtherCertificate(OtherCertificate req);

    /**
     * 修改其他证书
     */
    int updateOtherCertificate(OtherCertificate info);

    /**
     * 删除其他证书
     */
    int deleteOtherCertificateById(String otherId);
}
