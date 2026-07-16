SET REFERENTIAL_INTEGRITY FALSE;

-- datasource
INSERT INTO datasource (id, name, type, jdbc_url, username, password, status, last_collect_time, table_count, create_time) VALUES
(1, 'MySQL-生产库', 'MYSQL', 'jdbc:mysql://192.168.1.10:3306/prod_db', 'admin', '******', 'ACTIVE', '2024-06-15 10:30:00', 128, '2024-01-10 08:00:00'),
(2, 'Hive-数据仓库', 'HIVE', 'jdbc:hive2://192.168.1.20:10000/default', 'hive', '******', 'ACTIVE', '2024-06-14 18:00:00', 456, '2024-02-20 09:00:00'),
(3, 'PostgreSQL-分析库', 'POSTGRESQL', 'jdbc:postgresql://192.168.1.30:5432/analytics', 'pguser', '******', 'ACTIVE', '2024-06-15 08:00:00', 89, '2024-03-05 10:00:00'),
(4, 'REST-用户服务', 'REST', 'http://api.user-service.internal/v1', '', '', 'INACTIVE', '2024-05-01 12:00:00', 0, '2024-04-01 14:00:00');

-- metadata_table
INSERT INTO metadata_table (id, datasource_id, db_name, table_name, table_comment, row_count, size_bytes, owner, tags, quality_score, security_level, create_time, update_time) VALUES
(1, 1, 'prod_db', 'users', '用户基础信息表', 1250000, 268435456, '张三', '核心,PII', 92.5, 'L2', '2024-01-10 08:00:00', '2024-06-15 10:30:00'),
(2, 1, 'prod_db', 'orders', '订单主表', 5600000, 1073741824, '李四', '核心,交易', 88.0, 'L2', '2024-01-10 08:00:00', '2024-06-15 10:30:00'),
(3, 1, 'prod_db', 'order_items', '订单明细表', 15600000, 2147483648, '李四', '核心,交易', 85.5, 'L2', '2024-01-10 08:00:00', '2024-06-15 10:30:00'),
(4, 2, 'default', 'dwd_traffic_log', '流量日志明细宽表', 890000000, 549755813888, '王五', '日志,流量', 78.0, 'L1', '2024-02-20 09:00:00', '2024-06-14 18:00:00'),
(5, 2, 'default', 'dws_user_active_day', '用户日活跃汇总表', 36500000, 10737418240, '王五', '汇总,用户', 91.0, 'L1', '2024-02-20 09:00:00', '2024-06-14 18:00:00'),
(6, 3, 'analytics', 'ads_revenue_monthly', '月度收入分析表', 36, 1048576, '赵六', '分析,财务', 95.0, 'L3', '2024-03-05 10:00:00', '2024-06-15 08:00:00');

-- metadata_field
INSERT INTO metadata_field (id, table_id, field_name, data_type, comment, business_meaning, is_primary, is_sensitive) VALUES
(1, 1, 'id', 'BIGINT', '主键ID', '用户唯一标识', 1, 0),
(2, 1, 'username', 'VARCHAR(64)', '用户名', '用户登录名', 0, 0),
(3, 1, 'email', 'VARCHAR(128)', '邮箱', '用户联系邮箱', 0, 1),
(4, 1, 'phone', 'VARCHAR(20)', '手机号', '用户手机号码', 0, 1),
(5, 1, 'id_card', 'VARCHAR(18)', '身份证号', '用户实名证件号', 0, 1),
(6, 1, 'create_time', 'DATETIME', '创建时间', '记录创建时间', 0, 0),
(7, 2, 'order_id', 'BIGINT', '订单ID', '订单唯一标识', 1, 0),
(8, 2, 'user_id', 'BIGINT', '用户ID', '关联用户', 0, 0),
(9, 2, 'amount', 'DECIMAL(16,2)', '订单金额', '订单总金额', 0, 0),
(10, 2, 'status', 'TINYINT', '订单状态', '订单生命周期状态', 0, 0),
(11, 2, 'create_time', 'DATETIME', '创建时间', '订单创建时间', 0, 0),
(12, 4, 'event_time', 'TIMESTAMP', '事件时间', '日志发生时间', 0, 0),
(13, 4, 'user_id', 'STRING', '用户ID', '匿名用户标识', 0, 0),
(14, 4, 'page_url', 'STRING', '页面URL', '访问页面地址', 0, 0),
(15, 4, 'ip', 'STRING', 'IP地址', '用户来源IP', 0, 1);

-- table_change
INSERT INTO table_change (id, table_id, change_type, change_detail, version, change_time, operator) VALUES
(1, 1, 'SCHEMA', '新增字段: last_login_time DATETIME', 5, '2024-06-10 14:30:00', '张三'),
(2, 1, 'COMMENT', '更新表注释: 用户基础信息表（含实名信息）', 4, '2024-05-20 09:00:00', '张三'),
(3, 2, 'SCHEMA', '新增索引: idx_status_create_time', 8, '2024-06-12 11:00:00', '李四'),
(4, 4, 'DDL', '分区调整为按天分区', 3, '2024-06-01 08:00:00', '王五');

