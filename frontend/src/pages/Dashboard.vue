<script setup lang="ts">
import { ref, onMounted, onUnmounted, watch, nextTick } from 'vue'
import * as echarts from 'echarts'
import { useRouter } from 'vue-router'
import { getDashboardStats, getMyTodo } from '@/api'
import type { DashboardStats, WorkflowTask } from '@/types'
import { useAppStore } from '@/store/app'

const router = useRouter()
const appStore = useAppStore()

const stats = ref<DashboardStats | null>(null)
const todos = ref<WorkflowTask[]>([])
const loading = ref(true)

const trendChartRef = ref<HTMLDivElement>()
const distChartRef = ref<HTMLDivElement>()
let trendChart: echarts.ECharts | null = null
let distChart: echarts.ECharts | null = null

const modules = [
  { title: '元数据管理', icon: 'server', color: '#0052d9', path: '/metadata/overview', desc: '血缘解析·变更审计' },
  { title: '数据标准', icon: 'edit-1', color: '#7265e6', path: '/standard/tree', desc: '标准管理·落标稽核' },
  { title: '数据质量', icon: 'check-circle', color: '#00a870', path: '/quality/dashboard', desc: '规则监控·工单闭环' },
  { title: '数据安全', icon: 'lock-on', color: '#e34d59', path: '/security/classify', desc: '分级分类·脱敏审计' },
  { title: '资产目录', icon: 'folder', color: '#ed7b2f', path: '/catalog/search', desc: '智能检索·资产评价' },
  { title: '生命周期', icon: 'time', color: '#266fe8', path: '/lifecycle/dashboard', desc: '冷热分层·归档销毁' },
  { title: '治理工作流', icon: 'root-list', color: '#00a8e5', path: '/workflow/board', desc: '任务协同·审批流' },
]

const metricCards = ref([
  { title: '数据源', value: 0, icon: 'data-base', color: '#0052d9', unit: '个', suffix: '' },
  { title: '数据表', value: 0, icon: 'server', color: '#00a870', unit: '张', suffix: '' },
  { title: '字段总数', value: 0, icon: 'edit-1', color: '#ed7b2f', unit: '个', suffix: '' },
  { title: '质量评分', value: 0, icon: 'check-circle', color: '#7265e6', unit: '分', suffix: '', decimal: 1 },
])

function formatNumber(n: number) {
  return n.toLocaleString('zh-CN')
}

