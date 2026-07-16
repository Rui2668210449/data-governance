<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { MessagePlugin } from 'tdesign-vue-next'
import { getDataStandards, getMetadataTables, getTableFields } from '@/api'
import type { DataStandard, MetadataTable, MetadataField } from '@/types'

const loading = ref(false)
const standards = ref<DataStandard[]>([])
const tables = ref<MetadataTable[]>([])
const fields = ref<MetadataField[]>([])

const selectedTableId = ref<number | undefined>(undefined)

interface FieldStandardMapping {
  fieldId: number
  fieldName: string
  fieldType: string
  fieldComment: string
  standardId: number
  standardName: string
}
const mappings = ref<FieldStandardMapping[]>([])

// 当前选中的字段与标准（用于点击匹配模式）
const selectedFieldId = ref<number | undefined>(undefined)
const selectedStandardId = ref<number | undefined>(undefined)
// 拖拽状态
const draggingFieldId = ref<number | undefined>(undefined)
const draggingStandardId = ref<number | undefined>(undefined)

const tableOptions = computed(() =>
  tables.value.map((t) => ({
    label: `${t.dbName}.${t.tableName}（${t.tableComment}）`,
    value: t.id,
  }))
)

const unmappedFields = computed(() =>
  fields.value.filter((f) => !mappings.value.some((m) => m.fieldId === f.id))
)

const unmappedStandards = computed(() =>
  standards.value.filter((s) => !mappings.value.some((m) => m.standardId === s.id))
)

async function loadData() {
  loading.value = true
  try {
    const [stds, tbls] = await Promise.all([getDataStandards(), getMetadataTables()])
    standards.value = stds
    tables.value = tbls
    if (tbls.length > 0) {
      selectedTableId.value = tbls[0].id
      await loadFields(tbls[0].id)
    }
  } finally {
    loading.value = false
  }
}

async function loadFields(tableId: number) {
  mappings.value = []
  selectedFieldId.value = undefined
  selectedStandardId.value = undefined
  try {
    fields.value = await getTableFields(tableId)
  } catch {
    fields.value = []
  }
}

async function handleTableChange(val: number) {
  await loadFields(val)
}

// 点击字段
function clickField(f: MetadataField) {
  selectedFieldId.value = f.id
  tryAutoMatch()
}
// 点击标准
function clickStandard(s: DataStandard) {
  selectedStandardId.value = s.id
  tryAutoMatch()
}
function tryAutoMatch() {
  if (selectedFieldId.value && selectedStandardId.value) {
    addMapping(selectedFieldId.value, selectedStandardId.value)
    selectedFieldId.value = undefined
    selectedStandardId.value = undefined
  }
}
function addMapping(fieldId: number, standardId: number) {
  const f = fields.value.find((x) => x.id === fieldId)
  const s = standards.value.find((x) => x.id === standardId)
  if (!f || !s) return
  if (mappings.value.some((m) => m.fieldId === fieldId)) {
    MessagePlugin.warning(`字段「${f.fieldName}」已存在映射`)
    return
  }
  if (mappings.value.some((m) => m.standardId === standardId)) {
    MessagePlugin.warning(`标准「${s.name}」已被其他字段映射`)
    return
  }
  mappings.value.push({
    fieldId: f.id,
    fieldName: f.fieldName,
    fieldType: f.dataType,
    fieldComment: f.comment,
    standardId: s.id,
    standardName: s.name,
  })
}

// ============ 拖拽 ============
function onFieldDragStart(e: DragEvent, f: MetadataField) {
  draggingFieldId.value = f.id
  if (e.dataTransfer) {
    e.dataTransfer.effectAllowed = 'copy'
    e.dataTransfer.setData('text/fieldId', String(f.id))
  }
}
function onStandardDragStart(e: DragEvent, s: DataStandard) {
  draggingStandardId.value = s.id
  if (e.dataTransfer) {
    e.dataTransfer.effectAllowed = 'copy'
    e.dataTransfer.setData('text/standardId', String(s.id))
  }
}
function onMappingDrop(e: DragEvent) {
  e.preventDefault()
  const fieldId = Number(e.dataTransfer?.getData('text/fieldId'))
  const standardId = Number(e.dataTransfer?.getData('text/standardId'))
  // 字段拖入且当前已选中标准 -> 匹配
  if (fieldId && selectedStandardId.value) {
    addMapping(fieldId, selectedStandardId.value)
    selectedStandardId.value = undefined
    draggingFieldId.value = undefined
    return
  }
  if (standardId && selectedFieldId.value) {
    addMapping(selectedFieldId.value, standardId)
    selectedFieldId.value = undefined
    draggingStandardId.value = undefined
    return
  }
  if (fieldId && standardId) {
    addMapping(fieldId, standardId)
  }
  draggingFieldId.value = undefined
  draggingStandardId.value = undefined
}
function onDragOver(e: DragEvent) {
  e.preventDefault()
  if (e.dataTransfer) e.dataTransfer.dropEffect = 'copy'
}

