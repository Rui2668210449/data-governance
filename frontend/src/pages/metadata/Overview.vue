<script setup lang="ts">
import { ref, onMounted, onUnmounted, nextTick } from 'vue'
import { useRouter } from 'vue-router'
import * as echarts from 'echarts'
import { getDashboardStats, getMetadataTables } from '@/api'
import type { DashboardStats, MetadataTable } from '@/types'

const router = useRouter()

const stats = ref<DashboardStats | null>(null)
const tables = ref<MetadataTable[]>([])
const loading = ref(true)

const distChartRef = ref<HTMLDivElement>()
const barChartRef = ref<HTMLDivElement>()
let distChart: echarts.ECharts | null = null
let barChart: echarts.ECharts | null = null

interface MetricCard {
  title: string
  value: number
  icon: string
  color: string
  unit: string
  decimal?: number
}

const metricCards = ref<MetricCard[]>([
  { title: '数据源数', value: 0, icon: 'data-base', color: '#0052d9', unit: '个' },
  { title: '数据表数', value: 0, icon: 'server', color: '#00a870', unit: '张' },
  { title: '字段总数', value: 0, icon: 'edit-1', color: '#ed7b2f', unit: '个' },
  { title: '平均质量分', value: 0, icon: 'check-circle', color: '#7265e6', unit: '分', decimal: 1 },
])

function formatNumber(n: number) {
  return n.toLocaleString('zh-CN')
}

function formatRows(n: number) {
  if (n >= 100000000) return (n / 100000000).toFixed(2) + ' 亿'
  if (n >= 10000) return (n / 10000).toFixed(2) + ' 万'
  return n.toString()
}

// TOP10 表：按行数排序
const topTables = ref<MetadataTable[]>([])

function initDistChart() {
  if (!distChartRef.value || !stats.value) return
  distChart = echarts.init(distChartRef.value)
  const colors = ['#0052d9', '#266fe8', '#00a8e5', '#7265e6']
  distChart.setOption({
    tooltip: { trigger: 'item', formatter: '{b}: {c} ({d}%)' },
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
        data: stats.value.datasourceDistribution.map((d, i) => ({
          ...d,
          itemStyle: { color: colors[i % colors.length] },
        })),
      },
    ],
  })
}

function initBarChart() {
  if (!barChartRef.value) return
  barChart = echarts.init(barChartRef.value)
  // 按数据源表数量 TOP10（mock 数据源仅4个，全部展示）
  const dsData = (stats.value && stats.value.datasourceDistribution) || []
  // 用数据源分布数据 + 数据源表数量作为柱状图
  const names: string[] = []
  const counts: number[] = []
  // 优先使用 tables 中按数据源聚合
  const dsMap = new Map<string, number>()
  tables.value.forEach((t) => {
    const key = `数据源#${t.datasourceId}`
    dsMap.set(key, (dsMap.get(key) || 0) + 1)
  })
  // 若聚合数据为空（mock 表只有5条），回退到数据源分布的 name/value
  if (dsMap.size === 0) {
    dsData.slice(0, 10).forEach((d) => {
      names.push(d.name)
      counts.push(d.value)
    })
  } else {
    const arr = Array.from(dsMap.entries())
      .map(([k, v]) => ({ name: k, value: v }))
      .sort((a, b) => b.value - a.value)
      .slice(0, 10)
    arr.forEach((d) => {
      names.push(d.name)
      counts.push(d.value)
    })
  }

  barChart.setOption({
    tooltip: { trigger: 'axis', axisPointer: { type: 'shadow' } },
    grid: { left: 50, right: 20, top: 20, bottom: 40 },
    xAxis: {
      type: 'category',
      data: names,
      axisLine: { lineStyle: { color: '#e7e7e7' } },
      axisLabel: { color: '#595959', rotate: names.length > 6 ? 20 : 0 },
    },
    yAxis: {
      type: 'value',
      axisLine: { show: false },
      splitLine: { lineStyle: { color: '#f0f0f0' } },
      axisLabel: { color: '#595959' },
    },
    series: [
      {
        type: 'bar',
        data: counts,
        barMaxWidth: 32,
        itemStyle: {
          borderRadius: [4, 4, 0, 0],
          color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
            { offset: 0, color: '#266fe8' },
            { offset: 1, color: '#0052d9' },
          ]),
        },
      },
    ],
  })
}

