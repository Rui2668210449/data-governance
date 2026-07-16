<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { getAssetDetail, getTableFields, getQualityRules, getMaskPolicies } from '@/api'
import type { AssetCatalog, MetadataField, QualityRule, MaskPolicy } from '@/types'

const route = useRoute()
const router = useRouter()
const id = Number(route.params.id)

const asset = ref<AssetCatalog | null>(null)
const fields = ref<MetadataField[]>([])
const rules = ref<QualityRule[]>([])
const masks = ref<MaskPolicy[]>([])
const isFav = ref(false)
const rating = ref(5)
const loading = ref(true)
const tabValue = ref('overview')

const comments = ref([
  { user: '张三', time: '2026-07-15 14:30', content: '这个表的数据质量很高，推荐作为核心参考', rating: 5 },
  { user: '李四', time: '2026-07-10 09:20', content: '建议增加更多业务注释', rating: 4 },
  { user: '王五', time: '2026-07-08 16:45', content: '常用表，查询效率不错', rating: 5 },
])

const levelColor: Record<string, string> = { L1: '#8c8c8c', L2: '#0052d9', L3: '#ed7b2f', L4: '#e34d59' }

onMounted(async () => {
  const [a, f, r, m] = await Promise.all([
    getAssetDetail(id),
    getTableFields(id),
    getQualityRules(),
    getMaskPolicies(),
  ])
  asset.value = a
  fields.value = f
  rules.value = r.filter((rule) => rule.targetTable.includes(a.name))
  masks.value = m.filter((mask) => mask.tableId === id)
  rating.value = Math.round(a.rating)
  loading.value = false
})

function toggleFav() {
  isFav.value = !isFav.value
}
</script>

