// 通用响应
export interface ApiResult<T> {
  code: number
  message: string
  data: T
  traceId: string
}

export interface PageResult<T> {
  list: T[]
  total: number
  page: number
  size: number
}

// 数据源
export interface Datasource {
  id: number
  name: string
  type: 'MYSQL' | 'HIVE' | 'POSTGRESQL' | 'REST'
  jdbcUrl: string
  username: string
  status: 'ACTIVE' | 'INACTIVE'
  lastCollectTime: string
  tableCount: number
  createTime: string
}

// 元数据表
export interface MetadataTable {
  id: number
  datasourceId: number
  dbName: string
  tableName: string
  tableComment: string
  rowCount: number
  sizeBytes: number
  owner: string
  tags: string[]
  qualityScore: number
  securityLevel: string
  createTime: string
  updateTime: string
}

// 元数据字段
export interface MetadataField {
  id: number
  tableId: number
  fieldName: string
  dataType: string
  comment: string
  businessMeaning: string
  isPrimary: number
  isSensitive: number
}

// 表变更历史
export interface TableChange {
  id: number
  tableId: number
  changeType: 'ADD_FIELD' | 'DROP_FIELD' | 'MODIFY_TYPE' | 'RENAME'
  changeDetail: string
  version: string
  changeTime: string
  operator: string
}

// 血缘节点
export interface LineageNode {
  id: string
  label: string
  type: 'TABLE' | 'FIELD' | 'TASK'
  dbName?: string
}

// 血缘边
export interface LineageEdge {
  source: string
  target: string
  label?: string
}

// 质量规则
export interface QualityRule {
  id: number
  name: string
  dimension: '完整性' | '唯一性' | '准确性' | '规范性' | '时效性'
  ruleType: 'TEMPLATE' | 'SQL'
  ruleSql: string
  targetTable: string
  severity: 1 | 2 | 3
  enabled: boolean
  lastScore: number
  createTime: string
}

// 质量结果
export interface QualityResult {
  id: number
  ruleId: number
  checkTime: string
  passCount: number
  failCount: number
  score: number
  status: 'PASS' | 'FAIL'
}

// 质量工单
export interface QualityTicket {
  id: number
  title: string
  status: 'OPEN' | 'PROCESSING' | 'RESOLVED' | 'CLOSED'
  assignee: string
  priority: 'HIGH' | 'MEDIUM' | 'LOW'
  ruleName: string
  description: string
  createTime: string
  updateTime: string
}

// 资产目录
export interface AssetCatalog {
  id: number
  tableId: number
  name: string
  description: string
  catalogPath: string
  rating: number
  viewCount: number
  favCount: number
  owner: string
  tags: string[]
  type: 'TABLE' | 'REPORT' | 'API'
  updateTime: string
}

// 标签
export interface AssetTag {
  id: number
  name: string
  category: string
  color: string
}

// 分级分类
export interface ClassifyLabel {
  id: number
  name: string
  level: 'L1' | 'L2' | 'L3' | 'L4'
  rules: string
  fieldCount: number
}

// 脱敏策略
export interface MaskPolicy {
  id: number
  tableId: number
  tableName: string
  fieldName: string
  maskType: 'MASK' | 'REPLACE' | 'ENCRYPT' | 'HASH'
  expression: string
  preview: string
}

// 审计日志
export interface AuditLog {
  id: number
  user: string
  operation: 'QUERY' | 'EXPORT' | 'MODIFY' | 'DELETE'
  target: string
  targetLevel: string
  ip: string
  status: 'SUCCESS' | 'DENIED'
  time: string
}

// 生命周期策略
export interface LifecyclePolicy {
  id: number
  tableId: number
  tableName: string
  stage: 'HOT' | 'WARM' | 'COLD' | 'DELETE'
  retainDays: number
  action: string
  sizeBytes: number
}

// 工作流任务
export interface WorkflowTask {
  id: number
  title: string
  type: 'QUALITY' | 'SECURITY' | 'STANDARD' | 'APPROVAL'
  status: 'TODO' | 'DOING' | 'DONE'
  assignee: string
  priority: 'HIGH' | 'MEDIUM' | 'LOW'
  dueDate: string
  description: string
  createTime: string
}

// 数据标准
export interface DataStandard {
  id: number
  name: string
  category: string
  dataType: string
  length: number
  businessMeaning: string
  version: string
  status: 'DRAFT' | 'PUBLISHED' | 'DEPRECATED'
  mappingCount: number
  complianceRate: number
}

// 仪表盘统计
export interface DashboardStats {
  datasourceCount: number
  tableCount: number
  fieldCount: number
  qualityScore: number
  qualityScoreTrend: { date: string; score: number }[]
  todoCount: number
  issueCount: number
  datasourceDistribution: { name: string; value: number }[]
  tableGrowth: { date: string; count: number }[]
}
