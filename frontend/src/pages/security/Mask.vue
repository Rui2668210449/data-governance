<script setup lang="ts">
import { ref, reactive, computed, onMounted } from 'vue'
import { MessagePlugin, DialogPlugin } from 'tdesign-vue-next'
import {
  getMaskPolicies,
  saveMaskPolicy,
  getMetadataTables,
  getTableFields,
} from '@/api'
import type { MaskPolicy, MetadataTable, MetadataField } from '@/types'

const loading = ref(true)
const list = ref<MaskPolicy[]>([])
const tables = ref<MetadataTable[]>([])
const fields = ref<MetadataField[]>([])

const keyword = ref('')
const dialogVisible = ref(false)
const isEdit = ref(false)

const form = reactive({
  id: 0,
  tableId: undefined as number | undefined,
  tableName: '',
  fieldName: '',
  maskType: 'MASK' as MaskPolicy['maskType'],
  expression: '',
  preview: '',
})

const maskTypeOptions = [
  { value: 'MASK', label: '遮盖' },
  { value: 'REPLACE', label: '替换' },
  { value: 'ENCRYPT', label: '加密' },
  { value: 'HASH', label: '哈希' },
]

const maskColors: Record<string, string> = {
  MASK: '#0052d9',
  REPLACE: '#00a870',
  ENCRYPT: '#ed7b2f',
  HASH: '#7265e6',
}
const maskLabels: Record<string, string> = {
  MASK: '遮盖',
  REPLACE: '替换',
  ENCRYPT: '加密',
  HASH: '哈希',
}

const tableOptions = computed(() =>
  tables.value.map((t) => ({ value: t.id, label: `${t.dbName}.${t.tableName}` })),
)

const fieldOptions = computed(() => {
  const opts = fields.value.map((f) => ({
    value: f.fieldName,
    label: `${f.fieldName} (${f.dataType})`,
  }))
  if (form.fieldName && !opts.some((o) => o.value === form.fieldName)) {
    opts.push({ value: form.fieldName, label: form.fieldName })
  }
  return opts
})

const filteredList = computed(() => {
  const kw = keyword.value.trim().toLowerCase()
  if (!kw) return list.value
  return list.value.filter(
    (p) =>
      p.tableName.toLowerCase().includes(kw) ||
      p.fieldName.toLowerCase().includes(kw),
  )
})

const columns = [
  { colKey: 'tableName', title: '表名', ellipsis: true },
  { colKey: 'fieldName', title: '字段名', width: 140 },
  { colKey: 'maskType', title: '脱敏类型', width: 100, cell: 'maskType' },
  { colKey: 'expression', title: '脱敏表达式', ellipsis: true },
  { colKey: 'preview', title: '预览效果', width: 200, cell: 'preview' },
  { colKey: 'op', title: '操作', width: 130, cell: 'op' },
]

function onTableChange(val: unknown) {
  const id = val as number
  const t = tables.value.find((x) => x.id === id)
  if (t) form.tableName = t.tableName
  form.fieldName = ''
  getTableFields(id).then((fs) => {
    fields.value = fs
  })
}

function onMaskTypeChange(val: unknown) {
  const v = val as MaskPolicy['maskType']
  const map: Record<string, { expression: string; preview: string }> = {
    MASK: { expression: '保留首尾，中间以*遮盖', preview: '138****1234' },
    REPLACE: { expression: '替换为固定值 ***', preview: '***' },
    ENCRYPT: { expression: 'AES对称加密', preview: 'U2FsdGVkX1+...' },
    HASH: { expression: 'MD5哈希摘要', preview: 'a1b2c3d4e5f6...' },
  }
  if (!form.expression) form.expression = map[v].expression
  if (!form.preview) form.preview = map[v].preview
}

function openCreate() {
  isEdit.value = false
  form.id = 0
  form.tableId = undefined
  form.tableName = ''
  form.fieldName = ''
  form.maskType = 'MASK'
  form.expression = ''
  form.preview = ''
  dialogVisible.value = true
}

function openEdit(row: MaskPolicy) {
  isEdit.value = true
  form.id = row.id
  form.tableId = row.tableId
  form.tableName = row.tableName
  form.fieldName = row.fieldName
  form.maskType = row.maskType
  form.expression = row.expression
  form.preview = row.preview
  getTableFields(row.tableId).then((fs) => {
    fields.value = fs
  })
  dialogVisible.value = true
}

