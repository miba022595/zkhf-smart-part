package com.zkhf.epmis.process.mapper.enforce;

import com.zkhf.epmis.process.enforce.domain.EnforceRecord;
import com.zkhf.epmis.process.enforce.domain.EnforceRecordReq;

import java.util.List;

/**
 * 执法检查记录Mapper接口
 */
public interface EnforceRecordMapper {

    /**
     * 查询执法检查记录
     */
    EnforceRecord selectEnforceRecordById(Long id);

    /**
     * 查询执法检查记录列表
     */
    List<EnforceRecord> selectEnforceRecordList(EnforceRecordReq req);

    /**
     * 新增执法检查记录
     */
    int insertEnforceRecord(EnforceRecord info);

    /**
     * 修改执法检查记录
     */
    int updateEnforceRecord(EnforceRecord info);

    /**
     * 删除执法检查记录
     */
    int deleteEnforceRecordById(Long id);
}
