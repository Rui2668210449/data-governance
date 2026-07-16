<script setup lang="ts">
import { ref, computed, onMounted, onUnmounted, nextTick, watch } from 'vue'
import * as echarts from 'echarts'
import { MessagePlugin } from 'tdesign-vue-next'
import { getDataStandards } from '@/api'
import type { DataStandard } from '@/types'
import { useAppStore } from '@/store/app'

const appStore = useAppStore()
const loading = ref(false)
const standards = ref<DataStandard[]>([])
const auditing = ref(false)

const gaugeChartRef = ref<HTMLDivElement>()
const barChartRef = ref<HTMLDivElement>()
let gaugeChart: echarts.ECharts | null = null
let barChart: echarts.ECharts | null = null

// 整体落标率（按 mappingCount 加权平均）
const overallRate = computed(() => {
  if (standards.value.length === 0) return 0
  const totalMap = standards.value.reduce((s, x) => s + x.mappingCount, 0)
  const weighted = standards.value.reduce(
    (s, x) => s + x.complianceRate * x.mappingCount,
    0
  )
  return totalMap > 0 ? weighted / totalMap : 0
})

const overallStatus = computed(() => {
  const r = overallRate.value
  if (r < 60) return { text: '不达标', color: '#e34d59' }
  if (r < 90) return { text: '待改进', color: '#ed7b2f' }
  return { text: '达标', color: '#00a870' }
})

// 按业务域分组：各域落标率（按 mappingCount 加权）
const domainStats = computed(() => {
  const map = new Map<string, { totalMap: number; weighted: number }>()
  standards.value.forEach((s) => {
    const cur = map.get(s.category) || { totalMap: 0, weighted: 0 }
    cur.totalMap += s.mappingCount
    cur.weighted += s.complianceRate * s.mappingCount
    map.set(s.category, cur)
  })
  return Array.from(map.entries()).map(([cat, v]) => ({
    domain: cat,
    rate: v.totalMap > 0 ? v.weighted / v.totalMap : 0,
    count: v.totalMap,
  }))
})

// 明细列表：从 standards 生成模拟明细
interface AuditDetail {
  id: number
  tableName: string
  fieldName: string
  standardName: string
  compliant: boolean
  issue: string
}
const details = computed<AuditDetail[]>(() => {
  const list: AuditDetail[] = []
  const tableNames = ['user_info', 'order_detail', 'ods_user_log_d', 'rpt_user_active_m']
  const fieldPool = [
    { name: 'user_id', std: '用户ID' },
    { name: 'phone', std: '手机号' },
    { name: 'id_card', std: '身份证号' },
    { name: 'pay_amount', std: '订单金额' },
    { name: 'create_time', std: '订单时间' },
    { name: 'status', std: '用户状态' },
  ]
  let id = 1
  standards.value.forEach((s) => {
    // 每个标准关联 1-2 个明细
    const matched = fieldPool.find((f) => f.std === s.name)
    const targets = matched ? [matched] : []
    if (targets.length === 0) return
    targets.forEach((t) => {
      const compliant = s.complianceRate >= 95
      list.push({
        id: id++,
        tableName: tableNames[id % tableNames.length],
        fieldName: t.name,
        standardName: s.name,
        compliant,
        issue: compliant
          ? '—'
          : s.complianceRate < 60
          ? '字段未关联标准'
          : '部分数据未按标准格式存储',
      })
    })
  })
  return list
})

const detailColumns = [
  { colKey: 'tableName', title: '表名', minWidth: 160, ellipsis: true },
  { colKey: 'fieldName', title: '字段名', width: 130 },
  { colKey: 'standardName', title: '关联标准', width: 130 },
  { colKey: 'compliant', title: '是否合规', width: 100, cell: 'compliant' },
  { colKey: 'issue', title: '问题描述', minWidth: 200, ellipsis: true },
]

function initGaugeChart() {
  if (!gaugeChartRef.value) return
  gaugeChart = echarts.init(gaugeChartRef.value)
  drawGauge()
}

