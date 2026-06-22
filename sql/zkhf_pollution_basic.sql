SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- Table structure for gen_table
-- ----------------------------
DROP TABLE IF EXISTS `gen_table`;
CREATE TABLE `gen_table` (
  `table_id` bigint NOT NULL AUTO_INCREMENT COMMENT '编号',
  `table_name` varchar(200) DEFAULT '' COMMENT '表名称',
  `table_comment` varchar(500) DEFAULT '' COMMENT '表描述',
  `sub_table_name` varchar(64) DEFAULT NULL COMMENT '关联子表的表名',
  `sub_table_fk_name` varchar(64) DEFAULT NULL COMMENT '子表关联的外键名',
  `class_name` varchar(100) DEFAULT '' COMMENT '实体类名称',
  `tpl_category` varchar(200) DEFAULT 'crud' COMMENT '使用的模板（crud单表操作 tree树表操作）',
  `tpl_web_type` varchar(30) DEFAULT '' COMMENT '前端模板类型（element-ui模板 element-plus模板）',
  `package_name` varchar(100) DEFAULT NULL COMMENT '生成包路径',
  `module_name` varchar(30) DEFAULT NULL COMMENT '生成模块名',
  `business_name` varchar(30) DEFAULT NULL COMMENT '生成业务名',
  `function_name` varchar(50) DEFAULT NULL COMMENT '生成功能名',
  `function_author` varchar(50) DEFAULT NULL COMMENT '生成功能作者',
  `gen_type` char(1) DEFAULT '0' COMMENT '生成代码方式（0zip压缩包 1自定义路径）',
  `gen_path` varchar(200) DEFAULT '/' COMMENT '生成路径（不填默认项目路径）',
  `options` varchar(1000) DEFAULT NULL COMMENT '其它生成选项',
  `create_by` varchar(64) DEFAULT '' COMMENT '创建者',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  `update_by` varchar(64) DEFAULT '' COMMENT '更新者',
  `update_time` datetime DEFAULT NULL COMMENT '更新时间',
  `remark` varchar(500) DEFAULT NULL COMMENT '备注',
  PRIMARY KEY (`table_id`)
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '代码生成业务表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for gen_table_column
-- ----------------------------
DROP TABLE IF EXISTS `gen_table_column`;
CREATE TABLE `gen_table_column` (
  `column_id` bigint NOT NULL AUTO_INCREMENT COMMENT '编号',
  `table_id` bigint DEFAULT NULL COMMENT '归属表编号',
  `column_name` varchar(200) DEFAULT NULL COMMENT '列名称',
  `column_comment` varchar(500) DEFAULT NULL COMMENT '列描述',
  `column_type` varchar(100) DEFAULT NULL COMMENT '列类型',
  `java_type` varchar(500) DEFAULT NULL COMMENT 'JAVA类型',
  `java_field` varchar(200) DEFAULT NULL COMMENT 'JAVA字段名',
  `is_pk` char(1) DEFAULT NULL COMMENT '是否主键（1是）',
  `is_increment` char(1) DEFAULT NULL COMMENT '是否自增（1是）',
  `is_required` char(1) DEFAULT NULL COMMENT '是否必填（1是）',
  `is_insert` char(1) DEFAULT NULL COMMENT '是否为插入字段（1是）',
  `is_edit` char(1) DEFAULT NULL COMMENT '是否编辑字段（1是）',
  `is_list` char(1) DEFAULT NULL COMMENT '是否列表字段（1是）',
  `is_query` char(1) DEFAULT NULL COMMENT '是否查询字段（1是）',
  `query_type` varchar(200) DEFAULT 'EQ' COMMENT '查询方式（等于、不等于、大于、小于、范围）',
  `html_type` varchar(200) DEFAULT NULL COMMENT '显示类型（文本框、文本域、下拉框、复选框、单选框、日期控件）',
  `dict_type` varchar(200) DEFAULT '' COMMENT '字典类型',
  `sort` int DEFAULT NULL COMMENT '排序',
  `create_by` varchar(64) DEFAULT '' COMMENT '创建者',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  `update_by` varchar(64) DEFAULT '' COMMENT '更新者',
  `update_time` datetime DEFAULT NULL COMMENT '更新时间',
  PRIMARY KEY (`column_id`)
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '代码生成业务表字段' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for sys_config
-- ----------------------------
DROP TABLE IF EXISTS `sys_config`;
CREATE TABLE `sys_config` (
  `config_id` int NOT NULL AUTO_INCREMENT COMMENT '参数主键',
  `config_name` varchar(100) DEFAULT '' COMMENT '参数名称',
  `config_key` varchar(100) DEFAULT '' COMMENT '参数键名',
  `config_value` varchar(500) DEFAULT '' COMMENT '参数键值',
  `config_type` char(1) DEFAULT 'N' COMMENT '系统内置（Y是 N否）',
  `create_by` varchar(64) DEFAULT '' COMMENT '创建者',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  `update_by` varchar(64) DEFAULT '' COMMENT '更新者',
  `update_time` datetime DEFAULT NULL COMMENT '更新时间',
  `remark` varchar(500) DEFAULT NULL COMMENT '备注',
  PRIMARY KEY (`config_id`)
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '参数配置表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for sys_dict_custom_data
-- ----------------------------
DROP TABLE IF EXISTS `sys_dict_custom_data`;
CREATE TABLE `sys_dict_custom_data` (
  `dict_code` bigint NOT NULL AUTO_INCREMENT COMMENT '字典编码',
  `user_id` bigint NOT NULL COMMENT '用户ID',
  `dict_sort` int DEFAULT 0 COMMENT '字典排序',
  `dict_label` varchar(100) DEFAULT '' COMMENT '字典标签',
  `dict_value` varchar(100) DEFAULT '' COMMENT '字典键值',
  `dict_type` varchar(100) DEFAULT '' COMMENT '字典类型',
  `css_class` varchar(100) DEFAULT NULL COMMENT '样式属性（其他样式扩展）',
  `list_class` varchar(100) DEFAULT NULL COMMENT '表格回显样式',
  `is_default` char(1) DEFAULT 'N' COMMENT '是否默认（Y是 N否）',
  `status` char(1) DEFAULT '0' COMMENT '状态（0正常 1停用）',
  `create_by` varchar(64) DEFAULT '' COMMENT '创建者',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  `update_by` varchar(64) DEFAULT '' COMMENT '更新者',
  `update_time` datetime DEFAULT NULL COMMENT '更新时间',
  `remark` varchar(500) DEFAULT NULL COMMENT '备注',
  PRIMARY KEY (`dict_code`, `user_id`)
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '字典自定义数据表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for sys_dict_data
-- ----------------------------
DROP TABLE IF EXISTS `sys_dict_data`;
CREATE TABLE `sys_dict_data` (
  `dict_code` bigint NOT NULL AUTO_INCREMENT COMMENT '字典编码',
  `dict_sort` int DEFAULT 0 COMMENT '字典排序',
  `dict_label` varchar(100) DEFAULT '' COMMENT '字典标签',
  `dict_value` varchar(100) DEFAULT '' COMMENT '字典键值',
  `dict_type` varchar(100) DEFAULT '' COMMENT '字典类型',
  `css_class` varchar(100) DEFAULT NULL COMMENT '样式属性（其他样式扩展）',
  `list_class` varchar(100) DEFAULT NULL COMMENT '表格回显样式',
  `is_default` char(1) DEFAULT 'N' COMMENT '是否默认（Y是 N否）',
  `status` char(1) DEFAULT '0' COMMENT '状态（0正常 1停用）',
  `create_by` varchar(64) DEFAULT '' COMMENT '创建者',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  `update_by` varchar(64) DEFAULT '' COMMENT '更新者',
  `update_time` datetime DEFAULT NULL COMMENT '更新时间',
  `remark` varchar(500) DEFAULT NULL COMMENT '备注',
  PRIMARY KEY (`dict_code`)
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '字典数据表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for sys_dict_type
-- ----------------------------
DROP TABLE IF EXISTS `sys_dict_type`;
CREATE TABLE `sys_dict_type` (
  `dict_id` bigint NOT NULL AUTO_INCREMENT COMMENT '字典主键',
  `dict_name` varchar(100) DEFAULT '' COMMENT '字典名称',
  `dict_type` varchar(100) DEFAULT '' COMMENT '字典类型',
  `status` char(1) DEFAULT '0' COMMENT '状态（0正常 1停用）',
  `create_by` varchar(64) DEFAULT '' COMMENT '创建者',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  `update_by` varchar(64) DEFAULT '' COMMENT '更新者',
  `update_time` datetime DEFAULT NULL COMMENT '更新时间',
  `remark` text COMMENT '备注',
  PRIMARY KEY (`dict_id`),
  UNIQUE INDEX `dict_type`(`dict_type`)
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '字典类型表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for sys_logininfor
-- ----------------------------
DROP TABLE IF EXISTS `sys_logininfor`;
CREATE TABLE `sys_logininfor` (
  `info_id` bigint NOT NULL AUTO_INCREMENT COMMENT '访问ID',
  `user_name` varchar(50) DEFAULT '' COMMENT '用户账号',
  `ipaddr` varchar(128) DEFAULT '' COMMENT '登录IP地址',
  `login_location` varchar(255) DEFAULT '' COMMENT '登录地点',
  `browser` varchar(50) DEFAULT '' COMMENT '浏览器类型',
  `os` varchar(50) DEFAULT '' COMMENT '操作系统',
  `status` char(1) DEFAULT '0' COMMENT '登录状态（0成功 1失败）',
  `msg` varchar(255) DEFAULT '' COMMENT '提示消息',
  `login_time` datetime DEFAULT NULL COMMENT '访问时间',
  PRIMARY KEY (`info_id`),
  INDEX `idx_sys_logininfor_s`(`status`),
  INDEX `idx_sys_logininfor_lt`(`login_time`)
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '系统访问记录' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for sys_menu
-- ----------------------------
DROP TABLE IF EXISTS `sys_menu`;
CREATE TABLE `sys_menu` (
  `menu_id` bigint NOT NULL AUTO_INCREMENT COMMENT '菜单ID',
  `menu_name` varchar(50) NOT NULL COMMENT '菜单名称',
  `parent_id` bigint DEFAULT 0 COMMENT '父菜单ID',
  `order_num` int DEFAULT 0 COMMENT '显示顺序',
  `path` varchar(200) DEFAULT '' COMMENT '路由地址',
  `component` varchar(255) DEFAULT NULL COMMENT '组件路径',
  `query` varchar(255) DEFAULT NULL COMMENT '路由参数',
  `is_frame` int DEFAULT 1 COMMENT '是否为外链（0是 1否）',
  `is_cache` int DEFAULT 0 COMMENT '是否缓存（0缓存 1不缓存）',
  `menu_type` char(1) DEFAULT '' COMMENT '菜单类型（M目录 C菜单 F按钮）',
  `visible` char(1) DEFAULT '0' COMMENT '菜单状态（0显示 1隐藏）',
  `status` char(1) DEFAULT '0' COMMENT '菜单状态（0正常 1停用）',
  `perms` varchar(100) DEFAULT NULL COMMENT '权限标识',
  `icon` varchar(100) DEFAULT '#' COMMENT '菜单图标',
  `create_by` varchar(64) DEFAULT '' COMMENT '创建者',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  `update_by` varchar(64) DEFAULT '' COMMENT '更新者',
  `update_time` datetime DEFAULT NULL COMMENT '更新时间',
  `remark` varchar(500) DEFAULT '' COMMENT '备注',
  PRIMARY KEY (`menu_id`)
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '菜单权限表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for sys_notice
-- ----------------------------
DROP TABLE IF EXISTS `sys_notice`;
CREATE TABLE `sys_notice` (
  `notice_id` int NOT NULL AUTO_INCREMENT COMMENT '公告ID',
  `notice_title` varchar(50) NOT NULL COMMENT '公告标题',
  `notice_type` char(1) NOT NULL COMMENT '公告类型（1通知 2公告）',
  `notice_content` longblob NULL COMMENT '公告内容',
  `status` char(1) DEFAULT '0' COMMENT '公告状态（0正常 1关闭）',
  `create_by` varchar(64) DEFAULT '' COMMENT '创建者',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  `update_by` varchar(64) DEFAULT '' COMMENT '更新者',
  `update_time` datetime DEFAULT NULL COMMENT '更新时间',
  `remark` varchar(255) DEFAULT NULL COMMENT '备注',
  PRIMARY KEY (`notice_id`)
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '通知公告表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for sys_oper_log
-- ----------------------------
DROP TABLE IF EXISTS `sys_oper_log`;
CREATE TABLE `sys_oper_log` (
  `oper_id` bigint NOT NULL AUTO_INCREMENT COMMENT '日志主键',
  `title` varchar(50) DEFAULT '' COMMENT '模块标题',
  `business_type` int DEFAULT 0 COMMENT '业务类型（0其它 1新增 2修改 3删除）',
  `method` varchar(100) DEFAULT '' COMMENT '方法名称',
  `request_method` varchar(10) DEFAULT '' COMMENT '请求方式',
  `operator_type` int DEFAULT 0 COMMENT '操作类别（0其它 1后台用户 2手机端用户）',
  `oper_name` varchar(50) DEFAULT '' COMMENT '操作人员',
  `dept_name` varchar(50) DEFAULT '' COMMENT '部门名称',
  `oper_url` varchar(255) DEFAULT '' COMMENT '请求URL',
  `oper_ip` varchar(128) DEFAULT '' COMMENT '主机地址',
  `oper_location` varchar(255) DEFAULT '' COMMENT '操作地点',
  `oper_param` varchar(2000) DEFAULT '' COMMENT '请求参数',
  `json_result` varchar(2000) DEFAULT '' COMMENT '返回参数',
  `status` int DEFAULT 0 COMMENT '操作状态（0正常 1异常）',
  `error_msg` varchar(2000) DEFAULT '' COMMENT '错误消息',
  `oper_time` datetime DEFAULT NULL COMMENT '操作时间',
  `cost_time` bigint DEFAULT 0 COMMENT '消耗时间',
  PRIMARY KEY (`oper_id`),
  INDEX `idx_sys_oper_log_bt`(`business_type`),
  INDEX `idx_sys_oper_log_s`(`status`),
  INDEX `idx_sys_oper_log_ot`(`oper_time`)
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '操作日志记录' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for sys_post
-- ----------------------------
DROP TABLE IF EXISTS `sys_post`;
CREATE TABLE `sys_post` (
  `post_id` bigint NOT NULL AUTO_INCREMENT COMMENT '岗位ID',
  `post_code` varchar(64) NOT NULL COMMENT '岗位编码',
  `post_name` varchar(50) NOT NULL COMMENT '岗位名称',
  `post_sort` int NOT NULL COMMENT '显示顺序',
  `status` char(1) NOT NULL COMMENT '状态（0正常 1停用）',
  `create_by` varchar(64) DEFAULT '' COMMENT '创建者',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  `update_by` varchar(64) DEFAULT '' COMMENT '更新者',
  `update_time` datetime DEFAULT NULL COMMENT '更新时间',
  `remark` varchar(500) DEFAULT NULL COMMENT '备注',
  PRIMARY KEY (`post_id`)
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '岗位信息表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for sys_role
-- ----------------------------
DROP TABLE IF EXISTS `sys_role`;
CREATE TABLE `sys_role` (
  `role_id` bigint NOT NULL AUTO_INCREMENT COMMENT '角色ID',
  `role_name` varchar(30) NOT NULL COMMENT '角色名称',
  `role_key` varchar(100) NOT NULL COMMENT '角色权限字符串',
  `role_sort` int NOT NULL COMMENT '显示顺序',
  `data_scope` char(1) DEFAULT '1' COMMENT '数据范围（1：全部数据权限 2：自定数据权限 3：本部门数据权限 4：本部门及以下数据权限）',
  `menu_check_strictly` tinyint(1) DEFAULT 1 COMMENT '菜单树选择项是否关联显示',
  `dept_check_strictly` tinyint(1) DEFAULT 1 COMMENT '部门树选择项是否关联显示',
  `status` char(1) NOT NULL COMMENT '角色状态（0正常 1停用）',
  `del_flag` char(1) DEFAULT '0' COMMENT '删除标志（0代表存在 2代表删除）',
  `create_by` varchar(64) DEFAULT '' COMMENT '创建者',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  `update_by` varchar(64) DEFAULT '' COMMENT '更新者',
  `update_time` datetime DEFAULT NULL COMMENT '更新时间',
  `remark` varchar(500) DEFAULT NULL COMMENT '备注',
  PRIMARY KEY (`role_id`)
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '角色信息表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for sys_role_menu
-- ----------------------------
DROP TABLE IF EXISTS `sys_role_menu`;
CREATE TABLE `sys_role_menu` (
  `role_id` bigint NOT NULL COMMENT '角色ID',
  `menu_id` bigint NOT NULL COMMENT '菜单ID',
  PRIMARY KEY (`role_id`, `menu_id`)
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '角色和菜单关联表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for sys_user
-- ----------------------------
DROP TABLE IF EXISTS `sys_user`;
CREATE TABLE `sys_user` (
  `user_id` bigint NOT NULL AUTO_INCREMENT COMMENT '用户ID',
  `dept_id` bigint DEFAULT NULL COMMENT '部门ID',
  `user_name` varchar(30) NOT NULL COMMENT '用户账号',
  `nick_name` varchar(30) NOT NULL COMMENT '用户昵称',
  `user_type` varchar(2) DEFAULT '00' COMMENT '用户类型（00系统用户）',
  `email` varchar(50) DEFAULT '' COMMENT '用户邮箱',
  `phonenumber` varchar(11) DEFAULT '' COMMENT '手机号码',
  `sex` char(1) DEFAULT '0' COMMENT '用户性别（0男 1女 2未知）',
  `avatar` varchar(100) DEFAULT '' COMMENT '头像地址',
  `password` varchar(100) DEFAULT '' COMMENT '密码',
  `status` char(1) DEFAULT '0' COMMENT '帐号状态（0正常 1停用）',
  `del_flag` char(1) DEFAULT '0' COMMENT '删除标志（0代表存在 2代表删除）',
  `login_ip` varchar(128) DEFAULT '' COMMENT '最后登录IP',
  `login_date` datetime DEFAULT NULL COMMENT '最后登录时间',
  `create_by` varchar(64) DEFAULT '' COMMENT '创建者',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  `update_by` varchar(64) DEFAULT '' COMMENT '更新者',
  `update_time` datetime DEFAULT NULL COMMENT '更新时间',
  `remark` varchar(500) DEFAULT NULL COMMENT '备注',
  `ent_code` varchar(40) DEFAULT NULL COMMENT '企业编码',
  `ent_name` varchar(200) DEFAULT NULL COMMENT '企业名称',
  `social_credit_code` varchar(50) DEFAULT NULL COMMENT '社会统一信用代码',
  PRIMARY KEY (`user_id`)
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '用户信息表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for sys_user_ent
-- ----------------------------
DROP TABLE IF EXISTS `sys_user_ent`;
CREATE TABLE `sys_user_ent` (
  `user_id` bigint NOT NULL COMMENT '用户ID',
  `ent_code` varchar(26) NOT NULL COMMENT '企业编码',
  PRIMARY KEY (`user_id`, `ent_code`)
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '用户和企业关联表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for sys_user_post
-- ----------------------------
DROP TABLE IF EXISTS `sys_user_post`;
CREATE TABLE `sys_user_post` (
  `user_id` bigint NOT NULL COMMENT '用户ID',
  `post_id` bigint NOT NULL COMMENT '岗位ID',
  PRIMARY KEY (`user_id`, `post_id`)
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '用户与岗位关联表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for sys_user_role
-- ----------------------------
DROP TABLE IF EXISTS `sys_user_role`;
CREATE TABLE `sys_user_role` (
  `user_id` bigint NOT NULL COMMENT '用户ID',
  `role_id` bigint NOT NULL COMMENT '角色ID',
  PRIMARY KEY (`user_id`, `role_id`)
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '用户和角色关联表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for t_bas_approval_flow
-- ----------------------------
DROP TABLE IF EXISTS `t_bas_approval_flow`;
CREATE TABLE `t_bas_approval_flow` (
  `flow_id` varchar(26) NOT NULL COMMENT '流程主键ID',
  `ent_code` varchar(26) NOT NULL COMMENT '关联企业id',
  `flow_name` varchar(255) NOT NULL COMMENT '流程名称',
  `business_type` varchar(30) DEFAULT NULL COMMENT '业务类型：work_order-工单，purchase_order-采购单，expense-费用报销等',
  `start_node_id` varchar(26) DEFAULT NULL COMMENT '开始节点ID，指向流程的第一个审批节点',
  `version` varchar(20) DEFAULT '1.0' COMMENT '流程版本号，用于版本控制',
  `active` tinyint(1) DEFAULT 1 COMMENT '是否启用：0-停用，1-启用',
  `default_flow` tinyint(1) DEFAULT 0 COMMENT '是否默认审批流：0-非默认，1-默认',
  `description` text COMMENT '流程描述',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`flow_id`),
  INDEX `idx_ent_business`(`ent_code`, `business_type`),
  INDEX `idx_business_type`(`business_type`),
  INDEX `idx_active`(`active`)
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '审批流程定义表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for t_bas_approval_history
-- ----------------------------
DROP TABLE IF EXISTS `t_bas_approval_history`;
CREATE TABLE `t_bas_approval_history` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `business_type` varchar(50) NOT NULL COMMENT '业务类型：与流程定义中的业务类型对应',
  `business_key` varchar(30) NOT NULL COMMENT '业务主键：关联业务数据的唯一标识',
  `action_type` varchar(10) NOT NULL COMMENT '操作类型：START-发起审批，APPROVE-同意，REJECT-拒绝，CANCEL-取消',
  `action_user` bigint NOT NULL COMMENT '操作人：执行操作的用户ID',
  `action_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '操作时间',
  `comment` text COMMENT '操作说明：审批意见',
  `node_id` varchar(26) DEFAULT NULL COMMENT '节点ID：操作发生时所在的节点ID',
  `ip_address` varchar(50) DEFAULT NULL COMMENT 'IP地址：操作时的客户端IP地址',
  `action_location` varchar(255) DEFAULT NULL COMMENT '操作时的地点',
  `browser` varchar(255) DEFAULT NULL COMMENT '浏览器类型',
  `os` varchar(255) DEFAULT NULL COMMENT '操作系统',
  PRIMARY KEY (`id`),
  INDEX `idx_instance`(`business_type`, `business_key`),
  INDEX `idx_action_user`(`action_user`),
  INDEX `idx_action_time`(`action_time`),
  INDEX `idx_action_type`(`action_type`),
  INDEX `idx_node_id`(`node_id`)
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '审批历史表：存储审批过程中的所有操作记录' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for t_bas_approval_instance
-- ----------------------------
DROP TABLE IF EXISTS `t_bas_approval_instance`;
CREATE TABLE `t_bas_approval_instance` (
  `business_type` varchar(50) NOT NULL COMMENT '业务类型：与流程定义中的业务类型对应',
  `business_key` varchar(30) NOT NULL COMMENT '业务主键：关联业务数据的唯一标识',
  `form_data` json NULL COMMENT '表单数据：审批过程中填写的表单数据',
  `flow_id` varchar(26) NOT NULL COMMENT '流程ID，关联t_bas_approval_flow.flow_id',
  `status` varchar(20) NOT NULL COMMENT '审批状态：PROCESSING-审批中，APPROVED-已通过，REJECTED-已拒绝，CANCELLED-已取消',
  `current_node_id` varchar(26) DEFAULT NULL COMMENT '当前节点ID：当前正在审批的节点ID',
  `current_assignee` bigint DEFAULT NULL COMMENT '当前审批人：当前需要审批的用户ID',
  `initiator` bigint NOT NULL COMMENT '发起人：流程发起人的用户ID',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`business_type`, `business_key`),
  INDEX `idx_status`(`status`),
  INDEX `idx_current_assignee`(`current_assignee`),
  INDEX `idx_initiator`(`initiator`),
  INDEX `idx_flow_id`(`flow_id`)
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '审批实例表：存储具体的审批实例信息' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for t_bas_approval_message
-- ----------------------------
DROP TABLE IF EXISTS `t_bas_approval_message`;
CREATE TABLE `t_bas_approval_message` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `history_id` bigint NOT NULL COMMENT '关联审批历史ID',
  `push_user` bigint NOT NULL COMMENT '推送目标用户ID',
  `message_content` text COMMENT '消息内容',
  `push_status` tinyint NOT NULL DEFAULT 0 COMMENT '推送状态：0-未推送，1-已推送，2-推送失败，3-已读',
  `push_time` datetime DEFAULT NULL COMMENT '推送时间',
  `read_time` datetime DEFAULT NULL COMMENT '读取时间',
  `max_retries` tinyint DEFAULT 3 COMMENT '最大重试次数',
  `retry_interval` int DEFAULT 300 COMMENT '重试间隔(秒)，默认5分钟',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  INDEX `idx_history_id`(`history_id`),
  INDEX `idx_push_user`(`push_user`, `push_status`),
  INDEX `idx_push_status`(`push_status`),
  INDEX `idx_push_time`(`push_time`)
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '审批消息推送表：存储需要推送的消息记录，支持多人推送' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for t_bas_approval_node
-- ----------------------------
DROP TABLE IF EXISTS `t_bas_approval_node`;
CREATE TABLE `t_bas_approval_node` (
  `node_id` varchar(26) NOT NULL COMMENT '节点主键ID',
  `flow_id` varchar(26) NOT NULL COMMENT '流程ID，关联t_bas_approval_flow.flow_id',
  `node_name` varchar(255) NOT NULL COMMENT '节点显示名称',
  `assignee_user` bigint DEFAULT NULL COMMENT '审批人ID',
  `next_node_id` varchar(26) DEFAULT NULL COMMENT '下一节点ID，为空表示流程结束',
  `timeout_hours` int DEFAULT NULL COMMENT '超时时间(小时)，超时提醒',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`node_id`),
  INDEX `idx_next_node`(`flow_id`, `next_node_id`)
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '审批节点表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for t_bas_districts
-- ----------------------------
DROP TABLE IF EXISTS `t_bas_districts`;
CREATE TABLE `t_bas_districts` (
  `id` bigint NOT NULL COMMENT '编号',
  `pid` bigint NOT NULL COMMENT '上级编号',
  `name` varchar(32) NOT NULL DEFAULT '' COMMENT '名称',
  `pinyin` varchar(64) DEFAULT NULL COMMENT '拼音',
  `ext_name` varchar(64) DEFAULT '' COMMENT '扩展名',
  PRIMARY KEY (`id`)
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '地区' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for t_bas_effective_trans
-- ----------------------------
DROP TABLE IF EXISTS `t_bas_effective_trans`;
CREATE TABLE `t_bas_effective_trans` (
  `id` int NOT NULL AUTO_INCREMENT COMMENT '主键id',
  `ent_code` varchar(40) DEFAULT NULL COMMENT '企业编码',
  `pollution_type` tinyint DEFAULT NULL COMMENT '污染物类型，1废水；2废气',
  `stat_type` tinyint DEFAULT NULL COMMENT '统计类型：1传输率、2有效率',
  `group_type` varchar(50) DEFAULT NULL COMMENT '有效率时使用，字段分组',
  `pollutant_code` varchar(50) DEFAULT NULL COMMENT '污染因子code',
  `stat_field` varchar(255) DEFAULT NULL COMMENT '统计字段，多个字段逗号分隔，字段限值竖线分割',
  `remark` text COMMENT '备注',
  PRIMARY KEY (`id`)
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '企业数据有效率统计配置表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for t_bas_enterprise
-- ----------------------------
DROP TABLE IF EXISTS `t_bas_enterprise`;
CREATE TABLE `t_bas_enterprise` (
  `ent_code` varchar(26) NOT NULL COMMENT '企业编码',
  `parent_code` varchar(26) DEFAULT NULL COMMENT '父部门编码',
  `ent_name` varchar(255) DEFAULT NULL COMMENT '企业名称',
  `social_credit_code` varchar(50) DEFAULT NULL COMMENT '社会统一信用代码',
  `shorter_name` varchar(40) DEFAULT NULL COMMENT '企业简称',
  `legal_person` varchar(100) DEFAULT NULL COMMENT '法定代表人',
  `ent_director_name` varchar(50) DEFAULT NULL COMMENT '企业负责人姓名',
  `ent_director_phone` varchar(20) DEFAULT NULL COMMENT '企业负责人电话',
  `ent_director_email` varchar(100) DEFAULT NULL COMMENT '企业负责人邮箱',
  `region` varchar(30) DEFAULT NULL COMMENT '所在地区（地区选择）',
  `address` varchar(255) DEFAULT NULL COMMENT '详细地址（手填）',
  `longitude` double DEFAULT NULL COMMENT '经度',
  `latitude` double DEFAULT NULL COMMENT '纬度',
  `ent_status` varchar(30) DEFAULT NULL COMMENT '企业状态, 字典enterprise_status',
  `ent_scale` varchar(30) DEFAULT NULL COMMENT '企业规模，字典enterprise_scale',
  `ent_type` varchar(30) DEFAULT NULL COMMENT '企业类型，字典enterprise_type',
  `industry_type` varchar(30) DEFAULT NULL COMMENT '行业类型，字典industry_type',
  `pollution_class` varchar(30) DEFAULT NULL COMMENT '污染源类别，字典types_of_pollution_sources',
  `env_director_name` varchar(50) DEFAULT NULL COMMENT '环保负责人姓名',
  `env_director_phone` varchar(20) DEFAULT NULL COMMENT '环保负责人电话',
  `env_director_email` varchar(100) DEFAULT NULL COMMENT '环保负责人邮箱',
  `ent_introduction` text COMMENT '企业介绍',
  `we_com_msg` varchar(255) DEFAULT NULL COMMENT '企业微信关联信息',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  `update_time` datetime DEFAULT NULL COMMENT '更新时间',
  `env_policy_name` varchar(255) DEFAULT NULL COMMENT '环保制度名称',
  `env_policy_date` date DEFAULT NULL COMMENT '环保制度执行日期',
  `env_policy_level` varchar(64) DEFAULT NULL COMMENT '环保制度执行级别',
  PRIMARY KEY (`ent_code`)
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '企业基础信息表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for t_bas_fee_status_dict
-- ----------------------------
DROP TABLE IF EXISTS `t_bas_fee_status_dict`;
CREATE TABLE `t_bas_fee_status_dict` (
  `id` int NOT NULL AUTO_INCREMENT COMMENT '状态ID',
  `status_code` varchar(20) NOT NULL COMMENT '状态代码',
  `status_name` varchar(50) NOT NULL COMMENT '状态名称',
  `description` varchar(200) DEFAULT NULL COMMENT '状态描述',
  `sort_order` int DEFAULT 0 COMMENT '排序',
  PRIMARY KEY (`id`),
  UNIQUE INDEX `status_code`(`status_code`)
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '费用状态字典表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for t_bas_fee_type_dict
-- ----------------------------
DROP TABLE IF EXISTS `t_bas_fee_type_dict`;
CREATE TABLE `t_bas_fee_type_dict` (
  `id` int NOT NULL AUTO_INCREMENT COMMENT '类型ID',
  `type_code` varchar(20) NOT NULL COMMENT '类型编码',
  `type_name` varchar(50) NOT NULL COMMENT '类型名称',
  `description` varchar(200) DEFAULT NULL COMMENT '类型描述',
  `sort_order` int DEFAULT 0 COMMENT '排序',
  PRIMARY KEY (`id`),
  UNIQUE INDEX `type_code`(`type_code`)
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '费用类型字典表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for t_bas_industry_category
-- ----------------------------
DROP TABLE IF EXISTS `t_bas_industry_category`;
CREATE TABLE `t_bas_industry_category` (
  `id` varchar(10) NOT NULL COMMENT '主键id',
  `pid` varchar(10) NOT NULL COMMENT '父级id',
  `code` varchar(20) DEFAULT NULL COMMENT '代码',
  `name` varchar(255) DEFAULT NULL COMMENT '类别名称',
  `status` tinyint DEFAULT 1 COMMENT '状态，1可用',
  `desc` text COMMENT '说明',
  PRIMARY KEY (`id`)
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '国民经济行业分类' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for t_bas_payment_method_dict
-- ----------------------------
DROP TABLE IF EXISTS `t_bas_payment_method_dict`;
CREATE TABLE `t_bas_payment_method_dict` (
  `id` int NOT NULL AUTO_INCREMENT COMMENT '支付方式ID',
  `method_code` varchar(20) NOT NULL COMMENT '支付方式代码',
  `method_name` varchar(50) NOT NULL COMMENT '支付方式名称',
  `description` varchar(200) DEFAULT NULL COMMENT '方式描述',
  `sort_order` int DEFAULT 0 COMMENT '排序',
  PRIMARY KEY (`id`),
  UNIQUE INDEX `method_code`(`method_code`)
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '支付方式字典表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for t_bas_payment_status_dict
-- ----------------------------
DROP TABLE IF EXISTS `t_bas_payment_status_dict`;
CREATE TABLE `t_bas_payment_status_dict` (
  `id` int NOT NULL AUTO_INCREMENT COMMENT '状态ID',
  `status_code` varchar(20) NOT NULL COMMENT '状态代码',
  `status_name` varchar(50) NOT NULL COMMENT '状态名称',
  `description` varchar(200) DEFAULT NULL COMMENT '状态描述',
  `sort_order` int DEFAULT 0 COMMENT '排序',
  PRIMARY KEY (`id`),
  UNIQUE INDEX `status_code`(`status_code`)
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '付款状态字典表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for t_bas_pollutant_code
-- ----------------------------
DROP TABLE IF EXISTS `t_bas_pollutant_code`;
CREATE TABLE `t_bas_pollutant_code` (
  `pollutant_code` varchar(16) NOT NULL COMMENT '污染因子实际编码--以HJ-2017协议为主',
  `2017_code` varchar(16) DEFAULT NULL COMMENT '2017版报文编码',
  `2005_code` varchar(16) DEFAULT NULL COMMENT '2005版报文编码',
  `pollutant_name_en` varchar(64) DEFAULT NULL COMMENT '污染因子名称--英文',
  `pollutant_name_cn` varchar(64) DEFAULT NULL COMMENT '污染因子名称--中文',
  `pollutant_type` tinyint DEFAULT NULL COMMENT '污染因子类型：1：废水；2：废气；3：无组织',
  `pollutant_unit_en` varchar(64) DEFAULT NULL COMMENT '污染因子单位--英文',
  `pollutant_unit_cn` varchar(64) DEFAULT NULL COMMENT '污染因子单位--中文',
  `unit_pf_cn` varchar(64) DEFAULT NULL COMMENT '排放量单位--中文',
  `unit_pf_en` varchar(64) DEFAULT NULL COMMENT '排放量单位--英文',
  `pollutant_status` tinyint DEFAULT NULL COMMENT '使用状态：0：未使用；1：正在使用',
  `pollutant_sort` int DEFAULT NULL COMMENT '排序码',
  `remark` varchar(255) DEFAULT NULL COMMENT '备注',
  `mon_factor` json NULL COMMENT '监测因子项，key：监测值，val：描述',
  PRIMARY KEY (`pollutant_code`)
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '数采报文对应的污染因子关系 2017版本和2003版' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for t_bas_relate
-- ----------------------------
DROP TABLE IF EXISTS `t_bas_relate`;
CREATE TABLE `t_bas_relate` (
  `source_id` varchar(26) NOT NULL COMMENT '关联源方id',
  `target_type` varchar(30) NOT NULL COMMENT '关联的类型',
  `target_id` varchar(26) NOT NULL COMMENT '关联目标id',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`source_id`, `target_type`, `target_id`),
  INDEX `idx_target_type_id`(`target_type`, `target_id`),
  INDEX `idx_target_id`(`target_id`)
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '各配置的关联关系' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for t_bas_user_put_info
-- ----------------------------
DROP TABLE IF EXISTS `t_bas_user_put_info`;
CREATE TABLE `t_bas_user_put_info` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键',
  `user_id` bigint DEFAULT NULL COMMENT '用户ID',
  `out_put_id` varchar(26) DEFAULT NULL COMMENT '排口id',
  PRIMARY KEY (`id`),
  INDEX `int_user`(`user_id`)
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '用户收藏关注排口信息' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for t_data_annex
-- ----------------------------
DROP TABLE IF EXISTS `t_data_annex`;
CREATE TABLE `t_data_annex` (
  `annex_id` varchar(26) NOT NULL,
  `source_id` varchar(40) DEFAULT NULL COMMENT '附件归属id',
  `source_type` varchar(40) DEFAULT NULL COMMENT '附件归属类型',
  `file_type` varchar(16) DEFAULT NULL COMMENT '文件类型:如jpg、txt等',
  `file_name` varchar(255) DEFAULT NULL COMMENT '文件的名称，包括扩展名',
  `file_path` text COMMENT '文件路径',
  `file_size` bigint DEFAULT NULL COMMENT '文件大小',
  `file_desc` text COMMENT '文件描述',
  `create_user` varchar(64) DEFAULT NULL COMMENT '上传人',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '上传时间',
  PRIMARY KEY (`annex_id`),
  INDEX `index_uuid`(`source_type`, `source_id`)
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '基础附件表，外部用source_id、source_type关联' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for t_device_info
-- ----------------------------
DROP TABLE IF EXISTS `t_device_info`;
CREATE TABLE `t_device_info` (
  `mn_num` varchar(64) NOT NULL COMMENT '设备mn编号',
  `mn_name` varchar(255) DEFAULT NULL COMMENT '设备名称',
  `device_brand` varchar(255) DEFAULT NULL COMMENT '品牌',
  `device_model` varchar(255) DEFAULT NULL COMMENT '型号',
  `device_quantity` int DEFAULT NULL COMMENT '设备数量',
  `setup_time` datetime DEFAULT NULL COMMENT '安装时间',
  `lifespan` int DEFAULT NULL COMMENT '寿命',
  `life_unit` tinyint DEFAULT NULL COMMENT '寿命单位，2分钟、3小时、4日、5月、6年',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`mn_num`)
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '设备信息表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for t_emergency_contact
-- ----------------------------
DROP TABLE IF EXISTS `t_emergency_contact`;
CREATE TABLE `t_emergency_contact` (
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
  INDEX `idx_ent_code`(`ent_code`),
  INDEX `idx_contact_name`(`contact_name`),
  INDEX `idx_dept_group`(`dept_group`)
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '应急通讯录表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for t_emergency_drill
-- ----------------------------
DROP TABLE IF EXISTS `t_emergency_drill`;
CREATE TABLE `t_emergency_drill` (
  `drill_id` varchar(50) NOT NULL COMMENT '演练ID',
  `ent_code` varchar(50) DEFAULT NULL COMMENT '企业编码',
  `drill_name` varchar(200) NOT NULL COMMENT '演练名称',
  `drill_type` varchar(50) DEFAULT NULL COMMENT '演练类型',
  `drill_date` date DEFAULT NULL COMMENT '演练时间',
  `related_plan` varchar(200) DEFAULT NULL COMMENT '涉及预案',
  `drill_content` text COMMENT '演练内容',
  `drill_summary` text COMMENT '演练总结',
  `drill_status` int DEFAULT 1 COMMENT '演练状态：1-计划中，2-已完成',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`drill_id`),
  INDEX `idx_ent_code`(`ent_code`),
  INDEX `idx_drill_name`(`drill_name`),
  INDEX `idx_drill_type`(`drill_type`),
  INDEX `idx_drill_date`(`drill_date`)
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '应急演练表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for t_emergency_expert
-- ----------------------------
DROP TABLE IF EXISTS `t_emergency_expert`;
CREATE TABLE `t_emergency_expert` (
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
  INDEX `idx_ent_code`(`ent_code`),
  INDEX `idx_expert_name`(`expert_name`),
  INDEX `idx_specialty`(`specialty`)
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '应急专家表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for t_emergency_material
-- ----------------------------
DROP TABLE IF EXISTS `t_emergency_material`;
CREATE TABLE `t_emergency_material` (
  `material_id` varchar(50) NOT NULL COMMENT '物资ID',
  `ent_code` varchar(50) DEFAULT NULL COMMENT '企业编码',
  `material_name` varchar(200) NOT NULL COMMENT '物资名称',
  `model_spec` varchar(200) DEFAULT NULL COMMENT '型号规格',
  `store_place` varchar(200) DEFAULT NULL COMMENT '存放地点',
  `quantity` int DEFAULT NULL COMMENT '数量',
  `unit` varchar(50) DEFAULT NULL COMMENT '单位',
  `expire_date` date DEFAULT NULL COMMENT '保质期',
  `recheck_date` date DEFAULT NULL COMMENT '复检日期',
  `manufacturer_name` varchar(200) DEFAULT NULL COMMENT '生产厂家',
  `manufacturer_contact` varchar(100) DEFAULT NULL COMMENT '厂家联系人',
  `manufacturer_phone` varchar(20) NOT NULL COMMENT '厂家电话',
  `manager_name` varchar(100) DEFAULT NULL COMMENT '管理员',
  `manager_phone` varchar(20) NOT NULL COMMENT '管理员电话',
  `remark` varchar(500) DEFAULT NULL COMMENT '备注',
  `warn_status` int DEFAULT 0 COMMENT '预警状态：0-正常，1-即将到期，2-已过期',
  `warn_days` int DEFAULT 30 COMMENT '预警天数',
  `warn_type` varchar(20) DEFAULT NULL COMMENT '预警类型：expire-保质期，recheck-复检期',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`material_id`),
  INDEX `idx_ent_code`(`ent_code`),
  INDEX `idx_material_name`(`material_name`),
  INDEX `idx_warn_status`(`warn_status`),
  INDEX `idx_expire_date`(`expire_date`),
  INDEX `idx_recheck_date`(`recheck_date`)
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '应急物资表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for t_emergency_notice
-- ----------------------------
DROP TABLE IF EXISTS `t_emergency_notice`;
CREATE TABLE `t_emergency_notice` (
  `notice_id` varchar(50) NOT NULL COMMENT '通知ID',
  `ent_code` varchar(50) DEFAULT NULL COMMENT '企业编码',
  `notice_title` varchar(200) NOT NULL COMMENT '通知标题',
  `notice_content` text COMMENT '通知内容',
  `event_location` varchar(200) DEFAULT NULL COMMENT '事件地点',
  `event_time` datetime DEFAULT NULL COMMENT '事件时间',
  `receiver_names` text COMMENT '接收人姓名列表',
  `send_status` varchar(20) DEFAULT 'success' COMMENT '发送状态：success-成功，failed-失败',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`notice_id`),
  INDEX `idx_ent_code`(`ent_code`),
  INDEX `idx_create_time`(`create_time`)
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '应急通知表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for t_emergency_plan
-- ----------------------------
DROP TABLE IF EXISTS `t_emergency_plan`;
CREATE TABLE `t_emergency_plan` (
  `plan_id` varchar(50) NOT NULL COMMENT '预案ID',
  `ent_code` varchar(50) DEFAULT NULL COMMENT '企业编码',
  `plan_name` varchar(200) NOT NULL COMMENT '预案名称',
  `version` varchar(50) DEFAULT NULL COMMENT '版本号',
  `risk_unit` varchar(500) DEFAULT NULL COMMENT '风险单元',
  `handle_points` text COMMENT '处置要点',
  `plan_type` int DEFAULT 1 COMMENT '预案类型：1-附件管理，2-结构化',
  `remark` varchar(500) DEFAULT NULL COMMENT '备注',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`plan_id`),
  INDEX `idx_ent_code`(`ent_code`),
  INDEX `idx_plan_name`(`plan_name`)
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '应急预案表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for t_emergency_vehicle
-- ----------------------------
DROP TABLE IF EXISTS `t_emergency_vehicle`;
CREATE TABLE `t_emergency_vehicle` (
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
  INDEX `idx_ent_code`(`ent_code`),
  INDEX `idx_plate_no`(`plate_no`),
  INDEX `idx_vehicle_status`(`vehicle_status`)
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '应急车辆表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for t_ent_clean_produce
-- ----------------------------
DROP TABLE IF EXISTS `t_ent_clean_produce`;
CREATE TABLE `t_ent_clean_produce` (
  `clean_produce_id` varchar(40) NOT NULL COMMENT '排污许可id',
  `ent_code` varchar(40) DEFAULT NULL COMMENT '企业编码',
  `clean_name` varchar(255) DEFAULT NULL COMMENT '名称',
  `make_date` date DEFAULT NULL COMMENT '编制时间',
  `audit_focus` text COMMENT '审核重点',
  `plan_info` text COMMENT '方案情况',
  `reduce_effect` text COMMENT '减排效果',
  `work_progress` text COMMENT '工作进展 ',
  `effective_date` date DEFAULT NULL COMMENT '实施时间',
  PRIMARY KEY (`clean_produce_id`),
  INDEX `ind_ent_code`(`ent_code`)
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '企业清洁生产基础表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for t_ent_out_pollutant_permit
-- ----------------------------
DROP TABLE IF EXISTS `t_ent_out_pollutant_permit`;
CREATE TABLE `t_ent_out_pollutant_permit` (
  `ent_code` varchar(40) NOT NULL COMMENT '企业编码',
  `permit_level` varchar(32) DEFAULT NULL COMMENT '排污许可管理类别',
  `permit_num` varchar(50) DEFAULT NULL COMMENT '许可证编号',
  `begin_date` varchar(12) DEFAULT NULL COMMENT '有效期-开始时间（yyyy-MM-dd）',
  `end_date` varchar(12) DEFAULT NULL COMMENT '有效期-结束时间（yyyy-MM-dd）',
  `issue_office` varchar(255) DEFAULT NULL COMMENT '发证机关',
  `issue_date` varchar(12) DEFAULT NULL COMMENT '发证日期（yyyy-MM-dd）',
  `report_require` varchar(255) DEFAULT NULL COMMENT '执行报告报送要求',
  `product_desc` text COMMENT '主要产品',
  `product_output` text COMMENT '产量',
  `gas_poll_type` text COMMENT '废气污染物种类(pollutantCodes)',
  `gas_emission_rule` varchar(32) DEFAULT NULL COMMENT '废气排放规律',
  `gas_execute_standard` varchar(64) DEFAULT NULL COMMENT '废气执行标准',
  `gas_char_poll` text COMMENT '废气特征污染物',
  `water_poll_type` text COMMENT '废水污染物种类(pollutantCodes)',
  `water_emission_rule` varchar(32) DEFAULT NULL COMMENT '废水排放规律',
  `water_execute_standard` varchar(64) DEFAULT NULL COMMENT '废水执行标准',
  `water_char_poll` text COMMENT '废水特征污染物',
  `update_time` datetime DEFAULT NULL COMMENT '更新时间',
  `remark` text COMMENT '备注',
  PRIMARY KEY (`ent_code`)
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '企业排污许可基础表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for t_ent_out_pollutant_permit_count
-- ----------------------------
DROP TABLE IF EXISTS `t_ent_out_pollutant_permit_count`;
CREATE TABLE `t_ent_out_pollutant_permit_count` (
  `poll_permit_count_id` varchar(26) NOT NULL COMMENT '排污许可总量id',
  `ent_code` varchar(40) DEFAULT NULL COMMENT '企业编码',
  `poll_type` tinyint DEFAULT NULL COMMENT '污染物类别，1：废水；2：废气；3：无组织',
  `pollutant_code` varchar(64) DEFAULT NULL COMMENT '污染因子编码',
  `permit_year` int DEFAULT NULL COMMENT '年份',
  `permit_count` double DEFAULT NULL COMMENT '许可总量',
  PRIMARY KEY (`poll_permit_count_id`),
  INDEX `ind_ent_code`(`ent_code`)
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '企业排污许可总量基础表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for t_ent_out_put_alarm
-- ----------------------------
DROP TABLE IF EXISTS `t_ent_out_put_alarm`;
CREATE TABLE `t_ent_out_put_alarm` (
  `out_put_id` varchar(26) NOT NULL COMMENT '排口主键id（1,2,3...时为排口类型，公共的配置）',
  `alarm_code` int NOT NULL COMMENT '报警类型',
  `out_put_status` varchar(20) DEFAULT NULL COMMENT '排口状态标志',
  `is_enabled` tinyint DEFAULT 0 COMMENT '启动标志，0不启动、1启动',
  `data_type` varchar(20) DEFAULT NULL COMMENT '数据类型',
  `data_dur` int DEFAULT NULL COMMENT '持续时长(分钟)',
  `data_cycle` int DEFAULT NULL COMMENT '持续条数',
  PRIMARY KEY (`out_put_id`, `alarm_code`)
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '排口报警配置项' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for t_ent_out_put_info
-- ----------------------------
DROP TABLE IF EXISTS `t_ent_out_put_info`;
CREATE TABLE `t_ent_out_put_info` (
  `out_put_id` varchar(26) NOT NULL COMMENT '主键id',
  `ent_code` varchar(26) DEFAULT NULL COMMENT '关联企业id',
  `out_put_code` varchar(255) DEFAULT NULL COMMENT '废气排放口编码',
  `out_put_name` varchar(255) DEFAULT NULL COMMENT '废气排放口名称',
  `out_put_type` tinyint DEFAULT NULL COMMENT '排放口类型，1废水、2废气、3无组织、4VOC',
  `mn_num` varchar(255) DEFAULT NULL COMMENT '排放口设备mn号',
  `out_put_status` varchar(30) DEFAULT NULL COMMENT '排放口状态，字典out_put_status',
  `longitude` double DEFAULT NULL COMMENT '经度',
  `latitude` double DEFAULT NULL COMMENT '纬度',
  `out_put_height` double DEFAULT NULL COMMENT '排放口高度',
  `out_put_position` varchar(255) DEFAULT NULL COMMENT '排放口位置',
  `install_online` tinyint DEFAULT NULL COMMENT '是否安装在线设备',
  `trans_rate` float DEFAULT NULL COMMENT '数据传输率，%',
  `valid_rate` float DEFAULT NULL COMMENT '数据有效率，%',
  `real_data_interval` int DEFAULT NULL COMMENT '实时数据间隔，n秒',
  `minute_data_interval` int DEFAULT NULL COMMENT '分钟数据间隔，n分钟',
  `pollutant_code` text COMMENT '排口的污染物列表',
  `control_level` varchar(64) DEFAULT NULL COMMENT '控制级别（国控、省控、市控、无）',
  `dept_id` bigint DEFAULT NULL COMMENT '关联部门ID',
  `per_id` bigint DEFAULT NULL COMMENT '归属管理人员ID',
  `per_alarm_id` bigint DEFAULT NULL COMMENT '报警对应人id',
  `remark` varchar(255) DEFAULT NULL COMMENT '备注',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  `update_time` datetime DEFAULT NULL COMMENT '修改时间',
  `clock_range` int DEFAULT 100 COMMENT '打卡签到范围',
  PRIMARY KEY (`out_put_id`),
  INDEX `ind_ent_code`(`ent_code`),
  INDEX `ind_mn_num`(`mn_num`)
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '企业排口信息表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for t_ent_output_pollutant
-- ----------------------------
DROP TABLE IF EXISTS `t_ent_output_pollutant`;
CREATE TABLE `t_ent_output_pollutant` (
  `out_put_poll_id` varchar(26) NOT NULL COMMENT '排口污染物主键id',
  `out_put_id` varchar(26) NOT NULL COMMENT '关联排口主键id',
  `pollutant_code` varchar(40) NOT NULL COMMENT '排口关联污染物code',
  `pollutant_sort` int DEFAULT NULL COMMENT '排序码',
  `rtd_unit` varchar(20) DEFAULT NULL COMMENT '计量单位',
  `cou_unit` varchar(20) DEFAULT NULL COMMENT '累计单位',
  `decimal_places` int DEFAULT 3 COMMENT '小数点位数',
  `over_maxValue` double DEFAULT NULL COMMENT '超标上限',
  `over_minValue` double DEFAULT NULL COMMENT '超标下限',
  `is_zero_alarm` tinyint DEFAULT NULL COMMENT '是否零值报警 0：否；1：是',
  `is_continuity_alarm` tinyint DEFAULT NULL COMMENT '是否连续值报警 0：否；1：是',
  `is_negative_alarm` tinyint DEFAULT NULL COMMENT '是否负值报警 0：否；1：是',
  `monthly_limit_value` text COMMENT '月段污染物排放限值',
  `mon_factor` text COMMENT '监测因子项列表',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  `update_time` datetime DEFAULT NULL COMMENT '修改时间',
  PRIMARY KEY (`out_put_poll_id`),
  INDEX `ind_out_put_id`(`out_put_id`),
  INDEX `ind_poll_code`(`pollutant_code`)
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '企业排口污染物信息表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for t_ent_plc_info
-- ----------------------------
DROP TABLE IF EXISTS `t_ent_plc_info`;
CREATE TABLE `t_ent_plc_info` (
  `id` bigint UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '自增主键ID',
  `ent_code` varchar(26) NOT NULL COMMENT '企业编码（所属根企业）',
  `out_put_id` varchar(26) DEFAULT NULL COMMENT '关联排口ID',
  `unit_id` tinyint UNSIGNED NOT NULL COMMENT 'PLC单元编号(1-255)',
  `sort_order` int UNSIGNED NOT NULL DEFAULT 0 COMMENT '排序号(显示顺序)',
  `point_name` varchar(200) DEFAULT NULL COMMENT '点位名称/描述',
  `point_type` tinyint UNSIGNED DEFAULT NULL COMMENT '点位类型(1:DI数字输入 2:DO数字输出 3:AI模拟输入 4:AO模拟输出 5:寄存器)',
  `data_type` tinyint UNSIGNED DEFAULT NULL COMMENT '数据类型(1:bool 2:int16 3:uint16 4:int32 5:uint32 6:float32 7:float64)',
  `address` int UNSIGNED DEFAULT NULL COMMENT 'PLC绝对地址（硬件地址）',
  `register_address` int UNSIGNED DEFAULT NULL COMMENT 'Modbus寄存器地址（通信地址）',
  `coefficient` decimal(10, 4) DEFAULT 1.0000 COMMENT '转换系数',
  `unit` varchar(20) DEFAULT NULL COMMENT '计量单位',
  `min_value` decimal(20, 6) DEFAULT NULL COMMENT '量程最小值',
  `max_value` decimal(20, 6) DEFAULT NULL COMMENT '量程最大值',
  `precision` tinyint UNSIGNED DEFAULT NULL COMMENT '小数精度(0-6)',
  `status` tinyint UNSIGNED DEFAULT 1 COMMENT '状态(0:禁用 1:启用)',
  `description` varchar(500) DEFAULT NULL COMMENT '备注信息',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  INDEX `idx_out_put_id`(`out_put_id`) COMMENT '关联排口ID索引',
  INDEX `idx_unit_ent`(`unit_id`, `ent_code`) COMMENT '单元+企业查询',
  INDEX `idx_ent_status`(`ent_code`, `status`) COMMENT '企业+状态查询',
  INDEX `idx_ent_order`(`ent_code`, `sort_order`) COMMENT '企业+排序查询'
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '企业PLC设备点位信息表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for t_ent_poll_control_facility
-- ----------------------------
DROP TABLE IF EXISTS `t_ent_poll_control_facility`;
CREATE TABLE `t_ent_poll_control_facility` (
  `facility_id` varchar(26) NOT NULL COMMENT '主键id',
  `ent_code` varchar(26) DEFAULT NULL COMMENT '关联企业id',
  `out_put_type` tinyint DEFAULT NULL COMMENT '排放口类型，1废水、2废气、3无组织、4VOC',
  `facility_code` varchar(64) DEFAULT NULL COMMENT '治理设施编号',
  `facility_name` varchar(255) DEFAULT NULL COMMENT '治理设施名称',
  `pollutant` varchar(255) DEFAULT NULL COMMENT '主要污染物',
  `govern_process` varchar(255) DEFAULT NULL COMMENT '治理工艺',
  `efficiency` double DEFAULT NULL COMMENT '设计治理效率(%)',
  `design_capacity` varchar(32) DEFAULT NULL COMMENT '设计处理能力',
  `actual_operating_rate` int DEFAULT NULL COMMENT '设计治理设施运行率(0-100%)',
  `install_date` date DEFAULT NULL COMMENT '安装时间',
  `remark` text COMMENT '备注',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  `update_time` datetime DEFAULT NULL COMMENT '修改时间',
  PRIMARY KEY (`facility_id`),
  INDEX `ind_ent_code`(`ent_code`, `out_put_type`)
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '企业污染治理设施表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for t_ent_poll_control_facility_relate
-- ----------------------------
DROP TABLE IF EXISTS `t_ent_poll_control_facility_relate`;
CREATE TABLE `t_ent_poll_control_facility_relate` (
  `facility_id` varchar(26) NOT NULL COMMENT '企业污染治理设施主键id',
  `other_type` varchar(30) NOT NULL COMMENT '其他关联的类型',
  `other_id` varchar(26) NOT NULL COMMENT '其他关联的主键id',
  PRIMARY KEY (`facility_id`, `other_id`, `other_type`)
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '企业污染治理设施关联排口表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for t_ent_produce_facility
-- ----------------------------
DROP TABLE IF EXISTS `t_ent_produce_facility`;
CREATE TABLE `t_ent_produce_facility` (
  `facility_id` varchar(26) NOT NULL COMMENT '主键id',
  `ent_code` varchar(26) DEFAULT NULL COMMENT '所属企业',
  `facility_name` varchar(255) DEFAULT NULL COMMENT '生产设施名称',
  `facility_code` varchar(50) DEFAULT NULL COMMENT '生产设施编号',
  `facility_type` varchar(64) DEFAULT NULL COMMENT '设备类型',
  `specification` varchar(128) DEFAULT NULL COMMENT '设备规格',
  `facility_model` varchar(64) DEFAULT NULL COMMENT '型号',
  `supplier` varchar(255) DEFAULT NULL COMMENT '制造商/供应商',
  `buy_date` date DEFAULT NULL COMMENT '购置时间',
  `equipment_status` tinyint DEFAULT 1 COMMENT '设备状态：1-在用，2-停用',
  `facility_number` int DEFAULT NULL COMMENT '设施数量',
  `measure_unit` varchar(50) DEFAULT NULL COMMENT '计量单位',
  `design_capacity` varchar(255) DEFAULT NULL COMMENT '设计生产能力',
  `remark` text COMMENT '备注',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`facility_id`),
  INDEX `ind_ent_code`(`ent_code`)
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '企业生产设施/设备表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for t_ent_produce_facility_relate
-- ----------------------------
DROP TABLE IF EXISTS `t_ent_produce_facility_relate`;
CREATE TABLE `t_ent_produce_facility_relate` (
  `facility_id` varchar(26) NOT NULL COMMENT '企业生产设施主键id',
  `other_type` varchar(30) NOT NULL COMMENT '其他关联的类型',
  `other_id` varchar(26) NOT NULL COMMENT '其他关联的主键id',
  PRIMARY KEY (`facility_id`, `other_id`, `other_type`)
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '企业生产设施关联其他数据表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for t_ent_produce_workshop
-- ----------------------------
DROP TABLE IF EXISTS `t_ent_produce_workshop`;
CREATE TABLE `t_ent_produce_workshop` (
  `workshop_id` varchar(26) NOT NULL COMMENT '企业生产车间主键id',
  `ent_code` varchar(26) DEFAULT NULL COMMENT '所属企业',
  `workshop_name` varchar(255) DEFAULT NULL COMMENT '生产车间名称',
  `workshop_code` varchar(50) DEFAULT NULL COMMENT '生产车间编号',
  `dept_id` bigint DEFAULT NULL COMMENT '归属管理部门ID',
  `per_id` bigint DEFAULT NULL COMMENT '归属管理人员ID',
  `remark` text COMMENT '备注',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `longitude` double DEFAULT NULL COMMENT '经度',
  `latitude` double DEFAULT NULL COMMENT '纬度',
  PRIMARY KEY (`workshop_id`),
  INDEX `ind_ent_code`(`ent_code`)
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '企业生产车间表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for t_ent_production_line
-- ----------------------------
DROP TABLE IF EXISTS `t_ent_production_line`;
CREATE TABLE `t_ent_production_line` (
  `line_id` varchar(26) NOT NULL COMMENT '生产线ID',
  `line_code` varchar(50) NOT NULL COMMENT '生产线编码，车间下的编码唯一',
  `line_name` varchar(255) DEFAULT NULL COMMENT '生产线名称',
  `workshop_id` varchar(26) NOT NULL COMMENT '所属车间ID, 必填',
  `process_type` varchar(255) DEFAULT NULL COMMENT '工艺类型',
  `product_type` varchar(255) DEFAULT NULL COMMENT '产品类型',
  `status` tinyint(1) DEFAULT 1 COMMENT '状态：1-在用，2-停用',
  `capacity` decimal(12, 4) DEFAULT NULL COMMENT '设计产能',
  `capacity_unit` varchar(20) DEFAULT NULL COMMENT '产能单位',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`line_id`),
  INDEX `idx_workshop_id`(`workshop_id`),
  INDEX `idx_line_code`(`line_code`)
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '企业生产线信息表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for t_env_fee_invoices
-- ----------------------------
DROP TABLE IF EXISTS `t_env_fee_invoices`;
CREATE TABLE `t_env_fee_invoices` (
  `invoice_number` varchar(20) NOT NULL COMMENT '发票号码',
  `fee_id` varchar(26) DEFAULT NULL COMMENT '费用记录ID',
  `invoice_code` varchar(20) DEFAULT NULL COMMENT '发票代码',
  `invoice_amount` decimal(15, 2) DEFAULT NULL COMMENT '发票金额（不含税）',
  `tax_amount` decimal(15, 2) DEFAULT NULL COMMENT '税额',
  `invoice_date` date DEFAULT NULL COMMENT '开票日期',
  `invoice_type` varchar(20) DEFAULT NULL COMMENT '发票类型：VAT_SPECIAL-增值税专用发票, VAT_NORMAL-增值税普通发票, VAT_ELECTRONIC-增值税电子普通发票, TRANSPORT-运输发票等',
  `tax_rate` int DEFAULT NULL COMMENT '税率（单位：百分比，如13表示13%）',
  `seller_name` varchar(200) DEFAULT NULL COMMENT '销售方名称',
  `seller_tax_id` varchar(50) DEFAULT NULL COMMENT '销售方纳税人识别号',
  `buyer_name` varchar(200) DEFAULT NULL COMMENT '购买方名称',
  `buyer_tax_id` varchar(50) DEFAULT NULL COMMENT '购买方纳税人识别号',
  `invoice_status` tinyint DEFAULT NULL COMMENT '发票状态：1-正常 2-已红冲',
  `check_code` varchar(10) DEFAULT NULL COMMENT '发票校验码',
  `remark` text COMMENT '发票备注',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`invoice_number`),
  INDEX `idx_fee_id`(`fee_id`),
  INDEX `idx_invoice_type`(`invoice_type`),
  INDEX `idx_invoice_date`(`invoice_date`)
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '费用发票信息表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for t_env_fee_payments
-- ----------------------------
DROP TABLE IF EXISTS `t_env_fee_payments`;
CREATE TABLE `t_env_fee_payments` (
  `payment_number` varchar(50) NOT NULL COMMENT '付款流水号',
  `fee_id` varchar(26) DEFAULT NULL COMMENT '费用记录ID',
  `payment_amount` decimal(15, 2) DEFAULT NULL COMMENT '付款金额',
  `payment_date` date DEFAULT NULL COMMENT '付款日期',
  `payment_method` varchar(20) DEFAULT NULL COMMENT '支付方式',
  `bank_account` varchar(100) DEFAULT NULL COMMENT '银行账户',
  `transaction_number` varchar(100) DEFAULT NULL COMMENT '交易流水号',
  `payer_account` varchar(100) DEFAULT NULL COMMENT '付款方账户',
  `payee_account` varchar(100) DEFAULT NULL COMMENT '收款方账户',
  `payment_status` varchar(20) DEFAULT NULL COMMENT '付款状态',
  `payment_remark` text COMMENT '付款备注',
  `refund_amount` decimal(15, 2) DEFAULT NULL COMMENT '退款总金额',
  `refund_date` date DEFAULT NULL COMMENT '最后退款日期',
  `refund_remark` text COMMENT '退款备注',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '最后修改时间',
  PRIMARY KEY (`payment_number`),
  INDEX `idx_fee_id`(`fee_id`),
  INDEX `idx_payment_date`(`payment_date`),
  INDEX `idx_transaction_number`(`transaction_number`)
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '费用付款信息表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for t_env_fees
-- ----------------------------
DROP TABLE IF EXISTS `t_env_fees`;
CREATE TABLE `t_env_fees` (
  `fee_id` varchar(26) NOT NULL COMMENT '费用记录ID',
  `ent_code` varchar(26) DEFAULT NULL COMMENT '关联企业编码',
  `project_id` varchar(26) DEFAULT NULL COMMENT '关联项目（环评环保管理-项目主键id）',
  `fee_type` varchar(20) DEFAULT NULL COMMENT '费用类型（源自 t_bas_fee_type_dict）',
  `fee_code` varchar(50) DEFAULT NULL COMMENT '费用编号',
  `fee_amount` decimal(15, 2) DEFAULT NULL COMMENT '费用金额',
  `invoice_amount` decimal(15, 2) DEFAULT NULL COMMENT '开票金额（统计的开票金额减去红冲金额）',
  `payment_amount` decimal(15, 2) DEFAULT NULL COMMENT '付款金额（统计支付完成金额和部分退款中的付款金额）',
  `payment_date` date DEFAULT NULL COMMENT '缴费截至日期',
  `status` varchar(20) DEFAULT NULL COMMENT '费用状态（源自 t_bas_fee_status_dict）',
  `fee_desc` text COMMENT '费用描述',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`fee_id`),
  INDEX `idx_ent_code`(`ent_code`, `fee_type`),
  INDEX `idx_status`(`status`)
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '环保费用登记表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for t_env_investment
-- ----------------------------
DROP TABLE IF EXISTS `t_env_investment`;
CREATE TABLE `t_env_investment` (
  `investment_id` varchar(26) NOT NULL COMMENT '主键id',
  `ent_code` varchar(26) DEFAULT NULL COMMENT '所属企业（项目所在单位）',
  `project_name` varchar(255) DEFAULT NULL COMMENT '项目名称',
  `investment_amount` double DEFAULT NULL COMMENT '投资金额(万)',
  `dept_id` bigint DEFAULT NULL COMMENT '部门id（项目负责单位）',
  `pro_person_id` varchar(26) DEFAULT NULL COMMENT '环保人员主键id（项目负责人）',
  `investment_desc` text COMMENT '项目内容',
  `reduction_effect` text COMMENT '减排效果',
  `government_fund` tinyint DEFAULT NULL COMMENT '是否取得政府资金支持',
  `pipa_time` date DEFAULT NULL COMMENT '计划内部立项时间',
  `aipa_time` date DEFAULT NULL COMMENT '实际内部立项时间',
  `pce_time` date DEFAULT NULL COMMENT '计划施工入厂时间',
  `ace_time` date DEFAULT NULL COMMENT '实际施工入厂时间',
  `pc_time` date DEFAULT NULL COMMENT '计划完成时间',
  `af_time` date DEFAULT NULL COMMENT '实际完成时间',
  `pa_time` date DEFAULT NULL COMMENT '计划验收时间',
  `ac_time` date DEFAULT NULL COMMENT '实际验收时间',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  `update_time` datetime DEFAULT NULL COMMENT '更新时间',
  PRIMARY KEY (`investment_id`),
  INDEX `ind_ent_code`(`ent_code`)
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '环保法规与体系管理-环保投入' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for t_env_learn
-- ----------------------------
DROP TABLE IF EXISTS `t_env_learn`;
CREATE TABLE `t_env_learn` (
  `learn_id` varchar(26) NOT NULL COMMENT '主键id',
  `learn_theme` varchar(255) DEFAULT NULL COMMENT '学习主题',
  `required_duration` int DEFAULT NULL COMMENT '学习要求时长，小时',
  `learn_start` date DEFAULT NULL COMMENT '学习开始时间',
  `learn_end` date DEFAULT NULL COMMENT '学习结束时间',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`learn_id`)
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '环境政策法规信息学习管理' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for t_env_learn_detail
-- ----------------------------
DROP TABLE IF EXISTS `t_env_learn_detail`;
CREATE TABLE `t_env_learn_detail` (
  `learn_detail_id` varchar(26) NOT NULL COMMENT '学习详情主键id',
  `learn_user_id` varchar(26) DEFAULT NULL COMMENT '学习统计主键id',
  `learn_start` datetime DEFAULT NULL COMMENT '本次学习开始时间',
  `learn_end` datetime DEFAULT NULL COMMENT '本次学习结束时间',
  `duration` int DEFAULT 0 COMMENT '本次学习时间，分钟',
  PRIMARY KEY (`learn_detail_id`),
  INDEX `ind_learn_user`(`learn_user_id`)
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '环境政策法规信息学习详情' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for t_env_learn_ent
-- ----------------------------
DROP TABLE IF EXISTS `t_env_learn_ent`;
CREATE TABLE `t_env_learn_ent` (
  `learn_id` varchar(26) NOT NULL,
  `ent_code` varchar(26) NOT NULL,
  PRIMARY KEY (`learn_id`, `ent_code`),
  INDEX `idx_ent_code`(`ent_code`)
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '学习-企业关联表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for t_env_learn_policy
-- ----------------------------
DROP TABLE IF EXISTS `t_env_learn_policy`;
CREATE TABLE `t_env_learn_policy` (
  `learn_id` varchar(26) NOT NULL,
  `policy_id` varchar(26) NOT NULL,
  PRIMARY KEY (`learn_id`, `policy_id`),
  INDEX `idx_ent_code`(`policy_id`)
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '学习-政策法规关联表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for t_env_learn_user
-- ----------------------------
DROP TABLE IF EXISTS `t_env_learn_user`;
CREATE TABLE `t_env_learn_user` (
  `learn_user_id` varchar(26) NOT NULL COMMENT '学习统计主键id',
  `learn_id` varchar(26) DEFAULT NULL COMMENT '学习id',
  `user_id` bigint DEFAULT NULL COMMENT '账户id',
  `completed_duration` int DEFAULT 0 COMMENT '学习完成时长，小时',
  PRIMARY KEY (`learn_user_id`),
  INDEX `ind_learn`(`learn_id`),
  INDEX `ind_user`(`user_id`)
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '环境政策法规信息学习情况统计' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for t_env_mange_check
-- ----------------------------
DROP TABLE IF EXISTS `t_env_mange_check`;
CREATE TABLE `t_env_mange_check` (
  `check_id` varchar(26) NOT NULL COMMENT '环评环保管理-环评主键id',
  `ent_code` varchar(26) DEFAULT NULL COMMENT '企业编码',
  `check_name` varchar(255) DEFAULT NULL COMMENT '环评环保管理-环评名称',
  `check_reply` tinyint DEFAULT NULL COMMENT '是否需验收批复，0否，1是',
  `approval_depart` varchar(255) DEFAULT NULL COMMENT '审批部门',
  `reply_no` varchar(255) DEFAULT NULL COMMENT '批复文号',
  `check_agency` varchar(255) DEFAULT NULL COMMENT '验收监测机构',
  `check_begin_time` date DEFAULT NULL COMMENT '验收监测时间-开始时间',
  `check_end_time` date DEFAULT NULL COMMENT '验收监测时间-结束时间',
  `review_time` date DEFAULT NULL COMMENT '验收报告专家评审时间',
  `review_issue` text COMMENT '验收报告专家评审主要问题',
  `record_address` text COMMENT '验收报告公式地址',
  `check_record_begin_time` date DEFAULT NULL COMMENT '验收报告公式开始时间',
  `check_record_end_time` date DEFAULT NULL COMMENT '验收报告公式截至时间',
  `remark` text COMMENT '备注',
  PRIMARY KEY (`check_id`),
  INDEX `ind_ent_code`(`ent_code`)
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '企业环评环保管理-环保验收' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for t_env_mange_evaluate
-- ----------------------------
DROP TABLE IF EXISTS `t_env_mange_evaluate`;
CREATE TABLE `t_env_mange_evaluate` (
  `evaluate_id` varchar(26) NOT NULL COMMENT '环评环保管理-环评主键id',
  `ent_code` varchar(26) DEFAULT NULL COMMENT '企业编码',
  `evaluate_name` varchar(255) DEFAULT NULL COMMENT '环评环保管理-环评名称',
  `eia_level` varchar(255) DEFAULT NULL COMMENT '环评层级',
  `approval_depart` varchar(255) DEFAULT NULL COMMENT '审批部门',
  `reply_no` varchar(255) DEFAULT NULL COMMENT '批复文号',
  `rating_agency` varchar(255) DEFAULT NULL COMMENT '评价机构',
  `lead_author` varchar(255) DEFAULT NULL COMMENT '主笔人员',
  `rating_cost` int DEFAULT NULL COMMENT '评价费用(元)',
  `pollutant_code` text COMMENT '主要污染物',
  `pollutant_total` double DEFAULT NULL COMMENT '污染物总量(kg)',
  `contract_time` date DEFAULT NULL COMMENT '合同签订时间',
  `report_sub_time` date DEFAULT NULL COMMENT '报告提交时间',
  `publicity_time` date DEFAULT NULL COMMENT '对外公示时间',
  `approval_time` date DEFAULT NULL COMMENT '批复时间',
  `remark` text COMMENT '备注',
  PRIMARY KEY (`evaluate_id`),
  INDEX `ind_ent_code`(`ent_code`)
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '企业环评环保管理-环评' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for t_env_mange_project
-- ----------------------------
DROP TABLE IF EXISTS `t_env_mange_project`;
CREATE TABLE `t_env_mange_project` (
  `project_id` varchar(26) NOT NULL COMMENT '环评环保管理-项目主键id',
  `ent_code` varchar(26) NOT NULL COMMENT '企业编码',
  `project_name` varchar(255) DEFAULT NULL COMMENT '项目名称',
  `project_code` varchar(255) DEFAULT NULL COMMENT '项目代码',
  `project_nature` varchar(255) DEFAULT NULL COMMENT '项目性质',
  `main_content` text COMMENT '主要建设内容',
  `product` text COMMENT '产品',
  `p_capacity` text COMMENT '产能',
  `main_material` text COMMENT '原料信息',
  `sub_material` text COMMENT '辅料信息',
  `product_shift_sys` text COMMENT '生产班制',
  `construct_side` text COMMENT '建设地点',
  `industry_category` text COMMENT '国民经济行业类别',
  `grade` tinyint DEFAULT NULL COMMENT '环评等级，1报告书、2报告表、3登记表、0无需环评（默认）',
  `judgment_reason` text COMMENT '判断依据',
  `land_area` double DEFAULT NULL COMMENT '用地面积(平米)',
  `ext_appr_time` date DEFAULT NULL COMMENT '对外立项时间',
  `commence_time` date DEFAULT NULL COMMENT '开工时间',
  `product_time` date DEFAULT NULL COMMENT '投产时间',
  `main_env_facilities` text COMMENT '主要环保设施',
  `remark` text COMMENT '备注',
  PRIMARY KEY (`project_id`),
  INDEX `ind_ent_code`(`ent_code`)
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '企业环评环保管理-项目' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for t_env_mange_relate
-- ----------------------------
DROP TABLE IF EXISTS `t_env_mange_relate`;
CREATE TABLE `t_env_mange_relate` (
  `project_id` varchar(26) NOT NULL COMMENT '环评环保管理-项目主键id',
  `relate_type` tinyint NOT NULL COMMENT '关联类型，1环评、2环保验收',
  `relate_id` varchar(26) NOT NULL COMMENT '环评环保管理-关联主键id',
  PRIMARY KEY (`project_id`, `relate_type`, `relate_id`)
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '企业环评环保管理-项目和环评、环保验收的关联表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for t_env_manual_check_plan
-- ----------------------------
DROP TABLE IF EXISTS `t_env_manual_check_plan`;
CREATE TABLE `t_env_manual_check_plan` (
  `out_put_poll_id` varchar(26) NOT NULL COMMENT '排口污染物主键id',
  `execution_standard` varchar(255) DEFAULT NULL COMMENT '执行标准',
  `first_date` date DEFAULT NULL COMMENT '计划首次执行时间',
  `check_frequency` varchar(20) DEFAULT NULL COMMENT '计划检测频次（1日次、2周次、3月次、4季度、5半年、6年、7两年）',
  `plan_desc` text COMMENT '检测计划描述',
  `submit_content` text COMMENT '提交审核内容',
  `approval_opinion` text COMMENT '审批意见',
  `status` tinyint DEFAULT 1 COMMENT '计划状态：0-已取消，1-草稿，2-待审批，3-已审批，4-已完成，5-已终止',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`out_put_poll_id`),
  INDEX `ind_first_date`(`first_date`)
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '环境手工检测计划表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for t_env_manual_check_task
-- ----------------------------
DROP TABLE IF EXISTS `t_env_manual_check_task`;
CREATE TABLE `t_env_manual_check_task` (
  `task_id` varchar(26) NOT NULL COMMENT '检测任务主键id',
  `out_put_poll_id` varchar(26) DEFAULT NULL COMMENT '排口污染物主键id',
  `task_date` date DEFAULT NULL COMMENT '任务日期',
  `check_frequency` tinyint DEFAULT NULL COMMENT '计划检测频次，参考CheckFrequencyType',
  `monitor_unit` varchar(26) DEFAULT NULL COMMENT '检测单位（第三方单位）',
  `task_desc` text COMMENT '任务描述',
  `report_name` varchar(255) DEFAULT NULL COMMENT '报告名称',
  `report_code` varchar(64) DEFAULT NULL COMMENT '报告编号',
  `monitor_category` varchar(255) DEFAULT NULL COMMENT '监测类别',
  `sample_date` date DEFAULT NULL COMMENT '采样日期',
  `report_date` date DEFAULT NULL COMMENT '报告日期',
  `analysis_start_date` date DEFAULT NULL COMMENT '分析/监测周期-开始时间',
  `analysis_end_date` date DEFAULT NULL COMMENT '分析/监测周期-结束时间',
  `analysis_person` varchar(100) DEFAULT NULL COMMENT '分析/监测人员',
  `sampling_person` varchar(100) DEFAULT NULL COMMENT '采样人员',
  `operation_hour` int DEFAULT NULL COMMENT '本期运行时间(h)',
  `contact_person` varchar(100) DEFAULT NULL COMMENT '联系人',
  `contact_phone` varchar(20) DEFAULT NULL COMMENT '联系电话',
  `status` tinyint DEFAULT 1 COMMENT '任务状态：0-已取消，1-待下发，2-待执行，3-已完成',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`task_id`),
  INDEX `idx_out_put_poll`(`out_put_poll_id`),
  INDEX `idx_task_date`(`task_date`)
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '环境手工检测任务表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for t_env_policy_regulation
-- ----------------------------
DROP TABLE IF EXISTS `t_env_policy_regulation`;
CREATE TABLE `t_env_policy_regulation` (
  `policy_id` varchar(26) NOT NULL COMMENT '主键id',
  `ent_code` varchar(26) DEFAULT NULL COMMENT '所属企业',
  `policy_name` varchar(255) DEFAULT NULL COMMENT '政策法规名称',
  `policy_code` varchar(50) DEFAULT NULL COMMENT '政策法规编号（文号）',
  `issue_dept` varchar(255) DEFAULT NULL COMMENT '发行部门',
  `region` varchar(30) DEFAULT NULL COMMENT '发布地区（地区选择）',
  `policy_type` varchar(30) DEFAULT NULL COMMENT '法规类型，字典regulatory_type',
  `industry_category` text COMMENT '行业类别',
  `env_element` text COMMENT '环保要素，字典env_element（多选）',
  `mange_element` text COMMENT '管理要素，字典mange_element（多选）',
  `significance` varchar(30) DEFAULT NULL COMMENT '重要程度，字典sys_significance',
  `publish_date` date DEFAULT NULL COMMENT '发布日期',
  `implement_date` date DEFAULT NULL COMMENT '实施日期',
  `status` tinyint DEFAULT 1 COMMENT '状态：0-作废，1-有效，2-修订',
  `applicable_scope` text COMMENT '适用范围',
  `main_content` text COMMENT '主要内容',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`policy_id`),
  INDEX `ind_ent_code`(`ent_code`)
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '环境政策法规信息表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for t_env_pro_person
-- ----------------------------
DROP TABLE IF EXISTS `t_env_pro_person`;
CREATE TABLE `t_env_pro_person` (
  `pro_person_id` varchar(26) NOT NULL COMMENT '环保人员主键id',
  `ent_code` varchar(40) NOT NULL COMMENT '企业编码',
  `pro_name` varchar(32) DEFAULT NULL COMMENT '环保人员姓名',
  `dept_id` bigint DEFAULT NULL COMMENT '部门id',
  `pro_code` varchar(50) DEFAULT NULL COMMENT '环保人员编号',
  `pro_sex` tinyint DEFAULT NULL COMMENT '性别',
  `birth_date` date DEFAULT NULL COMMENT '出生日期',
  `tel_phone` varchar(20) DEFAULT NULL COMMENT '联系电话',
  `pro_title` varchar(255) DEFAULT NULL COMMENT '职称/证书',
  `pro_post` varchar(255) DEFAULT NULL COMMENT '岗位',
  `address` varchar(255) DEFAULT NULL COMMENT '住址',
  `entry_date` date DEFAULT NULL COMMENT '入职时间',
  `resign_date` date DEFAULT NULL COMMENT '离职时间',
  `remark` text COMMENT '备注',
  PRIMARY KEY (`pro_person_id`),
  INDEX `ind_ent_code`(`ent_code`)
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '企业环保人员表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for t_ext_unit_lib
-- ----------------------------
DROP TABLE IF EXISTS `t_ext_unit_lib`;
CREATE TABLE `t_ext_unit_lib` (
  `unit_code` varchar(50) NOT NULL COMMENT '统一社会信用代码',
  `unit_name` varchar(255) NOT NULL COMMENT '单位名称',
  `contact_person` varchar(100) DEFAULT NULL COMMENT '联系人',
  `contact_number` varchar(20) DEFAULT NULL COMMENT '联系电话',
  `social_num` int DEFAULT 0 COMMENT '缴纳社保人数',
  `register_addr` text COMMENT '注册地址',
  `cert_start` date DEFAULT NULL COMMENT '资质开始日期',
  `cert_end` date DEFAULT NULL COMMENT '资质结束日期',
  `serv_item` text COMMENT '服务内容',
  `extra_info` json NULL COMMENT '扩展信息',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`unit_code`),
  INDEX `ind_unit_name`(`unit_name`)
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '第三方单位库表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for t_ext_unit_user
-- ----------------------------
DROP TABLE IF EXISTS `t_ext_unit_user`;
CREATE TABLE `t_ext_unit_user` (
  `user_id` bigint NOT NULL COMMENT '用户ID',
  `unit_code` varchar(50) NOT NULL COMMENT '第三方信用代码',
  PRIMARY KEY (`user_id`, `unit_code`)
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '用户和第三方单位关联表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for t_govern_fund_support
-- ----------------------------
DROP TABLE IF EXISTS `t_govern_fund_support`;
CREATE TABLE `t_govern_fund_support` (
  `f_support_id` varchar(26) NOT NULL COMMENT '政府资金支持-主键id',
  `ent_code` varchar(40) DEFAULT NULL COMMENT '企业编码',
  `project_name` varchar(255) DEFAULT NULL COMMENT '项目名称',
  `investment_amount` double DEFAULT NULL COMMENT '投资金额(万)',
  `dept_id` bigint DEFAULT NULL COMMENT '部门id（项目负责单位）',
  `pro_person_id` varchar(26) DEFAULT NULL COMMENT '环保人员主键id（项目负责人）',
  `project_content` text COMMENT '项目内容',
  `reduce_effect` text COMMENT '减排效果',
  `eps_time` date DEFAULT NULL COMMENT '外部立项-计划时间',
  `epa_time` date DEFAULT NULL COMMENT '外部立项-实际时间',
  `pcs_time` date DEFAULT NULL COMMENT '项目完成-计划时间',
  `pca_time` date DEFAULT NULL COMMENT '项目完成-实际时间',
  `support_amount` double DEFAULT NULL COMMENT '政府资金支持金额(万)',
  `send_dept` varchar(255) DEFAULT NULL COMMENT '下发支持资金部门',
  PRIMARY KEY (`f_support_id`),
  INDEX `ind_ent_code`(`ent_code`)
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '政府资金支持' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for t_govern_fund_support_batch_actual
-- ----------------------------
DROP TABLE IF EXISTS `t_govern_fund_support_batch_actual`;
CREATE TABLE `t_govern_fund_support_batch_actual` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键id',
  `f_support_id` varchar(26) DEFAULT NULL COMMENT '政府资金支持-主键id',
  `actual_time` datetime DEFAULT NULL COMMENT '实际到账时间',
  `actual_amount` double DEFAULT NULL COMMENT '实际到账金额(万)',
  PRIMARY KEY (`id`),
  INDEX `ind_ent_code`(`f_support_id`)
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '政府资金支持-实际批次' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for t_govern_fund_support_batch_plan
-- ----------------------------
DROP TABLE IF EXISTS `t_govern_fund_support_batch_plan`;
CREATE TABLE `t_govern_fund_support_batch_plan` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键id',
  `f_support_id` varchar(26) DEFAULT NULL COMMENT '政府资金支持-主键id',
  `plan_name` varchar(255) DEFAULT NULL COMMENT '计划批次名称',
  `plan_rate` int DEFAULT NULL COMMENT '计划批次比例',
  `send_time` date DEFAULT NULL COMMENT '计划下发时间',
  PRIMARY KEY (`id`),
  INDEX `ind_ent_code`(`f_support_id`)
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '政府资金支持-计划批次' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for t_ops_att_record
-- ----------------------------
DROP TABLE IF EXISTS `t_ops_att_record`;
CREATE TABLE `t_ops_att_record` (
  `record_id` varchar(32) NOT NULL COMMENT '记录ID',
  `user_id` bigint DEFAULT NULL COMMENT '用户ID',
  `ops_unit_id` varchar(26) DEFAULT NULL COMMENT '运维单位（环境技术服务机构（第三方单位））',
  `ent_code` varchar(26) DEFAULT NULL COMMENT '企业编码',
  `out_put_id` varchar(26) DEFAULT NULL COMMENT '排放口ID',
  `punch_time_in` datetime DEFAULT NULL COMMENT '签到时间（点击按钮自动生成）',
  `punch_location_in` text COMMENT '签到定位信息（点击按钮时获取具体位置）',
  `punch_time_out` datetime DEFAULT NULL COMMENT '签退时间（点击按钮自动生成）',
  `punch_location_out` text COMMENT '签退定位信息（点击按钮时获取具体位置）',
  `punch_duration` bigint DEFAULT NULL COMMENT '打卡时长（分钟）',
  `assistant` bigint DEFAULT NULL COMMENT '协助人',
  `assistant_remark` text COMMENT '协助说明',
  PRIMARY KEY (`record_id`),
  INDEX `idx_user_id`(`user_id`),
  INDEX `idx_ent_code`(`ent_code`, `out_put_id`),
  INDEX `idx_punch_time`(`punch_time_in`)
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '考勤打卡记录表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for t_ops_record
-- ----------------------------
DROP TABLE IF EXISTS `t_ops_record`;
CREATE TABLE `t_ops_record` (
  `record_id` varchar(26) NOT NULL COMMENT '运维记录ID（主键）',
  `ent_code` varchar(26) NOT NULL COMMENT '企业编码',
  `out_put_id` varchar(26) NOT NULL COMMENT '排放口ID',
  `template_code` varchar(50) NOT NULL COMMENT '关联的运维类型编码',
  `ops_unit_id` varchar(26) DEFAULT NULL COMMENT '运维单位（环境技术服务机构（第三方单位））',
  `record_date` datetime DEFAULT NULL COMMENT '运维时间',
  `ops_user_id` bigint DEFAULT NULL COMMENT '运维人员',
  `qualified_flag` tinyint DEFAULT NULL COMMENT '是否合格（1：合格，0：不合格）',
  `review_status` tinyint DEFAULT 1 COMMENT '运维审批状态：1-草稿(已添加)、2-已提交、3-审核中、4-审核通过',
  `record_array` json NULL COMMENT '运维记录项',
  `create_by` varchar(64) DEFAULT NULL COMMENT '创建人',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_by` varchar(64) DEFAULT NULL COMMENT '更新人',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`record_id`),
  INDEX `idx_template_code`(`template_code`),
  INDEX `idx_record_date`(`record_date`),
  INDEX `idx_ops_user`(`ops_user_id`),
  INDEX `idx_ent_out_put`(`ent_code`, `out_put_id`, `template_code`)
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '运维记录表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for t_ops_task
-- ----------------------------
DROP TABLE IF EXISTS `t_ops_task`;
CREATE TABLE `t_ops_task` (
  `task_id` varchar(26) NOT NULL COMMENT '运维任务ID',
  `ent_code` varchar(26) NOT NULL COMMENT '企业编码',
  `out_put_id` varchar(26) NOT NULL COMMENT '排放口ID',
  `template_code` varchar(50) NOT NULL COMMENT '运维类型编码',
  `task_type` tinyint DEFAULT NULL COMMENT '任务类型（1：自动生成，2：手动创建）',
  `task_status` tinyint DEFAULT 0 COMMENT '任务状态（0：待分配，1：待执行，2：已取消，3：执行失败，4：已完成）',
  `task_desc` text COMMENT '任务执行情况描述',
  `plan_date` date DEFAULT NULL COMMENT '计划执行日期',
  `early_days` int DEFAULT NULL COMMENT '提前推送提醒(天)',
  `operator` bigint DEFAULT NULL COMMENT '执行人',
  `assign_time` datetime DEFAULT NULL COMMENT '分配时间',
  `create_id` bigint DEFAULT NULL COMMENT '创建人',
  `remark` text COMMENT '备注',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`task_id`),
  INDEX `idx_ent_out_put`(`ent_code`, `out_put_id`, `template_code`),
  INDEX `idx_template_code`(`template_code`),
  INDEX `idx_task_status`(`task_status`),
  INDEX `idx_plan_date`(`plan_date`),
  INDEX `idx_operator`(`operator`)
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '运维任务表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for t_ops_task_conf
-- ----------------------------
DROP TABLE IF EXISTS `t_ops_task_conf`;
CREATE TABLE `t_ops_task_conf` (
  `ent_code` varchar(26) NOT NULL COMMENT '企业编码',
  `out_put_id` varchar(26) NOT NULL COMMENT '排放口ID',
  `template_code` varchar(50) NOT NULL COMMENT '运维类型编码',
  `enabled` tinyint DEFAULT 1 COMMENT '是否启动任务（1：启动，0：不启动）',
  `begin_date` date DEFAULT NULL COMMENT '任务开始时间',
  `cycle_type` tinyint DEFAULT NULL COMMENT '执行周期类型（1：日，2：周，3：月，4：季度，6：年）',
  `cycle_value` int DEFAULT NULL COMMENT '周期数值（如：每3天，每2周等）',
  `early_days` int DEFAULT NULL COMMENT '提前推送提醒(天)',
  `create_id` bigint DEFAULT NULL COMMENT '创建人',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`ent_code`, `out_put_id`, `template_code`),
  INDEX `idx_template_code`(`template_code`)
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '运维任务配置表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for t_ops_task_stat
-- ----------------------------
DROP TABLE IF EXISTS `t_ops_task_stat`;
CREATE TABLE `t_ops_task_stat` (
  `stat_id` varchar(26) NOT NULL COMMENT '统计ID',
  `ent_code` varchar(26) NOT NULL COMMENT '企业编码',
  `out_put_id` varchar(26) NOT NULL COMMENT '排放口ID',
  `template_code` varchar(50) NOT NULL COMMENT '运维类型编码',
  `operator` bigint NOT NULL COMMENT '执行人',
  `task_stat_date` date NOT NULL COMMENT '统计日期',
  `auto_tasks` int DEFAULT 0 COMMENT '自动生成任务数',
  `manual_tasks` int DEFAULT 0 COMMENT '手动生成任务数',
  `completed_tasks` int DEFAULT 0 COMMENT '已完成任务数',
  `qualified_tasks` int DEFAULT 0 COMMENT '合格任务数',
  `unqualified_tasks` int DEFAULT 0 COMMENT '不合格任务数',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`stat_id`),
  INDEX `idx_ent_out_put`(`ent_code`, `out_put_id`),
  INDEX `idx_template_code`(`template_code`),
  INDEX `idx_operator`(`operator`),
  INDEX `idx_task_stat_date`(`task_stat_date`)
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '运维任务表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for t_ops_template
-- ----------------------------
DROP TABLE IF EXISTS `t_ops_template`;
CREATE TABLE `t_ops_template` (
  `ent_code` varchar(26) NOT NULL COMMENT '关联企业id，为-1时表示公共的配置',
  `out_put_id` varchar(26) NOT NULL COMMENT '关联排口主键id，企业code为-1时，表示排口类型，1废水、2废气...',
  `template_code` varchar(50) NOT NULL COMMENT '运维类型编码',
  `template_name` varchar(100) NOT NULL COMMENT '运维类型名称（如：设备巡检、排放口维护等）',
  `show_attachment_flag` tinyint DEFAULT 0 COMMENT '显示附件标记（1：需要，0：不需要）',
  `show_qualified_flag` tinyint DEFAULT 0 COMMENT '显示是否合格标志（1：需要，0：不需要）',
  `prompt_text` varchar(500) DEFAULT NULL COMMENT '提示语，为空时不显示',
  `item_array` json NULL COMMENT '运维配置项',
  `remark` varchar(500) DEFAULT NULL COMMENT '模板的备注说明',
  `create_by` varchar(64) DEFAULT NULL COMMENT '创建人',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_by` varchar(64) DEFAULT NULL COMMENT '更新人',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`ent_code`, `out_put_id`, `template_code`),
  INDEX `idx_template_code`(`template_code`)
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '运维模板配置表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for t_other_certificate
-- ----------------------------
DROP TABLE IF EXISTS `t_other_certificate`;
CREATE TABLE `t_other_certificate` (
  `other_id` varchar(26) NOT NULL COMMENT '其他证书主键id',
  `ent_code` varchar(26) DEFAULT NULL COMMENT '所属企业编码',
  `cert_name` varchar(255) DEFAULT NULL COMMENT '证书名称',
  `issue_office` varchar(255) DEFAULT NULL COMMENT '发证机构',
  `cert_belong` varchar(255) DEFAULT NULL COMMENT '归属',
  `belong_type` tinyint DEFAULT NULL COMMENT '归属类型，1机构，2个人',
  `begin_date` date DEFAULT NULL COMMENT '有效期-开始时间',
  `end_date` date DEFAULT NULL COMMENT '有效期-结束时间',
  `issue_date` date DEFAULT NULL COMMENT '发证日期',
  `remark` text COMMENT '备注',
  PRIMARY KEY (`other_id`)
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '其他证书表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for t_valid_period_conf
-- ----------------------------
DROP TABLE IF EXISTS `t_valid_period_conf`;
CREATE TABLE `t_valid_period_conf` (
  `ent_code` varchar(26) NOT NULL COMMENT '企业唯一编码',
  `conf_type` tinyint NOT NULL COMMENT '有效期类型',
  `yellow_threshold` smallint UNSIGNED NOT NULL DEFAULT 30 COMMENT '黄色提醒阈值(剩余天数≤30天触发)',
  `yellow_notify_freq` char(3) NOT NULL DEFAULT 'D05' COMMENT '黄色提醒频率格式:D-天/M-月/H-小时+数字,如D10=每10天1次',
  `orange_threshold` smallint UNSIGNED NOT NULL DEFAULT 10 COMMENT '橙色预警阈值(剩余天数≤10天触发)',
  `orange_notify_freq` char(3) NOT NULL DEFAULT 'D01' COMMENT '橙色预警频率(每天1次)',
  `red_threshold` smallint UNSIGNED NOT NULL DEFAULT 3 COMMENT '红色报警阈值(剩余天数≤3天触发)',
  `red_notify_freq` char(3) NOT NULL DEFAULT 'H03' COMMENT '红色报警频率(每3小时1次)',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '最后更新时间',
  PRIMARY KEY (`ent_code`, `conf_type`)
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '企业有效期预报警配置表(四色预警机制，频率格式统一为两位数字)' ROW_FORMAT = Dynamic;

SET FOREIGN_KEY_CHECKS = 1;
