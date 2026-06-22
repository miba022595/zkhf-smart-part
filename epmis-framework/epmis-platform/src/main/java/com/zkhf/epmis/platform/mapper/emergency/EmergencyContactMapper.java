package com.zkhf.epmis.platform.mapper.emergency;

import com.zkhf.epmis.platform.emergency.domain.EmergencyContact;
import com.zkhf.epmis.platform.emergency.domain.EmergencyContactReq;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 应急通讯录Mapper。
 * 负责应急联系人数据的查询、单条维护和批量导入写入。
 */
@Mapper
public interface EmergencyContactMapper {

    /**
     * 根据联系人ID查询详情。
     *
     * @param contactId 联系人ID
     * @return 联系人详情
     */
    EmergencyContact selectById(@Param("contactId") String contactId);

    /**
     * 按条件查询应急通讯录列表。
     *
     * @param req 查询条件
     * @return 联系人列表
     */
    List<EmergencyContact> selectList(EmergencyContactReq req);

    /**
     * 查询全部应急联系人。
     *
     * @return 联系人列表
     */
    List<EmergencyContact> selectAll();

    /**
     * 新增应急联系人。
     *
     * @param info 联系人信息
     * @return 影响行数
     */
    int insert(EmergencyContact info);

    /**
     * 批量新增应急联系人。
     *
     * @param list 联系人列表
     * @return 影响行数
     */
    int batchInsert(@Param("list") List<EmergencyContact> list);

    /**
     * 更新应急联系人。
     *
     * @param info 联系人信息
     * @return 影响行数
     */
    int update(EmergencyContact info);

    /**
     * 校验同一企业下联系人姓名是否重复。
     *
     * @param entCode 所属企业编码
     * @param contactName 联系人姓名
     * @param excludeContactId 需要排除的联系人ID
     * @return 重复数量
     */
    int countByEntCodeAndContactName(@Param("entCode") String entCode,
                                     @Param("contactName") String contactName,
                                     @Param("excludeContactId") String excludeContactId);

    /**
     * 批量查询指定企业下已有联系人名称。
     *
     * @param entCodes 企业编码列表
     * @return 联系人列表
     */
    List<EmergencyContact> selectByEntCodes(@Param("entCodes") List<String> entCodes);

    /**
     * 根据联系人ID删除记录。
     *
     * @param contactId 联系人ID
     * @return 影响行数
     */
    int deleteById(@Param("contactId") String contactId);
}
