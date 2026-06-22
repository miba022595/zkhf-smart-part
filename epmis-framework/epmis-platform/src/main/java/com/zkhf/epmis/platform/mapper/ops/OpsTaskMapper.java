package com.zkhf.epmis.platform.mapper.ops;

import com.zkhf.epmis.platform.ops.domain.OpsTask;
import com.zkhf.epmis.platform.ops.domain.OpsTaskConf;
import com.zkhf.epmis.platform.ops.domain.OpsTaskReq;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 运维任务Mapper接口
 */
public interface OpsTaskMapper {

    /**
     * 查询运维任务配置
     */
    OpsTaskConf selectOpsTaskConfDetail(@Param("entCode") String entCode, @Param("outPutId") String outPutId,
                                        @Param("templateCode") String templateCode);

    /**
     * 查询运维任务配置列表
     */
    List<OpsTaskConf> selectOpsTaskConfList(OpsTaskReq req);

    /**
     * 检查任务是否存在
     */
    int checkExistsTaskConf(OpsTaskConf info);

    /**
     * 新增运维任务配置
     */
    int insertOpsTaskConf(OpsTaskConf info);

    /**
     * 修改运维任务配置
     */
    int updateOpsTaskConf(OpsTaskConf info);

    /**
     * 删除运维任务配置
     */
    int deleteOpsTaskConf(@Param("entCode") String entCode, @Param("outPutId") String outPutId,
                              @Param("templateCode") String templateCode);

    /**
     * 查询运维任务
     */
    OpsTask selectOpsTaskByTaskId(String taskId);

    /**
     * 查询运维任务列表
     */
    List<OpsTask> selectPersonOpsTaskList(OpsTaskReq req);

    /**
     * 查询运维任务列表
     */
    List<OpsTask> selectOpsTaskList(OpsTaskReq req);

    /**
     * 新增运维任务
     */
    int insertOpsTask(OpsTask info);

    /**
     * 修改运维任务
     */
    int updateOpsTask(OpsTask info);

    /**
     * 删除运维任务
     */
    int deleteOpsTaskById(String taskId);

}
