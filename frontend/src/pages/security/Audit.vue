<script setup lang="ts">
import { ref, reactive, computed, onMounted, watch } from 'vue'
import { MessagePlugin } from 'tdesign-vue-next'
import { getAuditLogs } from '@/api'
import type { AuditLog } from '@/types'

const loading = ref(true)
const list = ref<AuditLog[]>([])

const filters = reactive({
  timeRange: [] as Array<unknown>,
  user: '',
  operation: '',
  status: '',
})

const operationOptions = [
  { value: 'QUERY', label: '查询' },
  { value: 'EXPORT', label: '导出' },
  { value: 'MODIFY', label: '修改' },
  { value: 'DELETE', label: '删除' },
]
const statusOptions = [
  { value: 'SUCCESS', label: '成功' },
  { value: 'DENIED', label: '拒绝' },
]

const opLabels: Record<string, string> = {
  QUERY: '查询',
  EXPORT: '导出',
  MODIFY: '修改',
  DELETE: '删除',
}
const opColors: Record<string, string> = {
  QUERY: '#0052d9',
  EXPORT: '#ed7b2f',
  MODIFY: '#00a870',
  DELETE: '#e34d59',
}
const levelColors: Record<string, string> = {
  L1: '#8c8c8c',
  L2: '#0052d9',
  L3: '#ed7b2f',
  L4: '#e34d59',
}

const current = ref(1)
const pageSize = ref(10)

function fmtDate(d: unknown): string {
  if (!d) return ''
  if (typeof d === 'string') return d.slice(0, 10)
  if (d instanceof Date) {
    const m = String(d.getMonth() + 1).padStart(2, '0')
    const day = String(d.getDate()).padStart(2, '0')
    return `${d.getFullYear()}-${m}-${day}`
  }
  if (typeof d === 'object' && d !== null && 'format' in d) {
    // dayjs-like
    return (d as { format: (s: string) => string }).format('YYYY-MM-DD')
  }
  return String(d)
}

const filteredList = computed(() => {
  let arr = list.value
  if (filters.user.trim()) {
    const kw = filters.user.trim().toLowerCase()
    arr = arr.filter(
      (l) => l.user.toLowerCase().includes(kw) || l.target.toLowerCase().includes(kw),
    )
  }
  if (filters.operation) arr = arr.filter((l) => l.operation === filters.operation)
  if (filters.status) arr = arr.filter((l) => l.status === filters.status)
  if (Array.isArray(filters.timeRange) && filters.timeRange.length === 2) {
    const start = fmtDate(filters.timeRange[0])
    const end = fmtDate(filters.timeRange[1])
    if (start && end) {
      arr = arr.filter((l) => {
        const d = l.time.slice(0, 10)
        return d >= start && d <= end
      })
    }
  }
  return arr
})

const pagedList = computed(() => {
  const start = (current.value - 1) * pageSize.value
  return filteredList.value.slice(start, start + pageSize.value)
})

watch(filteredList, () => {
  const maxPage = Math.max(1, Math.ceil(filteredList.value.length / pageSize.value))
  if (current.value > maxPage) current.value = maxPage
})

const columns = [
  { colKey: 'time', title: '时间', width: 170 },
  { colKey: 'user', title: '用户', width: 110 },
  { colKey: 'operation', title: '操作类型', width: 100, cell: 'operation' },
  { colKey: 'target', title: '目标对象', ellipsis: true },
  { colKey: 'targetLevel', title: '安全级别', width: 100, cell: 'targetLevel' },
  { colKey: 'ip', title: 'IP地址', width: 130 },
  { colKey: 'status', title: '状态', width: 90, cell: 'status' },
]

function onSearch() {
  current.value = 1
}

function onExport() {
  MessagePlugin.success(`已导出 ${filteredList.value.length} 条审计日志`)
}

const drawerVisible = ref(false)
const detail = ref<AuditLog | null>(null)

function onRowClick(context: { row: AuditLog }) {
  detail.value = context.row
  drawerVisible.value = true
}

onMounted(async () => {
  list.value = await getAuditLogs()
  loading.value = false
})
</script>

