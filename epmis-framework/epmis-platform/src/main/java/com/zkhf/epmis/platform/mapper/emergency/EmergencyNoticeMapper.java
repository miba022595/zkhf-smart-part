package com.zkhf.epmis.platform.mapper.emergency;

import com.zkhf.epmis.platform.emergency.domain.EmergencyNotice;
import com.zkhf.epmis.platform.emergency.domain.EmergencyNoticeReq;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 应急通知Mapper。
 * 负责通知发送记录的新增、查询和删除。
 */
public interface EmergencyNoticeMapper {

    /**
     * 新增应急通知发送记录。
     *
     * @param emergencyNotice 通知信息
     * @return 影响行数
     */
    int insert(EmergencyNotice emergencyNotice);

    /**
     * 根据通知ID删除记录。
     *
     * @param noticeId 通知ID
     * @return 影响行数
     */
    int deleteById(@Param("noticeId") String noticeId);

    /**
     * 按条件查询应急通知列表。
     *
     * @param req 查询条件
     * @return 通知列表
     */
    List<EmergencyNotice> selectList(EmergencyNoticeReq req);

    /**
     * 查询通知人员的昵称
     */
    List<Map<String, String>> selectNickName(@Param("userNames") Set<String> userNames);
}
