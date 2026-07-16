<script setup lang="ts">
import { ref, computed, onMounted, onUnmounted, nextTick, watch } from 'vue'
import * as echarts from 'echarts'
import { getLifecyclePolicies, getDatasources } from '@/api'
import type { LifecyclePolicy, Datasource } from '@/types'
import { useAppStore } from '@/store/app'

const appStore = useAppStore()
const loading = ref(false)
const policies = ref<LifecyclePolicy[]>([])
const datasources = ref<Datasource[]>([])

const pieChartRef = ref<HTMLDivElement>()
const stackChartRef = ref<HTMLDivElement>()
let pieChart: echarts.ECharts | null = null
let stackChart: echarts.ECharts | null = null

const stageMeta: Record<string, { text: string; color: string }> = {
  HOT: { text: '热数据', color: '#e34d59' },
  WARM: { text: '温数据', color: '#ed7b2f' },
  COLD: { text: '冷数据', color: '#0052d9' },
  DELETE: { text: '待删除', color: '#8c8c8c' },
}

function formatSize(bytes: number) {
  if (bytes >= 1073741824) return (bytes / 1073741824).toFixed(2) + ' GB'
  if (bytes >= 1048576) return (bytes / 1048576).toFixed(2) + ' MB'
  if (bytes >= 1024) return (bytes / 1024).toFixed(2) + ' KB'
  return bytes + ' B'
}

// 统计卡片
const totalSize = computed(() => policies.value.reduce((s, p) => s + p.sizeBytes, 0))
const hotSize = computed(() =>
  policies.value.filter((p) => p.stage === 'HOT').reduce((s, p) => s + p.sizeBytes, 0)
)
const coldSize = computed(() =>
  policies.value.filter((p) => p.stage === 'COLD').reduce((s, p) => s + p.sizeBytes, 0)
)

const statCards = computed(() => [
  {
    title: '总数据量',
    value: formatSize(totalSize.value),
    icon: 'server',
    color: '#0052d9',
    sub: `${policies.value.length} 张表纳入策略`,
  },
  {
    title: '热数据占比',
    value: totalSize.value > 0 ? ((hotSize.value / totalSize.value) * 100).toFixed(1) + '%' : '0%',
    icon: 'fire',
    color: '#e34d59',
    sub: formatSize(hotSize.value),
  },
  {
    title: '冷数据占比',
    value: totalSize.value > 0 ? ((coldSize.value / totalSize.value) * 100).toFixed(1) + '%' : '0%',
    icon: 'snowflake',
    color: '#0052d9',
    sub: formatSize(coldSize.value),
  },
  {
    title: '本月归档任务数',
    value: String(archiveTasks.value.length),
    icon: 'time',
    color: '#ed7b2f',
    sub: `成功 ${archiveTasks.value.filter((t) => t.status === 'SUCCESS').length} / 失败 ${archiveTasks.value.filter((t) => t.status === 'FAIL').length}`,
  },
])

// 按阶段聚合数据量
const stageAgg = computed(() => {
  const map: Record<string, number> = { HOT: 0, WARM: 0, COLD: 0, DELETE: 0 }
  policies.value.forEach((p) => {
    map[p.stage] = (map[p.stage] || 0) + p.sizeBytes
  })
  return map
})

// 归档任务执行状态：根据 policies 派生模拟任务
interface ArchiveTask {
  id: number
  name: string
  tableName: string
  fromStage: string
  toStage: string
  status: 'SUCCESS' | 'RUNNING' | 'FAIL'
  execTime: string
}
const archiveTasks = ref<ArchiveTask[]>([
  { id: 1, name: '冷数据归档任务', tableName: 'ods_user_log_d', fromStage: 'WARM', toStage: 'COLD', status: 'SUCCESS', execTime: '2026-07-16 03:15:00' },
  { id: 2, name: '温数据迁移任务', tableName: 'ods_trade_dwd_d', fromStage: 'HOT', toStage: 'WARM', status: 'SUCCESS', execTime: '2026-07-15 02:30:00' },
  { id: 3, name: '过期数据清理', tableName: 'order_detail', fromStage: 'COLD', toStage: 'DELETE', status: 'RUNNING', execTime: '2026-07-16 04:00:00' },
  { id: 4, name: '冷数据归档任务', tableName: 'rpt_user_active_m', fromStage: 'WARM', toStage: 'COLD', status: 'FAIL', execTime: '2026-07-14 03:00:00' },
])