<template>
  <div class="page-container" v-loading="loading">
    <!-- 筛选区 -->
    <div class="page-card">
      <div class="card-label" style="margin-bottom: 16px">审计日志检索</div>
      <div class="filter-row">
        <t-date-range-picker
          v-model="filters.timeRange"
          clearable
          style="width: 280px"
        />
        <t-input
          v-model="filters.user"
          placeholder="用户/目标对象"
          clearable
          style="width: 200px"
        >
          <template #prefix-icon><t-icon name="user" /></template>
        </t-input>
        <t-select
          v-model="filters.operation"
          :options="operationOptions"
          placeholder="操作类型"
          clearable
          style="width: 140px"
        />
        <t-select
          v-model="filters.status"
          :options="statusOptions"
          placeholder="状态"
          clearable
          style="width: 120px"
        />
        <t-button theme="primary" @click="onSearch">
          <template #icon><t-icon name="search" /></template>
          搜索
        </t-button>
        <t-button theme="default" variant="outline" @click="onExport">
          <template #icon><t-icon name="download" /></template>
          导出
        </t-button>
      </div>
    </div>

    <!-- 列表 -->
    <div class="page-card">
      <t-table
        :data="pagedList"
        row-key="id"
        size="small"
        :pagination="{ show: false }"
        :columns="columns"
        @row-click="onRowClick"
      >
        <template #operation="{ row }">
          <t-tag :color="opColors[row.operation]" variant="light">
            {{ opLabels[row.operation] }}
          </t-tag>
        </template>
        <template #targetLevel="{ row }">
          <t-tag :color="levelColors[row.targetLevel]" variant="light">
            {{ row.targetLevel }}
          </t-tag>
        </template>
        <template #status="{ row }">
          <t-tag v-if="row.status === 'SUCCESS'" theme="success" variant="light">成功</t-tag>
          <t-tag v-else theme="danger" variant="light">拒绝</t-tag>
        </template>
      </t-table>
      <div class="pagination-wrap">
        <t-pagination
          v-model:current="current"
          v-model:pageSize="pageSize"
          :total="filteredList.length"
          :page-size-options="[10, 20, 50]"
          show-jumper
        />
      </div>
    </div>

    <!-- 详情抽屉 -->
    <t-drawer v-model:visible="drawerVisible" header="日志详情" size="480px">
      <template v-if="detail">
        <div class="detail-item">
          <span class="d-label">时间</span>
          <span class="d-value">{{ detail.time }}</span>
        </div>
        <div class="detail-item">
          <span class="d-label">用户</span>
          <span class="d-value">{{ detail.user }}</span>
        </div>
        <div class="detail-item">
          <span class="d-label">操作类型</span>
          <t-tag :color="opColors[detail.operation]" variant="light">
            {{ opLabels[detail.operation] }}
          </t-tag>
        </div>
        <div class="detail-item">
          <span class="d-label">目标对象</span>
          <span class="d-value">{{ detail.target }}</span>
        </div>
        <div class="detail-item">
          <span class="d-label">安全级别</span>
          <t-tag :color="levelColors[detail.targetLevel]" variant="light">
            {{ detail.targetLevel }}
          </t-tag>
        </div>
        <div class="detail-item">
          <span class="d-label">IP地址</span>
          <span class="d-value">{{ detail.ip }}</span>
        </div>
        <div class="detail-item">
          <span class="d-label">状态</span>
          <t-tag v-if="detail.status === 'SUCCESS'" theme="success" variant="light">成功</t-tag>
          <t-tag v-else theme="danger" variant="light">拒绝</t-tag>
        </div>
      </template>
    </t-drawer>
  </div>
</template>

<style scoped>
.card-label {
  font-size: 15px;
  font-weight: 600;
  color: var(--text-primary);
}
.filter-row {
  display: flex;
  align-items: center;
  flex-wrap: wrap;
  gap: 12px;
}
.pagination-wrap {
  display: flex;
  justify-content: flex-end;
  margin-top: 16px;
}
:deep(.t-table__row) {
  cursor: pointer;
}
.detail-item {
  display: flex;
  align-items: center;
  padding: 10px 0;
  border-bottom: 1px solid #f0f0f0;
}
.d-label {
  width: 90px;
  color: var(--text-secondary);
  font-size: 13px;
  flex-shrink: 0;
}
.d-value {
  color: var(--text-primary);
  font-weight: 500;
}
</style>
