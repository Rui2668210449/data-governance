import type {
  Datasource,
  MetadataTable,
  MetadataField,
  TableChange,
  QualityRule,
  QualityTicket,
  AssetCatalog,
  AssetTag,
  ClassifyLabel,
  MaskPolicy,
  AuditLog,
  LifecyclePolicy,
  WorkflowTask,
  DataStandard,
  DashboardStats,
  LineageNode,
  LineageEdge,
} from '@/types'

// 仪表盘统计
export const dashboardStats: DashboardStats = {
  datasourceCount: 12,
  tableCount: 3486,
  fieldCount: 48235,
  qualityScore: 92.6,
  todoCount: 8,
  issueCount: 23,
  qualityScoreTrend: [
    { date: '07-01', score: 88.2 },
    { date: '07-02', score: 89.5 },
    { date: '07-03', score: 90.1 },
    { date: '07-04', score: 89.8 },
    { date: '07-05', score: 91.3 },
    { date: '07-06', score: 91.9 },
    { date: '07-07', score: 92.6 },
  ],
  datasourceDistribution: [
    { name: 'MySQL', value: 1456 },
    { name: 'Hive', value: 1280 },
    { name: 'PostgreSQL', value: 520 },
    { name: 'REST API', value: 230 },
  ],
  tableGrowth: [
    { date: '06月', count: 3100 },
    { date: '07月', count: 3486 },
  ],
}

// 数据源
export const datasources: Datasource[] = [
  { id: 1, name: '业务核心库', type: 'MYSQL', jdbcUrl: 'jdbc:mysql://10.0.1.10:3306/biz_core', username: 'datagov', status: 'ACTIVE', lastCollectTime: '2026-07-16 08:30:00', tableCount: 1456, createTime: '2026-01-15 10:00:00' },
  { id: 2, name: '数仓ODS层', type: 'HIVE', jdbcUrl: 'jdbc:hive2://10.0.1.20:10000/ods', username: 'hive_etl', status: 'ACTIVE', lastCollectTime: '2026-07-16 07:15:00', tableCount: 1280, createTime: '2026-01-20 14:00:00' },
  { id: 3, name: '分析报表库', type: 'POSTGRESQL', jdbcUrl: 'jdbc:postgresql://10.0.1.30:5432/report', username: 'report_user', status: 'ACTIVE', lastCollectTime: '2026-07-16 06:00:00', tableCount: 520, createTime: '2026-02-01 09:00:00' },
  { id: 4, name: '外部API数据', type: 'REST', jdbcUrl: 'https://api.example.com/v1', username: 'api_client', status: 'INACTIVE', lastCollectTime: '2026-07-14 22:00:00', tableCount: 230, createTime: '2026-03-10 11:00:00' },
]

// 元数据表
export const metadataTables: MetadataTable[] = [
  { id: 1, datasourceId: 1, dbName: 'biz_core', tableName: 'user_info', tableComment: '用户基本信息表', rowCount: 1258000, sizeBytes: 536870912, owner: '张三', tags: ['核心资产', '用户域'], qualityScore: 96.5, securityLevel: 'L3', createTime: '2026-01-15 10:30:00', updateTime: '2026-07-15 23:00:00' },
  { id: 2, datasourceId: 1, dbName: 'biz_core', tableName: 'order_detail', tableComment: '订单明细表', rowCount: 98560000, sizeBytes: 5368709120, owner: '李四', tags: ['核心资产', '交易域'], qualityScore: 91.2, securityLevel: 'L2', createTime: '2026-01-15 10:35:00', updateTime: '2026-07-16 01:00:00' },
  { id: 3, datasourceId: 2, dbName: 'ods', tableName: 'ods_user_log_d', tableComment: '用户行为日志日表', rowCount: 256000000, sizeBytes: 10737418240, owner: '王五', tags: ['行为域', 'ODS'], qualityScore: 85.0, securityLevel: 'L1', createTime: '2026-01-20 14:30:00', updateTime: '2026-07-16 03:00:00' },
  { id: 4, datasourceId: 2, dbName: 'ods', tableName: 'ods_trade_dwd_d', tableComment: '交易域明细宽表', rowCount: 128000000, sizeBytes: 8589934592, owner: '李四', tags: ['交易域', 'DWD'], qualityScore: 93.8, securityLevel: 'L2', createTime: '2026-01-20 15:00:00', updateTime: '2026-07-16 02:30:00' },
  { id: 5, datasourceId: 3, dbName: 'report', tableName: 'rpt_user_active_m', tableComment: '月活用户报表', rowCount: 8500000, sizeBytes: 268435456, owner: '赵六', tags: ['报表', '用户域'], qualityScore: 98.0, securityLevel: 'L1', createTime: '2026-02-01 09:30:00', updateTime: '2026-07-15 18:00:00' },
]