const taskStatusMeta: Record<string, { text: string; theme: 'success' | 'warning' | 'danger' }> = {
  SUCCESS: { text: '成功', theme: 'success' },
  RUNNING: { text: '进行中', theme: 'warning' },
  FAIL: { text: '失败', theme: 'danger' },
}

const taskColumns = [
  { colKey: 'name', title: '任务名', minWidth: 160, ellipsis: true },
  { colKey: 'tableName', title: '目标表', width: 160 },
  { colKey: 'transition', title: '源阶段 → 目标阶段', width: 200, cell: 'transition' },
  { colKey: 'status', title: '状态', width: 100, cell: 'status' },
  { colKey: 'execTime', title: '执行时间', width: 180 },
]

function initPieChart() {
  if (!pieChartRef.value) return
  pieChart = echarts.init(pieChartRef.value)
  const data = (['HOT', 'WARM', 'COLD', 'DELETE'] as const).map((s) => ({
    name: stageMeta[s].text,
    value: stageAgg.value[s] || 0,
    itemStyle: { color: stageMeta[s].color },
  }))
  pieChart.setOption({
    tooltip: {
      trigger: 'item',
      formatter: (p: { name: string; value: number; percent: number }) =>
        `${p.name}<br/>${formatSize(p.value)} (${p.percent}%)`,
    },
    legend: { bottom: 0, icon: 'circle', itemWidth: 8, itemHeight: 8 },
    series: [
      {
        type: 'pie',
        radius: ['45%', '70%'],
        center: ['50%', '42%'],
        avoidLabelOverlap: false,
        itemStyle: { borderRadius: 6, borderColor: '#fff', borderWidth: 2 },
        label: { show: false },
        emphasis: { label: { show: true, fontSize: 14, fontWeight: 'bold' } },
        data,
      },
    ],
  })
}

function initStackChart() {
  if (!stackChartRef.value) return
  stackChart = echarts.init(stackChartRef.value)
  // 各数据源在不同阶段的数据量分布：使用 policies 中的 sizeBytes
  // 由于 policies 没有直接关联数据源，用 tableName 关联 mock 的 datasources（这里直接用表名前缀模拟）
  const dsNames = ['业务核心库', '数仓ODS层', '分析报表库']
  const stages = ['HOT', 'WARM', 'COLD', 'DELETE'] as const
  // 按表名前缀归类到数据源
  const dsStageMap: Record<string, Record<string, number>> = {}
  dsNames.forEach((n) => {
    dsStageMap[n] = { HOT: 0, WARM: 0, COLD: 0, DELETE: 0 }
  })
  policies.value.forEach((p) => {
    let dsName = '业务核心库'
    if (p.tableName.startsWith('ods')) dsName = '数仓ODS层'
    else if (p.tableName.startsWith('rpt')) dsName = '分析报表库'
    dsStageMap[dsName][p.stage] += p.sizeBytes
  })
  // 转 GB
  const gb = (bytes: number) => Number((bytes / 1073741824).toFixed(2))
  stackChart.setOption({
    tooltip: {
      trigger: 'axis',
      axisPointer: { type: 'shadow' },
      formatter: (params: { seriesName: string; value: number }[]) => {
        let html = params[0]?.seriesName ? '' : ''
        return params
          .map((p) => `${p.seriesName}: ${p.value} GB`)
          .join('<br/>')
      },
    },
    legend: { bottom: 0, icon: 'circle', itemWidth: 8, itemHeight: 8 },
    grid: { left: 50, right: 20, top: 20, bottom: 50 },
    xAxis: {
      type: 'category',
      data: dsNames,
      axisLine: { lineStyle: { color: '#e7e7e7' } },
      axisLabel: { color: '#595959' },
    },
    yAxis: {
      type: 'value',
      name: 'GB',
      axisLine: { show: false },
      splitLine: { lineStyle: { color: '#f0f0f0' } },
      axisLabel: { color: '#595959' },
    },
    series: stages.map((s) => ({
      name: stageMeta[s].text,
      type: 'bar',
      stack: 'total',
      barMaxWidth: 40,
      itemStyle: { color: stageMeta[s].color },
      emphasis: { focus: 'series' },
      data: dsNames.map((n) => gb(dsStageMap[n][s])),
    })),
  })
}

function resizeCharts() {
  pieChart?.resize()
  stackChart?.resize()
}

