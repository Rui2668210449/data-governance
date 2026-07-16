<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { MessagePlugin, DialogPlugin } from 'tdesign-vue-next'
import { getLifecyclePolicies, getMetadataTables } from '@/api'
import type { LifecyclePolicy, MetadataTable } from '@/types'

const loading = ref(false)
const policies = ref<LifecyclePolicy[]>([])
const tables = ref<MetadataTable[]>([])
const keyword = ref('')

const stageMeta: Record<string, { text: string; color: string; bg: string }> = {
  HOT: { text: '热数据', color: '#e34d59', bg: 'rgba(227,77,89,0.1)' },
  WARM: { text: '温数据', color: '#ed7b2f', bg: 'rgba(237,123,47,0.1)' },
  COLD: { text: '冷数据', color: '#0052d9', bg: 'rgba(0,82,217,0.1)' },
  DELETE: { text: '待删除', color: '#8c8c8c', bg: 'rgba(140,140,140,0.1)' },
}

const actionOptions = [
  { label: '保留在热存储', value: '保留在热存储' },
  { label: '迁移至温存储', value: '迁移至温存储' },
  { label: '归档至OSS冷存储', value: '归档至OSS冷存储' },
  { label: '压缩后归档', value: '压缩后归档' },
  { label: '物理删除', value: '物理删除' },
]

const stageOptions = [
  { label: '热数据 (HOT)', value: 'HOT' },
  { label: '温数据 (WARM)', value: 'WARM' },
  { label: '冷数据 (COLD)', value: 'COLD' },
  { label: '待删除 (DELETE)', value: 'DELETE' },
]

const tableOptions = computed(() =>
  tables.value.map((t) => ({
    label: `${t.dbName}.${t.tableName}（${t.tableComment}）`,
    value: t.id,
  }))
)

function formatSize(bytes: number) {
  if (bytes >= 1073741824) return (bytes / 1073741824).toFixed(2) + ' GB'
  if (bytes >= 1048576) return (bytes / 1048576).toFixed(2) + ' MB'
  if (bytes >= 1024) return (bytes / 1024).toFixed(2) + ' KB'
  return bytes + ' B'
}

const filteredPolicies = computed(() =>
  policies.value.filter((p) => !keyword.value || p.tableName.includes(keyword.value))
)

const columns = [
  { colKey: 'tableName', title: '表名', minWidth: 180, ellipsis: true },
  { colKey: 'stage', title: '当前阶段', width: 110, cell: 'stage' },
  { colKey: 'retainDays', title: '保留天数', width: 110, cell: 'retainDays' },
  { colKey: 'action', title: '存储动作', minWidth: 160, ellipsis: true },
  { colKey: 'sizeBytes', title: '数据大小', width: 120, cell: 'size' },
  { colKey: 'op', title: '操作', width: 160, cell: 'op', fixed: 'right' as const },
]

async function loadData() {
  loading.value = true
  try {
    const [p, t] = await Promise.all([getLifecyclePolicies(), getMetadataTables()])
    policies.value = p
    tables.value = t
  } finally {
    loading.value = false
  }
}

// ============ 新建/编辑弹窗 ============
const dialogVisible = ref(false)
const dialogMode = ref<'create' | 'edit'>('create')
const submitting = ref(false)
const form = ref({
  id: 0,
  tableId: undefined as number | undefined,
  tableName: '',
  stage: 'HOT' as LifecyclePolicy['stage'],
  retainDays: 365,
  action: '保留在热存储',
  sizeBytes: 0,
})

const formRules = {
  tableId: [{ required: true, message: '请选择资产表', type: 'error' as const }],
  stage: [{ required: true, message: '请选择阶段', type: 'error' as const }],
  retainDays: [{ required: true, message: '请输入保留天数', type: 'error' as const }],
  action: [{ required: true, message: '请选择存储动作', type: 'error' as const }],
}

function openCreate() {
  dialogMode.value = 'create'
  form.value = {
    id: 0,
    tableId: undefined,
    tableName: '',
    stage: 'HOT',
    retainDays: 365,
    action: '保留在热存储',
    sizeBytes: 0,
  }
  dialogVisible.value = true
}

function openEdit(row: LifecyclePolicy) {
  dialogMode.value = 'edit'
  form.value = { ...row }
  dialogVisible.value = true
}

