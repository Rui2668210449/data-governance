<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { MessagePlugin, DialogPlugin } from 'tdesign-vue-next'
import {
  getQualityRules,
  saveQualityRule,
  trialQualityRule,
} from '@/api'
import type { QualityRule } from '@/types'

const loading = ref(false)
const rules = ref<QualityRule[]>([])

const dimensionFilter = ref('')
const keyword = ref('')

const dimensionOptions = [
  { label: '全部维度', value: '' },
  { label: '完整性', value: '完整性' },
  { label: '唯一性', value: '唯一性' },
  { label: '准确性', value: '准确性' },
  { label: '规范性', value: '规范性' },
  { label: '时效性', value: '时效性' },
]

const typeOptions = [
  { label: '模板规则', value: 'TEMPLATE' },
  { label: 'SQL规则', value: 'SQL' },
]

const severityOptions = [
  { label: '高', value: 1 },
  { label: '中', value: 2 },
  { label: '低', value: 3 },
]

const dimensionColorMap: Record<string, string> = {
  完整性: '#0052d9',
  唯一性: '#00a870',
  准确性: '#e34d59',
  规范性: '#ed7b2f',
  时效性: '#7265e6',
}

const severityTagMap: Record<number, { text: string; theme: 'danger' | 'warning' | 'primary' }> = {
  1: { text: '高', theme: 'danger' },
  2: { text: '中', theme: 'warning' },
  3: { text: '低', theme: 'primary' },
}

const filteredRules = computed(() => {
  return rules.value.filter((r) => {
    const dimOk = !dimensionFilter.value || r.dimension === dimensionFilter.value
    const kwOk = !keyword.value || r.name.includes(keyword.value) || r.targetTable.includes(keyword.value)
    return dimOk && kwOk
  })
})

const columns = [
  { colKey: 'name', title: '规则名称', minWidth: 180, ellipsis: true },
  { colKey: 'dimension', title: '维度', width: 100, cell: 'dimension' },
  { colKey: 'ruleType', title: '类型', width: 100, cell: 'ruleType' },
  { colKey: 'targetTable', title: '目标表', minWidth: 160, ellipsis: true },
  { colKey: 'severity', title: '严重程度', width: 100, cell: 'severity' },
  { colKey: 'lastScore', title: '最近评分', width: 160, cell: 'lastScore' },
  { colKey: 'enabled', title: '状态', width: 90, cell: 'enabled' },
  { colKey: 'op', title: '操作', width: 200, cell: 'op', fixed: 'right' as const },
]

function scoreColor(score: number) {
  if (score >= 95) return '#00a870'
  if (score >= 85) return '#ed7b2f'
  return '#e34d59'
}

async function loadData() {
  loading.value = true
  try {
    rules.value = await getQualityRules()
  } finally {
    loading.value = false
  }
}

async function handleToggleEnabled(row: QualityRule, val: boolean) {
  row.enabled = val
  await saveQualityRule({ ...row, enabled: val })
  MessagePlugin.success(val ? '规则已启用' : '规则已停用')
}

// ============ 新建/编辑弹窗 ============
const dialogVisible = ref(false)
const dialogMode = ref<'create' | 'edit'>('create')
const currentStep = ref(0)
const submitting = ref(false)

const defaultForm: QualityRule = {
  id: 0,
  name: '',
  dimension: '完整性',
  ruleType: 'SQL',
  ruleSql: '',
  targetTable: '',
  severity: 2,
  enabled: true,
  lastScore: 0,
  createTime: '',
}

const form = ref<QualityRule>({ ...defaultForm })

function openCreate() {
  dialogMode.value = 'create'
  currentStep.value = 0
  form.value = { ...defaultForm, id: 0, createTime: '' }
  dialogVisible.value = true
}

function openEdit(row: QualityRule) {
  dialogMode.value = 'edit'
  currentStep.value = 0
  form.value = { ...row }
  dialogVisible.value = true
}

function nextStep() {
  if (currentStep.value === 0) {
    if (!form.value.name || !form.value.targetTable) {
      MessagePlugin.warning('请填写规则名称和目标表')
      return
    }
  }
  if (currentStep.value === 1) {
    if (!form.value.ruleSql) {
      MessagePlugin.warning('请填写规则SQL')
      return
    }
  }
  if (currentStep.value < 2) currentStep.value += 1
}

function prevStep() {
  if (currentStep.value > 0) currentStep.value -= 1
}

async function handleSubmit() {
  submitting.value = true
  try {
    await saveQualityRule(form.value)
    MessagePlugin.success(dialogMode.value === 'create' ? '规则创建成功' : '规则更新成功')
    dialogVisible.value = false
    await loadData()
  } finally {
    submitting.value = false
  }
}

function handleDelete(row: QualityRule) {
  const confirm = DialogPlugin.confirm({
    header: '删除确认',
    body: `确认删除规则「${row.name}」吗？`,
    confirmBtn: '删除',
    cancelBtn: '取消',
    theme: 'danger',
    onConfirm: async () => {
      rules.value = rules.value.filter((r) => r.id !== row.id)
      MessagePlugin.success('规则已删除')
      confirm.destroy()
    },
  })
}