function resizeCharts() {
  distChart?.resize()
  barChart?.resize()
}

onMounted(async () => {
  const [s, t] = await Promise.all([getDashboardStats(), getMetadataTables()])
  stats.value = s
  tables.value = t
  metricCards.value[0].value = s.datasourceCount
  metricCards.value[1].value = s.tableCount
  metricCards.value[2].value = s.fieldCount
  metricCards.value[3].value = s.qualityScore
  // TOP10 表：按行数倒序
  topTables.value = [...t].sort((a, b) => b.rowCount - a.rowCount).slice(0, 10)
  loading.value = false
  await nextTick()
  initDistChart()
  initBarChart()
  window.addEventListener('resize', resizeCharts)
})

onUnmounted(() => {
  window.removeEventListener('resize', resizeCharts)
  distChart?.dispose()
  barChart?.dispose()
})
</script>

<template>
  <div class="page-container" v-loading="loading">
    <!-- 统计卡片 -->
    <div class="metric-grid">
      <div v-for="card in metricCards" :key="card.title" class="metric-card">
        <div class="metric-icon" :style="{ background: card.color + '15', color: card.color }">
          <t-icon :name="card.icon" size="24" />
        </div>
        <div class="metric-body">
          <div class="metric-value">
            {{ card.decimal ? card.value.toFixed(card.decimal) : formatNumber(card.value) }}
            <span class="metric-unit">{{ card.unit }}</span>
          </div>
          <div class="metric-title">{{ card.title }}</div>
        </div>
      </div>
    </div>

    <!-- 图表区 -->
    <div class="chart-row">
      <div class="page-card chart-card">
        <div class="flex-between" style="margin-bottom: 12px">
          <span class="card-label">数据源类型分布</span>
          <t-tag theme="primary" variant="light">环形图</t-tag>
        </div>
        <div ref="distChartRef" class="chart-box"></div>
      </div>
      <div class="page-card chart-card">
        <div class="flex-between" style="margin-bottom: 12px">
          <span class="card-label">各数据源表数量 TOP10</span>
          <t-tag theme="success" variant="light">柱状图</t-tag>
        </div>
        <div ref="barChartRef" class="chart-box"></div>
      </div>
    </div>

    <!-- TOP10 表列表 -->
    <div class="page-card">
      <div class="flex-between" style="margin-bottom: 16px">
        <span class="card-label">TOP10 表列表（按行数）</span>
        <t-button theme="primary" variant="text" @click="router.push('/metadata/datasource')">
          查看全部表 <t-icon name="chevron-right" />
        </t-button>
      </div>
      <t-table
        :data="topTables"
        row-key="id"
        :columns="[
          { colKey: 'tableName', title: '表名', ellipsis: true },
          { colKey: 'dbName', title: '所属库', width: 130 },
          { colKey: 'rowCount', title: '行数', width: 130, cell: 'rows' },
          { colKey: 'qualityScore', title: '质量分', width: 110, cell: 'score' },
          { colKey: 'owner', title: '负责人', width: 110 },
        ]"
        :pagination="{ show: false }"
        size="small"
      >
        <template #rows="{ row }">
          {{ formatRows(row.rowCount) }}
        </template>
        <template #score="{ row }">
          <t-tag
            size="small"
            variant="light"
            :theme="row.qualityScore >= 95 ? 'success' : row.qualityScore >= 85 ? 'primary' : 'warning'"
          >
            {{ row.qualityScore.toFixed(1) }}
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
  padding: 20px;
  display: flex;
  align-items: center;
  gap: 16px;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.04);
  transition: transform 0.2s, box-shadow 0.2s;
}
.metric-card:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.08);
}
.metric-icon {
  width: 52px;
  height: 52px;
  border-radius: 10px;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}
.metric-value {
  font-size: 28px;
  font-weight: 600;
  color: var(--text-primary);
  line-height: 1.2;
}
.metric-unit {
  font-size: 14px;
  font-weight: 400;
  color: var(--text-secondary);
  margin-left: 4px;
}
.metric-title {
  font-size: 13px;
  color: var(--text-secondary);
  margin-top: 4px;
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
  color: var(--text-primary);
}
.chart-box {
  height: 280px;
}
</style>
