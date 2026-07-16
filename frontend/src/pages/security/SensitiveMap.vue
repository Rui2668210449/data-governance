<script setup lang="ts">
import { ref, computed, onMounted, onUnmounted, nextTick, watch } from 'vue'
import * as echarts from 'echarts'
import {
  getClassifyLabels,
  getMetadataTables,
  getTableFields,
  getMaskPolicies,
  getDatasources,
} from '@/api'
import type {
  ClassifyLabel,
  MetadataTable,
  MetadataField,
  MaskPolicy,
  Datasource,
} from '@/types'
import { useAppStore } from '@/store/app'

const appStore = useAppStore()
const loading = ref(true)
const labels = ref<ClassifyLabel[]>([])
const tables = ref<MetadataTable[]>([])
const fields = ref<MetadataField[]>([])
const policies = ref<MaskPolicy[]>([])
const datasources = ref<Datasource[]>([])

const chartRef = ref<HTMLDivElement>()
let chart: echarts.ECharts | null = null

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
const levelList = ['L1', 'L2', 'L3', 'L4']

function guessLevel(fieldName: string): string {
  if (/id_?card|id_?no|bank|card_?no/i.test(fieldName)) return 'L4'
  if (/phone|mobile|email|mail|address|real_?name/i.test(fieldName)) return 'L3'
  if (/amount|money|price|salary|balance/i.test(fieldName)) return 'L2'
  return 'L1'
}

function guessType(fieldName: string): string {
  if (/phone|mobile/i.test(fieldName)) return 'VARCHAR(16)'
  if (/id|card/i.test(fieldName)) return 'VARCHAR(32)'
  if (/email|mail/i.test(fieldName)) return 'VARCHAR(128)'
  if (/amount|money|price/i.test(fieldName)) return 'DECIMAL(12,2)'
  return 'VARCHAR(64)'
}

// 敏感字段汇总: 组合 mock 字段(isSensitive=1) + 脱敏策略字段 + 补充未脱敏样例
const sensitiveFields = computed(() => {
  const fromFields = fields.value
    .filter((f) => f.isSensitive === 1)
    .map((f) => {
      const table = tables.value.find((t) => t.id === f.tableId)
      return {
        tableName: table?.tableName || '-',
        fieldName: f.fieldName,
        dataType: f.dataType,
        level: guessLevel(f.fieldName),
        masked: policies.value.some(
          (p) => p.tableId === f.tableId && p.fieldName === f.fieldName,
        ),
      }
    })
  const extra = policies.value
    .filter((p) => !fields.value.some((f) => f.tableId === p.tableId && f.fieldName === p.fieldName))
    .map((p) => ({
      tableName: p.tableName,
      fieldName: p.fieldName,
      dataType: guessType(p.fieldName),
      level: guessLevel(p.fieldName),
      masked: true,
    }))
  // 补充未脱敏样例以体现覆盖率
  const synthesized = [
    { tableName: 'order_detail', fieldName: 'user_phone', dataType: 'VARCHAR(16)', level: 'L3', masked: false },
    { tableName: 'ods_trade_dwd_d', fieldName: 'mobile', dataType: 'VARCHAR(16)', level: 'L3', masked: false },
    { tableName: 'ods_trade_dwd_d', fieldName: 'id_no', dataType: 'VARCHAR(32)', level: 'L4', masked: false },
  ]
  const merged = [...fromFields, ...extra, ...synthesized]
  return merged.map((item, idx) => ({ ...item, id: idx + 1 }))
})

const stats = computed(() => {
  const total = sensitiveFields.value.length
  const masked = sensitiveFields.value.filter((f) => f.masked).length
  const unmasked = total - masked
  const coverage = total ? Math.round((masked / total) * 1000) / 10 : 0
  return { total, masked, unmasked, coverage }
})

const dsNames = computed(() => datasources.value.map((d) => d.name))

const bubbleData = computed(() => {
  const result: Array<{
    value: [number, number, number]
    tableName: string
    level: string
    count: number
  }> = []
  const map = new Map<string, { x: number; y: number; count: number; tableName: string; level: string }>()
  sensitiveFields.value.forEach((f) => {
    const table = tables.value.find((t) => t.tableName === f.tableName)
    const ds = table ? datasources.value.find((d) => d.id === table.datasourceId) : null
    const dsName = ds?.name || '未知'
    const xIdx = Math.max(0, dsNames.value.indexOf(dsName))
    const yIdx = levelList.indexOf(f.level)
    if (yIdx < 0) return
    const key = `${xIdx}-${yIdx}`
    const cur = map.get(key)
    if (cur) {
      cur.count += 1
    } else {
      map.set(key, { x: xIdx, y: yIdx, count: 1, tableName: dsName, level: f.level })
    }
  })
  map.forEach((d) => {
    result.push({
      value: [d.x, d.y, d.count],
      tableName: d.tableName,
      level: d.level,
      count: d.count,
    })
  })
  return result
})