<template>
  <div class="page-container" v-loading="loading">
    <template v-if="asset">
      <div class="page-card">
        <div class="flex-between">
          <div class="asset-title-bar">
            <h2 class="page-title" style="margin-bottom:4px">{{ asset.name }}</h2>
            <div class="flex-center gap-8">
              <t-tag v-for="t in asset.tags" :key="t" size="small" variant="light">{{ t }}</t-tag>
              <t-tag size="small" :color="levelColor[asset.tags.find((tag) => ['L1','L2','L3','L4'].includes(tag)) || 'L1']" variant="light">
                {{ asset.tags.find((tag) => ['L1','L2','L3','L4'].includes(tag)) }}
              </t-tag>
            </div>
          </div>
          <div class="flex-center gap-12">
            <t-rate v-model="rating" size="20" />
            <t-button theme="default" variant="outline" @click="toggleFav">
              <template #icon><t-icon :name="isFav ? 'heart-filled' : 'heart'" /></template>
              {{ isFav ? '已收藏' : '收藏' }}
            </t-button>
          </div>
        </div>
      </div>

      <t-tabs v-model="tabValue">
        <t-tab-panel value="overview" label="概览">
          <div class="page-card">
            <t-descriptions :column="3">
              <t-descriptions-item label="资产名称">{{ asset.name }}</t-descriptions-item>
              <t-descriptions-item label="资产类型">{{ asset.type === 'TABLE' ? '数据表' : asset.type }}</t-descriptions-item>
              <t-descriptions-item label="目录路径">{{ asset.catalogPath }}</t-descriptions-item>
              <t-descriptions-item label="负责人">{{ asset.owner }}</t-descriptions-item>
              <t-descriptions-item label="评分">{{ asset.rating }} / 5.0</t-descriptions-item>
              <t-descriptions-item label="浏览数">{{ asset.viewCount }}</t-descriptions-item>
              <t-descriptions-item label="收藏数">{{ asset.favCount }}</t-descriptions-item>
              <t-descriptions-item label="更新时间">{{ asset.updateTime }}</t-descriptions-item>
              <t-descriptions-item label="描述">{{ asset.description }}</t-descriptions-item>
            </t-descriptions>
          </div>
          <div class="page-card">
            <div class="page-title" style="font-size:15px;margin-bottom:12px">字段信息</div>
            <t-table :data="fields" row-key="id" size="small" :columns="[
              { colKey: 'fieldName', title: '字段名', width: 140 },
              { colKey: 'dataType', title: '数据类型', width: 120 },
              { colKey: 'comment', title: '注释', ellipsis: true },
              { colKey: 'businessMeaning', title: '业务含义', ellipsis: true },
              { colKey: 'isPrimary', title: '主键', width: 80, cell: 'primary' },
              { colKey: 'isSensitive', title: '敏感', width: 80, cell: 'sensitive' },
            ]">
              <template #primary="{ row }">
                <t-tag v-if="row.isPrimary" theme="primary" size="small">主键</t-tag>
              </template>
              <template #sensitive="{ row }">
                <t-tag v-if="row.isSensitive" theme="danger" size="small">敏感</t-tag>
              </template>
            </t-table>
          </div>
        </t-tab-panel>

        <t-tab-panel value="lineage" label="血缘">
          <div class="page-card">
            <p>该资产的上下游血缘关系可通过血缘图谱查看。</p>
            <t-button theme="primary" @click="router.push('/metadata/lineage')">查看完整血缘图谱</t-button>
          </div>
        </t-tab-panel>

        <t-tab-panel value="quality" label="质量">
          <div class="page-card">
            <div class="flex-between">
              <div>
                <div style="font-size:36px;font-weight:700;color:#0052d9">{{ rules.length > 0 ? rules[0].lastScore : 96.5 }}</div>
                <div style="color:var(--text-secondary);font-size:13px">质量评分</div>
              </div>
              <div class="flex-center gap-12">
                <div style="text-align:center"><div style="font-size:20px;font-weight:600">{{ rules.length }}</div><div style="font-size:12px;color:var(--text-secondary)">规则数</div></div>
                <div style="text-align:center"><div style="font-size:20px;font-weight:600;color:#00a870">{{ rules.filter((r) => r.lastScore >= 95).length }}</div><div style="font-size:12px;color:var(--text-secondary)">优秀</div></div>
                <div style="text-align:center"><div style="font-size:20px;font-weight:600;color:#ed7b2f">{{ rules.filter((r) => r.lastScore < 85).length }}</div><div style="font-size:12px;color:var(--text-secondary)">需关注</div></div>
              </div>
            </div>
          </div>
          <div class="page-card">
            <div class="page-title" style="font-size:15px;margin-bottom:12px">关联质量规则</div>
            <t-table :data="rules" row-key="id" size="small" :columns="[
              { colKey: 'name', title: '规则名称', ellipsis: true },
              { colKey: 'dimension', title: '维度', width: 100 },
              { colKey: 'lastScore', title: '最近评分', width: 100, cell: 'score' },
              { colKey: 'enabled', title: '状态', width: 80, cell: 'status' },
            ]">
              <template #score="{ row }">
                <t-tag :theme="row.lastScore >= 95 ? 'success' : row.lastScore >= 85 ? 'warning' : 'danger'" size="small">{{ row.lastScore }}</t-tag>
              </template>
              <template #status="{ row }">
                <t-tag :theme="row.enabled ? 'success' : 'default'" variant="light" size="small">{{ row.enabled ? '启用' : '停用' }}</t-tag>
              </template>
            </t-table>
          </div>
        </t-tab-panel>

        <t-tab-panel value="security" label="安全">
          <div class="page-card">
            <t-descriptions :column="2">
              <t-descriptions-item label="安全级别">
                <t-tag :color="levelColor[asset.tags.find((tag) => ['L1','L2','L3','L4'].includes(tag)) || 'L1']" variant="light">
                  {{ asset.tags.find((tag) => ['L1','L2','L3','L4'].includes(tag)) }}
                </t-tag>
              </t-descriptions-item>
              <t-descriptions-item label="脱敏字段">{{ masks.length }} 个</t-descriptions-item>
            </t-descriptions>
          </div>
          <div class="page-card">
            <div class="page-title" style="font-size:15px;margin-bottom:12px">脱敏策略</div>
            <t-table :data="masks" row-key="id" size="small" :columns="[
              { colKey: 'fieldName', title: '字段', width: 140 },
              { colKey: 'maskType', title: '脱敏类型', width: 100, cell: 'type' },
              { colKey: 'expression', title: '表达式', ellipsis: true },
              { colKey: 'preview', title: '预览', width: 180 },
            ]">
              <template #type="{ row }">
                <t-tag size="small" :theme="row.maskType === 'MASK' ? 'primary' : row.maskType === 'REPLACE' ? 'success' : row.maskType === 'ENCRYPT' ? 'warning' : 'danger'" variant="light">
                  {{ row.maskType }}
                </t-tag>
              </template>
            </t-table>
          </div>
        </t-tab-panel>

        <t-tab-panel value="review" label="评价">
          <div class="page-card">
            <div class="page-title" style="font-size:15px;margin-bottom:16px">用户评价</div>
            <div class="review-list">
              <div v-for="c in comments" :key="c.user + c.time" class="review-item">
                <div class="flex-between" style="margin-bottom:8px">
                  <div class="flex-center gap-8">
                    <t-avatar size="32" style="background:#0052d9">{{ c.user.charAt(0) }}</t-avatar>
                    <div>
                      <div style="font-weight:500">{{ c.user }}</div>
                      <div style="font-size:12px;color:var(--text-secondary)">{{ c.time }}</div>
                    </div>
                  </div>
                  <t-rate :model-value="c.rating" size="16" disabled />
                </div>
                <div style="font-size:13px;color:var(--text-secondary)">{{ c.content }}</div>
              </div>
            </div>
          </div>
        </t-tab-panel>
      </t-tabs>
    </template>
  </div>
</template>

<style scoped>
.asset-title-bar {
  flex: 1;
}
.review-list {
  display: flex;
  flex-direction: column;
  gap: 16px;
}
.review-item {
  padding: 12px 0;
  border-bottom: 1px solid var(--border-color);
}
.review-item:last-child {
  border-bottom: none;
}
</style>
