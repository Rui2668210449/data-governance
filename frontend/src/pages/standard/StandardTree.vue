<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { MessagePlugin } from 'tdesign-vue-next'
import { getDataStandards } from '@/api'
import type { DataStandard } from '@/types'

const loading = ref(false)
const standards = ref<DataStandard[]>([])
const selectedCategory = ref<string>('用户域')
const selectedVersion = ref<string>('全部版本')
const keyword = ref('')

const statusMap: Record<string, { text: string; color: string }> = {
  DRAFT: { text: '草稿', color: '#8c8c8c' },
  PUBLISHED: { text: '已发布', color: '#00a870' },
  DEPRECATED: { text: '废弃', color: '#e34d59' },
}

// 分类树：从 standards 的 category 字段聚合
const treeData = computed(() => {
  const map = new Map<string, number>()
  standards.value.forEach((s) => {
    map.set(s.category, (map.get(s.category) || 0) + 1)
  })
  return [
    {
      label: '全部标准',
      value: '全部',
      children: Array.from(map.entries()).map(([cat, count]) => ({
        label: `${cat} (${count})`,
        value: cat,
      })),
    },
  ]
})

const versionOptions = computed(() => {
  const set = new Set<string>(['全部版本'])
  standards.value.forEach((s) => set.add(s.version))
  return Array.from(set).map((v) => ({ label: v, value: v }))
})

const filteredStandards = computed(() => {
  return standards.value.filter((s) => {
    const catOk = selectedCategory.value === '全部' || s.category === selectedCategory.value
    const verOk = selectedVersion.value === '全部版本' || s.version === selectedVersion.value
    const kwOk = !keyword.value || s.name.includes(keyword.value) || s.businessMeaning.includes(keyword.value)
    return catOk && verOk && kwOk
  })
})

async function loadData() {
  loading.value = true
  try {
    standards.value = await getDataStandards()
  } finally {
    loading.value = false
  }
}

function handleSelect(node: { value: string }) {
  selectedCategory.value = node.value
}

// ============ 新建标准弹窗 ============
const dialogVisible = ref(false)
const submitting = ref(false)
const form = ref({
  name: '',
  category: '用户域',
  dataType: 'VARCHAR',
  length: 64,
  businessMeaning: '',
})

const categoryOptions = [
  { label: '用户域', value: '用户域' },
  { label: '交易域', value: '交易域' },
]
const dataTypeOptions = [
  { label: 'BIGINT', value: 'BIGINT' },
  { label: 'VARCHAR', value: 'VARCHAR' },
  { label: 'DECIMAL', value: 'DECIMAL' },
  { label: 'DATETIME', value: 'DATETIME' },
  { label: 'TINYINT', value: 'TINYINT' },
]

const formRules = {
  name: [{ required: true, message: '请输入标准名称', type: 'error' as const }],
  category: [{ required: true, message: '请选择分类', type: 'error' as const }],
  dataType: [{ required: true, message: '请选择数据类型', type: 'error' as const }],
}

function openCreate() {
  form.value = {
    name: '',
    category: '用户域',
    dataType: 'VARCHAR',
    length: 64,
    businessMeaning: '',
  }
  dialogVisible.value = true
}

async function handleSubmit() {
  if (!form.value.name) {
    MessagePlugin.warning('请输入标准名称')
    return
  }
  submitting.value = true
  try {
    // 本地新增（无后端保存接口，使用 getDataStandards 后插入）
    const newId = standards.value.length
      ? Math.max(...standards.value.map((s) => s.id)) + 1
      : 1
    standards.value.push({
      id: newId,
      name: form.value.name,
      category: form.value.category,
      dataType: form.value.dataType,
      length: Number(form.value.length) || 0,
      businessMeaning: form.value.businessMeaning,
      version: 'v1.0',
      status: 'DRAFT',
      mappingCount: 0,
      complianceRate: 0,
    })
    MessagePlugin.success('标准创建成功')
    dialogVisible.value = false
  } finally {
    submitting.value = false
  }
}

