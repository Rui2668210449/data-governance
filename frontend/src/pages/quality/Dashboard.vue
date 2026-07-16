<script setup lang="ts">
import { ref, onMounted, onUnmounted, watch, nextTick } from 'vue'
import * as echarts from 'echarts'
import { getDashboardStats, getQualityTickets } from '@/api'
import { qualityTickets as mockTickets } from '@/mock'
import { useAppStore } from '@/store/app'
import type { DashboardStats, QualityTicket } from '@/types'

const appStore = useAppStore()
const loading = ref(true)
const stats = ref<DashboardStats | null>(null)
const tickets = ref<QualityTicket[]>([])

const trendChartRef = ref<HTMLDivElement>()
const heatChartRef = ref<HTMLDivElement>()
let trendChart: echarts.ECharts | null = null
let heatChart: echarts.ECharts | null = null

const dimensions = ['完整性', '唯一性', '准确性', '规范性', '时效性']
const tables = ['user_info', 'order_detail', 'ods_user_log_d', 'ods_trade_dwd_d', 'rpt_user_active_m']

// 维度评分卡片数据
const dimensionCards = ref([
  { name: '完整性', score: 98.5, icon: 'check-double', color: '#0052d9', trend: '+0.3' },
  { name: '唯一性', score: 99.2, icon: 'lock-on', color: '#00a870', trend: '+0.1' },
  { name: '准确性', score: 96.8, icon: 'aim', color: '#e34d59', trend: '-0.2' },
  { name: '规范性', score: 95.8, icon: 'edit-1', color: '#ed7b2f', trend: '+0.5' },
  { name: '时效性', score: 92.0, icon: 'time', color: '#7265e6', trend: '-1.2' },
])

// 热力图数据 [x维度index, y表index, score]
const heatData: [number, number, number][] = [
  [0, 0, 100], [0, 1, 99.5], [0, 2, 95.2], [0, 3, 97.8], [0, 4, 100],
  [1, 0, 99.2], [1, 1, 98.5], [1, 2, 96.0], [1, 3, 98.0], [1, 4, 100],
  [2, 0, 100], [2, 1, 100], [2, 2, 88.5], [2, 3, 96.8], [2, 4, 99.0],
  [3, 0, 95.8], [3, 1, 92.0], [3, 2, 85.0], [3, 3, 90.5], [3, 4, 98.0],
  [4, 0, 96.0], [4, 1, 94.5], [4, 2, 80.0], [4, 3, 88.0], [4, 4, 92.5],
]

const statusTagMap: Record<string, { text: string; theme: 'danger' | 'warning' | 'success' | 'default' }> = {
  OPEN: { text: '待处理', theme: 'danger' },
  PROCESSING: { text: '处理中', theme: 'warning' },
  RESOLVED: { text: '已解决', theme: 'success' },
  CLOSED: { text: '已关闭', theme: 'default' },
}

const priorityTagMap: Record<string, { text: string; theme: 'danger' | 'warning' | 'primary' }> = {
  HIGH: { text: '高', theme: 'danger' },
  MEDIUM: { text: '中', theme: 'warning' },
  LOW: { text: '低', theme: 'primary' },
}

const ticketColumns = [
  { colKey: 'title', title: '问题标题', minWidth: 220, ellipsis: true },
  { colKey: 'ruleName', title: '关联规则', width: 160, ellipsis: true },
  { colKey: 'status', title: '状态', width: 100, cell: 'status' },
  { colKey: 'priority', title: '优先级', width: 90, cell: 'priority' },
  { colKey: 'assignee', title: '负责人', width: 100 },
  { colKey: 'createTime', title: '时间', width: 170 },
]

function scoreColor(score: number) {
  if (score >= 95) return '#00a870'
  if (score >= 85) return '#ed7b2f'
  return '#e34d59'
}