-- quality_rule
INSERT INTO quality_rule (id, name, dimension, rule_type, rule_sql, target_table, severity, enabled, last_score, create_time) VALUES
(1, '用户表主键唯一性', 'UNIQUENESS', 'SQL', 'SELECT COUNT(*) FROM users GROUP BY id HAVING COUNT(*) > 1', 'users', 1, true, 100.0, '2024-01-15 10:00:00'),
(2, '订单金额非空检查', 'COMPLETENESS', 'SQL', 'SELECT COUNT(*) FROM orders WHERE amount IS NULL', 'orders', 2, true, 99.8, '2024-01-20 11:00:00'),
(3, '邮箱格式校验', 'VALIDITY', 'TEMPLATE', NULL, 'users', 2, true, 96.5, '2024-02-01 09:00:00'),
(4, '流量日志分区连续性', 'TIMELINESS', 'SQL', 'SELECT COUNT(DISTINCT dt) FROM dwd_traffic_log WHERE dt >= CURRENT_DATE - 7', 'dwd_traffic_log', 1, true, 100.0, '2024-03-10 14:00:00'),
(5, '手机号长度检查', 'VALIDITY', 'SQL', 'SELECT COUNT(*) FROM users WHERE LENGTH(phone) != 11', 'users', 2, false, 0.0, '2024-04-01 08:00:00');

-- quality_ticket
INSERT INTO quality_ticket (id, result_id, title, status, assignee, priority, rule_name, description, create_time, update_time) VALUES
(1, 2, '订单金额存在NULL值', 'OPEN', '李四', 'HIGH', '订单金额非空检查', '最近一轮质量监控发现 orders 表有 120 条记录 amount 字段为 NULL', '2024-06-14 09:00:00', '2024-06-14 09:00:00'),
(2, 3, '邮箱格式异常', 'PROCESSING', '张三', 'MEDIUM', '邮箱格式校验', 'users 表发现 350 条邮箱格式不合法，需清洗', '2024-06-13 10:00:00', '2024-06-14 15:00:00'),
(3, 1, '用户主键重复', 'RESOLVED', '张三', 'HIGH', '用户表主键唯一性', '历史数据迁移导致主键冲突，已修复', '2024-06-01 08:00:00', '2024-06-05 16:00:00');

-- asset_catalog
INSERT INTO asset_catalog (id, table_id, name, description, catalog_path, rating, view_count, fav_count, owner, tags, type, update_time) VALUES
(1, 1, '用户基础信息', '包含用户注册、实名、联系方式等核心信息', '/用户数据/基础信息', 4.8, 1250, 86, '张三', '核心,PII,用户', 'TABLE', '2024-06-15 10:00:00'),
(2, 2, '订单主数据', '电商交易订单主表，涵盖所有业务线订单', '/交易数据/订单', 4.5, 980, 62, '李四', '核心,交易,财务', 'TABLE', '2024-06-15 10:00:00'),
(3, 4, '流量日志宽表', 'DWD层流量日志明细，已关联用户、页面、设备维度', '/流量数据/日志', 4.2, 756, 45, '王五', '日志,流量,分析', 'TABLE', '2024-06-14 18:00:00'),
(4, NULL, '月度营收报表', '财务部门月度收入分析报表', '/报表/财务', 4.6, 543, 38, '赵六', '财务,分析,管理', 'REPORT', '2024-06-10 09:00:00'),
(5, NULL, '用户画像API', '提供用户标签和画像查询接口', '/API/用户服务', 4.3, 432, 27, '张三', 'API,用户,画像', 'API', '2024-06-08 14:00:00');

-- asset_tag
INSERT INTO asset_tag (id, name, category, color) VALUES
(1, '核心', '重要性', '#FF4D4F'),
(2, 'PII', '敏感类型', '#FAAD14'),
(3, '交易', '业务域', '#52C41A'),
(4, '日志', '数据类型', '#1890FF'),
(5, '财务', '业务域', '#722ED1'),
(6, '用户', '业务域', '#13C2C2'),
(7, '分析', '用途', '#EB2F96'),
(8, 'API', '资产类型', '#2F4554');

-- classify_label
INSERT INTO classify_label (id, name, level, rules, field_count) VALUES
(1, '个人身份信息', 'L1', '包含姓名、身份证号、护照号等', 5),
(2, '联系信息', 'L2', '包含手机号、邮箱、地址等', 3),
(3, '金融信息', 'L3', '包含银行卡号、交易金额、信用评分等', 4),
(4, '行为日志', 'L4', '包含浏览记录、点击流、搜索记录等', 8);

