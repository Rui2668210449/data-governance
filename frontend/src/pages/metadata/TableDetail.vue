<script setup lang="ts">
import { ref, computed, onMounted, onUnmounted, nextTick, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { MessagePlugin } from 'tdesign-vue-next'
import G6 from '@antv/g6'
import {
  getTableDetail,
  getTableFields,
  getTableChanges,
  getLineage,
} from '@/api'
import type {
  MetadataTable,
  MetadataField,
  TableChange,
  LineageNode,
  LineageEdge,
} from '@/types'

const route = useRoute()
const router = useRouter()

const tableId = computed(() => Number(route.params.id))

const detail = ref<MetadataTable | null>(null)
const fields = ref<MetadataField[]>([])
const changes = ref<TableChange[]>([])
const loading = ref(true)

const activeTab = ref<'fields' | 'lineage' | 'changes'>('fields')

// 血缘图
const lineageRef = ref<HTMLDivElement>()
let graph: InstanceType<typeof G6.Graph> | null = null
const lineageNodes = ref<LineageNode[]>([])
const lineageEdges = ref<LineageEdge[]>([])

function formatSize(bytes: number) {
  if (!bytes && bytes !== 0) return '-'
  if (bytes >= 1024 * 1024 * 1024) return (bytes / 1024 / 1024 / 1024).toFixed(2) + ' GB'
  if (bytes >= 1024 * 1024) return (bytes / 1024 / 1024).toFixed(2) + ' MB'
  if (bytes >= 1024) return (bytes / 1024).toFixed(2) + ' KB'
  return bytes + ' B'
}

function formatRows(n: number) {
  if (n >= 100000000) return (n / 100000000).toFixed(2) + ' 亿'
  if (n >= 10000) return (n / 10000).toFixed(2) + ' 万'
  return n.toLocaleString('zh-CN')
}

const securityColorMap: Record<string, string> = {
  L1: '#00a870',
  L2: '#0052d9',
  L3: '#ed7b2f',
  L4: '#e34d59',
}

const changeColorMap: Record<string, string> = {
  ADD_FIELD: '#00a870',
  DROP_FIELD: '#e34d59',
  MODIFY_TYPE: '#ed7b2f',
  RENAME: '#7265e6',
}

const changeLabelMap: Record<string, string> = {
  ADD_FIELD: '新增字段',
  DROP_FIELD: '删除字段',
  MODIFY_TYPE: '修改类型',
  RENAME: '重命名',
}

function initGraph() {
  if (!lineageRef.value) return
  const container = lineageRef.value
  const width = container.clientWidth || 800
  const height = 400

  if (graph) {
    graph.destroy()
    graph = null
  }

  const nodes = lineageNodes.value.map((n) => ({
    id: n.id,
    label: n.label,
    nodeType: n.type,
    dbName: n.dbName,
  }))
  const edges = lineageEdges.value.map((e, i) => ({
    source: e.source,
    target: e.target,
    label: e.label,
    id: `edge-${i}`,
  }))

  graph = new G6.Graph({
    container,
    width,
    height,
    modes: {
      default: ['drag-canvas', 'zoom-canvas', 'drag-node'],
    },
    layout: {
      type: 'dagre',
      rankdir: 'LR',
      nodesep: 40,
      ranksep: 80,
      preventOverlap: true,
    },
    defaultNode: {
      type: 'rect',
      size: [160, 36],
      style: {
        radius: 8,
        fill: '#0052d9',
        stroke: '#0052d9',
        lineWidth: 1,
      },
      labelCfg: {
        style: {
          fill: '#fff',
          fontSize: 12,
        },
      },
    },
    defaultEdge: {
      type: 'polyline',
      style: {
        stroke: '#8c8c8c',
        lineWidth: 1.5,
        endArrow: {
          path: G6.Arrow.triangle(8, 8, 0),
          fill: '#8c8c8c',
        },
      },
      labelCfg: {
        style: {
          fill: '#595959',
          fontSize: 11,
          background: {
            fill: '#ffffff',
            padding: [2, 4, 2, 4],
            radius: 2,
          },
        },
      },
    },
  })

  // 节点类型样式区分
  graph.data({
    nodes: nodes.map((n) => {
      if (n.nodeType === 'TASK') {
        return {
          ...n,
          type: 'diamond',
          size: [140, 50],
          style: {
            fill: '#ed7b2f',
            stroke: '#ed7b2f',
          },
        }
      }
      return n
    }),
    edges,
  })
  graph.render()
  graph.fitView(20)
}

async function loadAll() {
  loading.value = true
  try {
    const id = tableId.value
    const [d, f, c, l] = await Promise.all([
      getTableDetail(id),
      getTableFields(id),
      getTableChanges(id),
      getLineage(id),
    ])
    detail.value = d
    fields.value = f
    changes.value = c
    lineageNodes.value = l.nodes
    lineageEdges.value = l.edges
  } finally {
    loading.value = false
  }
}

watch(activeTab, async (val) => {
  if (val === 'lineage') {
    await nextTick()
    initGraph()
  }
})

onMounted(async () => {
  await loadAll()
})

onUnmounted(() => {
  if (graph) {
    graph.destroy()
    graph = null
  }
})
</script>

<template>
  <div class="page-container" v-loading="loading">
    <!-- 返回按钮 -->
    <div class="page-card" style="padding: 12px 16px">
      <t-button variant="text" theme="primary" @click="router.back()">
        <template #icon><t-icon name="chevron-left" /></template>
        返回
      </t-button>
    </div>

    <!-- 表基本信息 -->
    <div class="page-card" v-if="detail">
      <div class="flex-between" style="margin-bottom: 16px">
        <div class="flex gap-12" style="align-items: baseline">
          <span class="page-title" style="margin: 0">{{ detail.tableName }}</span>
          <span class="text-secondary">{{ detail.tableComment }}</span>
        </div>
        <t-tag :color="securityColorMap[detail.securityLevel]" variant="light">
          {{ detail.securityLevel }}
        </t-tag>
      </div>
      <div class="info-grid">
        <div class="info-item">
          <span class="info-label">所属库</span>
          <span class="info-value">{{ detail.dbName }}</span>
        </div>
        <div class="info-item">
          <span class="info-label">行数</span>
          <span class="info-value">{{ formatRows(detail.rowCount) }}</span>
        </div>
        <div class="info-item">
          <span class="info-label">大小</span>
          <span class="info-value">{{ formatSize(detail.sizeBytes) }}</span>
        </div>
        <div class="info-item">
          <span class="info-label">负责人</span>
          <span class="info-value">{{ detail.owner }}</span>
        </div>
        <div class="info-item">
          <span class="info-label">质量分</span>
          <span class="info-value">
            <t-tag
              size="small"
              variant="light"
              :theme="detail.qualityScore >= 95 ? 'success' : detail.qualityScore >= 85 ? 'primary' : 'warning'"
            >
              {{ detail.qualityScore.toFixed(1) }}
            </t-tag>
          </span>
        </div>
        <div class="info-item">
          <span class="info-label">安全级别</span>
          <span class="info-value">
            <t-tag :color="securityColorMap[detail.securityLevel]" variant="light">
              {{ detail.securityLevel }}
            </t-tag>
          </span>
        </div>
        <div class="info-item info-tags">
          <span class="info-label">标签</span>
          <span class="info-value">
            <t-space size="small" wrap>
              <t-tag
                v-for="tag in detail.tags"
                :key="tag"
                size="small"
                variant="light"
                theme="primary"
              >
                {{ tag }}
              </t-tag>
            </t-space>
          </span>
        </div>
      </div>
    </div>

    <!-- Tab 区 -->
    <div class="page-card">
      <t-tabs v-model="activeTab">
        <t-tab-panel value="fields" label="字段信息">
          <t-table
            :data="fields"
            row-key="id"
            :columns="[
              { colKey: 'fieldName', title: '字段名', ellipsis: true },
              { colKey: 'dataType', title: '类型', width: 140 },
              { colKey: 'comment', title: '注释', ellipsis: true },
              { colKey: 'businessMeaning', title: '业务含义', ellipsis: true },
              { colKey: 'isPrimary', title: '主键', width: 90, cell: 'pk' },
              { colKey: 'isSensitive', title: '是否敏感', width: 100, cell: 'sensitive' },
            ]"
            size="small"
            :pagination="{ show: false }"
            style="margin-top: 12px"
          >
            <template #pk="{ row }">
              <t-tag v-if="row.isPrimary === 1" size="small" theme="primary" variant="light">主键</t-tag>
              <span v-else class="text-secondary">-</span>
            </template>
            <template #sensitive="{ row }">
              <t-tag v-if="row.isSensitive === 1" size="small" theme="danger" variant="light">敏感</t-tag>
              <span v-else class="text-secondary">否</span>
            </template>
          </t-table>
        </t-tab-panel>

        <t-tab-panel value="lineage" label="血缘关系">
          <div ref="lineageRef" class="lineage-box"></div>
        </t-tab-panel>

        <t-tab-panel value="changes" label="变更历史">
          <div style="padding: 16px 8px">
            <t-timeline>
              <t-timeline-item
                v-for="c in changes"
                :key="c.id"
                :dot-color="changeColorMap[c.changeType]"
              >
                <div class="change-item">
                  <div class="flex-between">
                    <div class="flex gap-12" style="align-items: center">
                      <t-tag
                        size="small"
                        variant="light"
                        :color="changeColorMap[c.changeType]"
                      >
                        {{ changeLabelMap[c.changeType] || c.changeType }}
                      </t-tag>
                      <span class="change-version">{{ c.version }}</span>
                    </div>
                    <span class="text-secondary change-time">{{ c.changeTime }}</span>
                  </div>
                  <div class="change-detail">{{ c.changeDetail }}</div>
                  <div class="change-operator">操作人：{{ c.operator }}</div>
                </div>
              </t-timeline-item>
            </t-timeline>
          </div>
        </t-tab-panel>
      </t-tabs>
    </div>
  </div>
</template>

<style scoped>
.page-title {
  font-size: 20px;
  font-weight: 600;
  color: var(--text-primary);
}
.info-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 16px 24px;
}
.info-item {
  display: flex;
  align-items: baseline;
  gap: 8px;
}
.info-tags {
  grid-column: span 2;
}
.info-label {
  font-size: 13px;
  color: var(--text-secondary);
  flex-shrink: 0;
  min-width: 60px;
}
.info-value {
  font-size: 14px;
  color: var(--text-primary);
}
.lineage-box {
  width: 100%;
  height: 400px;
  margin-top: 12px;
  border: 1px solid var(--border-color);
  border-radius: 6px;
  background: #fafbfc;
}
.change-item {
  padding: 4px 0 8px 0;
}
.change-version {
  font-size: 13px;
  color: var(--brand-color);
  font-weight: 500;
}
.change-time {
  font-size: 12px;
}
.change-detail {
  font-size: 13px;
  color: var(--text-primary);
  margin: 6px 0 4px 0;
}
.change-operator {
  font-size: 12px;
  color: var(--text-secondary);
}
</style>