function initTrendChart() {
  if (!trendChartRef.value || !stats.value) return
  trendChart = echarts.init(trendChartRef.value)
  trendChart.setOption({
    tooltip: { trigger: 'axis' },
    grid: { left: 50, right: 20, top: 30, bottom: 30 },
    xAxis: {
      type: 'category',
      data: stats.value.qualityScoreTrend.map((d) => d.date),
      axisLine: { lineStyle: { color: '#e7e7e7' } },
      axisLabel: { color: '#595959' },
    },
    yAxis: {
      type: 'value',
      min: 80,
      max: 100,
      axisLine: { show: false },
      splitLine: { lineStyle: { color: '#f0f0f0' } },
      axisLabel: { color: '#595959' },
    },
    series: [
      {
        name: '质量评分',
        data: stats.value.qualityScoreTrend.map((d) => d.score),
        type: 'line',
        smooth: true,
        symbol: 'circle',
        symbolSize: 8,
        lineStyle: { width: 3, color: '#0052d9' },
        itemStyle: { color: '#0052d9' },
        areaStyle: {
          color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
            { offset: 0, color: 'rgba(0,82,217,0.25)' },
            { offset: 1, color: 'rgba(0,82,217,0.02)' },
          ]),
        },
      },
    ],
  })
}

function initHeatChart() {
  if (!heatChartRef.value) return
  heatChart = echarts.init(heatChartRef.value)
  heatChart.setOption({
    tooltip: {
      position: 'top',
      formatter: (params: { value: [number, number, number] }) => {
        return `${tables[params.value[1]]}<br/>${dimensions[params.value[0]]}: <b>${params.value[2]}</b>`
      },
    },
    grid: { left: 130, right: 20, top: 20, bottom: 80 },
    xAxis: {
      type: 'category',
      data: dimensions,
      splitArea: { show: true },
      axisLine: { lineStyle: { color: '#e7e7e7' } },
      axisLabel: { color: '#595959' },
    },
    yAxis: {
      type: 'category',
      data: tables,
      splitArea: { show: true },
      axisLine: { lineStyle: { color: '#e7e7e7' } },
      axisLabel: { color: '#595959' },
    },
    visualMap: {
      min: 80,
      max: 100,
      calculable: true,
      orient: 'horizontal',
      left: 'center',
      bottom: 10,
      inRange: {
        color: ['#e34d59', '#ed7b2f', '#fac036', '#00a870'],
      },
    },
    series: [
      {
        name: '质量评分',
        type: 'heatmap',
        data: heatData,
        label: { show: true, fontSize: 11, color: '#181818' },
        emphasis: {
          itemStyle: { shadowBlur: 10, shadowColor: 'rgba(0, 0, 0, 0.5)' },
        },
      },
    ],
  })
}

function resizeCharts() {
  trendChart?.resize()
  heatChart?.resize()
}

watch(() => appStore.collapsed, () => {
  nextTick(resizeCharts)
})

onMounted(async () => {
  const [s, t] = await Promise.all([
    getDashboardStats(),
    getQualityTickets(),
  ])
  stats.value = s
  tickets.value = t.length ? t : mockTickets
  loading.value = false
  await nextTick()
  initTrendChart()
  initHeatChart()
  window.addEventListener('resize', resizeCharts)
})

onUnmounted(() => {
  window.removeEventListener('resize', resizeCharts)
  trendChart?.dispose()
  heatChart?.dispose()
})
</script>

