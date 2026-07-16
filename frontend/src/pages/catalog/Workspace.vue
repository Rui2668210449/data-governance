<script setup lang="ts">
import { ref, onMounted, computed } from 'vue'
import { useRouter } from 'vue-router'
import { searchAssets } from '@/api'
import type { AssetCatalog } from '@/types'

const router = useRouter()
const allAssets = ref<AssetCatalog[]>([])
const tabValue = ref('mine')

const myAssets = computed(() => allAssets.value.filter((a) => a.owner === '张三'))
const favAssets = computed(() => allAssets.value.filter((a) => a.favCount > 30))
const recentAssets = computed(() => allAssets.value.slice().sort((a, b) => b.updateTime.localeCompare(a.updateTime)).slice(0, 5))

const stats = computed(() => ({
  mine: myAssets.value.length,
  fav: favAssets.value.length,
  view: 128,
}))

onMounted(async () => {
  allAssets.value = await searchAssets('')
})

function formatCount(n: number) {
  return n >= 10000 ? (n / 10000).toFixed(1) + 'w' : n.toString()
}
</script>

<template>
  <div class="page-container">
    <!-- 个人统计 -->
    <div class="stat-row">
      <div class="page-card stat-card">
        <div class="stat-icon" style="background:#0052d915;color:#0052d9"><t-icon name="server" size="22" /></div>
        <div>
          <div class="stat-num">{{ stats.mine }}</div>
          <div class="stat-label">我负责的资产</div>
        </div>
      </div>
      <div class="page-card stat-card">
        <div class="stat-icon" style="background:#00a87015;color:#00a870"><t-icon name="heart" size="22" /></div>
        <div>
          <div class="stat-num">{{ stats.fav }}</div>
          <div class="stat-label">我收藏的</div>
        </div>
      </div>
      <div class="page-card stat-card">
        <div class="stat-icon" style="background:#ed7b2f15;color:#ed7b2f"><t-icon name="browse" size="22" /></div>
        <div>
          <div class="stat-num">{{ stats.view }}</div>
          <div class="stat-label">本月浏览</div>
        </div>
      </div>
    </div>

    <div class="page-card">
      <t-tabs v-model="tabValue">
        <t-tab-panel value="mine" label="我负责的">
          <div class="asset-grid">
            <div v-for="asset in myAssets" :key="asset.id" class="asset-card" @click="router.push(`/catalog/asset/${asset.id}`)">
              <div class="asset-card-header">
                <t-icon name="server" size="20" style="color:#0052d9" />
                <span class="asset-card-name">{{ asset.name }}</span>
                <t-tag size="small" variant="light">{{ asset.tags.find((t) => ['L1','L2','L3','L4'].includes(t)) }}</t-tag>
              </div>
              <div class="asset-card-desc">{{ asset.description }}</div>
              <div class="asset-card-footer">
                <span><t-icon name="star-filled" size="12" style="color:#ed7b2f" /> {{ asset.rating }}</span>
                <span><t-icon name="browse" size="12" /> {{ formatCount(asset.viewCount) }}</span>
                <span><t-icon name="time" size="12" /> {{ asset.updateTime.split(' ')[0] }}</span>
              </div>
            </div>
          </div>
        </t-tab-panel>
        <t-tab-panel value="fav" label="我收藏的">
          <div class="asset-grid">
            <div v-for="asset in favAssets" :key="asset.id" class="asset-card" @click="router.push(`/catalog/asset/${asset.id}`)">
              <div class="asset-card-header">
                <t-icon name="server" size="20" style="color:#0052d9" />
                <span class="asset-card-name">{{ asset.name }}</span>
                <t-tag size="small" variant="light">{{ asset.tags.find((t) => ['L1','L2','L3','L4'].includes(t)) }}</t-tag>
              </div>
              <div class="asset-card-desc">{{ asset.description }}</div>
              <div class="asset-card-footer">
                <span><t-icon name="star-filled" size="12" style="color:#ed7b2f" /> {{ asset.rating }}</span>
                <span><t-icon name="browse" size="12" /> {{ formatCount(asset.viewCount) }}</span>
                <span><t-icon name="time" size="12" /> {{ asset.updateTime.split(' ')[0] }}</span>
              </div>
            </div>
          </div>
        </t-tab-panel>
        <t-tab-panel value="recent" label="最近浏览">
          <div class="asset-grid">
            <div v-for="asset in recentAssets" :key="asset.id" class="asset-card" @click="router.push(`/catalog/asset/${asset.id}`)">
              <div class="asset-card-header">
                <t-icon name="server" size="20" style="color:#0052d9" />
                <span class="asset-card-name">{{ asset.name }}</span>
                <t-tag size="small" variant="light">{{ asset.tags.find((t) => ['L1','L2','L3','L4'].includes(t)) }}</t-tag>
              </div>
              <div class="asset-card-desc">{{ asset.description }}</div>
              <div class="asset-card-footer">
                <span><t-icon name="star-filled" size="12" style="color:#ed7b2f" /> {{ asset.rating }}</span>
                <span><t-icon name="browse" size="12" /> {{ formatCount(asset.viewCount) }}</span>
                <span><t-icon name="time" size="12" /> {{ asset.updateTime.split(' ')[0] }}</span>
              </div>
            </div>
          </div>
        </t-tab-panel>
      </t-tabs>
    </div>
  </div>
</template>

<style scoped>
.stat-row {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 16px;
  margin-bottom: 16px;
}
.stat-card {
  display: flex;
  align-items: center;
  gap: 16px;
  margin-bottom: 0;
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
.stat-num {
  font-size: 24px;
  font-weight: 600;
  line-height: 1.2;
}
.stat-label {
  font-size: 13px;
  color: var(--text-secondary);
  margin-top: 2px;
}
.asset-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 12px;
  padding-top: 8px;
}
.asset-card {
  background: #fff;
  border-radius: 8px;
  padding: 16px;
  border: 1px solid var(--border-color);
  cursor: pointer;
  transition: all 0.2s;
}
.asset-card:hover {
  border-color: var(--brand-color);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.06);
}
.asset-card-header {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 8px;
}
.asset-card-name {
  font-weight: 600;
  font-size: 14px;
  flex: 1;
  min-width: 0;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}
.asset-card-desc {
  font-size: 12px;
  color: var(--text-secondary);
  line-height: 1.5;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
  margin-bottom: 10px;
}
.asset-card-footer {
  display: flex;
  gap: 12px;
  font-size: 12px;
  color: var(--text-secondary);
}
</style>
