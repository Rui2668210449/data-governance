<script setup lang="ts">
import { useRoute, useRouter } from 'vue-router'
import { useAppStore } from '@/store/app'

const route = useRoute()
const router = useRouter()
const appStore = useAppStore()

const menus = [
  {
    title: '工作台',
    icon: 'dashboard',
    path: '/dashboard',
  },
  {
    title: '元数据管理',
    icon: 'server',
    children: [
      { title: '资产全景图', path: '/metadata/overview' },
      { title: '数据源管理', path: '/metadata/datasource' },
      { title: '血缘图谱', path: '/metadata/lineage' },
    ],
  },
  {
    title: '数据标准',
    icon: 'edit-1',
    children: [
      { title: '标准树', path: '/standard/tree' },
      { title: '标准映射', path: '/standard/mapping' },
      { title: '落标稽核', path: '/standard/audit' },
    ],
  },
  {
    title: '数据质量',
    icon: 'check-circle',
    children: [
      { title: '质量规则', path: '/quality/rule' },
      { title: '质量看板', path: '/quality/dashboard' },
      { title: '质量工单', path: '/quality/ticket' },
    ],
  },
  {
    title: '数据安全',
    icon: 'lock-on',
    children: [
      { title: '分级标签', path: '/security/classify' },
      { title: '敏感数据地图', path: '/security/sensitive-map' },
      { title: '脱敏策略', path: '/security/mask' },
      { title: '审计日志', path: '/security/audit' },
    ],
  },
  {
    title: '资产目录',
    icon: 'folder',
    children: [
      { title: '资产检索', path: '/catalog/search' },
      { title: '标签管理', path: '/catalog/tag' },
      { title: '个人工作台', path: '/catalog/workspace' },
    ],
  },
  {
    title: '生命周期',
    icon: 'time',
    children: [
      { title: '生命周期策略', path: '/lifecycle/policy' },
      { title: '生命周期仪表盘', path: '/lifecycle/dashboard' },
    ],
  },
  {
    title: '治理工作流',
    icon: 'root-list',
    children: [
      { title: '治理看板', path: '/workflow/board' },
      { title: '我的待办', path: '/workflow/todo' },
    ],
  },
]

const expanded = ref<string[]>(['元数据管理'])

function isActive(path: string) {
  return route.path === path || route.path.startsWith(path + '/')
}

function go(path: string) {
  router.push(path)
}
</script>

<script lang="ts">
import { ref } from 'vue'
</script>

<template>
  <div class="sidebar">
    <div class="sidebar-logo">
      <t-icon name="data-base" size="28" style="color: #fff" />
      <span v-show="!appStore.collapsed" class="logo-text">数据治理平台</span>
    </div>
    <t-menu
      :collapsed="appStore.collapsed"
      :value="route.path"
      :expanded="expanded"
      @expand="(val: string[]) => (expanded = val)"
      theme="dark"
      style="background: transparent"
    >
      <template v-for="menu in menus" :key="menu.title">
        <t-submenu v-if="menu.children" :value="menu.title">
          <template #icon><t-icon :name="menu.icon" /></template>
          <template #title>{{ menu.title }}</template>
          <t-menu-item
            v-for="child in menu.children"
            :key="child.path"
            :value="child.path"
            @click="go(child.path)"
          >
            {{ child.title }}
          </t-menu-item>
        </t-submenu>
        <t-menu-item v-else :value="menu.path" @click="go(menu.path)">
          <template #icon><t-icon :name="menu.icon" /></template>
          {{ menu.title }}
        </t-menu-item>
      </template>
    </t-menu>
  </div>
</template>

<style scoped>
.sidebar {
  height: 100%;
  background: linear-gradient(180deg, #001b4e 0%, #002a6e 100%);
  display: flex;
  flex-direction: column;
}
.sidebar-logo {
  height: 56px;
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 0 18px;
  border-bottom: 1px solid rgba(255, 255, 255, 0.08);
  flex-shrink: 0;
}
.logo-text {
  color: #fff;
  font-size: 16px;
  font-weight: 600;
  white-space: nowrap;
}
.sidebar :deep(.t-menu) {
  flex: 1;
  overflow-y: auto;
  overflow-x: hidden;
}
.sidebar :deep(.t-menu__scroll) {
  background: transparent;
}
</style>
