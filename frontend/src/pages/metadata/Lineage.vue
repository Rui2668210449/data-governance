<script setup lang="ts">
import { ref, onMounted, onUnmounted, nextTick } from 'vue'
import { MessagePlugin } from 'tdesign-vue-next'
import G6 from '@antv/g6'
import { getLineage } from '@/api'
import type { LineageNode, LineageEdge } from '@/types'

const keyword = ref('')
const layoutType = ref<'dagre' | 'force'>('dagre')
const loading = ref(true)

const graphRef = ref<HTMLDivElement>()
let graph: InstanceType<typeof G6.Graph> | null = null
const allNodes = ref<LineageNode[]>([])
const allEdges = ref<LineageEdge[]>([])

// 节点详情抽屉
const drawerVisible = ref(false)
const selectedNode = ref<LineageNode | null>(null)
const nodeNeighbors = ref<{ upstream: LineageNode[]; downstream: LineageNode[] }>({
  upstream: [],
  downstream: [],
})

function buildGraphData() {
  const kw = keyword.value.trim().toLowerCase()
  let nodes = [...allNodes.value]
  let edges = [...allEdges.value]
  if (kw) {
    const matchedIds = new Set(
      nodes.filter((n) => n.label.toLowerCase().includes(kw) || (n.dbName || '').toLowerCase().includes(kw)).map((n) => n.id)
    )
    // 同时保留直接相邻节点，便于查看
    edges.forEach((e) => {
      if (matchedIds.has(e.source)) matchedIds.add(e.target)
      if (matchedIds.has(e.target)) matchedIds.add(e.source)
    })
    nodes = nodes.filter((n) => matchedIds.has(n.id))
    edges = edges.filter((e) => matchedIds.has(e.source) && matchedIds.has(e.target))
  }
  return {
    nodes: nodes.map((n) => ({
      id: n.id,
      label: n.label,
      nodeType: n.type,
      dbName: n.dbName,
      type: n.type === 'TASK' ? 'diamond' : 'rect',
      size: n.type === 'TASK' ? [140, 50] : [160, 36],
      style: {
        fill: n.type === 'TASK' ? '#ed7b2f' : '#0052d9',
        stroke: n.type === 'TASK' ? '#ed7b2f' : '#0052d9',
        radius: n.type === 'TASK' ? 0 : 8,
        lineWidth: 1,
      },
      labelCfg: {
        style: {
          fill: '#fff',
          fontSize: 12,
        },
      },
    })),
    edges: edges.map((e, i) => ({
      id: `edge-${i}`,
      source: e.source,
      target: e.target,
      label: e.label,
    })),
  }
}

function getLayoutConfig() {
  if (layoutType.value === 'force') {
    return {
      type: 'force',
      preventOverlap: true,
      nodeSpacing: 40,
      linkDistance: 150,
      nodeStrength: -50,
      edgeStrength: 0.7,
      collideStrength: 0.8,
      alpha: 0.3,
      alphaDecay: 0.028,
      alphaMin: 0.01,
    }
  }
  return {
    type: 'dagre',
    rankdir: 'LR',
    nodesep: 40,
    ranksep: 80,
    preventOverlap: true,
  }
}