// ============ 试跑 ============
const trialVisible = ref(false)
const trialLoading = ref(false)
const trialResult = ref<{ pass: number; fail: number; score: number } | null>(null)
const trialRule = ref<QualityRule | null>(null)

async function handleTrial(row: QualityRule) {
  trialRule.value = row
  trialResult.value = null
  trialVisible.value = true
  trialLoading.value = true
  try {
    const res = await trialQualityRule(row.id)
    trialResult.value = res
    row.lastScore = res.score
  } finally {
    trialLoading.value = false
  }
}

onMounted(loadData)
</script>

<template>
  <div class="page-container" v-loading="loading">
    <div class="page-card">
      <div class="flex-between" style="margin-bottom: 16px">
        <h3 class="page-title" style="margin: 0">质量规则配置</h3>
        <t-button theme="primary" @click="openCreate">
          <template #icon><t-icon name="add" /></template>
          新建规则
        </t-button>
      </div>

      <div class="flex gap-12" style="margin-bottom: 12px">
        <t-select
          v-model="dimensionFilter"
          :options="dimensionOptions"
          style="width: 160px"
          placeholder="维度筛选"
        />
        <t-input
          v-model="keyword"
          placeholder="搜索规则名称 / 目标表"
          clearable
          style="width: 280px"
        >
          <template #prefix-icon><t-icon name="search" /></template>
        </t-input>
      </div>

      <t-table
        :data="filteredRules"
        row-key="id"
        :columns="columns"
        size="small"
        :pagination="{ show: false }"
        bordered
      >
        <template #dimension="{ row }">
          <t-tag :color="dimensionColorMap[row.dimension]" variant="light" size="small">
            {{ row.dimension }}
          </t-tag>
        </template>
        <template #ruleType="{ row }">
          <t-tag size="small" variant="outline">
            {{ row.ruleType === 'TEMPLATE' ? '模板' : 'SQL' }}
          </t-tag>
        </template>
        <template #severity="{ row }">
          <t-tag :theme="severityTagMap[row.severity].theme" variant="light" size="small">
            {{ severityTagMap[row.severity].text }}
          </t-tag>
        </template>
        <template #lastScore="{ row }">
          <div class="score-cell">
            <div class="score-bar">
              <div
                class="score-bar-inner"
                :style="{ width: row.lastScore + '%', background: scoreColor(row.lastScore) }"
              ></div>
            </div>
            <span class="score-num" :style="{ color: scoreColor(row.lastScore) }">
              {{ row.lastScore.toFixed(1) }}
            </span>
          </div>
        </template>
        <template #enabled="{ row }">
          <t-switch
            :value="row.enabled"
            size="small"
            @change="(val: boolean) => handleToggleEnabled(row, val)"
          />
        </template>
        <template #op="{ row }">
          <t-button theme="primary" variant="text" size="small" @click="handleTrial(row)">
            试跑
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
      :header="dialogMode === 'create' ? '新建质量规则' : '编辑质量规则'"
      :footer="false"
      width="680px"
      :close-on-overlay-click="false"
    >
      <t-steps :current="currentStep" style="margin-bottom: 24px">
        <t-step-item title="基本信息" />
        <t-step-item title="规则配置" />
        <t-step-item title="确认" />
      </t-steps>

      <!-- 步骤1: 基本信息 -->
      <div v-show="currentStep === 0">
        <t-form :data="form" label-align="top">
          <t-form-item label="规则名称" name="name">
            <t-input v-model="form.name" placeholder="请输入规则名称" />
          </t-form-item>
          <div class="form-row">
            <t-form-item label="质量维度" name="dimension">
              <t-select v-model="form.dimension" :options="dimensionOptions.filter(o => o.value)" />
            </t-form-item>
            <t-form-item label="规则类型" name="ruleType">
              <t-select v-model="form.ruleType" :options="typeOptions" />
            </t-form-item>
          </div>
          <t-form-item label="目标表" name="targetTable">
            <t-input v-model="form.targetTable" placeholder="如 biz_core.user_info" />
          </t-form-item>
          <t-form-item label="严重程度" name="severity">
            <t-select v-model="form.severity" :options="severityOptions" />
          </t-form-item>
        </t-form>
      </div>

      <!-- 步骤2: 规则配置 -->
      <div v-show="currentStep === 1">
        <t-form :data="form" label-align="top">
          <t-form-item label="规则SQL" name="ruleSql">
            <t-textarea
              v-model="form.ruleSql"
              placeholder="SELECT COUNT(*) FROM table WHERE ..."
              :autosize="{ minRows: 8, maxRows: 16 }"
            />
          </t-form-item>
          <div class="sql-tip">
            <t-icon name="info-circle" />
            <span>规则SQL需返回数值或异常记录数，系统据此计算通过/失败比例</span>
          </div>
        </t-form>
      </div>

      <!-- 步骤3: 确认 -->
      <div v-show="currentStep === 2">
        <t-descriptions :column="1" bordered>
          <t-descriptions-item label="规则名称">{{ form.name }}</t-descriptions-item>
          <t-descriptions-item label="质量维度">{{ form.dimension }}</t-descriptions-item>
          <t-descriptions-item label="规则类型">{{ form.ruleType === 'TEMPLATE' ? '模板' : 'SQL' }}</t-descriptions-item>
          <t-descriptions-item label="目标表">{{ form.targetTable }}</t-descriptions-item>
          <t-descriptions-item label="严重程度">{{ severityTagMap[form.severity].text }}</t-descriptions-item>
          <t-descriptions-item label="规则SQL">
            <pre class="sql-preview">{{ form.ruleSql }}</pre>
          </t-descriptions-item>
        </t-descriptions>
      </div>

      <div class="flex-between" style="margin-top: 24px">
        <t-button variant="outline" :disabled="currentStep === 0" @click="prevStep">
          上一步
        </t-button>
        <div class="flex gap-12">
          <t-button variant="outline" @click="dialogVisible = false">取消</t-button>
          <t-button v-if="currentStep < 2" theme="primary" @click="nextStep">下一步</t-button>
          <t-button v-else theme="primary" :loading="submitting" @click="handleSubmit">
            {{ dialogMode === 'create' ? '提交创建' : '保存修改' }}
          </t-button>
        </div>
      </div>
    </t-dialog>

    <!-- 试跑结果弹窗 -->
    <t-dialog
      v-model:visible="trialVisible"
      header="规则试跑结果"
      :footer="false"
      width="480px"
    >
      <div v-loading="trialLoading" style="min-height: 160px">
        <template v-if="trialResult">
          <div class="trial-rule-name">
            <t-icon name="check-circle" style="color: #0052d9" />
            <span>{{ trialRule?.name }}</span>
          </div>
          <div class="trial-grid">
            <div class="trial-cell">
              <div class="trial-cell-value" style="color: #00a870">{{ trialResult.pass.toLocaleString() }}</div>
              <div class="trial-cell-label">通过数</div>
            </div>
            <div class="trial-cell">
              <div class="trial-cell-value" style="color: #e34d59">{{ trialResult.fail.toLocaleString() }}</div>
              <div class="trial-cell-label">失败数</div>
            </div>
            <div class="trial-cell">
              <div class="trial-cell-value" :style="{ color: scoreColor(trialResult.score) }">
                {{ trialResult.score.toFixed(2) }}
              </div>
              <div class="trial-cell-label">评分</div>
            </div>
          </div>
          <div class="trial-status">
            <t-tag
              :theme="trialResult.score >= 90 ? 'success' : trialResult.score >= 80 ? 'warning' : 'danger'"
              variant="light"
            >
              {{ trialResult.score >= 90 ? '通过' : trialResult.score >= 80 ? '警告' : '不通过' }}
            </t-tag>
          </div>
        </template>
      </div>
      <div class="flex-center" style="justify-content: flex-end; margin-top: 16px">
        <t-button theme="primary" @click="trialVisible = false">关闭</t-button>
      </div>
    </t-dialog>
  </div>
