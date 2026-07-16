<script setup lang="ts">
import { ref, reactive, computed, onMounted } from 'vue'
import { MessagePlugin } from 'tdesign-vue-next'
import { getClassifyLabels, getMetadataTables, getTableFields } from '@/api'
import type { ClassifyLabel, MetadataTable, MetadataField } from '@/types'

const loading = ref(true)
const labels = ref<ClassifyLabel[]>([])
const tables = ref<MetadataTable[]>([])
const fields = ref<MetadataField[]>([])

const selectedLabel = ref<ClassifyLabel | null>(null)
const actived = ref<Array<string | number>>([])

const dialogVisible = ref(false)
const form = reactive({
  name: '',
  level: 'L3' as ClassifyLabel['level'],
  rules: '',
})
const formRules = {
  name: [{ required: true, message: '请输入名称', type: 'error' }],
  level: [{ required: true, message: '请选择级别', type: 'error' }],
}

const levelColors: Record<string, string> = {
  L1: '#8c8c8c',
  L2: '#0052d9',
  L3: '#ed7b2f',
  L4: '#e34d59',
}
const levelNames: Record<string, string> = {
  L1: '公开级',
  L2: '内部级',
  L3: '敏感级',
  L4: '高敏级',
}
const levelOptions = ['L1', 'L2', 'L3', 'L4'].map((lv) => ({
  value: lv,
  label: `${lv} ${levelNames[lv]}`,
}))

const levelStats = computed(() => {
  const levels = ['L1', 'L2', 'L3', 'L4'] as const
  return levels.map((lv) => {
    const label = labels.value.find((l) => l.level === lv)
    return {
      level: lv,
      label: levelNames[lv],
      count: label?.fieldCount || 0,
      color: levelColors[lv],
    }
  })
})

const treeData = computed(() => {
  const levels = ['L1', 'L2', 'L3', 'L4'] as const
  return levels.map((lv) => ({
    label: `${lv} ${levelNames[lv]}`,
    value: lv,
    expanded: true,
    children: labels.value
      .filter((l) => l.level === lv)
      .map((l) => ({ label: l.name, value: String(l.id) })),
  }))
})

function guessLevel(fieldName: string): string {
  if (/id_?card|id_?no|bank|card_?no/i.test(fieldName)) return 'L4'
  if (/phone|mobile|email|mail|address|real_?name/i.test(fieldName)) return 'L3'
  if (/amount|money|price|salary|balance/i.test(fieldName)) return 'L2'
  return 'L1'
}

const sensitiveFields = computed(() => {
  return fields.value
    .filter((f) => f.isSensitive === 1)
    .map((f) => {
      const table = tables.value.find((t) => t.id === f.tableId)
      return {
        id: f.id,
        tableName: table?.tableName || '-',
        fieldName: f.fieldName,
        dataType: f.dataType,
        comment: f.comment,
        level: guessLevel(f.fieldName),
      }
    })
})

function onTreeActive(value: unknown) {
  const v = Array.isArray(value) ? value[value.length - 1] : value
  if (v == null) return
  actived.value = [v]
  const found = labels.value.find((l) => String(l.id) === String(v))
  if (found) selectedLabel.value = found
}

function handleScan() {
  MessagePlugin.success('敏感扫描任务已启动')
}

function openCreate() {
  form.name = ''
  form.level = 'L3'
  form.rules = ''
  dialogVisible.value = true
}

function onDialogConfirm() {
  if (!form.name.trim()) {
    MessagePlugin.warning('请输入分级名称')
    return
  }
  const newLabel: ClassifyLabel = {
    id: Date.now(),
    name: form.name.trim(),
    level: form.level,
    rules: form.rules,
    fieldCount: 0,
  }
  labels.value.push(newLabel)
  selectedLabel.value = newLabel
  actived.value = [String(newLabel.id)]
  dialogVisible.value = false
  MessagePlugin.success('分级标签已创建')
}

onMounted(async () => {
  const [ls, ts, fs] = await Promise.all([
    getClassifyLabels(),
    getMetadataTables(),
    getTableFields(1),
  ])
  labels.value = ls
  tables.value = ts
  fields.value = fs
  if (ls.length) {
    selectedLabel.value = ls[0]
    actived.value = [String(ls[0].id)]
  }
  loading.value = false
})
</script>