function removeMapping(m: FieldStandardMapping) {
  mappings.value = mappings.value.filter((x) => x.fieldId !== m.fieldId)
}

// ============ 批量自动匹配 ============
// 简单相似度：基于字段名包含标准名拼音/英文片段、或业务含义包含
function similarity(field: MetadataField, std: DataStandard): number {
  const fn = field.fieldName.toLowerCase()
  const sn = std.name.toLowerCase()
  let score = 0
  // 名称包含
  if (fn.includes(sn) || sn.includes(fn)) score += 50
  // 业务含义包含标准名
  if (field.businessMeaning && field.businessMeaning.includes(std.name)) score += 30
  // 注释包含
  if (field.comment && field.comment.includes(std.name)) score += 20
  // 数据类型一致
  if (field.dataType.toUpperCase().includes(std.dataType.toUpperCase())) score += 10
  return score
}

function autoMatchAll() {
  let count = 0
  const usedStandards = new Set(mappings.value.map((m) => m.standardId))
  const usedFields = new Set(mappings.value.map((m) => m.fieldId))
  const candidates: { fieldId: number; standardId: number; score: number }[] = []
  fields.value.forEach((f) => {
    if (usedFields.has(f.id)) return
    standards.value.forEach((s) => {
      if (usedStandards.has(s.id)) return
      const score = similarity(f, s)
      if (score > 0) candidates.push({ fieldId: f.id, standardId: s.id, score })
    })
  })
  candidates.sort((a, b) => b.score - a.score)
  candidates.forEach((c) => {
    if (usedFields.has(c.fieldId) || usedStandards.has(c.standardId)) return
    addMapping(c.fieldId, c.standardId)
    usedFields.add(c.fieldId)
    usedStandards.add(c.standardId)
    count++
  })
  if (count === 0) {
    MessagePlugin.warning('未找到可自动匹配的字段-标准对')
  } else {
    MessagePlugin.success(`已自动匹配 ${count} 个字段`)
  }
}

function handleSave() {
  MessagePlugin.success(`已保存 ${mappings.value.length} 条映射关系`)
}

const mappingColumns = [
  { colKey: 'fieldName', title: '物理字段', width: 160, cell: 'fieldName' },
  { colKey: 'arrow', title: '', width: 50, cell: 'arrow' },
  { colKey: 'standardName', title: '数据标准', width: 160 },
  { colKey: 'op', title: '操作', width: 80, cell: 'op' },
]

onMounted(loadData)
</script>

<template>
  <div class="page-container" v-loading="loading">
    <div class="page-card">
      <div class="flex-between" style="margin-bottom: 12px">
        <h3 class="page-title" style="margin: 0">标准映射工作台</h3>
        <div class="flex gap-12 flex-center">
          <span class="text-secondary">选择数据源表：</span>
          <t-select
            v-model="selectedTableId"
            :options="tableOptions"
            style="width: 360px"
            @change="handleTableChange"
          />
        </div>
      </div>

      <div class="map-layout">
        <!-- 左侧：物理字段 -->
        <div class="map-col">
          <div class="map-col-head">
            <span class="map-col-title">
              <t-icon name="data-base" style="color: #0052d9" /> 物理字段
            </span>
            <t-tag size="small" theme="primary" variant="light">
              剩余 {{ unmappedFields.length }}
            </t-tag>
          </div>
          <div class="map-list">
            <div
              v-for="f in unmappedFields"
              :key="f.id"
              class="map-item field-item"
              :class="{ active: selectedFieldId === f.id }"
              draggable="true"
              @click="clickField(f)"
              @dragstart="onFieldDragStart($event, f)"
            >
              <div class="item-main">
                <span class="item-name">{{ f.fieldName }}</span>
                <t-tag size="small" variant="outline">{{ f.dataType }}</t-tag>
              </div>
              <div class="item-sub">{{ f.comment }}{{ f.businessMeaning ? ' · ' + f.businessMeaning : '' }}</div>
            </div>
            <div v-if="unmappedFields.length === 0" class="map-empty">
              <t-icon name="check-circle" size="28" style="color: #00a870" />
              <div>所有字段已映射</div>
            </div>
          </div>
        </div>

        <!-- 中间：已建立的映射 -->
        <div
          class="map-col middle-col"
          @dragover="onDragOver"
          @drop="onMappingDrop"
        >
          <div class="map-col-head">
            <span class="map-col-title">
              <t-icon name="link" style="color: #7265e6" /> 映射关系
            </span>
            <t-tag size="small" theme="success" variant="light">
              已映射 {{ mappings.length }}
            </t-tag>
          </div>
          <div class="map-list">
            <div v-for="m in mappings" :key="m.fieldId" class="mapping-row">
              <div class="mapping-side">
                <span class="mapping-name">{{ m.fieldName }}</span>
                <t-tag size="small" variant="outline">{{ m.fieldType }}</t-tag>
              </div>
              <t-icon name="arrow-right" class="mapping-arrow" />
              <div class="mapping-side right">
                <t-icon name="edit-1" style="color: #7265e6" />
                <span class="mapping-name">{{ m.standardName }}</span>
              </div>
              <t-icon
                name="close"
                class="mapping-del"
                @click="removeMapping(m)"
              />
            </div>
            <div v-if="mappings.length === 0" class="map-empty drop-tip">
              <t-icon name="drag-move" size="32" style="color: #8c8c8c" />
              <div>拖拽左侧字段与右侧标准至此</div>
              <div class="text-secondary" style="font-size: 12px; margin-top: 4px">
                或依次点击字段与标准自动建立映射
              </div>
            </div>
          </div>
        </div>

        <!-- 右侧：数据标准 -->
        <div class="map-col">
          <div class="map-col-head">
            <span class="map-col-title">
              <t-icon name="edit-1" style="color: #ed7b2f" /> 数据标准
            </span>
            <t-tag size="small" theme="warning" variant="light">
              剩余 {{ unmappedStandards.length }}
            </t-tag>
          </div>
          <div class="map-list">
            <div
              v-for="s in unmappedStandards"
              :key="s.id"
              class="map-item standard-item"
              :class="{ active: selectedStandardId === s.id }"
              draggable="true"
              @click="clickStandard(s)"
              @dragstart="onStandardDragStart($event, s)"
            >
              <div class="item-main">
                <span class="item-name">{{ s.name }}</span>
                <t-tag size="small" variant="outline">{{ s.dataType }}{{ s.length ? `(${s.length})` : '' }}</t-tag>
              </div>
              <div class="item-sub">{{ s.businessMeaning }}</div>
            </div>
            <div v-if="unmappedStandards.length === 0" class="map-empty">
              <t-icon name="check-circle" size="28" style="color: #00a870" />
              <div>所有标准已映射</div>
            </div>
          </div>
        </div>
      </div>

      <div class="flex-between" style="margin-top: 16px">
        <t-button theme="default" variant="outline" @click="autoMatchAll">
          <template #icon><t-icon name="magic" /></template>
          批量自动匹配
        </t-button>
        <t-button theme="primary" @click="handleSave">
          <template #icon><t-icon name="save" /></template>
          保存映射
        </t-button>
      </div>
    </div>
  </div>