async function onConfirm() {
  if (!form.tableId || !form.fieldName || !form.maskType) {
    MessagePlugin.warning('请填写完整信息')
    return
  }
  const payload: Partial<MaskPolicy> = {
    id: form.id || undefined,
    tableId: form.tableId,
    tableName: form.tableName,
    fieldName: form.fieldName,
    maskType: form.maskType,
    expression: form.expression,
    preview: form.preview,
  }
  if (isEdit.value) {
    const idx = list.value.findIndex((p) => p.id === form.id)
    if (idx > -1) {
      list.value[idx] = { ...list.value[idx], ...payload } as MaskPolicy
    }
  } else {
    const saved = await saveMaskPolicy(payload)
    list.value.push(saved)
  }
  dialogVisible.value = false
  MessagePlugin.success(isEdit.value ? '脱敏策略已更新' : '脱敏策略已创建')
}

function onDelete(row: MaskPolicy) {
  const dlg = DialogPlugin.confirm({
    header: '确认删除',
    body: `确定删除策略 ${row.tableName}.${row.fieldName} 吗？`,
    onConfirm: () => {
      list.value = list.value.filter((p) => p.id !== row.id)
      dlg.destroy()
      MessagePlugin.success('策略已删除')
    },
  })
}

onMounted(async () => {
  const [ps, ts, fs] = await Promise.all([
    getMaskPolicies(),
    getMetadataTables(),
    getTableFields(1),
  ])
  list.value = ps
  tables.value = ts
  fields.value = fs
  loading.value = false
})
</script>

<template>
  <div class="page-container" v-loading="loading">
    <div class="page-card">
      <div class="flex-between" style="margin-bottom: 16px">
        <span class="card-label">脱敏策略配置</span>
        <div class="flex gap-12">
          <t-input
            v-model="keyword"
            placeholder="搜索表名/字段名"
            clearable
            style="width: 220px"
          >
            <template #prefix-icon><t-icon name="search" /></template>
          </t-input>
          <t-button theme="primary" @click="openCreate">
            <template #icon><t-icon name="add" /></template>
            新建脱敏策略
          </t-button>
        </div>
      </div>
      <t-table
        :data="filteredList"
        row-key="id"
        size="small"
        :pagination="{ show: false }"
        :columns="columns"
      >
        <template #maskType="{ row }">
          <t-tag :color="maskColors[row.maskType]" variant="light">
            {{ maskLabels[row.maskType] }}
          </t-tag>
        </template>
        <template #preview="{ row }">
          <code class="preview-code">{{ row.preview }}</code>
        </template>
        <template #op="{ row }">
          <t-button theme="primary" variant="text" size="small" @click.stop="openEdit(row)">
            编辑
          </t-button>
          <t-button theme="danger" variant="text" size="small" @click.stop="onDelete(row)">
            删除
          </t-button>
        </template>
      </t-table>
    </div>

    <t-dialog
      v-model:visible="dialogVisible"
      :header="isEdit ? '编辑脱敏策略' : '新建脱敏策略'"
      width="520px"
      @confirm="onConfirm"
    >
      <t-form :data="form" label-align="top">
        <t-form-item label="选择表" name="tableId">
          <t-select
            v-model="form.tableId"
            :options="tableOptions"
            placeholder="请选择表"
            @change="onTableChange"
          />
        </t-form-item>
        <t-form-item label="选择字段" name="fieldName">
          <t-select
            v-model="form.fieldName"
            :options="fieldOptions"
            placeholder="请选择字段"
          />
        </t-form-item>
        <t-form-item label="脱敏类型" name="maskType">
          <t-select
            v-model="form.maskType"
            :options="maskTypeOptions"
            @change="onMaskTypeChange"
          />
        </t-form-item>
        <t-form-item label="脱敏表达式" name="expression">
          <t-input v-model="form.expression" placeholder="如: 前3后4，中间4位*" />
        </t-form-item>
        <t-form-item label="预览效果" name="preview">
          <t-input v-model="form.preview" placeholder="如: 138****1234" />
        </t-form-item>
      </t-form>
    </t-dialog>
  </div>
</template>

<style scoped>
.card-label {
  font-size: 15px;
  font-weight: 600;
  color: var(--text-primary);
}
.preview-code {
  background: #f5f7fa;
  color: #8c8c8c;
  padding: 2px 8px;
  border-radius: 4px;
  font-family: 'Courier New', Consolas, monospace;
  font-size: 12px;
}
</style>