function initGraph() {
  if (!graphRef.value) return
  const container = graphRef.value
  const width = container.clientWidth || 1000
  const height = container.clientHeight || 600

  if (graph) {
    graph.destroy()
    graph = null
  }

  const data = buildGraphData()

  graph = new G6.Graph({
    container,
    width,
    height,
    modes: {
      default: ['drag-canvas', 'zoom-canvas', 'drag-node'],
    },
    layout: getLayoutConfig(),
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

  graph.data(data)
  graph.render()
  graph.fitView(40)

  graph.on('node:click', (evt: any) => {
    const node = evt.item
    if (!node) return
    const model = node.getModel()
    const nodeData = allNodes.value.find((n) => n.id === model.id)
    if (!nodeData) return
    selectedNode.value = nodeData
    // 计算上下游
    const upstreamIds = allEdges.value.filter((e) => e.target === nodeData.id).map((e) => e.source)
    const downstreamIds = allEdges.value.filter((e) => e.source === nodeData.id).map((e) => e.target)
    nodeNeighbors.value = {
      upstream: allNodes.value.filter((n) => upstreamIds.includes(n.id)),
      downstream: allNodes.value.filter((n) => downstreamIds.includes(n.id)),
    }
    drawerVisible.value = true
  })
}

function handleSearch() {
  initGraph()
}

function handleLayoutSwitch() {
  layoutType.value = layoutType.value === 'dagre' ? 'force' : 'dagre'
  initGraph()
  MessagePlugin.success(`已切换为${layoutType.value === 'dagre' ? '层次' : '力导'}布局`)
}

function handleReset() {
  keyword.value = ''
  layoutType.value = 'dagre'
  initGraph()
  graph?.fitView(40)
}

function handleResize() {
  if (!graph || !graphRef.value) return
  const width = graphRef.value.clientWidth
  const height = graphRef.value.clientHeight
  graph.changeSize(width, height)
}

onMounted(async () => {
  loading.value = true
  try {
    const res = await getLineage()
    allNodes.value = res.nodes
    allEdges.value = res.edges
  } finally {
    loading.value = false
  }
  await nextTick()
  initGraph()
  window.addEventListener('resize', handleResize)
})

onUnmounted(() => {
  window.removeEventListener('resize', handleResize)
  if (graph) {
    graph.destroy()
    graph = null
  }
})
</script>

<template>
  <div class="page-container" v-loading="loading">
    <!-- 工具栏 -->
    <div class="page-card">
      <div class="flex-between">
        <div class="page-title" style="margin: 0">血缘图谱</div>
        <div class="flex gap-12">
          <t-input
            v-model="keyword"
            placeholder="搜索节点名称"
            clearable
            style="width: 240px"
            @enter="handleSearch"
            @clear="handleSearch"
          >
            <template #prefix-icon>
              <t-icon name="search" />
            </template>
          </t-input>
          <t-button variant="outline" @click="handleLayoutSwitch">
            <template #icon><t-icon name="swap" /></template>
            布局：{{ layoutType === 'dagre' ? '层次' : '力导' }}
          </t-button>
          <t-button theme="primary" variant="outline" @click="handleReset">
            <template #icon><t-icon name="refresh" /></template>
            重置
          </t-button>
        </div>
      </div>
    </div>

    <!-- 图谱画布 -->
    <div class="page-card graph-card">
      <div ref="graphRef" class="graph-container"></div>

      <!-- 图例 -->
      <div class="legend">
        <div class="legend-title">节点类型</div>
        <div class="legend-item">
          <span class="legend-shape legend-table"></span>
          <span>表（TABLE）</span>
        </div>
        <div class="legend-item">
          <span class="legend-shape legend-task"></span>
          <span>任务（TASK）</span>
        </div>
      </div>
    </div>

    <!-- 节点详情抽屉 -->
    <t-drawer
      v-model:visible="drawerVisible"
      header="节点详情"
      :footer="false"
      size="360px"
    >
      <div v-if="selectedNode" class="drawer-content">
        <div class="detail-section">
          <div class="detail-label">节点名称</div>
          <div class="detail-value">{{ selectedNode.label }}</div>
        </div>
        <div class="detail-section">
          <div class="detail-label">节点类型</div>
          <div class="detail-value">
            <t-tag
              variant="light"
              :color="selectedNode.type === 'TABLE' ? '#0052d9' : '#ed7b2f'"
            >
              {{ selectedNode.type === 'TABLE' ? '表（TABLE）' : '任务（TASK）' }}
            </t-tag>
          </div>
        </div>
        <div class="detail-section" v-if="selectedNode.dbName">
          <div class="detail-label">所属库</div>
          <div class="detail-value">{{ selectedNode.dbName }}</div>
        </div>
        <div class="detail-section">
          <div class="detail-label">节点ID</div>
          <div class="detail-value mono">{{ selectedNode.id }}</div>
        </div>

        <div class="detail-section">
          <div class="detail-label">上游节点（{{ nodeNeighbors.upstream.length }}）</div>
          <div v-if="nodeNeighbors.upstream.length" class="neighbor-list">
            <t-tag
              v-for="n in nodeNeighbors.upstream"
              :key="n.id"
              size="small"
              variant="light"
              :color="n.type === 'TABLE' ? '#0052d9' : '#ed7b2f'"
              style="margin: 2px 4px 2px 0"
            >
              {{ n.label }}
            </t-tag>
          </div>
          <div v-else class="text-secondary">无</div>
        </div>

        <div class="detail-section">
          <div class="detail-label">下游节点（{{ nodeNeighbors.downstream.length }}）</div>
          <div v-if="nodeNeighbors.downstream.length" class="neighbor-list">
            <t-tag
              v-for="n in nodeNeighbors.downstream"
              :key="n.id"
              size="small"
              variant="light"
              :color="n.type === 'TABLE' ? '#0052d9' : '#ed7b2f'"
              style="margin: 2px 4px 2px 0"
            >
              {{ n.label }}
            </t-tag>
          </div>
          <div v-else class="text-secondary">无</div>
        </div>
      </div>
    </t-drawer>
  </div>
</template>

<style scoped>
.page-title {
  font-size: 18px;
  font-weight: 600;
  color: var(--text-primary);
}
.graph-card {
  position: relative;
  padding: 0;
  overflow: hidden;
}
.graph-container {
  width: 100%;
  height: calc(100vh - 180px);
  min-height: 400px;
  background: #fafbfc;
}
.legend {
  position: absolute;
  left: 16px;
  bottom: 16px;
  background: rgba(255, 255, 255, 0.95);
  border: 1px solid var(--border-color);
  border-radius: 6px;
  padding: 10px 12px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);
  font-size: 12px;
  z-index: 10;
}
.legend-title {
  font-weight: 600;
  color: var(--text-primary);
  margin-bottom: 6px;
}
.legend-item {
  display: flex;
  align-items: center;
  gap: 8px;
  color: var(--text-secondary);
  margin-top: 4px;
}
.legend-shape {
  display: inline-block;
  width: 16px;
  height: 12px;
}
.legend-table {
  background: #0052d9;
  border-radius: 3px;
}
.legend-task {
  background: #ed7b2f;
  width: 12px;
  height: 12px;
  transform: rotate(45deg);
}
.drawer-content {
  padding: 4px 0;
}
.detail-section {
  margin-bottom: 16px;
}
.detail-label {
  font-size: 12px;
  color: var(--text-secondary);
  margin-bottom: 4px;
}
.detail-value {
  font-size: 14px;
  color: var(--text-primary);
  word-break: break-all;
}
.detail-value.mono {
  font-family: 'Menlo', 'Monaco', monospace;
  font-size: 13px;
  background: #f5f7fa;
  padding: 4px 8px;
  border-radius: 4px;
}
.neighbor-list {
  margin-top: 4px;
}
</style>