-- mask_policy
INSERT INTO mask_policy (id, table_id, table_name, field_name, mask_type, expression, preview) VALUES
(1, 1, 'users', 'phone', 'MASK', 'CONCAT(LEFT(phone,3),"****",RIGHT(phone,4))', '138****8888'),
(2, 1, 'users', 'email', 'MASK', 'CONCAT(LEFT(email,2),"****",SUBSTRING_INDEX(email,"@",-1))', 'ab****@example.com'),
(3, 1, 'users', 'id_card', 'REPLACE', 'REPLACE(id_card,SUBSTRING(id_card,7,8),"********")', '110101********1234'),
(4, 4, 'dwd_traffic_log', 'ip', 'MASK', 'CONCAT(SUBSTRING_INDEX(ip,".",2),".*.*")', '192.168.*.*');

-- audit_log
INSERT INTO audit_log (id, audit_user, operation, target, target_level, ip, status, audit_time) VALUES
(1, 'admin', 'QUERY', 'users', 'TABLE', '10.0.0.5', 'SUCCESS', '2024-06-15 09:30:00'),
(2, 'zhangsan', 'EXPORT', 'orders', 'TABLE', '10.0.0.12', 'SUCCESS', '2024-06-15 10:00:00'),
(3, 'lisi', 'QUERY', 'dwd_traffic_log', 'TABLE', '10.0.0.15', 'DENIED', '2024-06-15 10:15:00'),
(4, 'wangwu', 'MODIFY', 'mask_policy', 'POLICY', '10.0.0.20', 'SUCCESS', '2024-06-14 16:00:00'),
(5, 'admin', 'DELETE', 'tmp_table_20240601', 'TABLE', '10.0.0.5', 'SUCCESS', '2024-06-14 08:00:00');

-- lifecycle_policy
INSERT INTO lifecycle_policy (id, table_id, table_name, stage, retain_days, action, size_bytes) VALUES
(1, 4, 'dwd_traffic_log', 'HOT', 7, 'KEEP', 549755813888),
(2, 4, 'dwd_traffic_log', 'WARM', 30, 'COMPRESS', 549755813888),
(3, 4, 'dwd_traffic_log', 'COLD', 90, 'ARCHIVE', 549755813888),
(4, 4, 'dwd_traffic_log', 'DELETE', 365, 'DROP', 549755813888),
(5, 5, 'dws_user_active_day', 'HOT', 30, 'KEEP', 10737418240),
(6, 5, 'dws_user_active_day', 'WARM', 180, 'COMPRESS', 10737418240),
(7, 5, 'dws_user_active_day', 'COLD', 730, 'ARCHIVE', 10737418240);

-- workflow_task
INSERT INTO workflow_task (id, title, type, status, assignee, priority, due_date, description, create_time) VALUES
(1, '修复用户表主键重复', 'QUALITY', 'DOING', '张三', 'HIGH', '2024-06-18 18:00:00', '历史迁移数据导致 users 表主键重复，需清洗修复', '2024-06-01 09:00:00'),
(2, '审核流量日志脱敏策略', 'SECURITY', 'TODO', '王五', 'MEDIUM', '2024-06-20 18:00:00', '新增 IP 字段脱敏策略，需安全团队审核', '2024-06-14 10:00:00'),
(3, '发布订单金额非空约束', 'STANDARD', 'DONE', '李四', 'HIGH', '2024-06-10 18:00:00', '在数据标准中增加 orders.amount 非空约束并发布', '2024-06-05 08:00:00'),
(4, '数据资产目录归档审批', 'APPROVAL', 'TODO', '赵六', 'LOW', '2024-06-25 18:00:00', '对 2023 年前不再维护的数据资产进行归档', '2024-06-12 14:00:00'),
(5, '新增手机号长度校验规则', 'QUALITY', 'TODO', '张三', 'MEDIUM', '2024-06-22 18:00:00', '数据质量规则：users.phone 长度必须为 11 位', '2024-06-13 11:00:00');

-- data_standard
INSERT INTO data_standard (id, name, category, data_type, length, business_meaning, version, status, mapping_count, compliance_rate) VALUES
(1, '用户手机号', '基础属性', 'VARCHAR', 11, '中国大陆手机号码，11位数字', 'v1.2', 'PUBLISHED', 8, 98.5),
(2, '身份证号', '基础属性', 'VARCHAR', 18, '中国公民身份证号码，18位字符', 'v1.0', 'PUBLISHED', 5, 99.2),
(3, '订单金额', '交易属性', 'DECIMAL', 16, '订单总金额，保留2位小数', 'v2.1', 'PUBLISHED', 4, 99.8),
(4, '事件时间', '时间属性', 'TIMESTAMP', 0, '数据记录发生的时间戳', 'v1.1', 'PUBLISHED', 12, 95.0),
(5, '用户标签', '用户属性', 'STRING', 256, '用户画像标签，多个标签以逗号分隔', 'v0.9', 'DRAFT', 3, 0.0),
(6, '信用评分', '金融属性', 'INT', 0, '用户信用评估分数，范围 350-950', 'v1.3', 'DEPRECATED', 2, 0.0);

SET REFERENTIAL_INTEGRITY TRUE;
