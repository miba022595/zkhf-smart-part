SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- Table structure for t_data_effective_trans
-- ----------------------------
DROP TABLE IF EXISTS `t_data_effective_trans`;
CREATE TABLE `t_data_effective_trans` (
  `eff_id` varchar(20) NOT NULL COMMENT '主键id',
  `out_put_id` varchar(26) NOT NULL COMMENT '关联排口主键id',
  `data_type` tinyint DEFAULT NULL COMMENT '数据来源；1 小时数据、2 日数据',
  `real_trans` int DEFAULT NULL COMMENT '传输实收量',
  `must_trans` int DEFAULT NULL COMMENT '传输应收量',
  `real_valid` int DEFAULT NULL COMMENT '有效实收量',
  `must_valid` int DEFAULT NULL COMMENT '有效应收量',
  `monitor_time` datetime DEFAULT NULL COMMENT '监测时间',
  PRIMARY KEY (`eff_id`, `out_put_id`),
  INDEX `index_monitor_time`(`monitor_time`)
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '数据传输有效率表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for t_data_hour_left_emission_info
-- ----------------------------
DROP TABLE IF EXISTS `t_data_hour_left_emission_info`;
CREATE TABLE `t_data_hour_left_emission_info` (
  `out_put_id` varchar(30) NOT NULL COMMENT '排口id',
  `pollutant_code` varchar(64) NOT NULL COMMENT '污染因子编码',
  `standard_value` double DEFAULT NULL COMMENT '污染物最大值',
  `avg_value` double DEFAULT NULL COMMENT '当前小时污染物累计平均值',
  `surplus_value` double DEFAULT NULL COMMENT '当前小时剩余控制平均值',
  PRIMARY KEY (`out_put_id`, `pollutant_code`)
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '小时剩余排放控制表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for t_data_out_alarm
-- ----------------------------
DROP TABLE IF EXISTS `t_data_out_alarm`;
CREATE TABLE `t_data_out_alarm` (
  `alarm_id` varchar(26) NOT NULL COMMENT '主键id',
  `out_put_id` varchar(26) DEFAULT NULL COMMENT '排口主键id',
  `pollutant_code` varchar(16) DEFAULT NULL COMMENT '污染因子code',
  `data_type` tinyint DEFAULT NULL COMMENT '数据类型：参见 {@link DataTypeEnum}',
  `alarm_type` tinyint DEFAULT NULL COMMENT '报警类型；参见 {@link AlarmDetailTypeEnum}',
  `start_time` datetime DEFAULT NULL COMMENT '报警发生时间',
  `end_time` datetime DEFAULT NULL COMMENT '报警结束时间',
  `alarm_status` tinyint DEFAULT 0 COMMENT '报警状态，0未解除；1已解除',
  `deal_status` tinyint DEFAULT 0 COMMENT '处理状态，0未处理；1已处理',
  `alarm_msg` text COMMENT '报警信息',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`alarm_id`),
  INDEX `idx_out_put_id`(`out_put_id`),
  INDEX `idx_time_range`(`start_time`),
  INDEX `ind_alarm_type`(`alarm_type`)
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '排放口报警记录表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Table structure for t_data_out_alarm_deal
-- ----------------------------
DROP TABLE IF EXISTS `t_data_out_alarm_deal`;
CREATE TABLE `t_data_out_alarm_deal` (
  `deal_id` varchar(26) NOT NULL COMMENT '主键id',
  `alarm_id` varchar(26) DEFAULT NULL COMMENT '报警主键id',
  `deal_user_id` bigint DEFAULT NULL COMMENT '处理账号ID',
  `deal_info` text COMMENT '处理信息',
  `deal_time` datetime DEFAULT NULL COMMENT '处理时间',
  PRIMARY KEY (`deal_id`),
  INDEX `ind_alarm_id`(`alarm_id`)
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '报警处理情况表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for t_data_out_base
-- ----------------------------
DROP TABLE IF EXISTS `t_data_out_base`;
CREATE TABLE `t_data_out_base` (
  `out_id` varchar(20) NOT NULL COMMENT '主键id',
  `data_type` tinyint DEFAULT NULL COMMENT '数据类型：1实时数据，2分钟数据，3小时数据；4日数据',
  `data_alarm` tinyint DEFAULT 0 COMMENT '该时刻是否有报警：0：正常；1：发生报警',
  `monitor_time` datetime DEFAULT NULL COMMENT '污染物监测时间',
  `data_info` json NULL COMMENT '污染信息',
  `create_time` datetime DEFAULT NULL COMMENT '数据创建时间',
  `update_time` datetime DEFAULT NULL COMMENT '数据更新时间',
  PRIMARY KEY (`out_id`),
  INDEX `idx_data_type`(`data_type`, `monitor_time`),
  INDEX `idx_data_alarm`(`data_alarm`)
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '排口监测数据' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Table structure for t_data_out_report
-- ----------------------------
DROP TABLE IF EXISTS `t_data_out_report`;
CREATE TABLE `t_data_out_report` (
  `out_put_id` varchar(30) NOT NULL COMMENT '排口id',
  `out_id` varchar(20) NOT NULL COMMENT '数据标识id，check_frequency+sample_date',
  `check_frequency` tinyint DEFAULT NULL COMMENT '计划检测频次，参考CheckFrequencyType',
  `data_alarm` tinyint DEFAULT 0 COMMENT '该时刻是否有报警：0：正常；1：发生报警',
  `sample_date` date DEFAULT NULL COMMENT '采样日期',
  `data_info` json COMMENT '污染信息',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '数据创建时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '数据更新时间',
  PRIMARY KEY (`out_put_id`, `out_id`),
  INDEX `idx_data_type`(`check_frequency`, `sample_date`),
  INDEX `idx_data_alarm`(`data_alarm`)
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '环境手工检测报告数据列表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Table structure for t_data_out_warn
-- ----------------------------
DROP TABLE IF EXISTS `t_data_out_warn`;
CREATE TABLE `t_data_out_warn` (
  `alarm_id` varchar(26) NOT NULL COMMENT '主键id',
  `out_put_id` varchar(26) DEFAULT NULL COMMENT '排口主键id',
  `pollutant_code` varchar(16) DEFAULT NULL COMMENT '污染因子code',
  `data_type` tinyint DEFAULT NULL COMMENT '数据类型：参见 {@link DataTypeEnum}',
  `alarm_type` tinyint DEFAULT NULL COMMENT '预警类型；参见 {@link AlarmDetailTypeEnum}',
  `start_time` datetime DEFAULT NULL COMMENT '预警发生时间',
  `alarm_msg` text COMMENT '预警信息',
  PRIMARY KEY (`alarm_id`),
  INDEX `idx_out_put`(`out_put_id`),
  INDEX `idx_time_range`(`start_time`),
  INDEX `ind_alarm_type`(`alarm_type`)
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '排放口预警记录表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Table structure for t_enforce_record
-- ----------------------------
DROP TABLE IF EXISTS `t_enforce_record`;
CREATE TABLE `t_enforce_record` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '执法记录ID',
  `ent_code` varchar(26) NOT NULL COMMENT '企业编码',
  `out_put_id` varchar(26) NOT NULL COMMENT '排放口ID',
  `unit_name` varchar(255) NOT NULL COMMENT '检查单位名称',
  `inspector` varchar(255) DEFAULT NULL COMMENT '检查人',
  `ops_unit` varchar(26) DEFAULT NULL COMMENT '运维单位ID（第三方单位）',
  `ops_user` bigint DEFAULT NULL COMMENT '运维人员ID',
  `check_date` datetime NOT NULL COMMENT '检查时间',
  `check_reason` text COMMENT '检查原因',
  `check_flag` tinyint DEFAULT 1 COMMENT '检查结果是否合格（1：合格，0：不合格/异常）',
  `check_conclusion` text COMMENT '检查结论',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `remark` text COMMENT '备注',
  PRIMARY KEY (`id`),
  INDEX `idx_ent_code`(`ent_code`),
  INDEX `idx_out_put`(`out_put_id`),
  INDEX `idx_check_date`(`check_date`),
  INDEX `idx_ops_user`(`ops_user`)
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '执法检查记录表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Table structure for t_ent_annual_emission
-- ----------------------------
DROP TABLE IF EXISTS `t_ent_annual_emission`;
CREATE TABLE `t_ent_annual_emission` (
  `ent_code` varchar(26) NOT NULL COMMENT '企业编码',
  `out_put_id` varchar(26) NOT NULL COMMENT '对应的排口主键id',
  `emission_year` int NOT NULL COMMENT '排放年份',
  `pollutant_code` varchar(16) NOT NULL COMMENT '污染因子编码',
  `emissions` decimal(20, 4) DEFAULT NULL COMMENT '排放量',
  PRIMARY KEY (`ent_code`, `out_put_id`, `emission_year`, `pollutant_code`)
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '企业年排量信息记录表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for t_plc_raw_base
-- ----------------------------
DROP TABLE IF EXISTS `t_plc_raw_base`;
CREATE TABLE `t_plc_raw_base` (
  `id` varchar(26) NOT NULL COMMENT '主键(ulid)',
  `type` varchar(10) NOT NULL COMMENT '数据类型，单元ID+_+功能码',
  `data` text COMMENT '十六进制数据',
  `report_time` datetime NOT NULL COMMENT '接收时间',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  INDEX `idx_type`(`type`, `report_time`),
  INDEX `idx_report_time`(`report_time`)
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = 'PLC监测数据' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Table structure for t_valid_period_info
-- ----------------------------
DROP TABLE IF EXISTS `t_valid_period_info`;
CREATE TABLE `t_valid_period_info` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键id',
  `ent_code` varchar(26) DEFAULT NULL COMMENT '企业唯一编码',
  `conf_type` tinyint DEFAULT NULL COMMENT '有效期类型',
  `item_id` varchar(26) DEFAULT NULL COMMENT '数据主键，唯一标识用',
  `item_name` varchar(255) DEFAULT NULL COMMENT '数据描述',
  `left_days` bigint DEFAULT NULL COMMENT '剩余有效天数',
  `alarm_type` varchar(10) DEFAULT NULL COMMENT '有效期报警类型',
  `alarm_rage` char(3) DEFAULT NULL COMMENT '报警频率，频率格式:D-天/M-月/H-小时+数字,如D10=每10天1次',
  `begin_date` date DEFAULT NULL COMMENT '有效期-开始时间',
  `end_date` date DEFAULT NULL COMMENT '有效期-结束时间',
  `send_time` datetime DEFAULT NULL COMMENT '上次发送报警的时间',
  PRIMARY KEY (`id`)
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '企业有效期预报警数据表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for t_waste_category
-- ----------------------------
DROP TABLE IF EXISTS `t_waste_category`;
CREATE TABLE `t_waste_category` (
  `category_id` varchar(26) NOT NULL COMMENT '主键id',
  `ent_code` varchar(26) NOT NULL COMMENT '所属企业',
  `out_put_id` varchar(26) DEFAULT NULL COMMENT '关联排口',
  `waste_dict_id` varchar(20) NOT NULL COMMENT '固废分类id树',
  `waste_name` varchar(200) NOT NULL COMMENT '废物名称(具体名称)',
  `disposal_method` varchar(50) DEFAULT NULL COMMENT '处置/处理方法',
  `waste_form` varchar(50) DEFAULT NULL COMMENT '废物形态',
  `design_output` double DEFAULT NULL COMMENT '设计生产量(t/a)',
  `package_type` varchar(50) DEFAULT NULL COMMENT '容器/包装类型',
  `main_component` text COMMENT '主要成分',
  `hazardous_component` text COMMENT '有害成分',
  `hazard_characteristic` varchar(50) DEFAULT NULL COMMENT '危险特性',
  `precautions` text COMMENT '注意事项',
  `emergency_measures` text COMMENT '应急措施',
  `disposal_unit` varchar(26) DEFAULT NULL COMMENT '委托处置单位',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`category_id`),
  INDEX `idx_ent_code`(`ent_code`),
  INDEX `idx_out_put_id`(`out_put_id`),
  INDEX `idx_waste_dict_id`(`waste_dict_id`),
  INDEX `idx_disposal_method`(`disposal_method`)
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '固废种类管理表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for t_waste_dict
-- ----------------------------
DROP TABLE IF EXISTS `t_waste_dict`;
CREATE TABLE `t_waste_dict` (
  `id` bigint NOT NULL COMMENT '编号',
  `pid` bigint NOT NULL COMMENT '上级编号',
  `name` varchar(100) NOT DEFAULT '' COMMENT '名称',
  `code` varchar(50) DEFAULT NULL COMMENT '代码(HW08/SW17等)',
  `tag` varchar(50) DEFAULT NULL COMMENT '固废代码(900-214-08等)',
  `ext_name` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT '' COMMENT '扩展名/全称',
  PRIMARY KEY (`id`),
  INDEX `idx_pid`(`pid`)
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '固废分类字典表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for t_waste_flow_rel
-- ----------------------------
DROP TABLE IF EXISTS `t_waste_flow_rel`;
CREATE TABLE `t_waste_flow_rel` (
  `flow_type` tinyint NOT NULL COMMENT '流转类型：1-产生→入库 2-产生→减量 3-产生→出库 4-入库→出库',
  `source_id` varchar(26) NOT NULL COMMENT '源记录id',
  `target_id` varchar(26) NOT NULL COMMENT '目标记录id',
  `qty` double NOT NULL COMMENT '处理量(t)',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`flow_type`, `source_id`, `target_id`),
  INDEX `idx_target_id`(`flow_type`, `target_id`)
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '固废流转关联表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for t_waste_generate
-- ----------------------------
DROP TABLE IF EXISTS `t_waste_generate`;
CREATE TABLE `t_waste_generate` (
  `id` varchar(26) NOT NULL COMMENT '主键id',
  `category_id` varchar(26) NOT NULL COMMENT '固废种类id',
  `room_id` varchar(26) DEFAULT NULL COMMENT '暂存场所id',
  `gen_time` datetime NOT NULL COMMENT '产生时间',
  `gen_qty` double NOT NULL COMMENT '产生量(t)',
  `remain_qty` double NOT NULL COMMENT '剩余量(t)',
  `temp_operator` bigint DEFAULT NULL COMMENT '暂存经办人id(内部账户id)',
  `temp_operator_out_name` varchar(50) DEFAULT NULL COMMENT '暂存经办人(外部人员姓名)',
  `temp_operator_out_phone` varchar(20) DEFAULT NULL COMMENT '暂存经办人(外部人员手机号)',
  `tran_operator` bigint DEFAULT NULL COMMENT '运送经办人id(内部账户id)',
  `tran_operator_out_name` varchar(50) DEFAULT NULL COMMENT '运送经办人(外部人员姓名)',
  `tran_operator_out_phone` varchar(20) DEFAULT NULL COMMENT '运送经办人(外部人员手机号)',
  `day_seq` int NOT NULL COMMENT '当天序号（从1开始）',
  `batch_no` varchar(50) NOT NULL COMMENT '系统批次号',
  `manual_batch_no` varchar(50) DEFAULT NULL COMMENT '手工批号（用户手动输入，用于兼容外部系统）',
  `remark` text COMMENT '备注',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  INDEX `idx_category_id`(`category_id`),
  INDEX `idx_room_id`(`room_id`),
  INDEX `idx_gen_time`(`gen_time`),
  INDEX `idx_temp_operator`(`temp_operator`),
  INDEX `idx_tran_operator`(`tran_operator`)
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '固废产生记录表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for t_waste_outbound
-- ----------------------------
DROP TABLE IF EXISTS `t_waste_outbound`;
CREATE TABLE `t_waste_outbound` (
  `id` varchar(26) NOT NULL COMMENT '主键id',
  `out_type` tinyint DEFAULT NULL COMMENT '出库类型，1贮存出库，2立产立清',
  `category_id` varchar(26) NOT NULL COMMENT '固废种类id',
  `out_time` datetime NOT NULL COMMENT '出库时间',
  `out_qty` double NOT NULL COMMENT '出库量(t)',
  `out_operator` bigint DEFAULT NULL COMMENT '出库经办人id(内部账户id)',
  `out_operator_out_name` varchar(50) DEFAULT NULL COMMENT '出库经办人(外部人员姓名)',
  `out_operator_out_phone` varchar(20) DEFAULT NULL COMMENT '出库经办人(外部人员手机号)',
  `tran_operator` bigint DEFAULT NULL COMMENT '运送经办人id(内部账户id)',
  `tran_operator_out_name` varchar(50) DEFAULT NULL COMMENT '运送经办人(外部人员姓名)',
  `tran_operator_out_phone` varchar(20) DEFAULT NULL COMMENT '运送经办人(外部人员手机号)',
  `tran_unit` varchar(26) DEFAULT NULL COMMENT '运输单位（第三方单位）',
  `vehicle_no` varchar(50) DEFAULT NULL COMMENT '车牌号',
  `disposal_unit` varchar(26) DEFAULT NULL COMMENT '处置单位（第三方单位）',
  `day_seq` int NOT NULL COMMENT '当天序号（从1开始）',
  `batch_no` varchar(50) DEFAULT NULL COMMENT '系统批次号（自动生成）',
  `manual_batch_no` varchar(50) DEFAULT NULL COMMENT '手工批号（用户手动输入）',
  `remark` text COMMENT '备注',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  INDEX `idx_out_type`(`out_type`),
  INDEX `idx_category_id`(`category_id`),
  INDEX `idx_out_time`(`out_time`),
  INDEX `idx_out_operator`(`out_operator`),
  INDEX `idx_tran_operator`(`tran_operator`),
  INDEX `idx_tran_unit`(`tran_unit`),
  INDEX `idx_disposal_unit`(`disposal_unit`)
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '固废出库记录表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for t_waste_reduction
-- ----------------------------
DROP TABLE IF EXISTS `t_waste_reduction`;
CREATE TABLE `t_waste_reduction` (
  `id` varchar(26) NOT NULL COMMENT '主键id',
  `category_id` varchar(26) NOT NULL COMMENT '固废种类id',
  `room_id` varchar(26) DEFAULT NULL COMMENT '处理场所id',
  `reduction_time` datetime NOT NULL COMMENT '减量处理时间',
  `reduction_qty` double NOT NULL COMMENT '减量量(t)',
  `duration` int DEFAULT NULL COMMENT '运行时长(分钟)',
  `method` varchar(255) DEFAULT NULL COMMENT '减量方式',
  `operator` bigint DEFAULT NULL COMMENT '操作人员id(内部账户id)',
  `operator_out_name` varchar(50) DEFAULT NULL COMMENT '操作人员(外部人员姓名)',
  `operator_out_phone` varchar(20) DEFAULT NULL COMMENT '操作人员(外部人员手机号)',
  `day_seq` int NOT NULL COMMENT '当天序号（从1开始）',
  `batch_no` varchar(50) NOT NULL COMMENT '系统批次号',
  `manual_batch_no` varchar(50) DEFAULT NULL COMMENT '手工批号（用户手动输入）',
  `remark` text COMMENT '备注',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  INDEX `idx_category_id`(`category_id`),
  INDEX `idx_room_id`(`room_id`),
  INDEX `idx_reduction_time`(`reduction_time`)
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '固废减量记录表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for t_waste_room
-- ----------------------------
DROP TABLE IF EXISTS `t_waste_room`;
CREATE TABLE `t_waste_room` (
  `room_id` varchar(26) NOT NULL COMMENT '固废间主键id',
  `ent_code` varchar(26) NOT NULL COMMENT '归属企业',
  `out_put_id` varchar(26) DEFAULT NULL COMMENT '关联排口',
  `room_name` varchar(100) NOT NULL COMMENT '固/危废间名称',
  `room_code` varchar(50) DEFAULT NULL COMMENT '贮存间编码',
  `waste_type` varchar(50) DEFAULT NULL COMMENT '废物类型',
  `room_type` varchar(50) DEFAULT NULL COMMENT '贮存间类型',
  `max_capacity` double DEFAULT NULL COMMENT '最大存放容量(t)',
  `warn_limit` double DEFAULT NULL COMMENT '库存预警阈值(t)',
  `area` double DEFAULT NULL COMMENT '面积(㎡)',
  `has_camera` tinyint DEFAULT 0 COMMENT '是否安装摄像头（0否 1是）',
  `has_leak_proof` tinyint DEFAULT 0 COMMENT '是否防渗（0否 1是）',
  `has_ventilation` tinyint DEFAULT 0 COMMENT '是否通风（0否 1是）',
  `has_fire_control` tinyint DEFAULT 0 COMMENT '是否有消防设施（0否 1是）',
  `has_emergency_supplies` tinyint DEFAULT 0 COMMENT '是否有应急物资（0否 1是）',
  `longitude` double DEFAULT NULL COMMENT '经度',
  `latitude` double DEFAULT NULL COMMENT '纬度',
  `remark` text COMMENT '备注',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`room_id`),
  INDEX `idx_ent_code`(`ent_code`),
  INDEX `idx_out_put_id`(`out_put_id`),
  INDEX `idx_waste_type`(`waste_type`),
  INDEX `idx_room_type`(`room_type`)
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '固废间管理表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for t_waste_storage
-- ----------------------------
DROP TABLE IF EXISTS `t_waste_storage`;
CREATE TABLE `t_waste_storage` (
  `id` varchar(26) NOT NULL COMMENT '主键id',
  `category_id` varchar(26) NOT NULL COMMENT '固废种类id',
  `room_id` varchar(26) NOT NULL COMMENT '储存场所id',
  `storage_time` datetime NOT NULL COMMENT '入库时间',
  `storage_qty` double NOT NULL COMMENT '入库量(t)',
  `remain_qty` double NOT NULL COMMENT '剩余量(t)',
  `storage_operator` bigint DEFAULT NULL COMMENT '入库经办人id(内部账户id)',
  `storage_operator_out_name` varchar(50) DEFAULT NULL COMMENT '入库经办人(外部人员姓名)',
  `storage_operator_out_phone` varchar(20) DEFAULT NULL COMMENT '入库经办人(外部人员手机号)',
  `tran_operator` bigint DEFAULT NULL COMMENT '运送经办人id(内部账户id)',
  `tran_operator_out_name` varchar(50) DEFAULT NULL COMMENT '运送经办人(外部人员姓名)',
  `tran_operator_out_phone` varchar(20) DEFAULT NULL COMMENT '运送经办人(外部人员手机号)',
  `day_seq` int NOT NULL COMMENT '当天序号（从1开始）',
  `batch_no` varchar(50) DEFAULT NULL COMMENT '系统批次号（自动生成）',
  `manual_batch_no` varchar(50) DEFAULT NULL COMMENT '手工批号（用户手动输入）',
  `remark` text COMMENT '备注',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  INDEX `idx_category_id`(`category_id`),
  INDEX `idx_room_id`(`room_id`),
  INDEX `idx_storage_time`(`storage_time`),
  INDEX `idx_storage_operator`(`storage_operator`),
  INDEX `idx_tran_operator`(`tran_operator`)
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '固废入库记录表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for t_waste_total_plan
-- ----------------------------
DROP TABLE IF EXISTS `t_waste_total_plan`;
CREATE TABLE `t_waste_total_plan` (
  `waste_dict_id` varchar(20) NOT NULL COMMENT '固废分类id树',
  `ent_code` varchar(26) NOT NULL COMMENT '所属企业',
  `out_put_id` varchar(26) NOT NULL COMMENT '关联排口',
  `year` int NOT NULL COMMENT '年份',
  `annual_limit` double DEFAULT NULL COMMENT '年度总量上限(t)',
  `first_limit` double DEFAULT NULL COMMENT '第一季度上限(t)',
  `second_limit` double DEFAULT NULL COMMENT '第二季度上限(t)',
  `third_limit` double DEFAULT NULL COMMENT '第三季度上限(t)',
  `fourth_limit` double DEFAULT NULL COMMENT '第四季度上限(t)',
  `warn_val` double DEFAULT NULL COMMENT '预警阈值(%)',
  `alarm_val` double DEFAULT NULL COMMENT '告警阈值(%)',
  `remark` text COMMENT '备注',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`waste_dict_id`, `ent_code`, `out_put_id`, `year`),
  INDEX `idx_ent_code`(`ent_code`),
  INDEX `idx_year`(`year`)
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '固废总量控制计划表' ROW_FORMAT = Dynamic;

SET FOREIGN_KEY_CHECKS = 1;
