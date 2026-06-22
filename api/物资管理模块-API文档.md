# 物资管理模块 - API 文档

服务信息：

- 服务名：`epmis-process-service`
- 默认端口：`9532`

## 1. 通用约定

### 1.1 鉴权

除匿名接口外，调用时需在请求头中携带：

```http
Authorization: Bearer <token>
```

### 1.2 响应格式

多数接口返回 `AjaxResult`：

```json
{
  "code": 200,
  "msg": "操作成功",
  "data": {}
}
```

列表接口在分页时通常额外包含：

```json
{
  "total": 123
}
```

### 1.3 分页参数

列表接口支持 URL 查询参数：

- `pageNum`：页码（从 1 开始）
- `pageSize`：每页条数

未传分页参数时，接口返回全量列表（并返回 `total = 列表长度`）。

### 1.4 字段更新限制（重要）

为保证数据一致性，部分字段在后端做了强约束（即使请求中传入也会被忽略或直接报错）：

- `entCode`：新增必填；修改接口不允许变更所属企业
- 单据号：`applyNo / inNo / outNo / returnNo` 为系统生成，修改接口不允许变更
- 关联关系：
  - 出库单 `applyId` 不允许修改
  - 归还单 `applyId / outId` 不允许修改
- 审核字段：`auditBy / auditTime / auditRemark` 仅由审批回写更新，业务编辑接口不写入
- 申请单派生状态：`outStatus / returnStatus` 由出库/归还联动回写，编辑接口传参会被忽略

## 2. 物资基础信息（台账）

基础路径：`/process/materialInfo`

### 2.1 列表查询

- 方法：`POST`
- 路径：`/process/materialInfo/list`
- Body：`MaterialInfoReq`（可为空）
  - `materialId`：物资ID
  - `entCode`：企业编码
  - `entCodes`：企业编码列表
  - `materialCode`：物资编号
  - `materialName`：物资名称
  - `brand`：品牌
  - `modelSpec`：规格型号
  - `categoryCode`：物资分类编码
  - `status`：状态
- 响应：`data = MaterialInfo[]`

### 2.2 详情

- 方法：`GET`
- 路径：`/process/materialInfo/detail/{materialId}`
- 响应：`data = MaterialInfo`
  - `currentQty`：当前库存（由各仓库库存汇总得到）
  - `stockList`：各仓库库存明细列表（精简字段，避免重复回传物资/企业信息）
    - `stockId`：库存ID
    - `warehouseId`：仓库ID
    - `warehouseCode`：仓库编号
    - `warehouseName`：仓库名称
    - `currentQty`：当前库存
    - `availableQty`：可用库存
    - `frozenQty`：冻结库存
    - `minStock`：最低库存预警值
    - `stockStatus`：库存状态（1正常 / 2低库存 / 3无库存）
    - `lastChangeTime`、`createTime`、`updateTime`：`yyyy-MM-dd HH:mm:ss`

### 2.3 新增

- 方法：`POST`
- 路径：`/process/materialInfo/add`
- Body：`MaterialInfo`
- 关键字段（必填校验）：
  - `entCode`：企业编码
  - `materialCode`：物资编号
  - `materialName`：物资名称
  - `unit`：计量单位
- 响应：`data = materialId`

### 2.4 修改

- 方法：`POST`
- 路径：`/process/materialInfo/edit`
- Body：`MaterialInfo`
- 必填：`materialId`
- 不允许修改：`entCode`

### 2.5 删除

- 方法：`POST`
- 路径：`/process/materialInfo/remove`
- Body：`{ "materialId": "..." }`
- 说明：若物资已被业务数据引用，则不能删除

### 2.6 导出

- 方法：`POST`
- 路径：`/process/materialInfo/export`
- Body：`MaterialInfoReq`（可为空）
- 响应：Excel 文件流，文件名：`物资台账.xlsx`

### 2.7 下载导入模板

- 方法：`GET`
- 路径：`/process/materialInfo/template`
- 响应：Excel 文件流，文件名：`物资导入模板.xlsx`
- 模板字段（第 1 行表头）：
  - `企业编码`、`物资编号`、`物资名称`、`品牌`、`规格型号`、`物资分类编码`、`物资分类名称`、`单位`、`单价`、`最低库存预警值`、`状态(0启用 1停用)`、`备注`

### 2.8 导入

- 方法：`POST`
- 路径：`/process/materialInfo/import`
- Content-Type：`multipart/form-data`
- 表单字段：`file`（Excel 文件）
- 响应：`data = { successCount, failCount, failMessages }`

## 3. 仓库信息

基础路径：`/process/materialWarehouse`

### 3.1 列表查询

- 方法：`POST`
- 路径：`/process/materialWarehouse/list`
- Body：`MaterialWarehouseReq`（可为空）
  - `warehouseId`：仓库ID
  - `entCode`：企业编码
  - `entCodes`：企业编码列表
  - `warehouseCode`：仓库编号
  - `warehouseName`：仓库名称
  - `status`：状态