function initTrendChart() {
  if (!trendChartRef.value || !stats.value) return
  trendChart = echarts.init(trendChartRef.value)
  trendChart.setOption({
    tooltip: { trigger: 'axis' },
    grid: { left: 40, right: 20, top: 20, bottom: 30 },
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

function initDistChart() {
  if (!distChartRef.value || !stats.value) return
  distChart = echarts.init(distChartRef.value)
  const colors = ['#0052d9', '#266fe8', '#00a8e5', '#7265e6']
  distChart.setOption({
    tooltip: { trigger: 'item' },
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

function resizeCharts() {
  trendChart?.resize()
  distChart?.resize()
}

watch(() => appStore.collapsed, () => {
  nextTick(resizeCharts)
})

onMounted(async () => {
  const [s, t] = await Promise.all([getDashboardStats(), getMyTodo()])
  stats.value = s
  todos.value = t
  metricCards.value[0].value = s.datasourceCount
  metricCards.value[1].value = s.tableCount
  metricCards.value[2].value = s.fieldCount
  metricCards.value[3].value = s.qualityScore
  loading.value = false
  await nextTick()
  initTrendChart()
  initDistChart()
  window.addEventListener('resize', resizeCharts)
})

onUnmounted(() => {
  window.removeEventListener('resize', resizeCharts)
  trendChart?.dispose()
  distChart?.dispose()
})

const todoColorMap: Record<string, string> = {
  HIGH: '#e34d59',
  MEDIUM: '#ed7b2f',
  LOW: '#0052d9',
}
const todoTypeMap: Record<string, string> = {
  QUALITY: '质量',
  SECURITY: '安全',
  STANDARD: '标准',
  APPROVAL: '审批',
}
</script>

<template>
  <div class="page-container" v-loading="loading">
    <!-- 指标卡片 -->
    <div class="metric-grid">
      <div v-for="card in metricCards" :key="card.title" class="metric-card">
        <div class="metric-icon" :style="{ background: card.color + '15', color: card.color }">
          <t-icon :name="card.icon" size="24" />
        </div>
        <div class="metric-body">
          <div class="metric-value">
            {{ card.decimal ? card.value.toFixed(card.decimal as number) : formatNumber(card.value) }}
            <span class="metric-unit">{{ card.unit }}</span>
          </div>
          <div class="metric-title">{{ card.title }}</div>
        </div>
      </div>
    </div>

    <!-- 图表区 -->
    <div class="chart-row">
      <div class="page-card chart-card trend-card">
        <div class="flex-between" style="margin-bottom: 12px">
          <span class="card-label">质量评分趋势</span>
          <t-tag theme="success" variant="light">近7日</t-tag>
        </div>
        <div ref="trendChartRef" class="chart-box"></div>
      </div>
      <div class="page-card chart-card dist-card">
        <div class="flex-between" style="margin-bottom: 12px">
          <span class="card-label">数据源分布</span>
          <t-tag theme="primary" variant="light">{{ stats?.tableCount || 0 }} 张表</t-tag>
        </div>
        <div ref="distChartRef" class="chart-box"></div>
      </div>
    </div>

    <!-- 模块入口 -->
    <div class="page-card">
      <div class="card-label" style="margin-bottom: 16px">治理模块</div>
      <div class="module-grid">
        <div
          v-for="m in modules"
          :key="m.title"
          class="module-card"
          @click="router.push(m.path)"
        >
          <div class="module-icon" :style="{ background: m.color + '15', color: m.color }">
            <t-icon :name="m.icon" size="26" />
          </div>
          <div class="module-info">
            <div class="module-title">{{ m.title }}</div>
            <div class="module-desc">{{ m.desc }}</div>
          </div>
          <t-icon name="chevron-right" size="18" class="module-arrow" />
        </div>
      </div>
    </div>

    <!-- 待办列表 -->
    <div class="page-card">
      <div class="flex-between" style="margin-bottom: 16px">
        <span class="card-label">我的待办</span>
        <t-button theme="primary" variant="text" @click="router.push('/workflow/todo')">
          查看全部 <t-icon name="chevron-right" />
        </t-button>
      </div>
      <t-table
        :data="todos"
        row-key="id"
        :columns="[
          { colKey: 'title', title: '任务标题', ellipsis: true },
          { colKey: 'type', title: '类型', width: 90, cell: 'type' },
          { colKey: 'priority', title: '优先级', width: 90, cell: 'priority' },
          { colKey: 'assignee', title: '负责人', width: 110 },
          { colKey: 'dueDate', title: '截止日期', width: 120 },
        ]"
        :pagination="{ show: false }"
        size="small"
      >
        <template #type="{ row }">
          <t-tag size="small" variant="light">{{ todoTypeMap[row.type] || row.type }}</t-tag>
        </template>
        <template #priority="{ row }">
          <t-tag size="small" :color="todoColorMap[row.priority]" variant="light">
            {{ row.priority === 'HIGH' ? '高' : row.priority === 'MEDIUM' ? '中' : '低' }}
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
  grid-template-columns: 2fr 1fr;
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
  height: 260px;
}
.module-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 12px;
}
.module-card {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 16px;
  border: 1px solid var(--border-color);
  border-radius: 8px;
  cursor: pointer;
  transition: all 0.2s;
}
.module-card:hover {
  border-color: var(--brand-color);
  background: rgba(0, 82, 217, 0.03);
}
.module-card:hover .module-arrow {
  transform: translateX(4px);
  color: var(--brand-color);
}
.module-icon {
  width: 44px;
  height: 44px;
  border-radius: 8px;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}
.module-info {
  flex: 1;
  min-width: 0;
}
.module-title {
  font-size: 14px;
  font-weight: 500;
  color: var(--text-primary);
}
.module-desc {
  font-size: 12px;
  color: var(--text-secondary);
  margin-top: 2px;
}
.module-arrow {
  color: var(--text-secondary);
  transition: transform 0.2s, color 0.2s;
}
</style>
