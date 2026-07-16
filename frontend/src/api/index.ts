import request from './request'
import * as mock from '@/mock'
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
  PageResult,
} from '@/types'

// mock 回退封装：后端不可用时返回 mock 数据
async function withMock<T>(req: Promise<T>, fallback: T): Promise<T> {
  try {
    const res = await req
    return res
  } catch {
    return fallback
  }
}

// ============ 仪表盘 ============
export function getDashboardStats(): Promise<DashboardStats> {
  return withMock(
    request.get('/dashboard/stats').then((r) => r.data.data),
    mock.dashboardStats
  )
}

// ============ 数据源 ============
export function getDatasources(): Promise<Datasource[]> {
  return withMock(
    request.get('/metadata/datasources').then((r) => r.data.data),
    mock.datasources
  )
}

export function saveDatasource(data: Partial<Datasource>): Promise<Datasource> {
  return withMock(
    request.post('/metadata/datasources', data).then((r) => r.data.data),
    { ...data, id: Date.now() } as Datasource
  )
}

export function deleteDatasource(id: number): Promise<void> {
  return withMock(
    request.delete(`/metadata/datasources/${id}`).then((r) => r.data.data),
    undefined as unknown as void
  )
}

// ============ 元数据表 ============
export function getMetadataTables(): Promise<MetadataTable[]> {
  return withMock(
    request.get('/metadata/tables').then((r) => r.data.data),
    mock.metadataTables
  )
}

export function getTableDetail(id: number): Promise<MetadataTable> {
  return withMock(
    request.get(`/metadata/tables/${id}`).then((r) => r.data.data),
    mock.metadataTables.find((t) => t.id === id) || mock.metadataTables[0]
  )
}

export function getTableFields(tableId: number): Promise<MetadataField[]> {
  return withMock(
    request.get(`/metadata/tables/${tableId}/fields`).then((r) => r.data.data),
    mock.metadataFields
  )
}

export function getTableChanges(tableId: number): Promise<TableChange[]> {
  return withMock(
    request.get(`/metadata/tables/${tableId}/changes`).then((r) => r.data.data),
    mock.tableChanges
  )
}

// ============ 血缘 ============
export function getLineage(tableId?: number): Promise<{ nodes: LineageNode[]; edges: LineageEdge[] }> {
  return withMock(
    request.get(`/metadata/lineage`, { params: { tableId } }).then((r) => r.data.data),
    { nodes: mock.lineageNodes, edges: mock.lineageEdges }
  )
}

// ============ 质量规则 ============
export function getQualityRules(): Promise<QualityRule[]> {
  return withMock(
    request.get('/quality/rules').then((r) => r.data.data),
    mock.qualityRules
  )
}

export function saveQualityRule(data: Partial<QualityRule>): Promise<QualityRule> {
  return withMock(
    request.post('/quality/rules', data).then((r) => r.data.data),
    { ...data, id: Date.now() } as QualityRule
  )
}

export function trialQualityRule(id: number): Promise<{ pass: number; fail: number; score: number }> {
  return withMock(
    request.post(`/quality/rules/${id}/trial`).then((r) => r.data.data),
    { pass: 1258000, fail: 235, score: 99.98 }
  )
}

// ============ 质量工单 ============
export function getQualityTickets(): Promise<QualityTicket[]> {
  return withMock(
    request.get('/quality/tickets').then((r) => r.data.data),
    mock.qualityTickets
  )
}

export function updateTicketStatus(id: number, status: string): Promise<void> {
  return withMock(
    request.put(`/quality/tickets/${id}/status`, { status }).then((r) => r.data.data),
    undefined as unknown as void
  )
}

// ============ 资产目录 ============
export function searchAssets(keyword: string): Promise<AssetCatalog[]> {
  return withMock(
    request.get('/catalog/search', { params: { keyword } }).then((r) => r.data.data),
    mock.assetCatalogs.filter((a) =>
      !keyword || a.name.includes(keyword) || a.description.includes(keyword) || a.tags.some((t) => t.includes(keyword))
    )
  )
}

export function getAssetDetail(id: number): Promise<AssetCatalog> {
  return withMock(
    request.get(`/catalog/assets/${id}`).then((r) => r.data.data),
    mock.assetCatalogs.find((a) => a.id === id) || mock.assetCatalogs[0]
  )
}

export function getAssetTags(): Promise<AssetTag[]> {
  return withMock(
    request.get('/catalog/tags').then((r) => r.data.data),
    mock.assetTags
  )
}

// ============ 数据安全 ============
export function getClassifyLabels(): Promise<ClassifyLabel[]> {
  return withMock(
    request.get('/security/classify').then((r) => r.data.data),
    mock.classifyLabels
  )
}

export function getMaskPolicies(): Promise<MaskPolicy[]> {
  return withMock(
    request.get('/security/mask/policies').then((r) => r.data.data),
    mock.maskPolicies
  )
}

export function saveMaskPolicy(data: Partial<MaskPolicy>): Promise<MaskPolicy> {
  return withMock(
    request.post('/security/mask/policies', data).then((r) => r.data.data),
    { ...data, id: Date.now() } as MaskPolicy
  )
}

export function getAuditLogs(params?: Record<string, unknown>): Promise<AuditLog[]> {
  return withMock(
    request.get('/security/audit/logs', { params }).then((r) => r.data.data),
    mock.auditLogs
  )
}

// ============ 生命周期 ============
export function getLifecyclePolicies(): Promise<LifecyclePolicy[]> {
  return withMock(
    request.get('/lifecycle/policies').then((r) => r.data.data),
    mock.lifecyclePolicies
  )
}

// ============ 工作流 ============
export function getWorkflowTasks(): Promise<WorkflowTask[]> {
  return withMock(
    request.get('/workflow/tasks').then((r) => r.data.data),
    mock.workflowTasks
  )
}

export function updateTaskStatus(id: number, status: string): Promise<void> {
  return withMock(
    request.put(`/workflow/tasks/${id}/status`, { status }).then((r) => r.data.data),
    undefined as unknown as void
  )
}

export function getMyTodo(): Promise<WorkflowTask[]> {
  return withMock(
    request.get('/workflow/todo').then((r) => r.data.data),
    mock.workflowTasks.filter((t) => t.status !== 'DONE')
  )
}

// ============ 数据标准 ============
export function getDataStandards(): Promise<DataStandard[]> {
  return withMock(
    request.get('/standard/list').then((r) => r.data.data),
    mock.dataStandards
  )
}

// 分页结果 mock 辅助
export function mockPage<T>(list: T[], page: number, size: number): PageResult<T> {
  const start = (page - 1) * size
  return { list: list.slice(start, start + size), total: list.length, page, size }
}