function handleTableSelect(val: number) {
  const t = tables.value.find((x) => x.id === val)
  if (t) {
    form.value.tableName = `${t.dbName}.${t.tableName}`
    form.value.sizeBytes = t.sizeBytes
  }
}

async function handleSubmit() {
  if (!form.value.tableId) {
    MessagePlugin.warning('请选择资产表')
    return
  }
  submitting.value = true
  try {
    await new Promise((r) => setTimeout(r, 200))
    if (dialogMode.value === 'create') {
      const newId = policies.value.length
        ? Math.max(...policies.value.map((p) => p.id)) + 1
        : 1
      policies.value.push({
        ...form.value,
        id: newId,
      } as LifecyclePolicy)
      MessagePlugin.success('策略创建成功')
    } else {
      const idx = policies.value.findIndex((p) => p.id === form.value.id)
      if (idx > -1) policies.value[idx] = { ...form.value } as LifecyclePolicy
      MessagePlugin.success('策略更新成功')
    }
    dialogVisible.value = false
  } finally {
    submitting.value = false
  }
}

function handleDelete(row: LifecyclePolicy) {
  const dialog = DialogPlugin.confirm({
    header: '删除确认',
    body: `确认删除「${row.tableName}」的生命周期策略吗？`,
    confirmBtn: '删除',
    cancelBtn: '取消',
    theme: 'danger',
    onConfirm: () => {
      policies.value = policies.value.filter((p) => p.id !== row.id)
      MessagePlugin.success('策略已删除')
      dialog.destroy()
    },
    onClose: () => {
      dialog.destroy()
    },
  })
}

// ============ 阶段预览：时间线 ============
const previewVisible = ref(false)
const previewRow = ref<LifecyclePolicy | null>(null)

function openPreview(row: LifecyclePolicy) {
  previewRow.value = row
  previewVisible.value = true
}

// 根据当前阶段生成完整生命周期时间线
function timelineItems(policy: LifecyclePolicy) {
  const items = [
    { content: '热数据阶段', desc: '高频访问，保留在热存储', type: 'HOT' as const },
    { content: '温数据阶段', desc: '访问频次降低，迁移至温存储', type: 'WARM' as const },
    { content: '冷数据阶段', desc: '低频访问，归档至冷存储', type: 'COLD' as const },
    { content: '删除阶段', desc: '超过保留期，物理删除', type: 'DELETE' as const },
  ]
  const order: string[] = ['HOT', 'WARM', 'COLD', 'DELETE']
  const curIdx = order.indexOf(policy.stage)
  return items.map((it, i) => ({
    ...it,
    color: i < curIdx ? '#8c8c8c' : i === curIdx ? stageMeta[it.type].color : '#d9d9d9',
    status: i < curIdx ? 'default' : i === curIdx ? 'primary' : 'default',
    label: i === curIdx ? '当前' : i < curIdx ? '已完成' : '未开始',
  }))
}

onMounted(loadData)
</script>