- 响应：`data = MaterialWarehouse[]`

### 3.2 新增

- 方法：`POST`
- 路径：`/process/materialWarehouse/add`
- Body：`MaterialWarehouse`
- 关键字段（必填校验）：`entCode`、`warehouseCode`、`warehouseName`

### 3.3 修改

- 方法：`POST`
- 路径：`/process/materialWarehouse/edit`
- Body：`MaterialWarehouse`
- 必填：`warehouseId`
- 不允许修改：`entCode`

### 3.4 删除

- 方法：`POST`
- 路径：`/process/materialWarehouse/remove`
- Body：`{ "warehouseId": "..." }`
- 说明：若仓库已被业务数据引用，则不能删除

## 4. 物资库存（汇总/流水）

基础路径：`/process/materialStock`

### 4.1 库存汇总列表

- 方法：`POST`
- 路径：`/process/materialStock/list`
- Body：`MaterialStockReq`（可为空）
  - `entCode`、`entCodes`、`warehouseId`
  - `materialId`、`materialName`、`brand`、`modelSpec`、`categoryCode`
  - `stockStatus`：库存状态（1正常 / 2低库存 / 3无库存）
- 响应：`data = MaterialStock[]`

### 4.2 库存流水列表

- 方法：`POST`
- 路径：`/process/materialStock/detail/list`
- Body：`MaterialStockFlowReq`（可为空）
  - `entCode`、`entCodes`、`warehouseId`、`materialId`
  - `bizType`：`IN/OUT/RETURN/ADJUST/FREEZE/UNFREEZE`
  - `startTime`、`endTime`：`yyyy-MM-dd HH:mm:ss`
- 响应：`data = MaterialStockFlow[]`

### 4.3 导出库存汇总

- 方法：`POST`
- 路径：`/process/materialStock/export`
- Body：`MaterialStockReq`（可为空）
- 响应：Excel 文件流，文件名：`物资库存汇总.xlsx`

### 4.4 导出库存流水

- 方法：`POST`
- 路径：`/process/materialStock/detail/export`
- Body：`MaterialStockFlowReq`（可为空）
- 响应：Excel 文件流，文件名：`物资库存明细.xlsx`

## 5. 物资申请/入库/出库/归还

公共查询对象（列表接口 Body）：`MaterialBizReq`（可为空）

- `id`：业务主键ID
- `entCode`、`entCodes`：企业编码/列表
- `warehouseId`：仓库ID
- `orderNo`：单据编号
- `materialName`：物资名称
- `status`：单据状态
- `auditStatus`：审核状态（申请单用）
- `startTime`、`endTime`：`yyyy-MM-dd HH:mm:ss`

### 5.0 审批说明

物资申请/入库/出库/归还均接入平台审批，审批过程为异步：

- **发起审批**：通过各单据的 `POST /edit` 接口把状态置为“待审核”
  - 申请单：`auditStatus = 2`
  - 入库/出库/归还：`status = 2`
- **审批取消**：在平台审批中将实例置为 `CANCELLED`（取消）即可，流程服务侧不提供单据取消/撤回接口；取消结果通过 `approval_result` 回写到业务单据
- **审批结果回写**：系统订阅平台 `approval_result` 消息，收到后自动更新单据状态，并驱动库存动作（见下方“出库单冻结流转”）

出库单冻结流转（正常逻辑）：

- **提交出库审批（`status=2`）**：立即冻结出库数量（`frozenQty` 增加、`availableQty` 减少），避免重复占用
- **审批通过（`APPROVED`）**：消耗冻结库存并扣减当前库存（`currentQty` 减少、`frozenQty` 减少）
- **审批驳回/取消（`REJECTED/CANCELLED`）**：单据回到可编辑状态（草稿）时解冻释放（`availableQty` 恢复、`frozenQty` 减少）

入库/归还库存生效（正常逻辑）：

- **入库单**：审批通过后才增加库存；审批驳回/取消只回到草稿，不影响库存
- **归还单**：审批通过后才按 `stockInQty` 回补库存，并回写出库/申请的归还数量；审批驳回/取消只回到草稿，不影响库存

平台审批回调（内部）消息字段（`approval_result`）：

- `businessType`：`material_apply / material_in / material_out / material_return`
- `businessKey`：对应单据主键（`applyId / inId / outId / returnId`）
- `status`：`PROCESSING / APPROVED / REJECTED / CANCELLED`
- `comment`：审批意见

回调 `status` 与单据状态的对应关系（简要）：

- `PROCESSING`：审批流进入处理阶段（审批中，业务侧通常仍显示为“待审核”）
- `APPROVED`：单据进入“已审核/审核通过”，并触发库存动作
- `REJECTED`：单据回到“草稿/驳回”（不同单据字段不同，见下方状态枚举）
- `CANCELLED`：申请单进入“已取消”；入库/出库/归还回到“草稿”（并按规则解冻或不生效库存）

### 5.1 物资申请单

基础路径：`/process/materialApplyOrder`

