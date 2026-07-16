<script setup lang="ts">
import { ref, onMounted, watch, computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { searchAssets, getAssetTags } from '@/api'
import type { AssetCatalog, AssetTag } from '@/types'

const route = useRoute()
const router = useRouter()

const keyword = ref((route.query.keyword as string) || '')
const assets = ref<AssetCatalog[]>([])
const tags = ref<AssetTag[]>([])
const selectedCategories = ref<string[]>([])
const selectedLevels = ref<string[]>([])
const loading = ref(false)

const categoryMap: Record<string, string[]> = {}
const tagCategories = ref<string[]>([])

function highlight(text: string, kw: string) {
  if (!kw) return text
  const re = new RegExp(`(${kw.replace(/[.*+?^${}()|[\]\\]/g, '\\$&')})`, 'gi')
  return text.replace(re, '<mark>$1</mark>')
}

function formatCount(n: number) {
  return n >= 10000 ? (n / 10000).toFixed(1) + 'w' : n.toString()
}

async function load() {
  loading.value = true
  const [a, t] = await Promise.all([searchAssets(keyword.value), getAssetTags()])
  assets.value = a
  tags.value = t
  categoryMap['重要等级'] = t.filter((x) => x.category === '重要等级').map((x) => x.name)
  categoryMap['业务域'] = t.filter((x) => x.category === '业务域').map((x) => x.name)
  categoryMap['数仓分层'] = t.filter((x) => x.category === '数仓分层').map((x) => x.name)
  categoryMap['资产类型'] = t.filter((x) => x.category === '资产类型').map((x) => x.name)
  tagCategories.value = Object.keys(categoryMap)
  loading.value = false
}

function onSearch() {
  load()
}

function isMatch(asset: AssetCatalog) {
  if (selectedCategories.value.length > 0) {
    if (!asset.tags.some((t) => selectedCategories.value.includes(t))) return false
  }
  if (selectedLevels.value.length > 0) {
    if (!selectedLevels.value.includes(asset.tags.find((t) => ['L1', 'L2', 'L3', 'L4'].includes(t)) || '')) return false
  }
  return true
}

const filteredAssets = computed(() => assets.value.filter(isMatch))

watch(() => route.query.keyword, (v) => {
  if (v) { keyword.value = v as string; load() }
})

onMounted(load)
</script>

<template>
  <div class="page-container">
    <div class="search-hero">
      <h1 class="search-title">数据资产检索</h1>
      <t-input v-model="keyword" placeholder="搜索表名、描述、标签..." size="large" class="search-input" @enter="onSearch" clearable>
        <template #suffix>
          <t-button theme="primary" @click="onSearch">
            <template #icon><t-icon name="search" /></template>
            搜索
          </t-button>
        </template>
      </t-input>
      <div class="search-hint">
        热门标签:
        <t-tag v-for="tag in tags.slice(0, 5)" :key="tag.id" variant="light" size="small" class="hint-tag" @click="keyword = tag.name; onSearch()">
          {{ tag.name }}
        </t-tag>
      </div>
    </div>

    <div class="search-body">
      <aside class="search-filter">
        <div class="page-card">
          <div class="filter-title">标签筛选</div>
          <div v-for="cat in tagCategories" :key="cat" class="filter-group">
            <div class="filter-group-title">{{ cat }}</div>
            <t-checkbox-group v-model="selectedCategories" direction="vertical" size="small">
              <t-checkbox v-for="name in categoryMap[cat]" :key="name" :label="name" :value="name" />
            </t-checkbox-group>
          </div>
          <div class="filter-group">
            <div class="filter-group-title">安全级别</div>
            <t-checkbox-group v-model="selectedLevels" direction="vertical" size="small">
              <t-checkbox label="L1 公开" value="L1" />
              <t-checkbox label="L2 内部" value="L2" />
              <t-checkbox label="L3 敏感" value="L3" />
              <t-checkbox label="L4 高敏" value="L4" />
            </t-checkbox-group>
          </div>
        </div>
      </aside>

      <div class="search-results">
        <div class="result-count">共 {{ filteredAssets.length }} 条结果</div>
        <div class="result-list" v-loading="loading">
          <div v-for="asset in filteredAssets" :key="asset.id" class="asset-card" @click="router.push(`/catalog/asset/${asset.id}`)">
            <div class="asset-header">
              <span class="asset-name" v-html="highlight(asset.name, keyword)"></span>
              <div class="asset-actions">
                <t-tag v-for="t in asset.tags.slice(0, 3)" :key="t" size="small" variant="light">{{ t }}</t-tag>
                <t-icon name="star-filled" size="14" style="color:#ed7b2f" />
                <span class="asset-rating">{{ asset.rating }}</span>
              </div>
            </div>
            <div class="asset-desc" v-html="highlight(asset.description, keyword)"></div>
            <div class="asset-meta">
              <span class="meta-item"><t-icon name="folder" size="14" /> {{ asset.catalogPath }}</span>
              <span class="meta-item"><t-icon name="browse" size="14" /> {{ formatCount(asset.viewCount) }}</span>
              <span class="meta-item"><t-icon name="star" size="14" /> {{ asset.favCount }}</span>
              <span class="meta-item"><t-icon name="time" size="14" /> {{ asset.updateTime }}</span>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
.search-hero {
  text-align: center;
  padding: 32px 0 24px;
}
.search-title {
  font-size: 28px;
  font-weight: 600;
  margin: 0 0 20px;
  color: var(--text-primary);
}
.search-input {
  width: 600px;
  margin: 0 auto;
}
.search-input :deep(.t-input__wrap) {
  height: 48px;
}
.search-hint {
  margin-top: 16px;
  color: var(--text-secondary);
  font-size: 13px;
}
.hint-tag {
  margin: 0 4px;
  cursor: pointer;
}
.search-body {
  display: flex;
  gap: 16px;
}
.search-filter {
  width: 220px;
  flex-shrink: 0;
}
.search-filter .page-card {
  margin-bottom: 0;
}
.filter-title {
  font-size: 15px;
  font-weight: 600;
  margin-bottom: 16px;
}
.filter-group {
  margin-bottom: 16px;
}
.filter-group-title {
  font-size: 13px;
  font-weight: 500;
  color: var(--text-secondary);
  margin-bottom: 8px;
}
.search-results {
  flex: 1;
  min-width: 0;
}
.result-count {
  font-size: 13px;
  color: var(--text-secondary);
  margin-bottom: 12px;
}
.result-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}
.asset-card {
  background: #fff;
  border-radius: 6px;
  padding: 16px;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.04);
  cursor: pointer;
  transition: all 0.2s;
}
.asset-card:hover {
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.08);
  border-color: var(--brand-color);
}
.asset-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 8px;
}
.asset-name {
  font-size: 15px;
  font-weight: 600;
  color: var(--brand-color);
}
.asset-name mark {
  background: #fff3cd;
  color: inherit;
  padding: 0 2px;
  border-radius: 2px;
}
.asset-actions {
  display: flex;
  align-items: center;
  gap: 8px;
}
.asset-rating {
  font-size: 13px;
  color: var(--text-secondary);
}
.asset-desc {
  font-size: 13px;
  color: var(--text-secondary);
  line-height: 1.6;
  margin-bottom: 10px;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}
.asset-desc mark {
  background: #fff3cd;
  padding: 0 2px;
  border-radius: 2px;
}
.asset-meta {
  display: flex;
  gap: 16px;
}
.meta-item {
  display: flex;
  align-items: center;
  gap: 4px;
  font-size: 12px;
  color: var(--text-secondary);
}
</style>