// 字段
export const metadataFields: MetadataField[] = [
  { id: 1, tableId: 1, fieldName: 'user_id', dataType: 'BIGINT', comment: '用户ID', businessMeaning: '用户唯一标识', isPrimary: 1, isSensitive: 0 },
  { id: 2, tableId: 1, fieldName: 'user_name', dataType: 'VARCHAR(64)', comment: '用户名', businessMeaning: '用户登录名', isPrimary: 0, isSensitive: 0 },
  { id: 3, tableId: 1, fieldName: 'phone', dataType: 'VARCHAR(16)', comment: '手机号', businessMeaning: '用户手机号', isPrimary: 0, isSensitive: 1 },
  { id: 4, tableId: 1, fieldName: 'id_card', dataType: 'VARCHAR(32)', comment: '身份证号', businessMeaning: '身份证号', isPrimary: 0, isSensitive: 1 },
  { id: 5, tableId: 1, fieldName: 'email', dataType: 'VARCHAR(128)', comment: '邮箱', businessMeaning: '用户邮箱', isPrimary: 0, isSensitive: 1 },
  { id: 6, tableId: 1, fieldName: 'register_time', dataType: 'DATETIME', comment: '注册时间', businessMeaning: '用户注册时间', isPrimary: 0, isSensitive: 0 },
  { id: 7, tableId: 1, fieldName: 'status', dataType: 'TINYINT', comment: '状态', businessMeaning: '0停用 1启用', isPrimary: 0, isSensitive: 0 },
]

// 变更历史
export const tableChanges: TableChange[] = [
  { id: 1, tableId: 1, changeType: 'ADD_FIELD', changeDetail: '新增字段 email VARCHAR(128)', version: 'v1.3', changeTime: '2026-07-15 23:00:00', operator: '张三' },
  { id: 2, tableId: 1, changeType: 'MODIFY_TYPE', changeDetail: 'phone 类型 VARCHAR(11) -> VARCHAR(16)', version: 'v1.2', changeTime: '2026-06-20 14:00:00', operator: '李四' },
  { id: 3, tableId: 1, changeType: 'ADD_FIELD', changeDetail: '新增字段 status TINYINT', version: 'v1.1', changeTime: '2026-05-10 10:00:00', operator: '张三' },
  { id: 4, tableId: 1, changeType: 'ADD_FIELD', changeDetail: '初始建表，7个字段', version: 'v1.0', changeTime: '2026-01-15 10:30:00', operator: '系统' },
]

// 血缘
export const lineageNodes: LineageNode[] = [
  { id: 'ods_user_log_d', label: 'ods.ods_user_log_d', type: 'TABLE', dbName: 'ods' },
  { id: 'dwd_user_active_d', label: 'dwd.dwd_user_active_d', type: 'TABLE', dbName: 'dwd' },
  { id: 'dws_user_active_m', label: 'dws.dws_user_active_m', type: 'TABLE', dbName: 'dws' },
  { id: 'rpt_user_active_m', label: 'report.rpt_user_active_m', type: 'TABLE', dbName: 'report' },
  { id: 'user_info', label: 'biz_core.user_info', type: 'TABLE', dbName: 'biz_core' },
  { id: 'etl_task_1', label: 'ETL:用户活跃明细', type: 'TASK' },
  { id: 'etl_task_2', label: 'ETL:用户活跃汇总', type: 'TASK' },
  { id: 'etl_task_3', label: 'ETL:月活报表', type: 'TASK' },
]

export const lineageEdges: LineageEdge[] = [
  { source: 'ods_user_log_d', target: 'etl_task_1' },
  { source: 'user_info', target: 'etl_task_1' },
  { source: 'etl_task_1', target: 'dwd_user_active_d' },
  { source: 'dwd_user_active_d', target: 'etl_task_2' },
  { source: 'etl_task_2', target: 'dws_user_active_m' },
  { source: 'dws_user_active_m', target: 'etl_task_3' },
  { source: 'etl_task_3', target: 'rpt_user_active_m' },
]