watch(() => appStore.collapsed, () => {
  nextTick(resizeCharts)
})

async function loadData() {
  loading.value = true
  try {
    const [p, d] = await Promise.all([getLifecyclePolicies(), getDatasources()])
    policies.value = p
    datasources.value = d
    await nextTick()
    initPieChart()
    initStackChart()
  } finally {
    loading.value = false
  }
}

onMounted(async () => {
  await loadData()
  window.addEventListener('resize', resizeCharts)
})

onUnmounted(() => {
  window.removeEventListener('resize', resizeCharts)
  pieChart?.dispose()
  stackChart?.dispose()
})
</script>

<template>
  <div class="page-container" v-loading="loading">
    <!-- 顶部统计卡片 -->
    <div class="metric-grid">
      <div v-for="card in statCards" :key="card.title" class="metric-card">
        <div class="metric-icon" :style="{ background: card.color + '15', color: card.color }">
          <t-icon :name="card.icon" size="24" />
        </div>
        <div class="metric-body">
          <div class="metric-value">{{ card.value }}</div>
          <div class="metric-title">{{ card.title }}</div>
          <div class="metric-sub">{{ card.sub }}</div>
        </div>
      </div>
    </div>

    <!-- 图表区 -->
    <div class="chart-row">
      <div class="page-card chart-card">
        <div class="flex-between" style="margin-bottom: 12px">
          <span class="card-label">各阶段数据量占比</span>
          <t-tag theme="primary" variant="light" size="small">HOT/WARM/COLD/DELETE</t-tag>
        </div>
        <div ref="pieChartRef" class="chart-box"></div>
      </div>
      <div class="page-card chart-card">
        <div class="flex-between" style="margin-bottom: 12px">
          <span class="card-label">各数据源阶段分布</span>
          <t-tag theme="success" variant="light" size="small">堆叠柱状图</t-tag>
        </div>
        <div ref="stackChartRef" class="chart-box"></div>
      </div>
    </div>

    <!-- 归档任务执行状态表 -->
    <div class="page-card">
      <div class="flex-between" style="margin-bottom: 12px">
        <span class="card-label">归档任务执行状态</span>
        <t-tag theme="warning" variant="light" size="small">
          进行中 {{ archiveTasks.filter(t => t.status === 'RUNNING').length }}
        </t-tag>
      </div>
      <t-table
        :data="archiveTasks"
        row-key="id"
        :columns="taskColumns"
        size="small"
        :pagination="{ show: false }"
        bordered
      >
        <template #transition="{ row }">
          <div class="transition-cell">
            <t-tag size="small" :color="stageMeta[row.fromStage].color" variant="light">
              {{ stageMeta[row.fromStage].text }}
            </t-tag>
            <t-icon name="arrow-right" size="14" style="color: #8c8c8c" />
            <t-tag size="small" :color="stageMeta[row.toStage].color" variant="light">
              {{ stageMeta[row.toStage].text }}
            </t-tag>
          </div>
        </template>
        <template #status="{ row }">
          <t-tag :theme="taskStatusMeta[row.status].theme" variant="light" size="small">
            {{ taskStatusMeta[row.status].text }}
          </t-tag>
        </template>
      </t-table>
    </div>
  </div>
</template>

<style scoped>
.metric-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 16px;
  margin-bottom: 16px;
}
.metric-card {
  background: #fff;
  border-radius: 6px;
  padding: 18px 20px;
  display: flex;
  align-items: center;
  gap: 14px;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.04);
  transition: transform 0.2s, box-shadow 0.2s;
}
.metric-card:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.08);
}
.metric-icon {
  width: 48px;
  height: 48px;
  border-radius: 10px;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}
.metric-body {
  min-width: 0;
}
.metric-value {
  font-size: 24px;
  font-weight: 600;
  color: var(--text-primary);
  line-height: 1.2;
}
.metric-title {
  font-size: 13px;
  color: var(--text-secondary);
  margin-top: 2px;
}
.metric-sub {
  font-size: 11px;
  color: var(--text-secondary);
  margin-top: 2px;
}
.chart-row {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 16px;
  margin-bottom: 16px;
}
.chart-card {
  margin-bottom: 0;
}
.card-label {
  font-size: 15px;
  font-weight: 600;
}
.chart-box {
  height: 280px;
}
.transition-cell {
  display: flex;
  align-items: center;
  gap: 6px;
}
</style>
