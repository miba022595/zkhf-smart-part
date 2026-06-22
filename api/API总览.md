# API 总览

## 1. 说明

当前项目未集成 Swagger / Knife4j，接口文档以源码和 Markdown 文档为准。本文档用于从服务维度快速了解接口入口、鉴权方式和重点模块。

## 2. 通用约定

### 2.1 鉴权

除匿名接口外，调用时需在请求头中携带：

```http
Authorization: Bearer <token>
```

Token 获取接口：

- `POST /login`

### 2.2 响应格式

多数接口返回 `AjaxResult`，典型结构：

```json
{
  "code": 200,
  "msg": "操作成功",
  "data": {}
}
```

### 2.3 接口风格

- 列表接口常见后缀：`/list`
- 新增接口常见后缀：`/add`
- 编辑接口常见后缀：`/edit`
- 删除接口常见后缀：`/remove`
- 导入模板接口常见后缀：`/importTemplate`、`/exportTemplate`
- 文件导入接口常见后缀：`/import`、`/importData`
- 导出接口常见后缀：`/export`

## 3. 认证服务 API

服务信息：

- 服务名：`epmis-auth-service`
- 默认端口：`9531`

### 3.1 登录与用户上下文

- `GET /version`
- `POST /login`
- `GET /getLoginUser`
- `GET /getInfo`
- `GET /getRouters`
- `GET /v2/getRouters`
- `GET /captchaImage`
- `POST /register`

### 3.2 系统管理

- 用户：`/system/user/**`
- 角色：`/system/role/**`
- 岗位：`/system/post/**`
- 菜单：`/system/menu/**`
- 参数配置：`/system/config/**`
- 通知公告：`/system/notice/**`
- 个人中心：`/system/user/profile/**`

### 3.3 监控与公共接口

- 操作日志：`/monitor/operlog/**`
- 登录日志：`/monitor/logininfor/**`
- 在线用户：`/monitor/online/**`
- 服务监控：`/monitor/server`
- 缓存监控：`/monitor/cache/**`
- 通用上传下载：`/common/**`

## 4. 平台服务 API

服务信息：

- 服务名：`epmis-platform-service`
- 默认端口：`9533`

### 4.1 基础数据

- 字典管理：`/platform/dict/**`
- 行政区划：`/platform/base/**`
- 行业分类：`/platform/base/**`
- 污染物编码：`/platform/base/**`
- 有效期配置：`/platform/base/**`

### 4.2 企业与台账

- 企业信息：`/platform/ent/**`
- 排口信息：`/platform/ent/**`
- 污染治理设施：`/platform/ent/**`
- 生产设施/生产线/生产车间：`/platform/ent/**`
- 设备信息：`/platform/ent/**`
- 第三方单位：`/platform/ent/**`

### 4.3 环保业务

- 清洁生产：`/platform/envProtect/**`
- 排污许可：`/platform/envProtect/**`
- 环评与环保管理：`/platform/envProtect/**`
- 环保人员：`/platform/envProtect/**`
- 环保政策法规：`/platform/envProtect/policy/**`
- 政府资金支持：`/platform/envProtect/policy/**`
- 环保投入与环保费用：`/platform/envFee/**`
- 手工监测计划与任务：`/platform/envManual/**`

### 4.4 应急管理

- 通讯录：`/platform/emergency/org/**`
- 专家：`/platform/emergency/expert/**`
- 车辆：`/platform/emergency/vehicle/**`
- 物资：`/platform/emergency/material/**`
- 预案：`/platform/emergency/plan/**`
- 演练：`/platform/emergency/drill/**`
- 通知：`/platform/emergency/notice/**`

详细文档：

- `api/环境应急管理模块-API文档.md`

### 4.5 其他能力

- 审批流：`/platform/approval/**`
- 附件：`/platform/annex/**`
- PLC：`/platform/plc/**`
- 运维任务：`/platform/ops/**`
- 首页统计：`/platform/homePage/**`
- AI 能力：`/platform/ai/**`
- 消息订阅：`/platform/sse/**`
- Feign 内部接口：`/platform/feign/**`

## 5. 数据处理服务 API

服务信息：

- 服务名：`epmis-process-service`
- 默认端口：`9532`

### 5.1 首页与统计

- 首页统计：`/process/homePage/**`
- 企业排放：`/process/emission/**`
- 有效传输率：`/process/effTrans/**`
- 有效期预警：`/process/validPeriod/**`

### 5.2 在线监测与报警

- 在线监测：`/process/onlineMonitoring/output/**`
- 持续报警：`/process/durAlarm/**`
- AI 数据：`/process/aiData/**`

### 5.3 固废管理

- 固废种类：`/process/wasteCategory/**`
- 固废间：`/process/wasteRoom/**`
- 固废统计：`/process/wasteStat/**`
- 固废库存过程：`/process/wasteStock/**`
- 固废总量控制：`/process/totalPlan/**`
- 固废字典：`/process/wasteDict/**`

### 5.4 物资管理

- 物资基础信息：`/process/materialInfo/**`
- 仓库信息：`/process/materialWarehouse/**`
- 物资库存：`/process/materialStock/**`
- 物资申请单：`/process/materialApplyOrder/**`
- 物资入库单：`/process/materialInOrder/**`
- 物资出库单：`/process/materialOutOrder/**`
- 物资归还单：`/process/materialReturnOrder/**`

详细文档：

- `api/物资管理模块-API文档.md`

### 5.5 其他接口

- 执法记录：`/process/enforceRecord/**`
- 手工监测报告：`/process/envManualCheck/**`
- PLC 数据：`/process/plc/**`
- SSE 消息订阅：`/process/message/subscribe/**`
- 异步导出：`/export/**`
- 测试发送：`/process/test/**`

## 6. 协议服务 API

服务信息：

- 服务名：`epmis-protocol-service`
- 默认端口：`9534`

### 6.1 协议与连接管理

- 指令发送：`/protocol/cmd/send`
- 在线设备列表：`/protocol/cmd/onlineList`
- 当前连接信息：`/protocol/connections/connections`
- 空闲连接检查：`/protocol/connections/idle/{minutes}`
- 关闭空闲连接：`/protocol/connections/close-idle`

## 7. 联调建议

### 7.1 最小联调流程

1. 调用认证服务 `/login` 获取 Token
2. 使用 Token 调用平台服务或流程服务接口
3. 若涉及协议联调，再调用协议服务接口

### 7.2 推荐优先验证的接口

- `POST /login`
- `GET /getInfo`
- `POST /platform/emergency/plan/list`
- `POST /platform/dict/type/list`
- `POST /process/onlineMonitoring/output/list`
- `GET /protocol/cmd/onlineList`

## 8. 文档维护建议

- 新增控制器时同步在本文档增加分组入口
- 对于复杂业务模块，单独维护专项 Markdown 文档
- 若后续接入 Swagger/Knife4j，可将本文档保留为业务导航页