// 质量规则
export const qualityRules: QualityRule[] = [
  { id: 1, name: 'user_id非空检查', dimension: '完整性', ruleType: 'TEMPLATE', ruleSql: 'SELECT COUNT(*) FROM user_info WHERE user_id IS NULL', targetTable: 'biz_core.user_info', severity: 1, enabled: true, lastScore: 100, createTime: '2026-02-10 10:00:00' },
  { id: 2, name: 'phone唯一性校验', dimension: '唯一性', ruleType: 'TEMPLATE', ruleSql: 'SELECT phone, COUNT(*) FROM user_info GROUP BY phone HAVING COUNT(*)>1', targetTable: 'biz_core.user_info', severity: 2, enabled: true, lastScore: 99.2, createTime: '2026-02-10 10:05:00' },
  { id: 3, name: '手机号格式校验', dimension: '规范性', ruleType: 'SQL', ruleSql: 'SELECT COUNT(*) FROM user_info WHERE phone NOT REGEXP "^1[3-9]\\\\d{9}$"', targetTable: 'biz_core.user_info', severity: 2, enabled: true, lastScore: 95.8, createTime: '2026-02-12 09:00:00' },
  { id: 4, name: '订单金额准确性', dimension: '准确性', ruleType: 'SQL', ruleSql: 'SELECT COUNT(*) FROM order_detail WHERE amount < 0', targetTable: 'biz_core.order_detail', severity: 1, enabled: true, lastScore: 100, createTime: '2026-03-01 14:00:00' },
  { id: 5, name: '日志时效性检查', dimension: '时效性', ruleType: 'TEMPLATE', ruleSql: 'SELECT COUNT(*) FROM ods_user_log_d WHERE dt = CURRENT_DATE', targetTable: 'ods.ods_user_log_d', severity: 3, enabled: false, lastScore: 80.0, createTime: '2026-03-15 16:00:00' },
]

// 质量工单
export const qualityTickets: QualityTicket[] = [
  { id: 1, title: 'user_info表phone字段存在重复值', status: 'PROCESSING', assignee: '张三', priority: 'HIGH', ruleName: 'phone唯一性校验', description: '检测到235条手机号重复记录，需确认是否为数据同步问题', createTime: '2026-07-15 09:00:00', updateTime: '2026-07-15 14:00:00' },
  { id: 2, title: 'ods_user_log_d昨日分区缺失', status: 'OPEN', assignee: '王五', priority: 'HIGH', ruleName: '日志时效性检查', description: '2026-07-15分区数据未及时产出', createTime: '2026-07-16 08:00:00', updateTime: '2026-07-16 08:00:00' },
  { id: 3, title: '手机号格式不规范', status: 'RESOLVED', assignee: '李四', priority: 'MEDIUM', ruleName: '手机号格式校验', description: '120条记录手机号格式异常，已清洗修复', createTime: '2026-07-10 10:00:00', updateTime: '2026-07-12 16:00:00' },
  { id: 4, title: 'order_detail负数金额', status: 'CLOSED', assignee: '李四', priority: 'LOW', ruleName: '订单金额准确性', description: '退款订单导致负数，属正常业务', createTime: '2026-07-05 11:00:00', updateTime: '2026-07-06 09:00:00' },
]