function drawGauge() {
  if (!gaugeChart) return
  const r = overallRate.value
  const color = r < 60 ? '#e34d59' : r < 90 ? '#ed7b2f' : '#00a870'
  gaugeChart.setOption({
    series: [
      {
        type: 'gauge',
        startAngle: 200,
        endAngle: -20,
        min: 0,
        max: 100,
        progress: {
          show: true,
          width: 18,
          itemStyle: { color },
        },
        axisLine: {
          lineStyle: { width: 18, color: [[1, '#f0f0f0']] },
        },
        pointer: { show: false },
        axisTick: { show: false },
        axisLabel: { show: false },
        splitLine: { show: false },
        anchor: { show: false },
        detail: {
          valueAnimation: true,
          formatter: '{value}%',
          fontSize: 36,
          fontWeight: 'bold',
          color,
          offsetCenter: [0, '10%'],
        },
        title: {
          show: true,
          offsetCenter: [0, '50%'],
          fontSize: 14,
          color: '#595959',
        },
        data: [{ value: Number(r.toFixed(1)), name: '整体落标率' }],
      },
    ],
  })
}

function initBarChart() {
  if (!barChartRef.value) return
  barChart = echarts.init(barChartRef.value)
  drawBar()
}

function drawBar() {
  if (!barChart) return
  const data = domainStats.value
  barChart.setOption({
    tooltip: {
      trigger: 'axis',
      axisPointer: { type: 'shadow' },
      formatter: (params: { name: string; value: number }[]) => {
        const p = params[0]
        const item = data.find((d) => d.domain === p.name)
        return `${p.name}<br/>落标率: ${p.value.toFixed(1)}%<br/>映射数: ${item?.count || 0}`
      },
    },
    grid: { left: 50, right: 20, top: 20, bottom: 40 },
    xAxis: {
      type: 'category',
      data: data.map((d) => d.domain),
      axisLine: { lineStyle: { color: '#e7e7e7' } },
      axisLabel: { color: '#595959' },
    },
    yAxis: {
      type: 'value',
      min: 0,
      max: 100,
      axisLine: { show: false },
      splitLine: { lineStyle: { color: '#f0f0f0' } },
      axisLabel: { color: '#595959', formatter: '{value}%' },
    },
    series: [
      {
        type: 'bar',
        data: data.map((d) => ({
          value: Number(d.rate.toFixed(1)),
          itemStyle: {
            color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
              { offset: 0, color: d.rate >= 90 ? '#00a870' : d.rate >= 60 ? '#ed7b2f' : '#e34d59' },
              { offset: 1, color: d.rate >= 90 ? '#7ed4a8' : d.rate >= 60 ? '#f5b582' : '#f2989e' },
            ]),
            borderRadius: [4, 4, 0, 0],
          },
        })),
        barMaxWidth: 48,
        label: {
          show: true,
          position: 'top',
          formatter: '{c}%',
          color: '#595959',
          fontSize: 12,
        },
      },
    ],
  })
}

function resizeCharts() {
  gaugeChart?.resize()
  barChart?.resize()
}

watch(() => appStore.collapsed, () => {
  nextTick(resizeCharts)
})

async function loadData() {
  loading.value = true
  try {
    standards.value = await getDataStandards()
    await nextTick()
    initGaugeChart()
    initBarChart()
  } finally {
    loading.value = false
  }
}

async function handleReaudit() {
  auditing.value = true
  MessagePlugin.info('开始重新稽核...')
  // 模拟稽核延迟
  await new Promise((resolve) => setTimeout(resolve, 800))
  // 对 complianceRate 做小幅扰动以体现稽核刷新
  standards.value = standards.value.map((s) => ({
    ...s,
    complianceRate: Math.min(100, Math.max(0, s.complianceRate + (Math.random() * 2 - 1))),
  }))
  await nextTick()
  drawGauge()
  drawBar()
  MessagePlugin.success('稽核完成，已更新数据')
  auditing.value = false
}

function handleExport() {
  MessagePlugin.success(`已导出 ${details.value.length} 条明细`)
}

onMounted(async () => {
  await loadData()
  window.addEventListener('resize', resizeCharts)
})

onUnmounted(() => {
  window.removeEventListener('resize', resizeCharts)
  gaugeChart?.dispose()
  barChart?.dispose()
})
</script>