function initChart() {
  if (!chartRef.value) return
  chart = echarts.init(chartRef.value)
  chart.setOption({
    tooltip: {
      formatter: (p: { data: { tableName: string; level: string; count: number } }) => {
        const d = p.data
        return `${d.tableName}<br/>级别: ${d.level} ${levelNames[d.level] || ''}<br/>敏感字段数: ${d.count}`
      },
    },
    grid: { left: 70, right: 30, top: 30, bottom: 50 },
    xAxis: {
      type: 'category',
      data: dsNames.value,
      name: '数据源',
      nameLocation: 'middle',
      nameGap: 30,
      axisLine: { lineStyle: { color: '#e7e7e7' } },
      axisLabel: { color: '#595959' },
    },
    yAxis: {
      type: 'category',
      data: levelList,
      name: '敏感级别',
      axisLine: { show: false },
      splitLine: { lineStyle: { color: '#f0f0f0' } },
      axisLabel: { color: '#595959' },
    },
    series: [
      {
        type: 'scatter',
        data: bubbleData.value,
        symbolSize: (d: number[]) => Math.sqrt(d[2]) * 18 + 16,
        itemStyle: {
          color: (p: { data: { level: string } }) => levelColors[p.data.level] || '#0052d9',
          opacity: 0.78,
        },
        label: {
          show: true,
          formatter: (p: { data: { count: number } }) => p.data.count,
          color: '#fff',
          fontWeight: 'bold',
          fontSize: 12,
        },
      },
    ],
  })
}

function resizeChart() {
  chart?.resize()
}

watch(() => appStore.collapsed, () => nextTick(resizeChart))

onMounted(async () => {
  const [ls, ts, fs, ps, ds] = await Promise.all([
    getClassifyLabels(),
    getMetadataTables(),
    getTableFields(1),
    getMaskPolicies(),
    getDatasources(),
  ])
  labels.value = ls
  tables.value = ts
  fields.value = fs
  policies.value = ps
  datasources.value = ds
  loading.value = false
  await nextTick()
  initChart()
  window.addEventListener('resize', resizeChart)
})

onUnmounted(() => {
  window.removeEventListener('resize', resizeChart)
  chart?.dispose()
})
</script>

<template>
  <div class="page-container" v-loading="loading">
    <!-- 顶部统计 -->
    <div class="stat-grid">
      <div class="stat-card">
        <div class="stat-icon" style="background: rgba(227, 77, 89, 0.12); color: #e34d59">
          <t-icon name="lock-on" size="22" />
        </div>
        <div class="stat-body">
          <div class="stat-value">{{ stats.total }}</div>
          <div class="stat-title">敏感字段总数</div>
        </div>
      </div>
      <div class="stat-card">
        <div class="stat-icon" style="background: rgba(0, 168, 112, 0.12); color: #00a870">
          <t-icon name="check-circle" size="22" />
        </div>
        <div class="stat-body">
          <div class="stat-value">{{ stats.masked }}</div>
          <div class="stat-title">已脱敏数</div>
        </div>
      </div>
      <div class="stat-card">
        <div class="stat-icon" style="background: rgba(237, 123, 47, 0.12); color: #ed7b2f">
          <t-icon name="error-circle" size="22" />
        </div>
        <div class="stat-body">
          <div class="stat-value">{{ stats.unmasked }}</div>
          <div class="stat-title">未脱敏数</div>
        </div>
      </div>
      <div class="stat-card">
        <div class="stat-icon" style="background: rgba(0, 82, 217, 0.12); color: #0052d9">
          <t-icon name="chart-bubble" size="22" />
        </div>
        <div class="stat-body">
          <div class="stat-value">{{ stats.coverage }}%</div>
          <div class="stat-title">脱敏覆盖率</div>
        </div>
      </div>
    </div>

    <!-- 气泡图 -->
    <div class="page-card">
      <div class="flex-between" style="margin-bottom: 12px">
        <span class="card-label">敏感数据分布地图</span>
        <div class="flex gap-12">
          <t-tag
            v-for="l in labels"
            :key="l.id"
            :color="levelColors[l.level]"
            variant="light"
          >
            {{ l.level }} {{ l.name }} · {{ l.fieldCount.toLocaleString('zh-CN') }}
          </t-tag>
        </div>
      </div>
      <div ref="chartRef" class="chart-box"></div>
    </div>

    <!-- 明细表 -->
    <div class="page-card">
      <div class="card-label" style="margin-bottom: 12px">敏感字段明细</div>
      <t-table
        :data="sensitiveFields"
        row-key="id"
        size="small"
        :pagination="{ show: false }"
        :columns="[
          { colKey: 'tableName', title: '表名', ellipsis: true },
          { colKey: 'fieldName', title: '字段名', width: 150 },
          { colKey: 'dataType', title: '类型', width: 140 },
          { colKey: 'level', title: '敏感级别', width: 120, cell: 'level' },
          { colKey: 'masked', title: '脱敏状态', width: 110, cell: 'masked' },
        ]"
      >
        <template #level="{ row }">
          <t-tag size="small" :color="levelColors[row.level]" variant="light">
            {{ row.level }} {{ levelNames[row.level] }}
          </t-tag>
        </template>
        <template #masked="{ row }">
          <t-tag v-if="row.masked" size="small" theme="success" variant="light">已脱敏</t-tag>
          <t-tag v-else size="small" theme="danger" variant="light">未脱敏</t-tag>
        </template>
      </t-table>
    </div>
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
  display: flex;
  align-items: center;
  gap: 14px;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.04);
}
.stat-icon {
  width: 48px;
  height: 48px;
  border-radius: 10px;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}
.stat-value {
  font-size: 26px;
  font-weight: 600;
  color: var(--text-primary);
  line-height: 1.2;
}
.stat-title {
  font-size: 13px;
  color: var(--text-secondary);
  margin-top: 4px;
}
.card-label {
  font-size: 15px;
  font-weight: 600;
  color: var(--text-primary);
}
.chart-box {
  height: 360px;
}
</style>