function rateColor(rate: number) {
  if (rate >= 90) return '#00a870'
  if (rate >= 60) return '#ed7b2f'
  return '#e34d59'
}

onMounted(loadData)
</script>

<template>
  <div class="page-container" v-loading="loading">
    <div class="std-layout">
      <!-- 左侧：分类树 -->
      <div class="page-card tree-panel">
        <div class="flex-between" style="margin-bottom: 12px">
          <span class="card-label">标准分类</span>
          <t-icon name="edit-1" style="color: #7265e6" />
        </div>
        <t-input
          v-model="keyword"
          placeholder="搜索标准名称/含义"
          clearable
          size="small"
          style="margin-bottom: 12px"
        >
          <template #prefix-icon><t-icon name="search" /></template>
        </t-input>
        <t-tree
          :data="treeData"
          :keys="{ value: 'value', label: 'label', children: 'children'}"
          :default-expanded="['全部']"
          :default-selected="selectedCategory"
          :activable="true"
          expand-on-click-node
          @click="handleSelect"
        />
        <div class="tree-stat">
          <div class="tree-stat-item">
            <span class="tree-stat-num">{{ standards.length }}</span>
            <span class="tree-stat-label">标准总数</span>
          </div>
          <div class="tree-stat-item">
            <span class="tree-stat-num" style="color: #00a870">
              {{ standards.filter(s => s.status === 'PUBLISHED').length }}
            </span>
            <span class="tree-stat-label">已发布</span>
          </div>
          <div class="tree-stat-item">
            <span class="tree-stat-num" style="color: #8c8c8c">
              {{ standards.filter(s => s.status === 'DRAFT').length }}
            </span>
            <span class="tree-stat-label">草稿</span>
          </div>
        </div>
      </div>

      <!-- 右侧：标准卡片列表 -->
      <div class="page-card right-panel">
        <div class="flex-between" style="margin-bottom: 16px">
          <div class="flex gap-12 flex-center">
            <h3 class="page-title" style="margin: 0">
              {{ selectedCategory === '全部' ? '全部标准' : selectedCategory }}
              <span class="count-badge">{{ filteredStandards.length }}</span>
            </h3>
            <t-select
              v-model="selectedVersion"
              :options="versionOptions"
              size="small"
              style="width: 140px"
            />
          </div>
          <t-button theme="primary" @click="openCreate">
            <template #icon><t-icon name="add" /></template>
            新建标准
          </t-button>
        </div>

        <div v-if="filteredStandards.length === 0" class="empty-tip">
          <t-icon name="info-circle" size="32" />
          <div>该分类下暂无标准</div>
        </div>

        <div class="card-grid">
          <div v-for="std in filteredStandards" :key="std.id" class="std-card">
            <div class="std-card-head">
              <div class="std-name">{{ std.name }}</div>
              <t-tag
                size="small"
                :color="statusMap[std.status].color"
                variant="light"
              >
                {{ statusMap[std.status].text }}
              </t-tag>
            </div>
            <div class="std-meta">
              <span class="meta-chip">
                <t-icon name="edit-1" size="12px" /> {{ std.dataType }}{{ std.length ? `(${std.length})` : '' }}
              </span>
              <span class="meta-chip">
                <t-icon name="version" size="12px" /> {{ std.version }}
              </span>
            </div>
            <div class="std-meaning">{{ std.businessMeaning }}</div>
            <div class="std-card-foot">
              <div class="foot-item">
                <span class="foot-label">映射数</span>
                <span class="foot-num">{{ std.mappingCount }}</span>
              </div>
              <div class="foot-item flex-1">
                <span class="foot-label">落标率</span>
                <t-progress
                  :percentage="std.complianceRate"
                  :color="rateColor(std.complianceRate)"
                  size="small"
                  style="flex: 1; margin-left: 8px"
                />
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- 新建标准弹窗 -->
    <t-dialog
      v-model:visible="dialogVisible"
      header="新建数据标准"
      :confirm-btn="{ content: '创建', loading: submitting, onClick: handleSubmit }"
      cancel-btn="取消"
      width="520px"
    >
      <t-form :data="form" :rules="formRules" label-align="top">
        <t-form-item label="标准名称" name="name">
          <t-input v-model="form.name" placeholder="例如：用户ID" />
        </t-form-item>
        <t-form-item label="所属分类" name="category">
          <t-select v-model="form.category" :options="categoryOptions" />
        </t-form-item>
        <t-form-item label="数据类型" name="dataType">
          <t-select v-model="form.dataType" :options="dataTypeOptions" />
        </t-form-item>
        <t-form-item label="长度" name="length">
          <t-input-number v-model="form.length" :min="0" theme="normal" style="width: 100%" />
        </t-form-item>
        <t-form-item label="业务含义" name="businessMeaning">
          <t-textarea
            v-model="form.businessMeaning"
            placeholder="请输入业务含义说明"
            :autosize="{ minRows: 2, maxRows: 4 }"
          />
        </t-form-item>
      </t-form>
    </t-dialog>
  </div>