// 资产目录
export const assetCatalogs: AssetCatalog[] = [
  { id: 1, tableId: 1, name: 'user_info', description: '用户基本信息表，存储用户核心属性，包括用户名、手机号、邮箱、注册时间等', catalogPath: '业务域/用户域/用户信息', rating: 4.8, viewCount: 2356, favCount: 45, owner: '张三', tags: ['核心资产', '用户域', 'L3'], type: 'TABLE', updateTime: '2026-07-15 23:00:00' },
  { id: 2, tableId: 2, name: 'order_detail', description: '订单明细表，记录每笔订单的商品、金额、时间等信息', catalogPath: '业务域/交易域/订单', rating: 4.6, viewCount: 3102, favCount: 68, owner: '李四', tags: ['核心资产', '交易域', 'L2'], type: 'TABLE', updateTime: '2026-07-16 01:00:00' },
  { id: 3, tableId: 3, name: 'ods_user_log_d', description: '用户行为日志日表，按天分区存储用户访问行为', catalogPath: '数据域/行为域/日志', rating: 4.2, viewCount: 1580, favCount: 23, owner: '王五', tags: ['行为域', 'ODS', 'L1'], type: 'TABLE', updateTime: '2026-07-16 03:00:00' },
  { id: 4, tableId: 4, name: 'ods_trade_dwd_d', description: '交易域明细宽表，整合订单、商品、用户维度', catalogPath: '数据域/交易域/明细', rating: 4.5, viewCount: 1980, favCount: 34, owner: '李四', tags: ['交易域', 'DWD', 'L2'], type: 'TABLE', updateTime: '2026-07-16 02:30:00' },
  { id: 5, tableId: 5, name: 'rpt_user_active_m', description: '月活用户报表，展示每月活跃用户统计指标', catalogPath: '应用域/报表/用户', rating: 4.9, viewCount: 2890, favCount: 56, owner: '赵六', tags: ['报表', '用户域', 'L1'], type: 'TABLE', updateTime: '2026-07-15 18:00:00' },
]

// 标签
export const assetTags: AssetTag[] = [
  { id: 1, name: '核心资产', category: '重要等级', color: '#0052d9' },
  { id: 2, name: '用户域', category: '业务域', color: '#00a870' },
  { id: 3, name: '交易域', category: '业务域', color: '#ed7b2f' },
  { id: 4, name: '行为域', category: '业务域', color: '#7265e6' },
  { id: 5, name: 'ODS', category: '数仓分层', color: '#8c8c8c' },
  { id: 6, name: 'DWD', category: '数仓分层', color: '#5470c6' },
  { id: 7, name: '报表', category: '资产类型', color: '#fac036' },
]

// 分级分类
export const classifyLabels: ClassifyLabel[] = [
  { id: 1, name: '公开数据', level: 'L1', rules: '默认级别，无敏感信息', fieldCount: 42000 },
  { id: 2, name: '内部数据', level: 'L2', rules: '内部业务数据，如订单金额', fieldCount: 4800 },
  { id: 3, name: '敏感数据', level: 'L3', rules: '手机号正则:^1[3-9]\\d{9}$;邮箱正则:^\\w+@\\w+\\.\\w+$', fieldCount: 1280 },
  { id: 4, name: '高敏数据', level: 'L4', rules: '身份证正则:^\\d{17}[\\dXx]$;银行卡正则:^\\d{16,19}$', fieldCount: 155 },
]

// 脱敏策略
export const maskPolicies: MaskPolicy[] = [
  { id: 1, tableId: 1, tableName: 'user_info', fieldName: 'phone', maskType: 'MASK', expression: '前3后4，中间4位*', preview: '138****1234' },
  { id: 2, tableId: 1, tableName: 'user_info', fieldName: 'id_card', maskType: 'MASK', expression: '前6后4，中间8位*', preview: '110101********1234' },
  { id: 3, tableId: 1, tableName: 'user_info', fieldName: 'email', maskType: 'MASK', expression: '用户名仅留首字符', preview: 'z***@example.com' },
  { id: 4, tableId: 2, tableName: 'order_detail', fieldName: 'pay_amount', maskType: 'HASH', expression: 'MD5哈希', preview: 'a1b2c3d4e5f6...' },
]

// 审计日志
export const auditLogs: AuditLog[] = [
  { id: 1, user: '张三', operation: 'QUERY', target: 'user_info', targetLevel: 'L3', ip: '10.0.2.15', status: 'SUCCESS', time: '2026-07-16 09:15:23' },
  { id: 2, user: '李四', operation: 'EXPORT', target: 'order_detail', targetLevel: 'L2', ip: '10.0.2.22', status: 'SUCCESS', time: '2026-07-16 09:10:01' },
  { id: 3, user: 'guest01', operation: 'QUERY', target: 'user_info', targetLevel: 'L3', ip: '10.0.5.88', status: 'DENIED', time: '2026-07-16 08:58:45' },
  { id: 4, user: '王五', operation: 'QUERY', target: 'ods_user_log_d', targetLevel: 'L1', ip: '10.0.3.10', status: 'SUCCESS', time: '2026-07-16 08:45:12' },
  { id: 5, user: '赵六', operation: 'MODIFY', target: 'rpt_user_active_m', targetLevel: 'L1', ip: '10.0.3.15', status: 'SUCCESS', time: '2026-07-15 18:30:00' },
  { id: 6, user: 'guest02', operation: 'EXPORT', target: 'order_detail', targetLevel: 'L2', ip: '10.0.5.90', status: 'DENIED', time: '2026-07-15 17:20:33' },
]

