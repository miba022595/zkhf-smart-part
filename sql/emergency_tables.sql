-- 应急物资表
CREATE TABLE IF NOT EXISTS `t_emergency_material` (
  `material_id` varchar(50) NOT NULL COMMENT '物资ID',
  `ent_code` varchar(50) DEFAULT NULL COMMENT '企业编码',
  `material_name` varchar(200) NOT NULL COMMENT '物资名称',
  `model_spec` varchar(200) DEFAULT NULL COMMENT '型号规格',
  `store_place` varchar(200) DEFAULT NULL COMMENT '存放地点',
  `quantity` int(11) DEFAULT NULL COMMENT '数量',
  `unit` varchar(50) DEFAULT NULL COMMENT '单位',
  `expire_date` date DEFAULT NULL COMMENT '保质期',
  `recheck_date` date DEFAULT NULL COMMENT '复检日期',
  `manufacturer_name` varchar(200) DEFAULT NULL COMMENT '生产厂家',
  `manufacturer_contact` varchar(100) DEFAULT NULL COMMENT '厂家联系人',
  `manufacturer_phone` varchar(20) NOT NULL COMMENT '厂家电话',
  `manager_name` varchar(100) DEFAULT NULL COMMENT '管理员',
  `manager_phone` varchar(20) NOT NULL COMMENT '管理员电话',
  `remark` varchar(500) DEFAULT NULL COMMENT '备注',
  `warn_status` int(11) DEFAULT 0 COMMENT '预警状态：0-正常，1-即将到期，2-已过期',
  `warn_days` int(11) DEFAULT 30 COMMENT '预警天数',
  `warn_type` varchar(20) DEFAULT NULL COMMENT '预警类型：expire-保质期，recheck-复检期',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`material_id`),
  KEY `idx_ent_code` (`ent_code`),
  KEY `idx_material_name` (`material_name`),
  KEY `idx_warn_status` (`warn_status`),
  KEY `idx_expire_date` (`expire_date`),
  KEY `idx_recheck_date` (`recheck_date`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='应急物资表';

-- 应急车辆表
CREATE TABLE IF NOT EXISTS `t_emergency_vehicle` (
  `vehicle_id` varchar(50) NOT NULL COMMENT '车辆ID',
  `ent_code` varchar(50) DEFAULT NULL COMMENT '企业编码',
  `plate_no` varchar(50) NOT NULL COMMENT '车牌号',
  `vehicle_type` varchar(50) DEFAULT NULL COMMENT '车辆类型',
  `driver_name` varchar(100) DEFAULT NULL COMMENT '驾驶员姓名',
  `driver_phone` varchar(20) NOT NULL COMMENT '驾驶员电话',
  `park_place` varchar(200) DEFAULT NULL COMMENT '停放地点',
  `vehicle_status` varchar(50) DEFAULT NULL COMMENT '车辆状态',
  `remark` varchar(500) DEFAULT NULL COMMENT '备注',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`vehicle_id`),
  KEY `idx_ent_code` (`ent_code`),
  KEY `idx_plate_no` (`plate_no`),
  KEY `idx_vehicle_status` (`vehicle_status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='应急车辆表';

-- 应急专家表
CREATE TABLE IF NOT EXISTS `t_emergency_expert` (
  `expert_id` varchar(50) NOT NULL COMMENT '专家ID',
  `ent_code` varchar(50) DEFAULT NULL COMMENT '企业编码',
  `expert_name` varchar(100) NOT NULL COMMENT '专家姓名',
  `work_unit` varchar(200) DEFAULT NULL COMMENT '工作单位',
  `position_title` varchar(100) DEFAULT NULL COMMENT '职务职称',
  `specialty` varchar(200) DEFAULT NULL COMMENT '擅长方向',
  `phone` varchar(20) NOT NULL COMMENT '联系电话',
  `email` varchar(100) DEFAULT NULL COMMENT '联系邮箱',
  `remark` varchar(500) DEFAULT NULL COMMENT '备注',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`expert_id`),
  KEY `idx_ent_code` (`ent_code`),
  KEY `idx_expert_name` (`expert_name`),
  KEY `idx_specialty` (`specialty`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='应急专家表';

-- 应急预案表
CREATE TABLE IF NOT EXISTS `t_emergency_plan` (
  `plan_id` varchar(50) NOT NULL COMMENT '预案ID',
  `ent_code` varchar(50) DEFAULT NULL COMMENT '企业编码',
  `plan_name` varchar(200) NOT NULL COMMENT '预案名称',
  `version` varchar(50) DEFAULT NULL COMMENT '版本号',
  `risk_unit` varchar(500) DEFAULT NULL COMMENT '风险单元',
  `handle_points` text DEFAULT NULL COMMENT '处置要点',
  `plan_type` int(11) DEFAULT 1 COMMENT '预案类型：1-附件管理，2-结构化',
  `remark` varchar(500) DEFAULT NULL COMMENT '备注',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`plan_id`),
  KEY `idx_ent_code` (`ent_code`),
  KEY `idx_plan_name` (`plan_name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='应急预案表';

-- 应急演练表
CREATE TABLE IF NOT EXISTS `t_emergency_drill` (
  `drill_id` varchar(50) NOT NULL COMMENT '演练ID',
  `ent_code` varchar(50) DEFAULT NULL COMMENT '企业编码',
  `drill_name` varchar(200) NOT NULL COMMENT '演练名称',
  `drill_type` varchar(50) DEFAULT NULL COMMENT '演练类型',
  `drill_date` date DEFAULT NULL COMMENT '演练时间',
  `related_plan` varchar(200) DEFAULT NULL COMMENT '涉及预案',
  `drill_content` text DEFAULT NULL COMMENT '演练内容',
  `drill_summary` text DEFAULT NULL COMMENT '演练总结',
  `drill_status` int(11) DEFAULT 1 COMMENT '演练状态：1-计划中，2-已完成',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`drill_id`),
  KEY `idx_ent_code` (`ent_code`),
  KEY `idx_drill_name` (`drill_name`),
  KEY `idx_drill_type` (`drill_type`),
  KEY `idx_drill_date` (`drill_date`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='应急演练表';

-- 应急通知表
CREATE TABLE IF NOT EXISTS `t_emergency_notice` (
  `notice_id` varchar(50) NOT NULL COMMENT '通知ID',
  `ent_code` varchar(50) DEFAULT NULL COMMENT '企业编码',
  `notice_title` varchar(200) NOT NULL COMMENT '通知标题',
  `notice_content` text DEFAULT NULL COMMENT '通知内容',
  `event_location` varchar(200) DEFAULT NULL COMMENT '事件地点',
  `event_time` datetime DEFAULT NULL COMMENT '事件时间',
  `receiver_names` text DEFAULT NULL COMMENT '接收人姓名列表',
  `send_status` varchar(20) DEFAULT 'success' COMMENT '发送状态：success-成功，failed-失败',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`notice_id`),
  KEY `idx_ent_code` (`ent_code`),
  KEY `idx_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='应急通知表';

-- 应急通讯录表
CREATE TABLE IF NOT EXISTS `t_emergency_contact` (
  `contact_id` varchar(50) NOT NULL COMMENT '联系人ID',
  `ent_code` varchar(50) DEFAULT NULL COMMENT '企业编码',
  `contact_name` varchar(100) NOT NULL COMMENT '姓名',
  `position` varchar(100) DEFAULT NULL COMMENT '职务',
  `dept_group` varchar(100) DEFAULT NULL COMMENT '所属部门/小组',
  `phone` varchar(20) NOT NULL COMMENT '手机号',
  `specialty` varchar(500) DEFAULT NULL COMMENT '擅长方向/职责描述',
  `remark` varchar(500) DEFAULT NULL COMMENT '备注',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`contact_id`),
  KEY `idx_ent_code` (`ent_code`),
  KEY `idx_contact_name` (`contact_name`),
  KEY `idx_dept_group` (`dept_group`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='应急通讯录表';