</template>

<style scoped>
.std-layout {
  display: grid;
  grid-template-columns: 280px 1fr;
  gap: 16px;
  align-items: start;
}
.tree-panel {
  margin-bottom: 0;
  position: sticky;
  top: 16px;
}
.right-panel {
  margin-bottom: 0;
}
.card-label {
  font-size: 15px;
  font-weight: 600;
}
.tree-stat {
  display: flex;
  justify-content: space-around;
  margin-top: 16px;
  padding-top: 12px;
  border-top: 1px solid var(--border-color);
}
.tree-stat-item {
  display: flex;
  flex-direction: column;
  align-items: center;
}
.tree-stat-num {
  font-size: 20px;
  font-weight: 600;
  color: var(--brand-color);
}
.tree-stat-label {
  font-size: 12px;
  color: var(--text-secondary);
  margin-top: 2px;
}
.count-badge {
  display: inline-block;
  margin-left: 8px;
  padding: 0 8px;
  height: 22px;
  line-height: 22px;
  background: rgba(0, 82, 217, 0.1);
  color: var(--brand-color);
  border-radius: 11px;
  font-size: 12px;
  font-weight: 500;
  vertical-align: middle;
}
.empty-tip {
  text-align: center;
  padding: 60px 0;
  color: var(--text-secondary);
}
.empty-tip div {
  margin-top: 8px;
}
.card-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(320px, 1fr));
  gap: 12px;
}
.std-card {
  border: 1px solid var(--border-color);
  border-radius: 8px;
  padding: 14px;
  transition: all 0.2s;
  background: #fff;
}
.std-card:hover {
  border-color: var(--brand-color);
  box-shadow: 0 4px 12px rgba(0, 82, 217, 0.08);
}
.std-card-head {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 8px;
}
.std-name {
  font-size: 15px;
  font-weight: 600;
  color: var(--text-primary);
}
.std-meta {
  display: flex;
  gap: 8px;
  margin-bottom: 8px;
}
.meta-chip {
  display: inline-flex;
  align-items: center;
  gap: 4px;
  padding: 2px 8px;
  background: #f5f7fa;
  border-radius: 4px;
  font-size: 12px;
  color: var(--text-secondary);
}
.std-meaning {
  font-size: 13px;
  color: var(--text-secondary);
  line-height: 1.5;
  min-height: 38px;
  margin-bottom: 10px;
}
.std-card-foot {
  display: flex;
  gap: 16px;
  align-items: center;
  padding-top: 10px;
  border-top: 1px dashed var(--border-color);
}
.foot-item {
  display: flex;
  align-items: center;
  gap: 6px;
}
.foot-item.flex-1 {
  flex: 1;
}
.foot-label {
  font-size: 12px;
  color: var(--text-secondary);
}
.foot-num {
  font-size: 14px;
  font-weight: 600;
  color: var(--text-primary);
}
</style>