<template>
  <div class="page-container" v-loading="loading">
    <!-- 顶部质量分卡片 + 同比环比 -->
    <div class="page-card score-hero">
      <div class="hero-left">
        <div class="hero-label">数据质量总评分</div>
        <div class="hero-score">
          <span class="hero-score-num">96.5</span>
          <span class="hero-score-unit">分</span>
        </div>
        <div class="hero-trend">
          <t-tag theme="success" variant="light" size="small">
            <template #icon><t-icon name="trending-up" /></template>
            环比 +2.3%
          </t-tag>
          <t-tag theme="primary" variant="light" size="small">
            <template #icon><t-icon name="trending-up" /></template>
            同比 +5.1%
          </t-tag>
        </div>
      </div>
      <div class="hero-right">
        <div class="hero-stat">
          <div class="hero-stat-value" style="color: #e34d59">{{ tickets.filter(t => t.status === 'OPEN').length }}</div>
          <div class="hero-stat-label">待处理工单</div>
        </div>
        <div class="hero-stat">
          <div class="hero-stat-value" style="color: #ed7b2f">{{ tickets.filter(t => t.status === 'PROCESSING').length }}</div>
          <div class="hero-stat-label">处理中</div>
        </div>
        <div class="hero-stat">
          <div class="hero-stat-value" style="color: #00a870">{{ tickets.filter(t => t.status === 'RESOLVED').length }}</div>
          <div class="hero-stat-label">已解决</div>
        </div>
        <div class="hero-stat">
          <div class="hero-stat-value" style="color: #0052d9">{{ stats?.tableCount || 0 }}</div>
          <div class="hero-stat-label">监控表数</div>
        </div>
      </div>
    </div>

    <!-- 5维度评分卡片 -->
    <div class="dim-grid">
      <div v-for="d in dimensionCards" :key="d.name" class="dim-card">
        <div class="dim-ring">
          <t-progress
            :percentage="d.score"
            theme="circle"
            size="64px"
            :color="d.color"
            track-color="#f0f0f0"
            :label="false"
          />
          <div class="dim-ring-score" :style="{ color: d.color }">{{ d.score }}</div>
        </div>
        <div class="dim-info">
          <div class="dim-name">
            <t-icon :name="d.icon" :style="{ color: d.color }" />
            <span>{{ d.name }}</span>
          </div>
          <div class="dim-trend" :style="{ color: d.trend.startsWith('-') ? '#e34d59' : '#00a870' }">
            {{ d.trend.startsWith('-') ? '' : '+' }}{{ d.trend }} 同比
          </div>
        </div>
      </div>
    </div>

    <!-- 折线图 + 热力图 -->
    <div class="chart-row">
      <div class="page-card chart-card trend-card">
        <div class="flex-between" style="margin-bottom: 12px">
          <span class="card-label">质量评分趋势</span>
          <t-tag theme="success" variant="light">近7日</t-tag>
        </div>
        <div ref="trendChartRef" class="chart-box"></div>
      </div>
      <div class="page-card chart-card heat-card">
        <div class="flex-between" style="margin-bottom: 12px">
          <span class="card-label">维度 × 数据表 质量热力矩阵</span>
          <t-tag theme="primary" variant="light">5×5</t-tag>
        </div>
        <div ref="heatChartRef" class="chart-box"></div>
      </div>
    </div>

    <!-- 问题列表 -->
    <div class="page-card">
      <div class="flex-between" style="margin-bottom: 16px">
        <span class="card-label">质量工单列表</span>
        <t-tag theme="warning" variant="light">{{ tickets.length }} 条</t-tag>
      </div>
      <t-table
        :data="tickets"
        row-key="id"
        :columns="ticketColumns"
        size="small"
        :pagination="{ show: false }"
        bordered
      >
        <template #status="{ row }">
          <t-tag :theme="statusTagMap[row.status].theme" variant="light" size="small">
            {{ statusTagMap[row.status].text }}
          </t-tag>
        </template>
        <template #priority="{ row }">
          <t-tag :theme="priorityTagMap[row.priority].theme" variant="light" size="small">
            {{ priorityTagMap[row.priority].text }}
          </t-tag>
        </template>
      </t-table>
    </div>
  </div>
</template>

<style scoped>
.score-hero {
  display: flex;
  align-items: center;
  justify-content: space-between;
  background: linear-gradient(135deg, #0052d9 0%, #266fe8 100%);
  color: #fff;
}
.hero-left {
  display: flex;
  flex-direction: column;
  gap: 8px;
}
.hero-label {
  font-size: 14px;
  opacity: 0.85;
}
.hero-score {
  display: flex;
  align-items: baseline;
  gap: 6px;
}
.hero-score-num {
  font-size: 48px;
  font-weight: 700;
  line-height: 1;
}
.hero-score-unit {
  font-size: 16px;
  opacity: 0.85;
}
.hero-trend {
  display: flex;
  gap: 8px;
}
.hero-right {
  display: flex;
  gap: 32px;
}
.hero-stat {
  text-align: center;
}
.hero-stat-value {
  font-size: 28px;
  font-weight: 600;
  line-height: 1.2;
}
.hero-stat-label {
  font-size: 12px;
  opacity: 0.85;
  margin-top: 4px;
}
.dim-grid {
  display: grid;
  grid-template-columns: repeat(5, 1fr);
  gap: 12px;
  margin-bottom: 16px;
}
.dim-card {
  background: #fff;
  border-radius: 6px;
  padding: 16px;
  display: flex;
  align-items: center;
  gap: 12px;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.04);
}
.dim-ring {
  position: relative;
  width: 60px;
  height: 60px;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}
.dim-ring-score {
  position: absolute;
  font-size: 14px;
  font-weight: 600;
}
.dim-info {
  flex: 1;
  min-width: 0;
}
.dim-name {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 14px;
  font-weight: 500;
  color: var(--text-primary);
}
.dim-trend {
  font-size: 12px;
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
  height: 320px;
}
</style>