</template>

<style scoped>
.score-cell {
  display: flex;
  align-items: center;
  gap: 8px;
}
.score-bar {
  flex: 1;
  height: 6px;
  background: #f0f0f0;
  border-radius: 3px;
  overflow: hidden;
  min-width: 80px;
}
.score-bar-inner {
  height: 100%;
  border-radius: 3px;
  transition: width 0.3s;
}
.score-num {
  font-weight: 600;
  font-size: 13px;
  min-width: 36px;
  text-align: right;
}
.form-row {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 12px;
}
.sql-tip {
  display: flex;
  align-items: center;
  gap: 6px;
  color: var(--text-secondary);
  font-size: 12px;
  padding: 8px 12px;
  background: #f5f7fa;
  border-radius: 4px;
}
.sql-preview {
  margin: 0;
  font-family: 'Consolas', 'Monaco', monospace;
  font-size: 12px;
  background: #f5f7fa;
  padding: 8px;
  border-radius: 4px;
  white-space: pre-wrap;
  word-break: break-all;
  max-height: 160px;
  overflow: auto;
}
.trial-rule-name {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 15px;
  font-weight: 600;
  margin-bottom: 20px;
}
.trial-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 12px;
  margin-bottom: 16px;
}
.trial-cell {
  background: #f5f7fa;
  border-radius: 6px;
  padding: 16px;
  text-align: center;
}
.trial-cell-value {
  font-size: 24px;
  font-weight: 600;
  line-height: 1.2;
}
.trial-cell-label {
  font-size: 12px;
  color: var(--text-secondary);
  margin-top: 4px;
}
.trial-status {
  text-align: center;
}
</style>