</template>

<style scoped>
.page-title {
  margin: 0;
}
.map-layout {
  display: grid;
  grid-template-columns: 1fr 1.2fr 1fr;
  gap: 12px;
  min-height: 480px;
}
.map-col {
  background: #fafbfc;
  border: 1px solid var(--border-color);
  border-radius: 6px;
  display: flex;
  flex-direction: column;
  min-height: 480px;
}
.middle-col {
  background: #f5f7fa;
  border: 1px dashed #c5c7cb;
}
.map-col-head {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 10px 12px;
  border-bottom: 1px solid var(--border-color);
}
.map-col-title {
  font-size: 13px;
  font-weight: 600;
  display: inline-flex;
  align-items: center;
  gap: 6px;
}
.map-list {
  flex: 1;
  overflow-y: auto;
  padding: 8px;
}
.map-item {
  background: #fff;
  border: 1px solid var(--border-color);
  border-radius: 4px;
  padding: 8px 10px;
  margin-bottom: 6px;
  cursor: grab;
  transition: all 0.15s;
}
.map-item:hover {
  border-color: var(--brand-color);
  box-shadow: 0 2px 6px rgba(0, 82, 217, 0.1);
}
.map-item:active {
  cursor: grabbing;
}
.map-item.active {
  border-color: var(--brand-color);
  background: rgba(0, 82, 217, 0.05);
}
.item-main {
  display: flex;
  justify-content: space-between;
  align-items: center;
}
.item-name {
  font-size: 13px;
  font-weight: 500;
  color: var(--text-primary);
}
.item-sub {
  font-size: 12px;
  color: var(--text-secondary);
  margin-top: 4px;
  line-height: 1.4;
}
.map-empty {
  text-align: center;
  padding: 40px 12px;
  color: var(--text-secondary);
  font-size: 13px;
}
.map-empty div {
  margin-top: 6px;
}
.drop-tip {
  padding: 80px 12px;
}
.mapping-row {
  display: flex;
  align-items: center;
  gap: 8px;
  background: #fff;
  border: 1px solid var(--border-color);
  border-radius: 4px;
  padding: 8px 10px;
  margin-bottom: 6px;
}
.mapping-side {
  display: flex;
  align-items: center;
  gap: 6px;
  flex: 1;
  min-width: 0;
}
.mapping-side.right {
  justify-content: flex-end;
}
.mapping-name {
  font-size: 13px;
  font-weight: 500;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}
.mapping-arrow {
  color: var(--brand-color);
  flex-shrink: 0;
}
.mapping-del {
  color: var(--text-secondary);
  cursor: pointer;
  flex-shrink: 0;
  transition: color 0.15s;
}
.mapping-del:hover {
  color: var(--danger);
}
</style>