<template>
  <div class="page-container" v-loading="loading">
    <div class="page-card">
      <div class="flex-between" style="margin-bottom: 16px">
        <h3 class="page-title" style="margin: 0">生命周期策略</h3>
        <t-button theme="primary" @click="openCreate">
          <template #icon><t-icon name="add" /></template>
          新建策略
        </t-button>
      </div>

      <div class="flex gap-12" style="margin-bottom: 12px">
        <t-input
          v-model="keyword"
          placeholder="搜索表名"
          clearable
          style="width: 280px"
        >
          <template #prefix-icon><t-icon name="search" /></template>
        </t-input>
        <div class="flex gap-8 flex-center">
          <t-tag
            v-for="(meta, key) in stageMeta"
            :key="key"
            size="small"
            :color="meta.color"
            variant="light"
          >
            {{ meta.text }}
          </t-tag>
        </div>
      </div>

      <t-table
        :data="filteredPolicies"
        row-key="id"
        :columns="columns"
        size="small"
        :pagination="{ show: false }"
        bordered
      >
        <template #stage="{ row }">
          <t-tag
            size="small"
            :color="stageMeta[row.stage].color"
            variant="light"
          >
            {{ stageMeta[row.stage].text }}
          </t-tag>
        </template>
        <template #retainDays="{ row }">
          {{ row.retainDays }} 天
        </template>
        <template #size="{ row }">
          {{ formatSize(row.sizeBytes) }}
        </template>
        <template #op="{ row }">
          <t-button theme="primary" variant="text" size="small" @click="openPreview(row)">
            预览
          </t-button>
          <t-button theme="primary" variant="text" size="small" @click="openEdit(row)">
            编辑
          </t-button>
          <t-button theme="danger" variant="text" size="small" @click="handleDelete(row)">
            删除
          </t-button>
        </template>
      </t-table>
    </div>

    <!-- 新建/编辑弹窗 -->
    <t-dialog
      v-model:visible="dialogVisible"
      :header="dialogMode === 'create' ? '新建生命周期策略' : '编辑生命周期策略'"
      :confirm-btn="{ content: '保存', loading: submitting, onClick: handleSubmit }"
      cancel-btn="取消"
      width="560px"
    >
      <t-form :data="form" :rules="formRules" label-align="top">
        <t-form-item label="资产表" name="tableId">
          <t-select
            v-model="form.tableId"
            :options="tableOptions"
            placeholder="请选择资产表"
            @change="handleTableSelect"
          />
        </t-form-item>
        <t-form-item label="当前阶段" name="stage">
          <t-radio-group v-model="form.stage">
            <t-radio
              v-for="opt in stageOptions"
              :key="opt.value"
              :value="opt.value"
              :color="stageMeta[opt.value].color"
            >
              {{ opt.label }}
            </t-radio>
          </t-radio-group>
        </t-form-item>
        <t-form-item label="保留天数" name="retainDays">
          <t-input-number
            v-model="form.retainDays"
            :min="1"
            theme="normal"
            style="width: 100%"
          >
            <template #suffix>天</template>
          </t-input-number>
        </t-form-item>
        <t-form-item label="存储动作" name="action">
          <t-select v-model="form.action" :options="actionOptions" />
        </t-form-item>
      </t-form>
    </t-dialog>

    <!-- 阶段预览抽屉 -->
    <t-drawer
      v-model:visible="previewVisible"
      :header="previewRow ? `${previewRow.tableName} 生命周期时间线` : ''"
      size="480px"
      :footer="false"
    >
      <div v-if="previewRow" class="preview-wrap">
        <div class="preview-summary">
          <div class="summary-item">
            <span class="summary-label">当前阶段</span>
            <t-tag :color="stageMeta[previewRow.stage].color" variant="light">
              {{ stageMeta[previewRow.stage].text }}
            </t-tag>
          </div>
          <div class="summary-item">
            <span class="summary-label">保留天数</span>
            <span class="summary-value">{{ previewRow.retainDays }} 天</span>
          </div>
          <div class="summary-item">
            <span class="summary-label">数据大小</span>
            <span class="summary-value">{{ formatSize(previewRow.sizeBytes) }}</span>
          </div>
          <div class="summary-item">
            <span class="summary-label">存储动作</span>
            <span class="summary-value">{{ previewRow.action }}</span>
          </div>
        </div>
        <div class="preview-tl">
          <t-timeline>
            <t-timeline-item
              v-for="(item, i) in timelineItems(previewRow)"
              :key="i"
              :dot-color="item.color"
            >
              <div class="tl-content">
                <div class="tl-head">
                  <span class="tl-title">{{ item.content }}</span>
                  <t-tag size="small" :color="item.color" variant="light">{{ item.label }}</t-tag>
                </div>
                <div class="tl-desc">{{ item.desc }}</div>
              </div>
            </t-timeline-item>
          </t-timeline>
        </div>
      </div>
    </t-drawer>
  </div>
</template>

<style scoped>
.page-title {
  margin: 0;
}
.preview-wrap {
  padding: 4px 0;
}
.preview-summary {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 12px;
  padding: 12px;
  background: #fafbfc;
  border: 1px solid var(--border-color);
  border-radius: 6px;
  margin-bottom: 20px;
}
.summary-item {
  display: flex;
  flex-direction: column;
  gap: 4px;
}
.summary-label {
  font-size: 12px;
  color: var(--text-secondary);
}
.summary-value {
  font-size: 14px;
  font-weight: 500;
}
.preview-tl {
  padding: 4px 8px;
}
.tl-content {
  padding-bottom: 8px;
}
.tl-head {
  display: flex;
  align-items: center;
  gap: 8px;
}
.tl-title {
  font-size: 14px;
  font-weight: 500;
}
.tl-desc {
  font-size: 12px;
  color: var(--text-secondary);
  margin-top: 4px;
}
</style>
