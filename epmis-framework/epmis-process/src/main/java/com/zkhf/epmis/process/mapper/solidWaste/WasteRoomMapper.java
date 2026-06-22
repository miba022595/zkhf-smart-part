package com.zkhf.epmis.process.mapper.solidWaste;

import java.util.List;
import java.util.Map;

import com.zkhf.epmis.process.solidWaste.domain.WasteRoom;
import com.zkhf.epmis.process.solidWaste.domain.WasteRoomReq;
import org.apache.ibatis.annotations.Param;

/**
 * 固废间管理Mapper接口
 */
public interface WasteRoomMapper {

    /**
     * 查询固废间管理列表
     */
    List<WasteRoom> selectWasteRoomList(WasteRoomReq req);

    /**
     * 新增固废间管理
     */
    int insertWasteRoom(WasteRoom info);

    /**
     * 修改固废间管理
     */
    int updateWasteRoom(WasteRoom info);

    /**
     * 删除固废间管理
     */
    int deleteWasteRoomByRoomId(String roomId);

    /**
     * 获取固废间被使用的次数
     */
    int selectUsedSizeRoom(String roomId);

    /**
     * 批量查询固废间的当前库存量
     * 从产生表和入库表的剩余量汇总
     */
    List<Map<String, Object>> selectRoomCapacityBatch(@Param("roomIds") List<String> roomIds);

}