<template>
  <div class="page-container" v-loading="loading">
    <!-- 顶部工具栏 -->
    <div class="page-card">
      <div class="flex-between">
        <h3 class="page-title" style="margin: 0">落标稽核报告</h3>
        <div class="flex gap-12">
          <t-button theme="default" variant="outline" :loading="auditing" @click="handleReaudit">
            <template #icon><t-icon name="refresh" /></template>
            重新稽核
          </t-button>
          <t-button theme="primary" @click="handleExport">
            <template #icon><t-icon name="download" /></template>
            导出报告
          </t-button>
        </div>
      </div>
    </div>

    <!-- 顶部状态 + 仪表盘 -->
    <div class="page-card">
      <div class="audit-top">
        <div class="gauge-wrap">
          <div ref="gaugeChartRef" class="gauge-box"></div>
          <div class="gauge-status" :style="{ color: overallStatus.color }">
            <t-icon
              :name="overallStatus.text === '达标' ? 'check-circle' : overallStatus.text === '待改进' ? 'error-triangle' : 'error-circle'"
              size="16"
            />
            <span>{{ overallStatus.text }}</span>
          </div>
        </div>
        <div class="top-right">
          <div class="stat-card">
            <div class="stat-num" style="color: #0052d9">{{ standards.length }}</div>
            <div class="stat-label">标准总数</div>
          </div>
          <div class="stat-card">
            <div class="stat-num" style="color: #7265e6">
              {{ standards.reduce((s, x) => s + x.mappingCount, 0) }}
            </div>
            <div class="stat-label">映射字段数</div>
          </div>
          <div class="stat-card">
            <div class="stat-num" style="color: #e34d59">
              {{ details.filter((d) => !d.compliant).length }}
            </div>
            <div class="stat-label">不合规字段</div>
          </div>
          <div class="legend-box">
            <div class="legend-item">
              <span class="legend-dot" style="background: #00a870"></span>
              <span>≥90% 达标</span>
            </div>
            <div class="legend-item">
              <span class="legend-dot" style="background: #ed7b2f"></span>
              <span>60-90% 待改进</span>
            </div>
            <div class="legend-item">
              <span class="legend-dot" style="background: #e34d59"></span>
              <span>&lt;60% 不达标</span>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- 各业务域落标率对比 -->
    <div class="page-card">
      <div class="flex-between" style="margin-bottom: 12px">
        <span class="card-label">各业务域落标率对比</span>
        <t-tag theme="primary" variant="light" size="small">按映射数加权</t-tag>
      </div>
      <div ref="barChartRef" class="bar-box"></div>
    </div>

    <!-- 明细列表 -->
    <div class="page-card">
      <div class="flex-between" style="margin-bottom: 12px">
        <span class="card-label">稽核明细</span>
        <t-tag theme="warning" variant="light" size="small">
          不合规 {{ details.filter((d) => !d.compliant).length }} 项
        </t-tag>
      </div>
      <t-table
        :data="details"
        row-key="id"
        :columns="detailColumns"
        size="small"
        :pagination="{ pageSize: 8 }"
        bordered
      >
        <template #compliant="{ row }">
          <t-tag
            :theme="row.compliant ? 'success' : 'danger'"
            variant="light"
            size="small"
          >
            {{ row.compliant ? '合规' : '不合规' }}
          </t-tag>
        </template>
      </t-table>
    </div>
  </div>
</template>

<style scoped>
.page-title {
  margin: 0;
}
.card-label {
  font-size: 15px;
  font-weight: 600;
}
.audit-top {
  display: grid;
  grid-template-columns: 320px 1fr;
  gap: 24px;
  align-items: center;
}
.gauge-wrap {
  text-align: center;
}
.gauge-box {
  height: 240px;
}
.gauge-status {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  font-size: 15px;
  font-weight: 600;
  margin-top: -10px;
}
.top-right {
  display: grid;
  grid-template-columns: repeat(3, 1fr) 1.5fr;
  gap: 12px;
}
.stat-card {
  background: #fafbfc;
  border: 1px solid var(--border-color);
  border-radius: 6px;
  padding: 16px;
  text-align: center;
}
.stat-num {
  font-size: 26px;
  font-weight: 600;
  line-height: 1.2;
}
.stat-label {
  font-size: 12px;
  color: var(--text-secondary);
  margin-top: 6px;
}
.legend-box {
  background: #fafbfc;
  border: 1px solid var(--border-color);
  border-radius: 6px;
  padding: 16px;
  display: flex;
  flex-direction: column;
  justify-content: center;
  gap: 10px;
}
.legend-item {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 13px;
  color: var(--text-secondary);
}
.legend-dot {
  display: inline-block;
  width: 10px;
  height: 10px;
  border-radius: 50%;
}
.bar-box {
  height: 280px;
}
</style>
