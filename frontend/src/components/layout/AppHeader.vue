<script setup lang="ts">
import { computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useAppStore } from '@/store/app'
import { MessagePlugin } from 'tdesign-vue-next'

const route = useRoute()
const router = useRouter()
const appStore = useAppStore()

const currentTitle = computed(() => (route.meta.title as string) || '数据治理平台')

const breadcrumb = computed(() => {
  const path = route.path
  const segs: string[] = []
  if (path.startsWith('/metadata')) segs.push('元数据管理')
  else if (path.startsWith('/standard')) segs.push('数据标准')
  else if (path.startsWith('/quality')) segs.push('数据质量')
  else if (path.startsWith('/security')) segs.push('数据安全')
  else if (path.startsWith('/catalog')) segs.push('资产目录')
  else if (path.startsWith('/lifecycle')) segs.push('生命周期')
  else if (path.startsWith('/workflow')) segs.push('治理工作流')
  if (route.meta.title) segs.push(route.meta.title as string)
  return segs
})

const searchValue = ref('')

function onSearch() {
  if (searchValue.value.trim()) {
    router.push({ path: '/catalog/search', query: { keyword: searchValue.value } })
  }
}

function logout() {
  localStorage.removeItem('gov_token')
  MessagePlugin.success('已退出登录')
  router.push('/login')
}
</script>

<script lang="ts">
import { ref } from 'vue'
</script>

<template>
  <header class="app-header">
    <div class="header-left">
      <t-button theme="default" variant="text" shape="square" @click="appStore.toggleCollapsed">
        <template #icon><t-icon name="view-list" /></template>
      </t-button>
      <t-breadcrumb class="header-crumb">
        <t-breadcrumb-item v-for="(item, i) in breadcrumb" :key="i">{{ item }}</t-breadcrumb-item>
      </t-breadcrumb>
    </div>
    <div class="header-right">
      <t-input
        v-model="searchValue"
        placeholder="搜索数据资产..."
        clearable
        style="width: 260px"
        @enter="onSearch"
      >
        <template #prefix-icon><t-icon name="search" /></template>
      </t-input>
      <t-tooltip content="消息通知">
        <t-badge :count="appStore.collapsed ? 0 : 5" size="small">
          <t-button theme="default" variant="text" shape="square">
            <template #icon><t-icon name="notification" /></template>
          </t-button>
        </t-badge>
      </t-tooltip>
      <t-dropdown :min-column-width="140">
        <div class="user-info">
          <t-avatar size="32px" style="background: var(--brand-color)">
            {{ appStore.userName.charAt(0) }}
          </t-avatar>
          <span class="user-name">{{ appStore.userName }}</span>
          <t-icon name="chevron-down" size="16" />
        </div>
        <t-dropdown-menu>
          <t-dropdown-item @click="router.push('/catalog/workspace')">
            <template #prefix><t-icon name="user-circle" /></template>
            个人中心
          </t-dropdown-item>
          <t-dropdown-item @click="router.push('/workflow/todo')">
            <template #prefix><t-icon name="check-rectangle" /></template>
            我的待办
          </t-dropdown-item>
          <t-dropdown-item divider @click="logout">
            <template #prefix><t-icon name="poweroff" /></template>
            退出登录
          </t-dropdown-item>
        </t-dropdown-menu>
      </t-dropdown>
    </div>
  </header>
</template>

<style scoped>
.app-header {
  height: 56px;
  background: #fff;
  border-bottom: 1px solid var(--border-color);
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 16px;
  flex-shrink: 0;
}
.header-left {
  display: flex;
  align-items: center;
  gap: 12px;
}
.header-crumb {
  font-size: 14px;
}
.header-right {
  display: flex;
  align-items: center;
  gap: 12px;
}
.user-info {
  display: flex;
  align-items: center;
  gap: 8px;
  cursor: pointer;
  padding: 4px 8px;
  border-radius: 4px;
  transition: background 0.2s;
}
.user-info:hover {
  background: var(--bg-page);
}
.user-name {
  font-size: 14px;
}
</style>
