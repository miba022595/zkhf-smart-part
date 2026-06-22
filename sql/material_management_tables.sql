-- ----------------------------
-- Table structure for t_material_info
-- ----------------------------
DROP TABLE IF EXISTS `t_material_info`;
CREATE TABLE `t_material_info` (
  `material_id` varchar(26) NOT NULL COMMENT '物资ID',
  `ent_code` varchar(26) NOT NULL COMMENT '企业编码',
  `material_code` varchar(64) NOT NULL COMMENT '物资编号',
  `material_name` varchar(200) NOT NULL COMMENT '物资名称',
  `brand` varchar(100) DEFAULT NULL COMMENT '品牌',
  `model_spec` varchar(200) DEFAULT NULL COMMENT '规格型号',
  `category_code` varchar(64) DEFAULT NULL COMMENT '物资分类编码',
  `category_name` varchar(100) DEFAULT NULL COMMENT '物资分类名称',
  `unit` varchar(32) NOT NULL COMMENT '计量单位',
  `unit_price` double DEFAULT 0 COMMENT '单价',
  `min_stock` double DEFAULT 0 COMMENT '最低库存预警值',
  `status` tinyint DEFAULT 0 COMMENT '状态（0正常 1停用）',
  `remark` varchar(500) DEFAULT NULL COMMENT '备注',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`material_id`),
  UNIQUE INDEX `uk_ent_material_code`(`ent_code`, `material_code`),
  INDEX `idx_ent_name`(`ent_code`, `material_name`),
  INDEX `idx_category`(`category_code`),
  INDEX `idx_status`(`status`)
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '物资基础信息表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for t_material_warehouse
-- ----------------------------
DROP TABLE IF EXISTS `t_material_warehouse`;
CREATE TABLE `t_material_warehouse` (
  `warehouse_id` varchar(26) NOT NULL COMMENT '仓库ID',
  `ent_code` varchar(26) NOT NULL COMMENT '企业编码',
  `warehouse_code` varchar(64) NOT NULL COMMENT '仓库编号',
  `warehouse_name` varchar(100) NOT NULL COMMENT '仓库名称',
  `manager_name` varchar(64) DEFAULT NULL COMMENT '仓库管理员',
  `manager_phone` varchar(20) DEFAULT NULL COMMENT '联系电话',
  `warehouse_address` varchar(255) DEFAULT NULL COMMENT '仓库地址',
  `sort_num` int DEFAULT 0 COMMENT '排序号',
  `status` tinyint DEFAULT 0 COMMENT '状态（0正常 1停用）',
  `remark` varchar(500) DEFAULT NULL COMMENT '备注',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`warehouse_id`),
  UNIQUE INDEX `uk_ent_warehouse_code`(`ent_code`, `warehouse_code`),
  INDEX `idx_ent_name`(`ent_code`, `warehouse_name`),
  INDEX `idx_status`(`status`)
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '仓库信息表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for t_material_stock
-- ----------------------------
DROP TABLE IF EXISTS `t_material_stock`;
CREATE TABLE `t_material_stock` (
  `stock_id` varchar(26) NOT NULL COMMENT '库存ID',
  `ent_code` varchar(26) NOT NULL COMMENT '企业编码',
  `warehouse_id` varchar(26) NOT NULL COMMENT '仓库ID',
  `material_id` varchar(26) NOT NULL COMMENT '物资ID',
  `current_qty` double DEFAULT 0 COMMENT '当前库存',
  `available_qty` double DEFAULT 0 COMMENT '可用库存',
  `frozen_qty` double DEFAULT 0 COMMENT '冻结库存',
  `min_stock` double DEFAULT 0 COMMENT '最低库存预警值',
  `stock_status` tinyint DEFAULT 1 COMMENT '库存状态：1-正常，2-低库存，3-无库存',
  `last_change_time` datetime DEFAULT NULL COMMENT '最近变动时间',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`stock_id`),
  UNIQUE INDEX `uk_ent_warehouse_material`(`ent_code`, `warehouse_id`, `material_id`),
  INDEX `idx_material`(`material_id`),
  INDEX `idx_stock_status`(`stock_status`),
  INDEX `idx_change_time`(`last_change_time`)
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '物资库存汇总表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for t_material_in_order
-- ----------------------------
DROP TABLE IF EXISTS `t_material_in_order`;
CREATE TABLE `t_material_in_order` (
  `in_id` varchar(26) NOT NULL COMMENT '入库单ID',
  `ent_code` varchar(26) NOT NULL COMMENT '企业编码',
  `in_no` varchar(64) NOT NULL COMMENT '入库单号',
  `in_time` datetime NOT NULL COMMENT '入库时间',
  `warehouse_id` varchar(26) NOT NULL COMMENT '仓库ID',
  `arrival_no` varchar(64) DEFAULT NULL COMMENT '到货单号',
  `purchaser` varchar(64) DEFAULT NULL COMMENT '采购人员',
  `in_user` varchar(64) DEFAULT NULL COMMENT '入库人员',
  `status` tinyint DEFAULT 1 COMMENT '状态：1-草稿，2-待审核，3-已审核，4-已完成，0-已作废',
  `audit_by` varchar(64) DEFAULT NULL COMMENT '审核人',
  `audit_time` datetime DEFAULT NULL COMMENT '审核时间',
  `audit_remark` varchar(500) DEFAULT NULL COMMENT '审核意见',
  `total_qty` double DEFAULT 0 COMMENT '入库总数量',
  `total_amount` double DEFAULT 0 COMMENT '入库总金额',
  `stock_effect_status` tinyint DEFAULT 0 COMMENT '库存生效状态：0-未生效，1-已生效',
  `remark` varchar(500) DEFAULT NULL COMMENT '备注',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`in_id`),
  UNIQUE INDEX `uk_in_no`(`in_no`),
  INDEX `idx_ent_time`(`ent_code`, `in_time`),
  INDEX `idx_warehouse`(`warehouse_id`),
  INDEX `idx_status`(`status`)
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '物资入库单' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for t_material_in_order_item
-- ----------------------------
DROP TABLE IF EXISTS `t_material_in_order_item`;
CREATE TABLE `t_material_in_order_item` (
  `in_item_id` varchar(26) NOT NULL COMMENT '入库单明细ID',
  `in_id` varchar(26) NOT NULL COMMENT '入库单ID',
  `material_id` varchar(26) NOT NULL COMMENT '物资ID',
  `material_code` varchar(64) NOT NULL COMMENT '物资编号',
  `material_name` varchar(200) NOT NULL COMMENT '物资名称',
  `brand` varchar(100) DEFAULT NULL COMMENT '品牌',
  `model_spec` varchar(200) DEFAULT NULL COMMENT '规格型号',
  `category_name` varchar(100) DEFAULT NULL COMMENT '分类名称',
  `unit` varchar(32) NOT NULL COMMENT '单位',
  `in_qty` double NOT NULL COMMENT '入库数量',
  `unit_price` double DEFAULT 0 COMMENT '单价',
  `amount` double DEFAULT 0 COMMENT '金额',
  `sort_num` int DEFAULT 1 COMMENT '排序号',
  `remark` varchar(500) DEFAULT NULL COMMENT '备注',
  PRIMARY KEY (`in_item_id`),
  INDEX `idx_in_id`(`in_id`),
  INDEX `idx_material_id`(`material_id`)
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '物资入库单明细' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for t_material_apply_order
-- ----------------------------
DROP TABLE IF EXISTS `t_material_apply_order`;
CREATE TABLE `t_material_apply_order` (
  `apply_id` varchar(26) NOT NULL COMMENT '申请单ID',
  `ent_code` varchar(26) NOT NULL COMMENT '企业编码',
  `apply_no` varchar(64) NOT NULL COMMENT '申请单号',
  `apply_time` datetime NOT NULL COMMENT '申请时间',
  `apply_reason` varchar(500) DEFAULT NULL COMMENT '申请事由',
  `apply_user` varchar(64) DEFAULT NULL COMMENT '申请人',
  `contact_phone` varchar(20) DEFAULT NULL COMMENT '联系方式',
  `expected_receive_time` datetime DEFAULT NULL COMMENT '期望领用时间',
  `audit_status` tinyint DEFAULT 1 COMMENT '审核状态：1-草稿，2-待审核，3-审核通过，4-审核驳回，5-已撤回，6-已完成',
  `audit_by` varchar(64) DEFAULT NULL COMMENT '审核人',
  `audit_time` datetime DEFAULT NULL COMMENT '审核时间',
  `audit_remark` varchar(500) DEFAULT NULL COMMENT '审核意见',
  `out_status` tinyint DEFAULT 0 COMMENT '出库状态：0-未出库，1-部分出库，2-已出库',
  `return_status` tinyint DEFAULT 0 COMMENT '归还状态：0-未归还，1-部分归还，2-已归还',
  `total_qty` double DEFAULT 0 COMMENT '申请总数量',
  `remark` varchar(500) DEFAULT NULL COMMENT '备注',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`apply_id`),
  UNIQUE INDEX `uk_apply_no`(`apply_no`),
  INDEX `idx_ent_time`(`ent_code`, `apply_time`),
  INDEX `idx_audit_status`(`audit_status`),
  INDEX `idx_out_status`(`out_status`)
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '物资申请单' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for t_material_apply_order_item
-- ----------------------------
DROP TABLE IF EXISTS `t_material_apply_order_item`;
CREATE TABLE `t_material_apply_order_item` (
  `apply_item_id` varchar(26) NOT NULL COMMENT '申请单明细ID',
  `apply_id` varchar(26) NOT NULL COMMENT '申请单ID',
  `material_id` varchar(26) NOT NULL COMMENT '物资ID',
  `material_code` varchar(64) NOT NULL COMMENT '物资编号',
  `material_name` varchar(200) NOT NULL COMMENT '物资名称',
  `brand` varchar(100) DEFAULT NULL COMMENT '品牌',
  `model_spec` varchar(200) DEFAULT NULL COMMENT '规格型号',
  `category_name` varchar(100) DEFAULT NULL COMMENT '分类名称',
  `unit` varchar(32) NOT NULL COMMENT '单位',
  `apply_qty` double NOT NULL COMMENT '申请数量',
  `out_qty` double DEFAULT 0 COMMENT '累计出库数量',
  `return_qty` double DEFAULT 0 COMMENT '累计归还数量',
  `purpose_desc` varchar(500) DEFAULT NULL COMMENT '用途说明',
  `sort_num` int DEFAULT 1 COMMENT '排序号',
  `remark` varchar(500) DEFAULT NULL COMMENT '备注',
  PRIMARY KEY (`apply_item_id`),
  INDEX `idx_apply_id`(`apply_id`),
  INDEX `idx_material_id`(`material_id`)
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '物资申请单明细' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for t_material_out_order
-- ----------------------------
DROP TABLE IF EXISTS `t_material_out_order`;
CREATE TABLE `t_material_out_order` (
  `out_id` varchar(26) NOT NULL COMMENT '出库单ID',
  `ent_code` varchar(26) NOT NULL COMMENT '企业编码',
  `out_no` varchar(64) NOT NULL COMMENT '出库单号',
  `out_time` datetime NOT NULL COMMENT '出库时间',
  `warehouse_id` varchar(26) NOT NULL COMMENT '仓库ID',
  `apply_id` varchar(26) DEFAULT NULL COMMENT '关联申请单ID',
  `receive_user` varchar(64) DEFAULT NULL COMMENT '领用人员',
  `out_user` varchar(64) DEFAULT NULL COMMENT '出库人员',
  `audit_by` varchar(64) DEFAULT NULL COMMENT '审核人员',
  `audit_time` datetime DEFAULT NULL COMMENT '审核时间',
  `audit_remark` varchar(500) DEFAULT NULL COMMENT '审核意见',
  `status` tinyint DEFAULT 1 COMMENT '状态：1-草稿，2-待审核，3-已审核，4-已完成，0-已作废',
  `stock_effect_status` tinyint DEFAULT 0 COMMENT '库存生效状态：0-未生效，1-已生效',
  `total_qty` double DEFAULT 0 COMMENT '出库总数量',
  `total_amount` double DEFAULT 0 COMMENT '出库总金额',
  `remark` varchar(500) DEFAULT NULL COMMENT '备注',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`out_id`),
  UNIQUE INDEX `uk_out_no`(`out_no`),
  INDEX `idx_ent_time`(`ent_code`, `out_time`),
  INDEX `idx_warehouse`(`warehouse_id`),
  INDEX `idx_apply_id`(`apply_id`),
  INDEX `idx_status`(`status`)
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '物资出库单' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for t_material_out_order_item
-- ----------------------------
DROP TABLE IF EXISTS `t_material_out_order_item`;
CREATE TABLE `t_material_out_order_item` (
  `out_item_id` varchar(26) NOT NULL COMMENT '出库单明细ID',
  `out_id` varchar(26) NOT NULL COMMENT '出库单ID',
  `apply_item_id` varchar(26) DEFAULT NULL COMMENT '关联申请单明细ID',
  `material_id` varchar(26) NOT NULL COMMENT '物资ID',
  `material_code` varchar(64) NOT NULL COMMENT '物资编号',
  `material_name` varchar(200) NOT NULL COMMENT '物资名称',
  `brand` varchar(100) DEFAULT NULL COMMENT '品牌',
  `model_spec` varchar(200) DEFAULT NULL COMMENT '规格型号',
  `category_name` varchar(100) DEFAULT NULL COMMENT '分类名称',
  `unit` varchar(32) NOT NULL COMMENT '单位',
  `apply_qty` double DEFAULT 0 COMMENT '申请数量',
  `out_qty` double NOT NULL COMMENT '实际出库数量',
  `returned_qty` double DEFAULT 0 COMMENT '累计归还数量',
  `unit_price` double DEFAULT 0 COMMENT '单价',
  `amount` double DEFAULT 0 COMMENT '金额',
  `sort_num` int DEFAULT 1 COMMENT '排序号',
  `remark` varchar(500) DEFAULT NULL COMMENT '备注',
  PRIMARY KEY (`out_item_id`),
  INDEX `idx_out_id`(`out_id`),
  INDEX `idx_apply_item_id`(`apply_item_id`),
  INDEX `idx_material_id`(`material_id`)
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '物资出库单明细' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for t_material_return_order
-- ----------------------------
DROP TABLE IF EXISTS `t_material_return_order`;
CREATE TABLE `t_material_return_order` (
  `return_id` varchar(26) NOT NULL COMMENT '归还单ID',
  `ent_code` varchar(26) NOT NULL COMMENT '企业编码',
  `return_no` varchar(64) NOT NULL COMMENT '归还单号',
  `apply_id` varchar(26) DEFAULT NULL COMMENT '关联申请单ID',
  `out_id` varchar(26) DEFAULT NULL COMMENT '关联出库单ID',
  `warehouse_id` varchar(26) DEFAULT NULL COMMENT '归还入库仓库ID',
  `apply_time` datetime DEFAULT NULL COMMENT '申请时间',
  `return_time` datetime NOT NULL COMMENT '归还时间',
  `return_user` varchar(64) DEFAULT NULL COMMENT '归还人员',
  `audit_by` varchar(64) DEFAULT NULL COMMENT '审核人员',
  `audit_time` datetime DEFAULT NULL COMMENT '审核时间',
  `audit_remark` varchar(500) DEFAULT NULL COMMENT '审核意见',
  `handler_user` varchar(64) DEFAULT NULL COMMENT '物资处理人员',
  `status` tinyint DEFAULT 1 COMMENT '状态：1-草稿，2-待审核，3-已审核，4-已完成，0-已作废',
  `stock_effect_status` tinyint DEFAULT 0 COMMENT '库存生效状态：0-未生效，1-已生效',
  `total_qty` double DEFAULT 0 COMMENT '归还总数量',
  `stock_in_qty` double DEFAULT 0 COMMENT '回补库存总数量',
  `remark` varchar(500) DEFAULT NULL COMMENT '归还说明',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`return_id`),
  UNIQUE INDEX `uk_return_no`(`return_no`),
  INDEX `idx_ent_time`(`ent_code`, `return_time`),
  INDEX `idx_out_id`(`out_id`),
  INDEX `idx_status`(`status`)
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '物资归还单' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for t_material_return_order_item
-- ----------------------------
DROP TABLE IF EXISTS `t_material_return_order_item`;
CREATE TABLE `t_material_return_order_item` (
  `return_item_id` varchar(26) NOT NULL COMMENT '归还单明细ID',
  `return_id` varchar(26) NOT NULL COMMENT '归还单ID',
  `out_item_id` varchar(26) DEFAULT NULL COMMENT '关联出库单明细ID',
  `material_id` varchar(26) NOT NULL COMMENT '物资ID',
  `material_code` varchar(64) NOT NULL COMMENT '物资编号',
  `material_name` varchar(200) NOT NULL COMMENT '物资名称',
  `brand` varchar(100) DEFAULT NULL COMMENT '品牌',
  `model_spec` varchar(200) DEFAULT NULL COMMENT '规格型号',
  `category_name` varchar(100) DEFAULT NULL COMMENT '分类名称',
  `unit` varchar(32) NOT NULL COMMENT '单位',
  `out_qty` double DEFAULT 0 COMMENT '原出库数量',
  `can_return_qty` double DEFAULT 0 COMMENT '可归还数量',
  `return_qty` double NOT NULL COMMENT '本次归还数量',
  `stock_in_qty` double DEFAULT 0 COMMENT '实际回补库存数量',
  `process_result` varchar(20) DEFAULT 'NORMAL' COMMENT '处理结果：NORMAL-完好，REPAIR-维修，SCRAP-报废，PARTIAL-部分回补',
  `sort_num` int DEFAULT 1 COMMENT '排序号',
  `remark` varchar(500) DEFAULT NULL COMMENT '备注',
  PRIMARY KEY (`return_item_id`),
  INDEX `idx_return_id`(`return_id`),
  INDEX `idx_out_item_id`(`out_item_id`),
  INDEX `idx_material_id`(`material_id`)
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '物资归还单明细' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for t_material_stock_flow
-- ----------------------------
DROP TABLE IF EXISTS `t_material_stock_flow`;
CREATE TABLE `t_material_stock_flow` (
  `flow_id` varchar(26) NOT NULL COMMENT '流水ID',
  `ent_code` varchar(26) NOT NULL COMMENT '企业编码',
  `warehouse_id` varchar(26) NOT NULL COMMENT '仓库ID',
  `material_id` varchar(26) NOT NULL COMMENT '物资ID',
  `biz_type` varchar(20) NOT NULL COMMENT '业务类型：IN/OUT/RETURN/ADJUST/FREEZE/UNFREEZE',
  `biz_id` varchar(26) NOT NULL COMMENT '业务主表ID',
  `biz_item_id` varchar(26) DEFAULT NULL COMMENT '业务明细ID',
  `biz_no` varchar(64) DEFAULT NULL COMMENT '业务单号',
  `qty_change` double NOT NULL COMMENT '库存变动数量，正数增加，负数减少',
  `before_qty` double DEFAULT 0 COMMENT '变动前当前库存',
  `after_qty` double DEFAULT 0 COMMENT '变动后当前库存',
  `before_available_qty` double DEFAULT 0 COMMENT '变动前可用库存',
  `after_available_qty` double DEFAULT 0 COMMENT '变动后可用库存',
  `before_frozen_qty` double DEFAULT 0 COMMENT '变动前冻结库存',
  `after_frozen_qty` double DEFAULT 0 COMMENT '变动后冻结库存',
  `operate_by` varchar(64) DEFAULT NULL COMMENT '操作人',
  `operate_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '操作时间',
  `remark` varchar(500) DEFAULT NULL COMMENT '备注',
  PRIMARY KEY (`flow_id`),
  INDEX `idx_ent_material_time`(`ent_code`, `material_id`, `operate_time`),
  INDEX `idx_biz`(`biz_type`, `biz_id`),
  INDEX `idx_warehouse`(`warehouse_id`)
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '物资库存流水表' ROW_FORMAT = Dynamic;