<template>
  <div class="page-container" v-loading="loading">
    <!-- 顶部分级统计卡片 -->
    <div class="stat-grid">
      <div
        v-for="s in levelStats"
        :key="s.level"
        class="stat-card"
        :style="{ borderTopColor: s.color }"
      >
        <div class="stat-head">
          <span class="stat-level" :style="{ color: s.color }">{{ s.level }}</span>
          <span class="stat-label">{{ s.label }}</span>
        </div>
        <div class="stat-value">{{ s.count.toLocaleString('zh-CN') }}</div>
        <div class="stat-foot">字段数量</div>
      </div>
    </div>

    <!-- 操作栏 -->
    <div class="page-card">
      <div class="flex-between">
        <span class="card-label">分级标签管理</span>
        <div class="flex gap-12">
          <t-button theme="primary" variant="outline" @click="handleScan">
            <template #icon><t-icon name="scan" /></template>
            发起扫描
          </t-button>
          <t-button theme="primary" @click="openCreate">
            <template #icon><t-icon name="add" /></template>
            新建分级
          </t-button>
        </div>
      </div>
    </div>

    <!-- 主体 -->
    <div class="main-grid">
      <!-- 左侧标签树 -->
      <div class="page-card tree-card">
        <div class="card-label" style="margin-bottom: 12px">分级标签树</div>
        <t-tree
          :data="treeData"
          activable
          :actived="actived"
          :default-expanded="['L1', 'L2', 'L3', 'L4']"
          :keys="{ label: 'label', value: 'value' }"
          @active="onTreeActive"
        />
      </div>

      <!-- 右侧详情 -->
      <div class="right-area">
        <div class="page-card">
          <div class="card-label" style="margin-bottom: 12px">标签详情</div>
          <template v-if="selectedLabel">
            <div class="detail-row">
              <span class="detail-label">名称</span>
              <span class="detail-value">{{ selectedLabel.name }}</span>
            </div>
            <div class="detail-row">
              <span class="detail-label">级别</span>
              <t-tag :color="levelColors[selectedLabel.level]" variant="light">
                {{ selectedLabel.level }} {{ levelNames[selectedLabel.level] }}
              </t-tag>
            </div>
            <div class="detail-row">
              <span class="detail-label">字段数</span>
              <span class="detail-value">{{ selectedLabel.fieldCount.toLocaleString('zh-CN') }}</span>
            </div>
            <div class="detail-row detail-block">
              <span class="detail-label">识别规则</span>
              <div class="detail-rules">{{ selectedLabel.rules }}</div>
            </div>
          </template>
          <t-empty v-else description="请选择左侧标签查看详情" />
        </div>

        <div class="page-card">
          <div class="flex-between" style="margin-bottom: 12px">
            <span class="card-label">敏感字段列表</span>
            <t-tag theme="danger" variant="light">{{ sensitiveFields.length }} 个</t-tag>
          </div>
          <t-table
            :data="sensitiveFields"
            row-key="id"
            size="small"
            :pagination="{ show: false }"
            :columns="[
              { colKey: 'tableName', title: '表名', ellipsis: true },
              { colKey: 'fieldName', title: '字段名', width: 130 },
              { colKey: 'dataType', title: '类型', width: 130 },
              { colKey: 'comment', title: '说明', ellipsis: true },
              { colKey: 'level', title: '级别', width: 90, cell: 'level' },
            ]"
          >
            <template #level="{ row }">
              <t-tag size="small" :color="levelColors[row.level]" variant="light">{{ row.level }}</t-tag>
            </template>
          </t-table>
        </div>
      </div>
    </div>

    <!-- 新建分级弹窗 -->
    <t-dialog
      v-model:visible="dialogVisible"
      header="新建分级标签"
      width="480px"
      @confirm="onDialogConfirm"
    >
      <t-form :data="form" :rules="formRules" label-align="top">
        <t-form-item name="name" label="名称">
          <t-input v-model="form.name" placeholder="请输入分级名称" clearable />
        </t-form-item>
        <t-form-item name="level" label="级别">
          <t-select v-model="form.level" :options="levelOptions" />
        </t-form-item>
        <t-form-item name="rules" label="识别规则">
          <t-textarea
            v-model="form.rules"
            placeholder="请输入识别规则(正则/说明)"
            :autosize="{ minRows: 3, maxRows: 6 }"
          />
        </t-form-item>
      </t-form>
    </t-dialog>
  </div>
</template>

<style scoped>
.stat-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 16px;
  margin-bottom: 16px;
}
.stat-card {
  background: #fff;
  border-radius: 6px;
  padding: 18px 20px;
  border-top: 3px solid #8c8c8c;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.04);
}
.stat-head {
  display: flex;
  align-items: baseline;
  gap: 8px;
}
.stat-level {
  font-size: 20px;
  font-weight: 700;
}
.stat-label {
  font-size: 13px;
  color: var(--text-secondary);
}
.stat-value {
  font-size: 26px;
  font-weight: 600;
  margin-top: 8px;
  color: var(--text-primary);
}
.stat-foot {
  font-size: 12px;
  color: var(--text-secondary);
  margin-top: 4px;
}
.main-grid {
  display: grid;
  grid-template-columns: 280px 1fr;
  gap: 16px;
  align-items: start;
}
.tree-card {
  margin-bottom: 0;
}
.right-area {
  display: flex;
  flex-direction: column;
  gap: 16px;
}
.right-area .page-card {
  margin-bottom: 0;
}
.card-label {
  font-size: 15px;
  font-weight: 600;
  color: var(--text-primary);
}
.detail-row {
  display: flex;
  align-items: center;
  padding: 8px 0;
}
.detail-label {
  width: 80px;
  color: var(--text-secondary);
  font-size: 13px;
  flex-shrink: 0;
}
.detail-value {
  color: var(--text-primary);
  font-weight: 500;
}
.detail-block {
  align-items: flex-start;
}
.detail-rules {
  flex: 1;
  background: #f5f7fa;
  border-radius: 4px;
  padding: 8px 12px;
  font-family: 'Courier New', monospace;
  font-size: 12px;
  color: var(--text-secondary);
  white-space: pre-wrap;
  word-break: break-all;
  line-height: 1.6;
}
</style>