// 生命周期策略
export const lifecyclePolicies: LifecyclePolicy[] = [
  { id: 1, tableId: 1, tableName: 'user_info', stage: 'HOT', retainDays: 365, action: '保留在热存储', sizeBytes: 536870912 },
  { id: 2, tableId: 2, tableName: 'order_detail', stage: 'WARM', retainDays: 180, action: '迁移至温存储', sizeBytes: 5368709120 },
  { id: 3, tableId: 3, tableName: 'ods_user_log_d', stage: 'COLD', retainDays: 90, action: '归档至OSS冷存储', sizeBytes: 10737418240 },
  { id: 4, tableId: 4, tableName: 'ods_trade_dwd_d', stage: 'WARM', retainDays: 180, action: '迁移至温存储', sizeBytes: 8589934592 },
  { id: 5, tableId: 5, tableName: 'rpt_user_active_m', stage: 'HOT', retainDays: 730, action: '保留在热存储', sizeBytes: 268435456 },
]

// 工作流任务
export const workflowTasks: WorkflowTask[] = [
  { id: 1, title: 'user_info表phone字段重复值修复', type: 'QUALITY', status: 'TODO', assignee: '张三', priority: 'HIGH', dueDate: '2026-07-17', description: '235条手机号重复记录需确认处理', createTime: '2026-07-15 09:00:00' },
  { id: 2, title: 'order_detail脱敏策略审批', type: 'SECURITY', status: 'TODO', assignee: '安全管理员', priority: 'MEDIUM', dueDate: '2026-07-18', description: '申请对pay_amount字段做HASH脱敏', createTime: '2026-07-15 14:00:00' },
  { id: 3, title: '用户域数据标准发布审批', type: 'STANDARD', status: 'DOING', assignee: '治理管理员', priority: 'HIGH', dueDate: '2026-07-16', description: '用户域数据元标准v2.0发布', createTime: '2026-07-14 10:00:00' },
  { id: 4, title: 'ods_user_log_d分区缺失排查', type: 'QUALITY', status: 'DOING', assignee: '王五', priority: 'HIGH', dueDate: '2026-07-16', description: '昨日分区数据未产出', createTime: '2026-07-16 08:00:00' },
  { id: 5, title: '月度数据资产盘点', type: 'APPROVAL', status: 'DONE', assignee: '治理管理员', priority: 'LOW', dueDate: '2026-07-10', description: '6月资产盘点完成', createTime: '2026-07-01 10:00:00' },
]

// 数据标准
export const dataStandards: DataStandard[] = [
  { id: 1, name: '用户ID', category: '用户域', dataType: 'BIGINT', length: 20, businessMeaning: '系统用户唯一标识', version: 'v2.0', status: 'PUBLISHED', mappingCount: 18, complianceRate: 100 },
  { id: 2, name: '手机号', category: '用户域', dataType: 'VARCHAR', length: 16, businessMeaning: '中国大陆手机号', version: 'v2.0', status: 'PUBLISHED', mappingCount: 32, complianceRate: 95.8 },
  { id: 3, name: '身份证号', category: '用户域', dataType: 'VARCHAR', length: 32, businessMeaning: '18位居民身份证号', version: 'v2.0', status: 'PUBLISHED', mappingCount: 12, complianceRate: 100 },
  { id: 4, name: '订单金额', category: '交易域', dataType: 'DECIMAL', length: 12, businessMeaning: '订单实付金额，单位元', version: 'v1.5', status: 'PUBLISHED', mappingCount: 24, complianceRate: 98.5 },
  { id: 5, name: '订单时间', category: '交易域', dataType: 'DATETIME', length: 0, businessMeaning: '订单创建时间', version: 'v1.5', status: 'PUBLISHED', mappingCount: 20, complianceRate: 100 },
  { id: 6, name: '用户状态', category: '用户域', dataType: 'TINYINT', length: 1, businessMeaning: '0停用 1启用', version: 'v1.0', status: 'DRAFT', mappingCount: 5, complianceRate: 80.0 },
]