- `POST /list`：列表（Body：`MaterialBizReq`）
- `GET /detail/{applyId}`：详情（返回 `MaterialApplyOrder`，包含 `itemList/annexInfoList/operateLogList`）
- `POST /add`：新增（Body：`MaterialApplyOrder`，必填：`entCode`、`itemList`）
- `POST /edit`：修改（Body：`MaterialApplyOrder`，必填：`applyId`）
- `POST /remove`：删除（Body：`{ "applyId": "..." }`）
- `POST /export`：导出（Body：`MaterialBizReq`），文件名：`物资申请单.xlsx`（含“申请单/申请明细”两个 Sheet）

提交审核（发起审批）：

- 调用：`POST /process/materialApplyOrder/edit`
- Body（仅做状态流转时可不传明细）：
  - `applyId`：必填
  - `auditStatus`：传 `2` 表示提交审核
  - `remark`：可选（提交说明/备注）

说明：

- `remark` 用于业务侧提交说明/备注（提交、回退、作废等业务动作均使用该字段）
- `auditRemark` 为审批意见字段，仅审批回写更新，业务编辑接口传入会被忽略

申请单审核状态 `auditStatus`：

- `1`：草稿
- `2`：待审核
- `3`：审核通过
- `4`：审核驳回
- `5`：已取消
- `6`：已保存

### 5.2 物资入库单

基础路径：`/process/materialInOrder`

- `POST /list`：列表（Body：`MaterialBizReq`）
- `GET /detail/{inId}`：详情（返回 `MaterialInOrder`，包含 `itemList/annexInfoList/operateLogList`）
- `POST /add`：新增（Body：`MaterialInOrder`，必填：`entCode/warehouseId/itemList`）
- `POST /edit`：修改（Body：`MaterialInOrder`，必填：`inId`）
- `POST /remove`：删除（Body：`{ "inId": "..." }`）
- `POST /export`：导出（Body：`MaterialBizReq`），文件名：`物资入库单.xlsx`

提交审核（发起审批）：

- 调用：`POST /process/materialInOrder/edit`
- Body（仅做状态流转时可不传明细）：
  - `inId`：必填
  - `status`：传 `2` 表示提交审核
  - `remark`：可选（提交说明/备注）

入库单状态 `status`：

- `1`：草稿
- `2`：待审核
- `3`：已审核
- `4`：已完成
- `6`：已保存
- `0`：已作废

### 5.3 物资出库单

基础路径：`/process/materialOutOrder`

- `POST /list`：列表（Body：`MaterialBizReq`）
- `GET /detail/{outId}`：详情（返回 `MaterialOutOrder`，包含 `itemList/annexInfoList/operateLogList`）
- `POST /add`：新增（Body：`MaterialOutOrder`，必填：`entCode/warehouseId/itemList`）
- `POST /edit`：修改（Body：`MaterialOutOrder`，必填：`outId`）
- `POST /remove`：删除（Body：`{ "outId": "..." }`）
- `POST /export`：导出（Body：`MaterialBizReq`），文件名：`物资出库单.xlsx`

提交审核（发起审批）：

- 调用：`POST /process/materialOutOrder/edit`
- Body（仅做状态流转时可不传明细）：
  - `outId`：必填
  - `status`：传 `2` 表示提交审核（提交后立即冻结出库数量）
  - `remark`：可选（提交说明/备注）

出库单状态 `status`：

- `1`：草稿
- `2`：待审核
- `3`：已审核
- `4`：已完成
- `6`：已保存
- `0`：已作废

### 5.4 物资归还单

基础路径：`/process/materialReturnOrder`

- `POST /list`：列表（Body：`MaterialBizReq`）
- `GET /detail/{returnId}`：详情（返回 `MaterialReturnOrder`，包含 `itemList/annexInfoList/operateLogList`）
- `POST /add`：新增（Body：`MaterialReturnOrder`，必填：`entCode/warehouseId/itemList`）
- `POST /edit`：修改（Body：`MaterialReturnOrder`，必填：`returnId`）
- `POST /remove`：删除（Body：`{ "returnId": "..." }`）
- `POST /export`：导出（Body：`MaterialBizReq`），文件名：`物资归还单.xlsx`

提交审核（发起审批）：

- 调用：`POST /process/materialReturnOrder/edit`
- Body（仅做状态流转时可不传明细）：
  - `returnId`：必填
  - `status`：传 `2` 表示提交审核
  - `remark`：可选（提交说明/备注）

归还单状态 `status`：

- `1`：草稿
- `2`：待审核
- `3`：已审核
- `4`：已完成
- `6`：已保存
- `0`：已作废

## 6. 示例

### 6.1 库存汇总列表

```http
POST /process/materialStock/list?pageNum=1&pageSize=10
Authorization: Bearer <token>
Content-Type: application/json

{
  "entCode": "ENT001",
  "warehouseId": "WH001",
  "materialName": "防护"
}
```

```json
{
  "code": 200,
  "msg": "操作成功",
  "data": [],
  "total": 0
}
```
